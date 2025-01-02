package net.coderazzi.cmdlinker.commands;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.ScriptProcessorListener;

public interface ScriptCommand {
    String getCommand();

    void execute(ScriptProcessorListener target, String commandLine) 
        throws ScriptCommandException;

}
