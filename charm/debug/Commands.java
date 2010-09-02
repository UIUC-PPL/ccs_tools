package charm.debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.text.ParseException;

import charm.debug.event.NotifyEvent;
import charm.debug.event.NotifyListener;
import charm.debug.fmt.PConsumer;
import charm.debug.fmt.PList;
import charm.debug.pdata.EpInfo;
import charm.debug.pdata.EpPList;

public class Commands implements NotifyListener, Runnable {
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
	
	public void run() {
		apply();
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
    		if (command.charAt(0)=='#') continue;
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
    		else if (command.startsWith("attach")) {
    			if (! command.equals("attach")) {
    				String parameters = command.substring(command.indexOf(' ')+1);
    				String hostname = parameters.substring(0, parameters.indexOf(' '));
    				String port = parameters.substring(parameters.indexOf(' ')+1);
    				debugger.setCcsParameters(hostname, port);
    			}
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
    		else if (command.startsWith("peset")) {
    			PeSet pes = debugger.getSelectedPeSet();
        		String bufTitle = new String("Details for set \""+pes.getName()+"\"");
        		String buf = pes.getDetail();
        		System.out.println(bufTitle+": {"+buf+" }");
    		}
    		else if (command.equals("quit")) {
        		debugger.server.bcastCcsRequest("ccs_debug_quit", "");
        		debugger.quitProgram(); 
    		}
			else if (command.equals("continue")) {
				//debugger.server.bcastCcsRequest("ccs_continue_break_point", "", debugger.getExecution().npes);
				debugger.command_continue();
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
			else if (command.startsWith("breakpoint")) {
				String condition = command.substring(command.indexOf(' ')).trim();
				String chareName = condition.substring(0, condition.indexOf(' '));
				String epName = condition.substring(condition.indexOf(' ')).trim();
				EpPList eps = ParDebug.debugger.getEpItems();
				Iterator it = eps.iterate();
				EpInfo info = null;
				boolean found = false;
				while (it.hasNext()) {
					info = (EpInfo)it.next();
					if (info.equals(chareName, epName)) {
						found = true;
						break;
					}
				}
				if (found) {
					System.out.println("Setting breakpoint: "+info.getChareName()+"::"+info.toString()+" ("+info.getEpIndex()+")");
					debugger.command_breakpoint(info.getEpIndex());
					//ParDebug.debugger.server.bcastCcsRequest("ccs_set_break_point", ""+info.getEpIndex());
					//info.getCheckBox().click();
				}
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
			else if (command.startsWith("deliver") || command.startsWith("conditional") ||
					 command.startsWith("undeliver") || command.startsWith("confirm")) {
				int separator = command.indexOf(' ');
				int index = 0;
				try {
					index = Integer.parseInt(command.substring(separator+1));
				} catch (NumberFormatException nfe) { System.out.println("Incorrect delivery index"); }
				debugger.deliverMessage(index, command.substring(0, separator));
			}
			else if (command.startsWith("repeat")) {
				int count = 1;
				int start = command.indexOf(' ');
				int end = command.indexOf(' ', start+1);
				if (end == -1) end = command.length();
				try {
					count = Integer.parseInt(command.substring(start+1, end));
				} catch (NumberFormatException nfe) { System.out.println("Could not understand how many times to repeat... defaulting to 1"); }
				String terminator = command.substring(end).trim();
				int match = position + 1;
				if (terminator.length() > 0) {
					while (match < list.size() && ! terminator.equals(list.elementAt(match))) match ++;
					if (match == list.size()) {
						System.err.println("Commands: Could not find a match for repeat string \""+terminator+"\"");
						System.exit(1);
					}
					list.removeElementAt(match);
				}
				for (int i=1; i<count; ++i) {
					for (int j=match-1; j>=position; --j) {
						list.insertElementAt(list.elementAt(j), match);
					}
				}
			}
    		else {
    			System.out.println("Command not recognized: "+command);
    		}
    	}
	}
	
	public void receiveNotification(NotifyEvent e) {
		events.add(e);
		System.out.println("Received notification "+e);
		if (waiting) {
			waiting = false;
			apply();
		}
	}
}
