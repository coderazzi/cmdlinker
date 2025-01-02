package net.coderazzi.cmdlinker.commands;

import java.awt.Font;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.ScriptProcessorListener;

import net.coderazzi.cmdlinker.gui.CmdLinker;

/**
 * Command to accepts the font command line Font command line has syntax: font
 * [name [style]] size Where size is a number, and style is one of: plain, bold,
 * italic, bold&italic the font can have spaces, but then quotes must be used
 */
public class ScriptFontCommand implements ScriptCommand {
    private final Pattern pattern = Pattern
            .compile(
                    "^(?:(\"[^\"]+\"|\\w+)(?:\\s+([a-z]+))?\\s+)?(\\d+)$",
                    Pattern.CASE_INSENSITIVE);

    private final HashMap<String, Integer> styles = new HashMap<>();

    public ScriptFontCommand() {
        styles.put("plain", Font.PLAIN);
        styles.put("bold", Font.BOLD);
        styles.put("italic", Font.ITALIC);
        styles.put("bold&italic", Font.BOLD | Font.ITALIC);
    }

    @Override
    public String getCommand() {
        return "FONT";
    }

    @Override
    public void execute(ScriptProcessorListener target, String commandLine)
            throws ScriptCommandException {
        Matcher match = pattern.matcher(commandLine);
        if (!match.matches())
            throw new ScriptCommandException(
                    "<<html>Invalid font syntax<br>FONT [name [style]] size</html>");
        target.setTextFont(getFont(match.group(1), match.group(2), match
                .group(3)));
    }

    private Font getFont(String name, String style, String size)
            throws ScriptCommandException {
        int s = Integer.parseInt(size);
        int st = Font.PLAIN;
        if (style != null) {
            Integer v = styles.get(style.toLowerCase());
            if (v == null)
                throw new ScriptCommandException("Invalid style: " + style);
            st = v;
        }
        if (name == null)
            name = CmdLinker.DEFAULT_FONT_FAMILY;
        return new Font(name, st, s);
    }

}
