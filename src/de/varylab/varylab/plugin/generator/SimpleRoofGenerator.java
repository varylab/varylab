package de.varylab.varylab.plugin.generator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.geometry.IndexedFaceSetFactory;
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

public class SimpleRoofGenerator extends AlgorithmDialogPlugin {

	private JPanel 
		panel = new JPanel();

	private SpinnerNumberModel
		xSizeModel = new SpinnerNumberModel(20.0, 0.1, 1000.0, 0.1),
		ySizeModel = new SpinnerNumberModel(10.0, 0.1, 1000.0, 0.1);
	
	private JSpinner
		xSizeSpinner = new JSpinner(xSizeModel),
		ySizeSpinner = new JSpinner(ySizeModel);
	
	private ConverterJR2Heds
		converter = new ConverterJR2Heds();
	
	public SimpleRoofGenerator() {
		panel .setLayout(new GridBagLayout());
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
		
		panel.add(new JLabel("xSize"), gbc1);
		panel.add(xSizeSpinner, gbc2);
		panel.add(new JLabel("ySize"), gbc1);
		panel.add(ySizeSpinner, gbc2);
	}
	
	
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException {
		IndexedFaceSet ifs = generateSimpleRoof();
		converter.ifs2heds(ifs, hds, hcp.getAdapters());
		hcp.update();
	}
	

	private IndexedFaceSet generateSimpleRoof() {
		double[][] verts = new double[12][3];
		double xStep = xSizeModel.getNumber().doubleValue()/3.0;
		double yStep = ySizeModel.getNumber().doubleValue()/2.0;
		for(int i = 0; i < 3; ++i ) {
			for(int j = 0; j < 4; ++j) {
				double move = ((i==1&&j==1)?-(xStep/2.0):0) + ((i==1&&j==2)?xStep/2.0:0);
				verts[i*4+j] = new double[] {j*xStep+move, i*yStep, 0};
			}
		}
		
		// faces
		int[][] faces = new int[10][];
		faces[0] = new int[]{0,5,4};
		faces[1] = new int[]{0,1,5};
		faces[2] = new int[]{4,5,8};
		faces[3] = new int[]{5,9,8};
		faces[4] = new int[]{1,2,6,5};
		faces[5] = new int[]{5,6,10,9};
		faces[6] = new int[]{2,3,6};
		faces[7] = new int[]{3,7,6};
		faces[8] = new int[]{6,7,11};
		faces[9] = new int[]{6,11,10};
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(verts.length);
		ifsf.setVertexCoordinates(verts);
		ifsf.setFaceCount(faces.length);
		ifsf.setFaceIndices(faces);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Generator;
	}
	
	@Override
	public String getAlgorithmName() {
		return "Simple Roof";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Simple Roof", "Thilo Roerig");
		info.icon = ImageHook.getIcon("roofGenerator.png", 16, 16);
		return info; 
	}
	
}
