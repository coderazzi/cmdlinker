package com.byteslooser.cmdlinker.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.byteslooser.cmdlinker.ScriptCommandException;
import com.byteslooser.cmdlinker.ScriptProcessorListener;

/**
 * Command to wait some time Syntax: wait time (in milliseconds)
 */
public class ScriptWaitCommand extends ScriptCommand {
    private Pattern pattern = Pattern.compile("^wait\\s+(\\d+)$",
            Pattern.CASE_INSENSITIVE);

    public ScriptWaitCommand() {
        super("wait");
    }

    @Override
    public void execute(ScriptProcessorListener target, String commandLine)
            throws ScriptCommandException {
        Matcher match = pattern.matcher(commandLine);
        if (!match.matches())
            throw new ScriptCommandException(
                    "Invalid wait syntax: wait milliseconds");
        target.waitTime(Long.valueOf(match.group(1)).longValue());
    }

}
