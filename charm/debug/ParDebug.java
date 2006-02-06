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
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.net.*;
import java.lang.*;

public class ParDebug extends JPanel
     implements ActionListener,ListSelectionListener{
   
    // ******* VARIABLES ************   
    //  FIXME: make these not be static, by moving main's command line
    //   handling into a regular function called by the constructor.
    private static String filename;
    private static String hostname;
    private static String portnumber;
    private static int numberPes;
    private static String clparams;
    private static String envDisplay;
    public static CpdUtil server;
    public static byte[] globals;

    //private CcsServer ccs; DEPRACATED: it goes into the variable "server"
    private DefaultListModel listModel;
    private PList listItems = null;
    private boolean isRunning = false; // True if the debugged program is running
    private boolean[] peList = null;
    
    private class CpdListInfo {
       public String display; // Client name CpdList should be displayed as.
       public String name; // Server name basic CpdList is registered under.
       public String detailedName; // Server name of detailed CpdList.
       
       public CpdListInfo(String display_,String name_) {
          display=display_; name=name_; detailedName=null;
       }
       public CpdListInfo(String display_,String name_,String detailed_) {
          display=display_; name=name_; detailedName=detailed_;
       }
    };
    
    /// CpdList names
    private CpdListInfo[] cpdLists =  {
        new CpdListInfo("-- Display --",null,null),
        new CpdListInfo("Array Elements","charm/arrayElementNames","charm/arrayElements"),
	new CpdListInfo("Messages in Queue","converse/localqueue",null),
	new CpdListInfo("Readonly Variables","charm/readonly",null),
	new CpdListInfo("Readonly Messages","charm/readonlyMsg",null),
	new CpdListInfo("Entry Points","charm/entries",null),
	new CpdListInfo("Chare Types","charm/chares",null),
	new CpdListInfo("Message Types","charm/messages",null),
	new CpdListInfo("Mainchares","charm/mains",null),
	new CpdListInfo("Viewable Lists","converse/lists",null)
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
    private PListOutputArea outputArea;
    private JTextField statusArea;
    private JComboBox listsbox;
    private JComboBox pesbox;
    private JList listItemNames;
    
    private JMenuBar menuBar;
    private JMenu menuFile;
    private JMenu menuAction;
    private JMenuItem menuFileOpen;
    private JMenuItem menuFileParameters;
    private JMenuItem menuActionStart; 
    private JMenuItem menuActionContinue;
    private JMenuItem menuActionQuit;
    private JMenuItem menuActionFreeze;
    private JMenuItem menuActionMemory;
    

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
    
    private static void setNumberPes(String peNumber) {
       try {
         numberPes = Integer.parseInt(peNumber);
       } 
       catch(Exception e) {
         System.out.println("Could not convert number of pes "+peNumber+" to an integer.");
	 System.exit(1); /* FIXME: this is overkill. */
       }
    }
    
    public void setParametersForProgram(String commandLine, String peNumber, String portno)
    {
       setNumberPes(peNumber);
       clparams = commandLine;
       portnumber = portno;
       addedRunParameter();
    } 
    private void addedRunParameter() {
       setStatusMessage("Executable: " +filename+ "        number of pes: "+numberPes);
    }
    
    public void setStatusMessage (String txt)
    {
       statusArea.setText(txt);
    }

    /// The user has just selected the cpdListIndex'th list on forPE.
    ///  Expand a list of the contents into dest.
    private void populateNewList(int cpdListIndex,int forPE, DefaultListModel dest)
    {
	  dest.removeAllElements();
          outputArea.setList(null);
	  listItems = null;
	  
          String lName=cpdLists[cpdListIndex].name;
	  if (lName==null) return; /* the initial empty list */
          int nItems=server.getListLength(lName,forPE);
          listItems = server.getPList(lName,forPE,0,nItems);
	  
	  for (PAbstract cur=listItems.elementAt(0);cur!=null;cur=cur.getNext()) {
	  	dest.addElement(cur.getDeepName());
	  }
    }
    
    /// The user has just selected listItem from the cpdListIndex'th list on forPE.
    ///  Expand this item.
    private void expandListElement(int cpdListIndex,int forPE,int listItem)
    {
    	String detailedName=cpdLists[cpdListIndex].detailedName;
	if (detailedName==null)
		outputArea.setList((PList)listItems.elementAt(listItem));
	else { /* There's a list to consult for further detail */
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
    public ParDebug() {
       isRunning = false;     

       setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       
       //Creating the menu
       menuBar = new JMenuBar();
       
       menuBar.add(menuFile = new JMenu("File"));
       menuFile.setMnemonic('F');
       
       menuFile.add(menuFileOpen = new JMenuItem("Open Program",'O'));
       listenTo(menuFileOpen,"browse","Open a parallel program to debug");
       menuFile.add(menuFileParameters = new JMenuItem("Program Parameters",'P'));
       listenTo(menuFileParameters,"params","Enter command-line parameters for the parallel program");
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

       //Creating status bar on the top
       statusArea = new JTextField(60);
       statusArea.setBorder(BorderFactory.createTitledBorder("Status"));
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

       add(statusArea);
       add(Box.createRigidArea(new Dimension(0, 20)));
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
        JScrollPane secondScrollPane = new JScrollPane(outputArea);
	secondScrollPane.setHorizontalScrollBarPolicy(
		 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
	);
        secondScrollPane.setBorder(BorderFactory.createTitledBorder("Details"));

        listScrollPane.setPreferredSize(new Dimension(200,230));
        secondScrollPane.setPreferredSize(new Dimension(400,230));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, secondScrollPane);
        splitPane.setOneTouchExpandable(true);
        
        panelForEntities.add(splitPane);
        add(panelForEntities);

	addedRunParameter();
        if (filename!="")
          startProgram();
    }

 
    public void actionPerformed(ActionEvent e) {
        int destPE = 0;
        if (e.getActionCommand().equals("browse")) 
	{ /* Bring up file dialog box to select a new executable */
           JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
           int returnVal = chooser.showOpenDialog(ParDebug.this);
           if(returnVal == JFileChooser.APPROVE_OPTION) {
               filename = chooser.getSelectedFile().getAbsolutePath();
	       addedRunParameter();
           }
        }
        else if (e.getActionCommand().equals("params")) 
	{ /* Bring up parameters dialog box to select run options */
           ParamsDialog dialogbox = new ParamsDialog(appFrame, true, this);
           dialogbox.setLocationRelativeTo(appFrame);
           dialogbox.setFields(clparams, ""+numberPes, portnumber);
           dialogbox.pack();
           dialogbox.setVisible(true);
        }
        else if (e.getActionCommand().equals("begin")) { /* buttons... */
	   startProgram();
        }
        else if (e.getActionCommand().equals("freeze")) {
           // stop program
           server.bcastCcsRequest("ccs_debug", "freeze",1, numberPes, peList);
           continueButton.setEnabled(true);
           freezeButton.setEnabled(false);
           setStatusMessage("Program is frozen on selected pes");
        }
	else if (e.getActionCommand().equals("unfreeze")){ 
           // start running again
           server.bcastCcsRequest("ccs_continue_break_point", "",0,numberPes,peList);
           continueButton.setEnabled(true); 
           freezeButton.setEnabled(true);
           setStatusMessage("Program is running");
        } 
        else if (e.getActionCommand().equals("quit")) {
	   server.bcastCcsRequest("ccs_debug_quit", "",0,numberPes,peList);
	   quitProgram(); 
        }
        else if (e.getActionCommand().equals("startgdb")) 
	{ 
           server.bcastCcsRequest("ccs_remove_all_break_points", "",0,numberPes,peList);
	   server.bcastCcsRequest("ccs_debug_startgdb","",1,numberPes,peList);
           setStatusMessage("Gdb started on selected pes");
        } 
        else if (e.getActionCommand().equals("breakpoints")) {
           // set or remove breakpoints
           
           JCheckBox chkbox = (JCheckBox)e.getSource();
           String entryPointName = chkbox.getText(); 
           if (chkbox.isSelected())
           {
        	server.bcastCcsRequest("ccs_set_break_point", entryPointName,0,numberPes,peList);
        	continueButton.setEnabled(true);
        	freezeButton.setEnabled(false);
        	setStatusMessage ("Break Point set at entry point " +entryPointName); 
           }
           else
           {
        	server.bcastCcsRequest("ccs_remove_break_point", entryPointName,0,numberPes,peList);
        	continueButton.setEnabled(true);   
        	freezeButton.setEnabled(true);
        	setStatusMessage ("Break Point removed at entry point " +entryPointName+" on selected Pes"); 
           }

        }
	else if (e.getActionCommand().equals("memory")) {
	    // ask the user for input
	    MemoryDialog input = new MemoryDialog(appFrame, true, numberPes);
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
	  quitProgram();
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
           isRunning = true;
           programOutputArea.setText("");
           String executable = filename;
	   String charmrunDir = new File(executable).getParent();
	   if (charmrunDir==null) charmrunDir=".";
           String charmrunPath = charmrunDir + "/charmrun";
           if (envDisplay.length() == 0) envDisplay = getEnvDisplay();
           // System.out.println(envDisplay);
	   
           String totCommandLine = charmrunPath + " " + "+p"+ numberPes + " " +executable + " " + clparams+"  +cpd +DebugDisplay " +envDisplay+" ++server ++charmdebug";
           if (portnumber.length() != 0)
               totCommandLine += " ++server-port " + portnumber;
	   if (!hostname.equals("localhost")) {
	       totCommandLine = "ssh " + hostname + " " + totCommandLine;
	   }
           System.out.println("ParDebug> "+totCommandLine);
           programOutputArea.setText(totCommandLine);
           Process p = null;
           Runtime runtime = null;
           runtime = Runtime.getRuntime();
           try {
                p = runtime.exec(totCommandLine);
           }
           catch (Exception exc) {
                 System.out.println("ParDebug> Error executing "+totCommandLine);
                 quitProgram();
                 return;
           }
           ServThread servthread = (new ServThread(this, p));
           servthread.start();
	   ServThread.charmrunIn = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
	   ServThread.charmrunOut = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	   // Retrieve the initial info from charmrun regarding the program segments
	   String initialInfo;
	   while ((initialInfo = ServThread.infoCommand(" ")).indexOf("\n.data") == -1) System.out.println("++|"+initialInfo+"|");
	   System.out.println("|"+initialInfo+"|");
	   int dataInitial = initialInfo.indexOf("\n.data");
	   int dataFinal = initialInfo.indexOf("\n",dataInitial+1);
	   String dataValues = initialInfo.substring(dataInitial+6,dataFinal).trim();
	   int endSize = dataValues.indexOf(' ');
	   int startPos = dataValues.lastIndexOf(' ');
	   int dataSize = Integer.parseInt(dataValues.substring(0,endSize));
	   int dataPos = Integer.parseInt(dataValues.substring(startPos+1));
	   //System.out.println("string1: |"+initialInfo.substring(dataInitial+6,dataFinal).trim()+"| "+dataSize+" "+dataPos);
	   int bssInitial = initialInfo.indexOf("\n.bss");
	   int bssFinal = initialInfo.indexOf("\n",bssInitial+1);
	   String bssValues = initialInfo.substring(bssInitial+6,bssFinal).trim();
	   endSize = bssValues.indexOf(' ');
	   startPos = bssValues.lastIndexOf(' ');
	   int bssSize = Integer.parseInt(bssValues.substring(0,endSize));
	   int bssPos = Integer.parseInt(bssValues.substring(startPos+1));
	   //System.out.println("string1: |"+initialInfo.substring(bssInitial+5,bssFinal).trim()+"| "+bssSize+" "+bssPos);
	   // FIXME: here we assume the program is 32 bit!!!
	   globals = new byte[16]; // 4 integers of 4 bytes each
	   CcsServer.writeInt(globals, 0, dataPos);
	   CcsServer.writeInt(globals, 4, dataPos+dataSize);
	   CcsServer.writeInt(globals, 8, bssPos);
	   CcsServer.writeInt(globals, 12, bssPos+bssSize);

	   // Delete the first print made by gdb at startup
	   ServThread.infoCommand(" ");

	 /* Wait until the "ccs:" line comes out of the program's stdout */
           long iter = 0;
           while (servthread.portno == null)
           {
              try { Thread.sleep(100); }
              catch(InterruptedException e1)
              { /* don't care about interrupted sleep */ }
              if(iter++ > 60*10) abort("Timeout waiting for program to start up (and print its CCS port number)");
           }
           if (portnumber.length() == 0)
               portnumber = servthread.portno;
	   System.out.println("ParDebug> Charmrun started (CCS port "+portnumber+")");
	 
	 /* Connect to the new program */
           String[] ccsArgs=new String[2];
           ccsArgs[0]=hostname;
           ccsArgs[1]= portnumber;
	   System.out.println("Connecting to: "+hostname+":"+portnumber);
           CcsServer ccs = CcsServer.create(ccsArgs,false);
	   server = new CpdUtil(ccs);

	 /* Create the pe list */
           peList = new boolean[numberPes]; 
           for (int i = 0; i < numberPes; i++)
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
          
         /* Create the entities lists */
           int nItems=server.getListLength("charm/entries",0);
	   EpPList epItems = new EpPList(server.getPList("charm/entries",0,0,nItems));
           
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
           sysEpsActualPanel.updateUI();
           listsbox.setEnabled(true);
           pesbox.setEnabled(true);  
           quitButton.setEnabled(true);
    }

    /// Exit the debugged program.
    public void quitProgram()
    {
            portnumber = "";
            isRunning = false;
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
    

    public static void printUsage()
    {
        System.out.println("Usage: java ParDebug [[-file <charm program name>] [[-param \"<charm program parameters>\"][-pes <number of pes>]] [-host <hostname>] [-port <port>] [-display <display>]]");
    }
  
    public static void main(String[] args) {
        hostname = "localhost";
        filename = "";
        portnumber = "";
        numberPes = 1;
	String numberPesString="1";
        clparams = "";
        envDisplay = "";

        // parsing command-line parameters
        int i = 0;
	boolean gotFilename=false;
        while (i < args.length)
        {
          if (args[i].equals("-host"))
             hostname = args[i+1];
          else if (args[i].equals("-port"))
            portnumber = args[i+1];
          else if (args[i].equals("-file"))
            filename = args[i+1];
          else if (args[i].equals("-param"))
            clparams = args[i+1];
          else if (args[i].equals("-pes") || args[i].equals("+p"))
            numberPesString = args[i+1];
          else if (args[i].equals("-display"))
            envDisplay = args[i+1];
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
	         filename=args[i]; 
		 gotFilename=true;
	       }
	     } 
	     else /* gotFilename, so these are arguments */ {
	       clparams=clparams+" "+args[i];
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
        setNumberPes(numberPesString);
           
        appFrame = new JFrame("Charm Parallel Debugger");
        appFrame.setSize(1000, 1000);
        final ParDebug debugger = new ParDebug();

        appFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (debugger.isRunning)
                   {
                        debugger.server.bcastCcsRequest("ccs_debug_quit", "",-1,numberPes,null);
                   } 
                System.exit(0); /* main window closed */
            }
        });

        appFrame.getContentPane().add(debugger, BorderLayout.CENTER);

        Rectangle bounds = (appFrame.getGraphicsConfiguration()).getBounds(); 
        appFrame.setLocation(50 +bounds.x, 50 + bounds.y);
        appFrame.setJMenuBar(debugger.menuBar);
        appFrame.pack();
        appFrame.setVisible(true);
    }
}
