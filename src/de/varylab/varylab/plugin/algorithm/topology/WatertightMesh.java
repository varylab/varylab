package de.varylab.varylab.plugin.algorithm.topology;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;

public class WatertightMesh extends AlgorithmPlugin {

	private static double
		threshold = 1E-8;
	
	@Override
	public String getAlgorithmName() {
		return "Watertight Mesh";
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Generator;
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) throws Exception {
		KdTree<V, E, F> kd = new KdTree<>(hds, a, 10, false);
		Set<V> collected = new LinkedHashSet<>();
		Map<V, List<V>> identMap = new LinkedHashMap<>();
		Map<List<V>, Integer> indexMap = new LinkedHashMap<>();
		int index = 0;
		for (V v : hds.getVertices()) {
			if (collected.contains(v)) continue;
			double[] p = a.getD(Position3d.class, v);
			List<V> identList = new ArrayList<>(kd.collectInRadius(p, threshold));
			for (V vv : identList) {
				identMap.put(vv, identList);
			}
			collected.addAll(identList);
			indexMap.put(identList, index++);
		}
		double[][] vData = new double[indexMap.size()][];
		for (List<V> cluster : identMap.values()) {
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
