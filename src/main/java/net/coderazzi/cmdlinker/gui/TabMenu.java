package net.coderazzi.cmdlinker.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

public class TabMenu {
    private int stopItem;

    private final JPopupMenu popupMenu;

    private JMenuItem stopPopup, stopMenu;

    private ActionListener copy, clear, search, display, restart, close, stop,
            rename;

    public TabMenu(Tab owner) {
        createActionListeners(owner);
        popupMenu = createMenu(new JPopupMenu());
    }

    public void populateMenu(JMenu menu) {
        createMenu(menu);
    }

    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    public void enableStopItem(boolean enable) {
        if (stopMenu != null)
            stopMenu.setEnabled(enable);
        stopPopup.setEnabled(enable);
    }

    private void createMenu(JMenu holder) {
        List<JMenuItem> items = createMenuItems();
        stopMenu = items.get(stopItem);
        for (JMenuItem item : items)
            if (item == null)
                holder.addSeparator();
            else
                holder.add(item);
    }

    private JPopupMenu createMenu(JPopupMenu holder) {
        List<JMenuItem> items = createMenuItems();
        stopPopup = items.get(stopItem);
        for (JMenuItem item : items)
            if (item == null)
                holder.addSeparator();
            else
                holder.add(item);
        return holder;
    }

    private List<JMenuItem> createMenuItems() {
        List<JMenuItem> ret = new ArrayList<>(13);

        JMenuItem item = new JMenuItem("Copy");
        item.addActionListener(copy);

        ret.add(item);

        item = new JMenuItem("Clear");
        item.addActionListener(clear);

        ret.add(item);
        ret.add(null); // separator

        item = new JMenuItem("Search ...");
        item.addActionListener(search);
        item.setEnabled(false);

        ret.add(item);
        ret.add(null); // separator

        item = new JMenuItem("Rename ...");
        item.addActionListener(rename);

        ret.add(item);

        item = new JMenuItem("Display ...");
        item.addActionListener(display);

        ret.add(item);
        ret.add(null); // separator

        item = new JMenuItem("Restart");
        item.addActionListener(restart);

        ret.add(item);

        item = new JMenuItem("Stop");
        item.addActionListener(stop);

        stopItem = ret.size();

        ret.add(item);
        ret.add(null); // separator

        item = new JMenuItem("Close");
        item.addActionListener(close);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                ActionEvent.CTRL_MASK));

        ret.add(item);

        return ret;
    }

    private void createActionListeners(final Tab owner) {
        copy = e -> owner.copyToClipboard();
        clear = e -> owner.clearTextArea();
        search = e -> {

        };
        rename = e -> owner.rename();
        display = e -> owner.changeDisplay();
        restart = e -> owner.restartCommand();
        stop = e -> owner.stopCommand();
        close = e -> owner.close();
    }
}
