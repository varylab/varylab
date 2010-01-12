package de.varylab.varylab.plugin.generator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import de.jreality.geometry.QuadMeshFactory;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.icon.ImageHook;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.plugin.GeneratorPlugin;

public class QuadMeshGenerator extends GeneratorPlugin {

	private JPanel
		panel = new JPanel();
	
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
		
		panel.add(new JLabel("test"), gbc2);
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Quad Mesh", "Stefan Sechelmann");
		info.icon = ImageHook.getIcon("shape_handles.png");
		return info; 
	}
	
	@Override
	protected void generate(
		Content content,
		HalfedgeInterfacePlugin<VVertex, VEdge, VFace, VHDS> hif
	) {
		double size = 10.0;
		int numU = 40;
		int numV = 40;
		double[][][] verts = new double[numU][numV][];
		for (int u = 0; u < verts.length; u++) {
			for (int v = 0; v  < verts[0].length; v++) {
				verts[u][v] = new double[]{(size*(u/(double)numU - 0.5)), size*(v/(double)numV - 0.5), 0};
			}
		}
		
		QuadMeshFactory qmf = new QuadMeshFactory();
		qmf.setEdgeFromQuadMesh(true);
		qmf.setULineCount(40);
		qmf.setVLineCount(40);
		qmf.setVertexCoordinates(verts);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setGenerateFaceNormals(true);
		qmf.update();
		content.setContent(qmf.getGeometry());
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
