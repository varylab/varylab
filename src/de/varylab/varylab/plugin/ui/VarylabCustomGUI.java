package de.varylab.varylab.plugin.ui;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import com.sun.awt.AWTUtilities;

import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.ui.layout.WrapLayout;

public class VarylabCustomGUI extends Plugin implements ComponentListener {

	private View 
		view = null;
	private JWindow 
		glassFrame = null;
	
	private List<WidgetPlugin>
		widgets = new LinkedList<WidgetPlugin>();
	
	
	public VarylabCustomGUI() {
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
		view.getCenterComponent().addComponentListener(this);
		Window parent = SwingUtilities.getWindowAncestor(view.getCenterComponent());
		parent.addComponentListener(this);
		view.getContentPanel().addComponentListener(this);
		glassFrame = new JWindow(parent);
		AWTUtilities.setWindowOpaque(glassFrame, false);
		EventForwarder forwarder = new EventForwarder(view.getViewer().getViewingComponent());
		glassFrame.addMouseListener(forwarder);
		glassFrame.addMouseMotionListener(forwarder);
		glassFrame.addMouseWheelListener(forwarder);
		glassFrame.addKeyListener(forwarder);
		glassFrame.setLayout(new WrapLayout(FlowLayout.LEADING, 0, 0));
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("VaryLab GUI", "Stefan Sechelmann");
	}

	
	private void updateWindow() {
		Point p = view.getContentPanel().getLocationOnScreen();
		Dimension size = view.getContentPanel().getSize();
		glassFrame.setLocation(p);
		glassFrame.setSize(size);
		glassFrame.setVisible(true);
	}
	
	
	
	public void addWidget(WidgetPlugin wp) {
		widgets.add(wp);
		glassFrame.add(wp.getWidgetComponent());
		
	}
	
	public void removeWidgetPlugin(WidgetPlugin wp) {
		widgets.remove(wp);
		glassFrame.remove(wp.getWidgetComponent());
	}
	
	
	private class EventForwarder implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

		private Component
			target = null;
		
		public EventForwarder(Component target) {
			this.target = target;
		}

		private void forward(AWTEvent eo) {
			target.dispatchEvent(eo);
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
			forward(e);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			forward(e);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			forward(e);			
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			forward(e);			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			forward(e);			
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			forward(e);			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			forward(e);			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			forward(e);			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			forward(e);			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			forward(e);			
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			forward(e);				
		}
		
	}
	

	@Override
	public void componentResized(ComponentEvent e) {
		updateWindow();
	}


	@Override
	public void componentMoved(ComponentEvent e) {
		updateWindow();
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

}
