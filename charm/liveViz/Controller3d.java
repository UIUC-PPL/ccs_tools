/*
A panel that allows you to manipulate a 3d object.

by Orion Lawlor,  6/22/2001
olawlor@acm.org
*/

import java.awt.*;
import java.awt.event.*;
import java.util.*;

class Controller3d implements MouseListener, MouseMotionListener
{
	CcsImagePanel master;
	Toolbar tools;

	int wid,ht;//Window size 
	
	//Z slice plane half-thickness (universe coords)
	double delZ;
	
	//Coordinate and rotation origin (universe)
	Vector3d o;
	//Coordinate axes
	Vector3d x,y,z;//Direction vectors (all unit, perp.)
	//Screen scaling (universe <-> pixels)
	double u2pix;//Pixels per object unit
	double pix2u;//Object units per pixel
	//Rot/trans/scale origin (screen pixels)
	int rtsoX,rtsoY;
	
	public void updateSize(int wid_,int ht_)
	{ 
		//Update origin
		rtsoX+=(wid_-wid)/2;
		rtsoY+=(ht_-ht)/2;
		
		wid=wid_;ht=ht_; 
	}
	
	private void rebuildAxes() {
		x=x.normalize();
		z=x.cross(y).normalize();
		y=z.cross(x).normalize();
	}

	public Controller3d(CcsImagePanel master_,Toolbar tools_,
		Vector3d oMin,Vector3d oMax,
		int wid_,int ht_)
	{
		master=master_;
		master.addMouseListener(this);
		master.addMouseMotionListener(this);
		tools=tools_;
		
		if (wid_<=0) wid_=200;
		if (ht_<=0) ht_=150;
		wid=wid_;ht=ht_;
		//Origin starts off at object center
		o=oMin.plus(oMax).scaleBy(0.5);
		
		//Scale factor starts off showing whole object in window
		int minPix=wid; if (minPix>ht) minPix=ht;
		Vector3d del=oMax.minus(oMin);
		pix2u=del.x/minPix;
		u2pix=1.0/pix2u;

	//	System.out.println("Controller3d: o="+o+"   pix2u="+pix2u+"  del="+del);
		
		//Screen X, Y axes line up with universe
		x=new Vector3d(1,0,0);
		y=new Vector3d(0,1,0);
		rebuildAxes();
		
		//Set rot/trans/scale origin
		rtsoX=wid/2; rtsoY=ht/2;
		
		delZ=del.z;
	}
	
	private void update() {
		master.requestImage();
		master.repaint(200);
	}
	
	public static class ImageRequest3d {
		public int wid,ht;//Size of requested image
		public Vector3d x,y,z;//Coordinate axes (Universe space)
		public Vector3d o;//Topleft corner of screen
		public double minZ,maxZ;
	}
	//Return the universe location of the given screen pixel
	private Vector3d universe(double sx,double sy) {
		return o.plus(x.scaleBy((sx-rtsoX)*pix2u).plus(y.scaleBy(-(sy-rtsoY)*pix2u)));
	}
	
	public ImageRequest3d get3dRequest() {
		ImageRequest3d ret=new ImageRequest3d();
		ret.wid=wid; ret.ht=ht;
		ret.x=x.scaleBy(u2pix); ret.y=y.scaleBy(-u2pix);
		ret.z=z;//Z always measured in object units
		//Shift origin so it points to screen topleft
		ret.o=universe(0,0);
		ret.minZ=-delZ; ret.maxZ=delZ;
		
	//	System.out.println("get3dRequest: "+wid+"x"+ht+"   X="+x+"  Y="+y+"  Z="+z+"  O="+o);
	//	System.out.println("get3dRequest: rX="+ret.x+"  rY="+ret.y+"  rZ="+ret.z+"  rO="+ret.o);
		return ret;
	}

//Axis/scale markers:
	//Convert this universe direction to a screen direction,
	// and draw it as an axis
	private void drawAxis(Graphics g,Dimension dim,Vector3d dir,String name)
	{
		double axisLen=15.0; //Axis lengths, pixels
		double fontShift=7.0;//Further distance for font start
		//Convert the universe axis to a screen direction
		Vector3d sDir=new Vector3d(dir.dot(x),-dir.dot(y),0);
		Vector3d aDir=sDir.scaleBy(axisLen);
		Vector3d nDir=sDir.scaleBy(axisLen+fontShift);
		Font f=g.getFont();
		
		int cx=(int)(2*axisLen);
		int cy=(int)(dim.height-2*axisLen);
		g.drawLine(cx,cy,(int)(cx+aDir.x),(int)(cy+aDir.y));
		g.drawString(name,
			(int)(cx+nDir.x),
			(int)(cy+nDir.y));
	}
	public void paint(Graphics g,Dimension dim)
	{
		g.setColor(Color.red);
		drawAxis(g,dim,new Vector3d(1,0,0),"X");
		g.setColor(Color.green);
		drawAxis(g,dim,new Vector3d(0,1,0),"Y");
		g.setColor(new Color(70,70,255));
		drawAxis(g,dim,new Vector3d(0,0,1),"Z");
	}
	
//Mouse handling:
	private static final int TOOL_SCROLL=0;
	private static final int TOOL_ZOOM=1;
	private static final int TOOL_ROTATE=2;
	private static final int TOOL_SLICE=3;
	private static final int TOOL_INFO=4;
	private int lastX=-1,lastY=-1;
	private double starting_pix2u;
	private Vector3d starting_o;
	private boolean inDrag;
	
	private void pointInfo(int mx,int my)
	{
		Vector3d loc=universe(mx,my);
		//status.setText
		System.out.println("Clicked at "+loc);		
	}
	
	private void updatePoint(MouseEvent evt,boolean firstTime) {
		int mx=evt.getX(),my=evt.getY();
		if (firstTime) 
		{//The start of a mouse move: save various parameters
			//Shift rot/trans/scale origin to mx,my
			o=universe(mx,my);
			rtsoX=mx; rtsoY=my;
			starting_o=o;
			starting_pix2u=pix2u;
			if (tools.getTool()==TOOL_INFO) pointInfo(mx,my);
		}
		else if ((lastX!=mx) || (lastY!=my))
		{ //Mouse moved: update the image
			switch(tools.getTool()) {
			case TOOL_SCROLL: { //Shift origin to mx,my
				rtsoX=mx; rtsoY=my;
				update();
			} break;
			case TOOL_ZOOM: { //Set scale to current shift
				double pix2zoom=1.0/50;//Pixels per factor of 2 zoom
				int d=(mx-rtsoX)+(my-rtsoY);
				pix2u=starting_pix2u/Math.pow(2.0,d*pix2zoom);
				u2pix=1.0/pix2u;
				update();
			} break;
			case TOOL_ROTATE: {
				double pix2rad=1.0/50;//Convert pixel shift to radian rotation
				double dx=mx-lastX, dy=-(my-lastY);
				x=x.plus(z.scaleBy(-dx*pix2rad));
				y=y.plus(z.scaleBy(-dy*pix2rad));
				rebuildAxes();
				update();
			} break;
			case TOOL_SLICE: {
				int d=(mx-rtsoX)+(my-rtsoY);
				o=starting_o.plus(z.scaleBy(d*pix2u));
				update();
			} break;
			case TOOL_INFO: pointInfo(mx,my); break;
			};
		}
		lastX=mx;lastY=my;
	}
	
	public void mousePressed(MouseEvent evt)
	{
		inDrag=true;
		updatePoint(evt,true);
	}
	public void mouseDragged(MouseEvent evt)
	{
		updatePoint(evt,false);
	}
	public void mouseReleased(MouseEvent evt)
	{
		updatePoint(evt,false);
		inDrag=false;
		lastX=-1;lastY=-1;
	}
	public void mouseMoved(MouseEvent evt)
	{}
	public void mouseClicked(MouseEvent evt)
	{}
	public void mouseEntered(MouseEvent evt)
	{}
	public void mouseExited(MouseEvent evt)
	{}
}





