package de.varylab.varylab.plugin.meshoptimizer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import de.jreality.plugin.basic.View;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.mtjoptimization.NotConvergentException;
import de.varylab.mtjoptimization.newton.NewtonOptimizer;
import de.varylab.mtjoptimization.newton.NewtonOptimizer.Solver;
import de.varylab.mtjoptimization.stepcontrol.ArmijoStepController;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.CombinedFunctional;
import de.varylab.varylab.math.CombinedOptimizable;
import de.varylab.varylab.math.CombinedOptimizableNM;
import de.varylab.varylab.math.ConjugateGradient;
import de.varylab.varylab.math.FixingConstraint;
import de.varylab.varylab.plugin.OptimizerPlugin;
import de.varylab.varylab.ui.image.ImageHook;

public class OptimizationManager extends ShrinkPanelPlugin implements ActionListener {
	
	private HalfedgeInterfacePlugin<VVertex, VEdge, VFace, VHDS> 
		hif = null;
	private List<OptimizerPlugin>
		optimizerPlugins = new LinkedList<OptimizerPlugin>();
	private JTable
		pluginTable = new JTable();
	private Set<String>
		activateSet = new HashSet<String>();
	private Map<String, Double>
		coefficientMap = new HashMap<String, Double>();
	private JPanel
		optimizationPanel = new JPanel(),
		constraintsPanel = new JPanel(),
		tablePanel = new JPanel(),
		pluginOptionsPanel = new JPanel(),
		optionsPanel = new JPanel();
	private JButton
		optimizeButton = new JButton("Optimize", ImageHook.getIcon("surface.png"));
	private JCheckBox
		fixBoundaryChecker = new JCheckBox("Fix Boundary"),
		fixXChecker = new JCheckBox("X"),
		fixYChecker = new JCheckBox("Y"),
		fixZChecker = new JCheckBox("Z");
	private SpinnerNumberModel
		accuracyModel = new SpinnerNumberModel(-8, -20, -1, -1),
		maxIterationsModel = new SpinnerNumberModel(150, 1, 10000, 1);
	private JSpinner
		accuracySpinner = new JSpinner(accuracyModel),
		maxIterationSpinner = new JSpinner(maxIterationsModel);
		
	
	public OptimizationManager() {
		setInitialPosition(SHRINKER_TOP);
		shrinkPanel.setLayout(new GridLayout(1, 3));
		
		tablePanel.setLayout(new GridLayout());
		tablePanel.setBorder(BorderFactory.createTitledBorder("Optimizer Plugins"));
		tablePanel.add(pluginTable);
		pluginTable.setBorder(BorderFactory.createEtchedBorder());
		
		pluginOptionsPanel.setLayout(new GridLayout());
		pluginOptionsPanel.setBorder(BorderFactory.createTitledBorder("Plugin Options"));
		
		optionsPanel.setLayout(new GridBagLayout());
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
		constraintsPanel.add(fixBoundaryChecker, gbc1);
		constraintsPanel.add(fixXChecker, gbc1);
		constraintsPanel.add(fixYChecker, gbc1);
		constraintsPanel.add(fixZChecker, gbc2);
		optionsPanel.add(constraintsPanel, gbc2);
		
		optimizationPanel.setLayout(new GridBagLayout());
		optimizationPanel.setBorder(BorderFactory.createTitledBorder("Optimization"));
		optimizationPanel.add(new JLabel("Tolerance"), gbc1);
		optimizationPanel.add(accuracySpinner, gbc2);
		optimizationPanel.add(new JLabel("Iterations"), gbc1);
		optimizationPanel.add(maxIterationSpinner, gbc2);
		optionsPanel.add(optimizationPanel, gbc2);
		
		gbc2.weighty = 1.0;
		optionsPanel.add(new JPanel(), gbc2);
		gbc2.weighty = 0.0;
		optionsPanel.add(optimizeButton, gbc2);
		
		shrinkPanel.add(tablePanel);
		shrinkPanel.add(pluginOptionsPanel);
		shrinkPanel.add(optionsPanel);
		pluginTable.setPreferredSize(new Dimension(10, 200));
		
		optimizeButton.addActionListener(this);
	}
	
	
	private void optimize() {
		VHDS hds = hif.getCachedHalfEdgeDataStructure();
		List<Functional<VVertex, VEdge, VFace>> funs = new LinkedList<Functional<VVertex,VEdge,VFace>>();
		Map<Functional<?, ?, ?>, Double> coeffs = new HashMap<Functional<?,?,?>, Double>();
		for (OptimizerPlugin op : optimizerPlugins) {
			if (!isActive(op)) continue;
			Functional<VVertex, VEdge, VFace> fun = op.createFunctional(hds);
			funs.add(fun);
			coeffs.put(fun, getCoefficient(op));
		}
		
		int dim = hds.numVertices() * 3;
		CombinedFunctional fun = new CombinedFunctional(funs, coeffs, dim);
		
		DenseVector u = new DenseVector(dim);
		for (VVertex v : hds.getVertices()) {
			u.set(v.getIndex() * 3 + 0, v.position[0]);
			u.set(v.getIndex() * 3 + 1, v.position[1]);
			u.set(v.getIndex() * 3 + 2, v.position[2]);
		}
		
		FixingConstraint fixConstraint = new FixingConstraint(
			fixBoundaryChecker.isSelected(), 
			fixXChecker.isSelected(), 
			fixYChecker.isSelected(), 
			fixZChecker.isSelected()
		);
		double acc = Math.pow(10, accuracyModel.getNumber().intValue());
		int maxIter = maxIterationsModel.getNumber().intValue();
		
		if (fun.hasHessian()) {
			CombinedOptimizable opt = new CombinedOptimizable(hds, fun);
			opt.addConstraint(fixConstraint);
			Matrix H = new CompRowMatrix(dim, dim, fun.getNonZeroPattern(hds));
			NewtonOptimizer optimizer = new NewtonOptimizer(H);
			optimizer.setStepController(new ArmijoStepController());
			optimizer.setSolver(Solver.CG);
			optimizer.setError(acc);
			optimizer.setMaxIterations(maxIter);
			try {
				optimizer.minimize(u, opt);
			} catch (NotConvergentException e) {
				e.printStackTrace();
				return;
			}
			for (VVertex v : hds.getVertices()) {
				int i = v.getIndex() * 3;
				v.position[0] = u.get(i + 0);
				v.position[1] = u.get(i + 1);
				v.position[2] = u.get(i + 2);
			}
		} else {
			CombinedOptimizableNM opt = new CombinedOptimizableNM(hds, fun);
			opt.addConstraint(fixConstraint);
			double[] uArr = u.getData();
			ConjugateGradient.setITMAX(maxIter);
			ConjugateGradient.setUseDBrent(true);
			ConjugateGradient.search(uArr, acc, opt);
			for (VVertex v : hds.getVertices()) {
				int i = v.getIndex() * 3;
				v.position[0] = uArr[i + 0];
				v.position[1] = uArr[i + 1];
				v.position[2] = uArr[i + 2];
			}
		}
		hif.updateHalfedgeContentAndActiveGeometry(hds);
	}
	
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (optimizeButton == s) {
			optimize();
		}
	}
	
	
	
	private class PluginTableModel extends DefaultTableModel {

		private static final long 
			serialVersionUID = 1L;
		
		@Override
		public int getRowCount() {
			return optimizerPlugins.size();
		}
		
		@Override
		public int getColumnCount() {
			return 4;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0: return Icon.class;
				case 1: return Boolean.class;
				case 2: return OptimizerPlugin.class;
				case 3: return Double.class;
				default: return String.class;
			}
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if (row < 0 || row >= optimizerPlugins.size()) {
				return "-";
			}
			OptimizerPlugin op = optimizerPlugins.get(row);
			Object value = null;
			switch (column) {
				case 0:
					return op.getPluginInfo().icon;
				case 1: 
					return activateSet.contains(op.getName());
				case 2:
					value = op;
					break;
				case 3:
					return getCoefficient(op);
				default: 
					value = "-";
					break;
			}
			return value;
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			switch (column) {
				case 1:
				case 3:
					return true;
				default: 
					return false;
			}
		}
		
		
	}
	
	private class SpinnerCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, ChangeListener {

		private static final long 
			serialVersionUID = 1L;
		private SpinnerNumberModel
			model = new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.1);
		private JSpinner
			spinner = new JSpinner(model);
		private int
			activeRow = -1;
		
		public SpinnerCellEditor() {
			spinner.addChangeListener(this);
		}
		
		@Override
		public Object getCellEditorValue() {
			return model.getNumber();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			activeRow = row;
			model.setValue(value);
			return spinner;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			activeRow = row;
			model.setValue(value);
			return spinner;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			OptimizerPlugin op = optimizerPlugins.get(activeRow);
			setCoefficient(op, model.getNumber().doubleValue());
		}

	}
	
	
	private class PluginActivationListener implements CellEditorListener {

		@Override
		public void editingCanceled(ChangeEvent e) {
		}

		@Override
		public void editingStopped(ChangeEvent e) {
			int row = pluginTable.getSelectedRow();
			if (pluginTable.getRowSorter() != null) {
				row = pluginTable.getRowSorter().convertRowIndexToModel(row);
			}
			OptimizerPlugin op = optimizerPlugins.get(row);
			setActive(op, !isActive(op));
		}
		
	}
	
	
	private double getCoefficient(OptimizerPlugin op) {
		if (!coefficientMap.containsKey(op.getName())) {
			coefficientMap.put(op.getName(), 1.0);
		}
		return coefficientMap.get(op.getName());
	}
	private void setCoefficient(OptimizerPlugin op, double coeff) {
		coefficientMap.put(op.getName(), coeff);
	}
	
	private boolean isActive(OptimizerPlugin op) {
		return activateSet.contains(op.getName());
	}
	
	private void setActive(OptimizerPlugin op, boolean active) {
		if (!active) {
			activateSet.remove(op.getName());
		} else {
			activateSet.add(op.getName());
		}
	}
	
	
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterfacePlugin.class);
		optimizerPlugins = c.getPlugins(OptimizerPlugin.class);
		pluginTable.setModel(new PluginTableModel());
		pluginTable.getColumnModel().getColumn(0).setMaxWidth(30);
		pluginTable.getColumnModel().getColumn(1).setMaxWidth(30);
		pluginTable.getColumnModel().getColumn(3).setMaxWidth(60);
		pluginTable.getColumnModel().getColumn(3).setCellEditor(new SpinnerCellEditor());
		pluginTable.getColumnModel().getColumn(3).setCellRenderer(new SpinnerCellEditor());
		pluginTable.getDefaultEditor(Boolean.class).addCellEditorListener(new PluginActivationListener());
		pluginTable.setRowHeight(22);
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "activationSet", activateSet);
		c.storeProperty(getClass(), "coefficientsMap", coefficientMap);
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		activateSet = c.getProperty(getClass(), "activationSet", activateSet);
		coefficientMap = c.getProperty(getClass(), "coefficientsMap", coefficientMap);
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Mesh Optimizer", "Stefan Sechelmann");
	}
	
}
