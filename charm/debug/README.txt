INTRODUCTION
This is a parallel debugger, charmdebug, that works 
over CCS.  It runs in a GUI window.

The program resumes/suspends execution when you click
the buttons Continue/Freeze along the top.

Breakpoints can be set/removed on entry points by 
checking/unchecking checkboxes on the left.

Gdb can be attached to specific pes (click the PE 
checkboxes on the right) at any time during program execution.

View array elements, messages, and other CpdLists via 
the drop-down menus along the bottom.


USAGE
On the net- versions of charm++, you can just do:
	charm/bin/charmdebug ./pgm +p3

Other versions of charm++ do not work yet from 
the GUI (fix this!).

We recommend using the new "PUPn" named-pup macros 
in all your pup routines, as otherwise all you see 
is the object values, not object names.

Override the ckDebugPup routine to give a more
detailed debug pup routine for your array elements.


VERSION HISTORY
Initially written by Rashmi Jyohti in the fall of 2003.
Modified by Orion Lawlor in the spring of 2004.

