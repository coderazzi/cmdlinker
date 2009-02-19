package com.byteslooser.cmdlinker.candy;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.byteslooser.cmdlinker.CmdLinker;

public class ProcessingScriptSplashScreen extends JDialog {
    public ProcessingScriptSplashScreen(final CmdLinker owner,
            final String processingScript) {
        super(owner, "CmdLinker", true);

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(new JLabel("Processing script '" + processingScript
                + "'...", SwingConstants.CENTER), BorderLayout.CENTER);
        labelPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel boxPanel = new JPanel(new BorderLayout());
        final JCheckBox box = new JCheckBox("Revert all effects if cancelled",
                true);
        boxPanel.add(box, BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout(6, 10));
        center.add(labelPanel, BorderLayout.CENTER);
        center.add(boxPanel, BorderLayout.SOUTH);
        center.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createEmptyBorder(6, 6, 6, 6), BorderFactory
                .createEtchedBorder()));

        JPanel buttonPanel = new JPanel(new BorderLayout());
        final JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                cancel.setEnabled(false);
                ProcessingScriptSplashScreen.this.setCursor(Cursor
                        .getPredefinedCursor(Cursor.WAIT_CURSOR));
                owner.abortScript(box.isEnabled());
            }
        });
        buttonPanel.add(cancel, BorderLayout.EAST);

        JPanel main = new JPanel(new BorderLayout(0, 10));
        main.add(center, BorderLayout.CENTER);
        main.add(buttonPanel, BorderLayout.SOUTH);
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        getRootPane().setContentPane(main);

        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }

}
