package de.varylab.varylab.hds.adapter;

import java.util.Map;

import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.math.functional.EdgeLengthAdapters.WeightFunction;

public class MapWeightAdapter implements WeightFunction<VEdge> {

	private double
		defaultWeight = 1.0;
	private Map<VEdge, Double> 
		weightMap = null;
	
	public MapWeightAdapter(double defaultWeight, Map<VEdge, Double> weightMap) {
		this.defaultWeight = defaultWeight;
		this.weightMap = weightMap;
	}
	
	@Override
	public Double getWeight(VEdge e) {
		if (!weightMap.containsKey(e)) {
			return defaultWeight;
		} else {
			return weightMap.get(e);
		}
	}

}
