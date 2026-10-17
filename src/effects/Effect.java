package effects;

import java.awt.Graphics2D;

/** Anything the overlay can play. */
public interface Effect {
    /** Advance by dt seconds. Return false when finished. */
    boolean update(double dt);

    /** Draw in overlay (window) coordinates. w/h = overlay size. */
    void draw(Graphics2D g, int w, int h);
}