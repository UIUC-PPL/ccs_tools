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
    private static String filename;
    private static String hostname;
    private static String portnumber;
    private static String numberPes;
    private static String clparams;
    private static String envDisplay;
    
    private CcsServer ccs;
    private DefaultListModel listModel;
    private Stringifiable listItems = null;
    private EpStringifiable epItems = null;
    private String[] listStrings =  {"lists","readonly", "readonlyMsg", "messages", "chares", "entries", "mains", "arrayelements", "localqueue"};
    private int noOfPes;
    private boolean isRunning = false;
    private boolean[] peList = null;
    private File parallelFile = null;
     

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
    private JTextArea outputArea;
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
    
    
    private int getListLength(String listName,int forPE) throws IOException
    {
      //Build a byte array describing the ccs request:
      int reqStr=listName.length();
      int reqLen=4+reqStr+1;
      System.out.println("length of reqStr is "+reqStr);
      byte[] req=new byte[reqLen];
      CcsServer.writeInt(req,0,reqStr);
      CcsServer.writeString(req,4,reqStr+1,listName);
      CcsServer.Request r=ccs.sendRequest("ccs_list_len",forPE,req);
 
      //Get the response and take it apart:
      byte[] resp=ccs.recvResponse(r);
      if (resp.length<4) return -1;
      return CcsServer.readInt(resp,0);
    }

    //Returns a set of CpdList items as a string
    private String stringList(String listName,int forPE,int lo,int hiPlusOne) throws IOException
    {
      //Build a byte array describing the ccs request:
      int reqStr=listName.length();
      int reqLen=4+4+4+0+4+reqStr+1;
      byte[] req=new byte[reqLen];
      CcsServer.writeInt(req,0,lo);
      CcsServer.writeInt(req,4,hiPlusOne);
      CcsServer.writeInt(req,8,0); /*no additional request data*/
      CcsServer.writeInt(req,12,reqStr);
      CcsServer.writeString(req,16,reqStr+1,listName);
      CcsServer.Request r=ccs.sendRequest("ccs_list_items.txt",forPE,req);
 
      //Get the response and print it:
      byte[] resp=ccs.recvResponse(r);
      return new String(resp);
    }

    //Sends a request to the ccs server
    private String sendCcsRequest(String ccsHandlerName, String parameterName, int destPE) throws IOException
    {
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
        System.out.println("ERROR> in retrieving IP address for DISPLAY variable");
        System.out.println(exc.getMessage());
        System.exit(1);
      }
      return displayEnv;
    }

    private Vector spitOutListItems(String lName, DefaultListModel lModel, int forPE)
    {
          Vector items = null;
          try {
             outputArea.setText("");
             int nItems=getListLength(lName,forPE);
             String itemsString =stringList(lName,forPE,0,nItems);
             System.out.println("Cpd list "+lName+" contains "+nItems+" items: and string is "+ itemsString+"\n");
             //itms=stringList(lName,forPE,0,nItems);
             if (lName.equalsIgnoreCase("charm/arrayelements"))
               {
                 listItems = (Stringifiable)(new ArrayStringifiable(itemsString));
               }
             else
               {
                 listItems = new Stringifiable(itemsString);
               }
               listItems.populate();
               items = listItems.getNames();
               int l = items.size();
               String str = listItems.getWholeString();
               System.out.println("Cpd list "+lName+" contains "+nItems+"vector contains :"+l+"\n");
               int i=0;
               if (lModel != null)
               {
                  lModel.removeAllElements();
                  while (i < l) {
                     String tmp = (items.elementAt(i)).toString();
                     i++;
                     //System.out.println(i+":"+tmp+"\n");
                     lModel.addElement(tmp);
                   }
               }
          }
          catch (IOException e1) {
              System.out.println(e1.getMessage());
              e1.printStackTrace();
              System.exit(1);
          }
          return items;
    }


    //if parameter forSelectedPes <= 0, ccs message sent to all pes
    private void sendAppropriateMessage(String ccsHandlerName, String parameterName, int forSelectedPes)
    {
          if (forSelectedPes <= 0)
          {
            int noOfPes = 1;
            try
            {
              noOfPes = Integer.parseInt(numberPes);
            }
            catch(Exception E)
            {
              //abort("Couldn't parse number of pes");
              System.out.println("Ccs error: Couldn't parse number of pes");
              System.exit(1);
            }
            try
            {

                for (int indexPE=0; indexPE < noOfPes; indexPE++)
                {
                    sendCcsRequest(ccsHandlerName, parameterName, indexPE);
                }
            }
            catch (IOException ee) {
              ee.printStackTrace();
            }
          }
          else
          {
              try
              {
                for (int indexPE=0; indexPE < noOfPes; indexPE++)
                {
                   if (peList[indexPE] == true)
                   {
                      sendCcsRequest(ccsHandlerName, parameterName, indexPE);
                   }
                }
              }
              catch (IOException ee)
              {
                ee.printStackTrace();
              }
          }

    }

 
    public ParDebug() {

       noOfPes = 0; 
       isRunning = false;     

       setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       
       //Creating the menu
       menuBar = new JMenuBar();
       
       menuBar.add(menuFile = new JMenu("File"));
       menuFile.setMnemonic('F');
       
       menuFile.add(menuFileOpen = new JMenuItem("Open Program",'O'));
       menuFileOpen.setToolTipText("Open a parallel program to debug");
       menuFileOpen.setActionCommand("browse");
       menuFileOpen.addActionListener(this); 
       
       menuFile.add(menuFileParameters = new JMenuItem("Program Parameters",'P'));
       menuFileParameters.setToolTipText("Enter command-line parameters for the parallel program");
       menuFileParameters.setActionCommand("params");
       menuFileParameters.addActionListener(this); 
       
       JMenuItem menuFileExit;
       menuFile.add(menuFileExit = new JMenuItem("Exit Debugger",'X'));
       menuFileExit.setActionCommand("exitDebugger");
       menuFileExit.addActionListener(this); 
       
       menuBar.add(menuAction = new JMenu("Action"));
       menuAction.setMnemonic('A');
       
       menuActionStart = new JMenuItem("Start",'S');
       menuActionStart.setToolTipText("Start the parallel program"); 
       menuActionFreeze = new JMenuItem("Freeze",'F');
       menuActionFreeze.setToolTipText("Freeze the parallel program"); 
       menuActionContinue = new JMenuItem("Continue",'C');
       menuActionContinue.setToolTipText("Continue to run the parallel program"); 
       menuActionQuit = new JMenuItem("Quit",'Q');
       menuActionQuit.setToolTipText("Quit the parallel program"); 
       menuAction.add(menuActionStart);
       menuAction.add(menuActionFreeze);
       menuAction.add(menuActionContinue);
       menuAction.add(menuActionQuit);

       //Creating status bar on the top
       statusArea = new JTextField(60);
       statusArea.setBorder(BorderFactory.createTitledBorder("Status"));
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

       entryPointsPanel.add(entryScrollPane1);
       entryPointsPanel.add(Box.createRigidArea(new Dimension(0,10)));
       entryPointsPanel.add(entryScrollPane2);


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

       middlePanel.add(entryPointsPanel);
       middlePanel.add(Box.createRigidArea(new Dimension(15,0)));
       middlePanel.add(secondPanelWithOutput);
       middlePanel.setPreferredSize(new Dimension(600, 380));

       add(statusArea);
       add(Box.createRigidArea(new Dimension(0, 20)));
       add(middlePanel);
        
 
       // ************ BOTTOM PANEL *****************
       // Entity list on the left, details on the right
       JPanel panelForComboBoxes = new JPanel();
       panelForComboBoxes.setLayout(new BoxLayout(panelForComboBoxes, BoxLayout.X_AXIS));
       panelForComboBoxes.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("View Entities on PE"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        String [] displayStrings = {"viewable entities", "readonly variables", "readonly messages", "messages", "chare table entities", "entry table entities", "main table entities", "array elements", "messages in queue"};
        listsbox = new JComboBox(displayStrings);
        listsbox.setActionCommand("lists");
        listsbox.addActionListener(this);
        listsbox.setEnabled(false);
        listsbox.setPreferredSize(new Dimension(450,70));

        pesbox = new JComboBox();
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
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane secondScrollPane = new JScrollPane(outputArea);
        secondScrollPane.setBorder(BorderFactory.createTitledBorder("Details"));

        listScrollPane.setPreferredSize(new Dimension(200,230));
        secondScrollPane.setPreferredSize(new Dimension(400,230));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, secondScrollPane);
        splitPane.setOneTouchExpandable(true);
        
        panelForEntities.add(splitPane);
        add(panelForEntities);

        setStatusMessage(new String("File: " +filename+ "        number of pes: "+numberPes));
	
	if (filename!="" && numberPes!="") startProgram();
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
    
    public void setParametersForProgram(String commandLine, String peNumber, String portno)
    {
       numberPes = peNumber;
       clparams = commandLine;
       portnumber = portno;
    } 
    
    public void setStatusMessage (String txt)
    {
       statusArea.setText(txt);
    }

 
    public void actionPerformed(ActionEvent e) {
        String parameterName;
        int destPE = 0;
        if (e.getActionCommand().equals("browse")) {
	   String curDir=System.getProperty("user.dir");
	   // System.out.println("Opening chooser in "+curDir);
           JFileChooser chooser = new JFileChooser(curDir);
           int returnVal = chooser.showOpenDialog(ParDebug.this);
           if(returnVal == JFileChooser.APPROVE_OPTION) {
               parallelFile = chooser.getSelectedFile();
               filename = parallelFile.getAbsolutePath();
           }
           setStatusMessage(new String("File: " +filename+ "        number of pes: "+numberPes));
        }
        else if (e.getActionCommand().equals("params")) {
           ParamsDialog dialogbox = new ParamsDialog(appFrame, true, this);
           dialogbox.setLocationRelativeTo(appFrame);
           dialogbox.setFields(clparams, numberPes, portnumber);  
           dialogbox.pack();
           dialogbox.setVisible(true);
           setStatusMessage(new String("File: " +filename+ "        number of pes: "+numberPes));
           
        }
        else if (e.getActionCommand().equals("begin")) {
	   startProgram();
        }
        
        else if (e.getActionCommand().equals("lists") || e.getActionCommand().equals("changepe")) {
          int forPE=0; //See what processor 0 is doing
          String lName = listStrings[listsbox.getSelectedIndex()];
          if ((lName == "lists") || (lName == "localqueue")) lName = "converse/"+lName; 
          else lName="charm/"+lName;
          forPE = Integer.parseInt((String)pesbox.getSelectedItem());
          System.out.println("list name is "+lName+" for PROCESSOR " +forPE);
          spitOutListItems(lName, listModel, forPE); 
        }
        else if (e.getActionCommand().equals("startgdb")) {
           parameterName = "";
           sendAppropriateMessage("ccs_remove_all_break_points", parameterName,0);
           int noOfPes = 1;
           try 
           {
             noOfPes = Integer.parseInt(numberPes);
           
             for (int indexPE=0; indexPE < noOfPes; indexPE++)
             {
              if (peList[indexPE] == true) 
              {
        	 sendCcsRequest("ccs_debug_startgdb", parameterName,indexPE);
              }
  
             }
           }  
           catch(Exception E)
           {
             System.out.println("Ccs error: Couldn't parse number of pes");
             System.exit(1);
           }   
           setStatusMessage("Gdb started on selected pes");
        } 
        else if (e.getActionCommand().equals("freeze")) {
            
           parameterName = "freeze";
           continueButton.setEnabled(true);
           quitButton.setEnabled(true);
           freezeButton.setEnabled(false);
           sendAppropriateMessage("ccs_debug", parameterName,1);
           setStatusMessage("Program is frozen on selected pes");
        }
        else if (e.getActionCommand().equals("breakpoints")) {
           // set or remove breakpoints
           
           JCheckBox chkbox = (JCheckBox)e.getSource();
           String entryPointName = chkbox.getText(); 
           if (chkbox.isSelected())
           {
        	parameterName = entryPointName; 
        	sendAppropriateMessage("ccs_set_break_point", parameterName,0); 
        	continueButton.setEnabled(true);
        	freezeButton.setEnabled(false);
        	setStatusMessage ("Break Point set at entry point " +entryPointName); 
           }
           else
           {
        	parameterName = entryPointName; 
        	sendAppropriateMessage("ccs_remove_break_point", parameterName,0); 
        	continueButton.setEnabled(true);   
        	quitButton.setEnabled(true);
        	freezeButton.setEnabled(true);
        	listsbox.setEnabled(true);
        	pesbox.setEnabled(true);
        	setStatusMessage ("Break Point removed at entry point " +entryPointName+" on selected Pes"); 
           }

        } else if (e.getActionCommand().equals("unfreeze")){ 
           // start running again......
           parameterName = ""; 
           setStatusMessage("Program is running");
           sendAppropriateMessage("ccs_continue_break_point", parameterName,0); 
           continueButton.setEnabled(true);   
           quitButton.setEnabled(true);
           freezeButton.setEnabled(true);
           listsbox.setEnabled(true);
           pesbox.setEnabled(true);
        } 
        else if (e.getActionCommand().equals("pecheck")) {
           JCheckBox chkbox = (JCheckBox)e.getSource();
           String peText = chkbox.getText();
           String peno = peText.substring(3);
           if (chkbox.isSelected()) 
              peList[Integer.parseInt(peno)] = true;
           else
              peList[Integer.parseInt(peno)] = false;
        } 
        else if (e.getActionCommand().equals("quit")) {
          parameterName = "";
          sendAppropriateMessage("ccs_debug_quit", parameterName,0);
          quitProgram(); 
        }
	else if (e.getActionCommand().equals("exitDebugger")) {
	  quitProgram();
          System.exit(1);
	}
    } // end of actionPerformed
     
    public void valueChanged(ListSelectionEvent e) {
      
      if(e.getValueIsAdjusting()) return;
      
      JList theList = (JList)e.getSource();
      if (theList == listItemNames)
      {
        if (theList.isSelectionEmpty()) {
        }
        else {
         int index = theList.getSelectedIndex();
         if ((listStrings[listsbox.getSelectedIndex()]).equals("arrayelements"))
         {
              Vector itemNames = listItems.getNames();
              String tmp = (itemNames.elementAt(index)).toString();
              int forPE = Integer.parseInt((String)pesbox.getSelectedItem());
              try {
                String data = sendCcsRequest("ccs_examine_arrayelement",tmp, forPE);
                outputArea.setText("");  
                outputArea.setText(data);
                outputArea.scrollRectToVisible(new Rectangle(0,outputArea.getHeight()-2, 1, 1));
              }
              catch (IOException ee)
              {
                ee.printStackTrace();
              }
               
         }
         else
         {
            Vector itemValues = listItems.getValues();
            String tmp = (itemValues.elementAt(index)).toString();
            outputArea.setText("");  
            outputArea.setText(tmp);
            outputArea.scrollRectToVisible(new Rectangle(0,outputArea.getHeight()-2, 1, 1));
         } 
        }
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
           if(envDisplay == null)
           {
               System.out.println("Error> In retrieving IP address for DISPLAY variable");
               System.exit(1);
           }
           System.out.println(envDisplay);
          
           String totCommandLine = null;
           if (portnumber.length() == 0)
           {
              totCommandLine = charmrunPath + " " + "+p"+ numberPes + " " +executable + " " + clparams+"  +cpd +DebugDisplay " +envDisplay+" ++server";
           }
           else
           {
               totCommandLine = charmrunPath + " " + "+p"+ numberPes + " " +executable + " " + clparams+"  +cpd +DebugDisplay " +envDisplay+" ++server" + " ++server-port " + portnumber;
           }
           System.out.println(totCommandLine);
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
           long iter = 0;
           try
           {
             Thread.sleep(2000);
           }
           catch(Exception e1)
           {
             System.out.println("Could not sleep");
             System.exit(1);
           }
           while (servthread.portno == null)
           {
              if(iter++ > 100000) System.exit(1);
           }
           if (portnumber.length() == 0)
               portnumber = servthread.portno;
           System.out.println("*******Port number is " +portnumber);
           String[] ccsArgs=new String[2];
           ccsArgs[0]=hostname;
           ccsArgs[1]= portnumber;
           ccs = CcsServer.create(ccsArgs,false);
           noOfPes = 1;
           try
           {
             noOfPes = Integer.parseInt(numberPes);
           }
           catch(Exception E)
           {
             System.out.println("Ccs error: Couldn't parse number of pes");
             System.exit(1);
           }
           peList = new boolean[noOfPes]; 
           for (int i = 0; i < noOfPes; i++)
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
           pesbox.setActionCommand("changepe");
          
           startButton.setEnabled(false);
           continueButton.setEnabled(true);
           quitButton.setEnabled(false);
           freezeButton.setEnabled(false);
           startGdbButton.setEnabled(true); 
          
    
           String itemsString=null; 
           try {
             int nItems=getListLength("charm/entries",0);
             itemsString =stringList("charm/entries",0,0,nItems);
           }
           catch (IOException e1) {
              System.out.println(e1.getMessage());
              e1.printStackTrace();
              System.exit(1);
           }
           epItems = new EpStringifiable(itemsString);
           epItems.populate();
           
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
    }

    /// Exit the program.
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
            outputArea.setText(""); 
            listsbox.setEnabled(false);
            pesbox.removeAllItems(); 
            pesbox.setActionCommand("");
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
        numberPes = "1";
        clparams = "";
        envDisplay = "";

        // parsing command-line parameters
        int i = 0;
	boolean gotFilename=false;
        while (i < args.length)
        {
          if (args[i].equals("-h") || args[i].equals("--help"))
          {
             printUsage();
             System.exit(1);
          }
          if (args[i].equals("-host"))
             hostname = args[i+1];
          else if (args[i].equals("-port"))
            portnumber = args[i+1];
          else if (args[i].equals("-file"))
            filename = args[i+1];
          else if (args[i].equals("-param"))
            clparams = args[i+1];
          else if (args[i].equals("-pes") || args[i].equals("+p"))
            numberPes = args[i+1];
          else if (args[i].equals("-display"))
            envDisplay = args[i+1];
          else
          { /* Just a 1-argument */
	     if (args[i].startsWith("+p"))
	       numberPes=args[i].substring(2);
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
           
           
        appFrame = new JFrame("Charm Parallel Debugger");
        appFrame.setSize(1000, 1000);
        final ParDebug debugger = new ParDebug();

        appFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (debugger.isRunning)
                   {
                        debugger.sendAppropriateMessage("ccs_debug_quit", "",-1);
                   } 
                System.exit(0);
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
