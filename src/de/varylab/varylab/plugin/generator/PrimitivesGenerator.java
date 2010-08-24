package de.varylab.varylab.plugin.generator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.jreality.geometry.Primitives;
import de.jreality.scene.IndexedFaceSet;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class PrimitivesGenerator extends AlgorithmDialogPlugin {

	private JPanel
		panel = new JPanel();
	private ButtonGroup
		primitivesGroup = new ButtonGroup();
	
	private JRadioButton
		cubeButton = new JRadioButton("cube"),
		openCubeButton = new JRadioButton("open cube"),
		tetrahedronButton = new JRadioButton("tetrahedron");

	private ConverterJR2Heds
		converter = new ConverterJR2Heds();
	
	public PrimitivesGenerator() {
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
		
		primitivesGroup.add(cubeButton);
		primitivesGroup.add(tetrahedronButton);
		
		panel.add(cubeButton,gbc2);
		panel.add(openCubeButton, gbc2);
		panel.add(tetrahedronButton,gbc2);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Primitives");
		info.icon = ImageHook.getIcon("cube.png", 16, 16);
		return info; 
	}
	
	
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException {
		IndexedFaceSet ifs = null;
		if(cubeButton.isSelected()) {
			ifs = Primitives.cube();
		}
		if(openCubeButton.isSelected()) {
			ifs = Primitives.openCube();
		}
		if(tetrahedronButton.isSelected()) {
			ifs = Primitives.tetrahedron();
		}
		converter.ifs2heds(ifs, hds, hcp.getAdapters());
		hcp.update();
	}

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Generator;
	}

	@Override
	public String getAlgorithmName() {
		return "Primitives";
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}
	
}
