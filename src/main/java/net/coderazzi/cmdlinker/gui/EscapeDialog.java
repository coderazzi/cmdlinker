package net.coderazzi.cmdlinker.gui;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.*;

public class EscapeDialog extends JDialog {
    protected EscapeDialog(JFrame parent, String title, boolean modal) {
        super(parent, title, modal);
    }

    protected JRootPane createRootPane() {
        ActionListener actionListener = actionEvent -> setVisible(false);
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JRootPane rootPane = new JRootPane();
        rootPane.registerKeyboardAction(actionListener, stroke,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        return rootPane;
    }

    protected JButton createAutoEnterButton(String label) {
        final JButton ret = new JButton(label);
        ret.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    ret.doClick();
            }
        });
        return ret;
    }

    protected JPanel createButtonsPanel(JButton ok, JButton cancel) {
        JPanel buttonsWrapper = new JPanel(new GridLayout(1, 2, 6, 6));
        buttonsWrapper.add(ok);
        buttonsWrapper.add(cancel);
        buttonsWrapper.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 12));

        JPanel buttons = new JPanel(new BorderLayout());
        buttons.add(buttonsWrapper, BorderLayout.EAST);

        return buttons;
    }

}
