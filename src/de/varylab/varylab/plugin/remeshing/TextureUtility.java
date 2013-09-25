package de.varylab.varylab.plugin.remeshing;

import java.util.Collections;
import java.util.LinkedList;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.EdgeVector;
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
		double[] ev = a.getD(EdgeVector.class, be);
		double[] ev2 = a.getD(EdgeVector.class, be.getOppositeEdge().getNextEdge());
		double orientation = Rn.determinant(new double[][]{{ev[0],ev[1]}, {ev2[0],ev2[1]}});
		E e = be;
		do {
			e = findNextTextureCorner(e, a);
			corners.add(e.getStartVertex());
		} while(e != be);
		if(orientation>0) {
			Collections.reverse(corners);
		}
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
	> double[] getDirection(V v1, V v2, AdapterSet a) {
		double[] p1 = a.getD(TexturePosition2d.class,v1);
		double[] p2 = a.getD(TexturePosition2d.class,v2);
		return Rn.subtract(null, p2, p1);
	}
}
