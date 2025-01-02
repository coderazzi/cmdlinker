package net.coderazzi.cmdlinker.gui;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class RunCommandDialog extends EscapeDialog {
    private final FileDialog fileDialog;
    private JTextField fileManual;
    private String command;
    private JButton cancel, ok;
    protected final OptionsHandler optionsHandler;

    public RunCommandDialog(JFrame parent, OptionsHandler optionsHandler) {
        this(parent, "new command", optionsHandler);
    }

    protected RunCommandDialog(JFrame parent, String title,
            OptionsHandler optionsHandler) {
        super(parent, "Run " + title, true);
        this.optionsHandler = optionsHandler;
        getRootPane().setContentPane(initDialog());
        fileDialog = new FileDialog(this, "Select " + title, FileDialog.LOAD);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                if (fileManual.getText().trim().isEmpty())
                    browse();
            }
        });
        pack();
        setLocationRelativeTo(parent);
    }

    public void copyParameters(RunCommandDialog copy) {
        fileManual.setText(copy.fileManual.getText());
        fileDialog.setDirectory(copy.fileDialog.getDirectory());
    }

    public String getCommand(String lastCommand) {
        if (lastCommand != null)
            setFileDirectory(lastCommand);
        else
            setDefaultFileDirectory();
        command = null;
        setVisible(true);
        if (command != null)
            persist(command, fileDialog.getDirectory());
        return command;
    }

    final protected void setFileDirectory(String command) {
        fileManual.setText(command);
        try {
            if (command.startsWith("\"")) {
                int index = command.indexOf('"', 1);
                if (index != -1)
                    command = command.substring(1, index);
            }
            File absolute = new File(command).getAbsoluteFile();
            if (absolute.exists())
            {
                File parent = absolute.getParentFile();
                fileManual.setText(absolute.getAbsolutePath());
                fileDialog.setDirectory(parent.getAbsolutePath());
            }
        } catch (Exception ex) {
            // do nothing
        }
    }

    protected void persist(String command, String directory) {
        optionsHandler.setLastCommand(command);
        optionsHandler.setLastCommandDirectory(directory);
    }

    protected void setDefaultFileDirectory() {
        String lastDir = optionsHandler.getLastCommandDirectory();
        if (lastDir != null)
            fileDialog.setDirectory(optionsHandler.getLastCommandDirectory());
        String lastCommand = optionsHandler.getLastCommand();
        if (lastCommand != null)
            setFileDirectory(lastCommand);
    }

    private void browse() {
        fileDialog.setVisible(true);
        String selection = fileDialog.getFile();
        if (selection != null) {
            fileManual.setText(new File(fileDialog.getDirectory(), selection)
                    .getAbsolutePath());
            ok.requestFocus();
        } else {
            cancel.requestFocus();
        }
    }

    private void okPressed() {
        String command = fileManual.getText().trim();
        if (validCommand(command)) {
            this.command = command;
            setVisible(false);
        }
    }

    protected boolean validCommand(String command) {
        return !command.isEmpty();
    }

    private JPanel initDialog() {
        cancel = createAutoEnterButton("Cancel");
        cancel.addActionListener(e -> {
            command = null;
            setVisible(false);
        });
        ok = createAutoEnterButton("Ok");
        ok.setEnabled(false);
        ok.addActionListener(e -> okPressed());

        fileManual = new JTextField(40);
        fileManual.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                check();
            }

            public void insertUpdate(DocumentEvent e) {
                check();
            }

            public void removeUpdate(DocumentEvent e) {
                check();
            }

            private void check() {
                ok.setEnabled(!fileManual.getText().trim().isEmpty());
            }
        });
        fileManual.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    okPressed();
                }
            }
        });

        JButton fileBrowse = createAutoEnterButton(". . .");
        fileBrowse.addActionListener(e -> browse());
        JPanel input = new JPanel(new BorderLayout(6, 6));
        input.add(fileManual, BorderLayout.CENTER);
        input.add(fileBrowse, BorderLayout.EAST);
        input.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createEmptyBorder(12, 12, 12, 12), BorderFactory
                .createCompoundBorder(BorderFactory
                        .createTitledBorder("Command"), BorderFactory
                        .createEmptyBorder(6, 6, 6, 6))));

        JPanel content = new JPanel(new BorderLayout());
        content.add(input, BorderLayout.NORTH);
        content.add(createButtonsPanel(ok, cancel), BorderLayout.SOUTH);

        JPanel ret = new JPanel(new BorderLayout());
        ret.add(content, BorderLayout.NORTH);

        return ret;
    }
}
