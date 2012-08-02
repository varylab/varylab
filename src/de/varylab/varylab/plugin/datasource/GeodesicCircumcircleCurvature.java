package de.varylab.varylab.plugin.datasource;

import static de.jreality.math.Rn.euclideanAngle;
import static de.jreality.math.Rn.subtract;
import static de.varylab.varylab.math.functional.OppositeEdgesCurvatureFunctional.findGeodesicPairs;

import java.util.Map;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jrworkspace.plugin.Plugin;

public class GeodesicCircumcircleCurvature extends Plugin implements DataSourceProvider {

	private GeodesicCircumcircleCurvatureAdapter
		adapter = new GeodesicCircumcircleCurvatureAdapter();
	private double[]	
		vec1 = new double[3],
		vec2 = new double[3],
		vec3 = new double[3];
	
	private class GeodesicCircumcircleCurvatureAdapter extends AbstractAdapter<Double> {

		public GeodesicCircumcircleCurvatureAdapter() {
			super(Double.class, true, false);
		}

		/**
		 * Uses the radius of the circumcircle in the plane spanned by "consecutive" edges.
		 */
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Double getE(E e, AdapterSet a) {
			V v = e.getTargetVertex();
			double[] vv = a.getD(Position3d.class, v);
			Map<E, E> geodesicPairs = findGeodesicPairs(v, false, true, a);
			if (geodesicPairs.isEmpty() || HalfEdgeUtils.isBoundaryVertex(v)) {
				return null;
			}
			E ee = geodesicPairs.get(e);
			if(ee == null) {return null;}
			double[] s = a.getD(Position3d.class, e.getStartVertex());
			double[] t = a.getD(Position3d.class, ee.getStartVertex());
			subtract(vec1, s, vv);
			subtract(vec2, t, vv);
			double alpha = Math.PI-euclideanAngle(vec1, vec2);
			Rn.subtract(vec3, vec1, vec2);
			double la = Rn.euclideanNorm(vec3);
			double c = 2.0 * (1-Math.cos(2.0*alpha)) / la;
			if (c < 0) {
				System.out.println("negative geodesic curvature!");
			}
			return Math.sqrt(Math.abs(c));
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Edge.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public String toString() {
			return "Geodesic Circumcircle Curvature";
		}
		
	}
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(adapter);
	}

}
