/*
A panel that displays an image aquired via CCS.

by Orion Lawlor,  6/14/2001
olawlor@acm.org
*/

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;
import java.net.UnknownHostException;

class CcsImagePanel extends MemImagePanel
{
    int redrawCount=0;
    Label status;
    CcsThread ccs;
    Controller3d cntl;
    Toolbar tools;
    Config config;

    public CcsImagePanel( MainPanel caller_,Label status_,Toolbar tools_,String server,int port)
    {
	cntl=null;
	config = null;
	tools=tools_;
	ccs=new CcsThread(status_,server,port);
	ccs.addRequest(new CcsConfigRequest(this,caller_));
    }
    public void stop() {
	ccs.finish();
    }

    private static class ByteImage {
	public int wid,ht;
	public byte[] data;
	public boolean isColor;
	public ByteImage(byte[] d,int w,int h,boolean c) {
	    data=d;
	    wid=w;ht=h;
	    isColor=c;
	}
    }

    private class CcsConfigRequest extends CcsThread.request{
	CcsImagePanel dest;
	MainPanel caller;
	public CcsConfigRequest(CcsImagePanel dest_,MainPanel caller_) {
	    super("lvConfig",0);
	    dest=dest_;
	    caller = caller_;
	}
	public void handleReply(byte[] configData){
	    try {
		DataInputStream is=
		    new DataInputStream(new ByteArrayInputStream(configData));
	        config = new Config(is);
	    }catch(IOException e) {e.printStackTrace();}
	    System.out.println("Config values: color="+config.isColor+
	    	" push="+config.isPush+
		" 3d="+config.is3d+ "\n");
	    
	    gotConfig(config.min,config.max);
	    caller.createGrid(); /*Actually puts us into the layout (otherwise get a gray box!)*/
	}
    }
    private void debug(String state) {
    	//System.out.println("liveViz client> "+state);
    }
    private boolean imageRequested=false;
    public void gotConfig(Vector3d min,Vector3d max) {
    	if (config.is3d) {
	  System.out.println("Box size: "+min+" to "+max);
	  cntl=new Controller3d(this,tools,min,max,lastWid,lastHt);
	}
	if (!imageRequested){
	    debug("Sending request from gotConfig");
	    requestImage();
	}
    }

    private class CcsImageRequest extends CcsThread.request {
	int wid,ht;
	CcsImagePanel dest;
	public CcsImageRequest(CcsImagePanel dest_,
			       Controller3d.ImageRequest3d r) {
	    super("lvImage",null);
	    dest=dest_;
	    wid=lastWid; ht=lastHt;
	    ByteArrayOutputStream bs=new ByteArrayOutputStream();
	    DataOutputStream os=new DataOutputStream(bs);
	    //	System.out.println("Sending request for ("+wid+"x"+ht+")");
	    try {
		os.writeInt(1); /*Client version*/
		os.writeInt(1); /*Request type*/
		os.writeInt(wid); os.writeInt(ht);
		if (r!=null) {
		  r.x.write(os);r.y.write(os);r.z.write(os);
		  r.o.write(os);
		  os.writeDouble(r.minZ);os.writeDouble(r.maxZ);
		}
	    } catch(IOException e) {e.printStackTrace();}
	    setData(bs.toByteArray());
	}
	public void handleReply(byte[] data) {
	    dest.gotImage(new ByteImage(data,wid,ht,config.isColor));
	}
    }
    
    public void requestImage() {
	imageRequested=true;
	Controller3d.ImageRequest3d r=null;
	if (cntl!=null) r=cntl.get3dRequest();
	ccs.addRequest(new CcsImageRequest(this,r),true);
    }
    private volatile ByteImage curImg;
    public void gotImage(ByteImage newImg) {
	curImg=newImg;
	debug("gotImage");
	imageRequested=false;
	repaint(1); //Redraw the onscreen image
    }
	
    /******** Redraw ***********/
    public void paint(Graphics g)
    {
	super.paint(g);
	if (cntl!=null) cntl.paint(g,getSize());
    }
    
    int lastWid=200,lastHt=150;
    //Fill this pixel buffer with the last stored image
    public void fillBuffer(int dest[],int dWid,int dHt)
    {
	lastWid=dWid;lastHt=dHt;
	if (cntl!=null) cntl.updateSize(dWid,dHt);
	ByteImage s=curImg;
	if (s==null) //No image delivered yet
	    return; 
	byte[] src=s.data;
	int w=dWid,h=dHt;//Copy region is intersection
	if (w>s.wid) w=s.wid;
	if (h>s.ht) h=s.ht;
	int x,y;
	debug("redraw");
	for (y=0;y<h;y++)
	  if (s.isColor)
	    { //We have a color image
		int srcIdx=y*s.wid*3; //Byte index multiplied by 3
		int destIdx=y*dWid;
		for (x=0;x<w;x++) {
		    int alpha= 0xff;
		    int red=   ((int)src[srcIdx+x*3+0])&0xff;
		    int green= ((int)src[srcIdx+x*3+1])&0xff;
		    int blue=  ((int)src[srcIdx+x*3+2])&0xff;
		    dest[destIdx+x]=(alpha<<24)+(red<<16)+(green<<8)+(blue);
		}
	    }
	  else
	    { //We have a black-and-white image
		int srcIdx=y*s.wid;
		int destIdx=y*dWid;
		for (x=0;x<w;x++) {
		    int val=   ((int)src[srcIdx+x+0])&0xff;
		    int alpha= 0xff;
		    int red=   val;
		    int green= val;
		    int blue=  val;
		    dest[destIdx+x]=(alpha<<24)+(red<<16)+(green<<8)+(blue);
		}
	    }
		
	if (!imageRequested)
	    if ((dWid!=s.wid)||(dHt!=s.ht)||config.isPush){
		debug("Sending request from fillBuffer");
		requestImage();
	    }
	    
    }
}





