package effects;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Small shared particle engine used by every effect. */
public class ParticleSystem {
    public enum Kind { BUBBLE, BLOB, DROP, CONFETTI }

    public static class Particle {
        public Kind kind;
        public double x, y, vx, vy, gravity, size, growth, wobble, phase, life, maxLife;
        public Color color;

        public Particle(Kind kind, double x, double y, double vx, double vy, double size, double maxLife, Color color) {
            this.kind = kind; this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.size = size; this.maxLife = maxLife; this.color = color;
            this.phase = Math.random() * 6.28;
        }
    }

    private final List<Particle> particles = new ArrayList<>();

    public void add(Particle p) { particles.add(p); }
    public boolean isEmpty()    { return particles.isEmpty(); }

    public void update(double dt) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.life += dt;
            p.vy += p.gravity * dt;
            p.x += (p.vx + Math.cos(p.phase + p.life * 4) * p.wobble) * dt;
            p.y += p.vy * dt;
            p.size += p.growth * dt;
            if (p.life >= p.maxLife) it.remove();
        }
    }

    private static Color alpha(Color c, double factor) {
        int a = (int) Math.max(0, Math.min(255, c.getAlpha() * factor));
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    public void draw(Graphics2D g) {
        for (Particle p : particles) {
            double t = p.life / p.maxLife;
            double a = t < 0.6 ? 1 : Math.max(0, (1 - t) / 0.4);
            switch (p.kind) {
                case BUBBLE -> {
                    g.setColor(alpha(p.color, a * 0.35));
                    g.fillOval((int) (p.x - p.size / 2), (int) (p.y - p.size / 2), (int) p.size, (int) p.size);
                    g.setColor(alpha(new Color(255, 255, 255, 230), a));
                    g.setStroke(new BasicStroke(1.5f));
                    g.drawOval((int) (p.x - p.size / 2), (int) (p.y - p.size / 2), (int) p.size, (int) p.size);
                    int h = (int) Math.max(2, p.size / 4);
                    g.fillOval((int) (p.x - p.size / 4), (int) (p.y - p.size / 3), h, h);
                }
                case BLOB -> {
                    g.setColor(alpha(p.color, a));
                    g.fillOval((int) (p.x - p.size / 2), (int) (p.y - p.size / 2), (int) p.size, (int) p.size);
                }
                case DROP -> {
                    g.setColor(alpha(p.color, a));
                    g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.drawLine((int) p.x, (int) p.y, (int) p.x, (int) (p.y + p.size));
                }
                case CONFETTI -> {
                    AffineTransform old = g.getTransform();
                    g.translate(p.x, p.y);
                    g.rotate(p.phase + p.life * 6);
                    g.setColor(alpha(p.color, a));
                    g.fillRect((int) -p.size / 2, (int) -p.size / 4, (int) p.size, (int) Math.max(2, p.size / 2));
                    g.setTransform(old);
                }
            }
        }
    }
}
