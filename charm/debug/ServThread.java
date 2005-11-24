package charm.debug;

import java.net.*;
import java.io.*;
import java.util.*;
import java.security.*;
import java.lang.*;

public class ServThread extends Thread {
  String portno = null;
  Runtime runtime = null; 
  Process p = null; 
  ParDebug mainThread = null;
  public ServThread(ParDebug d, Process par){
    mainThread = d;
    p = par;
  }

    // string used to pass the output back from the info gdb
    public static String infoStr;
    // streams to communicate with the charmrun process
    public static BufferedWriter charmrunIn;
    public static BufferedReader charmrunOut;


  public void run() {
    runtime = Runtime.getRuntime();
    try {
	//BufferedReader prerr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      BufferedReader prout = new BufferedReader(new InputStreamReader(p.getInputStream()));
      boolean foundPort = false;
      try {
	while (true) 
	{ /* Fetch a chunk of output and print it.
	     Passing the output to the GUI in large chunks is 
	     much much faster than passing it one line at a time.
	  */
           String outline;
	   StringBuffer outlinechunk=new StringBuffer();
	   final int maxChunk=50*1024; /* maximum chunk size */
	   do
           { /* Fetch a line of output and handle it */
              outline = prout.readLine();
	      if (outline == null) break;
              if(!foundPort)
              {
                 int portStart, portEnd;
                 if(outline.indexOf("ccs: Server IP =", 0) != -1)
                 {
                    portStart = outline.indexOf("Server port = ",0);
                    portStart += 14;
                    portEnd = outline.indexOf("$",0);
                    portno = outline.substring(portStart, portEnd-1);
                    foundPort = true;
                 }
              }

              if (outline.indexOf("Break point reached", 0) != -1)
              {
                mainThread.setStatusMessage(outline);
              }
	      else {
		  // User output: Print this out to a display area on the debugger
		  // Flush out stderr too:

		  // CHANGE Filippo: In order to use a stream for the debug,
		  // everything is printed by charmrun to stdout. stderr is now
		  // used for debugging (anyway they were treated in the same
		  // way).

		  //while (prerr.ready()) outline+=prerr.readLine()+"\n";
		  outlinechunk.append(outline+"\n");
              }
           }
	   while (prout.ready() && (outlinechunk.length()<maxChunk));
	   System.out.println("Parallel program printed: "+outlinechunk.toString());
           mainThread.displayProgramOutput(outlinechunk.toString());
	   
	   if (outline==null) break; /* Program is now finished. */
	}
      }
      catch(Exception e) {
          System.out.println("Failed to print");
      }
      System.out.println("Finished running parallel program");    
      mainThread.quitProgram();
    }
   catch (Exception e) {
      e.printStackTrace();
      System.out.println("error in ServThread. Exception caught");
   } 
 }

    public static String infoCommand(String command) {
	try {
	    charmrunIn.write(command);
	    charmrunIn.flush();
	    StringBuffer reply = new StringBuffer();
	    while (!charmrunOut.ready());
	    while (charmrunOut.ready()) reply.append((char)charmrunOut.read());
	    return reply.toString();
	} catch (IOException e) {
	    return "error";
	}
    }

};

