/**
 Abstract class to decode byte-level formatting of 
 PUP_fmt, used by CpdList's ccs_list_items.fmt.
 
 To use this class, inherit from it, override the 
 "list*" routines you're interested in, and call decode.
 
 Note that this class only deals with the low-level,
 byte-by-byte formatting issues.  It does not interpret
 sync codes, and so does not generate higher-level objects.
 
  Copyright University of Illinois at Urbana-Champaign
  Written by Orion Lawlor, olawlor@acm.org, 2004/1/20
*/
package charm.debug.fmt;

import charm.ccs.CcsServer; // for unpacking utility routines

public class Consumer {
// These MUST match the PUP_fmt declarations in ccs-builtins.C
public static final int 
		lengthLen_single=0, // field is a single item
    		lengthLen_byte=1, // following 8 bits gives length of array
		lengthLen_int=2, // following 32 bits gives length of array
		lengthLen_long=3 // following 64 bits gives length of array (unimpl)
;
public static final int  
		typeCode_byte=0, // unknown data type: nItems bytes
		typeCode_int=2, // 32-bit integer array: nItems ints
		typeCode_float=5, // 32-bit floating-point array: nItems floats
		typeCode_comment=10, // comment/label: nItems byte characters
		typeCode_sync=11 // synchronization code
;

// These MUST match the sync declarations in pup.h
public static final int  
  sync_builtin=0x70000000, // Built-in, standard sync codes begin here
  sync_begin=sync_builtin+0x01000000, // Sync code at start of collection
  sync_end=sync_builtin+0x02000000, // Sync code at end of collection
  sync_last_system=sync_builtin+0x09000000, // Sync code at end of "system" portion of object
  sync_array_m=0x00100000, // Sync mask for linear-indexed (0..n) array
  sync_list_m=0x00200000, // Sync mask for some other collection
  sync_object_m=0x00300000, // Sync mask for general object
  
  sync_begin_array=sync_begin+sync_array_m,
  sync_begin_list=sync_begin+sync_list_m, 
  sync_begin_object=sync_begin+sync_object_m, 
  
  sync_end_array=sync_end+sync_array_m, 
  sync_end_list=sync_end+sync_list_m, 
  sync_end_object=sync_end+sync_object_m, 
  
  sync_item=sync_builtin+0x00110000, // Sync code for an item in a collection
  sync_index=sync_builtin+0x00120000, // Sync code for index of item in a collection
  sync_last=0;

public void listByte(byte[] data) {}
public void listInt(int[] data) {}
public void listFloat(float[] data) {}
public void listComment(String cmt) {}
public void listSync(int syncCode) {}

    private void bad(String why) {
      System.out.println("FATAL ERROR in CpdList Decoding> "+why);
      System.exit(1);
    }

/** Decode this buffer, sending the resulting list items
  to this object.
*/
public void decode(byte[] buf) {
	decode(buf,0,buf.length);
}

/**
 Decode bytes[off ... off+length], sending the resulting
 list items to this object.
*/
public void decode(byte[] buf,int off,int buf_length) 
{
      int end=off+buf_length;
      while (off<end) {
        byte intro=buf[off++];
	int lengthLen=(intro>>4)&0x3;
	int typeCode=intro&0xF;
	// System.out.println("                 l="+lengthLen+" t="+typeCode+" o="+off);
	int i,length=0;
	final int intLen=4;
	switch(lengthLen) {
	case lengthLen_single: length=1; break;
	case lengthLen_byte: length=0xff & (int)buf[off++]; break;
	case lengthLen_int: length=CcsServer.readInt(buf,off); off+=intLen; break;
	default: bad("Unrecognized lengthLen "+lengthLen);
	};
	switch(typeCode) {
	case typeCode_byte: {
		byte[] data=new byte[length];
		for (i=0;i<length;i++) data[i]=buf[off++];
		listByte(data);
		} break;
	case typeCode_int: {
		int[] data=new int[length];
		for (i=0;i<length;i++) {
			data[i]=CcsServer.readInt(buf,off);
			off+=intLen;
		}
		listInt(data);
		} break;
	case typeCode_float: {
		float[] data=new float[length];
		for (i=0;i<length;i++) {
			data[i]=CcsServer.readFloat(buf,off);
			off+=intLen;
		}
		listFloat(data);
		} break;
	case typeCode_comment:
		listComment(new String(buf,off,length));
		off+=length;
		break;
	case typeCode_sync:
		listSync(CcsServer.readInt(buf,off)); 
		off+=intLen;
		break;
	default:
		bad("Unrecognized typeCode "+typeCode);
	};
      }
}
};
