package de.varylab.varylab.utilities;

import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Rn;

public class MathUtility {
	
	public static long binomCoeff(int n, int k) {
		if (n - k == 1 || k == 1)
			return n;
		long[][] b = new long[n + 1][n - k + 1];
		b[0][0] = 1;
		for (int i = 1; i < b.length; i++) {
			for (int j = 0; j < b[i].length; j++) {
				if (i == j || j == 0)
					b[i][j] = 1;
				else if (j == 1 || i - j == 1)
					b[i][j] = i;
				else
					b[i][j] = b[i - 1][j - 1] + b[i - 1][j];
			}
		}
		return b[n][n - k];
	}
	
	public static double[] get3DPoint(double[] fourDPoint){
		if(fourDPoint.length != 4){
			System.out.println("NO 4D point");
		}
		double[] threeDPoint = new double[3];
		threeDPoint[0] = fourDPoint[0] / fourDPoint[3];
		threeDPoint[1] = fourDPoint[1] / fourDPoint[3];
		threeDPoint[2] = fourDPoint[2] / fourDPoint[3];
		return threeDPoint;
	}
	
	public static double[][] get3DControlPoints(double[][]Pw){
		double[][] P = new double[Pw.length][3];
		for (int i = 0; i < P.length; i++) {
			P[i] = get3DPoint(Pw[i]);
		}
		return P;
	}
	
	public static double[][][] get3DControlmesh(double[][][]Pw){
		double[][][] P = new double[Pw.length][Pw[0].length][3];
		for (int i = 0; i < P.length; i++) {
			for (int j = 0; j < P[0].length; j++) {
				P[i][j] = get3DPoint(Pw[i][j]);
			}
		}
		return P;
	}
	
	public static double[] getFirstComponents(double[] fourDPoint){
		double[] p = new double[3];
		for (int i = 0; i < p.length; i++) {
			p[i] = fourDPoint[i];
		}
		return p;
	}
	
	public static double[][][] getFirstComponents3Controlmesh(double[][][]Pw){
		double[][][] P = new double[Pw.length][Pw[0].length][3];
		for (int i = 0; i < P.length; i++) {
			for (int j = 0; j < P[0].length; j++) {
				P[i][j] = getFirstComponents(Pw[i][j]);
			}
		}
		return P;
	}
	
	public static Matrix makeMappingMatrix(double[][] from, double[][] to) {
		double[] from1 = Rn.convertArray2DToArray1D(null, from);
		Matrix F = new Matrix(from1);
		F.transpose();
		F.invert();
		double[] to1 = Rn.convertArray2DToArray1D(null, to);
		Matrix T = new Matrix(to1);
		T.transpose();
		T.multiplyOnRight(F);
		return T;
	}

	public static double[] getDiagonalIntersection(double[] N, double[] p1, double[] p2, double[] p3, double[] p4) {
		double[] p1N = Rn.add(null, p1, N);
		double[] p2N = Rn.add(null, p2, N);
		double[] planeF = P3.planeFromPoints(null, p1, p2, p3);
		double[] plane01 = P3.planeFromPoints(null, p1, p3, p1N);
		double[] plane02 = P3.planeFromPoints(null, p2, p4, p2N);
		return P3.pointFromPlanes(null, planeF, plane01, plane02);
	}
	
	
}
