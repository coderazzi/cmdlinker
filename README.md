CmdLinker is a program to process a set of commands, displaying each on a
separate tab. It is useful to track a set of commands that need to be
executed concurrently.

For each executed command, the return code is displayed when the associated
process ends.
The menu entry Console allows to update the current console, from colors, 
to operations updating the process itself, restarting or stopping it.

While it is possible to execute a single command or operative system
script, it is also possible to launch specific CmdLinker scripts,
which allow the user to specify CmdLinker operations using a 
simple syntax:  
* non empty lines can be comments, if they start with a # character;  
otherwise they are treated as  Cmdlinker instructions, and the first word 
in the line specifies the operation:
* BACKGROUND  color
	* updates the background color of the next execution console.
* EXECUTE command
    * Processes the command, showing the output in a new tab
* FONT [name [style]] size
	* updates the font size of the next execution console
* FOREGROUND color
    * updates the foreground color of the next execution console.
* SCROLL on|off
	* sets scrolling on/off for the next execution console
* SHOW tab name
    * the tab is specified using the allocated name, or the index.    
* TAB tab name
    * sets the name of the next execution console
* WAIT milliseconds
    * waits the specific period before processing the next instruction

Colors can be specified in hexadecimal format, prepended with 0x or #
or with the well known name (red, pink, etc).

An example of such a script, running bash commands, would be:

    # example script
    
    FOREGROUND yellow
    BACKGROUND blue
    EXECUTE bash -c "echo Hello"
    
    WAIT 1000
    
    BACKGROUND black
    FOREGROUND white
    FONT 8
    TAB directory
    SCROLL off
    EXECUTE bash -c "ls -latiR .."
    
    SHOW bash
    WAIT 5000
    SHOW 2
  