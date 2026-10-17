package effects;

import javax.swing.JFrame;
import java.awt.*;
import java.util.Random;

/** Third strike: the meter bursts, the window shakes, then onDone restarts the game. */
public class OverflowEffect implements Effect {
    private static final double DURATION = 2.6;

    private final JFrame frame;
    private final Point origin;
    private final Runnable onDone;
    private final ParticleSystem ps = new ParticleSystem();
    private final Random rnd = new Random();
    private double t;

    public OverflowEffect(JFrame frame, Point meterPos, Runnable onDone) {
        this.frame = frame;
        this.origin = frame.getLocation();
        this.onDone = onDone;
        for (int i = 0; i < 80; i++) {
            double ang = rnd.nextDouble() * Math.PI * 2;
            double speed = 80 + rnd.nextDouble() * 260;
            ParticleSystem.Particle p = new ParticleSystem.Particle(ParticleSystem.Kind.BLOB,
                    meterPos.x, meterPos.y, Math.cos(ang) * speed, Math.sin(ang) * speed - 80,
                    20 + rnd.nextDouble() * 40, 1.6 + rnd.nextDouble() * 0.8,
                    new Color(90 + rnd.nextInt(50), 150 + rnd.nextInt(60), 30, 190));
            p.growth = 30;
            p.gravity = 60;
            ps.add(p);
        }
    }

    @Override public boolean update(double dt) {
        t += dt;
        ps.update(dt);
        double amp = 12 * Math.max(0, 1 - t / DURATION);
        frame.setLocation(origin.x + (int) ((rnd.nextDouble() - 0.5) * 2 * amp),
                          origin.y + (int) ((rnd.nextDouble() - 0.5) * 2 * amp));
        if (t >= DURATION) {
            frame.setLocation(origin);
            onDone.run();
            return false;
        }
        return true;
    }

    @Override public void draw(Graphics2D g, int w, int h) {
        double tint = Math.min(0.45, t * 0.5);
        g.setColor(new Color(80, 140, 30, (int) (255 * tint)));
        g.fillRect(0, 0, w, h);
        ps.draw(g);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 54));
        center(g, "STINK OVERFLOW!", w / 2, h / 2 - 10, new Color(40, 70, 10));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 22));
        center(g, "You smell too bad. New roads incoming...", w / 2, h / 2 + 35, new Color(40, 70, 10));
    }

    private void center(Graphics2D g, String s, int cx, int y, Color c) {
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.WHITE);
        g.drawString(s, cx - fm.stringWidth(s) / 2 + 2, y + 2);
        g.setColor(c);
        g.drawString(s, cx - fm.stringWidth(s) / 2, y);
    }
}
