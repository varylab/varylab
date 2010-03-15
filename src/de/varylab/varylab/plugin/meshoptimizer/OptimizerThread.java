package de.varylab.varylab.plugin.meshoptimizer;

import no.uib.cipr.matrix.DenseVector;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.CombinedFunctional;
import de.varylab.varylab.math.CombinedOptimizableNM;
import de.varylab.varylab.math.ConjugateGradient;
import de.varylab.varylab.math.Constraint;

public class OptimizerThread extends Thread {

	CombinedOptimizableNM opt = null;
	
	HalfedgeInterface
		hif = null;
	
	VHDS 
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
		int dim = hds.numVertices() * 3;
		DenseVector u = new DenseVector(dim);
		for (VVertex v : hds.getVertices()) {
			u.set(v.getIndex() * 3 + 0, v.position[0]);
			u.set(v.getIndex() * 3 + 1, v.position[1]);
			u.set(v.getIndex() * 3 + 2, v.position[2]);
		}
		uArr = u.getData();
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
				double fv = ConjugateGradient.search(uArr, acc, opt);
				if(watchFunctionValue){
					System.out.println(fv);
				}
				for (VVertex v : hds.getVertices()) {
					int i = v.getIndex() * 3;
					v.position[0] = uArr[i + 0];
					v.position[1] = uArr[i + 1];
					v.position[2] = uArr[i + 2];
				}
				HalfedgeSelection hes = hif.getSelection();
				hif.set(hds);
				hif.setSelection(hes);
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
