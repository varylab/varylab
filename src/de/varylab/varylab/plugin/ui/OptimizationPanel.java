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
import de.jreality.plugin.basic.View;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
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
		fixSelectionChecker = new JCheckBox("Fix Selection"),
		fixBoundaryChecker = new JCheckBox("Fix Boundary"),
		moveAlongBoundaryChecker = new JCheckBox("Allow Inner Boundary Movements"),
		fixXChecker = new JCheckBox("X"),
		fixYChecker = new JCheckBox("Y"),
		fixZChecker = new JCheckBox("Z");
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
		constraintsPanel.add(fixXChecker, gbc1);
		constraintsPanel.add(fixYChecker, gbc1);
		constraintsPanel.add(fixZChecker, gbc2);
		constraintsPanel.add(fixSelectionChecker, gbc1);
		constraintsPanel.add(fixBoundaryChecker, gbc2);
		constraintsPanel.add(moveAlongBoundaryChecker, gbc2);
		shrinkPanel.add(constraintsPanel, gbc2);
		
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
		List<Functional<VVertex, VEdge, VFace>> funs = new LinkedList<Functional<VVertex,VEdge,VFace>>();
		Map<Functional<?, ?, ?>, Double> coeffs = new HashMap<Functional<?,?,?>, Double>();
		for (OptimizerPlugin op : pluginsPanel.getActiveOptimizers()) {
			Functional<VVertex, VEdge, VFace> fun = op.getFunctional(hds);
			funs.add(fun);
			coeffs.put(fun, pluginsPanel.getCoefficient(op));
		}
		
		int dim = hds.numVertices() * 3;
		CombinedFunctional fun = new CombinedFunctional(funs, coeffs, dim);
		
		DenseVector u = new DenseVector(dim);
		for (VVertex v : hds.getVertices()) {
			u.set(v.getIndex() * 3 + 0, v.position[0]);
			u.set(v.getIndex() * 3 + 1, v.position[1]);
			u.set(v.getIndex() * 3 + 2, v.position[2]);
		}
		
		Set<VVertex> fixedVerts = hif.getSelection().getVertices(hif.get(new VHDS()));
		
		FixingConstraint fixConstraint = new FixingConstraint(
			fixedVerts,
			fixSelectionChecker.isSelected(),
			fixBoundaryChecker.isSelected(), 
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
		
		Vec x = new Vec(dim);
		for (VVertex v : hds.getVertices()) {
			x.setValue(v.getIndex() * 3 + 0, v.position[0], InsertMode.INSERT_VALUES);
			x.setValue(v.getIndex() * 3 + 1, v.position[1], InsertMode.INSERT_VALUES);
			x.setValue(v.getIndex() * 3 + 2, v.position[2], InsertMode.INSERT_VALUES);
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
		for (VVertex v : hds.getVertices()) {
			int i = v.getIndex() * 3;
			v.position[0] = x.getValue(i + 0);
			v.position[1] = x.getValue(i + 1);
			v.position[2] = x.getValue(i + 2);
		}
		HalfedgeSelection hes = hif.getSelection();
		hif.set(hds, new AdapterSet());
		hif.setSelection(hes);
	}
	
	private void evaluate() {
		VHDS hds = hif.get(new VHDS());
		List<Functional<VVertex, VEdge, VFace>> funs = new LinkedList<Functional<VVertex,VEdge,VFace>>();
		Map<Functional<?, ?, ?>, Double> coeffs = new HashMap<Functional<?,?,?>, Double>();
		for (OptimizerPlugin op : pluginsPanel.getActiveOptimizers()) {
			Functional<VVertex, VEdge, VFace> fun = op.getFunctional(hds);
			funs.add(fun);
			coeffs.put(fun, pluginsPanel.getCoefficient(op));
		}
		
		int dim = hds.numVertices() * 3;
		CombinedFunctional fun = new CombinedFunctional(funs, coeffs, dim);
		Energy E = new MyEnergy();
		
		DenseVector u = new DenseVector(dim);
		for (VVertex v : hds.getVertices()) {
			u.set(v.getIndex() * 3 + 0, v.position[0]);
			u.set(v.getIndex() * 3 + 1, v.position[1]);
			u.set(v.getIndex() * 3 + 2, v.position[2]);
		}
		DomainValue x = new MyDomainValue(u);
		fun.evaluate(hds, x, E, null, null);
		double energy = E.get();
		System.out.println("Energy:" + energy +"(" + energy/(2*Math.PI) + "·2π)");
	}
	
	
	public static int[] getPETScNonZeros(VHDS hds, CombinedFunctional fun){
		int [][] sparseStucture = fun.getNonZeroPattern(hds);
		int [] nnz = new int[sparseStucture.length];
		for(int i = 0; i < nnz.length; i++){
			nnz[i] = sparseStucture[i].length;
		}
		return nnz;
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
		List<Functional<VVertex, VEdge, VFace>> funs = new LinkedList<Functional<VVertex,VEdge,VFace>>();
		Map<Functional<?, ?, ?>, Double> coeffs = new HashMap<Functional<?,?,?>, Double>();
		for (OptimizerPlugin op : pluginsPanel.getActiveOptimizers()) {
			Functional<VVertex, VEdge, VFace> fun = op.getFunctional(hds);
			funs.add(fun);
			coeffs.put(fun, pluginsPanel.getCoefficient(op));
		}
		int dim = 3*hds.numVertices();
		CombinedFunctional fun = new CombinedFunctional(funs, coeffs, dim);
		Set<VVertex> fixedVerts = hif.getSelection().getVertices(hif.get(new VHDS()));
		
		FixingConstraint fixConstraint = new FixingConstraint(
			fixedVerts,
			fixSelectionChecker.isSelected(),
			fixBoundaryChecker.isSelected(), 
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
		c.storeProperty(getClass(), "fixSelection", fixSelectionChecker.isSelected());
		c.storeProperty(getClass(), "fixBoundary", fixBoundaryChecker.isSelected());
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
		fixSelectionChecker.setSelected(c.getProperty(getClass(), "fixSelection", fixSelectionChecker.isSelected()));
		fixBoundaryChecker.setSelected(c.getProperty(getClass(), "fixBoundary", fixBoundaryChecker.isSelected()));
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
