package charm.debug;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import charm.debug.preference.Execution;

public class ParamsDialog extends JDialog implements ActionListener, ChangeListener {

	//ParDebug mainObject = null;
	Execution exec = null;

	private JTextField  clParams, numPes, portno, sshport, hostname, username, filename, dir, inputFile, virtualPes, recordPes, replayPe;
	private JCheckBox suspendOnStart, sshTunnel, waitFile, virtualDebug, recplayActive, recplayDetail, recplayChecksum;
	private JButton chooser, dirchooser;
	private JLabel virtualPeslabel;
	private JRadioButton record, replay, recordDetail, replayDetail, checksumXOR, checksumCRC;
	private JPanel recplayEnabled, fullEnabled, checksumEnabled;

	public ParamsDialog(Frame parent, boolean modal, Execution obj) {
		super (parent, modal);
		exec = obj;
		initComponents();
		pack();
		setResizable(false);
	}

	/*
	public void setFields(String params, String pes, String pno, String host, String user, boolean ssh) {
		clParams.setText(params);
		numPes.setText(pes);
		portno.setText(pno);
		hostname.setText(host);
		username.setText(user);
		sshTunnel.setSelected(ssh);
	}
	*/

	private void initComponents() {

		setTitle("Program Parameters");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		JPanel contents = new JPanel();
		int nextLine=0;

		GridBagLayout grid = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		contents.setLayout(grid);

		c.gridx = 0;
		c.gridy = nextLine;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,12,0,0);
		JLabel filenamelabel = new JLabel("Executable:");
		filenamelabel.setLabelFor(filename);
		grid.setConstraints(filenamelabel, c);
		contents.add(filenamelabel);
		
		filename = new JTextField(35);
		filename.setActionCommand("cmd.ok");
		filename.addActionListener(this);
		c.gridx = 1;
		c.gridy = nextLine;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.insets = new Insets(11,7,0,0);
		grid.setConstraints(filename, c);
		
		chooser = new JButton("Change");
		chooser.setActionCommand("browse");
		chooser.addActionListener(this);
		c.gridx = 3;
		c.gridy = nextLine;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.EAST;
		c.insets = new Insets(12,5,0,10);
		grid.setConstraints(chooser, c);
		
		c.gridx = 0;
		c.gridy = ++nextLine;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,12,0,0);
		JLabel dirlabel = new JLabel("Working dir:");
		dirlabel.setLabelFor(dir);
		grid.setConstraints(dirlabel, c);
		contents.add(dirlabel);
		
		dir = new JTextField(35);
		dir.setActionCommand("cmd.ok");
		dir.addActionListener(this);
		c.gridx = 1;
		c.gridy = nextLine;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.insets = new Insets(11,7,0,0);
		grid.setConstraints(dir, c);
		
		dirchooser = new JButton("Change");
		dirchooser.setActionCommand("browsedir");
		dirchooser.addActionListener(this);
		c.gridx = 3;
		c.gridy = nextLine;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.EAST;
		c.insets = new Insets(12,5,0,10);
		grid.setConstraints(dirchooser, c);
		
		c.gridx = 0;
		c.gridy = ++nextLine;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,12,0,0);
		JLabel paramlabel = new JLabel();
		paramlabel.setText("Command Line Parameters:");
		paramlabel.setLabelFor(clParams);
		grid.setConstraints(paramlabel, c);
		contents.add(paramlabel);

		clParams = new JTextField(35);
		clParams.setActionCommand("cmd.ok");
		clParams.addActionListener(this);
		c.gridx = 1;
		c.gridy = nextLine;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.insets = new Insets(11,7,0,0);
		grid.setConstraints(clParams,c); 

		c.gridx = 0;
		c.gridy = ++nextLine;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,12,0,0);
		JLabel peslabel = new JLabel();
		peslabel.setText("Number of Processors:");
		peslabel.setLabelFor(numPes);
		grid.setConstraints(peslabel, c);
		contents.add(peslabel);

		numPes = new JTextField(5);
		numPes.setActionCommand("cmd.ok");
		numPes.addActionListener(this);
		c.gridx = 1;
		c.gridy = nextLine;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,8,0,0);
		grid.setConstraints(numPes,c); 

		c.gridx = 1;
		c.gridy = ++nextLine;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0,4,0,0);
		suspendOnStart = new JCheckBox("Suspend execution at startup");
		grid.setConstraints(suspendOnStart, c);
		
		c.gridx = 0;
		c.gridy = ++nextLine;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,8,0,0);
		virtualDebug = new JCheckBox("Virtualized debugging:");
		virtualDebug.addChangeListener(this);
		grid.setConstraints(virtualDebug, c);
		contents.add(virtualDebug);

		c.gridx = 1;
		c.gridy = nextLine;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,8,0,0);
		virtualPeslabel = new JLabel();
		virtualPeslabel.setText("Number of Virtal Processors:");
		virtualPeslabel.setForeground(Color.GRAY);
		virtualPeslabel.setLabelFor(virtualPes);
		grid.setConstraints(virtualPeslabel, c);
		contents.add(virtualPeslabel);

		c.gridx = 2;
		c.gridy = nextLine;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,8,0,0);
		virtualPes = new JTextField(5);
		virtualPes.setEnabled(false);
		virtualPes.setActionCommand("cmd.ok");
		virtualPes.addActionListener(this);
		grid.setConstraints(virtualPes, c);
		contents.add(virtualPes);

		c.gridx = 0;
		c.gridy = ++nextLine;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,12,0,0);
		JLabel portNumberLabel = new JLabel();
		portNumberLabel.setText("Port Number:");
		portNumberLabel.setLabelFor(portno);
		grid.setConstraints(portNumberLabel, c);
		contents.add(portNumberLabel);

		c.gridx = 1;
		c.gridy = nextLine;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,8,0,0);
		portno = new JTextField(5);
		portno.setActionCommand("cmd.ok");
		portno.addActionListener(this);
		grid.setConstraints(portno, c);
		//contents.add(portno);

		c.gridx = 0;
		c.gridy = ++nextLine;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,12,0,0);
		JLabel sshportNameLabel = new JLabel();
		sshportNameLabel.setText("SSH port number:");
		sshportNameLabel.setLabelFor(sshport);
		grid.setConstraints(sshportNameLabel, c);
		contents.add(sshportNameLabel);

		c.gridx = 1;
		c.gridy = nextLine;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,8,0,0);
		sshport = new JTextField(5);
		sshport.setActionCommand("cmd.ok");
		sshport.addActionListener(this);
		grid.setConstraints(sshport, c);

		c.gridx = 0;
		c.gridy = ++nextLine;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,12,0,0);
		JLabel hostNameLabel = new JLabel();
		hostNameLabel.setText("Host name:");
		hostNameLabel.setLabelFor(hostname);
		grid.setConstraints(hostNameLabel, c);
		contents.add(hostNameLabel);

		c.gridx = 1;
		c.gridy = nextLine;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,8,0,0);
		hostname = new JTextField(20);
		hostname.setActionCommand("cmd.ok");
		hostname.addActionListener(this);
		grid.setConstraints(hostname, c);

		c.gridx = 0;
		c.gridy = ++nextLine;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(6,12,0,0);
		JLabel userNameLabel = new JLabel();
		userNameLabel.setText("Username:");
		userNameLabel.setLabelFor(username);
		grid.setConstraints(userNameLabel, c);
		contents.add(userNameLabel);

		c.gridx = 1;
		c.gridy = nextLine;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(6,8,0,0);
		username = new JTextField(15);
		username.setActionCommand("cmd.ok");
		username.addActionListener(this);
		grid.setConstraints(username, c);

		c.gridx = 0;
		c.gridy = ++nextLine;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(6,12,0,0);
		JLabel inputFileLabel = new JLabel();
		inputFileLabel.setText("Input file:");
		inputFileLabel.setLabelFor(inputFile);
		grid.setConstraints(inputFileLabel, c);
		contents.add(inputFileLabel);
		
		c.gridx = 1;
		c.gridy = nextLine;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(6,8,0,0);
		inputFile = new JTextField(15);
		inputFile.setActionCommand("cmd.ok");
		inputFile.addActionListener(this);
		grid.setConstraints(inputFile, c);
		
		c.gridx = 1;
		c.gridy = ++nextLine;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0,4,0,0);
		waitFile = new JCheckBox("Wait for file to be created");
		grid.setConstraints(waitFile, c);
		
		c.gridx = 1;
		c.gridy = ++nextLine;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0,4,0,0);
		sshTunnel = new JCheckBox("Use ssh tunneling");
		grid.setConstraints(sshTunnel, c);
		
		contents.add(filename);
		contents.add(chooser);
		contents.add(dir);
		contents.add(dirchooser);
		contents.add(clParams);
		contents.add(numPes);
		contents.add(portno);
		contents.add(sshport);
		contents.add(hostname);
		contents.add(username);
		contents.add(inputFile);
		contents.add(waitFile);
		contents.add(sshTunnel);
		contents.add(suspendOnStart);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, 0));
		JButton okButton = new JButton();
		okButton.setText("OK");
		okButton.setActionCommand("cmd.ok");
		okButton.addActionListener(this);
		buttonPanel.add(okButton);

		// space
		buttonPanel.add(Box.createRigidArea(new Dimension(5,0)));

		// cancel button
		JButton cancelButton = new JButton();
		cancelButton.setText("CANCEL");
		cancelButton.setActionCommand("cmd.cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);

		/*
		c.gridx = 1;
		c.gridy = nextLine;
		c.gridwidth = 7;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(4,8,4,4);
		grid.setConstraints(buttonPanel, c);

		contents.add(buttonPanel);
		 */
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Basic", contents);
		
		JPanel recplay = new JPanel();
		//recplay.setLayout(new BoxLayout(recplay, BoxLayout.Y_AXIS));
		GridBagLayout recplayGrid = new GridBagLayout();
		GridBagConstraints recplayConstraint = new GridBagConstraints();
		recplayConstraint.gridwidth = 1;
		recplayConstraint.fill = GridBagConstraints.NONE;
		recplayConstraint.anchor = GridBagConstraints.WEST;
		recplay.setLayout(recplayGrid);
		recplayActive = new JCheckBox("Enable Record/Replay");
		recplayActive.addChangeListener(this);
		recplayConstraint.gridx=0;
		recplayConstraint.gridy=0;
		recplayGrid.setConstraints(recplayActive, recplayConstraint);
		recplay.add(recplayActive);
		recplayEnabled = new JPanel();
		recplayEnabled.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
		recplayEnabled.setLayout(new BoxLayout(recplayEnabled, BoxLayout.Y_AXIS));
		recplayConstraint.gridx=0;
		recplayConstraint.gridy=1;
		recplayGrid.setConstraints(recplayEnabled, recplayConstraint);
		recplay.add(recplayEnabled);
		
		
		ButtonGroup msgOrder = new ButtonGroup();
		record = new JRadioButton("Record the ordering of the messages", true);
		msgOrder.add(record);
		recplayEnabled.add(record);
		replay = new JRadioButton("Replay the message ordering", false);
		replay.addChangeListener(this);
		msgOrder.add(replay);
		recplayEnabled.add(replay);
		
		recplayDetail = new JCheckBox("Enable detailed Record/Replay");
		recplayEnabled.add(recplayDetail);
		recplayDetail.addChangeListener(this);
		fullEnabled = new JPanel();
		fullEnabled.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
		fullEnabled.setLayout(new BoxLayout(fullEnabled, BoxLayout.Y_AXIS));
		recplayConstraint.gridx=0;
		recplayConstraint.gridy=2;
		recplayGrid.setConstraints(fullEnabled, recplayConstraint);
		recplay.add(fullEnabled);
		
		JPanel fullPanel = new JPanel();
		GridBagLayout fullPanelGrid = new GridBagLayout();
		GridBagConstraints c1 = new GridBagConstraints();
		c1.gridwidth = 1;
		c1.fill = GridBagConstraints.NONE;
		c1.anchor = GridBagConstraints.WEST;
		fullPanel.setLayout(fullPanelGrid);
		//fullPanel.setLayout(new GridLayout(2, 2, 10, 10));
		ButtonGroup fullRecord = new ButtonGroup();
		//JPanel leftPanel = new JPanel();
		//leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		//JPanel rightPanel = new JPanel();
		//rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		recordDetail = new JRadioButton("Record selected processors", true);
		recordDetail.addChangeListener(this);
		fullRecord.add(recordDetail);
		c1.gridx=0;
		c1.gridy=0;
		fullPanelGrid.setConstraints(recordDetail, c1);
		fullPanel.add(recordDetail);
		recordPes = new JTextField(15);
		//recordPes.setMaximumSize(new Dimension(60, 12));
		c1.gridx=1;
		c1.gridy=0;
		fullPanelGrid.setConstraints(recordPes, c1);
		fullPanel.add(recordPes);
		replayDetail = new JRadioButton("Replay selected processor", false);
		replayDetail.addChangeListener(this);
		fullRecord.add(replayDetail);
		c1.gridx=0;
		c1.gridy=1;
		fullPanelGrid.setConstraints(replayDetail, c1);
		fullPanel.add(replayDetail);
		replayPe = new JTextField(5);
		//replayPe.setMaximumSize(new Dimension(40, 16));
		c1.gridx=1;
		c1.gridy=1;
		fullPanelGrid.setConstraints(replayPe, c1);
		fullPanel.add(replayPe);
		c1.gridx=2;
		c1.gridy=2;
		c1.weightx=1;
		c1.weighty=1;
		JPanel empty = new JPanel();
		fullPanelGrid.setConstraints(empty, c1);
		fullPanel.add(empty);
		//fullPanel.add(leftPanel);
		//fullPanel.add(rightPanel);
		fullEnabled.add(fullPanel);
		
		// Buttons for Checksum checking
		recplayChecksum = new JCheckBox("Enable checksum error detection");
		recplayConstraint.gridx=1;
		recplayConstraint.gridy=0;
		recplayGrid.setConstraints(recplayChecksum, recplayConstraint);
		recplay.add(recplayChecksum);
		recplayChecksum.addChangeListener(this);
		checksumEnabled = new JPanel();
		checksumEnabled.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
		checksumEnabled.setLayout(new BoxLayout(checksumEnabled, BoxLayout.Y_AXIS));
		recplayConstraint.gridx=1;
		recplayConstraint.gridy=1;
		recplayGrid.setConstraints(checksumEnabled, recplayConstraint);
		recplay.add(checksumEnabled);
		
		JPanel checksumPanel = new JPanel();
		GridBagLayout checksumPanelGrid = new GridBagLayout();
		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridwidth = 1;
		c2.fill = GridBagConstraints.NONE;
		c2.anchor = GridBagConstraints.WEST;
		checksumPanel.setLayout(checksumPanelGrid);
		ButtonGroup checksumGroup = new ButtonGroup();
		checksumXOR = new JRadioButton("XOR-based checksum", true);
		checksumXOR.addChangeListener(this);
		checksumGroup.add(checksumXOR);
		c2.gridx=0;
		c2.gridy=0;
		checksumPanelGrid.setConstraints(checksumXOR, c2);
		checksumPanel.add(checksumXOR);
		checksumCRC = new JRadioButton("CRC-32 checksum", false);
		checksumCRC.addChangeListener(this);
		checksumGroup.add(checksumCRC);
		c2.gridx=0;
		c2.gridy=1;
		checksumPanelGrid.setConstraints(checksumCRC, c2);
		checksumPanel.add(checksumCRC);
		c2.gridx=1;
		c2.gridy=2;
		c2.weightx=1;
		c2.weighty=1;
		JPanel empty2 = new JPanel();
		checksumPanelGrid.setConstraints(empty2, c2);
		checksumPanel.add(empty2);
		checksumEnabled.add(checksumPanel);
		
		// Add an empty panel for filling the space
		JPanel empty1 = new JPanel();
		recplayConstraint.gridx=0;
		recplayConstraint.gridy=5;
		recplayConstraint.weightx=1;
		recplayConstraint.weighty=1;
		recplayGrid.setConstraints(empty1, recplayConstraint);
		recplay.add(empty1);
		
		tabbedPane.addTab("Record/Replay", recplay);
		getContentPane().add(tabbedPane);
		getContentPane().add(buttonPanel);

		filename.setText(exec.executable);
		clParams.setText(exec.parameters);
		if (!exec.doNotSuspend) suspendOnStart.setSelected(true);  
		numPes.setText(""+exec.npes);
		portno.setText(""+exec.port);
		sshport.setText(""+exec.sshport);
		hostname.setText(exec.hostname);
		username.setText(exec.username);
		inputFile.setText(exec.inputFile);
		waitFile.setSelected(exec.waitFile);
		sshTunnel.setSelected(exec.sshTunnel);
		dir.setText(exec.workingDir);
		virtualDebug.setSelected(exec.virtualDebug);
		if (exec.virtualDebug) {
			virtualPeslabel.setForeground(Color.BLACK);
			virtualPes.setEnabled(true);
		}
		virtualPes.setText(""+exec.virtualNpes);
		if (exec.recplayActive) recplayActive.setSelected(true);
		if (exec.recplayDetailActive) recplayDetail.setSelected(true);
		if (exec.replay) replay.setSelected(true);
		if (exec.replayDetail) replayDetail.setSelected(true);
		if (recordDetail.isSelected()) recordPes.setText(exec.selectedPes);
		else if (replayDetail.isSelected()) replayPe.setText(exec.selectedPes);
		if (exec.recplayChecksum != Execution.CHECKSUM_NONE) {
			recplayChecksum.setSelected(true);
			if (exec.recplayChecksum == Execution.CHECKSUM_XOR) checksumXOR.setSelected(true);
			else if (exec.recplayChecksum == Execution.CHECKSUM_CRC) checksumCRC.setSelected(true);
		}

		enableRecplay();
	}

    public void actionPerformed(ActionEvent e) {
		boolean closeWindow = false;
    	//int destPE = 0;
    	if (e.getActionCommand().equals("browse")) 
    	{ /* Bring up file dialog box to select a new executable */
    		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
    		int returnVal = chooser.showOpenDialog(ParamsDialog.this);
    		if(returnVal == JFileChooser.APPROVE_OPTION) {
    			filename.setText(chooser.getSelectedFile().getAbsolutePath());
    		}
    	} else if (e.getActionCommand().equals("browsedir")) 
    	{ /* Bring up file dialog box to select a new executable */
    		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
    		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    		int returnVal = chooser.showOpenDialog(ParamsDialog.this);
    		if(returnVal == JFileChooser.APPROVE_OPTION) {
    			dir.setText(chooser.getSelectedFile().getAbsolutePath());
    		}
    	} else if (e.getActionCommand().equals("cmd.cancel")) {
			//System.out.println("your 'cancel' code here...");
			closeWindow = true;
		} else if (e.getActionCommand().equals("cmd.ok")) {
			//System.out.println("your 'OK' code here...");
			//mainObject.setParametersForProgram(clParams.getText(), numPes.getText(), portno.getText(),
			//		hostname.getText(), username.getText(), sshTunnel.isSelected());
			int numberPes;
			int vPes=0;
			int portNumber;
			int sshportNumber=0;
			try {
				numberPes = Integer.parseInt(numPes.getText());
				if (!portno.getText().equals("")) portNumber = Integer.parseInt(portno.getText());
				if (!sshport.getText().equals("")) sshportNumber = Integer.parseInt(sshport.getText());
				if (virtualDebug.isSelected()) vPes=Integer.parseInt(virtualPes.getText());
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(this, "All values must be positive integers", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (recplayActive.isSelected() && recplayDetail.isSelected()) {
				if (replayDetail.isSelected()) {
					int pe;
					try {
						pe = Integer.parseInt(replayPe.getText());
					} catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(this, "Only one processor may be selected", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (pe < 0 || pe >= numberPes) {
						JOptionPane.showMessageDialog(this, "The selected processor must be between 0 and the number of processors selected in the basic tab", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			exec.executable = filename.getText();
			exec.parameters = clParams.getText();
			exec.doNotSuspend = ! suspendOnStart.isSelected();
			exec.npes = numberPes;
			exec.port = portno.getText();
			exec.sshport = sshportNumber;
			exec.hostname = hostname.getText();
			exec.username = username.getText();
			exec.inputFile = inputFile.getText();
			exec.waitFile = waitFile.isSelected();
			exec.sshTunnel = sshTunnel.isSelected();
			exec.workingDir = dir.getText();
			exec.virtualDebug = virtualDebug.isSelected();
			exec.virtualNpes = vPes;
			exec.recplayActive = recplayActive.isSelected();
			exec.recplayDetailActive = recplayDetail.isSelected();
			exec.record = record.isSelected();
			exec.replay = replay.isSelected();
			exec.recordDetail = recordDetail.isSelected();
			exec.replayDetail = replayDetail.isSelected();
			exec.selectedPes = "";
			if (recplayActive.isSelected() && recplayDetail.isSelected()) {
				if (recordDetail.isSelected()) exec.selectedPes = recordPes.getText();
				else if (replayDetail.isSelected()) exec.selectedPes = replayPe.getText();
			}
			if (recplayChecksum.isSelected()) {
				if (checksumXOR.isSelected()) exec.recplayChecksum = Execution.CHECKSUM_XOR;
				else if (checksumCRC.isSelected()) exec.recplayChecksum = Execution.CHECKSUM_CRC;
				else {
					exec.recplayChecksum = Execution.CHECKSUM_NONE;
					JOptionPane.showMessageDialog(this, "Error", "The selected checksum option is not acceptable\nPlease contact a Charm++ developer", JOptionPane.ERROR_MESSAGE);
				}
			} else exec.recplayChecksum = Execution.CHECKSUM_NONE;
			closeWindow = true;
		} 
		if (closeWindow) {
			setVisible(false);
			dispose();
		}
    }

    private void enableRecplay() {
    	boolean status = recplayActive.isSelected();
		Color color = status ? Color.BLACK : Color.GRAY;
		record.setEnabled(status);
		record.setForeground(color);
		replay.setEnabled(status);
		replay.setForeground(color);
		recplayChecksum.setEnabled(status);
		recplayChecksum.setForeground(color);
		
		status = status && replay.isSelected();
		color = status ? Color.BLACK : Color.GRAY;
		recplayDetail.setEnabled(status);
		recplayDetail.setForeground(color);

    	status = recplayActive.isSelected() && recplayDetail.isSelected() && replay.isSelected();
		color = status ? Color.BLACK : Color.GRAY;
		recordDetail.setEnabled(status);
		recordDetail.setForeground(color);
		recordPes.setEnabled(status && recordDetail.isSelected());
		replayDetail.setEnabled(status);
		replayDetail.setForeground(color);
		replayPe.setEnabled(status && replayDetail.isSelected());
		
		status = recplayActive.isSelected() && recplayChecksum.isSelected();
		color = status ? Color.BLACK : Color.GRAY;
		checksumXOR.setEnabled(status);
		checksumXOR.setForeground(color);
		checksumCRC.setEnabled(status);
		checksumCRC.setForeground(color);
    }
    
	public void stateChanged(ChangeEvent e) {
		if (e.getSource()==virtualDebug) {
			if (virtualDebug.isSelected()) {
				virtualPeslabel.setForeground(Color.BLACK);
				virtualPes.setEnabled(true);
			} else {
				virtualPeslabel.setForeground(Color.GRAY);
				virtualPes.setEnabled(false);
			}
		}
		else if (e.getSource()==recplayActive ||
				 e.getSource()==recplayDetail ||
				 e.getSource()==replay ||
				 e.getSource()==recordDetail ||
				 e.getSource()==replayDetail ||
				 e.getSource()==recplayChecksum) enableRecplay();
    }

    /*
    private void windowAction(Object actionCommand) {
		boolean closeWindow = false;
		String cmd = null;
		if (actionCommand != null) {
			if (actionCommand instanceof ActionEvent) {
				cmd = ((ActionEvent)actionCommand).getActionCommand();
			} else {
				cmd = actionCommand.toString();
			}
		}
		if (cmd == null) {
			// do nothing
		} else if (cmd.equals("cmd.cancel")) {
			//System.out.println("your 'cancel' code here...");
			closeWindow = true;
		} else if (cmd.equals("cmd.ok")) {
			//System.out.println("your 'OK' code here...");
			//mainObject.setParametersForProgram(clParams.getText(), numPes.getText(), portno.getText(),
			//		hostname.getText(), username.getText(), sshTunnel.isSelected());
			int numberPes;
			int portNumber;
			try {
				numberPes = Integer.parseInt(numPes.getText());
				if (!portno.getText().equals("")) portNumber = Integer.parseInt(portno.getText());
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "All values must be positive integers", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			exec.executable = filename.getText();
			exec.parameters = clParams.getText();
			exec.npes = numberPes;
			exec.port = portno.getText();
			exec.hostname = hostname.getText();
			exec.username = username.getText();
			exec.sshTunnel = sshTunnel.isSelected();
			exec.workingDir = dir.getText();
			closeWindow = true;
		} 
		if (closeWindow) {
			setVisible(false);
			dispose();
		}
	} // windowAction()
    */

//	for debugging purposes
	public static void main (String args[])
	{
		JFrame frame= new JFrame() {
			public Dimension getPreferredSize() {
				return new Dimension (200, 100);
			}
		};
		frame.setTitle ("Debugging frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(false);
		JDialog dialog = new ParamsDialog(frame, true, new Execution());
		dialog.addWindowListener(new WindowAdapter() { 
			public void windowClosing(WindowEvent event){
				System.exit(0);
			}
			public void windowClosed(WindowEvent event){
				System.exit(0);
			}
		});
		dialog.pack();
		dialog.setVisible(true);
	}

};

