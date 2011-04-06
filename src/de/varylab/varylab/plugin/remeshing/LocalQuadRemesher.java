package de.varylab.varylab.plugin.remeshing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jreality.math.Pn;
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
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition4d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;

public class LocalQuadRemesher <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> {

	private Set<V>
		textureVertices = new HashSet<V>();

	private static double EPS = 1E-6;
	
	public LocalQuadRemesher() {
	}
	
	public Map<F,F> remesh(HDS surface, HDS remesh, AdapterSet a, boolean projective) {
		surface.createCombinatoriallyEquivalentCopy(remesh);
		
//		Triangulator.triangulate(surface);
		for(V v : surface.getVertices()) {
			a.set(Position.class,remesh.getVertex(v.getIndex()),a.getD(TexturePosition.class, v));
			a.set(TexturePosition.class,remesh.getVertex(v.getIndex()),a.getD(TexturePosition.class, v));
		}
		Map<F,F> newOldMap0 = subdivideInDirection(remesh, 0, a, projective);
		Map<F,F> newOldMap1 = subdivideInDirection(remesh, 1, a, projective);
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

	private Map<F,F> subdivideInDirection(HDS remesh, int direction, AdapterSet a, boolean projective) {
		List<E> oldEdges = new LinkedList<E>();
		for(E e : remesh.getPositiveEdges()) {
			oldEdges.add(e);
		}
		for(E e : oldEdges) {
			insertVerticesInDirection(e, direction, a, projective);
		}
//		for(V v : remesh.getVertices()) {
//			System.out.println(
//					v.getIndex() + ":" 
//						+ Arrays.toString(a.getD(Position.class, v)) + ", " 
//						+ Arrays.toString(a.getD(TexturePosition.class,v)));
//		}
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
				if(Math.abs(coord[direction]/coord[3]*2.0-Math.round(coord[direction]/coord[3]*2.0)) > EPS) {
					continue;
				}
				textureVertices.add(v);
				long approxCoord = Math.round(coord[direction]/coord[3]*2.0);
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

	private void insertVerticesInDirection(E rEdge, int dir, AdapterSet as, boolean projective) {
		
		V 	target = rEdge.getTargetVertex(),
			start  = rEdge.getStartVertex();
		
		int otherDir = (dir+1)%2;
		
		if(projective) {
			double[] 	
			       texStart = as.getD(TexturePosition4d.class, start),
			       texTarget = as.getD(TexturePosition4d.class, target);
			
			if(Math.abs(texStart[dir]*texTarget[3]-texTarget[dir]*texStart[3]) < EPS) { //horizontal
				return;
			}
			
			double
				min = 2.0*texStart[dir]/texStart[3],
				max = 2.0*texTarget[dir]/texTarget[3];
			
			boolean reverse = min > max;

			int k = (int)(reverse?Math.floor(min):Math.ceil(min));
			int stop = (int)(reverse?Math.ceil(max):Math.floor(max));
			
			while((reverse && (k >= stop)) || (!reverse && (k <= stop)) ) {
				double lambda = (k*texTarget[3]-2.0*texTarget[dir])/( 2.0*(texStart[dir]-texTarget[dir])-k*(texStart[3]-texTarget[3]));
				double[] newTexCoord = Rn.linearCombination(null, lambda, texStart, 1-lambda, texTarget);
				if( (Pn.distanceBetween(newTexCoord,texStart, Pn.EUCLIDEAN) >= EPS) &&
						(Pn.distanceBetween(newTexCoord,texTarget, Pn.EUCLIDEAN) >= EPS) ) {
					V newVertex = TopologyAlgorithms.splitEdge(rEdge);
					as.set(TexturePosition.class, newVertex, newTexCoord);
					as.set(Position.class,newVertex,newTexCoord);
					rEdge = rEdge.getNextEdge();
				}
				k = (reverse?k-1:k+1);
			}
			       
		} else {
			double[] 	
			       texStart = as.getD(TexturePosition2d.class, start),
			       texTarget = as.getD(TexturePosition2d.class, target),
			       texDir = Rn.subtract(null, texTarget, texStart);

			if(texDir[dir] != 0) { // non-horizontal
				double[] 
				       texStep = Rn.times(null, 0.5/Math.abs(texDir[dir]), texDir);

				double[] first = new double[2];
				first[dir] = ((texDir[dir]>0)?Math.ceil(2*texStart[dir]):Math.floor(2*texStart[dir]))/2.0;

				first[otherDir] = (first[dir]-texStart[dir])/texDir[dir]*texDir[otherDir]+texStart[otherDir];
				int i = 0;
				if(first[dir] == texStart[dir]) {
					++i;
				}
				while(texDir[dir]*(first[dir]+i*texStep[dir] - texTarget[dir]) < 0) {
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
	}
	
	public boolean isTextureVertex(V v) {
		return textureVertices.contains(v);
	}
}
