package de.varylab.varylab.plugin.visualizers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.generic.GaussCurvatureAdapter;
import de.jtem.halfedgetools.adapter.type.Label;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;

public class GaussCurvatureVisualizer extends VisualizerPlugin implements ChangeListener {
	
		private DecimalFormat
			format = new DecimalFormat("0.000");
		private SpinnerNumberModel
			placesModel = new SpinnerNumberModel(3, 0, 20, 1);
		private JSpinner	
			placesSpinner = new JSpinner(placesModel);
		private JPanel
			panel = new JPanel();

		public GaussCurvatureVisualizer() {
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
			
			panel.add(new JLabel("Decimal Places"), gbc1);
			panel.add(placesSpinner, gbc2);
			
			placesSpinner.addChangeListener(this);
		}
	
		@Override
		public AdapterSet getAdapters() {
			AdapterSet result = new AdapterSet();
			result.add(new GaussCurvatureLabelAdapter());
			return result;
		}


		@Override
		public String getName() {
			return "Gauss curvature";
		}

		@Label
		private class GaussCurvatureLabelAdapter extends AbstractAdapter<String> {

			private GaussCurvatureAdapter
				gca = new GaussCurvatureAdapter();
			
			public GaussCurvatureLabelAdapter() {
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
				if(HalfEdgeUtils.isBoundaryVertex(v)) {
					return "-";
				} else {
					return format.format(gca.getV(v, a));
				}
			}

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
		public JPanel getOptionPanel() {
			return panel;
		}
}
