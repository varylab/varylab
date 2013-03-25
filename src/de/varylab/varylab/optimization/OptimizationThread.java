package de.varylab.varylab.optimization;

import static de.jtem.jpetsc.InsertMode.INSERT_VALUES;
import static de.jtem.jpetsc.PETSc.PETSC_DEFAULT;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jreality.plugin.job.AbstractCancelableJob;
import de.jtem.jpetsc.Mat;
import de.jtem.jpetsc.Vec;
import de.jtem.jtao.Tao;
import de.jtem.jtao.Tao.GetSolutionStatusResult;
import de.jtem.jtao.Tao.Method;
import de.jtem.jtao.TaoMonitor;
import de.jtem.jtao.TaoVec;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.optimization.constraint.Constraint;
import de.varylab.varylab.optimization.tao.TaoUtility;

public class OptimizationThread extends AbstractCancelableJob implements TaoMonitor {

	private VHDS
		hds = null;
	private VaryLabFunctional
		functional = null;
	private List<Constraint>
		costraints = new LinkedList<Constraint>();
	private double
		tolerance = 1E-8,
		gradTolerance = 1E-8;
	private int 
		maxIterations = 50,
		currentIteration = 0;
	private boolean
		smoothingEnabled = false;
	private Method
		method = Method.CG;
	private List<OptimizationListener>
		listeners = Collections.synchronizedList(new LinkedList<OptimizationListener>());
	
	public OptimizationThread(VHDS hds, VaryLabFunctional fun) {
		this.hds = hds;
		this.functional = fun;
	}
	
	@Override
	public String getJobName() {
		return "Optimization " + method.getName();
	}
	
	@Override
	public void execute() throws Exception {
		fireOptimizationStarted();
		fireJobStarted(this);
		String[] taoCommand = new String[] {
			"-tao_nm_lamda", "0.01", 
			"-tao_nm_mu", "1.0"
		};
		Tao.Initialize("Tao Varylab", taoCommand, false);
		Tao solver = new Tao(method);
		VaryLabTaoApplication app = createApplication();
		solver.setApplication(app);
		solver.setMonitor(this);
		solver.setMaximumIterates(maxIterations);
		solver.setTolerances(tolerance, tolerance, tolerance, tolerance);
		solver.setGradientTolerances(gradTolerance, gradTolerance, gradTolerance);
		solver.solve();
		fireOptimizationFinished(solver.getSolutionStatus(), solver.getSolution());
		fireJobFinished(this);
	}
	
	
	private VaryLabTaoApplication createApplication() {
		VaryLabTaoApplication app = new VaryLabTaoApplication(hds, functional);
		int dim = hds.numVertices() * 3;
		Vec x = new Vec(dim);
		for (VVertex v : hds.getVertices()) {
			x.setValue(v.getIndex() * 3 + 0, v.P[0] / v.P[3], INSERT_VALUES);
			x.setValue(v.getIndex() * 3 + 1, v.P[1] / v.P[3], INSERT_VALUES);
			x.setValue(v.getIndex() * 3 + 2, v.P[2] / v.P[3], INSERT_VALUES);
		}
		app.setInitialSolutionVec(x);
		if (functional.hasHessian()) {
			int[] nnz = TaoUtility.getPETScNonZeros(hds, functional);
			Mat H = Mat.createSeqAIJ(dim, dim, PETSC_DEFAULT, nnz);
			H.assemble();
			app.setHessianMat(H, H);
		} else {
			switch (method) {
			case NLS:
			case NTR:
			case GPCG:
			case BQPIP:
			case KT:
				throw new RuntimeException("Cannot use method " + method.getName() + " without Hessian matrix");
			default:
				break;
			}
		}
		app.setSmoothingEnabled(smoothingEnabled);
		app.setConstraints(costraints);
		return app;
	}
	
	
	@Override
	public int monitor(Tao solver) {
		currentIteration++;
		fireOptimizationProgress(currentIteration, solver.getSolution());
		fireJobProgress(this, currentIteration / (double)maxIterations);
		if (isCancelRequested()) solver.setMaximumIterates(0);
		return 0;
	}
	
	public void addOptimizationListener(OptimizationListener l) {
		listeners.add(l);
	}
	public void removeOptimizationListener(OptimizationListener l) {
		listeners.remove(l);
	}

	protected void fireOptimizationStarted() {
		synchronized (listeners) {
			for (final OptimizationListener l : listeners) {
				Runnable delegate = new Runnable() {
					@Override
					public void run() {
						l.optimizationStarted(maxIterations);						
					}
				};
				EventQueue.invokeLater(delegate);
			}
		}
	}
	protected void fireOptimizationProgress(final int iteration, TaoVec solution) {
		final double[] solutionArr = solution.getArrayReadOnly();
		synchronized (listeners) {
			for (final OptimizationListener l : listeners) {
				Runnable delegate = new Runnable() {
					@Override
					public void run() {
						l.optimizationProgress(solutionArr, iteration);
					}
				};
				EventQueue.invokeLater(delegate);
			}
		}
	}
	protected void fireOptimizationFinished(final GetSolutionStatusResult status, TaoVec solution) {
		final double[] solutionArr = solution.getArrayReadOnly();
		synchronized (listeners) {
			for (final OptimizationListener l : listeners) {
				Runnable delegate = new Runnable() {
					@Override
					public void run() {
						l.optimizationFinished(status, solutionArr);
					}
				};
				EventQueue.invokeLater(delegate);
			}
		}
	}
	
	public void setCostraints(List<Constraint> costraints) {
		this.costraints = costraints;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public void setMaximumIterates(int num) {
		this.maxIterations = num;
	}
	public void setTolerances(double tol) {
		this.tolerance = tol;
	}
	public void setGradientTolerances(double gatol) {
		this.gradTolerance = gatol;
	}
	public void setSmoothingEnabled(boolean smoothingEnabled) {
		this.smoothingEnabled = smoothingEnabled;
	}
	
}
