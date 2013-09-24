package de.varylab.varylab.plugin.remeshing;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;

public class LatticeRemesher <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	HDS extends HalfEdgeDataStructure<V, E, F>
> {

	private Lattice<V, E, F, HDS> lattice = null;

	private boolean newVertices = true;
	private boolean cornersOnLattice = true;
//	private ConverterHeds2JR conv = new ConverterHeds2JR();
//	private SceneGraphComponent root = new SceneGraphComponent();
	
	public LatticeRemesher(Lattice<V, E, F, HDS> l, boolean newVertices, boolean cornersOnLattice) {
		lattice = l;
		this.newVertices = newVertices;
		this.cornersOnLattice = cornersOnLattice;
	}
	
	public LatticeRemesher(boolean newVertices, boolean cornersOnLattice) {
		this(null, newVertices, cornersOnLattice);
	}
	
	public LatticeRemesher(Lattice<V, E, F, HDS> l) {
		this(l, true, true);
	}
	
	public HDS remesh(HDS chds, AdapterSet a) throws RemeshingException {
		HDS hds = lattice.getHDS();
//		SceneGraphComponent child1 = new SceneGraphComponent("Initial Lattice");
//		child1.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child1);
		LinkedList<V> corners = TextureUtility.findCorners(chds, a); 
		LinkedList<LatticeLine2D<V, E, F, HDS>> lines = findLatticeLines(corners, a);
		LatticePolygon2D<V, E, F, HDS> polygon = findLatticePolygon(lines, a);
		
		LinkedList<E> edges = createInducedPolygon(polygon);
//		SceneGraphComponent child2 = new SceneGraphComponent();
//		child2.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child2);
		
		polygon.snapTextureCorners(corners, a);
//		SceneGraphComponent child3 = new SceneGraphComponent("Snap Texture Coordinates");
//		child3.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child3);
		
		removeOutside(edges, hds);
//		SceneGraphComponent child4 = new SceneGraphComponent("Outside removed");
//		child4.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child4);
		
		
		SpringRemeshingUtility.straightenBoundary(lattice, edges, polygon.getCorners(), a);
//		SceneGraphComponent child5 = new SceneGraphComponent("Straighten Boundary");
//		child5.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child5);
		
		
		SpringRemeshingUtility.relaxInterior(lattice, polygon.getCorners(), true, false, a);
//		SceneGraphComponent child6 = new SceneGraphComponent("Relax Interior");
//		child6.setGeometry(conv.heds2ifs(hds, new AdapterSet(new VPositionAdapter())));
//		root.addChild(child6);
		
		RemeshingUtility.alignRemeshBoundary(hds, chds, a);
		
//		JRViewer.display(root);
		return hds;
	}

	private LinkedList<LatticeLine2D<V, E, F, HDS>> findLatticeLines(LinkedList<V> corners, AdapterSet a) {
		LinkedList<LatticeLine2D<V, E, F, HDS>> lines = new LinkedList<LatticeLine2D<V, E, F, HDS>>();
		Iterator<V> ci = corners.iterator();
		V c2 = ci.next();
		V c1 = c2;
		do {
			c2 = ci.next();
			double[] 
		       v1 = a.getD(TexturePosition2d.class, c1),
		       v2 = a.getD(TexturePosition2d.class, c2);
			LatticeLine2D<V, E, F, HDS> line = lattice.getClosestLatticeLine(new double[]{v1[0],v1[1]}, new double[]{v2[0]-v1[0],v2[1]-v1[1]});
			if(cornersOnLattice && !lines.isEmpty()) { 
				forceIntersectionOnLattice(lines.getLast(), line);
			}
			lines.add(line);
			c1 = c2;
		} while(ci.hasNext());
		c2 = corners.getFirst();
		double[] 
		       v1 = a.getD(TexturePosition2d.class, c1),
		       v2 = a.getD(TexturePosition2d.class, c2);
		LatticeLine2D<V, E, F, HDS> line = lattice.getClosestLatticeLine(new double[]{v1[0],v1[1]}, new double[]{v2[0]-v1[0],v2[1]-v1[1]});
		if(cornersOnLattice) {
			forceIntersectionOnLattice(lines.getLast(), line);
		}
		lines.add(line);
		return lines;
	}

	private void forceIntersectionOnLattice(LatticeLine2D<V, E, F, HDS> line1, LatticeLine2D<V, E, F, HDS> line2) {
		double[] pt = line1.intersect(line2);
		while(!lattice.isLatticePoint(pt)) {
			line2.c += 1;
			pt = line1.intersect(line2);
		}
	}

	private LatticePolygon2D<V, E, F, HDS> findLatticePolygon(
		LinkedList<LatticeLine2D<V, E, F, HDS>> lines, 
		AdapterSet a
	) throws RemeshingException {
		LinkedList<V> verts = new LinkedList<V>();
		LinkedList<V> corners = new LinkedList<V>();
		LatticeLine2D<V, E, F, HDS> l1 = lines.getLast();
		LatticeLine2D<V, E, F, HDS> l2 = lines.getLast();
		Iterator<LatticeLine2D<V, E, F, HDS>> li = lines.iterator();
		do {
			l1 = l2;
			l2 = li.next();
			double[] pt = l1.intersect(l2);
			V v = lattice.getLatticeVertex(pt);
			if(!verts.isEmpty()) {
				if(verts.getLast() == v) {
					throw new RemeshingException("Two boundary vertices have been identified. Please refine texture.");
				}
				List<V> segment = l1.getOpenSegment(verts.getLast(),v,newVertices, a);
				
				verts.addAll(segment);
			}
			verts.add(v);
			corners.add(v);
		} while(li.hasNext());
		List<V> segment = lines.getLast().getOpenSegment(verts.getLast(),verts.getFirst(),newVertices, a);
		verts.addAll(segment);
		return new LatticePolygon2D<V, E, F, HDS>(verts,corners);
	}
	

	private LinkedList<E> createInducedPolygon(LatticePolygon2D<V, E, F, HDS> polygon) {
		LinkedList<E> bdPolygon = new LinkedList<E>();
		List<V> boundaryVertices = polygon.getVertices();
		int n = boundaryVertices.size();
		for(int i = 0; i < n; ++i) {
			V v1 = boundaryVertices.get(i);
			V v2 = boundaryVertices.get((i + 1) % n);
			E e = HalfEdgeUtils.findEdgeBetweenVertices(v1, v2);
			if(e == null) {
				List<E> edges = lattice.insertEdge(boundaryVertices.get(i),boundaryVertices.get((i+1)%boundaryVertices.size()), newVertices);
				bdPolygon.addAll(edges);
			} else {
				bdPolygon.add(e);
			}
		}
		return bdPolygon;
	}

	private void removeExteriorVertices(List<E> polygon) {
		F intFace = polygon.get(0).getRightFace();
		HalfEdgeDataStructure<V, E, F> hds = intFace.getHalfEdgeDataStructure();
		LinkedList<F> queue = new LinkedList<F>();
		HashSet<F> visited = new HashSet<F>();
		queue.add(intFace);
		visited.add(intFace);
		while (!queue.isEmpty()){
			F actFace = queue.poll();
			List<F> star = new LinkedList<F>();
			for(E e : HalfEdgeUtils.boundaryEdges(actFace)) {
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
		outsideVerts.addAll(hds.getVertices());
		for(F f : visited) {
			outsideVerts.removeAll(HalfEdgeUtils.boundaryVertices(f));
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

	private void removeOutside(LinkedList<E> polygon, HDS hds) {
		for(E e : polygon) {
			hds.removeFace(e.getLeftFace());
		}
		removeExteriorVertices(polygon);
	}
	

	public void setLattice(Lattice<V, E, F, HDS> l) {
		lattice = l;
	}
}
