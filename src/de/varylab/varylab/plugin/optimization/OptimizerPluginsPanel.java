package de.varylab.varylab.plugin.optimization;

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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
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
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class OptimizerPluginsPanel extends ShrinkPanelPlugin implements ListSelectionListener {

	private Map<String, Double>
		coefficientMap = new HashMap<String, Double>();
	private List<VarylabOptimizerPlugin>
		optimizerPlugins = new LinkedList<VarylabOptimizerPlugin>();
	private JTable
		pluginTable = new JTable();
	private JPanel
		tablePanel = new JPanel(),
		pluginOptionsPanel = new JPanel();
	private JScrollPane
		optionScroller = new JScrollPane(pluginOptionsPanel),
		pluginScroller = new JScrollPane(pluginTable);
	private Set<String>
		activateSet = new HashSet<String>();
	private JCheckBox
		normalizeEnergies = new JCheckBox("Normalize Energies", true); 
	private IconCellRenderer
		iconCellRenderer = new IconCellRenderer();
	private SpinnerCellEditor
		spinnerCellEditor = new SpinnerCellEditor(),
		spinnerCellRenderer = new SpinnerCellEditor();
	
	public OptimizerPluginsPanel() {
		setInitialPosition(SHRINKER_RIGHT);
		shrinkPanel.setTitle("Optimizer Plugins");
		tablePanel.setLayout(new GridLayout());
		tablePanel.setBorder(BorderFactory.createTitledBorder("Optimizer Plugins"));
		tablePanel.add(pluginScroller);
		pluginTable.setBorder(BorderFactory.createEtchedBorder());
		pluginTable.setRowHeight(22);
		pluginTable.getSelectionModel().addListSelectionListener(this);
		pluginTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		pluginTable.getTableHeader().setPreferredSize(new Dimension(100, 0));
		
		pluginOptionsPanel.setLayout(new GridLayout());
		pluginOptionsPanel.setBorder(BorderFactory.createTitledBorder("Plugin Options"));
		optionScroller.setPreferredSize(new Dimension(20, 150));
		
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
		shrinkPanel.add(optionScroller, c);
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
		VarylabOptimizerPlugin p = optimizerPlugins.get(row);
		if (p.getOptionPanel() == null) {
			pluginOptionsPanel.add(new JLabel("No Options"));
		} else {
			pluginOptionsPanel.add(p.getOptionPanel());
		}
		pluginOptionsPanel.revalidate();
		pluginOptionsPanel.repaint();
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
			return 3;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0: return Boolean.class;
				case 1: return VarylabOptimizerPlugin.class;
				case 2: return Double.class;
				default: return String.class;
			}
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if (row < 0 || row >= optimizerPlugins.size()) {
				return "-";
			}
			VarylabOptimizerPlugin op = optimizerPlugins.get(row);
			Object value = null;
			switch (column) {
				case 0: 
					return activateSet.contains(op.getName());
				case 1:
					value = op;
					break;
				case 2:
					return getCoefficient(op);
				default: 
					value = "-";
					break;
			}
			return value;
		}
		
		@Override
		public void setValueAt(Object aValue, int row, int column) {
			if (column == 0) {
				VarylabOptimizerPlugin op = optimizerPlugins.get(row);
				setActive(op, (boolean)aValue);
			}
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			switch (column) {
				case 0:
				case 2:
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
			TableCellRenderer defaultRenderer = table.getDefaultRenderer(String.class);
			Component c = defaultRenderer.getTableCellRendererComponent(table, "", isSelected, true, row, column);
			spinner.setOpaque(false);
			spinner.setBackground(c.getBackground());
			spinner.setForeground(c.getForeground());
			SwingUtilities.updateComponentTreeUI(spinner);
			return spinner;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			activeRow = row;
			model.setValue(value);
			TableCellRenderer defaultRenderer = table.getDefaultRenderer(String.class);
			Component c = defaultRenderer.getTableCellRendererComponent(table, "", isSelected, true, row, column);
			spinner.setOpaque(false);
			spinner.setBackground(c.getBackground());
			spinner.setForeground(c.getForeground());
			SwingUtilities.updateComponentTreeUI(spinner);
			return spinner;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			VarylabOptimizerPlugin op = optimizerPlugins.get(activeRow);
			setCoefficient(op, model.getNumber().doubleValue());
		}

		public JSpinner getSpinner() {
			return spinner;
		}
		
	}
	
	public double getCoefficient(VarylabOptimizerPlugin op) {
		if (!coefficientMap.containsKey(op.getName())) {
			coefficientMap.put(op.getName(), 1.0);
		}
		return coefficientMap.get(op.getName());
	}
	private void setCoefficient(VarylabOptimizerPlugin op, double coeff) {
		coefficientMap.put(op.getName(), coeff);
	}
	
	public boolean isActive(VarylabOptimizerPlugin op) {
		return activateSet.contains(op.getName());
	}
	
	private void setActive(VarylabOptimizerPlugin op, boolean active) {
		if (!active) {
			activateSet.remove(op.getName());
		} else {
			activateSet.add(op.getName());
		}
	}
	
	private void updatePluginTable() {
		pluginTable.setModel(new PluginTableModel());
		pluginTable.getColumnModel().getColumn(0).setMaxWidth(30);
		pluginTable.getColumnModel().getColumn(2).setMaxWidth(60);
		pluginTable.getColumnModel().getColumn(2).setCellEditor(spinnerCellEditor);
		pluginTable.getColumnModel().getColumn(2).setCellRenderer(spinnerCellRenderer);
	}
	
	public void addOptimizerPlugin(VarylabOptimizerPlugin op) {
		optimizerPlugins.add(op);
		updatePluginTable();
	}
	
	public void removeOptimizerPlugin(VarylabOptimizerPlugin op) {
		optimizerPlugins.remove(op);
		pluginTable.revalidate();
		updatePluginTable();
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		List<VarylabOptimizerPlugin> plugins = c.getPlugins(VarylabOptimizerPlugin.class);
		for (VarylabOptimizerPlugin p : plugins) {
			addOptimizerPlugin(p);
		}
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
	
	public List<VarylabOptimizerPlugin> getAllOptimizers() {
		return optimizerPlugins;
	}
	
	public List<VarylabOptimizerPlugin> getActiveOptimizers() {
		List<VarylabOptimizerPlugin> result = new LinkedList<VarylabOptimizerPlugin>();
		for (VarylabOptimizerPlugin p : getAllOptimizers()) {
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
	
	@Override
	public void mainUIChanged(String uiClass) {
		super.mainUIChanged(uiClass);
		SwingUtilities.updateComponentTreeUI(iconCellRenderer);
		SwingUtilities.updateComponentTreeUI(spinnerCellEditor.getSpinner());
		SwingUtilities.updateComponentTreeUI(spinnerCellRenderer.getSpinner());
	}

}
