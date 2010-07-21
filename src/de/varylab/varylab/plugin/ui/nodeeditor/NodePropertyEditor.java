package de.varylab.varylab.plugin.ui.nodeeditor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.jreality.plugin.basic.View;
import de.jtem.beans.InspectorPanel;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.SelectionAdapter;
import de.jtem.halfedgetools.plugin.SelectionInterface;
import de.jtem.halfedgetools.plugin.SelectionListener;
import de.jtem.halfedgetools.plugin.VisualizersManager;
import de.jtem.halfedgetools.util.NodeComparator;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.plugin.visualizers.WeightsVisualizer;

public class NodePropertyEditor extends ShrinkPanelPlugin implements ListSelectionListener, ChangeListener, ActionListener, SelectionListener {
	
	private HalfedgeInterface
		hif = null;
	private SelectionInterface
		sel = null;
	private WeightsVisualizer
		weightsVisualizer = null;
	private VisualizersManager
		visualizersManager = null;
	private boolean
		disableListeners = false;
	
	private Set<Node<?,?,?>>
		selectedNodes = new TreeSet<Node<?,?,?>>(new NodeComparator<Node<?,?,?>>());
	
	private JList
		selectedNodesList = new JList();
	private JScrollPane
		selectionScroller = new JScrollPane(selectedNodesList);
	private JComboBox
		adapterCombo = new JComboBox();
	private InspectorPanel
		inspector = new InspectorPanel(true);
	private JScrollPane
		ispectorScroller = new JScrollPane(inspector);
	
	
	public NodePropertyEditor() {
		shrinkPanel.setLayout(new GridBagLayout());
		shrinkPanel.setName("Node Properties");
		GridBagConstraints c1 = new GridBagConstraints();
		c1.insets = new Insets(1,1,1,1);
		c1.fill = GridBagConstraints.BOTH;
		c1.anchor = GridBagConstraints.WEST;
		c1.weightx = 1.0;
		c1.gridwidth = 1;
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets = new Insets(1,1,1,1);
		c2.fill = GridBagConstraints.BOTH;
		c2.anchor = GridBagConstraints.WEST;
		c2.weightx = 1.0;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		
		c2.weighty = 1.0;
		shrinkPanel.add(selectionScroller, c2);
		selectionScroller.setPreferredSize(new Dimension(10, 70));
		selectionScroller.setMinimumSize(new Dimension(10, 70));
		c2.weighty = 0.0;
		shrinkPanel.add(adapterCombo, c2);
		shrinkPanel.add(ispectorScroller, c2);
		
		inspector.addChangeListener(this);
		adapterCombo.addActionListener(this);
		selectedNodesList.getSelectionModel().addListSelectionListener(this);

		ispectorScroller.setPreferredSize(new Dimension(10, 100));
		ispectorScroller.setMinimumSize(new Dimension(10, 100));
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		sel = c.getPlugin(SelectionInterface.class);
		sel.addSelectionListener(this);
		weightsVisualizer = c.getPlugin(WeightsVisualizer.class);
		visualizersManager = c.getPlugin(VisualizersManager.class);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (adapterCombo == s) {
			List<Node<?,?,?>> listSelection = getSelectedNodes();
			if (listSelection.isEmpty()) return;
			Node<?,?,?> refNode = listSelection.iterator().next();
			@SuppressWarnings("unchecked")
			Adapter<Object> a = (Adapter<Object>)adapterCombo.getSelectedItem();
			Object o = a.get(refNode, hif.getAdapters());
			Object vc = new Object();
			if (o instanceof Double) {
				vc = new DoubleValueContainer((Double)o, listSelection, a, hif.getAdapters());
			}
			if (o instanceof double[]) {
				vc = new DoubleArrayValueContainer((double[])o, listSelection, a, hif.getAdapters());
			}
			if (o instanceof Integer) {
				vc = new IntegerValueContainer((Integer)o, listSelection, a, hif.getAdapters());
			}
			if (o instanceof Boolean) {
				vc = new BooleanValueContainer((Boolean)o, listSelection, a, hif.getAdapters());
			}
			inspector.setObject(vc);
		}
	}
	
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (selectedNodesList.getSelectedValue() == null) return;
		updateInspector();
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (disableListeners) return;
		if (selectedNodesList.getSelectedValue() == null) return;
		if (visualizersManager.isActive(weightsVisualizer)) {
			visualizersManager.updateContent();
		}
	}
	
	
	@Override
	public void selectionChanged(HalfedgeSelection s, SelectionInterface sif) {
		selectedNodes.clear();
		selectedNodes.addAll(s.getNodes());
		DefaultListModel model = new DefaultListModel();
		for (Node<?,?,?> n : selectedNodes) {
			model.addElement(n);
		}
		selectedNodesList.setModel(model);
		if (!selectedNodes.isEmpty()) {
			Node<?,?,?> n = selectedNodes.iterator().next();
			disableListeners = true;
			selectedNodesList.setSelectedValue(n, true);
			disableListeners = false;
		}
		updateInspector();
	}
	
	
	private void updateInspector() {
		AdapterSet aSet = hif.getAdapters();
		AdapterSet compSet = new AdapterSet();
		List<Node<?,?,?>> listSelection = getSelectedNodes();
		for (Adapter<?> a : aSet) {
			if (a instanceof SelectionAdapter) continue;
			boolean accept = true;
			for (Object n : listSelection) {
				accept &= a.canAccept(((Node<?,?,?>)n).getClass());
				accept &= a.isGetter() && a.isSetter();
			}
			if (accept) {
				compSet.add(a);
			}
		}
		DefaultComboBoxModel adapterModel = new DefaultComboBoxModel();
		for (Adapter<?> a : compSet) {
			adapterModel.addElement(a);
		}
		adapterCombo.setModel(adapterModel);
		adapterCombo.setSelectedIndex(0);
	}
	
	
	private List<Node<?,?,?>> getSelectedNodes() {
		Object[] listSelection = selectedNodesList.getSelectedValues();
		List<Node<?,?,?>> result = new LinkedList<Node<?,?,?>>();
		for (Object o : listSelection) {
			result.add((Node<?,?,?>)o);
		}
		return result;
	}
	
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Node Property Editor", "Varylab Group");
	}

}
