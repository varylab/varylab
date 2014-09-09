package de.varylab.varylab.plugin.algorithm.topology;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.bsp.KdTree;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;

public class WatertightMesh extends AlgorithmDialogPlugin {

	private JPanel
		optionPanel = new JPanel();
	private JLabel
		thresholdLabel = new JLabel("Threshold");
	private SpinnerNumberModel
		thresholdModel = new SpinnerNumberModel(-8, -20, 0, 1);
	private JSpinner
		thresholdSpinner = new JSpinner(thresholdModel);
	
	public WatertightMesh() {
		optionPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = GridBagConstraints.HORIZONTAL;
		optionPanel.add(thresholdLabel, c);
		optionPanel.add(thresholdSpinner, c);
	}
	
	@Override
	public String getAlgorithmName() {
		return "Watertight Mesh";
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Generator;
	}
	@Override
	protected JPanel getDialogPanel() {
		return optionPanel;
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hi) throws Exception {
		double threshold = Math.pow(10, thresholdModel.getNumber().doubleValue());
		KdTree<V, E, F> kd = new KdTree<>(hds, a, 10, false);
		Set<V> collected = new LinkedHashSet<>();
		Map<V, List<V>> identMap = new LinkedHashMap<>();
		Map<List<V>, Integer> indexMap = new LinkedHashMap<>();
		int index = 0;
		for (V v : hds.getVertices()) {
			if (collected.contains(v)) continue;
			double[] p = a.getD(Position3d.class, v);
			List<V> identList = new ArrayList<>(kd.collectInRadius(p, threshold));
			if (!identList.contains(v)) {
				identList.add(v);
			}
			for (V vv : identList) {
				identMap.put(vv, identList);
			}
			collected.addAll(identList);
			Object o = indexMap.put(identList, index);
			if (o == null) {
				index++;
			}
		}
		double[][] vData = new double[indexMap.size()][];
		for (List<V> cluster : indexMap.keySet()) {
			int i = indexMap.get(cluster);
			vData[i] = a.getD(Position3d.class, cluster.get(0));
		}
		int[][] iData = new int[hds.numFaces()][];
		for (F f : hds.getFaces()) {
			Set<List<V>> clusters = new LinkedHashSet<>();
			for (V v : HalfEdgeUtils.boundaryVertices(f)) {
				clusters.add(identMap.get(v));
			}
			iData[f.getIndex()] = new int[clusters.size()];
			int i = 0;
			for (List<V> cluster : clusters) {
				iData[f.getIndex()][i++] = indexMap.get(cluster);
			}
		}
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(vData.length);
		ifsf.setFaceCount(iData.length);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateVertexNormals(true);
		ifsf.setVertexCoordinates(vData);
		ifsf.setFaceIndices(iData);
		ifsf.update();
		hi.set(ifsf.getIndexedFaceSet());
	}
	
}
