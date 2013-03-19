package de.varylab.varylab.optimization;

import static de.jtem.jpetsc.InsertMode.INSERT_VALUES;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jtem.jpetsc.Vec;
import de.jtem.jtao.Tao;
import de.jtem.jtao.Tao.GetSolutionStatusResult;
import de.jtem.jtao.Tao.Method;
import de.jtem.jtao.TaoApplication;
import de.jtem.jtao.TaoMonitor;

public class OptimizationThread extends Thread implements TaoMonitor {

	private Tao
		solver = null;
	private TaoApplication
		application = null;
	private int 
		maxIterations = 50,
		currentIteration = 0;
	private List<OptimizationListener>
		listeners = Collections.synchronizedList(new LinkedList<OptimizationListener>());
	
	
	public OptimizationThread(TaoApplication app, Method method) {
		super("Optimization Thread");
		this.application = app;
		createSolver(method);
	}
	
	private void createSolver(Method method) {
		solver = new Tao(method);
		solver.setMonitor(this);
		solver.setApplication(application);
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
	protected void fireOptimizationProgress(final int iteration) {
		Vec solution = application.getSolutionVec();
		double[] solutionArr = solution.getArray();
		final double[] newSolutionArr = solutionArr.clone();
		solution.restoreArray();
		synchronized (listeners) {
			for (final OptimizationListener l : listeners) {
				Runnable delegate = new Runnable() {
					@Override
					public void run() {
						Vec solution = new Vec(newSolutionArr.length);
						solution.setBlockSize(solution.getSize());
						solution.setValuesBlocked(1, new int[] {0}, newSolutionArr, INSERT_VALUES);
						l.optimizationProgress(solution, iteration);
					}
				};
				EventQueue.invokeLater(delegate);
			}
		}
	}
	protected void fireOptimizationFinished() {
		Vec solution = application.getSolutionVec();
		double[] solutionArr = solution.getArray();
		final double[] newSolutionArr = solutionArr.clone();
		solution.restoreArray();
		final GetSolutionStatusResult status = solver.getSolutionStatus();
		synchronized (listeners) {
			for (final OptimizationListener l : listeners) {
				Runnable delegate = new Runnable() {
					@Override
					public void run() {
						Vec solution = new Vec(newSolutionArr.length);
						solution.setBlockSize(solution.getSize());
						solution.setValuesBlocked(1, new int[] {0}, newSolutionArr, INSERT_VALUES);
						l.optimizationFinished(status, solution);
					}
				};
				EventQueue.invokeLater(delegate);
			}
		}
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
