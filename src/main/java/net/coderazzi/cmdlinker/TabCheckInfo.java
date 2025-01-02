package com.byteslooser.cmdlinker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

public class TabCheckInfo extends JPanel {
    private JLabel checkInfoCommand, info1, info2;

    private JPanel checkInfo;

    public TabCheckInfo() {
        super(new BorderLayout());
        checkInfoCommand = new JLabel("", SwingConstants.CENTER);
        info1 = new JLabel("Checking the script", SwingConstants.CENTER);
        info2 = new JLabel("This tab would contain the output of:",
                SwingConstants.CENTER);
        checkInfo = new JPanel(new GridLayout(3, 1));
        checkInfo.add(info1);
        checkInfo.add(info2);
        checkInfo.add(checkInfoCommand);
        checkInfo.setBorder(BorderFactory
                .createBevelBorder(BevelBorder.LOWERED));
        JPanel checkInfoPane = new JPanel(new BorderLayout());
        checkInfoPane.add(checkInfo, BorderLayout.NORTH);
        add(checkInfoPane, BorderLayout.CENTER);
        checkInfo.setFont(checkInfo.getFont().deriveFont(Font.BOLD));
    }

    public void setCommand(String command) {
        checkInfoCommand.setText(command);
    }

    public void setFont(Font font) {
        super.setFont(font);
        if (info1 != null) {
            Font normalFont = font.deriveFont(Font.PLAIN);
            info1.setFont(normalFont);
            info2.setFont(normalFont);
            checkInfoCommand.setFont(font.deriveFont(Font.BOLD));
        }
    }

    public void setColors(Color foreground, Color background) {
        if (foreground != null) {
            info1.setForeground(foreground);
            info2.setForeground(foreground);
            checkInfoCommand.setForeground(foreground);
            checkInfo.setForeground(foreground);
        }
        if (background != null)
            checkInfo.setBackground(background);
    }

}