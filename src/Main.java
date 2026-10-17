import effects.AnimationClock;
import game.GameController;
import ui.MainFrame;

import javax.swing.SwingUtilities;

/** Usage: java Main [seed]   (pass a seed to get the same roads every time, e.g. for the live demo) */
public class Main {
    public static void main(String[] args) {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : System.nanoTime();
        System.out.println("Seed: " + seed);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            GameController controller = new GameController(frame, seed);
            controller.newGame("Welcome! You stink after a long lecture and need the quickest route home to the shower. "
                    + "Dijkstra's algorithm finds it, but YOU run it: pick the right node each turn. "
                    + "Three wrong answers and the Stink-O-Meter overflows!");
            AnimationClock.get().start();
            frame.setVisible(true);
        });
    }
}
