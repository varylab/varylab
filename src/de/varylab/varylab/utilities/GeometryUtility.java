package de.varylab.varylab.utilities;

import static de.jtem.halfedge.util.HalfEdgeUtils.boundaryVertices;
import static de.jtem.halfedgetools.util.GeometryUtility.circumCircle;
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

public class GeometryUtility {

	/**
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
		if(bd.size() == 3) {
			double[] p1 = as.getD(Position3d.class, bd.get(0));
			double[] p2 = as.getD(Position3d.class, bd.get(1));
			double[] p3 = as.getD(Position3d.class, bd.get(2));
			double a = Rn.euclideanDistance(p1, p2);
			double b = Rn.euclideanDistance(p2, p3);
			double c = Rn.euclideanDistance(p3, p1);
			double s = 0.5*(a+b+c);
			double[] center = Rn.linearCombination(null, b/(a+b+c), p1, 1, Rn.linearCombination(null, c/(a+b+c), p2, a/(a+b+c), p3));
			double a1 = 0,b1 = 0,c1 = 0;
			for (int k=0; k<3; k++) {
	           a1 += (p1[k] - p2[k]) * (p1[k] - p2[k]);
	           b1 += (p1[k] - p2[k]) * (p3[k] - p2[k]);
	           c1 += (p3[k] - p2[k]) * (p3[k] - p2[k]);
			}
			double area = a1*c1-b1*b1;
			if (area <= 0.0) {
	           area = sqrt(-area) / 2.0;
			} else {
	           area = sqrt(area) / 2.0;
			}
			return  new double[]{center[0], center[1], center[2], area/s};
		}
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
	
	public static < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] getCircumcircle(F f, AdapterSet a) {
		double[][] boundaryVertices = new double[HalfEdgeUtils.boundaryVertices(f).size()][3];
		int i = 0;
		for(V v: HalfEdgeUtils.boundaryVertices(f)) {
			boundaryVertices[i++] = a.getD(Position3d.class, v);
		}
		double[] circle = new double[4];
		i = 0;
		for(int j = 0; j < boundaryVertices.length/3; ++j) {
			Rn.add(circle,circle, circumCircle(boundaryVertices[j], boundaryVertices[j+boundaryVertices.length/3], boundaryVertices[j+2*boundaryVertices.length/3]));
			i++;
		}
		Rn.times(circle, 1.0/i, circle);
		return circle;
	}

}
