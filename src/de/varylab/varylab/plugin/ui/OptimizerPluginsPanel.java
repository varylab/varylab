package de.varylab.varylab.plugin.ui;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import de.jreality.plugin.basic.View;
import de.jtem.halfedgetools.plugin.swing.IconCellRenderer;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.plugin.OptimizerPlugin;

public class OptimizerPluginsPanel extends ShrinkPanelPlugin implements ListSelectionListener {

	private Map<String, Double>
		coefficientMap = new HashMap<String, Double>();
	private List<OptimizerPlugin>
		optimizerPlugins = new LinkedList<OptimizerPlugin>();
	private JTable
		pluginTable = new JTable();
	private JScrollPane
		pluginScroller = new JScrollPane(pluginTable);
	private Set<String>
		activateSet = new HashSet<String>();
	private JPanel
		tablePanel = new JPanel(),
		pluginOptionsPanel = new JPanel();
	private JCheckBox
		normalizeEnergies = new JCheckBox("Normalize Energies", true); 
	
	public OptimizerPluginsPanel() {
		tablePanel.setLayout(new GridLayout());
		tablePanel.setBorder(BorderFactory.createTitledBorder("Optimizer Plugins"));
		tablePanel.add(pluginScroller);
		pluginTable.setBorder(BorderFactory.createEtchedBorder());
		pluginTable.getDefaultEditor(Boolean.class).addCellEditorListener(new PluginActivationListener());
		pluginTable.setRowHeight(22);
		pluginTable.getSelectionModel().addListSelectionListener(this);
		pluginTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		pluginTable.getTableHeader().setPreferredSize(new Dimension(100, 0));
		
		pluginOptionsPanel.setLayout(new GridLayout());
		pluginOptionsPanel.setPreferredSize(new Dimension(20, 100));
		pluginOptionsPanel.setMinimumSize(new Dimension(20, 100));
		pluginOptionsPanel.setBorder(BorderFactory.createTitledBorder("Plugin Options"));
		
		shrinkPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(2, 2, 2, 2);
		shrinkPanel.add(normalizeEnergies, c);
		c.weighty = 1.0;
		shrinkPanel.add(tablePanel, c);
		c.weighty = 0.0;
		shrinkPanel.add(pluginOptionsPanel, c);
		tablePanel.setPreferredSize(new Dimension(250, 200));
		tablePanel.setMinimumSize(new Dimension(250, 200));
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int row = pluginTable.getSelectedRow();
		if (pluginTable.getRowSorter() != null) {
			row = pluginTable.getRowSorter().convertRowIndexToModel(row);
		}
		if (row < 0 || row >= optimizerPlugins.size()) return;
		pluginOptionsPanel.removeAll();
		OptimizerPlugin p = optimizerPlugins.get(row);
		if (p.getOptionPanel() == null) {
			pluginOptionsPanel.add(new JLabel("No Options"));
			pluginOptionsPanel.updateUI();
			return;
		}
		pluginOptionsPanel.add(p.getOptionPanel());
		pluginOptionsPanel.updateUI();
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

	public double getCoefficient(OptimizerPlugin op) {
		if (!coefficientMap.containsKey(op.getName())) {
			coefficientMap.put(op.getName(), 1.0);
		}
		return coefficientMap.get(op.getName());
	}
	private void setCoefficient(OptimizerPlugin op, double coeff) {
		coefficientMap.put(op.getName(), coeff);
	}
	
	public boolean isActive(OptimizerPlugin op) {
		return activateSet.contains(op.getName());
	}
	
	private void setActive(OptimizerPlugin op, boolean active) {
		if (!active) {
			activateSet.remove(op.getName());
		} else {
			activateSet.add(op.getName());
		}
	}
	
	private void updatePluginTable() {
		pluginTable.setModel(new PluginTableModel());
		pluginTable.getColumnModel().getColumn(0).setMaxWidth(30);
		pluginTable.getColumnModel().getColumn(0).setCellRenderer(new IconCellRenderer());
		pluginTable.getColumnModel().getColumn(1).setMaxWidth(30);
		pluginTable.getColumnModel().getColumn(3).setMaxWidth(60);
		pluginTable.getColumnModel().getColumn(3).setCellEditor(new SpinnerCellEditor());
		pluginTable.getColumnModel().getColumn(3).setCellRenderer(new SpinnerCellEditor());
	}
	
	public void addOptimizerPlugin(OptimizerPlugin op) {
		optimizerPlugins.add(op);
		updatePluginTable();
	}
	
	public void removeOptimizerPlugin(OptimizerPlugin op) {
		optimizerPlugins.remove(op);
		pluginTable.revalidate();
		updatePluginTable();
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
	
	public List<OptimizerPlugin> getAllOptimizers() {
		return optimizerPlugins;
	}
	
	public List<OptimizerPlugin> getActiveOptimizers() {
		List<OptimizerPlugin> result = new LinkedList<OptimizerPlugin>();
		for (OptimizerPlugin p : getAllOptimizers()) {
			if (isActive(p)) {
				result.add(p);
			}
		}
		return result;
	}
	
	public boolean isNormalizeEnergies() {
		return normalizeEnergies.isSelected();
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

}
