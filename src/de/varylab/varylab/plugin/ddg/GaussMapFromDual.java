package de.varylab.varylab.plugin.ddg;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.varylab.varylab.plugin.ddg.ChristoffelTransform.NormalMethod;

public class GaussMapFromDual extends AlgorithmPlugin {

	private ChristoffelTransform 
		christoffelTransform = null;

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.DDG;
	}

	@Override
	public String getAlgorithmName() {
		return "Gauss from dual";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet aSet, HalfedgeInterface hif) {
		HDS dualSurface = hif.createEmpty(hds);
		HalfEdgeUtils.copy(hds, dualSurface);
		for (V v : dualSurface.getVertices()) {
			V vv = hds.getVertex(v.getIndex());
			double[] pos = aSet.getD(Position3d.class, vv);
			aSet.set(Position.class, v, pos);
		}
		christoffelTransform.transform(dualSurface, aSet, 0.0, false, NormalMethod.Face_Sphere);
		for(V v : hds.getVertices()) {
			double[] 
			       pos = aSet.getD(Position3d.class,v),
			       dualPos = aSet.getD(Position3d.class, dualSurface.getVertex(v.getIndex()));
			Rn.subtract(pos, dualPos, pos);
			aSet.set(Position.class, v, pos);
		}
		hif.set(hds);
		hif.update();
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		christoffelTransform = c.getPlugin(ChristoffelTransform.class);
	}
}
