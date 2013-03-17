package de.varylab.varylab.halfedge.converter;

import java.util.Arrays;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;

public class UVMeshGenerator {

	@SuppressWarnings("unused")
	private VVertex v00,v10,v11,v01;
	
	private int 
		uSize = 2,
		vSize = 2;

	private VEdge startEdge;

	public UVMeshGenerator(VHDS hds) {
		checkZ2Combinatorics();
		VEdge be = HalfEdgeUtils.boundaryEdges(hds).iterator().next();

		while(HalfEdgeUtilsExtra.getDegree(be.getTargetVertex()) != 2) {
			be = be.getNextEdge();
		}

		v00 = be.getTargetVertex();
		be = be.getNextEdge();
		startEdge = be;
		while(HalfEdgeUtilsExtra.getDegree(be.getTargetVertex()) != 2) {
			++vSize;
			be = be.getNextEdge();
		}
		v01 = be.getTargetVertex();
		be = be.getNextEdge();
		while(HalfEdgeUtilsExtra.getDegree(be.getTargetVertex()) != 2) {
			++uSize;
			be = be.getNextEdge();
		}
		v11 = be.getTargetVertex();
		be = be.getNextEdge();
		while(HalfEdgeUtilsExtra.getDegree(be.getTargetVertex()) != 2) {
			be = be.getNextEdge();
		}
		v10 = be.getTargetVertex();
		
	}
	
	private boolean checkZ2Combinatorics() {
		//degree <= 4; 2-colourable ???
		return true;
	}

	public double[][][][] getArray() {
		double[][][][] xyzcoord = new double[uSize][vSize][1][4];
		VVertex vend = v01;
		VEdge se = startEdge;
		int	u = 0, v = 0;
		do {
			VEdge e = se;
			v = 0;
			xyzcoord[u][v][0] = Arrays.copyOf(e.getStartVertex().P,4);
			xyzcoord[u][v++][0][3] = 1;
			while(e.getTargetVertex() != vend) {
				xyzcoord[u][v][0] = Arrays.copyOf(e.getTargetVertex().P,4);
				xyzcoord[u][v++][0][3] = 1;
				e = getOpposingEdge(e);
			} 
			xyzcoord[u][v][0] = Arrays.copyOf(e.getTargetVertex().P,4);
			xyzcoord[u++][v][0][3] = 1;
			vend = e.getOppositeEdge().getPreviousEdge().getStartVertex();
			se = se.getOppositeEdge().getNextEdge().getNextEdge();
		} while(u < uSize);
		return xyzcoord;
	}

	private VEdge getOpposingEdge(VEdge e) {
		if(e.getLeftFace() == null) {
			return e.getNextEdge();
		}
		return e.getNextEdge().getOppositeEdge().getNextEdge();
	}
}
