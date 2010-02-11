package de.varylab.varylab.plugin.meshoptimizer;

import no.uib.cipr.matrix.DenseVector;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.CombinedFunctional;
import de.varylab.varylab.math.CombinedOptimizableNM;
import de.varylab.varylab.math.ConjugateGradient;
import de.varylab.varylab.math.Constraint;

public class OptimizerThread extends Thread {

	CombinedOptimizableNM opt = null;
	
	HalfedgeInterfacePlugin<VVertex, VEdge, VFace, VHDS>
		hif = null;
	
	VHDS 
		hds = null;
	
	double acc = 1E-6;
	double[] uArr = null;
	boolean 
		pause = true,
		running = true;
	
	public OptimizerThread() {
		super("OptimizerThread");
	}
	
	public void initOptimizer(
		HalfedgeInterfacePlugin<VVertex, VEdge, VFace, VHDS> hif,
		CombinedFunctional fun,
		Constraint constraint,
		double accuracy,
		int it) 
	{
		this.hif = hif;
		this.hds = hif.getCachedHalfEdgeDataStructure();
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
	
	@Override
	public void run() {
		try {
			while(running) {
				if(pause) {
					synchronized (this) {
						wait();
					}
				} 
				ConjugateGradient.search(uArr, acc, opt);
				for (VVertex v : hds.getVertices()) {
					int i = v.getIndex() * 3;
					v.position[0] = uArr[i + 0];
					v.position[1] = uArr[i + 1];
					v.position[2] = uArr[i + 2];
				}
				hif.updateHalfedgeContentAndActiveGeometry(hds);	
			}
		} catch(InterruptedException ex) {
			ex.printStackTrace();
		}
	}
}
