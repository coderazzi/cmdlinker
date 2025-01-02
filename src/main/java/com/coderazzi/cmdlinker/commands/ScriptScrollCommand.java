package com.byteslooser.cmdlinker.commands;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.byteslooser.cmdlinker.ScriptCommandException;
import com.byteslooser.cmdlinker.ScriptProcessorListener;

/**
 * Command to accepts the scroll command line Scroll command line has syntax:
 * scroll on|auto|lock|off
 */
public class ScriptScrollCommand extends ScriptCommand {
    private Pattern pattern = Pattern.compile("^scroll\\s+([a-z]+)$",
            Pattern.CASE_INSENSITIVE);

    private HashMap<String, Boolean> pars = new HashMap<String, Boolean>();

    public ScriptScrollCommand() {
        super("scroll");
        pars.put("on", true);
        pars.put("off", false);
        pars.put("auto", true);
        pars.put("lock", false);
    }

    @Override
    public void execute(ScriptProcessorListener target, String commandLine)
            throws ScriptCommandException {
        Matcher match = pattern.matcher(commandLine);
        if (!match.matches())
            throw new ScriptCommandException(
                    "Invalid scroll syntax: scroll on|auto|lock|off");
        target.setAutoScroll(getAutoScroll(match.group(1)));
    }

    private boolean getAutoScroll(String par) throws ScriptCommandException {
        Boolean v = pars.get(par);
        if (v == null)
            throw new ScriptCommandException("Invalid scroll parameter: " + par);
        return v.booleanValue();
    }

}
