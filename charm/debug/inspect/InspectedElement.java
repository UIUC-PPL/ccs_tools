package charm.debug.inspect;

public class InspectedElement {
	GenericElement e;
	String value;
	
	public InspectedElement(GenericElement el, String v) {
		e = el;
		value = v;
	}
	
	public boolean isPointer() {
		return (e.getPointer() + e.getType().getPointer()) > 0;
	}
	
	public String toString() {
		String t = e.getType().getName();
		String n = e.getName();
		int p = e.getPointer() + e.getType().getPointer();
    	StringBuffer result = new StringBuffer();
        if (t != null) result.append(t);
        for (int i=0; i<p; ++i) result.append('*');
        if (n != null) result.append(' ').append(n);
        result.append(" = ");
        if (value != null) result.append(value);
        return result.toString();
	}
}
