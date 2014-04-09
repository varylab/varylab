package de.varylab.varylab.plugin.hyperbolicnets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.adapter.type.generic.Position4d;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;

public class HyperbolicNet {

	private HashMap<VFace,Double> 
		weightMap = new HashMap<VFace,Double>();
	
	
//	private StripDecomposition<VEdge, VFace, VHDS>
//		stripDecomposition = null;
	
	private VHDS 
		hds = null;
	
	private double
		initialWeight = 1.0;
	
	private AdapterSet
		as = new AdapterSet();

	private int uLines = 5;

	private int vLines = 5;
	
	
	public HyperbolicNet(VHDS hds, AdapterSet as, double weight) {
		this.hds = hds;
		this.as.addAll(as);
		initialWeight = weight;
	} 
	
	public HyperbolicNet(VHDS hds, AdapterSet as) {
		this.hds = hds;
		this.as.addAll(as);
	}

	private void propagateWeights() {
		HashSet<VFace> facesDone = new HashSet<VFace>();
		HashMap<VFace,VEdge> edgeForPreviousQMap = new HashMap<VFace,VEdge>();
		
		int i = 0;
		VFace f = hds.getFace(i);
		while(HalfEdgeUtils.boundaryEdges(f).size() != 4) {
			f = hds.getFace(++i);
		}
		
		weightMap.put(f, initialWeight);
		
		LinkedList<VFace> queue = new LinkedList<VFace>();
		
		facesDone.add(f);
		addNonVisitedNeighbors(f, facesDone, edgeForPreviousQMap, queue);
		
		while(!queue.isEmpty()) {
			f = queue.pollLast();
			if(facesDone.contains(f)) {
				continue;
			}
			propagateWeight(edgeForPreviousQMap.get(f),f);
			facesDone.add(f);
			addNonVisitedNeighbors(f, facesDone, edgeForPreviousQMap, queue);
		}
	}

	private void propagateWeight(VEdge e, VFace f) {
		double[]
				v0 = as.getD(Position3d.class,e.getTargetVertex()),
				v1 = as.getD(Position3d.class,e.getNextEdge().getTargetVertex()),
				v2 = as.getD(Position3d.class,e.getPreviousEdge().getStartVertex()),
				v3 = as.getD(Position3d.class,e.getStartVertex()),
				v4 = as.getD(Position3d.class,e.getOppositeEdge().getNextEdge().getTargetVertex()),
				v5 = as.getD(Position3d.class,e.getOppositeEdge().getPreviousEdge().getStartVertex());
		
		double[]
				b30 = Rn.subtract(null, v3, v0),
				b50 = Rn.subtract(null, v5, v0),
				b20 = Rn.subtract(null, v2, v0),
				b10 = Rn.subtract(null, v1, v0),
				b40 = Rn.subtract(null, v4, v0);
		
		double newWeight = weightMap.get(e.getLeftFace())*
				Rn.determinant(new double[][]{b30,b50,b20})/
				Rn.determinant(new double[][]{b30,b10,b40});
		
		weightMap.put(f,newWeight);
	}

	private void addNonVisitedNeighbors(
			VFace f,
			HashSet<VFace> facesDone, 
			HashMap<VFace, VEdge> edgeForPreviousQMap, 
			LinkedList<VFace> queue) {
		for(VEdge be: HalfEdgeUtils.boundaryEdges(f)) {
			VFace nf = be.getRightFace();
			if(nf != null && !facesDone.contains(nf)) {
				edgeForPreviousQMap.put(nf,be);
				queue.push(nf);
			}
		}
	}

	public void adjustHomogeneousCoordinates() {
		VFace f = hds.getFaces().get(0);
		LinkedList<VFace> queue = new LinkedList<VFace>();
		HashSet<VFace> visitedFaces = new HashSet<VFace>(hds.numFaces());
		HashSet<VVertex> availableVertices = new HashSet<VVertex>(hds.getVertices());
		queue.add(f);
		while(!queue.isEmpty()) {
			f = queue.pollLast();
			if(visitedFaces.contains(f)) {
				continue;
			}
			if(!adjustHomogeneousCoordinates(f,availableVertices)) {
				System.out.println("Could not adjust homogeneous coordinates, implement backtracking !");
			} else {
				availableVertices.removeAll(HalfEdgeUtils.boundaryVertices(f));
			}
			visitedFaces.add(f);
			addNeighboringFaces(f,visitedFaces, queue);
		}
	}
	
	private void addNeighboringFaces(VFace f, HashSet<VFace> visited, LinkedList<VFace> queue) {
		for(VEdge be: HalfEdgeUtils.boundaryEdges(f)) {
			VFace nf = be.getRightFace();
			if(nf != null && !visited.contains(nf)) {
				queue.push(nf);
			}
		}
	}

	private boolean adjustHomogeneousCoordinates(VFace f, HashSet<VVertex> availableVertices) {
		List<VVertex> verts = HalfEdgeUtils.boundaryVertices(f);
		int i = 0;
		
		while(i < verts.size() && !availableVertices.contains(verts.get(i))) {
			++i;
		}
		if(i == verts.size()) {
			return false;
		} else {
			double[] v = as.getD(Position4d.class, verts.get(i));
			double wOld = getWeight(verts);
			double wNew = weightMap.get(f);
			double factor = ((i%2)==1)?wOld/wNew:wNew/wOld;
			as.set(Position.class, verts.get(i), Rn.times(null, factor, v));
			return true;
		}
	
	}

	private double getWeight(List<VVertex> verts) {
		double weight = 1.0;
		for(VVertex v : verts) {
			double[] coords = as.getD(Position4d.class, v);
			weight = coords[3]/weight;
		}
		return 1/weight;
	}

	public SceneGraphComponent getComponent() {
		propagateWeights();
		adjustHomogeneousCoordinates();
		SceneGraphComponent patchRoot = new SceneGraphComponent("Patches root");
		for(VFace f : hds.getFaces()) {
			List<VVertex> boundaryVertices = HalfEdgeUtils.boundaryVertices(f);
			int nVerts = boundaryVertices.size();
			if(nVerts == 4) {
				double[][][] quadVerts = new double[2][2][4];
				quadVerts[0][0] = as.getD(Position4d.class,boundaryVertices.get(0));
				quadVerts[1][0] = as.getD(Position4d.class,boundaryVertices.get(1));
				quadVerts[1][1] = as.getD(Position4d.class,boundaryVertices.get(2));
				quadVerts[0][1] = as.getD(Position4d.class,boundaryVertices.get(3));
				HyperboloidPatchFactory hpf = new HyperboloidPatchFactory();
				hpf.setGenerateVertexNormals(true);
				hpf.setGenerateFaceNormals(true);
				hpf.setGenerateEdgesFromFaces(true);
				hpf.setULineCount(uLines);
				hpf.setVLineCount(vLines);
				hpf.setQuad(quadVerts);
				hpf.setWeight(weightMap.get(f));
				hpf.setClosedInUDirection(false);
				hpf.setClosedInVDirection(false);
				hpf.update();
				SceneGraphComponent sgc = new SceneGraphComponent("Patch: " + f.getIndex());
				sgc.setGeometry(hpf.getGeometry());
				patchRoot.addChild(sgc);
			}
		}
		return patchRoot;
	}

	public void setULines(int intValue) {
		uLines = intValue;
	}
	
	public void setVLines(int intValue) {
		vLines = intValue;
	}
}
