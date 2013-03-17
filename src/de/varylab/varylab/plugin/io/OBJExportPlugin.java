package de.varylab.varylab.plugin.io;

import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.writer.WriterOBJ;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.flavor.UIFlavor;
import de.varylab.varylab.plugin.image.ImageHook;

public class OBJExportPlugin extends AlgorithmPlugin implements UIFlavor {

	private JFileChooser 
		saveChooser = new JFileChooser();
	private Scene 
		scene = null;
	private View 
		view = null;
	
	
	public OBJExportPlugin() {
		saveChooser.setDialogTitle("OBJ Files");
		saveChooser.setAcceptAllFileFilterUsed(false);
		saveChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		saveChooser.setMultiSelectionEnabled(false);
		File curDir = new File(System.getProperty("user.dir"));
		saveChooser.setCurrentDirectory(curDir);
		saveChooser.addChoosableFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj");
			}

			@Override
			public String getDescription() {
				return "Wavefront OBJ (*.obj)";
			}
			
		});
	}
	
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hcp) {
		Window w = SwingUtilities.getWindowAncestor(view.getCenterComponent());
		int result = saveChooser.showSaveDialog(w);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = saveChooser.getSelectedFile();
			if (!file.getName().toLowerCase().endsWith(".obj"))
				file = new File(file.getAbsolutePath() + ".obj");
			if (file.exists()) {
				int owr = JOptionPane.showConfirmDialog(w,
						"Do you want to overwrite the file: " + file + "?");
				if (owr != JOptionPane.OK_OPTION)
					return;
			}
			try {
				FileOutputStream fos = new FileOutputStream(file);
				WriterOBJ.write(scene.getContentComponent(), fos);
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(w, e1.getMessage());
			}
		}
	}

	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		scene = c.getPlugin(Scene.class);
		view = c.getPlugin(View.class);
	}
	

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.File;
	}
	
	@Override
	public String getAlgorithmName() {
		return "Export OBJ";
	}
	
	@Override
	public void mainUIChanged(String arg0) {
		SwingUtilities.updateComponentTreeUI(saveChooser);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Export OBJ");
		info.icon = ImageHook.getIcon("disk.png");
		return info;
	}
}
