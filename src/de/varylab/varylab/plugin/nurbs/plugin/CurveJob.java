package de.varylab.varylab.plugin.nurbs.plugin;

import de.jreality.plugin.job.AbstractJob;

public class CurveJob {
	
	private AbstractJob
		computationJob = null,
		updateJob = null;
	
	public AbstractJob getComputationJob() {
		return computationJob;
	}

	public AbstractJob getUpdateJob() {
		return updateJob;
	}

	public CurveJob(AbstractJob computationJob, AbstractJob updateJob) {
		this.computationJob = computationJob;
		this.updateJob = updateJob;
	}

}
