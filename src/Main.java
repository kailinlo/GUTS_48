import effects.AnimationClock;
import game.GameController;
import ui.IntroDialog;
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
            // EDITED: shorter opening message, because the intro pop-up now explains the story
            controller.newGame("Good luck! Remember: Dijkstra always visits the unvisited node with the smallest distance.");
            AnimationClock.get().start();
            frame.setVisible(true);

            // NEW: welcome pop-up (two slides) shown over the game when it opens
            new IntroDialog(frame).setVisible(true);
        });
    }
}
