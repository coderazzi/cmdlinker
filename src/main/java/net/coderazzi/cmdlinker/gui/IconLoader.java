package net.coderazzi.cmdlinker.gui;

import javax.swing.ImageIcon;
import java.net.URL;

public class IconLoader {
    
    private static final IconLoader singleton = new IconLoader();
    
    public static ImageIcon getIcon(String name)
    {
        return singleton.loadIcon(name);
    }
    
    private ImageIcon loadIcon(String name) {
        URL url = getClass().getResource("/images/"+name+".gif");
        if (url == null) {
            throw new IllegalArgumentException("Image not found: " + name);
        }
        return new ImageIcon(url);
    }
    
}
