package net.coderazzi.cmdlinker;

import java.awt.Color;
import java.util.HashMap;

public abstract class ColorString {
    private static final HashMap<String, Color> colors = new HashMap<>();

    static {
        colors.put("black", Color.black);
        colors.put("blue", Color.blue);
        colors.put("cyan", Color.cyan);
        colors.put("darkGray", Color.darkGray);
        colors.put("gray", Color.gray);
        colors.put("green", Color.green);
        colors.put("lightGray", Color.lightGray);
        colors.put("magenta", Color.magenta);
        colors.put("orange", Color.orange);
        colors.put("pink", Color.pink);
        colors.put("red", Color.red);
        colors.put("white", Color.white);
        colors.put("yellow", Color.yellow);
    }

    public static String getColor(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(),
                color.getBlue());
    }

    public static Color getColor(String parameter) {
        try {
            return Color.decode(parameter);
        } catch (NumberFormatException nfe) {
            return colors.get(parameter.toLowerCase());
        }
    }

    public static Color getValidColor(String parameter) throws ScriptCommandException {
        Color color = getColor(parameter);
        if (color == null)
            throw new ScriptCommandException("Invalid color: " + parameter);
        return color;
    }

}
