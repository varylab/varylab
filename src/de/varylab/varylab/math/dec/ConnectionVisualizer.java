package de.varylab.varylab.math.dec;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

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
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Label;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.jtem.jrworkspace.plugin.Controller;

public class ConnectionVisualizer extends VisualizerPlugin implements ChangeListener {
	
	private DecimalFormat
		format = new DecimalFormat("0.000");
	private SpinnerNumberModel
		placesModel = new SpinnerNumberModel(3, 0, 20, 1);
	private JSpinner	
		placesSpinner = new JSpinner(placesModel);
	private JPanel
		panel = new JPanel();
	private HalfedgeInterface
		hif = null;
	
	public ConnectionVisualizer() {
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
	public Set<? extends Adapter<?>> getAdapters() {
		Set<Adapter<?>> result = new HashSet<Adapter<?>>();
		result.add(new ConnectionLabelAdapter());
		return result;
	}


	@Override
	public String getName() {
		return "Connection";
	}

	@Label
	private class ConnectionLabelAdapter extends AbstractAdapter<String> {

		private ConnectionAdapter 
			ca = null;
		
		public ConnectionLabelAdapter() {
			super(String.class, true, false);
			ca = hif.getAdapters().query(ConnectionAdapter.class);
		}

		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Edge.class.isAssignableFrom(nodeClass);
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
		> String getE(E e, AdapterSet a) {
			Double d = ca.getE(e, a);
			if(d != null) {
				return format.format(d);// +";"+format.format(ca.getE(e.getOppositeEdge(), a));	
			} else {
				return "-";
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
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		
	}
}
