/**
  Copyright University of Illinois at Urbana-Champaign
  Written by Orion Sky Lawlor, olawlor@acm.org, 2004/1/21
*/
package charm.debug.fmt;

import java.awt.Graphics;
import java.awt.*;

/**
  Abstract interface for all "PUPped" objects.  These
  are local copies of objects that really exist on the 
  server.  "Objects" in this case means all possible C++ 
  types, ranging from native types like int and float to 
  arrays-of-structs-containing-vectors.
  
  The local copy has a bunch of operations, mostly related
  to debugger display, comparison, and query.
*/
public abstract class PAbstract {

/// Return the "name" label of this object.
	public String getDeepName() {
		PString s=(PString)(((PList)this).elementNamed("name"));
		return s.getString();
	}
	
/// Return a string representation of this object.
	public String toString() {
		if (getName()!=null) return getName()+"=";
		else return "";
	}

/// Name of the object, like "x".
	private String name=null; 
	public void setName(String n) {name=n;}
	public final String getName() {return name;}

/// Next object in our list (used by PList).
	private PAbstract next=null;
	public void setNext(PAbstract n) {next=n;}
	public final PAbstract getNext() {return next;}

/// Draw this object to this screen.
///  Returns false if the object could not be drawn.
	public boolean draw(PDisplayStyle p) {
		int drawStyle=p.getDrawStyle(this);
		return draw(p,drawStyle);
	}
	
	public boolean draw(PDisplayStyle p,int drawStyle) {
		if (getName()!=null) 
			p.drawString(getName()+"=");
		return true;
	}

// Stub testing driver:
	public static void main(String[] args) {
		Canvas c=new Canvas() {
		public void paint(Graphics g) {
			PDisplayStyle pd=new PDisplayStyle(g,500);
			int[] iarr={1,2,3,4};
			PNative n1=new PNative(iarr); n1.setName("Foo");
			PNative n2=new PNative(iarr); n2.setName("Bar");
			PNative n3=new PNative(iarr); n3.setName("Baz");
			PList o=new PList(); o.setName("Big");
			o.add(n1); o.add(n2);
			PList sub=new PList(); sub.setName("Sub");
			int[] sarr={23};
			sub.add(new PNative(sarr));
			sub.add(new PNative(sarr));
			sub.add(new PList());
			sub.add(new PNative(sarr));
			o.add(sub); o.add(n3);
			o.draw(pd);
		}
		public Dimension getPreferredSize() {return new Dimension(500,400); }
		};
		Frame f=new Frame("Test PAbstract");
		f.add(c); f.pack(); f.setVisible(true);
	}
};
