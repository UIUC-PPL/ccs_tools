/**
  Displays a PList onscreen.
  FIXME: allow manipulation of the PList
  
  Copyright University of Illinois at Urbana-Champaign
  written by Orion Lawlor in 1/2004
*/
package charm.debug;
import charm.debug.fmt.*;

import java.awt.*;
import javax.swing.*;

class PListOutputArea extends JPanel {
	private PList list=null;
	public void setList(PList list_) {
		list=list_;
		setSize(getPreferredSize());
		repaint(10);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (list!=null) {
			PDisplayStyle pd=new PDisplayStyle(g,getWidth());
			list.draw(pd);
		}
	}
	
	public Dimension getPreferredSize() {
		int ht=30;
		Graphics g=getGraphics();
		if (g!=null && list!=null) {
			PDisplayStyle pd=new PDisplayStyle(g,getWidth(),true);
			list.draw(pd);
			ht=pd.getHeight();
		}
		return new Dimension(1,ht);
	}
};
