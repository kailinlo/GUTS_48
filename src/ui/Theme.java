package ui;

import java.awt.Color;
import java.awt.Font;

/** Colours and fonts in one place. */
public final class Theme {
    private Theme() {}

    public static final Color BG        = new Color(203, 222, 189);
    public static final Color PANEL     = new Color(240, 241, 243);
    public static final Color WHITE     = Color.WHITE;
    public static final Color INK       = new Color(30, 30, 30);
    public static final Color GOOD      = new Color(25, 130, 55);
    public static final Color BAD       = new Color(190, 35, 35);
    public static final Color EDGE      = new Color(170, 172, 178);
    public static final Color VISITED   = new Color(190, 235, 195);
    public static final Color ROUTE     = new Color(60, 140, 230);
    public static final Color HOT       = new Color(240, 160, 30);
    public static final Color FLASH_GOOD = new Color(120, 230, 150);
    public static final Color FLASH_BAD  = new Color(240, 110, 110);

    public static Font font(int style, int size) {
        return new Font(Font.MONOSPACED, style, size);
    }
}
