package net.coderazzi.cmdlinker.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.Appearance;
import net.coderazzi.cmdlinker.gui.CmdLinker;

/**
 * Command to accepts the scroll command line Scroll command line has syntax:
 * scroll on|auto|lock|off
 */
public class ScriptScrollCommand implements ScriptCommand {
    private final Pattern pattern = Pattern.compile("^(on|off)$",
            Pattern.CASE_INSENSITIVE);

    @Override
    public String getCommand() {
        return "SCROLL";
    }

    @Override
    public void execute(String parameters, CmdLinker target, Appearance settings)
            throws ScriptCommandException {
        Matcher match = pattern.matcher(parameters);
        if (!match.matches())
            throw new ScriptCommandException(
                    "<html>Invalid scroll syntax<br>SCROLL on|off</html>");
        settings.setAutoScroll(match.group(1).equals("on"));
    }

}
