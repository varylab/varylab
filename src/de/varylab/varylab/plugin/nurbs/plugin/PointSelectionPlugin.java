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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.math.NurbsSurfaceUtility;
import de.varylab.varylab.plugin.nurbs.type.NurbsUVCoordinate;
import de.varylab.varylab.ui.DoubleArrayPrettyPrinter;
import de.varylab.varylab.ui.ListSelectRemoveTable;
import de.varylab.varylab.ui.ListSelectRemoveTableModel;

public class PointSelectionPlugin extends ShrinkPanelPlugin implements HalfedgeListener, ActionListener, TableModelListener {

	private HalfedgeInterface hif = null;
	private PointSelectionTool tool = new PointSelectionTool();
	private List<PointSelectionListener> listeners = new LinkedList<PointSelectionListener>();
	
	private JPanel panel = new JPanel();
	private PointGeneratorPanel pgp = new PointGeneratorPanel();
	

	private ListSelectRemoveTableModel<double[]> 
		activeModel = new ListSelectRemoveTableModel<double[]>("UV-Coordinates",new DoubleArrayPrettyPrinter());
	private ListSelectRemoveTable<double[]> 
		selectedPointsTable = new ListSelectRemoveTable<double[]>(activeModel);

	private Map<HalfedgeLayer, ListSelectRemoveTableModel<double[]>>
		layers2models = new HashMap<HalfedgeLayer, ListSelectRemoveTableModel<double[]>>();
	
	private JScrollPane selectedPointsPane = new JScrollPane(selectedPointsTable);
	
	private JButton distPointButton = new JButton("distPoints");
	private JButton uncheckButton = new JButton("None");
	private JButton checkButton = new JButton("All");
	private JButton removeSelectedButton = new JButton("Delete selected");
	private JButton 
		selectionButton = new JButton("Get selection"),
		equidistantPointsButton = new JButton("Equidistant points");
	
	private SpinnerNumberModel
		equidistantPointsModel = new SpinnerNumberModel(11, 2, 1000, 1);
		
	
	private JSpinner
		equidistantPointsSpinner = new JSpinner(equidistantPointsModel);
		
	
	private JCheckBox showBox = new JCheckBox("Show"),
			interactiveBox = new JCheckBox("Interactive Dragging");
	
	private SceneGraphComponent selectedPointsComponent = new SceneGraphComponent("Selected Nurbs Points");
	private NURBSSurface surface;
	private NurbsUVAdapter nurbsUVAdapter;
	

	private int numberOfPoints = 0;
	private boolean uDir = false;
	private boolean vDir = false;
	private boolean up = false;
	private boolean down = false;
	private boolean interactiveDragging = false;
	private double dist = 0.;
	private LinkedList<LinkedList<double[]>> commonPointList;
	

	
	private boolean startup = true;
	
	public PointSelectionPlugin() {
		tool.addActionListener(this);
		
		shrinkPanel.setName("Nurbs Selection");
		shrinkPanel.setLayout(new GridBagLayout());
		
		panel.setPreferredSize(new Dimension(250, 400));
		panel.setMinimumSize(new Dimension(250, 200));
		panel.setLayout(new GridBagLayout());
		panel.add(selectedPointsPane);
		
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		
		
		selectedPointsTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		activeModel.addTableModelListener(this);
		selectedPointsPane.setMinimumSize(new Dimension(200,150));
		panel.add(selectedPointsPane, rc);
		
		showBox.setSelected(true);
		showBox.addActionListener(this);
		
		interactiveBox.setSelected(false);
		interactiveBox.addActionListener(this);
		
		checkButton.addActionListener(this);
		uncheckButton.addActionListener(this);
		removeSelectedButton.addActionListener(this);
		selectionButton.addActionListener(this);
		equidistantPointsButton.addActionListener(this);
		distPointButton.addActionListener(this);
		
		initSelectedPointsComponent();
	
		panel.add(pgp,rc);
		panel.add(distPointButton,rc);
		panel.add(checkButton,lc);
		panel.add(uncheckButton,rc);
		panel.add(removeSelectedButton,lc);
		panel.add(selectionButton,rc);
		
		panel.add(equidistantPointsSpinner, lc);
		equidistantPointsSpinner.setEnabled(false);
		panel.add(equidistantPointsButton, rc);
		equidistantPointsButton.setEnabled(false);
		
		panel.add(showBox, lc);
		panel.add(interactiveBox, rc);
		
		
		shrinkPanel.add(panel,rc);
	}
	
	private class PointGeneratorPanel extends ShrinkPanel implements ActionListener, ChangeListener{
	
		private static final long 
			serialVersionUID = 1L;
		

		private JCheckBox upBox = new JCheckBox("up"),
						downBox = new JCheckBox("down"),
						uDirBox = new JCheckBox("u Direction"),
						vDirBox = new JCheckBox("v Direction");
		
		private SpinnerNumberModel distModel = new SpinnerNumberModel(0.5, 0.0, 1.0, 0.01),
						numberOfPointsModel = new SpinnerNumberModel(3, 1, 300, 1);
		
		private JSpinner distSpinner = new JSpinner(distModel),
				numberOfPointsSpinner = new JSpinner(numberOfPointsModel);
		
		
		public PointGeneratorPanel() {
			super("Point Generator");
			setShrinked(true);
			setLayout(new GridBagLayout());
			GridBagConstraints lc = LayoutFactory.createLeftConstraint();
			GridBagConstraints rc = LayoutFactory.createRightConstraint();

		
			add(new JLabel("Choose dist"), lc);
			add(distSpinner, rc);
			add(new JLabel("Choose Number Of Points"), lc);
			add(numberOfPointsSpinner, rc);
			add(uDirBox, lc);
			add(vDirBox, rc);
			add(upBox, lc);
			add(downBox, rc);
		
			uDirBox.addActionListener(this);
			vDirBox.addActionListener(this);
			upBox.addActionListener(this);
			downBox.addActionListener(this);
			distSpinner.addChangeListener(this);
			numberOfPointsSpinner.addChangeListener(this);
			
			
		}
		@Override
		public void stateChanged(ChangeEvent e) {
			
			if(distSpinner == e.getSource()){
				dist = distModel.getNumber().doubleValue();
			}
			if(numberOfPointsSpinner == e.getSource()){
				numberOfPoints = numberOfPointsModel.getNumber().intValue();
			}
			
		}
		
		private double getDist(){
			double min = Double.MAX_VALUE;
			for (double[] p : getSelectedPoints()) {
				if(uDir){
					double[] U = surface.getUKnotVector();
					double u0 = U[0];
					double um = U[U.length - 1];
					if(up){
						if(min > um - p[0]){
							min = um - p[0];
						}
					}
					if(down){
						if(min > p[0] - u0){
							min = p[0] - u0;
						}
					}
				}
				else if(vDir){
					double[] V = surface.getVKnotVector();
					double v0 = V[0];
					double vn = V[V.length - 1];
					if(up){
						if(min > vn - p[1]){
							min = vn - p[1];
						}
					}
					if(down){
						if(min > p[1] - v0){
							min = p[1] - v0;
						}
					}
				}
			}
			System.out.println("MIN = " + min);
			return distModel.getNumber().doubleValue() * min;
		}
	
		@Override
		public void actionPerformed(ActionEvent e){								
			numberOfPoints = numberOfPointsModel.getNumber().intValue();
			if(uDirBox.isSelected()){
				uDir = true;
			} else {
				uDir = false;
			}
			if(vDirBox.isSelected()){
				vDir = true;
			} else {
				vDir = false;
			}
			if(upBox.isSelected()){
				up = true;
			} else {
				up = false;
			}
			if(downBox.isSelected()){
				down = true;
			} else {
				down = false;
			}
			dist = getDist();
			if(interactiveBox.isSelected()){
				interactiveDragging = true;
			} else {
				interactiveDragging = false;
			}
		}
		
	}	
	
	
	
	private void initSelectedPointsComponent() {
		Appearance app = new Appearance();
		app.setAttribute(VERTEX_DRAW, true);
		app.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, ORANGE);
		app.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, ORANGE);
		selectedPointsComponent.setPickable(false);
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
			equidistantPointsSpinner.setEnabled(surface.isSurfaceOfRevolution());
			equidistantPointsButton.setEnabled(surface.isSurfaceOfRevolution());
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
			for(Vertex<?,?,?> v : hif.getSelection().getVertices()) {
				double[] pt = as.getD(NurbsUVCoordinate.class, v);
				if(!activeModel.contains(pt)) {
					activeModel.add(pt);
					selectedPointsComponent.addChild(createPointComponent(pt));
				}
			}
		} else if(source == equidistantPointsButton) {
			if(surface.isSurfaceOfRevolution()){
				for(double[] uv : getSelectedPoints()) {
					LinkedList<double[]> pts = NurbsSurfaceUtility.getEquidistantRotatedPoints(surface, equidistantPointsModel.getNumber().intValue(), uv);
					for (double[] pt : pts) {
						if(!activeModel.contains(pt)) {
							activeModel.add(pt);
							selectedPointsComponent.addChild(createPointComponent(pt));
						}
					}
				} 
			}
		} else if(source == distPointButton){
			LinkedList<double[]> selectedPoints = new LinkedList<>();
			for(double[] uv : getSelectedPoints()) {
				selectedPoints.add(uv);
			}
			commonPointList = NurbsSurfaceUtility.getCommonPointsFromSelection(surface, uDir, vDir, up, down, selectedPoints, dist, numberOfPoints);
			boolean firstPoint = true;
			for (LinkedList<double[]> list : commonPointList) {
				for (double[] pt : list) {
					if(!activeModel.contains(pt) && !firstPoint) {
						activeModel.add(pt);
						selectedPointsComponent.addChild(createPointComponent(pt));
					}
				}
				firstPoint = false;
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
	
	public boolean getUDir(){
		return uDir;
	}
	
	public boolean getVDir(){
		return vDir;
	}
	
	public LinkedList<LinkedList<double[]>> getCommonPointList(){
		return commonPointList;
	}
	
	public boolean getInteractiveDragging(){
		return interactiveDragging;
	}
	
//	public LinkedList<SelectedPoint> getSelectedPoints(){
//		return selctedPoints;
//	}
	
	
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
