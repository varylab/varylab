package de.varylab.varylab.plugin.nurbs.math;

import java.util.LinkedList;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSCurve;
import de.varylab.varylab.plugin.nurbs.NURBSCurve.EndPoints;
import de.varylab.varylab.utilities.MathUtility;

public class PointProjectionCurve {
	
	
	/**
	 * 
	 * @param P control points in 3D coords
	 * @param p point in 3D coords
	 * @param eps tolerance
	 * @return the endpoint P0 if the 
	 */
	
	private static EndPoints isP0(double[][] P, double[] p, double eps){
		double[] P0 = P[0];
		double[] pp0 = Rn.subtract(null, p, P0);
		double[] cPoint;
		boolean b0 = true;
		for (int i = 0; i < P.length; i++) {
			cPoint = Rn.subtract(null, P0, P[i]);
			if(i != 0 && Rn.innerProduct(Rn.normalize(null, pp0), Rn.normalize(null, cPoint)) < eps){
				b0 = false;
			}
		}
		if(b0){
			return EndPoints.P0;
		}
		return null;
	}
	
	private static EndPoints isPm(double[][] P, double[] p, double eps){
		double[] Pm = P[P.length - 1];
		double[] ppm = Rn.subtract(null, p, Pm);
		double[] cPoint;
		boolean bm = true;
		for (int i = 0; i < P.length; i++) {
			cPoint = Rn.subtract(null, Pm, P[i]);
			if(i != P.length - 1 && Rn.innerProduct(Rn.normalize(null, ppm), Rn.normalize(null, cPoint)) < eps){
				bm = false;
			}
		}
		if(bm){
			return EndPoints.Pm;
		}
		return null;
	}
	
	private static EndPoints isOnEndPoint(NURBSCurve nc, double[] p, double eps){
		double[][] P = MathUtility.get3DControlPoints(nc.getControlPoints());
		if(isP0(P,p,eps) != null){
			return isP0(P,p,eps);
		}
		if(isPm(P,p,eps) != null){
			return isPm(P,p,eps);
		} 
		return null;
	}
	
	private static boolean isPossibleCurveEndPoint(NURBSCurve nc, double[] p, double eps){
		if(isOnEndPoint(nc, p, eps) == null || nc.getEndPoints().contains(isOnEndPoint(nc, p, eps))){
			if(nc.getEndPoints().contains(isOnEndPoint(nc, p, eps))){
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param p a 3D point
	 * @param P 3D control points
	 * @return min distance
	 */
	private static double getMinDist(double[] p, double[][] P){
		double dist = Double.MAX_VALUE;
		for (int i = 0; i < P.length; i++) {
			if(dist > Rn.euclideanDistance(p, P[i])){
				dist = Rn.euclideanDistance(p, P[i]);
			}
		}
		return dist;
	}
	
	/**
	 * 
	 * @param p a 3D point
	 * @param P 3D control points
	 * @return max distance
	 */
	private static double getMaxDist(double[] p, double[][] P){
		double dist = Double.MIN_VALUE;
		for (int i = 0; i < P.length; i++) {
			if(dist < Rn.euclideanDistance(p, P[i])){
				dist = Rn.euclideanDistance(p, P[i]);
			}
		}
		return dist;
	}
	
	/**
	 * @param curveList: list of NURBS curves
	 * @param p: a 3D point
	 * @return the control points of the closest subcurve in 3D coords
	 */
	
	
	public static NURBSCurve getClosestSubcurve(LinkedList<NURBSCurve> curveList, double[] p){
//	public static double[][] getClosestSubcurve(LinkedList<NURBSCurve> curveList, double[] p){
		double minDist = Double.MAX_VALUE;
		NURBSCurve closestCurve = new NURBSCurve();
		for (NURBSCurve nc : curveList) {
			double dist = getMinDist(p, MathUtility.get3DControlPoints(nc.getControlPoints()));
			if(minDist > dist){
				minDist = dist;
				closestCurve = nc;
			}
		}
		return closestCurve;
//		return MathUtility.get3DControlPoints(closestCurve.getControlPoints());
	}
	
	/**
	 * 
	 * @param p point in 3D coords
	 * @param P control points in 3D coords
	 * @param closestMaxDistance maximal distance between p and the control points of the closest subcurve
	 * @return true if the minimal distance between p and the control points is less than closestMaxDistance
	 */
	
	private static boolean isPossibleCurveControlPoints(double[] p, double[][] P, double closestMaxDistance){
		double dist = getMinDist(p, P);
		if(dist < closestMaxDistance){
				return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param curveList
	 * @param p
	 * @return all candidates of curves that can contain the projection
	 */
	
	public static LinkedList<NURBSCurve> getPossibleCurves(LinkedList<NURBSCurve> curveList, double[] p){
		double eps = 0.000001;
//		if(curveList.size() < 2){
//			return curveList;
//		}
		LinkedList<NURBSCurve> possibleCurves = new LinkedList<NURBSCurve>();
		NURBSCurve closestCurve = getClosestSubcurve(curveList, p);
//		System.out.println("closest curve");
//		System.out.println(closestCurve.toString());
		double closestMaxDistance = getMaxDist(p, MathUtility.get3DControlPoints(closestCurve.getControlPoints()));
//		double[][] closestCP = getClosestSubcurve(curveList, p);
		possibleCurves.add(closestCurve);
		for (NURBSCurve nc : curveList) {
			if(nc != closestCurve){
				if(isPossibleCurveEndPoint(nc, p, eps) && isPossibleCurveControlPoints(p, nc.getControlPoints(), closestMaxDistance)){
//				if(isPossibleCurveEndPoint(nc, p, eps)){
//				if(isPossibleCurveControlPoints(p, nc.getControlPoints(), closestMaxDistance)){	
					possibleCurves.add(nc);
				}
			}
		}
		return possibleCurves ;
	}
	
	/**
	 * computes the closest point via the newton method
	 * @param nc NURBSCurve
	 * @param u start point in the domain
	 * @param point 
	 * @return the closest point on the curve if possible else null
	 */
	
	public static double[] NewtonMethod(NURBSCurve nc, double u, double[] point){
		double[] U = nc.getUKnotVector();
		double u0 = U[0];
		double um = U[U.length - 1];
		double[] p = MathUtility.get3DPoint(point);
		double[][]Ck = nc.getCurveDerivs(u);
		double[] C = Ck[0];
		double[] Cu = Ck[1];
		double[] Cuu = Ck[2];
		for (int i = 0; i < 12; i++) {
			u = u - (Rn.innerProduct(Cu, Rn.subtract(null, C, p))) / (Rn.innerProduct(Cuu, Rn.subtract(null, C, p)) + Rn.innerProduct(Cu, Cu));
			if(u < u0 || u > um){
				return null;
			}
			Ck = nc.getCurveDerivs(u);
			C = Ck[0];
			Cu = Ck[1];
			Cuu = Ck[2];
		}
		return nc.getCurvePoint(u);
	}
	
	/**
	 * computes the closest point
	 * @param nurbs NURBSCurve
	 * @param point
	 * @return the closest point on the curve
	 */
	
	public static double[] getClosestPoint(NURBSCurve nurbs, double[] point){
		double[] p = MathUtility.get3DPoint(point);
		double[] closestPoint = new double[4];
		double distNewton = Double.MAX_VALUE;
		double dist = Double.MAX_VALUE;
		double uStart = 0.;
		LinkedList<NURBSCurve>  possibleCurves = nurbs.decomposeIntoBezierCurvesList();
		for (int i = 0; i < 15; i++) {
			// start of newton method
			if(i > 5 && i < 12){
				for (NURBSCurve possibleNc : possibleCurves) {
					double[] U = possibleNc.getUKnotVector();
					double u = (U[0] + U[U.length - 1]) / 2;
					double[] homogCurvePoint = possibleNc.getCurvePoint(u);
					double[] curvePoint = MathUtility.get3DPoint(homogCurvePoint);
					if(distNewton > Rn.euclideanDistance(curvePoint, point)){
						distNewton = Rn.euclideanDistance(curvePoint, point);
						uStart = u;
					}
				}
			double[] result = NewtonMethod(nurbs, uStart, point);
				if(result != null){ // returns if successful
					return result;
				}
			}
			// end of the newton method
				
			LinkedList<NURBSCurve> subdividedCurves = new LinkedList<NURBSCurve>();
			possibleCurves = getPossibleCurves(possibleCurves, p);
			for (NURBSCurve nc : possibleCurves) {
				subdividedCurves.addAll(nc.subdivideIntoTwoNewCurves());
			}
			possibleCurves = subdividedCurves;
		}
		possibleCurves = getPossibleCurves(possibleCurves, p);
		for (NURBSCurve nc : possibleCurves) {
			double[] U = nc.getUKnotVector();
			double u = (U[0] + U[U.length - 1]) / 2;
			double[] homogCurvePoint = nc.getCurvePoint(u);
			double[] curvePoint = MathUtility.get3DPoint(homogCurvePoint);
			if(dist > Rn.euclideanDistance(curvePoint, p)){
				dist = Rn.euclideanDistance(curvePoint, p);
				closestPoint = homogCurvePoint;
			}
		}
		return closestPoint;
	}

}
