package de.varylab.varylab.plugin.ddg;

import java.util.HashMap;
import java.util.Map;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.algorithm.subdivision.CatmullClark;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;

public class CentralExtensionSubdivision extends AlgorithmPlugin {

	private CatmullClark
		catmullClark = new CatmullClark();
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.DDG;
	}

	@Override
	public String getAlgorithmName() {
		return "Central Extension";
	}

	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS subdivide(
		HDS hds, 
		AdapterSet a,
		Map<F, V> oldFnewVMap,
		Map<E, V> oldEnewVMap,
		Map<V, V> oldVnewVMap
	) {
		HDS hds2 = hcp.createEmpty(hds);
		catmullClark.subdivide(
			hds, 
			hds2, 
			a, 
			new HalfedgeSelection(), 
			false,
			false, 
			false,
			true,
			oldFnewVMap, 
			oldEnewVMap, 
			oldVnewVMap
		);
		for (F oldF : oldFnewVMap.keySet()) {
			V v = oldFnewVMap.get(oldF);
			double[] c = ChristoffelTransform.getIncircle(oldF, a);
			a.set(Position.class, v, new double[]{c[0], c[1], c[2]});
		}
		for (E e : oldEnewVMap.keySet()) {
			V v = oldEnewVMap.get(e);
			if (e.getLeftFace() == null) {
				e = e.getOppositeEdge();
			}
			F oldF = e.getLeftFace();
			assert oldF != null;
			double[] c = ChristoffelTransform.getIncircle(oldF, a);
			double r = c[3];
			double[] p0 = a.getD(Position3d.class, e.getStartVertex());
			double[] p1 = a.getD(Position3d.class, e.getTargetVertex());
			double[] p2 = a.getD(Position3d.class, e.getPreviousEdge().getStartVertex());
			double[] vec1 = Rn.subtract(null, p1, p0);
			double[] vec2 = Rn.subtract(null, p2, p0);
			double alpha = Rn.euclideanAngle(vec1, vec2);
			double r1 = r / Math.tan(alpha / 2);
			double[] vec3 = Rn.setToLength(null, vec1, r1);
			double[] pos = Rn.add(null, p0, vec3);
			a.set(Position.class, v, pos);
		}
		return hds2;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		Map<F, V> oldFnewVMap = new HashMap<F, V>();
		Map<E, V> oldEnewVMap = new HashMap<E, V>();
		Map<V, V> oldVnewVMap = new HashMap<V, V>();
		HDS hds2 = subdivide(hds, a, oldFnewVMap, oldEnewVMap, oldVnewVMap);
		hcp.set(hds2);
	}

}
