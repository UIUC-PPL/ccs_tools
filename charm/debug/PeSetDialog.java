package charm.debug;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.util.SortedSet;

import charm.util.JSelectField;

public class PeSetDialog extends JDialog implements ActionListener {
	private JTextField name;
	private JSelectField pes;
	private SortedSet pesList;
	private JButton ok;
    private JButton cancel;
	private boolean confirmed;
	private int numPes;
	
	public PeSetDialog(Frame parent, boolean modal, int npes) {
		super(parent, "New Pe Subset", modal);
		confirmed = false;
		numPes = npes;
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		
		JPanel topPane = new JPanel();
		topPane.setLayout(new GridLayout(2, 2));

		JLabel textName = new JLabel("Name: ");
		topPane.add(textName);
		name = new JTextField("");
		name.addActionListener(this);
		//name.getDocument().addDocumentListener(this);
		topPane.add(name);

		JLabel textPes = new JLabel("List of processors: ");
		topPane.add(textPes);
		pes = new JSelectField("", 30);
		pes.addActionListener(this);
		//pes.getDocument().addDocumentListener(this);
		topPane.add(pes);
		
		JPanel bottomPane = new JPanel();

		ok = new JButton("Ok");
		ok.setActionCommand("ok");
		ok.addActionListener(this);
		bottomPane.add(ok);

		cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		bottomPane.add(cancel);

		getContentPane().add(topPane);
		getContentPane().add(bottomPane);

		pack();
		setResizable(false);
		setVisible(true);
	}

	public boolean confirmed() {return confirmed;}
	public String getName() {return name.getText();}
	public SortedSet getPes() {return pesList;}
	
	private boolean parsePes() {
		pesList = pes.getValue(numPes);
		return pesList != null;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ok") ||
				e.getSource() == name ||
				e.getSource() == pes) {
			// check correctness of input
			try {
				if (name.getText().length() > 0 &&
						parsePes()) {
					confirmed=true;
					setVisible(false);
				} else if (name.getText().length() <= 0) {
					JOptionPane.showMessageDialog(this, "Must specify a name for the subset", "Error", JOptionPane.ERROR_MESSAGE);
					name.requestFocus();
				} else {
					JOptionPane.showMessageDialog(this, "Invalid specification of processors", "Error", JOptionPane.ERROR_MESSAGE);
					pes.requestFocus();					}
			} catch (NumberFormatException ne) {
				JOptionPane.showMessageDialog(this, "All values must be positive integers", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		if (e.getActionCommand().equals("cancel")) {
			setVisible(false);
		}

    }
}
