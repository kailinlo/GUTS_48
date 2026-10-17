package effects;

import javax.swing.Timer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** The ONE timer that drives every animation (~60 fps). */
public class AnimationClock {
    public interface Tickable { void tick(double dt); }

    private static final AnimationClock INSTANCE = new AnimationClock();

    private final List<Tickable> listeners = new CopyOnWriteArrayList<>();
    private final Timer timer;
    private long last;

    private AnimationClock() {
        timer = new Timer(16, e -> {
            long now = System.nanoTime();
            double dt = Math.min((now - last) / 1e9, 0.05);
            last = now;
            for (Tickable t : listeners) t.tick(dt);
        });
    }

    public static AnimationClock get() { return INSTANCE; }

    public void add(Tickable t) { listeners.add(t); }

    public void start() {
        last = System.nanoTime();
        timer.start();
    }
}
