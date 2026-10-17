package effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/** Green stink cloud plus a brief red flash: the player got it wrong. */
public class StinkCloudEffect implements Effect {
    private final ParticleSystem ps = new ParticleSystem();
    private double t;

    public StinkCloudEffect(double x, double y) {
        Random r = new Random();
        for (int i = 0; i < 26; i++) {
            ParticleSystem.Particle p = new ParticleSystem.Particle(ParticleSystem.Kind.BLOB,
                    x + r.nextGaussian() * 22, y + r.nextGaussian() * 12,
                    r.nextGaussian() * 25, -25 - r.nextDouble() * 45,
                    25 + r.nextDouble() * 30, 1.5 + r.nextDouble(),
                    new Color(100 + r.nextInt(40), 160 + r.nextInt(40), 40, 170));
            p.growth = 22;
            p.wobble = 10;
            ps.add(p);
        }
    }

    @Override public boolean update(double dt) {
        t += dt;
        ps.update(dt);
        return !ps.isEmpty();
    }

    @Override public void draw(Graphics2D g, int w, int h) {
        if (t < 0.4) {
            g.setColor(new Color(220, 30, 30, (int) (90 * (1 - t / 0.4))));
            g.fillRect(0, 0, w, h);
        }
        ps.draw(g);
    }
}
