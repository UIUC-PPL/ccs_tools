package charm.debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;
import java.text.ParseException;

import charm.debug.event.NotifyEvent;
import charm.debug.event.NotifyListener;
import charm.debug.fmt.PConsumer;
import charm.debug.fmt.PList;

public class Commands implements NotifyListener {
	ParDebug debugger;
	Vector list;
	int position;
	Vector events;
	boolean waiting;
	
	public Commands(File f, ParDebug d) {
		debugger = d;
		list = null;
		position = 0;
		try {
			BufferedReader com = new BufferedReader(new FileReader(f));
			System.out.println("Loading commands: "+f.getAbsolutePath());
			list = new Vector();
			events = new Vector();
			String str = null;
			while ((str = com.readLine()) != null) list.add(str); 
		} catch (IOException ioe) {
			System.out.println("Could not open command file ");
			ioe.printStackTrace();
		}
	}
	
	public String getNext() {
		if (position == list.size()) return null;
		return (String)list.elementAt(position++);
	}
	public void pushBack() {
		position--;
	}
	
	public void apply() {
		String command;
    	while ((command = getNext()) != null) {
    		System.out.println("applying command: "+command);
    		if (command.startsWith("sleep")) {
    			long time = Integer.parseInt(command.substring(command.indexOf(' ')).trim());
    			long end = (new Date()).getTime() + time;
    			long now;
    			while ((now=(new Date()).getTime()) < end) {
    				try {
    					Thread.sleep(end - now);
    				} catch (InterruptedException e) {/* Ignore exception */}
    			}
    		}
    		else if (command.equals("start")) {
    			debugger.startProgram(false);
    		}
    		else if (command.equals("attach")) {
    			debugger.startProgram(true);
    		}
    		else if (command.startsWith("python")) {
    			command = command.substring(command.indexOf(' ')).trim();
    			File f = new File(command.substring(0, command.indexOf(' ')));
    			PythonScript script = new PythonScript(debugger.getGdb());
    			String code;
    			try {
	                code = script.loadPythonCode(f);
                } catch (IOException e) {
                	System.out.println("could not load file "+f.getAbsolutePath());
                	break;
                }
                try {
	                script.parseCode(code);
                } catch (ParseException e) {
                	System.out.println("could not parse python code: "+code);
                }
                command = command.substring(command.indexOf(' ')).trim();
    			int boundChare = Integer.parseInt(command.substring(0, (command+" ").indexOf(' ')));
    			script.setChare(debugger.getGroupItems().elementAt(boundChare));
    			int next;
    			while ((next = command.indexOf(' ')) != -1) {
    				command = command.substring(next).trim();
    				int ep = Integer.parseInt(command.substring(0, (command+" ").indexOf(' ')));
    				int where = ep>0 ? 1 : 0;
    				ep = ep>0 ? ep : -ep;
    				script.addEP(where, debugger.getEpItems().getEntryFor(ep));
    			}
    			debugger.executePython(script);
    		}
    		else if (command.startsWith("time")) {
    			Date now = new Date();
    			System.out.println(command.substring(5)+" "+now+" ("+now.getTime()+")");
    		}
    		else if (command.equals("quit")) {
        		debugger.server.bcastCcsRequest("ccs_debug_quit", "", debugger.getExecution().npes);
        		debugger.quitProgram(); 
    		}
			else if (command.equals("continue")) {
				debugger.server.bcastCcsRequest("ccs_continue_break_point", "", debugger.getExecution().npes);
			}
			else if (command.startsWith("memstat")) {
				String input = command.substring(command.indexOf(' ')).trim();
				int pe = Integer.parseInt(input);;
				if (pe == -1) pe = 0;
	    		byte[] buf = ParDebug.debugger.server.sendCcsRequestBytes("ccs_debug_memStat", input, pe);
	    		PConsumer cons=new PConsumer();
	    		cons.decode(buf);
	    		PList stat = cons.getList();
	    		System.out.println(stat);
			}
			else if (command.equals("allocation")) {
				
			}
			else if (command.startsWith("wait")) {
				String condition = command.substring(command.indexOf(' ')).trim();
				boolean matchSuccess = false;
				int type = 0;
				if (condition.equals("breakpoint")) {
					type = NotifyEvent.BREAKPOINT;
				} else if (condition.equals("freeze")) {
					type = NotifyEvent.FREEZE;
				}
				for (int i=0; i<events.size(); ++i) {
					if (((NotifyEvent)events.get(i)).type == type) {
						matchSuccess = true;
						events.remove(i);
						break;
					}
				}

				if (!matchSuccess) {
					pushBack();
					waiting = true;
					break;
				}
			}
    		else {
    			System.out.println("Command not recognized: "+command);
    		}
    	}
	}
	
	public void receiveNotification(NotifyEvent e) {
		events.add(e);
		if (waiting) {
			waiting = false;
			apply();
		}
	}
}
