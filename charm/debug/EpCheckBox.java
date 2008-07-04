package charm.debug;

import java.awt.Color;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import charm.debug.pdata.EpInfo;

public class EpCheckBox extends JCheckBox {
	public EpInfo ep;
	
	EpCheckBox(EpInfo e) {
		super(e.toString());
		ep = e;
		e.setCheckBox(this);
		setOpaque(false);
	}
	
	void setCoverageColor(SortedSet activePes) {
		if (!ep.getBPSet().isEmpty()) {
			if (ep.getBPSet().containsAll(activePes)) {
				setForeground(Color.BLACK);
			} else {
				SortedSet tmp = new TreeSet();
				tmp.addAll(ep.getBPSet());
				tmp.retainAll(activePes);
				if (tmp.isEmpty()) {
					setForeground(Color.BLACK);
				} else {
					setForeground(Color.GRAY);
				}
			}
		}
	}
}
