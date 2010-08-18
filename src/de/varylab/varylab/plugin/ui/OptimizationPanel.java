package de.varylab.varylab.plugin.ui;

import static javax.swing.JOptionPane.WARNING_MESSAGE;

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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.Vector.Norm;
import de.jreality.plugin.basic.View;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.MyDomainValue;
import de.jtem.halfedgetools.functional.MyEnergy;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.varylab.jpetsc.InsertMode;
import de.varylab.jpetsc.Mat;
import de.varylab.jpetsc.PETSc;
import de.varylab.jpetsc.Vec;
import de.varylab.jtao.Tao;
import de.varylab.jtao.Tao.GetSolutionStatusResult;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.CombinedFunctional;
import de.varylab.varylab.math.CombinedOptimizableTao;
import de.varylab.varylab.math.FixingConstraint;
import de.varylab.varylab.math.TangentialConstraint;
import de.varylab.varylab.plugin.OptimizerPlugin;
import de.varylab.varylab.plugin.meshoptimizer.OptimizerThread;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class OptimizationPanel extends ShrinkPanelPlugin implements ActionListener {
	
	private HalfedgeInterface
		hif = null;
	private OptimizerPluginsPanel
		pluginsPanel = null;
	private JPanel
		constraintsPanel = new JPanel();
	private ShrinkPanel
		animationPanel = new ShrinkPanel("Animation");
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
	private OptimizerThread 
		optThread = new OptimizerThread();
		
	
	public OptimizationPanel() {
		setInitialPosition(SHRINKER_RIGHT);
		shrinkPanel.setLayout(new GridBagLayout());
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
		constraintsPanel.setBorder(BorderFactory.createTitledBorder("Constraints"));
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
		constraintsPanel.add(smoothGradientChecker, gbc1);
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
		animationPanel.add(initButton,gbc1);
		animationPanel.add(playButton,gbc1);
		animationPanel.add(pauseButton,gbc2);
		animationPanel.setShrinked(true);
		shrinkPanel.add(animationPanel,gbc2);
		
		shrinkPanel.add(evaluateButton,gbc1);
		shrinkPanel.add(optimizeButton,gbc2);


		optimizeButton.addActionListener(this);
		evaluateButton.addActionListener(this);
		initButton.addActionListener(this);
		playButton.addActionListener(this);
		playButton.setEnabled(false);
		pauseButton.addActionListener(this);
		pauseButton.setEnabled(false);
		
		optThread.start();
	}
	
	
	private void optimize() {
		VHDS hds = hif.get(new VHDS());
		int dim = hds.numVertices() * 3;
		CombinedFunctional fun = createFunctional(hds);
		
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
		CombinedOptimizableTao app = new CombinedOptimizableTao(hds, fun);
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
			x.setValue(v.getIndex() * 3 + 0, v.position[0], InsertMode.INSERT_VALUES);
			x.setValue(v.getIndex() * 3 + 1, v.position[1], InsertMode.INSERT_VALUES);
			x.setValue(v.getIndex() * 3 + 2, v.position[2], InsertMode.INSERT_VALUES);
		}
		
		double maxz_before = Double.NEGATIVE_INFINITY;
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
		Tao optimizer = new Tao(getTaoMethod());
		if (fun.hasHessian()) {
			Mat H = Mat.createSeqAIJ(dim, dim, PETSc.PETSC_DEFAULT, getPETScNonZeros(hds, fun));
			H.assemble();
			app.setHessianMat(H, H);
		} else {
			Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
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
			}
		}
		optimizer.setApplication(app);
		optimizer.setGradientTolerances(acc, acc, 0);
		optimizer.setMaximumIterates(maxIter);
		optimizer.setTolerances(0.0, 0.0, 0.0, 0.0);
		optimizer.solve();
		
		GetSolutionStatusResult stat = optimizer.getSolutionStatus();
		String status = stat.toString().replace("getSolutionStatus : ", "");
		System.out.println("optimization status ------------------------------------");
		System.out.println(status);
		
		double maxz_after= Double.NEGATIVE_INFINITY;
		if(fixHeightChecker.isSelected()) {
			for (int i = 0; i < x.getSize()/3;++i) {
				if(maxz_after < Math.abs(x.getValue(3*i+2))) {
					maxz_after = Math.abs(x.getValue(3*i+2));
				}
			}
		} else {
			maxz_after = 1.0;
		}
		
		for (VVertex v : hds.getVertices()) {
			int i = v.getIndex() * 3;
			v.position[0] = x.getValue(i + 0);
			v.position[1] = x.getValue(i + 1);
			v.position[2] = maxz_before*x.getValue(i + 2)/maxz_after;
		}
		HalfedgeSelection hes = new HalfedgeSelection(hif.getSelection());
		hif.set(hds, new AdapterSet());
		hif.setSelection(hes);
	}
	
	private void evaluate() {
		VHDS hds = hif.get(new VHDS());
		int dim = hds.numVertices() * 3;
		DomainValue x = createPositionValue(hds);
		Energy E = new MyEnergy();
		Vector G = new DenseVector(dim);
		MTJGradient mtjG = new MTJGradient(G);
		CombinedFunctional fun = createFunctional(hds);
		fun.evaluate(hds, x, E, mtjG, null);
		double energy = E.get();
		System.out.println("Energy:" + energy +"(" + energy/(2*Math.PI) + "·2π)");
		System.out.println("Gradient Length: " + G.norm(Norm.TwoRobust));
	}
	
	
	public static int[] getPETScNonZeros(VHDS hds, CombinedFunctional fun){
		int [][] sparseStucture = fun.getNonZeroPattern(hds);
		int [] nnz = new int[sparseStucture.length];
		for(int i = 0; i < nnz.length; i++){
			nnz[i] = sparseStucture[i].length;
		}
		return nnz;
	}
	
	
	public CombinedFunctional createFunctional(VHDS hds) {
		int dim = hds.numVertices() * 3;
		DomainValue x = createPositionValue(hds);
		List<Functional<VVertex, VEdge, VFace>> funs = new LinkedList<Functional<VVertex,VEdge,VFace>>();
		Map<Functional<?, ?, ?>, Double> coeffs = new HashMap<Functional<?,?,?>, Double>();
		for (OptimizerPlugin op : pluginsPanel.getActiveOptimizers()) {
			Functional<VVertex, VEdge, VFace> fun = op.getFunctional(hds);
			funs.add(fun);
			
			double coeff = pluginsPanel.getCoefficient(op);
			if (pluginsPanel.isNormalizeEnergies()) {
				Vector G = new DenseVector(dim);
				MTJGradient mtjG = new MTJGradient(G);
				fun.evaluate(hds, x, null, mtjG, null);
				double gl = G.norm(Norm.TwoRobust);
				if (gl > 1E-20) {
					coeff /= gl;
				}
			}
			
			coeffs.put(fun, coeff);
		}
		return new CombinedFunctional(funs, coeffs, dim);
	}
	
	public DomainValue createPositionValue(VHDS hds) {
		int dim = hds.numVertices() * 3;
		DenseVector u = new DenseVector(dim);
		for (VVertex v : hds.getVertices()) {
			u.set(v.getIndex() * 3 + 0, v.position[0]);
			u.set(v.getIndex() * 3 + 1, v.position[1]);
			u.set(v.getIndex() * 3 + 2, v.position[2]);
		}
		return new MyDomainValue(u);
	}
	
	
	
	protected static class MTJGradient implements Gradient {

		protected Vector
			G = null;
		
		public MTJGradient(Vector G) {
			this.G = G;
		}
		
		@Override
		public void add(int i, double value) {
			G.add(i, value);
		}

		@Override
		public void set(int i, double value) {
			G.set(i, value);
		}
		
		@Override
		public void setZero() {
			G.zero();
		}
		
		@Override
		public double get(int i) {
			return G.get(i);
		}
		
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
//		List<Functional<VVertex, VEdge, VFace>> funs = new LinkedList<Functional<VVertex,VEdge,VFace>>();
//		Map<Functional<?, ?, ?>, Double> coeffs = new HashMap<Functional<?,?,?>, Double>();
//		for (OptimizerPlugin op : pluginsPanel.getActiveOptimizers()) {
//			Functional<VVertex, VEdge, VFace> fun = op.getFunctional(hds);
//			funs.add(fun);
//			coeffs.put(fun, pluginsPanel.getCoefficient(op));
//		}
//		int dim = 3*hds.numVertices();
		CombinedFunctional fun = createFunctional(hds);
//		CombinedFunctional fun = new CombinedFunctional(funs, coeffs, dim);
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
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		pluginsPanel = c.getPlugin(OptimizerPluginsPanel.class);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Optimization Panel", "Stefan Sechelmann");
	}
	
}
