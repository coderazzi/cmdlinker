package net.coderazzi.cmdlinker.commands;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.ScriptProcessorListener;

/**
 * Command to wait some time Syntax: wait time (in milliseconds)
 */
public class ScriptWaitCommand implements ScriptCommand {

    @Override
    public String getCommand() {
        return "WAIT";
    }
    @Override
    public void execute(ScriptProcessorListener target, String commandLine)
            throws ScriptCommandException {
        try {
            target.waitTime(Long.valueOf(commandLine).longValue());
        }catch (NumberFormatException e){
            throw new ScriptCommandException(
                    "Invalid wait parameter: " + commandLine);
        }
    }

}
