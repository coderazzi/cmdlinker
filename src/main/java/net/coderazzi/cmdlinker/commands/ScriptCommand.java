package net.coderazzi.cmdlinker.commands;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.Appearance;
import net.coderazzi.cmdlinker.gui.CmdLinker;

public interface ScriptCommand {
    String getCommand();

    void execute(String parameters, CmdLinker target, Appearance settings)
        throws ScriptCommandException;

}
