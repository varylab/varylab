package de.varylab.varylab.plugin.generator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.plugin.basic.Content;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.proxy.scene.SceneGraphComponent;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.jreality.ConverterHeds2JR;
import de.jtem.halfedgetools.plugin.GeneratorPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.adapter.VPositionAdapter;
import de.varylab.varylab.plugin.ui.image.ImageHook;
import de.varylab.varylab.utilities.Rectangle2D;

public class HexMeshGenerator extends GeneratorPlugin {

	private JPanel
		panel = new JPanel();
	private SpinnerNumberModel
		alphaModel = new SpinnerNumberModel(Math.PI/3.0, 0.1, 2*Math.PI/3, 0.1),	
		xSizeModel = new SpinnerNumberModel(10.0, 0.1, 1000.0, 0.1),
		ySizeModel = new SpinnerNumberModel(10.0, 0.1, 1000.0, 0.1),
		numUModel = new SpinnerNumberModel(6, 1, 10000, 1),
		numVModel = new SpinnerNumberModel(6, 1, 10000, 1);
	private ConverterHeds2JR
		converter = new ConverterHeds2JR();
	private SceneGraphComponent
		root = new SceneGraphComponent();
		
	private JSpinner
		alphaSpinner = new JSpinner(alphaModel),
		xSizeSpinner = new JSpinner(xSizeModel),
		ySizeSpinner = new JSpinner(ySizeModel),
		numUSpinner = new JSpinner(numUModel),
		numVSpinner = new JSpinner(numVModel);
	
	private JRadioButton
		sizeButton = new JRadioButton("Size"),
		resolutionButton = new JRadioButton("Resolution", true);
	
	private ButtonGroup
		methodGroup = new ButtonGroup();
	
	public HexMeshGenerator() {
		panel.setLayout(new GridBagLayout());
		
		methodGroup.add(sizeButton);
		methodGroup.add(resolutionButton);
		
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
		
		panel.add(new JLabel("Angle"), gbc1);
		panel.add(alphaSpinner, gbc2);
		
		panel.add(sizeButton,gbc2);
		panel.add(new JLabel("xSize"), gbc1);
		panel.add(xSizeSpinner, gbc2);
		panel.add(new JLabel("ySize"), gbc1);
		panel.add(ySizeSpinner, gbc2);
		
		panel.add(resolutionButton,gbc2);
		panel.add(new JLabel("U Resolution"), gbc1);
		panel.add(numUSpinner, gbc2);
		panel.add(new JLabel("V Resolution"), gbc1);
		panel.add(numVSpinner, gbc2);
		
		
	}
	
	@Override
	protected void generate(Content content, HalfedgeInterface hif) {
		HexMeshFactory hmf = new HexMeshFactory(alphaModel.getNumber().doubleValue());
		if(resolutionButton.isSelected()) {
			hmf.setResolution(numUModel.getNumber().intValue(), numVModel.getNumber().intValue());
		}
		if(sizeButton.isSelected()) {
			hmf.setSize(new Rectangle2D(new double[]{0.0,0.0},new double[]{xSizeModel.getNumber().doubleValue(),ySizeModel.getNumber().doubleValue()}));
		}
		VHDS hds = hmf.getHDS();
//		Rectangle2D bbox = hmf.getInsideRectangle();
//		MatrixBuilder mb = MatrixBuilder.euclidean();
//		Matrix T = 
//			mb
//			.scale(xSizeModel.getNumber().doubleValue()/bbox.getWidth(), ySizeModel.getNumber().doubleValue()/bbox.getHeight(), 1)
//			.translate(-bbox.getMinX(), -bbox.getMinY(), 0)
//			.getMatrix();
//		hds.applyTransformation(T);
		AdapterSet adapters = new AdapterSet(new VPositionAdapter());
		IndexedFaceSet ifs =  converter.heds2ifs(hds, adapters);
		IndexedFaceSetUtility.calculateAndSetFaceNormals(ifs);
		root.setGeometry(ifs);
		content.setContent(root);
	}

	@Override
	protected String[] getMenuPath() {
		return new String[]{};
	}

	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Hex Mesh", "Thilo Roerig & Stefan Sechelmann");
		info.icon = ImageHook.getIcon("hex_gen.png", 16, 16);
		return info; 
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}
	
}
		
