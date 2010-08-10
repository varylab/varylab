package de.varylab.varylab.plugin.visualizers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;

public class HFaceSurface {

	private HashMap<VFace,HFace> 
		vhMap = new HashMap<VFace, HFace>();
	
	private HashMap<HFace,VFace> 
		hvMap = new HashMap<HFace, VFace>();
	
	private StripDecomposition<VEdge, VFace, VHDS>
		stripDecomposition = null;
	
	public HFaceSurface(VHDS hds) {
		stripDecomposition = new StripDecomposition<VEdge, VFace, VHDS>(hds);
		for(VFace f : hds.getFaces()) {
			HFace hf = new HFace(f);
			hvMap.put(hf, f);
			vhMap.put(f, hf);
		}
	} 
	
	public int getNumberOfParameters() {
		return stripDecomposition.getNumberOfStrips();
	}
	
	public void propagateQ(VEdge e, double[] q) {
		HashSet<VFace> facesDone = new HashSet<VFace>();
		HashMap<VFace,HFace> previousFaceMap = new HashMap<VFace, HFace>();
		
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
				previousFaceMap.put(nf,hf);
				queue.push(nf);
			}
		}
		while(!queue.isEmpty()) {
			vf = queue.pollLast();
			hf = vhMap.get(vf);
			HFace previousFace = previousFaceMap.get(vf);
			propagateQ(previousFace,hf);
			facesDone.add(vf);
			for(VEdge be: HalfEdgeUtils.boundaryEdges(vf)) {
				VFace nf = be.getRightFace();
				if(nf != null && !facesDone.contains(nf)) {
					queue.push(nf);
					previousFaceMap.put(nf,hf);
				}
			}	
		}
	}

	private void propagateQ(HFace previousFace, HFace nextFace) {
		VEdge e = HalfEdgeUtils.findEdgeBetweenFaces(hvMap.get(previousFace), hvMap.get(nextFace));
		VEdge oe = e.getOppositeEdge();
		// FIXME: do some projection here
		nextFace.setQ(oe, previousFace.getQ(e));
	}

	public void propagateParametrisation() {
		for(Strip<VEdge,VFace> s : stripDecomposition.getStrips()) {
			ListIterator<VFace> sit = s.getIterator();
			VFace vf = sit.next();
			HFace hf = vhMap.get(vf);
			hf.setParameterLine(s.getLeftEdge(vf),0.5);
			VFace previousFace = vf;
			while(sit.hasNext()) {
				vf = sit.next();
				propagateParametrisation(vhMap.get(previousFace), vhMap.get(vf));
				previousFace = vf;
			}
		}
	}

	private void propagateParametrisation(HFace previousFace, HFace nextFace) {
		VEdge e = HalfEdgeUtils.findEdgeBetweenFaces(hvMap.get(previousFace), hvMap.get(nextFace));
		VEdge oe = e.getOppositeEdge();
		// FIXME: do some intersection here
		nextFace.setParameterLine(oe, 0.5);
	}

	public Set<HFace> getFaces() {
		return hvMap.keySet();
	}
}
