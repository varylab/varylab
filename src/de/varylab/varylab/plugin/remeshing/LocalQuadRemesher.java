package de.varylab.varylab.plugin.remeshing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;

public class LocalQuadRemesher <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> {

	private Set<V>
		textureVertices = new HashSet<V>();
	
	public LocalQuadRemesher() {
	}
	
	public Map<F,F> remesh(HDS surface, HDS remesh, AdapterSet a) {
		surface.createCombinatoriallyEquivalentCopy(remesh);
		
//		Triangulator.triangulate(surface);
		for(V v : surface.getVertices()) {
			a.set(Position.class,remesh.getVertex(v.getIndex()),a.getD(TexturePosition.class, v));
			a.set(TexturePosition.class,remesh.getVertex(v.getIndex()),a.getD(TexturePosition.class, v));
		}
		Map<F,F> newOldMap0 = subdivideInDirection(remesh, 0, a);
		Map<F,F> newOldMap1 = subdivideInDirection(remesh, 1, a);
		Map<F,F> newOldMap = new HashMap<F, F>();
		for(F f : remesh.getFaces()) {
			F f1 = newOldMap1.get(f);
			if(f1 != null) {
				F f2 = newOldMap0.get(f1);
				if(f2 != null) {
					newOldMap.put(f, surface.getFace(f2.getIndex()));
				} else {
					newOldMap.put(f, surface.getFace(f1.getIndex()));
				}
			} else {
				F f2 = newOldMap0.get(f);
				if(f2 != null) {
					newOldMap.put(f, surface.getFace(f2.getIndex()));
				} else {
					newOldMap.put(f, surface.getFace(f.getIndex()));
				}
			}
		}
//		for(V v : remesh.getVertices()) {
//			System.out.println(
//					v.getIndex() + ":" 
//					+ Arrays.toString(a.getD(Position.class, v)) + ", " 
//					+ Arrays.toString(a.getD(TexturePosition.class,v)));
//		}
//		for(F f : newOldMap.keySet()) {
//			System.out.println(f +" -> "+ newOldMap.get(f));
//		}
		return newOldMap;
	}

	private Map<F,F> subdivideInDirection(HDS remesh, int direction, AdapterSet a) {
		List<E> oldEdges = new LinkedList<E>();
		for(E e : remesh.getPositiveEdges()) {
			oldEdges.add(e);
		}
		for(E e : oldEdges) {
			insertVerticesInDirection(e, direction, a);
		}
		return splitFacesInDirection(remesh, direction, a);
	}
	
	private Map<F,F> splitFacesInDirection(HDS remesh, int direction, AdapterSet as) {
		Map<F,F> newOldMap = new HashMap<F, F>();
		Map<F,List<V>> faceVertexMap = new HashMap<F, List<V>>();
		for(F f : remesh.getFaces()) {
			faceVertexMap.put(f, HalfEdgeUtils.boundaryVertices(f));
		}
		for(F f : faceVertexMap.keySet()) {
			Map<Long,V> coordinateVertexMap = new HashMap<Long, V>();
			for(V v : faceVertexMap.get(f)) {
				double[] coord = as.getD(Position.class, v);
				if(Math.abs(coord[direction]*2.0-Math.round(coord[direction]*2.0)) > 1E-6) {
					continue;
				}
				textureVertices.add(v);
				long approxCoord = Math.round(coord[direction]*2.0E6);
				if(!coordinateVertexMap.containsKey(approxCoord)) {
					coordinateVertexMap.put(approxCoord, v);
				} else {
					V oppositeVertex = coordinateVertexMap.get(approxCoord);
					F cf = RemeshingUtility.findCommonFace(v,oppositeVertex);
					E splitEdge = RemeshingUtility.splitFaceAt(cf, v, oppositeVertex);
					if(splitEdge != null) {
						newOldMap.put(splitEdge.getLeftFace(), f);
						newOldMap.put(splitEdge.getRightFace(), f);
					}
				}
			}
		}
		return newOldMap;
	}

	private void insertVerticesInDirection(E rEdge, int direction, AdapterSet as) {
		
		int otherDirection = (direction+1)%2;
		V 	target = rEdge.getTargetVertex(),
			start  = rEdge.getStartVertex();
		
		double[] 	
		       texStart = as.getD(TexturePosition2d.class, start),
		       texTarget = as.getD(TexturePosition2d.class, target),
		       texDir = Rn.subtract(null, texTarget, texStart);
		
		if(texDir[direction] != 0) { // non-horizontal
			double[] 
			       texStep = Rn.times(null, 0.5/Math.abs(texDir[direction]), texDir);
			
			double[] first = new double[2];
			first[direction] = ((texDir[direction]>0)?Math.ceil(2*texStart[direction]):Math.floor(2*texStart[direction]))/2.0;
			
			first[otherDirection] = (first[direction]-texStart[direction])/texDir[direction]*texDir[otherDirection]+texStart[otherDirection];
			int i = 0;
			if(first[direction] == texStart[direction]) {
				++i;
			}
			while(texDir[direction]*(first[direction]+i*texStep[direction] - texTarget[direction]) < 0) {
				V newVertex = TopologyAlgorithms.splitEdge(rEdge);
				double[] newCoord = new double[4];
				double[] newTexCoord = Rn.linearCombination(null, 1.0, first, i, texStep);
				newCoord[0] = newTexCoord[0];
				newCoord[1] = newTexCoord[1];
				newCoord[3] = 1.0;
				as.set(TexturePosition.class, newVertex, newCoord);
				as.set(Position.class,newVertex,newCoord);
				rEdge = rEdge.getNextEdge();
				++i;
			}
		}
	}
	
	public boolean isTextureVertex(V v) {
		return textureVertices.contains(v);
	}
}
