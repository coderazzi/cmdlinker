package com.byteslooser.cmdlinker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CommandExecutor {
    public static enum ExecutionEnd {
        Normal, ExceptionError, Terminated;
    }

    public interface Client {
        void errorOnCommand(String error, String reason);

        void commandStarted(String process);

        void commandCompleted(ExecutionEnd how, int errorCode);

        void commandOutput(String line);
    }

    private Client client;

    private ProcessBuilder builder;

    private Process process;

    private transient boolean destroyed, busy;

    public CommandExecutor(Client client) {
        assert client != null;
        this.client = client;
        destroyed = true;
        busy = false;
    }

    public synchronized void execute(final List<String> command) {
        assert !busy;
        busy = true;
        new Thread(new Runnable() {
            public void run() {
                executeOnThisThread(command);
            }
        }).start();
    }

    public synchronized void restart() {
        assert builder != null;
        terminate();
        new Thread(new Runnable() {
            public void run() {
                synchronized (CommandExecutor.this) {
                    while (busy)
                        try {
                            CommandExecutor.this.wait();
                        } catch (InterruptedException ie) {
                        }
                }
                executeOnThisThread(builder.command());
            }
        }).start();
    }

    private void executeOnThisThread(List<String> command) {
        destroyed = false;
        busy = true;
        builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true); // error and input stream together
        performExecution();
    }

    public boolean isBusy() {
        return busy;
    }

    public synchronized boolean terminate() {
        // wait for the thread to start (if not yet started)
        while (busy && process == null)
            try {
                wait();
            } catch (InterruptedException ie) {
            }

        boolean ret = busy;

        if (busy) {
            destroyed = true;
            process.destroy();
        }

        return ret;
    }

    private void performExecution() {
        ExecutionEnd howEnded = ExecutionEnd.Terminated;
        int errorCode = 0;
        try {
            process = builder.start();
            client.commandStarted(describeProcess());
            boolean well = handleProcess(process);
            process.waitFor();
            errorCode = process.exitValue();
            howEnded = well ? ExecutionEnd.Normal : ExecutionEnd.ExceptionError;
        } catch (IOException ioex) {
            if (!destroyed) {
                howEnded = ExecutionEnd.ExceptionError;
                client.errorOnCommand("Could not start " + describeProcess(),
                        "Reason: " + ioex.getMessage());
            }
        } catch (InterruptedException ie) {
            howEnded = ExecutionEnd.ExceptionError;
        } finally {
            synchronized (this) {
                if (destroyed)
                    howEnded = ExecutionEnd.Terminated;
                process = null;
                busy = false;
                notifyAll();
            }
            client.commandCompleted(howEnded, errorCode);
        }
    }

    private String describeProcess() {
        StringBuilder sb = new StringBuilder();
        for (String s : builder.command())
            sb.append(s).append(" ");
        if (sb.length() > 0)
            return sb.substring(0, sb.length() - 1);
        return sb.toString();
    }

    private boolean handleProcess(Process process) {
        BufferedReader br = new BufferedReader(new InputStreamReader(process
                .getInputStream()));
        try {
            String line = br.readLine();
            while (line != null) {
                client.commandOutput(line);
                line = br.readLine();
            }
            return true;
        } catch (IOException ex) {
            if (!destroyed)
                client.errorOnCommand("Error executing " + describeProcess(),
                        ex.toString());
        } finally {
            try {
                br.close();
            } catch (IOException ex2) {
            }
        }
        return false;
    }
}
