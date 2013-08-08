package de.varylab.varylab.plugin.nurbs.plugin;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static java.awt.Color.ORANGE;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

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
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NurbsUVCoordinate;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.ui.ButtonCellEditor;
import de.varylab.varylab.ui.ButtonCellRenderer;
import de.varylab.varylab.ui.DoubleArrayPrettyPrinter;
import de.varylab.varylab.ui.ListSelectRemoveTableModel;

public class PointSelectionPlugin extends ShrinkPanelPlugin implements HalfedgeListener, ActionListener, TableModelListener {

	private HalfedgeInterface hif = null;
	private PointSelectionTool tool = new PointSelectionTool();
	private List<PointSelectionListener> listeners = new LinkedList<PointSelectionListener>();
	
	private JPanel panel = new JPanel();
	
	private ListSelectRemoveTableModel<double[]> 
		psm = new ListSelectRemoveTableModel<double[]>("UV-Coordinate",new DoubleArrayPrettyPrinter());
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
	
	private boolean startup = true;
	
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
		selectedPointsTable.setDefaultEditor(JButton.class, new ButtonCellEditor());
		selectedPointsTable.setDefaultRenderer(JButton.class, new ButtonCellRenderer());
		selectedPointsTable.getColumnModel().getColumn(2).setMaxWidth(22);
		selectedPointsTable.getColumnModel().getColumn(2).setPreferredWidth(22);
		selectedPointsTable.getColumnModel().getColumn(0).setMaxWidth(22);
		selectedPointsTable.getColumnModel().getColumn(0).setPreferredWidth(22);
		
		psm.addTableModelListener(this);
		
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
		if(startup) {
			layer.addTemporaryGeometry(selectedPointsComponent);
			startup = false;
		}
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
		psm.clear();
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
			if(!psm.contains(pt)) {
				psm.add(pt);
				selectedPointsComponent.addChild(createPointComponent(pt));
			}
			firePointSelected(tool.getSelectedPoint());
		} else if(source == showBox) {
			selectedPointsComponent.setVisible(showBox.isSelected());
		} else if(source == checkButton) {
			psm.checkAll();
			for(SceneGraphComponent child : selectedPointsComponent.getChildComponents()) {
				child.setVisible(true);
			}
		} else if(source == uncheckButton) {
			psm.clearChecked();
			for(SceneGraphComponent child : selectedPointsComponent.getChildComponents()) {
				child.setVisible(false);
			}
		} else if(source == removeSelectedButton) {
			psm.removeChecked();
			resetSelectedPointsComponent();
		} else if(source == selectionButton) {
			AdapterSet as = hif.getActiveAdapters();
			as.addAll(hif.getAdapters());
			for(Vertex<?,?,?> v : hif.getSelection().getVertices()) {
				double[] pt = as.getD(NurbsUVCoordinate.class, v);
				if(!psm.contains(pt)) {
					psm.add(pt);
					selectedPointsComponent.addChild(createPointComponent(pt));
				}
			}
		}
		psm.fireTableDataChanged();
	}

	private void resetSelectedPointsComponent() {
		selectedPointsComponent.removeAllChildren();
		for(double[] pt : psm.getList()) {
			SceneGraphComponent ptComponent = createPointComponent(pt);
			ptComponent.setVisible(psm.isChecked(pt));
			selectedPointsComponent.addChild(ptComponent);
		}
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	public List<double[]> getSelectedPoints() {
		return psm.getChecked();
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
	
	@Override
	public void tableChanged(TableModelEvent e) {
		resetSelectedPointsComponent();
	}
	
}
