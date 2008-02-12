/**
  Parallel Debugger for Charm++ over CCS
  
  This is the main GUI panel, and is the central controller
  for everything the debugger does.
  
  Copyright University of Illinois at Urbana-Champaign
  Written by Rashmi Jothi in Fall of 2003
  Minor improvements by Orion Lawlor in 1/2004
*/
package charm.debug;

import charm.ccs.CcsServer;
import charm.debug.fmt.*;
import charm.debug.pdata.*;
import charm.debug.inspect.Inspector;
import charm.debug.inspect.InspectPanel;
import charm.debug.preference.*;

import javax.swing.*;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.net.*;

public class ParDebug extends JPanel
     implements ActionListener,ListSelectionListener{
   
    // ******* VARIABLES ************   
    //  FIXME: make these not be static, by moving main's command line
    //   handling into a regular function called by the constructor.
	private Preference preferences;
	private Execution exec;
    //private static String filename;
    //private static String hostname;
    //private static String username;
    //private static String portnumber;
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
    public static CpdUtil server;

    //private CcsServer ccs; DEPRACATED: it goes into the variable "server"
    private DefaultListModel listModel;
    private PList listItems = null;
    private boolean isRunning = false; // True if the debugged program is running
    private boolean[] peList = null;
    public static int currentListedPE;

    private MsgPList messageQueue;
    private EpPList epItems;
    private MsgTypePList msgItems;
    private ChareTypePList chareItems;
    
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
        new CpdListInfo("Charm Objects","charm/objectNames",null,new CharePList()),
        new CpdListInfo("Array Elements","charm/arrayElements",null,new CharePList()),
	new CpdListInfo("Messages in Queue","converse/localqueue",null,messageQueue=new MsgPList()),
	new CpdListInfo("Readonly Variables","charm/readonly",null,new ReadonlyPList()),
	new CpdListInfo("Readonly Messages","charm/readonlyMsg",null,null),
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
    private JButton quitButton;
    private JButton freezeButton;
    private JButton startGdbButton;
    
    private JPanel peActualPanel;
    private JPanel sysEpsActualPanel;
    private JPanel userEpsActualPanel;
    
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
    
    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenu menuAction;
    private JMenuItem menuFileOpen;
    private JMenuItem menuFileEdit;
    private JMenuItem menuFileSave;
    private JMenu menuRecent;
    private JMenuItem menuWindowSettings;
    private JMenuItem menuActionStart; 
    private JMenuItem menuActionContinue;
    private JMenuItem menuActionQuit;
    private JMenuItem menuActionFreeze;
    private JMenuItem menuActionMemory;
    private JMenuItem menuActionAllocationTree;
    private JMenuItem menuActionAllocationGraph;
    

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

    public Dimension getPreferredSize() {
    	if (preferences.size != null) return preferences.size;
    	return super.getPreferredSize();
    }
    
    public Point getPreferredLocation() {
    	return preferences.location;
    }
    
    /// The user has just selected the cpdListIndex'th list on forPE.
    ///  Expand a list of the contents into dest.
    private void populateNewList(int cpdListIndex,int forPE, DefaultListModel dest)
    {
	  dest.removeAllElements();
          outputArea.setList(null);
          newOutputArea.setText("");
	  listItems = null;
	  
          String lName=cpdLists[cpdListIndex].name;
	  if (lName==null) return; /* the initial empty list */
          GenericPList list = cpdLists[cpdListIndex].list;

          if (list == null || list.needRefresh()) {
              int nItems=server.getListLength(lName,forPE);
              listItems = server.getPList(lName,forPE,0,nItems);
          }

          if (list != null) {
              if (list.needRefresh()) list.load(listItems);
              list.populate(dest);
          } else {
              for (PAbstract cur=listItems.elementAt(0);cur!=null;cur=cur.getNext()) {
                  dest.addElement(cur.getDeepName());
              }
          }
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
       preferences = new Preference();
       preferences.load();
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
       menuAction.add(menuActionContinue = new JMenuItem("Continue",'C'));
       listenTo(menuActionContinue,"unfreeze","Continue to run the parallel program"); 
       menuAction.add(menuActionFreeze = new JMenuItem("Freeze",'F'));
       listenTo(menuActionFreeze,"freeze","Freeze the parallel program"); 
       menuAction.add(menuActionQuit = new JMenuItem("Quit",'Q'));
       listenTo(menuActionQuit,"quit","Quit the parallel program"); 
       menuAction.addSeparator();
       menuAction.add(menuActionMemory = new JMenuItem("Memory",'M'));
       listenTo(menuActionMemory,"memory","Inspect the application memory"); 
       menuAction.add(menuActionAllocationTree = new JMenuItem("Memory Allocation Tree",'T'));
       listenTo(menuActionAllocationTree,"allocationTree","Print the memory allocation tree");
       menuAction.add(menuActionAllocationGraph = new JMenuItem("Memory Allocation Graph",'G'));
       listenTo(menuActionAllocationGraph,"allocationGraph","Print the memory allocation graph");

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

       JPanel entryPointsPanel = new JPanel();
       entryPointsPanel.setLayout(new BoxLayout(entryPointsPanel, BoxLayout.Y_AXIS));
       entryPointsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Set Break Points"), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
       entryPointsPanel.setPreferredSize(new Dimension(200, 380));

       sysEpsActualPanel = new JPanel(); 
       sysEpsActualPanel.setLayout(new BoxLayout(sysEpsActualPanel, BoxLayout.Y_AXIS));
       JScrollPane entryScrollPane1 = new JScrollPane(sysEpsActualPanel);
       entryScrollPane1.setBorder(BorderFactory.createTitledBorder("System Entry Points"));
       entryScrollPane1.setPreferredSize(new Dimension(200,185));
       userEpsActualPanel = new JPanel(); 
       userEpsActualPanel.setLayout(new BoxLayout(userEpsActualPanel, BoxLayout.Y_AXIS));
       JScrollPane entryScrollPane2 = new JScrollPane(userEpsActualPanel);
       entryScrollPane2.setBorder(BorderFactory.createTitledBorder("User Entry Points"));
       entryScrollPane2.setPreferredSize(new Dimension(200,185));

       JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, entryScrollPane1, entryScrollPane2);
       splitPane1.setOneTouchExpandable(true);
       splitPane1.setResizeWeight(0.5);
       entryPointsPanel.add(splitPane1);
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
       peActualPanel = new JPanel();
       peActualPanel.setLayout(new BoxLayout(peActualPanel, BoxLayout.Y_AXIS));
       JScrollPane pesScrollPane = new JScrollPane(peActualPanel);
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

       JSplitPane middleSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, entryPointsPanel, secondPanelWithOutput);
       middleSplitPane.setResizeWeight(0.5);
       middlePanel.add(middleSplitPane);
       //middlePanel.add(entryPointsPanel);
       //middlePanel.add(Box.createRigidArea(new Dimension(15,0)));
       //middlePanel.add(secondPanelWithOutput);
       middlePanel.setPreferredSize(new Dimension(600, 380));

       add(middlePanel);
        
 
       // ************ BOTTOM PANEL *****************
       // Entity list on the left, details on the right
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
        add(panelForComboBoxes);

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
        add(panelForEntities);

        add(statusArea);
        //add(Box.createRigidArea(new Dimension(0, 20)));

        gdb = new GdbProcess(this);
	addedRunParameter();
        //if (exec.executable!="")
        //  startProgram();
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
	
	private void loadConfigFile(File filename) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
			exec = (Execution) ois.readObject();
			exec.locationOnDisk = filename.getAbsolutePath();
		} catch (IOException ioe) {
			setStatusMessage("Failed to load configuration file");
			return;
		} catch (ClassNotFoundException cnfe) {
			setStatusMessage("Configuration file corrupted");
			return;
		}
		loadedRunParameter();
		preferences.addRecent(exec.locationOnDisk);
		updateRecentConfig();
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
    				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
    				oos.writeObject(exec);
    				oos.close();
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
    		startProgram();
    	}
    	else if (e.getActionCommand().equals("freeze")) {
    		// stop program
    		server.bcastCcsRequest("ccs_debug", "freeze",1, exec.npes, peList);
    		continueButton.setEnabled(true);
    		freezeButton.setEnabled(false);
    		setStatusMessage("Program is frozen on selected pes");
    	}
    	else if (e.getActionCommand().equals("unfreeze")){ 
    		// start running again
    		server.bcastCcsRequest("ccs_continue_break_point", "",0,exec.npes,peList);
    		continueButton.setEnabled(true); 
    		freezeButton.setEnabled(true);
    		setStatusMessage("Program is running");
    	} 
    	else if (e.getActionCommand().equals("quit")) {
    		server.bcastCcsRequest("ccs_debug_quit", "",0,exec.npes,peList);
    		quitProgram(); 
    	}
    	else if (e.getActionCommand().equals("startgdb")) 
    	{ 
    		server.bcastCcsRequest("ccs_remove_all_break_points", "",0,exec.npes,peList);
    		//server.bcastCcsRequest("ccs_debug_startgdb","",1,numberPes,peList);
        	for (int i=0; i<exec.npes; ++i) {
        		if (peList[i]){
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
        			String script = "ssh "+ipAddress+" \"cat > /tmp/start_gdb."+pid+" << END_OF_SCRIPT\n"
        							+"shell /bin/rm -f /tmp/start_gdb."+pid+"\n"
        							+"handle SIGWINCH nostop noprint\n"
        							+"handle SIGWAITING nostop noprint\n"
        							+"attach "+pid+"\n"
        							+"END_OF_SCRIPT\n"
        							+"gdb "+new File(exec.executable).getAbsolutePath()
        							+" -x /tmp/start_gdb."+pid+"\"";
        			if (!exec.hostname.equals("localhost")) {
        				sshCommand = "ssh -T "+exec.hostname+" ssh -T";
        				str = new String[9];
        				str[5] = exec.hostname;
        				str[6] = "ssh";
        				//str[8] = "-T";
        				str[7] = ipAddress;
        				str[8] = script;
        			} else {
        				sshCommand = "ssh -T";
        				str = new String[7];
        				str[5] = ipAddress;
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

    		JCheckBox chkbox = (JCheckBox)e.getSource();
    		String entryPointName = chkbox.getText(); 
    		if (chkbox.isSelected())
    		{
    			server.bcastCcsRequest("ccs_set_break_point", entryPointName,0,exec.npes,peList);
    			continueButton.setEnabled(true);
    			freezeButton.setEnabled(false);
    			setStatusMessage ("Break Point set at entry point " +entryPointName); 
    		}
    		else
    		{
    			server.bcastCcsRequest("ccs_remove_break_point", entryPointName,0,exec.npes,peList);
    			continueButton.setEnabled(true);   
    			freezeButton.setEnabled(true);
    			setStatusMessage ("Break Point removed at entry point " +entryPointName+" on selected Pes"); 
    		}

    	}
    	else if (e.getActionCommand().equals("memory")) {
    		// ask the user for input
    		MemoryDialog input = new MemoryDialog(appFrame, true, exec.npes);
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
    	    AllocationGraphDialog input = new AllocationGraphDialog(appFrame, true, exec.npes);
    	    if (input.confirmed()) { 	    
    		JFrame frame = new JFrame("Allocation Graph");
    		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    		AllocationGraphPanel at = new AllocationGraphPanel();
    		JComponent newContentPane = at;
    		newContentPane.setOpaque(true);
    		frame.setContentPane(newContentPane);
    		frame.setTitle("Allocation Graph");

    		String executable = new File(exec.executable).getAbsolutePath();
    		String logFile = new File(executable).getParent();
    		if (logFile == null) logFile = ".";
    		logFile += "/memoryLog_";
    		at.load(frame, new MemoryTrace(logFile, exec.npes), input);

    		frame.setJMenuBar(at.getMenu());
    		frame.pack();
    		frame.setVisible(true);
    	    }
    	}
    	else if (e.getActionCommand().equals("pecheck")) 
    	{ /* Checked or unchecked a PE box */
    		JCheckBox chkbox = (JCheckBox)e.getSource();
    		String peText = chkbox.getText();
    		String peno = peText.substring(3);
    		if (chkbox.isSelected()) 
    			peList[Integer.parseInt(peno)] = true;
    		else
    			peList[Integer.parseInt(peno)] = false;
    	} 
    	else if (e.getActionCommand().equals("lists") 
    			|| e.getActionCommand().equals("changepe")) 
    	{ /* Clicked on list or pe drop-down */
    		if (pesbox.getSelectedItem()!=null) {
    			int forPE=Integer.parseInt((String)pesbox.getSelectedItem());
    			populateNewList(listsbox.getSelectedIndex(),forPE, listModel); 
    		}
    	}
    	else if (e.getActionCommand().equals("exitDebugger")) {
    		if (isRunning) {
    			server.bcastCcsRequest("ccs_debug_quit", "",0,exec.npes,peList);
    			quitProgram();
    		}
            preferences.save();
    		System.exit(0);
    	}
    } // end of actionPerformed
     
    public void valueChanged(ListSelectionEvent e) {
      
      if(e.getValueIsAdjusting()) return;
      
      JList theList = (JList)e.getSource();
      if (theList == listItemNames && !theList.isSelectionEmpty())
      {
        int forPE=Integer.parseInt((String)pesbox.getSelectedItem());
        expandListElement(listsbox.getSelectedIndex(),forPE,theList.getSelectedIndex());
      }
  
    } // end of valueChanged

/*************** Program Control ***********************/
    /// Start the program from scratch.
    public void startProgram() 
    {
    	gdb.terminate();
    	isRunning = true;
    	programOutputArea.setText("");
    	String executable = new File(exec.executable).getAbsolutePath();
    	String charmrunDir = new File(executable).getParent();
    	if (charmrunDir==null) charmrunDir=".";
    	String charmrunPath = charmrunDir + "/charmrun";
    	if (envDisplay.length() == 0) envDisplay = getEnvDisplay();
    	// System.out.println(envDisplay);

    	numberPesGlobal = exec.npes;
    	String totCommandLine = charmrunPath + " " + "+p"+ exec.npes + " " +executable + " " + exec.parameters+"  +cpd +DebugDisplay " +envDisplay+" ++server";// ++charmdebug";
    	if (exec.port.length() != 0)
    		totCommandLine += " ++server-port " + exec.port;
    	if (!exec.hostname.equals("localhost")) {
    		if (exec.username.length()>0) {
    			totCommandLine = "-l " + exec.username + " " + totCommandLine;
    		}
    		totCommandLine = "ssh " + exec.hostname + " " + totCommandLine;
    	}
    	System.out.println("ParDebug> "+totCommandLine);
    	programOutputArea.setText(totCommandLine);
    	Process p = null;
    	Runtime runtime = null;
    	runtime = Runtime.getRuntime();
    	try {
    		// start new process for charmrun
    		p = runtime.exec(totCommandLine);
    	}
    	catch (Exception exc) {
    		System.out.println("ParDebug> Error executing "+totCommandLine);
    		quitProgram();
    		return;
    	}

    	// start the thread that will handle the communication with charmrun
    	// (and the program output as a consequence)
    	servthread = (new ServThread(this, p));
    	servthread.start();

    	try {
    		// Retrieve the initial info from charmrun regarding the program segments
    		//StringBuffer initialInfoBuf = new StringBuffer();
    		String initialInfo = getInitialInfo(); //servthread.infoCommand(" ");
    		//while ((initialInfo = servthread.infoCommand(" ")).indexOf("\n(gdb)") == -1) {
    		//    initialInfoBuf.append(initialInfo);
    		//    System.out.println("++|"+initialInfo+"|");
    		//}
    		//System.out.println("|"+initialInfo+"|");
    		//initialInfoBuf.append(initialInfo);
    		//initialInfo = initialInfoBuf.toString();
    		//System.out.println("|"+initialInfo+"|");
    		int dataInitial = initialInfo.indexOf("\n.data ");
    		int dataFinal = initialInfo.indexOf("\n",dataInitial+1);
    		String dataValues = initialInfo.substring(dataInitial+6,dataFinal).trim();
    		int endSize = dataValues.indexOf(' ');
    		int startPos = dataValues.lastIndexOf(' ');
    		int dataSize = Integer.parseInt(dataValues.substring(0,endSize));
    		dataPos = Integer.parseInt(dataValues.substring(startPos+1));
    		//System.out.println("string1: |"+initialInfo.substring(dataInitial+6,dataFinal).trim()+"| "+dataSize+" "+dataPos);
    		int bssInitial = initialInfo.indexOf("\n.bss");
    		int bssFinal = initialInfo.indexOf("\n",bssInitial+1);
    		String bssValues = initialInfo.substring(bssInitial+6,bssFinal).trim();
    		endSize = bssValues.indexOf(' ');
    		startPos = bssValues.lastIndexOf(' ');
    		int bssSize = Integer.parseInt(bssValues.substring(0,endSize));
    		int bssPos = Integer.parseInt(bssValues.substring(startPos+1));
    		//System.out.println("string1: |"+initialInfo.substring(bssInitial+5,bssFinal).trim()+"| "+bssSize+" "+bssPos);
    		// FIXME: here we assume the program is 32 bit, or if 64 bit all the addresses are small
    		globals = new byte[16]; // 4 integers of 4 bytes each
    		CcsServer.writeInt(globals, 0, dataPos);
    		CcsServer.writeInt(globals, 4, dataPos+dataSize);
    		CcsServer.writeInt(globals, 8, bssPos);
    		CcsServer.writeInt(globals, 12, bssPos+bssSize);

    		// Delete the first print made by gdb at startup
    		//System.out.println(servthread.infoCommand(" "));

    		/* Wait until the "ccs:" line comes out of the program's stdout */
    		long iter = 0;
    		while (servthread.portno == null)
    		{
    			try { Thread.sleep(100); }
    			catch(InterruptedException e1)
    			{ /* don't care about interrupted sleep */ }
    			if(iter++ > 60*10) abort("Timeout waiting for program to start up (and print its CCS port number)");
    		}
    		if (exec.port.length() == 0)
    			exec.port = servthread.portno;
    		if (exec.hostname.equals("localhost")) hostnumber = servthread.hostName;
    		System.out.println("ParDebug> Charmrun started (CCS IP "+(exec.hostname.equals("localhost")?hostnumber:exec.hostname)+", port "+exec.port+")");

    		/* Connect to the new program */
    		String[] ccsArgs=new String[2];
    		ccsArgs[0]= exec.hostname.equals("localhost")?hostnumber:exec.hostname;
    		ccsArgs[1]= exec.port;
    		if (exec.sshTunnel) {
    			System.out.println("ParDebug> Tunneling connection through ssh");
    			try {
    				sshTunnel = runtime.exec("ssh -2 -c blowfish -L "+exec.port+":localhost:"+exec.port+" "+exec.hostname);
    			} catch (Exception exc) {
    				System.out.println("ParDebug> Could not create ssh tunnel");
    			}
    			try { Thread.sleep(5000); }
    			catch(InterruptedException e1) {}
    			ccsArgs[0] = "localhost";
    		}
    		System.out.println("Connecting to: "+exec.username+(exec.username.length()>0?"@":"")+(exec.hostname.equals("localhost")?hostnumber:exec.hostname)+":"+exec.port);
    		CcsServer ccs = CcsServer.create(ccsArgs,false);
    		server = new CpdUtil(ccs);

    		/* Create the pe list */
    		peList = new boolean[exec.npes]; 
    		for (int i = 0; i < exec.npes; i++)
    		{
    			String peNumber = (new Integer(i)).toString();
    			pesbox.addItem( peNumber );
    			String chkboxlabel = "pe " + peNumber;
    			JCheckBox chkbox = new JCheckBox(chkboxlabel); 
    			peActualPanel.add(chkbox);
    			chkbox.setSelected(true);
    			chkbox.addActionListener(this);
    			chkbox.setActionCommand("pecheck");     
    			peList[i] = true; 
    		}

    		peActualPanel.updateUI();

    		startButton.setEnabled(false);
    		continueButton.setEnabled(true);
    		quitButton.setEnabled(false);
    		freezeButton.setEnabled(false);
    		startGdbButton.setEnabled(true); 

    		int nItems;

    		/* Reset the type information stored */
    		Inspector.initialize(server);

    		/* Load the information regarding all chares */
    		nItems = server.getListLength("charm/chares",0);
    		chareItems.load(server.getPList("charm/chares",0,0,nItems));

    		/* Set up the lookup information for the Entry Methods */
    		epItems.setLookups(chareItems);

    		/* Create the entities lists */
    		nItems=server.getListLength("charm/entries",0);
    		epItems.load(server.getPList("charm/entries",0,0,nItems));

    		Vector items;
    		items = epItems.getUserEps();
    		int l = items.size();
    		int i = 0;
    		while (i < l)
    		{
    			String tmp = (items.elementAt(i)).toString();
    			i++;
    			JCheckBox chkbox = new JCheckBox(tmp);
    			chkbox.addActionListener(this);
    			chkbox.setActionCommand("breakpoints"); 
    			userEpsActualPanel.add(chkbox);

    		}
    		userEpsActualPanel.updateUI();
    		items = epItems.getSystemEps();
    		l = items.size();
    		i = 0;
    		while (i < l)
    		{
    			String tmp = (items.elementAt(i)).toString();
    			i++;
    			JCheckBox chkbox = new JCheckBox(tmp);
    			chkbox.addActionListener(this);
    			chkbox.setActionCommand("breakpoints"); 
    			sysEpsActualPanel.add(chkbox);

    		}

    		/* Load the information regarding all messages */
    		nItems = server.getListLength("charm/messages",0);
    		msgItems.load(server.getPList("charm/messages",0,0,nItems));

    		/* Set the lookup lists for the message queue inspector */
    		messageQueue.setLookups(epItems, msgItems, chareItems);
    	} catch (Exception e) {
    		System.out.println("Error while starting the application (error: "+e+". Aborting...");
    		p.destroy();
    	}

    	sysEpsActualPanel.updateUI();
    	listsbox.setEnabled(true);
    	pesbox.setEnabled(true);  
    	quitButton.setEnabled(true);
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
    	continueButton.setEnabled(false); 
    	quitButton.setEnabled(false);
    	freezeButton.setEnabled(false);
    	startGdbButton.setEnabled(false);
    	listModel.removeAllElements();
    	outputArea.setList(null); 
    	listsbox.setEnabled(false);
    	pesbox.removeAllItems(); 
    	pesbox.setEnabled(false);

    	peActualPanel.removeAll();
    	peActualPanel.updateUI();
    	userEpsActualPanel.removeAll(); 
    	userEpsActualPanel.updateUI(); 
    	sysEpsActualPanel.removeAll(); 
    	sysEpsActualPanel.updateUI(); 
    	setStatusMessage(new String("Ready to start new program"));
    }
    
    private String getInitialInfo() {
		String executable = new File(getFilename()).getAbsolutePath();
		String totCommandLine = "size -A " + executable;
		String hostname = getHostname();
		if (!hostname.equals("localhost")) {
			totCommandLine = hostname + " " + totCommandLine;
			String username = getUsername();
			if (!username.equals("")) {
				totCommandLine = "-l " + username + " " + totCommandLine;
			}
			totCommandLine = "ssh " + totCommandLine;
		}
		try {
			//System.out.println("Starting: '"+totCommandLine+"'");
			Process p = Runtime.getRuntime().exec(totCommandLine);
			BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuffer reply = new StringBuffer();
			int c;
			while ((c = output.read()) != -1) {
				reply.append((char)c);
			}
			//System.out.println("STRINGA: "+reply.toString());
			return reply.toString();
		} catch (Exception e) {
			System.out.println("Failed to start gdb info program");
			return "error";
		}
    }
    
    public static void printUsage()
    {
        System.out.println("Usage: java ParDebug [[-file <charm program name>] [[-param \"<charm program parameters>\"][-pes <number of pes>]] [-host <hostname>] [-user <username>] [-port <port>] [-sshtunnel] [-display <display>]]");
    }
    
    String getFilename() { return exec.executable; }
    String getHostname() { return exec.hostname; }
    String getUsername() { return exec.username; }
    
    public static int numberPesGlobal;
    public static void main(String[] args) {
    	Execution exec = new Execution();
        exec.hostname = "localhost";
        exec.username = "";
        exec.executable = "";
        exec.port = "";
        exec.npes = 1;
	String numberPesString="1";
        exec.parameters = "";
        envDisplay = "";
        exec.sshTunnel = false;
        sshTunnel = null;

        // parsing command-line parameters
        int i = 0;
	boolean gotFilename=false;
        while (i < args.length)
        {
          if (args[i].equals("-host"))
              exec.hostname = args[i+1];
          else if (args[i].equals("-user"))
              exec.username = args[i+1];
          else if (args[i].equals("-port"))
              exec.port = args[i+1];
          else if (args[i].equals("-file"))
              exec.executable = args[i+1];
          else if (args[i].equals("-param"))
              exec.parameters = args[i+1];
          else if (args[i].equals("-pes") || args[i].equals("+p"))
              numberPesString = args[i+1];
          else if (args[i].equals("-display"))
              envDisplay = args[i+1];
          else if (args[i].equals("-sshtunnel")) {
              exec.sshTunnel = true;
              i--;
          }
          else
          { /* Just a 1-argument */
	     if (args[i].startsWith("+p"))
	       numberPesString=args[i].substring(2);
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
        try {
        	exec.npes = Integer.parseInt(numberPesString);
        } catch (NumberFormatException e) {
        	System.out.println("Could not understand the specified number of processors");
        }
           
        appFrame = new JFrame("Charm Parallel Debugger");
        appFrame.setSize(1000, 1000);
        final ParDebug debugger = new ParDebug(exec);

        appFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (debugger.isRunning)
                   {
                        ParDebug.server.bcastCcsRequest("ccs_debug_quit", "",-1,numberPesGlobal,null);
                        debugger.quitProgram();
                   } 
                debugger.preferences.save();
                System.exit(0); /* main window closed */
            }
        });

        appFrame.getContentPane().add(debugger, BorderLayout.CENTER);

        Rectangle bounds = (appFrame.getGraphicsConfiguration()).getBounds(); 
        appFrame.setLocation(50 +bounds.x, 50 + bounds.y);
        appFrame.setJMenuBar(debugger.menuBar);
        appFrame.pack();
        if (debugger.getPreferredSize() != null) appFrame.setSize(debugger.getPreferredSize());
        if (debugger.getPreferredLocation() != null) appFrame.setLocation(debugger.getPreferredLocation());
        appFrame.setVisible(true);
    }
}
