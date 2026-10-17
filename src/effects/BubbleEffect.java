package effects;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/** Soap bubbles floating up from a node: the player got it right. */
public class BubbleEffect implements Effect {
    private final ParticleSystem ps = new ParticleSystem();

    public BubbleEffect(double x, double y) {
        Random r = new Random();
        for (int i = 0; i < 36; i++) {
            ParticleSystem.Particle p = new ParticleSystem.Particle(ParticleSystem.Kind.BUBBLE,
                    x + r.nextGaussian() * 25, y + r.nextGaussian() * 10,
                    r.nextGaussian() * 10, -60 - r.nextDouble() * 90,
                    8 + r.nextDouble() * 20, 1.4 + r.nextDouble(),
                    new Color(150, 210, 255, 255));
            p.wobble = 20;
            ps.add(p);
        }
    }

    @Override public boolean update(double dt) { ps.update(dt); return !ps.isEmpty(); }
    @Override public void draw(Graphics2D g, int w, int h) { ps.draw(g); }
}
