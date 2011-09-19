package de.varylab.varylab.plugin.datasource;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.jrworkspace.plugin.Plugin;

public class OppositeAnglesCurvature extends Plugin implements DataSourceProvider {

	private GeodesicAnglesCurvatureAdapter
		adapter = new GeodesicAnglesCurvatureAdapter();
	
	private class GeodesicAnglesCurvatureAdapter extends AbstractAdapter<Double> {

		public GeodesicAnglesCurvatureAdapter() {
			super(Double.class, true, false);
		}

		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Double getV(V v, AdapterSet a) {
			if (HalfEdgeUtils.isBoundaryVertex(v)) {
				return 0.0;
			}
			double[] p = a.getD(Position3d.class, v);
			List<V> star = HalfEdgeUtilsExtra.getVertexStar(v);
			int nn = star.size();
			double[] angles = new double[nn/2];
			if(nn % 2 != 0) {
				return 0.0;
			}
			for(int i = 0; i < nn; ++i){
				double[] p1 = a.getD(Position3d.class, star.get(i));
				double[] p2 = a.getD(Position3d.class, star.get((i+1) % nn));
				angles[i%(nn/2)] += ((i>=nn/2)?1.0:-1.0)*FunctionalUtils.angle(p1, p, p2); 
			}
			return Rn.euclideanNormSquared(angles);
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Vertex.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public String toString() {
			return "Opposite Angles Curvature";
		}
		
	}
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(adapter);
	}

}
