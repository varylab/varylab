package de.varylab.varylab.optimization;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jtem.jpetsc.Vec;
import de.jtem.jtao.Tao;

public class OptimizationThread extends Thread {

	private Tao
		solver = null;
	private Vec
		solution = null;
	private List<OptimizationListener>
		listeners = Collections.synchronizedList(new LinkedList<OptimizationListener>());
	
	public OptimizationThread(Tao solver, Vec solution) {
		super("Optimization Thread");
		this.solver = solver;
		this.solution = solution;
	}
	
	@Override
	public void run() {
		solver.solve();
		fireOptimizationFinished();
	}
	
	public void addOptimizationListener(OptimizationListener l) {
		listeners.add(l);
	}
	public void removeOptimizationListener(OptimizationListener l) {
		listeners.remove(l);
	}
	
	protected void fireOptimizationFinished() {
		synchronized (listeners) {
			for (OptimizationListener l : listeners) {
				l.optimizationFinished(solver, solution);
			}
		}
	}
	
}
