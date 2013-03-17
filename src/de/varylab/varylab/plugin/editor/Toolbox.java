package de.varylab.varylab.plugin.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JButton;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.scene.Camera;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.smoothing.LaplacianSmoothing;

public class Toolbox extends ShrinkPanelPlugin implements ActionListener {

	private HalfedgeInterface 
		hif = null;
	private Scene 
		scene = null;
	private JButton
		smoothCombButton = new JButton("Smooth (Comb.)"),
		xyViewButton = new JButton("xy"),
		yzViewButton = new JButton("yz"),
		xzViewButton = new JButton("xz"),
		togglePerspectiveButton = new JButton("persp.");

	public Toolbox() {
		shrinkPanel.setLayout(new GridBagLayout());
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
		shrinkPanel.add(smoothCombButton,gbc2);
		shrinkPanel.add(xyViewButton,gbc1);
		shrinkPanel.add(yzViewButton,gbc1);
		shrinkPanel.add(xzViewButton,gbc1);
		shrinkPanel.add(togglePerspectiveButton, gbc2);
		smoothCombButton.addActionListener(this);
		xyViewButton.addActionListener(this);
		yzViewButton.addActionListener(this);
		xzViewButton.addActionListener(this);
		togglePerspectiveButton.addActionListener(this);
	}
	

	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		scene = c.getPlugin(Scene.class);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Halfedge Toolbox", "Thilo Roerig");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		double[] oldTrafo = scene.getContentComponent().getTransformation().getMatrix();
		double scale = oldTrafo[15];
		if (smoothCombButton == src) {
			VHDS hds = hif.get(new VHDS());
			Set<VVertex> selectedVerts = hif.getSelection().getVertices(hds);
			if(selectedVerts.size() == 0) {
				selectedVerts.addAll(hds.getVertices());
			}
			LaplacianSmoothing.smoothCombinatorially(hds, selectedVerts, hif.getAdapters(), true);
			hif.set(hds);
		} else if (xyViewButton == src) {
			Matrix trafo = MatrixBuilder.euclidean().rotateX(0).scale(scale).getMatrix();
			trafo.assignTo(scene.getContentComponent());
			
		} else if (yzViewButton == src) {
			Matrix trafo = MatrixBuilder.euclidean().rotateY(Math.PI/2.0).scale(scale).getMatrix();
			trafo.assignTo(scene.getContentComponent());
		} else if (xzViewButton == src) {
			Matrix trafo = MatrixBuilder.euclidean().rotateX(Math.PI/2.0).scale(scale).getMatrix();
			trafo.assignTo(scene.getContentComponent());
		} else if (togglePerspectiveButton == src) {
			Camera cam = scene.getCameraComponent().getCamera();
			cam.setPerspective(!cam.isPerspective());	
		}
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
}
	
