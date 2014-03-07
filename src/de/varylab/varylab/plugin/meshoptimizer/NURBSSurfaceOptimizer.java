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
import static javax.swing.JOptionPane.WARNING_MESSAGE;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import de.jreality.scene.Appearance;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.functional.NURBSReferenceSurfaceFunctional;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.VarylabMain;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;
import de.varylab.varylab.plugin.io.NurbsIO;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurfaceFactory;

public class NURBSSurfaceOptimizer extends VarylabOptimizerPlugin implements ActionListener {

	private NURBSReferenceSurfaceFunctional<VVertex, VEdge, VFace> 
		functional = new NURBSReferenceSurfaceFunctional<VVertex, VEdge, VFace>();
	private NURBSSurface
		activeSurface = null;
	private NURBSSurfaceFactory
		surfaceFactory = new NURBSSurfaceFactory();
	private JFileChooser
		nurbsChooser = new JFileChooser(new File("."));
	
	private JPanel
		panel = new JPanel();
	private JCheckBox
		showLayerChecker = new JCheckBox("Show Surface"),
		wireFrameChecker = new JCheckBox("Wireframe");
	private JButton
		loadSurfaceButton = new JButton("Load NURBS Surface");
	private JLabel
		infoLabel = new JLabel("Load a NURBS surface...");
	
	private Appearance
		refSurfaceAppearance = new Appearance("Reference Surface Appearance");
	private HalfedgeLayer
		refSurfaceLayer = null; 
	private HalfedgeInterface 
		hif = null;
	
	public NURBSSurfaceOptimizer() {
		nurbsChooser.setMultiSelectionEnabled(false);
		nurbsChooser.setDialogTitle("Open NURBS Surface");
		nurbsChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		nurbsChooser.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "Nurbs Surface OBJ File (*.obj)";
			}
			
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj");
			}
			
		});
		makePanelLayout();
	}
	
	private void makePanelLayout() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1,1,1,1);
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(loadSurfaceButton, c);
		panel.add(infoLabel, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(showLayerChecker, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(wireFrameChecker, c);
		
		loadSurfaceButton.addActionListener(this);
		showLayerChecker.addActionListener(this);
		wireFrameChecker.addActionListener(this);
	}
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		functional.setNURBSSurface(activeSurface);
		return functional;
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		c.getPlugin(VarylabMain.class);
		refSurfaceLayer = new HalfedgeLayer(hif);
		refSurfaceLayer.set(new VHDS());
		refSurfaceLayer.setName("NURBS Reference Surface");
		refSurfaceLayer.setAppearance(refSurfaceAppearance);
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	

	@Override
	public String getName() {
		return "NURBS Reference Surface";
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("NURBS Reference Surface", "Varylab Group");
		return info;
	}
	
	private void setSurface(NURBSSurface S) {
		this.activeSurface = S;
		surfaceFactory.setSurface(S);
		surfaceFactory.update();
		refSurfaceLayer.set(surfaceFactory.getGeometry());
		String infoText = "Surface: ";
		infoText += "degree u/v: " + S.getUDegree() + "/" + S.getVDegree() + ", ";
		infoText += "control mesh size u/v: " + S.getNumUPoints() + "/" + S.getNumVPoints();
		infoLabel.setText(infoText);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (loadSurfaceButton == e.getSource()) {
			Window w = SwingUtilities.getWindowAncestor(panel);
			int result = nurbsChooser.showOpenDialog(w);
			if (result != JFileChooser.APPROVE_OPTION) return;
			File nurbsFile = nurbsChooser.getSelectedFile();
			try {
				FileReader nurbsReader = new FileReader(nurbsFile);
				NURBSSurface surface = NurbsIO.readNURBS(nurbsReader);
				if(!surface.hasClampedKnotVectors()) {
					surface.repairKnotVectors();
				}
				setSurface(surface);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(w, e + "\n" + ex.getMessage(), "Loading Error", WARNING_MESSAGE);
				return;
			}
		}
		updateStates();
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
		panel.revalidate();
	}
	
}
