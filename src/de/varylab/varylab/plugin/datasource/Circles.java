package de.varylab.varylab.plugin.datasource;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.plugin.visualizers.CircleVisualizer.Circle3d;
import de.varylab.varylab.utilities.GeometryUtility;

public class Circles extends Plugin implements DataSourceProvider {

	private IncircleAdapter
		inAdapter = new IncircleAdapter();
	private CircumcircleAdapter
		circumcircleAdapter = new CircumcircleAdapter();
	
	private class IncircleAdapter extends AbstractAdapter<Circle3d> {

		public IncircleAdapter() {
			super(Circle3d.class, true, false);
		}

		@Override
		public String toString() {
			return "Incircle";
		}

		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Circle3d getF(F f, AdapterSet a) {
			double[] c = GeometryUtility.getIncircle(f, a);
			double[] N = a.getD(Normal.class, f);
			return new Circle3d(new double[]{c[0], c[1], c[2]}, c[3], N);
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Face.class.isAssignableFrom(nodeClass);
		}
		
	}
	
	private class CircumcircleAdapter extends AbstractAdapter<Circle3d> {

		public CircumcircleAdapter() {
			super(Circle3d.class, true, false);
		}

		@Override
		public String toString() {
			return "Circumcircle";
		}

		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Circle3d getF(F f, AdapterSet a) {
			double[] c = GeometryUtility.getCircumcircle(f, a);
			double[] N = a.getD(Normal.class, f);
			return new Circle3d(new double[]{c[0], c[1], c[2]}, c[3], N);
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Face.class.isAssignableFrom(nodeClass);
		}
		
	}
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(inAdapter,circumcircleAdapter);
	}

}
