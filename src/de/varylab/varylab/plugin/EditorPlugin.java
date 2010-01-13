package de.varylab.varylab.plugin;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jreality.plugin.JRViewerUtility;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.ui.EditorsToolBar;
import de.varylab.varylab.ui.image.ImageHook;

public abstract class EditorPlugin extends Plugin {

	private View
		view = null;
	private Content
		content = null;
	private ViewMenuBar
		viewMenuBar = null;
	private EditorsToolBar
		toolBar = null;
	private HalfedgeInterfacePlugin<VVertex, VEdge, VFace, VHDS> 
		hif = null;
	private static Icon
		defaultIcon = ImageHook.getIcon("cog_edit.png");
	
	private class EditAction extends AbstractAction {
	
		private static final long 
			serialVersionUID = 1L;
	
		public EditAction() {
			Icon icon = getPluginInfo().icon != null ? getPluginInfo().icon : defaultIcon;
			putValue(Action.SMALL_ICON, icon);
			putValue(Action.NAME, getPluginInfo().name);
			putValue(Action.SHORT_DESCRIPTION, getPluginInfo().name);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Window w = SwingUtilities.getWindowAncestor(view.getCenterComponent());
			int result = OK_OPTION;
			if (getDialogPanel() != null) {
				result = JOptionPane.showOptionDialog(
					w, getDialogPanel(), 
					getPluginInfo().name, 
					OK_CANCEL_OPTION, 
					PLAIN_MESSAGE, 
					getPluginInfo().icon, 
					new String[] {"Edit", "Cancel"}, 
					"Edit"
				);
			}
			if (result == OK_OPTION) {
				edit(content, hif);
			}
		}
		
	}
	
	protected abstract void edit(Content content, HalfedgeInterfacePlugin<VVertex, VEdge, VFace, VHDS> hif);
	
	protected abstract String[] getMenuPath();
	
	protected JPanel getDialogPanel() {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
		content = JRViewerUtility.getContentPlugin(c);
		viewMenuBar = c.getPlugin(ViewMenuBar.class);
		toolBar = c.getPlugin(EditorsToolBar.class);
		hif = c.getPlugin(HalfedgeInterfacePlugin.class);
		EditAction action = new EditAction();
		String[] menuPath = getMenuPath();
		String[] menuPathLong = new String[menuPath.length + 1];
		System.arraycopy(menuPath, 0, menuPathLong, 1, menuPath.length);
		menuPathLong[0] = "Editors";
		viewMenuBar.addMenuItem(getClass(), 0, action, menuPathLong);
		toolBar.addAction(getClass(), 0, action);
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		viewMenuBar.removeAll(getClass());
		toolBar.removeAll(getClass());
	}
	
}
