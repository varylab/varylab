package de.varylab.varylab.plugin.nurbs.plugin;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static java.awt.Color.ORANGE;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import de.jreality.geometry.PointSetFactory;
import de.jreality.plugin.basic.View;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.ui.LayoutFactory;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NurbsUVCoordinate;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;

public class PointSelectionPlugin extends ShrinkPanelPlugin implements HalfedgeListener, ActionListener {

	private HalfedgeInterface hif = null;
	private PointSelectionTool tool = new PointSelectionTool();
	private List<PointSelectionListener> listeners = new LinkedList<PointSelectionListener>();
	
	private JPanel panel = new JPanel();
	
	private LinkedList<double[]> points = new LinkedList<double[]>();
	private LinkedList<double[]> selectedPoints = new LinkedList<double[]>();
	
	private PointSelectionModel psm = new PointSelectionModel();
	private JTable selectedPointsTable = new JTable(psm);
	private JScrollPane selectedPointsPane = new JScrollPane(selectedPointsTable);
	
	private JButton uncheckButton = new JButton("None");
	private JButton checkButton = new JButton("All");
	private JButton removeSelectedButton = new JButton("Delete selected");
	private JButton selectionButton = new JButton("Get selection");
	
	private JCheckBox showBox = new JCheckBox("Show");
	
	private SceneGraphComponent selectedPointsComponent = new SceneGraphComponent("Selected Nurbs Points");
	private NURBSSurface surface;
	private NurbsUVAdapter nurbsUVAdapter;
	
	
	public PointSelectionPlugin() {
		tool.addActionListener(this);
		
		shrinkPanel.setName("Nurbs Selection");
		shrinkPanel.setLayout(new GridBagLayout());
		
		panel.setPreferredSize(new Dimension(250, 200));
		panel.setMinimumSize(new Dimension(250, 200));
		panel.setLayout(new GridBagLayout());
		panel.add(selectedPointsPane);
		
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		
		selectedPointsTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		selectedPointsTable.setRowHeight(22);
		selectedPointsTable.getDefaultEditor(Boolean.class).addCellEditorListener(new PointVisibilityListener());
		selectedPointsTable.setDefaultEditor(JButton.class, new ButtonCellEditor());
		selectedPointsTable.setDefaultRenderer(JButton.class, new ButtonCellRenderer());
		selectedPointsTable.getColumnModel().getColumn(3).setMaxWidth(22);
		selectedPointsTable.getColumnModel().getColumn(3).setPreferredWidth(22);
		selectedPointsTable.getColumnModel().getColumn(0).setMaxWidth(22);
		selectedPointsTable.getColumnModel().getColumn(0).setPreferredWidth(22);
		
		selectedPointsPane.setMinimumSize(new Dimension(200,150));
		panel.add(selectedPointsPane, rc);
		
		showBox.setSelected(true);
		showBox.addActionListener(this);
		
		checkButton.addActionListener(this);
		uncheckButton.addActionListener(this);
		removeSelectedButton.addActionListener(this);
		selectionButton.addActionListener(this);
		
		initSelectedPointsComponent();
		panel.add(checkButton,lc);
		panel.add(uncheckButton,rc);
		panel.add(removeSelectedButton,lc);
		panel.add(selectionButton,rc);
		panel.add(showBox, rc);
		
		shrinkPanel.add(panel,rc);
	}
	
	private void initSelectedPointsComponent() {
		Appearance app = new Appearance();
		app.setAttribute(VERTEX_DRAW, true);
		app.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, ORANGE);
		app.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, ORANGE);
		selectedPointsComponent.setAppearance(app);
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addHalfedgeListener(this);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
	}

	@Override
	public PluginInfo getPluginInfo() {
		return super.getPluginInfo();
	}

	@Override
	public void dataChanged(HalfedgeLayer layer) {
		layer.removeTemporaryGeometry(selectedPointsComponent);
		layer.addTemporaryGeometry(selectedPointsComponent);
		addTool(layer);
		updateTool(layer);
	}

	private void updateTool(HalfedgeLayer layer) {
		nurbsUVAdapter = layer.getCurrentAdapters().query(NurbsUVAdapter.class);
		if(nurbsUVAdapter==null) {
			nurbsUVAdapter = layer.getActiveAdapters().query(NurbsUVAdapter.class);
		}
		
		if(nurbsUVAdapter != null) {
			surface = nurbsUVAdapter.getSurface();
		} else {
			surface = null;
		}
		tool.setSurface(surface);
		points.clear();
		selectedPoints.clear();
		selectedPointsComponent.removeAllChildren();
		psm.fireTableDataChanged();
	}

	private void addTool(HalfedgeLayer layer) {
		List<SceneGraphPath> paths = SceneGraphUtility.getPathsToNamedNodes(layer.getLayerRoot(), "Geometry");
		SceneGraphComponent comp;
		for (SceneGraphPath path : paths) {
			comp = path.getLastComponent();
			if (!comp.getTools().contains(tool))
				comp.addTool(tool);
		}
	}
	
	private void removeTool(HalfedgeLayer layer) {
		List<SceneGraphPath> paths = SceneGraphUtility.getPathsToNamedNodes(layer.getLayerRoot(), "Geometry");
		SceneGraphComponent comp;
		for (SceneGraphPath path : paths) {
			comp = path.getLastComponent();
			comp.removeTool(tool);
		}
	}

	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
		updateTool(layer);
	}

	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		old.removeTemporaryGeometry(selectedPointsComponent);
		active.addTemporaryGeometry(selectedPointsComponent);
		removeTool(old);
		addTool(active);
		updateTool(active);
	}

	@Override
	public void layerCreated(HalfedgeLayer layer) {
		addTool(layer);
	}

	@Override
	public void layerRemoved(HalfedgeLayer layer) {
		removeTool(layer);
	}

	public void addPointSelectionListener(PointSelectionListener psl) {
		listeners.add(psl);
	}
	
	public void removePointSelectionListener(PointSelectionListener psl) {
		listeners.remove(psl);
	}
	
	public void firePointSelected(final double[] uv) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				synchronized (listeners) {
					for (PointSelectionListener l : new LinkedList<PointSelectionListener>(listeners)) {
						l.pointSelected(uv);
					}
				}				
			}
		};
		EventQueue.invokeLater(r);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source == tool) {
			double[] pt = tool.getSelectedPoint();
			if(!points.contains(pt)) {
				points.add(pt);
			}
			selectedPoints.add(pt);
			selectedPointsComponent.addChild(createPointComponent(pt));
			psm.fireTableDataChanged();
			firePointSelected(tool.getSelectedPoint());
		} else if(source == showBox) {
			selectedPointsComponent.setVisible(showBox.isSelected());
		} else if(source == checkButton) {
			selectedPoints.clear();
			selectedPoints.addAll(points);
		} else if(source == uncheckButton) {
			selectedPoints.clear();
		} else if(source == removeSelectedButton) {
			for(double[] selPt : selectedPoints) {
				points.remove(selPt);
			}
			selectedPoints.clear();
			resetSelectedPointsComponent();
		} else if(source == selectionButton) {
			AdapterSet as = hif.getActiveAdapters();
			as.addAll(hif.getAdapters());
			for(Vertex<?,?,?> v : hif.getSelection().getVertices()) {
				double[] pt = as.getD(NurbsUVCoordinate.class, v);
				if(!points.contains(pt)) {
					points.add(pt);
				}
			}
		}
		psm.fireTableDataChanged();
	}

	private void resetSelectedPointsComponent() {
		selectedPointsComponent.removeAllChildren();
		for(double[] pt : points) {
			SceneGraphComponent ptComponent = createPointComponent(pt);
			ptComponent.setVisible(selectedPoints.contains(pt));
			selectedPointsComponent.addChild(ptComponent);
		}
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	private class PointSelectionModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		private String[] columnNames = {" ", "U", "V", " "};
		
		@Override
		public String getColumnName(int col) {
			return columnNames[col].toString();
	    }
		
	    @Override
		public int getRowCount() { 
	    	return (points==null)?0:points.size();
	    }
	    
	    @Override
		public int getColumnCount() { 
	    	return columnNames.length;
	    }
	    
	    @Override
		public Object getValueAt(int row, int col) {
	        if(col == 1) {
	        	return (points.get(row))[0];
	        }
	        if(col == 2) {
	        	return (points.get(row))[1];
	        }
	        if(col == 3) {
	        	return new RemoveButton(row);
	        }
	        return selectedPoints.contains(points.get(row));
	    }
	    
	    @Override
		public boolean isCellEditable(int row, int col) {
	    	switch (col) {
			case 0:
			case 3:
				return true;
			default:
				return false;
			}
	    }
	    
	    @Override
		public void setValueAt(Object value, int row, int col) {
	    }	
	    
	    @Override
	    public Class<?> getColumnClass(int col) {
	    	switch (col) {
			case 0:
				return Boolean.class;
			case 1:
			case 2:
				return double.class;
			case 3:
				return JButton.class;
			default:
				return String.class;
			}
	    }
	}
	
	public List<double[]> getSelectedPoints() {
		return selectedPoints ;
	}
	
	private class PointVisibilityListener implements CellEditorListener {

		@Override
		public void editingStopped(ChangeEvent e) {
			int row = selectedPointsTable.getSelectedRow();
			if (selectedPointsTable.getRowSorter() != null) {
				row = selectedPointsTable.getRowSorter().convertRowIndexToModel(row);
			}
			boolean isVisible = isSelected(row);
			if(isVisible){
				selectedPoints.remove(points.get(row));
				selectedPointsComponent.getChildComponent(row).setVisible(false);
			} else {
				selectedPoints.add(points.get(row));
				selectedPointsComponent.getChildComponent(row).setVisible(true);
			}
			
		}

		private boolean isSelected(int row) {
			return selectedPoints.contains(points.get(row));
		}

		@Override
		public void editingCanceled(ChangeEvent e) {
		}

	}

	private SceneGraphComponent createPointComponent(double[] uv) {
		PointSetFactory psfi = new PointSetFactory();
		psfi.setVertexCount(1);
		psfi.setVertexCoordinates(surface.getSurfacePoint(uv[0], uv[1]));
		psfi.update();
		SceneGraphComponent sgci = new SceneGraphComponent("uv" + Arrays.toString(uv));
		sgci.setGeometry(psfi.getGeometry());
		return sgci;
	}
	
	private void removePoint(int index) {
		double[] pt = points.remove(index);
		selectedPoints.remove(pt);
		selectedPointsComponent.removeChild(selectedPointsComponent.getChildComponent(index));
		psm.fireTableDataChanged();
	}

	private class ButtonCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		private JButton renderButton = new JButton();
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (value instanceof JButton) {
				JButton buttonValue = (JButton)value;
				renderButton.setIcon(buttonValue.getIcon());
				renderButton.setText(buttonValue.getText());
				return renderButton;
			} else {
				return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
			}
		}
		
		@Override
		public void updateUI() {
			super.updateUI();
			if (renderButton != null) {
				renderButton.updateUI();
			}
		}
		
	}
	
	private class RemoveButton extends JButton implements ActionListener {

		private static final long serialVersionUID = 1L;

		private int row = 0;
		
		public RemoveButton(int row) {
			super(ImageHook.getIcon("remove.png"));
			setSize(16,16);
			this.row=row;
			addActionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			removePoint(row);
		}

	}
	
	private class ButtonCellEditor extends AbstractCellEditor implements TableCellEditor {

		private static final long 	
			serialVersionUID = 1L;
		private JLabel
			defaultEditor = new JLabel("-");
		private Object 
			activeValue = null;
		
		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			this.activeValue = value;
			if (value instanceof Component) {
				return (Component)value;
			}
			return defaultEditor;
		}
		@Override
		public Object getCellEditorValue() {
			return activeValue;
		}
		
	}
	
	
}
