package de.varylab.varylab.plugin.dec;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.AbstractIterativeSolver;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import no.uib.cipr.matrix.sparse.GMRES;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.GaussCurvatureAdapter;
import de.jtem.halfedgetools.adapter.type.EdgeIndex;
import de.jtem.halfedgetools.dec.DiscreteDifferentialOperators;
import de.jtem.halfedgetools.dec.VectorFieldMapAdapter;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.varylab.discreteconformal.util.HomologyUtility;
import de.varylab.discreteconformal.util.Search;
import de.varylab.varylab.hds.adapter.ConnectionAdapter;
import de.varylab.varylab.hds.adapter.type.Connection;
import de.varylab.varylab.hds.adapter.type.Singularity;
import de.varylab.varylab.utilities.ConnectionUtility;

public class TrivialConnectionPlugin extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.VectorField;
	}

	@Override
	public String getAlgorithmName() {
		return "Trivial connection";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hcp) 
	{
		
		AdapterSet adapters = hcp.getAdapters();
		ConnectionAdapter trivialConnection = calculateConnection(hds, adapters);
		VectorFieldMapAdapter vfAdapter = ConnectionUtility.calculateVectorField(hds, adapters, trivialConnection);
		hcp.addLayerAdapter(trivialConnection,false);
		hcp.addLayerAdapter(vfAdapter,false);
		if(!checkCurvature(hds,hcp.getAdapters())) {
			System.out.println("Connection does not match curvature!");
		}
	}

	private <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> ConnectionAdapter calculateConnection(HDS heds, AdapterSet adapters) {
		//	Set<Face<?,?,?>> bdFaces = new HashSet<Face<?,?,?>>();
		//	for(Vertex<?,?,?> v: HalfEdgeUtils.boundaryVertices(heds)) {
		//		bdFaces.addAll(HalfEdgeUtilsExtra.getFaceStar(v));
		//	}
		int eulerChar = (heds.numVertices()-heds.numEdges()/2+heds.numFaces());
	
		//	int
		//		numVertices = hds.numVertices(),
		//		numEdges = hds.numEdges()/2,
		//		numBdEdges = HalfEdgeUtils.boundaryEdges(hds).size()*2;
		Set<List<E>> paths = HomologyUtility.getDualGeneratorPaths(heds.getVertex(0), new Search.DefaultWeightAdapter<E>());
		int numGenerators = paths.size();
		//TODO deal with boundary
		//	Matrix A = new FlexCompRowMatrix(numVertices-bdFaces.size()+paths.size(),numEdges-numBdEdges);
		//	Matrix d0 = getDifferential(0);
		Matrix d0 = DiscreteDifferentialOperators.getDifferential(heds, adapters, 0);
		Matrix d1 = DiscreteDifferentialOperators.getDifferential(heds, adapters, 1);
		//	Vector b = new DenseVector(heds.numEdges()/2);
		Vector b = new DenseVector(heds.numVertices()+numGenerators);
		GaussCurvatureAdapter gca = new GaussCurvatureAdapter();
		double totalSingularities = 0.0;
		for(V v: heds.getVertices()) {
			double val = -gca.get(v,adapters);
			double sing = adapters.get(Singularity.class, v, Double.class);
			totalSingularities += sing;
			val += 2*Math.PI*sing;
			b.set(v.getIndex(), val);
		}
		if(Math.abs(eulerChar - totalSingularities) >= 1E-6) {
			throw new IllegalArgumentException("Sum of singularities does not match Euler characteristic: " +totalSingularities+" != "+eulerChar);
		}
	
		Vector solution = new DenseVector(heds.numEdges()/2);
	
		//	Matrix A = new FlexCompColMatrix(heds.numEdges()/2, heds.numEdges()/2);
		Matrix A = new DenseMatrix(heds.numVertices()+numGenerators,heds.numEdges()/2);
		Iterator<MatrixEntry> mi = d0.iterator();
		while(mi.hasNext()) {
			MatrixEntry me = mi.next();
			// Attention: transposing!
			A.set(me.column(),me.row(),me.get());
		}
		int i = 0;
		for(List<E> path : paths) {
			for(E e: path) {
				int ei = adapters.get(EdgeIndex.class, e, Integer.class);
				A.set(heds.numVertices()+i,ei,e.isPositive()?+1.0:-1.0);
			}
		}
		A.solve(b, solution);
		//	AbstractIterativeSolver gmresA = new GMRES(solution);
		//	
		//	try {
		//		gmresA.solve(A, b, solution);
		//	} catch (IterativeSolverNotConvergedException e) {
		//		System.out.println("Iterative solver not converged");
		//		e.printStackTrace();
		//	}
		//	
		Matrix L = new FlexCompColMatrix(heds.numFaces(),heds.numFaces());
		d1.transBmult(d1, L);
		Vector z = new DenseVector(heds.numFaces());
		d1.mult(solution, z);
		Vector y = new DenseVector(heds.numFaces());
		AbstractIterativeSolver gmresL = new GMRES(y);
		try {
			gmresL.solve(L, z, y);
		} catch (IterativeSolverNotConvergedException e) {
			System.out.println("Iterative solver not converged");
			e.printStackTrace();
		}
		Vector proj = new DenseVector(heds.numEdges()/2);
		d1.transMult(y,proj);
		solution.add(-1.0,proj);
		return new ConnectionAdapter(solution);
	}

	
	
	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> boolean checkCurvature(HDS hds, AdapterSet adapters) {
		boolean ok = true;
		for(V v: hds.getVertices()) {
			ok &= checkVertex(v,adapters); 
		}
		return ok;
	}
	
	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F> 
	> boolean checkVertex(V v, AdapterSet adapters) {
		GaussCurvatureAdapter gca = new GaussCurvatureAdapter();
		E e = v.getIncomingEdge();
		double curvature = 0.0;
		do {
			curvature += adapters.get(Connection.class, e, Double.class);
			e = e.getNextEdge().getOppositeEdge();
		} while(e != v.getIncomingEdge());
		double singCurvature = adapters.get(Singularity.class, v, Double.class);
		double gaussCurvature = gca.getV(v, adapters);
		boolean vOK = (Math.abs(curvature + gaussCurvature - singCurvature*2*Math.PI ) <= 1E-6);
		if(!vOK) {
			System.out.println(v +":"+curvature +"!="+singCurvature +"+"+gaussCurvature);
		}
		return vOK;
	}

}
