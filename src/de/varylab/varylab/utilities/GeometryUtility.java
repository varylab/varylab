package de.varylab.varylab.utilities;

import static de.jtem.halfedge.util.HalfEdgeUtils.boundaryVertices;
import static java.lang.Math.PI;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

public class GeometryUtility {

	/**
	 * TODO: Generalize to work triangles
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param <HDS>
	 * @param f
	 * @param as
	 * @return
	 */
	public static < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] getIncircle(F f, AdapterSet as) {
		List<V> bd = boundaryVertices(f);
		if (bd.size() != 4) return new double[] {0,0,0,1};
		double[] p1 = as.getD(Position3d.class, bd.get(0));
		double[] p2 = as.getD(Position3d.class, bd.get(1));
		double[] p3 = as.getD(Position3d.class, bd.get(2));
		double[] p4 = as.getD(Position3d.class, bd.get(3));
		double[] v2 = Rn.subtract(null, p2, p1);
		double[] v4 = Rn.subtract(null, p4, p1);
		if (Math.abs(Rn.euclideanAngle(v2, v4) - PI) < 1E-1) { // rotate
			double[] tmp = p1;
			p1 = p2; p2 = p3; p3 = p4; p4 = tmp;
			v2 = Rn.subtract(null, p2, p1);
			v4 = Rn.subtract(null, p4, p1);
		}
		double p = Rn.euclideanDistance(p1, p3);
		double q = Rn.euclideanDistance(p2, p4);
		double a = Rn.euclideanDistance(p1, p2);
		double b = Rn.euclideanDistance(p2, p3);
		double c = Rn.euclideanDistance(p3, p4);
		double d = Rn.euclideanDistance(p4, p1);
		double alpha = Rn.euclideanAngle(v2, v4) / 2;
		double s = 0.5 * (a+b+c+d);
		double r = p*p*q*q - (a-b)*(a-b)*(a+b-s)*(a+b-s);
		r = Math.sqrt(r) / (2*s);
		double len = r / Math.sin(alpha);
		Rn.normalize(v2, v2);
		Rn.normalize(v4, v4);
		double[] dir = Rn.average(null, new double[][] {v2, v4});
		Rn.setToLength(dir, dir, len);
		double[] m = Rn.add(null, p1, dir);
		return new double[] {m[0], m[1], m[2], r};
	}

}
