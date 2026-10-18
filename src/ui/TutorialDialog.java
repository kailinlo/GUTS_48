package ui;

import model.Edge;
import model.Graph;
import model.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple pop-up that teaches Dijkstra's algorithm with the smallest possible example:
 * four nodes (A, B, C, D) and plain numbers on the lines.
 *
 * Open it with:   new TutorialDialog(frame).setVisible(true);
 * Or preview it:  java -cp out ui.TutorialDialog
 */
public class TutorialDialog extends JDialog {

    public TutorialDialog(Frame owner) {
        super(owner, true);
        setUndecorated(true);

        Content content = new Content(this::dispose);
        setContentPane(content);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1020, screen.width - 40), Math.min(720, screen.height - 60));
        setResizable(false);
        setLocationRelativeTo(owner);

        bind(KeyEvent.VK_RIGHT, content::next);
        bind(KeyEvent.VK_SPACE, content::next);
        bind(KeyEvent.VK_ENTER, content::next);
        bind(KeyEvent.VK_LEFT, content::previous);
        bind(KeyEvent.VK_ESCAPE, this::dispose);
    }

    private void bind(int key, Runnable action) {
        getRootPane().registerKeyboardAction(e -> action.run(),
                KeyStroke.getKeyStroke(key, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /** Standalone preview: java -cp out ui.TutorialDialog */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TutorialDialog(null).setVisible(true);
            System.exit(0);
        });
    }

    // =====================================================================================
    //  The example: A -> D with four nodes
    //
    //        B
    //      4/ \5          Start = A, end = D.
    //     A  2|  D        Best route: A -> C -> D (cost 5).
    //      1\ /4
    //        C
    // =====================================================================================

    private static final int INF = Integer.MAX_VALUE;
    private static final String GREEN = "#198237";
    private static final String BLUE = "#2a6fc4";

    static Graph exampleGraph() {
        Graph g = new Graph();
        g.addNode(new Node("a", "A", 130, 250));
        g.addNode(new Node("b", "B", 400, 110));
        g.addNode(new Node("c", "C", 400, 390));
        g.addNode(new Node("d", "D", 670, 250));
        g.addEdge("a", "b", 4);
        g.addEdge("a", "c", 1);
        g.addEdge("b", "c", 2);
        g.addEdge("b", "d", 5);
        g.addEdge("c", "d", 4);
        return g;
    }

    private enum Phase {
        BASICS("BASICS", new Color(90, 100, 130)),
        PICK("1. PICK", Theme.GOOD),
        CHECK("2. CHECK", new Color(205, 120, 10)),
        TRACE("TRACE BACK", new Color(40, 110, 200)),
        PLAY("YOUR TURN", Theme.INK);

        final String label;
        final Color colour;

        Phase(String label, Color colour) { this.label = label; this.colour = colour; }
    }

    /** Everything needed to draw one moment of the demo. */
    private static final class Slide {
        Phase phase;
        String title;
        String html;
        Map<String, Integer> dist;
        Map<String, String> prev;
        Set<String> visited = new LinkedHashSet<>();
        String current;                              // node being visited (ringed)
        List<String[]> hot = new ArrayList<>();      // lines highlighted orange
        String[] examined;                           // the one line being checked
        String focusRow;                             // table row outlined
        String changedRow;                           // table row that just changed (yellow)
        int emphasisCol;                             // 2 = previous column
        List<String> route = new ArrayList<>();      // blue route
    }

    private static String d(int v) { return v == INF ? "\u221e" : String.valueOf(v); }

    private static String c(String hex, String s) { return "<font color='" + hex + "'>" + s + "</font>"; }

    private static Slide slide(Phase phase, String title, String html,
                               Map<String, Integer> dist, Map<String, String> prev, Set<String> visited) {
        Slide s = new Slide();
        s.phase = phase;
        s.title = title;
        s.html = html;
        s.dist = new LinkedHashMap<>(dist);
        s.prev = new LinkedHashMap<>(prev);
        s.visited = new LinkedHashSet<>(visited);
        return s;
    }

    static List<Slide> buildSlides(Graph g) {
        List<Slide> out = new ArrayList<>();
        final String start = "a", end = "d";
        int total = g.getNodes().size();

        Map<String, Integer> dist = new LinkedHashMap<>();
        Map<String, String> prev = new LinkedHashMap<>();
        Set<String> visited = new LinkedHashSet<>();
        for (Node n : g.getNodes()) { dist.put(n.getId(), INF); prev.put(n.getId(), null); }
        dist.put(start, 0);

        // ---------------------------------------------------------------- basics
        out.add(slide(Phase.BASICS, "The goal",
                "Find the <b>cheapest way from A to D</b>. The number on each line is its cost.<br><br>"
              + "The table is our notebook:<br>"
              + "<b>DISTANCE</b> = cheapest cost found so far from A. <b>\u221e</b> means 'not found yet'.<br>"
              + "<b>PREVIOUS</b> = the node we came from to get there.",
                dist, prev, visited));

        out.add(slide(Phase.BASICS, "Two moves, repeated",
                "<b>1. PICK</b> the unvisited node with the <b>smallest distance</b>. It turns green.<br><br>"
              + "<b>2. CHECK</b> each unvisited neighbour:<br>"
              + "new distance = picked distance + line cost.<br>"
              + "If it is <b>smaller</b> than the table, update the table. If not, leave it.",
                dist, prev, visited));

        // ---------------------------------------------------------------- the algorithm
        while (visited.size() < total) {
            // ----- PICK
            String next = null;
            int best = INF;
            StringBuilder list = new StringBuilder();
            for (Node n : g.getNodes()) {
                if (visited.contains(n.getId())) continue;
                if (list.length() > 0) list.append(", ");
                list.append(n.getName()).append(" = ").append(d(dist.get(n.getId())));
                if (dist.get(n.getId()) < best) { best = dist.get(n.getId()); next = n.getId(); }
            }
            String name = g.getNode(next).getName();
            visited.add(next);

            List<String[]> hot = new ArrayList<>();
            for (Edge e : g.getEdges(next)) {
                Node o = g.getOtherNode(e, next);
                if (!visited.contains(o.getId())) hot.add(new String[] {next, o.getId()});
            }

            String pick = "Unvisited: " + list + ".<br>"
                    + "The smallest is <b>" + name + " (" + best + ")</b>, so visit " + name + ". It turns green.<br>"
                    + c(BLUE, "<b>In the game:</b> you click " + name + ".");
            if (hot.isEmpty()) pick += "<br><br>No unvisited neighbours, so nothing to check. <b>Everything is green: done!</b>";
            Slide s = slide(Phase.PICK, "Pick " + name, pick, dist, prev, visited);
            s.current = next;
            s.focusRow = next;
            s.hot = hot;
            out.add(s);

            // ----- CHECK
            for (Edge e : g.getEdges(next)) {
                Node o = g.getOtherNode(e, next);
                if (visited.contains(o.getId())) continue;
                int nd = best + e.getWeight();
                int old = dist.get(o.getId());
                boolean better = nd < old;
                if (better) { dist.put(o.getId(), nd); prev.put(o.getId(), next); }

                String t = "New distance = " + best + " + " + e.getWeight() + " = <b>" + nd + "</b><br>"
                        + "The table says " + o.getName() + " = " + d(old) + (old == INF ? " (not found yet)" : "") + "<br><br>";
                if (better) {
                    t += c(GREEN, "<b>" + nd + " is smaller. Update:</b>") + " " + o.getName() + " = " + nd
                       + ", previous = " + name + ".";
                } else {
                    t += c(BLUE, "<b>" + nd + " is not smaller than " + old + ". Leave it.</b>");
                }
                s = slide(Phase.CHECK, "Check " + o.getName() + " (from " + name + ")", t, dist, prev, visited);
                s.current = next;
                s.examined = new String[] {next, o.getId()};
                s.focusRow = o.getId();
                s.changedRow = better ? o.getId() : null;
                out.add(s);
            }
        }

        // ---------------------------------------------------------------- trace back
        List<String> back = new ArrayList<>();
        for (String at = end; at != null; at = prev.get(at)) back.add(at);

        for (int i = 0; i + 1 < back.size(); i++) {
            String from = g.getNode(back.get(i)).getName(), to = g.getNode(back.get(i + 1)).getName();
            String t = (i == 0 ? "All green, so the table is finished. To find the route, start at <b>"
                    + from + "</b> and follow PREVIOUS backwards.<br><br>" : "")
                    + "<b>" + from + "</b>'s previous is <b>" + to + "</b>. Draw the line " + to + " \u2013 " + from + ".";
            if (i + 2 >= back.size()) t += "<br><b>" + to + "</b> is the start, so we are done.";
            Slide s = slide(Phase.TRACE, "Trace back: " + from + " came from " + to, t, dist, prev, visited);
            s.emphasisCol = 2;
            s.focusRow = back.get(i);
            s.route = new ArrayList<>(back.subList(0, i + 2));
            out.add(s);
        }

        // ---------------------------------------------------------------- recap
        List<String> fwd = new ArrayList<>(back);
        java.util.Collections.reverse(fwd);
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < fwd.size(); i++) {
            if (i > 0) names.append(" \u2192 ");
            names.append(g.getNode(fwd.get(i)).getName());
        }
        Slide s = slide(Phase.PLAY, "Now you try!",
                "Shortest route: <b>" + names + "</b> (cost " + dist.get(end) + ").<br>"
              + "<b>1. PICK</b> the smallest unvisited distance.<br>"
              + "<b>2. CHECK</b> its neighbours and update if smaller.<br>"
              + "<b>3. REPEAT</b> until all green, then <b>trace back</b> with PREVIOUS.<br>"
              + c("#b02020", "<b>3 wrong clicks and the Stink-O-Meter explodes!</b>"),
                dist, prev, visited);
        s.route = new ArrayList<>(fwd);
        out.add(s);
        return out;
    }

    // =====================================================================================
    //  The window contents
    // =====================================================================================

    static class Content extends JPanel {
        private final Graph graph = exampleGraph();
        private final List<Slide> slides = buildSlides(graph);
        private final Runnable onClose;

        private final DemoMap map = new DemoMap(graph);
        private final DemoTable table = new DemoTable(graph);
        private final Chip chip = new Chip();
        private final JLabel title = new JLabel();
        private final JEditorPane body = htmlPane(17);
        private final JLabel counter = new JLabel("", SwingConstants.CENTER);
        private final Progress progress = new Progress();
        private final RoundButton back = new RoundButton(null, -1, this::previous);
        private final RoundButton nextBtn = new RoundButton(null, 1, this::next);
        private final RoundButton play = new RoundButton("PLAY!", 0, this::close);
        private final RoundButton skip = new RoundButton("SKIP", 0, this::close);
        private int index;

        Content(Runnable onClose) {
            this.onClose = onClose;
            setLayout(new BorderLayout(0, 10));
            setBackground(Theme.BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.INK, 3),
                    BorderFactory.createEmptyBorder(14, 16, 12, 16)));

            // header
            JLabel heading = new JLabel("How Dijkstra's algorithm works");
            heading.setFont(Theme.font(Font.BOLD, 24));
            heading.setForeground(Theme.INK);
            counter.setFont(Theme.font(Font.BOLD, 16));
            counter.setForeground(Theme.INK);
            JPanel headTop = new JPanel(new BorderLayout());
            headTop.setOpaque(false);
            headTop.add(heading, BorderLayout.WEST);
            headTop.add(counter, BorderLayout.EAST);
            JPanel header = new JPanel(new BorderLayout(0, 6));
            header.setOpaque(false);
            header.add(headTop, BorderLayout.NORTH);
            header.add(progress, BorderLayout.SOUTH);
            add(header, BorderLayout.NORTH);

            // middle: map | table + key
            JPanel side = new JPanel(new BorderLayout(0, 10));
            side.setOpaque(false);
            side.setPreferredSize(new Dimension(DemoTable.WIDTH, 0));
            side.add(table, BorderLayout.NORTH);
            side.add(new Legend(), BorderLayout.CENTER);
            JPanel middle = new JPanel(new BorderLayout(14, 0));
            middle.setOpaque(false);
            middle.add(map, BorderLayout.CENTER);
            middle.add(side, BorderLayout.EAST);
            add(middle, BorderLayout.CENTER);

            // bottom: explanation + navigation
            title.setFont(Theme.font(Font.BOLD, 20));
            title.setForeground(Theme.INK);
            JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            titleRow.setOpaque(false);
            titleRow.add(chip);
            titleRow.add(title);

            JPanel text = new JPanel(new BorderLayout(0, 8));
            text.setBackground(Theme.PANEL);
            text.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.INK, 2),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)));
            text.add(titleRow, BorderLayout.NORTH);
            text.add(body, BorderLayout.CENTER);
            text.setPreferredSize(new Dimension(0, 190));

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);
            left.add(back);
            left.add(skip);
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            right.setOpaque(false);
            right.add(nextBtn);
            right.add(play);
            JPanel nav = new JPanel(new GridLayout(1, 3));
            nav.setOpaque(false);
            nav.add(left);
            nav.add(new JLabel());
            nav.add(right);

            JPanel bottom = new JPanel(new BorderLayout(0, 10));
            bottom.setOpaque(false);
            bottom.add(text, BorderLayout.CENTER);
            bottom.add(nav, BorderLayout.SOUTH);
            add(bottom, BorderLayout.SOUTH);

            show(0);
        }

        void next()     { if (index < slides.size() - 1) show(index + 1); else close(); }
        void previous() { if (index > 0) show(index - 1); }
        void close()    { onClose.run(); }

        void show(int i) {
            index = i;
            Slide s = slides.get(i);
            map.setSlide(s);
            table.setSlide(s);
            chip.set(s.phase.label, s.phase.colour);
            title.setText(s.title);
            body.setText("<html>" + s.html + "</html>");
            counter.setText("Step " + (i + 1) + " / " + slides.size());
            progress.setFraction((i + 1) / (double) slides.size());
            back.setVisible(i > 0);
            skip.setVisible(i < slides.size() - 1);
            nextBtn.setVisible(i < slides.size() - 1);
            play.setVisible(i == slides.size() - 1);
            repaint();
        }
    }

    // =====================================================================================
    //  Drawing helpers and components (same colours/fonts as the game)
    // =====================================================================================

    private static void smooth(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private static void centred(Graphics2D g, String s, double cx, double cy) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, (float) (cx - fm.stringWidth(s) / 2.0), (float) (cy + (fm.getAscent() - fm.getDescent()) / 2.0));
    }

    private static boolean samePair(String a, String b, String x, String y) {
        return (a.equals(x) && b.equals(y)) || (a.equals(y) && b.equals(x));
    }

    /** Read-only HTML text that wraps to whatever width it is given. */
    private static JEditorPane htmlPane(int fontSize) {
        JEditorPane p = new JEditorPane();
        p.setContentType("text/html");
        p.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        p.setEditable(false);
        p.setFocusable(false);
        p.setOpaque(false);
        p.setBorder(null);
        p.setFont(Theme.font(Font.PLAIN, fontSize));
        p.setForeground(Theme.INK);
        return p;
    }

    // ---------------------------------------------------------------- the map
    private static class DemoMap extends JPanel {
        // visible part of the 800x500 map space
        private static final double VX = 50, VY = 50, VW = 700, VH = 430;
        private static final int R = 40;                       // node radius
        private static final Color RING = new Color(20, 80, 200);

        private final Graph graph;
        private Slide s;

        DemoMap(Graph graph) {
            this.graph = graph;
            setBackground(Theme.PANEL);
            setBorder(BorderFactory.createLineBorder(Theme.INK, 2));
        }

        void setSlide(Slide s) { this.s = s; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (s == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            double sc = Math.min(getWidth() / VW, getHeight() / VH);
            g2.translate((getWidth() - VW * sc) / 2, (getHeight() - VH * sc) / 2);
            g2.scale(sc, sc);
            g2.translate(-VX, -VY);

            for (Edge e : graph.getEdges()) drawEdge(g2, e);
            for (Node n : graph.getNodes()) drawNode(g2, n);
            for (Edge e : graph.getEdges()) drawWeight(g2, e);
            for (Node n : graph.getNodes()) drawTags(g2, n);
            g2.dispose();
        }

        private boolean onRoute(Edge e) {
            for (int i = 0; i + 1 < s.route.size(); i++)
                if (samePair(e.getA().getId(), e.getB().getId(), s.route.get(i), s.route.get(i + 1))) return true;
            return false;
        }

        private boolean isExamined(Edge e) {
            return s.examined != null && samePair(e.getA().getId(), e.getB().getId(), s.examined[0], s.examined[1]);
        }

        private boolean isHot(Edge e) {
            for (String[] h : s.hot) if (samePair(e.getA().getId(), e.getB().getId(), h[0], h[1])) return true;
            return false;
        }

        private void drawEdge(Graphics2D g, Edge e) {
            float w = 4f;
            Color col = Theme.EDGE;
            if (onRoute(e)) { col = Theme.ROUTE; w = 9f; }
            else if (isExamined(e)) { col = Theme.HOT; w = 10f; }
            else if (isHot(e)) { col = Theme.HOT; w = 6f; }
            g.setColor(col);
            g.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Line2D.Double(e.getA().getX(), e.getA().getY(), e.getB().getX(), e.getB().getY()));
        }

        private void drawWeight(Graphics2D g, Edge e) {
            boolean ex = isExamined(e), rt = onRoute(e);
            g.setFont(Theme.font(Font.BOLD, ex ? 26 : 22));
            String t = String.valueOf(e.getWeight());
            FontMetrics fm = g.getFontMetrics();
            double mx = (e.getA().getX() + e.getB().getX()) / 2.0, my = (e.getA().getY() + e.getB().getY()) / 2.0;
            int w = Math.max(34, fm.stringWidth(t) + 16), h = fm.getHeight();
            RoundRectangle2D box = new RoundRectangle2D.Double(mx - w / 2.0, my - h / 2.0, w, h, 12, 12);
            g.setColor(rt ? new Color(200, 225, 250) : Color.WHITE);
            g.fill(box);
            g.setColor(ex ? Theme.HOT : rt ? Theme.ROUTE : Theme.EDGE);
            g.setStroke(new BasicStroke(ex ? 3f : 1.5f));
            g.draw(box);
            g.setColor(Theme.INK);
            centred(g, t, mx, my);
        }

        private void drawNode(Graphics2D g, Node n) {
            int x = n.getX() - R, y = n.getY() - R;
            boolean cur = n.getId().equals(s.current);
            boolean target = s.examined != null && n.getId().equals(s.examined[1]);
            if (cur || target) {
                g.setColor(cur ? RING : Theme.HOT);
                g.setStroke(new BasicStroke(5f));
                g.drawOval(x - 8, y - 8, 2 * R + 16, 2 * R + 16);
            }
            g.setColor(s.visited.contains(n.getId()) ? Theme.VISITED : Color.WHITE);
            g.fillOval(x, y, 2 * R, 2 * R);
            g.setColor(Theme.INK);
            g.setStroke(new BasicStroke(2.5f));
            g.drawOval(x, y, 2 * R, 2 * R);
            g.setFont(Theme.font(Font.BOLD, 34));
            centred(g, n.getName(), n.getX(), n.getY());

            // distance badge
            String label = d(s.dist.get(n.getId()));
            g.setFont(Theme.font(Font.BOLD, 18));
            FontMetrics bm = g.getFontMetrics();
            int bw = Math.max(32, bm.stringWidth(label) + 14), bh = 28;
            int bx = n.getX() + R - bw / 2 + 8, by = n.getY() - R - 14;
            g.setColor(s.visited.contains(n.getId()) ? Theme.GOOD : new Color(60, 65, 80));
            g.fill(new RoundRectangle2D.Double(bx, by, bw, bh, 14, 14));
            g.setColor(Color.WHITE);
            centred(g, label, bx + bw / 2.0, by + bh / 2.0);
        }

        private void drawTags(Graphics2D g, Node n) {
            if (n.getId().equals("a") || n.getId().equals("d")) {
                g.setFont(Theme.font(Font.BOLD, 15));
                g.setColor(Theme.GOOD);
                centred(g, n.getId().equals("a") ? "START" : "END", n.getX(), n.getY() + R + 24);
            }
            if (n.getId().equals(s.current)) {
                String t = "visiting now";
                g.setFont(Theme.font(Font.BOLD, 14));
                FontMetrics fm = g.getFontMetrics();
                int w = fm.stringWidth(t) + 16, h = 22;
                double cx = n.getX(), cy = n.getY() + R + 30 + (n.getId().equals("a") || n.getId().equals("d") ? 24 : 0);
                g.setColor(RING);
                g.fill(new RoundRectangle2D.Double(cx - w / 2.0, cy - h / 2.0, w, h, 10, 10));
                g.setColor(Color.WHITE);
                centred(g, t, cx, cy);
            }
        }
    }

    // ---------------------------------------------------------------- the table
    private static class DemoTable extends JComponent {
        static final int WIDTH = 340;
        private static final int[] COL_W = {90, 125, 125};
        private static final int HEADER_H = 44, ROW_H = 38;
        private final Graph graph;
        private Slide s;

        DemoTable(Graph graph) {
            this.graph = graph;
            setPreferredSize(new Dimension(WIDTH, HEADER_H + ROW_H * graph.getNodes().size() + 4));
        }

        void setSlide(Slide s) { this.s = s; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            if (s == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int rows = graph.getNodes().size(), h = HEADER_H + rows * ROW_H;
            g2.setColor(Theme.PANEL);
            g2.fillRect(0, 0, WIDTH, h);

            String[] heads = {"NODE", "DISTANCE", "PREVIOUS"};
            g2.setFont(Theme.font(Font.BOLD, 13));
            int x = 0;
            for (int c = 0; c < 3; c++) {
                if (s.emphasisCol > 0 && s.emphasisCol == c) { g2.setColor(new Color(255, 214, 150)); g2.fillRect(x, 0, COL_W[c], HEADER_H); }
                g2.setColor(Theme.INK);
                centred(g2, heads[c], x + COL_W[c] / 2.0, HEADER_H / 2.0);
                x += COL_W[c];
            }

            int r = 0;
            for (Node n : graph.getNodes()) {
                int y = HEADER_H + r * ROW_H;
                Color bg = Theme.PANEL;
                if (s.visited.contains(n.getId())) bg = Theme.VISITED;
                if (n.getId().equals(s.changedRow)) bg = new Color(255, 236, 150);
                g2.setColor(bg);
                g2.fillRect(0, y, WIDTH, ROW_H);

                String p = s.prev.get(n.getId());
                g2.setColor(Theme.INK);
                g2.setFont(Theme.font(Font.BOLD, 18));
                centred(g2, n.getName(), COL_W[0] / 2.0, y + ROW_H / 2.0);
                centred(g2, d(s.dist.get(n.getId())), COL_W[0] + COL_W[1] / 2.0, y + ROW_H / 2.0);
                centred(g2, p == null ? "-" : graph.getNode(p).getName(), COL_W[0] + COL_W[1] + COL_W[2] / 2.0, y + ROW_H / 2.0);
                r++;
            }

            g2.setColor(new Color(200, 202, 208));
            g2.setStroke(new BasicStroke(1f));
            for (int i = 0; i <= rows; i++) g2.drawLine(0, HEADER_H + i * ROW_H, WIDTH, HEADER_H + i * ROW_H);
            g2.drawLine(COL_W[0], 0, COL_W[0], h);
            g2.drawLine(COL_W[0] + COL_W[1], 0, COL_W[0] + COL_W[1], h);
            g2.setColor(Theme.INK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(1, 1, WIDTH - 3, h - 3);

            if (s.emphasisCol > 0) {
                int cx = 0;
                for (int c = 0; c < s.emphasisCol; c++) cx += COL_W[c];
                g2.setColor(Theme.HOT);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRect(cx + 1, 1, COL_W[s.emphasisCol] - 2, h - 3);
            }
            if (s.focusRow != null) {
                int ri = 0;
                for (Node n : graph.getNodes()) {
                    if (n.getId().equals(s.focusRow)) {
                        g2.setColor(Theme.HOT);
                        g2.setStroke(new BasicStroke(3.5f));
                        g2.drawRect(2, HEADER_H + ri * ROW_H + 1, WIDTH - 5, ROW_H - 2);
                    }
                    ri++;
                }
            }
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------- colour key
    private static class Legend extends JComponent {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(Theme.PANEL);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Theme.INK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
            g2.setFont(Theme.font(Font.BOLD, 13));
            g2.drawString("KEY", 12, 22);
            g2.setFont(Theme.font(Font.PLAIN, 13));
            int y = 44, step = 24;

            g2.setColor(Theme.VISITED); g2.fillRect(12, y - 12, 26, 16);
            g2.setColor(Theme.INK); g2.setStroke(new BasicStroke(1.5f)); g2.drawRect(12, y - 12, 26, 16);
            g2.drawString("visited (final)", 48, y);
            y += step;

            g2.setColor(new Color(255, 236, 150)); g2.fillRect(12, y - 12, 26, 16);
            g2.setColor(Theme.INK); g2.drawRect(12, y - 12, 26, 16);
            g2.drawString("table value changed", 48, y);
            y += step;

            g2.setColor(new Color(20, 80, 200)); g2.setStroke(new BasicStroke(3.5f));
            g2.drawOval(12, y - 13, 26, 17);
            g2.setColor(Theme.INK);
            g2.drawString("node being visited", 48, y);
            y += step;

            g2.setColor(Theme.HOT); g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(14, y - 4, 36, y - 4);
            g2.setColor(Theme.INK);
            g2.drawString("line being checked", 48, y);
            y += step;

            g2.setColor(Theme.ROUTE); g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(14, y - 4, 36, y - 4);
            g2.setColor(Theme.INK);
            g2.drawString("final route", 48, y);
            g2.dispose();
        }
    }

    // ---------------------------------------------------------------- small widgets
    private static class Chip extends JComponent {
        private String text = "";
        private Color colour = Theme.INK;

        Chip() { setPreferredSize(new Dimension(150, 30)); }

        void set(String text, Color colour) { this.text = text; this.colour = colour; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(colour);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.font(Font.BOLD, 15));
            centred(g2, text, getWidth() / 2.0, getHeight() / 2.0);
            g2.dispose();
        }
    }

    private static class Progress extends JComponent {
        private double fraction;

        Progress() { setPreferredSize(new Dimension(0, 10)); }

        void setFraction(double f) { fraction = f; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(new Color(215, 217, 222));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(Theme.GOOD);
            g2.fillRoundRect(0, 0, (int) (getWidth() * fraction), getHeight(), 10, 10);
            g2.setColor(Theme.INK);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
        }
    }

    /** Rounded button in the game's style. dir: -1 = left arrow, 1 = right arrow, 0 = text label. */
    private static class RoundButton extends JComponent {
        private final String label;
        private final int dir;
        private boolean hover;

        RoundButton(String label, int dir, Runnable action) {
            this.label = label;
            this.dir = dir;
            setPreferredSize(new Dimension(label != null ? 130 : 76, 46));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { action.run(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int w = getWidth(), h = getHeight();
            g2.setColor(hover ? Theme.VISITED : Color.WHITE);
            g2.fillRoundRect(2, 2, w - 4, h - 4, 16, 16);
            g2.setColor(Theme.INK);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(2, 2, w - 4, h - 4, 16, 16);
            if (label != null) {
                g2.setFont(Theme.font(Font.BOLD, 17));
                centred(g2, label, w / 2.0, h / 2.0);
            } else {
                int cx = w / 2, cy = h / 2, s = dir;
                g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 14 * s, cy, cx + 14 * s, cy);
                g2.drawLine(cx + 14 * s, cy, cx + 5 * s, cy - 9);
                g2.drawLine(cx + 14 * s, cy, cx + 5 * s, cy + 9);
            }
            g2.dispose();
        }
    }
}
