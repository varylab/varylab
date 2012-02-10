package de.varylab.varylab.plugin.nurbs.math;


import java.util.ArrayList;
import java.util.Arrays;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;

/**
 *<p><strong>Some useful definitions and explanations</strong></p>
 * 
 * <strong>1.Knot vector</strong><br/>
 * The knot vector is defined by U:={u[0],u[1],...u[m-1],u[m]} where u[i]is a
 * double value and u[i] <= u[j] for all i < j. <br/> 
 * A special case (and this is what we use) is a clamped knot vector.<br/>
 * This vector has the following property: 
 * if u[0] = u[1] = ...= u[i] != u[i+1] then u[m-i-1] != u[m-i] =...= u[m-1] = u[m].<br/>
 * For example U:={0,0,0,1,2,2,2} is clamped.<br/>
 * The B-spline representation with a knot vector of the form U={0,...,0,1,...,1} ,
 * where 0 and 1 appear p + 1 times, is a generalization<br/> of the Bezier
 * representation.<br/>
 * More general we define the Bezier representation as U:={a,...,a,b,...,b} a < b(used in decomposeSurface)<br/>
 * This is a translation 0 -> a and a dilation 1 -> (b - a) of the Bernstein polynomials.
 * <p></p>
 * 
 * <strong>2.Degree</strong><br/>
 * The degree:=p is the degree of the basis functions. If U is clamped and defined 
 * like above, then p = i.<br/><br/>
 * 
 * <strong>3.Control  mesh</strong><br/>
 * The control polygon of a curve (or control mesh of a surface) Pw are the control
 * points corresponding to the basis functions.<br/><br/>
 * 
 * <strong>4.Formulas</strong><br/>
 * Everything is defined like above.<br/>
 * m = U.length - 1<br/>
 * n = Pw.length - 1<br/>
 * n = m - p - 1 <==> p = m - n - 1 <==> n = U.length - p - 2  (this fact is used in FindSpan)<br/>
 * hence a NURBS-surface is completely determined by U,V,Pw
 * 
 * 
 * <p>
 * @author seidel
 * 
 * 
 */
public class NURBSAlgorithm {
	
	private static long binomialCoefficient(int n, int k) {
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

	/**
	 * Algorithm A2.1 from the NURBS Book
	 * 
	 * @param n = U.length - p - 2 or number of control points - 1  (see page 82 proposition 3.2 NURBS Book)
	 * @param p = degree
	 * @param u = parameter
	 * @param U = knot vector
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
	
	public static double[][][] affineCoords(double[][][] P){
		double affine[][][] = new double [P.length][P[0].length][3];
		for (int i = 0; i < P.length; i++) {
			for (int j = 0; j < P[0].length; j++) {
				for (int k = 0; k < 3; k++) {
					affine[i][j][k] = P[i][j][k]/P[i][j][3];
				}
				
			}
		}
		return affine;
	}

	/**
	 * Algorithm A2.2 from the NURBS Book
	 * 
	 * @param i =  knot span computed by FindSpan
	 * @param u = parameter
	 * @param p = degree
	 * @param U = knot vector
	 * @param N = empty array of length p + 1
	 *            p = degree assuming u is in the ith span ==> nonzero basis
	 *            functions are N[i-p][p],...,N[i][p]
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
				double temp = N[r] / (right[r + 1] + left[j - r]);
				N[r] = saved + right[r + 1] * temp;
				saved = left[j - r] * temp;
			}
			N[j] = saved;
		}
	}


	/**
	 * Algorithm A2.3 from the NURBS Book
	 * 
	 * @param i = knot span given by FindSpan
	 * @param u = the point in the domain
	 * @param p = the degree
	 * @param n = the highest derivative
	 * @param U = knot vector
	 * @param ders  is an empty array[n+1][p+1]
	 * 
	 */
	
	public static void DersBasisFuns(int i, double u, int p, int n, double[] U,double[][] ders) {
		
		double[][] ndu = new double[p + 1][p + 1];
		double[] left = new double[p + 1];
		double[] right = new double[p + 1];
		ndu[0][0] = 1.0;
		for (int j = 1; j <= p; j++) {
			left[j] = u - U[i + 1 - j];
			right[j] = U[i + j] - u;
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
					j2 = k - 1;
				else
					j2 = p - r;
				for (int j = j1; j <= j2; j++) {
					a[s2][j] = (a[s1][j] - a[s1][j - 1]) / ndu[pk + 1][rk + j];
					d += a[s2][j] * ndu[rk + j][pk];
				}
				if (r <= pk) {
					a[s2][k] = -a[s1][k - 1] / ndu[pk + 1][r];
					d += a[s2][k] * ndu[r][pk];
				}
				ders[k][r] = d;
				int j = s1;
				s1 = s2;
				s2 = j;
			}
		}
		int r = p;
		for (int k = 1; k <= n; k++) {
			for (int j = 0; j <= p; j++) {
				ders[k][j] *= r;
			}
			r *= (p - k);
		}
	}

	/**
	 * Algorithm A2.5 from the NURBS book.
	 * 
	 * Computes all n derivatives of one basis function N_i,p (derivatives from
	 * the right)
	 * 
	 * 
	 */

	public static void dersOneBasisFuns(int p,int m, double[] U, int i, double u, int n, double[] ders) {

		double[][] N = new double[p+1][p+1];
		double saved = 0.0;
		double Uleft, Uright, temp;
		double[] nD = new double[n+2];
		if (u < U[i] || u >= U[i + p + 1]) {
			for (int k = 0; k <= n; k++) {
				ders[k] = 0.0;
			}
			return;
		}
		// initialize zeroth-degree functions
		for (int j = 0; j <= p; j++) {
			if (u >= U[i + j] && u < U[i + j + 1]) {
				N[j][0] = 1.0;
			} else {
				N[j][0] = 0.0;
				
			}
		}
		// compute full triangular table
		for (int k = 1; k <= p; k++) {
			if (N[0][k - 1] == 0)
				saved = 0.0;
			else
				saved = ((u - U[i]) * N[0][k - 1]) / (U[i + k] - U[i]);
			for (int j = 0; j < p - k + 1; j++) {
				Uleft = U[i + j + 1];
				Uright = U[i + j + k + 1];
				if (N[j + 1][k - 1] == 0.0) {
					N[j][k] = saved;
					saved = 0.0;
				} else {
					temp = N[j + 1][k - 1] / (Uright - Uleft);
					N[j][k] = saved + (Uright - u) * temp;
					saved = (u - Uleft) * temp;
				}
			}
		}
		ders[0] = N[0][p];
		// compute the derivatives
		for (int k = 1; k <= n; k++) {
			for (int j = 0; j <= k; j++) {
				nD[j] = N[j][p - k];
			}
			// l = jj in the book
			for (int l = 0; l <=k; l++) {
				if (nD[0] == 0.0) {
					saved = 0.0;
				} else
					saved = nD[0] / (U[i + p - k + l] - U[i]);
				for (int j = 0; j < k - l + 1; j++) {
					Uleft = U[i + j + 1];
					Uright = U[i + j + p + l + 1];
					if (nD[j + 1] == 0.0) {
						nD[j] = (p - k + l) * saved;
						saved = 0.0;
					} else {
						temp = nD[j + 1] / (Uright - Uleft);
						nD[j] = (p - k + l) * (saved - temp);
						saved = temp;
					}
				}
			}
			ders[k] = nD[0]; /* kth derivative */
		}
	}

	/**
	 * Algorithm A3.2 from the NURBS Book
	 * 
	 * @param n
	 * @param p
	 * @param U
	 * @param P
	 * @param u
	 * @param d 
	 * @param CK
	 */

	public static void CurveDerivatives(int n, int p, double[] U, double[] P, double u, int d, double[] CK) {
		int du = Math.min(d, p);
		for (int k = p + 1; k <= d; k++) {
			CK[k] = 0;
		}
		int span = FindSpan(n, p, u, U);
		double nders[][] = new double[du + 1][p + 1];
		DersBasisFuns(span, u, p, du, U, nders);
		for (int k = 0; k <= du; k++) {
			CK[k] = 0;
			for (int j = 0; j <= p; j++) {
				CK[k] = CK[k] + nders[k][j] * P[span - p + j];
			}
		}
	}
	
	/**
	 * Algorithm A3.6 from the NURBS Book
	 * @param n
	 * @param p = degree
	 * @param U = knotvector
	 * @param m
	 * @param q = degree
	 * @param V = knotvector
	 * @param P = controlmesh
	 * @param u = component of the point in the domain
	 * @param v = component of the point in the domain
	 * @param d is the highest derivative in both directions i.e. k + l <= d
	 * @param SKL array stores the partial derivatives
	 */
	
	public static void SurfaceDerivatives(int n, int p, double[] U,int m, int q, double [] V,double[][][]P,double u, double v, int d, double[][][]SKL){

		int du = Math.min(d, p);
		for(int k=p+1;k<=d;k++){
			for(int l=0;l<=d-k;l++){
			}
		}
		int dv = Math.min(d, q);

		int uspan = FindSpan(n,p,u,U);
		double [][] Nu = new double[du + 1][p + 1];
		DersBasisFuns(uspan, u, p, du, U, Nu);
		int vspan = FindSpan(m,q,v,V);
		double [][] Nv = new double[dv + 1][q + 1];
		DersBasisFuns(vspan, v, q, dv, V, Nv);
		for(int k=0; k<=du; k++){
			double[] []temp = new double[q + 1][4];
			for(int s=0; s<=q; s++){
				for(int r = 0; r<= p; r++){
					Rn.add(temp[s], temp[s],Rn.times(null, Nu[k][r], P[uspan - p + r][vspan - q + s]));
				}
			}
			int dd = Math.min(d-k,dv);
			for(int l=0; l<=dd; l++){
				for(int s=0; s<=q; s++){
				 Rn.add(SKL[k][l], SKL[k][l], Rn.times(null, Nv[l][s], temp[s]));
				}
			}
		}
	}
	


	/**
	 * Algorithm A4.3 from the NURBS Book
	 * 
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
		for (int l = 0; l <= q; l++) {
			for (int k = 0; k <= p; k++) {
				Rn.add(temp[l], temp[l],Rn.times(null, Nu[k], Pw[uspan - p + k][vspan - q + l]));
			}
		}
		for (int l = 0; l <= q; l++) {
			Rn.add(S, S, Rn.times(null, Nv[l], temp[l]));
		}
	}
	

	/**
	 * Algorithm A4.4 from the NURBS Book
	 * 
	 * @param Aders
	 * @param wders
	 * @param d
	 * @param SKL
	 */
	public static void RatSurfaceDerivs(double[][][] Aders, double[][] wders, int d, double[][][] SKL) {
		int a = wders.length-1;
		int b = wders[0].length-1;
		for (int k = 0; k <= a; k++) {
			for (int l = 0; l <= b; l++) {
				double [] v = Aders[k][l];
				for (int j = 1; j <= l; j++) {
					v = Rn.add(v, v, Rn.times(null, - binomialCoefficient(l, j) * wders[0][j], SKL[k][l - j]));
				}
				for (int i = 1; i <= k; i++) {
					v = Rn.add(v, v, Rn.times(null, - binomialCoefficient(k, i) * wders[i][0], SKL[k - i][l]));
					double []v2 = new double[3];
					for (int j = 1; j <= l; j++) {
						v2 = Rn.add(v2, v2, Rn.times(null, binomialCoefficient(l, j) * wders[i][j], SKL[k - i][l - j]));
					}
					v = Rn.add(v, v, Rn.times(null, - binomialCoefficient(k, i) ,v2));
				}
				SKL[k][l] = Rn.times(null, 1/wders[0][0], v);
				}
		}
	}
	
	
	/**
	 * Algorithm A5.1 from the NURBS Book
	 * 
	 * @param np = Pw.length -1
	 * @param p  = Up degree
	 * @param UP = given knot vector
	 * @param Pw = given control mesh
	 * @param u  = new knot
	 * @param k  = FindSpan(UP.length - 2, p, u, UP);
	 * @param s  = the given multiplicity of u (i.e. u exits in UP s times)
	 * @param r  = multiplicity of u
	 * @param nq = UQ.length = np + r
	 * @param UQ = new knot vector
	 * @param Qw = new control mesh
	 */
	
	public static void CurveKnotIns(int np, int p, double[] UP, double[][]Pw, double u, int k, int s, int r, int nq, double[] UQ, double[][]Qw){
		
		int mp = np + p + 1; // == UP.length - 1
		nq = np + r;
		int L = 0;
		double[][] Rw = new double[p + 1][];
		/* Load the knot vector */
		for(int i = 0; i <= k; i++){
			UQ[i] = UP[i];
		}
		for(int i = 0; i <= r; i++){
			UQ[k + i] = u;
		}
		for(int i = k + 1; i <= mp; i++){
			UQ[i + r] = UP[i];
		}
		/* Save unaltered control points*/
		for(int i = 0; i <= k - p; i++){
			Qw[i] = Pw[i];
		}
		for(int i = k - s; i <= np; i++){
			Qw[i + r] = Pw[i];
		}
		for(int i = 0; i <= p - s; i++){
			Rw[i] = Pw[k - p + i];
		}
		/* insert the knot r times */
		for(int j = 1; j <= r; j++){
			L = k - p + j;
			for(int i = 0; i <= p - j - s; i++){
				double alpha = (u - UP[L + i]) / (UP[i + k + 1] - UP[L + i]);
				Rw[i] = Rn.add(null, Rn.times(null, alpha, Rw[i + 1]), Rn.times(null, 1.0 - alpha, Rw[i]));
			}
			Qw[L] = Rw[0];
			Qw[k + r - j - s] = Rw[p - j - s];
		}
		/* Load remaining control points */
		for(int i =  L + 1; i <= k - s; i++){
			Qw[i] = Rw[i - L];
		}
	}
	
	
	
	
	/**
	 * Algorithm A5.3 from the NURBS Book
	 * 
	 * @param np  = length of the original control mesh in u direction - 1
	 * @param p   = UP degree 
	 * @param UP  = original knot vector U
	 * @param mp  = length of the original control mesh in v direction - 1
	 * @param q   = UV degree
	 * @param VP  = original knot vector V
	 * @param Pw  = original control mesh
	 * @param dir = u/v direction
	 * @param uv  = new knot
	 * @param k   = FindSpan(UP.length - 2, p, uv, UP); / VP
	 * @param s   = original multiplicity of uv (i.e. uv exits already in UP/UQ s times)
	 * @param r   = insert uv r times (s + r <= p)
	 * @param nq  = length of the new control mesh in u direction - 1
	 * @param UQ  = new knot vector U
	 * @param mq  = length of the new control mesh in v direction - 1
	 * @param VQ  = new knot vector V
	 * @param Qw  = new control mesh
	 */
	
	public static void SurfaceKnotIns(int np, int p, double[] UP,int mp, int q, double[] VP, double[][][]Pw, boolean dir, double uv, int k ,int s, int r, int nq, double[]UQ, int mq, double[]VQ, double[][][]Qw){
		if(dir){ //u direction
			/* Load the u-knot vector into UQ */
			for(int i = 0; i <= k; i++){
				UQ[i] = UP[i];
			}
			for(int i = 1; i <= r; i++){
				UQ[k + i] = uv; /* uv is in U */
			}
			for(int i = k + 1; i <= np + p + 1; i++){
				UQ[i + r] = UP[i];
			}
			/* copy the v-knot vector into VQ */
			for (int i = 0; i <= mp + q + 1; i++) {
				VQ[i] = VP[i];
			}
			double[][] alpha = new double[p - s][r + 1]; // ??????
			/* save the alphas */
			for(int j = 1; j <= r; j++){
				/**
				 * 			     { 1 if: i <= k-p+r-1
				 * alpha[i][r] = { (uv - UP[i]) / (UP[i+p-r+1] - UP[i]) if: k-p+r <= i <= k-s
				 * 			     { 0 if: >= k-s+1
				 */					
				int L = k - p + j; 
				for(int i = 0; i <= p - j - s; i++ ){
					alpha[i][j] = (uv - UP[L + i])/(UP[i + k + 1] - UP[L + i]);
				}
			}
			for(int row = 0; row <= mp; row++){ /* for each row do */
				/* save unaltered control points */
				for(int i = 0; i <= k - p; i++){ // 0 ... k-p
					Qw[i][row] = Pw[i][row];
				}
				for(int i = k ; i <= np; i++){ // k + r ... Qw.length
					Qw[i + r][row] = Pw[i][row];
				}
				/* Load auxiliary  control points */
				double[][] Rw = new double[p + 1][]; // length = p + 1
				for(int i = 0; i <= p ; i++){ // hier liegt der Fehler
					Rw[i] = Pw[k - p + i][row];
//					System.out.println("UEBERTRAGEN Rw["+i+"] " + Arrays.toString(Rw[i]));
				}
				for(int j = 1; j <= r; j++){
					int L = k - p + j;
					for(int i = 0; i <= p - j - s ; i++ ){
						Rw[i] = Rn.add(null, Rn.times(null, alpha[i][j], Rw[i + 1]), Rn.times(null, 1.0 - alpha[i][j], Rw[i]));
//						System.out.println(" INNEN Rw["+i+"] " + Arrays.toString(Rw[i]));
					}
					for (int i = 0; i < Rw.length; i++) {
//						System.out.println("FERTIG Rw["+i+"] " + Arrays.toString(Rw[i]));
					}
					Qw[L][row] = Rw[0];
					Qw[k + r - j][row] = Rw[p - j];
				}
			
				int L = k - p + r;
				for (int i = L + 1; i < k; i++) {
					Qw[i][row] = Rw[i - L];
				}
				
			}
		}
		if(!dir){  //v direction
			/* Load the v-knot vector */
			for(int i = 0; i <= k; i++){
				VQ[i] = VP[i];
			}
			for(int i = 1; i <= r; i++){
				VQ[k + i] = uv; //uv is in U
			}
			for(int i = k + 1; i <= mp + q + 1; i++){
				VQ[i + r] = VP[i];
			}
			/* copy the v-knot vector into VQ */
			for (int i = 0; i < UP.length; i++) {
				UQ[i] = UP[i];
			}
			double[][] alpha = new double[q - s][r + 1];
			/* save the alphas */
			for(int j = 1; j <= r; j++){
				int L = k - q + j;
				for(int i = 0; i <= q - j - s; i++ ){
					alpha[i][j] = (uv - VP[L + i])/(VP[i + k + 1] - VP[L + i]);
				}
			}
			for(int column = 0; column <= np; column++){ /* for each column do */
				/* save unaltered control points */
				for(int j = 0; j <= k - q; j++){
					Qw[column][j] = Pw[column][j];
				}
				for(int j = k - s; j <= mp; j++){
					Qw[column][j + r] = Pw[column][j];
				}
				/* Load auxiliary  control points */
				double[][] Rw = new double[q + 1][];
				for(int j = 0; j <= q; j++){
					Rw[j] = Pw[column][k - q + j];
				}
				for(int j = 0; j <= k - q; j++){
					Qw[column][j] = Pw[column][j];
				}
				for(int j = 1; j <= r; j++){
					int L = k - q + j;
					for(int i = 0; i <= q - j - s; i++ ){
						Rw[i] = Rn.add(null, Rn.times(null, alpha[i][j], Rw[i + 1]), Rn.times(null, 1.0 - alpha[i][j], Rw[i]));
					}
					Qw[column][L] = Rw[0];
					Qw[column][r + k - j] = Rw[q - j];
				}
				int L = k - q + r;
				for (int i = L + 1; i < k - s; i++) {
					Qw[column][i] = Rw[i - L];
				}
			}
		}

	}
	
	
	
	private static int getMultiplicity(double knot, double[] knotVector){
		int counter = 0;
		for (int i = 0; i < knotVector.length; i++) {
			if(knot == knotVector[i]){
				counter++;
			}
		}
		return counter;
	}
	
	/**
	 * this is SurfaceKnotIns with smaller input
	 * @param UP
	 * @param VP
	 * @param Pw
	 * @param dir
	 * @param uv
	 * @param r
	 * @return
	 */
	public static NURBSSurface SurfaceKnotInsertion(double[] UP, double[] VP, double[][][]Pw, boolean dir, double uv, int r){
		int mult; // = s
		int k;
		int np = Pw.length - 1;
		int p = UP.length - np - 2;
		int mp = Pw[0].length - 1;
		int q = VP.length - mp - 2;
		int nq = np;
		int mq = mp;
		if(dir){
			mult = getMultiplicity(uv, UP);
			k = FindSpan(np, p, uv, UP);
			nq = np + r;
		}
		else{
			mult = getMultiplicity(uv, VP);
			k = FindSpan(mp, q, uv, VP);
			mq = mp + r;
		}
		double[] UQ = new double[nq + p + 2];
		double[] VQ = new double[mq + q + 2];
		double[][][]Qw = new double[nq + 1][mq + 1][4];
		SurfaceKnotIns(np, p, UP, mp, q, VP, Pw, dir, uv, k, mult, r, nq, UQ, mq, VQ, Qw);
		NURBSSurface ns = new NURBSSurface(UQ, VQ, Qw, p, q);
		return ns;
	}
	
	
	public static NURBSSurface decomposeSurface(NURBSSurface ns){
		double[]U = ns.getUKnotVector();
		double[]V = ns.getVKnotVector();
		double[][][]Pw = ns.getControlMesh();
		ArrayList<Double> newUKnots = new ArrayList<Double>();
		ArrayList<Integer> Umult = new ArrayList<Integer>();
		getAllNewKnots(U, newUKnots, Umult);
		ArrayList<Double> newVKnots = new ArrayList<Double>();
		ArrayList<Integer> Vmult = new ArrayList<Integer>();
		getAllNewKnots(V, newVKnots, Vmult);
		NURBSSurface nsReturn = new NURBSSurface();
		boolean dir = true;
		for (int i = 0; i < newUKnots.size(); i++) {
			nsReturn = SurfaceKnotInsertion(U, V, Pw, dir, newUKnots.get(i), Umult.get(i));
			U = nsReturn.getUKnotVector();
			V = nsReturn.getVKnotVector();
			Pw = nsReturn.getControlMesh();
		}
		dir = false;
		for (int i = 0; i < newVKnots.size(); i++) {
			nsReturn = SurfaceKnotInsertion(U, V, Pw, dir, newVKnots.get(i), Vmult.get(i));
			U = nsReturn.getUKnotVector();
			V = nsReturn.getVKnotVector();
			Pw = nsReturn.getControlMesh();
		}
		return nsReturn;
	}
	
	public static double[] getAllDifferentKnotsFromFilledKnotVector(double[] knotVector, int p){
		int knotSize = (knotVector.length - 2) / p;
		double[] knots = new double[knotSize];
		for (int i = 0; i < knots.length; i++) {
			knots[i] = knotVector[p * i + 1];
		}
		return knots;
	}
	
	public static NURBSSurface[][] decomposeIntoBezierSurfaces(NURBSSurface ns){
		NURBSSurface nsDecompose = decomposeSurface(ns);
//		System.out.println("HIER nsDecompose " + nsDecompose.toString());
		double[] U = nsDecompose.getUKnotVector();
		double[] V = nsDecompose.getVKnotVector();
		double[][][]Pw = nsDecompose.getControlMesh();
		int p = getDegreeFromClampedKnotVector(U);
		int q = getDegreeFromClampedKnotVector(V);
		double[] diffrentUknots = getAllDifferentKnotsFromFilledKnotVector(U, p);
		double[] diffrentVknots = getAllDifferentKnotsFromFilledKnotVector(V, q);
		NURBSSurface[][] BezierSurfaces = new NURBSSurface[diffrentUknots.length - 1][diffrentVknots.length - 1];
		for (int i = 0; i < BezierSurfaces.length; i++) {
			for (int j = 0; j < BezierSurfaces.length; j++) {
				BezierSurfaces[i][j] = new NURBSSurface();
				double[] UknotVector = new double[2 * p + 2];
				for (int k = 0; k < UknotVector.length; k++) {
					if(k < UknotVector.length / 2){
						UknotVector[k] = diffrentUknots[i];
					}
					else{
						UknotVector[k] = diffrentUknots[i + 1];
					}
				}
				double[] VknotVector = new double[2 * q + 2];
				for (int k = 0; k < VknotVector.length; k++) {
					if(k < VknotVector.length / 2){
						VknotVector[k] = diffrentVknots[j];
					}
					else{
						VknotVector[k] = diffrentVknots[j + 1];
					}
				}
				BezierSurfaces[i][j].setUKnotVector(UknotVector);
				BezierSurfaces[i][j].setVKnotVector(VknotVector);
				double[][][]BezierControlPoints = new double[UknotVector.length - p - 1][VknotVector.length - q - 1][4];
				for (int iB = 0; iB < BezierControlPoints.length; iB++) {
					for (int jB = 0; jB < BezierControlPoints.length; jB++) {
						BezierControlPoints[iB][jB] = Pw[p * i + iB][q * j + jB];
					}
				}
				BezierSurfaces[i][j].setControlMesh(BezierControlPoints);
			}
		}
		return BezierSurfaces;
	}
	
	

//		
//	}
	
	private static int getDegreeFromClampedKnotVector(double[] knotVector){
		int count = 0;
		double before = knotVector[0];
		for (int i = 1; i < knotVector.length; i++) {
			if(before == knotVector[i]){
				count++;
				before = knotVector[i];
			}
			else{
				break;
			}
		}
		return count;
	}
	
	private static void getAllNewKnots(double[] knotVector,ArrayList<Double> newKnots, ArrayList<Integer> multiplicity){
		int p = getDegreeFromClampedKnotVector(knotVector);
//		System.out.println("p " + p);
		double before = knotVector[p];
		int count = 1;
		for (int i = p + 1; i < knotVector.length - p ; i++) {
			if(before == knotVector[i]){
//				System.out.println("equal before " + before + " knotVector["+i+"]" + knotVector[i]);
				count++;
			}
			else{
//				System.out.println("not equal before " + before + " knotVector["+i+"]" + knotVector[i]);
				if(count != p + 1 && count != p && knotVector[p] != knotVector[i - 1]){
					newKnots.add(knotVector[i - 1]);
					multiplicity.add(p - count);
					count = 1;
				}
				else{
					count = 1;
				}
			}
			before = knotVector[i];
		}
	
	}
	

	
//	/**
//	 * Algorithm A5.7 from the NURBS Book
//	 * 
//	 * @param n = length of control mesh in u direction
//	 * @param p = degree of U
//	 * @param U 
//	 * @param m = length of control mesh in v direction
//	 * @param q = degree of V
//	 * @param V
//	 * @param Pw = control mesh
//	 * @param dir 
//	 * @param nb
//	 * @param Qw
//	 */
//	
//	public static void DecomposeSurface(int p, double[] U,int q, double [] V,double[][]Pw, boolean dir, int nb, double[][][]Qw){
//		int n = Pw.length - 1;
//		int m = Pw[0].length - 1;
//		if(dir == true){
////			int a = p; 
//			int b = p + 1;
//			nb = 0;
//			for(int i = 0; i <= p; i++){
//				for(int row = 0; row <= m; row++){
//					Qw[nb][i][row] = Pw[i][row];
//				}
//			}
//			while (b < m){
//				int mult = 0; //get mult;
//				if(mult < p){
//					// get the numerator of the alphas
//					for(int j = 1; j <= p - mult; j++){
//						int save = 0;// save = ...
//						int s = mult + j;//s = ...
//						for(int k = p; k >= s; k--){
//							double alpha = 0.0; // get alpha
//							for(int row = 0; row <= m; row++){
//								Qw[nb][k][row] = alpha * Qw[nb][k][row] + (1.0 - alpha) * Qw[nb][k -1][row];
//							}
//						}
//						if(b < m){
//							for(int row = 0; row <= m; row++){
//								Qw[nb + 1][save][row] = Qw[nb][p][row];
//							}
//						}
//					}
//				}
//				nb = nb + 1;
//				if( b < m){
//					for(int i = p - mult; i <= p; i++){
//						for(int row = 0; row <= m; row++){
//							Qw[nb][i][row] = Pw[b - p + i][row];
//						}
//					}
////					a = b;
//					b = b + 1;
//				}
//			}
//		}
//	}
	
	public static void main(String[] agrs){
		


//		double[] Utest = {0,0,0,0,1,1,1,2,2,2,3,3,3,4,4,4,5,5,5,5};
//		double[] knots = getAllDifferentKnotsFromFilledKnotVector(Utest, 3);
//		System.out.println("KNOTS "+Arrays.toString(knots));
//		System.out.println("Utest " + Arrays.toString(Utest));
//		ArrayList<Double> newKnots = new ArrayList<Double>();
//		ArrayList<Integer> multiplicity = new ArrayList<Integer>();
//		getAllNewKnots(Utest, newKnots, multiplicity);
//		System.out.println("newKnots "+ newKnots.toString());
//		System.out.println("mult "+ multiplicity.toString());
		
		 /**
		  * initialize first surface
		  */
		
		double[] uv = {0.1234, 0.1234};
//		double[] uInsertion = {0.11, 0.22, 0.33, 0.44, 0.55, 0.66, 0.77, 0.88, 0.99};
//		double[] vInsertion = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		double[] uInsertion = {0.11, 0.22};
		double[] vInsertion = {0.8, 0.9};
		double[]U = {0.0,0.0,0.0,0.0,1.0,1.0,1.0,1.0};
		double[] V = {0.0,0.0,0.0,0.0,1.0,1.0,1.0,1.0};
		int p = 3;
		int q = 3;
		double[][][]Pw0 = {{{-16.84591428200644, 9.165210069137606, 0.0, 1.0}, {-15.36140842573768, 3.743536507112545, -5.35712982914381, 1.0}, {-14.52233989828141, -1.484505856268766, 3.743536507112545, 1.0}, {-18.26587640539396, -5.550761027787562, 0.0, 1.0}}, 
						{{-8.84249140473135, 12.39239671320014, 10.06882232947512, 1.0}, {-6.454373288125074, 4.711692500331306, 6.045596313210488, 1.0}, {-4.066255171518796, -1.871768253556271, 4.582605034568804, 1.0}, {-8.455229007443846, -8.197054075918842, 0.0, 1.0}}, 
						{{6.389829555243823, 12.39239671320014, 0.0, 1.0}, {10.56365761489803, 4.973453194794152, 1.742680787793771, 1.0}, {10.28396810574596, -2.78972356564517, -5.851965114566728, 1.0}, {7.938879144393841, -8.00342287727509, -6.841635685412578, 1.0}}, 
						{{17.1686329464127, 7.938879144393843, 0.0, 1.0}, {18.65313880268147, 0.839068527456261, 8.713403938968852, 1.0}, {18.65313880268147, -1.419962123387515, -3.808080239993792, 1.0}, {17.62043907658145, -7.164354349818831, 0.0, 1.0}}};

		NURBSSurface ns0 = new NURBSSurface(U, V, Pw0, p, q);
		double[] s0 = ns0.getSurfacePoint(uv[0], uv[1]);
		System.out.println("s0 " + Arrays.toString(s0));
		NURBSSurface ns1 = new NURBSSurface(U, V, Pw0, p, q);
		
		/**
		 * insert uInsertion into surface
		 */
		
		for (int i = 0; i < uInsertion.length; i++) {
			ns1 = ns1.SurfaceKnotInsertion(true, uInsertion[i], 1);

		}
		for (int i = 0; i < vInsertion.length; i++) {
			ns1 = ns1.SurfaceKnotInsertion(false, vInsertion[i], 1);
		}
		
		double[] s1 = ns1.getSurfacePoint(uv[0], uv[1]);
		System.out.println("s1 " + Arrays.toString(s1));

		NURBSSurface decomposed = ns1.decomposeSurface();
		System.out.println("decomposed " + decomposed.toString());

		double[] sDecomposed = decomposed.getSurfacePoint(uv[0], uv[1]);
		System.out.println("sDecomdosed " + Arrays.toString(sDecomposed));
		
		NURBSSurface[][] BezierSurfaces = ns1.decomposeIntoBezierSurfaces();
		NURBSSurface Bezier = new NURBSSurface();
		for (int i = 0; i < BezierSurfaces.length; i++) {
			for (int j = 0; j < BezierSurfaces[0].length; j++) {
//				System.out.println("DDDDDAAAAA");
//				System.out.println(BezierSurfaces[i][j]);
				if(BezierSurfaces[i][j].getUKnotVector()[0] <= uv[0] && BezierSurfaces[i][j].getUKnotVector()[BezierSurfaces[i][j].getUKnotVector().length - 1] >= uv[0] &&
						BezierSurfaces[i][j].getVKnotVector()[0] <= uv[1] && BezierSurfaces[i][j].getVKnotVector()[BezierSurfaces[i][j].getVKnotVector().length - 1] >= uv[1]){
					Bezier.setUKnotVector(BezierSurfaces[i][j].getUKnotVector());
					Bezier.setVKnotVector(BezierSurfaces[i][j].getVKnotVector());
					Bezier.setControlMesh(BezierSurfaces[i][j].getControlMesh());
					Bezier.setUDegree(p);
					Bezier.setVDegree(q);
				}
			}
		}
		System.out.println("BEZIER");
		System.out.println(Bezier.toString());
		double[] BezierPoint = Bezier.getSurfacePoint(uv[0], uv[1]);
//		double[] BezierPoint = new double[3];
//		SurfacePoint(p, Bezier.getUKnotVector(), q, Bezier.getVKnotVector(), Bezier.getControlMesh(), uv[0], uv[1], BezierPoint);
		System.out.println("BezierPoint " + Arrays.toString(BezierPoint));

	}
	

}
