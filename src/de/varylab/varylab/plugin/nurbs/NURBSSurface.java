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
		
		public LinkedList<NURBSTreeNode> decomposeIntoBezierTreeNodeList(){
			LinkedList<NURBSTreeNode> treeNodeList = new LinkedList<NURBSTreeNode>();
			NURBSSurface[][] Bezier = decomposeIntoBezierSurfaces();
			for (int i = 0; i < Bezier.length; i++) {
				for (int j = 0; j < Bezier[0].length; j++) {
					NURBSTreeNode ntn = new NURBSTreeNode(Bezier[i][j]);
					treeNodeList.add(ntn);
				}
			}
			return treeNodeList;
		}
		
		
		
		public static double[] get3DPoint(double[] fourDPoint){
			double[] threeDPoint = new double[3];
			threeDPoint[0] = fourDPoint[0] / fourDPoint[3];
			threeDPoint[1] = fourDPoint[1] / fourDPoint[3];
			threeDPoint[2] = fourDPoint[2] / fourDPoint[3];
			return threeDPoint;
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
 		
 		public NURBSSurface[] subdivideIntoFourNewPatchestoArray(){
 			NURBSSurface[] newPatches = new NURBSSurface[4];
 			LinkedList<NURBSSurface> list = subdivideIntoFourNewPatches();
 			for (int i = 0; i < newPatches.length; i++) {
				newPatches[i] = list.get(i);
			}
 			return newPatches;
 		}
 		
 		
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
 		
 		
 		private static NURBSTreeNode getClosestPatch(LinkedList<NURBSTreeNode> treeNodeList, double[] point){
 			double[] p = get3DPoint(point);
 			double minDist = Double.MAX_VALUE;
 			NURBSTreeNode closestPatch = new NURBSTreeNode();
 			for (NURBSTreeNode ntn : treeNodeList) {
 				double[] minSurfPoint = get3DPoint(ntn.getNs().getMinControlPoint(point));
				if(minDist > Rn.euclideanDistance(p, minSurfPoint)){
					minDist = Rn.euclideanDistance(p, minSurfPoint);
					closestPatch = ntn;
				}
			}
 			return closestPatch;
 		}
 		
 		
 		private static LinkedList<NURBSTreeNode> getPossiblePatches(LinkedList<NURBSTreeNode> treeNodeList, double[] point){
 			double[] p = get3DPoint(point);
 			NURBSTreeNode closestPatch = getClosestPatch(treeNodeList, point);
 			double[] maxPoint = get3DPoint(closestPatch.getNs().getMaxControlPoint(point));
 			double closestMaxDistance = Rn.euclideanDistance(p, maxPoint);
 			LinkedList<NURBSTreeNode> possiblePatches = new LinkedList<NURBSTreeNode>();
 			for (NURBSTreeNode ntn : treeNodeList) {
				 if(!ntn.getNs().isImpossiblePatch(point, closestMaxDistance)){
					 possiblePatches.add(ntn);
				 }
			}
 			return possiblePatches ;
 		}
 	
 		
		public double[] getClosestPoint(double[] point, NURBSTree nt){
 			double[] p = new double[3];
			double dist = Double.MAX_VALUE;
 			if(nt == null){
 				nt = new NURBSTree(decomposeIntoBezierSurfacesList());
 			}
 			LinkedList<NURBSTreeNode> possiblePatches = nt.getDummy().getBezierList();
 			for (int i = 0; i < 12; i++) {
 				if(i > 1 && i < 5){
 					double uStart = 0.;
 					double vStart = 0.;
 					for (NURBSTreeNode ntn : possiblePatches) {
 						double[] U = ntn.getNs().getUKnotVector();
 						double[] V = ntn.getNs().getVKnotVector();
 						double u = (U[0] + U[U.length - 1]) / 2;
 						double v = (V[0] + V[V.length - 1]) / 2;
 						double[] homogSurfPoint = ntn.getNs().getSurfacePoint(u, v);
 						double[] surfPoint = get3DPoint(homogSurfPoint);
 						if(dist > Rn.euclideanDistance(surfPoint, point)){
 							dist = Rn.euclideanDistance(surfPoint, point);
 							uStart = u;
 							vStart = v;
 						}
 					}
 					double[] result = newtonMethod(point, 0.00000000001,uStart, vStart);
 					if(result != null){
 						return result;
 					}
 				}
 				LinkedList<NURBSTreeNode> subdividedPatches = new LinkedList<NURBSTreeNode>();
 				possiblePatches = getPossiblePatches(possiblePatches, point);
 				for (NURBSTreeNode ntn : possiblePatches) {
					subdividedPatches.addAll(ntn.getAllChildNodes());
				}
 				possiblePatches = subdividedPatches;
 			}
 //			System.out.println(nt.toString());
 			
 			
 			for (NURBSTreeNode ntn : possiblePatches) {
				double[] U = ntn.getNs().getUKnotVector();
				double[] V = ntn.getNs().getVKnotVector();
				double u = (U[0] + U[U.length - 1]) / 2;
				double v = (V[0] + V[V.length - 1]) / 2;
				double[] homogSurfPoint = ntn.getNs().getSurfacePoint(u, v);
				double[] surfPoint = get3DPoint(homogSurfPoint);
				if(dist > Rn.euclideanDistance(surfPoint, point)){
					dist = Rn.euclideanDistance(surfPoint, point);
					p = homogSurfPoint;
				}
			}
 			return p;
 		}
 		

		private double[] newtonMethod(double[] P, double eps, double u, double v){
			CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(this, u, v);
			double[] S = getSurfacePoint(u, v);
			double[] S3D = get3DPoint(getSurfacePoint(u, v));
			double[] P3D = get3DPoint(P);
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
				u = deltaU + u;
				v = deltaV + v;
				boolean notInPatch = false;
				if(u < U[0] || u > U[U.length - 1] || v < V[0] || v > V[V.length - 1]){
					notInPatch = true;
				}
				if(notInPatch){
					return null;
				}
				ci = NURBSCurvatureUtility.curvatureAndDirections(this, u, v);
				S = getSurfacePoint(u, v);
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
//				}
			}
			if(f > eps || g > eps){
				System.out.println("f " + f + " g " + g);
			}
			return S;
		}
		
//		private double[] newtonMethod(double[] P, double eps, double u, double v){
//			CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(this, u, v);
//			double[] S = getSurfacePoint(u, v);
//			double[] S3D = get3DPoint(getSurfacePoint(u, v));
//			double[] P3D = get3DPoint(P);
//			double[] SminusP = Rn.subtract(null, S3D, P3D);
//			double euclNormSminusP = Rn.euclideanNorm(SminusP);
//			double[] r = Rn.times(null,1 / euclNormSminusP, SminusP);
//			double[] Su = ci.getSu();
//			double[] Sv = ci.getSv();
//			double[] Suu = ci.getSuu();
//			double[] Suv = ci.getSuv();
//			double[] Svv = ci.getSvv();
//			double[]SuTilde = Rn.times(null, 1 / Rn.euclideanNorm(Su), Su);
//			double[]SvTilde = Rn.times(null, 1 / Rn.euclideanNorm(Sv), Sv);
//			double f = Rn.innerProduct(r, SuTilde);
//			double g = Rn.innerProduct(r, SvTilde);
//			double[] ru = Rn.times(null, 1 / (euclNormSminusP * euclNormSminusP), Rn.subtract(null, Rn.times(null, euclNormSminusP, Su), Rn.times(null, 2 * Rn.innerProduct(Su, SminusP)/ euclNormSminusP, S)));
//			double[] rv = Rn.times(null, 1 / (euclNormSminusP * euclNormSminusP), Rn.subtract(null, Rn.times(null, euclNormSminusP, Sv), Rn.times(null, 2 * Rn.innerProduct(Sv, SminusP)/ euclNormSminusP, S)));
//			double[]SuuTilde = Rn.times(null, 1 / Rn.euclideanNormSquared(Su), Rn.subtract(null, Rn.times(null, Rn.euclideanNorm(Su), Suu), Rn.times(null, 2 * Rn.innerProduct(Suu, Su) / Rn.euclideanNorm(Su), Su)));
//			double[]SuvTilde = Rn.times(null, 1 / Rn.euclideanNormSquared(Su), Rn.subtract(null, Rn.times(null, Rn.euclideanNorm(Su), Suv), Rn.times(null, 2 * Rn.innerProduct(Suv, Su) / Rn.euclideanNorm(Su), Su)));
//			double[]SvvTilde = Rn.times(null, 1 / Rn.euclideanNormSquared(Sv), Rn.subtract(null, Rn.times(null, Rn.euclideanNorm(Sv), Svv), Rn.times(null, 2 * Rn.innerProduct(Svv, Sv) / Rn.euclideanNorm(Sv), Sv)));
//			double fu = Rn.innerProduct(ru, SuTilde) + Rn.innerProduct(r, SuuTilde);
//			double fv = Rn.innerProduct(rv, SuTilde) + Rn.innerProduct(r, SuvTilde);
//			double gv = Rn.innerProduct(rv, SvTilde) + Rn.innerProduct(r, SvvTilde);
//			double deltaU = Double.MAX_VALUE;
//			double deltaV = Double.MAX_VALUE;
//			for(int i = 0; i < 15; i++){
//				if(false){
////				if(f < eps * eps && g < eps * eps && deltaU < eps * eps && deltaV < eps * eps){	
//					System.out.println("terminiert nach " + i + " Schritten");
//					return S;
//					
//				}
//				else{
//				deltaV = ((-g * fu + f * fv) /(fu * gv - fv * fv));
//				deltaU = -((f + (fv * deltaV)) / fu);
//				u = deltaU + u;
//				v = deltaV + v;
//				boolean notInPatch = false;
//				if(u < U[0] || u > U[U.length - 1] || v < V[0] || v > V[V.length - 1]){
//					notInPatch = true;
//				}
//				if(notInPatch){
//					return null;
//				}
//				ci = NURBSCurvatureUtility.curvatureAndDirections(this, u, v);
//				S = getSurfacePoint(u, v);
//				S3D = get3DPoint(getSurfacePoint(u, v));
//				r = Rn.subtract(null, S3D, P);
//				Su = ci.getSu();
//				Sv = ci.getSv();
//				Suu = ci.getSuu();
//				Suv = ci.getSuv();
//				Svv = ci.getSvv();
//				f = Rn.innerProduct(r, Su);
//				g = Rn.innerProduct(r, Sv);
//				fu = Rn.innerProduct(Su, Su) + Rn.innerProduct(r, Suu);
//				fv = Rn.innerProduct(Su, Sv) + Rn.innerProduct(r, Suv);
//				gv = Rn.innerProduct(Sv, Sv) + Rn.innerProduct(r, Svv);
//				}
//			}
//			if(f > eps || g > eps){
//				System.out.println("f " + f + " g " + g);
//			}
//			return S;
//		}
	
	
}
