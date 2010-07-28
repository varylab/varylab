package de.varylab.varylab.plugin;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.writer.WriterOBJ;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.flavor.UIFlavor;
import de.varylab.varylab.plugin.ui.ExportToolBar;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class OBJExportPlugin extends Plugin implements UIFlavor {

	private ExportToolBar toolBar;

	private JFileChooser 
		saveChooser = new JFileChooser();
	private Icon
		defaultIcon = ImageHook.getIcon("disk.png");

	private View view;

//	private HalfedgeInterface hif;

	private Scene scene;
	
	private class ExportAction extends AbstractAction {
		
		private static final long 
			serialVersionUID = 1L;
		
//		private ConverterHeds2JR
//			converter = new ConverterHeds2JR();
		
		public ExportAction() {
			Icon icon = getPluginInfo().icon != null ? getPluginInfo().icon : defaultIcon;
			putValue(Action.SMALL_ICON, icon);
			putValue(Action.NAME, getPluginInfo().name);
			putValue(Action.SHORT_DESCRIPTION, getPluginInfo().name);
			saveChooser.setDialogTitle("OBJ Files");
			saveChooser.setAcceptAllFileFilterUsed(false);
			saveChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			saveChooser.setMultiSelectionEnabled(false);
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
		public void actionPerformed(ActionEvent e) {
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
//					WriterOBJ.write(converter.heds2ifs(hif.get(), hif.getAdapters(), null), fos);
					WriterOBJ.write(scene.getContentComponent(),fos);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(w, e1.getMessage());
				}
			}
		}
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		scene = c.getPlugin(Scene.class);
		view = c.getPlugin(View.class);
		toolBar = c.getPlugin(ExportToolBar.class);
		toolBar.addAction(getClass(), 0, new ExportAction());
		super.install(c);
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		toolBar.removeAll(getClass());
	}
	
	@Override
	public void mainUIChanged(String arg0) {
		SwingUtilities.updateComponentTreeUI(saveChooser);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Export OBJ");
		return info;
	}
}
