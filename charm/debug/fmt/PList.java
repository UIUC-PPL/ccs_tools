package charm.debug.fmt;
/**
  A PUP'd list of other PUP'd objects.
  This includes things like arrays and other collections,
  but also composite objects.
  
  Copyright University of Illinois at Urbana-Champaign
  Written by Orion Sky Lawlor, olawlor@acm.org, 2004/1/21
*/
public class PList extends PAbstract {
    /// PAbstract.getNext() forms linked list: head and tail fields.
    private PAbstract head,tail;
    private int size;
    PList() {emptyList();}
	
    /// Empty out our object list
    public void emptyList() {
	head=tail=null;
	size=0;
    }
	
    /// Add in an object to our list
    public void add(PAbstract cur) {
	if (tail==null) {
	    head=tail=cur;
	    size=1;
	} else {
	    tail.setNext(cur);
	    tail=cur;
	    size++;
	}
    }

    /// Return the total number of objects we contain
    public int size() {
	/* Old, inefficient way to do size
	int n=0;
	for (PAbstract cur=head;cur!=null;cur=cur.getNext())
	    n++;
	return n;
	*/
	return size;
    }

    /// Return our i'th object
    public PAbstract elementAt(int i) {
	int n=0;
	for (PAbstract cur=head;cur!=null;cur=cur.getNext()) {
	    if (n==i)
		return cur;
	    n++;
	}
	return null;		
    }

    /// Return the element with this name
    public PAbstract elementNamed(String s) {
	for (PAbstract cur=head;cur!=null;cur=cur.getNext()) {
	    if (cur.getName().equals(s))
		return cur;
	}
	return null;		
    }
	
    /// Return this list as a string
    public String toString() {
	StringBuffer ret=new StringBuffer(super.toString()+"{");
	for (PAbstract cur=head;cur!=null;cur=cur.getNext()) {
	    ret.append(cur.toString());
	    if (cur.getNext()!=null)
		ret.append(", ");
	}
	ret.append("} ");
	return ret.toString();		
    }
	
    // Return true if we have zero or one elements
    private boolean hasFew() {return head==tail;}

    /// Draw this object to this screen.
    public boolean draw(PDisplayStyle p,int drawStyle) {
	if (!hasFew()) {
	    super.draw(p,drawStyle); // draw our name
	    p.drawString("{");
	}
	if (drawStyle==PDisplayStyle.drawStyle_multirow) 
	    { /* indents and newlines */
		p.addIndent(1); 
		for (PAbstract cur=head;cur!=null;cur=cur.getNext()) {
		    cur.draw(p);
		    if (cur.getNext()!=null) 
			p.newRow();
		}
		p.addIndent(-1);
	    }
	else { /* all on one line */
	    for (PAbstract cur=head;cur!=null;cur=cur.getNext()) {
		if (!cur.draw(p))
		    return false;
	    }
	}
	if (!hasFew()) p.drawString("}");
	return true;
    }
};

