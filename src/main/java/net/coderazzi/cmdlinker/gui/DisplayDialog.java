package net.coderazzi.cmdlinker.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class DisplayDialog extends EscapeDialog {
    private Font font;
    private Color foreground, background;
    private boolean okPressed;

    public DisplayDialog(JFrame parent, Color foreground, Color background,
            Font font) {
        super(parent, "Change display properties", true);
        this.foreground = foreground;
        this.background = background;
        this.font = font;
        getRootPane().setContentPane(initDialog());
        pack();
        setLocationRelativeTo(parent);
    }

    public boolean okPressed() {
        return okPressed;
    }

    public Color getForegroundChoice() {
        return foreground;
    }

    public Color getBackgroundChoice() {
        return background;
    }

    public Font getFontChoice() {
        return font;
    }

    private Color changeColor(String name, Color current) {
        Color answer = JColorChooser.showDialog(this, "Please choose the "
                + name + " color", current);
        return answer == null ? current : answer;
    }

    private JPanel initDialog() {
        final JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setEditable(false);
        area.setText(getRandomText());
        area.setForeground(foreground);
        area.setBackground(background);
        area.setFont(font);
        area.setPreferredSize(new Dimension(300, 100));
        area.setBorder(BorderFactory.createEtchedBorder());

        JPanel areaPanel = new JPanel(new BorderLayout());
        areaPanel.add(area, BorderLayout.CENTER);
        areaPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Example"), BorderFactory
                .createEmptyBorder(6, 6, 6, 6)));

        JButton bgButton = createAutoEnterButton("Background");
        bgButton.addActionListener(e -> {
            background = changeColor("background", background);
            area.setBackground(background);
        });
        JButton fgButton = createAutoEnterButton("Foreground");
        fgButton.addActionListener(e -> {
            foreground = changeColor("foreground", foreground);
            area.setForeground(foreground);
        });
        final JComboBox<Integer> fonts = createFontsCombobox();
        fonts.setSelectedItem(font.getSize());
        fonts.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    okPressed();
            }
        });
        fonts.addItemListener(e -> {
            font = font.deriveFont(((Integer) fonts.getSelectedItem())
                    .floatValue());
            area.setFont(font);
        });

        JPanel fontPanel = new JPanel(new BorderLayout());
        fontPanel.add(fonts, BorderLayout.NORTH);
        fontPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Font size"), BorderFactory
                .createEmptyBorder(6, 6, 6, 6)));
        JPanel fontPanelWrapper = new JPanel(new BorderLayout());
        fontPanelWrapper.add(fontPanel, BorderLayout.NORTH);

        JPanel colorButtonsWrapper = new JPanel(new GridLayout(2, 1, 6, 6));
        colorButtonsWrapper.add(fgButton);
        colorButtonsWrapper.add(bgButton);
        colorButtonsWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 12,
                12));
        colorButtonsWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Colors"), BorderFactory
                        .createEmptyBorder(6, 6, 6, 6)));

        JPanel wrapper = new JPanel(new BorderLayout(6, 6));
        wrapper.add(colorButtonsWrapper, BorderLayout.NORTH);
        wrapper.add(fontPanelWrapper, BorderLayout.CENTER);

        JPanel colors = new JPanel(new BorderLayout(20, 10));
        colors.add(areaPanel, BorderLayout.CENTER);
        colors.add(wrapper, BorderLayout.EAST);
        colors.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createEmptyBorder(12, 12, 12, 12), BorderFactory
                .createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
                        BorderFactory.createEmptyBorder(6, 6, 6, 6))));

        JButton cancel = createAutoEnterButton("Cancel");
        cancel.addActionListener(e -> {
            okPressed = false;
            setVisible(false);
        });
        JButton ok = createAutoEnterButton("Ok");
        ok.addActionListener(e -> {
            okPressed = true;
            setVisible(false);
        });

        JPanel content = new JPanel(new BorderLayout());
        content.add(colors, BorderLayout.CENTER);
        content.add(createButtonsPanel(ok, cancel), BorderLayout.SOUTH);

        return content;
    }

    private String getRandomText() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            if (r.nextBoolean())
                sb.append(' ');
            else
                sb.append(r.nextInt(9));
        }
        return sb.toString();
    }

    public static JComboBox<Integer> createFontsCombobox() {
        JComboBox<Integer> ret = new JComboBox<>(getFontSizes());
        ret.setEditable(false);
        return ret;
    }

    private static Integer[] getFontSizes() {
        int start = 6, end = 72;
        Integer []ret = new Integer[end - start + 1];
        for (int i = start; i <= end; i++)
            ret[i - start] = i;
        return ret;
    }

}
