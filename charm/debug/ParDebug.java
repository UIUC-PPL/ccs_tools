/**
  Parallel Debugger for Charm++ over CCS
  
  This is the main GUI panel, and is the central controller
  for everything the debugger does.
  
  Copyright University of Illinois at Urbana-Champaign
  Written by Rashmi Jothi in Fall of 2003
  Minor improvements by Orion Lawlor in 1/2004
*/
package charm.debug;

import charm.ccs.*;
import charm.debug.event.NotifyEvent;
import charm.debug.event.NotifyListener;
import charm.debug.fmt.*;
import charm.debug.pdata.*;
import charm.debug.inspect.Inspector;
import charm.debug.inspect.InspectPanel;
import charm.debug.preference.*;
import charm.util.ReflectiveXML;

import javax.swing.*;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.swing.tree.*;
import org.xml.sax.SAXException;

public class ParDebug extends JPanel
     implements ActionListener,ListSelectionListener{
   
	public final static int MAJOR = 10;
	public final static int MINOR =  4;
	
    // ******* VARIABLES ************   
    //  FIXME: make these not be static, by moving main's command line
    //   handling into a regular function called by the constructor.
	private Preference preferences;
	private Execution exec;
    //private static String filename;
    //private static String hostname;
    //private static String username;
    private String portnumber;
    private static String hostnumber;
    //private static int numberPes;
    //private static String clparams;
    private static String envDisplay;
    //private static boolean tunnelNeeded;
    private static Process sshTunnel;
    public static byte[] globals;
    public static int dataPos;
    static ServThread servthread;
    private static GdbProcess gdb;

    /// This variable is responsible for handling all the CCS communication with
    /// the running application
    public CpdUtil server;

    //private CcsServer ccs; DEPRACATED: it goes into the variable "server"
    private DefaultListModel listModel;
    private PList listItems = null;
    private boolean isRunning = false; // True if the debugged program is running
    //private boolean[] peList = null;
    public static int currentListedPE;
    public int currentPopulatedList;
    
    private Processor[] pes = null;

	private CharePList groupItems;
    private MsgPList messageQueue;
    private EpPList epItems;
    private MsgTypePList msgItems;
    private ChareTypePList chareItems;
    //private SortedSet breakpointSet[];
    private int interpreterHandle;
    private PythonInstalledCode installedPythonScripts;
    private Vector notificationListeners;
    
    private class CpdListInfo {
       public String display; // Client name CpdList should be displayed as.
       public String name; // Server name basic CpdList is registered under.
       public String detailedName; // Server name of detailed CpdList.
        public GenericPList list; // Class that will be created consequently of the server reply

       public CpdListInfo(String display_,String name_) {
           display=display_; name=name_; detailedName=null; list=null;
       }
       public CpdListInfo(String display_,String name_,String detailed_,GenericPList list_) {
           display=display_; name=name_; detailedName=detailed_; list=list_;
       }
    };
    
    /// CpdList names
    private CpdListInfo[] cpdLists =  {
        new CpdListInfo("-- Display --",null,null,null),
        //new CpdListInfo("Array Elements","charm/arrayElementNames","charm/arrayElements",new CharePList()),
        new CpdListInfo("Charm Objects","charm/objectNames",null,groupItems=new CharePList()),
        new CpdListInfo("Array Elements","charm/arrayElements",null,new CharePList()),
	new CpdListInfo("Messages in Queue","converse/localqueue",null,messageQueue=new MsgPList()),
	new CpdListInfo("Readonly Variables","charm/readonly",null,new ReadonlyPList()),
	new CpdListInfo("Readonly Messages","charm/readonlyMsg",null,null),
	new CpdListInfo("Messages delivered stack","charm/messageStack", null, new MsgStackPList()),
	new CpdListInfo("Entry Points","charm/entries",null,epItems=new EpPList()),
	new CpdListInfo("Chare Types","charm/chares",null,chareItems=new ChareTypePList()),
	new CpdListInfo("Message Types","charm/messages",null,msgItems=new MsgTypePList()),
	new CpdListInfo("Mainchares","charm/mains",null,null),
	new CpdListInfo("Viewable Lists","converse/lists",null,null)
    };

    // ********* GUI ITEMS ************
    private static JFrame appFrame = null;
   
    private JButton startButton;
    private JButton continueButton;
    private JButton stepButton;
    private JButton quitButton;
    private JButton freezeButton;
    private JButton startGdbButton;
    
    private DefaultListModel peListModel;
    private JList peList;
    //private JPanel sysEpsActualPanel;
    //private JPanel userEpsActualPanel;
    
    private JTextArea programOutputArea;
    private JScrollPane outputAreaScrollPane;
    private InspectPanel outputPanel;
    private PListOutputArea outputArea;
    private JTextArea newOutputArea;
    private JSplitPane bottomSplitPane;
    private JTextField statusArea;
    private JComboBox listsbox;
    private JComboBox pesbox;
    private JList listItemNames;
    private JScrollPane entryScrollPane;
    
    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenu menuAction;
    private JMenuItem menuFileOpen;
    private JMenuItem menuFileEdit;
    private JMenuItem menuFileSave;
    private JMenu menuRecent;
    private JMenuItem menuWindowSettings;
    private JMenuItem menuActionStart; 
    private JMenuItem menuActionAttach;
    private JMenuItem menuActionContinue;
    private JMenuItem menuActionQuit;
    private JMenuItem menuActionDisconnect;
    private JMenuItem menuActionFreeze;
    private JMenuItem menuActionPython;
    private JMenuItem menuActionPythonInstalled;
    private JMenu menuMemory;
    private JMenuItem menuMemoryView;
    private JMenuItem menuMemoryAllocationTree;
    private JMenuItem menuMemoryAllocationGraph;
    private JMenuItem menuMemoryLeak;
    private JMenuItem menuMemoryQuickLeak;
    private JMenuItem menuMemoryMark;
    private JMenuItem menuMemoryUnmark;
    private JMenuItem menuMemoryStatistics;
    

/************** CCS (network) interface ************ /
DEPRECATED!! The correct implementation is in CpdList.java
    private int getListLength(String listName,int forPE)
    {
    try {
      //Build a byte array describing the ccs request:
      int reqStr=listName.length();
      int reqLen=4+reqStr+1;
      // System.out.println("getListLength "+listName+" for PE "+forPE);
      byte[] req=new byte[reqLen];
      CcsServer.writeInt(req,0,reqStr);
      CcsServer.writeString(req,4,reqStr+1,listName);
      CcsServer.Request r=ccs.sendRequest("ccs_list_len",forPE,req);
 
      //Get the response and take it apart:
      byte[] resp=ccs.recvResponse(r);
      if (resp.length<4) return -1;
      return CcsServer.readInt(resp,0);
    } catch (IOException e) {
      e.printStackTrace();
      abort("Network error connecting to PE "+forPE+" to access list "+listName);
      return 0;
    }
    }

    //Returns a set of CpdList items as a byte array
    private byte[] byteList(String listName,String fmt,int forPE,int lo,int hiPlusOne)
    {
    try {
      //Build a byte array describing the ccs request:
      int reqStr=listName.length();
      int reqLen=4+4+4+0+4+reqStr+1;
      byte[] req=new byte[reqLen];
      CcsServer.writeInt(req,0,lo);
      CcsServer.writeInt(req,4,hiPlusOne);
      CcsServer.writeInt(req,8,0); / *no additional request data* /
      CcsServer.writeInt(req,12,reqStr);
      CcsServer.writeString(req,16,reqStr+1,listName);
      CcsServer.Request r=ccs.sendRequest("ccs_list_items."+fmt,forPE,req);
 	
      return ccs.recvResponse(r);
    } catch (IOException e) {
      e.printStackTrace();
      abort("Network error connecting to PE "+forPE+" to access list "+listName);
      return null;
    }
    }
    
    //Returns a set of CpdList items as a PList
    private PList getPList(String listName,int forPE,int lo,int hiPlusOne) {
    	byte[] buf=byteList(listName,"fmt",forPE,lo,hiPlusOne);
	PConsumer cons=new PConsumer();
	cons.decode(buf);
	return cons.getList();
    }

    //Sends a request to the ccs server
    private String sendCcsRequest(String ccsHandlerName, String parameterName, int destPE) 
    {
    try {
      //Build a byte array describing the ccs request:
      int reqStr=parameterName.length();
      int reqLen = reqStr+1;
      byte[] req=new byte[reqLen];
      CcsServer.writeString(req,0,reqStr+1,parameterName);
      CcsServer.Request r=ccs.sendRequest(ccsHandlerName,destPE,req);
      if ( parameterName.equalsIgnoreCase("freeze") || ccsHandlerName.equalsIgnoreCase("ccs_debug_quit") || ccsHandlerName.equalsIgnoreCase("ccs_remove_all_break_points") || ccsHandlerName.equalsIgnoreCase("ccs_set_break_point") || ccsHandlerName.equalsIgnoreCase("ccs_remove_break_point") || ccsHandlerName.equalsIgnoreCase("ccs_continue_break_point"))
      {
        return null;
      }
      else {
         byte[] resp=ccs.recvResponse(r);
         return new String(resp);
      }
    } catch (IOException e) {
      e.printStackTrace();
      abort("Network error connecting to PE "+destPE+" to perform "+ccsHandlerName);
      return null;
    }
    }

    //if parameter forSelectedPes <= 0, ccs message sent to all pes
    private void bcastCcsRequest(String ccsHandlerName, String parameterName, int forSelectedPes)
    {
          if (forSelectedPes <= 0)
          { / * Send to all pes * /
                for (int indexPE=0; indexPE < numberPes; indexPE++) {
                    sendCcsRequest(ccsHandlerName, parameterName, indexPE);
		}
          }
          else
          { / * Send to selected subset of PEs * /
                for (int indexPE=0; indexPE < numberPes; indexPE++)
                if (peList[indexPE] == true) {
                      sendCcsRequest(ccsHandlerName, parameterName, indexPE);
                }
          }
    }
    */

    public static String infoCommand(String s) {
        return gdb.infoCommand(s);
    }

/************** Tiny GUI Routines ************/
    
    private void abort(String problem) {
      System.out.println(problem);
      
      if (false) {
   	 JDialog d=new JDialog((Frame)null,"ParDebug Fatal Error");
   	 d.addWindowListener(new WindowAdapter() {
   	       public void windowClosing(WindowEvent e) {
   	 	   System.exit(1);
   	       }
   	 });
   	 d.getContentPane().add(new Label(problem), BorderLayout.CENTER);
   	 d.pack();
   	 d.setVisible(true);
   	 while(true) { /* prevent caller from continuing */
   	 	 try { Thread.sleep(100); }
   	 	 catch(InterruptedException e1)
   	 	 { /* don't care about interrupted sleep */ }
   	 }
      }
      System.exit(1);
    }
    
    private String getEnvDisplay()
    {
      String displayEnv = null;
      String localip = null;
      try
      {
        localip = (InetAddress.getLocalHost()).getHostAddress();
        displayEnv = localip+":0.0";
        System.out.println("DISPLAY variable = " +displayEnv);
      }
      catch (Exception exc)
      {
        abort("Error retrieving IP address for DISPLAY variable: "+exc.getMessage());
      }
      return displayEnv;
    }

    // Called by "ServThread::run" when new terminal output arrives.
    public void displayProgramOutput (String line)
    {
       programOutputArea.append(line);
       programOutputArea.scrollRectToVisible(new Rectangle(
         0,programOutputArea.getHeight()-2, 
         1, 1
       ));
    } 
    
/*
    private static void setNumberPes(String peNumber) {
       try {
         numberPes = Integer.parseInt(peNumber);
       } 
       catch(Exception e) {
         System.out.println("Could not convert number of pes "+peNumber+" to an integer.");
	 System.exit(1); / * FIXME: this is overkill. * /
       }
    }
    
    public void setParametersForProgram(String commandLine, String peNumber, String portno, String host, String user, boolean ssh)
    {
       setNumberPes(peNumber);
       clparams = commandLine;
       portnumber = portno;
       hostname = host;
       username = user;
       tunnelNeeded = ssh;
       addedRunParameter();
    }
*/
    public int getNumPes() {
    	return exec.virtualDebug ? exec.virtualNpes : exec.npes;
    }
    public GdbProcess getGdb() {
    	return gdb;
    }
    public int getSelectedPe() {
    	return Integer.parseInt((String)pesbox.getSelectedItem());
    }
    public Processor getSelectedProcessor() {
    	return pes[getSelectedPe()];
    }
    private void addedRunParameter() {
       setStatusMessage("Executable: " +exec.executable+ "        number of pes: "+exec.npes);
    }
    private void loadedRunParameter() {
    	setStatusMessage("Loaded configuration => Executable: "+exec.executable+"     number of pes: "+exec.npes);
    }
    private void savedRunParameter(String file) {
    	setStatusMessage("Saved current configuration to file  "+file);
    }

    public void setStatusMessage (String txt)
    {
       statusArea.setText(txt);
    }
    
    public void addNotifyListener(NotifyListener l) {
    	notificationListeners.add(l);
    }
    public void removeNotifyListener(NotifyListener l) {
    	notificationListeners.remove(l);
    }
    
    /** Method called by ServThread to notify all listeners about events coming from the parallel application */
    public void notify(NotifyEvent e) {
    	for (int i=0; i<notificationListeners.size(); ++i) ((NotifyListener)notificationListeners.get(i)).receiveNotification(e);
    }
    
    /** Class containing all the notifications coming from the parallel application.
     *  This class is registered as a NotifyListener by ParDebug when initializing.
     *  Being an internal class it has access to all local variables of ParDebug.
     */
    private class NotifyGUI implements NotifyListener {

    	public void receiveNotification(NotifyEvent e) {
    		switch (e.type) {
    		case NotifyEvent.BREAKPOINT:
    			notifyBreakpoint(e.pe, e.txt);
    			break;
    		case NotifyEvent.FREEZE:
    			notifyFreeze(e.pe, e.txt);
    			break;
    		case NotifyEvent.ABORT:
    			notifyAbort(e.pe, e.txt);
    			break;
    		case NotifyEvent.SIGNAL:
    			notifySignal(e.pe, e.txt);
    			break;
    		case NotifyEvent.CORRUPTION:
    			notifyCorruption(e.pe, e.txt);
    			break;
    		}
    	}

    	public void notifyBreakpoint (int pe, String txt) {
    		System.out.println("notifyBreakpoint: "+txt+" pe="+pe);
    		if (pes[pe].isFrozen()) {
    			System.err.println("Error: processor "+pe+" already frozen!");
    			return;
    		}
    		pes[pe].setFrozen();
    		enableButtons();
    		peList.repaint();
    		//stepButton.setEnabled(true);
    		//continueButton.setEnabled(true);
    		//freezeButton.setEnabled(false);
    		CpdListInfo list = cpdLists[listsbox.getSelectedIndex()];
    		if (list.list == messageQueue) {
    			int forPE=Integer.parseInt((String)pesbox.getSelectedItem());
    			if (forPE == pe)
    				populateNewList(listsbox.getSelectedIndex(),forPE, listModel);
    		}
    		//setStatusMessage(txt);
    	}

    	public void notifyFreeze (int pe, String txt) {
    		System.out.println("notifyFreeze: "+txt+" pe="+pe);
    		if (pes[pe].isFrozen()) {
    			System.err.println("Error: processor "+pe+" already frozen!");
    			return;
    		}
    		pes[pe].setFrozen();
    		enableButtons();
    		peList.repaint();
    		//stepButton.setEnabled(true);
    		//continueButton.setEnabled(true);
    		//freezeButton.setEnabled(false);
    		CpdListInfo list = cpdLists[listsbox.getSelectedIndex()];
    		if (list.list == messageQueue) {
    			int forPE=Integer.parseInt((String)pesbox.getSelectedItem());
    			if (forPE == pe)
    				populateNewList(listsbox.getSelectedIndex(),forPE, listModel);
    		}
    	}

    	public void notifyAbort (int pe, String txt) {
    		System.out.println("notifyAbort: "+txt+" pe="+pe);
    		pes[pe].setDead();
    		enableButtons();
    		peList.repaint();
    		CpdListInfo list = cpdLists[listsbox.getSelectedIndex()];
    		if (list.list == messageQueue) {
    			int forPE=Integer.parseInt((String)pesbox.getSelectedItem());
    			if (forPE == pe)
    				populateNewList(listsbox.getSelectedIndex(),forPE, listModel);
    		}
    	}

    	public void notifySignal (int pe, String txt) {
    		//System.out.println("signal string: "+txt);
    		int signal = Integer.parseInt(txt);
    		System.out.println("notifySignal: "+txt+" pe="+pe+", sigNo="+signal);
    		pes[pe].setDead();
    		enableButtons();
    		peList.repaint();
    		CpdListInfo list = cpdLists[listsbox.getSelectedIndex()];
    		if (list.list == messageQueue) {
    			int forPE=Integer.parseInt((String)pesbox.getSelectedItem());
    			if (forPE == pe)
    				populateNewList(listsbox.getSelectedIndex(),forPE, listModel);
    		}
    	}

    	public void notifyCorruption (int pe, String txt) {
    		int space = txt.indexOf(' ');
    		String address = txt.substring(0, space); //Inspector.is64bit() ? Long.parseLong(txt.substring(2, space), 16) : Integer.parseInt(txt.substring(2, space), 16);
    		txt = txt.substring(space+1);
    		space = txt.indexOf(' ');
    		int chare = Integer.parseInt(txt.substring(0, space));
    		txt = txt.substring(space+1);
    		space = txt.indexOf(' ');
    		int owner = Integer.parseInt(txt.substring(0, space));
    		System.out.print("cross corruption: chare "+chare+" accessed memory location "+address+" owned by chare "+owner);
    		txt = txt.substring(space+1);
    		space = txt.indexOf(' ');
    		int count = Integer.parseInt(txt.substring(0, space));
    		if (count > 0) System.out.println(": {");
    		for (int i=0; i<count; ++i) {
    			txt = txt.substring(space+1);
    			space = txt.indexOf(' ');
    			System.out.println("  "+Symbol.get(Long.parseLong(txt.substring(2, space), 16)));
    		}
    		if (count > 0) System.out.print("}");
    		txt = txt.substring(space+1);
    		space = txt.indexOf(' ');
    		count = Integer.parseInt(txt.substring(0, space));
    		if (count > 0) System.out.println(" Stack trace of corruption: {");
    		for (int i=0; i<count; ++i) {
    			txt = txt.substring(space+1);
    			space = txt.indexOf(' ');
    			System.out.println("  "+Symbol.get(Long.parseLong(txt.substring(2, space), 16)));
    		}
    		if (count > 0) System.out.print("}");
    		System.out.println();
    	}
    }

    public Dimension getPreferredSize() {
    	if (preferences.size != null) return preferences.size;
    	return super.getPreferredSize();
    }
    
    public Point getPreferredLocation() {
    	return preferences.location;
    }
    
    public Preference getPreferences() {
    	return preferences;
    }

	public MsgPList getMessageList() {
		return new MsgPList(messageQueue);
	}
    
    /// The user has just selected the cpdListIndex'th list on forPE.
    ///  Expand a list of the contents into dest.
    private void populateNewList(int cpdListIndex,int forPE, DefaultListModel dest) {
    	dest.removeAllElements();
    	outputArea.setList(null);
    	newOutputArea.setText("");
    	listItems = null;

    	String lName=cpdLists[cpdListIndex].name;
    	if (lName==null) return; /* the initial empty list */
    	GenericPList list = cpdLists[cpdListIndex].list;

    	if (currentPopulatedList != -1 && cpdListIndex != currentPopulatedList) cpdLists[currentPopulatedList].list.removePopupMenu(listItemNames);
    	if (list == null || list.needRefresh()) {
    		int nItems=server.getListLength(lName,forPE);
    		listItems = server.getPList(lName,forPE,0,nItems);
    	}

    	if (list != null) {
    		if (list.needRefresh()) list.load(listItems);
    		list.populate(dest, listItemNames);
    	} else {
    		for (PAbstract cur=listItems.elementAt(0);cur!=null;cur=cur.getNext()) {
    			dest.addElement(cur.getDeepName());
    		}
    	}
    	currentPopulatedList = cpdListIndex;
    }
    
    /// The user has just selected listItem from the cpdListIndex'th list on forPE.
    ///  Expand this item.
    private void expandListElement(int cpdListIndex,int forPE,int listItem)
    {
        currentListedPE = forPE;
    	String detailedName=cpdLists[cpdListIndex].detailedName;
    	int position = bottomSplitPane.getDividerLocation();
	if (detailedName==null) {
                if (cpdLists[cpdListIndex].list != null) {
                    //outputAreaScrollPane.setViewportView(newOutputArea);
                	bottomSplitPane.setRightComponent(outputPanel);
                	bottomSplitPane.setDividerLocation(position);
                    Object selected = listItemNames.getSelectedValue();
                    if (selected instanceof GenericInfo) {
                        //newOutputArea.setText(((GenericInfo)selected).getDetails());
                        //System.out.println(((GenericInfo)selected).getDetails());
                        //newOutputArea.setCaretPosition(0);
                    	//outputAreaScrollPane.setViewportView(((GenericInfo)selected).getDetails());
                    	//bottomSplitPane.setRightComponent(((GenericInfo)selected).getDetails());
                    	((GenericInfo)selected).getDetails(outputPanel);
                    } else {
                        System.out.println("Error: element not of type GenericInfo");
                    }
                } else {
                    //outputAreaScrollPane.setViewportView(outputArea);
                	bottomSplitPane.setRightComponent(outputAreaScrollPane);
                	bottomSplitPane.setDividerLocation(position);
                    outputArea.setList((PList)listItems.elementAt(listItem));
                }
	} else { /* There's a list to consult for further detail */
                //outputAreaScrollPane.setViewportView(outputArea);
		bottomSplitPane.setRightComponent(outputAreaScrollPane);
    	bottomSplitPane.setDividerLocation(position);
		PList detailList=server.getPList(detailedName,forPE,listItem,listItem+1);
		outputArea.setList((PList)detailList);//.elementAt(0));
	}
    }

    private void listenTo(JMenuItem m,String command,String toolTip) {
       if (toolTip!=null) m.setToolTipText(toolTip);
       m.setActionCommand(command);
       m.addActionListener(this);
    }
    
/************** Giant Horrible GUI Routines ************/
    public ParDebug(Execution e) {
       isRunning = false;
       installedPythonScripts = null;
       currentPopulatedList = -1;
       //preferences = new Preference();
       //preferences.load();
	   preferences = Preference.load();
	   if (preferences == null) preferences = new Preference();
       exec = e;

       setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       
       //Creating the menu
       menuBar = new JMenuBar();
       
       menuBar.add(menuFile = new JMenu("File"));
       menuFile.setMnemonic('F');
       
       menuFile.add(menuFileOpen = new JMenuItem("Open Configuration",'O'));
       listenTo(menuFileOpen,"openConf","Open the configuration of a parallel program to debug");
       menuFile.add(menuFileEdit = new JMenuItem("Edit Configuration",'E'));
       listenTo(menuFileEdit,"editConf","Edit the parameters for the current configuration");
       menuFile.add(menuFileSave = new JMenuItem("Save Configuration",'S'));
       listenTo(menuFileSave,"saveConf","Save the current configuration");
       menuFile.add(menuRecent = new JMenu("Recent Configurations"));
       updateRecentConfig();
       menuFile.addSeparator();
       menuFile.add(menuWindowSettings = new JMenuItem("Save Window Settings",'W'));
       listenTo(menuWindowSettings,"saveWindowSet","Save the current window settings");
       JMenuItem menuFileExit;
       menuFile.add(menuFileExit = new JMenuItem("Exit Debugger",'X'));
       listenTo(menuFileExit,"exitDebugger",null);
       
       menuBar.add(menuAction = new JMenu("Action"));
       menuAction.setMnemonic('A');
       
       menuAction.add(menuActionStart = new JMenuItem("Start",'S'));
       listenTo(menuActionStart,"begin","Start the parallel program"); 
       menuAction.add(menuActionAttach = new JMenuItem("Attach",'A'));
       listenTo(menuActionAttach,"attach","Attach to a running parallel program"); 
       menuAction.add(menuActionContinue = new JMenuItem("Continue",'C'));
       listenTo(menuActionContinue,"unfreeze","Continue to run the parallel program"); 
       menuAction.add(menuActionFreeze = new JMenuItem("Freeze",'F'));
       listenTo(menuActionFreeze,"freeze","Freeze the parallel program"); 
       menuAction.add(menuActionDisconnect = new JMenuItem("Disconnect",'D'));
       listenTo(menuActionDisconnect,"disconnect","Disconnect from the parallel program"); 
       menuAction.add(menuActionQuit = new JMenuItem("Quit",'Q'));
       listenTo(menuActionQuit,"quit","Quit the parallel program"); 
       menuAction.addSeparator();
       menuAction.add(menuActionPython = new JMenuItem("Python script", 'P'));
       listenTo(menuActionPython,"python","Run python script on application");
       menuActionPython.setEnabled(false);
       menuAction.add(menuActionPythonInstalled = new JMenuItem("View python scripts", 'S'));
       listenTo(menuActionPythonInstalled,"pythoninstalled","View python scripts installed");
       menuActionPythonInstalled.setEnabled(false);
       
       menuBar.add(menuMemory = new JMenu("Memory"));
       menuMemory.setMnemonic('M');
       menuMemory.add(menuMemoryView = new JMenuItem("Memory View",'M'));
       listenTo(menuMemoryView,"memory","Inspect the application memory"); 
       menuMemoryView.setEnabled(false);
       menuMemory.add(menuMemoryAllocationTree = new JMenuItem("Memory Allocation Tree",'T'));
       listenTo(menuMemoryAllocationTree,"allocationTree","Print the memory allocation tree");
       menuMemoryAllocationTree.setEnabled(false);
       menuMemory.add(menuMemoryAllocationGraph = new JMenuItem("Memory Allocation Graph",'G'));
       listenTo(menuMemoryAllocationGraph,"allocationGraph","Print the memory allocation graph");
       menuMemory.addSeparator();
       menuMemory.add(menuMemoryQuickLeak = new JMenuItem("Quick Leak Search",'Q'));
       listenTo(menuMemoryQuickLeak,"leakquick","Quick search for memory leacks");
       menuMemoryQuickLeak.setEnabled(false);
       menuMemory.add(menuMemoryLeak = new JMenuItem("Leak Search",'L'));
       listenTo(menuMemoryLeak,"leaksearch","Search for memory leacks");
       menuMemoryLeak.setEnabled(false);
       menuMemory.add(menuMemoryMark = new JMenuItem("Mark Memory Clean",'M'));
       listenTo(menuMemoryMark,"memorymark","Mark all existing memory as not leaking");
       menuMemoryMark.setEnabled(false);
       menuMemory.add(menuMemoryUnmark = new JMenuItem("Unmark memory Clean",'U'));
       listenTo(menuMemoryUnmark,"memoryunmark","Removing not-leak mark on all existing memory");
       menuMemoryUnmark.setEnabled(false);
       menuMemory.add(menuMemoryStatistics = new JMenuItem("Statistics",'S'));
       listenTo(menuMemoryStatistics,"memstat","Display memory statistics");
       menuMemoryStatistics.setEnabled(false);
       
       //Creating status bar
       statusArea = new JTextField(60);
       //statusArea.setBorder(BorderFactory.createTitledBorder("Status"));
       statusArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),BorderFactory.createEmptyBorder(2,2,2,2)));
       statusArea.setMaximumSize(new Dimension(1000000,40));
       statusArea.setEditable(false);
       statusArea.setBackground(Color.lightGray); 

       // ************ MIDDLE PANEL *****************
       // Entry points on the left, program output in the middle with buttons on top, pe selection on the right
       JPanel middlePanel = new JPanel();
       middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));

       //JPanel entryPointsPanel = new JPanel();
       //entryPointsPanel.setLayout(new BoxLayout(entryPointsPanel, BoxLayout.Y_AXIS));
       //entryPointsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Set Break Points"), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
       //entryPointsPanel.setPreferredSize(new Dimension(200, 380));

       //sysEpsActualPanel = new JPanel(); 
       //sysEpsActualPanel.setLayout(new BoxLayout(sysEpsActualPanel, BoxLayout.Y_AXIS));
       //JScrollPane entryScrollPane1 = new JScrollPane(sysEpsActualPanel);
       //entryScrollPane1.setBorder(BorderFactory.createTitledBorder("System Entry Points"));
       //entryScrollPane1.setPreferredSize(new Dimension(200,185));
       //userEpsActualPanel = new JPanel(); 
       //userEpsActualPanel.setLayout(new BoxLayout(userEpsActualPanel, BoxLayout.Y_AXIS));
       //entryScrollPane2 = new JScrollPane(userEpsActualPanel);
       //entryScrollPane2.setBorder(BorderFactory.createTitledBorder("User Entry Points"));
       //entryScrollPane2.setPreferredSize(new Dimension(200,185));
       entryScrollPane = new JScrollPane();
       entryScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Set Break Points"), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
       entryScrollPane.setPreferredSize(new Dimension(200,380));

       //JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, entryScrollPane1, entryScrollPane2);
       //splitPane1.setOneTouchExpandable(true);
       //splitPane1.setResizeWeight(0.5);
       //entryPointsPanel.add(splitPane1);
       //entryPointsPanel.add(entryScrollPane1);
       //entryPointsPanel.add(Box.createRigidArea(new Dimension(0,10)));
       //entryPointsPanel.add(entryScrollPane2);


       JPanel secondPanelWithOutput = new JPanel();
       secondPanelWithOutput.setLayout(new BoxLayout(secondPanelWithOutput, BoxLayout.Y_AXIS));

       JPanel buttonPanel = new JPanel();
       buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
       buttonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Control Buttons"), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
       buttonPanel.setPreferredSize(new Dimension(500, 80));

       startButton = new JButton("Start");
       startButton.setVerticalTextPosition(AbstractButton.CENTER);
       startButton.setHorizontalTextPosition(AbstractButton.LEFT);
       startButton.setMnemonic(KeyEvent.VK_S);
       startButton.setActionCommand("begin");
       startButton.setEnabled(true);
       startButton.setToolTipText("Run the parallel program from scratch.");
       startButton.addActionListener(this);
       startButton.setPreferredSize(new Dimension(100,80));

       stepButton = new JButton("Step");
       stepButton.setVerticalTextPosition(AbstractButton.BOTTOM);
       stepButton.setHorizontalTextPosition(AbstractButton.CENTER);
       stepButton.setMnemonic(KeyEvent.VK_P);
       stepButton.setActionCommand("step");
       stepButton.setEnabled(false);
       stepButton.setToolTipText("Deliver a single message.");
       stepButton.addActionListener(this);
       stepButton.setPreferredSize(new Dimension(100,80));
     
       continueButton = new JButton("Continue");
       continueButton.setVerticalTextPosition(AbstractButton.BOTTOM);
       continueButton.setHorizontalTextPosition(AbstractButton.CENTER);
       continueButton.setMnemonic(KeyEvent.VK_U);
       continueButton.setActionCommand("unfreeze");
       continueButton.setEnabled(false);
       continueButton.setToolTipText("Resume execution.");
       continueButton.addActionListener(this);
       continueButton.setPreferredSize(new Dimension(100,80));

       freezeButton = new JButton("Freeze");
       freezeButton.setVerticalTextPosition(AbstractButton.BOTTOM);
       freezeButton.setHorizontalTextPosition(AbstractButton.CENTER);
       freezeButton.setActionCommand("freeze");
       freezeButton.setEnabled(false);
       freezeButton.setToolTipText("Stop execution.");
       freezeButton.addActionListener(this);
       freezeButton.setPreferredSize(new Dimension(100,80));

       quitButton = new JButton("Quit");
       quitButton.setVerticalTextPosition(AbstractButton.BOTTOM);
       quitButton.setHorizontalTextPosition(AbstractButton.CENTER);
       quitButton.setMnemonic(KeyEvent.VK_Q);
       quitButton.setActionCommand("quit");
       quitButton.setEnabled(false);
       quitButton.setToolTipText("End the program.");
       quitButton.addActionListener(this);
       quitButton.setPreferredSize(new Dimension(100,80));
        
       startGdbButton = new JButton("Start GDB");
       startGdbButton.setVerticalTextPosition(AbstractButton.BOTTOM);
       startGdbButton.setHorizontalTextPosition(AbstractButton.CENTER);
       startGdbButton.setActionCommand("startgdb");
       startGdbButton.setEnabled(false);
       startGdbButton.setToolTipText("Start gdb on selected processors.");
       startGdbButton.addActionListener(this);
       startGdbButton.setPreferredSize(new Dimension(100,80));

       buttonPanel.add(startButton);
       buttonPanel.add(Box.createHorizontalGlue());
       buttonPanel.add(stepButton);
       buttonPanel.add(Box.createHorizontalGlue());
       buttonPanel.add(continueButton);
       buttonPanel.add(Box.createHorizontalGlue());
       buttonPanel.add(freezeButton);
       buttonPanel.add(Box.createHorizontalGlue());
       buttonPanel.add(quitButton);
       buttonPanel.add(Box.createHorizontalGlue());
       buttonPanel.add(startGdbButton);

       JPanel outputAndPePanel= new JPanel();
       outputAndPePanel.setLayout(new BoxLayout(outputAndPePanel,BoxLayout.X_AXIS));
       programOutputArea = new JTextArea();
       programOutputArea.setColumns(100);
       programOutputArea.setLineWrap(true);
       JScrollPane programOutputScrollPane = new JScrollPane(programOutputArea);
       programOutputScrollPane.setBorder(BorderFactory.createTitledBorder("Program Output"));
       programOutputScrollPane.setPreferredSize(new Dimension(350,300));

       JPanel pePanel = new JPanel();
       pePanel.setLayout(new BoxLayout(pePanel, BoxLayout.Y_AXIS));
       pePanel.setPreferredSize(new Dimension(50, 380));
       peListModel = new DefaultListModel();
       peList = new JList(peListModel);
	   peList.setCellRenderer(new PeSet.CellRenderer());
       peList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
       peList.getSelectionModel().addListSelectionListener(new PeSetListener());
       //peActualPanel.setPreferredSize(new Dimension(50, 1));
       MouseListener popupListener = new PeSet.PopupListener(this, peList);
       peList.addMouseListener(popupListener);
       //peActualPanel.setLayout(new BoxLayout(peActualPanel, BoxLayout.Y_AXIS));
       JScrollPane pesScrollPane = new JScrollPane(peList);
       pesScrollPane.setBorder(BorderFactory.createTitledBorder("Pes"));
       pePanel.add(pesScrollPane);

       outputAndPePanel.add(programOutputScrollPane);
       outputAndPePanel.add(Box.createRigidArea(new Dimension(5,0)));
       outputAndPePanel.add(pePanel);
       outputAndPePanel.setPreferredSize(new Dimension(500,300));

       secondPanelWithOutput.add(buttonPanel);
       secondPanelWithOutput.add(Box.createRigidArea(new Dimension(0,5)));
       secondPanelWithOutput.add(outputAndPePanel);
       secondPanelWithOutput.setPreferredSize(new Dimension(500,380));

       JSplitPane middleSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, entryScrollPane, secondPanelWithOutput);
       middleSplitPane.setResizeWeight(0.5);
       middlePanel.add(middleSplitPane);
       //middlePanel.add(entryPointsPanel);
       //middlePanel.add(Box.createRigidArea(new Dimension(15,0)));
       //middlePanel.add(secondPanelWithOutput);
       middlePanel.setPreferredSize(new Dimension(600, 380));

       //add(middlePanel);
        
 
       // ************ BOTTOM PANEL *****************
       // Entity list on the left, details on the right
       JPanel bottomPanel = new JPanel();
       bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
       
       JPanel panelForComboBoxes = new JPanel();
       panelForComboBoxes.setLayout(new BoxLayout(panelForComboBoxes, BoxLayout.X_AXIS));
       panelForComboBoxes.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("View Entities on PE"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

	String[] displayStrings=new String[cpdLists.length];
	for (int i=0;i<cpdLists.length;i++)
		displayStrings[i]=cpdLists[i].display;
        listsbox = new JComboBox(displayStrings);
        listsbox.setActionCommand("lists");
        listsbox.addActionListener(this);
        listsbox.setEnabled(false);
        listsbox.setPreferredSize(new Dimension(450,70));

        pesbox = new JComboBox();
	pesbox.setActionCommand("changepe");
        pesbox.addActionListener(this);
        pesbox.setEnabled(false);
        pesbox.setPreferredSize(new Dimension(100,70));

        panelForComboBoxes.add(listsbox);
        panelForComboBoxes.add(Box.createRigidArea(new Dimension(50,70)));
        panelForComboBoxes.add(pesbox);
        panelForComboBoxes.setPreferredSize(new Dimension(600,70));
        bottomPanel.add(panelForComboBoxes);

        JPanel panelForEntities = new JPanel();
        panelForEntities.setLayout(new BoxLayout(panelForEntities, BoxLayout.X_AXIS));
        panelForEntities.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(""), BorderFactory.createEmptyBorder(1,1, 1, 1)));
        panelForEntities.setPreferredSize(new Dimension(600,230));

        //first display pane - names of the list items
        listModel = new DefaultListModel();
        listItemNames = new JList(listModel);
        listItemNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listItemNames.addListSelectionListener(this);

        JScrollPane listScrollPane = new JScrollPane(listItemNames);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Entities"));
        
        //second display pane
        outputArea = new PListOutputArea();
        newOutputArea = new JTextArea();
        newOutputArea.setEditable(false);
        newOutputArea.setBackground(outputArea.getBackground());
        newOutputArea.setLineWrap(true);
        newOutputArea.setWrapStyleWord(true);
        outputAreaScrollPane = new JScrollPane(outputArea);
        outputAreaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outputAreaScrollPane.setBorder(BorderFactory.createTitledBorder("Details"));
        outputPanel = new InspectPanel();
        outputPanel.setBorder(BorderFactory.createTitledBorder("Details"));
        
        //listScrollPane.setSize(new Dimension(200,230));
        //outputPanel.setSize(new Dimension(400,230));

        bottomSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, outputPanel);
        bottomSplitPane.setOneTouchExpandable(true);
        
        panelForEntities.add(bottomSplitPane);
        
        bottomPanel.add(panelForEntities);
        
        JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, middlePanel, bottomPanel);
        verticalSplitPane.setResizeWeight(0.5);
        add(verticalSplitPane);

        add(statusArea);
        //add(Box.createRigidArea(new Dimension(0, 20)));

        gdb = new GdbProcess(this);
        addedRunParameter();
        //if (exec.executable!="")
        //  startProgram();
        
        notificationListeners = new Vector();
        addNotifyListener(new NotifyGUI());
    }

    public void messageDelivered() {
		CpdListInfo list = cpdLists[listsbox.getSelectedIndex()];
		if (list.list == messageQueue) {
			int forPE=Integer.parseInt((String)pesbox.getSelectedItem());
			populateNewList(listsbox.getSelectedIndex(),forPE, listModel);
		}    		setStatusMessage("Single message delivered");
    }
    
    public void deliverConditional(int idx) {
    	int pe = Integer.parseInt((String)pesbox.getSelectedItem());
    	server.sendCcsRequest("deliverConditional", ""+idx, pe);
    	pes[pe].setConditional();
    	messageDelivered();
    }
    
    public void endConditional(int idx) {
    	int pe = Integer.parseInt((String)pesbox.getSelectedItem());
    	server.sendCcsRequest("endConditional", ""+idx, pe);
    	if (idx==0) pes[pe].setFrozen();
    	messageDelivered();
    }
    
    public void commitConditional(int idx, int numConditional) {
    	int pe = Integer.parseInt((String)pesbox.getSelectedItem());
    	server.sendCcsRequest("commitConditional", ""+(idx+1), pe);
    	if (idx == numConditional-1) pes[pe].setFrozen();
    	messageDelivered();
    }
    
	public void updateRecentConfig() {
		Object []files = preferences.getRecent();
		menuRecent.removeAll();
		//System.out.println("updateRecentConfig: "+files.length);
		if (files.length > 0) {
			menuRecent.setEnabled(true);
			JMenuItem item;
			for (int i=0; i<files.length; ++i) {
				//System.out.println("updateRecentConfig: "+files[i]);
				item = new JMenuItem((String)files[i]);
				listenTo(item, "openRecent", null);
				menuRecent.add(item);
			}
		} else {
			menuRecent.setEnabled(false);
		}
	}
	
	public void loadConfigFile(File filename) {
		try {
			exec = Execution.load(filename);
		} catch (IOException ioe) {
			setStatusMessage("Failed to load configuration file");
			return;
		} catch (ClassNotFoundException cnfe) {
			setStatusMessage("Configuration file corrupted");
			return;
		} catch (SAXException se) {
			setStatusMessage("Configuration file corrupted");
			return;
		}
		loadedRunParameter();
		preferences.addRecent(exec.locationOnDisk);
		updateRecentConfig();
	}

	public void executePython(PythonScript inputPython) {
		Vector[] eps = inputPython.getSelectedEPs();
		for (int i=0; i<eps[0].size(); ++i) System.out.println("Python EP before: "+eps[0].elementAt(i));
		for (int i=0; i<eps[1].size(); ++i) System.out.println("Python EP after: "+eps[1].elementAt(i));
		if (eps[0].size() == 0 && eps[1].size() == 0) {
			PythonExecute code = new PythonExecute(inputPython.getText(), inputPython.getMethod(), new PythonIteratorGroup(inputPython.getChareGroup()), false, true, 0);
			code.setKeepPrint(true);
			code.setWait(true);
			code.setPersistent(true);
			if (interpreterHandle > 0) {
				code.setInterpreter(interpreterHandle);
			}
			System.out.println("Sending python request "+(code.isHighLevel()?" highlevel":"")+
					(code.isKeepPrint()?" keepPrint":"")+(code.isKeepWait()?" keepWait":"")+
					(code.isPersistent()?" persistent":"")+(code.isIterate()?" iterative":""));
			byte[] reply = server.sendCcsRequestBytes("CpdPythonGroup", code.pack(), 0, true);
			if (reply.length == 0) {
				System.out.println("The python module was not linked in the application");
				return;
			}
			interpreterHandle = CcsServer.readInt(reply, 0);
			System.out.println("Python interpreter: "+interpreterHandle);
			PythonFinished finished = new PythonFinished(interpreterHandle, true);
			byte[] finishReply = server.sendCcsRequestBytes("CpdPythonGroup", finished.pack(), 0, true);
			PythonPrint print = new PythonPrint(interpreterHandle, false);
			byte[] output = server.sendCcsRequestBytes("CpdPythonGroup", print.pack(), 0, true);
			System.out.println("Python printed: "+new String(output));
		} else {
			// install the python script for continuous execution
			int[] epIdx = new int[eps[0].size() + eps[1].size()];
			int count = 0;
			for (int i=0; i<eps[0].size(); ++i) epIdx[count++] = - ((EpInfo)eps[0].elementAt(i)).getEpIndex();
			for (int i=0; i<eps[1].size(); ++i) epIdx[count++] = ((EpInfo)eps[1].elementAt(i)).getEpIndex();
			PythonExecute code = new PythonExecute(inputPython.getText(), inputPython.getMethod(), new PythonIteratorPersistent(inputPython.getChareGroup(), eps[0].size()+eps[1].size(), epIdx), true, true, 0);
			code.setKeepPrint(false);
			code.setWait(false);
			byte[] reply = server.sendCcsRequestBytes("CpdPythonPersistent", code.pack(), 0, true);
			if (reply.length == 0) {
				System.out.println("The python module was not linked in the application");
				return;
			}
			// record the information of what is installed, so it can be deleted later
			installedPythonScripts.add(inputPython.getOriginalCode(), eps, inputPython.getChare());
		}
	}

    public void actionPerformed(ActionEvent e) {
    	//int destPE = 0;
    	if (e.getActionCommand().equals("openConf")) 
    	{ /* Bring up file dialog box to select a new configuration */
    		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
    		chooser.addChoosableFileFilter(new CpdFilter());
    		chooser.setAcceptAllFileFilterUsed(false);
    		int returnVal = chooser.showOpenDialog(ParDebug.this);
    		if(returnVal == JFileChooser.APPROVE_OPTION) {
    			loadConfigFile(chooser.getSelectedFile());
    		}
    	}
    	else if (e.getActionCommand().equals("editConf")) 
    	{ /* Bring up parameters dialog box to select run options */
    		ParamsDialog dialogbox = new ParamsDialog(appFrame, true, exec);
    		dialogbox.setLocationRelativeTo(appFrame);
    		//dialogbox.setFields(clparams, ""+numberPes, portnumber, hostname, username, tunnelNeeded);
    		dialogbox.pack();
    		dialogbox.setVisible(true);
    	}
    	else if (e.getActionCommand().equals("saveConf")) {
    		/* Bring up file dialog box to save the current configuration */
    		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
    		chooser.addChoosableFileFilter(new CpdFilter());
    		chooser.setAcceptAllFileFilterUsed(false);
    		int returnVal = chooser.showSaveDialog(ParDebug.this);
    		if(returnVal == JFileChooser.APPROVE_OPTION) {
    			File filename = chooser.getSelectedFile();
    			if (!filename.getName().endsWith(".cpd")) {
    				filename = new File(filename.getAbsolutePath()+".cpd");
    			}
    			if (filename.exists()) {
    				int response;
    				response = JOptionPane.showConfirmDialog(ParDebug.this, "Do you want to overwrite the file?", "File overwrite", JOptionPane.YES_NO_OPTION);
    				if (response != JOptionPane.YES_OPTION) {
    					setStatusMessage("Save aborted");
    					return;
    				}
    			}
    			try {
					exec.save(filename);
    			} catch (IOException ioe) {
    				setStatusMessage("Failed to save configuration file");
    				return;
    			}
    			savedRunParameter(filename.getAbsolutePath());
    			exec.locationOnDisk = filename.getAbsolutePath();
    			preferences.addRecent(exec.locationOnDisk);
    			updateRecentConfig();
    		}
    	}
    	else if (e.getActionCommand().equals("openRecent")) {
    		JMenuItem item = (JMenuItem)e.getSource();
    		String filename = item.getText();
    		System.out.println("Loading configuration file: "+filename);
    		loadConfigFile(new File(filename));
    	}
    	else if (e.getActionCommand().equals("saveWindowSet")) {
    		preferences.location = getParent().getLocationOnScreen();
    		preferences.size = getParent().getSize();
    	}
    	else if (e.getActionCommand().equals("begin")) { /* buttons... */
    		startProgram(false);
    	}
    	else if (e.getActionCommand().equals("attach")) {
    		startProgram(true);
    	} else if (e.getActionCommand().equals("freeze")) {
    		// stop program
    		server.bcastCcsRequest("ccs_debug", "freeze", ((PeSet)peList.getSelectedValue()).runningIterator().toIDs());
    		Iterator iter = ((PeSet)peList.getSelectedValue()).runningIterator();
    		while (iter.hasNext()) {
    			((Processor)iter.next()).setFreezing();
    		}
    		enableButtons();
    		setStatusMessage("Program is frozen on selected pes");
    		peList.repaint();
    	}
    	else if (e.getActionCommand().equals("unfreeze")){ 
    		// start running again
    		command_continue();
			peList.repaint();
    	}
    	else if (e.getActionCommand().equals("step")) {
    		// deliver a single message
    		server.bcastCcsRequest("ccs_single_step", "", ((PeSet)peList.getSelectedValue()).frozenIterator().toIDs());
    		messageDelivered();
    	}
    	else if (e.getActionCommand().equals("disconnect")) {
    		servthread.terminate();
    		servthread.interrupt();
    	}
    	else if (e.getActionCommand().equals("quit")) {
    		server.bcastCcsRequest("ccs_debug_quit", "");
    		servthread.terminate();
    	}
    	else if (e.getActionCommand().equals("startgdb")) 
    	{ 
    		SortedSet set = ((PeSet)peList.getSelectedValue()).getList();
    		//server.bcastCcsRequest("ccs_remove_all_break_points", "", set.iterator());
    		//server.bcastCcsRequest("ccs_debug_startgdb","",1,numberPes,peList);
        	for (int i=0; i<getNumPes(); ++i) {
        		if (set.contains(pes[i])){
        			PList pl = server.getPList("hostinfo", i);
        			PList cur=(PList)pl.elementAt(0);
        			byte[] addr = ((PString)cur.elementNamed("address")).getBytes();
        			int[] address = new int[4];
        			for (int j=0; j<4; ++j) {
        				address[j] = addr[j];
        				if (address[j] < 0) address[j] += 256;
        			}
        			int pid = ((PNative)cur.elementNamed("pid")).getIntValue(0);
        			String sshCommand = null;
        			String[] str = null;
        			String ipAddress = address[0]+"."+address[1]+"."+address[2]+"."+address[3];
        			String script = "ssh "+(exec.sshport>0?("-p "+exec.sshport):"")+ipAddress
        							+" \"cat > /tmp/start_gdb."+pid+" << END_OF_SCRIPT\n"
        							+"shell /bin/rm -f /tmp/start_gdb."+pid+"\n"
        							+"handle SIGWINCH nostop noprint\n"
        							+"handle SIGWAITING nostop noprint\n"
        							+"attach "+pid+"\n"
        							+"END_OF_SCRIPT\n"
        							+"gdb "+new File(getFilename()).getAbsolutePath()
        							+" -x /tmp/start_gdb."+pid+"\"";
        			if (!exec.hostname.equals("localhost") && exec.sshTunnel) { // TODO: This part does not work!!!!
        				sshCommand = "ssh -T "+exec.hostname+" ssh -T";
        				str = new String[9];
        				str[5] = exec.hostname;
        				str[6] = "ssh";
        				//str[8] = "-T";
        				str[7] = ipAddress;
        				str[8] = script;
        			} else { // TODO: Fix when exec.sshport is set!
        				sshCommand = "ssh -T";
        				str = new String[7];
        				//str[5] = ipAddress;
        				str[6] = script;
        			}
        			//String str[] = {"xterm ", "-title", "Node "+i, "-e", "ssh", hostname};
        			str[0] = "xterm";
        			str[1] = "-title";
        			str[2] = "Node "+i;
        			str[3] = "-e";
        			str[4] = "/bin/bash";
        			str[5] = "-c";
        			//str[5] = "-T";
        			for (int k=0;k<str.length;++k) System.out.print(str[k]+" ");
        			System.out.println();
        			try{
        				Runtime.getRuntime().exec(str);
        				//BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(gdb.getOutputStream()));
        				//bw.write("ssh clarity gdb "+new File(filename).getAbsolutePath());
        				//bw.write("attach "+pid);
        				//bw.flush();
        			} catch (Exception e1) {
        				System.err.println(e);
        			}
        		}
        	}
    		setStatusMessage("Gdb started on selected pes");
    	} 
    	else if (e.getActionCommand().equals("breakpoints")) {
    		// set or remove breakpoints

    		//JCheckBox chkbox = (JCheckBox)e.getSource();
    		//String entryPointName = chkbox.getText();
    		EpCheckBox chkbox = (EpCheckBox)e.getSource();
    		command_breakpoint(chkbox);
    	}
    	else if (e.getActionCommand().equals("memory")) {
    		// ask the user for input
    		MemoryDialog input = new MemoryDialog(appFrame, true, getNumPes());
    		if (input.confirmed()) {
    			JFrame frame = new JFrame("Memory visualization");
    			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    			MemoryPanel memory = new MemoryPanel();
    			JComponent newContentPane = memory;
    			newContentPane.setOpaque(true);
    			frame.setContentPane(newContentPane);

    			memory.loadData(input);

    			frame.setTitle("Memory Processor "+input.getPe());
    			frame.setJMenuBar(memory.getMenu());
    			frame.pack();
    			frame.setVisible(true);
    		}
    	}
    	else if (e.getActionCommand().equals("allocationTree")) {
    		JFrame frame = new JFrame("Allocation Tree");
    		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    		AllocationTreePanel at = new AllocationTreePanel();
    		JComponent newContentPane = at;
    		newContentPane.setOpaque(true);
    		frame.setContentPane(newContentPane);
    		frame.setTitle("Allocation Tree");
    		
    		at.loadTree(frame);
    		
    		frame.pack();
    		frame.setVisible(true);
    	}
    	else if (e.getActionCommand().equals("allocationGraph")) {
    	    // ask the user for input
    	    AllocationGraphDialog input = new AllocationGraphDialog(appFrame, true, getNumPes());
    	    if (input.confirmed()) { 	    
    		JFrame frame = new JFrame("Allocation Graph");
    		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    		AllocationGraphPanel at = new AllocationGraphPanel();
    		JComponent newContentPane = at;
    		newContentPane.setOpaque(true);
    		frame.setContentPane(newContentPane);
    		frame.setTitle("Allocation Graph");

    		String executable = new File(getFilename()).getAbsolutePath();
    		String logFile = new File(executable).getParent();
    		if (logFile == null) logFile = ".";
    		logFile += "/memoryLog_";
    		at.load(frame, new MemoryTrace(logFile, getNumPes()), input);

    		frame.setJMenuBar(at.getMenu());
    		frame.pack();
    		frame.setVisible(true);
    	    }
    	}
    	else if (e.getActionCommand().equals("leaksearch") || e.getActionCommand().equals("leakquick")) {
    		String input = JOptionPane.showInputDialog("Processor to scan (-1 for all)");
    		int inputValue;
    		int pe;
    		try {
    			inputValue = Integer.parseInt(input);
    		} catch (NumberFormatException ex) {
    			return;
    		}
    		pe = inputValue;
    		if (pe >= getNumPes()) {
    			JOptionPane.showMessageDialog(this, "There are only "+getNumPes()+" processors.", "Error", JOptionPane.ERROR_MESSAGE);
    			return;
    		}
    		if (inputValue == -1) pe = 0;
    		CcsServer.writeInt(ParDebug.globals, ParDebug.globals.length-8, e.getActionCommand().equals("leakquick") ? 1 : 0);
    		CcsServer.writeInt(ParDebug.globals, ParDebug.globals.length-4, inputValue);
    		debugger.server.sendCcsRequestBytes("converse_memory_leak", ParDebug.globals, pe, true);

    		/*
    		if (inputValue == -1) frame.setTitle("Combined Allocation Tree");
    		else frame.setTitle("Allocation Tree Processor "+input);
    		if (inputValue == -1) inputValue = 0; /* Send request to 0 */
    		
    	}
    	else if (e.getActionCommand().equals("memorymark") || e.getActionCommand().equals("memoryunmark")) {
    		String input = JOptionPane.showInputDialog("Processor to mark (-1 for all)");
    		int pe;
    		try {
    			pe = Integer.parseInt(input);
    		} catch (NumberFormatException ex) {
    			return;
    		}
    		if (pe >= getNumPes()) {
    			JOptionPane.showMessageDialog(this, "There are only "+getNumPes()+" processors.", "Error", JOptionPane.ERROR_MESSAGE);
    			return;
    		}
    		if (pe == -1) pe = 0;
    		byte[] data = new byte[1];
    		data[0] = (e.getActionCommand().equals("memorymark") ? (byte)1 : 0);
    		debugger.server.sendCcsRequestBytes("converse_memory_mark", data, pe, false);
    	}
    	else if (e.getActionCommand().equals("memstat")) {
    		String input = JOptionPane.showInputDialog("Processor to load (-1 for all)");
    		int pe;
    		try {
    			pe = Integer.parseInt(input);
    		} catch (NumberFormatException ex) {
    			return;
    		}
    		if (pe >= getNumPes()) {
    			JOptionPane.showMessageDialog(this, "There are only "+getNumPes()+" processors.", "Error", JOptionPane.ERROR_MESSAGE);
    			return;
    		}
    		if (pe == -1) pe = 0;
    		byte[] buf = debugger.server.sendCcsRequestBytes("ccs_debug_memStat", input, pe);
    		PConsumer cons=new PConsumer();
    		cons.decode(buf);
    		PList stat = cons.getList();
    		System.out.println(stat);
    		MemStat m = new MemStat();
    		m.load(stat);
    		System.out.println(m.toString());
    		JOptionPane.showMessageDialog(this, m.display(), "Memory Statistics", JOptionPane.INFORMATION_MESSAGE);
    	}
    	else if (e.getActionCommand().equals("python")) {
    		new PythonDialog(this, false, groupItems, chareItems, gdb, server);
    		/*
			//JDialog frame = new JDialog(appFrame, "Python script", true);
			//frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    		//if (inputPython == null) {
    		PythonDialog inputPython = new PythonDialog(appFrame, false, groupItems, chareItems, gdb, server);
    		//} else {
    		//	JOptionPane.showMessageDialog(this, "There is already a python instance active", "Error", JOptionPane.ERROR_MESSAGE);
    		//}
			//PythonDialog input = new PythonDialog(appFrame, false, groupItems, chareItems, gdb, server);
			if (inputPython.confirmed()) {
				PythonExecute code = new PythonExecute(inputPython.getText(), inputPython.getMethod(), new PythonIteratorGroup(inputPython.getChareGroup()), false, true, 0);
				code.setKeepPrint(true);
				code.setWait(true);
				code.setPersistent(true);
				if (interpreterHandle > 0) {
					code.setInterpreter(interpreterHandle);
				}
				System.out.println("Sending python request "+(code.isHighLevel()?" highlevel":"")+
								   (code.isKeepPrint()?" keepPrint":"")+(code.isKeepWait()?" keepWait":"")+
								   (code.isPersistent()?" persistent":"")+(code.isIterate()?" iterative":""));
				byte[] reply = server.sendCcsRequestBytes("CpdPythonGroup", code.pack(), 0, true);
				if (reply.length == 0) {
					System.out.println("The python module was not linked in the application");
					return;
				}
				interpreterHandle = CcsServer.readInt(reply, 0);
				System.out.println("Python interpreter: "+interpreterHandle);
				PythonPrint print = new PythonPrint(interpreterHandle, true);
				byte[] output = server.sendCcsRequestBytes("CpdPythonGroup", print.pack(), 0, true);
				System.out.println("Python printed: "+new String(output));
				inputPython = null;
			}
			inputPython = null;
			*/
    	} else if (e.getActionCommand().equals("pythoninstalled")) {
    		installedPythonScripts.setVisible(true);
    	}
    	else if (e.getActionCommand().equals("newPeSet")) {
    		PeSetDialog input = new PeSetDialog(appFrame, true, getNumPes());
    		if (input.confirmed()) {
    			Iterator iter = input.getPes().iterator();
    			if (iter.hasNext()) {
	    			TreeSet set = new TreeSet();
	    			while (iter.hasNext()) set.add(pes[((Integer)iter.next()).intValue()]);
	    			PeSet newset = new PeSet(input.getName(), set);
	    			peListModel.addElement(newset);
	    			peList.setSelectedValue(newset, true);
	    			/*
	    			System.out.println("Name: "+input.getName());
	    			Collection l = input.getPes();
	    			if (l != null) {
	    				Iterator iter = l.iterator();
	    				while (iter.hasNext()) {
	    					System.out.println(iter.next());
	    				}
	    			}
	    			*/
	    			}
    		}
    	}
    	else if (e.getActionCommand().equals("deletePeSet")) {
    		int idx = peList.getSelectedIndex();
    		if (idx > 0) peListModel.remove(idx);
    	}
    	else if (e.getActionCommand().equals("peSetDetails")) {
    		PeSet set = (PeSet)peList.getSelectedValue();
    		String bufTitle = new String("Details for set \""+set.getName()+"\"");
    		StringBuffer buf = new StringBuffer();
    		Iterator iter = set.getList().iterator();
    		while (iter.hasNext()) buf.append(" "+iter.next());
    		System.out.println(bufTitle+": {"+buf.toString()+" }");
    		JOptionPane.showMessageDialog(this, buf.toString(), bufTitle, JOptionPane.INFORMATION_MESSAGE);
    	}
    	/*
    	else if (e.getActionCommand().equals("pecheck")) 
    	{ // Checked or unchecked a PE box
    		JCheckBox chkbox = (JCheckBox)e.getSource();
    		String peText = chkbox.getText();
    		String peno = peText.substring(3);
    		if (chkbox.isSelected()) 
    			peList[Integer.parseInt(peno)] = true;
    		else
    			peList[Integer.parseInt(peno)] = false;
    	}
    	*/
    	else if (e.getActionCommand().equals("lists") 
    			|| e.getActionCommand().equals("changepe")) 
    	{ /* Clicked on list or pe drop-down */
    		System.out.println("Starting generation new list "+(new Date()).getTime());
    		if (pesbox.getSelectedItem()!=null) {
    			int forPE=Integer.parseInt((String)pesbox.getSelectedItem());
    			populateNewList(listsbox.getSelectedIndex(),forPE, listModel); 
    		}
    		System.out.println("Ended generation new list "+(new Date()).getTime());
    	}
    	else if (e.getActionCommand().equals("exitDebugger")) {
    		if (isRunning) {
    			server.bcastCcsRequest("ccs_debug_quit", "");
    			quitProgram();
    		}
            preferences.save();
    		System.exit(0);
    	}
    } // end of actionPerformed

    public void command_continue() {
		server.bcastCcsRequest("ccs_continue_break_point", "", ((PeSet)peList.getSelectedValue()).frozenIterator().toIDs());
		Iterator iter = ((PeSet)peList.getSelectedValue()).frozenIterator();
		while (iter.hasNext()) {
			((Processor)iter.next()).setRunning();
		}
		enableButtons();
		setStatusMessage("Program is running on selected pes");
    }
    
    public void command_breakpoint(int epIdx) {
    	EpCheckBox chkbox = epItems.getEntryFor(epIdx).getCheckBox();
    	chkbox.doClick();
    	//command_breakpoint(chkbox);
    }
    public void command_breakpoint(EpCheckBox chkbox) {
		int breakpointIndex = chkbox.ep.getEpIndex();
		String entryPointName = ""+breakpointIndex;
		if (((PeSet)peList.getSelectedValue()).isSomeConditional()) {
			chkbox.setSelected(! chkbox.isSelected());
			JOptionPane.showMessageDialog(this, "Cannot set breakpoints while in conditional mode!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (chkbox.isSelected())
		{
			chkbox.ep.addBP(((PeSet)peList.getSelectedValue()).getList());
			byte []reply = server.bcastCcsRequest("ccs_set_break_point", entryPointName,((PeSet)peList.getSelectedValue()).toIDsArray());
			//stepButton.setEnabled(true);
			//continueButton.setEnabled(true);
			//freezeButton.setEnabled(false);
			if (reply[0] != 0) setStatusMessage ("Break Point set at entry point " +entryPointName);
			else JOptionPane.showMessageDialog(this, "Could not set breakpoint!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			chkbox.ep.removeBP(((PeSet)peList.getSelectedValue()).getList());
			byte []reply = server.bcastCcsRequest("ccs_remove_break_point", entryPointName,((PeSet)peList.getSelectedValue()).toIDsArray());
			//stepButton.setEnabled(true);
			//continueButton.setEnabled(true);   
			//freezeButton.setEnabled(true);
			if (reply[0] != 0) setStatusMessage ("Break Point removed at entry point " +entryPointName+" on selected Pes");
			else JOptionPane.showMessageDialog(this, "Could not remove breakpoint!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		PeSet set = (PeSet)peList.getSelectedValue();
		if (set != null) chkbox.setCoverageColor(set.getList());
		CpdListInfo list = cpdLists[listsbox.getSelectedIndex()];
		if (list.list == messageQueue) {
			int forPE=Integer.parseInt((String)pesbox.getSelectedItem());
			populateNewList(listsbox.getSelectedIndex(),forPE, listModel);
		}
		System.out.println("breakpoint triggered");
    }
    
    public void valueChanged(ListSelectionEvent e) {
      
      if(e.getValueIsAdjusting()) return;
      
      System.out.println("Starting generation new detailed information "+(new Date()).getTime());
      JList theList = (JList)e.getSource();
      if (theList == listItemNames && !theList.isSelectionEmpty())
      {
        int forPE=Integer.parseInt((String)pesbox.getSelectedItem());
        expandListElement(listsbox.getSelectedIndex(),forPE,theList.getSelectedIndex());
      }
      System.out.println("Ended generation new detailed information "+(new Date()).getTime());
  
    } // end of valueChanged

    private void enableButtons() {
    	PeSet set = (PeSet)peList.getSelectedValue();
		stepButton.setEnabled(true);
		continueButton.setEnabled(true);
		menuActionContinue.setEnabled(true);
		freezeButton.setEnabled(true);
		menuActionFreeze.setEnabled(true);
    	if (set.isAllFrozen()) {
    		freezeButton.setEnabled(false);
    		menuActionFreeze.setEnabled(false);
    	}
    	if (set.isAllRunning() || set.isSomeDead()) {
    		stepButton.setEnabled(false);
    		continueButton.setEnabled(false);
    		menuActionContinue.setEnabled(false);
    	}
    }
    
    private class PeSetListener implements ListSelectionListener {

    	public void valueChanged(ListSelectionEvent e) {
    		PeSet set = (PeSet)peList.getSelectedValue();
    		if (set != null) {
    			enableButtons();
    			SortedSet actualSet = set.getList();
    			Vector data = epItems.getSystemEps();
    			for (int i=0; i<data.size(); ++i) ((EpInfo)data.elementAt(i)).getCheckBox().setCoverageColor(actualSet);
    			data = epItems.getUserEps();
    			for (int i=0; i<data.size(); ++i) ((EpInfo)data.elementAt(i)).getCheckBox().setCoverageColor(actualSet);
    			entryScrollPane.repaint();
    		}
    	}
    }
    
/*************** Program Control ***********************/
    /// Start the program from scratch.
    public void startProgram(boolean attachMode) {
    	gdb.terminate();
    	isRunning = true;
        interpreterHandle = 0;
        installedPythonScripts = new PythonInstalledCode(false);
    	programOutputArea.setText("");
    	String executable = new File(getFilename()).getAbsolutePath();
    	String charmrunDir = new File(executable).getParent();
    	if (charmrunDir==null) charmrunDir=".";
    	String charmrunPath = charmrunDir + "/charmrun";
    	if (envDisplay.length() == 0) envDisplay = getEnvDisplay();
    	// System.out.println(envDisplay);

    	String totCommandLine = charmrunPath + " " + "+p"+ exec.npes + " " +executable + " " + exec.parameters+"  +cpd +DebugDisplay " +envDisplay+" ++server";// ++charmdebug";
    	if (!exec.doNotSuspend) totCommandLine += " +DebugSuspend";
    	if (exec.virtualDebug) {
    		totCommandLine += " +bgnetwork dummy +LBPeriod 1000000 +x1 +y1 +z";
    		if (!exec.recplayActive || !exec.recplayDetailActive || !exec.replayDetail) totCommandLine += exec.virtualNpes;
    		else totCommandLine += "1";
    	}
    	if (exec.recplayActive) {
    		if (exec.record) totCommandLine += " +record";
    		else {
    			totCommandLine += " +replay";
    			if (exec.recplayDetailActive) {
    				if (exec.recordDetail) totCommandLine += " +record-detail "+exec.selectedPes;
    				else totCommandLine += " +replay-detail "+exec.selectedPes+"/"+exec.npes;
    			}
    		}
    		switch (exec.recplayChecksum) {
    		case Execution.CHECKSUM_XOR:
    			totCommandLine += " +recplay-xor";
    			break;
    		case Execution.CHECKSUM_CRC:
    			totCommandLine += " +recplay-crc";
    			break;
    		default:
    		}
    	}
    	if (exec.port.length() != 0)
    		totCommandLine += " ++server-port " + exec.port;
    	if (!exec.hostname.equals("localhost") || exec.sshport != 0) {
        	if (exec.workingDir != null && exec.workingDir.length() != 0) {
        		totCommandLine = "cd "+exec.workingDir+"; "+totCommandLine;
        	}
    		if (exec.username.length()>0) {
    			totCommandLine = "-l " + exec.username + " " + totCommandLine;
    		}
    		if (exec.sshport>0) {
    			totCommandLine = "-p " + exec.sshport + " " + totCommandLine;
    		}
    		totCommandLine = "ssh " + exec.hostname + " " + totCommandLine;
    	}
    	System.out.println("ParDebug> "+totCommandLine);
		Process p = null;
		Runtime runtime = null;
		runtime = Runtime.getRuntime();
    	if (! attachMode) {
    		programOutputArea.setText(totCommandLine);
    		try {
    			// start new process for charmrun
    			File workDir = null;
    			if (exec.workingDir != null && exec.workingDir.length() != 0) {
    				workDir = new File(exec.workingDir);
    			}
    			p = runtime.exec(totCommandLine, null, workDir);
    			//BufferedReader prerr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
    			//BufferedReader prout = new BufferedReader(new InputStreamReader(p.getInputStream()));
    			//System.out.println("Start reading p");
    			//System.out.println("1|"+prout.readLine()+"|");
    			//System.out.println("2|"+prerr.readLine()+"|");
    			//System.out.println("Finished reading p");
    		}
    		catch (Exception exc) {
    			System.out.println("ParDebug> Error executing "+totCommandLine);
    			quitProgram();
    			return;
    		}

    		// start the thread that will handle the communication with charmrun
    		// (and the program output as a consequence)
    		servthread = (new ServThread.Charmrun(this, p));
    		servthread.start();
    		while (servthread.getFlag() == 0);
    		if (servthread.getFlag() != 1) {
    			setStatusMessage("Failed to start program");
    			return;
    		}
    	} else {
    		programOutputArea.setText("Attaching to running program\n");
    		if (! exec.inputFile.equals("")) servthread = (new ServThread.File(this, new File(exec.inputFile), exec.waitFile));
    		else servthread = (new ServThread.CCS(this, exec));
    		servthread.start();
    	}
    	try {
    		/* Wait until the "ccs:" line comes out of the program's stdout */
    		long iter = 0;
    		if (! attachMode || ! exec.inputFile.equals("")) {
    			while (servthread.portno == null)
    			{
    				if (! exec.waitFile) {
    					try { Thread.sleep(100); }
    					catch(InterruptedException e1)
    					{ /* don't care about interrupted sleep */ }
    					if(iter++ > 60*10) abort("Timeout waiting for program to start up (and print its CCS port number)");
    				}
    				else {
    					/* We are attaching to a file, which might take a while to appear */
    					try { Thread.sleep(5000); }
    					catch(InterruptedException e1) { }
    				}
    			}
    		}
    		
    		if (attachMode && exec.inputFile.equals("") && (exec.port.length() == 0 || exec.ccshost.equals("localhost"))) {
    			JOptionPane.showMessageDialog(this, "Hostname and port number must be specified in attach mode", "Error", JOptionPane.ERROR_MESSAGE);
    			quitProgram();
    			return;
    		}
    		if (exec.port.length() == 0) portnumber = servthread.portno;
    		else portnumber = exec.port;
    		if (exec.hostname.equals("localhost")) hostnumber = servthread.hostName;
    		System.out.println("ParDebug> Charmrun started (CCS IP "+(exec.hostname.equals("localhost")?hostnumber:exec.hostname)+", port "+portnumber+")");

    		/* Connect to the new program */
    		String[] ccsArgs=new String[2];
    		ccsArgs[0]= exec.hostname.equals("localhost")?hostnumber:exec.hostname;
    		ccsArgs[1]= portnumber;
    		if (exec.sshTunnel) {
    			System.out.println("ParDebug> Tunneling connection through ssh");
    			try {
    				String str = "ssh"+(exec.sshport>0?(" -p "+exec.sshport):"")+" -2 -c blowfish -L "
    							 +portnumber+":localhost:"+portnumber+" "+exec.hostname;
    				System.out.println("> "+str);
    				sshTunnel = runtime.exec(str);
    			} catch (Exception exc) {
    				System.out.println("ParDebug> Could not create ssh tunnel");
    			}
    			try { Thread.sleep(5000); }
    			catch(InterruptedException e1) {}
    			ccsArgs[0] = "localhost";
    		}
    		System.out.println("Connecting to: "+exec.username+(exec.username.length()>0?"@":"")+(exec.hostname.equals("localhost")?hostnumber:exec.hostname)+":"+portnumber);
    		CcsServer ccs = CcsServer.create(ccsArgs,false);
    		server = new CpdUtil(ccs);

    		/* Reset the type information stored */
    		Inspector.initialize(server);

    		// Retrieve the initial info from charmrun regarding the program segments
    		//StringBuffer initialInfoBuf = new StringBuffer();
    		boolean success = getInitialInfo(); //servthread.infoCommand(" ");
    		if (!success) {
    			JOptionPane.showMessageDialog(this, "Could not gather information from executable", "Error", JOptionPane.ERROR_MESSAGE);
    			quitProgram();
    			return;
    		}
    		//while ((initialInfo = servthread.infoCommand(" ")).indexOf("\n(gdb)") == -1) {
    		//    initialInfoBuf.append(initialInfo);
    		//    System.out.println("++|"+initialInfo+"|");
    		//}
    		//System.out.println("|"+initialInfo+"|");
    		//initialInfoBuf.append(initialInfo);
    		//initialInfo = initialInfoBuf.toString();
    		//System.out.println("|"+initialInfo+"|");
    		// Delete the first print made by gdb at startup
    		//System.out.println(servthread.infoCommand(" "));

    		/* Create the pe list */
    		//peList = new boolean[exec.npes];
    		if (attachMode) {
    			if (exec.virtualDebug) JOptionPane.showInternalMessageDialog(this, "Warning, attach mode does not yet support virtual debugging!");
    			Date start = new Date();
    			byte[] stat = server.bcastCcsRequest("ccs_debug", "status");
    			System.out.println("status: received "+stat.length+" bytes");
    			exec.virtualDebug = Inspector.isEmulated();
    			exec.npes = stat.length / 8;
    			exec.virtualNpes = exec.npes;
    			pes = new Processor[exec.npes];
        		for (int i = 0; i < exec.npes; i++) {
        			String peNumber = (new Integer(i)).toString();
        			pesbox.addItem( peNumber );
        			pes[i] = new Processor(i);
        		}
    			IntBuffer b = ByteBuffer.wrap(stat).asIntBuffer();
    			for (int i=0; i<exec.npes; ++i) {
    				if (b.get(i*2+1) == 1) {
    					pes[b.get(i*2)].setRunning();
    				}
    			}
    			System.out.println("Single bcast connection: "+((new Date()).getTime()-start.getTime()));
    		} else {
    			if (exec.recplayActive && exec.recplayDetailActive && exec.replayDetail) {
    				pes = new Processor[1];
    				pesbox.addItem(exec.selectedPes);
    				pes[0] = new Processor(Integer.parseInt(exec.selectedPes));
    			} else {
    				pes = new Processor[getNumPes()];
    				for (int i = 0; i < getNumPes(); i++) {
    					String peNumber = (new Integer(i)).toString();
    					pesbox.addItem( peNumber );
    					pes[i] = new Processor(i);
    				}
    			}
    		}
    		if (attachMode) {
    			Processor fakePE[] = new Processor[exec.npes];
    			Date start = new Date();
    			// gather the status of the processes
    			byte[][] status = server.bcastCcsRequest("ccs_debug", "status", exec.npes);
    			for (int i=0; i<exec.npes; ++i) {
        			IntBuffer buf = ByteBuffer.wrap(status[i]).asIntBuffer();
    				if (buf.get(1) == 1) {
    					fakePE[buf.get(0)].setRunning();
    				}
    			}
    			System.out.println("Multiple ptp connection: "+((new Date()).getTime()-start.getTime()));
    		}
			PeSet all = new PeSet("all", pes);
			peListModel.addElement(all);
			peList.setSelectedIndex(0);

    		peList.updateUI();

    		startButton.setEnabled(false);
    		menuActionStart.setEnabled(false);
    		menuActionAttach.setEnabled(false);
			stepButton.setEnabled(true);
    		continueButton.setEnabled(true);
    		menuActionContinue.setEnabled(true);
    		quitButton.setEnabled(false);
    		freezeButton.setEnabled(false);
    		startGdbButton.setEnabled(true); 
    		menuActionDisconnect.setEnabled(true);
    		menuActionPython.setEnabled(true);
    		menuActionPythonInstalled.setEnabled(true);
    		menuMemoryView.setEnabled(true);
    		menuMemoryAllocationTree.setEnabled(true);
    		menuMemoryLeak.setEnabled(true);
    		menuMemoryQuickLeak.setEnabled(true);
    		menuMemoryMark.setEnabled(true);
    		menuMemoryUnmark.setEnabled(true);
    		menuMemoryStatistics.setEnabled(true);
    		enableButtons();
    		
    		int nItems;

    		/* Load the information regarding all chares */
    		nItems = server.getListLength("charm/chares",0);
    		chareItems.load(server.getPList("charm/chares",0,0,nItems));

    		/* Set up the lookup information for the Entry Methods */
    		epItems.setLookups(chareItems);

    		/* Create the entities lists */
    		nItems=server.getListLength("charm/entries",0);
    		epItems.load(server.getPList("charm/entries",0,0,nItems));

    		DefaultMutableTreeNode[] chareRoots = new DefaultMutableTreeNode[chareItems.size()];
    		System.out.println("allocated "+chareItems.size()+" chareRoots");
    		Vector items;
    		items = epItems.getUserEps();
    		int l = items.size();
    		int i = 0;
    		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    		DefaultMutableTreeNode userRoot = new DefaultMutableTreeNode("User Entries");
    		for (i=0; i < l; ++i)
    		{
    			//String tmp = (items.elementAt(i)).toString();
    			//JCheckBox chkbox = new JCheckBox(tmp);
    			EpCheckBox chkbox = new EpCheckBox((EpInfo)items.elementAt(i));
    			chkbox.addActionListener(this);
    			chkbox.setActionCommand("breakpoints"); 
    			//userEpsActualPanel.add(chkbox);
    			//userRoot.add(new EpTreeCheckBox(chkbox));
    			int chareType =((EpInfo)items.elementAt(i)).getChareType();
    			if (chareRoots[chareType] == null) {
    				chareRoots[chareType] = new DefaultMutableTreeNode(chareItems.elementAt(chareType).getType());
    				userRoot.add(chareRoots[chareType]);
    			}
    			chareRoots[chareType].add(new EpTreeCheckBox(chkbox));
    		}
    		//userEpsActualPanel.add(treeBP);
    		//userEpsActualPanel.updateUI();
    		
    		items = epItems.getSystemEps();
    		l = items.size();
    		DefaultMutableTreeNode sysRoot = new DefaultMutableTreeNode("System Entries");
    		for (i=0; i < l; ++i)
    		{
    			//String tmp = (items.elementAt(i)).toString();
    			//JCheckBox chkbox = new JCheckBox(tmp);
    			EpCheckBox chkbox = new EpCheckBox((EpInfo)items.elementAt(i));
    			chkbox.addActionListener(this);
    			chkbox.setActionCommand("breakpoints"); 
    			//sysEpsActualPanel.add(chkbox);
    			//sysRoot.add(new EpTreeCheckBox(chkbox));
    			int chareType =((EpInfo)items.elementAt(i)).getChareType(); 
    			if (chareRoots[chareType] == null) {
    				chareRoots[chareType] = new DefaultMutableTreeNode(chareItems.elementAt(chareType).getType());
    				sysRoot.add(chareRoots[chareType]);
    			}
    			chareRoots[chareType].add(new EpTreeCheckBox(chkbox));
    		}
    		root.add(sysRoot);
    		root.add(userRoot);
    		
    		JTree treeBP = new JTree(root);
    		treeBP.setRootVisible(false);
    		treeBP.collapseRow(0);
    		treeBP.expandRow(1);
    		treeBP.addMouseListener(new EpTreeListener(treeBP));
    		treeBP.setCellRenderer(new EpTreeRenderer());
    		treeBP.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    		entryScrollPane.setViewportView(treeBP);

    		/* Load the information regarding all messages */
    		nItems = server.getListLength("charm/messages",0);
    		msgItems.load(server.getPList("charm/messages",0,0,nItems));

    		/* Set the lookup lists for the message queue inspector */
    		messageQueue.setLookups(epItems, msgItems, chareItems);

			/* Load the groups the have been registered */
			nItems = server.getListLength("charm/objectNames",0);
			groupItems.load(server.getPList("charm/objectNames",0,0,nItems));

    	} catch (Exception e) {
    		System.out.println("Error while starting the application (error: "+e+". Aborting...");
			e.printStackTrace();
    		if (! attachMode) p.destroy();
    	}

    	//sysEpsActualPanel.updateUI();
    	listsbox.setEnabled(true);
    	pesbox.setEnabled(true);  
    	quitButton.setEnabled(true);
    	listsbox.setSelectedIndex(3);
    }

    /// Exit the debugged program.
    public void quitProgram()
    {
    	gdb.terminate();
    	//exec.port = "";
    	isRunning = false;
    	if (sshTunnel != null) {
    		try { Thread.sleep(2);
    		} catch (InterruptedException e) {}
    		sshTunnel.destroy();
    		sshTunnel = null;
    	}
    	startButton.setEnabled(true);
		stepButton.setEnabled(false);
    	continueButton.setEnabled(false); 
    	quitButton.setEnabled(false);
    	freezeButton.setEnabled(false);
    	startGdbButton.setEnabled(false);
    	listModel.removeAllElements();
    	outputArea.setList(null);
        newOutputArea.setText("");
    	listsbox.setEnabled(false);
    	pesbox.removeAllItems(); 
    	pesbox.setEnabled(false);
    	menuActionStart.setEnabled(true);
    	menuActionAttach.setEnabled(true);
    	menuActionContinue.setEnabled(false);
    	menuActionFreeze.setEnabled(false);
    	menuActionDisconnect.setEnabled(false);
    	menuActionQuit.setEnabled(false);
    	menuActionPython.setEnabled(false);
    	menuActionPythonInstalled.setEnabled(false);
    	menuMemoryView.setEnabled(false);
    	menuMemoryAllocationTree.setEnabled(false);
    	menuMemoryLeak.setEnabled(false);
    	menuMemoryQuickLeak.setEnabled(false);
    	menuMemoryMark.setEnabled(false);
    	menuMemoryUnmark.setEnabled(false);
    	menuMemoryStatistics.setEnabled(false);

    	peList.removeAll();
    	peListModel.removeAllElements();
    	peList.updateUI();
    	//userEpsActualPanel.removeAll(); 
    	//userEpsActualPanel.updateUI(); 
    	//sysEpsActualPanel.removeAll(); 
    	//sysEpsActualPanel.updateUI();
        if (installedPythonScripts != null) installedPythonScripts.dispose();
    	entryScrollPane.setViewportView(new JLabel());
    	setStatusMessage(new String("Ready to start new program"));
    }
    
    private boolean getInitialInfo() {
		String executable = new File(getFilename()).getAbsolutePath();
		String totCommandLine = "size -A " + executable;
		System.out.println(totCommandLine);
		String hostname = getHostname();
		String initialInfo;
		String commandLinePrefix = "";
		if (!hostname.equals("localhost")) {
			commandLinePrefix = hostname + " ";
			String username = getUsername();
			if (!username.equals("")) {
				commandLinePrefix = "-l " + username + " " + commandLinePrefix;
			}
			commandLinePrefix = "ssh " + commandLinePrefix;
		}
		try {
			//System.out.println("Starting: '"+totCommandLine+"'");
			Process p = Runtime.getRuntime().exec(commandLinePrefix+totCommandLine);
			BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuffer reply = new StringBuffer();
			int c;
			while ((c = output.read()) != -1) {
				reply.append((char)c);
			}
			//System.out.println("STRINGA: "+reply.toString());
			//return reply.toString();
			initialInfo = reply.toString();
		} catch (Exception e) {
			System.out.println("Failed to start gdb info program");
			return false;
		}

		boolean success;
		int dataSize=0,bssPos=0,bssSize=0; 
		try {
			int dataInitial = initialInfo.indexOf("\n.data ");
			int dataFinal = initialInfo.indexOf("\n",dataInitial+1);
			String dataValues = initialInfo.substring(dataInitial+6,dataFinal).trim();
			int endSize = dataValues.indexOf(' ');
			int startPos = dataValues.lastIndexOf(' ');
			dataSize = Integer.parseInt(dataValues.substring(0,endSize));
			dataPos = Integer.parseInt(dataValues.substring(startPos+1));
			System.out.println("string1: |"+initialInfo.substring(dataInitial+6,dataFinal).trim()+"| "+dataSize+" "+dataPos);
			int bssInitial = initialInfo.indexOf("\n.bss");
			int bssFinal = initialInfo.indexOf("\n",bssInitial+1);
			String bssValues = initialInfo.substring(bssInitial+6,bssFinal).trim();
			endSize = bssValues.indexOf(' ');
			startPos = bssValues.lastIndexOf(' ');
			bssSize = Integer.parseInt(bssValues.substring(0,endSize));
			bssPos = Integer.parseInt(bssValues.substring(startPos+1));
			System.out.println("string1: |"+initialInfo.substring(bssInitial+5,bssFinal).trim()+"| "+bssSize+" "+bssPos);
			success = true;
		} catch (Exception e) {
			System.out.println("Failed to read ELF format");
			success = false;
		}
		
		if (!success) {
			// could not read "size -A" output, try XCOFF "dump -h"
			totCommandLine = "dump -h -Xany " + executable;
			try {
				//System.out.println("Starting: '"+totCommandLine+"'");
				Process p = Runtime.getRuntime().exec(commandLinePrefix+totCommandLine);
				BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
				StringBuffer reply = new StringBuffer();
				int c;
				while ((c = output.read()) != -1) {
					reply.append((char)c);
				}
				//System.out.println("STRINGA: "+reply.toString());
				//return reply.toString();
				initialInfo = reply.toString();
			} catch (Exception e) {
				System.out.println("Failed to start gdb info program");
				return false;
			}
			
			try {
				int dataInitial = initialInfo.indexOf("Section Header for .data");
				dataInitial = initialInfo.indexOf("0x",dataInitial+1);
				int dataFinal = initialInfo.indexOf(' ',dataInitial+1);
				dataPos = (int)Long.parseLong(initialInfo.substring(dataInitial+2,dataFinal), 16);
				dataInitial = initialInfo.indexOf("0x",dataInitial+1);
				dataInitial = initialInfo.indexOf("0x",dataInitial+1);
				dataInitial = initialInfo.indexOf("0x",dataInitial+1);
				dataFinal = initialInfo.indexOf(' ',dataInitial+1);
				dataSize = Integer.parseInt(initialInfo.substring(dataInitial+2, dataFinal), 16);
				System.out.println("string1: "+dataSize+" "+dataPos);
				
				int bssInitial = initialInfo.indexOf("Section Header for .bss");
				bssInitial = initialInfo.indexOf("0x",bssInitial+1);
				int bssFinal = initialInfo.indexOf(' ',bssInitial+1);
				bssPos = (int)Long.parseLong(initialInfo.substring(bssInitial+2,bssFinal), 16);
				bssInitial = initialInfo.indexOf("0x",bssInitial+1);
				bssInitial = initialInfo.indexOf("0x",bssInitial+1);
				bssInitial = initialInfo.indexOf("0x",bssInitial+1);
				bssFinal = initialInfo.indexOf(' ',bssInitial+1);
				bssSize = Integer.parseInt(initialInfo.substring(bssInitial+2, bssFinal), 16);
				System.out.println("string1: "+bssSize+" "+bssPos);
				success = true;
			} catch (Exception e) {
				System.out.println("Failed to read XCOFF format");
				success = false;
			}
		}
	
		if (!success) return false;
		// FIXME: here we assume the program is 32 bit, or if 64 bit all the addresses are small
		
		// HACK!
		if (Inspector.is64bit()) {
			globals = new byte[40]; // 4 pointers of 8 bytes each + 8 extra bytes
   			CcsServer.writeLong(globals, 0, dataPos);
			CcsServer.writeLong(globals, 8, dataPos+dataSize);
			CcsServer.writeLong(globals, 16, bssPos);
			CcsServer.writeLong(globals, 24, bssPos+bssSize);
		} else {
			globals = new byte[24]; // 4 pointers of 4 bytes each + 8 extra bytes
			CcsServer.writeInt(globals, 0, dataPos);
			CcsServer.writeInt(globals, 4, dataPos+dataSize);
			CcsServer.writeInt(globals, 8, bssPos);
			CcsServer.writeInt(globals, 12, bssPos+bssSize);
		}
		return true;
    }
    
    public static void printUsage()
    {
        System.out.println("Usage: charmdebug [options] [<executable> [<arguments>] ]");
        System.out.println("  -host <name>        remote host where to start the application");
        System.out.println("  -user <name>        username to use for ssh");
        System.out.println("  -sshport <num>      port to use for ssh");
        System.out.println("  -port <num>         use specific CCS port");
        System.out.println("  -pes <num> (or +p<num>)");
        System.out.println("                      number of processors to use");
        System.out.println("  -dir <dir>          specify working directory");
        System.out.println("  -outputfile <file>  file where to read program output (for attach)");
        System.out.println("  -waitfile           wait for output file to be created (for attach)");
        System.out.println("  -sshtunnel          tunnel CCS requests though ssh");
        System.out.println("  -config <file>      read configuration parameters from file");
        System.out.println("  -commands <file>    execute commands read from file at startup");
        //System.out.println("  -host    ");
        //System.out.println("  -host    ");
        //		" [[-file <charm program name>] [[-param \"<charm program parameters>\"][-pes <number of pes>]] [-host <hostname>] " +
        //		"[-user <username>] [-port <port>] [-sshtunnel] [-display <display>]]");
    }
    
    String getFilename() {
    	if (exec.executable.charAt(0) == '/') return exec.executable;
    	return exec.workingDir+((exec.workingDir.equals("")||exec.workingDir.endsWith("/"))?"":"/")+exec.executable;
    }
    String getHostname() { return exec.hostname; }
    String getUsername() { return exec.username; }
    int getSshPort() { return exec.sshport; }
    EpPList getEpItems() { return (EpPList)epItems.clone(); }
    CharePList getGroupItems() { return groupItems; }
    Execution getExecution() { return exec; }
    
    public static ParDebug debugger;
    public static void main(String[] args) {
    	Execution exec = new Execution();
    	exec.hostname = "localhost";
    	exec.username = "";
    	exec.executable = "";
    	exec.workingDir = System.getProperty("user.dir");
    	exec.port = "";
    	exec.ccshost = "";
    	exec.npes = 1;
    	String numberPesString="1";
    	String sshportString = null;
    	exec.parameters = "";
    	exec.inputFile = "";
    	exec.waitFile = false;
    	envDisplay = "";
    	exec.sshTunnel = false;
    	sshTunnel = null;
    	boolean noWindow = false;
    	File commands = null;

    	// parsing command-line parameters
    	int i = 0;
    	boolean gotFilename=false;
    	while (i < args.length)
    	{
    		if (args[i].equals("-host"))
    			exec.hostname = args[i+1];
    		else if (args[i].equals("-user"))
    			exec.username = args[i+1];
    		else if (args[i].equals("-sshport")) {
    			sshportString = args[i+1];
    			try {
    				exec.sshport = Integer.parseInt(sshportString);
    			} catch (NumberFormatException e) {
    				System.out.println("Could not understand the specified ssh port number");
    			}
    		}
    		else if (args[i].equals("-ccshost"))
    			exec.ccshost = args[i+1];
    		else if (args[i].equals("-port"))
    			exec.port = args[i+1];
    		else if (args[i].equals("-file"))
    			exec.executable = args[i+1];
    		else if (args[i].equals("-param"))
    			exec.parameters = args[i+1];
    		else if (args[i].equals("-pes") || args[i].equals("+p")) {
    			numberPesString = args[i+1];
    	    	try {
    	    		exec.npes = Integer.parseInt(numberPesString);
    	    	} catch (NumberFormatException e) {
    	    		System.out.println("Could not understand the specified number of processors");
    	    	}
    		}
    		else if (args[i].equals("-virtual")) {
    			exec.virtualDebug = true;
    			try {
    				exec.virtualNpes = Integer.parseInt(args[i+1]);
    			} catch (NumberFormatException e) {
    	    		System.out.println("Could not understand the specified number of virtual processors");
    	    	}
    		}
    		else if (args[i].equals("-display"))
    			envDisplay = args[i+1];
    		else if (args[i].equals("-dir"))
    			exec.workingDir = args[i+1];
    		else if (args[i].equals("-outputfile"))
    			exec.inputFile = args[i+1];
    		else if (args[i].equals("-waitfile")) {
    			exec.waitFile = true;
    			i--;
    		}
    		else if (args[i].equals("-sshtunnel")) {
    			exec.sshTunnel = true;
    			i--;
    		}
    		else if (args[i].equals("-noWindow")) {
    			noWindow = true;
    			i--;
    		}
    		else if (args[i].equals("-config")) {
    			try {
    				File config = new File(args[i+1]);
    				System.out.println("Config: "+config.getAbsolutePath());
    				exec = Execution.load(new File(args[i+1]));
    			} catch (IOException ioe) {
    				System.out.println("Could not open configuration file ");
    				ioe.printStackTrace();
    			} catch (ClassNotFoundException cnfe) {
    				System.out.println("Configuration file corrupted");
    			} catch (SAXException se) {
					System.out.println("Configuration file corrupted");
				}
    		}
    		else if (args[i].equals("-commands")) {
    			commands = new File(args[i+1]);
    		}
    		else
    		{ /* Just a 1-argument */
    			if (args[i].startsWith("+p")) {
    				numberPesString=args[i].substring(2);
    				try {
    					exec.npes = Integer.parseInt(numberPesString);
    				} catch (NumberFormatException e) {
    					System.out.println("Could not understand the specified number of processors");
    				}
    			}
    			else if (!gotFilename) {
    				if (args[i].startsWith("-") || args[i].startsWith("+")) {
    					printUsage();
    					System.exit(1);
    				}
    				else {
    					exec.executable=args[i]; 
    					gotFilename=true;
    				}
    			} 
    			else /* gotFilename, so these are arguments */ {
    				exec.parameters += " "+args[i];
    			}
    			i--; /* HACK: turns i+=2 into i++ for these single arguments... */
    		}
    		i = i+2;
    	}
    	if (i>args.length)
    	{
    		printUsage();
    		System.exit(1);
    	}
    	//setNumberPes(numberPesString);
//    	try {
//    		exec.npes = Integer.parseInt(numberPesString);
//    	} catch (NumberFormatException e) {
//    		System.out.println("Could not understand the specified number of processors");
//    	}

		PeSet.CellRenderer.initIcons();

    	appFrame = new JFrame("Charm Parallel Debugger");
    	appFrame.setSize(1000, 1000);
    	debugger = new ParDebug(exec);

    	appFrame.addWindowListener(new WindowAdapter() {
    		public void windowClosing(WindowEvent e) {
    			if (debugger.isRunning)
    			{
    				debugger.server.bcastCcsRequest("ccs_debug_quit", "");
    				debugger.quitProgram();
    			} 
    			debugger.preferences.save();
    			System.exit(0); /* main window closed */
    		}
    	});

    	appFrame.getContentPane().add(debugger, BorderLayout.CENTER);

    	if (! noWindow) {
    		Rectangle bounds = (appFrame.getGraphicsConfiguration()).getBounds(); 
    		appFrame.setLocation(50 +bounds.x, 50 + bounds.y);
    		appFrame.setJMenuBar(debugger.menuBar);
    		appFrame.pack();
    		if (debugger.getPreferredSize() != null) appFrame.setSize(debugger.getPreferredSize());
    		if (debugger.getPreferredLocation() != null) appFrame.setLocation(debugger.getPreferredLocation());
    		appFrame.setVisible(true);
    	}
    	if (commands != null) {
    		Commands cmds = new Commands(commands, debugger);
    		cmds.apply();
    	}
    }
}
