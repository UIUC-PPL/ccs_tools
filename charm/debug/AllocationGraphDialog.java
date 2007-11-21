package charm.debug;

import java.awt.event.ActionListener;

import javax.swing.*;

import java.awt.*;

import javax.swing.event.*;
import java.awt.event.*;

public class AllocationGraphDialog extends JDialog implements ActionListener,
        DocumentListener {

	// Default values
	//private static final int EVENTS_PER_BAR = 1;
	//private static final int BAR_WIDTH = 2;
	//private static final int VERTICAL_PIXELS = 800;

	JComboBox peList;
	JTextField firstEvent;
	JTextField lastEvent;
	JLabel numEvents;
	JTextField barWidth;
	JTextField eventsPerBar;
	JLabel hPixels;
	JTextField vPixels;
	JButton ok;
	JButton cancel;
	boolean confirmed;

	AllocationGraphDialog(Frame parent, boolean modal, int nprocs) {
		super(parent, "Allocation Graph", modal);
		confirmed = false;
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		JPanel topPane = new JPanel();
		topPane.setLayout(new GridLayout(8, 2));

		JLabel textPeList = new JLabel("Processor number: ");
		topPane.add(textPeList);
		String pes[] = new String[nprocs];
		for (int i = 0; i < nprocs; ++i)
			pes[i] = (new Integer(i)).toString();
		peList = new JComboBox(pes);
		peList.setSelectedIndex(0);
		topPane.add(peList);

		JLabel textFirstEvent = new JLabel("First event: ");
		topPane.add(textFirstEvent);
		firstEvent = new JTextField("1");
		firstEvent.addActionListener(this);
		firstEvent.getDocument().addDocumentListener(this);
		topPane.add(firstEvent);

		JLabel textLastEvent = new JLabel("Last event: ");
		topPane.add(textLastEvent);
		lastEvent = new JTextField("1");
		lastEvent.addActionListener(this);
		lastEvent.getDocument().addDocumentListener(this);
		topPane.add(lastEvent);

		JLabel textNumEvents = new JLabel("Total events: ");
		topPane.add(textNumEvents);
		int events = Integer.parseInt(lastEvent.getText())
		        - Integer.parseInt(firstEvent.getText()) + 1;
		numEvents = new JLabel("" + events);
		topPane.add(numEvents);

		JLabel textEventsPerBar = new JLabel("Events per bar: ");
		topPane.add(textEventsPerBar);
		eventsPerBar = new JTextField("1");
		eventsPerBar.addActionListener(this);
		eventsPerBar.getDocument().addDocumentListener(this);
		topPane.add(eventsPerBar);

		JLabel textBarWidth = new JLabel("Bar width: ");
		topPane.add(textBarWidth);
		barWidth = new JTextField("2");
		barWidth.addActionListener(this);
		barWidth.getDocument().addDocumentListener(this);
		topPane.add(barWidth);

		JLabel textHPixels = new JLabel("Horizontal pixels: ");
		topPane.add(textHPixels);
		int pixels = events * Integer.parseInt(barWidth.getText())
		        / Integer.parseInt(eventsPerBar.getText());
		hPixels = new JLabel("" + pixels);
		topPane.add(hPixels);

		JLabel textVPixels = new JLabel("Vertical pixels: ");
		topPane.add(textVPixels);
		vPixels = new JTextField("800");
		vPixels.addActionListener(this);
		topPane.add(vPixels);

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

	public boolean confirmed() {
		return confirmed;
	}

	public int getPe() {
		return peList.getSelectedIndex();
	}

	public int getFirstEvent() {
		return Integer.parseInt(firstEvent.getText());
	}

	public int getLastEvent() {
		return Integer.parseInt(lastEvent.getText());
	}

	public int getEventsPerBar() {
		return Integer.parseInt(eventsPerBar.getText());
	}

	public int getBarWidth() {
		return Integer.parseInt(barWidth.getText());
	}

	public int getWidth() {
		return Integer.parseInt(vPixels.getText());
	}

	public int getHeight() {
		return Integer.parseInt(vPixels.getText());
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ok") || e.getSource() == firstEvent
		        || e.getSource() == lastEvent || e.getSource() == eventsPerBar
		        || e.getSource() == barWidth || e.getSource() == vPixels) {
			// check correctness of input
			try {
				if (peList.getSelectedIndex() >= 0
				        && Integer.parseInt(firstEvent.getText()) >= 0
				        && Integer.parseInt(lastEvent.getText()) >= Integer.parseInt(firstEvent.getText())
				        && Integer.parseInt(eventsPerBar.getText()) > 0
				        && Integer.parseInt(barWidth.getText()) > 0
				        && Integer.parseInt(vPixels.getText()) > 10) {
					confirmed = true;
					setVisible(false);
				} else if (Integer.parseInt(firstEvent.getText()) < 0) {
					JOptionPane.showMessageDialog(this, "The first event must be positive", "Error", JOptionPane.ERROR_MESSAGE);
					firstEvent.requestFocus();
				} else if (Integer.parseInt(lastEvent.getText()) < Integer.parseInt(firstEvent.getText())) {
					JOptionPane.showMessageDialog(this, "The last event must be greater than the first event", "Error", JOptionPane.ERROR_MESSAGE);
					lastEvent.requestFocus();
				} else if (Integer.parseInt(eventsPerBar.getText()) <= 0) {
					JOptionPane.showMessageDialog(this, "The number of events per bar must be positive", "Error", JOptionPane.ERROR_MESSAGE);
					eventsPerBar.requestFocus();
				} else if (Integer.parseInt(barWidth.getText()) <= 0) {
					JOptionPane.showMessageDialog(this, "The width of the bars must be positive", "Error", JOptionPane.ERROR_MESSAGE);
					barWidth.requestFocus();
				} else if (Integer.parseInt(vPixels.getText()) <= 10) {
					JOptionPane.showMessageDialog(this, "The number of vertical pixels must be greater than 10", "Error", JOptionPane.ERROR_MESSAGE);
					vPixels.requestFocus();
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
			int first = Integer.parseInt(firstEvent.getText());
			int last = Integer.parseInt(lastEvent.getText());
			if (first >= 0 && last > first) {
				int events = last - first;
				numEvents.setText("" + events);
			} else throw new NumberFormatException();
		} catch (NumberFormatException ne) {
			numEvents.setText("??");
		}
		try {
			int events = Integer.parseInt(numEvents.getText());
			int size = Integer.parseInt(barWidth.getText());
			int epb = Integer.parseInt(eventsPerBar.getText());
			if (size > 0 && epb > 0) {
				int pixels = events * size / epb;
				hPixels.setText("" + pixels);
			} else throw new NumberFormatException();
		} catch (NumberFormatException ne) {
			hPixels.setText("??");
		}
	}

}
