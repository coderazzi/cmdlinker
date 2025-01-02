package net.coderazzi.cmdlinker.commands;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.ScriptProcessorListener;

import net.coderazzi.cmdlinker.candy.ColorString;

/**
 * Command to accepts the color command line Color command line has syntax:
 * color foreground background Where each color is one of: -well known color:
 * black, white, etc. -RGB color, preceeded by 0x or #. The color is then given
 * with 3 or 6 digits
 */
public class ScriptColorsCommand implements ScriptCommand {
    private Pattern pattern = Pattern.compile("^(\\S+)\\s+(\\S+)$",
            Pattern.CASE_INSENSITIVE);

    @Override
    public String getCommand() {
        return "COLORS";
    }

    @Override
    public void execute(ScriptProcessorListener target, String commandLine)
            throws ScriptCommandException {
        Matcher match = pattern.matcher(commandLine);
        if (!match.matches())
            throw new ScriptCommandException(
                    "<html>Invalid colors syntax<br>COLORS foreground background</html>");
        target.setColors(getColor(match.group(1)), getColor(match.group(2)));
    }

    private Color getColor(String parameter) throws ScriptCommandException {
        Color color = ColorString.getColor(parameter);
        if (color == null)
            throw new ScriptCommandException("Invalid color: " + parameter);
        return color;
    }

}
