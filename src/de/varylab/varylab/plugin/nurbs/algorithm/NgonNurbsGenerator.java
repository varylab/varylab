package de.varylab.varylab.plugin.nurbs.algorithm;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.icon.ImageHook;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.math.ConstructionTools;
import de.varylab.varylab.plugin.nurbs.math.NurbsSurfaceUtility;

public class NgonNurbsGenerator extends AlgorithmDialogPlugin {

	private JPanel
		panel = new JPanel();
	private SpinnerNumberModel
		sizeModel = new SpinnerNumberModel(3, 3, 100, 1),
		numUModel = new SpinnerNumberModel(31, 11, 1000, 3),
		numVModel = new SpinnerNumberModel(31, 11, 1000, 3);
	private JSpinner
		sizeSpinner = new JSpinner(sizeModel),
		numUSpinner = new JSpinner(numUModel),
		numVSpinner = new JSpinner(numVModel);
	
	public NgonNurbsGenerator() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.weightx = 1.0;
		gbc1.gridwidth = GridBagConstraints.RELATIVE;
		gbc1.insets = new Insets(2, 2, 2, 2);
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.weightx = 1.0;
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.insets = new Insets(2, 2, 2, 2);
		
		panel.add(new JLabel("Size"), gbc1);
		panel.add(sizeSpinner, gbc2);
		panel.add(new JLabel("U Resolution"), gbc1);
		panel.add(numUSpinner, gbc2);
		panel.add(new JLabel("V Resolution"), gbc1);
		panel.add(numVSpinner, gbc2);		
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Ngon Nurbs Mesh", "Christoph Seidel");
		info.icon = ImageHook.getIcon("quad_gen.png", 16, 16);
		return info; 
	}
	
	
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hcp) {
		int n = sizeModel.getNumber().intValue();
		NURBSSurface ngon = ConstructionTools.constructNGon(n);
//		NurbsSurfaceUtility.addNurbsMesh(ngon, hcp.getActiveLayer(), numUModel.getNumber().intValue(), numVModel.getNumber().intValue());
		NurbsSurfaceUtility.addNurbsMesh(ngon, hcp.getActiveLayer(), 10 * n + 1, 10 * n + 1);
	}
	

	@Override
	public String getCategory() {
		return "NURBS";
	}

	@Override
	public String getAlgorithmName() {
		return "Ngon nurbs mesh";
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}
	
}
