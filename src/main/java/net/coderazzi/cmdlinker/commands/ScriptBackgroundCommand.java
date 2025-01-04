package net.coderazzi.cmdlinker.commands;

import net.coderazzi.cmdlinker.ColorString;
import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.ScriptProcessorListener;

/**
 * Command to accepts the color command line
 * Color is one of:
 * -well known color:  black, white, etc.
 * -RGB color, preceded by 0x or #. The color is then give with 3 or 6 digits
 */
public class ScriptBackgroundCommand implements ScriptCommand {

    @Override
    public String getCommand() {
        return "BACKGROUND";
    }

    @Override
    public void execute(ScriptProcessorListener target, String commandLine)
            throws ScriptCommandException {
        target.setBackground(ColorString.getValidColor(commandLine));
    }
}
