package net.coderazzi.cmdlinker.commands;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.Appearance;
import net.coderazzi.cmdlinker.gui.CmdLinker;

/**
 * Command to create a new tab Syntax: createTab [name]
 */
public class ScriptTabCommand implements ScriptCommand {

    @Override
    public String getCommand() {
        return "TAB";
    }

    @Override
    public void execute(String parameters, CmdLinker target, Appearance settings)
            throws ScriptCommandException {
        settings.setTitle(parameters);
    }
}
