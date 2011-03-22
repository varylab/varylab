package de.varylab.varylab.plugin.meshoptimizer;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.plugin.OptimizerPlugin;

public class InflateOptimizer extends OptimizerPlugin {

	
	private class InflateFunctional implements Functional<VVertex, VEdge, VFace> {

		private double[]
		    p0 = new double[3],
		    p1 = new double[3],
		    p2 = new double[3];
		
		
		public InflateFunctional() {
		}

		@Override
		public <
			HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>
		> void evaluate(
			HDS hds, 
			DomainValue x, 
			Energy E, 
			Gradient G, 
			Hessian H
		) {
			if (E != null) {
				for (VFace f : hds.getFaces()) {
					List<VVertex> bList = HalfEdgeUtils.boundaryVertices(f);
					VVertex v0 = bList.get(0);
					VVertex v1 = bList.get(1);
					VVertex v2 = bList.get(2);
					FunctionalUtils.getPosition(v0, x, p0);
					FunctionalUtils.getPosition(v1, x, p1);
					FunctionalUtils.getPosition(v2, x, p2);
					double det = 0.5 * Rn.determinant(new double[][] {p0, p1, p2});
					E.add(det);
				}
			}
		}

		@Override
		public boolean hasHessian() {
			return false;
		}

		@Override
		public <
			HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>
		> int getDimension(HDS hds) {
			return hds.numVertices() * 3;
		}

		@Override
		public <
			HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace
		>> int[][] getNonZeroPattern(HDS hds) {
			return null;
		}
		
	}
	
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return new InflateFunctional();
	}

	@Override
	public String getName() {
		return "Inflate to CMC?";
	}

}
