package charm.debug.pdata;

import charm.debug.ParDebug;
import charm.debug.fmt.PList;
import charm.debug.inspect.Inspector;
import charm.debug.inspect.InspectPanel;

// Information regarding a message in the queue
public class MsgStackInfo extends GenericInfo {
	int level;
	long obj;
	long msg;

    MsgStackInfo(int _level, long _obj, long _msg) {
    	obj = _obj;
    	msg = _msg;
    	level = _level;
    }

    public String toString() {
    	StringBuffer buf = new StringBuffer("<html><body>");
    	buf.append("Message 0x"+msg);
    	buf.append("</body>");
    	buf.append("</html>");
        return //type.toString()+"\nFrom "+from+" of size "+userSize+"\nTo: "+
            buf.toString();
        //+userData.toString()+"\n";
    }

    public void getDetails(InspectPanel panel) {
        /*panel.load("<html>Chare pointer: 0x"+Long.toHexString(obj)+"<br>"+
            "Message pointer: 0x"+Long.toHexString(msg)+
            "</html>");*/
    	panel.load(ParDebug.debugger.getSelectedPe(), new Slot(obj), null);
    }
}
