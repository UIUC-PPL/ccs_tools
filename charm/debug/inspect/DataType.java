package charm.debug.inspect;

import charm.debug.ParDebug;
import java.util.Vector;
import java.nio.ByteBuffer;
import java.util.regex.*;

/** This class contains a basic class type of the program being debugged.
 */
public class DataType extends GenericType {
    String desc;
    Vector superclasses;
    Vector variables;
    boolean isVirtual;
    boolean isUnion;
    boolean isEnum;
    int size;
    String thisType;

    //static final Matcher functionMatcher = Pattern.compile("[^()]+ \\Q(*)(\\E.*\\)").matcher("");
    static final Matcher functionMatcher = Pattern.compile(".*\\Q(*)(\\E.*\\)").matcher("");

    public void setGdbType(String s) { thisType = s; }
    
/*
   cleanUnwantedChar is a utility function that discards text such as
   gdb, newline character from string, to get (integer) address from the string
*/

    public String cleanUnwantedChar(String s){
        if(s.contains("(gdb)")) {
            s = s.replace("(gdb)","");
            s = s.replace("\n","");
        }
        return s;
    }

/*
   This function finds the offset by identifying a valid offsetValue
   and doing required offset calculation with pos
*/
    public int findOffset(String offsetValue, int pos){
         int offset = -1;
         if (offsetValue.indexOf(" 0x") != -1) {
            int start = offsetValue.indexOf(" 0x")+1;
            int end = offsetValue.indexOf(" ", start);
            if (end == -1) end = offsetValue.length()-1;
            String temp=cleanUnwantedChar(offsetValue.substring(start,end));
            offset = Integer.decode(temp).intValue();
         }
         if(offset != -1) {
            offset = offset - pos;
         }
         return offset;
    }

    public GenericType build(String n, String d) {
        name = n;
        if (n == null) name = "";
        desc = d;
        isVirtual = false;
        isUnion = false;
        isEnum = false;
        size = 0;
        superclasses = new Vector();
        variables = new Vector();
        if (desc == null) desc = getDescription(name);
        if (desc == null) return this;
        GenericType resultType = this;

        //System.out.println(desc);
        if (desc.indexOf("virtual") != -1) isVirtual = true;
        int opening = desc.indexOf("{");
        if (opening == -1) {
            System.out.println(name+"|"+desc+"|");
            desc = null;
            return this;
        }
        String[] firstLine = desc.substring(0, opening-1).split("\\s:\\s");
        if (firstLine.length > 2) {
            desc = null;
            return this;
        }
        int nameStart = firstLine[0].trim().indexOf(" ");
        String type = firstLine[0].trim();
        if (nameStart > 0) type = type.substring(0, nameStart);
        if (!firstLine[0].trim().substring(nameStart+1).equals(name)) {
            TypedefType dt = new TypedefType();
            int endClass = desc.lastIndexOf("}");
            int pointers = desc.substring(endClass+1).trim().length();
            dt.build(name, this, pointers);
            Inspector.putType(name, dt);
            name = firstLine[0].trim().substring(nameStart+1);
            Inspector.putType(name, this);
            resultType = dt;
            // if we are dealing with a pointer typedef, we cannot dereference anymore, so we null the thisType string
            if (pointers > 0) thisType = null;
            //System.out.println("|"+name+"|"+firstLine[1].trim().substring(nameStart+1));
            //System.out.println(desc);
            //System.out.println("The returned info does not match the requested type");
            //desc = null;
            //return;
        }
        if (type.equals("class") || type.equals("struct")) {

        } else if (type.equals("union")) {
            isUnion = true;
        } else if (type.equals("enum")) {
            isEnum = true;
        } else {
            System.out.println(name);
            System.out.println(desc);
            System.out.println("The returned info is not correct, type "+type);
            desc = null;
            return this;
        }
        String line;
        if (firstLine.length == 2) {
            line = firstLine[1].trim()+",";
            //System.out.println("|"+line+"|");
            int startCut = 0;
            int templateDepth = 0;
            for (int endCut=0; endCut < line.length(); ++endCut) {
                if (line.charAt(endCut) == '<') templateDepth++;
                if (line.charAt(endCut) == '>') templateDepth--;
                if (line.charAt(endCut) == ',' && templateDepth==0) {
                    // perform a cut of a superclass definition
                    String superName = line.substring(startCut,endCut);
                    if (superName.startsWith("public") || superName.startsWith("protected") ||
                        superName.startsWith("private"))
                        superName = superName.substring(superName.indexOf(" ")+1);
                    System.out.println("superName: "+superName);
                    GenericType superType = Inspector.getType(superName);
                    if (superType == null) {
                        superType = new DataType();
                        Inspector.putType(superName, superType);
                        superType.build(superName, getDescription(superName));
                    }
                    if (superType instanceof DataType) {
                        if (((DataType)superType).isVirtual) isVirtual=true;
                    } else if (superType instanceof TypedefType) {
                        if (((TypedefType)superType).isVirtual()) isVirtual=true;
                    }

                    String offsetValue = ParDebug.infoCommand("print (class "+superName+"*)((class "+name+"*)"+ParDebug.dataPos+")\n");
                    System.out.println("info:print (class "+superName+"*)((class "+name+"*)"+ParDebug.dataPos+") = "+offsetValue);
                    int offset = findOffset(offsetValue, ParDebug.dataPos);
                    superclasses.add(new SuperClassElement(superType, offset));
                    endCut++;
                    startCut = endCut+1;
                }
            }
        }
        //for (int i=0; i<superclasses.size(); ++i) {
        //    System.out.println("superclass: |"+superclasses.elementAt(i)+"|");
        //}
        
        // set size of this type
        String typeSize = ParDebug.infoCommand("print/x sizeof(class "+name+")\n");
        System.out.println("info:print/x sizeof(class "+name+") = "+typeSize);
        if (typeSize.indexOf("=") == -1) {
            typeSize = ParDebug.infoCommand("print/x sizeof("+name+")\n");
            System.out.println("info:print/x sizeof("+name+") = "+typeSize);
        }
        int tempSize = findOffset(typeSize, 0);
        if(tempSize!=-1){
          size = tempSize;
        }
        String[] piece = desc.substring(opening).split("[{:;][\n\r]");
        for (int i=1; i<piece.length-1; ++i) {
            line = piece[i].trim();
            System.out.println("|"+line+"|");
            if (line.equals("public")) continue;
            if (line.equals("protected")) continue;
            if (line.equals("private")) continue;
            if (line.indexOf("(") != -1) continue;
            if (line.indexOf("static") != -1) continue;
            //System.out.println("variable: "+line.trim());
            if (line.startsWith("volatile")) line = line.substring(9);
            int variableStart = line.lastIndexOf(" ");
            //boolean isTemplate = (line.indexOf("<") != -1);
            //if (isTemplate) System.out.println("resolve template");
            int arrayStart = line.indexOf("[");
            boolean isArray = (arrayStart != -1);
            int pointerCount = 0;
            
            while (line.charAt(variableStart+1+pointerCount) == '*') pointerCount++;
            if (variableStart < 0) continue;
            String varType = line.substring(0, variableStart).trim();
            if (varType.startsWith("const")) {
            	varType = varType.substring(varType.indexOf(" ")+1);
            }
            if (varType.startsWith("class") || varType.startsWith("struct")) {
                varType = varType.substring(varType.indexOf(" ")+1);
            }
            String varName = line.substring(variableStart+pointerCount+1, (isArray?arrayStart:line.length()));
            int arraySize = 0;
            if (isArray) arraySize = Integer.parseInt(line.substring(arrayStart+1,line.indexOf("]")));
            System.out.println("Result: |"+varType+"|"+varName+"|"+(pointerCount>0?"pointer":"")+(isArray?Integer.toString(arraySize):""));
            GenericType dt = null;
            if (varType.startsWith("{")) {
                System.out.println("Conctructing anonymous type");
                // anonymous type
                StringBuffer concatenation = new StringBuffer();
                int anonymousDepth = 1;
                int startCut = piece[i].indexOf("{");
                int j = startCut+1;
                for ( ; i<piece.length-1; ++i) {
                    // 1) concatenate all remaining pieces together
                    concatenation.append(piece[i]).append(";\n");
                    System.out.println("line: "+piece[i]);
                    // 2) find where it ends the anonymous type
                    for ( ; j < concatenation.length(); ++j) {
                        if (concatenation.charAt(j) == '{') anonymousDepth++;
                        if (concatenation.charAt(j) == '}') anonymousDepth--;
                        System.out.println("j: "+j+" val: "+concatenation.charAt(j)+" depth: "+anonymousDepth);
                        if (anonymousDepth==0) {
                            // 3) create the anonymous type
                            String anonymousDesc = concatenation.substring(0, j+1);
                            System.out.println("anon desc: "+anonymousDesc);
                            dt = new DataType();
                            dt.build("", anonymousDesc);
                            break;
                        }
                    }
                    if (j < concatenation.length()) {
                        // this was the last piece of the anonymous type
                        varName = concatenation.substring(j+1,concatenation.indexOf(";",j+1)).trim();
                        break;
                    }
                }
            } else {
                dt = Inspector.getType(varType);
                if (dt == null) {
                	// this type is not yet built, build it
                	String varTypeDesc;
                	if (varType.startsWith("._") || varType.contains("::._")) varTypeDesc = getDescription("(("+name+"*)0)->"+varName);
                	else varTypeDesc = getDescription(varType);
                	if (varTypeDesc == null) {
                		dt = new UnknownType();
                	} else {
                    	int lastLine = varTypeDesc.lastIndexOf('\n');
                    	if (lastLine < 0) lastLine = 0;
                		if (functionMatcher.reset(varTypeDesc.substring(lastLine+1)).matches()) {
                			// function pointer style
                			dt = new FunctionType();
                		} else if (varTypeDesc.indexOf("{") != -1) {
                			// normal class/struct/union definition
                			dt = new DataType();
                			String tmp;
                			if (thisType == null) {
                				tmp = "(("+name+"*)"+ParDebug.dataPos+")";
                				String without = ParDebug.infoCommand("print "+tmp+"\n");
                				if (without.indexOf(" 0x") == -1) tmp = "((struct "+name+"*)"+ParDebug.dataPos+")";  
                			} else {
                				tmp = thisType;
                			}
                			if (pointerCount == 0) ((DataType)dt).setGdbType(tmp+"->"+varName);
                		} else {
                			// the only thing remaining should be a typedef...
                			dt = new TypedefType();
                		}
                	}
                	Inspector.putType(varType, dt);
                	dt = dt.build(varType, varTypeDesc);
                }
            }
            int baseOffset = ParDebug.dataPos;
            String offsetValue = ParDebug.infoCommand("print &((class "+name+"*)"+ParDebug.dataPos+")->"+varName+"\n");
            System.out.println("info:print &((class "+name+"*)"+ParDebug.dataPos+")->"+varName+" = "+offsetValue);
            if (offsetValue.indexOf(" 0x") == -1) {
                offsetValue = ParDebug.infoCommand("print &(("+name+"*)"+ParDebug.dataPos+")->"+varName+"\n");
                System.out.println("info:print &(("+name+"*)"+ParDebug.dataPos+")->"+varName+" = "+offsetValue);
            }
            if (offsetValue.indexOf(" 0x") == -1 && thisType != null) {
            	String bo = ParDebug.infoCommand("print &"+thisType+"\n");
            	int start = bo.indexOf(" 0x")+1;
            	if (start > 0) {
            		int end = bo.indexOf(" ",start);
            		if (end == -1) end = bo.length()-1;
            		baseOffset = Integer.decode(bo.substring(start,end)).intValue();
                	offsetValue = ParDebug.infoCommand("print &"+thisType+"->"+varName+"\n");
                	System.out.println("info:print &"+thisType+"->"+varName+" = "+offsetValue);
            	} else {
            		System.out.println("WARNING: Could not get base offset for data!!!!!");
            	}
            }
            int offset = findOffset(offsetValue, baseOffset);
            variables.add(new VariableElement(dt, varName, arraySize, pointerCount, offset));
        }
        return resultType;
    }

    public int getSize() {
        return size;
    }

    public int getChildren() {
        return superclasses.size() + variables.size();
    }

    public GenericElement getChild(int i) {
        if (i >= superclasses.size()) {
            i -= superclasses.size();
            if (i >= variables.size()) return null;
            return (VariableElement)variables.elementAt(i);
        }
        return (SuperClassElement)superclasses.elementAt(i);
    }

    public String getValue(TypeVisitor v) { return null; }

    public boolean hasSuperclass(GenericType s) {
    	for (int i=0; i<superclasses.size(); ++i) {
    		SuperClassElement sup = (SuperClassElement)superclasses.elementAt(i);
    		if (sup.getType().equals(s)) return true;
    		GenericType gt = sup.getType().getType();
    		if (gt instanceof DataType && ((DataType)gt).hasSuperclass(s)) return true;
    	}
    	return false;
    }
    
    public int getSuperclassOffset(GenericType s) {
    	for (int i=0; i<superclasses.size(); ++i) {
    		SuperClassElement sup = (SuperClassElement)superclasses.elementAt(i);
    		if (sup.getType().equals(s)) return sup.getOffset();
    		GenericType gt = sup.getType().getType();
    		if (gt instanceof DataType) {
    			int result = ((DataType)gt).getSuperclassOffset(s);
    			if (result >= 0) return result + sup.getOffset();
    		}
    	}
    	return -1;
    }
    
    public boolean hasVariable(String name) {
    	for (int i=0; i<variables.size(); ++i) {
    		VariableElement el = (VariableElement)variables.elementAt(i);
    		if (el.getName().equals(name)) return true;
    	}
    	for (int i=0; i<superclasses.size(); ++i) {
    		SuperClassElement sup = (SuperClassElement)superclasses.elementAt(i);
    		GenericType gt = sup.getType().getType();
    		if (gt instanceof DataType && ((DataType)gt).hasVariable(name)) return true;
    	}
    	return false;
    }
    
    public int getVariableOffset(String name) {
    	for (int i=0; i<variables.size(); ++i) {
    		VariableElement el = (VariableElement)variables.elementAt(i);
    		if (el.getName().equals(name)) return el.getOffset();
    	}
    	for (int i=0; i<superclasses.size(); ++i) {
    		SuperClassElement sup = (SuperClassElement)superclasses.elementAt(i);
    		GenericType gt = sup.getType().getType();
    		if (gt instanceof DataType) {
    			int result = ((DataType)gt).getVariableOffset(name);
    			if (result >= 0) return result + sup.getOffset();
    		}
    	}
    	return -1;
    }
    
    public VariableElement getVariable(String name) {
    	for (int i=0; i<variables.size(); ++i) {
    		VariableElement el = (VariableElement)variables.elementAt(i);
    		if (el.getName().equals(name)) return el;
    	}
    	for (int i=0; i<superclasses.size(); ++i) {
    		SuperClassElement sup = (SuperClassElement)superclasses.elementAt(i);
    		GenericType gt = sup.getType().getType();
    		if (gt instanceof DataType) {
    			VariableElement result = ((DataType)gt).getVariable(name);
    			if (result != null) return result;
    		}
    	}
    	return null;
    }
    
    public String toString(String ind) {
        if (name == null) return "";
        if (desc == null) return "(no info available)";
        StringBuffer buf = new StringBuffer();
        if (isEnum) return "enum "+name;
        String indent = "  " + ind;
        if (isUnion) buf.append("union ");
        else buf.append("class ");
        buf.append(name).append(" {\n");
        if (isVirtual) buf.append(indent).append("(virtual)").append("\n");

        // print superclasses
        for (int i=0; i<superclasses.size(); ++i) {
            buf.append(indent).append("super ").append(((SuperClassElement)superclasses.elementAt(i)).toString(indent)).append("\n");
        }

        // print variables
        for (int i=0; i<variables.size(); ++i) {
            buf.append(indent).append(((VariableElement)variables.elementAt(i)).toString(indent)).append("\n");
        }

        buf.append(ind).append("}");
        return buf.toString();
    }

    public String memoryToString(String ind, ByteBuffer mem, int start) {
        if (name == null) return "?";
        if (desc == null) return "(no info available)";
        System.out.println(name+" start = "+start);
        StringBuffer buf = new StringBuffer();
        String indent = "  " + ind;
        //if (isUnion) buf.append("union ");
        //else buf.append("class ");
        //buf.append(name);
        buf.append("{\n");
        if (isVirtual) {
            buf.append(indent).append("virtual table: ").append(printPointer(mem, start)).append("\n");
        }

        // print superclasses
        for (int i=0; i<superclasses.size(); ++i) {
            buf.append(indent).append("super ").append(((SuperClassElement)superclasses.elementAt(i)).memoryToString(indent,mem,start)).append("\n");
        }

        // print variables
        for (int i=0; i<variables.size(); ++i) {
            buf.append(indent).append(((VariableElement)variables.elementAt(i)).memoryToString(indent,mem,start)).append("\n");
        }

        if (isEnum) buf.append(indent).append("enum\n");
        buf.append(ind).append("}");
        return buf.toString();
    }

    public void visit(TypeVisitor v) {
        //if (name == null) v.setUnknown();
        if (name == null || desc == null) return; //v.setNoInfo();
        v.push();
        if (isVirtual) {
            v.addType("virtual table");
            v.addValue(v.printPointer());
        }
        
        // print superclasses
        for (int i=0; i<superclasses.size(); ++i) {
            ((SuperClassElement)superclasses.elementAt(i)).visit(v);
        }

        // print variables
        for (int i=0; i<variables.size(); ++i) {
            ((VariableElement)variables.elementAt(i)).visit(v);
        }

        //if (isEnum) v.add("enum");
        v.pop();
    }
}
