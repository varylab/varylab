package de.varylab.varylab.plugin.nurbs.math;

import java.util.Arrays;
import java.util.LinkedList;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.BoundaryLines;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.CornerPoints;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;
import de.varylab.varylab.utilities.MathUtility;

/**
 * 
 * @author seidel <br>
 * 
 * This class provides methods for the computation of the point projection onto a NURBS surface<br/>
 */

public class PointProjection {
		
	
	private static CornerPoints isP00(double[][][] P, double[] p, double eps){
		double[] P00 = P[0][0];
		double[] pp00 = Rn.subtract(null, p, P00);
		double[] cMPoint;
		boolean b00 = true;
		for (int i = 0; i < P.length; i++) {
			for (int j = 0; j < P[0].length; j++) {
				cMPoint = Rn.subtract(null, P00, P[i][j]);
				if(!(i == 0 && j == 0) && Rn.innerProduct(Rn.normalize(null, pp00), Rn.normalize(null, cMPoint)) < eps){
					b00 = false;
				}
			}
		}
		if(b00){
			return CornerPoints.P00;
		}
		return null;
	}
	
	private static CornerPoints isP0n(double[][][] P, double[] p, double eps){
		double[] P0n = P[0][P[0].length - 1];
		double[] pp0n = Rn.subtract(null, p, P0n);
		double[] cMPoint;
		boolean b0n = true;
		for (int i = 0; i < P.length; i++) {
			for (int j = 0; j < P[0].length; j++) {
				cMPoint = Rn.subtract(null, P0n, P[i][j]);
				if(!(i == 0 && j == P[0].length - 1) && Rn.innerProduct(Rn.normalize(null, pp0n), Rn.normalize(null, cMPoint)) < eps){
					b0n = false;
				}
			}
		}
		if(b0n){
			return CornerPoints.P0n;
		}
		return null;
	}
	
	private static CornerPoints isPm0(double[][][] P, double[] p, double eps){
		double[] Pm0 = P[P.length - 1][0];
		double[] ppm0 = Rn.subtract(null, p, Pm0);
		double[] cMPoint;
		boolean bm0 = true;
		for (int i = 0; i < P.length; i++) {
			for (int j = 0; j < P[0].length; j++) {
				cMPoint = Rn.subtract(null, Pm0, P[i][j]);
				if(!(i == P.length - 1 && j == 0) && Rn.innerProduct(Rn.normalize(null, ppm0), Rn.normalize(null, cMPoint)) < eps){
					bm0 = false;
				}
			}
		}
		if(bm0){
			return CornerPoints.Pm0;
		}
		return null;
	}
	
	private static CornerPoints isPmn(double[][][] P, double[] p, double eps){
		double[] Pmn = P[P.length - 1][P[0].length - 1];
		double[] ppmn = Rn.subtract(null, p, Pmn);
		double[] cMPoint;
		boolean bmn = true;
		for (int i = 0; i < P.length; i++) {
			for (int j = 0; j < P[0].length; j++) {
				cMPoint = Rn.subtract(null, Pmn, P[i][j]);
				if(!(i == P.length - 1 && j == P[0].length - 1) && Rn.innerProduct(Rn.normalize(null, ppmn), Rn.normalize(null, cMPoint)) < eps){
					bmn = false;
				}
			}
		}
		if(bmn){
			return CornerPoints.Pmn;
		}
		return null;
	}
	
	/**
	 * 
	 * @param ns
	 * @param point is a point in the 4 dim space
	 * @return
	 */
	
	
	private static CornerPoints isOnCornerPoint(NURBSSurface ns, double[] p, double eps){
		double[][][] P = MathUtility.get3DControlmesh(ns.getControlMesh());
		if(isP00(P,p,eps) != null){
			return isP00(P,p,eps);
		}
		if(isP0n(P,p,eps) != null){
			return isP0n(P,p,eps);
		} 
		if(isPm0(P,p,eps) != null){
			return isPm0(P,p,eps);
		} 
		else if(isPmn(P,p,eps) != null){
			return isPmn(P,p,eps);
		}
		return null;
	}
	
	private static double[] Dir(double[] Pi, double[] Pj){
		double[] dir = new double[3];
		dir[0] = Pi[3] * Pj[0] - Pj[3] * Pi[0];
		dir[1] = Pi[3] * Pj[1] - Pj[3] * Pi[1];
		dir[2] = Pi[3] * Pj[2] - Pj[3] * Pi[2];
		return dir; 
	}
	
	/**
	 * this is an estimation for the tangent cone in u direction
	 * @param ns
	 * @return tangent cone Tu
	 */
	 
	
	private static double[][][] getTu(NURBSSurface ns){
		double[][][]Pw = ns.getControlMesh();
		int m = Pw.length - 1;
		int n = Pw[0].length - 1;
		double[][][]Tu = new double[m][2 * n + 1][3];
		for (int i = 0; i < Tu.length; i++) {
			for (int l = 0; l < Tu[0].length; l++) {
				for (int j = Math.max(0, l - n); j <= Math.min(l, n); j++) {
					Rn.add(Tu[i][l], Tu[i][l], Rn.times(null, MathUtility.binomCoeff(n, j) * MathUtility.binomCoeff(n, l - j),Dir(Pw[i][j],Pw[i + 1][l - j])));
				}
			}
		}
		return Tu;
	}
		
	private static double[][][] getTv(NURBSSurface ns){
		double[][][]Pw = ns.getControlMesh();
		int m = Pw.length - 1;
		int n = Pw[0].length - 1;
		double[][][]Tv = new double[2 * m + 1][n][3];
		for (int k = 0; k < Tv.length; k++) {
			for (int j = 0; j < Tv[0].length; j++) {
				for (int i = Math.max(0, k - m); i <= Math.min(k, m); i++) {
					Rn.add(Tv[k][j], Tv[k][j], Rn.times(null, MathUtility.binomCoeff(m, i) * MathUtility.binomCoeff(m, k - i),Dir(Pw[i][j],Pw[k - i][j + 1])));
				}
			}
		}
		return Tv;	
	}
	
	
	private static BoundaryLines isOnBoundaryCurve(NURBSSurface ns, double[] p, double eps){
		double[][][]Pw = ns.getControlMesh();
		double[][][]P = MathUtility.get3DControlmesh(Pw);
		int m = Pw.length;
		int n = Pw[0].length;
		boolean u0 = true;
		boolean um = true;
		boolean v0 = true;
		boolean vn = true;
		//Tu
		double[][][]Tu = getTu(ns);
		for (int j = 0; j < n; j++){
			double[] diff = Rn.subtract(null, P[0][j], p);
			for (int k = 0; k < Tu.length; k++) {
				for (int l = 0; l < Tu[0].length; l++) {
					if(Rn.innerProduct(diff, Tu[k][l]) <= eps){
						u0 = false;
					}
				}
			}
		}
		if(u0){
			return BoundaryLines.u0;
		}
		//TminusU
		for (int j = 0; j < n; j++){
			double[] diff = Rn.subtract(null, p ,P[m - 1][j]);
			for (int k = 0; k < Tu.length; k++) {
				for (int l = 0; l < Tu[0].length; l++) {
					if(Rn.innerProduct(diff, Tu[k][l]) <= eps){
						um = false;
					}
				}
			}
		}
		if(um){
			return BoundaryLines.um;
		}
		// Tv
		double[][][]Tv = getTv(ns);
		for (int i = 0; i < m; i++) {
			double[] diff = Rn.subtract(null, P[i][0], p);
			for (int k = 0; k < Tv.length; k++) {
				for (int l = 0; l < Tv[0].length; l++) {
					if(Rn.innerProduct(diff, Tv[k][l]) <= eps){
						v0 = false;
					}
				}
			}
		}
		if(v0){
			return BoundaryLines.v0;
		}
		//TminusV
		for (int i = 0; i < m; i++){
			double[] diff = Rn.subtract(null, p, P[i][n - 1]);
			for (int k = 0; k < Tv.length; k++) {
				for (int l = 0; l < Tv[0].length; l++) {
					if(Rn.innerProduct(diff, Tv[k][l]) <= eps){
						vn = false;
					}
				}
			}
		}
		if(vn){
			return BoundaryLines.vn;
		}
		return null;
	}
	
	/**
	 * 
	 * @param p a 3D point
	 * @param P a 3D control mesh
	 * @return min distance
	 */
	
	private static double getMinDist(double[] p, double[][][] P){
		double dist = Double.MAX_VALUE;
		for (int i = 0; i < P.length; i++) {
			for (int j = 0; j < P[0].length; j++) {
				if(dist > Rn.euclideanDistance(p, P[i][j])){
					dist = Rn.euclideanDistance(p, P[i][j]);
				}
			}
		}
		return dist;
	}
	
	/**
	 * 
	 * @param p a 3D point
	 * @param P a 3D control mesh
	 * @return max distance
	 */

	private static double getMaxDist(double[] p, double[][][] P){
		double dist = Double.MIN_VALUE;
		for (int i = 0; i < P.length; i++) {
			for (int j = 0; j < P[0].length; j++) {
				if(dist < Rn.euclideanDistance(p, P[i][j])){
					dist = Rn.euclideanDistance(p, P[i][j]);
				}
			}
		}
		return dist;
	}
	
	/**
	 * @param surfList: list of NURBS surfaces
	 * @param p: a 3D point
	 * @return closest mesh in 3D coords
	 */
	
	private static double[][][] getClosestPatch(LinkedList<NURBSSurface> surfList, double[] p){
		double minDist = Double.MAX_VALUE;
		NURBSSurface closestPatch = new NURBSSurface();
		for (NURBSSurface ns : surfList) {
			double dist = getMinDist(p, MathUtility.get3DControlmesh(ns.getControlMesh()));
			if(minDist > dist){
				minDist = dist;
				closestPatch = ns;
			}
		}
		return MathUtility.get3DControlmesh(closestPatch.getControlMesh());
	}
	
	/**
	 * 
	 * @param p 3D point
	 * @param P 3D control mesh
	 * @param closestMaxDistance (the maximal distance between p and all points of the closest control mesh)
	 * @return
	 */

	private static boolean isPossiblePatchControlMesh(double[] p, double[][][] P, double closestMaxDistance){
		double dist = getMinDist(p, P);
		if(dist < closestMaxDistance){
				return true;
		}
		return false;
	}
	
	private static boolean isPossiblePatchBoundary(NURBSSurface ns, double[] p, double eps){
		if(isOnBoundaryCurve(ns, p, eps) == null || ns.getBoundLines().contains(isOnBoundaryCurve(ns, p, eps))){
			return true;
		}
		return false;	
	}
	
	private static boolean isPossiblePatchCornerPoint(NURBSSurface ns, double[] p, double eps){
		if(isOnCornerPoint(ns, p, eps) == null || ns.getCornerPoints().contains(isOnCornerPoint(ns, p, eps))){
			return true;
		}
		return false;
	}
	
	
	public static LinkedList<NURBSSurface> getPossiblePatches(LinkedList<NURBSSurface> surfList, double[] p){
		double[][][] closestMesh = getClosestPatch(surfList, p);
//		System.out.println("POINT");
//		System.out.println(Arrays.toString(p));
//		System.out.println();
		double eps = 0.000001;
		double closestMaxDistance = getMaxDist(p, closestMesh);
		LinkedList<NURBSSurface> possiblePatches = new LinkedList<NURBSSurface>();
		for (NURBSSurface ns : surfList) {
//			boolean bound = false;
//			boolean mesh = false;
			double[][][] cm = MathUtility.get3DControlmesh(ns.getControlMesh());
//			if(isPossiblePatchBoundary(ns, p, eps) && isPossiblePatchCornerPoint(ns, p, eps)){
//				possiblePatches.add(ns);
//			}
//			if(isPossiblePatchControlMesh(p, cm, closestMaxDistance)){
//				possiblePatches.add(ns);
//			}
//			if(isPossiblePatchCornerPoint(ns, p)){
//				possiblePatches.add(ns);
//			}
			if(isPossiblePatchControlMesh(p, cm, closestMaxDistance) && isPossiblePatchCornerPoint(ns, p, eps)){
				possiblePatches.add(ns);
					}
//			if(isPossiblePatchBoundary(ns, p, eps) && isPossiblePatchCornerPoint(ns, p, eps) && isPossiblePatchControlMesh(p, cm, closestMaxDistance)){
//				possiblePatches.add(ns);
//			}
//			if(isPossiblePatchCornerPoint(ns, p, eps) && isPossiblePatchControlMesh(p, cm, closestMaxDistance) && isPossiblePatchBoundary(ns, p, eps)){
//				possiblePatches.add(ns);
//			}
//			if(isPossiblePatchControlMesh(p, cm, closestMaxDistance) && isPossiblePatchBoundary(ns, p, eps)){
//				possiblePatches.add(ns);
//			}
			
		}
		return possiblePatches ;
	}
	
	public static double[] getClosestPoint(NURBSSurface nurbs, double[] point){
		if(nurbs.getRevolutionDir() != null){
			return PointProjectionSurfaceOfRevolution.getClosestPoint(nurbs, point);
		}
		double[] p = MathUtility.get3DPoint(point);
		double[] closestPoint = new double[4];
		double distNewton = Double.MAX_VALUE;
		double dist = Double.MAX_VALUE;
		double uStart = 0.;
		double vStart = 0.;
//		double eps = 0.000001;
		LinkedList<BoundaryLines> firstBl = new LinkedList<NURBSSurface.BoundaryLines>();
		firstBl.add(BoundaryLines.u0);
		firstBl.add(BoundaryLines.um);
		firstBl.add(BoundaryLines.v0);
		firstBl.add(BoundaryLines.vn);
		nurbs.setBoundLines(firstBl);
		
		LinkedList<NURBSSurface>  possiblePatches = nurbs.decomposeIntoBezierSurfacesList();
		for (int i = 0; i < 15; i++) {
			
				// start of the newton method
				if(i > 5 && i < 10){
					for (NURBSSurface possibleNs : possiblePatches) {
						double[] U = possibleNs.getUKnotVector();
						double[] V = possibleNs.getVKnotVector();
						double u = (U[0] + U[U.length - 1]) / 2;
						double v = (V[0] + V[V.length - 1]) / 2;
						double[] homogSurfPoint = possibleNs.getSurfacePoint(u, v);
						double[] surfPoint = MathUtility.get3DPoint(homogSurfPoint);
						if(distNewton > Rn.euclideanDistance(surfPoint, point)){
							distNewton = Rn.euclideanDistance(surfPoint, point);
							uStart = u;
							vStart = v;
						}
					}
				double[] result = newtonMethod (nurbs, point, 0.0000000000001,uStart, vStart);
					if(result != null){ // returns if successful
						return result;
					}
				}
				// end of the newton method
				
				LinkedList<NURBSSurface> subdividedPatches = new LinkedList<NURBSSurface>();
				possiblePatches = getPossiblePatches(possiblePatches, p);
//				System.out.println("possiblePatches.size(): " + possiblePatches.size());
				for (NURBSSurface ns : possiblePatches) {
					subdividedPatches.addAll(ns.subdivideIntoFourNewPatches());
				}
				possiblePatches = subdividedPatches;
			}
			
			for (NURBSSurface ns : possiblePatches) {
			double[] U = ns.getUKnotVector();
			double[] V = ns.getVKnotVector();
			double u = (U[0] + U[U.length - 1]) / 2;
			double v = (V[0] + V[V.length - 1]) / 2;
			double[] homogSurfPoint = ns.getSurfacePoint(u, v);
			double[] surfPoint = MathUtility.get3DPoint(homogSurfPoint);
			if(dist > Rn.euclideanDistance(surfPoint, p)){
				dist = Rn.euclideanDistance(surfPoint, p);
				closestPoint = homogSurfPoint;
			}
		}
		return closestPoint;
	}
	
	public static String TvToString(double[][][] Tv){
		String str = new String();
		for (int i = 0; i < Tv.length; i++) {
			str = str + "\n";
			for (int j = 0; j < Tv[0].length; j++) {
				str = str + Arrays.toString(Tv[i][j]) + " , ";
			}
			
		}
		return str;
	}
	
	
	private static double[] newtonMethod(NURBSSurface ns, double[] P, double eps, double u, double v){
		CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(ns, u, v);
		double[] S = ns.getSurfacePoint(u, v);
		double[] S3D = MathUtility.get3DPoint(S);
		double[] P3D = MathUtility.get3DPoint(P);
		double[] U = ns.getUKnotVector();
		double[] V = ns.getVKnotVector();
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
//			if(f < eps && g < eps && deltaU < eps && deltaV < eps){
//			if(false){	
//				System.out.println("terminiert nach " + i + " Schritten");
//				return S;
//				
//			}
//			else{
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
			ci = NURBSCurvatureUtility.curvatureAndDirections(ns, u, v);
			S = ns.getSurfacePoint(u, v);
			S3D = MathUtility.get3DPoint(S);
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
	
	
	
	
	public static void main(String[] args){
		LinkedList<BoundaryLines> bList = new LinkedList<NURBSSurface.BoundaryLines>();
//		bList.add(BoundaryLines.u0);
//		bList.add(BoundaryLines.v0);
		System.out.println(bList.contains(null));
	}
}
