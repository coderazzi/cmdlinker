package net.coderazzi.cmdlinker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;

import net.coderazzi.cmdlinker.commands.ScriptColorsCommand;
import net.coderazzi.cmdlinker.commands.ScriptCommand;
import net.coderazzi.cmdlinker.commands.ScriptCreateTabCommand;
import net.coderazzi.cmdlinker.commands.ScriptExecuteCommand;
import net.coderazzi.cmdlinker.commands.ScriptFontCommand;
import net.coderazzi.cmdlinker.commands.ScriptScrollCommand;
import net.coderazzi.cmdlinker.commands.ScriptShowTabCommand;
import net.coderazzi.cmdlinker.commands.ScriptWaitCommand;

public class ScriptProcessor implements Runnable {
    private ScriptProcessorListener owner;

    private String file;

    private HashMap<String, ScriptCommand> commands = new HashMap<String, ScriptCommand>();

    private transient boolean aborted;

    public ScriptProcessor(ScriptProcessorListener owner) {
        this.owner = owner;
        addCommand(new ScriptExecuteCommand());
        addCommand(new ScriptColorsCommand());
        addCommand(new ScriptCreateTabCommand());
        addCommand(new ScriptFontCommand());
        addCommand(new ScriptScrollCommand());
        addCommand(new ScriptShowTabCommand());
        addCommand(new ScriptWaitCommand());
    }

    private void addCommand(ScriptCommand command) {
        commands.put(command.getCommand(), command);
    }

    public synchronized void process(String scriptFile) {
        aborted = false;
        while (isBusy())
            try {
                wait();
            } catch (InterruptedException ie) {
            }
        this.file = scriptFile;
        new Thread(this).start();
    }

    /**
     * Aborts the script.
     */
    public synchronized void abort() {
        aborted = true;
    }

    public boolean isBusy() {
        return file != null;
    }

    public void run() {
        try {
            processFile(file);
        } catch (IOException ex) {
            owner.scriptProcessingError("Could not read file " + file);
        }
        owner.scriptProcessed(file);
    }

    public void processFile(String file) throws IOException {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(
                new FileInputStream(file)));
        try {
            String line = reader.readLine();
            try {
                while (!aborted && (line != null)) {
                    processLine(line.trim());
                    line = reader.readLine();
                }
            } catch (ScriptCommandException sex) {
                owner.scriptProcessingError(
                    "<html>" + file + ":" + reader.getLineNumber() + "<br>" + sex.getMessage() + "</html>");
            }
        } finally {
            reader.close();
            synchronized (this) {
                this.file = null;
                notifyAll();
            }
        }
    }

    private void processLine(String line) throws ScriptCommandException {
        if (line.length() > 0 && line.charAt(0) != '#') {
            int space = line.indexOf(' ');
            if (space != -1) {
                String command = line.substring(0, space);
                ScriptCommand sc = commands.get(command);
                if (sc != null) {
                    sc.execute(owner, line.substring(space + 1).trim());
                    return;
                }                
                line = command;
            }
            throw new ScriptCommandException("Unknown command: " + line);
        }
    }

}
