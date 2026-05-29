/*
  Applet that repeatedly displays a memory image - 9/97 O. Lawlor
*/
package charm.liveViz;

import java.awt.*;
import java.awt.event.*;

public class MainApplet
{
// This little stub main allows the applet to also be run
// as a java application.
	public static void main(String args[]) {
		if (args.length<2) {
			System.out.println("Usage: demo <ccs server>" +
			  "<ccs port>\nor:    demo <ccs server> <ccs port>" +
			  " -w <width> -h <height> -timeout <period>");
			System.exit(1);
		}

		int     width     = 300;
		int     height    = 200;
		boolean maximized = false;

		boolean wFlag = false;
		boolean hFlag = false;
		boolean tFlag = false;

		String isTimeoutSet = "false";
		String timeoutPeriod = "0";

		for (int i = 2; i < args.length; i++) {
			if (args[i].equals("-w")) {
				wFlag = true;
			} else if (args[i].equals("-h")) {
				hFlag = true;
			} else if (args[i].equals("-timeout")) {
				tFlag = true;
			} else if (wFlag) {
				width = Integer.parseInt(args[i]);
				wFlag = false;
			} else if (hFlag) {
				height = Integer.parseInt(args[i]);
				hFlag = false;
			} else if (tFlag) {
				isTimeoutSet = "true";
				timeoutPeriod = args[i];
				tFlag = false;
			}
		}
		if (width < 0 || height < 0) {
			System.out.println("ERROR: Invalid width or height");
			System.exit(1);
		}

		createWindow(new MainPanel(args[0],args[1],isTimeoutSet,timeoutPeriod),
				true, width, height, maximized);
	}

	private static Frame createWindow(MainPanel p,boolean closeExit,
	  int width, int height, boolean maximized) {
		Frame top=new Frame();
		if (maximized) top.setExtendedState(Frame.MAXIMIZED_BOTH);
		if (closeExit) {
		  top.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{ System.exit(0); }
		  });
		}
		top.setTitle("Parallel Data Visualization");
		top.add(p);
		top.pack();
		top.setSize(width,height);
		top.setVisible(true);
		return top;
	}

}
