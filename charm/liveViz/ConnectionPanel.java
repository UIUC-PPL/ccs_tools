package charm.liveViz;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.UnknownHostException;
import charm.ccs.*;
import charm.util.*;

class ConnectionPanel extends Panel {
  private TextField serverField;
  private TextField portField;
  private Button connectButton;
  private MainPanel owner;

  private CcsServer server;

  private void connect() {
    String hostName = serverField.getText();
    int port = Integer.parseInt(portField.getText());
    Thread serverThread = new Thread(new Runnable() {
      public void run() {
        CcsServer tmp;
        System.out.println("Connecting to " + hostName + ":" + port + "...\n");
        try {
          tmp = new CcsServer(hostName, port);
        } catch (UnknownHostException e) {
          System.out.println("ERROR> Bad host name");
          return;
        } catch (IOException e) {
          System.out.println("ERROR> Could not connect");
          return;
        }
        System.out.println("Connected!\n");
        server = tmp;
        serverField.disable();
        portField.disable();
        connectButton.disable();
        owner.setCcsServer(server);
      }
    });
    serverThread.start();
  }

  private void init(MainPanel p) {
    setLayout(new GridLayout(0,5));

    serverField = new TextField(20);
    portField = new TextField(4);
    connectButton = new Button("Connect");
    owner = p;

    server = null;

    connectButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        connect();
      }
    });

    add(new Label("Server:"));
    add(serverField);
    add(new Label("Port:"));
    add(portField);
    add(connectButton);
  }

  public ConnectionPanel(MainPanel p, String server, String port) {
    init(p);
    serverField.setText(server);
    portField.setText(port);
    connect();
  }

  public ConnectionPanel(MainPanel p) {
    init(p);
  }
}
