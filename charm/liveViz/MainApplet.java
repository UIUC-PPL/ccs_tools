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
			  " -w <width> -h <height> -timeout <period>");
			System.exit(1);
		}

		int     width     = 400;
		int     height    = 300;
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

    MenuBar menuBar = new MenuBar();
    Menu menu = new Menu("Options");
    MenuItem showControls = new MenuItem("Show Controls");
    MenuItem hideControls = new MenuItem("Hide Controls");
    MenuItem showImage = new MenuItem("Show Image View");
    MenuItem hideImage = new MenuItem("Hide Image View");
    MenuItem showBalance = new MenuItem("Show Balance View");
    MenuItem hideBalance = new MenuItem("Hide Balance View");
    MenuItem showPerf = new MenuItem("Show Performance View");
    MenuItem hidePerf = new MenuItem("Hide Performance View");
    menu.add(showControls);
    menu.add(hideControls);
    menu.add(showImage);
    menu.add(hideImage);
    menu.add(showBalance);
    menu.add(hideBalance);
    menu.add(showPerf);
    menu.add(hidePerf);
    menuBar.add(menu);

    showControls.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        p.showControls();
      }
    });
    hideControls.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        p.hideControls();
      }
    });

    showImage.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        p.showImage();
      }
    });
    hideImage.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        p.hideImage();
      }
    });

    showBalance.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        p.showBalance();
      }
    });
    hideBalance.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        p.hideBalance();
      }
    });

    showPerf.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        p.showPerf();
      }
    });
    hidePerf.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        p.hidePerf();
      }
    });

		top.setTitle("Parallel Data Visualization");
    top.setMenuBar(menuBar);
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
			getParameter("ccs_server_port"),getParameter("ccs_timeoutset"),getParameter("ccs_timeoutperiod"));
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
