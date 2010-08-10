package de.varylab.varylab.plugin.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.ui.layout.WrapLayout;

public class VarylabCustomGUI extends Plugin implements ComponentListener {

	private View 
		view = null;
	private JPanel
		panel = new JPanel();
	
	private List<WidgetPlugin>
		widgets = new LinkedList<WidgetPlugin>();
	
	
	public VarylabCustomGUI() {
		panel.setOpaque(false);
		panel.setLayout(new WrapLayout(FlowLayout.LEADING, 2, 2));
	}
	
	
	private void updateLayout() {
		Component parent = panel.getParent();
		Component viewPanel = view.getViewer().getViewingComponent();
		Dimension size = view.getViewer().getViewingComponentSize();
		Point p = SwingUtilities.convertPoint(viewPanel, new Point(), parent);
		panel.setLocation(p);
		panel.setSize(size);
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
		Component viewComponent = view.getViewer().getViewingComponent();
		JFrame mainFrame = (JFrame)SwingUtilities.getWindowAncestor(viewComponent);
		JLayeredPane layers = mainFrame.getLayeredPane();
		layers.add(panel, JLayeredPane.MODAL_LAYER);
		viewComponent.addComponentListener(this);
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("VaryLab GUI", "Stefan Sechelmann");
	}
	
	
	public void addWidget(WidgetPlugin wp) {
		widgets.add(wp);
		panel.add(wp.getWidgetComponent());
		
	}
	
	public void removeWidgetPlugin(WidgetPlugin wp) {
		widgets.remove(wp);
		panel.remove(wp.getWidgetComponent());
	}


	@Override
	public void componentResized(ComponentEvent e) {
		updateLayout();
	}


	@Override
	public void componentMoved(ComponentEvent e) {
		updateLayout();
	}


	@Override
	public void componentShown(ComponentEvent e) {
		updateLayout();
	}


	@Override
	public void componentHidden(ComponentEvent e) {
		updateLayout();
	}
	

}
