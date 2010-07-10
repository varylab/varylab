package de.varylab.varylab.plugin.visualizers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Collection;
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
import de.varylab.varylab.math.CollectionUtility;

public class StarPlanarityVisualizer extends VisualizerPlugin implements ChangeListener, ActionListener {

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
			maxVolume = 0.0;
		
		
		public StarPlanarityVisualizer() {
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
			maxVolume = getMaxVolume(hds, a);
		}

		public static <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>,
			HDS extends HalfEdgeDataStructure<V, E, F>
		> double getMaxVolume(HDS hds, AdapterSet a){
			double maxUneven = 0.0;
			for (V v: hds.getVertices()) {
				double vol = getVolume(v, a);
				if (vol > maxUneven)
					maxUneven = vol;
			}
			return maxUneven;
		}


//		public static <
//			V extends Vertex<V, E, F>,
//			E extends Edge<V, E, F>,
//			F extends Face<V, E, F>,
//			HDS extends HalfEdgeDataStructure<V, E, F>
//		> double getMinVolume(HDS hds, AdapterSet a){
//			double minUneven = Double.MAX_VALUE;
//			for (V v : hds.getVertices()) {
//				double vol = getVolume(v, a);
//				if (vol < minUneven)
//					minUneven = vol;
//			}
//			return minUneven;
//		}


		public static <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>,
			HDS extends HalfEdgeDataStructure<V, E, F>
		> double getMeanVolume(HDS hds, AdapterSet a){
			double meanVol = 0.0;
			int count = 0;
			for (V v : hds.getVertices()) {
				meanVol += getVolume(v, a);
				count++;
			}
			return meanVol / count;
		}


		public static <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> double getVolume(V v, AdapterSet ad) {
			List<V> neighbors = HalfEdgeUtils.neighboringVertices(v);
			double volume = 0.0;
			neighbors.add(v);
			double[] base = null;
			double[][] tet = new double[3][3];
			for(Collection<V> tets : CollectionUtility.subsets(neighbors,4)) {
				base = null;
				int i = 0;
				for(V u : tets) {
					if(base == null) {
						base = ad.get(Position.class, u, double[].class);
						continue;
					}
					Rn.subtract(tet[i++], ad.get(Position.class,u,double[].class),base); 
				}
				volume += Math.abs(Rn.determinant(tet));
			}
			return volume/12.0;
		}
		
		
		@Label
		private class StarPlanarityLabelAdapter extends AbstractAdapter<String> {

			public StarPlanarityLabelAdapter() {
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
				if(HalfEdgeUtils.neighboringVertices(v).size() < 3) {
					return "";
				} else {
					return format.format(getVolume(v, a) * 100);
				}
			}

		}



		@Color
		private class StarPlanarityColorAdapter extends AbstractAdapter<double[]> {

			public StarPlanarityColorAdapter() {
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
				if(maxVolume < 1E-8) {
					return new double[]{0,1,0};
				} 
				double col = getVolume(v, a) / maxVolume;
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
				result.add(new StarPlanarityColorAdapter());
			}
			if (showLabels.isSelected()) {
				result.add(new StarPlanarityLabelAdapter());
			}
			return result;
		}


		@Override
		public String getName() {
			return "Vertex Star Planarity";
		}
}
