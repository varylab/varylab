package de.varylab.varylab.plugin.visualizers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;

public class HFaceSurface {

	private HashMap<VFace,HFace> 
		vhMap = new HashMap<VFace, HFace>();
	
	private StripDecomposition<VEdge, VFace, VHDS>
		stripDecomposition = null;
	
	public HFaceSurface(VHDS hds) {
		stripDecomposition = new StripDecomposition<VEdge, VFace, VHDS>(hds);
		for(VFace f : hds.getFaces()) {
			HFace hf = new HFace(f);
			vhMap.put(f, hf);
		}
	} 
	
	public int getNumberOfParameters() {
		return stripDecomposition.getNumberOfStrips();
	}
	
	public void propagateQ(VEdge e, double[] q) {
		HashSet<VFace> facesDone = new HashSet<VFace>();
		HashMap<VFace,VEdge> previousQMap = new HashMap<VFace, VEdge>();
		
		if(e.getLeftFace() == null) {
			e = e.getOppositeEdge();
		}
		LinkedList<VFace> queue = new LinkedList<VFace>();
		VFace vf = e.getLeftFace();
		HFace hf = vhMap.get(vf);
		hf.setQ(e, q);
		facesDone.add(vf);
		for(VEdge be: HalfEdgeUtils.boundaryEdges(vf)) {
			VFace nf = be.getRightFace();
			if(nf != null && !facesDone.contains(nf)) {
				previousQMap.put(nf,be);
				queue.push(nf);
			}
		}
		while(!queue.isEmpty()) {
			vf = queue.pollLast();
			hf = vhMap.get(vf);
			VEdge pe = previousQMap.get(vf);
			hf.setQ(pe.getOppositeEdge(),vhMap.get(pe.getLeftFace()).getQ(pe));
			facesDone.add(vf);
			for(VEdge be: HalfEdgeUtils.boundaryEdges(vf)) {
				VFace nf = be.getRightFace();
				if(nf != null && !facesDone.contains(nf)) {
					queue.push(nf);
					previousQMap.put(nf,be);
				}
			}	
		}
	}

	public void propagateParametrisation() {
		for(Strip<VEdge,VFace> s : stripDecomposition.getStrips()) {
			ListIterator<VFace> sit = s.getIterator();
			VFace vf = sit.next();
			HFace hf = vhMap.get(vf);
			hf.setParameterLine(s.getLeftEdge(vf),0.5);
			while(sit.hasNext()) {
				vf = sit.next();
				hf = vhMap.get(vf);
				VEdge pe = s.getLeftEdge(vf).getOppositeEdge();
				propagateParametrisation(pe,hf);
			}
		}
	}

	private void propagateParametrisation(VEdge e, HFace nextFace) {
		VEdge oe = e.getOppositeEdge();
		// FIXME: do some intersection here
		nextFace.setParameterLine(oe, 0.5);
	}

	public Collection<HFace> getFaces() {
		return vhMap.values();
	}
}
