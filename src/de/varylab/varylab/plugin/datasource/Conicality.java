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
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jrworkspace.plugin.Plugin;

public class Conicality extends Plugin implements DataSourceProvider {

	private ConicalityAdapter
		adapter = new ConicalityAdapter();
	private double[]	
		vec1 = new double[3],
		vec2 = new double[3],
		vec3 = new double[3],
		vec4 = new double[3];
		
	
	private class ConicalityAdapter extends AbstractAdapter<Double> {

		public ConicalityAdapter() {
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
			List<V> neighbors = HalfEdgeUtils.neighboringVertices(v);
			if (neighbors.size() != 4) {
				return 0.0;
			}
			if (HalfEdgeUtils.isBoundaryVertex(v)) {
				return 0.0;
			}
			V v1 = neighbors.get(0);
			V v2 = neighbors.get(1);
			V v3 = neighbors.get(2);
			V v4 = neighbors.get(3);
			double[] p = a.getD(Position.class, v);
			double[] p1 = a.getD(Position.class, v1);
			double[] p2 = a.getD(Position.class, v2);
			double[] p3 = a.getD(Position.class, v3);
			double[] p4 = a.getD(Position.class, v4);
			Rn.subtract(vec1, p1, p);
			Rn.subtract(vec2, p2, p);
			Rn.subtract(vec3, p3, p);
			Rn.subtract(vec4, p4, p);
			double a1 = Rn.euclideanAngle(vec1, vec2);
			double a2 = Rn.euclideanAngle(vec2, vec3);
			double a3 = Rn.euclideanAngle(vec3, vec4);
			double a4 = Rn.euclideanAngle(vec4, vec1);
			return Math.abs((a1 + a3) - (a2 + a4));
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Vertex.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public String toString() {
			return "Conicality";
		}
		
	}
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(adapter);
	}

}
