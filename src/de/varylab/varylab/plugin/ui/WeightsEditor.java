package de.varylab.varylab.plugin.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.jreality.plugin.basic.View;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Weight;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.SelectionInterface;
import de.jtem.halfedgetools.plugin.SelectionListener;
import de.jtem.halfedgetools.plugin.VisualizersManager;
import de.jtem.halfedgetools.util.NodeComparator;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.plugin.visualizers.WeightsVisualizer;

public class WeightsEditor extends ShrinkPanelPlugin implements ListSelectionListener, ChangeListener, ActionListener, SelectionListener {
	
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
	
	private SpinnerNumberModel
		weightModel = new SpinnerNumberModel(1.0, -1000.0, 1000.0, 0.1),
		smoothLambdaModel = new SpinnerNumberModel(0.5, 0.0, 1.0, 0.01);
	private JSpinner
		weightSpinner = new JSpinner(weightModel),
		smoothLambdaSpinner = new JSpinner(smoothLambdaModel);
	private JList
		selectedNodesList = new JList();
	private JScrollPane
		selectionScroller = new JScrollPane(selectedNodesList);
	private JButton
		smoothButton = new JButton("Smooth Weights");
	
	
	public WeightsEditor() {
		shrinkPanel.setLayout(new GridBagLayout());
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
		
		shrinkPanel.add(selectionScroller, c2);
		selectionScroller.setPreferredSize(new Dimension(10, 70));
		selectionScroller.setMinimumSize(new Dimension(10, 70));
		shrinkPanel.add(new JLabel("Weight"), c1);
		shrinkPanel.add(weightSpinner, c2);
		shrinkPanel.add(new JSeparator(), c2);
		shrinkPanel.add(smoothButton, c1);
		shrinkPanel.add(smoothLambdaSpinner);
		
		smoothButton.addActionListener(this);
		weightSpinner.addChangeListener(this);
		selectedNodesList.getSelectionModel().addListSelectionListener(this);
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
		if (smoothButton == s) {
			double lambda = smoothLambdaModel.getNumber().doubleValue();
			Map<VVertex, Double> difMap = new HashMap<VVertex, Double>();
			VHDS hds = hif.get(new VHDS());
			for (VVertex v : hds.getVertices()) {
				// :TODO implement heat diffusion?
			}
		}
	}
	
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (selectedNodesList.getSelectedValue() == null) return;
		Node<?,?,?> n = (Node<?,?,?>)selectedNodesList.getSelectedValue();
		AdapterSet a = hif.getAdapters();
		Double w = a.get(Weight.class, n, Double.class);
		if (w != null) {
			weightModel.setValue(w);
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (disableListeners) return;
		if (selectedNodesList.getSelectedValue() == null) return;
		AdapterSet a = hif.getAdapters();
		Object[] listSelection = selectedNodesList.getSelectedValues();
//		int[] selIndices = selectedNodesList.getSelectedIndices();
		for (Object s : listSelection) {
			Double w = weightModel.getNumber().doubleValue();
			a.set(Weight.class, (Node<?,?,?>)s, w);
		}
		if (visualizersManager.isActive(weightsVisualizer)) {
//			HalfedgeSelection s = new HalfedgeSelection(selectedNodes);			
			visualizersManager.updateContent();
//			sel.setSelection(s);
//			selectedNodesList.setSelectedIndices(selIndices);
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
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

}
