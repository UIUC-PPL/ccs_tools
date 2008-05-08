package charm.debug;

import java.io.*;

public class GdbProcess {
	ParDebug mainThread;
	Process gdb = null;
	// streams to communicate with the gdb process
	public BufferedWriter gdbIn;
	public BufferedReader gdbOut;
	private FileWriter debugOutput;

	public GdbProcess(ParDebug pd) {
		mainThread = pd;
		try {
			debugOutput = new FileWriter("servthread_debug.out");
		} catch (IOException e) {
			System.out.println("Error while opening dubug output");
		}
	}
	
	public String infoCommand(String command) {
		if (gdb == null) {
			String executable = new File(mainThread.getFilename()).getAbsolutePath();
			String totCommandLine = "gdb " + executable;
			String hostname = mainThread.getHostname();
			if (!hostname.equals("localhost")) {
				totCommandLine = hostname + " " + totCommandLine;
				String username = mainThread.getUsername();
				if (!username.equals("")) {
					totCommandLine = "-l " + username + " " + totCommandLine;
				}
				totCommandLine = "ssh " + totCommandLine;
			}
			try {
				//System.out.println("Starting: '"+totCommandLine+"'");
				gdb = Runtime.getRuntime().exec(totCommandLine);
				gdbIn = new BufferedWriter(new OutputStreamWriter(gdb.getOutputStream()));
				gdbOut = new BufferedReader(new InputStreamReader(gdb.getInputStream()));
			} catch (Exception e) {
				System.out.println("Failed to start gdb info program");
				return "error";
			}
			infoCommand(" ");
		}
		try {
			gdbIn.write(command);
			gdbIn.flush();
			try {
				debugOutput.write("request: {"+command+"}");
			} catch (Exception ex) {}
			StringBuffer reply = new StringBuffer();
			while (!reply.toString().endsWith("(gdb) ")) {
				//Thread.yield();
				while (gdbOut.ready()) reply.append((char)gdbOut.read());
			}
			reply.setLength(reply.length()-6);
			try {
				debugOutput.write("reply: |"+reply+"|");
				debugOutput.flush();
			} catch (Exception ex) {}
			return reply.toString();
		} catch (IOException e) {
			return "error";
		}
	}

	public void terminate() {
		if (gdb != null) gdb.destroy();
		gdb = null;
	}
}
