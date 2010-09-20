package de.varylab.varylab.plugin.visualizers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
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
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Color;
import de.jtem.halfedgetools.adapter.type.Label;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;

public class ConicalityVisualizer extends VisualizerPlugin implements ChangeListener, ActionListener {

		private DecimalFormat
			format = new DecimalFormat("0.00");
		private SpinnerNumberModel
			placesModel = new SpinnerNumberModel(2, 0, 20, 1);
		private JSpinner	
			placesSpinner = new JSpinner(placesModel);
		private JCheckBox	
			showLabels = new JCheckBox("Labels", false),
			showColors = new JCheckBox("Colors", true);
		private JPanel
			panel = new JPanel();
		private double
			maxEnergy = 0.0;
		
		
		public ConicalityVisualizer() {
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
			manager.updateContent();
		}
		
		
		@Override
		public  < 
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>,
			HDS extends HalfEdgeDataStructure<V, E, F>
		> void initVisualization(HDS hds, AdapterSet a, HalfedgeInterface hif) {
			maxEnergy = getMaxEnergy(hds, a);
		}

		public static <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>,
			HDS extends HalfEdgeDataStructure<V, E, F>
		> double getMaxEnergy(HDS hds, AdapterSet a){
			double maxUneven = 0.0;
			for (V v: hds.getVertices()) {
				double vol = getEnergy(v, a);
				if (vol > maxUneven)
					maxUneven = vol;
			}
			return maxUneven;
		}


		public static <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>,
			HDS extends HalfEdgeDataStructure<V, E, F>
		> double getMeanEnergy(HDS hds, AdapterSet a){
			double meanVol = 0.0;
			int count = 0;
			for (V v : hds.getVertices()) {
				meanVol += getEnergy(v, a);
				count++;
			}
			return meanVol / count;
		}


		public static <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> double getEnergy(V v, AdapterSet ad) {
			List<V> neighbors = HalfEdgeUtils.neighboringVertices(v);
			if (neighbors.size() != 4) {
				return 0.0;
			}
			if (HalfEdgeUtils.isBoundaryVertex(v)) {
				return 0.0;
			}
			V v1 = neighbors.get(0);
			V v2 = neighbors.get(1);
			V v3 = neighbors.get(2);
			V v4 = neighbors.get(3);
			double[] p = ad.get(Position.class, v, double[].class);
			double[] p1 = ad.get(Position.class, v1, double[].class);
			double[] p2 = ad.get(Position.class, v2, double[].class);
			double[] p3 = ad.get(Position.class, v3, double[].class);
			double[] p4 = ad.get(Position.class, v4, double[].class);
			double[] vec1 = Rn.subtract(null, p1, p);
			double[] vec2 = Rn.subtract(null, p2, p);
			double[] vec3 = Rn.subtract(null, p3, p);
			double[] vec4 = Rn.subtract(null, p4, p);
			double a1 = Rn.euclideanAngle(vec1, vec2);
			double a2 = Rn.euclideanAngle(vec2, vec3);
			double a3 = Rn.euclideanAngle(vec3, vec4);
			double a4 = Rn.euclideanAngle(vec4, vec1);
			return Math.abs((a1 + a3) - (a2 + a4));
		}
		
		
		@Label
		private class ConicalityLabelAdapter extends AbstractAdapter<String> {

			public ConicalityLabelAdapter() {
				super(String.class, true, false);
			}

			@Override
			public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
				return Vertex.class.isAssignableFrom(nodeClass);
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
			> String getV(V v, AdapterSet a) {
				if(HalfEdgeUtils.neighboringVertices(v).size() != 4) {
					return "";
				} else {
					return format.format(getEnergy(v, a));
				}
			}

		}



		@Color
		private class ConicalityColorAdapter extends AbstractAdapter<double[]> {

			public ConicalityColorAdapter() {
				super(double[].class, true, false);
			}

			@Override
			public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
				return Vertex.class.isAssignableFrom(nodeClass);
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
			> double[] getV(V v, AdapterSet a) {
				if(maxEnergy < 1E-8) {
					return new double[]{0,1,0};
				} 
				double col = getEnergy(v, a) / maxEnergy;
				return new double[]{col, 1 - col, 0};
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
				result.add(new ConicalityColorAdapter());
			}
			if (showLabels.isSelected()) {
				result.add(new ConicalityLabelAdapter());
			}
			return result;
		}


		@Override
		public String getName() {
			return "Conicality";
		}
}
