package de.varylab.varylab.plugin.optimization;

import static de.jtem.jpetsc.InsertMode.INSERT_VALUES;
import static de.jtem.jpetsc.NormType.NORM_FROBENIUS;
import static javax.swing.JOptionPane.WARNING_MESSAGE;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
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
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import de.jreality.plugin.basic.View;
import de.jreality.plugin.job.JobQueuePlugin;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jpetsc.InsertMode;
import de.jtem.jpetsc.Mat;
import de.jtem.jpetsc.PETSc;
import de.jtem.jpetsc.Vec;
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
import de.varylab.varylab.optimization.VaryLabTaoApplication;
import de.varylab.varylab.optimization.constraint.FixingConstraint;
import de.varylab.varylab.optimization.constraint.SmoothGradientConstraint;
import de.varylab.varylab.optimization.constraint.TangentialConstraint;
import de.varylab.varylab.optimization.tao.TaoDomainValue;
import de.varylab.varylab.optimization.tao.TaoEnergy;
import de.varylab.varylab.optimization.tao.TaoGradient;
import de.varylab.varylab.optimization.tao.TaoUtility;
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
	private JButton
		optimizeButton = new JButton("Optimize", ImageHook.getIcon("surface.png")),
		initButton = new JButton("Init"),
		playButton = new JButton("Play",ImageHook.getIcon("Play24.gif")),
		pauseButton = new JButton("Pause",ImageHook.getIcon("Pause24.gif")),
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
		optThread = new AnimationOptimizerThread();
	
	private double 
		maxz_before = Double.NEGATIVE_INFINITY;
		
	
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
		animationPanel.add(initButton, gbc1);
		animationPanel.add(playButton, gbc1);
		animationPanel.add(pauseButton, gbc2);
		animationPanel.setShrinked(true);
		shrinkPanel.add(animationPanel, gbc2);
		
		shrinkPanel.add(evaluateButton, gbc1);
		shrinkPanel.add(optimizeButton, gbc2);
		shrinkPanel.add(progressBar, gbc2);
		

		optimizeButton.addActionListener(this);
		evaluateButton.addActionListener(this);
		initButton.addActionListener(this);
		playButton.addActionListener(this);
		playButton.setEnabled(false);
		pauseButton.addActionListener(this);
		pauseButton.setEnabled(false);
		
		progressBar.setString("Optimization Progress");
		progressBar.setStringPainted(true);
		
		optThread.start();
	}
	
	private void optimize() {
		Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
		VHDS hds = hif.get(new VHDS());
		int dim = hds.numVertices() * 3;
		VaryLabFunctional fun = createFunctional(hds);
		
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
		
		double acc = Math.pow(10, accuracyModel.getNumber().intValue());
		int maxIter = maxIterationsModel.getNumber().intValue();
		Tao.Initialize();
		VaryLabTaoApplication app = new VaryLabTaoApplication(hds, fun);
		app.addConstraint(fixConstraint);
		if(tangentialConstraintChecker.isSelected()) {
			app.addConstraint(new TangentialConstraint());
		}
		if(smoothGradientChecker.isSelected()) {
			app.addConstraint(new SmoothGradientConstraint());
		}
		app.enableSmoothing(smoothSurfaceChecker.isSelected());
		
		Vec x = new Vec(dim);
		for (VVertex v : hds.getVertices()) {
			x.setValue(v.getIndex() * 3 + 0, v.P[0] / v.P[3], InsertMode.INSERT_VALUES);
			x.setValue(v.getIndex() * 3 + 1, v.P[1] / v.P[3], InsertMode.INSERT_VALUES);
			x.setValue(v.getIndex() * 3 + 2, v.P[2] / v.P[3], InsertMode.INSERT_VALUES);
		}
		
		maxz_before = Double.NEGATIVE_INFINITY;
		if(fixHeightChecker.isSelected()) {
			for (int i = 0; i < x.getSize()/3;++i) {
				if(maxz_before < Math.abs(x.getValue(3*i+2))) {
					maxz_before = Math.abs(x.getValue(3*i+2));
				}
			}
		} else {
			maxz_before = 1.0;
		}
		
		app.setInitialSolutionVec(x);
		if (fun.hasHessian()) {
			Mat H = Mat.createSeqAIJ(dim, dim, PETSc.PETSC_DEFAULT, TaoUtility.getPETScNonZeros(hds, fun));
			H.assemble();
			app.setHessianMat(H, H);
		} else {
			switch (getTaoMethod()) {
			case NLS:
			case NTR:
			case GPCG:
			case BQPIP:
			case KT:
				JOptionPane.showMessageDialog(
					w, 
					"Cannot use method " + getTaoMethod() + " without Hessian matrix", 
					"Method Error", 
					WARNING_MESSAGE
				);
				return;
			default:
				break;
			}
		}
		activeJob = new OptimizationThread(app, getTaoMethod(), jobQueue);
		activeJob.setTolerances(0.0, 0.0, 0.0, 0.0);
		activeJob.setGradientTolerances(acc, acc, 0);
		activeJob.setMaximumIterates(maxIter);
		activeJob.addOptimizationListener(this);
		activeJob.start();
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
		
		double maxz_after= Double.NEGATIVE_INFINITY;
		if(fixHeightChecker.isSelected()) {
			for (int i = 0; i < x.length/3;++i) {
				if(maxz_after < Math.abs(x[3*i+2])) {
					maxz_after = Math.abs(x[3*i+2]);
				}
			}
		} else {
			maxz_after = 1.0;
		}
		
		double zScale = maxz_before/maxz_after;
		final Adapter<double[]> posAdapter = new CoordinateArrayAdapter(x, zScale);
		Runnable updater = new Runnable() {
			@Override
			public void run() {
				hif.updateGeometry(posAdapter);
			}
		};
		EventQueue.invokeLater(updater);
	}
	
	
	private void evaluate() {
		VHDS hds = hif.get(new VHDS());
		int dim = hds.numVertices() * 3;
		TaoDomainValue x = createPositionValue(hds);
		TaoEnergy E = new TaoEnergy();
		Vec gVec = new Vec(dim);
		TaoGradient G = new TaoGradient(gVec);
		VaryLabFunctional fun = createFunctional(hds);
		fun.evaluate(hds, x, E, G, null);
		double energy = E.get();
		System.out.println("Energy:" + energy +"(" + energy/(2*Math.PI) + "·2π)");
		System.out.println("Gradient Length: " + gVec.norm(NORM_FROBENIUS));
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
				Vec gVec = new Vec(dim);
				TaoGradient G = new TaoGradient(gVec);
				fun.evaluate(hds, x, null, G, null);
				double gl = gVec.norm(NORM_FROBENIUS);
				if (gl > 1E-8) {
					coeff /= gl;
				}
			}
			
			coeffs.put(fun, coeff);
		}
		return new VaryLabFunctional(funs, coeffs, dim);
	}
	
	
	
	public TaoDomainValue createPositionValue(VHDS hds) {
		int dim = hds.numVertices() * 3;
		Vec u = new Vec(dim);
		for (VVertex v : hds.getVertices()) {
			u.setValue(v.getIndex() * 3 + 0, v.P[0], INSERT_VALUES);
			u.setValue(v.getIndex() * 3 + 1, v.P[1], INSERT_VALUES);
			u.setValue(v.getIndex() * 3 + 2, v.P[2], INSERT_VALUES);
		}
		return new TaoDomainValue(u);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (optimizeButton == s) {
			optimize();
		} else if(evaluateButton == s) {
			evaluate();
		} else if(initButton == s) {
			initOptAnimation();
			playButton.setEnabled(true);
		} else if(playButton == s) {
			pauseButton.setEnabled(true);
			playButton.setEnabled(false);
			animateOptimization();
		} else if(pauseButton == s) {
			optThread.setPause(true);
			pauseButton.setEnabled(false);
			playButton.setEnabled(true);
		}
	}
	
	private void initOptAnimation() {
		VHDS hds = hif.get(new VHDS());
		VaryLabFunctional fun = createFunctional(hds);
		Set<VVertex> fixedVerts = hif.getSelection().getVertices(hif.get(new VHDS()));
		
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
		double acc = Math.pow(10, accuracyModel.getNumber().intValue());
		int step = maxIterationsModel.getNumber().intValue();
		
		optThread.initOptimizer(hif, fun, fixConstraint, acc, step);
	}
	
	private void animateOptimization() {
		optThread.setPause(false);
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
