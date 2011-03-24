package de.varylab.varylab.plugin.remeshing;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;

public class TextureUtility {

	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> LinkedList<V> findCorners(HDS hds, AdapterSet a) {
		LinkedList<V> corners = new LinkedList<V>();
		E be = HalfEdgeUtils.boundaryEdges(hds).iterator().next();
		be = findNextTextureCorner(be, a);
		E e = be;
		do {
			e = findNextTextureCorner(e, a);
			corners.add(e.getStartVertex());
		} while(e != be);
		return corners;
	}

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> E findNextTextureCorner(E be, AdapterSet a) {
		double theta = 0.0;
		do {
			E pe = be.getOppositeEdge();
			be = be.getNextEdge();
			double[] e1 = a.getD(TexturePosition2d.class, be.getTargetVertex());
			double[] e2 = a.getD(TexturePosition2d.class, pe.getTargetVertex());
			double[] m = a.getD(TexturePosition2d.class, be.getStartVertex());
			double[] v1 = Rn.subtract(null, e1, m);
			double[] v2 = Rn.subtract(null, e2, m);
			theta = Rn.euclideanAngle(v1, v2);
		} while(Math.abs(Math.PI - theta) < 1E-3);
		return be;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<V> findTextureVertices(HDS hds, AdapterSet a, Matrix texMatrix) {
		Set<V> textureVertices = new HashSet<V>();
		Matrix inv = texMatrix.getInverse();
		for(V v : hds.getVertices()) {
			double[] texCoord = a.getD(TexturePosition.class, v);
			texMatrix.transformVector(texCoord);
			if((Math.abs(texCoord[0]*2.0-Math.round(texCoord[0]*2.0)) < 1E-6) &&
				(Math.abs(texCoord[1]*2.0-Math.round(texCoord[1]*2.0)) < 1E-6)) {
				textureVertices.add(v);
			}
			inv.transformVector(texCoord);
		}
		return textureVertices;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<E> findTextureEdges(HDS hds, AdapterSet a, Matrix texMatrix) {
	Set<E> textureEdges = new HashSet<E>();
	Matrix inv = texMatrix.getInverse();
	for(E e : hds.getPositiveEdges()) {
		V 	sv = e.getStartVertex(),
			tv = e.getTargetVertex();
		double[] 
		    stex = a.getD(TexturePosition.class, sv),
		    ttex = a.getD(TexturePosition.class, tv),
		    evec = Rn.subtract(null, ttex, stex);
			
		texMatrix.transformVector(stex);
		if( ((Math.abs(evec[0]) < 1E-6) && (Math.abs(stex[0]*2.0-Math.round(stex[0]*2.0)) < 1E-6)) ||
			((Math.abs(evec[1]) < 1E-6) && (Math.abs(stex[1]*2.0-Math.round(stex[1]*2.0)) < 1E-6)) ){
			textureEdges.add(e);
			textureEdges.add(e.getOppositeEdge());
		}
		inv.transformVector(stex);
	}
	return textureEdges;
}

}
