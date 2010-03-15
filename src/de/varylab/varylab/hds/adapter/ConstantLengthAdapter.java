package de.varylab.varylab.hds.adapter;

import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.math.functional.EdgeLengthAdapters.Length;

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
