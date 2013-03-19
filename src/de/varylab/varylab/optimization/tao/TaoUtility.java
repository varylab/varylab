package de.varylab.varylab.optimization.tao;

import de.jtem.halfedge.HalfEdgeDataStructure;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.optimization.VaryLabFunctional;

public class TaoUtility {

	public static <
		HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>
	> int[] getPETScNonZeros(HDS hds, VaryLabFunctional fun){
		int [][] sparseStucture = fun.getNonZeroPattern(hds);
		int [] nnz = new int[sparseStucture.length];
		for(int i = 0; i < nnz.length; i++){
			nnz[i] = sparseStucture[i].length;
		}
		return nnz;
	}

}
