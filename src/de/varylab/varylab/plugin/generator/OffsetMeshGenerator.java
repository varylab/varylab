package de.varylab.varylab.plugin.generator;

import static javax.swing.BorderFactory.createTitledBorder;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.jreality.math.Rn;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.VectorField;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;

public class OffsetMeshGenerator extends AlgorithmDialogPlugin implements ChangeListener, ActionListener, ListSelectionListener {

	private JPanel	
		optionsPanel = new JPanel();
	private JList<Adapter<double[]>>
		adapterList = new JList<>();
	private JList<HalfedgeLayer>
		layerList = new JList<>();
	private	SpinnerNumberModel
		scaleModel = new SpinnerNumberModel(0.1, -10000.0, 10000.0, 0.01); 
	private JSpinner
		scaleSpinner = new JSpinner(scaleModel);
	private JRadioButton
		vectorButton = new JRadioButton("Use Vector Data", true),
		geometryButton = new JRadioButton("Use Geometry Data");
	private ButtonGroup
		dataGroup = new ButtonGroup();
		
	
	private HalfedgeLayer
		offsetLayer = null;
	private AdapterSet
		aSet = null;
	private HalfEdgeDataStructure<?, ?, ?>
		baseHDS = null;
	
	public OffsetMeshGenerator() {
		optionsPanel.setLayout(new GridBagLayout());
		optionsPanel.setPreferredSize(new Dimension(200, 250));
		
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		
		rc.weighty = 1.0;
		adapterList.setSelectionMode(SINGLE_SELECTION);
		adapterList.setBorder(createTitledBorder("Offset Vector"));
		optionsPanel.add(adapterList, rc);
		
		layerList.setSelectionMode(SINGLE_SELECTION);
		layerList.setBorder(createTitledBorder("Offset Geometry"));
		optionsPanel.add(layerList, rc);
	
		rc.weighty = 0.0;
		optionsPanel.add(vectorButton, lc);
		optionsPanel.add(geometryButton, rc);
		
		optionsPanel.add(new JLabel("Scale"), lc);
		optionsPanel.add(scaleSpinner, rc);
		scaleSpinner.addChangeListener(this);
		
		dataGroup.add(vectorButton);
		dataGroup.add(geometryButton);
		vectorButton.addActionListener(this);
		geometryButton.addActionListener(this);
		layerList.addListSelectionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		updateOffsetMesh();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		updateOffsetMesh();
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		HalfedgeLayer l = layerList.getSelectedValue();
		boolean compatible = l.get().numVertices() == baseHDS.numVertices();
		geometryButton.setEnabled(compatible);
		if (!compatible) {
			vectorButton.setSelected(true);
		}
		updateOffsetMesh();
	}
	

	@Override
	protected JPanel getDialogPanel() {
		return optionsPanel;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeBeforeDialog(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		this.offsetLayer = new HalfedgeLayer(hi);
		this.offsetLayer.setName("Offset Mesh");
		this.baseHDS = hds;
		this.aSet = a;
		hi.addLayer(offsetLayer);
		offsetLayer.set(hds);
		
		DefaultListModel<Adapter<double[]>> model = new DefaultListModel<>();
		TreeSet<Adapter<double[]>> dataSet = new TreeSet<>();
		dataSet.addAll(a.queryAll(VectorField.class, hds.getVertexClass(), double[].class));
		dataSet.addAll(a.queryAll(Normal.class, hds.getVertexClass(), double[].class));
		for (Adapter<double[]> d : dataSet) {
			model.addElement(d);
		}
		adapterList.setModel(model);
		adapterList.setSelectedIndex(0);
		
		DefaultListModel<HalfedgeLayer> layerModel = new DefaultListModel<>();
		for (HalfedgeLayer l : hi.getAllLayers()) {
			layerModel.addElement(l);
		}
		layerList.setModel(layerModel);
		layerList.setSelectedIndex(0);

		updateOffsetMesh();
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialogCancel(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		hi.removeLayer(offsetLayer);
	}
	
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F> , 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void updateOffsetMesh() {
		double scale = scaleModel.getNumber().doubleValue();
		VHDS offsetMesh = new VHDS(); 
		baseHDS.createCombinatoriallyEquivalentCopy(offsetMesh);
		for (Vertex<?, ?, ?> v : baseHDS.getVertices()) {
			VVertex vv = offsetMesh.getVertex(v.getIndex());
			double[] vec = getVectorForVertex(vv);
			double[] nvec = Rn.normalize(null, vec);
			double[] pos = aSet.getD(Position3d.class, v);
			double[] offsetPos = {pos[0] + scale * nvec[0], pos[1] + scale * nvec[1], pos[2] + scale * nvec[2]};
			aSet.set(Position.class, vv, offsetPos);
		}
		offsetLayer.set(offsetMesh);
	}
	
	private double[] getVectorForVertex(VVertex v) {
		Vertex<?, ?, ?> vv = baseHDS.getVertex(v.getIndex());
		if (vectorButton.isSelected()) {
			Adapter<double[]> data = adapterList.getSelectedValue();
			return data.get(vv, aSet);
		} else {
			HalfedgeLayer l = layerList.getSelectedValue();
			Vertex<?, ?, ?> vecVertex = l.get().getVertex(v.getIndex());
			return aSet.getD(Position3d.class, vecVertex);
		}
	}
	

	@Override
	public String getAlgorithmName() {
		return "Offset Mesh";
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Generator;
	}

}
