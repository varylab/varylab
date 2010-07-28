package de.varylab.varylab.plugin.visualizers;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Color;
import de.jtem.halfedgetools.adapter.type.Label;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;

public class WeightsVisualizer extends VisualizerPlugin implements ActionListener, ChangeListener {

	
	private DecimalFormat
		format = new DecimalFormat("0.000");
	private SpinnerNumberModel
		placesModel = new SpinnerNumberModel(3, 0, 20, 1);
	private JSpinner	
		placesSpinner = new JSpinner(placesModel);
	private JCheckBox	
		showVertices = new JCheckBox("V", false),
		showEdges = new JCheckBox("E", false),
		showFaces = new JCheckBox("F", true),
		showLabels = new JCheckBox("Labels", false),
		showColors = new JCheckBox("Colors", true);
	private JPanel
		panel = new JPanel();
	private double
		minV = 0.0,
		minE = 0.0,
		minF = 0.0,
		maxV = 1.0,
		maxE = 1.0,
		maxF = 1.0,
		spanV = 1.0,
		spanE = 1.0,
		spanF = 1.0;
	
	
	public WeightsVisualizer() {
		panel.setLayout(new GridLayout(2, 3));
		panel.add(showVertices);
		panel.add(showEdges);
		panel.add(showFaces);
		panel.add(showColors);
		panel.add(showLabels);
		panel.add(new JLabel("Decimal Places"));
		panel.add(placesSpinner);
		
		showVertices.addActionListener(this);
		showEdges.addActionListener(this);
		showFaces.addActionListener(this);
		showColors.addActionListener(this);
		showLabels.addActionListener(this);
		placesSpinner.addChangeListener(this);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		updateContent();
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		String fs = "0.";
		for (int i = 0; i < placesModel.getNumber().intValue(); i++) {
			fs += "0";
		}
		format = new DecimalFormat(fs);
		updateContent();
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "showVertices", showVertices.isSelected());
		c.storeProperty(getClass(), "showEdges", showEdges.isSelected());
		c.storeProperty(getClass(), "showFaces", showFaces.isSelected());
		c.storeProperty(getClass(), "showLabels", showLabels.isSelected());
		c.storeProperty(getClass(), "showColors", showColors.isSelected());
		c.storeProperty(getClass(), "decimalPlaces", placesModel.getNumber().intValue());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		showVertices.setSelected(c.getProperty(getClass(), "showVertices", showVertices.isSelected()));
		showEdges.setSelected(c.getProperty(getClass(), "showEdges", showEdges.isSelected()));
		showFaces.setSelected(c.getProperty(getClass(), "showFaces", showFaces.isSelected()));
		showLabels.setSelected(c.getProperty(getClass(), "showLabels", showLabels.isSelected()));
		showColors.setSelected(c.getProperty(getClass(), "showColors", showColors.isSelected()));
		placesModel.setValue(c.getProperty(getClass(), "decimalPlaces", placesModel.getNumber().intValue()));
	}
	
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void initVisualization(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		// find max and min of all weights
		if (!(hds instanceof VHDS)) return;
		VHDS vhds = (VHDS)hds;
		minV = Double.MAX_VALUE;
		minE = Double.MAX_VALUE;
		minF = Double.MAX_VALUE;
		maxV = -Double.MAX_VALUE;
		maxE = -Double.MAX_VALUE;
		maxF = -Double.MAX_VALUE;
		for (VVertex v : vhds.getVertices()) {
			if (minV > v.getWeight()) {
				minV = v.getWeight();
			}
			if (maxV < v.getWeight()) {
				maxV = v.getWeight();
			}
		}
		for (VEdge e : vhds.getEdges()) {
			if (minE > e.getWeight()) {
				minE = e.getWeight();
			}
			if (maxE < e.getWeight()) {
				maxE = e.getWeight();
			}
		}
		for (VFace f : vhds.getFaces()) {
			if (minF > f.getWeight()) {
				minF = f.getWeight();
			}
			if (maxF < f.getWeight()) {
				maxF = f.getWeight();
			}
		}
		spanV = maxV - minV;
		spanE = maxE - minE;
		spanF = maxF - minF;
	}
	
	
	@Label
	private class WeightsLabelAdapter extends AbstractAdapter<String> {
	
		public WeightsLabelAdapter() {
			super(String.class, true, false);
		}
	
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			if (VVertex.class.isAssignableFrom(nodeClass)) {
				return showVertices.isSelected();
			}
			if (VEdge.class.isAssignableFrom(nodeClass)) {
				return showEdges.isSelected();
			}
			if (VFace.class.isAssignableFrom(nodeClass)) {
				return showFaces.isSelected();
			}
			return false;
		}
	
		@Override
		public double getPriority() {
			return 0;
		}
	
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>,
			N extends Node<V, E, F>
		> String get(N n, AdapterSet a) {
			double w = 1.0;
			if (n instanceof VVertex) {
				w = ((VVertex) n).getWeight();
			}
			if (n instanceof VEdge) {
				w = ((VEdge) n).getWeight();
			}
			if (n instanceof VFace) {
				w = ((VFace) n).getWeight();
			}
			return format.format(w);
		}
		
	}
	
	
	@Color
	private class WeightsColorAdapter extends AbstractAdapter<double[]> {
	
		private double[]
		    minColor = {0, 0, 1},
		    maxColor = {1, 1, 0};
		
		
		public WeightsColorAdapter() {
			super(double[].class, true, false);
		}
	
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			if (VVertex.class.isAssignableFrom(nodeClass)) {
				return showVertices.isSelected();
			}
			if (VEdge.class.isAssignableFrom(nodeClass)) {
				return showEdges.isSelected();
			}
			if (VFace.class.isAssignableFrom(nodeClass)) {
				return showFaces.isSelected();
			}
			return false;
		}
	
		@Override
		public double getPriority() {
			return 0;
		}
	
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>,
			N extends Node<V, E, F>
		> double[] get(N n, AdapterSet a) {
			double alpha = 1.0;
			if (n instanceof VVertex) {
				double w = ((VVertex) n).getWeight();
				alpha = w - minV;
				if (spanV == 0) {
					alpha = 1.0;
				} else {
					alpha /= spanV;
				}
			}
			if (n instanceof VEdge) {
				double w = ((VEdge) n).getWeight();
				alpha = w - minE;
				if (spanE == 0) {
					alpha = 1.0;
				} else {
					alpha /= spanE;
				}
			}
			if (n instanceof VFace) {
				double w = ((VFace) n).getWeight();
				alpha = w - minF;
				if (spanF == 0) {
					alpha = 1.0;
				} else {
					alpha /= spanF;
				}
			}
			return Rn.linearCombination(null, 1 - alpha, minColor, alpha, maxColor);
		}
		
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
	
	@Override
	public Set<? extends Adapter<?>> getAdapters() {
		Set<Adapter<?>> result = new HashSet<Adapter<?>>();
		if (showColors.isSelected()) {
			result.add(new WeightsColorAdapter());
		}
		if (showLabels.isSelected()) {
			result.add(new WeightsLabelAdapter());
		}
		return result;
	}
	
	
	@Override
	public String getName() {
		return "Node Weights";
	}

}
