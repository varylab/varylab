package de.varylab.varylab.plugin.meshoptimizer;

import static de.jreality.scene.Appearance.DEFAULT;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static java.awt.Color.ORANGE;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.jreality.scene.Appearance;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.flavor.UIFlavor;
import de.varylab.varylab.functional.ReferenceSurfaceFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.VarylabMain;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class ReferenceSurfaceOptimizer extends VarylabOptimizerPlugin implements ActionListener, UIFlavor {


	private ReferenceSurfaceFunctional<VVertex, VEdge, VFace>
		functional = new ReferenceSurfaceFunctional<VVertex, VEdge, VFace>();
	private JPanel
		panel = new JPanel();
	private JCheckBox
		showLayerChecker = new JCheckBox("Show Surface"),
		wireFrameChecker = new JCheckBox("Wireframe");
	
	private Appearance
		refSurfaceAppearance = new Appearance("Reference Surface Appearance");
	private HalfedgeLayer
		refSurfaceLayer = null; 
	
	private HalfedgeInterface 
		hif = null;
	
	public ReferenceSurfaceOptimizer() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.insets = new Insets(1,1,1,1);
		c1.fill = GridBagConstraints.BOTH;
		c1.anchor = GridBagConstraints.WEST;
		c1.weightx = 1.0;
		c1.gridwidth = 1;
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets = new Insets(1,1,1,1);
		c2.fill = GridBagConstraints.BOTH;
		c2.anchor = GridBagConstraints.WEST;
		c2.weightx = 1.0;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(showLayerChecker,c1);
		panel.add(wireFrameChecker,c2);
		showLayerChecker.addActionListener(this);
		wireFrameChecker.addActionListener(this);
	}
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		VHDS refHDS = refSurfaceLayer.get(new VHDS());
		functional.setReferenceSurface(refHDS, hif.getAdapters());
		return functional;
	}
	
	
	public VHDS getReferenceSurface() {
		if (refSurfaceLayer == null) {
			return null;
		} else {
			return refSurfaceLayer.get(new VHDS());
		}
	}
	

	@Override
	public String getName() {
		return "Reference Mesh";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Reference Mesh", "Thilo Roerig");
//		info.icon = ImageHook.getIcon("electro.png");
		return info;
	}

	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
	
	private void updateStates() {
		refSurfaceAppearance.setAttribute(FACE_DRAW, !wireFrameChecker.isSelected());
		refSurfaceAppearance.setAttribute(EDGE_DRAW, wireFrameChecker.isSelected());
		refSurfaceAppearance.setAttribute(VERTEX_DRAW, false);
		refSurfaceAppearance.setAttribute(LINE_SHADER + "." + TUBES_DRAW, false);
		refSurfaceAppearance.setAttribute(TRANSPARENCY_ENABLED, true);
		refSurfaceAppearance.setAttribute(POLYGON_SHADER + "." + TRANSPARENCY, 0.3);
		refSurfaceAppearance.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, ORANGE);
		refSurfaceAppearance.setAttribute(POLYGON_SHADER + "." + TEXTURE_2D, DEFAULT); 
		refSurfaceAppearance.setAttribute(LINE_SHADER + "." + TRANSPARENCY, 0.3);
		refSurfaceAppearance.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, ORANGE);
		refSurfaceLayer.setAppearance(refSurfaceAppearance);
		wireFrameChecker.setEnabled(showLayerChecker.isSelected());
		if (showLayerChecker.isSelected()) {
			hif.addLayer(refSurfaceLayer);
		} else {
			hif.removeLayer(refSurfaceLayer);
		}
	}
	
	

	@Override
	public void actionPerformed(ActionEvent e) {
		hif.checkContent();
		updateStates();
	}
	
	@Override
	public void install(Controller c) throws Exception {
		hif = c.getPlugin(HalfedgeInterface.class);
		c.getPlugin(VarylabMain.class);
		refSurfaceLayer = new HalfedgeLayer(hif);
		refSurfaceLayer.set(new VHDS());
		refSurfaceLayer.setName("Reference Surface");
		refSurfaceLayer.setAppearance(refSurfaceAppearance);
		super.install(c);
	}
	

}
