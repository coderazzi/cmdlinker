package net.coderazzi.cmdlinker.commands;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.ScriptProcessorListener;

/**
 * Command to create a new tab Syntax: createTab [name]
 */
public class ScriptCreateTabCommand implements ScriptCommand {

    @Override
    public String getCommand() {
        return "TAB";
    }

    @Override
    public void execute(ScriptProcessorListener target, String commandLine)
            throws ScriptCommandException {
        target.createTab(commandLine);
    }
}
