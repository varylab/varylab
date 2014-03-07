package de.varylab.varylab.plugin.ddg;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position4d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.varylab.varylab.utilities.MathUtility;

public class KoenigsDual extends AlgorithmPlugin {

	public KoenigsDual() {
	}

	@Override
	public String getAlgorithmName() {
		return "Create Koenigs Dual";
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.DDG;
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		Map<E, double[]> edgeNormals = new HashMap<>();
		for (E e : hds.getPositiveEdges()) {
			double[] n = getEdgeNormal(e, a);
			edgeNormals.put(e, n);
			edgeNormals.put(e.getOppositeEdge(), n);
		}
		dualizeSurfaceKoenigs(hds, edgeNormals, 0.0, a);
		hi.update();
	}
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void dualizeSurfaceKoenigs(HalfEdgeDataStructure<V, E, F> S, Map<E, double[]> edgeNormals, double associatedPsi, AdapterSet a) {
		Map<V, double[]> dualMap = new HashMap<V, double[]>();
		Set<E> doneEdges = new HashSet<E>();
		
		Queue<E> edgeQueue = new LinkedList<E>();
		Map<E, Double> factorMap = new HashMap<E, Double>();
	
		E e0 = S.getEdge(0);
		for (E e : S.getPositiveEdges()) {
			if (e.getLeftFace() != null && e.getRightFace() != null) {
				e0 = e;
				break;
			}
		}
		E e0Opp = e0.getOppositeEdge();
		V v0 = e0.getStartVertex();
		V v1 = e0.getTargetVertex();
		double[] g = edgeNormals.get(e0);
		double[] n = MathUtility.calculateDiagonalIntersection(e0.getLeftFace(), a);
		double[] v1P = a.getD(Position4d.class, v1);
		double[] v0P = a.getD(Position4d.class, v0);
		double[] eVec = Rn.subtract(null, v1P, v0P);
		eVec = associatedEdgeRotation(eVec, g, n, associatedPsi);
		double[] v1s = Rn.add(null, v0P, eVec);
		
		dualMap.put(v0, v0P);
		dualMap.put(v1, v1s);
		factorMap.put(e0, 1.0);
		factorMap.put(e0.getOppositeEdge(), 1.0);
		if (e0.getLeftFace() != null) edgeQueue.offer(e0);
		if (e0Opp.getLeftFace() != null) edgeQueue.offer(e0Opp);
		
		while (!edgeQueue.isEmpty()){
			E e = edgeQueue.poll();
			E ee = e.getPreviousEdge();
			if (doneEdges.contains(e)) {
				continue;
			} else {
				doneEdges.add(e);
			}
			double lambda = factorMap.get(e);
			
			double[] A = a.getD(Position4d.class, e.getStartVertex());
			double[] B = a.getD(Position4d.class, e.getTargetVertex());
			double[] D = a.getD(Position4d.class, ee.getStartVertex());
			double[] M = MathUtility.calculateDiagonalIntersection(e.getLeftFace(), a);
			Pn.dehomogenize(A, A);
			Pn.dehomogenize(B, B);
			Pn.dehomogenize(D, D);
			Pn.dehomogenize(M, M);
			
			double[] AD = Rn.subtract(null, D, A);
			double[] MD = Rn.subtract(null, D, M);
			double[] e2 = Rn.subtract(null, B, M);
	
			double beta = Rn.euclideanNorm(e2);
			Rn.normalize(e2, e2);
			
			double alpha = lambda / beta;
			
			double delta = Rn.innerProduct(MD, e2);
			double scale = delta * alpha;
	
			Rn.times(AD, 1 / scale, AD);
			
			g = edgeNormals.get(ee);
			n =  MathUtility.calculateDiagonalIntersection(ee.getLeftFace(), a);
			AD = associatedEdgeRotation(AD, g, n, associatedPsi);
			
			double[] As = dualMap.get(e.getStartVertex());
			double[] Ds = Rn.add(null, As, AD);
			dualMap.put(ee.getStartVertex(), Ds);
			
			edgeQueue.offer(ee);
			factorMap.put(ee, scale);
			if (ee.getOppositeEdge().getLeftFace() != null) {
				edgeQueue.offer(ee.getOppositeEdge());
				factorMap.put(ee.getOppositeEdge(), scale);
			}
		}
		for (V v : S.getVertices()){
			double[] p = dualMap.get(v);
			if (p != null) {
				a.set(Position.class, v, p);
			}
		}
	}
	
	
	static double[] associatedEdgeRotation(double[] dualEdgeVec, double[] g, double[] n, double psi) {
		double alpha = Rn.euclideanAngle(g, n);
		double phi = atan2(sin(psi), cos(psi)*cos(alpha));
		double sinPsi = sin(psi);
		double tanAlpha = Math.tan(alpha);
		double scale = Math.sqrt(1 + sinPsi*sinPsi*tanAlpha*tanAlpha);
		Matrix R = new Matrix();
		MatrixBuilder.euclidean().rotate(-phi, g).assignTo(R);
		double[] r = R.multiplyVector(dualEdgeVec);
		Rn.times(r, scale, r);
		return r;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getEdgeNormal(E e, AdapterSet a) {
		double[] s = a.getD(Position4d.class, e.getStartVertex());
		double[] t = a.getD(Position4d.class, e.getTargetVertex());
		double[] eVec = Rn.subtract(null, t, s);
		Rn.normalize(eVec, eVec);
		double[] sVec = Pn.dehomogenize(null, s);
		sVec[3] = 0.0;
		double lambda = Rn.innerProduct(sVec, eVec);
		Rn.times(eVec, lambda, eVec);
		Rn.subtract(sVec, sVec, eVec);
		Rn.normalize(sVec, sVec);
		double check = Math.abs(Rn.innerProduct(sVec, eVec));
		assert check < 1E-7 : "edge normal assertion: " + check;
		return sVec;
	}


}
