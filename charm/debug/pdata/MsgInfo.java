package charm.debug.pdata;

import java.nio.ByteBuffer;

import javax.swing.tree.DefaultMutableTreeNode;

import charm.debug.fmt.*;
import charm.debug.inspect.*;

// Information regarding a message in the queue
public class MsgInfo extends GenericInfo {
    int from;
    int prioBits;
    int userSize;
    MsgTypeInfo type;
    int envType;
    ChareTypeInfo chare;
    EpInfo ep;
    PList userData;
    int flags;
    int destID;
    long destPtr;
    ArrayElement idx;
    
    // Variables for array elements
    class ArrayElement {
    	int nInts;
    	int dimension;
    	int index[];
    	
    	ArrayElement(int nI, int dim, int[] idx) {
    		nInts = nI;
    		dimension = dim;
    		index = idx;
    	}
    }
    
    public static final int BREAKPOINT = 0x1;
    public static final int CONDITIONAL = 0x10;

    // The following list is exactly the same as that found in Charm++ source code
    // regarding CkEnvelopeType (in charm.h)
    public static final int NewChareMsg    =1;
    public static final int NewVChareMsg   =2;
    public static final int BocInitMsg     =3;
    public static final int ForChareMsg    =4;
    public static final int ForBocMsg      =5;
    public static final int ForVidMsg      =6;
    public static final int FillVidMsg     =7;
    public static final int DeleteVidMsg   =8;
    public static final int RODataMsg      =9;
    public static final int ROMsgMsg       =10;
    public static final int StartExitMsg   =11;
    public static final int ExitMsg        =12;
    public static final int ReqStatMsg     =13;
    public static final int StatMsg        =14;
    public static final int StatDoneMsg    =15;
    public static final int NodeBocInitMsg =16;
    public static final int ForNodeBocMsg  =17;
    public static final int ArrayEltInitMsg =18;
    public static final int ForArrayEltMsg  =19;
    public static final int ForIDedObjMsg  = 20;
    public static final int WarnMsg        = 21;
    public static final int WarnDoneMsg    = 22;
    public static final String[] envTypes = {
    	"",
    	"NewChareMsg",
    	"NewVChareMsg",
    	"BocInitMsg",
    	"ForChareMsg",
    	"ForBocMsg",
    	"ForVidMsg",
    	"FillVidMsg",
    	"DeleteVidMsg",
    	"RODataMsg",
    	"ROMsgMsg",
    	"StartExitMsg",
    	"ExitMsg",
    	"ReqStatMsg",
    	"StatMsg",
        "StatDoneMsg",
    	"NodeBocInitMsg",
    	"ForNodeBocMsg",
    	"ArrayEltInitMsg",
    	"ForArrayEltMsg",
	    "ForIDedObjMsg",
        "WarnMsg",
        "WarnDoneMsg"
    };

    MsgInfo(int f, int p, int s, MsgTypeInfo t, int mf, ChareTypeInfo c, EpInfo e, PList d, int fl) {
        from = f;
        prioBits = p;
        userSize = s;
        type = t;
        envType = mf;
        chare = c;
        ep = e;
        userData = d;
        flags = fl;
        destID = -1;
        destPtr = -1;
        idx = null;
    }
    
    public void setArrayElement(int aID, int nI, int dim, int[] index) {
    	destID = aID;
    	idx = new ArrayElement(nI, dim, index);
    }

    public void setGroupID(int gID) {
    	destID = gID;
    }
    
    public void setObjectPtr(long ptr) {
    	destPtr = ptr;
    }
    
    public boolean isConditional() {return (flags & CONDITIONAL) != 0;}
    
    public String toString() {
    	StringBuffer buf = new StringBuffer("<html><body");
    	if ((flags & CONDITIONAL) != 0) buf.append(" bgcolor=\"#b545ff\"");
    	else if ((flags & BREAKPOINT) != 0) buf.append(" bgcolor=\"#FF3333\"");
    	else if (ep.checkBox.isSelected()) buf.append(" bgcolor=\"#EEAA55\"");
    	buf.append(">");
    	buf.append(chare.getType()+"::"+ep.toString());
    	buf.append("</body>");
    	buf.append("</html>");
        return //type.toString()+"\nFrom "+from+" of size "+userSize+"\nTo: "+
            buf.toString();
        //+userData.toString()+"\n";
    }

    public void getDetails(InspectPanel panel) {
        System.out.println(Inspector.getTypeCreate(chare.getType()));
        StringBuffer buf = new StringBuffer();
        if (userData.elementNamed("Bytes") != null) {
        	panel.load(new VariableElement(type.type, "", 1, 0, 0), ByteBuffer.wrap(((PString)userData.elementNamed("Bytes")).getBytes()).order(Inspector.getByteOrder()), 0);
        	DefaultMutableTreeNode envelope = new DefaultMutableTreeNode("envelope");
        	DefaultMutableTreeNode destination;
        	envelope.add(new DefaultMutableTreeNode("Sender processor: "+from));
        	envelope.add(new DefaultMutableTreeNode("Envelope type: "+envTypes[envType]));
        	envelope.add(destination = new DefaultMutableTreeNode("Destination: "+chare.getType()+"::"+ep.toString()));
        	if (idx != null) {
        		buf.append("array id="+destID+", index="+"[");
        		for (int i=0; i<idx.dimension; ++i) buf.append(idx.index[i]+",");
        		buf.setLength(buf.length()-1);
        		buf.append("]");
        		destination.add(new DefaultMutableTreeNode(buf.toString()));
        	} else if (destID != -1) {
        		destination.add(new DefaultMutableTreeNode("group id="+destID));
        	} else if (destPtr != -1) {
        		destination.add(new DefaultMutableTreeNode("ptr=0x"+Long.toHexString(destPtr)));
        	}
        	envelope.add(new DefaultMutableTreeNode("Size: "+userSize));
        	panel.addBeforeTree(envelope);
        } else {
        	buf.append("<html><table><tr><td>Sender processor:</td><td>"+from+"</td></tr>"+
        			"<tr><td>Envelope type:</td><td>"+envTypes[envType]+"</td></tr>"+
        			"<tr><td>Destination:</td><td>"+chare.getType()+"::"+ep.toString());

        	if (idx != null) {
        		buf.append("</td></tr><tr><td></td><td>array id="+destID+", index="+"[");
        		for (int i=0; i<idx.dimension; ++i) buf.append(idx.index[i]+",");
        		buf.setLength(buf.length()-1);
        		buf.append("]");
        	} else if (destID != -1) {
        		buf.append("</td></tr><tr><td></td><td>group id="+destID);
        	} else if (destPtr != -1) {
        		buf.append("</td></tr><tr><td></td><td>ptr=0x"+Long.toHexString(destPtr));
        	}
        	buf.append("</td></tr>");
        	buf.append("<tr><td>Size:</td><td>"+userSize+"</td></tr>"+
        			"<tr><td>User data:</td><td>"+userData.toString()+
        	"</td></tr</table></html>");
        	panel.load(buf.toString());
        	System.out.println("Msg: "+type.toString()+"\n desc: "+type.getDescription());
        }
//        if (userData.elementNamed("Bytes") != null) {
//        	StringVisitor st = new StringVisitor(ByteBuffer.wrap(((PString)userData.elementNamed("Bytes")).getBytes()), 0);
//        	st.visit(type.type);
//        	System.out.println(st.getResult());
//        }
    }
}

