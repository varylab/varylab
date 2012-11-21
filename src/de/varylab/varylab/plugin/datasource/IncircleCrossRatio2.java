package de.varylab.varylab.plugin.datasource;

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
import de.varylab.varylab.utilities.GeometryUtility;

public class IncircleCrossRatio2 extends Plugin implements DataSourceProvider {

	private CrossRatioAdapter
		adapter = new CrossRatioAdapter();
	
	private class CrossRatioAdapter extends AbstractAdapter<Double> {

		public CrossRatioAdapter() {
			super(Double.class, true, false);
		}

		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Double getF(F f, AdapterSet a) {
			double[] c = GeometryUtility.getIncircle(f, a);
			double r = c[3];
			double[] center = {c[0], c[1], c[2]};
			E e0 = f.getBoundaryEdge();
			E e1 = e0.getNextEdge();
			E e2 = e1.getNextEdge();
			E e3 = e2.getNextEdge();
			double[] p0 = a.getD(Position3d.class, e0.getStartVertex()); 
			double[] p1 = a.getD(Position3d.class, e1.getStartVertex()); 
			double[] p2 = a.getD(Position3d.class, e2.getStartVertex()); 
			double[] p3 = a.getD(Position3d.class, e3.getStartVertex()); 
			double r0 = Rn.euclideanDistance(p0, center);
			double r1 = Rn.euclideanDistance(p1, center);
			double r2 = Rn.euclideanDistance(p2, center);
			double r3 = Rn.euclideanDistance(p3, center);
			double rsq = r*r;
			double l0 = 2 * r * Math.sqrt(1 - rsq/(r0*r0));
			double l1 = 2 * r * Math.sqrt(1 - rsq/(r1*r1));
			double l2 = 2 * r * Math.sqrt(1 - rsq/(r2*r2));
			double l3 = 2 * r * Math.sqrt(1 - rsq/(r3*r3));
			return -l0 * l2 / l1 / l3;
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Face.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public String toString() {
			return "Incircle Cross-Ratio 2";
		}
		
	}
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(adapter);
	}
	
}
