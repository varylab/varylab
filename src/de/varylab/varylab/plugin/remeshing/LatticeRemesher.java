package de.varylab.varylab.plugin.remeshing;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;

public class LatticeRemesher <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	HDS extends HalfEdgeDataStructure<V, E, F>
> {

	private Logger 
		log = Logger.getLogger(LatticeRemesher.class.getName());
	
	private Lattice<V, E, F, HDS> lattice = null;

	private boolean addNewVertices = true;
	private boolean cornersOnLattice = true;
	private boolean relaxInterior = true;
//	private ConverterHeds2JR conv = new ConverterHeds2JR();
//	private SceneGraphComponent root = new SceneGraphComponent();
	
	public LatticeRemesher(Lattice<V, E, F, HDS> l, boolean newVertices, boolean cornersOnLattice, boolean relaxInterior) {
		lattice = l;
		this.addNewVertices = newVertices;
		this.cornersOnLattice = cornersOnLattice;
		this.relaxInterior = relaxInterior;
	}
	
	public LatticeRemesher(boolean newVertices, boolean cornersOnLattice) {
		this(null, newVertices, cornersOnLattice, true);
	}
	
	public LatticeRemesher(boolean newVertices, boolean cornersOnLattice, boolean relaxInterior) {
		this(null, newVertices, cornersOnLattice, relaxInterior);
	}
	
	public LatticeRemesher(Lattice<V, E, F, HDS> l) {
		this(l, true, true, true);
	}
	
	public HDS remesh(HDS surf, AdapterSet a) throws RemeshingException {
		log.fine("start lattice remeshing");
		
//		SceneGraphComponent child0 = new SceneGraphComponent("Texture Coordinate Mesh");
//		child0.setGeometry(conv.heds2ifs(surf, new AdapterSet(new VTexturePositionAdapter(), new VTexturePositionPositionAdapter())));
//		root.addChild(child0);
		
		HDS hds = lattice.getHDS();
//		SceneGraphComponent child1 = new SceneGraphComponent("Initial Lattice");
//		child1.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child1);
		
		// find all boundary vertices such that the angle is not pi
		log.fine("finding corners");
		LinkedList<V> corners = TextureUtility.findCorners(surf, a);
		
		log.fine("checking quantization");
		if(!lattice.checkQuantization(corners,a)) {
			throw new RemeshingException("\nPolygon in texture coordinates does not\nhave quantized edge directions.\nConsider generating quantized coordinates using\nthe Discrete Conformal Parametrization plugin.");
		}
		// find lines with slopes close to the slopes of the edges
		log.fine("finding lattice lines");
		LinkedList<LatticeLine2D<V, E, F, HDS>> lines = lattice.findLatticeLines(corners, a, cornersOnLattice);
		
		log.fine("create lattice polygon");
		LatticePolygon2D<V, E, F, HDS> polygon = lattice.findLatticePolygon(lines, a, addNewVertices);
		
		log.fine("create induced polygon");
		LinkedList<E> edges = lattice.createInducedPolygon(polygon, addNewVertices);
//		SceneGraphComponent child2 = new SceneGraphComponent("Induced Polygon");
//		child2.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child2);
		
		log.fine("snapping texture corners");
		polygon.snapTextureCorners(corners, a);
//		SceneGraphComponent child3 = new SceneGraphComponent("Snap Texture Coordinates");
//		child3.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child3);
		
		log.fine("removing edges outside the polygon");
		removeOutside(edges, hds);
//		SceneGraphComponent child4 = new SceneGraphComponent("Outside removed");
//		child4.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child4);
		
		log.fine("straighten boundary");
		SpringRemeshingUtility.straightenBoundary(lattice, edges, polygon.getCorners(), a);
//		SceneGraphComponent child5 = new SceneGraphComponent("Straighten Boundary");
//		child5.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child5);
		
		if(relaxInterior) {
			log.fine("relaxing interior mesh");
			SpringRemeshingUtility.relaxInterior(lattice, polygon.getCorners(), true, false, a);
		}
//		SceneGraphComponent child6 = new SceneGraphComponent("Relax Interior");
//		child6.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child6);
		
		log.fine("aligning remesh boundary");
		RemeshingUtility.alignRemeshBoundary(hds, surf, a);
		
//		EventQueue.invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				JRViewer.display(root);				
//			}
//		});
		
		log.fine("remeshing done");
		return hds;
	}	

	private void removeOutside(LinkedList<E> polygon, HDS hds) {
		F intFace = null;
		for(E be : HalfEdgeUtils.boundaryEdges(hds)) {
			if(!(polygon.contains(be) || polygon.contains(be.getOppositeEdge()))) {
				intFace = be.getRightFace();
				break;
			}
		}
		LinkedList<F> queue = new LinkedList<F>();
		HashSet<F> visited = new HashSet<F>();
		queue.add(intFace);
		visited.add(intFace);
		while (!queue.isEmpty()){
			F actFace = queue.poll();
			List<F> star = new LinkedList<F>();
			for(E e : HalfEdgeUtils.boundaryEdges(actFace)) {
				if(polygon.contains(e) || polygon.contains(e.getOppositeEdge())){
					continue;
				}
				if(e.getRightFace() != null) {
					star.add(e.getRightFace());
				}
			}
			for (F f : star){
				if (!visited.contains(f)){	
					visited.add(f);
					queue.offer(f);
				}
			}
		}
		Set<V> outsideVerts = new HashSet<V>();
		for(F f : visited) {
			outsideVerts.addAll(HalfEdgeUtils.boundaryVertices(f));
		}
		for(E pe : polygon) {
			outsideVerts.remove(pe.getStartVertex());
		}
		for(V v : outsideVerts) {
			TopologyAlgorithms.removeVertex(v);
		}
		// remove remaining outer edges
		List<E> pEdges = new LinkedList<E>(hds.getEdges());
		for (E e : pEdges) {
			if (e.isPositive()) continue;
			if (e.getLeftFace() == null && e.getRightFace() == null) {
				TopologyAlgorithms.removeEdge(e);
			}
		}
	}
	

	public void setLattice(Lattice<V, E, F, HDS> l) {
		lattice = l;
	}
}
