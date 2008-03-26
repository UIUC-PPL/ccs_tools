package charm.debug;

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
}
