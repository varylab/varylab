package de.varylab.varylab.plugin.nurbs.algorithm;


import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

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

public class SplitInTheMiddle extends AlgorithmDialogPlugin {
	
	private JPanel
		panel = new JPanel();
	private JRadioButton 
		uSplit = new JRadioButton("split in u dir");
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
		return "Split in the middle";
	}

	public SplitInTheMiddle() {
		panel.setLayout(new GridLayout());
		panel.add(uSplit);
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
			throw new RuntimeException("No nurbs surfaces on any layer to project onto.");
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
//		Map<Integer, double[]> indexMap = new HashMap<Integer, double[]>();
		i = 0;
//		int n = hds.numVertices();
		boolean dir = false;
		if(uSplit.isSelected()){
			dir = true;
		}
		NURBSSurface[] surfs = NurbsDeformationTools.splitInTheMiddle(targetSurface, dir);
		HalfedgeLayer hel1 = new HalfedgeLayer(hi);
		hel1.setName("surface 1");
		NurbsSurfaceUtility.addNurbsMesh(surfs[0],hel1,10,10);
		HalfedgeLayer hel2 = new HalfedgeLayer(hi);
		hel1.setName("surface 2");
		NurbsSurfaceUtility.addNurbsMesh(surfs[1],hel2,10,10);
//		writeToFile(fS);
	
//		hel.addTemporaryGeometry(fS.getIndexedFaceSet());
		hi.addLayer(hel1); //add and activate
		hi.addLayer(hel2); //add and activate
		hi.update();
		//NurbsUVAdapter uvAdapter = new NurbsUVAdapter(targetSurface, indexMap); //put onto layer
	}

}
