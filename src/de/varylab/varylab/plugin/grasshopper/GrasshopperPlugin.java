package de.varylab.varylab.plugin.grasshopper;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.ConsolePlugin;
import de.jreality.plugin.job.Job;
import de.jreality.plugin.job.JobListener;
import de.jreality.util.NativePathUtility;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.JRHalfedgeViewer;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.adapter.VPositionAdapter;
import de.varylab.varylab.plugin.VarylabMain;
import de.varylab.varylab.plugin.grasshopper.data.RVLLineSetFactory;
import de.varylab.varylab.plugin.grasshopper.data.RVLMeshFactory;
import de.varylab.varylab.plugin.grasshopper.data.RVLUtility;
import de.varylab.varylab.plugin.grasshopper.data.binding.RVLLineSet;
import de.varylab.varylab.plugin.grasshopper.data.binding.RVLMesh;
import de.varylab.varylab.plugin.meshoptimizer.PlanarQuadsOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.SpringOptimizer;
import de.varylab.varylab.plugin.optimization.OptimizationPanel;

public class GrasshopperPlugin extends Plugin {

	private static Logger	
		log = Logger.getLogger(GrasshopperPlugin.class.getName());
	private HalfedgeInterface
		hif = null;
	private HalfedgeLayer 
		layer = null;
	protected static final int 
		SERVER_PORT = 6789;
	private Selection
		selection = new Selection();
		
	private class ComponentClient extends Thread {
			
		private Socket
			socket = null;
		private OptimizationPanel
			optimizationPanel = null;
		public static final String 
			UTF8_BOM = "\uFEFF";
		
		public ComponentClient(Socket socket, OptimizationPanel optPanel) {
			super("Grashopper Component@" + socket);
			this.socket = socket;
			this.optimizationPanel = optPanel;
		}
		
		@Override
		public void run() {
			InputStream in = null;
			OutputStream out = null;
			try {
				in = socket.getInputStream();
				out = socket.getOutputStream();
			} catch (IOException e) {
				log.warning("could not receive grashopper data:" + e);
				return;
			}
			InputStreamReader inReader = new InputStreamReader(in);
			LineNumberReader lineReader = new LineNumberReader(inReader);
			try {
				String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
				String cmd = lineReader.readLine();
				if (cmd.startsWith(UTF8_BOM)) {
					cmd = cmd.substring(1);
				}
				switch (cmd) {
					case "COMMAND SEND MESH":
					case "COMMAND SEND LINESET":{
						String data = "";
						String line = "";
						while ((line = lineReader.readLine()) != null) {
							data += line;
						}
						String xml = xmlHeader + data;
						receiveGeometry(xml, false);								
						socket.close();
					}
					break;
					case "COMMAND RECEIVE MESH": {
						VHDS hds = hif.get(new VHDS());
						writeHDSAsMesh(hds, out);
						socket.close();
					}
					break;
					case "COMMAND RECEIVE LINESET": {
						VHDS hds = hif.get(new VHDS());
						writeHDSAsLineSet(hds, out);
						socket.close();
					}
					break;
					case "COMMAND OPTIMIZE MESH":
					case "COMMAND OPTIMIZE LINESET": {
						String data = "";
						String line = "";
						while ((line = lineReader.readLine()) != null) {
							data += line;
						}
						String xml = xmlHeader + data;
						doOptimization(xml, out);
					}
					break;
				}
			} catch (Exception e) {
				log.warning("error transferring data: " + e);
			}
		}
		
		public void writeHDSAsLineSet(VHDS hds, OutputStream out) throws Exception {
			AdapterSet aSet = AdapterSet.createGenericAdapters();
			aSet.addAll(getLayer().getAdapters());
			RVLLineSet lineSet = RVLUtility.toRVLLineSet(hds, aSet);
			String xml = RVLLineSetFactory.lineSetToXML(lineSet);
			OutputStreamWriter outWriter = new OutputStreamWriter(out);
			outWriter.write(xml + "\r\n");
			outWriter.flush();
		}
		
		public void writeHDSAsMesh(VHDS hds, OutputStream out) throws Exception {
			AdapterSet aSet = AdapterSet.createGenericAdapters();
			aSet.addAll(getLayer().getAdapters());
			RVLMesh mesh = RVLUtility.toRVLMesh(hds, aSet);
			String xml = RVLMeshFactory.meshToXML(mesh);
			OutputStreamWriter outWriter = new OutputStreamWriter(out);
			outWriter.write(xml + "\r\n");
			outWriter.flush();
		}
		
		private void storeSelection() {
			selection = new Selection(getLayer().getSelection());
		}
		
		private void restoreSelection() {
			HalfEdgeDataStructure<?, ?, ?> hds = getLayer().get();
			Selection result = new Selection();
			for (Face<?,?,?> f : selection.getFaces()) {
				if (f.getIndex() < hds.numFaces()) {
					result.add(hds.getFace(f.getIndex()));
				}
			}
			for (Edge<?,?,?> e : selection.getEdges()) {
				if (e.getIndex() < hds.numEdges()) {
					result.add(hds.getEdge(e.getIndex()));
				}
			}
			for (Vertex<?,?,?> v : selection.getVertices()) {
				if (v.getIndex() < hds.numVertices()) {
					result.add(hds.getVertex(v.getIndex()));
				}
			}
			getLayer().setSelection(result);
		}
		
		public void receiveGeometry(String xml, boolean joinLineSet) {
			storeSelection();
			StringReader xmlReader = new StringReader(xml);
			String startXML = xml.substring(0, 100);
			if (startXML.contains("RVLMesh")) {
				try {
					RVLMesh mesh = RVLMeshFactory.loadRVLMesh(xmlReader);
					getLayer().set(RVLUtility.toIndexedFaceSet(mesh));
					log.info("mesh: " + mesh);
				} catch (Exception e) {
					log.warning("could not parse grasshopper mesh: " + e + "\n" + startXML + "...");
				}
			} else 
			if (startXML.contains("RVLLineSet")){
				try {
					RVLLineSet lineSet = RVLLineSetFactory.loadRVLLineSet(xmlReader);
					VHDS hds = RVLUtility.toHDS(lineSet, joinLineSet);
					getLayer().set(hds);
					log.info("line set: " + lineSet);
				} catch (Exception e) {
					log.warning("could not parse grasshopper line set: " + e + "\n" + startXML + "...");
				}
			} else {
				log.warning("data type not recognized: " + startXML +"...");
				return;
			}
			restoreSelection();
		}
		
		
		public void doOptimization(String xml, final OutputStream out) throws IOException {
			storeSelection();
			StringReader xmlReader = new StringReader(xml);
			String startXML = xml.substring(0, 100);
			final AtomicBoolean isMesh = new AtomicBoolean(true);
			if (startXML.contains("RVLMesh")) {
				try {
					RVLMesh mesh = RVLMeshFactory.loadRVLMesh(xmlReader);
					getLayer().set(RVLUtility.toIndexedFaceSet(mesh));
					log.info("mesh: " + mesh);
					isMesh.set(true);
				} catch (Exception e) {
					log.warning("could not parse grasshopper mesh: " + e + "\n" + startXML + "...");
				}
			} else 
			if (startXML.contains("RVLLineSet")){
				try {
					RVLLineSet lineSet = RVLLineSetFactory.loadRVLLineSet(xmlReader);
					VHDS hds = RVLUtility.toHDS(lineSet, true);
					getLayer().set(hds);
					log.info("line set: " + lineSet);
					isMesh.set(false);
				} catch (Exception e) {
					log.warning("could not parse grasshopper line set: " + e + "\n" + startXML + "...");
				}
			} else {
				log.warning("data type not recognized: " + startXML +"...");
				return;
			}
			restoreSelection();
			JobListener jobListener = new JobListener() {
				@Override
				public void jobStarted(Job arg0) {
				}
				
				@Override
				public void jobProgress(Job arg0, double arg1) {
				}
				
				@Override
				public void jobFinished(Job arg0) {
					Runnable queuedWoker = new Runnable() {
						@Override
						public void run() {
							try {
								VHDS hds = getLayer().get(new VHDS());
								if (isMesh.get()) {
									writeHDSAsMesh(hds, out);
								} else {
									writeHDSAsLineSet(hds, out);
								}
								out.flush();
								out.close();
							} catch (Exception e) {
								log.warning("error writing response: " + e);
							}
						}
					};
					EventQueue.invokeLater(queuedWoker);
				}
				
				@Override
				public void jobFailed(Job arg0, Exception e) {
					try {
						OutputStreamWriter outWriter = new OutputStreamWriter(out);
						outWriter.write("Optimization failed: " + e);
						out.flush();
						out.close();
					} catch (IOException e2) {
						log.warning("error writing response: " + e2);
					}
				}
				
				@Override
				public void jobCancelled(Job arg0) {
					try {
						OutputStreamWriter outWriter = new OutputStreamWriter(out);
						outWriter.write("Optimization cancelled by user.");
						out.flush();
						out.close();
					} catch (IOException e2) {
						log.warning("error writing response: " + e2);
					}
				}
			};
			optimizationPanel.optimize(getLayer(), jobListener);
		}
		
	}
	
	private class Server extends Thread {
		
		private OptimizationPanel
			optPanel = null;
		private boolean 
			keepRunning = true;
		
		public Server(OptimizationPanel optPanel) {
			super();
			this.optPanel = optPanel;
		}

		@Override
		public void run() {
			ServerSocket server = null;
			try {
				server = new ServerSocket(SERVER_PORT);
				log.info("started grashopper connection server: " + server);
			} catch (IOException e) {
				log.warning("could not start grashopper server: " + e);
				return;
			}
			while (keepRunning) {
				try {
					Socket compSocket = server.accept();
					log.info("grashopper component connected at " + compSocket);
					ComponentClient client = new ComponentClient(compSocket, optPanel);
					client.start();
				} catch (IOException e) {
					log.warning("error connecting grashopper component: " + e);
				}
			}
			try {
				server.close();
			} catch (Exception e) {
				log.warning(e.toString());
			}
		}
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		OptimizationPanel optPanel = c.getPlugin(OptimizationPanel.class);
		Server server = new Server(optPanel);
		server.start();
	}
	
	
	private HalfedgeLayer getLayer() {
		if (layer != null) {
			return layer;
		}
		layer = hif.createLayer("Grashopper Geometry");
		layer.addAdapter(new VPositionAdapter(), true);
		hif.activateLayer(layer);
		return layer;
	}
	
	public static void main(String[] args) {
		NativePathUtility.set("native");
		JRHalfedgeViewer.initHalfedgeFronted();
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addContentUI();
		v.addContentSupport(ContentType.Raw);
		v.setPropertiesFile("GrashopperTest.xml");
		v.registerPlugin(VarylabMain.class);
		v.registerPlugin(GrasshopperPlugin.class);
		v.registerPlugin(SpringOptimizer.class);
		v.registerPlugin(ConsolePlugin.class);
		v.registerPlugin(PlanarQuadsOptimizer.class);
		v.registerPlugin(GrashopperDebug.class);
		v.startup();
	}

}
