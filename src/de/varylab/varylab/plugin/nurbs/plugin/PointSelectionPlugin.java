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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurve;
import de.varylab.varylab.plugin.nurbs.type.NurbsUVCoordinate;
import de.varylab.varylab.ui.DoubleArrayPrettyPrinter;
import de.varylab.varylab.ui.ListSelectRemoveTable;
import de.varylab.varylab.ui.ListSelectRemoveTableModel;

public class PointSelectionPlugin extends ShrinkPanelPlugin implements HalfedgeListener, ActionListener, TableModelListener {

	private HalfedgeInterface hif = null;
	private PointSelectionTool tool = new PointSelectionTool();
	private List<PointSelectionListener> listeners = new LinkedList<PointSelectionListener>();
	
	private JPanel panel = new JPanel();
	

	private ListSelectRemoveTableModel<double[]> 
		activeModel = new ListSelectRemoveTableModel<double[]>("UV-Coordinates",new DoubleArrayPrettyPrinter());
	private ListSelectRemoveTable<double[]> 
		selectedPointsTable = new ListSelectRemoveTable<double[]>(activeModel);

	private Map<HalfedgeLayer, ListSelectRemoveTableModel<double[]>>
		layers2models = new HashMap<HalfedgeLayer, ListSelectRemoveTableModel<double[]>>();
	
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
		activeModel.addTableModelListener(this);
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
		return new PluginInfo("Nurbs surface point selection", "Nurbs team");
	}

	@Override
	public void dataChanged(HalfedgeLayer layer) {
		layer.addTemporaryGeometry(selectedPointsComponent);
		if(startup) {
			addTool(layer);
			startup = false;
		}
		if(activeModel == null) {
			if(layers2models.containsKey(layer)) {
				activeModel = layers2models.get(layer);
				activeModel.clear();
			} else {
				activeModel = new ListSelectRemoveTableModel<double[]>("UV-coordinates", new DoubleArrayPrettyPrinter());
				activeModel.addTableModelListener(this);
				layers2models.put(layer,activeModel);
			}
		} else {
			activeModel.clear();
		}
		updateTool(layer);
		activeModel.fireTableDataChanged();
	}

	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
		updateTool(layer);
		if(tool.getSurface() == null) {
			activeModel.clear();
			activeModel.fireTableDataChanged();
		}
	}

	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		if(old == active) {
			return;
		}
		old.removeTemporaryGeometry(selectedPointsComponent);
		active.addTemporaryGeometry(selectedPointsComponent);
		layers2models.put(old,activeModel);
		if(layers2models.containsKey(active)) {
			activeModel = layers2models.get(active);
		} else {
			System.err.println("this should not happen!");
		}
		selectedPointsTable.setModel(activeModel);
		removeTool(old);
		addTool(active);
		updateTool(active);
		activeModel.fireTableDataChanged();
	}

	@Override
	public void layerCreated(HalfedgeLayer layer) {
		ListSelectRemoveTableModel<double[]> newModel = new ListSelectRemoveTableModel<double[]>("UV-coordinates", new DoubleArrayPrettyPrinter());
		newModel.addTableModelListener(this);
		layers2models.put(layer,newModel);
	}

	@Override
	public void layerRemoved(HalfedgeLayer layer) {
		layers2models.remove(layer);
		removeTool(layer);
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

	private void updateTool(HalfedgeLayer layer) {
		nurbsUVAdapter = layer.getAdapters().query(NurbsUVAdapter.class);
		if(nurbsUVAdapter==null) {
			nurbsUVAdapter = layer.getActiveAdapters().query(NurbsUVAdapter.class);
		}
		
		if(nurbsUVAdapter != null) {
			surface = nurbsUVAdapter.getSurface();
		} else {
			surface = null;
		}
		tool.setSurface(surface);
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
			if(!activeModel.contains(pt)) {
				activeModel.add(pt);
				selectedPointsComponent.addChild(createPointComponent(pt));
			}
			firePointSelected(tool.getSelectedPoint());
		} else if(source == showBox) {
			selectedPointsComponent.setVisible(showBox.isSelected());
		} else if(source == checkButton) {
			activeModel.checkAll();
			for(SceneGraphComponent child : selectedPointsComponent.getChildComponents()) {
				child.setVisible(true);
			}
		} else if(source == uncheckButton) {
			activeModel.clearChecked();
			for(SceneGraphComponent child : selectedPointsComponent.getChildComponents()) {
				child.setVisible(false);
			}
		} else if(source == removeSelectedButton) {
			activeModel.removeChecked();
			resetSelectedPointsComponent();
		} else if(source == selectionButton) {
			AdapterSet as = hif.getActiveAdapters();
			as.addAll(hif.getAdapters());
			if(surface.isSurfaceOfRevolution() && hif.getSelection().getVertices().size() == 1){
				LinkedList<Vertex<?,?,?> > list = new LinkedList<Vertex<?,?,?>>();
				for (Vertex<?, ?, ?> v : hif.getSelection().getVertices()) {
					list.add(v);
				}
						
				Vertex<?,?,?> v = list.getFirst();
				double[] initialPoint = as.getD(NurbsUVCoordinate.class, v);
				LinkedList<double[]> pts = IntegralCurve.getEquidistantRotatedPoints(surface, 22, initialPoint);
				for (double[] pt : pts) {
					if(!activeModel.contains(pt)) {
						activeModel.add(pt);
						selectedPointsComponent.addChild(createPointComponent(pt));
					}
				}
			}
			else{
				for(Vertex<?,?,?> v : hif.getSelection().getVertices()) {
					double[] pt = as.getD(NurbsUVCoordinate.class, v);
					if(!activeModel.contains(pt)) {
						activeModel.add(pt);
						selectedPointsComponent.addChild(createPointComponent(pt));
					}
				}
			}
			
		}
		activeModel.fireTableDataChanged();
	}

	private void resetSelectedPointsComponent() {
		selectedPointsComponent.removeAllChildren();
		for(double[] pt : activeModel.getList()) {
			SceneGraphComponent ptComponent = createPointComponent(pt);
			ptComponent.setVisible(activeModel.isChecked(pt));
			selectedPointsComponent.addChild(ptComponent);
		}
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	public List<double[]> getSelectedPoints() {
		return activeModel.getChecked();
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
		selectedPointsTable.adjustColumnSizes();
	}
	
}
