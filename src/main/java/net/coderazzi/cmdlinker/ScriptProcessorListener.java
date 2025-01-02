package net.coderazzi.cmdlinker;

import java.awt.Color;
import java.awt.Font;

public interface ScriptProcessorListener {
    /**
     * Method called to create a new tab. The provided name could be null. This
     * method is always called from a separate thread.
     */
    public void createTab(String name) throws ScriptCommandException;

    /**
     * Execute command This method is always called from a separate thread.
     */
    public void execute(String command) throws ScriptCommandException;

    /**
     * Method called when there is an error on a given ScriptProcessor. After
     * this method, nothing else is called This method is always called from a
     * separate thread.
     */
    public void scriptProcessingError(String error);

    /**
     * Method called to set the color of all or the most recent created tab This
     * method is always called from a separate thread.
     */
    public void setColors(Color foreground, Color background)
            throws ScriptCommandException;

    /**
     * Method called to set the font of all or the most recent created tab This
     * method is always called from a separate thread.
     */
    public void setTextFont(Font font) throws ScriptCommandException;

    /**
     * Method called to set the auto scroll value of all or the most recent
     * created tab This method is always called from a separate thread.
     */
    public void setAutoScroll(boolean scroll) throws ScriptCommandException;

    /**
     * Method called to display an specific tab This method is always called
     * from a separate thread.
     */
    public void showTab(String which) throws ScriptCommandException;

    /**
     * Method called to wait a given time This method is always called from a
     * separate thread.
     */
    public void waitTime(long milliseconds) throws ScriptCommandException;

    /**
     * Method called when the script has been processed
     */
    public void scriptProcessed(String scriptName);
}
