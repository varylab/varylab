package de.varylab.varylab.math;

import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.jtem.jpetsc.Mat;
import de.jtem.jpetsc.Vec;
import de.jtem.jtao.TaoAppAddCombinedObjectiveAndGrad;
import de.jtem.jtao.TaoAppAddHess;
import de.jtem.jtao.TaoApplication;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.adapter.VertexDomainValueAdapter;
import de.varylab.varylab.math.constraint.Constraint;
import de.varylab.varylab.math.tao.TaoDomainValue;
import de.varylab.varylab.math.tao.TaoEnergy;
import de.varylab.varylab.math.tao.TaoGradient;
import de.varylab.varylab.math.tao.TaoHessian;
import de.varylab.varylab.plugin.smoothing.LaplacianSmoothing;

public class CombinedOptimizableTao extends TaoApplication implements
		TaoAppAddCombinedObjectiveAndGrad, TaoAppAddHess {

	private VHDS
		hds = null;
	private CombinedFunctional
		fun = null;
	private List<Constraint>
		constraints = new LinkedList<Constraint>();
	private boolean
		smoothing = false;

	public CombinedOptimizableTao(VHDS hds, CombinedFunctional fun) {
		this.hds = hds;
		this.fun = fun;
	}
	
	
	public void addConstraint(Constraint c) {
		constraints.add(c);
	}
	
	public void removeConstraint(Constraint c) {
		constraints.remove(c);
	}

	public void enableSmoothing(boolean smoothing) {
		this.smoothing = smoothing;
	}


	private void applyConstraints(DomainValue x, Gradient G, Hessian H) {
		for (Constraint c : constraints) {
			if (G != null) {
				c.editGradient(hds, getDomainDimension(), x, G);
			}
			if (H != null) {
				c.editHessian(hds, getDomainDimension(), x, H);
			}
		}
	}
	

	@Override
	public double evaluateObjectiveAndGradient(Vec x, Vec g) {
		TaoDomainValue u = new TaoDomainValue(x);
		TaoGradient G = new TaoGradient(g);
		TaoEnergy E = new TaoEnergy();
		fun.evaluate(hds, u, E, G, null);
		applyConstraints(u, G, null);
		if(smoothing) applySmoothing(u);
		g.assemble();
		return E.get();
	}

	private void applySmoothing(DomainValue x) {
		LaplacianSmoothing.smoothCombinatorially(hds, new AdapterSet(new VertexDomainValueAdapter(x)), true);
	}


	@Override
	public PreconditionerType evaluateHessian(Vec x, Mat H, Mat Hpre) {
		TaoDomainValue u = new TaoDomainValue(x);
		TaoHessian taoHess = new TaoHessian(H);
		fun.evaluate(hds, u, null, null, taoHess);
		applyConstraints(u, null, taoHess);
		H.assemble();
		return PreconditionerType.SAME_NONZERO_PATTERN;
	}

	
	public int getDomainDimension() {
		return hds.numVertices() * 3;
	}
	
	
}
