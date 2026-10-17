package effects;

import java.awt.*;
import java.util.Random;

/** Shower, bubbles and confetti when the student reaches Home. */
public class VictoryEffect implements Effect {
    private static final double SHOWER_TIME = 3.2;
    private static final double DURATION = 4.6;

    private final int width;
    private final int height;
    private final ParticleSystem ps = new ParticleSystem();
    private final Random rnd = new Random();
    private double t;
    private double dropAcc;
    private double bubbleAcc;

    public VictoryEffect(int width, int height) {
        this.width = width;
        this.height = height;
        Color[] colours = { new Color(255, 90, 120), new Color(255, 210, 60), new Color(90, 200, 255), new Color(120, 220, 120) };
        for (int i = 0; i < 90; i++) {
            ParticleSystem.Particle p = new ParticleSystem.Particle(ParticleSystem.Kind.CONFETTI,
                    width / 2.0 + rnd.nextGaussian() * 60, 80,
                    rnd.nextGaussian() * 220, -100 - rnd.nextDouble() * 200,
                    8 + rnd.nextDouble() * 8, 2.5 + rnd.nextDouble() * 1.5,
                    colours[rnd.nextInt(colours.length)]);
            p.gravity = 320;
            ps.add(p);
        }
    }

    @Override public boolean update(double dt) {
        t += dt;
        if (t < SHOWER_TIME) {
            dropAcc += dt * 160;
            while (dropAcc >= 1) {
                dropAcc--;
                ParticleSystem.Particle p = new ParticleSystem.Particle(ParticleSystem.Kind.DROP,
                        rnd.nextDouble() * width, -10, 0, 420 + rnd.nextDouble() * 160,
                        14 + rnd.nextDouble() * 10, 1.6, new Color(120, 190, 255, 220));
                ps.add(p);
            }
            bubbleAcc += dt * 25;
            while (bubbleAcc >= 1) {
                bubbleAcc--;
                ps.add(new ParticleSystem.Particle(ParticleSystem.Kind.BUBBLE,
                        rnd.nextDouble() * width, height + 10, rnd.nextGaussian() * 10,
                        -90 - rnd.nextDouble() * 120, 12 + rnd.nextDouble() * 30, 3.5,
                        new Color(170, 220, 255, 255)));
            }
        }
        ps.update(dt);
        return t < DURATION;
    }

    @Override public void draw(Graphics2D g, int w, int h) {
        ps.draw(g);
        double a = t < 0.5 ? t / 0.5 : (t > DURATION - 1 ? Math.max(0, DURATION - t) : 1);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 64));
        String s = "SQUEAKY CLEAN!";
        FontMetrics fm = g.getFontMetrics();
        int x = w / 2 - fm.stringWidth(s) / 2;
        int y = h / 2 - 40;
        g.setColor(new Color(255, 255, 255, (int) (230 * a)));
        g.drawString(s, x + 3, y + 3);
        g.setColor(new Color(30, 120, 200, (int) (255 * a)));
        g.drawString(s, x, y);
    }
}
