package de.varylab.varylab.plugin.optimization;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import de.jreality.math.Rn;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.job.JobQueuePlugin;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.jtem.jtao.Tao;
import de.jtem.jtao.Tao.GetSolutionStatusResult;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.halfedge.adapter.CoordinateArrayAdapter;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.optimization.AnimationOptimizerThread;
import de.varylab.varylab.optimization.IterationProtocol;
import de.varylab.varylab.optimization.OptimizationListener;
import de.varylab.varylab.optimization.OptimizationThread;
import de.varylab.varylab.optimization.VaryLabFunctional;
import de.varylab.varylab.optimization.array.ArrayDomainValue;
import de.varylab.varylab.optimization.array.ArrayGradient;
import de.varylab.varylab.optimization.constraint.Constraint;
import de.varylab.varylab.optimization.constraint.FixingConstraint;
import de.varylab.varylab.optimization.constraint.SmoothGradientConstraint;
import de.varylab.varylab.optimization.constraint.TangentialConstraint;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class OptimizationPanel extends ShrinkPanelPlugin implements ActionListener, OptimizationListener {
	
	private HalfedgeInterface
		hif = null;
	private OptimizerPluginsPanel
		pluginsPanel = null;
	private IterationProtocolPanel
		protocolPanel = null;
	private JobQueuePlugin
		jobQueue = null;
	private OptimizationThread
		activeJob = null;
	private JProgressBar
		progressBar = new JProgressBar();
	private ShrinkPanel
		animationPanel = new ShrinkPanel("Animation"),
		constraintsPanel = new ShrinkPanel("Constraints");
	private JToggleButton
		animateToggle = new JToggleButton("Animate");
	private JButton
		optimizeButton = new JButton("Optimize", ImageHook.getIcon("surface.png")),
		evaluateButton = new JButton("Evaluate");
	private JComboBox
		methodCombo = new JComboBox(Tao.Method.values());

	private JCheckBox
		fixSelectionXChecker = new JCheckBox("X", true),
		fixSelectionYChecker = new JCheckBox("Y", true),
		fixSelectionZChecker = new JCheckBox("Z", true),
		fixBoundaryXChecker = new JCheckBox("X", true),
		fixBoundaryYChecker = new JCheckBox("Y", true),
		fixBoundaryZChecker = new JCheckBox("Z", true),
		fixHeightChecker = new JCheckBox("Fix Height"),
		moveAlongBoundaryChecker = new JCheckBox("Allow Inner Boundary Movements"),
		fixXChecker = new JCheckBox("X"),
		fixYChecker = new JCheckBox("Y"),
		fixZChecker = new JCheckBox("Z"),
		tangentialConstraintChecker = new JCheckBox("Tangential"),
		smoothGradientChecker = new JCheckBox("Smooth Gradient"),
		smoothSurfaceChecker = new JCheckBox("SmoothSurface"); 
	
	private SpinnerNumberModel
		accuracyModel = new SpinnerNumberModel(-8, -20, -1, -1),
		maxIterationsModel = new SpinnerNumberModel(5, 1, 10000, 1);
	private JSpinner
		accuracySpinner = new JSpinner(accuracyModel),
		maxIterationSpinner = new JSpinner(maxIterationsModel);
	
	private AnimationOptimizerThread
		animationOptimizer = null;
	
	
	public OptimizationPanel() {
		setInitialPosition(SHRINKER_RIGHT);
		shrinkPanel.setLayout(new GridBagLayout());
		shrinkPanel.setTitle("Optimization");
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.weightx = 1.0;
		gbc1.gridwidth = 1;
		gbc1.insets = new Insets(2, 2, 2, 2);
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.weightx = 1.0;
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.insets = new Insets(2, 2, 2, 2);
		
		constraintsPanel.setLayout(new GridBagLayout());
		constraintsPanel.add(new JLabel("Global"), gbc1);
		constraintsPanel.add(fixXChecker, gbc1);
		constraintsPanel.add(fixYChecker, gbc1);
		constraintsPanel.add(fixZChecker, gbc2);
		constraintsPanel.add(new JLabel("Selection"), gbc1);
		constraintsPanel.add(fixSelectionXChecker, gbc1);
		constraintsPanel.add(fixSelectionYChecker, gbc1);
		constraintsPanel.add(fixSelectionZChecker, gbc2);
		constraintsPanel.add(new JLabel("Boundary"), gbc1);
		constraintsPanel.add(fixBoundaryXChecker, gbc1);
		constraintsPanel.add(fixBoundaryYChecker, gbc1);
		constraintsPanel.add(fixBoundaryZChecker, gbc2);
		constraintsPanel.add(moveAlongBoundaryChecker, gbc2);
		constraintsPanel.add(tangentialConstraintChecker, gbc2);
		constraintsPanel.add(smoothGradientChecker, gbc2);
		constraintsPanel.add(smoothSurfaceChecker,gbc2);
		shrinkPanel.add(constraintsPanel, gbc2);
		shrinkPanel.add(fixHeightChecker, gbc2);
		
		shrinkPanel.add(new JLabel("Tolerance"), gbc1);
		shrinkPanel.add(accuracySpinner, gbc2);
		shrinkPanel.add(new JLabel("Iterations"), gbc1);
		shrinkPanel.add(maxIterationSpinner, gbc2);
		shrinkPanel.add(new JLabel("Method"), gbc1);
		shrinkPanel.add(methodCombo, gbc2);		
		
		animationPanel.setLayout(new GridBagLayout());
		animationPanel.add(animateToggle, gbc2);
		animationPanel.setShrinked(true);
		shrinkPanel.add(animationPanel, gbc2);
		
		shrinkPanel.add(evaluateButton, gbc1);
		shrinkPanel.add(optimizeButton, gbc2);
		shrinkPanel.add(progressBar, gbc2);
		

		optimizeButton.addActionListener(this);
		evaluateButton.addActionListener(this);
		animateToggle.addActionListener(this);
		
		progressBar.setString("Optimization Progress");
		progressBar.setStringPainted(true);
	}
	
	private void optimize() {
		VHDS hds = hif.get(new VHDS());
		VaryLabFunctional fun = createFunctional(hds);
		
		double acc = Math.pow(10, accuracyModel.getNumber().intValue());
		int maxIter = maxIterationsModel.getNumber().intValue();
		
		activeJob = new OptimizationThread(hds, fun);
		activeJob.setCostraints(createConstraints(hds));
		activeJob.setMethod(getTaoMethod());
		activeJob.setTolerances(0.0);
		activeJob.setGradientTolerances(acc);
		activeJob.setMaximumIterates(maxIter);
		activeJob.setSmoothingEnabled(smoothSurfaceChecker.isSelected());
		activeJob.addOptimizationListener(this);
		jobQueue.queueJob(activeJob);
	}
	
	private void optimizeAnimated() {
		if (animateToggle.isSelected()) {
			VHDS hds = hif.get(new VHDS());
			VaryLabFunctional fun = createFunctional(hds);
			animationOptimizer = new AnimationOptimizerThread(fun, hif);
			jobQueue.queueJob(animationOptimizer);
		} else if (animationOptimizer != null) {
			animationOptimizer.requestCancel();
		}
	}
	
	
	@Override
	public void optimizationStarted(int maxIterations) {
		progressBar.setMinimum(0);
		progressBar.setMaximum(maxIterations);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setString("0");
	}
	
	@Override
	public void optimizationProgress(double[] solution, int iteration) {
		progressBar.setValue(iteration);
		progressBar.setString("" + iteration);
		progressBar.repaint();
		VHDS hds = hif.get(new VHDS());
		List<IterationProtocol> protocol = new LinkedList<IterationProtocol>();
		for (VarylabOptimizerPlugin op : pluginsPanel.getActiveOptimizers()) {
			protocol.add(op.getIterationProtocol(solution, hds));
		}
		protocolPanel.appendIterationProtocol(protocol);
	}
	
	
	@Override
	public void optimizationFinished(GetSolutionStatusResult stat, double[] x) {
		String status = stat.toString().replace("getSolutionStatus : ", "");
		System.out.println("optimization status ------------------------------------");
		System.out.println(status);
		progressBar.setString("Finished " + stat.reason);
		final Adapter<double[]> posAdapter = new CoordinateArrayAdapter(x, 1.0);
		Runnable updater = new Runnable() {
			@Override
			public void run() {
				hif.updateGeometry(posAdapter);
			}
		};
		EventQueue.invokeLater(updater);
	}
	
	
	private void evaluate() {
		// TODO: create job and react on finish
//		VHDS hds = hif.get(new VHDS());
//		int dim = hds.numVertices() * 3;
//		DomainValue x = createPositionValue(hds);
//		Energy E = new ArrayEnergy();
//		Vec gVec = new Vec(dim);
//		TaoGradient G = new TaoGradient(gVec);
//		VaryLabFunctional fun = createFunctional(hds);
//		fun.evaluate(hds, x, E, G, null);
//		double energy = E.get();
//		System.out.println("Energy:" + energy +"(" + energy/(2*Math.PI) + "·2π)");
//		System.out.println("Gradient Length: " + gVec.norm(NORM_FROBENIUS));
	}
	
	public List<Constraint> createConstraints(VHDS hds) {
		List<Constraint> result = new LinkedList<Constraint>();
		Set<VVertex> fixedVerts = hif.getSelection().getVertices(hds);
		FixingConstraint fixConstraint = new FixingConstraint(
			fixedVerts,
			fixSelectionXChecker.isSelected(),
			fixSelectionYChecker.isSelected(),
			fixSelectionZChecker.isSelected(),
			fixBoundaryXChecker.isSelected(), 
			fixBoundaryYChecker.isSelected(), 
			fixBoundaryZChecker.isSelected(), 
			moveAlongBoundaryChecker.isSelected(),
			fixXChecker.isSelected(), 
			fixYChecker.isSelected(), 
			fixZChecker.isSelected()
		);
		result.add(fixConstraint);
		if(tangentialConstraintChecker.isSelected()) {
			result.add(new TangentialConstraint());
		}
		if(smoothGradientChecker.isSelected()) {
			result.add(new SmoothGradientConstraint());
		}
		return result;
	}
	
	
	public VaryLabFunctional createFunctional(VHDS hds) {
		int dim = hds.numVertices() * 3;
		DomainValue x = createPositionValue(hds);
		List<Functional<VVertex, VEdge, VFace>> funs = new LinkedList<Functional<VVertex,VEdge,VFace>>();
		Map<Functional<?, ?, ?>, Double> coeffs = new HashMap<Functional<?,?,?>, Double>();
		for (VarylabOptimizerPlugin op : pluginsPanel.getActiveOptimizers()) {
			Functional<VVertex, VEdge, VFace> fun = op.getFunctional(hds);
			funs.add(fun);
			
			double coeff = pluginsPanel.getCoefficient(op);
			if (pluginsPanel.isNormalizeEnergies()) {
				double[] gArr = new double[dim];
				ArrayGradient G = new ArrayGradient(gArr);
				fun.evaluate(hds, x, null, G, null);
				double gl = Rn.euclideanNorm(gArr);
				if (gl > 1E-8) {
					coeff /= gl;
				}
			}
			
			coeffs.put(fun, coeff);
		}
		return new VaryLabFunctional(funs, coeffs, dim);
	}
	
	
	
	public DomainValue createPositionValue(VHDS hds) {
		int dim = hds.numVertices() * 3;
		double[] x = new double[dim];
		for (VVertex v : hds.getVertices()) {
			x[v.getIndex() * 3 + 0] = v.P[0];
			x[v.getIndex() * 3 + 1] = v.P[1];
			x[v.getIndex() * 3 + 2] = v.P[2];
		}
		return new ArrayDomainValue(x);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (optimizeButton == s) {
			optimize();
		} else if(evaluateButton == s) {
			evaluate();
		} else if(animateToggle == s) {
			optimizeAnimated();
		}
	}
	
	private Tao.Method getTaoMethod() {
		return (Tao.Method)methodCombo.getSelectedItem();
	}
	

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "gradAbsToleranceExp", accuracyModel.getValue());
		c.storeProperty(getClass(), "maxIterations", maxIterationsModel.getValue());
		c.storeProperty(getClass(), "taoMethod", getTaoMethod());
		c.storeProperty(getClass(), "fixX", fixXChecker.isSelected());
		c.storeProperty(getClass(), "fixY", fixYChecker.isSelected());
		c.storeProperty(getClass(), "fixZ", fixZChecker.isSelected());
		c.storeProperty(getClass(), "fixSelectionX", fixSelectionXChecker.isSelected());
		c.storeProperty(getClass(), "fixSelectionY", fixSelectionYChecker.isSelected());
		c.storeProperty(getClass(), "fixSelectionZ", fixSelectionZChecker.isSelected());
		c.storeProperty(getClass(), "fixBoundaryX", fixBoundaryXChecker.isSelected());
		c.storeProperty(getClass(), "fixBoundaryY", fixBoundaryYChecker.isSelected());
		c.storeProperty(getClass(), "fixBoundaryZ", fixBoundaryZChecker.isSelected());
		c.storeProperty(getClass(), "boundaryMovement", moveAlongBoundaryChecker.isSelected());
		c.storeProperty(getClass(), "constraintsShrinked", constraintsPanel.isShrinked());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		accuracyModel.setValue(c.getProperty(getClass(), "gradAbsToleranceExp", accuracyModel.getValue()));
		maxIterationsModel.setValue(c.getProperty(getClass(), "maxIterations", maxIterationsModel.getValue()));
		methodCombo.setSelectedItem(c.getProperty(getClass(), "taoMethod", Tao.Method.CG));
		fixXChecker.setSelected(c.getProperty(getClass(), "fixX", fixXChecker.isSelected()));
		fixYChecker.setSelected(c.getProperty(getClass(), "fixY", fixYChecker.isSelected()));
		fixZChecker.setSelected(c.getProperty(getClass(), "fixZ", fixZChecker.isSelected()));
		fixSelectionXChecker.setSelected(c.getProperty(getClass(), "fixSelectionX", fixSelectionXChecker.isSelected()));
		fixSelectionYChecker.setSelected(c.getProperty(getClass(), "fixSelectionY", fixSelectionYChecker.isSelected()));
		fixSelectionZChecker.setSelected(c.getProperty(getClass(), "fixSelectionZ", fixSelectionZChecker.isSelected()));
		fixBoundaryXChecker.setSelected(c.getProperty(getClass(), "fixBoundaryX", fixBoundaryXChecker.isSelected()));
		fixBoundaryYChecker.setSelected(c.getProperty(getClass(), "fixBoundaryY", fixBoundaryYChecker.isSelected()));
		fixBoundaryZChecker.setSelected(c.getProperty(getClass(), "fixBoundaryZ", fixBoundaryZChecker.isSelected()));
		moveAlongBoundaryChecker.setSelected(c.getProperty(getClass(), "boundaryMovement", moveAlongBoundaryChecker.isSelected()));
		constraintsPanel.setShrinked(c.getProperty(getClass(), "constraintsShrinked", true));
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		pluginsPanel = c.getPlugin(OptimizerPluginsPanel.class);
		protocolPanel = c.getPlugin(IterationProtocolPanel.class);
		jobQueue = c.getPlugin(JobQueuePlugin.class);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Optimization Panel", "Stefan Sechelmann");
	}
	
}
