package de.varylab.varylab.math.functional.edgelength;

import de.jtem.halfedgetools.jreality.node.DefaultJREdge;
import de.varylab.varylab.math.functional.EdgeLengthAdapters.Length;

public class LengthAdapter implements Length<DefaultJREdge> {

	private double 
		l0 = 0.0;
	
	public LengthAdapter(double l) {
		this.l0 = l;
	}
	
	@Override
	public Double getTargetLength(DefaultJREdge e) {
		return l0;
	}
	
	public void setL0(double l0) {
		this.l0 = l0;
	}
	
}


