package net.coderazzi.cmdlinker.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.ScriptProcessorListener;

/**
 * Command to create a new tab Syntax: createTab [name]
 */
public class ScriptCreateTabCommand extends ScriptCommand {
    private Pattern pattern = Pattern.compile(
            "^createTab(?:\\s+([^\\s].*?))?\\s*$", Pattern.CASE_INSENSITIVE);

    public ScriptCreateTabCommand() {
        super("createTab");
    }

    @Override
    public void execute(ScriptProcessorListener target, String commandLine)
            throws ScriptCommandException {
        Matcher match = pattern.matcher(commandLine);
        if (!match.matches())
            throw new ScriptCommandException(
                    "Invalid createTab syntax : internal error");
        String name = getUnquotedString(match.group(1));
        target.createTab(name);
    }
}
