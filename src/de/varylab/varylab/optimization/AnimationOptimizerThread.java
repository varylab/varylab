package de.varylab.varylab.optimization;

import static de.jtem.jpetsc.InsertMode.INSERT_VALUES;
import static de.jtem.jpetsc.PETSc.PETSC_DEFAULT;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import de.jreality.plugin.job.AbstractCancelableJob;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jpetsc.Vec;
import de.jtem.jtao.Tao;
import de.jtem.jtao.Tao.Method;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.halfedge.adapter.CoordinatePetscAdapter;
import de.varylab.varylab.optimization.constraint.Constraint;

public class AnimationOptimizerThread extends AbstractCancelableJob {

	private HalfedgeInterface
		hif = null;
	private VaryLabFunctional
		functional = null;
	private List<Constraint>
		constraints = new LinkedList<Constraint>();
	private int 
		gcCounter = 0;

	public AnimationOptimizerThread(VaryLabFunctional fun, HalfedgeInterface hif) {
		this.hif = hif;
		this.functional = fun;
	}
	
	@Override
	public String getJobName() {
		return "Animated Optimization";
	}
	
	@Override
	public void execute() throws Exception {
		Tao solver = new Tao(Method.CG);
		VHDS hds = hif.get(new VHDS());
		Vec xVec = new Vec(hds.numVertices() * 3);
		xVec.setBlockSize(3);
		CoordinatePetscAdapter xAdapter = new CoordinatePetscAdapter(xVec, 1);
		
		VaryLabTaoApplication opt = new VaryLabTaoApplication(hds, functional);
		opt.setConstraints(constraints);
		opt.setInitialSolutionVec(xVec);
		
		solver.setMaximumIterates(1);
		solver.setTolerances(PETSC_DEFAULT, PETSC_DEFAULT, PETSC_DEFAULT, PETSC_DEFAULT);
		solver.setGradientTolerances(PETSC_DEFAULT, PETSC_DEFAULT, PETSC_DEFAULT);
		solver.setApplication(opt);
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
			Thread.sleep(30);
		}
	}
	

	public void readPositionsToX(final Vec xVec, final VHDS hds) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				xVec.assemblyBegin();
				for (VVertex v : hds.getVertices()) {
					int[] pos = {v.getIndex()};
					xVec.setValuesBlocked(1, pos, v.P, INSERT_VALUES);
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
	
}
