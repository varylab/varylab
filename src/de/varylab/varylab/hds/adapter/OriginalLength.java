package de.varylab.varylab.hds.adapter;

import de.jreality.math.Rn;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.functional.EdgeLengthAdapters.Length;

public class OriginalLength implements Length<VEdge> {

	private double[] 
	    el = null; 
	
	public OriginalLength(VHDS hds) {
		el = new double[hds.numEdges()];
		for(VEdge e: hds.getEdges()) {
			VVertex s = e.getStartVertex();
			VVertex t = e.getTargetVertex();
			el[e.getIndex()] = Rn.euclideanDistance(s.position, t.position);
		}
	}
	
	@Override
	public Double getTargetLength(VEdge e) {
		return el[e.getIndex()];
	}

}
