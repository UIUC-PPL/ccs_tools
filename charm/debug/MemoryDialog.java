package charm.debug;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;


public class MemoryDialog extends JDialog
    implements ActionListener, DocumentListener {

    // Default values
    private static final int VERTICAL_LINES = 80;
    private static final int LINE_SIZE = 10;
    private static final int HORIZONTAL_PIXELS = 1000;

    JComboBox peList;
    JTextField scanSize;
    JTextField lines;
    JLabel vPixels;
    JTextField hPixels;
    JButton ok;
    JButton cancel;
    boolean confirmed;

    MemoryDialog(Frame parent, boolean modal, int nprocs) {
	super(parent, "Memory", modal);
	confirmed = false;
	getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

	JPanel topPane = new JPanel();
	topPane.setLayout(new GridLayout(5, 2));

	JLabel textPeList = new JLabel("Processor number: ");
	topPane.add(textPeList);
	String pes[] = new String[nprocs];
	for (int i=0; i<nprocs; ++i) pes[i] = (new Integer(i)).toString();
	peList = new JComboBox(pes);
	peList.setSelectedIndex(0);
	topPane.add(peList);

	JLabel textScanSize = new JLabel("Line width: ");
	topPane.add(textScanSize);
	scanSize = new JTextField("16");
	scanSize.addActionListener(this);
	scanSize.getDocument().addDocumentListener(this);
	topPane.add(scanSize);

	JLabel textLines = new JLabel("Number of lines: ");
	topPane.add(textLines);
	lines = new JTextField("50");
	lines.addActionListener(this);
	lines.getDocument().addDocumentListener(this);
	topPane.add(lines);

	JLabel textVPixels = new JLabel("Veritcal pixels: ");
	topPane.add(textVPixels);
	int pixels = Integer.parseInt(scanSize.getText()) * Integer.parseInt(lines.getText());
	vPixels = new JLabel(""+pixels);
	topPane.add(vPixels);

	JLabel textHPixels = new JLabel("Horizontal pixels: ");
	topPane.add(textHPixels);
	hPixels = new JTextField("1000");
	hPixels.addActionListener(this);
	topPane.add(hPixels);

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

    public int getPe() { return peList.getSelectedIndex(); }
    public int getScan() { return Integer.parseInt(scanSize.getText()); }
    public int getLines() { return Integer.parseInt(lines.getText()); }
    public int getHPixels() { return Integer.parseInt(hPixels.getText()); }

    public boolean confirmed() {return confirmed;}

    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("ok") ||
	    e.getSource() == scanSize ||
	    e.getSource() == lines ||
	    e.getSource() == hPixels) {
	    // check correctness of input
	    try {
		if (peList.getSelectedIndex() >= 0 &&
		    Integer.parseInt(scanSize.getText()) > 3 &&
		    Integer.parseInt(lines.getText()) > 0 &&
		    Integer.parseInt(hPixels.getText()) > 10) {
		    confirmed=true;
		    setVisible(false);
		} else if (Integer.parseInt(scanSize.getText()) <= 3) {
		    JOptionPane.showMessageDialog(this, "The width of the line must be greater than 3", "Error", JOptionPane.ERROR_MESSAGE);
		    scanSize.requestFocus();
		} else if (Integer.parseInt(lines.getText()) <= 0) {
		    JOptionPane.showMessageDialog(this, "The number of lines must be positive", "Error", JOptionPane.ERROR_MESSAGE);
		    lines.requestFocus();
		} else if (Integer.parseInt(hPixels.getText()) <= 10) {
		    JOptionPane.showMessageDialog(this, "The number of horizontal pixels must be greater than 10", "Error", JOptionPane.ERROR_MESSAGE);
		    hPixels.requestFocus();
		}
	    } catch (NumberFormatException ne) {
		JOptionPane.showMessageDialog(this, "All values must be positive integers", "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}
	if (e.getActionCommand().equals("cancel")) {
	    setVisible(false);
	}
    }

    public void insertUpdate(DocumentEvent e) {
	updatePixels();
    }

    public void removeUpdate(DocumentEvent e) {
	updatePixels();
    }

    public void changedUpdate(DocumentEvent e) {
	// Plain text components don't fire this event
    }

    private void updatePixels() {
	try {
	    int ss = Integer.parseInt(scanSize.getText());
	    int l = Integer.parseInt(lines.getText());
	    if (ss > 0 && l > 0) {
		int vp = ss*l;
		vPixels.setText(""+vp);
	    }
	} catch (NumberFormatException ne) {
	    vPixels.setText("??");
	}
    }

}
