package net.coderazzi.cmdlinker.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.ScriptProcessorListener;

/**
 * Command to execute a new command Syntax [&execute] command [parameters]
 */
public class ScriptExecuteCommand extends ScriptCommand {
    private Pattern pattern = Pattern.compile(
            "^(?:&execute\\s+)?([^\\s].*?)\\s*$", Pattern.CASE_INSENSITIVE);

    public ScriptExecuteCommand() {
        super("&execute");
    }

    @Override
    public void execute(ScriptProcessorListener target, String commandLine)
            throws ScriptCommandException {
        Matcher match = pattern.matcher(commandLine);
        if (!match.matches())
            throw new ScriptCommandException(
                    "Invalid execute syntax: internal error");
        target.execute(match.group(1));
    }

}
