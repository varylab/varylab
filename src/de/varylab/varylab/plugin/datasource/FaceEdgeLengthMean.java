package de.varylab.varylab.plugin.datasource;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Length;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jrworkspace.plugin.Plugin;

public class FaceEdgeLengthMean extends Plugin implements DataSourceProvider {

	private MeanEdgeLengthAdapter
		adapter = new MeanEdgeLengthAdapter();
	
	private class MeanEdgeLengthAdapter extends AbstractAdapter<Double> {

		public MeanEdgeLengthAdapter() {
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
		> Double getF(F f, AdapterSet a) {
			double sum = 0.0;
			double numEdges = 0;
			for (E e : HalfEdgeUtils.boundaryEdges(f)) {
				sum += a.get(Length.class, e, Double.class);
				numEdges++;
			}
			return sum / numEdges; 
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Face.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public String toString() {
			return "Mean Face Edge Length";
		}
		
	}
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(adapter);
	}

}
