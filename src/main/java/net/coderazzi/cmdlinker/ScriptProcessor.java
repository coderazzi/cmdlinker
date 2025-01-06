package net.coderazzi.cmdlinker;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import net.coderazzi.cmdlinker.commands.*;
import net.coderazzi.cmdlinker.gui.CmdLinker;

import javax.swing.*;

public class ScriptProcessor {
    private final CmdLinker owner;
    private final HashMap<String, ScriptCommand> commands = new HashMap<>();
    private volatile boolean aborted, running;

    public ScriptProcessor(CmdLinker owner) {
        this.owner = owner;
        addCommand(new ScriptExecuteCommand());
        addCommand(new ScriptForegroundCommand());
        addCommand(new ScriptBackgroundCommand());
        addCommand(new ScriptTabCommand());
        addCommand(new ScriptFontCommand());
        addCommand(new ScriptScrollCommand());
        addCommand(new ScriptShowCommand());
        addCommand(new ScriptWaitCommand());
    }

    private void addCommand(ScriptCommand command) {
        commands.put(command.getCommand(), command);
    }

    public synchronized void process(final String scriptFile, final Appearance appearance) {
        aborted = false;
        while (running)
            try {
                wait();
            } catch (InterruptedException ie) {
                break;
            }
        running = true;
        new Thread(() -> {
            try {
                processFile(scriptFile, new Appearance(appearance));
            } catch (IOException ex) {
                errorProcessingScript("Could not read file " + scriptFile);
            }
            owner.scriptProcessed(scriptFile);
        }).start();
    }

    /**
     * Aborts the script.
     */
    public synchronized void abort() {
        aborted = true;
    }

    public void processFile(String file, Appearance appearance) throws IOException {
        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(
                Files.newInputStream(Paths.get(file))))) {
            String line = reader.readLine();
            try {
                while (!aborted && (line != null)) {
                    processLine(line.trim(), appearance);
                    line = reader.readLine();
                }
            } catch (ScriptCommandException sex) {
                errorProcessingScript(
                        "<html>" + file + ":" + reader.getLineNumber() + "<br>" + sex.getMessage() + "</html>");
            }
        } finally {
            synchronized (this) {
                running = false;
                notifyAll();
            }
        }
    }

    private void errorProcessingScript(final String error) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                owner, error, "Script Error", JOptionPane.ERROR_MESSAGE));
    }


    private void processLine(String line, Appearance appearance) throws ScriptCommandException {
        if (!line.isEmpty() && line.charAt(0) != '#') {
            int space = line.indexOf(' ');
            if (space != -1) {
                String command = line.substring(0, space);
                ScriptCommand sc = commands.get(command);
                if (sc != null) {
                    sc.execute(line.substring(space + 1).trim(), owner, appearance);
                    return;
                }
                line = command;
            }
            throw new ScriptCommandException("Unknown command: " + line);
        }
    }

}
