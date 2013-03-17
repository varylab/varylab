package de.varylab.varylab.halfedge.adapter;

import de.jreality.math.Rn;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.varylab.varylab.functional.EdgeLengthAdapters.Length;
import de.varylab.varylab.halfedge.VEdge;

public class LengthRangeAdapter implements Length<VEdge> {

	private double 
		minLength = 0.0,
		maxLength = Double.POSITIVE_INFINITY;
	
	private AdapterSet
		adapters = null;
	
	public LengthRangeAdapter(double lmin, double lmax, AdapterSet as) {
		if(as != null) {
			adapters = as;			
		} else {
			adapters = new AdapterSet();
		}
		minLength = lmin;
		maxLength = lmax;
	}
	
	@Override
	public Double getTargetLength(VEdge e) {
		double length = getLength(e,adapters);
		return length;
	}
	
	private double getLength(VEdge e, AdapterSet as) {
		double[] 
		       sv = as.get(Position.class, e.getStartVertex(), double[].class),
		       tv = as.get(Position.class, e.getStartVertex(), double[].class);
		double edgeLength = Rn.euclideanDistance(sv, tv);
		if(edgeLength < minLength) {
			return minLength;
		} else if(edgeLength > maxLength) {
			return maxLength;
		} 
		return edgeLength;
	}
}
