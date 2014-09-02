package de.varylab.varylab.plugin.nurbs.scene;

import java.awt.EventQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Rn;
import de.jreality.plugin.job.AbstractJob;
import de.jreality.plugin.job.JobQueuePlugin;
import de.jreality.plugin.job.ParallelJob;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;
import de.varylab.varylab.plugin.nurbs.plugin.CurveJob;

public class DraggableIntegralCurveListener implements PointDragListener {
	
	private double[] 
		p = null;
	
	private boolean
		recomputeAll = true;
	
	private List<CurveJob> curveJobQueue = Collections.synchronizedList(new LinkedList<CurveJob>());
	
	private NURBSSurface 
		surface = null;
	
	private DraggableIntegralNurbsCurves
		curve = null;

	private double startTol;

	private JobQueuePlugin jobQueuePlugin = null;
	
	public DraggableIntegralCurveListener(NURBSSurface surface, DraggableIntegralNurbsCurves curve, JobQueuePlugin queue) {
		this.surface = surface;
		this.curve = curve;
		jobQueuePlugin = queue;
	}

	@Override
	public void pointDragStart(PointDragEvent e) {
		startTol = curve.getTol();
	}

	@Override
	public void pointDragged(final PointDragEvent e) {
		p = new double[]{e.getX(), e.getY(), e.getZ(), 1.0};
		updateAllCurves(curve, p, 1E-2);
	}

	@Override
	public void pointDragEnd(PointDragEvent e) {
		updateAllCurves(curve, p, startTol);
		
		AbstractJob updateJob = new AbstractJob() {
			@Override
			public String getJobName() {
				return "Update curve table";
			}
			
			@Override
			protected void executeJob() throws Exception {
				synchronized(curveJobQueue) {
					curveJobQueue.remove(0);
					processCurveJobs();
				}
			}
		};
		addCurveJobs(new CurveJob(null, updateJob));
	}	
	
	private void updateAllCurves(DraggableIntegralNurbsCurves dc, final double[] p, double tol) {
		Collection<AbstractJob> jobs = new LinkedList<AbstractJob>();
		final List<PolygonalLine> linesToRemove = Collections.synchronizedList(new LinkedList<PolygonalLine>());
		final List<PolygonalLine> linesToAdd = Collections.synchronizedList(new LinkedList<PolygonalLine>());
		
		AbstractJob j = createCurveJob(dc, p, tol, linesToRemove, linesToAdd);
		jobs.add(j);

		if(recomputeAll){
			jobs.addAll(createCommonCurvesJobs(dc, p, tol, linesToRemove, linesToAdd));
		}
		ParallelJob parallelJob = new ParallelJob(jobs);
		
		AbstractJob updateJob = createUpdateJob(dc, linesToRemove, linesToAdd);
		addCurveJobs(new CurveJob(parallelJob, updateJob));
	}

	private AbstractJob createUpdateJob(final DraggableIntegralNurbsCurves curve, final List<PolygonalLine> linesToRemove, final List<PolygonalLine> linesToAdd) {
		AbstractJob updateJob = new AbstractJob() {
			@Override
			public String getJobName() {
				return "Update curves display";
			}
			
			@Override
			protected void executeJob() throws Exception {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						List<DraggableIntegralNurbsCurves> cc = curve.getCommonCurves();
						curve.updateComponent();
						for (final DraggableIntegralNurbsCurves dc : cc) {
							dc.updateComponent();
						}
						synchronized(curveJobQueue) {
							curveJobQueue.remove(0);
							processCurveJobs();
						}
					}
				};
				EventQueue.invokeLater(r);
			}
		};
		return updateJob;
	}

	private void addCurveJobs(CurveJob cj) {
		synchronized(curveJobQueue) {
			if(curveJobQueue.size() == 2) {
				curveJobQueue.set(1,cj);
			} else {
				curveJobQueue.add(cj);
			} 
			if(curveJobQueue.size() == 1) {
				processCurveJobs();
			}
		}
	}

	private void processCurveJobs() {
		synchronized(curveJobQueue) {
			if(!curveJobQueue.isEmpty()) {
				CurveJob cj = curveJobQueue.get(0);
				AbstractJob parallelJob = cj.getComputationJob();
				if(parallelJob != null) {
					jobQueuePlugin .queueJob(parallelJob);
				}
				jobQueuePlugin.queueJob(cj.getUpdateJob());
			}
		}
	}
	
	private Collection<AbstractJob> createCommonCurvesJobs(final DraggableIntegralNurbsCurves curve, final double[] p, final double tol, final List<PolygonalLine> linesToRemove, final List<PolygonalLine> linesToAdd) {
		Collection<AbstractJob> jobs = new LinkedHashSet<AbstractJob>();
		double[] uv = surface.getClosestPointDomain(p);
		// TODO: replace uv with p??
		final double[] translation = Rn.subtract(null, uv, curve.getInitialUV());
		List<DraggableIntegralNurbsCurves> cc = curve.getCommonCurves();
		for (final DraggableIntegralNurbsCurves dc : cc) {
			double[] otherStartUV = dc.getInitialUV();
			double[] newCoords = Rn.add(null, otherStartUV, translation);
			jobs.add(createCurveJob(dc, surface.getSurfacePoint(newCoords), tol, linesToRemove, linesToAdd));
		}
		return jobs;
	}
	
	private AbstractJob createCurveJob(final DraggableIntegralNurbsCurves curve, final double[] p, final double tol, final List<PolygonalLine> linesToRemove, final List<PolygonalLine> linesToAdd) {
		AbstractJob j = new AbstractJob() {
			
			@Override
			public String getJobName() {
				return "Recompute curve " + p;
			}
			
			@Override
			protected void executeJob() throws Exception {
				synchronized (curve) {
					curve.setTol(tol);
					linesToRemove.addAll(curve.getPolygonalLines());
					curve.recomputeCurves(p);
					linesToAdd.addAll(curve.getPolygonalLines());
				}
			}
		};
		return j;
	}
	
}