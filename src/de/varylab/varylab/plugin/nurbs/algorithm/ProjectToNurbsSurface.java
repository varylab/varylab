package de.varylab.varylab.plugin.nurbs.algorithm;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position4d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.math.PointProjectionSurface;

public class ProjectToNurbsSurface extends AlgorithmDialogPlugin {

	private JPanel
		panel = new JPanel();
	
	private List<NURBSSurface>
		nurbsSurfaces = new LinkedList<NURBSSurface>();
	private JList<HalfedgeLayer>
		nurbsLayerList = new JList<HalfedgeLayer>();
	private JScrollPane
		nurbsLayerScroller = new JScrollPane(nurbsLayerList);
	
	public ProjectToNurbsSurface() {
		panel.setLayout(new GridLayout());
		panel.add(nurbsLayerScroller);
		nurbsLayerScroller.setPreferredSize(new Dimension(150, 100));
	}
	
	@Override
	public String getCategory() {
		return "NURBS";
	}
	
	@Override
	public String getAlgorithmName() {
		return "Project to Nurbs surface";
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
		Map<Integer, double[]> indexMap = new HashMap<Integer, double[]>();
		i = 0;
		int n = hds.numVertices();
		for(V v : hds.getVertices()) {
			double[] pt = a.getD(Position4d.class,v);
			double[] uv = PointProjectionSurface.getClosestPointDomain(targetSurface, pt);
			indexMap.put(v.getIndex(),uv);
			double[] targetPoint = targetSurface.getSurfacePoint(uv[0], uv[1]);
			a.set(Position.class, v, targetPoint);
			getCurrentJob().fireJobProgress((double)(i++)/n);
		}
		hi.update();
		//NurbsUVAdapter uvAdapter = new NurbsUVAdapter(targetSurface, indexMap); //put onto layer
	}

}
