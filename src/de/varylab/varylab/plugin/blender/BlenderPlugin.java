package de.varylab.varylab.plugin.blender;

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
import de.jtem.halfedgetools.JRHalfedgeViewer;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.adapter.VPositionAdapter;
import de.varylab.varylab.plugin.VarylabMain;
import de.varylab.varylab.plugin.blender.data.BlenderMeshFactory;
import de.varylab.varylab.plugin.blender.data.BlenderMeshUtility;
import de.varylab.varylab.plugin.blender.data.binding.BlenderMesh;
import de.varylab.varylab.plugin.meshoptimizer.PlanarQuadsOptimizer;
import de.varylab.varylab.plugin.meshoptimizer.SpringOptimizer;
import de.varylab.varylab.plugin.optimization.OptimizationPanel;

public class BlenderPlugin extends Plugin {

	private static Logger	
		log = Logger.getLogger(BlenderPlugin.class.getName());
	private HalfedgeInterface
		hif = null;
	private HalfedgeLayer 
		layer = null;
	protected static final int 
		SERVER_PORT = 8790;
	
		
	private class ComponentClient extends Thread {
			
		private Socket
			socket = null;
		private OptimizationPanel
			optimizationPanel = null;
		public static final String 
			UTF8_BOM = "\uFEFF";
		
		public ComponentClient(Socket socket, OptimizationPanel optPanel) {
			super("Blender Component@" + socket);
			this.socket = socket;
			this.optimizationPanel = optPanel;
		}
		
		@Override
		public void run() {
			InputStream in = null;
			try {
				in = socket.getInputStream();
			} catch (IOException e) {
				log.warning("could not receive blender data:" + e);
				return;
			}
			InputStreamReader inReader = new InputStreamReader(in);
			LineNumberReader lineReader = new LineNumberReader(inReader);
			try {
				String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
				String data = lineReader.readLine();
				if (data.startsWith(UTF8_BOM)) {
					data = data.substring(1);
				}
				if (data.startsWith("COMMAND GEOMETRY")) {
					String layerName = data.split(" ")[2];
					VHDS hds = null;
					if (layerName.equals("_selected_")) {
						hds = hif.get(new VHDS());
					} else {
						log.warning("layer selection not yet implemented");
					}
					OutputStream out = socket.getOutputStream();
					writeHDSAsMesh(hds, out);
				} else {
					String xml = xmlHeader + data;
					OutputStream out = socket.getOutputStream();
					doOptimization(xml, out);
				}
			} catch (Exception e) {
				log.warning("error transferring data: " + e);
			}
		}
		
		public void writeHDSAsMesh(VHDS hds, OutputStream out) throws Exception {
			AdapterSet aSet = AdapterSet.createGenericAdapters();
			aSet.addAll(getLayer().getAdapters());
			BlenderMesh mesh = BlenderMeshUtility.toBlenderMesh(hds, aSet);
			String xml = BlenderMeshFactory.meshToXML(mesh);
			OutputStreamWriter outWriter = new OutputStreamWriter(out);
			outWriter.write(xml + "\r\n");
			outWriter.flush();
			outWriter.close();
			out.close();
		}
		
		public void doOptimization(String xml, final OutputStream out) throws IOException {
			StringReader xmlReader = new StringReader(xml);
			String startXML = xml.substring(0, 100);
			final AtomicBoolean isMesh = new AtomicBoolean(true);
			if (startXML.contains("BlenderMesh")) {
				try {
					BlenderMesh mesh = BlenderMeshFactory.loadBlenderMesh(xmlReader);
					getLayer().set(BlenderMeshUtility.toIndexedFaceSet(mesh));
					log.info("mesh: " + mesh);
					isMesh.set(true);
				} catch (Exception e) {
					log.warning("could not parse blender mesh: " + e + "\n" + startXML + "...");
				}
			} 
//			else 
//			if (startXML.contains("RVLLineSet")){
//				try {
//					RVLLineSet lineSet = RVLLineSetFactory.loadRVLLineSet(xmlReader);
//					VHDS hds = RVLUtility.toHDS(lineSet);
//					getLayer().set(hds);
//					log.info("line set: " + lineSet);
//					isMesh.set(false);
//				} catch (Exception e) {
//					log.warning("could not parse grasshopper line set: " + e + "\n" + startXML + "...");
//				}
//			} else {
//				log.warning("data type not recognized: " + startXML +"...");
//				out.close();
//				return;
//			}
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
//								if (isMesh.get()) {
								writeHDSAsMesh(hds, out);
//								} else {
//									writeHDSAsLineSet(hds, out);
//								}
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
				log.info("started blender connection server: " + server);
			} catch (IOException e) {
				log.warning("could not start blender server: " + e);
				return;
			}
			while (keepRunning) {
				try {
					Socket compSocket = server.accept();
					log.info("blender component connected at " + compSocket);
					ComponentClient client = new ComponentClient(compSocket, optPanel);
					client.start();
				} catch (IOException e) {
					log.warning("error connecting blender component: " + e);
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
		layer = hif.createLayer("Blender Geometry");
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
		v.registerPlugin(BlenderPlugin.class);
		v.registerPlugin(SpringOptimizer.class);
		v.registerPlugin(ConsolePlugin.class);
		v.registerPlugin(PlanarQuadsOptimizer.class);
//		v.registerPlugin(GrashopperDebug.class);
		v.startup();
	}

}
