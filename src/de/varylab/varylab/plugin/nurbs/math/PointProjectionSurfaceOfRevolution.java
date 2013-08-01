package de.varylab.varylab.plugin.nurbs.math;

import java.util.LinkedList;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSCurve;
import de.varylab.varylab.plugin.nurbs.NURBSCurve.EndPoints;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.RevolutionDir;
import de.varylab.varylab.utilities.MathUtility;


/**
 * 
 * @author seidel
 * @see <a href = "http://page.math.tu-berlin.de/~seidel/NURBS.pdf"> NURBS </a>
 */

public class PointProjectionSurfaceOfRevolution {
	
	/**
	 * 
	 * @param q1
	 * @param q2
	 * @param q3
	 * @return midpoints
	 */
	
	
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
	
	/**
	 * 
	 * @param points1 3 points in 3D coords defining a circle
	 * @param points2 3 points in 3D coords defining a circle
	 * @return true if the midpoints of the circles coincide
	 */
	
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
	
	/**
	 * 
	 * @param points
	 * @return true if all points lie on a common line
	 */
	
	public static boolean pointsAreOnACommonLine(double[][] points){
		double[] zero = {0.0,0.0,0.0};
		double[] vec1 = Rn.normalize(null, Rn.subtract(null, points[1], points[0]));
		for (int i = 2; i < points.length; i++) {
			double[] vec2 = Rn.normalize(null, Rn.subtract(null, points[i], points[0]));
			if(!Rn.equals(Rn.crossProduct(null, vec1, vec2), zero, 0.001)){
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * 
	 * @param ns
	 * @return true if the surface is a surface of revolution in u direction
	 */
	
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
				p1[j] = MathUtility.get3DPoint(ns.getSurfacePoint(u, v0 + 2 * j / 6. * (vn - v0)));
				p2[j] = MathUtility.get3DPoint(ns.getSurfacePoint(u, v0 + (2 * j + 1) / 6. * (vn - v0)));
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
		if(!pointsAreOnACommonLine(midPointsU)){
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param ns
	 * @return true if the surface is a surface of revolution in v direction
	 */
	
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
		
		if(!pointsAreOnACommonLine(midPointsV)){
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param ns
	 * @return the direction of the rotation axis
	 */
	
	public static RevolutionDir getRotationDir(NURBSSurface ns){
		if(isSurfaceOfRevolutionVDir(ns)){
			return  RevolutionDir.vDir;
		}
		if(isSurfaceOfRevolutionUDir(ns)){
			return  RevolutionDir.uDir;
		}
		return null;
	}
	
	/**
	 * 
	 * @param ns NURBSSurface
	 * @return the rotation axis if the NURBSSurface is a surface of revolution else null
	 */
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
	
	/**
	 * we use QR decomposition
	 * @param v 2D vector
	 * @param w 2D vector
	 * @param x := lambda * v + my * w
	 * @return lambda , my
	 */
	
	public static double[] getLinearCombination(double[] v, double[] w, double[] x){
		double s = -v[1] / Math.sqrt(v[0] * v[0] + v[1] * v[1]);
		double c = v[0] / Math.sqrt(v[0] * v[0] + v[1] * v[1]);
		double my = (s * x[0] + c * x[1]) / (s * w[0] + c * w[1]);
		double lambda = (c * x[0] - s * x[1] - my * (c * w[0] - s * w[1]))/ Math.sqrt(v[0] * v[0] + v[1] * v[1]);
		double[] comb = {lambda, my};
		return comb;
	}
	
		
	/**
	 * this is a special QR decomposition <br/>
	 * we rotate the ray such that the y-component is 0, then we check if the x-components of v and w after rotation are positive
	 * and finally we check if the y-components of v and w have opposite signs.
	 * @param v first end point of the line segment
	 * @param w second end point of the line segment
	 * @param ray
	 * @return true if ray intersect the line segment
	 */
	public static boolean rayIntersectLineSegment(double[] v, double[] w, double[] ray){
		double s = -ray[1] / Math.sqrt(ray[0] * ray[0] + ray[1] * ray[1]);
		double c = ray[0] / Math.sqrt(ray[0] * ray[0] + ray[1] * ray[1]);
		double vRot0 = c * v[0] - s * v[1];
		double wRot0 = c * w[0] - s * w[1];
		double vRot1 = s * v[0] + c * v[1];
		double wRot1 = s * w[0] + c * w[1];
		if(vRot0 > 0 && wRot0 > 0 && (vRot1 <= 0 && wRot1 >= 0) || (wRot1 <= 0 && vRot1 >= 0)){
			return true;
		}
		return false;
	}
		
	
	/**
	 * check if ray is in the convex cone spanned be v and w
	 * @param v first end point of the line segment
	 * @param w second end point of the line segment
	 * @param ray
	 * @return true if ray intersect the line segment
	 */
//	public static boolean rayIntersectLineSegment(double[] v, double[] w, double[] ray){
//		double[] comb = getLinearCombination(v, w, ray);
//		if(comb[0] >= 0 && comb[1] >= 0){
//			return true;
//		}
//		return false;
//	}
	
	
	

	
	/**
	 * we assume a surface of revolution in u direction
	 * @param cpw control points
	 * @param point
	 * @return true if the point will be projected into the interior of a surface
	 * of revolution 
	 */
	
	public static boolean isInInterior(double[][] cpw, double[] point){
		double[][] cp = cpw;
		double[] ray = {point[0], point[2]};
		for (int i = 0; i < cp.length - 1; i++) {
			double[] v = {cp[i][0],cp[i][2]};
			double[] w = {cp[i + 1][0],cp[i + 1][2]};
			if(rayIntersectLineSegment(v, w, ray)){
				return true;
			}
		}
		return false;
	}
	
	public static double[] getOrthVector(double[] v, double[] w, double[] p){
		double[] w1 = Rn.subtract(null, w, v); 
		double[] p1 = Rn.subtract(null, p, v); 
		double lambda = Rn.innerProduct(w1, p1) / Rn.innerProduct(w1, w1);
		double[] proj = Rn.times(null, lambda, w1);
		return Rn.subtract(null, p1, proj);
	}
	
	/**
	 * 
	 * @param controlPoints
	 * @param axis
	 * @return the x- vector of the new frame
	 */
	
	public static double[] getNewX(double[][] controlPoints, double[][] axis){
		if(!Rn.equals(MathUtility.get3DPoint(controlPoints[0]), axis[0], 0.00001)){
			return Rn.normalize(null, Rn.subtract(null, MathUtility.get3DPoint(controlPoints[0]), axis[0]));
		}
		else{
			for (int i = 1; i < controlPoints.length; i++) {
				double[] newX = getOrthVector(axis[0], axis[1], MathUtility.get3DPoint(controlPoints[i]));
				if(!(Math.abs(Rn.euclideanNorm(newX)) < 0.0001)){
					return Rn.normalize(null, newX);
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param points
	 * @return true if all points are equal up to a small epsilon
	 */
	
	public static boolean pointsAreEqual(double[][] points){
		for (int i = 0; i < points.length - 1; i++) {
			if(!Rn.equals(points[i], points[i + 1], 0.0001)){
				return false;
			}
		}
		return true;
	}
	
	
	public static double[][] getIthVControlPoints(int i, double[][][] Pw){
		double[][] VcontrolPoints = new double[Pw[0].length][];
		for (int j = 0; j < Pw[0].length; j++) {
			VcontrolPoints[j] = MathUtility.get3DPoint(Pw[i][j].clone());
		}
		return VcontrolPoints;
	}
	
	
	
	public static double[][] getJthUControlPoints(int j, double[][][] Pw){
		double[][] UcontrolPoints = new double[Pw.length][];
		for (int i = 0; i < Pw.length; i++) {
			UcontrolPoints[i] = MathUtility.get3DPoint(Pw[i][j].clone());
		}
		return UcontrolPoints;
	}
	
	/**
	 * 
	 * @param Pw
	 * @return non equal control points in v direction
	 */
	
	public static double[][] getDistinctVControlPoints(double[][][] Pw){
		double[][] VcontrolPoints = new double[Pw[0].length][];
		for (int i = 0; i < Pw.length; i++) {
			VcontrolPoints = getIthVControlPoints(i, Pw);
			if(!pointsAreEqual(VcontrolPoints)){
				return VcontrolPoints;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param Pw
	 * @return non equal control points in u direction
	 */
	
	public static double[][] getDistinctUControlPoints(double[][][] Pw){
		double[][] UcontrolPoints = new double[Pw.length][];
		for (int j = 0; j < Pw[0].length; j++) {
			UcontrolPoints = getJthUControlPoints(j, Pw);
			if(!pointsAreEqual(UcontrolPoints)){
				return UcontrolPoints;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param points
	 * @return same points with y-component 0
	 */
	
	public static double[][] tranlateToXZPlane(double[][] points){
		for (int i = 0; i < points.length; i++) {
			points[i][1] = 0.0;
		}
		return points;
	}
	
	private static double[] getClosestPointUDir(NURBSSurface ns, double[] point){
		double[] p = point.clone();
		double[] closestPoint = new double[4];
		LinkedList<EndPoints> ep = new LinkedList<NURBSCurve.EndPoints>();
		ep.add(EndPoints.P0);
		ep.add(EndPoints.Pm);
		// two points of the the rotation axis
		double[][] axis = PointProjectionSurfaceOfRevolution.getRotationAxis(ns); 
		double[][][]Pw = ns.getControlMesh().clone(); 
		double[][] controlPoints = new double[Pw.length][];
		for (int i = 0; i < controlPoints.length; i++) {
			controlPoints[i] = Pw[i][0].clone();
		}
		double[][] VcontrolPoints = getDistinctVControlPoints(Pw);
		double[] P00 = controlPoints[0].clone();
		double[] newX = getNewX(controlPoints, axis);
		double[] newY = Rn.normalize(null, Rn.subtract(null, axis[1], axis[0]));
		double[] newZ = Rn.crossProduct(null, newX, newY);
		Matrix MCurve = new Matrix(newX[0], newY[0], newZ[0], axis[0][0], newX[1], newY[1], newZ[1], axis[0][1], newX[2], newY[2], newZ[2], axis[0][2], 0.0, 0.0, 0.0, 1.0);
		MCurve.invert();
		P00 = MCurve.multiplyVector(P00);
		for (int i = 0; i < controlPoints.length; i++) {
			controlPoints[i] = MCurve.multiplyVector(controlPoints[i]);
		}
		for (int j = 0; j < VcontrolPoints.length; j++) {
			VcontrolPoints[j] = MCurve.multiplyVector(VcontrolPoints[j]);
		}
		p = MCurve.multiplyVector(p);
		//p translated to the x-z plane
		double[] pTrans = {p[0],0, p[2],1};
//		Rn.normalize(pTrans, pTrans);/		VcontrolPoints = tranlateToXZPlane(VcontrolPoints);
		// check if projection is in the interior of the surface
		boolean projIn = isInInterior(VcontrolPoints, pTrans);
		
		if(!projIn){
			if(Rn.innerProduct(MathUtility.get3DPoint(pTrans), VcontrolPoints[0]) < Rn.innerProduct(MathUtility.get3DPoint(pTrans), VcontrolPoints[VcontrolPoints.length - 1])){	
				for (int i = 0; i < controlPoints.length; i++) {
					controlPoints[i] = Pw[i][Pw[0].length - 1].clone();
					controlPoints[i] = MCurve.multiplyVector(controlPoints[i]);
				}
				MCurve.invert();
				NURBSCurve nc = new NURBSCurve(controlPoints, ns.getUKnotVector(), ns.getUDegree(), ep);
				closestPoint = PointProjectionCurve.getClosestPoint(nc, p);
				closestPoint = MCurve.multiplyVector(closestPoint);
				return closestPoint;
			}
			MCurve.invert();
			NURBSCurve nc = new NURBSCurve(controlPoints, ns.getUKnotVector(), ns.getUDegree(), ep);
			closestPoint = PointProjectionCurve.getClosestPoint(nc, p);
			closestPoint = MCurve.multiplyVector(closestPoint);
			return closestPoint;
		}
		MatrixBuilder b4 = MatrixBuilder.euclidean();
		double[] e1 = {1,0,0,1};
		NURBSCurve nc = new NURBSCurve(controlPoints, ns.getUKnotVector(), ns.getUDegree(), ep);
		b4.rotateFromTo(pTrans, e1);
		Matrix M4 = b4.getMatrix();
		p = M4.multiplyVector(p);
		closestPoint = PointProjectionCurve.getClosestPoint(nc, p);
		Matrix MPoint = M4;
		MPoint.multiplyOnRight(MCurve);
		MPoint.invert();
		closestPoint = MPoint.multiplyVector(closestPoint);
		return closestPoint;
	}
	
	private static double[] getClosestPointVDir(NURBSSurface ns, double[] point){
		double[] p = point.clone();
		double[] closestPoint = new double[4];
		LinkedList<EndPoints> ep = new LinkedList<NURBSCurve.EndPoints>();
		ep.add(EndPoints.P0);
		ep.add(EndPoints.Pm);
		// two points of the the rotation axis
		double[][] axis = PointProjectionSurfaceOfRevolution.getRotationAxis(ns); 
		double[][][]Pw = ns.getControlMesh().clone(); 
		double[][] controlPoints = new double[Pw[0].length][];
		for (int j = 0; j < controlPoints.length; j++) {
			controlPoints[j] = Pw[0][j].clone();
		}
		double[][] UcontrolPoints = getDistinctUControlPoints(Pw);
		double[] P00 = controlPoints[0].clone();
		double[] newX = getNewX(controlPoints, axis);
		double[] newY = Rn.normalize(null, Rn.subtract(null, axis[1], axis[0]));
		double[] newZ = Rn.crossProduct(null, newX, newY);
		Matrix MCurve = new Matrix(newX[0], newY[0], newZ[0], axis[0][0], newX[1], newY[1], newZ[1], axis[0][1], newX[2], newY[2], newZ[2], axis[0][2], 0.0, 0.0, 0.0, 1.0);
		MCurve.invert();
		P00 = MCurve.multiplyVector(P00);
		for (int i = 0; i < controlPoints.length; i++) {
			controlPoints[i] = MCurve.multiplyVector(controlPoints[i]);
		}
		for (int j = 0; j < UcontrolPoints.length; j++) {
			UcontrolPoints[j] = MCurve.multiplyVector(UcontrolPoints[j]);
		}
		p = MCurve.multiplyVector(p);
		//p translated to the x-z plane
		double[] pTrans = {p[0],0, p[2],1};
		UcontrolPoints = tranlateToXZPlane(UcontrolPoints);
		// check if projection is in the interior of the surface
		boolean projIn = isInInterior(UcontrolPoints, pTrans);
		
		if(!projIn){
			// check 
			if(Rn.innerProduct(MathUtility.get3DPoint(pTrans), UcontrolPoints[0]) < Rn.innerProduct(MathUtility.get3DPoint(pTrans), UcontrolPoints[UcontrolPoints.length - 1])){	
				for (int j = 0; j < controlPoints.length; j++) {
					controlPoints[j] = Pw[Pw.length - 1][j].clone();
					controlPoints[j] = MCurve.multiplyVector(controlPoints[j]);
				}
				MCurve.invert();
				NURBSCurve nc = new NURBSCurve(controlPoints, ns.getVKnotVector(), ns.getVDegree(), ep);
				closestPoint = PointProjectionCurve.getClosestPoint(nc, p);
				closestPoint = MCurve.multiplyVector(closestPoint);
				return closestPoint;
			}
			MCurve.invert();
			NURBSCurve nc = new NURBSCurve(controlPoints, ns.getVKnotVector(), ns.getVDegree(), ep);
			closestPoint = PointProjectionCurve.getClosestPoint(nc, p);
			closestPoint = MCurve.multiplyVector(closestPoint);
			return closestPoint;
		}
		MatrixBuilder b4 = MatrixBuilder.euclidean();
		double[] e1 = {1,0,0,1};
		NURBSCurve nc = new NURBSCurve(controlPoints, ns.getVKnotVector(), ns.getVDegree(), ep);
		b4.rotateFromTo(pTrans, e1);
		Matrix M4 = b4.getMatrix();
		p = M4.multiplyVector(p);
		closestPoint = PointProjectionCurve.getClosestPoint(nc, p);
		Matrix MPoint = M4;
		MPoint.multiplyOnRight(MCurve);
		MPoint.invert();
		closestPoint = MPoint.multiplyVector(closestPoint);
		return closestPoint;
	}
	
	/**
	 * if this method is called, it is already checked that the surface is a surface of revolution.<br\>
	 * 
	 * @TODO in v direction
	 * @param ns NURBSSurface of revolution
	 * @param point
	 * @return the closest closest point
	 */
	
	public static double[] getClosestPoint(NURBSSurface ns, double[] point){
		if(ns.getRevolutionDir() == RevolutionDir.uDir){
			return getClosestPointUDir(ns, point);
		}
		else{
			return getClosestPointVDir(ns, point);
		}
		
		
	}
	


}
