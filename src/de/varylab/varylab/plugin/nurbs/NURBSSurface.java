package de.varylab.varylab.plugin.nurbs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import de.jreality.math.Rn;
import de.jtem.numericalMethods.calculus.function.RealFunctionOfSeveralVariables;
import de.jtem.numericalMethods.calculus.minimizing.NelderMead;
import de.jtem.numericalMethods.calculus.rootFinding.NewtonRaphson;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.math.LineSegmentIntersection;
import de.varylab.varylab.plugin.nurbs.math.NURBSAlgorithm;
import de.varylab.varylab.plugin.nurbs.math.NURBSCurvatureUtility;
import de.varylab.varylab.plugin.nurbs.math.PointProjection;
import de.varylab.varylab.utilities.MathUtility;



	public class NURBSSurface {
		
		public enum BoundaryLines{u0, um, v0, vn};
		public enum CornerPoints{P00, Pm0, P0n, Pmn};
		private LinkedList<BoundaryLines> boundLines = new LinkedList<NURBSSurface.BoundaryLines>();
		private LinkedList<CornerPoints> cornerPoints = new LinkedList<CornerPoints>();
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
			cornerPoints.add(CornerPoints.P00);
			cornerPoints.add(CornerPoints.Pm0);
			cornerPoints.add(CornerPoints.P0n);
			cornerPoints.add(CornerPoints.Pmn);
			boundLines.add(BoundaryLines.u0);
			boundLines.add(BoundaryLines.um);
			boundLines.add(BoundaryLines.v0);
			boundLines.add(BoundaryLines.vn);
		}
		
		public NURBSSurface(double[] UVec, double[] VVec, double[][][] cm, int pDegree, int qDegree, LinkedList<BoundaryLines> boundList, LinkedList<CornerPoints> cornerList){
			U = UVec;
			V = VVec;
			controlMesh = cm;
			p = pDegree;
			q = qDegree;
			cornerPoints = cornerList;
			boundLines = boundList;
		}
		
		public LinkedList<CornerPoints> getCornerPoints() {
			return cornerPoints;
		}

		public void setCornerPoints(LinkedList<CornerPoints> cornerPoints) {
			this.cornerPoints = cornerPoints;
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
		
		

		public LinkedList<BoundaryLines> getBoundLines() {
			return boundLines;
		}

		public void setBoundLines(LinkedList<BoundaryLines> boundLines) {
			this.boundLines = boundLines;
		}
		
		public void setAllBoundLines(){
			LinkedList<BoundaryLines> bl = new LinkedList<NURBSSurface.BoundaryLines>();
			bl.add(BoundaryLines.u0);
			bl.add(BoundaryLines.um);
			bl.add(BoundaryLines.v0);
			bl.add(BoundaryLines.vn);
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
			str = str + '\n' + "boundary lines: " + boundaryToString();
			return str;
		}
		
		
		private LinkedList<double[][]> getBoundarySegments(){
			LinkedList<double[][]> segList = new LinkedList<double[][]>();
			double[] p1 = {U[0],V[0]}; 
			double[] p2 = {U[U.length - 1],V[0]}; 
			double[] p3 = {U[U.length - 1],V[V.length - 1]};
			double[] p4 = {U[0],V[V.length - 1]};
			double[][] seg1 = {p1,p2};
			double[][] seg2 = {p2,p3};
			double[][] seg3 = {p3,p4};
			double[][] seg4 = {p4,p1};
			segList.add(seg1); segList.add(seg2); segList.add(seg3); segList.add(seg4);
			return segList;
		}
		
		private static double[] intersectionPoint(double[][] first, double[][] second){
			double s1 = first[0][0];
			double s2 = first[0][1];
			double t1 = first[1][0];
			double t2 = first[1][1];
			double p1 = second[0][0];
			double p2 = second[0][1];
			double q1 = second[1][0];
			double q2 = second[1][1];
			double lambda = ((p1 - s1) * (s2 - t2) - (p2 - s2) * (s1 - t1)) / ((q2 - p2) * (s1 - t1) - (q1 - p1) * (s2 - t2));
			return Rn.add(null, second[0],Rn.times(null, lambda,Rn.subtract(null, second[1], second[0])));
		}
		
		public static boolean twoSegmentIntersection(double[][] seg1, double[][] seg2){
			double[] p1 = seg1[0];
			double[] p2 = seg1[1]; 
			double[] p3 = seg2[0]; 
			double[] p4 = seg2[1];
			double lengthSeg1 = Rn.euclideanDistance(p1, p2);
			double lengthSeg2 = Rn.euclideanDistance(p3, p4);
			double[] p2MinusP1 = Rn.add(null, p2, Rn.times(null, -1, p1));
			double[] q2 = Rn.add(null, p2, Rn.times(null, lengthSeg1 / 100, p2MinusP1));	
			double[] q1 = Rn.add(null, p1, Rn.times(null, lengthSeg1 / -100, p2MinusP1));
			double[] p4MinusP3 = Rn.add(null, p4, Rn.times(null, -1, p3));
			double[] q4 = Rn.add(null, p4, Rn.times(null, lengthSeg2 / 100, p4MinusP3));	
			double[] q3 = Rn.add(null, p3, Rn.times(null, lengthSeg2 / -100, p4MinusP3));
			
			if(LineSegmentIntersection.counterClockWiseOrder(q1, q3, q4) == LineSegmentIntersection.counterClockWiseOrder(q2, q3, q4)){
				return false;
			}
			else if(LineSegmentIntersection.counterClockWiseOrder(q1, q2, q3) == LineSegmentIntersection.counterClockWiseOrder(q1, q2, q4)){
				return false;
			}
			else{
				return true;
			}	
		}
		
		private static double[] getBoundaryIntersection(double[][]seg, LinkedList<double[][]> boundary){
			for (double[][] b : boundary) {
				if(twoSegmentIntersection(seg, b)){
					return intersectionPoint(seg, b);
				}
			}
			return null;
		}
		
		
		
		/**
		 * 
		 * @param u
		 * @param v
		 * @return the point in R^4
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
			NURBSSurface ns = new NURBSSurface(UQ, VQ, Qw, p, q, getBoundLines(), getCornerPoints());
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
			LinkedList<BoundaryLines> bList = ns.getBoundLines();
			LinkedList<CornerPoints> cornerList = ns.getCornerPoints();
			
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
			NURBSSurface nsReturn = new NURBSSurface(UQ, VQ, Qw, p, q,bList,cornerList);
			return nsReturn;
		}
		
		
		/**
		 * decomposes both knot vectors of this surface s.t. both are filled<br/>
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
			LinkedList<BoundaryLines> originalBl = getBoundLines();
			double u0 = U[0];
			double um = U[U.length - 1];
			double v0 = V[0];
			double vn = V[V.length - 1];
			double[][][]Pw = nsDecompose.getControlMesh();
			int p = getDegreeFromClampedKnotVector(U);
			int q = getDegreeFromClampedKnotVector(V);
			double[] differentUknots = getAllDifferentKnotsFromFilledKnotVector(U, p);
			double[] differentVknots = getAllDifferentKnotsFromFilledKnotVector(V, q);
			NURBSSurface[][] BezierSurfaces = new NURBSSurface[differentUknots.length - 1][differentVknots.length - 1];
			for (int i = 0; i < BezierSurfaces.length; i++) {
				for (int j = 0; j < BezierSurfaces[0].length; j++) {
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
					LinkedList<BoundaryLines> bl = new LinkedList<NURBSSurface.BoundaryLines>();
					if(UknotVector[0] == u0 && originalBl.contains(BoundaryLines.u0)){
						bl.add(BoundaryLines.u0);
					}
					if(UknotVector[UknotVector.length - 1] == um && originalBl.contains(BoundaryLines.um)){
						bl.add(BoundaryLines.um);
					}
					if(VknotVector[0] == v0 && originalBl.contains(BoundaryLines.v0)){
						bl.add(BoundaryLines.v0);
					}
					if(VknotVector[VknotVector.length - 1] == vn && originalBl.contains(BoundaryLines.vn)){
						bl.add(BoundaryLines.vn);
					}
					
					double[][][]BezierControlPoints = new double[UknotVector.length - p - 1][VknotVector.length - q - 1][4];
					for (int iB = 0; iB < BezierControlPoints.length; iB++) {
						for (int jB = 0; jB < BezierControlPoints[0].length; jB++) {
							BezierControlPoints[iB][jB] = Pw[p * i + iB][q * j + jB];
						}
					}
					LinkedList<CornerPoints> cornerList = new LinkedList<CornerPoints>();
					if(bl.size() > 1){
						if(bl.contains(BoundaryLines.u0) && bl.contains(BoundaryLines.v0)){
							cornerList.add(CornerPoints.P00);
						}
						if(bl.contains(BoundaryLines.u0) && bl.contains(BoundaryLines.vn)){
							cornerList.add(CornerPoints.P0n);
						}
						if(bl.contains(BoundaryLines.um) && bl.contains(BoundaryLines.v0)){
							cornerList.add(CornerPoints.Pm0);
						}
						if(bl.contains(BoundaryLines.um) && bl.contains(BoundaryLines.vn)){
							cornerList.add(CornerPoints.Pmn);
						}
					}
					BezierSurfaces[i][j] = new NURBSSurface(UknotVector, VknotVector, BezierControlPoints, p, q, bl, cornerList);
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
		
//		private static double[] projectOnto(double[] pos, double[] v1, double[] v2, double[] v3) {
//			double[] fn = Rn.crossProduct(null, Rn.subtract(null, v2, v1), Rn.subtract(null, v3, v1));
//			double[] proj = Rn.subtract(null, pos, v1);
//			Rn.projectOntoComplement(proj, proj, fn);
//			return Rn.add(null,proj,v1);
//		}
		
 		
 		public LinkedList<NURBSSurface> subdivideIntoFourNewPatches(){
 			LinkedList<NURBSSurface> newPatches = new LinkedList<NURBSSurface>();
 			double uInsert = (U[U.length - 1] + U[0]) / 2.0;
 			double vInsert = (V[V.length - 1] + V[0]) / 2.0;
 			NURBSSurface nsInsert = SurfaceKnotInsertion(true, uInsert, 1);
 			nsInsert = SurfaceKnotInsertion(nsInsert, false, vInsert, 1);
 			newPatches = nsInsert.decomposeIntoBezierSurfacesList();
 			return newPatches;
 		}
 		
 
 	
 		
//		public double[] getClosestPointDist(double[] point, NURBSTree nt){
//			
//			double[] p = new double[3];
// 			int n = controlMesh.length;
// 			int m = controlMesh[0].length; 
// 			double[] pp00 = Rn.subtract(null, MathUtility.get3DPoint(point), MathUtility.get3DPoint(controlMesh[0][0]));
// 			double[] pp01 = Rn.subtract(null, MathUtility.get3DPoint(point), MathUtility.get3DPoint(controlMesh[0][m - 1]));
// 			double[] pp10 = Rn.subtract(null, MathUtility.get3DPoint(point), MathUtility.get3DPoint(controlMesh[n - 1][0]));
// 			double[] pp11 = Rn.subtract(null, MathUtility.get3DPoint(point), MathUtility.get3DPoint(controlMesh[n - 1][m - 1]));
// 			boolean b00 = true;
// 			boolean b01 = true;
// 			boolean b10 = true;
// 			boolean b11 = true;
// 			double[] cMPoint;
// 			for (int i = 0; i < n; i++) {
//				for (int j = 0; j < m; j++) {
//					double[] ijPoint = MathUtility.get3DPoint(controlMesh[i][j]);
//					cMPoint = Rn.subtract(null, ijPoint, MathUtility.get3DPoint(controlMesh[0][0]));
//					if(Rn.innerProduct(pp00, cMPoint) > 0){
//						b00 = false;
//					}
//					cMPoint = Rn.subtract(null, ijPoint,MathUtility. get3DPoint(controlMesh[0][m - 1]));
//					if(Rn.innerProduct(pp01, cMPoint) > 0){
//						b01 = false;
//					}
//					cMPoint = Rn.subtract(null, ijPoint, MathUtility.get3DPoint(controlMesh[n - 1][0]));
//					if(Rn.innerProduct(pp10, cMPoint) > 0){
//						b10 = false;
//					}
//					cMPoint = Rn.subtract(null, ijPoint, MathUtility.get3DPoint(controlMesh[n - 1][m - 1]));
//					if(Rn.innerProduct(pp11, cMPoint) > 0){
//						b11 = false;
//					}
//					
//				}
//			}
// 			if(b00){
// 				System.out.println("HALLO b00");
// 				return controlMesh[0][0];
// 			}
// 			if(b01){
// 				System.out.println("HALLO b01");
// 				return controlMesh[0][m - 1];
// 			}
// 			if(b10){
// 				System.out.println("HALLO b10");
// 				return controlMesh[n - 1][0];
// 			}
// 			if(b11){
// 				System.out.println("HALLO b11");
// 				return controlMesh[n - 1][m - 1];
// 			}
// 			double dist = Double.MAX_VALUE;
//			
// 			if(nt == null){
// 				nt = new NURBSTree(decomposeIntoBezierSurfacesList());
// 			}
// 			LinkedList<NURBSTreeNode> possiblePatches = nt.getDummy().getBezierList();
// 			for (int i = 0; i < 10; i++) {
// 				if(i == 11){
// 					double uStart = 0.;
// 					double vStart = 0.;
// 					for (NURBSTreeNode ntn : possiblePatches) {
// 						double[] U = ntn.getNs().getUKnotVector();
// 						double[] V = ntn.getNs().getVKnotVector();
// 						double u = (U[0] + U[U.length - 1]) / 2;
// 						double v = (V[0] + V[V.length - 1]) / 2;
// 						double[] homogSurfPoint = ntn.getNs().getSurfacePoint(u, v);
// 						double[] surfPoint = MathUtility.get3DPoint(homogSurfPoint);
// 						if(dist > Rn.euclideanDistance(surfPoint, point)){
// 							dist = Rn.euclideanDistance(surfPoint, point);
// 							uStart = u;
// 							vStart = v;
// 						}
// 					}
//					double[] result = newtonMethodDist (point, 0.0000000000001,uStart, vStart);
////					System.out.println("result in Dist " + Arrays.toString(result));
// 					if(result != null){
//// 						System.out.println("newtonMethodDist is sucsessful");
// 						return result;
// 					}
// 				}
// 				LinkedList<NURBSTreeNode> subdividedPatches = new LinkedList<NURBSTreeNode>();
// 				possiblePatches = getPossiblePatches(possiblePatches, point);
// 				for (NURBSTreeNode ntn : possiblePatches) {
//					subdividedPatches.addAll(ntn.getAllChildNodes());
//				}
// 				possiblePatches = subdividedPatches;
// 			}
// //			System.out.println(nt.toString());
// 			
// 			
// 			for (NURBSTreeNode ntn : possiblePatches) {
//				double[] U = ntn.getNs().getUKnotVector();
//				double[] V = ntn.getNs().getVKnotVector();
//				double u = (U[0] + U[U.length - 1]) / 2;
//				double v = (V[0] + V[V.length - 1]) / 2;
//				double[] homogSurfPoint = ntn.getNs().getSurfacePoint(u, v);
//				double[] surfPoint = MathUtility.get3DPoint(homogSurfPoint);
//				if(dist > Rn.euclideanDistance(surfPoint, point)){
//					dist = Rn.euclideanDistance(surfPoint, point);
//					p = homogSurfPoint;
//				}
//			}
// 			return p;
// 		}
		
		public double[] getClosestPointOrth(double[] point){
 			return PointProjection.getClosestPoint(this, point);
 		}
	

		private double[] newtonMethodOrthogonal(double[] P, double eps, double u, double v){
			CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(this, u, v);
			double[] S = getSurfacePoint(u, v);
			double[] S3D = MathUtility.get3DPoint(getSurfacePoint(u, v));
			double[] P3D = MathUtility.get3DPoint(P);
			double[] r = Rn.times(null, 1 / Rn.euclideanNorm(Rn.subtract(null, S3D, P3D)), Rn.subtract(null, S3D, P3D));
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
			double deltaU = Double.MAX_VALUE;
			double deltaV = Double.MAX_VALUE;
			double[]oldIteration = new double[2];
			double[]newIteration = new double[2];
			double patchDist = Math.min(U[U.length-1] - U[0], V[V.length-1] - V[0]) / 3.;
			for(int i = 0; i < 12; i++){
//				if(f < eps && g < eps && deltaU < eps && deltaV < eps){
//				if(false){	
//					System.out.println("terminiert nach " + i + " Schritten");
//					return S;
//					
//				}
//				else{
				deltaV = ((-g * fu + f * fv) /(fu * gv - fv * fv));
				deltaU = -((f + (fv * deltaV)) / fu);
				oldIteration[0] = u;
				oldIteration[1] = v;
				u = deltaU + u;
				v = deltaV + v;
				newIteration[0] = u;
				newIteration[1] = v;
				if((u < U[0] || u > U[U.length - 1] || v < V[0] || v > V[V.length - 1])){
					return null;
				}
				if(Rn.euclideanDistance(newIteration, oldIteration) > patchDist){
					return null;
					
				}
				ci = NURBSCurvatureUtility.curvatureAndDirections(this, u, v);
				S = getSurfacePoint(u, v);
				S3D = MathUtility.get3DPoint(getSurfacePoint(u, v));
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
			if(f > eps || g > eps){
				System.out.println("f " + f + " g " + g);
			}
			return S;
		}
		
		
		
		private double[] newtonMethodDist(double[] p, double eps, double u, double v){
			CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(this, u, v);
			double[] S = getSurfacePoint(u, v);
			double[] S3D = MathUtility.get3DPoint(getSurfacePoint(u, v));
			double[] P3D = MathUtility.get3DPoint(p);
			double[] Su = ci.getSu();
			double[] Sv = ci.getSv();
			double[] Suu = ci.getSuu();
			double[] Suv = ci.getSuv();
			double[] Svv = ci.getSvv();
			
			// we define the functional F(u,v):= sqrt(<S(u,v) - p,S(u,v) - p>) and minimize F
			double F = Math.sqrt(Rn.innerProduct(Rn.subtract(null, S3D, P3D), Rn.subtract(null, S3D, P3D)));
			double Fu = Rn.innerProduct(Su, Rn.subtract(null, S3D, P3D)) / F;
			double Fv = Rn.innerProduct(Sv, Rn.subtract(null, S3D, P3D)) / F;
			double Fuu = ((Rn.innerProduct(Suu, Rn.subtract(null, S3D, P3D)) + Rn.innerProduct(Su, Su)) * F - Rn.innerProduct(Su, Rn.subtract(null, S3D, P3D)) * Fu) / (F * F);
			double Fuv = ((Rn.innerProduct(Suv, Rn.subtract(null, S3D, P3D)) + Rn.innerProduct(Su, Sv)) * F - Rn.innerProduct(Su, Rn.subtract(null, S3D, P3D)) * Fv) / (F * F);
			double Fvv = ((Rn.innerProduct(Svv, Rn.subtract(null, S3D, P3D)) + Rn.innerProduct(Sv, Sv)) * F - Rn.innerProduct(Sv, Rn.subtract(null, S3D, P3D)) * Fv) / (F * F);
			double deltaU = Double.MAX_VALUE;;
			double deltaV = Double.MAX_VALUE;;
			for(int i = 0; i < 12; i++){
//				if(Fu < eps && Fv < eps && deltaU < eps && deltaV < eps){
////					System.out.println("terminiert nach " + i + " Schritten");
//					return S;
//				}
				deltaV = ((-Fv * Fuu + Fu * Fuv) /(Fuu * Fvv - Fuv * Fuv));
				deltaU = -((Fu + (Fuv * deltaV)) / Fuu);
				u = deltaU + u;
				v = deltaV + v;
				if(u < U[0] || u > U[U.length - 1] || v < V[0] || v > V[V.length - 1]){
//					System.out.println("not in patch dist");
					return null;
				}
				ci = NURBSCurvatureUtility.curvatureAndDirections(this, u, v);
				S = getSurfacePoint(u, v);
				S3D = MathUtility.get3DPoint(getSurfacePoint(u, v));
				Su = ci.getSu();
				Sv = ci.getSv();
				Suu = ci.getSuu();
				Suv = ci.getSuv();
				Svv = ci.getSvv();
				F = Math.sqrt(Rn.innerProduct(Rn.subtract(null, S3D, P3D), Rn.subtract(null, S3D, P3D)));
				Fu = Rn.innerProduct(Su, Rn.subtract(null, S3D, P3D)) / F;
				Fv = Rn.innerProduct(Sv, Rn.subtract(null, S3D, P3D)) / F;
				Fuu = ((Rn.innerProduct(Suu, Rn.subtract(null, S3D, P3D)) + Rn.innerProduct(Su, Su)) * F - Rn.innerProduct(Su, Rn.subtract(null, S3D, P3D)) * Rn.innerProduct(Su, Rn.subtract(null, S3D, P3D))) / (F * F);
				Fuv = ((Rn.innerProduct(Suv, Rn.subtract(null, S3D, P3D)) + Rn.innerProduct(Su, Sv)) * F - Rn.innerProduct(Su, Rn.subtract(null, S3D, P3D)) * Rn.innerProduct(Sv, Rn.subtract(null, S3D, P3D))) / (F * F);
				Fvv = ((Rn.innerProduct(Svv, Rn.subtract(null, S3D, P3D)) + Rn.innerProduct(Sv, Sv)) * F - Rn.innerProduct(Sv, Rn.subtract(null, S3D, P3D)) * Rn.innerProduct(Sv, Rn.subtract(null, S3D, P3D))) / (F * F);
			}
			return S;
		}
		
		public String boundaryToString(){
			String str = new String();
			if(boundLines.size() == 0){
				str = "no boundary line";
			}
			for (BoundaryLines bl : boundLines) {
				str = str + bl +", ";
			}
			return str;
		}
		
		public String cornersToString(){
			String str = new String();
			if(cornerPoints.size() == 0){
				str = "no corners";
			}
			for (CornerPoints cp : cornerPoints) {
				str = str + cp +", ";
			}
			return str;
		}
		
		
	
}
