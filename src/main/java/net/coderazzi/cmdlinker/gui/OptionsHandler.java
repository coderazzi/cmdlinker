package net.coderazzi.cmdlinker.gui;

import net.coderazzi.cmdlinker.Appearance;
import net.coderazzi.cmdlinker.ColorString;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class OptionsHandler {
    private final static String HELP_OPTION = "-help";
    private final static String COMMAND_OPTION = "-c";
    private final static String FOREGROUND_OPTION = "-fg";
    private final static String BACKGROUND_OPTION = "-bg";
    private final static String FONT_OPTION = "-font";
    private static final String PROP_BACKGROUND = "bgColor";
    private static final String PROP_FOREGROUND = "fgColor";
    private static final String PROP_FONT = "fontSize";
    private static final String PROP_SCRIPT = "script";
    private static final String PROP_COMMAND = "command";
    private static final String PROP_COMMAND_DIR = "commandDir";
    private static final String PROP_X = "x";
    private static final String PROP_Y = "y";
    private static final String PROP_W = "w";
    private static final String PROP_H = "h";

    private Appearance appearance = new Appearance();
    private String lastScript, lastCommand, lastCommandDir;
    private int x = -1, y = -1, w = -1, h = -1;
    private boolean usingPersistence;
    private boolean parameterCommand = false;
    private String parameterScript;
    private String argumentsError;

    private final List<CmdLinker> windows = new ArrayList<>();

    static public OptionsHandler createOptionsHandler(String[] args) {
        OptionsHandler oh = new OptionsHandler();
        oh.handleInitialArgs(args);
        return oh;
    }

    public String getInitialArgumentsStringError() {
        return argumentsError;
    }

    public void setAppearance(Appearance appearance) {
        this.appearance = appearance;
    }

    public Appearance getAppearance() {
        return appearance;
    }

    public String getLastCommandDirectory() {
        return lastCommandDir;
    }

    public void setLastCommandDirectory(String which) {
        lastCommandDir = which;
    }

    public String getLastCommand() {
        return lastCommand;
    }

    public void setLastCommand(String which) {
        lastCommand = which;
    }

    public String getLastScript() {
        return lastScript;
    }

    public void setLastScript(String which) {
        lastScript = which;
    }

    public String getScript() {
        return parameterScript;
    }

    public boolean isCommand() {
        return parameterCommand;
    }

    public void registerWindow(final CmdLinker parent, final CmdLinker child) {
        windows.add(child);
        child.setOptionsHandler(this);
        child.setPersistOptions(usingPersistence);
        if (parent == null) {
            if (x == -1)
                positionWindow(child, 20, 20, 600, 500);
            else
                positionWindow(child, x, y, w, h);
        } else {
            Point pp = parent.getLocation();
            Dimension size = parent.getSize();
            positionWindow(child, pp.x + 20, pp.y + 20, size.width, size.height);
        }
        child.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                w = child.getSize().width;
                h = child.getSize().height;
                x = child.getLocation().x;
                y = child.getLocation().y;
            }

            public void windowClosed(WindowEvent e) {
                windows.remove(child);
                if (windows.isEmpty()) {
                    if (usingPersistence) {
                        savePropertiesFile();
                    }
                    else {
                        removePropertiesFile();
                    }
                    System.exit(0);
                }
            }
        });
    }

    private void positionWindow(Window window, int x, int y, int w, int h) {
        Dimension bounds = Toolkit.getDefaultToolkit().getScreenSize();
        h = Math.max(100, Math.min(h, bounds.height));
        w = Math.max(100, Math.min(w, bounds.width));
        y = Math.max(0, Math.min(y, bounds.height - h));
        x = Math.max(0, Math.min(x, bounds.width - w));
        window.setSize(w, h);
        window.setLocation(x, y);
    }

    public void updatePersistOptions(boolean enable) {
        if (enable != usingPersistence) {
            usingPersistence = enable;
            for (CmdLinker window : windows)
                window.setPersistOptions(enable);
        }
    }

    private OptionsHandler() {
        readPropertiesFile();
    }

    private void decodeProperties(Properties props) {
        appearance.setBackground(ColorString.getColor(props.getProperty(PROP_BACKGROUND, "")));
        appearance.setForeground(ColorString.getColor(props.getProperty(PROP_FOREGROUND, "")));
        appearance.setFontSize(getIntProperty(props.getProperty(PROP_FONT)));
        x = getIntProperty(props.getProperty(PROP_X, "-1"));
        y = getIntProperty(props.getProperty(PROP_Y, "-1"));
        w = getIntProperty(props.getProperty(PROP_W, "-1"));
        h = getIntProperty(props.getProperty(PROP_H, "-1"));
        if (x == -1 || y == -1 || w == -1 || h == -1)
            x = y = w = h = -1;
        lastScript = props.getProperty(PROP_SCRIPT);
        lastCommand = props.getProperty(PROP_COMMAND);
        lastCommandDir = props.getProperty(PROP_COMMAND_DIR);
        usingPersistence = true;
    }

    private Properties encodeProperties() {
        Properties ret = new Properties();
        ret.put(PROP_BACKGROUND, ColorString.getColor(appearance.getBackground()));
        ret.put(PROP_FOREGROUND, ColorString.getColor(appearance.getForeground()));
        ret.put(PROP_FONT, String.valueOf(appearance.getFont().getSize()));
        if (x != -1)
            ret.put(PROP_X, String.valueOf(x));
        if (y != -1)
            ret.put(PROP_Y, String.valueOf(y));
        if (w != -1)
            ret.put(PROP_W, String.valueOf(w));
        if (h != -1)
            ret.put(PROP_H, String.valueOf(h));
        if (lastScript != null)
            ret.put(PROP_SCRIPT, lastScript);
        if (lastCommand != null)
            ret.put(PROP_COMMAND, lastCommand);
        if (lastCommandDir != null)
            ret.put(PROP_COMMAND_DIR, lastCommandDir);
        return ret;
    }

    private int getIntProperty(String value) {
        if (value != null)
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException nf) {
                // will just return -1
            }
        return -1;
    }

    private void removePropertiesFile() {
        File f = getPropertyFile();
        if (f.exists())
            f.delete();
    }

    private void savePropertiesFile() {
        try {
            try (FileOutputStream fos = new FileOutputStream(getPropertyFile())) {
                encodeProperties().store(fos, "CmdLinker configuration file");
            }
        } catch (IOException ex) {
            System.out.println("Sorry, problem while storing options");
        }
    }

    private void readPropertiesFile() {
        try {
            try (FileInputStream fis = new FileInputStream(getPropertyFile())) {
                Properties props = new Properties();
                props.load(fis);
                decodeProperties(props);
            }
        } catch (IOException ex) {
            // do nothing if it cannot be read
        }
    }

    private File getPropertyFile() {
        return new File(System.getProperty("user.home", ""), ".cmdlinker.conf");
    }

    private void handleInitialArgs(String[] args) {
        int i = 0;
        while (i < args.length && argumentsError == null) {
            switch (args[i]) {
                case HELP_OPTION:
                    argumentsError = getHelpString();
                    break;
                case FOREGROUND_OPTION:
                    if (++i == args.length)
                        argumentsError = "Color not provided";
                    else {
                        String color = args[i++];
                        Color foreground = ColorString.getColor(color);
                        if (foreground == null)
                            argumentsError = "Invalid color:" + color;
                        appearance.setForeground(foreground);
                    }
                    break;
                case BACKGROUND_OPTION:
                    if (++i == args.length)
                        argumentsError = "Color not provided";
                    else {
                        String color = args[i++];
                        Color background = ColorString.getColor(color);
                        if (background == null)
                            argumentsError = "Invalid color:" + color;
                        appearance.setBackground(background);
                    }
                    break;
                case FONT_OPTION:
                    if (++i == args.length)
                        argumentsError = "Font size not provided";
                    else {
                        String font = args[i++];
                        try {
                            appearance.setFontSize(Integer.parseInt(font));
                        } catch (NumberFormatException nfe) {
                            argumentsError = "Invalid font size:" + font;
                        }
                    }
                    break;
                default:
                    String first = args[i++];
                    if (first.equals(COMMAND_OPTION))
                        this.parameterCommand = true;
                    else
                        --i;

                    if (i == args.length)
                        argumentsError = "Script or command not provided";
                    else if (!this.parameterCommand && (i + 1 != args.length))
                        argumentsError = "A script cannot be provided with arguments";
                    else {
                        this.parameterScript = args[i++];
                        while (i < args.length)
                            this.parameterScript += " " + args[i++];
                    }
                    break;
            }
        }
    }

    private String getHelpString() {
        return String.format(
            "Syntax: [%s] [%s color] [%s color] [%s fontSize] [script | %s command [...]]",
            HELP_OPTION, FOREGROUND_OPTION, BACKGROUND_OPTION,
            FONT_OPTION, COMMAND_OPTION);
    }

}
