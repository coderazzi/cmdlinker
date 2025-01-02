package net.coderazzi.cmdlinker.candy;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private JTextField fileManual;

    private FileDialog fileDialog;

    private String command;

    private JButton cancel, ok;

    protected OptionsHandler optionsHandler;

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
                if (fileManual.getText().trim().length() == 0)
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
        return command.length() != 0;
    }

    private JPanel initDialog() {
        cancel = createAutoEnterButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                command = null;
                setVisible(false);
            }
        });
        ok = createAutoEnterButton("Ok");
        ok.setEnabled(false);
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed();
            }
        });

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
                ok.setEnabled(fileManual.getText().trim().length() != 0);
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
        fileBrowse.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                browse();
            }
        });
        JPanel input = new JPanel(new BorderLayout(6, 6));
        input.add(fileManual, BorderLayout.CENTER);
        input.add(fileBrowse, BorderLayout.EAST);
        input.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createEmptyBorder(12, 12, 12, 12), BorderFactory
                .createCompoundBorder(BorderFactory
                        .createTitledBorder("Command"), BorderFactory
                        .createEmptyBorder(6, 6, 6, 6))));

        JPanel buttonsWrapper = new JPanel(new GridLayout(1, 2, 6, 6));
        buttonsWrapper.add(ok);
        buttonsWrapper.add(cancel);
        buttonsWrapper
                .setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 12));

        JPanel buttons = new JPanel(new BorderLayout());
        buttons.add(buttonsWrapper, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout());
        content.add(input, BorderLayout.NORTH);
        content.add(buttons, BorderLayout.SOUTH);

        JPanel ret = new JPanel(new BorderLayout());
        ret.add(content, BorderLayout.NORTH);

        return ret;
    }
}
