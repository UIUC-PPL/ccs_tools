package charm.debug;

//import java.net.*;
import java.io.*;

import javax.swing.SwingUtilities;
//import java.security.*;


/**
 * This class implements a thread whose job is to handle the I/O communication
 * with the currently debugged program. The communication here refers to the
 * stdin, stdout, stderr streams.
 */
public class ServThread extends Thread {
	String hostName = null;
	String portno = null;
	Runtime runtime = null; 
	Process p = null; 
	ParDebug mainThread = null;
	volatile int flag;
	public ServThread(ParDebug d, Process par){
		mainThread = d;
		p = par;
		flag = 0;
		/*try {
			//charmrunIn = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			//charmrunOut = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			//debugOutput = new FileWriter("servthread_debug.out");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in ServThread while opening the streams");
		}*/
	}

	// debub output file
	public FileWriter debugOutput;

	// string used to pass the output back from the info gdb
	public static String infoStr;


	public void run() {
		runtime = Runtime.getRuntime();
		try {
			System.out.println("ServThread started");
			BufferedReader prout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader prerr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			boolean foundPort = false;
			try {
				while (true) 
				{ /* Fetch a chunk of output and print it.
				 * Passing the output to the GUI in large chunks is 
				 * much much faster than passing it one line at a time.
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
							int nameStart, nameEnd;
							//System.out.println("Debug servhread: "+outline);
							if(outline.indexOf("ccs: Server IP =", 0) != -1)
							{
								nameStart = outline.indexOf("Server IP = ",0);
								nameStart += 12;
								nameEnd = outline.indexOf(",",nameStart);
								hostName = outline.substring(nameStart, nameEnd);
								portStart = outline.indexOf("Server port = ",0);
								portStart += 14;
								portEnd = outline.indexOf("$",0);
								portno = outline.substring(portStart, portEnd-1);
								foundPort = true;
								flag = 1;
							}
							if(outline.indexOf("Password:", 0) != -1) {
								System.out.println("Password requested");
								char [] passwd = null;
								new PasswordDialog(passwd);
								System.out.println(passwd);
							}
						}

						if (outline.indexOf("Break point reached", 0) != -1)
						{
							Runnable doWorkRunnable = new Runnable() {
							    public void run() { mainThread.notifyBreakpoint(); }
							};
							SwingUtilities.invokeLater(doWorkRunnable);
							//mainThread.notifyBreakpoint(outline);
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

					if (outline==null) {
						System.out.println("ServThread terminated");
						flag = 2;
						break; /* Program is now finished. */
					}
				}
			}
			catch(Exception e) {
				System.out.println("Failed to print");
				e.printStackTrace();
			}
			System.out.println("Finished running parallel program");
			mainThread.quitProgram();
			//debugOutput.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("error in ServThread. Exception caught");
		} 
	}

	public int getFlag() { return flag; }
};

