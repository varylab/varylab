package de.varylab.varylab.plugin.visualizers;

import static de.jtem.halfedgetools.util.HalfEdgeUtilsExtra.getDegree;
import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Color;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VVertex;

public class OddVertexVisualizer extends VisualizerPlugin {

	@Color
	public class OddVertexAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

		public OddVertexAdapter() {
			super(VVertex.class, null, null, double[].class, true, false);
		}
		
		@Override
		public double getPriority() {
			return 1;
		}
		
		@Override
		public double[] getVertexValue(VVertex v, AdapterSet a) {
			if (getDegree(v) % 2 == 0) {
				return new double[]{0,1,0};
			} else {
				return new double[]{1,0,0};
			}
		}
		
	}
	
	
	@Override
	public AdapterSet getAdapters() {
		return new AdapterSet(new OddVertexAdapter());
	}


	@Override
	public String getName() {
		return "Odd Vertices";
	}

}
