package de.varylab.varylab.plugin.nurbs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;
import de.varylab.varylab.plugin.nurbs.data.NURBSTree;
import de.varylab.varylab.plugin.nurbs.data.NURBSTreeNode;
import de.varylab.varylab.plugin.nurbs.math.NURBSAlgorithm;
import de.varylab.varylab.plugin.nurbs.math.NURBSCurvatureUtility;



	public class NURBSSurface {
//		private double time = 0.0;
		private double[] U;
		private double[] V;
		private LinkedList<NURBSTrimLoop> trimC = new LinkedList<NURBSTrimLoop>();
		private LinkedList<NURBSTrimLoop> holeC = new LinkedList<NURBSTrimLoop>();
		private double[][][] controlMesh;
		private int p, q;
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
		
		@Override
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
			str = str + '\n' + "p " + p;
			str = str + '\n' + "q " + q;
			str = str + '\n' + "control mesh";
			for (int i = 0; i < controlMesh.length; i++) {
				str = str + '\n';
				for (int j = 0; j < controlMesh[0].length; j++) {
					str = str + Arrays.toString(controlMesh[i][j]) + " ";
				}
			}
			return str;
		}
		
		
		/**
		 * 
		 * @param u
		 * @param v
		 * @return the point in R^3
		 */
		public double[] getSurfacePoint( double u, double v) {
			double[] S = new double[4];
			NURBSAlgorithm.SurfacePoint(p, U, q, V, controlMesh, u, v, S);
			return S;
		}
		
		
		/**
		 * 
		 * @param knot
		 * @param knotVector
		 * @return multiplicity of a given knot in the knot vector
		 */
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
		 * 
		 * @param knotVector
		 * @return degree
		 */
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
		
		/**
		 * This method stores all interior knots, which have not max. multiplicity
		 * (= degree), in the List newKnots and computes the remaining
		 * multiplicity, such that after knot insertion this knot has max.
		 * multiplicity and writes this value into the List multiplicity.<br/>
		 * <strong>Example</strong><br/>
		 * U = {0,0,0,0,1,2,2,3,3,3,3} hence<br/>
		 * newKnots = {1,2}<br/>
		 * multiplicity = {2,1}<br/>
		 * i.e. insert 1 twice and 2 once
		 * @param knotVector 
		 * @param newKnots (empty)
		 * @param multiplicity (empty)
		 * @return newKnots, multiplicity
		 */
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
		
		/**
		 * 
		 * @param knotVector
		 * @param p
		 * @return the set of the multiset filled knot vector 
		 */
		private static double[] getAllDifferentKnotsFromFilledKnotVector(double[] knotVector, int p){
			int knotSize = (knotVector.length - 2) / p;
			double[] knots = new double[knotSize];
			for (int i = 0; i < knots.length; i++) {
				knots[i] = knotVector[p * i + 1];
			}
			return knots;
		}
		
		/**
		 * insert the knot uv r times into a knot vector of this surface. If dir == true into
		 *  U else into V
		 * @param dir
		 * @param uv
		 * @param r
		 * @return
		 */
		
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
		
		/**
		 * insert the knot uv r times into a knot vector of surface ns. If dir == true into
		 *  U else into V
		 * @param dir
		 * @param uv
		 * @param r
		 * @return
		 */
		
		public NURBSSurface SurfaceKnotInsertion(NURBSSurface ns, boolean dir, double uv, int r){
			double[] UP = ns.getUKnotVector();
			double[] VP = ns.getVKnotVector();
			double[][][]Pw = ns.getControlMesh();
			
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
			NURBSSurface nsReturn = new NURBSSurface(UQ, VQ, Qw, p, q);
			return nsReturn;
		}
		
		
		/**
		 * decomposes both knot vectors of this surface s.d. both are filled<br/>
		  * <strong>Example</strong><br/>
		  * Uold = {000012234444}<br/>
		  * Unew = {00001112223334444}
		  * 
		 * 
		 * @return decomposed surface
		 */
		
		public NURBSSurface decomposeSurface(){
			double[]U = this.getUKnotVector();
			double[]V = this.getVKnotVector();
			double[][][]Pw = this.getControlMesh();
			ArrayList<Double> newUKnots = new ArrayList<Double>();
			ArrayList<Integer> Umult = new ArrayList<Integer>();
			getAllNewKnots(U, newUKnots, Umult);
			ArrayList<Double> newVKnots = new ArrayList<Double>();
			ArrayList<Integer> Vmult = new ArrayList<Integer>();
			getAllNewKnots(V, newVKnots, Vmult);
			NURBSSurface nsReturn = new NURBSSurface(U, V, Pw, p, q);
			boolean dir = true;
			for (int i = 0; i < newUKnots.size(); i++) {
				nsReturn = SurfaceKnotInsertion(nsReturn, dir, newUKnots.get(i), Umult.get(i));
			}
			dir = false;
			for (int i = 0; i < newVKnots.size(); i++) {
				nsReturn = SurfaceKnotInsertion(nsReturn, dir, newVKnots.get(i), Vmult.get(i));
			}
			return nsReturn;
		}
		
		
		/**
		 * computes all Bezier patches from this surface
		 * @return Bezier patches
		 */
		
		public NURBSSurface[][] decomposeIntoBezierSurfaces(){
			NURBSSurface nsDecompose = decomposeSurface();
			double[] U = nsDecompose.getUKnotVector();
			double[] V = nsDecompose.getVKnotVector();
			double[][][]Pw = nsDecompose.getControlMesh();
			int p = getDegreeFromClampedKnotVector(U);
			int q = getDegreeFromClampedKnotVector(V);
			double[] differentUknots = getAllDifferentKnotsFromFilledKnotVector(U, p);
//			System.out.println("differentUknots " + Arrays.toString(differentUknots));
			double[] differentVknots = getAllDifferentKnotsFromFilledKnotVector(V, q);
//			System.out.println("differentVknots " + Arrays.toString(differentVknots));
			NURBSSurface[][] BezierSurfaces = new NURBSSurface[differentUknots.length - 1][differentVknots.length - 1];
			for (int i = 0; i < BezierSurfaces.length; i++) {
				for (int j = 0; j < BezierSurfaces[0].length; j++) {
					BezierSurfaces[i][j] = new NURBSSurface();
					double[] UknotVector = new double[2 * p + 2];
					for (int k = 0; k < UknotVector.length; k++) {
						if(k < UknotVector.length / 2){
							UknotVector[k] = differentUknots[i];
						}
						else{
							UknotVector[k] = differentUknots[i + 1];
						}
					}
					double[] VknotVector = new double[2 * q + 2];
					for (int k = 0; k < VknotVector.length; k++) {
						if(k < VknotVector.length / 2){
							VknotVector[k] = differentVknots[j];
						}
						else{
							VknotVector[k] = differentVknots[j + 1];
						}
					}
					double[][][]BezierControlPoints = new double[UknotVector.length - p - 1][VknotVector.length - q - 1][4];
					for (int iB = 0; iB < BezierControlPoints.length; iB++) {
						for (int jB = 0; jB < BezierControlPoints[0].length; jB++) {
							BezierControlPoints[iB][jB] = Pw[p * i + iB][q * j + jB];
						}
					}
					BezierSurfaces[i][j].setUKnotVector(UknotVector);
					BezierSurfaces[i][j].setVKnotVector(VknotVector);
					BezierSurfaces[i][j].setControlMesh(BezierControlPoints);
					BezierSurfaces[i][j].setUDegree(p);
					BezierSurfaces[i][j].setVDegree(q);
				}
			}
			return BezierSurfaces;
		}
		
		public LinkedList<NURBSSurface> decomposeIntoBezierSurfacesList(){
			LinkedList<NURBSSurface> surfaceList = new LinkedList<NURBSSurface>();
			NURBSSurface[][] Bezier = decomposeIntoBezierSurfaces();
			for (int i = 0; i < Bezier.length; i++) {
				for (int j = 0; j < Bezier[0].length; j++) {
					surfaceList.add(Bezier[i][j]);
				}
			}
			return surfaceList;
		}
		
		
		/**
		 * 
		 * Distance Calculation Between a Point
		 *and a NURBS Surface
		 *Eva Dyllong and Wolfram Luther
		 *
		 */
		
//		private static double max(double[] array){
//			double max = Double.MIN_VALUE;
//			for (int i = 0; i < array.length; i++) {
//				if(array[i] > max){
//					max = array[i];
//				}
//			}
//			return max;
//		}
		
//		private static double phi(double[]w){
//			int p = w.length; 
//			double[] inverseW = new double[p];
//			double[] smallW = new double[p - 2];
//			for (int i = 0; i < p; i++) {
//				inverseW[i] = 1 / w[i];
//				if(i != 0 && i != p - 1){
//					smallW[i - 1] = w[i];
//				}
//			}
//			double maxW = max(smallW);
//			double maxInverseW = max(inverseW);
//			return 1 - 1 / (1 + maxInverseW * maxW * (Math.pow(2, p - 1) - 1));
//		}
		
		public static double[] get3DPoint(double[] fourDPoint){
			double[] threeDPoint = new double[3];
			threeDPoint[0] = fourDPoint[0] / fourDPoint[3];
			threeDPoint[1] = fourDPoint[1] / fourDPoint[3];
			threeDPoint[2] = fourDPoint[2] / fourDPoint[3];
			return threeDPoint;
		}
		
		private double[][][] get3DControlmesh(){
			double[][][] threeDControlmesh = new double[controlMesh.length][controlMesh[0].length][3];
			for (int i = 0; i < controlMesh.length; i++) {
				for (int j = 0; j < controlMesh[0].length; j++) {
					threeDControlmesh[i][j] = get3DPoint(controlMesh[i][j]);
				}
			}
			return threeDControlmesh;
		}
		
	
		
		private static double[] projectOntoLine(double[] pos, double[] v, double[] w) {
			double[] l = Rn.subtract(null, w, v); // w - v
			double[] np = Rn.subtract(null, pos, v); // pos - v
			Rn.projectOnto(np, np, l);
			Rn.add(np, np, v);
			return np;
		}
		
		private static boolean pointLiesOnSegment(double[] point, double[] v, double[] w){
//			System.out.println("pointLiesOnSegment");
//			System.out.println("point " + Arrays.toString(point));
//			System.out.println("v " + Arrays.toString(v));
//			System.out.println("w " + Arrays.toString(w));
			if((point[0] < v[0] && point[0] < w[0]) || (point[0] > v[0] && point[0] > w[0])){
				System.out.println("1. wrong");
				return false;
			}
			else if((point[1] < v[1] && point[1] < w[1]) || (point[1] > v[1] && point[1] > w[1])){
				System.out.println("2. wrong");
				return false;
			}
			else if((point[2] < v[2] && point[2] < w[2]) || (point[2] > v[2] && point[2] > w[2])){
				System.out.println("3. wrong");
				return false;
			}
			else{
				return true;
			}
		}
		
		private static double[] projectOnto(double[] pos, double[] v1, double[] v2, double[] v3) {
			double[] fn = Rn.crossProduct(null, Rn.subtract(null, v2, v1), Rn.subtract(null, v3, v1));
			double[] proj = Rn.subtract(null, pos, v1);
			Rn.projectOntoComplement(proj, proj, fn);
			return Rn.add(null,proj,v1);
		}
		
		
		
		/**
		 * 
		 * Algorithms from PAPER:
		 * "Point inversion and projection for NURBS curve:
		 * 			Control polygon approach"
		 *  by YingLiang Ma and W T Hewitt
		 * @return is valid
		 */
		
		
		/**
		 * Algorithm 1
		 * @param Polygon
		 * @return
		 */
//		private static boolean isValidPolygon(double[][] Polygon){
//			int n = Polygon.length - 1;
//			double R = 0;
//			System.out.println("isValidPolygon");
//			System.out.println("our polygon");
//			for (int i = 0; i < Polygon.length; i++) {
//				System.out.println(Arrays.toString(Polygon[i]));
//			}
//			for (int i = 1; i < n; i++) {
////				System.out.println("n " + n);
////				System.out.println("Polygon["+ (i + 1) +"] " + Arrays.toString(Polygon[i + 1]));
////				System.out.println("Line: " + Arrays.toString(Polygon[i - 1]) + " " + Arrays.toString(Polygon[i + 1]));
//				double[] V1 = projectOntoLine(Polygon[i], Polygon[i - 1], Polygon[i + 1]);
//				System.out.println("Projection onto neighbourpoints" + Arrays.toString(V1));
//				double[] V2 = projectOntoLine(Polygon[i], Polygon[0], Polygon[n]);
//				System.out.println("Projection onto endpoints" + Arrays.toString(V2));
//				boolean validProjection = pointLiesOnSegment(V2, Polygon[0], Polygon[n]);
//				if(!validProjection){
//					System.out.println("!validProjection");
//					return false;
//				}
//				double[] V1Pi = Rn.subtract(null, Polygon[i], V1);
//			
//				if(i < (n / 2)){
//					double[] V1Pn = Rn.subtract(null, Polygon[n], V1);
//					R = Rn.innerProduct(V1Pi, V1Pn);
//				}
//				else{
//					double[] V1P0 = Rn.subtract(null, Polygon[0], V1);
//					R = Rn.innerProduct(V1Pi, V1P0);
//				}
//				if(R > 0){
//					System.out.println("!valid");
//					return false;
//				}
//			}
//			return true;
//		}
		 
		/**
		 * Algorithm 2
		 * @return
		 */
		
		
//		public boolean hasValidControlmesh(){
//			System.out.println("hasValidControlmesh()");
//			double[][][] threeDControlMesh = get3DControlmesh();
//			System.out.println("in U direction");
//			for (int i = 0; i < threeDControlMesh.length; i++) {
//				double[][]Polygon = new double[threeDControlMesh[0].length][];
//				System.out.println("polygon");
//				for (int j = 0; j < threeDControlMesh[i].length; j++) {
//					Polygon[j] = threeDControlMesh[i][j];
//					System.out.println(Arrays.toString(Polygon[j]));
//				}
//				if(!isValidPolygon(Polygon)){
//					System.out.println("U false");
//					return false;
//				}
//			}
//			System.out.println("in V direction");
//			for (int j = 0; j < threeDControlMesh[0].length; j++) {
//				double[][]Polygon = new double[threeDControlMesh.length][];
//				System.out.println("polygon");
//				for (int i = 0; i < threeDControlMesh.length; i++) {
//					Polygon[i] = threeDControlMesh[i][j];
//					System.out.println(Arrays.toString(Polygon[i]));
//				}
//				if(!isValidPolygon(Polygon)){
//					System.out.println("V false");
//					return false;
//				}
//			}
//			return true;
//		}
		
		
		/**
		 * Algorithm 3
		 * @param Polygon
		 * @param P
		 * @return
		 */
//		private boolean pointNearestBezierCurve(double[][] Polygon, double[] P){
//			int n  = Polygon.length - 1;
//			double[] P0P = Rn.subtract(null, Polygon[0], P);
//			double[] P0P1 = Rn.subtract(null, Polygon[0], Polygon[0]);
//			double[] PnP = Rn.subtract(null, Polygon[n], P);
//			double[] PnPn_1 = Rn.subtract(null, Polygon[n - 1], Polygon[n]);
//			double[] PnP0 = Rn.subtract(null, Polygon[0], Polygon[n]);
//			double R1 = Rn.innerProduct(P0P, P0P1);
//			double R2 = Rn.innerProduct(PnP, PnPn_1);
//			double R3 = Rn.innerProduct(PnP0, PnP);
//			double R4 = Rn.innerProduct(PnP0, P0P);
//			if(R1 < 0 ||  R2 < 0 && R3 * R4 > 0){
//				return false;
//			}
//			else{
//				return true;
//			}
//		}
		
		
		/**
		 * Algoritm 4
		 * @param P
		 * @return
		 */
//		private boolean pointNearestBezierPatch(double[] P){
//			boolean Flag = false;
//			for (int i = 0; i < controlMesh.length; i++) {
//				double[][]Polygon = new double[controlMesh[i].length][];
//				for (int j = 0; j < controlMesh[i].length; j++) {
//					Polygon[j] = controlMesh[i][j];
//				}
//				if(pointNearestBezierCurve(Polygon, P)){
//					Flag = true;
//					break;
//				}
//			}
//			if(!Flag){
//				return false;// the nearest point is the point on the boundary curve
//			}
//			Flag = false;
//			for (int j = 0; j < controlMesh[0].length; j++) {
//				double[][]Polygon = new double[controlMesh.length][];
//				for (int i = 0; i < controlMesh.length; i++) {
//					Polygon[j] = controlMesh[i][j];
//				}
//				if(pointNearestBezierCurve(Polygon, P)){
//					Flag = true;
//					break;
//				}
//			}
//			if(!Flag){
//				return false;// the nearest point is the point on the boundary curve
//			}
//			return true;
//		}
		
 		protected boolean isFlatEnough(double eps){
			double[] v1 = controlMesh[0][0];
			double[] v2 = controlMesh[0][controlMesh[0].length - 1];
			double[] v3 = controlMesh[controlMesh.length - 1][0];
			for (int i = 0; i < controlMesh.length; i++) {
				for (int j = 0; j < controlMesh[0].length; j++) {
					double[] proj = projectOnto(controlMesh[i][j], v1, v2, v3);
					double dist = Rn.euclideanDistance(proj, controlMesh[i][j]);
					if(dist > eps){
						return false;
					}
				}
			}
			return true;
		}
		
 		
 		public LinkedList<NURBSSurface> subdivideIntoFourNewPatches(){
 			LinkedList<NURBSSurface> newPatches = new LinkedList<NURBSSurface>();
 			double uInsert = (U[U.length - 1] + U[0]) / 2.0;
 			double vInsert = (V[V.length - 1] + V[0]) / 2.0;
 			NURBSSurface nsInsert = SurfaceKnotInsertion(true, uInsert, 1);
 			nsInsert = SurfaceKnotInsertion(nsInsert, false, vInsert, 1);
 			newPatches = nsInsert.decomposeIntoBezierSurfacesList();
 			return newPatches;
 		}
 		
 		public NURBSSurface[] subdivideIntoFourNewPatchestoArray(){
 			NURBSSurface[] newPatches = new NURBSSurface[4];
 			LinkedList<NURBSSurface> list = subdivideIntoFourNewPatches();
 			for (int i = 0; i < newPatches.length; i++) {
				newPatches[i] = list.get(i);
			}
 			return newPatches;
 		}
 		
// 		private static int binomialCoefficient(int n, int k) {
// 			if (n - k == 1 || k == 1)
// 				return n;
//
// 			long[][] b = new long[n + 1][n - k + 1];
// 			b[0][0] = 1;
// 			for (int i = 1; i < b.length; i++) {
// 				for (int j = 0; j < b[i].length; j++) {
// 					if (i == j || j == 0)
// 						b[i][j] = 1;
// 					else if (j == 1 || i - j == 1)
// 						b[i][j] = i;
// 					else
// 						b[i][j] = b[i - 1][j - 1] + b[i - 1][j];
// 				}
// 			}
// 			return (int)b[n][n - k];
// 		}

 		
 
 		
//		public LinkedList<NURBSSurface> subdivideBezierIntoTwoHalfPatches(boolean uDirection){
// 			LinkedList<NURBSSurface> halfPatches = new LinkedList<NURBSSurface>();
// 			double[][][] cm = getControlMesh();
// 			double[][][] firstCm = new double[cm.length][cm[0].length][4];
// 			double[][][] secondCm = new double[cm.length][cm[0].length][4];
// 			NURBSSurface firstNs = new NURBSSurface();
// 			NURBSSurface secondNs = new NURBSSurface();
// 			if(uDirection){
// 				double[][][]newCm = new double[2 * p + 1][cm[0].length][4];
// 				if(p == 1){
// 					for (int j = 0; j < cm[0].length; j++) {
//						newCm[0][j] = cm[0][j];
//						newCm[1][j] = Rn.add(null,Rn.times(null, 0.5, cm[0][j]), Rn.times(null, 0.5, cm[1][j]));
//						newCm[2][j] = cm[1][j];
//					}
// 				}
// 				else if(p == 2){
// 					for (int j = 0; j < cm[0].length; j++) {
// 						newCm[0][j] = cm[0][j];
// 						newCm[1][j] = Rn.add(null,Rn.times(null, 0.5, cm[0][j]), Rn.times(null, 0.5, cm[1][j]));
// 						newCm[2][j] = Rn.add(null, Rn.add(null,Rn.times(null, 0.25, cm[0][j]), Rn.times(null, 0.5, cm[1][j])), Rn.times(null, 0.25, cm[2][j]));
// 						newCm[3][j] = Rn.add(null,Rn.times(null, 0.5, cm[1][j]), Rn.times(null, 0.5, cm[2][j]));
// 						newCm[4][j] = cm[2][j];
// 					}
// 				}
// 				else if(p == 3){
// 					for (int j = 0; j < cm[0].length; j++) {
// 						newCm[0][j] = cm[0][j];
// 						newCm[1][j] = Rn.add(null,Rn.times(null, 0.5, cm[0][j]), Rn.times(null, 0.5, cm[1][j]));
// 						newCm[2][j] = Rn.add(null, Rn.add(null,Rn.times(null, 0.25, cm[0][j]), Rn.times(null, 0.5, cm[1][j])), Rn.times(null, 0.25, cm[2][j]));
// 						newCm[3][j] = Rn.add(null, Rn.add(null,Rn.times(null, 0.125, cm[0][j]), Rn.times(null, 0.375, cm[1][j])), Rn.add(null,Rn.times(null, 0.375, cm[2][j]), Rn.times(null, 0.125, cm[3][j])));
// 						newCm[4][j] = Rn.add(null, Rn.add(null,Rn.times(null, 0.25, cm[1][j]), Rn.times(null, 0.5, cm[2][j])), Rn.times(null, 0.25, cm[3][j]));
// 						newCm[5][j] = Rn.add(null,Rn.times(null, 0.5, cm[2][j]), Rn.times(null, 0.5, cm[3][j]));
// 						newCm[6][j] = cm[3][j];
// 					}
// 				}
// 				else{
//	 				for (int j = 0; j < cm[0].length; j++) {
//	 					for (int i = 0; i <= 2 * p; i++) {
//	 						if(i <= p){
//	 							newCm[i][j] = new double[4];
//	 							for(int k = 0; k <= i; k++){
//	 								int dom = binomialCoefficient(i, k);
//	 								double num = Math.pow(2, i);
//	 								Rn.add(newCm[i][j], newCm[i][j], Rn.times(null, num / dom, cm[k][j]));
//	 							}
//	 						}
//	 						else{
//	 							newCm[i][j] = new double[4];
//	 							for(int k = 0; k <= (2 * p) - i; k++){
//	 								int dom = binomialCoefficient((2 * p) - i, k);
//	 								double num = Math.pow(2, ((2 * p) - i));
//	 								Rn.add(newCm[i][j], newCm[i][j], Rn.times(null, num / dom, cm[p - k][j]));
//	 							}
//	 						}
//	 					}
//	 				}
// 				}
// 				double u0 = U[0];
// 				double u1 = U[U.length - 1];
// 				double uHalf = (u0 + u1) / 2;
// 				
// 				double[] firstU = new double[U.length];
// 				for(int i  = 0; i < U.length / 2; i++){
// 					firstU[i] = u0;
// 				}
// 				for(int i  = U.length / 2; i < U.length; i++){
// 					firstU[i] = uHalf;
// 				}
// 				
// 				double[] secondU = new double[U.length];
// 				for(int i  = 0; i < U.length / 2; i++){
// 					secondU[i] = uHalf;
// 				}
// 				for(int i  = U.length / 2; i < U.length; i++){
// 					secondU[i] = u1;
// 				}
// 				
// 				for (int i = 0; i < firstCm.length; i++) {
// 					for (int j = 0; j < firstCm[0].length; j++) {
// 						firstCm[i][j] = newCm[i][j];
// 					}
//				}
// 				
// 				for (int i = 0; i < secondCm.length; i++) {
// 					for (int j = 0; j < secondCm[0].length; j++) {
// 						secondCm[i][j] = newCm[p  + i][j];
// 					}
//				}
// 				firstNs = new NURBSSurface(firstU, V, firstCm, p, q);
// 				secondNs = new NURBSSurface(secondU, V, secondCm, p, q);
// 				
// 				
// 				
// 			}
// 			if(!uDirection){
// 				double[][][]newCm = new double[cm.length][2 * q + 1][4];
// 				if(q == 1){
// 					for (int i = 0; i < cm.length; i++) {
//						newCm[i][0] = cm[i][0];
//						newCm[i][1] = Rn.add(null,Rn.times(null, 0.5, cm[i][0]), Rn.times(null, 0.5, cm[i][1]));
//						newCm[i][2] = cm[i][1];
//					}
// 				}
// 				else if(q == 2){
// 					for (int i = 0; i < cm.length; i++) {
// 						newCm[i][0] = cm[i][0];
// 						newCm[i][1] = Rn.add(null,Rn.times(null, 0.5, cm[i][0]), Rn.times(null, 0.5, cm[i][1]));
// 						newCm[i][2] = Rn.add(null, Rn.add(null,Rn.times(null, 0.25, cm[i][0]), Rn.times(null, 0.5, cm[i][1])), Rn.times(null, 0.25, cm[i][2]));
// 						newCm[i][3] = Rn.add(null,Rn.times(null, 0.5, cm[i][1]), Rn.times(null, 0.5, cm[i][2]));
// 						newCm[i][4] = cm[i][2];
// 					}
// 				}
// 				else if(q == 3){
// 					for (int i = 0; i < cm.length; i++) {
// 						newCm[i][0] = cm[i][0];
// 						newCm[i][1] = Rn.add(null,Rn.times(null, 0.5, cm[i][0]), Rn.times(null, 0.5, cm[i][1]));
// 						newCm[i][2] = Rn.add(null, Rn.add(null,Rn.times(null, 0.25, cm[i][0]), Rn.times(null, 0.5, cm[i][1])), Rn.times(null, 0.25, cm[i][2]));
// 						newCm[i][3] = Rn.add(null, Rn.add(null,Rn.times(null, 0.125, cm[i][0]), Rn.times(null, 0.375, cm[i][1])), Rn.add(null,Rn.times(null, 0.375, cm[i][2]), Rn.times(null, 0.125, cm[i][3])));
// 						newCm[i][4] = Rn.add(null, Rn.add(null,Rn.times(null, 0.25, cm[i][1]), Rn.times(null, 0.5, cm[i][2])), Rn.times(null, 0.25, cm[i][3]));
// 						newCm[i][5] = Rn.add(null,Rn.times(null, 0.5, cm[i][2]), Rn.times(null, 0.5, cm[i][3]));
// 						newCm[i][6] = cm[i][3];
// 					}
// 				}
// 				else{
//	 				for (int i = 0; i < cm[0].length; i++) {
//	 					for (int j = 0; j <= 2 * q; j++) {
//	 						if(j <= q){
//	 							newCm[i][j] = new double[4];
//	 							for(int k = 0; k <= j; k++){
//	 								int dom = binomialCoefficient(j, k);
//	 								double num = Math.pow(2, i);
//	 								Rn.add(newCm[i][j], newCm[i][j], Rn.times(null, num / dom, cm[i][k]));
//	 							}
//	 						}
//	 						else{
//	 							newCm[i][j] = new double[4];
//	 							for(int k = 0; k <= (2 * q) - j; k++){
//	 								int dom = binomialCoefficient((2 * q) - j, k);
//	 								double num = Math.pow(2, ((2 * q) - j));
//	 								Rn.add(newCm[i][j], newCm[i][j], Rn.times(null, num / dom, cm[i][q - k]));
//	 							}
//	 						}
//	 					}
//	 				}
// 				}
// 				double v0 = V[0];
// 				double v1 = V[V.length - 1];
// 				double vHalf = (v0 + v1) / 2;
// 				
// 				double[] firstV = new double[V.length];
// 				for(int i  = 0; i < V.length / 2; i++){
// 					firstV[i] = v0;
// 				}
// 				for(int i  = V.length / 2; i < V.length; i++){
// 					firstV[i] = vHalf;
// 				}
// 				
// 				double[] secondV = new double[V.length];
// 				for(int i  = 0; i < V.length / 2; i++){
// 					secondV[i] = vHalf;
// 				}
// 				for(int i  = V.length / 2; i < V.length; i++){
// 					secondV[i] = v1;
// 				}
// 				for (int i = 0; i < firstCm.length; i++) {
// 					for (int j = 0; j < firstCm[0].length; j++) {
// 						firstCm[i][j] = newCm[i][j];
// 					}
//				}
// 				
// 				for (int i = 0; i < secondCm.length; i++) {
// 					for (int j = 0; j < secondCm[0].length; j++) {
// 						secondCm[i][j] = newCm[i][q + j];
// 					}
//				}
// 				firstNs = new NURBSSurface(U, firstV, firstCm, p, q);
// 				secondNs = new NURBSSurface(U, secondV, secondCm, p, q);
// 			}
// 			halfPatches.add(firstNs);
// 			halfPatches.add(secondNs);
// 			return halfPatches;
// 		}
 		
// 		public LinkedList<NURBSSurface> subdivideBezierIntoFourBezierPatches(){
//// 			double firstTimeDouble = System.currentTimeMillis();
// 			LinkedList<NURBSSurface> newPatches = new LinkedList<NURBSSurface>();
// 			LinkedList<NURBSSurface> uPatches = subdivideBezierIntoTwoHalfPatches(true);
// 			for (NURBSSurface ns : uPatches) {
//				newPatches.addAll(ns.subdivideBezierIntoTwoHalfPatches(false));
//			}
//// 			double lastTimeDouble = System.currentTimeMillis();
////			time = time + (lastTimeDouble - firstTimeDouble);
// 			return newPatches;
// 		}
 		
// 		public LinkedList<NURBSSurface> subdivideUntilEveryPatchIsValid(){
// 			LinkedList<NURBSSurface> validList = new LinkedList<NURBSSurface>();
// 			LinkedList<NURBSSurface> oldList = decomposeIntoBezierSurfacesList();
// 			System.out.println("subdivideUntilEveryPatchIsValid()");
// 			while(!oldList.isEmpty()){
// 				System.out.println("IN WHILE");
// 				if(oldList.peekLast().hasValidControlmesh()){
// 					System.out.println("IF");
// 					NURBSSurface validSurface = oldList.pollLast();
// 					System.out.println("validSurface "+validSurface.toString());
// 					validList.add(validSurface);
// 				}
// 				else{
// 					System.out.println("else");
// 					NURBSSurface notValidSurface = oldList.pollLast();
// 					System.out.println("notValidSurface " + notValidSurface.toString());
// 					LinkedList<NURBSSurface> newPatches = notValidSurface.subdivideIntoFourNewPatches();
// 					oldList.addAll(newPatches);
// 				}
// 			}			
// 			return validList;
// 		}
		
// 		/**
// 		 * Newton's method
// 		 * @param intitialUV
// 		 * @return
// 		 */
// 		
// 		private double[] getClosestPoint(double[] intitialUV, double[] P){
// 			double u = intitialUV[0];
// 			double v = intitialUV[1];
// 			double[] S = getSurfacePoint(u, v);
// 			CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(this, u, v);
// 			double[] Su = ci.getSu();
//// 			double
// 			return null;
// 		}
 		
 		
 		private boolean isImpossiblePatch(double[] point, double closestMaxDistance){
 			double[] p = get3DPoint(point);
			for (int i = 0; i < controlMesh.length; i++) {
				for (int j = 0; j < controlMesh[0].length; j++) {
					double dist = Rn.euclideanDistance(p, get3DPoint(controlMesh[i][j]));
					if(dist < closestMaxDistance){
						return false;
					}
				}
			}
			return true;
		}
 		
 		private double[] getMinControlPoint(double[] point){
 			double[] p = get3DPoint(point);
 			double dist = Double.MAX_VALUE;
 			double[] minPoint = new double[3];
 			for (int i = 0; i < controlMesh.length; i++) {
 				for (int j = 0; j < controlMesh[0].length; j++) {
 					if(dist > Rn.euclideanDistance(p, get3DPoint(controlMesh[i][j]))){
 						dist = Rn.euclideanDistance(p, get3DPoint(controlMesh[i][j]));
 						minPoint = controlMesh[i][j];
 					}
 				}
			}
 		// returns the point in homog coords
 			return minPoint;
 		}
 		
 		private double[] getMaxControlPoint(double[] point){
 			double[] p = get3DPoint(point);
 			double dist = Double.MIN_VALUE;
 			double[] maxPoint = new double[3];
 			for (int i = 0; i < controlMesh.length; i++) {
 				for (int j = 0; j < controlMesh[0].length; j++) {
 					if(dist < Rn.euclideanDistance(p, get3DPoint(controlMesh[i][j]))){
 						dist = Rn.euclideanDistance(p, get3DPoint(controlMesh[i][j]));
 						maxPoint = controlMesh[i][j];
 					}
 				}
			}
 			// returns the point in homog coords
 			return maxPoint;
 		}
 		
 		private static NURBSSurface getClosestPatch(LinkedList<NURBSSurface> surfList, double[] point){
 			double[] p = get3DPoint(point);
 			double minDist = Double.MAX_VALUE;
 			NURBSSurface closestPatch = new NURBSSurface();
 			for (NURBSSurface ns : surfList) {
 				double[] minSurfPoint = get3DPoint(ns.getMinControlPoint(point));
				if(minDist > Rn.euclideanDistance(p, minSurfPoint)){
					minDist = Rn.euclideanDistance(p, minSurfPoint);
					closestPatch = ns;
				}
			}
 			return closestPatch;
 		}
 		
 		private static LinkedList<NURBSSurface> getPossiblePatches(LinkedList<NURBSSurface> surfList, double[] point){
 			double[] p = get3DPoint(point);
 			NURBSSurface closestPatch = getClosestPatch(surfList, point);
 			double[] maxPoint = get3DPoint(closestPatch.getMaxControlPoint(point));
 			double closestMaxDistance = Rn.euclideanDistance(p, maxPoint);
 			LinkedList<NURBSSurface> possiblePatches = new LinkedList<NURBSSurface>();
 			for (NURBSSurface ns : surfList) {
				 if(!ns.isImpossiblePatch(point, closestMaxDistance)){
					 possiblePatches.add(ns);
				 }
			}
 			return possiblePatches ;
 		}
 		
// 		private static LinkedList<NURBSSurface> subdivideUntilEveryPatchIsFlatEnough(LinkedList<NURBSSurface> surfList, double eps){
// 			LinkedList<NURBSSurface> flatList = new LinkedList<NURBSSurface>();
// 			for (NURBSSurface ns : surfList) {
//				if(ns.isFlatEnough(eps)){
//					flatList.add(ns);
//				}
//				else{
//					LinkedList<NURBSSurface> newList = ns.subdivideIntoFourNewPatches();
//					newList = subdivideUntilEveryPatchIsFlatEnough(newList, eps);
//					flatList.addAll(newList);
//				}
//			}
// 			return flatList;
// 		}
		
		
		/**
		 * 
		 * @param p
		 * @return
		 */
		public double[] getClosestPoint(double[] point){
			double[] p = new double[3];
			double dist = Double.MAX_VALUE;
			LinkedList<NURBSSurface> possiblePatches = decomposeIntoBezierSurfacesList();
//			NURBSSurface original = possiblePatches.getFirst();
			
		
			for (int i = 0; i < 12; i++) {
				LinkedList<NURBSSurface> subdividedPatches = new LinkedList<NURBSSurface>();
				possiblePatches = getPossiblePatches(possiblePatches, point);
				for (NURBSSurface ns : possiblePatches) {
					subdividedPatches.addAll(ns.subdivideIntoFourNewPatches());
				}
				possiblePatches = subdividedPatches;
//				System.out.println("Listenlaenge nach " + i + " Schritten: " + possiblePatches.size());
			}
		
			for (NURBSSurface ns : possiblePatches) {
				double[] U = ns.getUKnotVector();
				double[] V = ns.getVKnotVector();
				double u = (U[0] + U[U.length - 1]) / 2;
				double v = (V[0] + V[V.length - 1]) / 2;
				double[] homogSurfPoint = ns.getSurfacePoint(u, v);
				double[] surfPoint = get3DPoint(homogSurfPoint);
				if(dist > Rn.euclideanDistance(surfPoint, point)){
					dist = Rn.euclideanDistance(surfPoint, point);
					p = homogSurfPoint;
				}
			}
			return p;
		}
 		
 		public double[] getClosestPointWithTree(double[] point, NURBSTree nt){
 			double[] p = new double[3];
			double dist = Double.MAX_VALUE;
 			LinkedList<NURBSSurface> possiblePatches = decomposeIntoBezierSurfacesList();
 			if(nt == null){
 				nt = new NURBSTree(decomposeIntoBezierSurfacesList());
 			}
 			for (int i = 0; i < 10; i++) {
 				LinkedList<NURBSSurface> subdividedPatches = new LinkedList<NURBSSurface>();
 				possiblePatches = getPossiblePatches(possiblePatches, point);
 				for (NURBSSurface ns : possiblePatches) {
 					NURBSTreeNode ntn = new NURBSTreeNode(ns);
					subdividedPatches.addAll(ntn.getAllChilds());
				}
 				possiblePatches = subdividedPatches;
 			}
 			
 			for (NURBSSurface ns : possiblePatches) {
				double[] U = ns.getUKnotVector();
				double[] V = ns.getVKnotVector();
				double u = (U[0] + U[U.length - 1]) / 2;
				double v = (V[0] + V[V.length - 1]) / 2;
				double[] homogSurfPoint = ns.getSurfacePoint(u, v);
				double[] surfPoint = get3DPoint(homogSurfPoint);
				if(dist > Rn.euclideanDistance(surfPoint, point)){
					dist = Rn.euclideanDistance(surfPoint, point);
					p = homogSurfPoint;
				}
			}
 			return p;
 		}
 		
// 		public double[] getClosestPoint(double[] point){
// 
//			double dist = Double.MAX_VALUE;
//			LinkedList<NURBSSurface> possiblePatches = decomposeIntoBezierSurfacesList();
////			NURBSSurface original = possiblePatches.getFirst();
//			
//		
//			for (int i = 0; i < 12; i++) {
//				LinkedList<NURBSSurface> subdividedPatches = new LinkedList<NURBSSurface>();
//				possiblePatches = getPossiblePatches(possiblePatches, point);
//				for (NURBSSurface ns : possiblePatches) {
//					subdividedPatches.addAll(ns.subdivideIntoFourNewPatches());
//				}
//				possiblePatches = subdividedPatches;
////				System.out.println("Listenlaenge nach " + i + " Schritten: " + possiblePatches.size());
//			}
////			NURBSSurface bestSurface = new NURBSSurface();
//			double uStart = 0.;
//			double vStart = 0.;
//			for (NURBSSurface ns : possiblePatches) {
//				double[] U = ns.getUKnotVector();
//				double[] V = ns.getVKnotVector();
//				double u = (U[0] + U[U.length - 1]) / 2;
//				double v = (V[0] + V[V.length - 1]) / 2;
//				double[] homogSurfPoint = ns.getSurfacePoint(u, v);
//				double[] surfPoint = get3DPoint(homogSurfPoint);
//				if(dist > Rn.euclideanDistance(surfPoint, point)){
//					dist = Rn.euclideanDistance(surfPoint, point);
//					uStart = u;
//					vStart = v;
//				}
//			}
//
//			return newtonMethod(point, 0.001);
//		}
 		
 		

 		
		
		private double[] newtonMethod(double[] P, double eps){
			double u = (U[U.length - 1] + U[0]) / 2;
			double v = (V[V.length - 1] + V[0]) / 2;
			CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(this, u, v);
//			double[] S = getSurfacePoint(u, v);
			double[] S3D = get3DPoint(getSurfacePoint(u, v));
			double[] P3D = get3DPoint(P);
			double[] r = Rn.subtract(null, S3D, P3D);
			double[] Su = ci.getSu();
			double[] Sv = ci.getSv();
			double[] Suu = ci.getSuu();
			double[] Suv = ci.getSuv();
			double[] Svv = ci.getSvv();
			double f = Rn.innerProduct(r, Su);
			double g = Rn.innerProduct(r, Sv);
			double fu = Rn.innerProduct(Su, Su) + Rn.innerProduct(r, Suu);
			double fv = Rn.innerProduct(Su, Sv) + Rn.innerProduct(r, Suv);
			double gv = Rn.innerProduct(Sv, Sv) + Rn.innerProduct(r, Svv);
			for(int i = 0; i < 12; i++){
				if(Rn.euclideanDistanceSquared(S3D, P3D) < eps){
//					System.out.println(" genauigkeit erreicht");
					return S3D;
				}
				else{
					double deltaV = ((-g * fu + f * fv) /(fu * gv - fv * fv));
					double deltaU = -((f + (fv * deltaV)) / fu);
					u = deltaU + u;
					v = deltaV + v;
					boolean notInPatch = false;
					if(u < U[0]){
						u = U[0];
						notInPatch = true;
					}
					if(u > U[U.length - 1]){
						u = U[U.length - 1];
						notInPatch = true;
					}
					if(v < V[0]){
						v = V[0];
						notInPatch = true;
					}
					if(v > V[V.length - 1]){
						v = V[V.length - 1];
						notInPatch = true;
					}
					if(notInPatch){
//						System.out.println();
//						System.out.println("not in patch");
//						System.out.println();
//						S = getSurfacePoint(u, v);
						return get3DPoint(getSurfacePoint(u, v));
					}
					ci = NURBSCurvatureUtility.curvatureAndDirections(this, u, v);
					S3D = get3DPoint(getSurfacePoint(u, v));
					r = Rn.subtract(null, S3D, P);
					Su = ci.getSu();
					Sv = ci.getSv();
					Suu = ci.getSuu();
					Suv = ci.getSuv();
					Svv = ci.getSvv();
					f = Rn.innerProduct(r, Su);
					g = Rn.innerProduct(r, Sv);
					fu = Rn.innerProduct(Su, Su) + Rn.innerProduct(r, Suu);
					fv = Rn.innerProduct(Su, Sv) + Rn.innerProduct(r, Suv);
					gv = Rn.innerProduct(Sv, Sv) + Rn.innerProduct(r, Svv);
				}
			}
//			System.out.println("am ende");
			return S3D;
		}
	
		
		public static void main(String[] args){
//			double u = 0.2;
//			double v = 0.3;
//			double[] U = {0.0, 0.0, 0.0, 0.0, 6.08276253029822, 13.69853563616213, 17.82164126177979, 20.82164126177979, 20.82164126177979, 20.82164126177979, 20.82164126177979};
//			double[] V = {0.0, 0.0, 0.0, 3.141592653589793, 3.141592653589793, 6.283185307179586, 6.283185307179586, 6.283185307179586};
//			int p = 3;
//			int q = 2;
//			double[][][]Pw0 = {{{0.0, 8.0, 0.0, 1.0},{0.0, 5.656854249492381, 5.65685424949238, 0.7071067811865476},{0.0, 4.898587196589413E-16, 8.0, 1.0},{0.0, -5.65685424949238, 5.656854249492381, 0.7071067811865476},{0.0, -8.0, 9.797174393178826E-16, 1.0}}, 
//							{{1.878837730442698, 8.762285965878977, 0.0, 1.0},{1.3285388999451744, 6.195871825168743, 6.195871825168742, 0.7071067811865476},{1.878837730442698, 5.365352730663731E-16, 8.762285965878977, 1.0},{1.3285388999451744, -6.195871825168742, 6.195871825168743, 0.7071067811865476},{1.878837730442698, -8.762285965878977, 1.073070546132746E-15, 1.0}},
//							{{6.732195665770425, 10.3579035456024, 0.0, 1.0},{4.760381207540952, 7.324143835971641, 7.324143835971641, 0.7071067811865476},{6.732195665770425, 6.342386711499501E-16, 10.3579035456024, 1.0},{4.760381207540952, -7.324143835971641, 7.324143835971641, 0.7071067811865476},{6.732195665770425, -10.3579035456024, 1.2684773422999E-15, 1.0}},
//							{{11.79896093016204, 4.652377985466189, 0.0, 1.0},{8.343125284672714, 3.2897280221661513, 3.289728022166151, 0.7071067811865476},{11.79896093016204, 2.84875990416239E-16, 4.652377985466189, 1.0},{8.343125284672714, -3.289728022166151, 3.2897280221661513, 0.7071067811865476},{11.79896093016204, -4.652377985466189, 5.69751980832478E-16, 1.0}}, 
//							{{16.6099396548775, 7.289389793384495, 0.0, 1.0},{11.745000965063225, 5.154376953614183, 5.154376953614182, 0.7071067811865476},{16.6099396548775, 4.463463939102854E-16, 7.289389793384495, 1.0},{11.745000965063225, -5.154376953614182, 5.154376953614183, 0.7071067811865476},{16.6099396548775, -7.289389793384495, 8.926927878205708E-16, 1.0}}, 
//							{{19.00504976463275, 7.100369463198012, 0.0, 1.0},{13.438599565359619, 5.020719396357201, 5.0207193963572, 0.7071067811865476},{19.00504976463275, 4.347722367934528E-16, 7.100369463198012, 1.0},{13.438599565359619, -5.0207193963572, 5.020719396357201, 0.7071067811865476},{19.00504976463275, -7.100369463198012, 8.695444735869056E-16, 1.0}}, 
//							{{20.0, 7.0, 0.0, 1.0},{14.142135623730951, 4.949747468305833, 4.949747468305832, 0.7071067811865476},{20.0, 4.286263797015736E-16, 7.0, 1.0},{14.142135623730951, -4.949747468305832, 4.949747468305833, 0.7071067811865476},{20.0, -7.0, 8.572527594031472E-16, 1.0}}}; 
//			NURBSSurface ns = new NURBSSurface(U, V, Pw0, p, q);
//			NURBSSurface nsDecomposed = ns.decomposeSurface();
//			double[] originalPoint = ns.getSurfacePoint(u, v);
//			double[] decomposedPoint = nsDecomposed.getSurfacePoint(u, v);
//			System.out.println("nsDecomposed ");
//			System.out.println(nsDecomposed.toString());
//			double[] bezierPoint = new double[4];
//			LinkedList<NURBSSurface> bezierList = ns.decomposeIntoBezierSurfacesList();
//			System.out.println("bezierList size " + bezierList.size());
//			int counter = 0;
//			for (NURBSSurface bezier : bezierList) {
//				counter++;
//				System.out.println(counter + ". bezier");
//				System.out.println(bezier.toString());
//				double[] bU = bezier.getUKnotVector();
//				double[] bV = bezier.getVKnotVector();
//				double uStart = bU[0];
//				double uEnd = bU[bU.length - 1];
//				double vStart = bV[0];
//				double vEnd = bV[bV.length - 1];
//				if(u >= uStart && u <= uEnd && v >= vStart && v<= vEnd){
//					bezierPoint = bezier.getSurfacePoint(u, v);
//				}
//			}
//			System.out.println("originalPoint " + Arrays.toString(originalPoint));
//			System.out.println("decomposedPoint " + Arrays.toString(decomposedPoint));
//			System.out.println("bezierPoint " + Arrays.toString(bezierPoint));	
			double a = 1;
			double b = 2;
			double c = 3;
			double f = 1;
			double g = 1;
			double y = ((-g * a + f * b) /(a * c - b * b));
			double x = -((f + (b * y)) / a);
			System.out.println("x = " + x + "; y = " + y);
		
		}
		
	
}
