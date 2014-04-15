package de.varylab.varylab.halfedge.adapter;

import java.util.TreeSet;

import de.jreality.math.Pn;
import de.varylab.varylab.functional.adapter.Length;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;

public class DiscreteLengthAdapter implements Length<VEdge> {

	private double[] el = null;
	
	public DiscreteLengthAdapter(double min, double max, int steps, RoundingMode mode, VHDS hds) {
		el = new double[hds.numEdges()];
		
		TreeSet<Double> lengths = new TreeSet<Double>();
		double step = (max-min)/steps;
		for(int i = 0; i <= steps; ++i) {
			lengths.add(min + i*step);
		}
		for(VEdge e: hds.getPositiveEdges()) {
			VVertex s = e.getStartVertex();
			VVertex t = e.getTargetVertex();
			double d = Pn.distanceBetween(s.getP(), t.getP(), Pn.EUCLIDEAN);
			Double targetLength = null;
			switch (mode) {
			case FLOOR:
				targetLength = lengths.floor(d);
				if(targetLength == null) {
					targetLength = min;
				}
				break;
			case CEIL:
				targetLength = lengths.ceiling(d);
				if(targetLength == null) {
					targetLength = max;
				}
				break;
			case CLOSEST:
				Double 
					floor = lengths.floor(d),
					ceil = lengths.ceiling(d);
				if(floor == null) {
					targetLength = ceil;
				} else if(ceil == null) {
					targetLength = floor;
				} else {
					if(ceil - d > d - floor) {
						targetLength = floor;
					} else {
						targetLength = ceil;
					}
				}
				break;
			default:
				targetLength = 0.0;
				break;
			}
			el[e.getIndex()] = targetLength;
			el[e.getOppositeEdge().getIndex()] = targetLength;
		}
	}
	
	@Override
	public Double getTargetLength(VEdge e) {
		return el[e.getIndex()];
	}
	
}
