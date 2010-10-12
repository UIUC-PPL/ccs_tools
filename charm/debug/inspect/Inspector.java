package charm.debug.inspect;

import charm.debug.CpdUtil;
import charm.debug.ParDebug;

import java.util.Hashtable;
import java.nio.ByteOrder;

import javax.swing.JOptionPane;

/** This class has the capability to inspect a region of memory given its type,
    and return different representations of it
 */
public class Inspector {
    private static Hashtable allTypes;
    private static ByteOrder byteOrder;
    private static boolean addressSpace64;
    private static boolean bigEmulator;
    private static int sizeInt;
    private static int sizeLong;
    private static int sizeLongLong;
    private static int sizeBool;

    public static void initialize(CpdUtil server) {
        allTypes = new Hashtable();
        allTypes.put("int", new PrimitiveType("int"));
        allTypes.put("unsigned int", new PrimitiveType("unsigned int"));
        allTypes.put("short", new PrimitiveType("short"));
        allTypes.put("unsigned short", new PrimitiveType("unsigned short")); 
        allTypes.put("long", new PrimitiveType("long"));
        allTypes.put("unsigned long", new PrimitiveType("unsigned long"));
        allTypes.put("long long", new PrimitiveType("long long"));
        allTypes.put("unsigned long long", new PrimitiveType("unsigned long long"));
        allTypes.put("char", new PrimitiveType("char"));
        allTypes.put("unsigned char", new PrimitiveType("unsigned char"));
        allTypes.put("void", new PrimitiveType("void"));
        allTypes.put("float", new PrimitiveType("float"));
        allTypes.put("double", new PrimitiveType("double"));
        allTypes.put("bool", new PrimitiveType("bool"));

        /* Gather the information about the machine from the running application */
        byte[] machineType = server.sendCcsRequestBytes("debug/converse/arch", "", 0);
        System.out.println("Major = "+machineType[0]+", minor = "+machineType[1]);
        if (machineType[0] != ParDebug.MAJOR || machineType[1] != ParDebug.MINOR) {
        	System.err.println("Warning: incompatible version of Charm++ found!!!!");
        	int n = JOptionPane.showConfirmDialog(ParDebug.debugger, "The version of Charm++ used does not match that of CharmDebug\nContinue anyway?", "Version mismatch!", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        	if (n == JOptionPane.NO_OPTION) System.exit(0);
        }
        
        System.out.print("Machine is ");
        if (machineType[2] == 1) {
            System.out.print("32 bit, ");
            addressSpace64 = false;
        } else if (machineType[2] == 2) {
            System.out.print("64 bit, ");
            addressSpace64 = true;
        } else System.out.print("unknown pointer size, ");
        if ((machineType[3] & 0x3) == 1) {
            System.out.println("little endian");
            byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else if ((machineType[3] & 0x3) == 2) {
            System.out.println("big endian");
            byteOrder = ByteOrder.BIG_ENDIAN;
        } else {
        	System.err.println("Unparsable answer from remote application. How did this happen?");
        	System.exit(1);
        }
        bigEmulator = false;
        if ((machineType[3] & 0x4) != 0) {
        	bigEmulator = true;
        }
        sizeInt = machineType[4];
        sizeLong = machineType[5];
        sizeLongLong = machineType[6];
        sizeBool = machineType[7];
        System.out.println("Data size: int="+sizeInt+", long="+sizeLong+", long long="+sizeLongLong+", boolean="+sizeBool);
    }

    public static ByteOrder getByteOrder() {return byteOrder;}
    public static boolean is64bit() {return addressSpace64;}
    public static boolean isEmulated() {return bigEmulator;}
    public static int getIntSize() {return sizeInt;}
    public static int getLongSize() {return sizeLong;}
    public static int getLongLongSize() {return sizeLongLong;}
    public static int getBoolSize() {return sizeBool;}
    public static int getPointerSize() {return is64bit()?8:4;}

    public static GenericType getTypeCreate(String type) {
        GenericType result = (GenericType)allTypes.get(type);
        if (result==null) {
        	if (type.endsWith("*")) {
        		TypedefType res = new TypedefType();
        		allTypes.put(type, res);
        		res.build(type, type);
        		result = res;
        	} else {
        		DataType res = new DataType();
        		allTypes.put(type, res);
        		res.build(type);
        		result = res;
        	}
        }
        return result;
    }

    public static GenericType getType(String type) {
        GenericType result = (GenericType)allTypes.get(type);
        return result;
    }

    public static void putType(String type, GenericType dt) {
        allTypes.put(type, dt);
    }
}
