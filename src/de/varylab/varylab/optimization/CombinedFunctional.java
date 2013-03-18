package de.varylab.varylab.optimization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jpetsc.Mat;
import de.jtem.jpetsc.PETSc;
import de.jtem.jpetsc.Vec;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.optimization.tao.TaoDomainValue;
import de.varylab.varylab.optimization.tao.TaoEnergy;
import de.varylab.varylab.optimization.tao.TaoGradient;
import de.varylab.varylab.optimization.tao.TaoHessian;
import de.varylab.varylab.optimization.tao.TaoUtility;

public class CombinedFunctional {

	private List<Functional<VVertex, VEdge, VFace>>
		funList = null;
	private Map<Functional<?, ?, ?>, Double> 
		coeffs = null;
	private TaoEnergy 
		E2 = null;
	private TaoGradient 
		G2 = null;
	private TaoHessian
		H2 = null;
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
		E2 = new TaoEnergy();
		Vec gVec = new Vec(dim);
		G2 = new TaoGradient(gVec);
	}
	
	
	public void evaluate(VHDS hds, TaoDomainValue x, TaoEnergy E, TaoGradient G, TaoHessian H) {
		if (E != null) E.setZero();
		if (G != null) G.setZero();
		if (H != null) {
			H.setZero();
			if (H2 == null) {
				int[] taonzp = TaoUtility.getPETScNonZeros(hds, this);
				Mat hMat = Mat.createSeqAIJ(dim, dim, PETSc.PETSC_DEFAULT, taonzp);
				H2 = new TaoHessian(hMat);
			}
		}
		for (Functional<VVertex, VEdge, VFace> fun : funList) {
			Double coeff = coeffs.get(fun);
			if (coeff == null || coeff == 0.0) continue;
			E2.setZero();
			G2.setZero();
			TaoEnergy ener = E == null ? null : E2;
			TaoGradient grad = G == null ? null : G2;
			TaoHessian hess = H == null ? null : H2;
			fun.evaluate(hds, x, ener, grad, hess);
			if (ener != null) {
				E.add(coeff * ener.get());
			}
			if (grad != null) {
				G.add(coeff, grad);
			}
			if (H != null && hess != null) {
				H.add(coeff, hess);
			}
		}
	}

	public <HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>> int getDimension(HDS hds) {
		return hds.numVertices() * 3;
	}

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
	
	
	public boolean hasHessian() {
		boolean hasHessian = true;
		for (Functional<VVertex, VEdge, VFace> fun : funList) {
			hasHessian &= fun.hasHessian();
		}
		return hasHessian;
	}

}
