package de.varylab.varylab.plugin.remeshing;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.varylab.discreteconformal.heds.CoHDS;
import de.varylab.discreteconformal.heds.CoVertex;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;

public class LatticeRemesher {

	private Lattice lattice = null;
	private VHDS hds = new VHDS();

	private boolean newVertices = true;
	private boolean cornersOnLattice = true;
//	private ConverterHeds2JR conv = new ConverterHeds2JR();
//	private SceneGraphComponent root = new SceneGraphComponent();
	
	public LatticeRemesher(Lattice l, boolean newVertices, boolean cornersOnLattice) {
		lattice = l;
		this.newVertices = newVertices;
		this.cornersOnLattice = cornersOnLattice;
	}
	
	public LatticeRemesher(boolean newVertices, boolean cornersOnLattice) {
		this(null,newVertices,cornersOnLattice);
	}
	
	public LatticeRemesher(Lattice l) {
		this(l,true,true);
	}
	
	public VHDS fitToBoundary(CoHDS chds) throws RemeshingException {
		hds = lattice.getHDS();
//		SceneGraphComponent child1 = new SceneGraphComponent();
//		child1.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child1);
		LinkedList<CoVertex> corners = TextureUtility.findCorners(chds); 
		LinkedList<LatticeLine2D> lines = findLatticeLines(corners);
		LatticePolygon2D polygon = findLatticePolygon(lines);
		
		LinkedList<VEdge> edges = createInducedPolygon(polygon);
//		SceneGraphComponent child2 = new SceneGraphComponent();
//		child2.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child2);
		
		polygon.snapTextureCorners(corners);
//		SceneGraphComponent child3 = new SceneGraphComponent();
//		child3.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child3);
		
		removeOutside(edges);
//		SceneGraphComponent child4 = new SceneGraphComponent();
//		child4.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child4);
		
		
		SpringRemeshingUtility.straightenBoundary(
			lattice,
			edges,
			polygon.getCorners() 
		);
//		SceneGraphComponent child5 = new SceneGraphComponent();
//		child5.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child5);
		
		
		SpringRemeshingUtility.relaxInterior(
			lattice, 
			polygon.getCorners(), 
			true, 
			false
		);
//		SceneGraphComponent child6 = new SceneGraphComponent();
//		child6.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child6);
		
		RemeshingUtility.projectOntoBoundary(hds, chds);
		
//		JRViewer.display(root);
		return hds;
	}

	private LinkedList<LatticeLine2D> findLatticeLines(LinkedList<CoVertex> corners) {
		LinkedList<LatticeLine2D> lines = new LinkedList<LatticeLine2D>();
		Iterator<CoVertex> ci = corners.iterator();
		CoVertex c2 = ci.next();
		CoVertex c1 = c2;
		do {
			c2 = ci.next();
			double[] 
			       v1 = c1.getTextureCoord().get(),
			       v2 = c2.getTextureCoord().get(); 
			LatticeLine2D line = lattice.getClosestLatticeLine(new double[]{v1[0],v1[1]}, new double[]{v2[0]-v1[0],v2[1]-v1[1]});
			if(cornersOnLattice && !lines.isEmpty()) { 
				forceIntersectionOnLattice(lines.getLast(), line);
			}
			lines.add(line);
			c1 = c2;
		} while(ci.hasNext());
		c2 = corners.getFirst();
		double[] 
		       v1 = c1.getTextureCoord().get(),
		       v2 = c2.getTextureCoord().get(); 
		LatticeLine2D line = lattice.getClosestLatticeLine(new double[]{v1[0],v1[1]}, new double[]{v2[0]-v1[0],v2[1]-v1[1]});
		if(cornersOnLattice) {
			forceIntersectionOnLattice(lines.getLast(), line);
		}
		lines.add(line);
		return lines;
	}

	private void forceIntersectionOnLattice(LatticeLine2D line1, LatticeLine2D line2) {
		double[] pt = line1.intersect(line2);
		while(!lattice.isLatticePoint(pt)) {
			line2.c += 1;
			pt = line1.intersect(line2);
		}
	}

	private LatticePolygon2D findLatticePolygon(LinkedList<LatticeLine2D> lines) throws RemeshingException {
		LinkedList<VVertex> verts = new LinkedList<VVertex>();
		LinkedList<VVertex> corners = new LinkedList<VVertex>();
		LatticeLine2D l1 = lines.getLast();
		LatticeLine2D l2 = lines.getLast();
		Iterator<LatticeLine2D> li = lines.iterator();
		do {
			l1 = l2;
			l2 = li.next();
			double[] pt = l1.intersect(l2);
			VVertex v = lattice.getLatticeVertex(pt);
			if(!verts.isEmpty()) {
				List<VVertex> segment = l1.getOpenSegment(verts.getLast(),v,newVertices);
				if(segment.size() == 0) {
					throw new RemeshingException("Two boundary vertices have been identified. Please refine texture.");
				}
				verts.addAll(segment);
			}
			verts.add(v);
			corners.add(v);
		} while(li.hasNext());
		List<VVertex> segment = lines.getLast().getOpenSegment(verts.getLast(),verts.getFirst(),newVertices);
		verts.addAll(segment);
		return new LatticePolygon2D(verts,corners);
	}
	

	private LinkedList<VEdge> createInducedPolygon(LatticePolygon2D polygon) {
		LinkedList<VEdge> bdPolygon = new LinkedList<VEdge>();
		List<VVertex> boundaryVertices = polygon.getVertices();
		int n = boundaryVertices.size();
		for(int i = 0; i < n; ++i) {
			VVertex v1 = boundaryVertices.get(i);
			VVertex v2 = boundaryVertices.get((i + 1) % n);
			VEdge e = HalfEdgeUtils.findEdgeBetweenVertices(v1, v2);
			if(e == null) {
				List<VEdge> edges = lattice.insertEdge(boundaryVertices.get(i),boundaryVertices.get((i+1)%boundaryVertices.size()), newVertices);
				bdPolygon.addAll(edges);
			} else {
				bdPolygon.add(e);
			}
		}
		return bdPolygon;
	}

	private void removeExteriorVertices(List<VEdge> polygon) {
		VFace intFace = polygon.get(0).getRightFace();
		HalfEdgeDataStructure<VVertex, VEdge, VFace> hds = intFace.getHalfEdgeDataStructure();
		LinkedList<VFace> queue = new LinkedList<VFace>();
		HashSet<VFace> visited = new HashSet<VFace>();
		queue.add(intFace);
		while (!queue.isEmpty()){
			VFace actFace = queue.poll();
			List<VFace> star = new LinkedList<VFace>();
			for(VEdge e : HalfEdgeUtils.boundaryEdges(actFace)) {
				if(e.getRightFace() != null) {
					star.add(e.getRightFace());
				}
			}
			for (VFace f : star){
				if (!visited.contains(f)){
					visited.add(f);
					queue.offer(f);
				}
			}
		}
		Set<VVertex> outsideVerts = new HashSet<VVertex>();
		outsideVerts.addAll(hds.getVertices());
		for(VFace f : visited) {
			outsideVerts.removeAll(HalfEdgeUtils.boundaryVertices(f));
		}
		for(VVertex v : outsideVerts) {
			TopologyAlgorithms.removeVertex(v);
		}
		// remove remaining outer edges
		List<VEdge> pEdges = new LinkedList<VEdge>(hds.getEdges());
		for (VEdge e : pEdges) {
			if (e.isPositive()) continue;
			if (e.getLeftFace() == null && e.getRightFace() == null) {
				TopologyAlgorithms.removeEdge(e);
			}
		}
	}

	private void removeOutside(LinkedList<VEdge> polygon) {
		for(VEdge e : polygon) {
			hds.removeFace(e.getLeftFace());
		}
		removeExteriorVertices(polygon);
	}

	public void setLattice(Lattice l) {
		lattice = l;
	}
}
