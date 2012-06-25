package de.varylab.varylab.plugin.datasource;

import static java.lang.Math.acos;
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
import de.jtem.mfc.field.Complex;
import de.varylab.varylab.plugin.ddg.ChristoffelTransform;

public class IncircleCrossRatio extends Plugin implements DataSourceProvider {

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
			double[] c = ChristoffelTransform.getIncircle(f, a);
			double r = c[3];
			double[] center = {c[0], c[1], c[2]};
			E e0 = f.getBoundaryEdge();
			E e1 = e0.getNextEdge();
			E e2 = e1.getNextEdge();
			double[] p0 = a.getD(Position3d.class, e0.getStartVertex()); 
			double[] p1 = a.getD(Position3d.class, e1.getStartVertex()); 
			double[] p2 = a.getD(Position3d.class, e2.getStartVertex()); 
			double l0 = Rn.euclideanDistance(p0, center);
			double l1 = Rn.euclideanDistance(p1, center);
			double l2 = Rn.euclideanDistance(p2, center);
			double alpha0 = 2 * acos(r / l0);
			double alpha1 = 2 * acos(r / l1);
			double alpha2 = 2 * acos(r / l2);
			Complex z0 = Complex.fromPolar(1, 0.0);
			Complex z1 = Complex.fromPolar(1, alpha0);
			Complex z2 = Complex.fromPolar(1, alpha0 + alpha1);
			Complex z3 = Complex.fromPolar(1, alpha0 + alpha1 + alpha2);
			Complex d0 = z0.minus(z1);
			Complex d1 = z2.minus(z3);
			Complex d2 = z1.minus(z2);
			Complex d3 = z3.minus(z0);
			Complex cr = d0.times(d1).divide(d2).divide(d3);
			return cr.re;
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Face.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public String toString() {
			return "Incircle Cross-Ratio";
		}
		
	}
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(adapter);
	}
	
}
