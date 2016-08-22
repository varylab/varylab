package de.varylab.varylab.utilities;

import static de.jtem.halfedge.util.HalfEdgeUtils.boundaryVertices;
import static de.jtem.halfedgetools.util.GeometryUtility.circumCircle;

import java.util.List;

import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

public class GeometryUtility {

	public static < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] getIncircle(F f, AdapterSet as) {
		List<V> bd = boundaryVertices(f);
		int n = bd.size();
        double[] v0 = as.getD(Position3d.class, bd.get(0));
        double[] v1 = as.getD(Position3d.class, bd.get(1));
        double[] v2 = as.getD(Position3d.class, bd.get(2));
        double[] v3 = as.getD(Position3d.class, bd.get(n - 1));
        return getIncircle(v0, v1, v2, v3);
	}
	
	/**
	 * Calculate the incircle of the tangents v3:v0; v0:v2, and v2:v3 
	 * @param v0
	 * @param v1
	 * @param v2
	 * @param v3
	 * @return Returns an array of length 4 containing the 3 coordinates 
	 * of the center and the radius in the 4th dimension.
	 */
	static double[] getIncircle(double[] v0, double[] v1, double[] v2, double[] v3) {
		// edge vectors
		double[] V0 = Rn.subtract(null, v0, v1);
	    double[] V1 = Rn.subtract(null, v1, v0);
	    double[] V2 = Rn.subtract(null, v2, v1);
	    double[] V3 = Rn.subtract(null, v3, v0);
	    double[] N = Rn.crossProduct(null, V1, V3);
	    Rn.normalize(V0, V0);
	    Rn.normalize(V1, V1);
	    Rn.normalize(V2, V2);
	    Rn.normalize(V3, V3);
	    
	    // find bisecting planes
	    double[] B0 = Rn.add(null, V1, V3);
	    double[] B1 = Rn.add(null, V0, V2);
	    double[] N0 = Rn.crossProduct(null, B0, N);
	    double[] N1 = Rn.crossProduct(null, B1, N);
	    double[] N2 = Rn.crossProduct(null, N, V3);
	    Rn.normalize(N0, N0);
	    Rn.normalize(N1, N1);
	    Rn.normalize(N2, N2);
	    double d0 = Rn.innerProduct(N0, v0);
	    double d1 = Rn.innerProduct(N1, v1);
	    double d2 = Rn.innerProduct(N2, v0);
	    double[] p0 = {N0[0], N0[1], N0[2], -d0};
	    double[] p1 = {N1[0], N1[1], N1[2], -d1};
	    
	    // find intersection
	    v0 = Pn.homogenize(null, v0);
	    v1 = Pn.homogenize(null, v1);
	    v2 = Pn.homogenize(null, v2);
	    v3 = Pn.homogenize(null, v3);
	    double[] p2 = P3.planeFromPoints(null, v0, v1, v3);
	    double[] c = P3.pointFromPlanes(null, p0, p1, p2);
	    Pn.dehomogenize(c, c);
	    
	    // radius
	    c[3] = d2 - Rn.innerProduct(N2, new double[]{c[0], c[1], c[2]});
	    return c;
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
			Rn.add(circle, circle, circumCircle(
				boundaryVertices[j], 
				boundaryVertices[j+boundaryVertices.length/3], 
				boundaryVertices[j+2*boundaryVertices.length/3]
			));
			i++;
		}
		Rn.times(circle, 1.0/i, circle);
		return circle;
	}

}
