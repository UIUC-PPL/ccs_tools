
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
    CcsServer ccs;
    
    private static String filename, hostname, portnumber, numberPes, clparams, envDisplay;
    
    private DefaultListModel listModel;
    private Stringifiable listItems = null;
    private EpStringifiable epItems = null;
    private JPanel peActualPanel, sysEpsActualPanel, userEpsActualPanel;
    private String[] listStrings =  {"lists","readonly", "readonlyMsg", "messages", "chares", "entries", "mains", "arrayelements", "localqueue", "schedqueue"};
    private int noOfPes;
    private boolean isRunning = false;
    private boolean[] peList = null;
     
    private static JFrame appFrame = null;

    protected JButton startButton, continuebutton, quitbutton, freezebutton,   startgdb;
    protected JTextArea programOutputArea, outputArea;
    protected JTextField statusArea;
    protected File parallelFile = null;
    protected JComboBox listsbox, pesbox;
    protected JList listItemNames;
    
 
    protected JMenuBar menuBar;
    protected JMenu menuFile, menuAction;
    protected JMenuItem menuFileOpen, menuFileParameters;
    protected JMenuItem menuActionStart, menuActionContinue, menuActionQuit, menuActionFreeze;
    
    
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

    //Convert a set of CpdList items into a string
    //private Stringifiable stringList(String listName,int forPE,int lo,int hiPlusOne) throws IOException
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
      //return new Stringifiable(resp);
      return new String(resp);
    }

    private String sendCcsRequest(String ccsHandlerName, String parameterName, int destPE) throws IOException
    {
      //Build a byte array describing the ccs request:
      int reqStr=parameterName.length();
      int reqLen = reqStr+1;
      byte[] req=new byte[reqLen];
      CcsServer.writeString(req,0,reqStr+1,parameterName);
      CcsServer.Request r=ccs.sendRequest(ccsHandlerName,destPE,req);
      //if ( parameterName.equalsIgnoreCase("freeze") || ccsHandlerName.equalsIgnoreCase("ccs_debug_quit") || ccsHandlerName.equalsIgnoreCase("ccs_remove_all_break_points") || ccsHandlerName.equalsIgnoreCase("ccs_set_break_point") || ccsHandlerName.equalsIgnoreCase("ccs_remove_break_point") || ccsHandlerName.equalsIgnoreCase("ccs_debug_startgdb") )
      if ( parameterName.equalsIgnoreCase("freeze") || ccsHandlerName.equalsIgnoreCase("ccs_debug_quit") || ccsHandlerName.equalsIgnoreCase("ccs_remove_all_break_points") || ccsHandlerName.equalsIgnoreCase("ccs_set_break_point") || ccsHandlerName.equalsIgnoreCase("ccs_remove_break_point") || ccsHandlerName.equalsIgnoreCase("ccs_continue_break_point"))
      {
        // System.out.println("freeze .. so continue");
        return null;
      }
      else {
         byte[] resp=ccs.recvResponse(r);
         return new String(resp);
      }
    }

    public void displayProgramOutput (String line)
    {
       programOutputArea.append(line);
       programOutputArea.scrollRectToVisible(new Rectangle(0,programOutputArea.getHeight()-2, 1, 1));
    } 
    
    public ParDebug() {

        noOfPes = 0; 
        isRunning = false;     
        // Three panels
        // 1. for starting parallel program
        // 2. Control buttons
        // 3. Display and related controls

        //setLayout(new GridLayout(3,1));
       setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

       
       //Creating the menu
       menuBar = new JMenuBar();
       
       menuFile = new JMenu("File");
       menuFile.setMnemonic('F');
       menuFileOpen = new JMenuItem();
       menuFileOpen.setText("Open");
       menuFileOpen.setMnemonic('O');
       menuFileOpen.setToolTipText("Open a parallel program to debug");
       menuFileOpen.setActionCommand("browse");
       menuFileOpen.addActionListener(this); 
       menuFileParameters = new JMenuItem();
       menuFileParameters.setText("Parameters (Command-line etc)");
       menuFileParameters.setMnemonic('P');
       menuFileParameters.setToolTipText("Enter command-line parameters for the parallel program");
       menuFileParameters.setActionCommand("params");
       menuFileParameters.addActionListener(this); 
       menuFile.add(menuFileOpen);
       menuFile.add(menuFileParameters);
       
       menuAction = new JMenu("Action");
       menuAction.setMnemonic('A');
       menuActionStart = new JMenuItem();
       menuActionStart.setText("Start");
       menuActionStart.setMnemonic('S');
       menuActionStart.setToolTipText("Start the parallel program"); 
       menuActionFreeze = new JMenuItem();
       menuActionFreeze.setText("Freeze");
       menuActionFreeze.setMnemonic('F');
       menuActionFreeze.setToolTipText("Freeze the parallel program"); 
       menuActionContinue = new JMenuItem();
       menuActionContinue.setText("Continue");
       menuActionContinue.setMnemonic('C');
       menuActionContinue.setToolTipText("Continue to run the parallel program"); 
       menuActionQuit = new JMenuItem();
       menuActionQuit.setText("Quit");
       menuActionQuit.setMnemonic('Q');
       menuActionQuit.setToolTipText("Quit the parallel program"); 
       menuAction.add(menuActionStart);
       menuAction.add(menuActionFreeze);
       menuAction.add(menuActionContinue);
       menuAction.add(menuActionQuit);
       menuBar.add(menuFile);
       menuBar.add(menuAction);

       statusArea = new JTextField(60);
       statusArea.setBorder(BorderFactory.createTitledBorder("Status"));
       statusArea.setEditable(false);
       statusArea.setBackground(Color.lightGray); 

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
       startButton.setToolTipText("Click this button to run the parallel program.");
       startButton.addActionListener(this);
       startButton.setPreferredSize(new Dimension(100,80));

       continuebutton = new JButton("Continue");
       continuebutton.setVerticalTextPosition(AbstractButton.BOTTOM);
       continuebutton.setHorizontalTextPosition(AbstractButton.CENTER);
       continuebutton.setMnemonic(KeyEvent.VK_U);
       continuebutton.setActionCommand("unfreeze");
       continuebutton.setEnabled(false);
       continuebutton.setToolTipText("Click this button to resume action.");
       continuebutton.addActionListener(this);
       continuebutton.setPreferredSize(new Dimension(100,80));


        freezebutton = new JButton("Freeze");
        freezebutton.setVerticalTextPosition(AbstractButton.BOTTOM);
        freezebutton.setHorizontalTextPosition(AbstractButton.CENTER);
        freezebutton.setActionCommand("freeze");
        freezebutton.setEnabled(false);
        freezebutton.setToolTipText("Click this button to freeze action.");
        freezebutton.addActionListener(this);
        freezebutton.setPreferredSize(new Dimension(100,80));

        
        quitbutton = new JButton("Quit");
        quitbutton.setVerticalTextPosition(AbstractButton.BOTTOM);
        quitbutton.setHorizontalTextPosition(AbstractButton.CENTER);
        quitbutton.setMnemonic(KeyEvent.VK_Q);
        quitbutton.setActionCommand("quit");
        quitbutton.setEnabled(false);
        quitbutton.setToolTipText("Click this button to quit the program.");
        quitbutton.addActionListener(this);
        quitbutton.setPreferredSize(new Dimension(100,80));

        
        startgdb = new JButton("Start GDB");
        startgdb.setVerticalTextPosition(AbstractButton.BOTTOM);
        startgdb.setHorizontalTextPosition(AbstractButton.CENTER);
        startgdb.setActionCommand("startgdb");
        startgdb.setEnabled(false);
        startgdb.setToolTipText("Click this button to start gdb.");
        startgdb.addActionListener(this);
        startgdb.setPreferredSize(new Dimension(100,80));


        buttonPanel.add(startButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(continuebutton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(freezebutton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(quitbutton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(startgdb);

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
        
        JPanel panelForComboBoxes = new JPanel();
        panelForComboBoxes.setLayout(new BoxLayout(panelForComboBoxes, BoxLayout.X_AXIS));
        panelForComboBoxes.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("View Entities on PE"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        String [] displayStrings = {"viewable entities", "readonly variables", "readonly messages", "messages", "chare table entities", "entry table entities", "main table entities", "array elements", "messages in local queue", "messages in scheduler queue"};
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
 
    public void actionPerformed(ActionEvent e) {
        String parameterName;
        int destPE = 0;
        if (e.getActionCommand().equals("browse")) {
           JFileChooser chooser = new JFileChooser();
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
           //System.out.println("in begin!!!");
           isRunning = true;
           programOutputArea.setText("");
           String executable = filename;
           String charmrunPath = (new File(executable)).getParent() + "/charmrun";
           //if(numberPes.length() == 0) numberPes = "1";
           //String envDisplay = getEnvDisplay();
           if (envDisplay.length() == 0) envDisplay = getEnvDisplay();
           if(envDisplay == null)
           {
               System.out.println("Error> In retrieving IP address for DISPLAY variable");
               System.exit(1);
           }
           System.out.println(envDisplay);
          
           String totCommandLine = null;
           //String totCommandLine = charmrunPath + " " + "+p"+ numberPes + " " +executable + " " + args+"  +cpd +DebugDisplay \"128.174.241.75:0.0\" ++server" + " ++server-port " + portnumber;
           if (portnumber.length() == 0)
           {
              //totCommandLine = charmrunPath + " " + "+p"+ numberPes + " " +executable + " " + clparams+"  +cpd +DebugDisplay \"128.174.241.77:0.0\" ++server";
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
                //p = runtime.exec("../server/charmrun +p1 ../server/cfdAMR 2 ++server ++server-port 1234");
                p = runtime.exec(totCommandLine);
           }
           catch (Exception exc) {
                 System.out.println("error in ParDebug. Exception caught");
                 cleanUpAndStart();
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
           //System.out.println("port number ="+portnumber);
           ccs = CcsServer.create(ccsArgs,true);
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
           continuebutton.setEnabled(true);
           quitbutton.setEnabled(false);
           freezebutton.setEnabled(false);
           startgdb.setEnabled(true); 
          
    
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
           //spitOutListItems("charm/entries", null, 0); 
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
        
        else if (e.getActionCommand().equals("lists") || e.getActionCommand().equals("changepe")) {
          int forPE=0; //See what processor 0 is doing
          String lName = listStrings[listsbox.getSelectedIndex()];
          if ((lName == "lists") || (lName == "localqueue") || (lName == "schedqueue")) lName = "converse/"+lName; 
          else lName="charm/"+lName;
          forPE = Integer.parseInt((String)pesbox.getSelectedItem());
          System.out.println("list name is "+lName+" for PROCESSOR " +forPE);
          spitOutListItems(lName, listModel, forPE); 
        }
        else {
          if (e.getActionCommand().equals("startgdb")) {
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
               /*for (int indexPE=0; indexPE < noOfPes; indexPE++)
               {
                if (peList[indexPE] == false) 
                {
                   sendCcsRequest("ccs_continue_break_point", parameterName,indexPE);
                }
  
               }*/
             }  
             catch(Exception E)
             {
               System.out.println("Ccs error: Couldn't parse number of pes");
               System.exit(1);
             }   
             //sendAppropriateMessage("ccs_debug_startgdb", parameterName,1);
             //sendAppropriateMessage("ccs_continue_break_point", parameterName,0);
             setStatusMessage("Gdb started on selected pes");
          } 
          if (e.getActionCommand().equals("freeze")) {
              
             parameterName = "freeze";
             continuebutton.setEnabled(true);
             quitbutton.setEnabled(true);
             freezebutton.setEnabled(false);
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
                  continuebutton.setEnabled(true);
                  freezebutton.setEnabled(false);
                  setStatusMessage ("Break Point set at entry point " +entryPointName); 
             }
             else
             {
                  parameterName = entryPointName; 
                  sendAppropriateMessage("ccs_remove_break_point", parameterName,0); 
                  continuebutton.setEnabled(true);   
                  quitbutton.setEnabled(true);
                  freezebutton.setEnabled(true);
                  listsbox.setEnabled(true);
                  pesbox.setEnabled(true);
                  setStatusMessage ("Break Point removed at entry point " +entryPointName+" on selected Pes"); 
             }

          } else if (e.getActionCommand().equals("unfreeze")){ 
             // start running again......
             parameterName = ""; 
             setStatusMessage("Program is running");
             sendAppropriateMessage("ccs_continue_break_point", parameterName,0); 
             continuebutton.setEnabled(true);   
             quitbutton.setEnabled(true);
             freezebutton.setEnabled(true);
             listsbox.setEnabled(true);
             pesbox.setEnabled(true);
             //setStatusMessage("Program is running");
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
            cleanUpAndStart(); 
          }
       }//end of else
    }


    public void cleanUpAndStart()
    {
            portnumber = "";
            filename = "";
            clparams = "";
            numberPes = "1";
            isRunning = false;
            startButton.setEnabled(true);
            continuebutton.setEnabled(false); 
            quitbutton.setEnabled(false);
            freezebutton.setEnabled(false);
            startgdb.setEnabled(false);
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

    private Vector spitOutListItems(String lName, DefaultListModel lModel, int forPE)
    {
          Vector items = null;
          try {
             outputArea.setText("");  
             int nItems=getListLength(lName,forPE);
             String itemsString =stringList(lName,forPE,0,nItems);
             //System.out.println("Cpd list "+lName+" contains "+nItems+" items:\n");
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

        int i = 0;
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
          else if (args[i].equals("-pes"))
            numberPes = args[i+1];
          else if (args[i].equals("-display"))
            envDisplay = args[i+1];
          else
          {
             printUsage();
             System.exit(1);
          }
          i = i+2;
        } 
        if (i>args.length)
        {
             printUsage();
             System.exit(1);
        }
           
           
        //System.out.println("filename =" + filename + " pes ="+ numberPes + " parameters = " +clparams+ " host= " +hostname+ " port= " + portnumber);
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

        //frame.getContentPane().add(new ParDebug(), BorderLayout.CENTER);
        appFrame.getContentPane().add(debugger, BorderLayout.CENTER);

        
        Rectangle bounds = (appFrame.getGraphicsConfiguration()).getBounds(); 
        appFrame.setLocation(50 +bounds.x, 50 + bounds.y);
        appFrame.pack();
        appFrame.setVisible(true);
        appFrame.setJMenuBar(debugger.menuBar);
    }
}
