package de.varylab.varylab.plugin.nurbs.algorithm;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurfaceFactory;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.math.NurbsDeformationTools;
import de.varylab.varylab.plugin.nurbs.math.NurbsSurfaceUtility;

public class StretchXYZ extends AlgorithmDialogPlugin {

	private JPanel
		panel = new JPanel();
	private SpinnerNumberModel
		xSpinnerModel = new SpinnerNumberModel(1, -100.0, 100.0, 0.1),
		ySpinnerModel = new SpinnerNumberModel(1, -100.0, 100.0, 0.1),
		zSpinnerModel = new SpinnerNumberModel(1, -100.0, 100.0, 0.1);
	
	private JSpinner
		xSpinner =new JSpinner(xSpinnerModel),
		ySpinner =new JSpinner(ySpinnerModel),
		zSpinner =new JSpinner(zSpinnerModel);

	private List<NURBSSurface>
		nurbsSurfaces = new LinkedList<NURBSSurface>();
	private JList<HalfedgeLayer>
		nurbsLayerList = new JList<HalfedgeLayer>();
	
	
	
	@Override
	public String getCategory() {
		return "NURBS";
	}
	
	@Override
	public String getAlgorithmName() {		
		return "StrechXYZ";
	}
	
	public StretchXYZ() {
		panel.setLayout(new GridLayout());
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		lc.gridwidth = 2;
		panel.add(new JLabel("Stretch x-Direction"), lc);
		lc.gridwidth = 1;
		panel.add(xSpinner, rc);
		lc.gridwidth = 2;
		panel.add(new JLabel("Stretch y-Direction"), lc);
		lc.gridwidth = 1;
		panel.add(ySpinner, rc);
		lc.gridwidth = 2;
		panel.add(new JLabel("Stretch z-Direction"), lc);
		lc.gridwidth = 1;
		panel.add(zSpinner, rc);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeBeforeDialog(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		nurbsSurfaces.clear();
		DefaultListModel<HalfedgeLayer> layerModel = new DefaultListModel<HalfedgeLayer>();
		for(HalfedgeLayer layer : hi.getAllLayers()) {
			NurbsUVAdapter uvAdapter = layer.getVolatileAdapters().query(NurbsUVAdapter.class);
			if(uvAdapter != null) {
				nurbsSurfaces.add(uvAdapter.getSurface());
				layerModel.addElement(layer);
			}
		}
		if(nurbsSurfaces.isEmpty()) {
			throw new RuntimeException("No nurbs surfaces on any layer to stretch.");
		}
		nurbsLayerList.setModel(layerModel);
		nurbsLayerList.setSelectedIndex(0);
		panel.revalidate();
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		
		int i = nurbsLayerList.getSelectedIndex();
		NURBSSurface targetSurface = nurbsSurfaces.get(i);
		NURBSSurfaceFactory nsf = new NURBSSurfaceFactory();
		nsf.setSurface(targetSurface);
		NurbsUVAdapter uvAdapter = nsf.getUVAdapter();
		
		double x = xSpinnerModel.getNumber().doubleValue();
		double y = ySpinnerModel.getNumber().doubleValue();
		double z = zSpinnerModel.getNumber().doubleValue();
		NURBSSurface stretch = NurbsDeformationTools.stretch(targetSurface, x, y, z);
		HalfedgeLayer hel = new HalfedgeLayer(hi);
		hel.setName("streched surface");
		NurbsSurfaceUtility.addNurbsMesh(stretch,hel,uvAdapter.getULineCount(),uvAdapter.getVLineCount());
		hi.addLayer(hel); //add and activate
		hi.update();
	}

}
