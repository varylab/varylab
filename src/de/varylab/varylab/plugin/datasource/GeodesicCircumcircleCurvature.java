package de.varylab.varylab.plugin.datasource;

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
import de.varylab.varylab.halfedge.adapter.type.CircumcircleCurvature;
import de.varylab.varylab.utilities.GeodesicUtility;

public class GeodesicCircumcircleCurvature extends Plugin implements DataSourceProvider {

	private GeodesicCircumcircleCurvatureAdapter
		adapter = new GeodesicCircumcircleCurvatureAdapter();
	
	@CircumcircleCurvature
	private class GeodesicCircumcircleCurvatureAdapter extends AbstractAdapter<Double> {

		public GeodesicCircumcircleCurvatureAdapter() {
			super(Double.class, true, false);
		}

		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Double getV(V v, AdapterSet a) {
			Map<E, E> geoMap = GeodesicUtility.findGeodesicPairs(v, false, false, a);
			double[] curvatures = new double[geoMap.size()];
			if (curvatures.length == 0) return null; // unknown curvature
			int i = 0;
			for (E e : geoMap.keySet()) {
				curvatures[i++] = getE(e,a); 
			}
			return Rn.euclideanNormSquared(curvatures);
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
			Map<E, E> geodesicPairs = GeodesicUtility.findGeodesicPairs(v, false, true, a);
			if (geodesicPairs.isEmpty() || HalfEdgeUtils.isBoundaryVertex(v)) {
				return null;
			}
			E ee = geodesicPairs.get(e);
			if(ee == null) {return null;}
			double[] s = a.getD(Position3d.class, e.getStartVertex());
			double[] t = a.getD(Position3d.class, ee.getStartVertex());
			return GeodesicUtility.circumcircleCurvature(s, vv, t);
		}

		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Vertex.class.isAssignableFrom(nodeClass) || Edge.class.isAssignableFrom(nodeClass);
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
