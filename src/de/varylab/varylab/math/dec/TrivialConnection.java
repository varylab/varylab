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
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.varylab.varylab.hds.adapter.GaussianCurvatureAdapter;

public class TrivialConnection {

	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void calculateConnection(HDS hds, HalfedgeInterface hif) {
		Set<F> bdFaces = new HashSet<F>();
		for(V v: HalfEdgeUtils.boundaryVertices(hds)) {
			bdFaces.addAll(HalfEdgeUtilsExtra.getFaceStar(v));
		}
//		int
//			numVertices = hds.numVertices(),
//			numEdges = hds.numEdges()/2,
//			numBdEdges = HalfEdgeUtils.boundaryEdges(hds).size()*2;
//		Set<List<E>> paths = HomologyUtility.getDualGeneratorPaths(hds.getVertex(0), new Search.DefaultWeightAdapter<E>());
		//TODO deal with boundary
//		Matrix A = new FlexCompRowMatrix(numVertices-bdFaces.size()+paths.size(),numEdges-numBdEdges);
		DiscreteDifferentialOperators dop = new DiscreteDifferentialOperators(hds, hif.getAdapters());
		Matrix d0 = dop.getBoundaryOperator(0);
		Matrix d1 = dop.getBoundaryOperator(1);
		Vector b = new DenseVector(hds.numVertices());
		GaussianCurvatureAdapter gca = new GaussianCurvatureAdapter();
		for(V v: hds.getVertices()) {
			b.set(v.getIndex(), gca.get(v,hif.getAdapters())-2*Math.PI);
		}
		Vector solution = new DenseVector(hds.numEdges()/2);
		d0.solve(b, solution);
		Matrix L = new DenseMatrix(1,1);
		
		
	}
}
