package de.varylab.varylab.math;

import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;

public class SparseUtility {

	
	
	public static int[] getPETScNonZeros(VHDS hds){
		int [][] sparseStucture = makeNonZeros(hds);
		int [] nnz = new int[sparseStucture.length];
		for(int i = 0; i < nnz.length; i++){
			nnz[i] = sparseStucture[i].length;
		}
		return nnz;
	}
	
	
	public static int[][] makeNonZeros(VHDS hds) {
		int n = 0;
		for (VVertex v : hds.getVertices()) {
			if (v.getSolverIndex() >= 0) {
				n++;
			}
		}
		int[][] nz = new int[n][];
		for (VVertex v : hds.getVertices()) {
			if (v.getSolverIndex() < 0)
				continue;
			List<VEdge> star = HalfEdgeUtils.incomingEdges(v);
			List<Integer> nonZeroIndices = new LinkedList<Integer>();
			nonZeroIndices.add(v.getSolverIndex());
			for (VEdge e : star) {
				VVertex connectedVertex = e.getOppositeEdge().getTargetVertex();
				if (connectedVertex.getSolverIndex() < 0)
					continue;
				nonZeroIndices.add(connectedVertex.getSolverIndex());
			}
			nz[v.getSolverIndex()] = new int[nonZeroIndices.size()];
			int i = 0;
			for (Integer index : nonZeroIndices) {
				nz[v.getSolverIndex()][i++] = index;
			}
		}
		return nz;
	}

}
