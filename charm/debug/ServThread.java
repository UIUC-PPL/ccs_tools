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
    //System.out.println("constructor of ServThread");
    p = par;
  }

  public void run() {
    runtime = Runtime.getRuntime();
    try {
      //p = runtime.exec("../server/charmrun +p2 ../server/cfdAMR 2 ++server ++server-port 1236");
      //int sleepMs = 100;
      //sleep(sleepMs);
      BufferedReader prout = new BufferedReader(new InputStreamReader(p.getInputStream()));
      int foundPort = 0;
      try {
           String outline = prout.readLine();
           while (outline != null)
           {
              if(foundPort == 0)
              {
                 int portStart, portEnd;
                 if(outline.indexOf("ccs: Server IP =", 0) != -1)
                 {
                    portStart = outline.indexOf("Server port = ",0);
                    portStart += 14;
                    portEnd = outline.indexOf("$",0);
                    portno = outline.substring(portStart, portEnd-1);
                    //System.out.println("Port Number got " +portno);
                    foundPort = 1;
                 }

              }

              if (outline.indexOf("Break point reached", 0) != -1)
              {
                mainThread.setStatusMessage(outline);
              }
              else
              { 
                 // Print this out to a display area on the debugger
                  mainThread.displayProgramOutput(outline);
                  mainThread.displayProgramOutput(new String("\n"));
              }
              outline = prout.readLine();
           }
      }
      catch(Exception e) {
          System.out.println("Failed to print");
        
      }
      System.out.println("Success running parallel pgm");    
      mainThread.cleanUpAndStart();
    }
   catch (Exception e) {
      System.out.println("error in ServThread. Exception caught");
   } 
 }


};

