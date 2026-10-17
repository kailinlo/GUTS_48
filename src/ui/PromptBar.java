package ui;

import javax.swing.*;
import java.awt.*;

/** Bottom panel: feedback message, current question, and the route buttons. */
public class PromptBar extends JPanel {
    private final JTextArea feedback = area(Font.BOLD);
    private final JTextArea question = area(Font.PLAIN);
    private final JButton undo = new JButton("Undo last step");
    private final JButton submit = new JButton("Submit route");
    private final JButton again = new JButton("Play again");

    public PromptBar() {
        setLayout(new BorderLayout(0, 6));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 18, 8, 18));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(feedback);
        text.add(Box.createVerticalStrut(8));
        text.add(question);
        add(text, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        for (JButton b : new JButton[] { undo, submit, again }) {
            b.setFont(Theme.font(Font.BOLD, 13));
            b.setFocusable(false);
            buttons.add(b);
        }
        add(buttons, BorderLayout.SOUTH);
        showPathButtons(false);
        showPlayAgain(false);
    }

    private static JTextArea area(int style) {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setFocusable(false);
        a.setOpaque(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(Theme.font(style, 15));
        a.setAlignmentX(Component.LEFT_ALIGNMENT);
        return a;
    }

    public void setFeedback(String text, Color colour) { feedback.setText(text); feedback.setForeground(colour); }
    public void setQuestion(String text)               { question.setText(text); question.setForeground(Theme.INK); }
    public void showPathButtons(boolean show)          { undo.setVisible(show); submit.setVisible(show); }
    public void showPlayAgain(boolean show)            { again.setVisible(show); }

    public void onUndo(Runnable r)      { undo.addActionListener(e -> r.run()); }
    public void onSubmit(Runnable r)    { submit.addActionListener(e -> r.run()); }
    public void onPlayAgain(Runnable r) { again.addActionListener(e -> r.run()); }
}
