package com.byteslooser.cmdlinker.candy;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class IconLoader {
    
    private static IconLoader singleton = new IconLoader();
    
    public static ImageIcon getIcon(String name)
    {
        return singleton.loadIcon(name);
    }
    
    private ImageIcon loadIcon(String name)
    {
        return new ImageIcon(getClass().getResource("/images/"+name+".gif"));
    }
    
}
