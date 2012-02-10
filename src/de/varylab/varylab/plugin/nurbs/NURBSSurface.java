package de.varylab.varylab.plugin.nurbs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import de.varylab.varylab.plugin.nurbs.math.NURBSAlgorithm;

	public class NURBSSurface {

		protected double[] U;
		protected double[] V;
		protected LinkedList<NURBSTrimLoop> trimC = new LinkedList<NURBSTrimLoop>();
		protected LinkedList<NURBSTrimLoop> holeC = new LinkedList<NURBSTrimLoop>();
		protected double[][][] controlMesh;
		protected int p, q;
		private String name = "Nurbs Surface";

		public NURBSSurface() {
			
		}
		
		public NURBSSurface(double[] UVec, double[] VVec, double[][][] cm, int pDegree, int qDegree){
			U = UVec;
			V = VVec;
			controlMesh = cm;
			p = pDegree;
			q = qDegree;
		}

		public void getSurfacePoint(double u, double v, double[] S) {
			NURBSAlgorithm.SurfacePoint(p, U, q, V, controlMesh, u, v, S);
		}

		public double[][][] getControlMesh() {
			return controlMesh;
		}

		public LinkedList<NURBSTrimLoop> getTrimCurves() {
			return trimC;
		}

		public LinkedList<NURBSTrimLoop> getHoleCurves() {
			return holeC;
		}

		public double[] getUKnotVector() {
			return U;
		}

		public double[] getVKnotVector() {
			return V;
		}

		public int getUDegree() {
			return p;
		}

		public int getVDegree() {
			return q;
		}

		public void setTrimCurves(LinkedList<NURBSTrimLoop> tc) {
			trimC = tc;
		}

		public void setHoleCurves(LinkedList<NURBSTrimLoop> hc) {
			holeC = hc;
		}

		public void setUKnotVector(double[] u) {
			U = u;
		}

		public void setVKnotVector(double[] v) {
			V = v;
		}

		public void setUDegree(int p) {
			this.p = p;
		}

		public void setVDegree(int q) {
			this.q = q;
		}

		/**
		 * Use homogeneous coordinates. The fourth coordinates will be the weights
		 * of the control points
		 * 
		 * @param cm
		 */
		public void setControlMesh(double[][][] cm) {
			this.controlMesh = cm;
		}

		public void setSurfaceData(double[][][] cm, double[] U, double[] V, int p,
				int q) {
			this.controlMesh = cm;
			this.U = U;
			this.V = V;
			this.p = p;
			this.q = q;
		}

		public void setDefaultKnots() {
			int n = controlMesh.length;
			int m = controlMesh[0].length;
			U = new double[p + n +1];
			V = new double[q + 1 +m];
			for (int i = 0; i < (p + 1); i++) {
				U[i] = 0.0;
				U[U.length - i - 1] = 1.0;
			}
			for (int i = 0; i < n - p - 1; i++) {
				U[p + 1 + i] = (i + 1) / (double) (n - p);
			}
			for (int i = 0; i < (q + 1); i++) {
				V[i] = 0.0;
				V[V.length - i - 1] = 1.0;
			}
			for (int i = 0; i < m - q - 1; i++) {
				V[q + 1 + i] = (i + 1) / (double) (m - q);
			}
		}

		public int getNumUPoints() {
			return controlMesh.length;
		}

		public int getNumVPoints() {
			return controlMesh[0].length;
		}

		public double[][] getWeights() {
			double[][] weights = new double[getNumUPoints()][getNumVPoints()];

			for (int i = 0; i < weights.length; i++) {
				for (int j = 0; j < weights[0].length; j++) {
					weights[i][j] = controlMesh[i][j][3];
				}
			}
			return weights;
		}

		public void setName(String aValue) {
			name  = aValue;
		}

		public String getName() {
			return name;
		}
		
		public String toString() {
			String str = new String();
			str = str + "NURBSSurface" + '\n' + "U knot vector" + '\n';
			for (int i = 0; i < U.length; i++) {
				str = str + U[i] + ", ";
			}
			str = str + '\n' + "V knot vector" + '\n';
			for (int i = 0; i < V.length; i++) {
				str = str + V[i] + ", ";
			}
			str = str + '\n' + "control mesh";
			for (int i = 0; i < controlMesh.length; i++) {
				str = str + '\n';
				for (int j = 0; j < controlMesh[0].length; j++) {
					str = str + Arrays.toString(controlMesh[i][j]) + " ";
				}
			}
			return str;
		}
		
		
		public double[] getSurfacePoint( double u, double v) {
			double[] S = new double[3];
			NURBSAlgorithm.SurfacePoint(p, U, q, V, controlMesh, u, v, S);
			return S;
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
			double before = knotVector[p];
			int count = 1;
			for (int i = p + 1; i < knotVector.length - p ; i++) {
				if(before == knotVector[i]){
					count++;
				}
				else{
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
		
		private static double[] getAllDifferentKnotsFromFilledKnotVector(double[] knotVector, int p){
			int knotSize = (knotVector.length - 2) / p;
			double[] knots = new double[knotSize];
			for (int i = 0; i < knots.length; i++) {
				knots[i] = knotVector[p * i + 1];
			}
			return knots;
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
//		public static NURBSSurface SurfaceKnotInsertion(double[] UP, double[] VP, double[][][]Pw, boolean dir, double uv, int r){
//			int mult; // = s
//			int k;
//			int np = Pw.length - 1;
//			int p = UP.length - np - 2;
//			int mp = Pw[0].length - 1;
//			int q = VP.length - mp - 2;
//			int nq = np;
//			int mq = mp;
//			if(dir){
//				mult = getMultiplicity(uv, UP);
//				k = NURBSAlgorithm.FindSpan(np, p, uv, UP);
//				nq = np + r;
//			}
//			else{
//				mult = getMultiplicity(uv, VP);
//				k = NURBSAlgorithm.FindSpan(mp, q, uv, VP);
//				mq = mp + r;
//			}
//			double[] UQ = new double[nq + p + 2];
//			double[] VQ = new double[mq + q + 2];
//			double[][][]Qw = new double[nq + 1][mq + 1][4];
//			NURBSAlgorithm.SurfaceKnotIns(np, p, UP, mp, q, VP, Pw, dir, uv, k, mult, r, nq, UQ, mq, VQ, Qw);
//			NURBSSurface ns = new NURBSSurface(UQ, VQ, Qw, p, q);
//			return ns;
//		}
		
		public NURBSSurface SurfaceKnotInsertion(boolean dir, double uv, int r){
			double[] UP = this.getUKnotVector();
			double[] VP = this.getVKnotVector();
			double[][][]Pw = this.getControlMesh();
			
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
				k = NURBSAlgorithm.FindSpan(np, p, uv, UP);
				nq = np + r;
			}
			else{
				mult = getMultiplicity(uv, VP);
				k = NURBSAlgorithm.FindSpan(mp, q, uv, VP);
				mq = mp + r;
			}
			double[] UQ = new double[nq + p + 2];
			double[] VQ = new double[mq + q + 2];
			double[][][]Qw = new double[nq + 1][mq + 1][4];
			NURBSAlgorithm.SurfaceKnotIns(np, p, UP, mp, q, VP, Pw, dir, uv, k, mult, r, nq, UQ, mq, VQ, Qw);
			NURBSSurface ns = new NURBSSurface(UQ, VQ, Qw, p, q);
			return ns;
			
		}
		
		
		
		
		public NURBSSurface decomposeSurface(){
			ArrayList<Double> newUKnots = new ArrayList<Double>();
			ArrayList<Integer> Umult = new ArrayList<Integer>();
			getAllNewKnots(U, newUKnots, Umult);
			ArrayList<Double> newVKnots = new ArrayList<Double>();
			ArrayList<Integer> Vmult = new ArrayList<Integer>();
			getAllNewKnots(V, newVKnots, Vmult);
			NURBSSurface nsReturn = new NURBSSurface(U, V, controlMesh, p, q);
			boolean dir = true;
			for (int i = 0; i < newUKnots.size(); i++) {
				NURBSSurface ns = SurfaceKnotInsertion(dir, newUKnots.get(i), Umult.get(i));
				nsReturn.setUKnotVector(ns.getUKnotVector());
				nsReturn.setVKnotVector(ns.getVKnotVector());
				nsReturn.setControlMesh(ns.getControlMesh());
				System.out.println();
				System.out.println("nsRETURN " + nsReturn.toString());
				System.out.println();
			}
			dir = false;
			for (int i = 0; i < newVKnots.size(); i++) {
				NURBSSurface ns = SurfaceKnotInsertion(dir, newVKnots.get(i), Vmult.get(i));
				nsReturn.setUKnotVector(ns.getUKnotVector());
				nsReturn.setVKnotVector(ns.getVKnotVector());
				nsReturn.setControlMesh(ns.getControlMesh());
				System.out.println();
				System.out.println("nsRETURN " + nsReturn.toString());
				System.out.println();
			}
			return nsReturn;
		}
		
		
	
		
		public NURBSSurface[][] decomposeIntoBezierSurfaces(){
			NURBSSurface nsDecompose = decomposeSurface();
			System.out.println("nsDecompose in NURBSSURFACE");
			System.out.println(nsDecompose.toString());
			nsDecompose = NURBSAlgorithm.decomposeSurface(this);
			System.out.println("nsDecompose in NURBSAlgorithm");
			System.out.println(nsDecompose.toString());
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
	
}
