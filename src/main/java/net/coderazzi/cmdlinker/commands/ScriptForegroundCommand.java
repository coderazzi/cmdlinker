package net.coderazzi.cmdlinker.commands;

import net.coderazzi.cmdlinker.ScriptCommandException;

import net.coderazzi.cmdlinker.ColorString;
import net.coderazzi.cmdlinker.Appearance;
import net.coderazzi.cmdlinker.gui.CmdLinker;

/**
 * Command to accepts the color command line
 * Color is one of:
 * -well known color:  black, white, etc.
 * -RGB color, preceded by 0x or #. The color is then give with 3 or 6 digits
 */
public class ScriptForegroundCommand implements ScriptCommand {

    @Override
    public String getCommand() {
        return "FOREGROUND";
    }

    @Override
    public void execute(String parameters, CmdLinker target, Appearance settings)
            throws ScriptCommandException {
        settings.setForeground(ColorString.getValidColor(parameters));
    }

}
