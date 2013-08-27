package de.varylab.varylab.plugin.grashopper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.job.Job;
import de.jreality.plugin.job.JobListener;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.util.NativePathUtility;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.JRHalfedgeViewer;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.halfedge.adapter.VPositionAdapter;
import de.varylab.varylab.plugin.grashopper.data.RVLMeshFactory;
import de.varylab.varylab.plugin.grashopper.data.binding.Face;
import de.varylab.varylab.plugin.grashopper.data.binding.FaceList;
import de.varylab.varylab.plugin.grashopper.data.binding.RVLMesh;
import de.varylab.varylab.plugin.grashopper.data.binding.Vertex;
import de.varylab.varylab.plugin.grashopper.data.binding.VertexList;
import de.varylab.varylab.plugin.meshoptimizer.SpringOptimizer;
import de.varylab.varylab.plugin.optimization.OptimizationPanel;

public class GrashopperPlugin extends Plugin {

	private static Logger	
		log = Logger.getLogger(GrashopperPlugin.class.getName());
	private HalfedgeLayer 
		layer = null;
		
	private class ComponentClient extends Thread {
			
		private Socket
			socket = null;
		private OptimizationPanel
			optimizationPanel = null;
		private HalfedgeInterface
			hif = null;
		
		
		
		public ComponentClient(Socket socket, HalfedgeInterface hif, OptimizationPanel optPanel) {
			super("Grashopper Component@" + socket);
			this.socket = socket;
			this.hif = hif;
			this.optimizationPanel = optPanel;
		}
		
		@Override
		public void run() {
			InputStream in = null;
			try {
				in = socket.getInputStream();
			} catch (IOException e) {
				log.warning("could not receive grashopper data:" + e);
				hif.removeLayer(layer);
				return;
			}
			InputStreamReader inReader = new InputStreamReader(in);
			LineNumberReader lineReader = new LineNumberReader(inReader);
//			StringBuffer buffer = new StringBuffer();
			try {
				String line = lineReader.readLine();
//				while (line != null) {
//					buffer.append(line);
//					buffer.append("\n");
//					line = lineReader.readLine();
//				}
				processData(line, socket.getOutputStream());
				log.info("got data: " + line);
			} catch (IOException e) {
				log.warning("error transferring data: " + e);
			}
		}
		
		
		public IndexedFaceSet toIndexedFaceSet(RVLMesh mesh) {
			int numVerts = mesh.getVertices().getVertex().size();
			double[][] verts = new double[numVerts][];
			for (Vertex v : mesh.getVertices().getVertex()) {
				verts[v.getID()] = new double[] {v.getX(), v.getY(), v.getZ()};
			}
			int numFaces = mesh.getFaces().getFace().size();
			int[][] faces = new int[numFaces][];
			int faceId = 0;
			for (Face f : mesh.getFaces().getFace()) {
				if (f.isIsTriangle()) {
					faces[faceId++] = new int[]{f.getA(), f.getB(), f.getC()}; 
				} else {
					faces[faceId++] = new int[]{f.getA(), f.getB(), f.getC(), f.getD()};
				}
			}
			IndexedFaceSetFactory iff = new IndexedFaceSetFactory();
			iff.setVertexCount(numVerts);
			iff.setFaceCount(numFaces);
			iff.setVertexCoordinates(verts);
			iff.setFaceIndices(faces);
			iff.update();
			return iff.getIndexedFaceSet();
		}
		
		public RVLMesh toRVLMesh(VHDS hds, AdapterSet a) {
			RVLMesh mesh = new RVLMesh();
			VertexList vList = new VertexList();
			FaceList fList = new FaceList();
			for (VVertex v : hds.getVertices()) {
				double[] p = a.getD(Position3d.class, v);
				Vertex rv = new Vertex();
				rv.setID(v.getIndex());
				rv.setX(p[0]);
				rv.setY(p[1]);
				rv.setZ(p[2]);
				vList.getVertex().add(rv);
			}
			mesh.setVertices(vList);
			for(VFace f : hds.getFaces()) {
				Face rf = new Face();
				List<VVertex> bv = HalfEdgeUtils.boundaryVertices(f);
				if (bv.size() == 3) {
					rf.setA(bv.get(0).getIndex());
					rf.setB(bv.get(1).getIndex());
					rf.setC(bv.get(2).getIndex());
					rf.setD(-1);
					rf.setIsTriangle(true);
				} else 
				if (bv.size() == 4) {
					rf.setA(bv.get(0).getIndex());
					rf.setB(bv.get(1).getIndex());
					rf.setC(bv.get(2).getIndex());
					rf.setD(bv.get(3).getIndex());
					rf.setIsTriangle(false);
				}
				fList.getFace().add(rf);
			}
			mesh.setFaces(fList);
			return mesh;
		}
		
		
		public void processData(String xml, final OutputStream out) throws IOException {
			try {
				StringReader xmlReader = new StringReader(xml);
				RVLMesh mesh = RVLMeshFactory.loadRVLMesh(xmlReader);
				layer.set(toIndexedFaceSet(mesh));
				log.info("mesh: " + mesh);
			} catch (Exception e) {
				log.warning("could not parse grashopper mesh: " + e);
			}
			JobListener jobListener = new JobListener() {
				@Override
				public void jobStarted(Job arg0) {
				}
				
				@Override
				public void jobProgress(Job arg0, double arg1) {
				}
				
				@Override
				public void jobFinished(Job arg0) {
					try {
						VHDS hds = new VHDS();
						hds = layer.get(hds);
						AdapterSet aSet = AdapterSet.createGenericAdapters();
						aSet.addAll(layer.getAdapters());
						RVLMesh mesh = toRVLMesh(hds, aSet);
						String xml = RVLMeshFactory.meshToXML(mesh);
						OutputStreamWriter outWriter = new OutputStreamWriter(out);
						outWriter.write(xml);
						outWriter.flush();
						out.close();
					} catch (Exception e) {
						log.warning("error writing response: " + e);
					}
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
			VHDS hds = layer.get(new VHDS());
			optimizationPanel.optimize(hds, jobListener);
		}
		
	}
	
	private class Server extends Thread {
		
		private HalfedgeInterface 
			hif = null;
		private OptimizationPanel
			optPanel = null;
		
		public Server(HalfedgeInterface hif, OptimizationPanel optPanel) {
			super();
			this.hif = hif;
			this.optPanel = optPanel;
		}

		@Override
		public void run() {
			ServerSocket server = null;
			try {
				server = new ServerSocket(8000);
				log.info("started grashopper connection server: " + server);
			} catch (IOException e) {
				log.warning("could not start grashopper server: " + e);
				return;
			}
			while (true) {
				try {
					Socket compSocket = server.accept();
					log.info("grashopper component connected at " + compSocket);
					ComponentClient client = new ComponentClient(compSocket, hif, optPanel);
					client.start();
				} catch (IOException e) {
					log.warning("error connecting grashopper component: " + e);
				}
			}
		}
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		HalfedgeInterface hif = c.getPlugin(HalfedgeInterface.class);
		layer = hif.createLayer("Grashopper Geometry");
		layer.addAdapter(new VPositionAdapter(), true);
		OptimizationPanel optPanel = c.getPlugin(OptimizationPanel.class);
		Server server = new Server(hif, optPanel);
		server.start();
	}
	
	
	public static void main(String[] args) {
		NativePathUtility.set("native");
		JRHalfedgeViewer.initHalfedgeFronted();
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addContentUI();
		v.addContentSupport(ContentType.Raw);
		v.setPropertiesFile("GrashopperTest.xml");
		v.registerPlugin(GrashopperPlugin.class);
		v.registerPlugin(GrashopperDebug.class);
		v.registerPlugin(SpringOptimizer.class);
		v.startup();
	}

}
