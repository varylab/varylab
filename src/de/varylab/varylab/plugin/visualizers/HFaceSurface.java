package de.varylab.varylab.plugin.visualizers;

import static de.jtem.projgeom.P5.LINE_SPACE;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.projgeom.P5;
import de.jtem.projgeom.PlueckerLineGeometry;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.utilities.SelectionUtility;

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
	
	public void propagateQ(VEdge e, double s) {
		HashSet<VFace> facesDone = new HashSet<VFace>();
		HashMap<VFace,VEdge> edgeForPreviousQMap = new HashMap<VFace, VEdge>();
		
		if(e.getLeftFace() == null) {
			e = e.getOppositeEdge();
		}
		LinkedList<VFace> queue = new LinkedList<VFace>();
		VFace vf = e.getLeftFace();
		HFace hf = vhMap.get(vf);
		
		double[] q = Rn.linearCombination(null, Math.cos(s), hf.getDiagonals()[0], Math.sin(s), hf.getDiagonals()[1]);
		
		hf.setQ(e, q);
		facesDone.add(vf);
		for(VEdge be: HalfEdgeUtils.boundaryEdges(vf)) {
			VFace nf = be.getRightFace();
			if(nf != null && !facesDone.contains(nf)) {
				edgeForPreviousQMap.put(nf,be);
				queue.push(nf);
			}
		}
		while(!queue.isEmpty()) {
			vf = queue.pollLast();
			hf = vhMap.get(vf);
			VEdge prevEdge = edgeForPreviousQMap.get(vf);
			VEdge aktEdge = prevEdge.getOppositeEdge();
			
			double[] pc1 = hf.getPluecker(aktEdge);
			double[] pc2 = hf.getPluecker(SelectionUtility.getOppositeEdgeInFace(aktEdge));
			double[] prevQ = vhMap.get(prevEdge.getLeftFace()).getQ(prevEdge);
			double[] aktQ = P5.getIntersectionOfLineWithPolar(null, prevQ, pc1 , pc2, LINE_SPACE);
			
			hf.setQ(aktEdge,aktQ);
			facesDone.add(vf);
			for(VEdge be: HalfEdgeUtils.boundaryEdges(vf)) {
				VFace nf = be.getRightFace();
				if(nf != null && !facesDone.contains(nf)) {
					queue.push(nf);
					edgeForPreviousQMap.put(nf,be);
				}
			}	
		}
	}

	public void propagateParametrisation() {
		for(Strip<VEdge,VFace> s : stripDecomposition.getStrips()) {
			ListIterator<VFace> sit = s.getIterator();
			VFace vf = sit.next();
			HFace hf = vhMap.get(vf);
			VEdge startEdge = s.getLeftEdge(vf);
			double d = 0.5;
			double[] y = Rn.linearCombination(null, d, startEdge.getStartVertex().position, 1-d, startEdge.getTargetVertex().position);
			// TODO: This won't work anymore when homogeneous coords are implemented in the halfedge project...
			hf.setParameterLine(startEdge,Pn.homogenize(null, y));
			
			while(sit.hasNext()) {
				vf = sit.next();
				hf = vhMap.get(vf);
				VEdge pe = s.getLeftEdge(vf).getOppositeEdge();
				propagateParametrisation(pe,hf);
			}
		}
	}

	private void propagateParametrisation(VEdge e, HFace nextFace) {
		HFace aktFace = vhMap.get(e.getLeftFace());
		double[]
		       pc1 = aktFace.getPluecker(e),
		       pc2 = aktFace.getParameterLine(e.getPreviousEdge());
		nextFace.setParameterLine(e.getOppositeEdge(), PlueckerLineGeometry.lineIntersectLineWhileExistenceIsUnchecked(null, pc1, pc2));
	}

	public Collection<HFace> getFaces() {
		return vhMap.values();
	}

}
