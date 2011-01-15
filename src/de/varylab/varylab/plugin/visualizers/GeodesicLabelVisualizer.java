package de.varylab.varylab.plugin.visualizers;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Color;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

public class GeodesicLabelVisualizer extends VisualizerPlugin {

	private Map<Integer, double[]>
		colorMap = new HashMap<Integer, double[]>();
	private Random
		rnd = new Random();
	
	
	@Color
	private class GeodesicColorsAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {
		
		private final double[]
		    defaultColor = {0.1,0.1,0.1};
		
		public GeodesicColorsAdapter() {
			super(null, VEdge.class, null, double[].class, true, false);
		}
		
		@Override
		public double[] getEdgeValue(VEdge e, AdapterSet a) {
			int label = e.getGeodesicLabel();
			if (label == -1) return defaultColor;
			if (!colorMap.containsKey(label)) {
				double[] color = {rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble()};
				colorMap.put(label, color);
			}
			return colorMap.get(label);
		}

		@Override
		public double getPriority() {
			return 3;
		}
		
	}
	
	
	
	@Override
	public AdapterSet getAdapters() {
		return new AdapterSet(new GeodesicColorsAdapter());
	}
	
	
	@Override
	public String getName() {
		return "Geodesics";
	}

}
