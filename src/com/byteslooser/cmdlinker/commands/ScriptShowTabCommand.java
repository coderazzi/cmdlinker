package com.byteslooser.cmdlinker.commands;

import com.byteslooser.cmdlinker.ScriptCommandException;
import com.byteslooser.cmdlinker.ScriptProcessorListener;

/**
 * Command to show an specific tab syntax: showTab [name|number]
 */
public class ScriptShowTabCommand extends ScriptCommand {
    public ScriptShowTabCommand() {
        super("showTab");
    }

    @Override
    public void execute(ScriptProcessorListener target, String commandLine)
            throws ScriptCommandException {
        String rest = commandLine.substring(getCommand().length()).trim();
        target.showTab(getUnquotedString(rest));
    }

}
