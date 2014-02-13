package de.varylab.varylab.plugin.hyperbolicnets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.plugin.basic.View;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.halfedge.VHDS;

public class HyperbolicNetsPlugin extends ShrinkPanelPlugin implements ActionListener, ChangeListener {

	private JButton
		hyperboloidPatchButton = new JButton("Add Patches"),
		removeButton = new JButton("Remove Patches");
	
	private SpinnerNumberModel
		uModel = new SpinnerNumberModel(5, 2, 100, 1),
		vModel = new SpinnerNumberModel(5, 2, 100, 1),
		wModel = new SpinnerNumberModel(1.0, 0.000001, 1000, 1.0);
	
	private JSpinner
		uSpinner = new JSpinner(uModel),
		vSpinner = new JSpinner(vModel),
		wSpinner = new JSpinner(wModel);
	
	private HalfedgeInterface 
		hif = null;
	
	private SceneGraphComponent 
		patchComponent = new SceneGraphComponent("Hyperbolic patches");
	
	public HyperbolicNetsPlugin() {
		hyperboloidPatchButton.addActionListener(this);
		removeButton.addActionListener(this);
		uSpinner.addChangeListener(this);
		vSpinner.addChangeListener(this);
		wSpinner.addChangeListener(this);
		
		
		shrinkPanel.setLayout(new GridBagLayout());
		GridBagConstraints 
			rc = LayoutFactory.createRightConstraint(),
			lc = LayoutFactory.createLeftConstraint();
		
		shrinkPanel.add(hyperboloidPatchButton,rc);
		shrinkPanel.add(new JLabel("u"),lc);
		shrinkPanel.add(uSpinner,rc);
		shrinkPanel.add(new JLabel("v"),lc);
		shrinkPanel.add(vSpinner,rc);
		shrinkPanel.add(new JLabel("W"),lc);
		shrinkPanel.add(wSpinner,rc);
		shrinkPanel.add(removeButton,rc);
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == hyperboloidPatchButton) {
			addHyperboloidPatches();
		} else if(e.getSource() == removeButton) {
			hif.removeTemporaryGeometry(patchComponent);
		} 
	}

	private void addHyperboloidPatches() {
		
		try {
			hif.removeTemporaryGeometry(patchComponent);
		} catch (NullPointerException e) {
			//ignore
		}
		patchComponent.removeAllChildren();
		
		VHDS hds = hif.get(new VHDS());
		AdapterSet as = hif.getAdapters();
		HyperbolicNet hypNet = new HyperbolicNet(hds, as, wModel.getNumber().doubleValue());
		hypNet.setULines(uModel.getNumber().intValue());
		hypNet.setVLines(vModel.getNumber().intValue());
		patchComponent.addChild(hypNet.getComponent());
		hif.addTemporaryGeometry(patchComponent);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		hif = c.getPlugin(HalfedgeInterface.class);
		super.install(c);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		addHyperboloidPatches();
	}
	
}
