package ui;

import effects.AnimationClock;

import javax.swing.*;
import java.awt.*;

/** Vertical gauge. Each strike fills a third; overflow shows green slime spilling out of the top. */
public class StinkometerPanel extends JPanel implements AnimationClock.Tickable {
    private static final int MAX = 3;

    private double target;
    private double shown;
    private boolean overflow;
    private int strikes;
    private double time;

    public StinkometerPanel() {
        setOpaque(false);
        AnimationClock.get().add(this);
    }

    public void setLevel(int strikes) {
        this.strikes = strikes;
        this.target = Math.min(strikes, MAX);
    }

    public void setOverflow(boolean o) {
        this.overflow = o;
        if (o) target = MAX + 0.4;
    }

    public void reset() {
        strikes = 0;
        target = 0;
        shown = 0;
        overflow = false;
    }

    @Override public void tick(double dt) {
        time += dt;
        shown += (target - shown) * Math.min(1, dt * 5);
        repaint();
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int barW = 52;
        int x = (getWidth() - barW) / 2;
        int top = 20;
        int barH = getHeight() - 90;

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, top, barW, barH, 8, 8);

        double frac = Math.min(1, shown / MAX);
        int fillH = (int) (barH * frac);
        Color low = new Color(225, 200, 70), high = new Color(215, 55, 55);
        float f = (float) frac;
        g2.setColor(new Color((int) (low.getRed() * (1 - f) + high.getRed() * f),
                (int) (low.getGreen() * (1 - f) + high.getGreen() * f),
                (int) (low.getBlue() * (1 - f) + high.getBlue() * f)));
        g2.fillRoundRect(x, top + barH - fillH, barW, fillH, 8, 8);

        g2.setColor(new Color(0, 0, 0, 80));
        g2.setStroke(new BasicStroke(1.5f));
        for (int i = 1; i < MAX; i++) {
            int ty = top + barH - barH * i / MAX;
            g2.drawLine(x, ty, x + barW, ty);
        }
        g2.setColor(Theme.INK);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, top, barW, barH, 8, 8);

        if (overflow) {
            g2.setColor(new Color(110, 170, 40));
            for (int i = 0; i < 5; i++) {
                int bx = x + 4 + i * 10;
                int r = 10 + (int) (Math.abs(Math.sin(time * 3 + i)) * 8);
                g2.fillOval(bx - r / 2, top - r / 2 - (int) (Math.abs(Math.sin(time * 4 + i * 2)) * 14), r, r);
            }
        }

        g2.setFont(Theme.font(Font.BOLD, 12));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(Theme.INK);
        String label = "Stink-O-Meter";
        g2.drawString(label, (getWidth() - fm.stringWidth(label)) / 2, top + barH + 24);
        String s = strikes + " / " + MAX;
        g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, top + barH + 42);
        g2.dispose();
    }
}
