package de.varylab.varylab.plugin.visualizers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Color;
import de.jtem.halfedgetools.adapter.type.Label;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;

public class CircularityVisualizer extends VisualizerPlugin implements ActionListener, ChangeListener {

	private DecimalFormat
		format = new DecimalFormat("0.000");
	private SpinnerNumberModel
		placesModel = new SpinnerNumberModel(3, 0, 20, 1);	
	private JSpinner	
		placesSpinner = new JSpinner(placesModel);
	private JCheckBox	
		showLabels = new JCheckBox("Labels", false),
		showColors = new JCheckBox("Colors", true);
	private JPanel
		panel = new JPanel();
	private double 
		maxUncircularity = 0.0;
	
	public CircularityVisualizer() {
		panel.setLayout(new GridBagLayout());
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
		
		panel.add(showColors, gbc1);
		panel.add(showLabels, gbc2);
		panel.add(new JLabel("Decimal Places"), gbc1);
		panel.add(placesSpinner, gbc2);
		
		showColors.addActionListener(this);
		showLabels.addActionListener(this);
		placesSpinner.addChangeListener(this);
	}
	
	@Override
	public  < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void initVisualization(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		maxUncircularity = getMaxUncircularity(hds, a);
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
	public void actionPerformed(ActionEvent e) {
		updateContent();
	}
	
	@Label
	private class CircularityLabelAdapter extends AbstractAdapter<String> {
		
		public CircularityLabelAdapter() {
			super(String.class, true, false);
		}

		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Face.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public double getPriority() {
			return 0;
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> String getF(F f, AdapterSet a) {
			double result = getCircularity(f, a);
			return format.format(result);
		}
	}

	@Color
	private class CircularityColorAdapter extends AbstractAdapter<double[]> {
		
		private final double[]
		    colorGreen = {0, 1, 0};
		
		public CircularityColorAdapter() {
			super(double[].class, true, false);
		}

		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Face.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public double getPriority() {
			return 0;
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> double[] getF(F f, AdapterSet a) {
			if (maxUncircularity == 0) {
				return colorGreen;
			}
			double circ = getCircularity(f, a);
			if(circ == -1) {
				return new double[]{0,0,1.0};
			} else {
				double col = circ / maxUncircularity;
				return new double[] {col, 1 - col, 0};
			}
		}
		
	}
	
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>double getCircularity(F f, AdapterSet a) {
		double result = 0.0;
		double[]
		       v1 = new double[3], 
		       v2 = new double[3],
		       v3 = new double[3],
		       v4 = new double[3];
		ArrayList<V> bdVerts = new ArrayList<V>(HalfEdgeUtils.boundaryVertices(f));
		if(bdVerts.size() != 4) {
			return -1;
		} else {
			v1 = a.get(Position.class, bdVerts.get(0), double[].class);
			v2 = a.get(Position.class, bdVerts.get(1), double[].class);
			v3 = a.get(Position.class, bdVerts.get(2), double[].class);
			v4 = a.get(Position.class, bdVerts.get(3), double[].class);
			double 
				alpha1 = FunctionalUtils.angle(v4, v1, v2),
				alpha2 = FunctionalUtils.angle(v1, v2, v3),
				alpha3 = FunctionalUtils.angle(v2, v3, v4),
				alpha4 = FunctionalUtils.angle(v3, v4, v1);
			result += (Math.PI-alpha1-alpha3)*(Math.PI-alpha1-alpha3);
			result += (Math.PI-alpha2-alpha4)*(Math.PI-alpha2-alpha4);
		}
		return result;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double getMaxUncircularity(HDS hds, AdapterSet a){
		double maxUncirc = 0.0;
		for (F f : hds.getFaces()) {
			double circ = getCircularity(f, a);
			if (circ > maxUncirc)
				maxUncirc = circ;
		}
		return maxUncirc;
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
	@Override
	public String getName() {
		return "Circularity Visualizer";
	}

	@Override
	public AdapterSet getAdapters() {
		AdapterSet result = new AdapterSet();
		if (showColors.isSelected()) {
			result.add(new CircularityColorAdapter());
		}
		if (showLabels.isSelected()) {
			result.add(new CircularityLabelAdapter());
		}
		return result;
	}
}
