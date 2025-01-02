package net.coderazzi.cmdlinker.candy;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.coderazzi.cmdlinker.Tab;

public class TabOptions extends JPanel {
    private Tab owner;

    private JCheckBox autoScroll;

    private JComboBox<Integer> fonts;

    public TabOptions(Tab tab, int fontSize, boolean autoscroll) {
        super(new BorderLayout());
        this.owner = tab;

        JPanel internal = new JPanel(new BorderLayout(12, 0));
        JPanel fontPanel = new JPanel(new BorderLayout(4, 0));

        autoScroll = new JCheckBox("Autoscroll", autoscroll);
        fonts = DisplayDialog.createFontsCombobox();
        setFontSize(fontSize);

        fontPanel.add(new JLabel("Font size:"), BorderLayout.WEST);
        fontPanel.add(fonts, BorderLayout.EAST);
        internal.add(fontPanel, BorderLayout.WEST);
        internal.add(autoScroll, BorderLayout.EAST);

        add(internal, BorderLayout.EAST);

        autoScroll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                owner.setAutoScroll(autoScroll.isSelected());
            }
        });
        fonts.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                owner.setTextFontSize(((Integer) fonts.getSelectedItem())
                        .floatValue());
            }
        });
    }

    public void enableAutoScroll(boolean enable) {
        autoScroll.setEnabled(enable);
    }

    public void setFontSize(int fontSize) {
        fonts.setSelectedItem(new Integer(fontSize));
    }

    public void setAutoScroll(boolean value) {
        autoScroll.setSelected(value);
    }

}
