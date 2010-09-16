package de.varylab.varylab.math.dec;

import java.util.HashSet;
import java.util.Set;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.varylab.varylab.hds.adapter.GaussianCurvatureAdapter;

public class TrivialConnection {

	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Vector calculateConnection(HDS hds, HalfedgeInterface hif) {
		Set<F> bdFaces = new HashSet<F>();
		for(V v: HalfEdgeUtils.boundaryVertices(hds)) {
			bdFaces.addAll(HalfEdgeUtilsExtra.getFaceStar(v));
		}
		int eulerChar = (hds.numVertices()-hds.numEdges()/2+hds.numFaces());
		
//		int
//			numVertices = hds.numVertices(),
//			numEdges = hds.numEdges()/2,
//			numBdEdges = HalfEdgeUtils.boundaryEdges(hds).size()*2;
//		Set<List<E>> paths = HomologyUtility.getDualGeneratorPaths(hds.getVertex(0), new Search.DefaultWeightAdapter<E>());
		//TODO deal with boundary
//		Matrix A = new FlexCompRowMatrix(numVertices-bdFaces.size()+paths.size(),numEdges-numBdEdges);
		DiscreteDifferentialOperators<V,E,F,HDS> dop = new DiscreteDifferentialOperators<V,E,F,HDS>(hds, hif.getAdapters());
		Matrix d0 = new DenseMatrix(dop.getDifferential(0));
		Matrix d1 = dop.getDifferential(1);
		Vector b = new DenseVector(hds.numVertices());
		GaussianCurvatureAdapter gca = new GaussianCurvatureAdapter();
		AdapterSet as = hif.getAdapters();
		double totalSingularities = 0.0;
		for(V v: hds.getVertices()) {
			double val = gca.get(v,hif.getAdapters());
			double sing = as.get(Singularity.class, v, Double.class);
			totalSingularities += sing;
			val -= 2*Math.PI*sing;
			b.set(v.getIndex(), val);
		}
		if(eulerChar != totalSingularities) {
			throw new IllegalArgumentException("Sum of singularities does not match Euler characteristic: " +totalSingularities+" != "+eulerChar);
		}
		Vector solution = new DenseVector(hds.numEdges()/2);
		d0.transSolve(b,solution);
		
		Matrix L = new DenseMatrix(hds.numFaces(),hds.numFaces());
		d1.transBmult(d1, L);
		Vector z = new DenseVector(hds.numFaces());
		d1.mult(solution, z);
		Vector y = new DenseVector(hds.numFaces());
		L.solve(z,y);
		Vector proj = new DenseVector(hds.numEdges()/2);
		d1.transMult(y,proj);
		solution.add(-1.0,proj);
		return solution;
	}
	
	
}
