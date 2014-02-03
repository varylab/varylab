package de.varylab.varylab.halfedge.adapter;

import java.util.Collection;

import de.jreality.math.Rn;
import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.VectorField;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.numericalMethods.algebra.linear.solve.AXB;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.optimization.util.CollectionUtility;

@VectorField
public class ConicalNormalAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

	public ConicalNormalAdapter() {
		super(VVertex.class, null, null, double[].class, true, false);
	}

	@Override
	public double[] getVertexValue(VVertex v, AdapterSet a) {
		double[] normal = new double[3];
		for(Collection<VFace> triples : CollectionUtility.subsets(HalfEdgeUtilsExtra.getFaceStar(v),3)) {
			Rn.add(normal, normal, coneNormal(triples,a));
		}
		
		return normal;
	}
	
	private static double[] coneNormal(Collection<VFace> neighbors, AdapterSet as) {
		double[] normal = new double[3];
		int size = neighbors.size();
		double[][] faceNormals = new double[size][3];
		int i = 0;
		for(VFace f: neighbors) {
			faceNormals[i++] = as.getD(Normal.class,f);
		}
		
		double[]
				b = new double[]{Rn.euclideanNorm(faceNormals[0]), Rn.euclideanNorm(faceNormals[1]), Rn.euclideanNorm(faceNormals[2])};
//		double[][] 
//				Q = new double[3][3],
//				R = new double[3][3];
//		Householder.decompose(faceNormals, Q, R);
//		double[] x = new double[3];
//		RXB.solve(R, x, b);
//		MatrixOperations.transpose(Q, Q);
//		MatrixOperations.times(normal, x, Q);
		AXB.solve(faceNormals, normal, b);
		return normal;
	}
	
}
