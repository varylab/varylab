package de.varylab.varylab.plugin.nurbs.algorithm;

import java.util.Iterator;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.selection.TypedSelection;
import de.jtem.halfedgetools.selection.VertexSelection;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.adapter.VectorFieldMapAdapter;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;
import de.varylab.varylab.plugin.nurbs.math.NURBSCurvatureUtility;

public class ComputeVectorFields extends AlgorithmPlugin {

	@Override
	public String getCategory() {
		return "NURBS";
	}
	
	@Override
	public String getAlgorithmName() {
		return "Compute principle curvature vector fields";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		TypedSelection<V> selection = hi.getSelection().getVertices(hds);
		VectorFieldMapAdapter vfmax = new VectorFieldMapAdapter();
		vfmax.setName("max nurbs field");
		VectorFieldMapAdapter vfmin = new VectorFieldMapAdapter();
		vfmin.setName("min nurbs field");
		NurbsUVAdapter nurbsAdapter = a.query(NurbsUVAdapter.class);
		if(nurbsAdapter != null) {
			NURBSSurface surface = nurbsAdapter.getSurface();
			for(V v : selection){
				CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(surface, nurbsAdapter.getV(v, null));
				double[][] principleDirections = ci.getCurvatureDirections();
				vfmax.setV(v,principleDirections[0],null);
				vfmin.setV(v,principleDirections[1],null);
			}
		} else {
			throw new RuntimeException("No nurbs surface on active layer.");
		}
		hi.addLayerAdapter(vfmax, false);
		hi.addLayerAdapter(vfmin, false);
		hi.update();
	}

}
