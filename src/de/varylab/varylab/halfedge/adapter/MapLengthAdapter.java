package de.varylab.varylab.halfedge.adapter;

import java.util.Map;

import de.varylab.varylab.functional.EdgeLengthAdapters.Length;
import de.varylab.varylab.halfedge.VEdge;

public class MapLengthAdapter implements Length<VEdge> {

	private double
		defaultLength = 1.0;
	private Map<VEdge, Double> 
		lengthMap = null;
	
	public MapLengthAdapter(double defaultLength, Map<VEdge, Double> lengthMap) {
		this.defaultLength = defaultLength;
		this.lengthMap = lengthMap;
	}
	
	@Override
	public Double getTargetLength(VEdge e) {
		if (!lengthMap.containsKey(e)) {
			return defaultLength;
		} else {
			return lengthMap.get(e);
		}
	}
	
}
