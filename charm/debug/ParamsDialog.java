package charm.debug;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import charm.debug.preference.Execution;

public class ParamsDialog extends JDialog implements ActionListener {

	//ParDebug mainObject = null;
	Execution exec = null;

	private JTextField  clParams, numPes, portno, hostname, username, filename, dir;
	private JCheckBox sshTunnel;
	private JButton chooser, dirchooser;

	public ParamsDialog(Frame parent, boolean modal, Execution obj) {
		super (parent, modal);
		exec = obj;
		initComponents();
		pack();
		setResizable(false);
	}

	public void setFields(String params, String pes, String pno, String host, String user, boolean ssh) {
		clParams.setText(params);
		numPes.setText(pes);
		portno.setText(pno);
		hostname.setText(host);
		username.setText(user);
		sshTunnel.setSelected(ssh);
	}

	private void initComponents() {

		setTitle("Program Parameters");
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		JPanel contents = new JPanel();

		GridBagLayout grid = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		contents.setLayout(grid);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,12,0,0);
		JLabel filenamelabel = new JLabel("Executable:");
		filenamelabel.setLabelFor(filename);
		grid.setConstraints(filenamelabel, c);
		contents.add(filenamelabel);
		
		filename = new JTextField(35);
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.insets = new Insets(11,7,0,0);
		grid.setConstraints(filename, c);
		
		chooser = new JButton("Change");
		chooser.setActionCommand("browse");
		chooser.addActionListener(this);
		c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.EAST;
		c.insets = new Insets(12,5,0,10);
		grid.setConstraints(chooser, c);
		
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,12,0,0);
		JLabel dirlabel = new JLabel("Working dir:");
		dirlabel.setLabelFor(dir);
		grid.setConstraints(dirlabel, c);
		contents.add(dirlabel);
		
		dir = new JTextField(35);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.insets = new Insets(11,7,0,0);
		grid.setConstraints(dir, c);
		
		dirchooser = new JButton("Change");
		dirchooser.setActionCommand("browsedir");
		dirchooser.addActionListener(this);
		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.EAST;
		c.insets = new Insets(12,5,0,10);
		grid.setConstraints(dirchooser, c);
		
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,12,0,0);
		JLabel paramlabel = new JLabel();
		paramlabel.setText("Command Line Parameters:");
		paramlabel.setLabelFor(clParams);
		grid.setConstraints(paramlabel, c);
		contents.add(paramlabel);

		clParams = new JTextField(35);
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.insets = new Insets(11,7,0,0);
		grid.setConstraints(clParams,c); 

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,12,0,0);
		JLabel peslabel = new JLabel();
		peslabel.setText("Number of Processors:");
		peslabel.setLabelFor(numPes);
		grid.setConstraints(peslabel, c);
		contents.add(peslabel);

		numPes = new JTextField(5);
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,8,0,0);
		grid.setConstraints(numPes,c); 


		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,12,0,0);
		JLabel portNumberLabel = new JLabel();
		portNumberLabel.setText("Port Number:");
		portNumberLabel.setLabelFor(portno);
		grid.setConstraints(portNumberLabel, c);
		contents.add(portNumberLabel);

		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,8,0,0);
		portno = new JTextField(5);
		grid.setConstraints(portno, c);
		//contents.add(portno);

		c.gridx = 0;
		c.gridy = 5;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,12,0,0);
		JLabel hostNameLabel = new JLabel();
		hostNameLabel.setText("Host name:");
		hostNameLabel.setLabelFor(hostname);
		grid.setConstraints(hostNameLabel, c);
		contents.add(hostNameLabel);

		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(12,8,0,0);
		hostname = new JTextField(20);
		grid.setConstraints(hostname, c);

		c.gridx = 0;
		c.gridy = 6;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(6,12,0,0);
		JLabel userNameLabel = new JLabel();
		userNameLabel.setText("Username:");
		userNameLabel.setLabelFor(username);
		grid.setConstraints(userNameLabel, c);
		contents.add(userNameLabel);

		c.gridx = 1;
		c.gridy = 6;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(6,8,0,0);
		username = new JTextField(15);
		grid.setConstraints(username, c);

		c.gridx = 1;
		c.gridy = 7;
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
		contents.add(hostname);
		contents.add(username);
		contents.add(sshTunnel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, 0));
		JButton okButton = new JButton();
		okButton.setText("OK");
		okButton.setActionCommand("cmd.ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				windowAction(event);
			}
		});
		buttonPanel.add(okButton);

		// space
		buttonPanel.add(Box.createRigidArea(new Dimension(5,0)));

		// cancel button
		JButton cancelButton = new JButton();
		cancelButton.setText("CANCEL");
		cancelButton.setActionCommand("cmd.cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				windowAction(event);
			}
		});
		buttonPanel.add(cancelButton);

		c.gridx = 1;
		c.gridy = 7;
		c.gridwidth = 7;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(4,8,4,4);
		grid.setConstraints(buttonPanel, c);

		contents.add(buttonPanel);

		getContentPane().add(contents);

		filename.setText(exec.executable);
		clParams.setText(exec.parameters);
		numPes.setText(""+exec.npes);
		portno.setText(""+exec.port);
		hostname.setText(exec.hostname);
		username.setText(exec.username);
		sshTunnel.setSelected(exec.sshTunnel);
		dir.setText(exec.workingDir);
	}

    public void actionPerformed(ActionEvent e) {
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
    	}
    }
    
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
		JDialog dialog = new ParamsDialog(frame, true, null);
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

