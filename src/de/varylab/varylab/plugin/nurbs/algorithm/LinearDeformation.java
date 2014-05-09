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
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.math.NurbsDeformationTools;
import de.varylab.varylab.plugin.nurbs.math.NurbsSurfaceUtility;

public class LinearDeformation extends AlgorithmDialogPlugin {

	private JPanel
		panel = new JPanel();
	private SpinnerNumberModel
		x_vecSpinnerModel = new SpinnerNumberModel(1, -100.0, 100.0, 0.1),
		y_vecSpinnerModel = new SpinnerNumberModel(1, -100.0, 100.0, 0.1),
		z_vecSpinnerModel = new SpinnerNumberModel(1, -100.0, 100.0, 0.1),
		x_dirSpinnerModel = new SpinnerNumberModel(1, -100.0, 100.0, 0.1),
		y_dirSpinnerModel = new SpinnerNumberModel(1, -100.0, 100.0, 0.1),
		z_dirSpinnerModel = new SpinnerNumberModel(1, -100.0, 100.0, 0.1),
		deformSpinnerModel = new SpinnerNumberModel(1, -100.0, 100.0, 0.1);
	

	private JSpinner
		x_vecSpinner = new JSpinner(x_vecSpinnerModel),
		y_vecSpinner = new JSpinner(y_vecSpinnerModel),
		z_vecSpinner = new JSpinner(z_vecSpinnerModel),
		x_dirSpinner = new JSpinner(x_dirSpinnerModel),
		y_dirSpinner = new JSpinner(y_dirSpinnerModel),
		z_dirSpinner = new JSpinner(z_dirSpinnerModel),
		deformSpinner = new JSpinner(deformSpinnerModel);

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
		return "Linear Deformation";
	}
	
	public LinearDeformation() {
		panel.setLayout(new GridLayout(0,2));
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		lc.gridwidth = 2;
		panel.add(new JLabel(" x "), lc);
		lc.gridwidth = 1;
		panel.add(x_vecSpinner, rc);
		lc.gridwidth = 2;
		panel.add(new JLabel(" y "), lc);
		lc.gridwidth = 1;
		panel.add(y_vecSpinner, rc);
		lc.gridwidth = 2;
		panel.add(new JLabel(" z "), lc);
		lc.gridwidth = 1;
		panel.add(z_vecSpinner, rc);
		lc.gridwidth = 2;
		panel.add(new JLabel(" x-Dir "), lc);
		lc.gridwidth = 1;
		panel.add(x_dirSpinner, rc);
		lc.gridwidth = 2;
		panel.add(new JLabel(" y-Dir "), lc);
		lc.gridwidth = 1;
		panel.add(y_dirSpinner, rc);
		lc.gridwidth = 2;
		panel.add(new JLabel(" z-Dir "), lc);
		lc.gridwidth = 1;
		panel.add(z_dirSpinner, rc);
		lc.gridwidth = 2;
		panel.add(new JLabel(" deform factor "), lc);
		lc.gridwidth = 1;
		panel.add(deformSpinner, rc);
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
			throw new RuntimeException("No nurbs surfaces on any layer to deform.");
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
		HalfedgeLayer layer = hi.getActiveLayer();
		NurbsUVAdapter uvAdapter = layer.getVolatileAdapters().query(NurbsUVAdapter.class);
		if(uvAdapter == null) {
			throw new RuntimeException("No nurbs surfaces on any layer to deform.");
		}	
		NURBSSurface targetSurface = uvAdapter.getSurface();	
		double x = x_vecSpinnerModel.getNumber().doubleValue();
		double y = y_vecSpinnerModel.getNumber().doubleValue();
		double z = z_vecSpinnerModel.getNumber().doubleValue();
		double[] vec = {x,y,z};
		double xDir = x_dirSpinnerModel.getNumber().doubleValue();
		double yDir = y_dirSpinnerModel.getNumber().doubleValue();
		double zDir = z_dirSpinnerModel.getNumber().doubleValue();
		double[] dir = {xDir, yDir, zDir};
		double deformFactor = deformSpinnerModel.getNumber().doubleValue();
		
		NURBSSurface deform = NurbsDeformationTools.deform(targetSurface, vec, dir, deformFactor);
		System.out.println("letzt endlich");
		System.out.println(deform.toString());
		HalfedgeLayer hel = new HalfedgeLayer(hi);
		hel.setName("deformed surface");
		NurbsSurfaceUtility.addNurbsMesh(deform,hel,uvAdapter.getULineCount(),uvAdapter.getVLineCount());
		
	
		hi.addLayer(hel); //add and activate
		hi.update();
	}

}
