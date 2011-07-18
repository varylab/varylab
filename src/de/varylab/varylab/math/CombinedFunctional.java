package de.varylab.varylab.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

public class CombinedFunctional implements Functional<VVertex, VEdge, VFace> {

	private List<Functional<VVertex, VEdge, VFace>>
		funList = null;
	private Map<Functional<?, ?, ?>, Double> 
		coeffs = null;
	private SimpleEnergy 
		E2 = null;
	private MTJGradient 
		G2 = null;
	private int
		dim = 0;
	
	public CombinedFunctional(
		List<Functional<VVertex, VEdge, VFace>> funList, 
		Map<Functional<?, ?, ?>, Double> coeffs,
		int dim
	) {
		this.dim = dim;
		this.funList = funList;
		this.coeffs = coeffs;
		E2 = new SimpleEnergy();
		G2 = new MTJGradient(new DenseVector(dim));
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
		if (E != null) E.setZero();
		if (G != null) G.setZero();
		if (H != null) H.setZero();
		for (Functional<VVertex, VEdge, VFace> fun : funList) {
			Double coeff = coeffs.get(fun);
			if (coeff == null || coeff == 0.0) continue;
			E2.setZero();
			G2.setZero();
			SimpleEnergy ener = E == null ? null : E2;
			MTJGradient grad = G == null ? null : G2;
			MTJHessian hess = H == null ? null : new MTJHessian(new DenseMatrix(dim, dim));;
			fun.evaluate(hds, x, ener, grad, hess);
			if (E != null && ener != null) {
				E.add(coeff * ener.E);
			}
			if (G != null && grad != null) {
				for (int i = 0; i < dim; i++) {
					double val = coeff * grad.G.get(i);
					if (val != 0.0) {
						G.add(i, val);
					}
				}
			}
			if (H != null && hess != null) {
				for (int i = 0; i < dim; i++) {
					for (int j = 0; j < dim; j++) {
						double val = coeff * hess.H.get(i, j);
						if (val != 0.0) {
							H.add(i, j, val);
						}
					}
				}
			}
		}
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>> int getDimension(HDS hds) {
		return hds.numVertices() * 3;
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>> int[][] getNonZeroPattern(HDS hds) {
		if (funList.size() == 1) {
			return funList.get(0).getNonZeroPattern(hds);
		}
		ArrayList<Set<Integer>> pattern = new ArrayList<Set<Integer>>();
		for (int i = 0; i < dim; i++) {
			pattern.add(new HashSet<Integer>());
		}
		for (Functional<VVertex, VEdge, VFace> fun : funList) {
			int[][] p = fun.getNonZeroPattern(hds);
			for (int i = 0; i < dim; i++) {
				Set<Integer> nz = pattern.get(i);
				int[] row = p[i];
				for (int j = 0; j < row.length; j++) {
					nz.add(row[j]);
				}
			}
		}
		int[][] patArr = new int[dim][];
		for (int i = 0; i < dim; i++) {
			Set<Integer> nzSet = pattern.get(i);
			int[] nz = new int[nzSet.size()];
			int count = 0;
			for (Integer row : nzSet) {
				nz[count++] = row;
			}
			Arrays.sort(nz);
			patArr[i] = nz;
		}
		return patArr;
	}
	
	
	@Override
	public boolean hasHessian() {
		boolean hasHessian = true;
		for (Functional<VVertex, VEdge, VFace> fun : funList) {
			hasHessian &= fun.hasHessian();
		}
		return hasHessian;
	}

}
