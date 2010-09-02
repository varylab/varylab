package de.varylab.varylab.math.dec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.sparse.CompDiagMatrix;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.BaryCenter;
import de.jtem.halfedgetools.adapter.type.Position;

public class DiscreteDifferentialOperators {

	private Map<Edge<?,?,?>, Integer>
		edgeMap = new HashMap<Edge<?,?,?>, Integer>();
		
	private HalfEdgeDataStructure<?,?,?>
		heds = null;
		
	private Matrix
		d0 = null, // boundary Operator edges->vertices
		d1 = null, // boundary Operator faces->edges
		D0 = null, // differential 0-forms->1-forms
		D1 = null, // differential 1-forms->2-forms
		cD0 = null, // co-differential 1-forms->0-forms
		cD1 = null, // co-differential 2-forms->1-forms
		s0 = null, // hodge star 0-forms->dual 2-forms
		s1 = null, // hodge star 1-forms->dual 1-forms
		s2 = null, // hodge star 2-forms->dual 0-forms
		L0 = null, // laplace operator dim 0
		L1 = null, // laplace operator dim 0
		L2 = null; // laplace operator dim 0
		
	
	private final double EPS = 1E-10;
	
	private AdapterSet
		adapters = new AdapterSet();
	
	public DiscreteDifferentialOperators(HalfEdgeDataStructure<?,?,?> hds, AdapterSet as) {
		if(as != null) {
			adapters.addAll(as);
		}
		adapters.add(new CircumcircleCenterAdapter());
		heds = hds;
		int i = 0;
		for(Edge<?,?,?> e : hds.getPositiveEdges()) {
			edgeMap.put(e, i);
			edgeMap.put(e.getOppositeEdge(),i);
			++i;
		}
	}
	
	public Matrix getBoundaryOperator(int dim) {
		if(dim < -1 || dim > 2) {
			throw new IllegalArgumentException("No boundary operators for dimension "+dim);
		}
		switch (dim) {
		case -1: 
		{
			return new CompDiagMatrix(1,heds.numVertices());
		}
		case 0:
			if(d0 == null) {
				d0 = new FlexCompColMatrix(heds.numVertices(),heds.numEdges()/2);
			}
			for(Edge<?,?,?> e : heds.getEdges()) {
				if(e.isPositive()) {
					int j = edgeMap.get(e);
					d0.set(e.getStartVertex().getIndex(),j,-1.0);
					d0.set(e.getTargetVertex().getIndex(),j,1.0);
				}
			}
			return d0;
		case 1:
			if(d1 == null) {
				d1 = new FlexCompColMatrix(heds.numEdges()/2,heds.numFaces());
			}
			for(Face<?,?,?> f : heds.getFaces()) {
				int j = f.getIndex();
				Edge<?,?,?> e = f.getBoundaryEdge();
				do {
					double val = e.isPositive()?1.0:-1.0;
					d1.set(edgeMap.get(e),j,val);
					e = e.getNextEdge();
				} while(e != f.getBoundaryEdge());
			}
			return d1;
		case 2:
			{
				return new CompDiagMatrix(heds.numFaces(),1); 
			}
		}
		return null;
	}
	
	public Matrix getHodgeStar(int dim) {
		if(dim < -1 || dim > 2) {
			throw new IllegalArgumentException("No hodge star operators for dimension "+dim);
		}
		if(!adapters.contains(Position.class, heds.getVertexClass(), double[].class)) {
			throw new RuntimeException("Need adapter for position of vertices to calculate hodge star operator.");
		}
		
		switch (dim) {
		case -1:
			return new CompDiagMatrix(1,1);
		case 0:
			if(s0 == null) {
				s0 = new CompDiagMatrix(heds.numVertices(), heds.numVertices());
			}
			for(Vertex<?,?,?> v : heds.getVertices()) {
				double[] vc = adapters.get(Position.class, v, double[].class);
				Edge<?,?,?> e = v.getIncomingEdge();
				double volume = 0.0;
				do {
					double[]
					       ovc = adapters.get(Position.class, e.getStartVertex(), double[].class),
					       emc = Rn.linearCombination(null, 0.5, vc, 0.5, ovc);
					Face<?,?,?> face = e.getLeftFace();
					if(face != null) {
						double[] ccc = adapters.get(BaryCenter.class, face, double[].class);
						double 
							b = Rn.euclideanDistance(vc, emc),
							h = Rn.euclideanDistance(ccc, emc);
						volume += b*h;
					}
					face = e.getRightFace();
					if(face != null) {
						double[] ccc = adapters.get(BaryCenter.class, e.getRightFace(), double[].class);
						double
							b = Rn.euclideanDistance(vc, emc),
							h = Rn.euclideanDistance(ccc, emc);
						volume += b*h;
					}
					e = e.getNextEdge().getOppositeEdge();
				} while(e != v.getIncomingEdge());
				volume += EPS;
				s0.set(v.getIndex(),v.getIndex(),volume/2.0);
			}
			return s0;
		case 1:
			if(s1 == null) {
				s1 = new CompDiagMatrix(heds.numEdges()/2,heds.numEdges()/2);
			}
			for(Edge<?,?,?> e : heds.getPositiveEdges()) {
				double dualVolume = 0.0;
				double[]
				       svc = adapters.get(Position.class, e.getStartVertex(), double[].class),
				       tvc = adapters.get(Position.class, e.getTargetVertex(), double[].class),
				       emc = Rn.linearCombination(null, 0.5, svc, 0.5, tvc);
				double primalVolume = Rn.euclideanDistance(svc, tvc) + EPS;;
				
				Face<?,?,?> face = e.getLeftFace();
				if(face != null) {
					double[] ccc = adapters.get(BaryCenter.class, face, double[].class);
					double h = Rn.euclideanDistance(ccc, emc);
					dualVolume += h;
				}
				face = e.getRightFace();
				if(face != null) {
					double[] ccc = adapters.get(BaryCenter.class, e.getRightFace(), double[].class);
					double h = Rn.euclideanDistance(ccc, emc);
					dualVolume += h;
				}
				dualVolume += EPS;
				s1.set(edgeMap.get(e),edgeMap.get(e),dualVolume/primalVolume);
			}
			return s1;
		case 2:
			if(s2 == null) {
				s2 = new CompDiagMatrix(heds.numFaces(),heds.numFaces());
			}
			for(Face<?,?,?> f : heds.getFaces()) {
				adapters.add(new TriangleVolumeAdapter());
				double volume = adapters.get(Volume.class, f, Double.class) + EPS;
				s2.set(f.getIndex(),f.getIndex(),1.0/volume);
			}
			return s2;
		case 3:
			return new CompDiagMatrix(1,1);
		}
		return null;
	}
	
	public Matrix getDifferential(int dim) {
		if(dim < -1 || dim > 2) {
			throw new IllegalArgumentException("No differential for dimension "+dim);
		}
		switch (dim) {
			case -1:
				return new CompDiagMatrix(heds.numVertices(),1); 			
			case 0:
			{
				if(D0 == null) {
					D0 = new FlexCompRowMatrix(heds.numEdges()/2,heds.numVertices());
					getBoundaryOperator(0).transpose(D0);
				}
				return D0;
			}
			case 1:
			{
				if(D1 == null) {
					D1 = new FlexCompRowMatrix(heds.numFaces(),heds.numEdges()/2);
					getBoundaryOperator(1).transpose(D1);
				}
				return D1;
			}
			case 2:
				return new CompDiagMatrix(1,heds.numFaces()); 
		}
		return null;
	}
	
	public Matrix getCoDifferential(int dim) {
		if(dim < -1 || dim > 2) {
			throw new IllegalArgumentException("No differential for dimension "+dim);
		}
		Matrix M = null;
		switch (dim) {
			case -1:
			{
				M =  new CompDiagMatrix(1,heds.numVertices());
				break;
			}
			case 0:
			{
				if(cD0 == null) {
					cD0 = new FlexCompColMatrix(heds.numVertices(),heds.numEdges()/2);
					calculateCodifferential(dim, cD0);
				}
				M = cD0;
				break;
			}
			case 1:
			{
				if(cD1 == null) {
					cD1 = new FlexCompColMatrix(heds.numEdges()/2,heds.numFaces());
					calculateCodifferential(dim, cD1);
				}
				M = cD1;
				break;
			}
			case 2:
			{
				M =  new CompDiagMatrix(heds.numFaces(),1);
				break;
			}
		}
 		return M;
	}

	private void calculateCodifferential(int dim, Matrix M) {
		Matrix hs0inv = invertDiagonalMatrix(getHodgeStar(dim));
		Matrix hs1 = getHodgeStar(dim+1);
		Matrix tmp = new FlexCompColMatrix(M);
		getBoundaryOperator(dim).mult(-1.0, hs1, tmp);
		hs0inv.mult(tmp,M);
	}
	
	public Matrix getLaplaceOperator(int dim) {
		if(dim < 0 || dim > 2) {
			throw new IllegalArgumentException("No laplacian for dimension "+dim);
		}
		Matrix M = null;
		switch (dim) {
		case 0:
			if(L0 == null) {
				L0 = new FlexCompColMatrix(heds.numVertices(),heds.numVertices());
				calculateLaplaceOperator(dim, L0);		
			}
			M = L0;
			break;
		case 1:
			if(L1 == null) {
				L1 = new FlexCompColMatrix(heds.numEdges()/2,heds.numEdges()/2);
				calculateLaplaceOperator(dim, L1);
			}
			M = L1;
			break;
		case 2:
			if(L2 == null) {
				L2 = new FlexCompColMatrix(heds.numFaces(),heds.numFaces());
				calculateLaplaceOperator(dim, L2);
			}
			M = L2;
			break;
		}
		return M;
	}

	private void calculateLaplaceOperator(int dim, Matrix M) {
		getCoDifferential(dim).mult(getDifferential(dim), M);
		getDifferential(dim-1).multAdd(getCoDifferential(dim-1), M);
	}
	
	private CompDiagMatrix invertDiagonalMatrix(Matrix m) {
		CompDiagMatrix minv = new CompDiagMatrix(m.numRows(),m.numColumns());
		Iterator<MatrixEntry> it = m.iterator();
		while(it.hasNext()) {
			MatrixEntry me = it.next();
			minv.set(me.row(),me.column(),1.0/me.get());
		}
		return minv;
	}
	
	
}
