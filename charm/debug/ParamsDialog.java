package charm.debug;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class ParamsDialog extends JDialog {

ParDebug mainObject = null;

private JTextField  clParams,  numPes, portno;

public ParamsDialog(Frame parent, boolean modal, ParDebug obj)
{
  super (parent, modal);
  mainObject = obj;
  initComponents();
  pack();
}

public void setFields(String params, String pes, String pno)
{
  clParams.setText(params);
  numPes.setText(pes);
  portno.setText(pno);
}

private void initComponents()
{

  setTitle("Program Parameters");
  Container contents = getContentPane();

  GridBagLayout grid = new GridBagLayout();
  GridBagConstraints c = new GridBagConstraints();
  contents.setLayout(grid);

  c.gridx = 0;
  c.gridy = 0;
  c.anchor = GridBagConstraints.WEST;
  c.insets = new Insets(12,12,0,0);
  JLabel paramlabel = new JLabel();
  paramlabel.setText("Command Line Parameters:");
  paramlabel.setLabelFor(clParams);
  grid.setConstraints(paramlabel, c);
  contents.add(paramlabel);

  clParams = new JTextField(35);
  c.gridx = 1;
  c.gridy = 0;
  c.gridwidth = 3;
  c.fill = GridBagConstraints.HORIZONTAL;
  c.weightx = 1.0;
  c.insets = new Insets(11,7,0,0);
  grid.setConstraints(clParams,c); 

  c.gridx = 0;
  c.gridy = 1;
  c.anchor = GridBagConstraints.WEST;
  c.insets = new Insets(12,12,0,0);
  JLabel peslabel = new JLabel();
  peslabel.setText("Number of Processors:");
  peslabel.setLabelFor(numPes);
  grid.setConstraints(peslabel, c);
  contents.add(peslabel);
  
  numPes = new JTextField(5);
  c.gridx = 1;
  c.gridy = 1;
  c.gridwidth = 1;
  c.fill = GridBagConstraints.NONE;
  c.anchor = GridBagConstraints.WEST;
  c.insets = new Insets(12,8,0,0);
  grid.setConstraints(numPes,c); 


  c.gridx = 0;
  c.gridy = 2;
  c.anchor = GridBagConstraints.WEST;
  c.insets = new Insets(12,12,0,0);
  JLabel portNumberLabel = new JLabel();
  portNumberLabel.setText("Port Number:");
  portNumberLabel.setLabelFor(portno);
  grid.setConstraints(portNumberLabel, c);
  contents.add(portNumberLabel);

  c.gridx = 1;
  c.gridy = 2;
  c.gridwidth = 1;
  c.fill = GridBagConstraints.NONE;
  c.anchor = GridBagConstraints.WEST;
  c.insets = new Insets(12,8,0,0);
  portno = new JTextField(5);
  grid.setConstraints(portno, c);
  //contents.add(portno);
  
  contents.add(clParams);
  contents.add(numPes);
  contents.add(portno);  
  
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
  c.gridy = 3;
  c.gridwidth = 7;
  c.weightx = 0.0;
  c.weighty = 0.0;
  c.anchor = GridBagConstraints.EAST;
  grid.setConstraints(buttonPanel, c);
  contents.add(buttonPanel);
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
            mainObject.setParametersForProgram(clParams.getText(), numPes.getText(), portno.getText());
            closeWindow = true;
        } 
        if (closeWindow) {
            setVisible(false);
            dispose();
        }
} // windowAction()


//for debugging purposes
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

