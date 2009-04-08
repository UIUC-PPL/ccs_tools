package charm.debug;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.*;
import java.io.*;
import java.text.ParseException;
import java.util.Vector;

import charm.debug.inspect.DataType;
import charm.debug.inspect.GenericType;
import charm.debug.inspect.Inspector;
import charm.debug.inspect.VariableElement;
import charm.debug.pdata.CharePList;
import charm.debug.pdata.ChareInfo;
import charm.debug.pdata.ChareTypePList;
import charm.debug.pdata.EpInfo;
import charm.debug.pdata.EpPList;
import charm.debug.preference.PyFilter;

public class PythonDialog extends JDialog
	implements ActionListener {

	private ParDebug parent;
	private JMenuBar menuBar;
	private JMenu menuFile;
	private JMenuItem menuOpen;
	private JMenuItem menuSave;
	private JMenu menuRecent;
	private JComboBox chare;
	private JTextArea input;
	private JScrollPane inputScrollPane;
	private JScrollPane entryScrollPane;
	private JButton confirm;
	private JButton cancel;
	PythonScript script;
	
	public PythonDialog(ParDebug parent, boolean modal, CharePList chareItems, ChareTypePList chareTypeItems, GdbProcess gdb, CpdUtil server) {
		super((Frame)null, "Python script", modal);
		this.parent = parent;
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		script = new PythonScript(gdb);
		
		getContentPane().setLayout(new BorderLayout());

		JSplitPane top = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		top.setResizeWeight(0.5);
		
		//JSplitPane topLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		//topLeft.setResizeWeight(0);
		JPanel topLeft = new JPanel();
		//topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));
		topLeft.setLayout(new BorderLayout());
		
		chare = new JComboBox();
		chare.addItem("Select one...");
		int i=0;
		ChareInfo type;
		while ((type = chareItems.elementAt(i)) != null) {
			chare.addItem(type);
			++i;
		}
		//topLeft.setTopComponent(chare);
		topLeft.add(chare, BorderLayout.NORTH);

		input = new JTextArea("", 20, 40);
		input.setText(PythonScript.beginning+"\n\t");
		input.setTabSize(4);
		input.setToolTipText("Write your python code here");
		inputScrollPane = new JScrollPane(input);

		//topLeft.setBottomComponent(inputScrollPane);
		topLeft.add(inputScrollPane);
		top.setLeftComponent(topLeft);
		
		entryScrollPane = new JScrollPane();
		
		/* Create the entities lists */
		//int nItems=server.getListLength("charm/entries",0);
		EpPList epItems = parent.getEpItems();
		//epItems.setLookups(chareTypeItems);
		//epItems.load(server.getPList("charm/entries",0,0,nItems));
		
		
		DefaultMutableTreeNode[] chareRoots = new DefaultMutableTreeNode[chareTypeItems.size()];
		Vector items;
		items = epItems.getUserEps();
		int l = items.size();
		i = 0;
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		DefaultMutableTreeNode userRoot = new DefaultMutableTreeNode("User Entries");
		for (i=0; i < l; ++i)
		{
			//String tmp = (items.elementAt(i)).toString();
			//JCheckBox chkbox = new JCheckBox(tmp);
			EpCheckBox chkbox = new EpCheckBox((EpInfo)items.elementAt(i));
			chkbox.addActionListener(this);
			chkbox.setActionCommand("entry"); 
			//userEpsActualPanel.add(chkbox);
			//userRoot.add(new EpTreeCheckBox(chkbox));
			int chareType =((EpInfo)items.elementAt(i)).getChareType();
			if (chareRoots[chareType] == null) {
				chareRoots[chareType] = new DefaultMutableTreeNode(chareTypeItems.elementAt(chareType).getType());
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
			chkbox.setActionCommand("entry"); 
			//sysEpsActualPanel.add(chkbox);
			//sysRoot.add(new EpTreeCheckBox(chkbox));
			int chareType =((EpInfo)items.elementAt(i)).getChareType(); 
			if (chareRoots[chareType] == null) {
				chareRoots[chareType] = new DefaultMutableTreeNode(chareTypeItems.elementAt(chareType).getType());
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
		
		top.setRightComponent(entryScrollPane);
		getContentPane().add(top);
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

		buttons.add(Box.createHorizontalGlue());
		confirm = new JButton("Send");
		confirm.setActionCommand("ok");
		confirm.addActionListener(this);
		confirm.setToolTipText("Execute the python script to the application");
		buttons.add(confirm);
		buttons.add(Box.createRigidArea(new Dimension(30, 1)));
		
		cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		buttons.add(cancel);
		buttons.add(Box.createHorizontalGlue());
		
		getContentPane().add(buttons, BorderLayout.SOUTH);
		
		// Create the menu
		menuBar = new JMenuBar();
		menuBar.add(menuFile = new JMenu("File"));
		menuFile.setMnemonic('F');
		menuFile.add(menuOpen = new JMenuItem("Open",'O'));
		menuOpen.setActionCommand("open");
		menuOpen.addActionListener(this);
		menuFile.add(menuSave = new JMenuItem("Save...",'S'));
		menuSave.setActionCommand("save");
		menuSave.addActionListener(this);
		menuFile.add(menuRecent = new JMenu("Recent codes"));
		updateRecentConfig();

		setJMenuBar(menuBar);
		pack();
		setVisible(true);
	}

	private void updateRecentConfig() {
		Object []files = parent.getPreferences().getRecentPython();
		menuRecent.removeAll();
		//System.out.println("updateRecentConfig: "+files.length);
		if (files.length > 0) {
			menuRecent.setEnabled(true);
			JMenuItem item;
			for (int i=0; i<files.length; ++i) {
				//System.out.println("updateRecentConfig: "+files[i]);
				item = new JMenuItem((String)files[i]);
			    item.setActionCommand("openRecent");
			    item.addActionListener(this);
				menuRecent.add(item);
			}
		} else {
			menuRecent.setEnabled(false);
		}
    }

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ok")) {
			if (chare.getSelectedIndex() == 0 && script.getSelectedEPs()[0].size() == 0 && script.getSelectedEPs()[1].size() == 0) {
				JOptionPane.showMessageDialog(this, "Must either select an object on which to execute the script, or select entry methods on which to install the script", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (chare.getSelectedIndex() != 0) {
				script.setChare((ChareInfo)chare.getSelectedItem());
			}
			
			try {
				script.parseCode(input.getText());
			} catch (ParseException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
			
			parent.getPreferences().addRecentPython(input.getText());
			setVisible(false);
			parent.executePython(script);
		}
		else if (e.getActionCommand().equals("cancel")) {
			setVisible(false);
		} else if (e.getActionCommand().equals("entry")) {
			EpCheckBox chkbox = (EpCheckBox)e.getSource();
			int click = chkbox.click();
			if (click > 0) script.addEP(click-1, chkbox.ep);
			else script.removeEP(-click-1, chkbox.ep);
		} else if (e.getActionCommand().equals("openRecent")) {
	   		JMenuItem item = (JMenuItem)e.getSource();
    		input.setText(item.getText());
		} else if (e.getActionCommand().equals("open")) {
    		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
    		chooser.addChoosableFileFilter(new PyFilter());
    		chooser.setAcceptAllFileFilterUsed(false);
    		int returnVal = chooser.showOpenDialog(this);
    		if(returnVal == JFileChooser.APPROVE_OPTION) {
    			loadPythonCode(chooser.getSelectedFile());
    		}
		} else if (e.getActionCommand().equals("save")) {
    		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
    		chooser.addChoosableFileFilter(new PyFilter());
    		chooser.setAcceptAllFileFilterUsed(false);
    		int returnVal = chooser.showSaveDialog(this);
    		if(returnVal == JFileChooser.APPROVE_OPTION) {
    			File filename = chooser.getSelectedFile();
    			if (!filename.getName().endsWith(".py")) {
    				filename = new File(filename.getAbsolutePath()+".py");
    			}
    			if (filename.exists()) {
    				int response;
    				response = JOptionPane.showConfirmDialog(this, "Do you want to overwrite the file?", "File overwrite", JOptionPane.YES_NO_OPTION);
    				if (response != JOptionPane.YES_OPTION) {
    					//setStatusMessage("Save aborted");
    					return;
    				}
    			}
    			savePythonCode(filename);
    		}	
		}
	}

	boolean loadPythonCode(File f) {
		try {
			input.setText(script.loadPythonCode(f));
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Unable to load file '"+f.getName()+"'", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	boolean savePythonCode(File f) {
		try {
			script.savePythonCode(f, input.getText());
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Unable to save file '"+f.getName()+"'", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}
