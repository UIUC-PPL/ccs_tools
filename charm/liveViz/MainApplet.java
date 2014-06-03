/*
  Applet that repeatedly displays a memory image - 9/97 O. Lawlor
*/
package charm.liveViz;

import java.applet.Applet;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;

public class MainApplet extends Applet
{
// This little stub main allows the applet to also be run
// as a java application.
	public static void main(String args[]) {
		if (args.length<2) {
			System.out.println("Usage: demo <ccs server>" +
			  "<ccs port>\nor:    demo <ccs server> <ccs port>" +
			  " -w <width> -h <height>");
			System.exit(1);
		}

		int     width     = 300;
		int     height    = 200;
		boolean maximized = false;

		boolean wFlag = false;
		boolean hFlag = false;

		for (int i = 2; i < args.length; i++) {
			if (args[i].equals("-w")) {
				wFlag = true;
			} else if (args[i].equals("-h")) {
				hFlag = true;
			} else if (wFlag) {
				width = Integer.parseInt(args[i]);
				wFlag = false;
			} else if (hFlag) {
				height = Integer.parseInt(args[i]);
				hFlag = false;
			}
		}
		if (width < 0 || height < 0) {
			System.out.println("ERROR: Invalid width or height");
			System.exit(1);
		}

		createWindow(new MainPanel(args[0],args[1]),true, width,
		height, maximized);
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

// Properties of the applet proper:
// show/hide window when requested.
	private MainPanel panel;
	private Frame frame;

	public void start() {
		panel=new MainPanel(getParameter("ccs_server_name"),
			getParameter("ccs_server_port"));
		frame=createWindow(panel,false, 300, 200, false);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{ e.getWindow().dispose(); }
		});
	}

	public void stop() {
		panel.stop();
		frame.dispose();
		panel=null;
		frame=null;
	}
}
