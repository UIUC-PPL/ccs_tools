/**
  Copyright University of Illinois at Urbana-Champaign
  Written by Orion Sky Lawlor, olawlor@acm.org, 2004/1/21
*/
package charm.debug.fmt;

import java.awt.Graphics;
import java.awt.Font;
import java.awt.FontMetrics;

/**
  State class used while drawing a set of PObjects.
  This is basically just a standard line-wrapped 
  text layout.
*/
public class PDisplayStyle {
	private Graphics g;
	private Font font;
	private FontMetrics fm;
	private int wid; // Size of display region
	private int indent; // pixels from left border to skip
	private int indentDel; // change in indent between objects
	private int pitch; // pixels between each row of data
	private int spaceWidth; // pixels between each string
	private boolean measureMode;
	/// The current raster position for drawing, e.g., text.
	private int x,y;
	
	
	private void init(int wid_) {
		font=new Font("Serif",Font.PLAIN,12);
		g.setFont(font);
		fm=g.getFontMetrics();
		wid=wid_;
		pitch=fm.getHeight();
		spaceWidth=fm.stringWidth(" ");
		indentDel=2*pitch;
		indent=10;
		x=indent; y=pitch;
		measureMode=false;
	}
	
	/// Display style for text measurement
	public PDisplayStyle(Graphics g_,int wid_,boolean forMeasure) {
		g=g_;
		init(wid_);
		g=null;
	}
	
	/// Display style for final output:
	public PDisplayStyle(Graphics g_,int wid_) {
		g=g_; 
		init(wid_);
		g.setFont(font);
	}
	
	/// Return the height of one row of text.
	public int getRowHeight() {return pitch;}
	/// Return the width of this string of text.
	public int getWidth(String s) {
		return fm.stringWidth(s)+spaceWidth;
	}
	
	public static final int drawStyle_onerow=0;
	public static final int drawStyle_multirow=1;
	
	// Measure this object's width
	private int measureWidth(PAbstract obj) {
		measureMode=true;
		int saved_x=x, saved_y=y;
		x=0;y=0;
		obj.draw(this,drawStyle_onerow);
		int w=x;
		x=saved_x; y=saved_y;
		measureMode=false;
		return w;
	}
	
	/// Set up (x,y) to draw this object, and
	///  return the drawStyle to use.
	public int getDrawStyle(PAbstract obj) {
		int drawStyle=drawStyle_onerow;
		if (measureMode) { // Already just measuring-- just use 1 row
			return drawStyle;
		}
		int w=measureWidth(obj);
		if (x+w>=wid) { // Too big for this line:
			if (x!=indent) { // May just need a line wrap
				newRow();
			}
			if (x+w>=wid) { // Still too big-- draw multi-row:
				drawStyle=drawStyle_multirow;
			}
		}
		return drawStyle;
	}
	
	/// Render this string, advancing the raster position
	public boolean drawString(String s) {
		if (x>=wid) return false;
		int w=getWidth(s);
		if (x+w>=wid) { /* too far: mark with ellipsis */
			if (!measureMode && g!=null) g.drawString("...",x,y);
			x=wid;
			return false;
		}
		else {
			if (!measureMode && g!=null) g.drawString(s,x,y);
			x+=w;
			return true;
		}
	}
	
	/// Start a new row of objects
	public void newRow() {
		y+=pitch;
		x=indent;
	}
	/// Adjust the horizontal cursor location by this many "objects"
	public void addIndent(int del) {
		indent+=indentDel*del;
		// System.out.println("After indent+= "+del+", indent="+indent);
		newRow();
	}
	
	/// Return the total amount of vertical space used so far
	public int getHeight() {return y+pitch;}
};
