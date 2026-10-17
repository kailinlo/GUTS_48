package ui;

import effects.EffectsOverlay;

import javax.swing.*;
import java.awt.*;

/** The window layout from the wireframe. */
public class MainFrame extends JFrame {
    private final MapPanel map = new MapPanel();
    private final StinkometerPanel stink = new StinkometerPanel();
    private final TablePanel table = new TablePanel();
    private final PromptBar prompt = new PromptBar();
    private final ExplanationPanel explanation = new ExplanationPanel();
    private final EffectsOverlay overlay = new EffectsOverlay();

    public MainFrame() {
        super("The Great Stink Escape");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(12, 10));
        root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        JLabel title = new JLabel("The Great Stink Escape", SwingConstants.CENTER);
        title.setFont(Theme.font(Font.BOLD, 40));
        root.add(title, BorderLayout.NORTH);

        stink.setPreferredSize(new Dimension(110, 0));
        root.add(stink, BorderLayout.WEST);

        JPanel centre = new JPanel(new BorderLayout(0, 12));
        centre.setOpaque(false);
        centre.add(map, BorderLayout.CENTER);
        prompt.setPreferredSize(new Dimension(0, 175));
        centre.add(prompt, BorderLayout.SOUTH);
        root.add(centre, BorderLayout.CENTER);

        JPanel east = new JPanel(new BorderLayout(0, 12));
        east.setOpaque(false);
        east.setPreferredSize(new Dimension(330, 0));
        table.setPreferredSize(new Dimension(0, 290));
        east.add(table, BorderLayout.NORTH);
        east.add(explanation, BorderLayout.CENTER);
        root.add(east, BorderLayout.EAST);

        setContentPane(root);
        setGlassPane(overlay);
        overlay.setVisible(true);

        setSize(1200, 780);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public MapPanel getMapPanel()               { return map; }
    public StinkometerPanel getStinkometer()    { return stink; }
    public TablePanel getTablePanel()           { return table; }
    public PromptBar getPromptBar()             { return prompt; }
    public ExplanationPanel getExplanationPanel() { return explanation; }
    public EffectsOverlay getOverlay()          { return overlay; }

    /** Convert a point in any component to overlay (window) coordinates for effects. */
    public Point toOverlay(Component c, Point p) {
        return SwingUtilities.convertPoint(c, p, overlay);
    }
}
