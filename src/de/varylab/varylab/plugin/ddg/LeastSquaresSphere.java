package de.varylab.varylab.plugin.ddg;

import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.proxy.scene.SceneGraphComponent;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.varylab.jpetsc.InsertMode;
import de.varylab.jpetsc.Vec;
import de.varylab.jtao.Tao;
import de.varylab.varylab.math.LeastSquaresSphereTao;

public class LeastSquaresSphere extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.DDG;
	}

	@Override
	public String getAlgorithmName() {
		return "LS Sphere";
	}

	
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] getSphereCenter(HDS hds, AdapterSet a) {
		double acc = Math.pow(10, -6);
		int maxIter = 100;
		Tao.Initialize();
		double[][] points = new double[hds.numVertices()][3];
		int i = 0;
		for(V v : hds.getVertices()) {
			points[i++] = a.getD(Position3d.class,v);
		}
		LeastSquaresSphereTao lss = new LeastSquaresSphereTao(points);
		Vec x = new Vec(4);
		double[] barycenter = Rn.average(null, points);
		double r = 0;
		for(V v : hds.getVertices()) {
			r += Rn.euclideanDistance(barycenter, a.getD(Position3d.class,v));
		}
		r /= hds.numVertices();
		
		x.setValue(0, barycenter[0],InsertMode.INSERT_VALUES);
		x.setValue(1, barycenter[1],InsertMode.INSERT_VALUES);
		x.setValue(2, barycenter[2],InsertMode.INSERT_VALUES);
		x.setValue(3, r,InsertMode.INSERT_VALUES);
		
		lss.setInitialSolutionVec(x);
		
		Tao optimizer = new Tao(Tao.Method.CG);
		optimizer.setApplication(lss);
		optimizer.setGradientTolerances(acc, acc, 0);
		optimizer.setMaximumIterates(maxIter);
		optimizer.setTolerances(0.0, 0.0, 0.0, 0.0);
		optimizer.solve();
		
//		GetSolutionStatusResult stat = optimizer.getSolutionStatus();
//		String status = stat.toString().replace("getSolutionStatus : ", "");
//		System.out.println("optimization status ------------------------------------");
//		System.out.println(status);
		
		return x.getArray();
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		double[] eCenter = getSphereCenter(hds, a);
		double[] center = {eCenter[0],eCenter[1],eCenter[2],1.0};
		double radius = eCenter[3];
		SceneGraphComponent cg = new SceneGraphComponent();
		cg.setGeometry(Primitives.sphere(30));
		MatrixBuilder.euclidean().translate(center).scale(radius).assignTo(cg);
		hi.getActiveLayer().addTemporaryGeometry(cg);
	}

}
