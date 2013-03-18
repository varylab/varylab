package de.varylab.varylab.optimization;

import static de.jtem.jpetsc.InsertMode.INSERT_VALUES;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jpetsc.Vec;
import de.jtem.jtao.Tao;
import de.jtem.jtao.Tao.Method;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.halfedge.adapter.CoordinatePetscAdapter;
import de.varylab.varylab.optimization.constraint.Constraint;

public class AnimationOptimizerThread extends Thread {

	private Tao
		solver = new Tao(Method.CG);
	private CombinedOptimizableTao
		opt = null;
	private HalfedgeInterface
		hif = null;
	private VHDS 
		hds = null;

	private Vec
		xVec = null;
	private CoordinatePetscAdapter 
		xAdapter = null;
	private boolean 
		pause = true,
		running = true;
	private int 
		gcCounter = 0;

	public AnimationOptimizerThread() {
		super("OptimizerThread");
	}
	
	public void initOptimizer(
		HalfedgeInterface hif,
		CombinedFunctional fun,
		Constraint constraint,
		double accuracy,
		int roundIterations 
	) {
		this.hif = hif;
		this.hds = hif.get(new VHDS());
		hif.update();
		xVec = new Vec(hds.numVertices() * 3);
		xVec.setBlockSize(3);
		xAdapter = new CoordinatePetscAdapter(xVec, 1);
		
		opt = new CombinedOptimizableTao(hds, fun);
		opt.addConstraint(constraint);
		opt.setInitialSolutionVec(xVec);
		
		solver.setMaximumIterates(roundIterations);
		solver.setTolerances(accuracy, accuracy, accuracy, accuracy);
		solver.setApplication(opt);
	}
	
	public boolean isPaused() {
		return pause;
	}

	public synchronized void setPause(boolean p) {
		this.pause = p;
		notify();
	}
	
	public void terminate() {
		running = false;
	}
	

	public void readPositionsToX() throws InterruptedException, InvocationTargetException {
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
	

	public void writePositionsToHDS() throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				hif.updateGeometryNoUndo(xAdapter);
			}
		});
	}
	
	
	@Override
	public void run() {
		
		while(running) {
			try {
				if(pause) {
					synchronized (this) {
						wait();
					}
				}
				// update and solve
				readPositionsToX();
				solver.solve();
				writePositionsToHDS();
				// clean memory from time to time
				if (gcCounter++ > 200) {
					System.gc();
					gcCounter = 0;
				}
				Thread.sleep(30);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
