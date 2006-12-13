package charm.debug.inspect;

/** Prototype abstract class for all types that can be visited by a TypeVisitor */
public abstract class VisitableType {

    /** Method used to visit a type */
    public abstract void visit(TypeVisitor v);
}
