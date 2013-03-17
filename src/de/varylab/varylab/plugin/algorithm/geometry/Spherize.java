package de.varylab.varylab.plugin.algorithm.geometry;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.ddg.LeastSquaresSphere;
import de.varylab.varylab.plugin.image.ImageHook;

public class Spherize extends AlgorithmPlugin {

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		double[] sphereCenter = LeastSquaresSphere.getSphereCenter(hds, a);
		double[] center = new double[]{sphereCenter[0],sphereCenter[1],sphereCenter[2]};
		double radius = sphereCenter[3];
		for(V v : hds.getVertices()) {
			double[] coord = Rn.subtract(null, a.get(Position3d.class, v, double[].class),center);
			double norm = Rn.euclideanNorm(coord);
			Rn.times(coord, radius / norm, coord);
			a.set(Position.class, v, Rn.add(null, center, coord));
		}
		hi.update();
	}

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Geometry;
	}
	
	
	@Override
	public String getAlgorithmName() {
		return "Spherize";
	}

	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Project To BestFit-Sphere");
		info.icon = ImageHook.getIcon("spherize.png", 16, 16);
		return info;
	}

}
