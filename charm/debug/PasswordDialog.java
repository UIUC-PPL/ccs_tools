package charm.debug;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

/* PasswordDemo.java requires no other files. */

public class PasswordDialog extends JDialog
                          implements ActionListener {
    private static String OK = "ok";
	private static String CANCEL = "cancel";
	private char[] result;

    private JPasswordField passwordField;

    public PasswordDialog(char[] res) {
		super();
		setTitle("Password Requested");
        //Use the default FlowLayout.
		result = res;
		addWindowListener(new WindowAdapter() {
				public void windowActivated(WindowEvent e) {
					passwordField.requestFocusInWindow();
				}
				public void windowClosing() {
					result = null;
					dispose();
				}
			});
		
        //Create everything.
		JPanel main = new JPanel();
		main.setLayout(new GridLayout(0,1));
        passwordField = new JPasswordField(10);
        passwordField.setActionCommand(OK);
        passwordField.addActionListener(this);

        JLabel label = new JLabel("Enter the password: ");
        label.setLabelFor(passwordField);

        JComponent buttonPane = createButtonPanel();

        //Lay out everything.
        JPanel textPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        textPane.add(label);
        textPane.add(passwordField);

        main.add(textPane);
        main.add(buttonPane);

		setContentPane(main);
		pack();
		setVisible(true);
    }

    protected JComponent createButtonPanel() {
        JPanel p = new JPanel(new GridLayout(1,0));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.setActionCommand(OK);
        cancelButton.setActionCommand(CANCEL);
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);

        p.add(okButton);
        p.add(cancelButton);

        return p;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

		if (CANCEL.equals(cmd)) {
			result = null;
		}			
        else if (OK.equals(cmd)) {
            result = passwordField.getPassword();
        }
    }

    //Must be called from the event dispatch thread.
    protected void resetFocus() {
        passwordField.requestFocusInWindow();
    }
}
