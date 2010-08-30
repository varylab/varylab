package de.varylab.varylab.math.dec;

import java.util.HashMap;
import java.util.Map;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.CompDiagMatrix;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
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
		d0 = null,
		d1 = null,
		s0 = null,
		s1 = null,
		s2 = null;
	
	public DiscreteDifferentialOperators(HalfEdgeDataStructure<?,?,?> hds) {
		heds = hds;
		int i = 0;
		for(Edge<?,?,?> e : hds.getPositiveEdges()) {
			edgeMap.put(e, i);
			edgeMap.put(e.getOppositeEdge(),i);
			++i;
		}
	}
	
	public Matrix getBoundaryMatrix(int dim) {
		if(dim < 0 || dim > 1) {
			throw new IllegalArgumentException("No boundary operators for dimension "+dim);
		}
		switch (dim) {
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
		}
		return null;
	}
	
	public Matrix getHodgeStar(int dim, AdapterSet as) {
		if(dim < 0 || dim > 2) {
			throw new IllegalArgumentException("No hodge star operators for dimension "+dim);
		}
		as.add(new CircumcircleCenterAdapter());
		switch (dim) {
		case 0:
			if(s0 == null) {
				s0 = new CompDiagMatrix(heds.numVertices(), heds.numVertices());
			}
			for(Vertex<?,?,?> v : heds.getVertices()) {
				double[] vc = as.get(Position.class, v, double[].class);
				Edge<?,?,?> e = v.getIncomingEdge();
				double volume = 0.0;
				do {
					double[]
					       ovc = as.get(Position.class, e.getStartVertex(), double[].class),
					       emc = Rn.linearCombination(null, 0.5, vc, 0.5, ovc);
					Face<?,?,?> face = e.getLeftFace();
					if(face != null) {
						double[] ccc = as.get(BaryCenter.class, face, double[].class);
						double 
							b = Rn.euclideanDistance(vc, emc),
							h = Rn.euclideanDistance(ccc, emc);
						volume += b*h;
					}
					face = e.getRightFace();
					if(face != null) {
						double[] ccc = as.get(BaryCenter.class, e.getRightFace(), double[].class);
						double
							b = Rn.euclideanDistance(vc, emc),
							h = Rn.euclideanDistance(ccc, emc);
						volume += b*h;
					}
					e = e.getNextEdge().getOppositeEdge();
				} while(e != v.getIncomingEdge());
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
				       svc = as.get(Position.class, e.getStartVertex(), double[].class),
				       tvc = as.get(Position.class, e.getTargetVertex(), double[].class),
				       emc = Rn.linearCombination(null, 0.5, svc, 0.5, tvc);
				double primalVolume = Rn.euclideanDistance(svc, tvc);
				
				Face<?,?,?> face = e.getLeftFace();
				if(face != null) {
					double[] ccc = as.get(BaryCenter.class, face, double[].class);
					double h = Rn.euclideanDistance(ccc, emc);
					dualVolume += h;
				}
				face = e.getRightFace();
				if(face != null) {
					double[] ccc = as.get(BaryCenter.class, e.getRightFace(), double[].class);
					double h = Rn.euclideanDistance(ccc, emc);
					dualVolume += h;
				}
				s1.set(edgeMap.get(e),edgeMap.get(e),dualVolume/primalVolume);
			}
			return s1;
		case 2:
			if(s2 == null) {
				s2 = new CompDiagMatrix(heds.numFaces(),heds.numFaces());
			}
			for(Face<?,?,?> f : heds.getFaces()) {
				as.add(new TriangleVolumeAdapter());
				s2.set(f.getIndex(),f.getIndex(),1.0/as.get(Volume.class, f, Double.class));
			}
			return s2;
		}
		return null;
	}
}
