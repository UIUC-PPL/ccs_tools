package charm.util;

/*Vector is really quite a simple class-- it's just
three cartesian coordinates.  These can be interpreted
as a position or a direction, depending on the
circumstance.

There are public routines to add, subtract, scale, 
take the dot and cross product, normalize, and
return the magnitude of vectors.

All the data members (all 3 of them) are public.
This is so routines like Matrix3D can directly
(i.e. efficiently) read and write vectors.
Many OOP-ers disagree with this-- as I would for
large or complex classes-- but for tiny classes
I see no reason not to allow direct acess to 
data members.  In Java, a language without structures,
this is doubly true.
*/
import java.io.*;

public final class Vector3d {

	public double x,y,z;

	public Vector3d() { }
	public Vector3d(double v) {x=y=z=v;}
	public Vector3d(double X,double Y,double Z) {x=X;y=Y;z=Z;}

	final public void copyFrom(Vector3d v)
		{x=v.x;y=v.y;z=v.z;}

	final public double magSqr()
	{
		return x*x+y*y+z*z;
	}
	final public double magnitude()
	{
		return Math.sqrt(magSqr());
	}

	final public Vector3d normalize(){return scaleBy(1.0/magnitude());}

	final public Vector3d plus(Vector3d a)
		{return new Vector3d(x+a.x,y+a.y,z+a.z);}

	final public Vector3d minus(Vector3d a)
		{return new Vector3d(x-a.x,y-a.y,z-a.z);}
	final public Vector3d scaleBy(double scale)
		{return new Vector3d(scale*x,scale*y,scale*z);}
	final public double dot(Vector3d a)
		{return x*a.x+y*a.y+z*a.z;}

	final public Vector3d cross(Vector3d b)
	{/*Return right-handed cross product of this Vector and b*/
		return new Vector3d(y*b.z-z*b.y,
					  z*b.x-x*b.z,
					  x*b.y-y*b.x);
	}

	public String toString() {
		return "("+x+","+y+","+z+")";
	}

	public static Vector3d read(DataInputStream is) throws IOException {
		Vector3d ret=new Vector3d();
		ret.x=is.readDouble();
		ret.y=is.readDouble();
		ret.z=is.readDouble();
		return ret;
	}

	public void write(DataOutputStream os) throws IOException {
		os.writeDouble(x);
		os.writeDouble(y);
		os.writeDouble(z);
	}

};

