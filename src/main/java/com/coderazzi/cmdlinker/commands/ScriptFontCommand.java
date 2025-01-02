package com.byteslooser.cmdlinker.commands;

import java.awt.Font;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.byteslooser.cmdlinker.CmdLinker;
import com.byteslooser.cmdlinker.ScriptCommandException;
import com.byteslooser.cmdlinker.ScriptProcessorListener;

/**
 * Command to accepts the font command line Font command line has syntax: font
 * [name [style]] size Where sizeis a number, and style is one of: plain, bold,
 * italic, bold&italic the font can have spaces, but then quotes must be used
 */
public class ScriptFontCommand extends ScriptCommand {
    private Pattern pattern = Pattern
            .compile(
                    "^font\\s+(?:((?:\"[^\"]+\")|(?:\\w+))(?:\\s+([a-z]+))?\\s+)?(\\d+)$",
                    Pattern.CASE_INSENSITIVE);

    private HashMap<String, Integer> styles = new HashMap<String, Integer>();

    public ScriptFontCommand() {
        super("font");
        styles.put("plain", Font.PLAIN);
        styles.put("bold", Font.BOLD);
        styles.put("italic", Font.ITALIC);
        styles.put("bold&italic", Font.BOLD | Font.ITALIC);
    }

    @Override
    public void execute(ScriptProcessorListener target, String commandLine)
            throws ScriptCommandException {
        Matcher match = pattern.matcher(commandLine);
        if (!match.matches())
            throw new ScriptCommandException(
                    "Invalid font syntax: font [name [style]] size");
        target.setTextFont(getFont(match.group(1), match.group(2), match
                .group(3)));
    }

    private Font getFont(String name, String style, String size)
            throws ScriptCommandException {
        int s = Integer.valueOf(size);
        int st = Font.PLAIN;
        if (style != null) {
            Integer v = styles.get(style.toLowerCase());
            if (v == null)
                throw new ScriptCommandException("Invalid style: " + style);
            st = v.intValue();
        }
        if (name == null)
            name = CmdLinker.DEFAULT_FONT_FAMILY;
        return new Font(name, st, s);
    }

}
