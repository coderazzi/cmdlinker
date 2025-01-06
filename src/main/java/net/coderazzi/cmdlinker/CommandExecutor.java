package net.coderazzi.cmdlinker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CommandExecutor {
    public enum ExecutionEnd {
        Normal, ExceptionError, Terminated
    }

    public interface Client {
        void errorOnCommand(String process, String error);
        void commandStarted(String process);
        void commandCompleted(ExecutionEnd how, int errorCode);
        void appendOutputLine(String line);
    }

    private final Client client;
    private ProcessBuilder builder;
    private Process process;
    private volatile boolean destroyed, busy;

    public CommandExecutor(Client client) {
        assert client != null;
        this.client = client;
        destroyed = true;
        busy = false;
    }

    public synchronized void execute(final List<String> command) {
        assert !busy;
        busy = true;
        new Thread(() -> executeOnThisThread(command)).start();
    }

    public synchronized void restart() {
        assert builder != null;
        terminate();
        new Thread(() -> {
            synchronized (CommandExecutor.this) {
                while (busy)
                    try {
                        CommandExecutor.this.wait();
                    } catch (InterruptedException ie) {
                        break;
                    }
            }
            executeOnThisThread(builder.command());
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

    public synchronized void terminate() {
        // wait for the thread to start (if not yet started)
        while (busy && process == null)
            try {
                wait();
            } catch (InterruptedException ie) {
                break;
            }

        if (busy) {
            destroyed = true;
            process.destroy();
        }
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
                client.errorOnCommand( describeProcess(), "Could not start: " + ioex.getMessage());
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
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process
                .getInputStream()))) {
            String line = br.readLine();
            while (line != null) {
                client.appendOutputLine(line);
                line = br.readLine();
            }
            return true;
        } catch (Exception ex) {
            if (!destroyed)
                client.errorOnCommand(describeProcess(), ex.toString());
        }
        return false;
    }
}
