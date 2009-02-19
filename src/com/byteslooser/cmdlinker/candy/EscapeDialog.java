package com.byteslooser.cmdlinker.candy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

public class EscapeDialog extends JDialog {
    protected EscapeDialog(JFrame parent, String title, boolean modal) {
        super(parent, title, modal);
    }

    protected JRootPane createRootPane() {
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        };
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

}
