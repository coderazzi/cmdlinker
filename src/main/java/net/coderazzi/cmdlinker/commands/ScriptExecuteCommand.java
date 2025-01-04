package net.coderazzi.cmdlinker.commands;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.Appearance;
import net.coderazzi.cmdlinker.gui.CmdLinker;

/**
 * Command to execute a new command Syntax [&execute] command [parameters]
 */
public class ScriptExecuteCommand implements ScriptCommand {

    @Override
    public String getCommand() {
        return "EXECUTE";
    }

    @Override
    public void execute(String parameters, CmdLinker target, Appearance settings)
            throws ScriptCommandException {
        target.execute(parameters, settings);
    }

}
