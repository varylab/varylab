package de.varylab.varylab.plugin.meshoptimizer;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static java.awt.Color.ORANGE;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.SwingUtilities.getWindowAncestor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.plugin.basic.View;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.generic.NormalAdapter;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.flavor.UIFlavor;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.hds.adapter.VPositionAdapter;
import de.varylab.varylab.math.functional.ReferenceSurfaceFunctional;
import de.varylab.varylab.plugin.OptimizerPlugin;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class ReferenceSurfaceOptimizer extends OptimizerPlugin implements ActionListener, UIFlavor {


	private ReferenceSurfaceFunctional<VVertex, VEdge, VFace>
		functional = new ReferenceSurfaceFunctional<VVertex, VEdge, VFace>();

	private JPanel
		panel = new JPanel();
	private JButton
		loadButton = new JButton("Load");
	private JFileChooser 
		chooser = FileLoaderDialog.createFileChooser();

	private JCheckBox
		showSurfaceChecker = new JCheckBox("Show Surface"),
		wireFrameChecker = new JCheckBox("Wireframe");
	
	private Appearance
		refSurfaceAppearance = new Appearance("Reference Surface Appearance");
	private HalfedgeLayer
		refSurfaceLayer = null; 
	
	private View 
		view = null;
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
		panel.add(loadButton,c2);
		panel.add(showSurfaceChecker,c1);
		panel.add(wireFrameChecker,c2);
		wireFrameChecker.setEnabled(false);
		showSurfaceChecker.setEnabled(false);
		showSurfaceChecker.addActionListener(this);
		wireFrameChecker.addActionListener(this);
		loadButton.addActionListener(this);
	}
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		return functional;
	}

	@Override
	public String getName() {
		return "Reference Surface Energy";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Reference Surface Optimizer", "Thilo Roerig");
		info.icon = ImageHook.getIcon("electro.png");
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
		refSurfaceAppearance.setAttribute(LINE_SHADER + "." + TRANSPARENCY, 0.3);
		refSurfaceAppearance.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, ORANGE);
		refSurfaceLayer.setAppearance(refSurfaceAppearance);
		wireFrameChecker.setEnabled(showSurfaceChecker.isSelected());
		if (showSurfaceChecker.isSelected()) {
			hif.addLayer(refSurfaceLayer);
		} else {
			hif.removeLayer(refSurfaceLayer);
		}
	}
	
	

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(loadButton == src) {
			SceneGraphComponent c = loadFile();
			Geometry g = SceneGraphUtility.getFirstGeometry(c);
			if (g == null || !(g instanceof IndexedFaceSet)) return;
			IndexedFaceSet ifs = (IndexedFaceSet)g;
			IndexedFaceSetUtility.calculateAndSetNormals(ifs);
			refSurfaceLayer.set(ifs);
			AdapterSet as = AdapterSet.createGenericAdapters(); 
			as.add(new VPositionAdapter());
			as.add(new NormalAdapter());
			VHDS refSurface = refSurfaceLayer.get(new VHDS());
			functional.setReferenceSurface(refSurface, as);
			showSurfaceChecker.setEnabled(true);
		}
		hif.checkContent();
		updateStates();
	}
	
	@Override
	public void install(Controller c) throws Exception {
		view = c.getPlugin(View.class);
		hif = c.getPlugin(HalfedgeInterface.class);
		refSurfaceLayer = new HalfedgeLayer(hif);
		refSurfaceLayer.setName("Reference Surface");
		refSurfaceLayer.setAppearance(refSurfaceAppearance);
		super.install(c);
	}
	
	@Override
	public void mainUIChanged(String lnfClass) {
		super.mainUIChanged(lnfClass);
		SwingUtilities.updateComponentTreeUI(chooser);
	}
	
	private SceneGraphComponent loadFile() {
		Window w = getWindowAncestor(view.getViewer().getViewingComponent());
		File file = null;
		File userDir = new File(System.getProperty("user.dir"));
		if (userDir.exists()) {
			chooser.setCurrentDirectory(userDir);
		}
		if (chooser.showOpenDialog(w) == APPROVE_OPTION) {
			file = chooser.getSelectedFile();
		}
		SceneGraphComponent c = null;
		if (file != null) {
			try {
				c = Readers.read(Input.getInput(file));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return c;
	}

}
