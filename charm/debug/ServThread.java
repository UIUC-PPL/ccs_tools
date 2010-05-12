package charm.debug;

//import java.net.*;
import java.io.*;
import java.nio.channels.ClosedByInterruptException;

import javax.swing.SwingUtilities;

import charm.ccs.CcsServer;
import charm.debug.event.NotifyEvent;
import charm.debug.preference.Execution;
//import java.security.*;


/**
 * This class implements a thread whose job is to handle the I/O communication
 * with the currently debugged program. The communication here refers to the
 * stdin, stdout, stderr streams.
 */
public abstract class ServThread extends Thread {
	String hostName = null;
	String portno = null;
	//Runtime runtime = null; 
	ParDebug mainThread = null;
	volatile int flag;
	static final int maxChunk=50*1024; /* maximum chunk size */
	
	/** This class is used to get the output of an application started directly by CharmDebug. */
	public static class Charmrun extends ServThread {
		Process p = null; 
		BufferedReader prout, prerr;

		public Charmrun(ParDebug d, Process par) {
			super(d);
			p = par;
			prout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			prerr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		}

		String getNextOutput(StringBuffer outlinechunk) throws Exception {
			boolean foundPort = false;
			String outline;
			
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
				//else {
				// User output: Print this out to a display area on the debugger
				// Flush out stderr too:

				// CHANGE Filippo: In order to use a stream for the debug,
				// everything is printed by charmrun to stdout. stderr is now
				// used for debugging (anyway they were treated in the same
				// way).

				while (prerr.ready()) outline+="\n"+prerr.readLine();
				outlinechunk.append(outline+"\n");
				//}

			}
			while (prout.ready() && (outlinechunk.length()<maxChunk));
			return outline;
		}
	}
	
	/** This class is used to fetch output from a program to which CharmDebug "attach"ed. */
	public static class CCS extends ServThread {
		CcsServer server;
		public CCS(ParDebug d, Execution e) {
			super(d);
			String[] args = new String[2];
			args[0] = e.hostname;
			if (e.ccshost != null && e.ccshost.length() > 0) args[0] = e.ccshost; 
			args[1] = e.port;
			server = CcsServer.create(args, false);
			System.out.println("Created CCSserver");
			try {
	            server.sendRequest("redirect stdio", 0);
            } catch (IOException e1) {
            	System.out.println("redirect stdio request failed");
            }
		}
		
		String getNextOutput(StringBuffer outlinechunk) {
			CcsServer.Request r;
            try {
	            r = server.sendRequest("fetch stdio", 0);
				byte[] resp=server.recvResponse(r);
				System.out.println("reply fetch stdio: "+new String(resp));
				if (resp != null) {
					outlinechunk.append(new String(resp));
					return new String(resp);
				}
				else return null;
            } catch (ClosedByInterruptException ce) {
            	System.out.println("Socket closed by interrupt");
            	return null;
            } catch (IOException e) {
            	System.out.println("Exception while fetching stdio");
            	e.printStackTrace();
	            return null;
            }
			
		}
	}
	
	public static class File extends ServThread {
		BufferedReader prout;

		public File(ParDebug d, java.io.File f, boolean wait) {
			super(d);
			while (wait) {
				try {
					prout = new BufferedReader(new FileReader(f));
					System.out.println(prout);
					wait = false;
				} catch (FileNotFoundException e) {
					if (wait) {
						try { Thread.sleep(5000); }
						catch(InterruptedException e1) { }
					}
					else System.out.println("Could not open file \""+f+"\"");
				}
			}
		}

		String getNextOutput(StringBuffer outlinechunk) throws Exception {
			boolean foundPort = false;
			String outline;
			
			do
			{ /* Fetch a line of output and handle it */
				outline = prout.readLine();
				if (outline == null) {
					continue;
				}
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
				//else {
				// User output: Print this out to a display area on the debugger
				// Flush out stderr too:

				// CHANGE Filippo: In order to use a stream for the debug,
				// everything is printed by charmrun to stdout. stderr is now
				// used for debugging (anyway they were treated in the same
				// way).

				//while (prerr.ready()) outline+=prerr.readLine()+"\n";
				outlinechunk.append(outline+"\n");
				//}

			}
			while (!terminating && ((prout.ready() && (outlinechunk.length()<maxChunk)) || (outlinechunk.length()==0)));
			return outline;
		}
	}
	
	public ServThread(ParDebug d) {
		mainThread = d;
		flag = 0;
		terminating = false;
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

	boolean terminating;

	/* This method is specific for each type of input method:
	 * STDOUT for programs started directly;
	 * CCS for programs attached.
	 */
	abstract String getNextOutput(StringBuffer outlinechunk) throws Exception;
	
	public void terminate() { terminating=true; }
	
	public void run() {
		//runtime = Runtime.getRuntime();
		try {
			System.out.println("ServThread started");
			try {
				while (true) 
				{ /* Fetch a chunk of output and print it.
				 * Passing the output to the GUI in large chunks is 
				 * much much faster than passing it one line at a time.
				 */
					StringBuffer outlinechunk=new StringBuffer();
					String outline = getNextOutput(outlinechunk);
					
					process(outlinechunk);
					
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
			if (isInterrupted()) {
				System.out.println("Disconnecting from parallel program");
			} else {
				System.out.println("Finished running parallel program");
			}
			Runnable doWorkRunnable = new Runnable() {
				public void run() { mainThread.quitProgram(); }
			};
			SwingUtilities.invokeLater(doWorkRunnable);
			//debugOutput.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("error in ServThread. Exception caught");
		} 
	}
	
	void process(StringBuffer outlinechunk) {
		int cpdStart;
		while ((cpdStart = outlinechunk.indexOf("CPD: ")) != -1) {
			int cpdEnd = outlinechunk.indexOf("\n", cpdStart+1);
			String outline = outlinechunk.substring(cpdStart+5, cpdEnd);
			outlinechunk.delete(cpdStart, cpdEnd+1);
			int pe = Integer.parseInt(outline.substring(0, outline.indexOf(' ')));
			outline = outline.substring(outline.indexOf(' ')+1);
			mainThread.setStatusMessage(outline);
			int type = 0, begin = 0;

			if (outline.indexOf("BP", 0) != -1) {
				type = NotifyEvent.BREAKPOINT;
				begin = 3;
			} else if (outline.indexOf("Freeze") != -1) {
				type = NotifyEvent.FREEZE;
				begin = 7;
			} else if (outline.indexOf("Abort") != -1) {
				type = NotifyEvent.ABORT;
				begin = 6;
			} else if (outline.indexOf("Signal") != -1) {
				type = NotifyEvent.SIGNAL;
				begin = 7;
			} else if (outline.indexOf("Cross") != -1) {
				type = NotifyEvent.CORRUPTION;
				begin = 6;
			} else {
				System.out.println("ServThread: error while processing string '"+outline+"'");
			}

			Runnable doWorkRunnable = new Notify(new NotifyEvent(type, pe, outline.substring(begin)));
			SwingUtilities.invokeLater(doWorkRunnable);
		}
		if (outlinechunk.length() > 0) {
			System.out.println("Parallel program printed: "+outlinechunk.toString());
			mainThread.displayProgramOutput(outlinechunk.toString());
		}
	}

	public int getFlag() { return flag; }
	
	public class Notify implements Runnable {
		private NotifyEvent e;
		public Notify(NotifyEvent e_) {e = e_;}
		public void run() {
			mainThread.notify(e);
		}
	}
};

