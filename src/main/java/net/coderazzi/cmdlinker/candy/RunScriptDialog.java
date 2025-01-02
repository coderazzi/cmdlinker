package net.coderazzi.cmdlinker.candy;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class RunScriptDialog extends RunCommandDialog {

    public RunScriptDialog(JFrame parent, OptionsHandler optionsHandler) {
        super(parent, "CmdLinker script", optionsHandler);
    }

    protected void persist(String command, String directory) {
        optionsHandler.setLastScript(command);
    }

    protected void setDefaultFileDirectory() {
        String lastScript = optionsHandler.getLastScript();
        if (lastScript != null)
            setFileDirectory(lastScript);
    }

    protected boolean validCommand(String selectedFile) {
        if (!new File(selectedFile).isFile()) {
            JOptionPane.showMessageDialog(this,
                    "The specified file does not exist", "Invalid file",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
}
