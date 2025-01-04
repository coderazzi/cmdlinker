package net.coderazzi.cmdlinker;

import java.awt.*;

public class Appearance {
    public final static String DEFAULT_FONT_FAMILY = "Monospaced";
    private Color foreground;
    private Color background;
    private String title;
    private Font font;
    private boolean autoScroll;

    public Appearance()  {
        font =  new Font(DEFAULT_FONT_FAMILY, Font.PLAIN, 11);
        autoScroll = true;
        foreground = Color.BLACK;
        background = Color.WHITE;
    }

    public Appearance(Appearance appearance)  {
        foreground = appearance.foreground;
        background = appearance.background;
        title = appearance.title;
        font = appearance.font;
        autoScroll = appearance.autoScroll;
    }

    public Color getForeground() {
        return foreground;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title==null? null : title.trim();
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public boolean isAutoScroll() {
        return autoScroll;
    }

    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }

    public void setFontSize(int size) {
        this.font = font.deriveFont((float) size);
    }
}
