package com.byteslooser.cmdlinker.commands;

import com.byteslooser.cmdlinker.ScriptCommandException;
import com.byteslooser.cmdlinker.ScriptProcessorListener;

public abstract class ScriptCommand {
    private String command;

    public ScriptCommand(String command) {
        this.command = command.toLowerCase();
    }

    public String getCommand() {
        return command;
    }

    public abstract void execute(ScriptProcessorListener target,
            String commandLine) throws ScriptCommandException;

    protected String getUnquotedString(String s) {
        if (s != null) {
            if (s.length() > 1 && s.charAt(0) == '"'
                    && s.charAt(s.length() - 1) == '"') {
                s = s.substring(1, s.length() - 1);
            }
        }
        return s;
    }
}
