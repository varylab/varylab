package de.varylab.varylab.plugin.remeshing;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;

public class QuadTextureUtility {

	private enum TextureDirection {
		none,
		horizontal,
		vertical,
		both
	}
	
	private static double EPS = 1E-6;
	
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS createTextureGeometry(HDS hds, AdapterSet a, HalfedgeInterface hif, Matrix textureMatrix) {
		HDS newHDS = hif.createEmpty(hds);
		Set<V> textureVertices = new HashSet<V>(QuadTextureUtility.findTextureVertices(hds, a, textureMatrix, true));
		Collection<V> bdVertices =  HalfEdgeUtils.boundaryVertices(hds);
		Set<E> textureEdges = QuadTextureUtility.findTextureEdges(hds, a, textureMatrix);
		Collection<E> bdEdges = HalfEdgeUtils.boundaryEdges(hds);
		for(E e : bdEdges ) {
			textureEdges.add(e);
			textureEdges.add(e.getOppositeEdge());
		}
		Set<E> visitedTextureInEdges = new HashSet<E>();
		
		//create new vertices
		Map<V,V> textureVertexMap = new HashMap<V, V>();
		for(V v : textureVertices) {
			V newV = newHDS.addNewVertex();
			a.set(Position.class, newV, a.getD(Position.class, v));
			textureVertexMap.put(v, newV);
		}
		HashSet<V> newBdVertices = new HashSet<V>();
		for(V v : bdVertices) { 
			V newV = newHDS.addNewVertex();
			a.set(Position.class, newV, a.getD(Position.class, v));
			textureVertexMap.put(v, newV);
			newBdVertices.add(newV);
		}
		textureVertices.addAll(bdVertices);
		
		//create new edges
		HashSet<E> newBdEdges = new HashSet<E>();
		Map<E,E> textureEdgeMap = new HashMap<E,E>();
		for(V v : textureVertices) {
			for(E e : HalfEdgeUtils.incomingEdges(v)) {
				if(textureEdges.contains(e)) {
					E newEdge = newHDS.addNewEdge();
					newEdge.setIsPositive(true);
					textureEdgeMap.put(e, newEdge);
					newEdge.setTargetVertex(textureVertexMap.get(v));
					if(bdEdges.contains(e)) {
						newBdEdges.add(newEdge);
					}
				}
			}
		}
		
		LinkedList<E> queue = new LinkedList<E>();
		V rootVertex = textureVertices.iterator().next();
		List<E> inTextureEdges = HalfEdgeUtils.incomingEdges(rootVertex);
		inTextureEdges.retainAll(textureEdges);
		queue.addAll(inTextureEdges);
		while(!queue.isEmpty()) {
			E e = queue.pollLast();
			if(visitedTextureInEdges.contains(e)) {
				continue;
			}
			visitedTextureInEdges.add(e);
			V start = e.getTargetVertex();
			E nextEdge = findNextTextureVertex(e.getOppositeEdge(), textureVertices, textureEdges);
			visitedTextureInEdges.add(nextEdge);
			V target = nextEdge.getTargetVertex();
			linkVerticesAndEdges(textureVertexMap.get(start),textureVertexMap.get(target),
								 textureEdgeMap.get(e), textureEdgeMap.get(nextEdge));
			List<E> nextInEdges = HalfEdgeUtils.incomingEdges(target);
			nextInEdges.retainAll(textureEdges);
			nextInEdges.remove(nextEdge);
			queue.addAll(nextInEdges);
		}
		
		
		//link new vertices around edge
		for(V v : textureVertices) {
			E start = getNextTextureEdgeAtVertex(v.getIncomingEdge(),textureEdges);
			E e = start; 
			E next = start;
			do {
				e = next;
				next = getNextTextureEdgeAtVertex(e,textureEdges);
				textureEdgeMap.get(e).linkNextEdge(textureEdgeMap.get(next).getOppositeEdge());
			} while(next != start);
		}
		
		//insert new faces
		for(E e : newHDS.getEdges()) {
			if(e.getLeftFace() == null && !newBdEdges.contains(e)) {
				HalfEdgeUtils.fillHole(e);
			}
		}
		
		return newHDS;
	}
	
	private static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> E findNextTextureVertex(E e, Set<V> textureVertices, Set<E> textureEdges) {
		E tEdge = e;
		V v = e.getTargetVertex();
		while(!textureVertices.contains(v)) {
			tEdge = getNextTextureEdgeAtVertex(tEdge,textureEdges).getOppositeEdge();
			v = tEdge.getTargetVertex();
		}
		return tEdge;
	}
	
	private static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> E getNextTextureEdgeAtVertex(E e, Set<E> textureEdges) {
		do {
			e = e.getNextEdge().getOppositeEdge();
		} while(!textureEdges.contains(e));
		return e;
	}
	
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void linkVerticesAndEdges(V start, V target, E startIn, E targetIn) {
		startIn.linkOppositeEdge(targetIn);
		startIn.setTargetVertex(start);
		targetIn.setTargetVertex(target);
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<V> findTextureVertices(HDS hds, AdapterSet a, Matrix texMatrix, boolean verticesOnly) {
		Set<V> textureVertices = new HashSet<V>();
		for(V v : hds.getVertices()) {
			TextureDirection tDir = onTextureDirection(v, a, texMatrix);
			if((verticesOnly && tDir == TextureDirection.both) ||
			   (!verticesOnly && (tDir == TextureDirection.horizontal || tDir == TextureDirection.vertical))) {
				textureVertices.add(v);
			}
		}
		return textureVertices;
	}

	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> TextureDirection onTextureDirection(V v, AdapterSet a, Matrix texMatrix) {
		double[] texCoord = new double[4];
		System.arraycopy(a.getD(TexturePosition.class, v),0,texCoord,0,4);
		texMatrix.transformVector(texCoord);
		boolean onHorizontalEdge = Math.abs(texCoord[0]*2.0-Math.round(texCoord[0]*2.0)) < EPS;
		boolean onVerticalEdge = Math.abs(texCoord[1]*2.0-Math.round(texCoord[1]*2.0)) < EPS;
		if(onHorizontalEdge && onVerticalEdge) { 
			return TextureDirection.both;
		} else if(onHorizontalEdge) {
			return TextureDirection.horizontal;
		} else if(onVerticalEdge) {
			return TextureDirection.vertical;
		} else {
			return TextureDirection.none;
		}
	}

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<E> findTextureEdges(HDS hds, AdapterSet a, Matrix texMatrix) {
		Set<E> textureEdges = new HashSet<E>();
		for(E e : hds.getPositiveEdges()) {
			if(QuadTextureUtility.isTextureEdge(e, a, texMatrix)) {
				textureEdges.add(e);
				textureEdges.add(e.getOppositeEdge());
			}
		}
		return textureEdges;
	}

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> boolean isTextureEdge(E e, AdapterSet a, Matrix texMatrix) {
		V 	sv = e.getStartVertex(),
			tv = e.getTargetVertex();
		double[] 
		    stex = new double[4],
		    ttex = new double[4];
		System.arraycopy(a.getD(TexturePosition.class, tv), 0, stex, 0, 4);
		System.arraycopy(a.getD(TexturePosition.class, sv), 0, ttex, 0, 4);
		texMatrix.transformVector(stex);
		texMatrix.transformVector(ttex);
		double[]
		       evec = Rn.subtract(null, ttex, stex);
		
		if( ((Math.abs(evec[0]) < 1E-6) && (Math.abs(stex[0]*2.0-Math.round(stex[0]*2.0)) < EPS)) ||
			((Math.abs(evec[1]) < 1E-6) && (Math.abs(stex[1]*2.0-Math.round(stex[1]*2.0)) < EPS)) ){
			return true;
		}
		return false;
	}

}
