/*
Configuration of the CCS server.  This class should match the 
"liveVizConfig" class in liveViz0.h.
*/
import java.io.*;

public class Config {
    public int version; //Version of CCS server's getImageConfig command
    public boolean isColor;
    public boolean isPush;
    public boolean is3d;
    public Vector3d min,max;
   
    private Vector3d readVector3d(DataInputStream is) throws IOException {
      double x=is.readDouble(), y=is.readDouble(), z=is.readDouble();
      return new Vector3d(x,y,z);
    }
    public Config(DataInputStream is) throws IOException {
        version=is.readInt();
	isColor=is.readInt()!=0;
	isPush=is.readInt()!=0;
	is3d=is.readInt()!=0;
	if (is3d) {
		min=readVector3d(is);
		max=readVector3d(is);
	} else
		min=max=new Vector3d(0,0,0);
    }
}
