/**
  A PUP'd string.
  
  Copyright University of Illinois at Urbana-Champaign
  Written by Orion Sky Lawlor, olawlor@acm.org, 2004/1/22
*/
package charm.debug.fmt;

public class PString extends PAbstract {
    private byte[] b;
    private String v;
    private String vq;
    PString(byte[] b_) {
        b = b_;
        v=new String(b_);
        vq="\""+v+"\""; // quotes around v
    }
    /// Extract raw bytes
    public byte[] getBytes() {return b;}

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

