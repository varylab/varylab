package de.varylab.varylab.halfedge.adapter;

import de.jreality.math.Rn;
import de.varylab.varylab.functional.adapter.Length;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;

public class OriginalLength implements Length<VEdge> {

	private double[] 
	    el = null; 
	
	public OriginalLength(VHDS hds) {
		el = new double[hds.numEdges()];
		for(VEdge e: hds.getEdges()) {
			VVertex s = e.getStartVertex();
			VVertex t = e.getTargetVertex();
			el[e.getIndex()] = Rn.euclideanDistance(s.getP(), t.getP());
		}
	}
	
	@Override
	public Double getTargetLength(VEdge e) {
		return el[e.getIndex()];
	}

}
