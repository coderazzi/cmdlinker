Instructions:

CmdLinker executes any provided program, displaying the output on a new tab.
If a process ends, the return code wll be displayed. 
The menu entry Console allows to update the current console, from colors, 
to operations updating the process itself, restarting or stopping it.

The option File / New Command starts a single process;. 
Of more interest is the handling of CmdLinker scripts, 
with allow the user to script the CmdLinker operations. 
These scripts have a specific format; 
non empty lines can be comments, if start with a # character,
or Cmdlinker instructions. These instructions are specified with the
first word in the line:
EXECUTE command : will process the command, showing the output in a new tab
FOREGROUND color
	updates the foreground color of the next execution console.
	Colors can be specified in hexadecimal format, prepended with 0x or #
	or with the well known name (red, pink, etc).
BACKGROUND color
	updates the backgorund color of the next execution console.
	Colors can be specified in hexadecimal format, prepended with 0x or #
	or with the well known name (red, pink, etc).
FONT [name [style]] size
	updates the font size of the next execution console
SCROLL on|off
	sets scrolling on/off for the next execution console
WAIT milliseconds
FOCUS tab name
NAME tab name
	sets the name of the next execution console 
