package de.varylab.varylab.functional;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.algorithm.triangulation.Triangulator;
import de.jtem.halfedgetools.bsp.KdTree;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;


public class ReferenceSurfaceFunctional<
	V extends Vertex<V, E, F>, 
	E extends Edge<V, E, F>, 
	F extends Face<V, E, F>>
implements Functional<V, E, F> {

	private HashMap<V,double[]> 
		closestPointMap = new HashMap<V, double[]>();
	private HalfEdgeDataStructure<V, E, F> 
		refSurface = null;
	private AdapterSet
		refas = null;
	private KdTree<V, E, F> 
		kdtree = null;
	
	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluate(
			HDS hds,
			DomainValue x, 
			Energy E, 
			Gradient G, 
			Hessian H
	) {
		if(refSurface == null) {
			if(E != null) E.setZero();
			if(G != null) G.setZero();
			return;
		}
		if(E != null || G != null) {
			computeClosestPointMap(hds, x);
		}
		if (E != null) {
			E.set(evaluate(hds, x));
		}
		if (G != null) {
			evaluateGradient(hds, x, G);
		}
		closestPointMap.clear();
	}

	private <HDS extends HalfEdgeDataStructure<V, E, F>> void computeClosestPointMap(
			HDS hds, DomainValue x) {
		double[] vpos = new double[3];
		for(V v: hds.getVertices()) {
			FunctionalUtils.getPosition(v, x, vpos);
			double[] pt = getClosestPointOnSurface(refSurface, refas, kdtree, vpos);
			closestPointMap.put(v, pt);
		}
	}

	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int getDimension(HDS hds) {
		return 3*hds.numVertices();
	}

	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int[][] getNonZeroPattern(HDS hds) {
		return null;
	}

	@Override
	public boolean hasHessian() {
		return false;
	}

	// Calculate the energy of a given configuration
	public double evaluate(HalfEdgeDataStructure<V, E, F> hds, DomainValue x) {
		double result = 0.0;
		for(V v : hds.getVertices()) {
			double[] vpos = FunctionalUtils.getPosition(v, x, null);
			double[] pt = closestPointMap.get(v);
			if(pt == null) {
				pt = getClosestPointOnSurface(refSurface, refas, kdtree, vpos);
				closestPointMap.put(v, pt);
			}
			result += Rn.euclideanNormSquared(Rn.subtract(null, vpos, pt));
		}
		return result;
	}
	
	
	public double[] getClosestPointOnReference(double[] vpos) {
		return getClosestPointOnSurface(refSurface, refas, kdtree, vpos);
	}
	

	public double[] getClosestPointOnSurface(
		HalfEdgeDataStructure<V, E, F> surface,
		AdapterSet ras,
		KdTree<V, E, F> kdtree,
		double[] v)
	{
		Collection<V> closest = kdtree.collectKNearest(v, 5);
		double[] closestPointOnSurface = new double[3];
		double[] pointOnSurface = new double[3];
		double distance = -1;
		HashSet<F> visitedFaces = new HashSet<F>();
		for(V vc : closest) {
			for(F f: HalfEdgeUtilsExtra.getFaceStar(vc)) {
				if(visitedFaces.contains(f)) continue;
				pointOnSurface = projectOnto(v, f, ras);
				double actDist = Rn.euclideanDistanceSquared(pointOnSurface, v);
				if(distance == -1 || actDist < distance) {
					System.arraycopy(pointOnSurface, 0, closestPointOnSurface, 0, 3);
					distance = actDist;
				}
				visitedFaces.add(f);
			}
		}
		return closestPointOnSurface;
	}

	private double[] projectOnto(double[] pos, F f, AdapterSet as) {
		double[] fn = as.getD(Normal.class, f);
		List<V> verts = HalfEdgeUtils.boundaryVertices(f);
		
		double[] 
		       v1 = as.getD(Position3d.class, verts.get(0)),
		       v2 = as.getD(Position3d.class, verts.get(1)),
		       v3 = as.getD(Position3d.class, verts.get(2));
		
		double[] 
		       vt1 = Rn.subtract(null, v1, v1),
		       vt2 = Rn.subtract(null, v2, v1),
		       vt3 = Rn.subtract(null, v3, v1);
		
		double[] proj = Rn.subtract(null, pos, v1);
		
		Rn.projectOntoComplement(proj, proj, fn);
		
		double[] bc = barycentricCoordinates(proj,vt1,vt2,vt3);
		if(bc[0] < 0) {
			proj = projectOntoLine(proj,vt2,vt3);
		}
		if(bc[1] < 0) {
			proj = projectOntoLine(proj,vt1,vt3);
		}
		if(bc[2] < 0) {
			proj = projectOntoLine(proj,vt1,vt2);
		}
		return Rn.add(null,proj,v1);
	}
	
	private double[] projectOntoLine(double[] pos, double[] v, double[] w) {
		double[] 
		       l = Rn.subtract(null, w, v),
		       np = Rn.subtract(null, pos, v);
		Rn.projectOnto(np, np, l);
		Rn.add(np,np,v);
		return np;
	}

	private double[] barycentricCoordinates(
			double[] pos, 
			double[] v1,
			double[] v2, 
			double[] v3) {
		double[][] A = new double[3][4];
		System.arraycopy(v1, 0, A[0], 0, 3);
		System.arraycopy(v2, 0, A[1], 0, 3);
		System.arraycopy(v3, 0, A[2], 0, 3);
		for(int i = 0; i < 3; ++i ) {
			A[i][3] = 1.0;
		}
		DenseVector b = new DenseVector(4);
		b.set(0,pos[0]);
		b.set(1,pos[1]);
		b.set(2,pos[2]);
		b.set(3,1.0);
		DenseVector baryCoords = new DenseVector(3);
		DenseMatrix m = new DenseMatrix(A);
		m.transSolve(b, baryCoords);
		return baryCoords.getData();
	}

	public void evaluateGradient(
		//input
			HalfEdgeDataStructure<V, E, F> hds,
			DomainValue x,
		//output
			Gradient grad
	) {
		for(V v: hds.getVertices()) {
			double[] pt = closestPointMap.get(v);
			double[] vpos = new double[3];
			FunctionalUtils.getPosition(v, x, vpos);
			double[] v2pt = Rn.subtract(null, vpos, pt);
			Rn.times(v2pt, 2.0, v2pt);
			FunctionalUtils.addVectorToGradient(grad, 3*v.getIndex(), v2pt);
		}
	}

	public void evaluateHessian(
		// input
			HalfEdgeDataStructure<V, E, F> G, DomainValue x,
		// output
			Hessian hess) {
	}

	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void setReferenceSurface(HDS refSurface, AdapterSet as) {
		this.refSurface = refSurface;
		Triangulator.triangulateByCuttingCorners(refSurface,as);
		kdtree = new KdTree<V, E, F>(refSurface, as, 10, false);
		refas = as;
	}
	
	
	public KdTree<V, E, F> getKdtree() {
		return kdtree;
	}
	
}
