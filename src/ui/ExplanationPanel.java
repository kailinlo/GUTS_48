package ui;

import javax.swing.*;
import java.awt.*;

/** Right-hand panel: how Dijkstra works, plus a running log of what each step did. */
public class ExplanationPanel extends JPanel {
    private final JTextArea text = new JTextArea();

    public ExplanationPanel() {
        setLayout(new BorderLayout(0, 6));
        setBackground(Theme.PANEL);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("HOW DIJKSTRA WORKS", SwingConstants.CENTER);
        title.setFont(Theme.font(Font.BOLD, 14));
        add(title, BorderLayout.NORTH);

        text.setEditable(false);
        text.setFocusable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(Theme.font(Font.PLAIN, 12));
        text.setBackground(Theme.PANEL);

        JScrollPane scroll = new JScrollPane(text);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
    }

    public void showIntro() {
        text.setText(
            "1. Start at Lecture (distance 0). Every other node is \u221e because we haven't found a route yet.\n\n"
          + "2. Pick the UNVISITED node with the smallest distance.\n\n"
          + "3. Look at its roads. If going through it gives a neighbour a shorter distance, update the table and note where you came from.\n\n"
          + "4. Repeat until every node is visited.\n\n"
          + "5. Start at Home and follow the 'previous node' column back to Lecture. That's the quickest route!\n\n"
          + "--- STEP LOG ---\n");
    }

    public void append(String s) {
        text.append(s);
        text.setCaretPosition(text.getDocument().getLength());
    }
}
