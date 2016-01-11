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
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.FILES_ONLY;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.plugin.job.AbstractJob;
import de.jreality.plugin.job.Job;
import de.jreality.plugin.job.JobQueuePlugin;
import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.GeometryPreviewerPanel;
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

	private JobQueuePlugin
		jobQueue = null;
	private Logger
		log = Logger.getLogger(ReferenceSurfaceOptimizer.class.getName());
	private ReferenceSurfaceFunctional<VVertex, VEdge, VFace>
		functional = new ReferenceSurfaceFunctional<VVertex, VEdge, VFace>();
	private JPanel
		panel = new JPanel();
	private JCheckBox
		showLayerChecker = new JCheckBox("Show Surface", false),
		wireFrameChecker = new JCheckBox("Wireframe", true);
	private JButton
		loadMeshButton = new JButton("Load Reference OBJ Mesh...");
	
	private JFileChooser 
		chooser = new JFileChooser();		
	private GeometryPreviewerPanel 
		previewPanel = new GeometryPreviewerPanel();
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
		panel.add(loadMeshButton, c2);
		panel.add(showLayerChecker,c1);
		panel.add(wireFrameChecker,c2);
		showLayerChecker.addActionListener(this);
		wireFrameChecker.addActionListener(this);
		loadMeshButton.addActionListener(this);
		
		chooser.setAccessory(previewPanel);
		chooser.addPropertyChangeListener(previewPanel);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new FileNameExtensionFilter("Reference Mesh OBJ (*.obj)", "obj"));
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
		if (loadMeshButton == e.getSource()) {
			final Window w = SwingUtilities.getWindowAncestor(panel);
			chooser.setDialogTitle("Import Into Layer");
			if (chooser.showOpenDialog(w) != APPROVE_OPTION) {
				return;
			}
			final File file = chooser.getSelectedFile();
			Job job = new AbstractJob() {
				@Override
				public String getJobName() {
					return "Load Reference Mesh";
				}
				@Override
				protected void executeJob() throws Exception {
					ReaderOBJ r = new ReaderOBJ();
					SceneGraphComponent root = r.read(file);
					Geometry g = SceneGraphUtility.getFirstGeometry(root);
					if (g == null) {
						log.warning("no geometry found in " + file);
						return;
					}
					if (g instanceof IndexedFaceSet) {
						IndexedFaceSet ifs = (IndexedFaceSet) g;
						IndexedFaceSetUtility.calculateAndSetNormals(ifs);
						refSurfaceLayer.set(ifs);
					}
				}
			};
			jobQueue.queueJob(job);
		}
		hif.checkContent();
		updateStates();
	}
	
	@Override
	public void install(Controller c) throws Exception {
		hif = c.getPlugin(HalfedgeInterface.class);
		jobQueue = c.getPlugin(JobQueuePlugin.class);
		c.getPlugin(VarylabMain.class);
		refSurfaceLayer = new HalfedgeLayer(hif);
		refSurfaceLayer.set(new VHDS());
		refSurfaceLayer.setName("Reference Surface");
		refSurfaceLayer.setAppearance(refSurfaceAppearance);
		super.install(c);
	}
	

}
