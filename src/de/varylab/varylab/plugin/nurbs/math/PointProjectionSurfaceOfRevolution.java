package de.varylab.varylab.plugin.nurbs.math;

import java.util.Arrays;
import java.util.LinkedList;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSCurve;
import de.varylab.varylab.plugin.nurbs.NURBSCurve.EndPoints;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.BoundaryLines;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.CornerPoints;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.RevolutionDir;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.utilities.MathUtility;


/**
 * 
 * @author seidel
 *
 */

public class PointProjectionSurfaceOfRevolution {
	
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
	
	private static double getMinDist(double[] p, double[][] P){
		double dist = Double.MAX_VALUE;
		for (int i = 0; i < P.length; i++) {
			if(dist > Rn.euclideanDistance(p, P[i])){
				dist = Rn.euclideanDistance(p, P[i]);
			}
		}
		return dist;
	}
	
	public static double[][] getClosestSubcurve(LinkedList<NURBSCurve> curveList, double[] p){
		double minDist = Double.MAX_VALUE;
		NURBSCurve closestCurve = new NURBSCurve();
		for (NURBSCurve nc : curveList) {
			double dist = getMinDist(p, MathUtility.get3DControlPoints(nc.getControlPoints()));
			if(minDist > dist){
				minDist = dist;
				closestCurve = nc;
			}
		}
		System.out.println("CLOSEST CURVE");
		if(closestCurve.getUKnotVector() == null){
			System.out.println("NO CURVE");
		}
		System.out.println(closestCurve.toString());
		return MathUtility.get3DControlPoints(closestCurve.getControlPoints());
	}
	
	private static double getMaxDist(double[] p, double[][] P){
		double dist = Double.MIN_VALUE;
		for (int i = 0; i < P.length; i++) {
			if(dist < Rn.euclideanDistance(p, P[i])){
				dist = Rn.euclideanDistance(p, P[i]);
			}
		}
		return dist;
	}
	
	private static boolean isPossibleCurveControlPoints(double[] p, double[][] P, double closestMaxDistance){
		double dist = getMinDist(p, P);
		if(dist < closestMaxDistance){
//			System.out.println("isPossibleCurveControlPoints true");
				return true;
		}
//		System.out.println("isPossibleCurveControlPoints false");
		return false;
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
//				System.out.println("contains endpoints");
			}
			return true;
		}
		return false;
	}
	
	public static double[] getMidpointFromCircle(double[] q1, double[] q2, double[] q3){
		if(Rn.equals(q1, q2, 0.0001) && Rn.equals(q1, q3, 0.0001)){
		return q1;
		}
		double[] p2 = Rn.subtract(null, q2, q1);
		double[] p3 = Rn.subtract(null, q3, q1);
		double p22 = Rn.innerProduct(p2, p2);
		double p33 = Rn.innerProduct(p3, p3);
		double p23 = Rn.innerProduct(p2, p3);
		double lambda = 0.5 * (p33 * p23 - p22 * p33) / (p23 * p23 - p22 * p33);
		double my = 0.5 * p22 * (1 - 2 * lambda) / p23;
		double[] m = Rn.add(null, Rn.times(null, lambda, p2), Rn.times(null, my, p3));
		if(Double.isNaN(m[0])){
			return null;
		}
		Rn.add(m, m, q1);
		return m;
	}
	
	private static boolean isCircle(double[][]points1, double[][]points2){
		double[] m1 = getMidpointFromCircle(points1[0], points1[1], points1[2]);
		double[] m2 = getMidpointFromCircle(points2[0], points2[1], points2[2]);
		if(m1 == null || m2 == null){
			return false;
		}
		if(!Rn.equals(m1, m2, 0.0001)){
			return false;
		}
		return true;
	}
	

	
	/**
	 * planes are defined by 3 vectors
	 * @param plane1
	 * @param plane2
	 * @return true if plane1, plane2 parallel
	 */
	public static boolean affinePlanesAreParallel(double[][] plane1, double[][] plane2){
		double[] p11 = Rn.subtract(null, plane1[1], plane1[0]);
		double[] p12 = Rn.subtract(null, plane1[2], plane1[0]);
		double[] p21 = Rn.subtract(null, plane2[1], plane2[0]);
		double[] p22 = Rn.subtract(null, plane2[2], plane2[0]);
		double[][] matrix = new double[3][];
		matrix[0] = p11;
		matrix[1] = p12;
		matrix[2] = p21;
		if(Math.abs(Rn.determinant(matrix)) > 0.001){
			return false;
		}
		matrix[2] = p22;
		if(Math.abs(Rn.determinant(matrix)) > 0.001){
			return false;
		}
		return true;
	}
	
	public static boolean pointsAreCollinear(double[][] points){
		double[] zero = {0.0,0.0,0.0};
//		double[] vec1 = Rn.normalize(null, Rn.subtract(null, points[1], points[0]));
		double[] vec1 = Rn.subtract(null, points[1], points[0]);
		for (int i = 2; i < points.length; i++) {
//			double[] vec2 = Rn.normalize(null, Rn.subtract(null, points[i], points[0]));
			double[] vec2 = Rn.subtract(null, points[i], points[0]);
			if(!Rn.equals(Rn.crossProduct(null, vec1, vec2), zero, 0.001)){
				return false;
			}
		}
		return true;
	}
	
	
	public static boolean isSurfaceOfRevolutionUDir(NURBSSurface ns){
		double[] U = ns.getUKnotVector();
		double[] V = ns.getVKnotVector();
		double u0 = U[0];
		double um = U[U.length - 1];
		double v0 = V[0];
		double vn = V[V.length - 1];
		double[][] midPointsU = new double[6][];
		double [][] plane1 = new double[3][];
		double [][] plane2 = new double[3][];
		for (int i = 0; i < 6; i++) {
			double u = u0 + i / 5. * (um - u0);
			double[][] p1 = new double[3][];
			double[][] p2 = new double[3][];
			for (int j = 0; j < 3; j++) {
				p1[j] = MathUtility.get3DPoint(ns.getSurfacePoint(u, v0 + 2 * j / 5. * (vn - v0)));
				p2[j] = MathUtility.get3DPoint(ns.getSurfacePoint(u, v0 + (2 * j + 1) / 5. * (vn - v0)));
			}
			if(!isCircle(p1, p2)){
				return false;
			}
			midPointsU[i] = getMidpointFromCircle(p1[0], p1[1], p1[2]);
			if(i == 0){
				plane1[0] = midPointsU[0];
				plane1[1] = p1[1];
				plane1[2] = p1[2];
			}else{
				plane2[0] = midPointsU[i];
				plane2[1] = p1[1];
				plane2[2] = p1[2];
			}
			if(i > 0 && !affinePlanesAreParallel(plane1, plane2)){
				return false;
			}
		}
		if(!pointsAreCollinear(midPointsU)){
			return false;
		}
		return true;
	}
	
	public static boolean isSurfaceOfRevolutionVDir(NURBSSurface ns){
		double[] U = ns.getUKnotVector();
		double[] V = ns.getVKnotVector();
		double u0 = U[0];
		double um = U[U.length - 1];
		double v0 = V[0];
		double vn = V[V.length - 1];
		double[][] midPointsV = new double[6][];
		double [][] plane1 = new double[3][];
		double [][] plane2 = new double[3][];
		for (int i = 0; i < 6; i++) {
			double v = v0 + i / 5. * (vn - v0);
			double[][] p1 = new double[3][];
			double[][] p2 = new double[3][];
			for (int j = 0; j < 3; j++) {
				p1[j] = MathUtility.get3DPoint(ns.getSurfacePoint(u0 + 2 * j / 5. * (um - u0), v));
				p2[j] = MathUtility.get3DPoint(ns.getSurfacePoint(u0 + (2 * j + 1) / 5. * (um - u0), v));
				}
				if(!isCircle(p1, p2)){
					return false;
				}
			midPointsV[i] = getMidpointFromCircle(p1[0], p1[1], p1[2]);
			if(i == 0){
				plane1[0] = midPointsV[0];
				plane1[1] = p1[1];
				plane1[2] = p1[2];
			}else{
				plane2[0] = midPointsV[i];
				plane2[1] = p1[1];
				plane2[2] = p1[2];
			}
			if(i > 0 && !affinePlanesAreParallel(plane1, plane2)){
				return false;
			}
		}
		
		if(!pointsAreCollinear(midPointsV)){
			return false;
		}
		return true;
	}
	
	public static RevolutionDir getRotationDir(NURBSSurface ns){
		RevolutionDir dir = null;
		if(isSurfaceOfRevolutionUDir(ns)){
			dir = RevolutionDir.uDir;
		}
		if(isSurfaceOfRevolutionVDir(ns)){
			dir = RevolutionDir.vDir;
		}
		return dir;
	}
	
	
	public static double[][] getRotationAxis(NURBSSurface ns){
		double[] U = ns.getUKnotVector();
		double[] V = ns.getVKnotVector();
		double u0 = U[0];
		double um = U[U.length - 1];
		double v0 = V[0];
		double vn = V[V.length - 1];
		double[][] axis = new double[2][];
		if(ns.getRevolutionDir() == RevolutionDir.uDir){
			double[] p1 = MathUtility.get3DPoint(ns.getSurfacePoint(u0, v0 + 0.25 * (vn - v0)));
			double[] p2 = MathUtility.get3DPoint(ns.getSurfacePoint(u0, v0 + 0.5 * (vn - v0)));
			double[] p3 = MathUtility.get3DPoint(ns.getSurfacePoint(u0, v0 + 0.75 * (vn - v0)));
			double[] m0 = getMidpointFromCircle(p1, p2, p3);
			p1 = MathUtility.get3DPoint(ns.getSurfacePoint(um, v0 + 0.25 * (vn - v0)));
			p2 = MathUtility.get3DPoint(ns.getSurfacePoint(um, v0 + 0.5 * (vn - v0)));
			p3 = MathUtility.get3DPoint(ns.getSurfacePoint(um, v0 + 0.75 * (vn - v0)));
			double[] m1 = getMidpointFromCircle(p1, p2, p3);
			axis[0] = m0;
			axis[1] = m1;
			return axis;
		}
		if(ns.getRevolutionDir() == RevolutionDir.vDir){
			double[] p1 = MathUtility.get3DPoint(ns.getSurfacePoint(u0 + 0.25 * (um - u0), v0));
			double[] p2 = MathUtility.get3DPoint(ns.getSurfacePoint(u0 + 0.5 * (um - u0), v0));
			double[] p3 = MathUtility.get3DPoint(ns.getSurfacePoint(u0 + 0.75 * (um - u0), v0));
			double[] m0 = getMidpointFromCircle(p1, p2, p3);
			p1 = MathUtility.get3DPoint(ns.getSurfacePoint(u0 + 0.25 * (um - u0), vn));
			p2 = MathUtility.get3DPoint(ns.getSurfacePoint(u0 + 0.5 * (um - u0), vn));
			p3 = MathUtility.get3DPoint(ns.getSurfacePoint(u0 + 0.75 * (um - u0), vn));
			double[] m1 = getMidpointFromCircle(p1, p2, p3);
			axis[0] = m0;
			axis[1] = m1;
			return axis;
		}
		return null;
	}
	
	public static double[] NewtonMethod(NURBSCurve nc, double u, double[] point){
		double[] p = MathUtility.get3DPoint(point);
		double[][]Ck = nc.getCurveDerivs(u);
		double[] C = Ck[0];
		double[] Cu = Ck[1];
		double[] Cuu = Ck[2];
		for (int i = 0; i < 12; i++) {
			u = u - (Rn.innerProduct(Cu, Rn.subtract(null, C, p))) / (Rn.innerProduct(Cuu, Rn.subtract(null, C, p)) + Rn.innerProduct(Cu, Cu));
			Ck = nc.getCurveDerivs(u);
			C = Ck[0];
			Cu = Ck[1];
			Cuu = Ck[2];
		}
		return nc.getCurvePoint(u);
	}
	
	
	

	
	public static LinkedList<NURBSCurve> getPossibleCurves(LinkedList<NURBSCurve> curveList, double[] p){
		double eps = 0.000001;
		LinkedList<NURBSCurve> possibleCurves = new LinkedList<NURBSCurve>();
		for (NURBSCurve nc : curveList) {
//			nc.toString();
			if(isPossibleCurveEndPoint(nc, p, eps)){
				possibleCurves.add(nc);
			}
		}
		return possibleCurves ;
	}
	
	public static double[] getClosestPointOnCurve(NURBSCurve nurbs, double[] point){
		double[] p = MathUtility.get3DPoint(point);
		double[] closestPoint = new double[4];
		double distNewton = Double.MAX_VALUE;
		double dist = Double.MAX_VALUE;
		double uStart = 0.;
//		double eps = 0.000001;
		
		
		LinkedList<NURBSCurve>  possibleCurves = nurbs.decomposeIntoBezierCurvesList();
		for (int i = 0; i < 15; i++) {
			
				// start of newton method
//				if(i > 5 && i < 10){
//					for (NURBSCurve possibleNc : possibleCurves) {
//						double[] U = possibleNc.getUKnotVector();
//						double u = (U[0] + U[U.length - 1]) / 2;
//						double[] homogCurvePoint = possibleNc.getCurvePoint(u);
//						double[] curvePoint = MathUtility.get3DPoint(homogCurvePoint);
//						if(distNewton > Rn.euclideanDistance(curvePoint, point)){
//							distNewton = Rn.euclideanDistance(curvePoint, point);
//							uStart = u;
//						}
//					}
//				double[] result = NewtonMethod(nurbs, uStart, point);
//					if(result != null){ // returns if successful
//						return result;
//					}
//				}
				// end of the newton method
				
				LinkedList<NURBSCurve> subdividedCurves = new LinkedList<NURBSCurve>();
				possibleCurves = getPossibleCurves(possibleCurves, p);
//				System.out.println("possiblePatches.size(): " + possiblePatches.size());
				for (NURBSCurve nc : possibleCurves) {
					subdividedCurves.addAll(nc.subdivideIntoTwoNewCurves());
				}
				possibleCurves = subdividedCurves;
			}
			possibleCurves = getPossibleCurves(possibleCurves, p);
//			System.out.println("listenlaenge = " + possibleCurves.size());
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
	
	public static boolean isInInterior(double[][] cpw, double[] point){
		double[] p = Rn.normalize(null, MathUtility.get3DPoint(point));
		double[][] cp = MathUtility.get3DControlPoints(cpw);
		for (int i = 0; i < cp.length; i++) {
			Rn.normalize(cp[i], cp[i]);
		}
		double[][] cpXZ = new double[cp.length][2];
		for (int i = 0; i < cpXZ.length; i++) {
			cpXZ[0] = cp[0];
			cpXZ[1] = cp[2];
		}
		double[][] segPoint = {{p[0],p[2]},{0,0}};
		LineSegment lsPoint = new LineSegment();
		lsPoint.setSegment(segPoint);
		for (int i = 0; i < cp.length - 1; i++) {
			double[][] seg = {{cp[i][0],cp[i][2]},{cp[i + 1][0],cp[i + 1][2]}};
			LineSegment ls = new LineSegment();
			ls.setSegment(seg);
			if(LineSegmentIntersection.twoSegmentIntersection(lsPoint, ls)){
				return true;
			}
		}
		return false;
	}
	
	public static double[] getClosestPoint(NURBSSurface ns, double[] point){
		double[] p = point.clone();
		double[] closestPoint = new double[4];
		LinkedList<EndPoints> ep = new LinkedList<NURBSCurve.EndPoints>();
		ep.add(EndPoints.P0);
		ep.add(EndPoints.Pm);
		double[][] axis = PointProjectionSurfaceOfRevolution.getRotationAxis(ns);
		double[][][]Pw = ns.getControlMesh().clone();
		double[][][]P = new double [Pw.length][Pw[0].length][];
		for (int i = 0; i < P.length; i++) {
			for (int j = 0; j < P[0].length; j++) {
				P[i][j] = Pw[i][j].clone();
			}
		}
		MatrixBuilder b1 = MatrixBuilder.euclidean();
		//translation
		b1.translate(Rn.times(null, -1, axis[0]));
		Matrix M1 = b1.getMatrix();
		double[] axis1 = M1.multiplyVector(axis[1]).clone();
		// 1. rotation
		double[] e2 = {0,1,0,1};
		MatrixBuilder b2 = MatrixBuilder.euclidean();
		b2.rotateFromTo(axis1, e2);
		Matrix M2 = b2.getMatrix();
		axis1 =  M2.multiplyVector(axis1);
		double[] P00 = Pn.dehomogenize(null, P[0][0].clone());
		P00 = M1.multiplyVector(P00);
		P00 = M2.multiplyVector(P00);
		P00[1] = 0;
		P00[3] = 0;
		double[] e1 = {1,0,0,1};
		MatrixBuilder b3 = MatrixBuilder.euclidean();
		b3.rotateFromTo(P00, e1);
		Matrix M3 = b3.getMatrix();
		P00 = M3.multiplyVector(P00);
		Matrix MCurve = M3;
		MCurve.multiplyOnRight(M2);
		MCurve.multiplyOnRight(M1);
		double[][] controlPoints = new double[P.length][];
		double[][] VcontrolPoints = new double[P[0].length][];
		for (int j = 0; j < P[0].length; j++) {
			VcontrolPoints[j] = MCurve.multiplyVector(P[0][j]);
		}
		for (int i = 0; i < P.length; i++) {
			controlPoints[i] = MCurve.multiplyVector(P[i][0]);
		}
		p = MCurve.multiplyVector(p);
		MatrixBuilder b4 = MatrixBuilder.euclidean();
		//p translated to the x-z plane
		double[] pTrans = {p[0],0, p[2],1};
		
		// check if projection is in the interior of the surface
		boolean projIn = isInInterior(VcontrolPoints, pTrans);
		if(!projIn){
			// check if the projection is on the other boundary curve
			if(Rn.innerProduct(MathUtility.get3DPoint(pTrans), MathUtility.get3DPoint(VcontrolPoints[0])) < Rn.innerProduct(MathUtility.get3DPoint(pTrans), MathUtility.get3DPoint(VcontrolPoints[VcontrolPoints.length - 1]))){
				for (int i = 0; i < P.length; i++) {
					controlPoints[i] = MCurve.multiplyVector(P[i][P[0].length - 1]);
				}
				MCurve.invert();
				NURBSCurve nc = new NURBSCurve(controlPoints, ns.getUKnotVector(), ns.getUDegree(), ep);
				closestPoint = getClosestPointOnCurve(nc, p);
				closestPoint = MCurve.multiplyVector(closestPoint);
				return closestPoint;
			}
			MCurve.invert();
			NURBSCurve nc = new NURBSCurve(controlPoints, ns.getUKnotVector(), ns.getUDegree(), ep);
			closestPoint = getClosestPointOnCurve(nc, p);
			closestPoint = MCurve.multiplyVector(closestPoint);
			return closestPoint;
		}
		NURBSCurve nc = new NURBSCurve(controlPoints, ns.getUKnotVector(), ns.getUDegree(), ep);
		b4.rotateFromTo(pTrans, e1);
		Matrix M4 = b4.getMatrix();
		p = M4.multiplyVector(p);
		closestPoint = getClosestPointOnCurve(nc, p);
		Matrix MPoint = M4;
		MPoint.multiplyOnRight(MCurve);
		MPoint.invert();
		closestPoint = MPoint.multiplyVector(closestPoint);
		return closestPoint;
	}
	
	

}
