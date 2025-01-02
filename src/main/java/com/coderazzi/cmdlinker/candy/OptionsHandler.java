package com.byteslooser.cmdlinker.candy;

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

import com.byteslooser.cmdlinker.CmdLinker;

public class OptionsHandler {
    private final static String HELP_OPTION = "-help";

    private final static String CHECK_OPTION = "-check";

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

    private Color background, foreground;

    private int fontSize;

    private String lastScript, lastCommand, lastCommandDir;

    private int x = -1, y = -1, w = -1, h = -1;

    private boolean usingPersistence;

    private boolean isCommand = false;

    private boolean isCheck = false;

    private String script;

    private String argumentsError;

    private List<CmdLinker> clients = new ArrayList<CmdLinker>();

    static public OptionsHandler createOptionsHandler(String[] args) {
        OptionsHandler oh = new OptionsHandler();
        oh.handleInitialArgs(args);
        return oh;
    }

    public String getInitialArgumentsStringError() {
        return argumentsError;
    }

    public void setDisplayProperties(Color foreground, Color background,
            int fontSize) {
        if (foreground != null)
            this.foreground = foreground;
        if (background != null)
            this.background = background;
        this.fontSize = fontSize;
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

    public void registerClient(final CmdLinker parent, final CmdLinker client) {
        clients.add(client);
        client.setOptionsHandler(this);
        client.setPersistOptions(usingPersistence);
        if (parent == null) {
            client.setColors(foreground, background);
            if (fontSize >= 6)
                client.setInitFontSize(fontSize);
            if (script != null) {
                if (isCheck)
                    client.checkScript(script);
                else if (isCommand)
                    client.processCommand(script);
                else
                    client.processScript(script);
            }
            if (x == -1)
                positionWindow(client, 20, 20, 600, 500);
            else
                positionWindow(client, x, y, w, h);
        } else {
            Point pp = parent.getLocation();
            Dimension size = parent.getSize();
            positionWindow(client, pp.x + 20, pp.y + 20, size.width,
                    size.height);
        }
        client.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                w = client.getSize().width;
                h = client.getSize().height;
                x = client.getLocation().x;
                y = client.getLocation().y;
            }

            public void windowClosed(WindowEvent e) {
                clients.remove(client);
                if (clients.size() == 0) {
                    if (usingPersistence)
                        savePropertiesFile();
                    else
                        removePropertiesFile();
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

    public void persistOptions(boolean enable) {
        if (enable != usingPersistence) {
            usingPersistence = enable;
            for (CmdLinker client : clients)
                client.setPersistOptions(enable);
        }
    }

    private OptionsHandler() {
        readPropertiesFile();
    }

    private void decodeProperties(Properties props) {
        background = ColorString.getColor(props
                .getProperty(PROP_BACKGROUND, ""));
        foreground = ColorString.getColor(props
                .getProperty(PROP_FOREGROUND, ""));
        fontSize = getIntProperty(props.getProperty(PROP_FONT));
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
        if (background != null)
            ret.put(PROP_BACKGROUND, ColorString.getColor(background));
        if (foreground != null)
            ret.put(PROP_FOREGROUND, ColorString.getColor(foreground));
        if (fontSize != -1)
            ret.put(PROP_FONT, String.valueOf(fontSize));
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
                return Integer.valueOf(value).intValue();
            } catch (NumberFormatException nf) {
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
            FileOutputStream fos = new FileOutputStream(getPropertyFile());
            try {
                encodeProperties().store(fos, "CmdLinker configuration file");
            } finally {
                fos.close();
            }
        } catch (IOException ex) {
            System.out.println("Sorry, problem while storing options");
        }
    }

    private void readPropertiesFile() {
        try {
            FileInputStream fis = new FileInputStream(getPropertyFile());
            try {
                Properties props = new Properties();
                props.load(fis);
                decodeProperties(props);
            } finally {
                fis.close();
            }
        } catch (IOException ex) {
        }
    }

    private File getPropertyFile() {
        return new File(System.getProperty("user.home", ""), "cmdlinker.conf");
    }

    private void handleInitialArgs(String[] args) {
        int i = 0;
        while (i < args.length && argumentsError == null) {
            if (args[i].equals(HELP_OPTION)) {
                argumentsError = getHelpString();
            } else if (args[i].equals(FOREGROUND_OPTION)) {
                if (++i == args.length)
                    argumentsError = "Color not provided";
                else {
                    String color = args[i++];
                    this.foreground = ColorString.getColor(color);
                    if (this.foreground == null)
                        argumentsError = "Invalid color:" + color;
                }
            } else if (args[i].equals(BACKGROUND_OPTION)) {
                if (++i == args.length)
                    argumentsError = "Color not provided";
                else {
                    String color = args[i++];
                    this.background = ColorString.getColor(color);
                    if (this.background == null)
                        argumentsError = "Invalid color:" + color;
                }
            } else if (args[i].equals(FONT_OPTION)) {
                if (++i == args.length)
                    argumentsError = "Font size not provided";
                else {
                    String font = args[i++];
                    try {
                        this.fontSize = Integer.valueOf(font).intValue();
                    } catch (NumberFormatException nfe) {
                        argumentsError = "Invalid font size:" + font;
                    }
                }
            } else {
                String first = args[i++];
                if (first.equals(COMMAND_OPTION))
                    this.isCommand = true;
                else if (first.equals(CHECK_OPTION))
                    this.isCheck = true;
                else
                    --i;

                if (i == args.length)
                    argumentsError = "Script or command not provided";
                else if (!this.isCommand && (i + 1 != args.length))
                    argumentsError = "A script cannot be provided with arguments";
                else {
                    this.script = args[i++];
                    while (i < args.length)
                        this.script += " " + args[i++];
                }
            }
        }
    }

    private String getHelpString() {
        return String
                .format(
                        "Syntax: [%s] [%s color] [%s color] [%s fontSize] [%s|%s] scriptOrCommand"
                                + "\n  Using %s does not execute the programs in command"
                                + "\n  With the argument %s, the following arguments are treated as a command",
                        HELP_OPTION, FOREGROUND_OPTION, BACKGROUND_OPTION,
                        FONT_OPTION, CHECK_OPTION, COMMAND_OPTION,
                        CHECK_OPTION, COMMAND_OPTION);
    }

}
