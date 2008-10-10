package charm.debug;

import java.io.*;
import java.text.ParseException;
import java.util.Vector;

import javax.swing.JOptionPane;

import charm.debug.inspect.DataType;
import charm.debug.inspect.GenericType;
import charm.debug.inspect.Inspector;
import charm.debug.inspect.VariableElement;
import charm.debug.pdata.ChareInfo;
import charm.debug.pdata.EpInfo;

public class PythonScript {
  	
	public static final String beginning = "def method(self):";
	private static int seqNumber = 0;
	private GdbProcess info;
	private String code;
	private String parsedString;
	private ChareInfo chare;
	private Vector selectedEPs;

	public PythonScript(GdbProcess gdb) {
		info = gdb;
		chare = null;
		code = null;
		selectedEPs = new Vector();
	}
	
	public String parseCode(String str) throws ParseException {
		code = str;
		parsedString = str;
		if (!parsedString.startsWith(beginning)) {
			//JOptionPane.showMessageDialog(this, "The code must start with '"+beginning+"'", "Error", JOptionPane.ERROR_MESSAGE);
			throw new ParseException("The code must start with '"+beginning+"'", 0);
		}
		// insert unique identifier to method name
		parsedString = parsedString.substring(0,10)+seqNumber+parsedString.substring(10);
		seqNumber++;
		// parse the code, and make the appropriate corrections
		int first = 0, last = 0;
		while ((first = parsedString.indexOf("charm.get", first)) != -1) {
			first += 9;
			int startArgs = parsedString.indexOf('(', first);
			if (parsedString.startsWith("Static",first)) {
				int splitPoint = parsedString.indexOf(")",startArgs);
				String name = parsedString.substring(startArgs+1,splitPoint).trim();
				System.out.println("Static name = "+name);
				String address = info.infoCommand("print &"+name.substring(1,name.length()-1)+"\n");
				int startAddress = address.indexOf("0x");
				if (startAddress == -1) {
					//JOptionPane.showMessageDialog(this, "Static variable "+name+" not found", "Error", JOptionPane.ERROR_MESSAGE);
					throw new ParseException("Static variable "+name+" not found", 0);
				}
				System.out.println("Reply: "+address);
				char retType = 'p';
				if (address.indexOf("(int *)") != -1) retType = 'i';
				else if (address.indexOf("(char *)") != -1) retType = 'b';
				else if (address.indexOf("(short *)") != -1) retType = 'h';
				else if (address.indexOf("(long *)") != -1) retType = 'l';
				else if (address.indexOf("(float *)") != -1) retType = 'f';
				else if (address.indexOf("(double *)") != -1) retType = 'd';
				else if (address.indexOf("(char **)") != -1) retType = 's';
				parsedString = parsedString.substring(0,startArgs+1)+
					","+address.substring(startAddress).trim()+",'"+retType+"'"+
					parsedString.substring(splitPoint);
			} else {
				int typeEnd = parsedString.indexOf(',', startArgs);
				last = typeEnd;
				String type = parsedString.substring(last+1,parsedString.indexOf(',',last+1)).trim();
				GenericType t = Inspector.getTypeCreate(type);
				System.out.println("get type "+type+" (last="+last+")");
				last = parsedString.indexOf(',',last+1);
				int splitPoint = parsedString.indexOf(')',last+1);
				if (parsedString.startsWith("Array",first)) {
					//int num;
					//try {
					//num = Integer.parseInt(parsedString.substring(last+1,parsedString.indexOf(')',last+1)).trim());
					//}
					int size = t.getSize();
					parsedString = parsedString.substring(0,typeEnd+1)+
						(size)+ parsedString.substring(last);
				} else if (parsedString.startsWith("Value",first)) {
					String name = parsedString.substring(last+1,parsedString.indexOf(')',last+1)).trim();
					if (! (t instanceof DataType)) {
						//JOptionPane.showMessageDialog(this, "Invalid parameter '"+type+"' to function getValue", "Error", JOptionPane.ERROR_MESSAGE);
						throw new ParseException("Invalid parameter '"+type+"' to function getValue", 0);
					}
					DataType dt = (DataType)t;
					int offset = dt.getVariableOffset(name);
					if (offset < 0) {
						//JOptionPane.showMessageDialog(this, "Invalid variable '"+name+"' in type '"+type+"' to function getValue", "Error", JOptionPane.ERROR_MESSAGE);
						throw new ParseException("Invalid variable '"+name+"' in type '"+type+"' to function getValue", 0);
					}
					VariableElement element = dt.getVariable(name);
					GenericType resultType = element.getType();
					char charType;
					System.out.println("Resulting type: "+resultType.getName()+", ptr="+resultType.getPointer()+" ,size="+resultType.getSize());
					if (resultType.getPointer() > 0 || element.getPointer() > 0 || element.getArray() > 0) charType = 'p';
					else if (resultType.getName().equals("int")) charType = 'i';
					else if (resultType.getName().equals("char")) charType = 'b';
					else if (resultType.getName().equals("short")) charType = 'h';
					else if (resultType.getName().equals("long")) charType = 'l';
					else if (resultType.getName().equals("float")) charType = 'f';
					else if (resultType.getName().equals("double")) charType = 'd';
					else if (resultType.getName().equals("bool")) charType = 'i';
					else charType = 'p';
					parsedString = parsedString.substring(0,typeEnd+1)+
						offset+",'"+charType+"'"+
						parsedString.substring(splitPoint);
				} else if (parsedString.startsWith("Cast",first)) {
					String newtype = parsedString.substring(last+1,parsedString.indexOf(')',last+1)).trim();
					GenericType nt = Inspector.getTypeCreate(newtype);
					if (! (t instanceof DataType)) {
						//JOptionPane.showMessageDialog(this, "Invalid parameter '"+type+"' to function getCast", "Error", JOptionPane.ERROR_MESSAGE);
						throw new ParseException("Invalid parameter '"+type+"' to function getCast", 0);
					}
					if (! (nt instanceof DataType)) {
						//JOptionPane.showMessageDialog(this, "Invalid parameter '"+newtype+"' to function getCast", "Error", JOptionPane.ERROR_MESSAGE);
						throw new ParseException("Invalid parameter '"+newtype+"' to function getCast", 0);
					}
					DataType dt = (DataType)t;
					DataType ndt = (DataType)nt;
					int offset = 0;
					if (dt.hasSuperclass(ndt)) offset = dt.getSuperclassOffset(ndt);
					else if (ndt.hasSuperclass(dt)) offset = ndt.getSuperclassOffset(dt);
					else {
						//JOptionPane.showMessageDialog(this, "Could not cast between '"+type+"' and '"+newtype+"'", "Error", JOptionPane.ERROR_MESSAGE);
						throw new ParseException("Could not cast between '"+type+"' and '"+newtype+"'", 0);
					}
					parsedString = parsedString.substring(0,typeEnd+1)+
						offset+parsedString.substring(splitPoint);
				}
			}
		}
		return parsedString;
  	}

	public String loadPythonCode(File f) throws IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(f));
		char[] buf = new char[1024];
		int numRead=0;
		while((numRead=reader.read(buf)) != -1){
			fileData.append(buf, 0, numRead);
		}
		reader.close();
		return fileData.toString();
	}
	
	public void savePythonCode(File f, String code) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		writer.write(code);
		writer.close();
	}

	public String getOriginalCode() {
		return code;
	}
	public String getText() {
		return parsedString;
	}
	public String getMethod() {
		return parsedString.substring(4, parsedString.indexOf('('));
	}

	public void setChare(ChareInfo c) {
		chare = c;
	}
	public int getChareGroup() {
		return chare.getGroupID();
	}
	public ChareInfo getChare() {
		return chare;
	}
	
	public void addEP(EpInfo e) {
		selectedEPs.add(e);
	}
	public void removeEP(EpInfo e) {
		selectedEPs.remove(e);
	}
  	public Vector getSelectedEPs() {
		return selectedEPs;
	}
}
