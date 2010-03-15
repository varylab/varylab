package de.varylab.varylab.math.functional;

import de.jreality.math.Matrix;

public class VolumeFunctionalUtils {

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
