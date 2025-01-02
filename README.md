Instructions:

CmdLinker executes any provided program, displaying the output on a new tab. The option File / New Command starts this process.

If a process ends, the return code wll be displayed. The menu entry Console allows to update the current console, from colors, to operations updating the process itself, restarting or stopping it.

Of more interest is the handling of CmdLinker scripts, with allow the user to script the CmdLinker operations. These scripts have a specific format; supported operations are:

comments: any line starting with '#'.
empty lines: these are just discarded
execute command: either enter the full path to the application/script, or prefix it as: &execute Command
	This execution creates a new tab automatically.
create a new tab: tab NAME of the tab. Note that if the tab name is already existing, it will just create a new tab with the same name
update appearance:
	colors foreground background
		The colors can be given with the well know name, or as RGB prepended with 0x ot #
	font size
	scroll on / auto | lock / off: enable auto scrolling or not
wait:
	wait milliseconds	


&execute lsall