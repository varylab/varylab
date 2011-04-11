package de.varylab.varylab.plugin.visualizers;

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
import de.jtem.halfedgetools.plugin.VisualizerPlugin;

public class DiagonalLengthVisualizer extends VisualizerPlugin {

	
	private class DiagonalLengthDifAdapter extends AbstractAdapter<Double> {
		
		public DiagonalLengthDifAdapter() {
			super(Double.class, true, false);
		}

		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Face.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public double getPriority() {
			return 0;
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Double getF(F f, AdapterSet a) {
			List<E> b = HalfEdgeUtils.boundaryEdges(f);
			if (b.size() != 4) return -1.0;
			double[] p1 = a.getD(Position3d.class, b.get(0).getTargetVertex());
			double[] p2 = a.getD(Position3d.class, b.get(1).getTargetVertex());
			double[] p3 = a.getD(Position3d.class, b.get(2).getTargetVertex());
			double[] p4 = a.getD(Position3d.class, b.get(3).getTargetVertex());
			double l1 = Rn.euclideanDistance(p1, p3);
			double l2 = Rn.euclideanDistance(p2, p4);
			double dif = l1 - l2;
			return dif * dif;
		}
	}

	
	@Override
	public String getName() {
		return "Diagonal Lengths Difference";
	}

	@Override
	public AdapterSet getAdapters() {
		AdapterSet result = new AdapterSet();
		result.add(new DiagonalLengthDifAdapter());
		return result;
	}

}
