package net.coderazzi.cmdlinker.gui;

import java.awt.BorderLayout;
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

import net.coderazzi.cmdlinker.Appearance;
import net.coderazzi.cmdlinker.ScriptCommandException;
import net.coderazzi.cmdlinker.ScriptProcessor;
import net.coderazzi.cmdlinker.Version;

public class CmdLinker extends JFrame  {
    private final List<Tab> tabs = new ArrayList<>();
    private final ScriptProcessor scriptProcessor;
    private final MainMenu menuBar;

    private Appearance appearance;
    private JTabbedPane tabsPane;
    private ProcessingScriptSplashScreen splashScreen;
    private transient boolean aborted, revertChangesIfAborted;
    private int firstScriptTab;
    private String lastScript, lastCommand;
    private RunScriptDialog runScriptDialog;
    private RunCommandDialog runCommandDialog;
    private OptionsHandler optionsHandler;
    private final Pattern separatorPattern = Pattern
            .compile("^(?:\"((\\\\.|[^\\\\\"])*)\"|([^\"]\\S*))\\s*(.*)$");

    public CmdLinker(Appearance appearance) {
        super("Cmd Linker");
        this.appearance = appearance;
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
                CmdLinker.this.appearance = tab.getAppearance();
            }
            menuBar.setTabMenu(tab);
        });
    }

    protected CmdLinker(CmdLinker copy) {
        this(new Appearance(copy.appearance));
        copy.optionsHandler.registerWindow(copy, this);
    }

    public void setOptionsHandler(OptionsHandler optionsHandler) {
        assert this.optionsHandler == null;
        this.optionsHandler = optionsHandler;
        menuBar.setOptionsHandler(optionsHandler);
        runScriptDialog = new RunScriptDialog(this, optionsHandler);
        runCommandDialog = new RunCommandDialog(this, optionsHandler);
    }

    public void setColorsAndFont(Appearance appearance) {
        for (Tab tab : tabs) {
            tab.setColorsAndFont(appearance);
        }
        this.appearance.setBackground(appearance.getBackground());
        this.appearance.setFont(appearance.getFont());
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
    public void promptForCommand() {
        String command = runCommandDialog.getCommand(lastCommand);
        if (command != null) {
            lastCommand = command;
            processCommand(command);
        }
    }

    /**
     * Called from Gui
     */
    public void createNewWindow() {
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
            execute(command, appearance);
        } catch (ScriptCommandException sce) {
            JOptionPane.showMessageDialog(this, sce.getMessage(),
                    "Invalid command", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Called from Gui
     */
    public void processScript(String name) {
        lastScript = name;
        firstScriptTab = tabs.size();
        aborted = false;
        splashScreen = new ProcessingScriptSplashScreen(this, name);
        scriptProcessor.process(name, new Appearance(appearance));
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

    private void initFrame() {
        setLayout(new BorderLayout());
        tabsPane = new JTabbedPane();
        add(tabsPane, BorderLayout.CENTER);
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
            menuBar.enableRunLastScript(true);
        });
    }

    /**
     * ScriptProcessorListener interface
     */
    public void waitTime(long milliseconds) {
        long finalTime = System.currentTimeMillis() + milliseconds;
        while (System.currentTimeMillis() < finalTime && !aborted) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ie) {
                break;
            }
        }
    }

    /**
     * ScriptProcessorListener interface
     */
    public void execute(String command, Appearance appearance) throws ScriptCommandException {
        List<String> splitCommand = parseExecutable(command);
        executeSwingTask(new Executor(command, splitCommand, appearance));
    }

    /**
     * ScriptProcessorListener interface
     */
    public void showTab(String which) {
        executeSwingTask(new TabShower(which));
    }

    private Tab createTab(Appearance appearance) {
        Tab ret = new Tab(CmdLinker.this, appearance);
        tabs.add(ret);
        ret.addToTabPane(tabsPane, appearance.getTitle());
        appearance.setTitle(null);
        menuBar.enableCloseAll(true);
        return ret;
    }

    /**
     * **************************************************************************
     * Runnable to modify the name and tip of a created tab
     * **************************************************************************
     */
    class Executor implements Runnable {
        final String command;
        final List<String> split;
        final Appearance appearance;

        public Executor(String command, List<String> split, Appearance appearance) {
            this.command = command;
            this.split = split;
            this.appearance = appearance;
        }

        public void run() {
            String title = appearance.getTitle();
            if (title == null || title.isEmpty()) {
                appearance.setTitle(extractTabName(split.get(0)));
            }
            Tab tab = createTab(appearance);
            tabsPane.setToolTipTextAt(tabs.size() - 1, command);
            tab.process(split);
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
            CmdLinker linker = new CmdLinker(arguments.getAppearance());
            arguments.registerWindow(null, linker);
            linker.setVisible(true);
            String script = arguments.getScript();
            if (script != null) {
                if (arguments.isCommand())
                    linker.processCommand(script);
                else
                    linker.processScript(script);
            }
        }
    }

}
