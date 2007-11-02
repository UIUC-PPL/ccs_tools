/** Print incoming Cpd data to stdout, in the 
same format at PUP::toText
*/
package charm.debug.fmt;

public class Verbose extends Consumer {

private int indent=0;
private String startLine(int del) {
	StringBuffer ret=new StringBuffer();
	for (int i=0;i<indent+del;i++) 
		ret.append("\t");
	return ret.toString();
}
private String startLine() {return startLine(0);}

// Begin/end a nested collection
private void beginCollection(String type) {
	System.out.println(startLine()+type+" = {");
	indent++;
}
// Begin/end a nested collection
private void endCollection(String type) {
	indent--;
	System.out.println(startLine()+"} "+type);
}

// Begin/end an array of native types (no recursion)
private String beginArray(String type,int len) {
	if (len>1) {
		beginCollection(type+" array");
		System.out.print(startLine());
		return "";
	}
	else {
		System.out.print(startLine()+type+"=");
		return "";
	}
}
private void endArray(String type,int len) {
	if (len>1) {
		System.out.println();
		endCollection(type+" array");
	}
	else 
		System.out.println();
}

public void listByte(byte[] data) {
	System.out.println(startLine()+"byte[] = '"+new String(data)+"'");
}
public void listInt(int[] data) {
	String indent=beginArray("int",data.length);
	for (int i=0;i<data.length;i++)
		System.out.print(indent+data[i]+", ");
	endArray("int",data.length);
}
public void listFloat(float[] data) {
	String indent=beginArray("float",data.length);
	for (int i=0;i<data.length;i++) 
		System.out.print(indent+data[i]+", ");
	endArray("float",data.length);
}
public void listLong(long[] data) {
	String indent=beginArray("long",data.length);
	for (int i=0;i<data.length;i++)
		System.out.print(indent+data[i]+", ");
	endArray("long",data.length);
}
public void listDouble(double[] data) {
	String indent=beginArray("double",data.length);
	for (int i=0;i<data.length;i++) 
		System.out.print(indent+data[i]+", ");
	endArray("double",data.length);
}
public void listComment(String cmt) {
	System.out.println(startLine()+"//"+cmt+"");
}
public void listSync(int syncCode) {
	switch(syncCode) {
	case sync_begin_array: beginCollection("array"); break;
	case sync_end_array: endCollection("array"); break;
	
	case sync_begin_list: beginCollection("list"); break;
	case sync_end_list: endCollection("list"); break;
	
	case sync_begin_object: beginCollection("object"); break;
	case sync_end_object: endCollection("object"); break;
	
	case sync_index: /* element index */
		System.out.println("\n"+startLine(-1)+"/* Index */");
		break;
	case sync_item: /* element itself */
		System.out.println(startLine(-1)+",/* Element */");
		break;
	
	case sync_last_system: /* end of system gobbledygook */
		System.out.println(startLine()+",/* --- end of system crap ---- */");
		break;
	
	default:
		System.out.println(startLine()+"  (sync=0x"+Integer.toHexString(syncCode)+")");
		break;
	};
}

};
