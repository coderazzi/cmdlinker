package net.coderazzi.cmdlinker.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.ScriptProcessor;
import net.coderazzi.cmdlinker.ScriptProcessorListener;
import net.coderazzi.cmdlinker.Version;

public class CmdLinker extends JFrame implements ScriptProcessorListener {
    public final static String DEFAULT_FONT_FAMILY = "Monospaced";

    private final List<Tab> tabs = new ArrayList<>();
    private final ScriptProcessor scriptProcessor;
    private final MainMenu menuBar;

    private JTabbedPane tabsPane;
    private Tab currentScriptTab;
    private ProcessingScriptSplashScreen splashScreen;
    private Font font, backupFont;
    private Color foreground, background, backupFg, backupBg;
    private boolean autoScroll = true, currentTabAvailable, checkingCommand;
    private transient boolean aborted, revertChangesIfAborted;
    private int firstScriptTab;
    private String lastScript, lastCommand;
    private RunScriptDialog runScriptDialog;
    private RunCommandDialog runCommandDialog;
    private OptionsHandler optionsHandler;
    private final Pattern separatorPattern = Pattern
            .compile("^(?:\"((\\\\.|[^\\\\\"])*)\"|([^\"]\\S*))\\s*(.*)$");

    public CmdLinker() {
        super("Cmd Linker");
        setIconImage(IconLoader.getIcon("cmdlinker").getImage());
        initFrame();
        pack();
        scriptProcessor = new ScriptProcessor(this);
        menuBar = new MainMenu(this);
        setJMenuBar(menuBar);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                if (isBusy()) {
                    if (JOptionPane.OK_OPTION == JOptionPane
                            .showConfirmDialog(
                                    CmdLinker.this,
                                    "There are commands on execution. Are you sure to exit?",
                                    "Closing", JOptionPane.OK_CANCEL_OPTION)) {
                        terminateAllConsoles();
                        return;
                    }
                    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                }
            }
        });

        tabsPane.addChangeListener(e -> {
            int index = tabsPane.getSelectedIndex();
            Tab tab = null;
            if (index != -1) {
                tab = tabs.get(index);
            }
            menuBar.setTabMenu(tab);
        });
    }

    protected CmdLinker(CmdLinker copy) {
        this();
        this.setOptionsHandler(copy.optionsHandler);
        this.lastScript = copy.lastScript;
        this.lastCommand = copy.lastCommand;
        this.font = copy.font;
        this.background = copy.background;
        this.foreground = copy.foreground;
        this.runScriptDialog.copyParameters(copy.runScriptDialog);
        this.runCommandDialog.copyParameters(copy.runCommandDialog);
        copy.optionsHandler.registerClient(copy, this);
    }

    public void setOptionsHandler(OptionsHandler optionsHandler) {
        assert this.optionsHandler == null;
        this.optionsHandler = optionsHandler;
        menuBar.setOptionsHandler(optionsHandler);
        runScriptDialog = new RunScriptDialog(this, optionsHandler);
        runCommandDialog = new RunCommandDialog(this, optionsHandler);
    }

    /**
     * Called from the Gui
     */
    public void changeDisplay() {
        DisplayDialog cd = new DisplayDialog(this, foreground, background, font);
        cd.setVisible(true);
        if (cd.okPressed()) {
            foreground = cd.getForegroundChoice();
            background = cd.getBackgroundChoice();
            font = cd.getFontChoice();
            optionsHandler.setDisplayProperties(foreground, background, font
                    .getSize());
            if (!tabs.isEmpty()) {
                if (JOptionPane.YES_OPTION == JOptionPane
                        .showConfirmDialog(
                                this,
                                "Set up these colors on existing consoles?"
                                        + "\n(Answering 'No' implies that the colors are only used on new consoles)",
                                "Colors change", JOptionPane.YES_NO_OPTION)) {
                    for (Tab tab : tabs) {
                        tab.setColors(foreground, background);
                        tab.setTextFont(font);
                    }
                }
            }
        }
    }

    public void setPersistOptions(boolean persist) {
        menuBar.saveOptions(persist);
    }

    /**
     * Called from the Gui thread by a sub tab to get closed
     */
    public void closeTab(Tab tab) {
        int index = tabs.indexOf(tab);
        if (index != -1) {
            tabsPane.removeTabAt(index);
            tabs.remove(index);
            if (tabsPane.getTabCount() == 0) {
                menuBar.setTabMenu(null);
                menuBar.enableCloseAll(false);
            } else if (index >= tabsPane.getTabCount()) {
                tabsPane.setSelectedIndex(index - 1);
            } else {
                menuBar.setTabMenu(tabs.get(index));
            }
        }
    }

    /**
     * Called from the Gui thread by a sub tab to get closed
     */
    public void renameTab(Tab tab, String name) {
        int index = tabs.indexOf(tab);
        if (index != -1) {
            tabsPane.setTitleAt(index, name);
        }
    }

    /**
     * Called from Gui
     */
    public void abortScript(boolean revertAllChanges) {
        aborted = true;
        revertChangesIfAborted = revertAllChanges;
        scriptProcessor.abort();
    }

    /**
     * Called from Gui
     */
    public void newCommand() {
        String command = runCommandDialog.getCommand(lastCommand);
        if (command != null) {
            lastCommand = command;
            processCommand(command);
        }
    }

    /**
     * Called from Gui
     */
    public void newWindow() {
        new CmdLinker(this).setVisible(true);
    }

    /**
     * Called from Gui
     */
    public void processCommand(String command) {
        menuBar.enableRunLastScript(false);
        lastScript = null;
        lastCommand = command;
        firstScriptTab = tabs.size();
        try {
            execute(command);
            showTab("1");
        } catch (ScriptCommandException sce) {
            JOptionPane.showMessageDialog(this, sce.getMessage(),
                    "Invalid command", JOptionPane.ERROR_MESSAGE);
        }
        currentScriptTab = null;
    }

    /**
     * Called from Gui
     */
    public void checkScript(String name) {
        checkingCommand = true;
        processScript(name);
    }

    /**
     * Called from Gui
     */
    public void checkScript() {
        String command = runScriptDialog.getCommand(lastScript);
        if (command != null) {
            checkScript(command);
        }
    }

    /**
     * Called from Gui
     */
    public void processScript(String name) {
        lastScript = name;
        firstScriptTab = tabs.size();
        aborted = false;
        backupFg = foreground;
        backupBg = background;
        backupFont = font;
        splashScreen = new ProcessingScriptSplashScreen(this, name);
        scriptProcessor.process(name);
        splashScreen.setVisible(true);
    }

    /**
     * Called from Gui
     */
    public void processScript() {
        String command = runScriptDialog.getCommand(lastScript);
        if (command != null) {
            processScript(command);
        }
    }

    public void closeAllConsoles() {
        while (!tabs.isEmpty())
            tabs.get(0).close();
    }

    /**
     * Called from Gui
     */
    public void restartLastScript() {
        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
                "Are you sure to restart\n" + lastScript + "?", "Restart",
                JOptionPane.YES_NO_OPTION)) {
            while (tabs.size() > firstScriptTab)
                tabs.get(tabs.size() - 1).close();
            processScript(lastScript);
        }
    }

    public boolean isBusy() {
        for (Tab tab : tabs)
            if (tab.isBusy())
                return true;
        return false;
    }

    public void terminateAllConsoles() {
        for (Tab tab : tabs)
            tab.stopCommand();
    }

    public void setInitFontSize(int size) {
        font = font.deriveFont((float) size);
    }

    private void initFrame() {
        setLayout(new BorderLayout());
        tabsPane = new JTabbedPane();
        add(tabsPane, BorderLayout.CENTER);
        font = new Font(DEFAULT_FONT_FAMILY, Font.PLAIN, 11);
    }

    private void executeSwingTask(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread())
            runnable.run();
        else
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException | InvocationTargetException ie) {
                //nothing to do
            }
    }

    private List<String> parseExecutable(String executionParameters)
            throws ScriptCommandException {
        List<String> ret = new ArrayList<>();
        String parameters = executionParameters;
        Matcher m = separatorPattern.matcher(parameters);
        while (m.matches()) {
            if (m.group(1) != null)
                ret.add(m.group(1));
            else
                ret.add(m.group(3));
            parameters = m.group(4);
            m = separatorPattern.matcher(parameters);
        }
        System.out.println("Execution of " + ret.size());
        for (String s : ret) {
            System.out.println(s);
        }
        if (!parameters.trim().isEmpty())
            throw new ScriptCommandException("Could not parse command >"
                    + executionParameters);
        return ret;
    }

    /**
     * ScriptProcessorListener interface
     */
    public void scriptProcessed(String scriptName) {
        executeSwingTask(() -> {
            splashScreen.setVisible(false);
            splashScreen = null;
            if (currentScriptTab != null && currentTabAvailable) {
                if (!aborted)
                    JOptionPane
                            .showMessageDialog(
                                    CmdLinker.this,
                                    "Every created tab must have an associated task",
                                    "Script error",
                                    JOptionPane.ERROR_MESSAGE);
                currentScriptTab.close();
            }
            if (aborted && revertChangesIfAborted) {
                while (tabs.size() > firstScriptTab) {
                    tabs.get(tabs.size() - 1).close();
                }
            } else {
                if (tabsPane.getSelectedIndex() < firstScriptTab
                        && tabsPane.getTabCount() >= firstScriptTab) {
                    tabsPane.setSelectedIndex(firstScriptTab);
                }
            }
            currentScriptTab = null;
            foreground = backupFg;
            background = backupBg;
            font = backupFont;
            menuBar.enableRunLastScript(!checkingCommand);
            checkingCommand = false;
        });
    }

    /**
     * ScriptProcessorListener interface
     */
    public void waitTime(long milliseconds) {
        if (!checkingCommand) {
            long finalTime = System.currentTimeMillis() + milliseconds;
            while (System.currentTimeMillis() < finalTime && !aborted) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
    }

    /**
     * ScriptProcessorListener interface
     */
    public void execute(String command) throws ScriptCommandException {
        List<String> splitCommand = parseExecutable(command);
        boolean createTab = !currentTabAvailable;
        currentTabAvailable = false;
        executeSwingTask(new TabExecutor(command, splitCommand, createTab));
    }

    /**
     * ScriptProcessorListener interface
     */
    public void createTab(String name) throws ScriptCommandException {
        if (currentTabAvailable)
            throw new ScriptCommandException(
                    "Cannot create a tab if there is one available for usage");
        currentTabAvailable = true;
        executeSwingTask(new TabCreator(name));
    }

    /**
     * ScriptProcessorListener interface
     */
    public synchronized void scriptProcessingError(final String error) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                CmdLinker.this, error, "Script Error",
                JOptionPane.ERROR_MESSAGE));
    }

    /**
     * ScriptProcessorListener interface
     */
    public void showTab(String which) {
        executeSwingTask(new TabShower(which));
    }

    /**
     * ScriptProcessorListener interface
     */
    public void setColors(Color foreground, Color background) {
        executeSwingTask(new ColorsSetter(foreground, background));
    }

    /**
     * ScriptProcessorListener interface
     */
    public void setTextFont(Font font) {
        executeSwingTask(new FontSetter(font));
    }

    /**
     * ScriptProcessorListener interface
     */
    public void setAutoScroll(boolean scroll) {
        executeSwingTask(new ScrollSetter(scroll));
    }

    /**
     * **************************************************************************
     * Runnable to set the colors of a tab
     * **************************************************************************
     */
    class ColorsSetter implements Runnable {
        final Color foregColor;
        final Color backColor;

        public ColorsSetter(Color foregColor, Color backColor) {
            this.foregColor = foregColor;
            this.backColor = backColor;
        }

        public void run() {
            if (foregColor != null)
                CmdLinker.this.foreground = foregColor;
            if (backColor != null)
                CmdLinker.this.background = backColor;
            if (currentScriptTab != null) {
                currentScriptTab.setColors(foregColor, backColor);
            }
        }
    }

    /**
     * **************************************************************************
     * Runnable to set the font value of a tab
     * **************************************************************************
     */
    class FontSetter implements Runnable {
        final Font fnt;

        public FontSetter(Font font) {
            this.fnt = font;
        }

        public void run() {
            if (currentScriptTab == null)
                CmdLinker.this.font = fnt;
            else
                currentScriptTab.setTextFont(fnt);
        }
    }

    /**
     * **************************************************************************
     * Runnable to set the scroll value of a tab
     * **************************************************************************
     */
    class ScrollSetter implements Runnable {
        final boolean scroll;

        public ScrollSetter(boolean scroll) {
            this.scroll = scroll;
        }

        public void run() {
            if (currentScriptTab == null)
                CmdLinker.this.autoScroll = scroll;
            else
                currentScriptTab.setAutoScroll(scroll);
        }
    }

    /**
     * **************************************************************************
     * Runnable to create a new tab
     * **************************************************************************
     */
    class TabCreator implements Runnable {
        String name;

        public TabCreator(String name) {
            this.name = name;
        }

        public void run() {
            currentScriptTab = new Tab(CmdLinker.this, name);
            if (font != null)
                currentScriptTab.setTextFont(font);
            currentScriptTab.setColors(foreground, background);
            currentScriptTab.setAutoScroll(autoScroll);
            tabs.add(currentScriptTab);
            if (name == null)
                name = String.valueOf(tabs.size());
            currentScriptTab.addToTabPane(tabsPane, name);
            menuBar.enableCloseAll(true);
        }
    }

    /**
     * **************************************************************************
     * Runnable to modify the name and tip of a created tab
     * **************************************************************************
     */
    class TabExecutor implements Runnable {
        final String command;
        final List<String> split;
        final boolean createTab;

        public TabExecutor(String command, List<String> split, boolean createTab) {
            this.command = command;
            this.createTab = createTab;
            this.split = split;
        }

        public void run() {
            String name = extractTabName(split.get(0));
            if (createTab) {
                new TabCreator(name).run();
            } else if (currentScriptTab.getName() == null) {
                currentScriptTab.setName(name);
                tabsPane.setTitleAt(tabs.size() - 1, name);
            }
            tabsPane.setToolTipTextAt(tabs.size() - 1, command);
            if (checkingCommand) {
                currentScriptTab.showCheckInfo(command);
            } else {
                currentScriptTab.process(split);
            }
        }

        private String extractTabName(String executable) {
            int start, end;
            start = 1 + executable.lastIndexOf(File.separatorChar);
            end = executable.indexOf('.', start);
            if (end == -1)
                end = executable.length();
            return executable.substring(start, end);
        }

    }

    /**
     * **************************************************************************
     * Runnable to show a given tab
     * **************************************************************************
     */
    class TabShower implements Runnable {
        final String name;

        public TabShower(String name) {
            this.name = name;
        }

        private int getTabIndex() {
            if (tabs.isEmpty()) {
                JOptionPane
                        .showMessageDialog(CmdLinker.this, "showTab",
                                "There are no tabs to show!",
                                JOptionPane.ERROR_MESSAGE);
                return -1;
            }
            if (name.isEmpty()) {
                return tabs.size() - 1;
            }
            for (int i = firstScriptTab; i < tabs.size(); i++) {
                Tab tab = tabs.get(i);
                if (name.equals(tab.getName()))
                    return i;
            }
            int index = -1;
            try {
                index = firstScriptTab + Integer.parseInt(name) - 1;
            } catch (NumberFormatException ne) {
                // index remains -1
            }
            if (index < 0 || index >= tabs.size()) {
                JOptionPane.showMessageDialog(CmdLinker.this, "showTab",
                        "There is no tab to show named " + name,
                        JOptionPane.ERROR_MESSAGE);
                return -1;
            }
            return index;
        }

        public void run() {
            int index = getTabIndex();
            if (index != -1)
                tabsPane.setSelectedIndex(index);
        }
    }

    public static void main(String[] args)  {
        System.out.println(Version.getVersion());
        OptionsHandler arguments = OptionsHandler.createOptionsHandler(args);
        String argumentsError = arguments.getInitialArgumentsStringError();
        if (argumentsError != null) {
            System.out.println("\n" + argumentsError);
        } else {
            CmdLinker linker = new CmdLinker();
            arguments.registerClient(null, linker);
            linker.setVisible(true);
        }
    }

}
