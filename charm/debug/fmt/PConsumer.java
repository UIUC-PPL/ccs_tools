/**
 Interpret sync codes for pup'd objects, to generate
 PAbstract higher-level objects.
 
  Copyright University of Illinois at Urbana-Champaign
  Written by Orion Lawlor, olawlor@acm.org, 2004/1/22
*/
package charm.debug.fmt;

import java.util.Vector;

public class PConsumer extends Consumer {

private PList finalList;
public PList getList() {return finalList;}

/********* State variables used during traversal: ***********/

// Comments usually apply to the *following* object--
//  this state variable holds the comment until the next object arrives.
private String lastComment;

private void applyComment(PAbstract p) {
	if (lastComment!=null) {
		p.setName(lastComment);
		lastComment=null;
	}
}

// Incoming objects get added here
private PList dest=null;

// Stack of pending destination objects (PList objects)
private Vector destStack=new Vector();
private void destPush() {
	if (dest!=null) destStack.addElement(dest);
	dest=new PList();
	applyComment(dest);
}
private void destPop() {
	PList oldDest=dest;
	int top=destStack.size()-1;
	if (top>=0) { /* There's a higher element in the stack */
		dest=(PList)destStack.elementAt(top);
		destStack.removeElementAt(top);
		if (oldDest!=null) dest.add(oldDest);
	} else { /* no higher element-- we're done */
		finalList=dest;
		dest=null;
	}
}

// Add this object to the current destination
private void add(PAbstract obj) {
	applyComment(obj);
	if (dest!=null) 
		dest.add(obj);
}

/************ External list interface **********/
public void listComment(String cmt) {
	lastComment=cmt;
}
public void listSync(int syncCode) {
	switch(syncCode) {
	// Objects: just push and pop
	case sync_begin_object: destPush(); break;
	case sync_end_object: destPop(); break;
	
	// Arrays: push twice-- outer list contains array elements, inner list contains object fields:
	case sync_begin_array: 
		destPush(); destPush(); dest=null; /* listComment("[0]"); destPush(); */ break;
	case sync_end_array: 
		destPop(); destPop(); break;
	
	// Lists: should have "index" fields to provide name
	case sync_begin_list: 
		destPush(); destPush(); break;
	case sync_end_list: 
		destPop(); destPop(); break;
	
	case sync_last_system: {
		// Clear out all stored objects-- we don't care about system stuff
		PList oldDest=dest;
		dest=new PList();
		dest.setName(oldDest.getName());
		} break;
	case sync_index: {
		// FIXME: store index in some usable form
		} break;
	case sync_item: {
		destPop(); // pop off old item
		// FIXME: comment is only valid for 1D arrays
		listComment("["+dest.size()+"]");
		destPush(); // push on new item
		} break;
	default:
		// just ignore other sync codes
		break;
	};
}

public void listByte(byte[] data) {
	add(new PString(new String(data)));
}
public void listInt(int[] data) {
	add(new PNative(data));
}
public void listFloat(float[] data) {
	add(new PNative(data));
}

};
