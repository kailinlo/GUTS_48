package effects;

import javax.swing.JComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Transparent layer over the whole window (used as the glass pane). Clicks pass straight through. */
public class EffectsOverlay extends JComponent implements AnimationClock.Tickable {
    private final List<Effect> effects = new ArrayList<>();

    public EffectsOverlay() {
        setOpaque(false);
        AnimationClock.get().add(this);
    }

    public void add(Effect e) { effects.add(e); }

    /** Returning false makes mouse events fall through to the components underneath. */
    @Override public boolean contains(int x, int y) { return false; }

    @Override public void tick(double dt) {
        if (effects.isEmpty()) return;
        for (Effect e : new ArrayList<>(effects)) {
            if (!e.update(dt)) effects.remove(e);
        }
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Effect e : new ArrayList<>(effects)) e.draw(g2, getWidth(), getHeight());
        g2.dispose();
    }
}
