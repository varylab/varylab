package de.varylab.varylab.math.dec;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.jtem.jrworkspace.plugin.Controller;

public class VectorFieldVisualizer extends VisualizerPlugin implements ChangeListener{
	
	private SceneGraphComponent 
		fieldComponent = new SceneGraphComponent("Vector fields");
	private SpinnerNumberModel
		lengthModel = new SpinnerNumberModel(1.0, -100.0, 100.0, 0.01),
		thicknessModel = new SpinnerNumberModel(0.01,0.0,1.0,0.01);
	private JSpinner
		lengthSpinner = new JSpinner(lengthModel),
		thicknessSpinner = new JSpinner(thicknessModel);
	private JPanel
		panel = new JPanel();
	private JTable
		fieldTable = new JTable();
	private JScrollPane
		fieldScrollPane = new JScrollPane(fieldTable);
	private Set<Adapter<?>>
		activeFields = new HashSet<Adapter<?>>();
	private HalfedgeInterface 
		hif = null;
	
	public VectorFieldVisualizer() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2);
	
		c.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(new JLabel("Length"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(lengthSpinner, c);
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(new JLabel("Thickness"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(thicknessSpinner, c);
		
		fieldTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
		fieldTable.getDefaultEditor(Boolean.class).addCellEditorListener(new VectorFieldActivationListener());
		fieldTable.setRowHeight(22);
//		fieldTable.getSelectionModel().addListSelectionListener(this);
		fieldTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		fieldTable.setBorder(BorderFactory.createEtchedBorder());
		panel.add(fieldScrollPane,c);
		
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		manager.update();
	}
	
	@Override
	public String getName() {
		return "Vector Field Visualizer";
	}
	
	@Override
	public JPanel getOptionPanel() {
		fieldTable.setModel(new VectorFieldTableModel());
		fieldTable.getColumnModel().getColumn(0).setMaxWidth(30);
		if(fieldTable.getRowCount() == 0) {
			return null;
		}
		panel.updateUI();
		return panel;
	}
	
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void initVisualization(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		for(SceneGraphComponent child : new LinkedList<SceneGraphComponent>(fieldComponent.getChildComponents())) {
			fieldComponent.removeChild(child);
		}
		
		if(activeFields.size()==0) {
			return;
		}
		
		for(Adapter<?> vf : activeFields) {
			fieldComponent.addChild(generateVectorFieldComponent((VectorFieldAdapter)vf,hds,a));
		}
	}

	private < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> SceneGraphComponent generateVectorFieldComponent(VectorFieldAdapter vf, HDS hds, AdapterSet a) {
		IndexedLineSetFactory ilf = new IndexedLineSetFactory();
		int numFaces = hds.numFaces();
		ilf.setVertexCount(2*numFaces);
		ilf.setEdgeCount(numFaces);
		int[][] edges = new int[numFaces][2];
		double[][] vertices = new double[2*numFaces][];
		int i = 0;
		for (F f: hds.getFaces()) {
			edges[i] = new int[]{i,numFaces+i};
			double[] v = vf.get(f, a);
			E e = f.getBoundaryEdge();
			vertices[i] = new double[3];
			int j = 0;
			do {
				Rn.add(vertices[i],vertices[i],a.get(Position.class,e.getTargetVertex(),double[].class));
				e = e.getNextEdge();
				j++;
			} while(e != f.getBoundaryEdge());
			Rn.times(vertices[i],1.0/j,vertices[i]);
			double[] lv = Rn.times(null, lengthModel.getNumber().doubleValue(), v);
			vertices[i + numFaces] = Rn.add(null, vertices[i], lv);
			i++;
		}
		ilf.setEdgeIndices(edges);
		ilf.setVertexCoordinates(vertices);
		ilf.update();
		
		BallAndStickFactory bsf = new BallAndStickFactory(ilf.getIndexedLineSet());
		// bsf.setBallRadius(.04);
		bsf.setShowBalls(false);
	    bsf.setStickRadius(thicknessModel.getNumber().doubleValue());
	    bsf.setShowArrows(true);
	    bsf.setArrowScale(.02);
	    bsf.setArrowSlope(1.5);
	    bsf.setArrowPosition(1);
		bsf.update();
		return bsf.getSceneGraphComponent();
	}
	
	
	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "normalLength", lengthModel.getNumber());
	}
	
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		lengthModel.setValue(c.getProperty(getClass(), "normalLength", lengthModel.getNumber()));
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		lengthSpinner.addChangeListener(this);
		hif = c.getPlugin(HalfedgeInterface.class);
	}
	
	@Override
	public SceneGraphComponent getComponent() {
		return fieldComponent;
	}

	private class VectorFieldTableModel extends DefaultTableModel {

		private static final long 
			serialVersionUID = 1L;
		
		@Override
		public int getRowCount() {
			return hif.getAdapters().queryAll(VectorField.class).size();
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0: return Boolean.class;
				case 1: return Adapter.class;
				default: return String.class;
			}
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			List<Adapter<?>> vectorFields = hif.getAdapters().queryAll(VectorField.class);
			if (row < 0 || row >= vectorFields.size()) {
				return "-";
			}
			Adapter<?> a = vectorFields.get(row);
			Object value = null;
			switch (column) {
				case 0: 
					return isActive(a);
				case 1:
					value = a;
					break;
				default: 
					value = "-";
					break;
			}
			return value;
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			switch (column) {
				case 0:
					return true;
				default: 
					return false;
			}
		}
		
		
	}
	
	private class VectorFieldActivationListener implements CellEditorListener {

		@Override
		public void editingCanceled(ChangeEvent e) {
		}

		@Override
		public void editingStopped(ChangeEvent e) {
			int row = fieldTable.getSelectedRow();
			List<Adapter<?>> vf = hif.getAdapters().queryAll(VectorField.class);
			Adapter<?> a = vf.get(row);
			setActive(a, !isActive(a));
			updateContent();
			fieldTable.revalidate();
		}

	}

	private void setActive(Adapter<?> a, boolean active) {
		if (active) {
			activeFields.add(a);
		} else {
			activeFields.remove(a);
		}
	}

	private boolean isActive(Adapter<?> a) {
		return activeFields.contains(a);
	}

//	@Override
//	public void valueChanged(ListSelectionEvent e) {
//		int row = fieldTable.getSelectedRow();
//		if (fieldTable.getRowSorter() != null) {
//			row = fieldTable.getRowSorter().convertRowIndexToModel(row);
//		}
//		List<Adapter<?>> vf = adapters.queryAll(VectorField.class);
//		if (row < 0 || row >= vf.size()) return;
//	}
}
