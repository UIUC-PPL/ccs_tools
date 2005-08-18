/*
A panel that displays a memory image.

by Orion Lawlor,  6/14/2001
olawlor@acm.org
*/
package charm.liveViz;

import java.awt.*;
import java.awt.image.*;

class MemImagePanel extends Panel
{
	public MemImagePanel()
	{
		
	}        
	public Dimension getMinimumSize() {return new Dimension(150,100);}
	public Dimension getPreferredSize() {return new Dimension(500,300);}

    /******** Redraw ***********/
	//Fill this pixel buffer
	public void fillBuffer(int pix[],int w,int h)
	{
		int x,y;
		
		for (y=0;y<h;y++)
		{
		    for (x=0;x<w;x++) {
			int red=x&0xff;
			int green=y&0xff;
			int blue=red;
			pix[y*w+x]=(0xff<<24)+(red<<16)+(green<<8)+(blue);
		    }
		}
	}

	public void update(Graphics g) {
		paint(g);
	}
	public void paint(Graphics g) 
	{
		update_anim(g);
	}

	//This small pixel cache improves garbage collection performance
	// by reusing the pixel buffer.
	int pix[]=null;
	int cached_w=-1,cached_h=-1;
	void init_anim(int w,int h)
	{
		cached_w=w;
		cached_h=h;
		pix=new int[w*h];
		//		source.setSize(w,h);
	}
	
	void update_anim(Graphics g)
	{
		int w = getSize().width, h = getSize().height;
		if (cached_w!=w||cached_h!=h)
			init_anim(w,h);
		cached_w=w;
		cached_h=h;
		fillBuffer(pix,w,h);
/*
		//Set the high bytes of the buffer
		for (int i=0;i<w*h;i++)
			pix[i]|=(255 << 24);
*/
		Image img=createImage(new MemoryImageSource(w,h,pix,0,w));
		g.drawImage(img, 0, 0, null);
	}

}


