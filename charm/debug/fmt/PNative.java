/**
  A PUP'd array (or singleton) of native types.
  
  Copyright University of Illinois at Urbana-Champaign
  Written by Orion Sky Lawlor, olawlor@acm.org, 2004/1/21
*/
package charm.debug.fmt;

public class PNative extends PAbstract {
    private float[] v_float;
    private double[] v_double;
    private int[] v_int;
    private long[] v_long;
    private int len;
    PNative(int[] v) {v_int=v; v_long=null; v_float=null; v_double=null; len=v.length;}
    PNative(long[] v) {v_int=null; v_long=v; v_float=null; v_double=null; len=v.length;}
    PNative(float[] v) {v_int=null; v_long=null; v_float=v; v_double=null; len=v.length; }
    PNative(double[] v) {v_int=null; v_long=null; v_float=null; v_double=v; len=v.length; }

/// Return the element with this name
	public String toString() {
		StringBuffer ret=new StringBuffer(super.toString());
		if (len>1) ret.append("{");
		for (int i=0;i<len;i++) {
			if (v_int!=null) ret.append(""+v_int[i]);
                        if (v_long!=null) ret.append(""+v_long[i]);
			if (v_float!=null) ret.append(""+v_float[i]);
                        if (v_double!=null) ret.append(""+v_double[i]);
			if (i+1!=len)
				ret.append(", ");
		}
		if (len>1) ret.append("} ");
		return ret.toString();		
	}
	
    public int length() {return len;}
/// Assuming this is an int, return the i'th value
    public int getIntValue(int i) {return v_int[i];}
    public long getLongValue(int i) {return v_long[i];}
    public float getFloatValue(int i) {return v_float[i];}
    public double getDoubleValue(int i) {return v_double[i];}

/// Draw this object to this screen.
	public boolean draw(PDisplayStyle p,int drawStyle) {
		super.draw(p,drawStyle);
		for (int i=0;i<len;i++) {
			String v="??";
			if (v_int!=null) v=Integer.toString(v_int[i]);
                        if (v_long!=null) v=Long.toString(v_int[i]);
			if (v_float!=null) v=Float.toString(v_float[i]);
                        if (v_double!=null) v=Double.toString(v_double[i]);
			if (!p.drawString(v)) return false; // too far to right
		}
		return true;
	}
};

