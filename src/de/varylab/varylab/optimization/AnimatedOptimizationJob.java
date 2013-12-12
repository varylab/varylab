package de.varylab.varylab.optimization;

import static de.jtem.jpetsc.InsertMode.INSERT_VALUES;
import static de.jtem.jpetsc.PETSc.PETSC_DEFAULT;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.jpetsc.Vec;
import de.jtem.jtao.Tao;
import de.jtem.jtao.Tao.Method;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.halfedge.adapter.CoordinatePetscAdapter;
import de.varylab.varylab.optimization.constraint.Constraint;
import de.varylab.varylab.optimization.util.PetscTaoUtility;

public class AnimatedOptimizationJob extends AbstractOptimizationJob {

	private HalfedgeInterface
		hif = null;
	private HalfedgeLayer
		sourceLayer = null;
	private VHDS
		hds = null;
	private VaryLabFunctional
		functional = null;
	private List<Constraint>
		constraints = new LinkedList<Constraint>();
	private Method
		method = Method.CG;
	private int 
		gcCounter = 0;

	public AnimatedOptimizationJob(VaryLabFunctional fun, HalfedgeInterface hif, HalfedgeLayer sourceLayer) {
		super("Animated Optimization");
		this.hif = hif;
		this.sourceLayer = sourceLayer;
		this.functional = fun;
		
	}
	
	@Override
	public void executeJob() throws Exception {
		hif.update(); // create undo step
		this.hds = sourceLayer.get(new VHDS());
		fireOptimizationStarted(1);
		PetscTaoUtility.initializePetscTao();
		Tao solver = new Tao(method);
		Vec xVec = new Vec(hds.numVertices() * 3);
		xVec.setBlockSize(3);
		CoordinatePetscAdapter xAdapter = new CoordinatePetscAdapter(xVec, 1);
		
		functional.initializeTaoVectors(hds);
		VaryLabTaoApplication app = new VaryLabTaoApplication(hds, functional);
		app.setConstraints(constraints);
		app.setInitialSolutionVec(xVec);
		
		solver.setMaximumIterates(1);
		solver.setTolerances(PETSC_DEFAULT, PETSC_DEFAULT, PETSC_DEFAULT, PETSC_DEFAULT);
		solver.setGradientTolerances(PETSC_DEFAULT, PETSC_DEFAULT, PETSC_DEFAULT);
		solver.setApplication(app);
		int iterationCounter = 0;
		while (!isCancelRequested()) {
			// update and solve
			readPositionsToX(xVec, hds);
			solver.solve();
			writePositionsToHDS(xAdapter);
			// clean memory from time to time
			if (gcCounter++ > 200) {
				System.gc();
				gcCounter = 0;
			}
			fireOptimizationProgress(iterationCounter++, app.getSolutionVec());
			Thread.sleep(30);
		}
		fireOptimizationFinished(solver.getSolutionStatus(), app.getSolutionVec());
	}
	

	public void readPositionsToX(final Vec xVec, final VHDS hds) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				xVec.assemblyBegin();
				for (VVertex v : hds.getVertices()) {
					int[] pos = {v.getIndex()};
					xVec.setValuesBlocked(1, pos, v.getP(), INSERT_VALUES);
				}
				xVec.assemblyEnd();
			}
		});
	}
	

	public void writePositionsToHDS(final Adapter<double[]> coords) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				hif.updateGeometryNoUndo(coords);
			}
		});
	}
	
	public void setMethod(Method method) {
		this.method = method;
	}
	
	public void setConstraints(List<Constraint> constraints) {
		this.constraints = constraints;
	}
	
	@Override
	public HalfedgeLayer getSourceLayer() {
		return sourceLayer;
	}
	@Override
	public VHDS getHDS() {
		return hds;
	}
	
}
