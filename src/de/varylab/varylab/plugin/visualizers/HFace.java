package de.varylab.varylab.plugin.visualizers;

import java.util.Arrays;
import java.util.HashMap;

import de.jtem.halfedge.util.HalfEdgeUtils;
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
	
	public HFace(VFace f) {
		vFace = f;
		calculatePlueckerCoordinates();
	}
	
	//get the pluecker coordinates of the specified edge
	public double[] getPluecker(VEdge e) {
		return edgePlueckerMap.get(e);
	}
	
	//get the q of the specified edge
	public double[] getQ(VEdge e) {
		return edgeQMap.get(e);
	}
	
	//set q on all four edges of the quad
	public void setQ(VEdge e, double[] q) {
		// TODO: set q's on all four halfedges s.t. the patch lies in the interior.
		// FIXME: now all q's are just set to be equal!!!
		for(VEdge be: HalfEdgeUtils.boundaryEdges(vFace)) {
			edgeQMap.put(be,q);
		}
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

	public void setParameterLine(VEdge e, double d) {
		// FIXME: calculate the pluecker coordinates of the intermediate line
		// corresponding to the parameter d?!
		double[] pc = new double[]{d,0,0,0,0,0};
		
		edgeParameterLineMap.put(e.getNextEdge(),pc);
		edgeParameterLineMap.put(e.getPreviousEdge(),pc);
	}
	
	public double[][] getParameterLines(VEdge e) {
		VEdge oppositeEdgeInFace = SelectionUtility.getOppositeEdgeInFace(e);
		double[][] pl = new double[3][6];
		pl[0] = Arrays.copyOf(getQ(e), 6);
		pl[1] = Arrays.copyOf(edgeParameterLineMap.get(e), 6);
		pl[2] = Arrays.copyOf(edgeParameterLineMap.get(oppositeEdgeInFace), 6);
		return pl;
	}

}
