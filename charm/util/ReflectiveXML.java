/**
 * ReflectiveXML allows a simple java class to be serialized into an XML
 * file, and later reconstructed from that file. It's main purpose it to
 * save configuration parameters or other properties into a human readable
 * format. This format allows the user to modify it manually if he so
 * chooses. Moreover, and most importantly, if the definition of the class
 * is changed in a later version of the program, the configuration file is
 * still usable, with the clause that new variables will be initialized to
 * null for classes, false for boolean, 0 for all other primitive types.
 * 
 * Currently ReflectiveXML supports generic classes, arrays, and primitive
 * types. Static variables are currently overwritten when an object is restored.
 * 
 * The two methods of interest to the user are:
 * 
 * - public static void write(FileWriter f, Object o) throws IOException
 * - public static Object read(FileReader f) throws IOException, SAXException
 * 
 * The first serializes an object "o" into an opened file "f". The second reads
 * the serialization of an object saved with the first method from an opened
 * file "f" and return the new object.
 * 
 * Created by Filippo Gioachin, April 2008.
 * gioachin@uiuc.edu
 * 
 */

package charm.util;

import java.io.*;
import java.lang.reflect.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.Stack;

public class ReflectiveXML {

	static final String INDENTATION = "  ";

	private static class Handler extends DefaultHandler {
		Stack recursion;
		Object object;

		Handler() {
			recursion = new Stack();
			object = null;
		}

		public void startElement(String namespaceURI, String localName,
								 String qualifiedName, Attributes attrs
								 ) throws SAXException {
			System.out.println("start "+localName);
			String fieldName = attrs.getValue("name");
			String arrPos = attrs.getValue("position");
			try {
				if (localName.equals("class")) {
					String type = attrs.getValue("type");
					Field fld = null;
					Object container = null;
					Object o = null;
					if (fieldName != null || arrPos != null) {
						container = recursion.peek();
						if (container != null && fieldName != null) {
							fld = container.getClass().getDeclaredField(fieldName);
							fld.setAccessible(true);
						}
					}
					if (type != null) {
						try {
							Class classtype = Class.forName(type);
							o = classtype.newInstance();
							// Blank all fields.
							Field fieldlist[] = classtype.getDeclaredFields();
							AccessibleObject.setAccessible(fieldlist, true);
							for (int i = 0; i < fieldlist.length; i++) {
								Field fld1 = fieldlist[i];
								if (! Modifier.isFinal(fld1.getModifiers())) {
									if (fld1.getType().isPrimitive()) {
										if (fld1.getType().getName().equals("boolean")) {
											fld1.set(o, new Boolean(false));
										} else {
											fld1.set(o, new Integer(0));
										}
									} else {
										fld1.set(o, null);
									}
								}
							}
						} catch (ClassNotFoundException e) {
							System.err.println(e);
						} catch (InstantiationException e) {
							System.err.println(e);
						}
					}
					if (fld != null) {
						fld.set(container, o);
					}
					if (arrPos != null) {
						Array.set(container, Integer.parseInt(arrPos), o);
					}
					recursion.push(o);
				} else if (localName.equals("array")) {
					String type = attrs.getValue("type");
					int length = Integer.parseInt(attrs.getValue("size"));
					Object[] array = null;
					if (length > 0) {
						try {
							array = (Object[])Array.newInstance(Class.forName(type), length);
						} catch (ClassNotFoundException e) {
							System.err.println(e);
						}
						if (fieldName != null || arrPos != null) {
							Object container = recursion.peek();
							if (container != null) {
								if (fieldName != null) {
									Field fld = container.getClass().getDeclaredField(fieldName);
									fld.setAccessible(true);
									fld.set(container, array);
								} else if (arrPos != null) {
									Array.set(container, Integer.parseInt(arrPos), array);
								}
							}
						}
					}
					recursion.push(array);
				} else {
					Object container = recursion.peek();
						if (container != null) {
							Field fld = null;
							if (fieldName != null) {
								fld = container.getClass().getDeclaredField(fieldName);
								fld.setAccessible(true);
							}
							if (localName.equals("int")) {
								if (fieldName != null) 
									fld.setInt(container, Integer.parseInt(attrs.getValue("value")));
								else if (arrPos != null)
									Array.setInt(container, Integer.parseInt(arrPos), Integer.parseInt(attrs.getValue("value")));
							} else if (localName.equals("long")) {
								if (fieldName != null)
									fld.setLong(container, Long.parseLong(attrs.getValue("value")));
								else if (arrPos != null)
									Array.setLong(container, Integer.parseInt(arrPos), Long.parseLong(attrs.getValue("value")));
							} else if (localName.equals("float")) {
								if (fieldName != null)
									fld.setFloat(container, Float.parseFloat(attrs.getValue("value")));
								else if (arrPos != null)
									Array.setFloat(container, Integer.parseInt(arrPos), Float.parseFloat(attrs.getValue("value")));
							} else if (localName.equals("double")) {
								if (fieldName != null)
									fld.setDouble(container, Double.parseDouble(attrs.getValue("value")));
								else if (arrPos != null)
									Array.setDouble(container, Integer.parseInt(arrPos), Double.parseDouble(attrs.getValue("value")));
							} else if (localName.equals("boolean")) {
								if (fieldName != null)
									fld.setBoolean(container, Boolean.getBoolean(attrs.getValue("value")));
								else if (arrPos != null)
									Array.setBoolean(container, Integer.parseInt(arrPos), Boolean.getBoolean(attrs.getValue("value")));
							} else if (localName.equals("byte")) {
								if (fieldName != null)
									fld.setByte(container, Byte.parseByte(attrs.getValue("value")));
								else if (arrPos != null)
									Array.setByte(container, Integer.parseInt(arrPos), Byte.parseByte(attrs.getValue("value")));
							} else if (localName.equals("char")) {
								if (fieldName != null)
									fld.setChar(container, attrs.getValue("value").charAt(0));
								else if (arrPos != null)
									Array.setChar(container, Integer.parseInt(arrPos), attrs.getValue("value").charAt(0));
							} else if (localName.equals("short")) {
								if (fieldName != null)
									fld.setShort(container, Short.parseShort(attrs.getValue("value")));
								else if (arrPos != null)
									Array.setShort(container, Integer.parseInt(arrPos), Short.parseShort(attrs.getValue("value")));
							} else if (localName.equals("java.lang.String")) {
								String str = attrs.getValue("value");
								if (str != null) str = str.replace("#$%", "\"").replace("#%$", "\n").replace("$#%", "\t");
								if (fieldName != null)
									fld.set(container, str);
								else if (arrPos != null)
									Array.set(container, Integer.parseInt(arrPos), str);
							} else {
								System.err.println("Found attribute with name '"+fld+"'!");
							}
						}
				}
			} catch (NoSuchFieldException e) {
				System.err.println("Could not resolve field "+fieldName);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				System.err.println("Why an illegal Access?!??!");
				e.printStackTrace();
			}
		}
		
		public void endElement(String namespaceURI, String localName,
							   String qualifiedName) throws SAXException {
			System.out.println("end "+localName);
			if (localName.equals("class") || localName.equals("array")) {
				Object o = recursion.pop();
				if (recursion.empty()) object = o;
			}
		}

		Object getParsedObject() {
			if (recursion.empty()) return object;
			return null;
		}
	}

	/**
	 * Read in an xml file written with the write methods of this class,
	 * and convert it into an object, which will be returned.
	 */
	public static Object read(FileReader f) throws IOException, SAXException {
		XMLReader reader = null;
		//try {
		reader = XMLReaderFactory.createXMLReader();
		Handler handler = new Handler();
		reader.setContentHandler(handler);

		InputSource source = new InputSource(f);
		reader.parse(source);
		/*		
		} catch (SAXException e) {
			System.err.println(e);
		}
		*/

		return handler.getParsedObject();
	}

	/**
	 * Write an xml file containing the serialization of the object
	 * passed. This file can be used to recreate the saved object with
	 * the read method of this class.
	 */
	public static void write(FileWriter f, Object o) throws IOException {
		f.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
		//f.write("<class type=\""+o.getClass().getName()+"\">\n");
		write(f, o, o.getClass(), null, -1, "");
		//f.write("</class>\n");
	}

	private static void write(FileWriter f, Object o, Class cls, String name, int arrPos, String prefix)
		throws IOException {
		Class realcls = cls;
		if (o != null) realcls = o.getClass();

		try {
			if (cls.isArray()) {
				f.write(prefix+"<array type=\""+cls.getComponentType().getName()+"\" name=\""+name+"\" size=\""+Array.getLength(o)+"\"");
				if (arrPos >= 0) f.write(" position=\""+arrPos+"\"");
				f.write(">\n");
				for (int j=0; j<Array.getLength(o); ++j) {
					Object elem = Array.get(o, j);
					if (elem != null) {
						write(f, elem, cls.getComponentType(), null, j, prefix+INDENTATION);
					}
				}
				f.write(prefix+"</array>\n");
			} else if (cls.isPrimitive()) {// || realCls.equals(Class.forName("java.lang.String"))) {
				f.write(prefix+"<"+cls.getName());
				if (name != null) f.write(" name=\""+name+"\"");
				f.write(" value=\""+o+"\"");
				if (arrPos >= 0) f.write(" position=\""+arrPos+"\"");
				f.write(" />\n");
			} else if (realcls.equals(Class.forName("java.lang.String"))) {
				f.write(prefix+"<"+realcls.getName());
				if (name != null) f.write(" name=\""+name+"\"");
				if (o != null) {
					f.write(" value=\""+((String)o).replace("\"", "#$%").replace("\n", "#%$").replace("\t", "$#%")+"\"");
				}
				if (arrPos >= 0) f.write(" position=\""+arrPos+"\"");
				f.write(" />\n");
			} else {
				f.write(prefix+"<class");
				if (o != null) f.write(" type=\""+realcls.getName()+"\"");
				if (name != null) f.write(" name=\""+name+"\"");
				if (arrPos >= 0) f.write(" position=\""+arrPos+"\"");
				if (o != null) {
					f.write(">\n");
					Field fieldlist[] = realcls.getDeclaredFields();
					AccessibleObject.setAccessible(fieldlist, true);
					for (int i = 0; i < fieldlist.length; i++) {
						Field fld = fieldlist[i];
						if (! Modifier.isFinal(fld.getModifiers())) {
							Object sub = fld.get(o);
							write(f, sub, fld.getType(), fld.getName(), -1, prefix+INDENTATION);
						}
					}
					f.write(prefix+"</class>\n");
				} else {
					f.write(" />\n");
				}
			}

		} catch (IllegalAccessException e) {
			System.err.println(e);
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		}
	}

}
