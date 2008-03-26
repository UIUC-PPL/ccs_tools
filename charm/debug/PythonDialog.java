package charm.debug;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.*;
import java.util.Vector;

import charm.debug.pdata.CharePList;
import charm.debug.pdata.ChareInfo;
import charm.debug.pdata.ChareTypePList;
import charm.debug.pdata.EpInfo;
import charm.debug.pdata.EpPList;

public class PythonDialog extends JDialog
	implements ActionListener {

	private static final String beginning = "def method(self):";

	private JComboBox chare;
	private JTextArea input;
	private JScrollPane inputScrollPane;
	private JScrollPane entryScrollPane;
	private JButton confirm;
	private JButton cancel;
	private boolean confirmed;
	private String parsedString;
	private GdbProcess info;

	public PythonDialog(Frame parent, boolean modal, CharePList chareItems, ChareTypePList chareTypeItems, GdbProcess gdb, CpdUtil server) {
		super(parent, "Python script", modal);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		confirmed = false;
		parsedString = null;
		info = gdb;

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
		input.setText(beginning+"\n\t");
		input.setTabSize(4);
		input.setToolTipText("Write your python code here");
		inputScrollPane = new JScrollPane(input);

		//topLeft.setBottomComponent(inputScrollPane);
		topLeft.add(inputScrollPane);
		top.setLeftComponent(topLeft);
		
		entryScrollPane = new JScrollPane();
		
		/* Create the entities lists */
		int nItems=server.getListLength("charm/entries",0);
		EpPList epItems = new EpPList();
		epItems.setLookups(chareTypeItems);
		epItems.load(server.getPList("charm/entries",0,0,nItems));
		
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
			chkbox.setActionCommand("breakpoints"); 
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
		
		pack();
		setVisible(true);
	}

	public boolean confirmed() {
		return confirmed;
	}

	public String getText() {
		return parsedString;
	}

	public int getChareGroup() {
		return ((ChareInfo)chare.getSelectedItem()).getGroupID();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ok")) {
			if (chare.getSelectedIndex() == 0) {
				JOptionPane.showMessageDialog(this, "Must select an object on which to execute the script", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			parsedString = input.getText();
			if (!parsedString.startsWith(beginning)) {
				JOptionPane.showMessageDialog(this, "The code must start with '"+beginning+"'", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// parse the code, and make the appropriate corrections
			int first = 0, last = 0;
			while ((first = parsedString.indexOf("charm.get", first)) != -1) {
				first += 9;
				int startArgs = parsedString.indexOf('(', first);
				if (parsedString.startsWith("Static",first)) {
					int splitPoint = parsedString.indexOf(")",startArgs);
					String name = parsedString.substring(startArgs+1,splitPoint).trim();
					System.out.println("Static name = "+name);
					String address = info.infoCommand("print &"+name.substring(1,name.length()-1)+"\n");
					int startAddress = address.indexOf("0x");
					if (startAddress == -1) {
						JOptionPane.showMessageDialog(this, "Static variable "+name+" not found", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					System.out.println("Reply: "+address);
					char retType = 'p';
					if (address.indexOf("(int *)") != -1) retType = 'i';
					else if (address.indexOf("(char *)") != -1) retType = 'b';
					else if (address.indexOf("(short *)") != -1) retType = 'h';
					else if (address.indexOf("(long *)") != -1) retType = 'l';
					else if (address.indexOf("(float *)") != -1) retType = 'f';
					else if (address.indexOf("(double *)") != -1) retType = 'd';
					else if (address.indexOf("(char **)") != -1) retType = 's';
					parsedString = parsedString.substring(0,splitPoint)+
						","+address.substring(startAddress).trim()+",'"+retType+"'"+
						parsedString.substring(splitPoint);
				} else {
					last = parsedString.indexOf(',', startArgs);
					String type = parsedString.substring(last+1,parsedString.indexOf(',',last+1)).trim();
					System.out.println("get type "+type+" (last="+last+")");
					last = parsedString.indexOf(',',last+1);
					if (parsedString.startsWith("Array",first)) {
						
					} else if (parsedString.startsWith("Value",first)) {
						
					} else if (parsedString.startsWith("Cast",first)) {
						
					}
				}
			}

			confirmed = true;
			setVisible(false);
		}
		else if (e.getActionCommand().equals("cancel")) {
			confirmed = false;
			parsedString = null;
			setVisible(false);
		}
	}
}
