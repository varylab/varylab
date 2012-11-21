package de.varylab.varylab.plugin.datasource;

import static de.jreality.math.Rn.euclideanAngle;
import static de.jreality.math.Rn.innerProduct;
import static de.jreality.math.Rn.subtract;
import static de.jreality.math.Rn.times;
import static java.lang.Math.sin;

import java.util.Map;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.utilities.GeodesicUtility;

public class GeodesicCurvature extends Plugin implements DataSourceProvider {

	private GeodesicEdgeCurvatureAdapter
		adapter = new GeodesicEdgeCurvatureAdapter();
	private double[]	
		vec1 = new double[3],
		vec2 = new double[3],
		vec3 = new double[3],
		vec4 = new double[3];
	
	private class GeodesicEdgeCurvatureAdapter extends AbstractAdapter<Double> {

		public GeodesicEdgeCurvatureAdapter() {
			super(Double.class, true, false);
		}

		/**
		 * Uses the radius of the circumcircle in the plane normal to the vertex normal as discrete curvature.
		 */
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Double getV(V v, AdapterSet a) {
			double[] vv = a.getD(Position3d.class, v);
			double[] nv = a.getD(Normal.class, v);
			Map<E, E> geodesicPairs = GeodesicUtility.findGeodesicPairs(v, false, false, a);
			if (geodesicPairs.isEmpty() || HalfEdgeUtils.isBoundaryVertex(v)) {
				return null;
			}
			double curvature = 0.0;
			for (E e : geodesicPairs.keySet()) {
				E ee = geodesicPairs.get(e);
				double[] s = a.getD(Position3d.class, e.getStartVertex());
				double[] t = a.getD(Position3d.class, ee.getStartVertex());
				subtract(vec1, s, vv);
				subtract(vec2, t, vv);
				subtract(vec1, vec1, times(vec3, innerProduct(nv, vec1), nv)); 
				subtract(vec2, vec2, times(vec4, innerProduct(nv, vec2), nv));
				double alpha = euclideanAngle(vec1, vec2);
				Rn.subtract(vec3, vec1, vec2);
				double la = Rn.euclideanNorm(vec3);
				double r = la / 2.0 / sin(alpha/2);
				if (1 / r < 0) {
					System.out.println("negative geodesic curvature!");
				}
				curvature += 1 / r; 
			}
			return curvature;
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Vertex.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public String toString() {
			return "Geodesic Curvature";
		}
		
	}
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(adapter);
	}

}
