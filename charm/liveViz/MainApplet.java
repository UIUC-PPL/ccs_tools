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
//This little stub main allows the applet to also be run
// as a java application.
	public static void main(String args[]) {
		if (args.length!=2) {
			System.out.println("Usage: demo <ccs server> <ccs port>\n");
			System.exit(1);
		}
		else {
		    createWindow(new MainPanel(args[0],args[1]),true);
		}
	}

	private static Frame createWindow(MainPanel p,boolean closeExit)
	{
		Frame top=new Frame();
		if (closeExit)
		  top.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{ System.exit(0); }
		  });
		top.setTitle("Parallel Data Visualization");
		top.add(p);
		top.pack();
		top.setSize(300,200);
		top.setVisible(true);
		return top;
	}

	
//Properties of the applet proper:
//  show/hide window when requested.	
	private MainPanel panel;
	private Frame frame;
	public void start()
	{
		panel=new MainPanel(getParameter("ccs_server_name"),
			   getParameter("ccs_server_port"));
		frame=createWindow(panel,false);
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
