package de.varylab.varylab.plugin.grasshopper;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import de.jreality.plugin.basic.View;
import de.jreality.ui.LayoutFactory;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class GrashopperDebug extends ShrinkPanelPlugin implements ActionListener {

	

	private static Logger	
		log = Logger.getLogger(GrashopperDebug.class.getName());	
	
	private JTextField
		hostField = new JTextField("127.0.0.1");
	
	private SpinnerNumberModel
		portModel = new SpinnerNumberModel(GrasshopperPlugin.SERVER_PORT, 0, 65535, 1);
	
	private JSpinner
		portSpinner = new JSpinner(portModel);
	
	private JTextArea
		sendArea = new JTextArea(4,20),
		receiveArea = new JTextArea(4,20);
	
	private JButton
		sendButton = new JButton("Connect to port");
	
	public GrashopperDebug() {
		
		sendArea.setMaximumSize(new Dimension(120, 40));
		
		
		receiveArea.setMaximumSize(new Dimension(120, 40));
		receiveArea.setEditable(false);
		
		sendButton.addActionListener(this);
		
		shrinkPanel.setLayout(new GridBagLayout());
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		
		shrinkPanel.add(new JLabel("Host"),lc);
		shrinkPanel.add(hostField,rc);
		
		
		shrinkPanel.add(new JLabel("Port"),lc);
		shrinkPanel.add(portSpinner,rc);
		
		shrinkPanel.add(new JScrollPane(sendArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS),rc);
		shrinkPanel.add(new JScrollPane(receiveArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS),rc);
		
		shrinkPanel.add(sendButton,rc);
		
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			Socket socket = new Socket(InetAddress.getByName(hostField.getText()), portModel.getNumber().intValue());
			DebugClient client = new DebugClient(socket);
			client.start();
		} catch (UnknownHostException e1) {
			log.warning("error connecting grashopper plugin. Unknown host: " + hostField.getText());
			e1.printStackTrace();
		} catch (IOException e1) {
			log.warning("error connecting grashopper plugin: " + e1);
			e1.printStackTrace();
		}
	}

	public class DebugClient extends Thread {

		private Socket 
			socket = null;
		
		public DebugClient(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			try	{
				OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
				// remove line ends
				String text = sendArea.getText().replaceAll("\r", "").replaceAll("\n", "");
				out.write(text+"\n");
				out.flush();
				
				receiveArea.setText("");

				InputStreamReader in = new InputStreamReader(socket.getInputStream());
				LineNumberReader lineReader = new LineNumberReader(in);
				String line = lineReader.readLine();
				receiveArea.append(prettyFormat(line,2));
			} catch (UnknownHostException e1) {
				log.warning("Unknown Host " + hostField.getText());
			} catch (IOException e1) {
				log.warning("Could not create connection to " + hostField.getText() + ":" + portModel.getNumber().intValue());
			}
		}
	}
	
	private static String prettyFormat(String input, int indent) {
	    try {
	        Source xmlInput = new StreamSource(new StringReader(input));
	        StringWriter stringWriter = new StringWriter();
	        StreamResult xmlOutput = new StreamResult(stringWriter);
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", indent);
	        Transformer transformer = transformerFactory.newTransformer(); 
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.transform(xmlInput, xmlOutput);
	        return xmlOutput.getWriter().toString();
	    } catch (Exception e) {
	        throw new RuntimeException(e); // simple exception handling, please review it
	    }
	}
}
