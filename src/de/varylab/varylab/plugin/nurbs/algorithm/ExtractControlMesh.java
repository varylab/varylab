package de.varylab.varylab.plugin.nurbs.algorithm;

import de.jreality.geometry.QuadMeshFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;

public class ExtractControlMesh extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Custom;
	}

	@Override
	public String getAlgorithmName() {
		return "Extract Control Mesh";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		AdapterSet as = hi.getAdapters();
		as.addAll(hi.getActiveVolatileAdapters());
		NurbsUVAdapter nurbsAdapter = as.query(NurbsUVAdapter.class);
		if(nurbsAdapter != null) {
			double[][][] cm = nurbsAdapter.getSurface().getControlMesh();
			QuadMeshFactory qmf = new QuadMeshFactory();
			qmf.setULineCount(cm[0].length);
			qmf.setVLineCount(cm.length);
			qmf.setVertexCoordinates(cm);
			qmf.setGenerateEdgesFromFaces(true);
			qmf.update();
			IndexedFaceSet ifs = qmf.getIndexedFaceSet();
			HalfedgeLayer newLayer = hi.createLayer("Control Mesh");
			newLayer.set(ifs);
			newLayer.setActive(true);
		} else {
			throw new RuntimeException("No nurbs surface on active layer.");
		}
	}

}
