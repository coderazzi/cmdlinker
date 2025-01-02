package com.byteslooser.cmdlinker.commands;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.byteslooser.cmdlinker.ScriptCommandException;
import com.byteslooser.cmdlinker.ScriptProcessorListener;
import com.byteslooser.cmdlinker.candy.ColorString;

/**
 * Command to accepts the color command line Color command line has syntax:
 * color foreground background Where each color is one of: -well known color:
 * black, white, etc. -RGB color, preceeded by 0x or #. The color is then given
 * with 3 or 6 digits
 */
public class ScriptColorsCommand extends ScriptCommand {
    private Pattern pattern = Pattern.compile("^colors\\s+(\\w+)\\s+(\\w+)$",
            Pattern.CASE_INSENSITIVE);

    public ScriptColorsCommand() {
        super("colors");
    }

    @Override
    public void execute(ScriptProcessorListener target, String commandLine)
            throws ScriptCommandException {
        Matcher match = pattern.matcher(commandLine);
        if (!match.matches())
            throw new ScriptCommandException(
                    "Invalid colors syntax: colors foreground background");
        target.setColors(getColor(match.group(1)), getColor(match.group(2)));
    }

    private Color getColor(String parameter) throws ScriptCommandException {
        Color color = ColorString.getColor(parameter);
        if (color == null)
            throw new ScriptCommandException("Invalid color: " + parameter);
        return color;
    }

}
