package de.varylab.varylab.plugin.ddg;

import static java.lang.Math.PI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.jrworkspace.plugin.Controller;

public class AssociatedFamily extends AlgorithmDialogPlugin {

	private ChristoffelTransfom
		christoffelTransfom = null;
	private JPanel
		panel = new JPanel();
	private SpinnerNumberModel
		angleModel = new SpinnerNumberModel(0.0, 0.0, PI * 2, 0.1);
	private JSpinner
		angleSpinner = new JSpinner(angleModel);
	
	
	public AssociatedFamily() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c1 = LayoutFactory.createLeftConstraint();
		GridBagConstraints c2 = LayoutFactory.createRightConstraint();
		panel.add(new JLabel("Turning Angle"), c1);
		panel.add(angleSpinner, c2);
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		christoffelTransfom.transfom(hds, a);
		
		christoffelTransfom.transfom(hds, a);
		hi.update();
	}
	
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		christoffelTransfom = c.getPlugin(ChristoffelTransfom.class);
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Geometry;
	}

	@Override
	public String getAlgorithmName() {
		return "Associated Family";
	}

}
