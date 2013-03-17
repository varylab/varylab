package de.varylab.varylab.halfedge.adapter;

import de.varylab.varylab.functional.EdgeLengthAdapters.Length;
import de.varylab.varylab.halfedge.VEdge;

public class ConstantLengthAdapter implements Length<VEdge> {

	private double 
		length = 0.0;
	
	public ConstantLengthAdapter(double l0) {
		this.length = l0;
	}
	
	@Override
	public Double getTargetLength(VEdge e) {
		return length;
	}
	
	public void setL0(double l0) {
		this.length = l0;
	}
	
}
