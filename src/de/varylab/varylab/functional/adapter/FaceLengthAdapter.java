package de.varylab.varylab.functional.adapter;

import java.util.HashMap;
import java.util.Map;

import de.jreality.math.Pn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.adapter.RoundingMode;

public class FaceLengthAdapter implements Length<VEdge> {
	
	Map<VFace, Double>
		faceLengthMap = new HashMap<>();
	
	public FaceLengthAdapter(VHDS hds, RoundingMode roundingMode) {
		int nEdges = 0;
		for(VFace f : hds.getFaces()) {
			nEdges = 0;
			double targetLength = 0;
			switch (roundingMode) {
			case FLOOR:
				targetLength = Double.MAX_VALUE;
				for(VEdge e : HalfEdgeUtils.boundaryEdges(f)) {
					double d = Pn.distanceBetween(e.getStartVertex().getP(), e.getTargetVertex().getP(),Pn.EUCLIDEAN);
					if(d < targetLength) {
						targetLength = d;
					}
				}
				break;
			case CEIL:
				targetLength = Double.MIN_VALUE;
				for(VEdge e : HalfEdgeUtils.boundaryEdges(f)) {
					double d = Pn.distanceBetween(e.getStartVertex().getP(), e.getTargetVertex().getP(),Pn.EUCLIDEAN);
					if(d > targetLength) {
						targetLength = d;
					}
				}
				break;
			case CLOSEST:
			default:
				for(VEdge e : HalfEdgeUtils.boundaryEdges(f)) {
					double d = Pn.distanceBetween(e.getStartVertex().getP(), e.getTargetVertex().getP(),Pn.EUCLIDEAN);
					targetLength += d;
					++nEdges;
				}
				targetLength /= (double)nEdges;
				break;
			}
			faceLengthMap.put(f, targetLength);
		}
	}

	@Override
	public Double getTargetLength(VEdge e) {
		if(e.getLeftFace() == null) {
			return faceLengthMap.get(e.getRightFace());
		} else {
			return faceLengthMap.get(e.getLeftFace());
		}
	}

}
