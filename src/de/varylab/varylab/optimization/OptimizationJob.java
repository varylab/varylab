package de.varylab.varylab.optimization;

import static de.jtem.jpetsc.InsertMode.INSERT_VALUES;
import static de.jtem.jpetsc.PETSc.PETSC_DEFAULT;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.jpetsc.Mat;
import de.jtem.jpetsc.Vec;
import de.jtem.jtao.Tao;
import de.jtem.jtao.Tao.Method;
import de.jtem.jtao.TaoMonitor;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.optimization.constraint.Constraint;
import de.varylab.varylab.optimization.tao.TaoUtility;
import de.varylab.varylab.optimization.util.PetscTaoUtility;

public class OptimizationJob extends AbstractOptimizationJob implements TaoMonitor {

	private Logger
		log = Logger.getLogger(OptimizationJob.class.getName());
	private VHDS
		hds = null;
	private HalfedgeLayer
		sourceLayer = null;
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
	private VaryLabTaoApplication
		activeApplication = null;
	
	public OptimizationJob(VHDS hds, HalfedgeLayer sourceLayer, VaryLabFunctional fun) {
		super("Optimization");
		this.hds = hds;
		this.sourceLayer = sourceLayer;
		this.functional = fun;
	}
	
	@Override
	public void executeJob() throws Exception {
		fireOptimizationStarted(maxIterations);
		PetscTaoUtility.initializePetscTao();
		functional.initializeTaoVectors(hds);
		Tao solver = new Tao(method);
		activeApplication = createApplication();
		solver.setApplication(activeApplication);
		solver.setMonitor(this);
		solver.setMaximumIterates(maxIterations);
		solver.setTolerances(tolerance, tolerance, tolerance, tolerance);
		solver.setGradientTolerances(gradTolerance, gradTolerance, gradTolerance);
		solver.solve();
		fireOptimizationFinished(solver.getSolutionStatus(), activeApplication.getSolutionVec());
	}
	
	
	private VaryLabTaoApplication createApplication() {
		VaryLabTaoApplication app = new VaryLabTaoApplication(hds, functional);
		int dim = hds.numVertices() * 3;
		Vec x = new Vec(dim);
		for (VVertex v : hds.getVertices()) {
			double[] P = v.getP();
			x.setValue(v.getIndex() * 3 + 0, P[0] / P[3], INSERT_VALUES);
			x.setValue(v.getIndex() * 3 + 1, P[1] / P[3], INSERT_VALUES);
			x.setValue(v.getIndex() * 3 + 2, P[2] / P[3], INSERT_VALUES);
		}
		app.setInitialSolutionVec(x);
		switch (method) {
			case NLS:
			case NTR:
			case GPCG:
			case BQPIP:
			case KT:
				Mat H = null;
				if (!functional.hasHessian()) {
					log.warning("using finite differences for Hessian calculation, this can be very slow.");
					H = new Mat(dim, dim);
				} else {
					int[] nnz = TaoUtility.getPETScNonZeros(hds, functional);
					H = Mat.createSeqAIJ(dim, dim, PETSC_DEFAULT, nnz);
				}
				H.assemble();
				app.setHessianMat(H, H);
			default:
		}
		app.setSmoothingEnabled(smoothingEnabled);
		app.setConstraints(costraints);
		return app;
	}
	
	@Override
	public int monitor(Tao solver) {
		currentIteration++;
		fireOptimizationProgress(currentIteration, activeApplication.getSolutionVec());
		fireJobProgress(currentIteration / (double)maxIterations);
		if (isCancelRequested()) solver.setMaximumIterates(0);
		return 0;
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
	@Override
	public VHDS getHDS() {
		return hds;
	}
	@Override
	public HalfedgeLayer getSourceLayer() {
		return sourceLayer;
	}
	
}
