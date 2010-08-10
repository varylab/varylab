package de.varylab.varylab.plugin.visualizers;

import static de.jtem.projgeom.P5.LINE_SPACE;

import hyperbolicnets.core.DataModel;

import java.util.Arrays;
import java.util.HashMap;

import de.jreality.math.Rn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.projgeom.P5;
import de.jtem.projgeom.PlueckerLineGeometry;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.utilities.SelectionUtility;


public class HFace {

	private VFace 
		vFace = null;

	private HashMap<VEdge, double[]>
		edgePlueckerMap = new HashMap<VEdge, double[]>(),
		edgeQMap = new HashMap<VEdge, double[]>(),
		edgeParameterLineMap = new HashMap<VEdge, double[]>();
	
	private double[][] 
	    diagonals = new double[2][6];
	
	public HFace(VFace f) {
		vFace = f;
		calculatePlueckerCoordinates();
		calculateDiagonals();
	}
	
	private void calculateDiagonals() {
		VEdge e = vFace.getBoundaryEdge();
		VVertex 
			v1 = e.getStartVertex(),
			v2 = e.getTargetVertex(),
			v3 = e.getNextEdge().getTargetVertex(),
			v4 = e.getPreviousEdge().getStartVertex();
		double[]
		       vc1 = new double[4],
		       vc2 = new double[4],
		       vc3 = new double[4],
		       vc4 = new double[4];
		System.arraycopy(v1.position, 0, vc1, 0, 3);
		System.arraycopy(v2.position, 0, vc2, 0, 3);
		System.arraycopy(v3.position, 0, vc3, 0, 3);
		System.arraycopy(v4.position, 0, vc4, 0, 3);
		vc1[3]=1;
		vc2[3]=1;
		vc3[3]=1;
		vc4[3]=1;
		PlueckerLineGeometry.lineFromPoints(diagonals[0], vc1, vc3);
		PlueckerLineGeometry.lineFromPoints(diagonals[1], vc2, vc4);
	}

	//get the pluecker coordinates of the specified edge
	public double[] getPluecker(VEdge e) {
		return edgePlueckerMap.get(e);
	}
	
	//get the q of the specified edge
	public double[] getQ(VEdge e) {
		return edgeQMap.get(e);
	}
	
	//get the 1/2 parameter line beloning to the family of the specified edge
	public double[] getParameterLine(VEdge e) {
		return edgeParameterLineMap.get(e);
	}
	
	//set q on all four edges of the quad
	public void setQ(VEdge e1, double[] q) {
		// TODO: check that q's on all four halfedges are set s.t. the patch lies in the interior.
		// (Maybe at another place as this should be checked only for the inital quad...)
		double[] qStar = P5.getIntersectionOfLineWithPolar(null, diagonals[0], diagonals[0], q, LINE_SPACE);
		VEdge
			e2 = e1.getNextEdge(),
			// TODO: Use SelectionUtility.getOppositeEdgeInFace(VEdge) ?
			e3 = e2.getNextEdge(),
			e4 = e3.getNextEdge();
		
		edgeQMap.put(e1,q);
		edgeQMap.put(e3,q);
		edgeQMap.put(e2,qStar);
		edgeQMap.put(e4,qStar);
	}

	private void calculatePlueckerCoordinates() {
		double[] plueckerCoords = new double[6];
		for(VEdge e : HalfEdgeUtils.boundaryEdges(vFace)) {
			VVertex 
				v1 = e.getStartVertex(),
				v2 = e.getTargetVertex();
			double[]
			       vc1 = new double[4],
			       vc2 = new double[4];
			System.arraycopy(v1.position, 0, vc1, 0, 3);
			System.arraycopy(v2.position, 0, vc2, 0, 3);
			vc1[3]=1;
			vc2[3]=1;
			PlueckerLineGeometry.lineFromPoints(plueckerCoords, vc1,vc2);
			edgePlueckerMap.put(e,plueckerCoords);
		}
	}

	/**
	 * @param e - corresponds to the A-net edge l 
	 * @param y - homogeneous coordinates of a point on l
	 * 
	 * Calculates Pluecker coordinates of line through y in the complementary planar family (i.e. not containing l)
	 * and links them with the next and previous halfedges of e.
	 */
	public void setParameterLine(VEdge e, double[] y) {
		VEdge oppositeEdgeInFace = SelectionUtility.getOppositeEdgeInFace(e);
		double[] pc1 = edgePlueckerMap.get(e);
		double[] pc2 = edgePlueckerMap.get(oppositeEdgeInFace);
		double[] pc = PlueckerLineGeometry.normalize(null, DataModel.getLineOfPlanarFamilyThroughPoint(pc1, pc2, edgeQMap.get(e), y));
		
		edgeParameterLineMap.put(e.getNextEdge(),pc);
		edgeParameterLineMap.put(e.getPreviousEdge(),pc);
	}
	
	// returns pluecker coords of 3 lines spanning the planar family containing the A-net edge corresponding to e
	public double[][] getParameterLines(VEdge e) {
		VEdge oppositeEdgeInFace = SelectionUtility.getOppositeEdgeInFace(e);
		double[][] pl = new double[3][6];
		pl[0] = Arrays.copyOf(edgePlueckerMap.get(e), 6);
		pl[1] = Arrays.copyOf(edgeParameterLineMap.get(e), 6);
		pl[2] = Arrays.copyOf(edgePlueckerMap.get(oppositeEdgeInFace), 6);
		return pl;
	}

	public VFace getvFace() {
		return vFace;
	}

	public double[][] getDiagonals() {
		return diagonals;
	}
	
}
