/**
  A PUP'd string.
  
  Copyright University of Illinois at Urbana-Champaign
  Written by Orion Sky Lawlor, olawlor@acm.org, 2004/1/22
*/
package charm.debug.fmt;

public class PString extends PAbstract {
	private String v;
	private String vq;
	PString(String v_) {
		v=v_;
		vq="\""+v+"\""; // quotes around v
	}
/// Extract unquoted value
	public String getString() {return v;}
	
/// Return a string version of us
	public String toString() {return super.toString()+vq;}
	
/// Draw this object to this screen.
	public boolean draw(PDisplayStyle p,int drawStyle) {
		super.draw(p,drawStyle);
		return p.drawString(vq);
	}
};

