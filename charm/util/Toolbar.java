/*
A panel that displays a vertical list of tools,
and allows the user to switch between them.

by Orion Lawlor,  6/23/2001
olawlor@acm.org
*/
package charm.util;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*; 
import java.net.URL;

public class Toolbar extends Panel implements MouseListener, MouseMotionListener
{
	private int curTool;//Currently selected tool
	private int nTools;//Number of tool images
	private int tw,th;//Each tool image's width and height
	private Image tools;//All the tool images (vertical, selected versions to right)
	private Label status; //Status panel
	private String[] toolDesc;//Tool descriptions

	public Toolbar(URL imageURL,int tw_,int th_,int nTools_,
		Label status_,String[] toolDesc_)
	{
		tw=tw_; th=th_; nTools=nTools_;
		curTool=0;
		//Try to load the tools image
		tools=null;
		try {
			tools = Toolkit.getDefaultToolkit().getImage(imageURL);
			MediaTracker tracker = new MediaTracker(this);
			tracker.addImage(tools, 0);
			tracker.waitForID(0);
		} catch (Exception E) {
			System.out.println("Error loading toolbar image "+imageURL);
			E.printStackTrace();
		}
		status=status_;
		toolDesc=toolDesc_;
		//Grab our own mouse events
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public Dimension getMinimumSize() {return new Dimension(tw,th*nTools);}
	public Dimension getPreferredSize() {return new Dimension(tw,th*nTools);}
	public Dimension getMaximumSize() {return new Dimension(tw,30000);}
	
	public void setTool(int t) {
		if (t!=curTool) {
			curTool=t;
			repaint(20);
		}
	}
	public int getTool() { return curTool; }
	
    /******** Mouse handling *******/
	private int toTool(MouseEvent evt) {
		int t=evt.getY()/th;
		if (t>=nTools) return -1;
		else return t;
	}
	public void mouseMoved(MouseEvent evt)
	{
		if (toolDesc==null || toTool(evt)==-1) 
			status.setText("");
		else
			status.setText(toolDesc[toTool(evt)]);
	}
	public void mousePressed(MouseEvent evt)
	{
		if (toTool(evt)!=-1)
			setTool(toTool(evt));
	}
	
	private String savedStatus="";
	public void mouseEntered(MouseEvent evt)
	{ savedStatus=status.getText(); }
	public void mouseExited(MouseEvent evt)
	{ status.setText(savedStatus); }
	
	public void mouseClicked(MouseEvent evt)
	{}
	public void mouseDragged(MouseEvent evt)
	{}
	public void mouseReleased(MouseEvent evt)
	{}
	
    /******** Redraw ***********/
	public void update(Graphics g) {
		paint(g);
	}
	public void paint(Graphics g) 
	{
		Dimension dim=getSize();
		int w=dim.width, h=dim.height;
		for (int t=0;t<nTools;t++) {
			int srcX=0,srcY=t*th;
			int destX=0,destY=t*th;
			if (t==curTool) 
				srcX+=tw;//Draw selected version
			g.drawImage(tools,
				destX,destY,destX+tw,destY+th,
				srcX,srcY,srcX+tw,srcY+th,null);
		}
		g.setColor(Color.lightGray);
		g.fillRect(0,nTools*th,w,h);
	}
}


