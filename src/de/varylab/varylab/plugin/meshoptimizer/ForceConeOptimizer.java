package de.varylab.varylab.plugin.meshoptimizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.functional.ForceConeFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class ForceConeOptimizer extends VarylabOptimizerPlugin { 
	
	private ForceConeFunctional<VVertex, VEdge, VFace>
		functional = new ForceConeFunctional<VVertex, VEdge, VFace>();

	private JPanel
		optionsPanel = new JPanel();
	private JComboBox
		angleTermCombo = new JComboBox(new String[]{"Term 1", "Term 2", "Term 3"}),
		distanceTermCombo = new JComboBox(new String[]{"Term 1", "Term 2", "Term 3", "Term 4"}),
		vertexTermCombo = new JComboBox(new String[]{"Term 1", "Term 2"});
	private JCheckBox
		angleChecker = new JCheckBox("Angle"),
		distanceChecker = new JCheckBox("Distance"),
		vertexChecker = new JCheckBox("Vertex");
	
	
	public ForceConeOptimizer() {
		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		GridBagConstraints c2 = new GridBagConstraints();
		c1.gridwidth = GridBagConstraints.RELATIVE;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c1.fill = GridBagConstraints.HORIZONTAL;
		c2.fill = GridBagConstraints.HORIZONTAL;
		c1.insets = new Insets(1, 1, 1, 1);
		c2.insets = new Insets(1, 1, 1, 1);
		c1.weightx = 1.0;
		c2.weightx = 1.0;
		optionsPanel.add(angleChecker, c1);
		optionsPanel.add(angleTermCombo, c2);
		optionsPanel.add(distanceChecker, c1);
		optionsPanel.add(distanceTermCombo, c2);
		optionsPanel.add(vertexChecker, c1);
		optionsPanel.add(vertexTermCombo, c2);
		c2.weighty = 1.0;
		optionsPanel.add(new JPanel(), c2);
	}
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		functional.setUseAngleTerm(angleChecker.isSelected());
		functional.setAngleTerm(angleTermCombo.getSelectedIndex());
		functional.setUseDistanceTerm(distanceChecker.isSelected());
		functional.setDistanceTerm(distanceTermCombo.getSelectedIndex());
		functional.setUseVertexTerm(vertexChecker.isSelected());
		functional.setVertexTerm(vertexTermCombo.getSelectedIndex());
		return functional;
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		Class<?> ctx = ForceConeFunctional.class;
		c.storeProperty(ctx, "useAngleTerm", angleChecker.isSelected());
		c.storeProperty(ctx, "useDistanceTerm", distanceChecker.isSelected());
		c.storeProperty(ctx, "useVertexTerm", vertexChecker.isSelected());
		c.storeProperty(ctx, "angleTerm", angleTermCombo.getSelectedIndex());
		c.storeProperty(ctx, "disctanceTerm", distanceTermCombo.getSelectedIndex());
		c.storeProperty(ctx, "vertexTerm", vertexTermCombo.getSelectedIndex());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		Class<?> ctx = ForceConeFunctional.class;
		angleChecker.setSelected(c.getProperty(ctx, "useAngleTerm", angleChecker.isSelected()));
		distanceChecker.setSelected(c.getProperty(ctx, "useDistanceTerm", distanceChecker.isSelected()));
		vertexChecker.setSelected(c.getProperty(ctx, "useVertexTerm", vertexChecker.isSelected()));
		angleTermCombo.setSelectedIndex(c.getProperty(ctx, "angleTerm", angleTermCombo.getSelectedIndex()));
		distanceTermCombo.setSelectedIndex(c.getProperty(ctx, "disctanceTerm", distanceTermCombo.getSelectedIndex()));
		vertexTermCombo.setSelectedIndex(c.getProperty(ctx, "vertexTerm", vertexTermCombo.getSelectedIndex()));
	}
	
	@Override
	public JPanel getOptionPanel() {
		return optionsPanel;
	}
	
	@Override
	public String getName() {
		return "Force cones";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Force Cone Optimizer", "Thilo Roerig");
		info.icon = ImageHook.getIcon("forceCone.png",16,16);
		return info;
	}

}
