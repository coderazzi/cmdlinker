package net.coderazzi.cmdlinker.commands;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.Appearance;
import net.coderazzi.cmdlinker.gui.CmdLinker;

/**
 * Command to show a specific tab syntax: showTab [name|number]
 */
public class ScriptShowCommand implements ScriptCommand {

    @Override
    public String getCommand() {
        return "SHOW";
    }

    @Override
    public void execute(String parameters, CmdLinker target, Appearance settings)
            throws ScriptCommandException {
        target.showTab(parameters);
    }

}
