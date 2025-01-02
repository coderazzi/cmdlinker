package net.coderazzi.cmdlinker.candy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import net.coderazzi.cmdlinker.CommandExecutor;

public class TabStatus {
    
    final static private Color WELL_COMPLETED = new Color(0x008b45);

    final static private Color NON_ZERO_ERROR_CODE = new Color(0x27408b);

    final static private Color ERROR_ON_COMMAND = new Color(0x8b1a1a);

    final static private Color TERMINATED = new Color(0x8b5a00);

    private JPanel statusPanel;
    private JLabel statusLabel;
    private JTabbedPane tabPane;
    private JPanel owner;
    
    public TabStatus(final JPanel tab)
    {
        this.owner=tab;
        statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("", SwingConstants.LEFT);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC));
        statusPanel.add(statusLabel, BorderLayout.SOUTH);
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.setVisible(false);
    }
    
    public void setTabbedPane(JTabbedPane tabsPane)
    {
        this.tabPane=tabsPane;
        setIcon("running");
    }

    
    public JPanel getStatusPanel()
    {
        return statusPanel;
    }
    
    public void setRunning()
    {
        statusPanel.setVisible(false);
    }

    public void setEnded(CommandExecutor.ExecutionEnd howEnded, int errorCode)
    {
        String text, icon;
        Color foreground;
        if (howEnded == CommandExecutor.ExecutionEnd.Normal) {
            text = "Command completed, with error code " + errorCode;
            if (errorCode==0){
                foreground=WELL_COMPLETED;
                icon="ok";
            }
            else{
                foreground = NON_ZERO_ERROR_CODE;
                icon="warning";
            }
        } else if (howEnded == CommandExecutor.ExecutionEnd.ExceptionError) {
            text = "Exception during execution";
            foreground = ERROR_ON_COMMAND;
            icon="error";
        } else {
            text = "Terminated by user";
            foreground = TERMINATED;
            icon="terminated";
        }
        statusLabel.setForeground(foreground);
        statusLabel.setText(text);
        setIcon(icon);
        statusPanel.setVisible(true);        
    }

    private void setIcon(String name)
    {
        if (tabPane!=null)
        {
            tabPane.setIconAt(tabPane.indexOfComponent(owner), IconLoader.getIcon(name));            
        }
    }
    
}
