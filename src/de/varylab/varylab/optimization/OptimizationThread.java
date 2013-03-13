package de.varylab.varylab.optimization;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jtem.jtao.Tao;
import de.jtem.jtao.Tao.Method;
import de.jtem.jtao.TaoApplication;
import de.jtem.jtao.TaoMonitor;

public class OptimizationThread extends Thread implements TaoMonitor {

	private Tao
		solver = new Tao(Method.LMVM);
	private TaoApplication
		application = null;
	private int 
		maxIterations = 50,
		currentIteration = 0;
	private List<OptimizationListener>
		listeners = Collections.synchronizedList(new LinkedList<OptimizationListener>());
	
	
	public OptimizationThread(TaoApplication app) {
		super("Optimization Thread");
		this.application = app;
		solver.setApplication(app);
		solver.setMonitor(this);
		solver.setMaximumIterates(maxIterations);
	}
	
	@Override
	public void run() {
		fireOptimizationStarted();
		solver.solve();
		fireOptimizationFinished();
	}
	
	@Override
	public int monitor(Tao solver) {
		currentIteration++;
		fireOptimizationProgress(currentIteration);
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
			for (OptimizationListener l : listeners) {
				l.optimizationStarted(application, maxIterations);
			}
		}
	}
	protected void fireOptimizationProgress(int iteration) {
		synchronized (listeners) {
			for (OptimizationListener l : listeners) {
				l.optimizationProgress(application, iteration);
			}
		}
	}
	protected void fireOptimizationFinished() {
		synchronized (listeners) {
			for (OptimizationListener l : listeners) {
				l.optimizationFinished(application);
			}
		}
	}
	
	public void setMethod(Method m) {
		solver.setMethod(m);
	}
	public void setMaximumIterates(int num) {
		this.maxIterations = num;
		solver.setMaximumIterates(num);
	}
	public void setTolerances(double fatol, double frtol, double catol, double crtol) {
		solver.setTolerances(fatol, frtol, catol, crtol);
	}
	public void setGradientTolerances(double gatol, double grtol, double gttol) {
		solver.setGradientTolerances(gatol, grtol, gttol);
	}
	
}
