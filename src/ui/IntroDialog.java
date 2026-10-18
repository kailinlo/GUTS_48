package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * NEW FILE: two-slide welcome pop-up shown when the game opens.
 * Uses the same Theme colours and monospaced font as the main game.
 */
public class IntroDialog extends JDialog {

    private static final String[] SLIDES = {
        // slide 1
        "<div style='font-size:26px;font-weight:bold'>Welcome to the<br>&quot;The Great Stink Escape&quot;</div>"
      + "<br><br>an interactive step through game that helps you learn how Dijkstra's algorithm works",

        // slide 2
        "<div style='font-size:22px;font-weight:bold'>Oh no!</div><br>"
      + "You have had a long day of lectures and realised you <b>STINK</b>, ew!!"
      + "<br><br>You need the quickest route home to shower. Dijkstra's algorithm can find it, "
      + "but <b>YOU</b> need to follow the steps of the algorithm correctly to get there."
      + "<br><br>Work through the map from your lecture to your shower at home by clicking the right node at each turn."
      + "<br><br><b>Careful, three incorrect steps and your Stink-O-Meter will explode!</b>"
    };

    private final CardLayout cards = new CardLayout();
    private final JPanel deck = new JPanel(cards);
    private final JLabel counter = new JLabel("", SwingConstants.CENTER);
    private final NavButton back = new NavButton(null, -1, this::previous);
    private final NavButton next = new NavButton(null, 1, this::forward);
    private final NavButton start = new NavButton("START", 0, this::dispose);
    private int index = 0;

    public IntroDialog(Frame owner) {
        super(owner, true);
        setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.INK, 3),
                BorderFactory.createEmptyBorder(16, 16, 14, 16)));

        // slides
        for (int i = 0; i < SLIDES.length; i++) deck.add(slide(SLIDES[i]), String.valueOf(i));
        root.add(deck, BorderLayout.CENTER);

        // navigation row: [ back ]   1 / 2   [ next | start ]
        counter.setFont(Theme.font(Font.BOLD, 14));
        counter.setForeground(Theme.INK);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(back);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(next);
        right.add(start);

        JPanel nav = new JPanel(new GridLayout(1, 3));
        nav.setOpaque(false);
        nav.add(left);
        nav.add(counter);
        nav.add(right);
        root.add(nav, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(740, 560);
        setResizable(false);
        setLocationRelativeTo(owner);

        // Esc also closes it
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        show(0);
    }

    private JPanel slide(String html) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.PANEL);
        JLabel label = new JLabel("<html><div style='text-align:center'>" + html + "</div></html>",
                SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(600, 400));   // fixed width so the text wraps inside the slide
        label.setFont(Theme.font(Font.PLAIN, 16));
        label.setForeground(Theme.INK);
        p.add(label);
        return p;
    }

    private void forward()  { if (index < SLIDES.length - 1) show(index + 1); }
    private void previous() { if (index > 0) show(index - 1); }

    private void show(int i) {
        index = i;
        cards.show(deck, String.valueOf(i));
        counter.setText((i + 1) + " / " + SLIDES.length);
        back.setVisible(i > 0);
        next.setVisible(i < SLIDES.length - 1);
        start.setVisible(i == SLIDES.length - 1);
    }

    /** Rounded button drawn in the game's style. dir: -1 = left arrow, 1 = right arrow, 0 = text label. */
    private static class NavButton extends JComponent {
        private final String label;
        private final int dir;
        private boolean hover;

        NavButton(String label, int dir, Runnable action) {
            this.label = label;
            this.dir = dir;
            setPreferredSize(new Dimension(label != null ? 130 : 76, 46));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { action.run(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            g2.setColor(hover ? Theme.VISITED : Color.WHITE);
            g2.fillRoundRect(2, 2, w - 4, h - 4, 16, 16);
            g2.setColor(Theme.INK);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(2, 2, w - 4, h - 4, 16, 16);

            if (label != null) {
                g2.setFont(Theme.font(Font.BOLD, 17));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, (w - fm.stringWidth(label)) / 2, (h + fm.getAscent()) / 2 - 3);
            } else {
                int cx = w / 2, cy = h / 2, s = dir;       // s flips the arrow left/right
                g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 14 * s, cy, cx + 14 * s, cy);
                g2.drawLine(cx + 14 * s, cy, cx + 5 * s, cy - 9);
                g2.drawLine(cx + 14 * s, cy, cx + 5 * s, cy + 9);
            }
            g2.dispose();
        }
    }
}
