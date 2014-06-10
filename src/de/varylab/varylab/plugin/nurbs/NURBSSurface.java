package de.varylab.varylab.plugin.nurbs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.numericalMethods.calculus.function.RealFunctionOfSeveralVariables;
import de.jtem.numericalMethods.calculus.minimizing.Info;
import de.jtem.numericalMethods.calculus.minimizing.NelderMead;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.nurbs.adapter.FlatIndexFormAdapter;
import de.varylab.varylab.plugin.nurbs.adapter.VectorFieldMapAdapter;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.math.NURBSAlgorithm;
import de.varylab.varylab.plugin.nurbs.math.NURBSCurvatureUtility;
import de.varylab.varylab.plugin.nurbs.math.NurbsSurfaceUtility;
import de.varylab.varylab.plugin.nurbs.math.PointProjectionSurface;
import de.varylab.varylab.plugin.nurbs.math.PointProjectionSurfaceOfRevolution;
import de.varylab.varylab.plugin.nurbs.type.NurbsUVCoordinate;

/**
 * 
 * @author seidel
 *
 */


	public class NURBSSurface {
		
		private static Logger logger = Logger.getLogger(NURBSSurface.class.getName());
		
		public enum BoundaryLines{u0, um, v0, vn};
		public enum CornerPoints{P00, Pm0, P0n, Pmn};
		public enum RevolutionDir{uDir, vDir};
		public enum ClosingDir{uClosed, vClosed, uvClosed, nonClosed};
		private LinkedList<BoundaryLines> boundLines = new LinkedList<NURBSSurface.BoundaryLines>();
		private LinkedList<CornerPoints> cornerPoints = new LinkedList<CornerPoints>();
		private RevolutionDir revDir = null;
		private ClosingDir closDir = null;
		private double[] U;
		private double[] V;
		private LinkedList<NURBSTrimLoop> trimC = new LinkedList<NURBSTrimLoop>();
		private LinkedList<NURBSTrimLoop> holeC = new LinkedList<NURBSTrimLoop>();
		private double[][][] controlMesh;
		private int p, q;
		private String name = "Nurbs Surface";
		private LinkedList<Double> boundaryValues = null;
		private double[] closedBoundaryValues = null;

//		private List<double[]> umbilics = new LinkedList<double[]>();
		
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
//			revDir = PointProjectionSurfaceOfRevolution.getRotationDir(this);
//			closDir = getClosingDir();
		}
		
		public NURBSSurface(double[] UVec, double[] VVec, double[][][] cm, int pDegree, int qDegree, LinkedList<BoundaryLines> boundList, LinkedList<CornerPoints> cornerList){
			U = UVec;
			V = VVec;
			controlMesh = cm;
			p = pDegree;
			q = qDegree;
			cornerPoints = cornerList;
			boundLines = boundList;
//			revDir = PointProjectionSurfaceOfRevolution.getRotationDir(this);
			closDir = getClosingDir();
		}
		
		public NURBSSurface(double[][][] cm, int pDeg, int qDeg) {
			controlMesh = cm;
			p = pDeg;
			q = qDeg;
			int m = cm.length;
			int n = cm[0].length;
			U = NurbsSurfaceUtility.uniformKnotVector(m, pDeg);
			V = NurbsSurfaceUtility.uniformKnotVector(n, qDeg);
		}
		
		public boolean hasClampedKnotVectors(){
			return (NURBSAlgorithm.isClamped(U, p) && NURBSAlgorithm.isClamped(V, q));
		}
		
		private static boolean isLeftClampedKnotVector(double[] U, int p){
			return (U[0] == U[p]);
		}
		
		private static boolean isRightClampedKnotVector(double[] U, int p){
			return (U[U.length - 1] == U[U.length - 1 - p]);
		}
		
		private void repairLeftSide(boolean dir){
			double uv;
			if(dir){
				uv = U[p];
			}
			else{
				uv = V[q];
			}
			NURBSSurface[] split = splitAtKnot1(dir, uv);
			this.setControlMesh(split[1].getControlMesh());
			this.setUKnotVector(split[1].getUKnotVector());
			this.setVKnotVector(split[1].getVKnotVector());
		}
		
		private void repairRightSide(boolean dir){
			double uv;
			if(dir){
				uv = U[U.length - 1 - p];
			}
			else{
				uv = V[V.length - 1 - q];
			}
			NURBSSurface[] split = splitAtKnot1(dir, uv);
			this.setControlMesh(split[0].getControlMesh());
			this.setUKnotVector(split[0].getUKnotVector());
			this.setVKnotVector(split[0].getVKnotVector());
			
		}
		
		
		public void repairKnotVectors(){
			if(!NURBSAlgorithm.isClamped(U, p)){
				if(!isLeftClampedKnotVector(U, p)){
					repairLeftSide(true);
				}
				if(!isRightClampedKnotVector(U, p)){
					repairRightSide(true);
				}
				
			}
			if(!NURBSAlgorithm.isClamped(V, q)){
				if(!isLeftClampedKnotVector(V, q)){
					repairLeftSide(false);
				}
				if(!isRightClampedKnotVector(V, q)){
					repairRightSide(false);
				}
			}
		}

		/**
		 * if this surface is no surface of revolution, then revDir = null
		 */
		
		public void setRevolutionDir(){
			revDir = PointProjectionSurfaceOfRevolution.getRotationDir(this);
		}
		
		

		public RevolutionDir getRevolutionDir(){
			return revDir;
		}
		
		
		public double[] getGluedBoundaryValues(){
			if(getClosingDir() == ClosingDir.nonClosed){
				return null;
			}
			else if(getClosingDir() == ClosingDir.uClosed){
				closedBoundaryValues = new double[2];
				closedBoundaryValues[0] = U[0];
				closedBoundaryValues[1] = U[U.length - 1];
			}
			else if(getClosingDir() == ClosingDir.vClosed){
				closedBoundaryValues = new double[2];
				closedBoundaryValues[0] = V[0];
				closedBoundaryValues[1] = V[V.length - 1];
			}
			return closedBoundaryValues;
		}
		
		
		

		public LinkedList<Double> getBoundaryValues() {
			if(boundaryValues == null){
				boundaryValues = determineBoundaryValues();
			}
			return boundaryValues;
		}
		

		public void setBoundaryValues(LinkedList<Double> boundaryValues) {
			this.boundaryValues = boundaryValues;
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
		
		private ClosingDir determineClosingCondition(){
			if(isClosedUDir() && isClosedVDir()){
				return ClosingDir.uvClosed;
			}
			if(isClosedUDir()){
				return ClosingDir.uClosed;
			}
			if(isClosedVDir()){
				return ClosingDir.vClosed;
			}
			else{
				return ClosingDir.nonClosed;
			}
		}
		
		public void setClosingDir(ClosingDir dir){
			closDir = dir;
		}
		
		public ClosingDir getClosingDir(){
			if(closDir == null){
				return determineClosingCondition();
			}
			else{
				return closDir;
			}
		}
		
		private boolean isClosedUDir(){
			int m = controlMesh.length;
			int n = controlMesh[0].length;
			for (int j = 0; j < n; j++) {
				if(Rn.euclideanDistance(controlMesh[0][j], controlMesh[m - 1][j]) > 0.0001){
					return false;
				}
			}
			return true;
		}
		
		private boolean isClosedVDir(){
			int m = controlMesh.length;
			int n = controlMesh[0].length;
			for (int i = 0; i < m; i++) {
				if(Rn.euclideanDistance(controlMesh[i][0],  controlMesh[i][n - 1]) > 0.0001){
					return false;
				}
			}
			return true;
		}
		
		
		
		public boolean isClosedBoundaryPoint(double[] point){
			if(getClosingDir() == ClosingDir.uClosed){
				if(point[0] == U[0] || point[0] == U[U.length - 1]){
					return true;
				}
				else return false;
			}
			if(getClosingDir() == ClosingDir.vClosed){
				if(point[1] == V[0] || point[1] == V[V.length - 1]){
					return true;
				}
				else return false;
			}
			if(getClosingDir() == ClosingDir.uvClosed){
				if(point[0] == U[0] || point[0] == U[U.length - 1] || point[1] == V[0] || point[1] == V[V.length - 1]){
					return true;
				}
				else return false;
			}
			return false;
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
		
		
		/**
		 * 
		 * @param u
		 * @param v
		 * @return the point in R^4
		 */
		public double[] getSurfacePoint( double u, double v) {
			double[] S = new double[4];
			if(u < U[0]) {
				u = U[0];
			} else if(u > U[U.length-1]) {
				u = U[U.length-1];
			}
			if(v < V[0]) {
				v = V[0];
			} else if(v > V[V.length-1]) {
				v = V[V.length-1];
			}
			NURBSAlgorithm.SurfacePoint(p, U, q, V, controlMesh, u, v, S);
			return S;
		}
		
		
		/**
		 * 
		 * @param knot
		 * @param knotVector
		 * @return multiplicity of a given knot in the knot vector
		 */
		public static int getMultiplicity(double knot, double[] knotVector){
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
		public static int getDegreeFromClampedKnotVector(double[] knotVector){
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
		public static void getAllNewKnots(double[] knotVector,ArrayList<Double> newKnots, ArrayList<Integer> multiplicity){
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
		 * @return all distinct interior knots 
		 */
		public static double[] getAllDifferentKnotsFromFilledKnotVector(double[] knotVector, int p){
			int knotSize = (knotVector.length - 2) / p;
			double[] knots = new double[knotSize];
			for (int i = 0; i < knots.length; i++) {
				knots[i] = knotVector[p * i + 1];
			}
			return knots;
		}
		
		/**
		 * insert the knot uv r times into the knot vector of this surface. If dir == true into
		 *  U else into V
		 * @param dir
		 * @param uv
		 * @param r
		 * @return the same surface defined by the new knot vector and an adapted control mesh
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
				if(uv == UP[0]){
					logger.info("set k  in U");
					k = mult - 1;
				}
				else{
					k = NURBSAlgorithm.FindSpan(np, p, uv, UP);					
				}
				nq = np + r;
			}
			else{
				mult = getMultiplicity(uv, VP);
				if(uv == VP[0]){
					logger.info("set k  in V");
					k = mult - 1;
				}
				else{
					k = NURBSAlgorithm.FindSpan(mp, q, uv, VP);
				}
				mq = mp + r;
			}
			double[] UQ = new double[nq + p + 2];
			double[] VQ = new double[mq + q + 2];
			double[][][]Qw = new double[nq + 1][mq + 1][4];
			NURBSAlgorithm.SurfaceKnotIns(np, p, UP, mp, q, VP, Pw, dir, uv, k, mult, r, nq, UQ, mq, VQ, Qw);
			NURBSSurface ns = new NURBSSurface(UQ, VQ, Qw, p, q, getBoundLines(), getCornerPoints());
			return ns;
		}
		
		private static double[] insertUInKnot(int p, double u, double[] U){
			int k = NURBSAlgorithm.FindSpan(U.length - p - 2, p, u, U);
			double[] newU = new double[U.length + 1];
			for (int i = 0; i <= k; i++) {
				newU[i] = U[i];
			}
			newU[k + 1] = u;
			for (int i = k+2; i < newU.length; i++) {
				newU[i] = U[i - 1];
			}
			return newU;
		}
		
		public NURBSSurface SurfaceKnotInsertion(boolean dir, double uv){
			double[] U = getUKnotVector();
			double[] V = getVKnotVector();
			int p = getUDegree();
			int q = getVDegree();
			double[][][] Pw = getControlMesh();
			if(dir){
				double[] newU = insertUInKnot(p, uv, U);
				double[][][] newPw = new double[Pw.length + 1][Pw[0].length][4];
				int k = NURBSAlgorithm.FindSpan(U.length - p - 2, p, uv, U);
				double[] alpha = new double[p];
				for (int i = 0; i < alpha.length; i++) {
					int j = i + k - p + 1;
					alpha[i] = (uv - U[j]) / (U[j + p] - U[j]);
				}
				for (int j = 0; j < newPw[0].length; j++) {
					for (int i = 0; i <= k - p; i++) {
						newPw[i][j] = Pw[i][j];
					}
					for (int i = k - p + 1; i <= k; i++) {
						Rn.add(newPw[i][j], Rn.times(null, alpha[i - k + p - 1], Pw[i][j]), Rn.times(null, 1 - alpha[i - k + p - 1], Pw[i - 1][j]));
								
					}
					for (int i = k + 1; i < newPw.length; i++) {
						newPw[i][j] = Pw[i - 1][j];
					}
				}
				return new NURBSSurface(newU, V, newPw, p, q);
			}
			else{
				double[] newV = insertUInKnot(q, uv, V);
				double[][][] newPw = new double[Pw.length][Pw[0].length + 1][4];
				int k = NURBSAlgorithm.FindSpan(V.length - q - 2, q, uv, V);
				double[] alpha = new double[q];
				for (int i = 0; i < alpha.length; i++) {
					int j = i + k - q + 1;
					alpha[i] = (uv - V[j]) / (V[j + q] - V[j]);
				}
				for (int i = 0; i < newPw.length; i++) {
					for (int j = 0; j <= k - q; j++) {
						newPw[i][j] = Pw[i][j];
					}
					for (int j = k - q + 1; j <= k; j++) {
						Rn.add(newPw[i][j], Rn.times(null, alpha[j - k + q - 1], Pw[i][j]), Rn.times(null, 1 - alpha[j - k + q - 1], Pw[i][j - 1]));
								
					}
					for (int j = k + 1; j < newPw[0].length; j++) {
						newPw[i][j] = Pw[i][j - 1];
					}
				}
				return new NURBSSurface(U, newV, newPw, p, q);
			}
		}
		
		
		
		/**
		 * insert the knot uv r times into the knot vector of the surface ns. If dir == true into
		 *  U else into V
		 * @param dir
		 * @param uv
		 * @param r
		 * @return the same surface defined by the new knot vector and an adapted control mesh
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
		 * 
		 * @param KnotVec
		 * @param knot
		 * @return the position of the knot appearing the first time in the knot vector 
		 */
		
		public static int getFirstPositionOfKnotInKnotVector(double[] KnotVec, double knot){
			for (int i = 0; i < KnotVec.length; i++) {
				if(KnotVec[i] == knot){
					return i;
				}
			}
			return Integer.MAX_VALUE;
		}
		
		public NURBSSurface[] splitInTheMiddle(boolean dir){
			double middle;
			if(dir){
				double[] U = getUKnotVector();
				middle = U[0] + (U[U.length - 1] - U[0]) / 2.0;
			}
			else{
				double[] V = getVKnotVector();
				middle = V[0] + (V[V.length - 1] - V[0]) / 2.0;
			}
			return splitAtKnot(dir, middle);
		}
		
		/**
		 * 
		 * @param dir
		 * @param uv
		 * @return 2 parts of the surface splitted at the knot uv
		 */
		
		public NURBSSurface[] splitAtKnot(boolean dir, double uv){
			NURBSSurface[] splitSurf = new NURBSSurface[2];
			NURBSSurface nsFilled = this;
			if(dir){
				int mult = getMultiplicity(uv, U);
				int insertNumber = p - mult;
				nsFilled = SurfaceKnotInsertion(nsFilled, true, uv, insertNumber);
				double[][][] cmFilled = nsFilled.getControlMesh();
				double[] filledU = nsFilled.getUKnotVector();
				int first = getFirstPositionOfKnotInKnotVector(filledU, uv);
				double[] U1 = new double[first + p + 1];
				for (int i = 0; i < U1.length - 1; i++) {
					U1[i] = filledU[i];
				}
				U1[U1.length - 1] = uv;
				double[] U2 = new double[filledU.length - first + 1];
				for (int i = 1; i < U2.length; i++) {
					U2[i] = filledU[i + first - 1];
				}
				U2[0] = uv;
				int l = U1.length - 1;
				int k = U2.length - 1;
				double[][][] cm1 = new double[l - p][cmFilled[0].length][];
				for (int i = 0; i < cm1.length; i++) {
					for (int j = 0; j < cm1[0].length; j++) {
						cm1[i][j] = cmFilled[i][j];
					}
					
				}
				NURBSSurface ns1 = new NURBSSurface(U1, V, cm1, p, q);
				logger.info("ns1");
				logger.info(ns1.toObj());
				double[][][] cm2 = new double[k - p][cmFilled[0].length][];
			
				for (int i = l - p - 1; i < cmFilled.length; i++) {
					for (int j = 0; j < cm2[0].length; j++) {
						cm2[i - l + p + 1][j] = cmFilled[i][j];
					}
				}
				NURBSSurface ns2 = new NURBSSurface(U2, V, cm2, p, q);
				logger.info("ns2");
				logger.info(ns2.toObj());
				splitSurf[0] = ns1;
				splitSurf[1] = ns2;
				return splitSurf;
			}
			else{
				int mult = getMultiplicity(uv, V);
				int insertNumber = q - mult;
				nsFilled = SurfaceKnotInsertion(nsFilled, false, uv, insertNumber);
				double[][][] cmFilled = nsFilled.getControlMesh();
				double[] filledV = nsFilled.getVKnotVector();
				int first = getFirstPositionOfKnotInKnotVector(filledV, uv);
				double[] V1 = new double[first + q + 1];
				for (int i = 0; i < V1.length - 1; i++) {
					V1[i] = filledV[i];
				}
				V1[V1.length - 1] = uv;
				double[] V2 = new double[filledV.length - first + 1];
				for (int i = 1; i < V2.length; i++) {
					V2[i] = filledV[i + first - 1];
				}
				V2[0] = uv;
				int l = V1.length - 1;
				int k = V2.length - 1;
				double[][][] cm1 = new double[cmFilled.length][l - q][];
				for (int i = 0; i < cm1.length; i++) {
					for (int j = 0; j < cm1[0].length; j++) {
						cm1[i][j] = cmFilled[i][j];
					}
					
				}
				NURBSSurface ns1 = new NURBSSurface(U, V1, cm1, p, q);
				logger.info("ns1");
				logger.info(ns1.toObj());
				double[][][] cm2 = new double[cmFilled.length][k - q][];
				for (int i = 0; i < cm2.length; i++) {
					for (int j = l - q - 1; j < cmFilled[0].length; j++) {
						cm2[i][j - l + q + 1] = cmFilled[i][j];
					}
				}
				NURBSSurface ns2 = new NURBSSurface(U, V2, cm2, p, q);
				logger.info("ns2");
				logger.info(ns2.toObj());
				splitSurf[0] = ns1;
				splitSurf[1] = ns2;
				return splitSurf;
			}
		}
		
		public NURBSSurface[] splitAtKnot1(boolean dir, double uv){
			NURBSSurface[] splitSurf = new NURBSSurface[2];
			NURBSSurface nsFilled = this;
			if(dir){
				int mult = getMultiplicity(uv, U);
				int insertNumber = p - mult;
				for (int i = 0; i < insertNumber; i++) {
					nsFilled = nsFilled.SurfaceKnotInsertion(true, uv);
				}
				double[][][] cmFilled = nsFilled.getControlMesh();
				double[] filledU = nsFilled.getUKnotVector();
				int first = getFirstPositionOfKnotInKnotVector(filledU, uv);
				double[] U1 = new double[first + p + 1];
				for (int i = 0; i < U1.length - 1; i++) {
					U1[i] = filledU[i];
				}
				U1[U1.length - 1] = uv;
				double[] U2 = new double[filledU.length - first + 1];
				for (int i = 1; i < U2.length; i++) {
					U2[i] = filledU[i + first - 1];
				}
				U2[0] = uv;
				int l = U1.length - 1;
				int k = U2.length - 1;
				double[][][] cm1 = new double[l - p][cmFilled[0].length][];
				for (int i = 0; i < cm1.length; i++) {
					for (int j = 0; j < cm1[0].length; j++) {
						cm1[i][j] = cmFilled[i][j];
					}
					
				}
				NURBSSurface ns1 = new NURBSSurface(U1, V, cm1, p, q);
//				logger.info("ns1");
//				logger.info(ns1.toObj());
				double[][][] cm2 = new double[k - p][cmFilled[0].length][];
			
				for (int i = l - p - 1; i < cmFilled.length; i++) {
					for (int j = 0; j < cm2[0].length; j++) {
						cm2[i - l + p + 1][j] = cmFilled[i][j];
					}
				}
				NURBSSurface ns2 = new NURBSSurface(U2, V, cm2, p, q);
//				logger.info("ns2");
//				logger.info(ns2.toObj());
				splitSurf[0] = ns1;
				splitSurf[1] = ns2;
				return splitSurf;
			}
			else{
				int mult = getMultiplicity(uv, V);
				int insertNumber = q - mult;
				for (int i = 0; i < insertNumber; i++) {
					nsFilled = nsFilled.SurfaceKnotInsertion(false, uv);
				}
				double[][][] cmFilled = nsFilled.getControlMesh();
				double[] filledV = nsFilled.getVKnotVector();
				int first = getFirstPositionOfKnotInKnotVector(filledV, uv);
				double[] V1 = new double[first + q + 1];
				for (int i = 0; i < V1.length - 1; i++) {
					V1[i] = filledV[i];
				}
				V1[V1.length - 1] = uv;
				double[] V2 = new double[filledV.length - first + 1];
				for (int i = 1; i < V2.length; i++) {
					V2[i] = filledV[i + first - 1];
				}
				V2[0] = uv;
				int l = V1.length - 1;
				int k = V2.length - 1;
				double[][][] cm1 = new double[cmFilled.length][l - q][];
				for (int i = 0; i < cm1.length; i++) {
					for (int j = 0; j < cm1[0].length; j++) {
						cm1[i][j] = cmFilled[i][j];
					}
					
				}
				NURBSSurface ns1 = new NURBSSurface(U, V1, cm1, p, q);
				logger.info("ns1");
				logger.info(ns1.toObj());
				double[][][] cm2 = new double[cmFilled.length][k - q][];
				for (int i = 0; i < cm2.length; i++) {
					for (int j = l - q - 1; j < cmFilled[0].length; j++) {
						cm2[i][j - l + q + 1] = cmFilled[i][j];
					}
				}
				NURBSSurface ns2 = new NURBSSurface(U, V2, cm2, p, q);
				logger.info("ns2");
				logger.info(ns2.toObj());
				splitSurf[0] = ns1;
				splitSurf[1] = ns2;
				return splitSurf;
			}
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
		 * the surface is splitted at each interior knot
		 * @return all Bezier surfaces
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
//			logger.info("V = " + Arrays.toString(V));
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
//					logger.info("Pw.length = " + Pw.length + " UknotVector.length - p - 1 = " + (UknotVector.length - p - 1));
//					logger.info("Pw[0].length = " + Pw[0].length + " VknotVector.length - q - 1 = " + (VknotVector.length - q - 1));
					double[][][]BezierControlPoints = new double[UknotVector.length - p - 1][VknotVector.length - q - 1][4];
					for (int iB = 0; iB < BezierControlPoints.length; iB++) {
						for (int jB = 0; jB < BezierControlPoints[0].length; jB++) {
//							logger.info("q * j + jB = " + (q * j + jB)); logger.info("q = " + q);
//							logger.info("BezierSurfaces[0].length - 1 = " + (BezierSurfaces[0].length - 1) + " j = " + j);
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
		
		/**
		 * the surface is splitted at each interior knot
		 * @return all Bezier surfaces
		 */
		
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
 		 * @return 
 		 */
		
 		public LinkedList<NURBSSurface> subdivideIntoFourNewPatches(){
 			LinkedList<NURBSSurface> newPatches = new LinkedList<NURBSSurface>();
 			double uInsert = (U[U.length - 1] + U[0]) / 2.0;
 			double vInsert = (V[V.length - 1] + V[0]) / 2.0;
 			NURBSSurface nsInsert = SurfaceKnotInsertion(true, uInsert, 1);
 			nsInsert = SurfaceKnotInsertion(nsInsert, false, vInsert, 1);
 			newPatches = nsInsert.decomposeIntoBezierSurfacesList();
 			return newPatches;
 		}
 		
 		
 		public boolean isSurfaceOfRevolution(){
 			if(PointProjectionSurfaceOfRevolution.isSurfaceOfRevolutionUDir(this) || PointProjectionSurfaceOfRevolution.isSurfaceOfRevolutionVDir(this)){
 				return true;
 			}
 			return false;
 		}
		
		public double[] getClosestPoint(double[] point){
 			return PointProjectionSurface.getClosestPoint(this, point);
 		}
		
		public double[] getClosestPointDomain(double[] point){
 			return PointProjectionSurface.getClosestPointDomain(this, point);
 		}
		
		
		/**
		 * 
		 * @return
		 */
		
		public NURBSSurface interchangeUV(){
			NURBSSurface nsEnd = new NURBSSurface();
			nsEnd.setUDegree(q);
			nsEnd.setVDegree(p);
			nsEnd.setUKnotVector(V.clone());
			nsEnd.setVKnotVector(U.clone());
			double[][][] cmEnd = new double[controlMesh[0].length][controlMesh.length][];
			for (int i = 0; i < cmEnd.length; i++) {
				for (int j = 0; j < cmEnd[0].length; j++) {
					cmEnd[i][j] = controlMesh[j][i].clone();
				}
			}
			nsEnd.setControlMesh(cmEnd);
			nsEnd.setRevolutionDir();
			return nsEnd;
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
		
		public String toObj(){
			String str = new String();
			for (int i = 0; i < controlMesh.length; i++) {
				for (int j = 0; j < controlMesh[0].length; j++) {
					str = str + '\n' + "v " + controlMesh[i][j][0] / controlMesh[i][j][3] + " " + controlMesh[i][j][1] / controlMesh[i][j][3] + " " + controlMesh[i][j][2] / controlMesh[i][j][3] + " " + controlMesh[i][j][3];
				}
			}
			str =  str + '\n' + "deg " + q + " " + p;
			str =  str + '\n' + "surf " + V[0] + " " + V[V.length - 1] + " " + U[0] + " " + U[U.length - 1];
			int k = 0;
			for (int i = 0; i < controlMesh.length; i++) {
				for (int j = 0; j < controlMesh[0].length; j++) {
					k++;
					str =  str + " " + k;
				}
			}
			str =  str + '\n' + "parm u";
			for (int i = 0; i < V.length; i++) {
				str =  str + " " + V[i];
			}
			str =  str + '\n' + "parm v";
			for (int i = 0; i < U.length; i++) {
				str =  str + " " + U[i];
			}
			
			str =  str + '\n' + "end";
			return str;
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
			str = str + '\n' + "boundary lines: " + boundaryToString() + '\n';
			
			if(revDir == null){
				str = str + "no surface of revolution " + '\n';
			}else{
				str = str + "surface of revolution ";
				str = str + '\n' + revDir + '\n';
			}
			closDir = getClosingDir();
			if(closDir == ClosingDir.nonClosed){
				str = str + "surface is nonclosed ";
			}
			else{
				str = str + "closed surface";
				str = str + '\n' + closDir;
			}
			
			return str;
		}
		
		public String toReadableInputString(){
			String str = new String();
			str = str + "double[] U = {";
			for (int i = 0; i < U.length; i++) {
				if(i == 0){
					str = str  + U[i] + ", ";
				}
				else if(i == U.length - 1){
					str = str + U[i] + "};" + '\n';
				}
				else{
					str = str + U[i] + ", ";
				}
			}
			
			str = str + "double[] V = {";
			for (int i = 0; i < V.length - 1; i++) {
				str = str + V[i] + ", ";
			}
			str = str + V[V.length - 1] + "};" + '\n';
			str = str + "int p = " + p + ";" + '\n';
			str = str + "int q = " + q + ";" + '\n';
			str = str + "double[][][] controlMesh = " + '\n';
			for (int i = 0; i < controlMesh.length; i++) {
				for (int j = 0; j < controlMesh[0].length; j++) {
					
					for (int k = 0; k < controlMesh[0][0].length; k++) {
						if(i == 0 && j == 0 && k == 0){
							str = str + "{{{" + controlMesh[i][j][k] + ", ";
						}
						else if(i == controlMesh.length - 1 && j == controlMesh[0].length - 1 && k == controlMesh[0][0].length - 1){
							str = str + controlMesh[i][j][k] + "}}};";
						}
						else if(j == 0 && k == 0){
							str = str + "{{" + controlMesh[i][j][k] + ", ";
						}
						else if(j == controlMesh[0].length - 1 && k == controlMesh[0][0].length - 1){
							str = str + controlMesh[i][j][k] + "}}," + '\n';
						}
						else if(k == 0){
							str = str + "{" + controlMesh[i][j][k] + ", ";
						}
						else if(k == controlMesh[0][0].length - 1){
							str = str + controlMesh[i][j][k] + "}, ";
						}
						else{
							str = str + controlMesh[i][j][k] + ", ";
						}
					}
				}
				
			}
			return str;
			
		}
		
		public List<double[]> getBoundaryVerticesUV() {
			List<double[]> boundaryVerts = new LinkedList<double[]>();
			double[] boundVert1 = new double[2];
			boundVert1[0] = U[0];
			boundVert1[1] = V[0];
			double[] boundVert2 = new double[2];
			boundVert2[0] = U[U.length - 1];
			boundVert2[1] = V[0];
			double[] boundVert3 = new double[2];
			boundVert3[0] = U[U.length - 1];
			boundVert3[1] = V[V.length - 1];
			double[] boundVert4 = new double[2];
			boundVert4[0] = U[0];
			boundVert4[1] = V[V.length - 1];
			boundaryVerts.add(boundVert1);
			boundaryVerts.add(boundVert2);
			boundaryVerts.add(boundVert3);
			boundaryVerts.add(boundVert4);
			return boundaryVerts;
		}
		
		public List<LineSegment> getCompleteDomainBoundarySegments() {
			List<LineSegment> boundarySegments = new LinkedList<LineSegment>();
			List<double[]> boundaryVertices = getBoundaryVerticesUV();
			double[] 
					boundVert1 = boundaryVertices.get(0),
					boundVert2 = boundaryVertices.get(1),
					boundVert3 = boundaryVertices.get(2),
					boundVert4 = boundaryVertices.get(3);
					
			double[][] seg1 = new double[2][2];
			seg1[0] = boundVert1;
			seg1[1] = boundVert2;
			LineSegment b1 = new LineSegment(seg1, 1, 1);
			double[][] seg2 = new double[2][2];
			seg2[0] = boundVert2;
			seg2[1] = boundVert3;
			LineSegment b2 = new LineSegment(seg2, 1, 2);
			double[][] seg3 = new double[2][2];
			seg3[0] = boundVert3;
			seg3[1] = boundVert4;
			LineSegment b3 = new LineSegment(seg3, 1, 3);
			double[][] seg4 = new double[2][2];
			seg4[0] = boundVert4;
			seg4[1] = boundVert1;
			LineSegment b4 = new LineSegment(seg4, 1, 4);
			boundarySegments.add(b1);
			boundarySegments.add(b2);
			boundarySegments.add(b3);
			boundarySegments.add(b4);
			return boundarySegments;
		}

		public List<LineSegment> getBoundarySegments() {
			List<LineSegment> boundarySegments = new LinkedList<LineSegment>();
			List<double[]> boundaryVertices = getBoundaryVerticesUV();
			double[] 
					boundVert1 = boundaryVertices.get(0),
					boundVert2 = boundaryVertices.get(1),
					boundVert3 = boundaryVertices.get(2),
					boundVert4 = boundaryVertices.get(3);
					
			double[][] seg1 = new double[2][2];
			seg1[0] = boundVert1;
			seg1[1] = boundVert2;
			LineSegment b1 = new LineSegment(seg1, 1, 1);
			double[][] seg2 = new double[2][2];
			seg2[0] = boundVert2;
			seg2[1] = boundVert3;
			LineSegment b2 = new LineSegment(seg2, 1, 2);
			double[][] seg3 = new double[2][2];
			seg3[0] = boundVert3;
			seg3[1] = boundVert4;
			LineSegment b3 = new LineSegment(seg3, 1, 3);
			double[][] seg4 = new double[2][2];
			seg4[0] = boundVert4;
			seg4[1] = boundVert1;
			LineSegment b4 = new LineSegment(seg4, 1, 4);
			if(getClosingDir() == ClosingDir.nonClosed){
				boundarySegments.add(b1);
				boundarySegments.add(b2);
				boundarySegments.add(b3);
				boundarySegments.add(b4);
			}
			else if(getClosingDir() == ClosingDir.uClosed){
				boundarySegments.add(b1);
				boundarySegments.add(b3);
			}
			else if(getClosingDir() == ClosingDir.vClosed){
				boundarySegments.add(b2);
				boundarySegments.add(b4);
			}
			return boundarySegments;
		}
		
		public LinkedList<Double> determineClosedBoundaryValues(){
			LinkedList<Double> closedBoundaryValues = new LinkedList<Double>();
			double[] U = getUKnotVector();
			double[] V = getVKnotVector();
			if(getClosingDir() == ClosingDir.vClosed){
				double v0 = V[0];
				double vn = V[V.length - 1];
				closedBoundaryValues.add(v0);
				closedBoundaryValues.add(vn);
			}
			else{
				double u0 = U[0];
				double um = U[U.length - 1];
				closedBoundaryValues.add(u0);
				closedBoundaryValues.add(um);
			}
			return closedBoundaryValues;
		}
		
		
		
		public LinkedList<Double> determineBoundaryValues(){
			LinkedList<Double> boundaryValues = new LinkedList<Double>();
			double[] U = getUKnotVector();
			double[] V = getVKnotVector();
			if(getClosingDir() == ClosingDir.uClosed){
				double v0 = V[0];
				double vn = V[V.length - 1];
				boundaryValues.add(v0);
				boundaryValues.add(vn);
			}
			else if(getClosingDir() == ClosingDir.vClosed){
				double u0 = U[0];
				double um = U[U.length - 1];
				boundaryValues.add(u0);
				boundaryValues.add(um);
			}
			else{
				double u0 = U[0];
				double um = U[U.length - 1];
				double v0 = V[0];
				double vn = V[V.length - 1];
				boundaryValues.add(u0);
				boundaryValues.add(um);
				boundaryValues.add(v0);
				boundaryValues.add(vn);
			}
			return boundaryValues;
		}
		
		
		public double[] determineGluedBoundary(){
			double[] gluedBoundValues = new double[2];
			if(getClosingDir() == ClosingDir.nonClosed){
				return null;
			}
			else{
				if(getClosingDir() == ClosingDir.uClosed){
					gluedBoundValues[0] = U[0];
					gluedBoundValues[1] = U[U.length - 1];
				}
				else{
					gluedBoundValues[0] = V[0];
					gluedBoundValues[1] = V[V.length - 1];
				}
			}
			return gluedBoundValues;
		}
		
		
		
		
	//	TODO: Why does this method need hds and as?
	public LinkedList<double[]> findUmbilics(VHDS hds, AdapterSet as) {
		
		VectorFieldMapAdapter linefield = new VectorFieldMapAdapter();
		FlatIndexFormAdapter indexAdapter= new FlatIndexFormAdapter(); 
	
		double u0 = U[0];
		double u1 = U[U.length - 1];
		double v0 = V[0];
		double v1 = V[V.length - 1];
		for (VVertex v : hds.getVertices()) {
			double[] NurbsuvCoordinate = as.get(NurbsUVCoordinate.class, v,	double[].class);
			double uCoord = NurbsuvCoordinate[0];
			double vCoord = NurbsuvCoordinate[1];
			if (uCoord < u0 || uCoord > u1) {
				logger.info("uCoord is out of domain " + uCoord);
			}
			if (vCoord < v0 || vCoord > v1) {
				logger.info("uCoord is out of domain " + vCoord);
			}
			double[] p = {uCoord, vCoord};
			CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(this, p);
			double[] vector = ci.getPrincipalDirections()[0];
			linefield.set(v, vector, as);
		}

		as.add(indexAdapter);
		as.add(linefield);
		LinkedList<Double> umbFaces = new LinkedList<Double>();
		LinkedList<VFace> umbilicFaces = new LinkedList<VFace>();
		for (VFace f : hds.getFaces()) {
			Double result = indexAdapter.get(f, as);
			if (Math.abs(Math.abs(result) - 1) < 0.01
					|| Math.abs(Math.abs(result) - 2) < 0.01) {
				umbFaces.add(result);
				umbilicFaces.add(f);
			}
		}

		UmbilicFunction fun = new UmbilicFunction(this);
		
		LinkedList<double[]> possibleUmbs = new LinkedList<double[]>();
		for (VFace f : umbilicFaces) {
			logger.info("Faceindex: " + f.getIndex());
			VVertex v = f.getBoundaryEdge().getStartVertex();
			double[] start = as.get(NurbsUVCoordinate.class, v, double[].class);
			double[][] xi = computeXi(start);
			double value = NelderMead.search(start, xi, 1E-12, fun, 100,
					new Info());
			logger.info("\n");
			logger.info("NM Value " + value);
			logger.info("NM Pos " + Arrays.toString(start));
			possibleUmbs.add(start);
		}

		double epsilon = 0.00001;
		HashMap<double[], List<double[]>> near = new HashMap<double[], List<double[]>>();
		for (double[] umb1 : possibleUmbs) {
			List<double[]> nearUmb1 = new LinkedList<double[]>();
			for (double[] umb2 : possibleUmbs) {
				if (umb1 != umb2) {
					if (Rn.euclideanDistance(umb1, umb2) < epsilon) {
						nearUmb1.add(umb2);
					}
				}
			}
			near.put(umb1, nearUmb1);
		}
		List<double[]> allNearUmb = new LinkedList<double[]>();
		List<double[]> allNearFirstUmb = new LinkedList<double[]>();
		for (double[] umb : possibleUmbs) {
			if (near.containsKey(umb)) {
				allNearUmb.add(umb);
				if (!allNearFirstUmb.contains(umb)) {
					for (double[] u : near.get(umb)) {
						allNearFirstUmb.add(u);
					}
				}
			}
		}
		possibleUmbs.removeAll(allNearFirstUmb);
		return possibleUmbs;
	}
	
	private double[][] computeXi(double[] p) {
		double[][] xi = new double[2][];
		double[] x1 = new double[2];
		double[] x2 = new double[2];
		double pu = p[0];
		double pv = p[1];
		double uend = U[U.length - 1];
		double vend = V[V.length - 1];
		if(pu + 0.05 < uend){
			x1[0] = 0.001; 
		}
		else{
			x1[0] = -0.001;
		}
		x1[1] = 0;
		if(pv + 0.05 < vend){
			x2[1] = 0.001;
		}
		else{
			x2[1] = -0.001;
		}
		x2[0] = 0;
		xi[0] = x1;
		xi[1] = x2;
		return xi;
	}
	
	private class UmbilicFunction implements RealFunctionOfSeveralVariables {
		
		private NURBSSurface surface = null;
		
		public UmbilicFunction(NURBSSurface ns) {
			surface = ns;
		}
		
		@Override
		public int getNumberOfVariables() {
			return 2;
		}

		@Override
		public double eval(double[] p) {
			double u0 = U[0];
			double u1 = U[U.length - 1];
			double v0 = V[0];
			double v1 = V[V.length - 1];
			if (p[0] < u0 || p[0] > u1) {
				// logger.info("Nelder-Mead out of domain");
				// logger.info("p[0]: " + p[0]);
				return 10000;
			}
			if (p[1] < v0 || p[1] > v1) {
				// logger.info("Nelder-Mead out of domain");
				// logger.info("p[1]: " + p[1]);
				return 10000;
			}
			// if(p[0] < u0){
			// p[0] = u0;
			// }
			// else if(p[0] > u1){
			// p[0] = u1;
			// }
			// if(p[1] < v0){
			// p[1] = v0;
			// }
			// else if(p[1] > v1){
			// p[1] = v1;
			// }
			CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(surface, p);
			double H = ci.getMeanCurvature();
			double K = ci.getGaussCurvature();
			return Math.abs(H * H - K);
		}
	};

	
	
	public Geometry createNurbsMesh(int u, int v) {
		NURBSSurfaceFactory qmf = new NURBSSurfaceFactory();
		qmf.setGenerateVertexNormals(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setULineCount(u);
		qmf.setVLineCount(v);
		qmf.setSurface(this);
		qmf.update();
		return qmf.getGeometry();
	}
	
	public static void main(String[] args){
		double[] U = {0,0,0,2,2,2};
		double u = 1;
		int p = 2;
		double[] newU = insertUInKnot(p, u, U);
		logger.info(Arrays.toString(newU));
	}
}
