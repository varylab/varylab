package de.varylab.varylab.plugin.datasource;

import static de.jtem.halfedgetools.functional.FunctionalUtils.angle;
import static de.varylab.varylab.math.functional.OppositeEdgesCurvatureFunctional.findGeodesicPairs;
import static java.lang.Math.PI;

import java.util.Map;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jrworkspace.plugin.Plugin;

public class GeodesicCurvature extends Plugin implements DataSourceProvider {

	private GeodesicEdgeCurvatureAdapter
		adapter = new GeodesicEdgeCurvatureAdapter();
	
	private class GeodesicEdgeCurvatureAdapter extends AbstractAdapter<Double> {

		public GeodesicEdgeCurvatureAdapter() {
			super(Double.class, true, false);
		}

		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Double getV(V v, AdapterSet a) {
			double[] p = a.getD(Position3d.class, v);
			Map<E, E> geoMap = findGeodesicPairs(v, false, a);
			double[] angles = new double[geoMap.size()];
			if (angles.length == 0) return null; // unknown curvature
			int i = 0;
			for (E e : geoMap.keySet()) {
				E ee = geoMap.get(e);
				double[] p1 = a.getD(Position3d.class, e.getStartVertex());
				double[] p2 = a.getD(Position3d.class, ee.getStartVertex());
				angles[i++] = PI - angle(p1, p, p2); 
			}
			return Rn.euclideanNormSquared(angles);
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Vertex.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public String toString() {
			return "Exterior Geodesic Curvature";
		}
		
	}
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(adapter);
	}

}
