package de.varylab.varylab.plugin.nurbs.plugin;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.Color;
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
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.ui.LayoutFactory;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;

public class PointSelectionPlugin extends ShrinkPanelPlugin implements HalfedgeListener, ActionListener {

	private HalfedgeInterface hif = null;
	private PointSelectionTool tool = new PointSelectionTool();
	private List<PointSelectionListener> listeners = new LinkedList<PointSelectionListener>();
	
	private JPanel panel = new JPanel();
	
	private List<double[]> points = new LinkedList<double[]>();
	private List<double[]> selectedPoints = new LinkedList<double[]>();
	
	private PointSelectionModel psm = new PointSelectionModel();
	private JTable selectedPointsTable = new JTable(psm);
	private JScrollPane selectedPointsPane = new JScrollPane(selectedPointsTable);
	private JCheckBox showBox = new JCheckBox("Show");
	private SceneGraphComponent selectedPointsComponent = new SceneGraphComponent("Selected Nurbs Points");
	private NURBSSurface surface;
	
	
	public PointSelectionPlugin() {
		tool.addActionListener(this);
		
		shrinkPanel.setName("Nurbs Selection");
		panel.setPreferredSize(new Dimension(250, 200));
		panel.setMinimumSize(new Dimension(250, 200));
		panel.setLayout(new GridBagLayout());
		panel.add(selectedPointsPane);
		
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		selectedPointsTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		selectedPointsTable.setRowHeight(22);
		selectedPointsTable.getDefaultEditor(Boolean.class).addCellEditorListener(new PointVisibilityListener());
		selectedPointsTable.setDefaultEditor(JButton.class, new ButtonCellEditor());
		selectedPointsTable.setDefaultRenderer(JButton.class, new ButtonCellRenderer());
		selectedPointsPane.setMinimumSize(new Dimension(200,150));
		panel.add(selectedPointsPane, rc);
		
		showBox.setSelected(true);
		showBox.addActionListener(this);
		panel.add(showBox, rc);
		
		shrinkPanel.add(panel);
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
		addTool(layer);
	}

	private void addTool(HalfedgeLayer layer) {
		NurbsUVAdapter nurbsAdapter = layer.getCurrentAdapters().query(NurbsUVAdapter.class);
		layer.addTemporaryGeometry(selectedPointsComponent);
		if(nurbsAdapter != null) {
			surface = nurbsAdapter.getSurface();
			List<SceneGraphPath> paths = SceneGraphUtility.getPathsToNamedNodes(layer.getLayerRoot(), "Geometry");
			SceneGraphComponent comp;
			for (SceneGraphPath path : paths) {
				comp = path.getLastComponent();
				if (!comp.getTools().contains(tool))
					comp.addTool(tool);
			}
		} else {
			surface = null;
		}
		tool.setSurface(surface);
		points.clear();
		selectedPoints.clear();
		selectedPointsComponent.removeAllChildren();
		psm.fireTableDataChanged();
	}

	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
	}

	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		addTool(active);
	}

	@Override
	public void layerCreated(HalfedgeLayer layer) {
		addTool(layer);
	}

	@Override
	public void layerRemoved(HalfedgeLayer layer) {
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
			points.add(pt);
			selectedPoints.add(pt);
			selectedPointsComponent.addChild(createPointComponent(pt));
			psm.fireTableDataChanged();
			firePointSelected(tool.getSelectedPoint());
		} else if(source == showBox) {
			selectedPointsComponent.setVisible(showBox.isSelected());
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
			boolean isVisible = !selectedPointsComponent.getChildComponent(row).isVisible();
			selectedPointsComponent.getChildComponent(row).setVisible(isVisible);
			if(!isVisible){
				selectedPoints.remove(points.get(row));
			} else {
				selectedPoints.add(points.get(row));
			}
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
		Appearance iAp = new Appearance();
		sgci.setAppearance(iAp);
		DefaultGeometryShader idgs = ShaderUtility.createDefaultGeometryShader(iAp, false);
		DefaultPointShader ipointShader = (DefaultPointShader)idgs.getPointShader();
		ipointShader.setDiffuseColor(Color.orange);
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
