package de.varylab.varylab.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.EdgeVector;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.dec.VectorFieldMapAdapter;
import de.varylab.varylab.hds.adapter.ConnectionAdapter;

public class ConnectionUtility {

	public static double[] transportVector(
			double[] vector, 
			Face<?, ?, ?> actFace, 
			Edge<?, ?, ?> bdEdge,
			ConnectionAdapter trivialConnection,
			AdapterSet adapters) {
		double[]
		       common = adapters.get(EdgeVector.class, bdEdge, double[].class),
		       le = adapters.get(EdgeVector.class,bdEdge.getPreviousEdge().getOppositeEdge(),double[].class),
		       re = adapters.get(EdgeVector.class,bdEdge.getOppositeEdge().getPreviousEdge(),double[].class);
		Matrix 
			bl = new DenseMatrix(orthonormalBasis(common,le)),
			br = new DenseMatrix(orthonormalBasis(common,re)),
			rot = new DenseMatrix(rotationMatrix(trivialConnection.get(bdEdge, adapters))),
			tmp = new DenseMatrix(3,3),
			tmp2 = new DenseMatrix(3,3);
		DenseVector tvec = new DenseVector(3);
		rot.mult(bl, tmp);
		br.transAmult(tmp, tmp2);
		tmp2.mult(new DenseVector(vector), tvec);
		return Matrices.getArray(tvec);
	}

	private static double[][] rotationMatrix(double angle) {
		return new double[][] {
				new double[]{Math.cos(angle),-Math.sin(angle),0.0},
				new double[]{Math.sin(angle),Math.cos(angle),0.0},
				new double[]{0.0,0.0,1.0}
				};
	}

	private static double[][] orthonormalBasis(double[] v1, double[] v2) {
		double[]
		       b1 = Rn.normalize(null, v1),
		       b2 = Rn.normalize(null, Rn.add(null, v2, Rn.times(null, -Rn.innerProduct(v2, b1), b1))),
		       b3 = Rn.normalize(null, Rn.crossProduct(null, b1, b2));
		return new double[][]{b1,b2,b3};
	}
	
	public static <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> VectorFieldMapAdapter calculateVectorField(
		HDS heds, 
		AdapterSet adapters, 
		ConnectionAdapter connection) 
	{
		Map<Face<?, ?, ?>, double[]> vf = new HashMap<Face<?,?,?>, double[]>();
		Stack<F> queue = new Stack<F>();
		F f = heds.getFace(0);
		E e = f.getBoundaryEdge();
		double[] ev = Rn.subtract(null, 
						adapters.get(Position3d.class, e.getTargetVertex(), double[].class),
						adapters.get(Position3d.class, e.getStartVertex(), double[].class));
		vf.put(f, Rn.normalize(null, ev));
		queue.add(f);
		while(!queue.isEmpty()) {
			F actFace = queue.pop();
			E bdEdge = actFace.getBoundaryEdge();
			do {
				F neighFace = bdEdge.getRightFace();
				if(neighFace != null && !(vf.containsKey(neighFace))) {
					double[] neighVector = ConnectionUtility.transportVector(vf.get(actFace),actFace,bdEdge,connection,adapters);
					vf.put(neighFace, neighVector);
					//	System.out.println(neighFace +":"+Arrays.toString(neighVector));
					queue.add(neighFace);
				}
				bdEdge = bdEdge.getNextEdge();
			} while(bdEdge != actFace.getBoundaryEdge());
		}
		return new VectorFieldMapAdapter(vf, "Trivial Connection");
	}

}
