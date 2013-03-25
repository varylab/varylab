package de.varylab.varylab.optimization;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jreality.plugin.job.AbstractCancelableJob;
import de.jreality.plugin.job.JobQueuePlugin;
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
	private JobQueuePlugin
		jobQueue = null;
	private OptimizationJob
		optimizationJob = new OptimizationJob();
	
	
	public OptimizationThread(TaoApplication app, Method method, JobQueuePlugin jobQueue) {
		super("Optimization Thread " + method);
		this.application = app;
		this.jobQueue = jobQueue;
		createSolver(method);
	}
	
	private void createSolver(Method method) {
		solver = new Tao(method);
		solver.setMonitor(this);
		solver.setApplication(application);
		solver.setMaximumIterates(maxIterations);
	}
	
	
	protected class OptimizationJob extends AbstractCancelableJob {

		@Override
		public String getJobName() {
			return "Optimization " + solver.getMethod();
		}

		@Override
		public void execute() throws Exception {
			if (isCancelRequested()) return;
			synchronized (this) {
				notifyAll();
				// wait for optimization
				try { wait(); } catch (Exception e){}
			}
			
		}
		
		public void fireJobFinished() {
			super.fireJobFinished(this);
		}
		public void fireJobProgress(double progress) {
			super.fireJobProgress(this, progress);
		}
		public void fireJobStarted() {
			super.fireJobStarted(this);
		}
		
		@Override
		public void requestCancel() {
			super.requestCancel();
			synchronized (this) {
				notifyAll();
			}
		}
		
	}
	
	@Override
	public void run() {
		synchronized (optimizationJob) {
			jobQueue.queueJob(optimizationJob);
			// wait for job being started
			try { optimizationJob.wait(); } catch (Exception e){}
		}
		if (optimizationJob.isCancelRequested()) {
			synchronized (optimizationJob) {
				optimizationJob.notifyAll();
			}
			return;
		}
		try {
		optimizationJob.fireJobStarted();
		fireOptimizationStarted();
		solver.solve();
		fireOptimizationFinished();
		optimizationJob.fireJobFinished();
		} finally {
			// notify job
			synchronized (optimizationJob) {
				optimizationJob.notifyAll();
			}
		}
	}
	
	@Override
	public int monitor(Tao solver) {
		currentIteration++;
		fireOptimizationProgress(currentIteration);
		optimizationJob.fireJobProgress(currentIteration / (double)maxIterations);
		if (optimizationJob.isCancelRequested()) {
			solver.setMaximumIterates(0);
		}
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
		final double[] solutionCopy = solutionArr.clone();
		solution.restoreArray();
		synchronized (listeners) {
			for (final OptimizationListener l : listeners) {
				Runnable delegate = new Runnable() {
					@Override
					public void run() {
						l.optimizationProgress(solutionCopy, iteration);
					}
				};
				EventQueue.invokeLater(delegate);
			}
		}
	}
	protected void fireOptimizationFinished() {
		Vec solution = application.getSolutionVec();
		double[] solutionArr = solution.getArray();
		final double[] solutionCopy = solutionArr.clone();
		solution.restoreArray();
		final GetSolutionStatusResult status = solver.getSolutionStatus();
		synchronized (listeners) {
			for (final OptimizationListener l : listeners) {
				Runnable delegate = new Runnable() {
					@Override
					public void run() {
						l.optimizationFinished(status, solutionCopy);
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
