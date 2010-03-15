package de.varylab.varylab.plugin.generator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.icon.ImageHook;
import de.jreality.scene.proxy.scene.SceneGraphComponent;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.GeneratorPlugin;

public class QuadMeshGenerator extends GeneratorPlugin {

	private JPanel
		panel = new JPanel();
	private SpinnerNumberModel
		sizeModel = new SpinnerNumberModel(10.0, 0.1, 1000.0, 0.1),
		numUModel = new SpinnerNumberModel(6, 2, 10000, 1),
		numVModel = new SpinnerNumberModel(6, 2, 10000, 1);
	private JSpinner
		sizeSpinner = new JSpinner(sizeModel),
		numUSpinner = new JSpinner(numUModel),
		numVSpinner = new JSpinner(numVModel);
	private JCheckBox	
		faceCornerChecker = new JCheckBox("Use Face Center Corner"),
		diamondShaped = new JCheckBox("Use Diamonds", true);
	
	public QuadMeshGenerator() {
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
		panel.add(diamondShaped, gbc2);
		panel.add(faceCornerChecker, gbc2);
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Quad Mesh", "Stefan Sechelmann");
		info.icon = ImageHook.getIcon("combinatorics.png");
		return info; 
	}
	
	@Override
	protected void generate(
		Content content,
		HalfedgeInterface hif
	) {
		if (diamondShaped.isSelected()) {
			generateDiamonds(content);
		} else {
			generate(content);
		}
	}
	
	
	
	private void generate(Content content) {
		int numU = numUModel.getNumber().intValue();
		int numV = numVModel.getNumber().intValue();
		double[][][] verts = new double[numV][numU][];
		for (int u = 0; u < verts.length; u++) {
			for (int v = 0; v  < verts[0].length; v++) {
				verts[u][v] = new double[]{
					v / (double)(numV - 1) - 0.5, 
					u / (double)(numU - 1) - 0.5, 
					0
				};
			}
		}
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setEdgeFromQuadMesh(true);
		qmf.setULineCount(numU);
		qmf.setVLineCount(numV);
		qmf.setVertexCoordinates(verts);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setGenerateFaceNormals(true);
		qmf.update();
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("GeneratedQuadMesh");
		root.setGeometry(qmf.getGeometry());
		MatrixBuilder.euclidean().scale(sizeModel.getNumber().doubleValue()).assignTo(root);
		content.setContent(root);
	}
	
	
	private void generateDiamonds(Content content) {
		int numU = numUModel.getNumber().intValue();
		int numV = numVModel.getNumber().intValue();
		// vertices
		double[][] verts = new double[numU*numV + (numU - 1)*(numV - 1)][3];
		for (int u = 0; u < numU; u++) {
			for (int v = 0; v  < numV; v++) {
				int i = u * numV + v;
				verts[i] = new double[]{
					u / (double)(numU - 1), 
					v / (double)(numV - 1), 
					0
				};
			}
		}
		double stepU = 1 / (double)(numU - 1);
		double stepV = 1 / (double)(numV - 1);
		int offset = numU*numV;
		for (int u = 0; u < numU - 1; u++) {
			for (int v = 0; v  < numV - 1; v++) {
				int i = offset + u * (numV - 1) + v;
				verts[i] = new double[]{
					u/(double)(numU - 1) + stepU/2, 
					v/(double)(numV - 1) + stepV/2, 
					0
				};
			}
		}
		// faces
		int numFaces = numU*(numV-1) + (numU-1)*numV;
		int[][] faces = new int[numFaces][4];
		int f = 0;
		for (int u = 0; u < numU - 1; u++) {
			for (int v = 0; v  < numV; v++) {
				int i = u * numV + v;
				if (v == 0) {
					faces[f++] = new int[] {i, i + numV, offset + u*(numV-1) + v};
				} else 
				if (v == numV - 1) {
					faces[f++] = new int[] {i, offset + u*(numV-1) + v - 1, i + numV};
				} else {
					faces[f++] = new int[] {i, offset + u*(numV-1) + v - 1, i + numV, offset + u*(numV-1) + v};
				}
			}
		}	
		for (int u = 0; u < numU; u++) {
			for (int v = 0; v  < numV - 1; v++) {
				int i = u * (numV - 1) + v;
				if (u == 0) {
					faces[f++] = new int[] {u*numV + v, offset + i, u*numV + v + 1};
				} else 
				if (u == numU - 1) {
					faces[f++] = new int[] {u*numV + v, u*numV + v + 1, offset + (u-1)*(numV-1) + v};
				} else {
					faces[f++] = new int[] {u*numV + v, offset + i, u*numV + v + 1, offset + (u-1)*(numV-1) + v};
				}
			}
		}	
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(verts.length);
		ifsf.setVertexCoordinates(verts);
		ifsf.setFaceCount(faces.length);
		ifsf.setFaceIndices(faces);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("GeneratedQuadMesh");
		root.setGeometry(ifsf.getGeometry());
		MatrixBuilder.euclidean().scale(sizeModel.getNumber().doubleValue()).assignTo(root);
		content.setContent(root);
	}
	
	

	@Override
	protected String[] getMenuPath() {
		return new String[] {};
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}
	
}
