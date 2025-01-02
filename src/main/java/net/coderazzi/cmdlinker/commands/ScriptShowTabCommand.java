package net.coderazzi.cmdlinker.commands;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.ScriptProcessorListener;

/**
 * Command to show an specific tab syntax: showTab [name|number]
 */
public class ScriptShowTabCommand implements ScriptCommand {

    @Override
    public String getCommand() {
        return "SHOW";
    }

    @Override
    public void execute(ScriptProcessorListener target, String commandLine)
            throws ScriptCommandException {
        target.showTab(commandLine);
    }

}
