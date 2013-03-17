package de.varylab.varylab.plugin.meshoptimizer;

import javax.swing.SwingUtilities;

import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.hds.adapter.CoordinateArrayAdapter;
import de.varylab.varylab.math.CombinedFunctional;
import de.varylab.varylab.math.CombinedOptimizableNM;
import de.varylab.varylab.math.constraint.Constraint;
import de.varylab.varylab.math.util.ConjugateGradient;

public class OptimizerThread extends Thread {

	private CombinedOptimizableNM 
		opt = null;
	private HalfedgeInterface
		hif = null;
	private VHDS 
		hds = null;
	
	double acc = 1E-6;
	double[] uArr = null;
	boolean 
		pause = true,
		running = true;
	int gcCounter = 0;

	private boolean watchFunctionValue = true;

	public OptimizerThread() {
		super("OptimizerThread");
	}
	
	public void initOptimizer(
		HalfedgeInterface hif,
		CombinedFunctional fun,
		Constraint constraint,
		double accuracy,
		int it) 
	{
		this.hif = hif;
		this.hds = hif.get(new VHDS());
		opt = new CombinedOptimizableNM(hds, fun);
		opt.addConstraint(constraint);
		uArr = new double[hds.numVertices() * 3];
		ConjugateGradient.setITMAX(it);
		ConjugateGradient.setUseDBrent(true);
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
	
	public void watchFunctionValue(boolean watch) {
		watchFunctionValue = watch;
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
				// get active coordinates
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						for (VVertex v : hds.getVertices()) {
							uArr[v.getIndex() * 3 + 0] = v.P[0];
							uArr[v.getIndex() * 3 + 1] = v.P[1];
							uArr[v.getIndex() * 3 + 2] = v.P[2];
						}
					}
				});
				// minimize
				double fv = ConjugateGradient.search(uArr, acc, opt);
				if(watchFunctionValue){
					System.out.println(fv);
				}
				// update coordinates
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						CoordinateArrayAdapter posAdapter = new CoordinateArrayAdapter(uArr, 1);
						hif.updateGeometryNoUndo(posAdapter);
					}
				});
				// clean memory from time to time
				if (gcCounter++ > 200) {
					System.gc();
					gcCounter = 0;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
}
