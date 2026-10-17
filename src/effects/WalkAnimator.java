package effects;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/** Moves the student along a list of map-space points at a steady speed. Draws nothing itself. */
public class WalkAnimator implements Effect {
    private final List<Point2D.Double> points;
    private final double speed;
    private final Consumer<Point2D.Double> onMove;
    private final IntConsumer onReach;
    private final Runnable onDone;

    private final Point2D.Double pos;
    private int target = 1;

    public WalkAnimator(List<Point2D.Double> points, double speed, Consumer<Point2D.Double> onMove,
                        IntConsumer onReach, Runnable onDone) {
        this.points = points;
        this.speed = speed;
        this.onMove = onMove;
        this.onReach = onReach;
        this.onDone = onDone;
        this.pos = new Point2D.Double(points.get(0).x, points.get(0).y);
    }

    @Override public boolean update(double dt) {
        double budget = speed * dt;
        while (budget > 0 && target < points.size()) {
            Point2D.Double goal = points.get(target);
            double dx = goal.x - pos.x, dy = goal.y - pos.y;
            double dist = Math.hypot(dx, dy);
            if (dist <= budget) {
                pos.setLocation(goal.x, goal.y);
                budget -= dist;
                onReach.accept(target);
                target++;
            } else {
                pos.setLocation(pos.x + dx / dist * budget, pos.y + dy / dist * budget);
                budget = 0;
            }
        }
        onMove.accept(new Point2D.Double(pos.x, pos.y));
        if (target >= points.size()) {
            onDone.run();
            return false;
        }
        return true;
    }

    @Override public void draw(Graphics2D g, int w, int h) { }
}
