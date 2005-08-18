/*  
	MainApplet.java

    A java applet/application which sets up liveviz and python ccs connections.
	The liveviz connection is in poll mode, which allows the server to generate an image
	when it desires. Thus the python connection is used to instruct the server 
	to generate an image which will be returned through the liveviz connection.

	Use this client with the polling server called 'lvPythonServer'.
	
*/


package charm.lvClient;

import charm.ccs.*;
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
	  String servername = new String(args[0]);
	  String port = new String(args[1]);
	  CcsAcceptor cap = new CcsAcceptor(servername, port);
	  CcsAcceptor cal = new CcsAcceptor(servername, port);

	  System.out.println("Please enter either a [l]iveviz request or [p]ython request or [q]uit\n");

	  // A simple loop which reads characters from STDIN
	  // if an 'l' is entered, a liveviz request is sent
	  // if a  'p' is entered, a python  request is sent
	  try {
		int s;
		while ( (s=System.in.read()) != 0) {
		  if (s == 'l'){
			System.out.println("You typed an l: Requsting an image from the server\n");	
			cal.readyLiveViz(1);
		  }
		  else if (s == 'p'){
			System.out.println("You typed a p: Instructing server to generate an image\n");
			cap.DoPythonReq("charm.pycall();");
		  }
		  else if(s == 'q'){
			System.out.println("Done\n");
			System.exit(0);
		  }
		}
	  }
	  catch (Exception e) {
		System.out.println("Some exception occured while reading your input\n");
	  }
	  
	}
  }

}
