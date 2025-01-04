package net.coderazzi.cmdlinker.commands;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.Appearance;
import net.coderazzi.cmdlinker.gui.CmdLinker;

/**
 * Command to wait some time Syntax: wait time (in milliseconds)
 */
public class ScriptWaitCommand implements ScriptCommand {

    @Override
    public String getCommand() {
        return "WAIT";
    }
    @Override
    public void execute(String parameters, CmdLinker target, Appearance settings)
            throws ScriptCommandException {
        try {
            target.waitTime(Long.parseLong(parameters));
        }catch (NumberFormatException e){
            throw new ScriptCommandException(
                    "Invalid wait parameter: " + parameters);
        }
    }

}
