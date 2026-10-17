package game;

import effects.*;
import logic.AlgorithmStep;
import logic.Dijkstra;
import logic.WeightGenerator;
import model.Edge;
import model.Graph;
import model.Node;
import ui.*;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/** Connects clicks -> rules -> screen. Every UI update and effect is triggered from here. */
public class GameController {
    private static final String START = "lecture";
    private static final String END = "home";

    private final MainFrame frame;
    private final WeightGenerator generator;

    private Graph graph;
    private GameSession session;
    private final List<String> route = new ArrayList<>();
    private boolean busy;   // true while an effect/animation is playing and input should be ignored

    public GameController(MainFrame frame, long seed) {
        this.frame = frame;
        this.generator = new WeightGenerator(seed);

        frame.getMapPanel().setNodeClickListener(this::onNodeClicked);
        frame.getPromptBar().onUndo(this::onUndo);
        frame.getPromptBar().onSubmit(this::onSubmitRoute);
        frame.getPromptBar().onPlayAgain(() -> newGame("New roads, new route! Same smell. Let's go again."));
    }

    // ------------------------------------------------------------------ setup

    public void newGame(String message) {
        graph = Graph.createDefault();
        Dijkstra dijkstra = generator.generate(graph, START, END);
        session = new GameSession(graph, dijkstra);
        route.clear();
        busy = false;

        MapPanel map = frame.getMapPanel();
        map.setGraph(graph);
        map.setRoute(List.of());
        map.setHotEdges(List.of());
        map.setClean(false);
        map.setWalking(false);
        map.setStinkLevel(0);
        Node s = graph.getNode(START);
        map.setStudentAt(s.getX(), s.getY());

        frame.getStinkometer().reset();
        frame.getTablePanel().reset();
        refreshTable();
        frame.getExplanationPanel().showIntro();

        PromptBar prompt = frame.getPromptBar();
        prompt.showPathButtons(false);
        prompt.showPlayAgain(false);
        prompt.setFeedback(message, Theme.INK);
        prompt.setQuestion("Click the unvisited node with the smallest distance in the table. (Start: Lecture is 0.)");
    }

    private void refreshTable() {
        AlgorithmStep step = session.getCurrentStep();
        frame.getTablePanel().update(step, graph);
        frame.getMapPanel().setDistances(step.getDistances());
        frame.getMapPanel().setVisited(step.getVisited());
    }

    // ------------------------------------------------------------------ input

    private void onNodeClicked(String id) {
        if (busy) return;
        switch (session.getState()) {
            case CHOOSING -> handlePick(id);
            case FINAL_PATH -> handleRouteClick(id);
            default -> { }
        }
    }

    private void handlePick(String id) {
        PromptBar prompt = frame.getPromptBar();
        Node chosen = graph.getNode(id);
        if (session.getCurrentStep().getVisited().contains(id)) {
            prompt.setFeedback("You've already visited " + chosen.getName() + ". Pick an unvisited node.", Theme.INK);
            return;
        }

        ChoiceResult result = session.submitChoice(chosen);
        MapPanel map = frame.getMapPanel();
        Point p = nodePoint(id);

        if (result.isCorrect()) {
            frame.getOverlay().add(new BubbleEffect(p.x, p.y));
            map.flash(id, Theme.FLASH_GOOD);
            prompt.setFeedback(result.getExplanation(), Theme.GOOD);
            refreshTable();
            frame.getExplanationPanel().append(session.describeLastUpdate());

            List<Edge> hot = new ArrayList<>();
            for (Edge e : graph.getEdges(id)) {
                String other = graph.getOtherNode(e, id).getId();
                if (!session.getCurrentStep().getVisited().contains(other)) hot.add(e);
            }
            map.setHotEdges(hot);

            if (session.getState() == GameState.FINAL_PATH) startFinalPath();
            else prompt.setQuestion("Which unvisited node should Dijkstra visit next? (Orange roads were just checked.)");
        } else {
            map.flash(id, Theme.FLASH_BAD);
            handleStrike(result, p);
        }
    }

    private void startFinalPath() {
        route.clear();
        route.add(START);
        frame.getMapPanel().setHotEdges(List.of());
        frame.getMapPanel().setRoute(route);
        frame.getPromptBar().showPathButtons(true);
        frame.getPromptBar().setQuestion("All nodes visited! Now trace the shortest route from Lecture to Home: click the nodes "
                + "in order, then press Submit route. Tip: follow the 'previous node' column back from Home.");
        frame.getExplanationPanel().append("All nodes visited. Now build the route!\n");
    }

    private void handleRouteClick(String id) {
        PromptBar prompt = frame.getPromptBar();
        String last = route.get(route.size() - 1);
        if (id.equals(last)) return;
        if (route.contains(id)) {
            prompt.setFeedback("That node is already on your route. Use 'Undo last step' to go back.", Theme.INK);
            return;
        }
        if (graph.getEdge(last, id) == null) {
            prompt.setFeedback("There's no road from " + graph.getNode(last).getName()
                    + " to " + graph.getNode(id).getName() + ".", Theme.INK);
            return;
        }
        route.add(id);
        frame.getMapPanel().setRoute(route);
        prompt.setFeedback("Route so far: " + routeText() + "  (" + session.pathCost(route) + " min)", Theme.INK);
    }

    private void onUndo() {
        if (busy || session.getState() != GameState.FINAL_PATH || route.size() <= 1) return;
        route.remove(route.size() - 1);
        frame.getMapPanel().setRoute(route);
        frame.getPromptBar().setFeedback("Route so far: " + routeText(), Theme.INK);
    }

    private void onSubmitRoute() {
        if (busy || session.getState() != GameState.FINAL_PATH) return;
        ChoiceResult result = session.submitFinalPath(route);
        PromptBar prompt = frame.getPromptBar();

        if (result.isCorrect()) {
            busy = true;
            prompt.showPathButtons(false);
            prompt.setFeedback(result.getExplanation(), Theme.GOOD);
            prompt.setQuestion("Watch the student scurry home...");
            startWalk();
        } else {
            Point p = nodePoint(route.get(route.size() - 1));
            route.clear();
            route.add(START);
            frame.getMapPanel().setRoute(route);
            handleStrike(result, p);
        }
    }

    // ------------------------------------------------------------------ outcomes

    /** Shared wrong-answer handling for node picks and route submissions. */
    private void handleStrike(ChoiceResult result, Point effectPoint) {
        PromptBar prompt = frame.getPromptBar();
        prompt.setFeedback(result.getExplanation(), Theme.BAD);
        frame.getOverlay().add(new StinkCloudEffect(effectPoint.x, effectPoint.y));
        frame.getStinkometer().setLevel(result.getStrikes());
        frame.getMapPanel().setStinkLevel(result.getStrikes());
        frame.getExplanationPanel().append("Strike " + result.getStrikes() + "/" + GameSession.MAX_STRIKES + "!\n");

        if (result.isGameOver()) {
            busy = true;
            prompt.showPathButtons(false);
            frame.getStinkometer().setOverflow(true);
            Point meter = frame.toOverlay(frame.getStinkometer(),
                    new Point(frame.getStinkometer().getWidth() / 2, 30));
            frame.getOverlay().add(new OverflowEffect(frame, meter,
                    () -> newGame("Stink overflow! The roads have changed, so you'll need to work it out again.")));
        }
    }

    private void startWalk() {
        List<String> path = new ArrayList<>(route);
        List<Point2D.Double> points = new ArrayList<>();
        for (String id : path) {
            Node n = graph.getNode(id);
            points.add(new Point2D.Double(n.getX(), n.getY()));
        }
        MapPanel map = frame.getMapPanel();
        map.setWalking(true);

        frame.getOverlay().add(new WalkAnimator(points, 150,
                pos -> map.setStudentAt(pos.x, pos.y),
                i -> {
                    Point sp = nodePoint(path.get(i));
                    frame.getOverlay().add(new BubbleEffect(sp.x, sp.y));
                },
                () -> {
                    map.setWalking(false);
                    map.setClean(true);
                    session.finishWalk();
                    frame.getStinkometer().reset();
                    frame.getOverlay().add(new VictoryEffect(frame.getOverlay().getWidth(), frame.getOverlay().getHeight()));
                    frame.getPromptBar().setQuestion("You're clean! Want another go with a brand new set of roads?");
                    frame.getPromptBar().showPlayAgain(true);
                    frame.getExplanationPanel().append("Home! Shower time.\n");
                }));
    }

    // ------------------------------------------------------------------ helpers

    private Point nodePoint(String id) {
        Node n = graph.getNode(id);
        Point inMap = frame.getMapPanel().toScreenPoint(n.getX(), n.getY());
        return frame.toOverlay(frame.getMapPanel(), inMap);
    }

    private String routeText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < route.size(); i++) {
            if (i > 0) sb.append(" \u2192 ");
            sb.append(graph.getNode(route.get(i)).getName());
        }
        return sb.toString();
    }
}
