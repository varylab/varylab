package de.varylab.varylab.math.nurbs;

import de.jreality.math.Rn;

public class NURBSAlgorithm {

	/**
	 * Algorithm A4.3 from the NURBS Book
	 * @param p
	 * @param U
	 * @param q
	 * @param V
	 * @param Pw
	 * @param u
	 * @param v
	 * @param S
	 */
	public static void SurfacePoint(int p, double[] U, int q, double[] V, double[][][] Pw, double u, double v, double[] S) {
		int n = Pw.length - 1;
		int m = Pw[0].length - 1;
		int uspan = FindSpan(n, p, u, U);
		int vspan = FindSpan(m, q, v, V);
		double[] Nu = new double[p + 1];
		double[] Nv = new double[q + 1];
		BasisFuns(uspan, u, p, U, Nu);
		BasisFuns(vspan, v, q, V, Nv);
		double[][] temp = new double[q + 1][4];
		for (int l = 0; l <= q; l++)
			for (int k = 0; k <= p; k++)
				Rn.add(temp[l], temp[l], Rn.times(null, Nu[k], Pw[uspan - p + k][vspan - q + l]));
		for (int l = 0; l <= q; l++)
			Rn.add(S, S, Rn.times(null, Nv[l], temp[l]));
	}
	
	
	
	
	/**
	 * Algorithm A2.1 from the NURBS Book
	 * @param p
	 * @param u
	 * @param U
	 * @return
	 */
	public static int FindSpan(int n, int p, double u, double[] U) {
		if (u == U[n + 1])
			return n;
		int low = p;
		int high = n + 1;
		int mid = (low + high) / 2;
		while (u < U[mid] || u >= U[mid + 1]) {
			if (u < U[mid]) {
				high = mid;
			} else {
				low = mid;
			}
			mid = (low + high) / 2;
		}
		return mid;
	}
	
	/**
	 * Algorithm A2.2 from the NURBS Book
	 * @param i
	 * @param u
	 * @param p
	 * @param U
	 * @param N
	 */
	public static void BasisFuns(int i, double u, int p, double[] U, double[] N) {
		N[0] = 1.0;
		double[] left = new double[p + 1];
		double[] right = new double[p + 1];
		for (int j = 1; j <= p; j++) {
			left[j] = u - U[i + 1 - j];
			right[j] = U[i + j] - u;
			double saved = 0.0;
			for (int r = 0; r < j; r++) {
				double temp = N[r] / (right[r+1] + left[j-r]);
				N[r] = saved + right[r+1] * temp;
				saved = left[j-r] * temp;
			}
			N[j] = saved;
		}
	}
	
	
	
	
	
	/**
	 * Algorithm A2.3 from the NURBS Book
	 * @param i
	 * @param u
	 * @param p
	 * @param n
	 * @param U
	 * @param ders
	 */
	public void DersBasisFuns(int i, double u, int p, int n, double[] U, double[][] ders) {
		double[][] ndu = new double[p + 1][p + 1];
		double[] left = new double[p + 1];
		double[] right = new double[p + 1];
		ndu[0][0] = 0.0;
		for (int j = 1; j <= p; j++) {
			left[j] = u - U[i + 1 - j];
			right[j] = U[i+j] - u;
			double saved = 0.0;
			for (int r = 0; r < j; r++) {
				ndu[j][r] = right[r + 1] + left[j - r];
				double temp = ndu[r][j - 1] / ndu[j][r];
				ndu[r][j] = saved + right[r + 1] * temp;
				saved = left[j - r] * temp;
			}
			ndu[j][j] = saved;
		}
		for (int j = 0; j <= p; j++) {
			ders[0][j] = ndu[j][p];
		}
		for (int r = 0; r <= p; r++) {
			int s1 = 0;
			int s2 = 1;
			double[][] a = new double[2][p + 1];
			a[0][0] = 1.0;
			for (int k = 1; k <= n; k++) {
				double d = 0.0;
				int rk = r - k;
				int pk = p - k;
				if (r >= k) {
					a[s2][0] = a[s1][0] / ndu[pk + 1][rk];
					d = a[s2][0] * ndu[rk][pk];
				}
				int j1, j2;
				if (rk >= -1)
					j1 = 1;
				else
					j1 = -rk;
				if (r - 1 <= pk) 
					j2 = k-1;
				else
					j2 = p-r;
				for (int j = j1; j <= j2; j++) {
					a[s2][j] = (a[s1][j] - a[s1][j-1]) / ndu[pk+1][rk+j];
					d += a[s2][j] * ndu[rk + j][pk];
				}
				if (r <= pk) {
					a[s2][k] = -a[s1][k-1] / ndu[pk+1][r];
					d += a[s2][k] * ndu[r][pk];
				}
				ders[k][r] = d;
				int j = s1;
				s1 = s2;
				s2 = j;
			}
		}
		int r = p;
		for (int k = 1; k <= p; k++) {
			for (int j = 0; j <= p; j++) {
				ders[k][j] *= r;
			}
			r *= (p - k);
		}
	}
	
	
	
	
	
	
	
}
