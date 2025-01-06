package net.coderazzi.cmdlinker.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import net.coderazzi.cmdlinker.Appearance;
import net.coderazzi.cmdlinker.CommandExecutor;

public class Tab extends JPanel implements CommandExecutor.Client, Runnable {
    final static private int MAX_LINE_LENGTH_TO_CUT = 80;
    final static private int MAX_DOCUMENT_SIZE = 80000;

    private final TabStatus status;
    private final StringBuilder buffer = new StringBuilder();
    private final CmdLinker owner;
    private final TabMenu associatedMenu;
    private final Appearance appearance;

    private JTextArea text;
    private Document document;
    private TabOptions tabOptions;
    private boolean autoScroll;
    private CommandExecutor commandExecutor;

    /**
     * Constructor. The name parameter is used just by convenience, accessible
     * via getName
     */
    public Tab(CmdLinker owner, Appearance appearance) {
        super(new BorderLayout());
        this.appearance = new Appearance(appearance);
        this.owner = owner;
        this.associatedMenu = new TabMenu(this);
        status=new TabStatus(this);
        add(createTextArea(), BorderLayout.CENTER);
    }

    public Appearance getAppearance() {
        return appearance;
    }
    
    public void addToTabPane(JTabbedPane tabsPane, String name) {
        tabsPane.addTab(name, this);
        status.setTabbedPane(tabsPane);
    }


    public void populateMenu(JMenu menu) {
        associatedMenu.populateMenu(menu);
    }

    public void rename() {
        String newName = JOptionPane.showInputDialog(owner, "New tab name:", appearance.getTitle());
        if (newName != null) {
            appearance.setTitle(newName);
            owner.renameTab(this, newName);
        }
    }

    /**
     * Called from the GUI thread to close this tab.
     */
    public synchronized void close() {
        if (isBusy())
            commandExecutor.terminate();
        owner.closeTab(this);
    }

    /**
     * Called from the GUI thread to restart the command
     */
    public synchronized void stopCommand() {
        commandExecutor.terminate();
    }

    /**
     * Called from the GUI thread to restart the command
     */
    public synchronized void restartCommand() {
        commandExecutor.restart();
    }

    private JPanel createTextArea() {
        text = new JTextArea();
        text.setFont(appearance.getFont());
        text.setForeground(appearance.getForeground());
        text.setBackground(appearance.getBackground());
        text.setEditable(false);
        text.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    associatedMenu.getPopupMenu().show(e.getComponent(),
                            e.getX(), e.getY());
                }
            }
        });
        document = text.getDocument();

        tabOptions = new TabOptions(this, text.getFont().getSize(), autoScroll);

        JPanel ret = new JPanel(new BorderLayout());
        ret.add(status.getStatusPanel(), BorderLayout.NORTH);
        ret.add(new JScrollPane(text), BorderLayout.CENTER);
        ret.add(tabOptions, BorderLayout.SOUTH);
        return ret;
    }

    /**
     * Processes the passed command
     */
    public synchronized void process(List<String> command) {
        commandExecutor = new CommandExecutor(this);
        commandExecutor.execute(command);
    }

    /**
     * Method called from GUI thread
     */
    public void copyToClipboard() {
        String selected = text.getSelectedText();
        if (selected == null || selected.isEmpty())
            selected = text.getText();
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(selected), null);
    }

    /**
     * Method called from GUI thread
     */
    public void clearTextArea() {
        try {
            document.remove(0, document.getLength());
        } catch (BadLocationException bex) {
            // sure
        }
    }

    public boolean isBusy() {
        return commandExecutor != null && commandExecutor.isBusy();
    }

    /**
     * This method must be called from the GUI thread
     */
    public void setTextFont(Font font) {
        int oldSize = text.getFont().getSize();
        text.setFont(font);
        if (oldSize != font.getSize())
            tabOptions.setFontSize(font.getSize());
    }

    public void setTextFontSize(float size) {
        setTextFont(text.getFont().deriveFont(size));
    }

    public void showAppearanceMenu() {
        DisplayDialog cd = new DisplayDialog(owner, appearance);
        cd.setVisible(true);
        if (cd.okPressed()) {
            Appearance appearance = cd.getAppearance();
            if (cd.isApplyToAll()) {
                owner.setColorsAndFont(appearance);
            } else {
                setColorsAndFont(appearance);
            }
        }
    }

    public void setColorsAndFont(Appearance appearance) {
        this.appearance.setBackground(appearance.getBackground());
        this.appearance.setForeground(appearance.getForeground());
        this.appearance.setFont(appearance.getFont());
        text.setForeground(appearance.getForeground());
        text.setBackground(appearance.getBackground());
        setTextFont(appearance.getFont());
    }

    /**
     * This method must be called from the GUI thread
     */
    public void setAutoScroll(boolean value) {
        autoScroll = value;
        if (autoScroll)
            text.setCaretPosition(document.getLength());
    }

    private void updateOutput() {
        SwingUtilities.invokeLater(this);
    }

    private int getMaxBufferSize() {
        return MAX_DOCUMENT_SIZE;
    }

    private int restrictDocumentSize(int docLength) {
        int maxSize = getMaxBufferSize();
        if (docLength > maxSize) {
            int offset = docLength - maxSize;
            try {
                String line = document.getText(offset, MAX_LINE_LENGTH_TO_CUT);
                int newline = line.indexOf('\n');
                if (newline != -1) {
                    offset += newline + 1;
                }
                document.remove(0, offset);
                return offset;
            } catch (BadLocationException ex) {
                // ok
            }
        }
        return 0;
    }

    public synchronized void run() {
        int pos = text.getCaretPosition();
        text.append(buffer.toString());
        buffer.delete(0, buffer.length());

        int docLength = document.getLength();
        int removed = restrictDocumentSize(docLength);
        if (autoScroll) {
            text.setCaretPosition(docLength - removed);
        } else {
            if (pos > removed) {
                pos -= removed;
            } else {
                pos = 0;
            }
            text.setCaretPosition(pos);
        }

        if (!isBusy())
            buffer.trimToSize();
    }

    public void commandStarted(String process) {
        executeSwingTask(() -> {
            clearTextArea();
            status.setRunning();
            associatedMenu.enableStopItem(true);
            tabOptions.enableAutoScroll(true);
        });
    }

    public synchronized void appendOutputLine(String line) {
        boolean empty = buffer.length() == 0;
        buffer.append(line).append("\n");
        if (empty)
            updateOutput();
    }

    public void commandCompleted(final CommandExecutor.ExecutionEnd howEnded,
            final int errorCode) {
        executeSwingTask(() -> {
            status.setEnded(howEnded, errorCode);
            associatedMenu.enableStopItem(false);
            tabOptions.enableAutoScroll(false);
        });
    }

    public void errorOnCommand(String process, String error) {
        JOptionPane.showMessageDialog(this, error, process, JOptionPane.ERROR_MESSAGE);
        updateOutput();
    }

    private void executeSwingTask(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread())
            runnable.run();
        else
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException | InvocationTargetException ie) {
                // what else to do.
            }
    }

}
