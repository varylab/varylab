package de.varylab.varylab.optimization;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jreality.plugin.job.AbstractCancelableJob;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.jpetsc.Vec;
import de.jtem.jtao.Tao.GetSolutionStatusResult;
import de.varylab.varylab.halfedge.VHDS;

public abstract class AbstractOptimizationJob extends AbstractCancelableJob {

	private String
		jobName = "Optimization";
	private List<OptimizationListener>
		listeners = Collections.synchronizedList(new LinkedList<OptimizationListener>());
	
	public AbstractOptimizationJob(String name) {
		this.jobName = name;
	}
	
	public abstract HalfedgeLayer getSourceLayer();
	public abstract VHDS getHDS();
	
	@Override
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public void addOptimizationListener(OptimizationListener l) {
		listeners.add(l);
	}
	public void removeOptimizationListener(OptimizationListener l) {
		listeners.remove(l);
	}

	protected void fireOptimizationStarted(final int maxIterations) {
		synchronized (listeners) {
			for (final OptimizationListener l : listeners) {
				Runnable delegate = new Runnable() {
					@Override
					public void run() {
						l.optimizationStarted(AbstractOptimizationJob.this, maxIterations);						
					}
				};
				EventQueue.invokeLater(delegate);
			}
		}
	}
	protected void fireOptimizationProgress(final int iteration, Vec solution) {
		double[] solutionArr = solution.getArray();
		final double[] solutionCopy = solutionArr.clone();
		solution.restoreArray();
		synchronized (listeners) {
			for (final OptimizationListener l : listeners) {
				Runnable delegate = new Runnable() {
					@Override
					public void run() {
						l.optimizationProgress(AbstractOptimizationJob.this, solutionCopy, iteration);
					}
				};
				EventQueue.invokeLater(delegate);
			}
		}
	}
	protected void fireOptimizationFinished(final GetSolutionStatusResult status, Vec solution) {
		double[] solutionArr = solution.getArray();
		final double[] solutionCopy = solutionArr.clone();
		solution.restoreArray();
		synchronized (listeners) {
			for (final OptimizationListener l : listeners) {
				Runnable delegate = new Runnable() {
					@Override
					public void run() {
						l.optimizationFinished(AbstractOptimizationJob.this, status, solutionCopy);
					}
				};
				EventQueue.invokeLater(delegate);
			}
		}
	}
	
}
