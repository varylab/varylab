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
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.functional.ReferenceSurfaceFunctional;
import de.varylab.varylab.plugin.meshoptimizer.ReferenceSurfaceOptimizer;

public class ReferenceDistance extends Plugin implements DataSourceProvider {

	private ReferenceSurfaceOptimizer
		refSurfaceOptimizer = null;
	
	private class ReferenceDistanceAdapter extends AbstractAdapter<Double> {
		
		public ReferenceDistanceAdapter() {
			super(Double.class, true, false);
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Vertex.class.isAssignableFrom(nodeClass);
		}

		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Double getV(V v, AdapterSet a) {
			VHDS ref = refSurfaceOptimizer.getReferenceSurface();
			if (ref == null || ref.numVertices() == 0) {
				return -1.0;
			}
			double[] pos = a.getD(Position3d.class, v);
			ReferenceSurfaceFunctional<VVertex, VEdge, VFace> fun = (ReferenceSurfaceFunctional<VVertex, VEdge, VFace>)refSurfaceOptimizer.getFunctional(null);
			double[] refPos = fun.getClosestPointOnReference(pos);
			return Rn.euclideanDistance(pos, refPos);
		}
		
		@Override
		public String toString() {
			return "Reference Surface Distance";
		}
		
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		refSurfaceOptimizer = c.getPlugin(ReferenceSurfaceOptimizer.class);
	}
	
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(new ReferenceDistanceAdapter());
	}

}
