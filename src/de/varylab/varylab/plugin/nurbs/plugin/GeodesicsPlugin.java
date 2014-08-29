package de.varylab.varylab.plugin.nurbs.plugin;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.VERTEX_SHADER;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.Pn;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.job.AbstractJob;
import de.jreality.plugin.job.Job;
import de.jreality.plugin.job.JobQueuePlugin;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsWeightAdapter;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurvesOriginal;

public class GeodesicsPlugin extends ShrinkPanelPlugin implements ActionListener {
	
	private static Logger
		logger = Logger.getLogger(GeodesicsPlugin.class.getSimpleName());
	
	private SpinnerNumberModel
		tolExpModel = new SpinnerNumberModel(-3, -30.0, 0, 1),
		epsExpModel = new SpinnerNumberModel(-2, -30.0, 0, 1),
		nearbyModel = new SpinnerNumberModel(-3, -30.0, 0, 1);
	private JSpinner
		tolSpinner = new JSpinner(tolExpModel),
		epsSpinner = new JSpinner(epsExpModel),
		nearbySpinner = new JSpinner(nearbyModel);
	private JButton
		goButton = new JButton("Go");
	private JRadioButton
		segmentButton = new JRadioButton("Geodesic Segment");
	
	private HalfedgeInterface 
		hif = null;
	
	private NurbsManagerPlugin 
		nurbsManager = null;
	
	private PointSelectionPlugin
		pointSelectionPlugin = null;

	private JobQueuePlugin 
		jobQueue = null;
	
	
	public GeodesicsPlugin() {
		shrinkPanel.setShrinked(true);
		shrinkPanel.setLayout(new GridBagLayout());
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		shrinkPanel.add(new JLabel("Tolerance Exp"), lc);
		shrinkPanel.add(tolSpinner, rc);
		shrinkPanel.add(new JLabel("Eps Exp"), lc);
		shrinkPanel.add(epsSpinner, rc);
		shrinkPanel.add(new JLabel("nearby target Exp"), lc);
		shrinkPanel.add(nearbySpinner, rc);
		shrinkPanel.add(segmentButton, rc);
		shrinkPanel.add(goButton, rc);
		goButton.addActionListener(this);
		JScrollPane curvePanel = new JScrollPane(new JLabel("all curves"));
		curvePanel.setToolTipText("teyety");
		shrinkPanel.add(curvePanel);
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		final double tol = Math.pow(10,tolExpModel.getNumber().doubleValue());
		final double eps = Math.pow(10, epsExpModel.getNumber().doubleValue());
		final double nearby = Math.pow(10, nearbyModel.getNumber().doubleValue());
		
		final List<double[]> verts = pointSelectionPlugin.getSelectedPoints();
		if(verts.size() != 2){
			logger.info("Select only two vertices!");
		} else {
			final List<double[]> points = new LinkedList<>();
			final NURBSSurface activeNurbsSurface = nurbsManager.getActiveNurbsSurface();
			final Job computeGeodesicsJob;
			if(segmentButton.isSelected()){
				computeGeodesicsJob = new AbstractJob() {
					
					@Override
					public String getJobName() {
						return "Compute geodesics";
					}
					
					@Override
					protected void executeJob() throws Exception {
						points.addAll(IntegralCurvesOriginal.geodesicSegmentBetweenTwoPoints(activeNurbsSurface, verts.get(0), verts.get(1), eps, tol, nearby));
						double length = 0;
						for (int i = 0; i < points.size()-1; i++) {
							double[] uv = points.get(i);
							double[] uv2 = points.get(i+1);
							length += Pn.distanceBetween(activeNurbsSurface.getSurfacePoint(uv[0],uv[1]),
									activeNurbsSurface.getSurfacePoint(uv2[0],uv2[1]), Pn.EUCLIDEAN);
						}
						logger.info("Geodesic segment length: " + length);
					}
				};
				
				
			} else {
				computeGeodesicsJob = new AbstractJob() {
					
					@Override
					public String getJobName() {
						return "Compute geodesics";
					}
					
					@Override
					protected void executeJob() throws Exception {
						points.addAll(IntegralCurvesOriginal.geodesicExponentialGivenByTwoPoints(activeNurbsSurface, verts.get(0), verts.get(1), eps, tol,nearby));
						points.addAll(IntegralCurvesOriginal.geodesicExponentialGivenByTwoPoints(activeNurbsSurface, verts.get(1), verts.get(0), eps, tol, nearby));
					}
				};
				
			}
			Job updateJob = new AbstractJob() {

				@Override
				public String getJobName() {
					return "Update geodesics";
				}

				@Override
				protected void executeJob() throws Exception {
					addGeodesicComponent(activeNurbsSurface, points);
					
				}
			};
			jobQueue.queueJob(computeGeodesicsJob);
			jobQueue.queueJob(updateJob);
		}
	}

	private void addGeodesicComponent(NURBSSurface activeNurbsSurface, List<double[]> points) {
		IndexedLineSetFactory lsf = new IndexedLineSetFactory();
		double[][] surfacePoints = new double[points.size()][];
		int[][] indices = new int[points.size()-1][2];
		
		lsf.setVertexCount(points.size());
		for (int i = 0; i < points.size(); i++) {
			double[] uv = points.get(i);
			surfacePoints[i] = activeNurbsSurface.getSurfacePoint(uv[0], uv[1]);
			if(i != 0) {
				indices[i-1][0] = i-1;
				indices[i-1][1] = i;
			}
		}
		lsf.setVertexCoordinates(surfacePoints);
		lsf.setEdgeCount(points.size()-1);
		lsf.setEdgeIndices(indices);
		lsf.update();
		
		final SceneGraphComponent sgc = new SceneGraphComponent("geodesic segment");

		sgc.setGeometry(lsf.getGeometry());

		Appearance geodesicApp = new Appearance();
		geodesicApp.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		geodesicApp.setAttribute(CommonAttributes.EDGE_DRAW, true);
		geodesicApp.setAttribute(CommonAttributes.FACE_DRAW, false);
		geodesicApp.setAttribute(VERTEX_SHADER+"."+DIFFUSE_COLOR, Color.ORANGE);
		
		sgc.setAppearance(geodesicApp);
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				hif.getActiveLayer().addTemporaryGeometry(sgc);
			}
		};
		EventQueue.invokeLater(r);
	}

	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addAdapter(new NurbsWeightAdapter(), true);
		nurbsManager = c.getPlugin(NurbsManagerPlugin.class);
		pointSelectionPlugin = c.getPlugin(PointSelectionPlugin.class);
		jobQueue = c.getPlugin(JobQueuePlugin.class);
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
}


