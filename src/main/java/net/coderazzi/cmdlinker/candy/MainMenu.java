package net.coderazzi.cmdlinker.candy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.coderazzi.cmdlinker.CmdLinker;
import net.coderazzi.cmdlinker.Tab;
import net.coderazzi.cmdlinker.Version;

public class MainMenu extends JMenuBar {
    private CmdLinker owner;

    private JMenuItem restart, closeAll;

    private JMenu consoleMenu;

    private JCheckBoxMenuItem saveOptions;

    public MainMenu(CmdLinker owner) {
        this.owner = owner;
        initMenuItems();
    }

    public void setOptionsHandler(final OptionsHandler optionsHandler) {
        assert !saveOptions.isEnabled();
        saveOptions.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                optionsHandler.persistOptions(saveOptions.isSelected());
            }
        });
    }

    public void saveOptions(boolean enable) {
        saveOptions.setSelected(enable);
    }

    public void setTabMenu(Tab tab) {
        if (tab == null) {
            consoleMenu.setEnabled(false);
        } else {
            consoleMenu.setEnabled(true);
            consoleMenu.removeAll();
            tab.populateMenu(consoleMenu);
        }
    }

    public void enableRunLastScript(boolean enable) {
        restart.setEnabled(enable);
    }

    public void enableCloseAll(boolean enable) {
        closeAll.setEnabled(enable);
    }

    private void initMenuItems() {
        consoleMenu = new JMenu("Console");
        consoleMenu.setMnemonic(KeyEvent.VK_C);
        consoleMenu.setEnabled(false);
        add(initFileMenuItems());
        add(consoleMenu);
        add(initOptionMenuItems());
        add(initHelpMenuItems());
    }

    private JMenu initFileMenuItems() {
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);

        JMenuItem item = new JMenuItem("New command ...", KeyEvent.VK_N);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                owner.newCommand();
            }
        });
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                ActionEvent.CTRL_MASK));
        menu.add(item);
        menu.addSeparator();

        item = new JMenuItem("Process script ...", KeyEvent.VK_S);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                owner.processScript();
            }
        });
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                ActionEvent.CTRL_MASK));
        menu.add(item);

        restart = new JMenuItem("Restart script", KeyEvent.VK_R);
        restart.setEnabled(false);
        restart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                owner.restartLastScript();
            }
        });
        menu.add(restart);
        menu.addSeparator();

        JMenuItem newWindow = new JMenuItem("New window");
        newWindow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                owner.newWindow();
            }
        });
        menu.add(newWindow);

        closeAll = new JMenuItem("Close all tabs");
        closeAll.setEnabled(false);
        closeAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                        owner, "Are you sure?", "Closing all consoles",
                        JOptionPane.YES_NO_OPTION))
                    owner.closeAllConsoles();
            }
        });
        menu.add(closeAll);
        menu.addSeparator();

        item = new JMenuItem("Exit", KeyEvent.VK_X);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
                ActionEvent.ALT_MASK));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                owner.dispatchEvent(new WindowEvent(owner,
                        WindowEvent.WINDOW_CLOSING));
            }
        });
        menu.add(item);

        return menu;
    }

    private JMenu initHelpMenuItems() {
        JMenu menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);

        JMenuItem item = new JMenuItem("Check script...");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                owner.checkScript();
            }
        });
        menu.add(item);
        menu.addSeparator();

        item = new JMenuItem("About...");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(owner, Version.getVersion(),
                        "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menu.add(item);

        return menu;
    }

    private JMenu initOptionMenuItems() {
        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic(KeyEvent.VK_O);
        JMenuItem item = new JMenuItem("Display...");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                owner.changeDisplay();
            }
        });
        optionsMenu.add(item);
        optionsMenu.addSeparator();

        saveOptions = new JCheckBoxMenuItem("Save options");
        optionsMenu.add(saveOptions);
        return optionsMenu;
    }
}
