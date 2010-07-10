package de.varylab.varylab.hds.adapter;

import java.util.Map;

import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.math.functional.EdgeLengthAdapters.Length;

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
