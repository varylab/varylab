package de.varylab.varylab.utilities;

import static de.jreality.math.Rn.euclideanAngle;
import static de.jreality.math.Rn.subtract;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.varylab.varylab.halfedge.adapter.type.GeodesicLabel;

public class GeodesicUtility {

	public static double circumcircleCurvatureSquared(double[] a, double[] b, double[] c) {
		double[] vec1 = subtract(null, a, b);
		double[] vec2 = subtract(null, c, b);
		double alpha = euclideanAngle(vec1, vec2);
		double[] vec3 = Rn.subtract(null, vec1, vec2);
		double l2 = Rn.euclideanNormSquared(vec3);
		double sine = Math.sin(alpha);
		return 4 * sine * sine /l2;
	}
	
	public static double circumcircleCurvature(double[] a, double[] b, double[] c) {
		return Math.sqrt(circumcircleCurvatureSquared(a, b, c));
	}

	/**
	 * Finds pairs of edges in the star of vertex v which have the
	 * same geodesic label. The remaining edges with label -1 are paired
	 * such that there are the same number of edges with label -1 on the left
	 * as on the right of the pair. If this is not possible the edges remain unpaired.
	 * @param v 
	 * @param symmetric if true symmetrizes the map of pairs
	 * @return a map which maps one edge of each pair onto its partner
	 */
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	>  Map<E, E> findGeodesicPairs(V v, boolean manualOnly, boolean symmetric, AdapterSet a) {
		Map<E, E> r = new HashMap<E, E>();
		List<E> star = HalfEdgeUtils.incomingEdges(v);
		Set<Integer> geodesicsSet = new HashSet<Integer>();
		for (E e : star) {
			Integer index = a.getDefault(GeodesicLabel.class, e, -1);
			geodesicsSet.add(index);
		}
		for (Integer index : geodesicsSet) {
			if (index == -1) continue;
			List<E> gSet = new LinkedList<E>();
			for (E e : star) {
				Integer i = a.getDefault(GeodesicLabel.class, e, -1);
				if (i.equals(index)) {
					gSet.add(e);
				}
			}
			if (gSet.size() == 2) {
				r.put(gSet.get(0), gSet.get(1));
				if(symmetric) {
					r.put(gSet.get(1), gSet.get(0));
				}
			}
			star.removeAll(gSet);
		}
		if (manualOnly || star.size() % 2 != 0 || star.size() < 4) return r;
		int nn = star.size();
		for (int i = 0; i < nn/2; i++) {
			E e1 = star.get(i);
			E e2 = star.get(i + nn/2);
			r.put(e1, e2);
			if(symmetric) {
				r.put(e2, e1);
			}
		}
		return r;
	}
}
