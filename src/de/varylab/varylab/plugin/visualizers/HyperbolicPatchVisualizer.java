package de.varylab.varylab.plugin.visualizers;

import hyperbolicnets.core.DataModel;
import hyperbolicnets.core.HyperbolicPatch;
import hyperbolicnets.core.PlanarFamily;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.scene.SceneGraphComponent;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.projgeom.P5;
import de.jtem.projgeom.PlueckerLineGeometry;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VHDS;

public class HyperbolicPatchVisualizer extends VisualizerPlugin implements ActionListener {

	HalfedgeInterface 
		hif = null;
	
	private JPanel
		panel = new JPanel();
	
	private JButton
		updateButton = new JButton("Update");
	
	private SpinnerNumberModel
		resolutionModel = new SpinnerNumberModel(11,2,51,1),
		parameterModel = new SpinnerNumberModel(0.25,0,1,0.05);
	
	private JSpinner
		resolutionSpinner = new JSpinner(resolutionModel),
		parameterSpinner = new JSpinner(parameterModel);

	private int resolution = 11;

	private double parameter = parameterModel.getNumber().doubleValue();

	public HyperbolicPatchVisualizer() {
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
				
		panel.add(new JLabel("Resolution"), gbc1);
		panel.add(resolutionSpinner, gbc2);
		panel.add(new JLabel("Parameter"), gbc1);
		panel.add(parameterSpinner, gbc2);
		updateButton.addActionListener(this);
		panel.add(updateButton,gbc2);
	}
	
	@Override
	public String getName() {
		return "Hyperbolic Patch Visualizer";
	}

	@Override
	public void install(Controller c) throws Exception {
		hif = c.getPlugin(HalfedgeInterface.class);
		super.install(c);
	}
	
	@Override
	public void updateContent() {
		manager.updateContent();
	}

	@Override
	public SceneGraphComponent getComponent() {
		VHDS hds = new VHDS();
		hds = hif.get(hds);
		HFaceSurface hSurface = new HFaceSurface(hds);
		//TODO: Implement interactive choice of starting edge
		VEdge startEdge = hds.getEdge(0);
		
		//TODO: Adapt to proper range?
		hSurface.propagateQ(startEdge, parameter * Math.PI);
		
		//TODO: Implement interactive choice of Inital 1/2 - parameter lines
		hSurface.propagateParametrisation(hif.getAdapters());
		
		SceneGraphComponent patchRoot = new SceneGraphComponent();
		patchRoot.setName("Hyperbolic Patches");
		for(HFace hf : hSurface.getFaces()) {
			VEdge e = hf.getvFace().getBoundaryEdge();
			PlanarFamily pf1 = new PlanarFamily(hf.getParameterLines(e), P5.LINE_SPACE, PlueckerLineGeometry.getTolerance());
			PlanarFamily pf2 = new PlanarFamily(hf.getParameterLines(e.getNextEdge()), P5.LINE_SPACE, PlueckerLineGeometry.getTolerance());
			HyperbolicPatch hp = new HyperbolicPatch("Hyperbolic Patch", new PlanarFamily[] {pf1,pf2});
			patchRoot.addChild(hp.getSgc());
		}
		
		return patchRoot;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		resolution = resolutionModel.getNumber().intValue();
		DataModel.setResolution(resolution);
		parameter = parameterModel.getNumber().doubleValue();
		manager.updateContent();
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
}
