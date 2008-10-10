package charm.debug.inspect;

/**
   This class provides basic functionality for all the elements contained into a complex type
 */
public abstract class GenericElement extends VisitableType {
    GenericType type;
    int offset;

    /** Returns the name associated with this element */
    public abstract String getName();

    /** Returns the offset of this element inside the container */
    public int getOffset() { return offset; }

    /** Returns the data type associated with this element */
    public GenericType getType() { return type; }

    public GenericElement castNewType(GenericType t, int p) { return null; }
    
    public boolean equals(GenericElement e) { return false; }
    
    public int getPointer() { return 0; }
}
