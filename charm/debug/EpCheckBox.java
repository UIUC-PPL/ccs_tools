package charm.debug;

import java.awt.Color;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import charm.debug.pdata.EpInfo;

// This class is used to contain an entry method, and show it as a CheckBox.
// It is used both in the main window (colored according to the selected PeSet,
// and in the PythonDialog to select which entry methods to hook the script to.

public class EpCheckBox extends JCheckBox {
	public EpInfo ep;
	public int status;
	
	EpCheckBox(EpInfo e) {
		super(e.toString());
		ep = e;
		status = 0;
		e.setCheckBox(this);
		setOpaque(false);
	}
	
	int click() {
		switch (status) {
		case 0:
			status = 1;
			setForeground(Color.GRAY);
			setSelected(true);
			setText("B: "+getText());
			return 1;
		case 1:
			status = 3;
			setForeground(Color.BLACK);
			setSelected(true);
			setText(getText().substring(3));
			return 2;
		case 3:
			status = 2;
			setForeground(Color.GRAY);
			setSelected(true);
			setText("A: "+getText());
			return -1;
		case 2:
			status = 0;
			setForeground(Color.BLACK);
			setSelected(false);
			setText(getText().substring(3));
			return -2;
		default:
			return 0;
		}
	}
	
	void setCoverageColor(SortedSet activePes) {
		if (ep.getBPSet().isEmpty()) {
			setForeground(Color.BLACK);
			setSelected(false);
		} else {
			if (ep.getBPSet().containsAll(activePes)) {
				setForeground(Color.BLACK);
				setSelected(true);
			} else {
				SortedSet tmp = new TreeSet();
				tmp.addAll(ep.getBPSet());
				tmp.retainAll(activePes);
				if (tmp.isEmpty()) {
					setForeground(Color.BLACK);
					setSelected(false);
				} else {
					setForeground(Color.GRAY);
					setSelected(true);
				}
			}
		}
	}
}
