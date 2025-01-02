package net.coderazzi.cmdlinker.commands;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.ScriptProcessorListener;

/**
 * Command to execute a new command Syntax [&execute] command [parameters]
 */
public class ScriptExecuteCommand implements ScriptCommand {

    @Override
    public String getCommand() {
        return "EXECUTE";
    }

    @Override
    public void execute(ScriptProcessorListener target, String commandLine)
            throws ScriptCommandException {
        target.execute(commandLine);
    }

}
