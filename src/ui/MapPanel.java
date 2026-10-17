package ui;

import effects.AnimationClock;
import logic.AlgorithmStep;
import model.Edge;
import model.Graph;
import model.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

/** Draws the map, edge weights, clickable nodes and the student. Works in an 800x500 map space. */
public class MapPanel extends JPanel implements AnimationClock.Tickable {
    public interface NodeClickListener { void onNodeClicked(String nodeId); }

    private static final double W = 800, H = 500;
    private static final int NODE_W = 116, NODE_H = 64;

    private Graph graph;
    private Set<String> visited = new HashSet<>();
    private Map<String, Integer> distances = new HashMap<>();
    private List<String> route = new ArrayList<>();
    private List<Edge> hotEdges = new ArrayList<>();

    private String flashId;
    private Color flashColor = Color.WHITE;
    private double flashTime;

    private double studentX, studentY;
    private int stinkLevel;
    private boolean clean, walking;
    private double time;
    private NodeClickListener listener;

    public MapPanel() {
        setBackground(Theme.PANEL);
        AnimationClock.get().add(this);
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                String id = nodeAt(e.getX(), e.getY());
                if (id != null && listener != null) listener.onNodeClicked(id);
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                setCursor(nodeAt(e.getX(), e.getY()) != null
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        });
    }

    // ---------- state setters (called by the controller) ----------
    public void setNodeClickListener(NodeClickListener l) { this.listener = l; }
    public void setGraph(Graph g)                         { this.graph = g; }
    public void setVisited(Set<String> v)                 { this.visited = new HashSet<>(v); }
    public void setDistances(Map<String, Integer> d)      { this.distances = new HashMap<>(d); }
    public void setRoute(List<String> r)                  { this.route = new ArrayList<>(r); }
    public void setHotEdges(List<Edge> e)                 { this.hotEdges = new ArrayList<>(e); }
    public void setStudentAt(double x, double y)          { this.studentX = x; this.studentY = y; }
    public void setStinkLevel(int level)                  { this.stinkLevel = level; }
    public void setClean(boolean c)                       { this.clean = c; }
    public void setWalking(boolean w)                     { this.walking = w; }

    public void flash(String nodeId, Color colour) {
        this.flashId = nodeId;
        this.flashColor = colour;
        this.flashTime = 0.9;
    }

    @Override public void tick(double dt) {
        time += dt;
        if (flashTime > 0) flashTime -= dt;
        repaint();
    }

    // ---------- coordinate helpers ----------
    private double scale() { return Math.min(getWidth() / W, getHeight() / H); }
    private double offX()  { return (getWidth() - W * scale()) / 2; }
    private double offY()  { return (getHeight() - H * scale()) / 2; }

    /** Map-space position converted to this panel's pixel coordinates. */
    public Point toScreenPoint(double lx, double ly) {
        return new Point((int) (offX() + lx * scale()), (int) (offY() + ly * scale()));
    }

    private String nodeAt(int mx, int my) {
        if (graph == null) return null;
        double lx = (mx - offX()) / scale(), ly = (my - offY()) / scale();
        for (Node n : graph.getNodes()) {
            double dx = (lx - n.getX()) / (NODE_W / 2.0), dy = (ly - n.getY()) / (NODE_H / 2.0);
            if (dx * dx + dy * dy <= 1) return n.getId();
        }
        return null;
    }

    // ---------- painting ----------
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graph == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.translate(offX(), offY());
        g2.scale(scale(), scale());

        drawEdges(g2);
        for (Node n : graph.getNodes()) drawNode(g2, n);
        drawWeights(g2);
        drawStudent(g2);
        g2.dispose();
    }

    private boolean onRoute(Edge e) {
        for (int i = 0; i + 1 < route.size(); i++) {
            String a = route.get(i), b = route.get(i + 1);
            if ((e.getA().getId().equals(a) && e.getB().getId().equals(b))
                    || (e.getA().getId().equals(b) && e.getB().getId().equals(a))) return true;
        }
        return false;
    }

    private void drawEdges(Graphics2D g) {
        for (Edge e : graph.getEdges()) {
            if (onRoute(e)) { g.setColor(Theme.ROUTE); g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); }
            else if (hotEdges.contains(e)) { g.setColor(Theme.HOT); g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); }
            else { g.setColor(Theme.EDGE); g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); }
            g.draw(new Line2D.Double(e.getA().getX(), e.getA().getY(), e.getB().getX(), e.getB().getY()));
        }
    }

    private void drawWeights(Graphics2D g) {
        g.setFont(Theme.font(Font.BOLD, 15));
        FontMetrics fm = g.getFontMetrics();
        for (Edge e : graph.getEdges()) {
            String s = e.getWeight() + " min";
            double mx = (e.getA().getX() + e.getB().getX()) / 2.0;
            double my = (e.getA().getY() + e.getB().getY()) / 2.0;
            int w = fm.stringWidth(s) + 14, h = fm.getHeight() + 2;
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(mx - w / 2.0, my - h / 2.0, w, h, 10, 10));
            g.setColor(Theme.EDGE);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(new RoundRectangle2D.Double(mx - w / 2.0, my - h / 2.0, w, h, 10, 10));
            g.setColor(Theme.INK);
            g.drawString(s, (float) (mx - fm.stringWidth(s) / 2.0), (float) (my + fm.getAscent() / 2.0 - 1));
        }
    }

    private void drawNode(Graphics2D g, Node n) {
        int x = n.getX() - NODE_W / 2, y = n.getY() - NODE_H / 2;
        Color fill = Color.WHITE;
        if (route.contains(n.getId())) fill = new Color(200, 225, 250);
        else if (visited.contains(n.getId())) fill = Theme.VISITED;
        if (n.getId().equals(flashId) && flashTime > 0) {
            float f = (float) Math.min(1, flashTime / 0.9);
            fill = blend(fill, flashColor, f);
        }
        g.setColor(fill);
        g.fillOval(x, y, NODE_W, NODE_H);
        g.setColor(Theme.INK);
        g.setStroke(new BasicStroke(2f));
        g.drawOval(x, y, NODE_W, NODE_H);

        g.setFont(Theme.font(Font.PLAIN, 15));
        FontMetrics fm = g.getFontMetrics();
        String[] words = n.getName().split(" ");
        int lineH = fm.getHeight() - 2;
        int top = n.getY() - (words.length * lineH) / 2 + fm.getAscent() - 2;
        for (int i = 0; i < words.length; i++) {
            g.drawString(words[i], n.getX() - fm.stringWidth(words[i]) / 2, top + i * lineH);
        }

        // distance badge
        Integer d = distances.get(n.getId());
        String label = (d == null || d == AlgorithmStep.INFINITY) ? "\u221e" : String.valueOf(d);
        g.setFont(Theme.font(Font.BOLD, 14));
        FontMetrics bm = g.getFontMetrics();
        int bw = Math.max(26, bm.stringWidth(label) + 12), bh = 22;
        int bx = n.getX() + NODE_W / 2 - bw + 6, by = n.getY() - NODE_H / 2 - 6;
        g.setColor(visited.contains(n.getId()) ? Theme.GOOD : new Color(60, 65, 80));
        g.fill(new RoundRectangle2D.Double(bx, by, bw, bh, 12, 12));
        g.setColor(Color.WHITE);
        g.drawString(label, bx + (bw - bm.stringWidth(label)) / 2, by + 16);
    }

    private static Color blend(Color a, Color b, float f) {
        return new Color(
                (int) (a.getRed() * (1 - f) + b.getRed() * f),
                (int) (a.getGreen() * (1 - f) + b.getGreen() * f),
                (int) (a.getBlue() * (1 - f) + b.getBlue() * f));
    }

    private void drawStudent(Graphics2D g) {
        double fx = studentX;
        double fy = studentY - NODE_H / 2.0 + 6 - (walking ? Math.abs(Math.sin(time * 10)) * 6 : 0);
        Color skin = clean ? new Color(255, 222, 190) : (stinkLevel > 0 ? new Color(205, 215, 140) : new Color(240, 205, 170));
        Color shirt = clean ? new Color(80, 170, 230) : new Color(125, 105, 60);

        g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double leg = walking ? Math.sin(time * 14) * 9 : 4;
        g.setColor(new Color(50, 60, 110));
        g.draw(new Line2D.Double(fx, fy - 22, fx - leg - 4, fy));
        g.draw(new Line2D.Double(fx, fy - 22, fx + leg + 4, fy));

        g.setStroke(new BasicStroke(14, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(shirt);
        g.draw(new Line2D.Double(fx, fy - 26, fx, fy - 42));

        g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(skin);
        double arm = walking ? Math.sin(time * 14) * 8 : 6;
        g.draw(new Line2D.Double(fx - 6, fy - 40, fx - 14, fy - 28 + arm));
        g.draw(new Line2D.Double(fx + 6, fy - 40, fx + 14, fy - 28 - arm));

        g.setColor(skin);
        g.fillOval((int) fx - 11, (int) fy - 66, 22, 22);
        g.setColor(Theme.INK);
        g.fillOval((int) fx - 5, (int) fy - 59, 3, 3);
        g.fillOval((int) fx + 3, (int) fy - 59, 3, 3);
        g.setStroke(new BasicStroke(1.5f));
        if (clean) g.drawArc((int) fx - 5, (int) fy - 56, 10, 6, 200, 140);
        else g.drawArc((int) fx - 4, (int) fy - 52, 8, 5, 20, 140);

        if (!clean) {
            // stink lines: more with every strike (and a faint one from the start)
            int lines = 1 + stinkLevel * 2;
            g.setColor(new Color(100, 170, 40, 190));
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < lines; i++) {
                double lx = fx - 22 + (i % 6) * 9 + (i / 6) * 4;
                Path2D.Double p = new Path2D.Double();
                p.moveTo(lx, fy - 70);
                for (int k = 1; k <= 8; k++) {
                    p.lineTo(lx + Math.sin(time * 4 + i + k * 0.9) * 4, fy - 70 - k * 3.5);
                }
                g.draw(p);
            }
            if (stinkLevel >= 2) {
                g.setColor(new Color(40, 40, 40));
                for (int i = 0; i < 3; i++) {
                    double a = time * 5 + i * 2.1;
                    g.fillOval((int) (fx + Math.cos(a) * 24), (int) (fy - 56 + Math.sin(a * 1.3) * 12), 4, 4);
                }
            }
        } else {
            g.setColor(new Color(255, 255, 255, 230));
            for (int i = 0; i < 4; i++) {
                double a = time * 2 + i * 1.6;
                double sx = fx + Math.cos(a) * 26, sy = fy - 44 + Math.sin(a * 1.2) * 22;
                double r = 3 + Math.abs(Math.sin(time * 6 + i)) * 3;
                g.setStroke(new BasicStroke(1.8f));
                g.draw(new Line2D.Double(sx - r, sy, sx + r, sy));
                g.draw(new Line2D.Double(sx, sy - r, sx, sy + r));
            }
        }
    }
}
