package de.varylab.varylab.plugin.meshoptimizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jreality.plugin.basic.View;
import de.jreality.reader.Readers;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.generic.NormalAdapter;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
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

	private JPanel
		panel = new JPanel();
	
	private JButton
		loadButton = new JButton("Load");
	
	private ReferenceSurfaceFunctional<VVertex, VEdge, VFace>
		functional = new ReferenceSurfaceFunctional<VVertex, VEdge, VFace>();
	
	private JFileChooser 
		chooser = FileLoaderDialog.createFileChooser();

	private JCheckBox
		showSurfaceChecker = new JCheckBox("Show Surface"),
		wireFrameChecker = new JCheckBox("Wireframe");
	
	private View view;

	private SceneGraphComponent refSGC;
	
	private VHDS refSurface = new VHDS();
	
	private ConverterJR2Heds 
		converter = new ConverterJR2Heds();

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
		showSurfaceChecker.addActionListener(this);
		wireFrameChecker.addActionListener(this);
		loadButton.addActionListener(this);
		
		chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
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

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(loadButton == src) {
			if(showSurfaceChecker.isSelected()) {
				showSurfaceChecker.setSelected(false);
				hif.getAuxComponent().removeChild(refSGC);
			}
			loadFile();
			AdapterSet as = new AdapterSet(new VPositionAdapter(), new NormalAdapter());
			converter.ifs2heds((IndexedFaceSet)SceneGraphUtility.getFirstGeometry(refSGC), refSurface, as);
			functional.setReferenceSurface(refSurface, as);
		} else if (showSurfaceChecker == src) {
			if(showSurfaceChecker.isSelected()) {
				hif.getAuxComponent().addChild(refSGC);
				wireFrameChecker.setEnabled(true);
				if(wireFrameChecker.isSelected()) {
					refSGC.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
				}
			} else {
				hif.getAuxComponent().removeChild(refSGC);
				wireFrameChecker.setEnabled(false);
			}
		} else if(wireFrameChecker == src) {
			if(wireFrameChecker.isSelected()) {
				refSGC.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, false);
			} else {
				refSGC.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, true);
			}
		}
		
	}
	
	@Override
	public void install(Controller c) throws Exception {
		view = c.getPlugin(View.class);
		hif = c.getPlugin(HalfedgeInterface.class);
		super.install(c);
	}
	
	@Override
	public void mainUIChanged(String arg0) {
		SwingUtilities.updateComponentTreeUI(chooser);
	}
	
	private void loadFile() {
		File file = null;
		if (chooser.showOpenDialog(view.getViewer().getViewingComponent()) == JFileChooser.APPROVE_OPTION) {
			file = chooser.getSelectedFile();
		}
		if (file != null) {
			try {
				refSGC = Readers.read(Input.getInput(file));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			refSGC = null;
		}
	}

}
