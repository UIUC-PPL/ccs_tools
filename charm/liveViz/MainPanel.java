/*
  Top-level controller-- creates toolbar,
  status bar, and main panel.
*/
package charm.liveViz;

import charm.util.Toolbar;
import java.applet.Applet;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;

public class MainPanel extends Panel
{
    CcsImagePanel source;
    Label status;
    Toolbar tools;

    public void stop() {
	source.stop();
    }
	
    public MainPanel(String server,String port,String isTimeoutSet_,String timeoutPeriod_)
    {
	tools = null;
	status=new Label();
	String connMachine=server;
	int connPort=Integer.parseInt(port);
	boolean isTimeoutSet = Boolean.parseBoolean(isTimeoutSet_);
	int timeoutPeriod = Integer.parseInt(timeoutPeriod_);
		
	//Build the toolbar descriptions
	String[] toolDesc={
	    "Pan the image (drag around)",
	    "Zoom the image (drag in or out)",
	    "Rotate the image (drag around in 3D)",
	    "Move the slicing plane (drag forward or back)",
	    "Retrieve detailed information"};
		
	//Find the toolbar image (from .jar file)
	URL toolsImg=ClassLoader.getSystemResource("charm/liveViz/toolbar.jpg");
	if (toolsImg==null) { // can't find image-- try from web...
	    try { toolsImg=new URL("http://charm.cs.uiuc.edu/2001/demo/astro1/toolbar.jpg"); }
	    catch (Exception E) { }
	}
	
	//Diplay the toolbar only if it is a 3D image
	//if(source.config.is3d)
	System.out.println("The toolbar created\n");  
	tools=new Toolbar(toolsImg,32,32,5,status,toolDesc);

	source=new CcsImagePanel(this,status,tools,connMachine,connPort,isTimeoutSet,timeoutPeriod);
		
    }
    
    public void createGrid() {
	
	GridBagLayout gbl = new GridBagLayout();
	setLayout(gbl);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.anchor=GridBagConstraints.NORTHEAST;
		
	//display toolbar only if it is 3d
	if(source.is3d())
	    {
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.gridx=gbc.gridy=0;
		gbc.weightx=0.01; gbc.weighty=1.0;
		gbc.gridwidth=1;
		gbc.gridheight=2;
		add(tools,gbc);
	    }

	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridwidth=1; gbc.gridheight=1;
	gbc.gridx=1; 
	gbc.gridy=0;
	gbc.weightx=1.0; gbc.weighty=1.0;
	add(source,gbc);
		
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.gridwidth=1; gbc.gridheight=1;
	gbc.gridx=1; 
	gbc.gridy=1;
	gbc.weightx=1.0; gbc.weighty=0.01;
	add(status,gbc);
	validate();
    }
}
