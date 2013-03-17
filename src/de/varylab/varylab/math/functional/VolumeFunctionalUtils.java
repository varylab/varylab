package de.varylab.varylab.math.functional;

import java.util.Collection;
import java.util.List;

import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.varylab.varylab.math.util.CollectionUtility;

public class VolumeFunctionalUtils {

	public static <V extends Vertex<?,?,?>> double calculateSumDetSquared(DomainValue x, List<V> verts) {
		double result = 0.0;
		for(Collection<V> tets : CollectionUtility.subsets(verts,4)) {
			result += detSquared(x,tets);
		}
		return result;
	}
	
	// calculate the volume of the tetrahedron spanned by the verts
	public static <V extends Vertex<?,?,?>> double detSquared(DomainValue x, Collection<V> verts) {
		double[][] tet = new double[4][4];
		int i = 0;
		for(V v : verts) {
			tet[i++] = FunctionalUtils.getHomogPosition(x,v);
		}
		double det = Rn.determinant(tet);
		return det*det;
	}
	
	
	
	public static 
		<V extends Vertex<?,?,?>>
	void addSumDetSquaredGradient(DomainValue x, Gradient G, List<V> verts, double scale) {
		for(Collection<V> tets : CollectionUtility.subsets(verts,4)) {
			addDetSquaredGradient(x, G, tets, scale);
		}
	}

	public static <
		V extends Vertex<?,?,?>
	> void addDetSquaredGradient(DomainValue x, Gradient G,	Collection<V> tets, double scale) {
		Matrix mat = new Matrix();
		int i = 0;
		for(V w : tets) {
			mat.setColumn(i++, FunctionalUtils.getHomogPosition(x,w));
		}
		int j = 0;
		for (V w : tets){
			int vertexIndex = w.getIndex();
			G.add(vertexIndex * 3 + 0, differentiateDet2(mat, 0, j) * scale);
			G.add(vertexIndex * 3 + 1, differentiateDet2(mat, 1, j) * scale);
			G.add(vertexIndex * 3 + 2, differentiateDet2(mat, 2, j) * scale);
			j++;
		}
	}

	public static double differentiateDet2(Matrix A, int i, int j){
		double a_11 = A.getEntry(0, 0);
		double a_12 = A.getEntry(0, 1);
		double a_13 = A.getEntry(0, 2);
		double a_14 = A.getEntry(0, 3);
		double a_21 = A.getEntry(1, 0);
		double a_22 = A.getEntry(1, 1);
		double a_23 = A.getEntry(1, 2);
		double a_24 = A.getEntry(1, 3);
		double a_31 = A.getEntry(2, 0);
		double a_32 = A.getEntry(2, 1);
		double a_33 = A.getEntry(2, 2);
		double a_34 = A.getEntry(2, 3);
		double detA = (a_11 * a_22 * a_33 - a_11 * a_22 * a_34 - a_11 * a_32 * a_23 + a_11 * a_32 * a_24 + a_11 * a_23 * a_34 - a_11 * a_24 * a_33
			     - a_21 * a_12 * a_33 + a_21 * a_12 * a_34 + a_21 * a_32 * a_13 - a_21 * a_32 * a_14 - a_21 * a_13 * a_34 + a_21 * a_14 * a_33
			     + a_31 * a_12 * a_23 - a_31 * a_12 * a_24 - a_31 * a_22 * a_13 + a_31 * a_22 * a_14 + a_31 * a_13 * a_24 - a_31 * a_14 * a_23
			     - a_12 * a_23 * a_34 + a_12 * a_24 * a_33 + a_22 * a_13 * a_34 - a_22 * a_14 * a_33 - a_32 * a_13 * a_24 + a_32 * a_14 * a_23);
		if (i == 0 && j == 0){
			return 2 * detA * (a_22 * a_33 - a_22 * a_34 - a_32 * a_23 + a_32 * a_24 + a_23 * a_34 - a_24 * a_33);
		} else if (i == 0 && j == 1){
			return 2 * detA * (-a_21 * a_33 + a_21 * a_34 + a_31 * a_23 - a_31 * a_24 - a_23 * a_34 + a_24 * a_33);
		} else if (i == 0 && j == 2){
			return 2 * detA * (a_21 * a_32 - a_21 * a_34 - a_31 * a_22 + a_31 * a_24 + a_22 * a_34 - a_32 * a_24);
		} else if (i == 0 && j == 3){
			return 2 * detA * (-a_21 * a_32 + a_21 * a_33 + a_31 * a_22 - a_31 * a_23 - a_22 * a_33 + a_32 * a_23);
		} else if (i == 1 && j == 0){
			return 2 * detA * (-a_12 * a_33 + a_12 * a_34 + a_32 * a_13 - a_32 * a_14 - a_13 * a_34 + a_14 * a_33);
		} else if (i == 1 && j == 1){
			return 2 * detA * (a_11 * a_33 - a_11 * a_34 - a_31 * a_13 + a_31 * a_14 + a_13 * a_34 - a_14 * a_33);
		} else if (i == 1 && j == 2){
			return 2 * detA * (-a_11 * a_32 + a_11 * a_34 + a_31 * a_12 - a_31 * a_14 - a_12 * a_34 + a_32 * a_14);
		} else if (i == 1 && j == 3){
			return 2 * detA * (a_11 * a_32 - a_11 * a_33 - a_31 * a_12 + a_31 * a_13 + a_12 * a_33 - a_32 * a_13);
		} else if (i == 2 && j == 0){
			return 2 * detA * (a_12 * a_23 - a_12 * a_24 - a_22 * a_13 + a_22 * a_14 + a_13 * a_24 - a_14 * a_23);
		} else if (i == 2 && j == 1){
			return 2 * detA * (-a_11 * a_23 + a_11 * a_24 + a_21 * a_13 - a_21 * a_14 - a_13 * a_24 + a_14 * a_23);
		} else if (i == 2 && j == 2){
			return 2 * detA * (a_11 * a_22 - a_11 * a_24 - a_21 * a_12 + a_21 * a_14 + a_12 * a_24 - a_22 * a_14);
		} else if (i == 2 && j == 3){
			return 2 * detA * (-a_11 * a_22 + a_11 * a_23 + a_21 * a_12 - a_21 * a_13 - a_12 * a_23 + a_22 * a_13);
		}
		return 0.0;
	}
	

}
