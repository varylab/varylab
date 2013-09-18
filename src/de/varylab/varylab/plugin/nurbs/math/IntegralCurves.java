package de.varylab.varylab.plugin.nurbs.math;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.ClosingDir;
import de.varylab.varylab.plugin.nurbs.data.ChristoffelInfo;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;
import de.varylab.varylab.plugin.nurbs.data.IntObjects;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;

public class IntegralCurves {
	
	public enum VectorField{curvature, conjugate};

	public static double[] getMaxMinCurv(NURBSSurface ns, double u, double v,boolean max) {
		if (max) {
			return NURBSCurvatureUtility.curvatureAndDirections(ns, u, v).getCurvatureDirectionsDomain()[1];
		} else {	
			return NURBSCurvatureUtility.curvatureAndDirections(ns, u, v).getCurvatureDirectionsDomain()[0];
		}
	}
	
	public static double[] getSymmetricDirection(NURBSSurface ns, double[] p) {
	double[] dir = {1,1};
	if(!ns.isSurfaceOfRevolution()){
		return dir;
	}
	CurvatureInfo ci =  NURBSCurvatureUtility.curvatureAndDirections(ns, p[0], p[1]);
//	double K = ci.getGaussCurvature();
//	if(K > 0){
		double[][] sF = ci.getSecondFundamental();
		double l = sF[0][0];
		double n  = sF[1][1];
//		if(n * l <= 0){
//			System.out.println("n * l <= 0");
//		}
		dir[0] = Math.sqrt(n / l);
		return dir;
//	}
//	else{
//		return getAssymptotic(ns, p);
//	}
	}
	
	
//	private static double[] getAssymptotic(NURBSSurface ns, double[] p){
//		CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(ns, p[0], p[1]);
//		double[][] sF = ci.getSecondFundamental();
//		double l = sF[0][0];
//		double m = sF[0][1];
//		double n = sF[1][1];
//		double dir0 = -(m / l) - Math.sqrt(m * m - l * n) / l;
//		double[] dir = {dir0, 1};
//		return dir;
//	}
	
	
	private static double[] getConj(NURBSSurface ns, double[] v, double[] p){
		CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(ns, p[0], p[1]);
		double[][] sF = ci.getSecondFundamental();
		double[] b = new double[2];
		b[0] = v[0] * sF[0][0] + v[1] * sF[1][0];
		b[1] = v[0] * sF[0][1] + v[1] * sF[1][1];
		double[] w = new double[2];
		w[0] = -b[1];
		w[1] = b[0];
		double[] fu = ci.getSu();
		double[] fv = ci.getSv();
		double[] W = Rn.add(null, Rn.times(null, w[0], fu), Rn.times(null, w[1], fv));
		Rn.normalize(W, W);
		if(Math.abs(1 - Rn.euclideanNorm(W)) > 0.001){
			System.out.println("length: " + Rn.euclideanNorm(w));
		}
		return w;
	}
	
	private static double[] getVecField(NURBSSurface ns, double[] p, boolean conj) {
		double[] vec = getSymmetricDirection(ns, p);
		if(conj){
			return getConj(ns, vec, p);
		}else{
			return vec;
		}
	}
	
	private static boolean segmentIntersectBoundary(LineSegment seg, List<LineSegment> boundary){
		for (LineSegment lS : boundary) {
			if(LineSegmentIntersection.segmentIntersectsLine(seg, lS)){
				System.out.println("segment " + seg.toString());
				System.out.println("boundary " + lS.toString());
				return true;
			}
		}
		return false;
	}
	
	public static double[] intersectionPoint(double[][] line1, double[][] line2){
		double s1 = line1[0][0];
		double s2 = line1[0][1];
		double t1 = line1[1][0];
		double t2 = line1[1][1];
		double p1 = line2[0][0];
		double p2 = line2[0][1];
		double q1 = line2[1][0];
		double q2 = line2[1][1];
		double lambda = ((p1 - s1) * (s2 - t2) - (p2 - s2) * (s1 - t1)) / ((q2 - p2) * (s1 - t1) - (q1 - p1) * (s2 - t2));
		return Rn.add(null, line2[0],Rn.times(null, lambda, Rn.add(null, line2[1], Rn.times(null, -1, line2[0]))));
	}
	
	public static double[] intersectionPoint(LineSegment first, LineSegment second){
		return intersectionPoint(first.getSegment(), second.getSegment());
	}
	
	public static boolean isOutsideOfShiftedDomain(NURBSSurface ns, double[] point){
		if(ns.getClosingDir() == ClosingDir.uClosed){
			double[] V = ns.getVKnotVector();
			double v0 = V[0];
			double vn = V[V.length - 1];
			if(point[1] < v0 || point[1] > vn){
				return true;
			}else{
				return false;
			}
		}
		if(ns.getClosingDir() == ClosingDir.vClosed){
			double[] U = ns.getUKnotVector();
			double u0 = U[0];
			double um = U[U.length - 1];
			if(point[0] < u0 || point[0] > um){
				return true;
			}else{
				return false;
			}
		}
		return true;
	}
	
//	private static double[] projectIntoShiftedDomain(NURBSSurface ns, double[] point){
//		double[] projectedPoint = point.clone();
//		if(ns.getClosingDir() == ClosingDir.uClosed){
//			double[] V = ns.getVKnotVector();
//			double v0 = V[0];
//			double vn = V[V.length - 1];
//			if(projectedPoint[1] < v0){
//				projectedPoint[1] = v0;
//			}
//			if(projectedPoint[1] > vn){
//				projectedPoint[1] = vn;
//			}
//		}
//		else{
//			double[] U = ns.getUKnotVector();
//			double u0 = U[0];
//			double um = U[U.length - 1];
//			if(projectedPoint[0] < u0){
//				projectedPoint[0] = u0;
//			}
//			if(projectedPoint[0] > um){
//				projectedPoint[0] = um;
//			}
//		}
//		return projectedPoint;
//	}
	
	private static boolean isNotAtBoundary(NURBSSurface ns, double[] point){
		LinkedList<Double> boundaryValues = ns.getBoundaryValues();
		for (Double value : boundaryValues) {
			if(point[0] == value || point[1] == value){
				System.out.println();
				System.out.println(" not at boundary,  U = "+ Arrays.toString(ns.getUKnotVector())+" V = " +ns.getVKnotVector()+" point = " + Arrays.toString(point));
				System.out.println();
				return false;
			}
		}
		return true;
	}
	
	private static double getMinBoundValue(double[] point, LinkedList<Double> boundaryValues){
		double minDist = Double.MAX_VALUE;
		double result = Double.MAX_VALUE;
		for (Double value: boundaryValues) {
			if(minDist > Math.min((Math.abs(value - point[0])), (Math.abs(value - point[1])))){
				minDist = Math.min((Math.abs(value - point[0])), (Math.abs(value - point[1])));
				result = value;
			}
		}
		return result;
	}
	
	private static boolean isUBoundaryValue(NURBSSurface ns, double boundaryValue){
		double[] U = ns.getUKnotVector();
		if(boundaryValue == U[0] || boundaryValue == U[U.length -1]){
			return true;
		}
		return false;
	}
	
	private static double[] projectOntoBoundary(NURBSSurface ns, double[] point){
		LinkedList<Double> boundaryValues = ns.getBoundaryValues();
		double boundaryValue = getMinBoundValue(point, boundaryValues);
		
		if(isUBoundaryValue(ns, boundaryValue)){
			point[0] = boundaryValue;
		}
		else{
			point[1] = boundaryValue;
		}
		return point;
	}
	
	
	
	private static double[] boundaryIntersection(NURBSSurface ns, LineSegment seg, List<LineSegment> boundary){
		double minDist = Double.MAX_VALUE;
		double[] intersection = null;
		for (LineSegment lS : boundary) {
			if(Rn.euclideanDistance(seg.getSegment()[0], intersectionPoint(seg, lS)) < minDist){
				intersection = intersectionPoint(seg, lS);
				minDist = Rn.euclideanDistance(seg.getSegment()[0], intersectionPoint(seg, lS));
			}
		}
		if(isNotAtBoundary(ns, intersection)){
			System.out.println("NOT AT BOUNDARY");
			intersection = projectOntoBoundary(ns, intersection);
		}
		return intersection;
	}
	
//	private static double[] boundaryIntersection(NURBSSurface ns, LineSegment seg, List<LineSegment> boundary){
//		double minDist = Double.MAX_VALUE;
//		double[] intersection = null;
//		for (LineSegment lS : boundary) {
//			if(Rn.euclideanDistance(seg.getSegment()[0], intersectionPoint(seg, lS)) < minDist){
//				intersection = intersectionPoint(seg, lS);
//				minDist = Rn.euclideanDistance(seg.getSegment()[0], intersectionPoint(seg, lS));
//			}
//		}
//		if(ns.getClosingDir() == null){
//			if(isOutsideOfDomain(ns, intersection)){
//				intersection = projectPointIntoDomain(ns, intersection);
//			}
//		}else{
//			if(isOutsideOfShiftedDomain(ns, intersection)){
//				intersection = projectIntoShiftedDomain(ns, intersection);
//			}
//		}
//		
//		return intersection;
//	}
	
//	private static boolean isOutsideOfDomain(NURBSSurface ns, double[] point){
//		double u0 = ns.getUKnotVector()[0];
//		double u1 = ns.getUKnotVector()[ns.getUKnotVector().length - 1];
//		double v0 = ns.getVKnotVector()[0];
//		double v1 = ns.getVKnotVector()[ns.getVKnotVector().length - 1];
//		if(point[0] < u0 || point[0] > u1 ||point[1] < v0 || point[1] > v1){
//			return true;
//		}
//		return false;
//	}
//	
//	private static double[] projectPointIntoDomain(NURBSSurface ns, double[] point){
//		double u0 = ns.getUKnotVector()[0];
//		double u1 = ns.getUKnotVector()[ns.getUKnotVector().length - 1];
//		double v0 = ns.getVKnotVector()[0];
//		double v1 = ns.getVKnotVector()[ns.getVKnotVector().length - 1];
//		double[] domainPoint = point;
//		if(point[0] < u0){
//			domainPoint[0] = u0;
//		}
//		else if(point[0] > u1){
//			domainPoint[0] = u1;
//		}
//		else if(point[1] < v0){
//			domainPoint[1] = v0;
//		}
//		else if(point[1] > v1){
//			domainPoint[1] = v1;
//		}
//		return domainPoint;
//	}
	
	private static boolean lineContainsProjectedPoint(double[] src,  double[] fixed){
		double[] start = {0, 0};
		if(src[0] >= start[0] && src[0] <= fixed[0] && src[1] >= start[1] && src[1] <= fixed[1]){
			return true;
		}
		return false;
	}
	
	private static double distLineSegmentPoint(double[] point, LineSegment seg){
		double[] start = seg.getSegment()[0];
		double[] end = seg.getSegment()[1];
		double[] fixed = Rn.subtract(null, end, start);
		double[] src = Rn.subtract(null, point, start);
		double[] projection = Rn.projectOnto(null, src, fixed);
		if(lineContainsProjectedPoint(projection, fixed)){
			return Rn.euclideanDistance(projection, src);
		}
		else{
			return Math.min(Rn.euclideanDistance(start, point), Rn.euclideanDistance(end, point));
		}
	}
	
//	public static double[] goOverUBoundary(double u0, double um, double[] point, boolean left, boolean right){
//		if(point[0] < u0){
//			System.out.println("point = " + Arrays.toString(point));
//			point[0] = um - (u0 - point[0]);
//			System.out.println("go left");
//			left = true;
//		}
//		else if(point[0] > um){
//			System.out.println("point = " + Arrays.toString(point));
//			point[0] = u0 + point[0] - um;
//			System.out.println("go right");
//			right = true;
//		}
//		else{
//			left = false;
//			right = false;
//		}
//		return point;
//	}
//	public static double[] goOverVBoundary(double v0, double vn, double[] point, boolean down, boolean up){
//		if(point[1] < v0){
//			System.out.println("point = " + Arrays.toString(point));
//			point[1] = vn - (v0 - point[1]);
//			System.out.println("NEW point = " + Arrays.toString(point));
//			System.out.println("go down");
//			down = true;
//		}
//		else if(point[1] > vn){
//			System.out.println("point = " + Arrays.toString(point));
//			point[1] = v0 + point[1] - vn;
//			System.out.println("NEW point = " + Arrays.toString(point));
//			System.out.println("go up");
//			up = true;
//		}
//		else{
//			down = false;
//			up = false;
//		}
//		return point;
//	}
	
//	private static boolean terminationCondition(NURBSSurface ns, LineSegment seg, LinkedList<double[]> u, LinkedList<LineSegment> boundary){
//		if(segmentIntersectBoundary(seg, boundary)) {
//			return true;
//		}
//		return false;
//	}
	
	private static boolean terminationCondition(NURBSSurface ns, LineSegment seg, LinkedList<double[]> u, List<LineSegment> boundary){
		if(segmentIntersectBoundary(seg, boundary)) {
			double[] intersection = boundaryIntersection(ns, seg, boundary);
			u.add(intersection);
			return true;
		}
		return false;
	}
	
	
	/**
	* <p><strong>computes the curvature lines</strong></p>
	*
	* this is a step adaptive runge kutta (Bogacki-Shampine) method with
	the following pseudo code</br>
	* </br>
	* <strong>while</strong> (!nearby)
	*
	* @param ns
	* @param y0
	* @param tol
	* @param secondOrientation --> is true if we use the direction of the
	given vectorfield and false if we use the opposite direction
	* @param max
	* @param eps --> if we obtain a closed curve then eps is the maximal
	distance between the start point and the last point
	* @return
	*/
	public static IntObjects rungeKuttaCurvatureLine(NURBSSurface ns, double[] y0,double tol, boolean secondOrientation, boolean max, List<double[]> umbilics, double umbilicStop, List<LineSegment> boundary) {
		double[][] A = { { 0, 0, 0, 0 }, { 0.5, 0, 0, 0 }, { 0, 0.75, 0, 0 },{
			2 / 9., 1 / 3., 4 / 9., 0 } };
		double[] c1 = { 2 / 9., 1 / 3., 4 / 9., 0 };
		double[] c2 = { 7 / 24., 0.25, 1 / 3., 1 / 8. };
	double[] b = { 0, 0.5, 0.75, 1 };
	// double[][] A =	{{0,0,0,0,0,0,0},{1/5.,0,0,0,0,0,0},{3/40.,9/40.,0,0,0,0,0},{44/45.,-56/15.,32/9.,0,0,0,0},{19372/6561.,-25360/2187.,64448/6561.,-212/729.,0,0,0},{9017/3168.,-355/33.,46732/5247.,49/176.,-5103/18656.,0,0},{35/384.,	0, 500/1113., 125/192., -2187/6784., 11/84.,0}};
	// double[] c1 = {35/384., 0, 500/1113., 125/192., -2187/6784., 11/84.,	0 };
	// double[] c2 = {5179/57600., 0, 7571/16695., 393/640.,	-92097/339200., 187/2100., 1/40.};
	// double[] b = { 0,1/5., 3/10.,4/5.,8/9.,1,1 };
	double u0 = ns.getUKnotVector()[0];
	double um = ns.getUKnotVector()[ns.getUKnotVector().length - 1];
	double v0 = ns.getVKnotVector()[0];
	double vn = ns.getVKnotVector()[ns.getVKnotVector().length - 1];
	LinkedList<double[]> u = new LinkedList<double[]>();
	int dim = y0.length;
	double maxDist = Math.min(Math.abs(ns.getUKnotVector()[0] -ns.getUKnotVector()[ns.getUKnotVector().length - 1]), Math.abs(ns.getVKnotVector()[0] - ns.getVKnotVector()[ns.getVKnotVector().length - 1]));
	double h = maxDist / 50;
	double tau;
	double vau;
	double [] vec1 = new double[2];
	double [] vec2 = new double[2];
	boolean closed = false;
	u.add(y0);
	double[] orientation = new double[2];
	if (!secondOrientation) {
		orientation = IntegralCurves.getMaxMinCurv(ns, y0[0], y0[1], max);
	} else {
		orientation = Rn.times(null, -1, IntegralCurves.getMaxMinCurv(ns, y0[0], y0[1], max));
	}
	boolean nearBy = false;
	double dist;
	double[] ori = orientation;
	LineSegment seg = new LineSegment();

	while (!nearBy) {
		double[] v = new double[dim];
		double[] sumA = new double[dim];
		for (int i = 0; i < dim; i++) {
			v[i] = u.getLast()[i]; // initialisiere das AWP v = y0
		}
		double[][] k = new double[b.length][2];

		if (Rn.innerProduct(orientation,IntegralCurves.getMaxMinCurv(ns, v[0], v[1], max)) > 0) {
			k[0] = Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns, v[0],
					v[1], max));
		} else {
			k[0] = Rn.times(null, -1,
					Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns, v[0], v[1], max)));
		}
		double[][] segment = new double[2][2];
		for (int l = 1; l < b.length; l++) {
			sumA = Rn.times(null, A[l][0], k[0]);
			for (int m = 1; m < l - 1; m++) {
				sumA = Rn.add(null, sumA, Rn.times(null, A[l][m], k[m]));
			}
			segment[0][0]= v[0];
			segment[0][1]= v[1];
			segment[1][0]= v[0] + h * sumA[0];
			segment[1][1]= v[1] + h * sumA[1];
			seg.setSegment(segment);
//			if (segmentIntersectBoundary(seg, boundary)) {
//				System.out.println("out of domain 1");
//				double[] intersection = boundaryIntersection(ns, seg, boundary);
//				u.add(intersection);
//				IntObjects intObj = new IntObjects(u, ori, nearBy, max);
//				System.out.println("letztes element: " +
//						Arrays.toString(intObj.getPoints().getLast()));
//				return intObj;
//			}
			if(terminationCondition(ns, seg, u, boundary)){
				System.out.println("out of domain 1");
				u = setIntoDomain(ns, u0, um, v0, vn, u);
				IntObjects intObj = new IntObjects(u, ori, nearBy, max);
				System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
				return intObj;
			}
			if(Rn.innerProduct(orientation,Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns, v[0] + h * sumA[0], v[1] + h * sumA[1], max))) > 0) {
				k[l] = Rn.normalize(null, IntegralCurves.getMaxMinCurv(ns,v[0] + h * sumA[0], v[1] + h * sumA[1], max));
			} else {
				k[l] = Rn.times(null, -1, Rn.normalize(null, IntegralCurves.getMaxMinCurv(ns, v[0] + h * sumA[0], v[1] + h* sumA[1], max)));
			}
		}
		double[] Phi1 = new double[dim];
		double[] Phi2 = new double[dim];
		for (int l = 0; l < b.length; l++) {
			Phi1 = Rn.add(null, Phi1, Rn.times(null, c1[l], k[l]));
			Phi2 = Rn.add(null, Phi2, Rn.times(null, c2[l], k[l]));
		}
		v = Rn.add(null, v, Rn.times(null, h, Phi2));
		tau = Rn.euclideanNorm(Rn.subtract(null, Phi2, Phi1));
		vau = Rn.euclideanNorm(u.getLast()) + 1;
		if (tau <= tol * vau) {
			segment[0] = u.getLast();
			segment[1] = Rn.add(null, u.getLast(), Rn.times(null, h, Phi1));
			seg.setSegment(segment);
			u.add(segment[1]);
			for (double[] umb : umbilics) {
				if(u.size() > 1){
					double[][] lastSegment = new double[2][2];
					lastSegment[1] = u.pollLast();
					lastSegment[0] = u.getLast();
					seg.setSegment(lastSegment);
					dist = distLineSegmentPoint(umb, seg);
					if(dist < umbilicStop){
						u.add(umb);
						IntObjects intObj = new IntObjects(u, ori, nearBy, max);
//						intObj.setUmbilicIndex(umbilics.indexOf(umb));
						System.out.println("near umbilic");
						System.out.println("letztes element: " +
								Arrays.toString(intObj.getPoints().getLast()));
						return intObj;
					}
					else{
						u.add(lastSegment[1]);
					}
				}
			}
//			if (segmentIntersectBoundary(seg, boundary)) {
//				System.out.println("out of domain 2");
//				double[] intersection = boundaryIntersection(ns ,seg, boundary);
//				if(isOutOfDomain(ns, intersection)){
//					intersection = projectPointIntoDomain(ns, intersection);
//				}
//				u.pollLast();
//				u.add(intersection);
//				IntObjects intObj = new IntObjects(u, ori, nearBy, max);
//				System.out.println("letztes element: " +
//						Arrays.toString(intObj.getPoints().getLast()));
//				return intObj;
//			}
			if(terminationCondition(ns, seg, u, boundary)){
				System.out.println("out of domain 2");
				double[] last = u.pollLast();
				u.pollLast();
				u.add(last);
				u = setIntoDomain(ns, u0, um, v0, vn, u);
				IntObjects intObj = new IntObjects(u, ori, nearBy, max);
				System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
				return intObj;
			}
			if (Rn.innerProduct(orientation,IntegralCurves.getMaxMinCurv(ns, u.getLast()[0],u.getLast()[1], max)) > 0) {
				orientation = IntegralCurves.getMaxMinCurv(ns,u.getLast()[0], u.getLast()[1], max);
			} else {
				orientation = Rn.times(null, -1, IntegralCurves.getMaxMinCurv(ns, u.getLast()[0],u.getLast()[1], max));
			}
		}
		if ((tau <= tol * vau / 2 || tau >= tol * vau)) {
			double hOld = h;
			h = h * Math.sqrt(tol * vau / tau);
			if(h > maxDist / 2){
				h = hOld;
			}
		}
		if(u.size() == 2){
			vec1 = Rn.subtract(null, u.getLast(), u.getFirst());
		}
		if(u.size() > 2){
			double[][] lastSegment = new double[2][2];
			lastSegment[1] = u.pollLast();
			lastSegment[0] = u.getLast();
			vec2 = Rn.subtract(null, lastSegment[1], lastSegment[0]);
			seg.setSegment(lastSegment);
			dist = distLineSegmentPoint(y0, seg);
			if(Rn.innerProduct(vec1, vec2) < 0){
				closed = true;
				// System.out.println(" innerproduct > 0");
				// System.out.println("vec1 " + Arrays.toString(vec1));
				// System.out.println("vec2 " + Arrays.toString(vec1));
			}
			if(dist < umbilicStop && closed){
				nearBy = true;
				System.out.println("closed");
				IntObjects intObj = new IntObjects(u, ori, nearBy, max);
				return intObj;
			}
			else{
				u.add(lastSegment[1]);
				}
			}
		}
		System.out.println("u.size() " + u.size());
		IntObjects intObj = new IntObjects(u, ori, nearBy, max);
		System.out.println("letzter Punkt:"+Arrays.toString(intObj.getPoints().getLast()));
		return intObj;
	}
	
	
	public static IntObjects rungeKuttaConjugateLine(NURBSSurface ns, double[] y0, double tol, boolean secondOrientation, boolean conj, List<double[]> umbilics, double umbilicStop, List<LineSegment> boundary) {
	double[][] A = { { 0, 0, 0, 0 }, { 0.5, 0, 0, 0 }, { 0, 0.75, 0, 0 },{
	2 / 9., 1 / 3., 4 / 9., 0 } };
	double[] c1 = { 2 / 9., 1 / 3., 4 / 9., 0 };
	double[] c2 = { 7 / 24., 0.25, 1 / 3., 1 / 8. };
	double[] b = { 0, 0.5, 0.75, 1 };
	// double[][] A =	{{0,0,0,0,0,0,0},{1/5.,0,0,0,0,0,0},{3/40.,9/40.,0,0,0,0,0},{44/45.,-56/15.,32/9.,0,0,0,0},{19372/6561.,-25360/2187.,64448/6561.,-212/729.,0,0,0},{9017/3168.,-355/33.,46732/5247.,49/176.,-5103/18656.,0,0},{35/384.,	0, 500/1113., 125/192., -2187/6784., 11/84.,0}};
	// double[] c1 = {35/384., 0, 500/1113., 125/192., -2187/6784., 11/84.,	0 };
	// double[] c2 = {5179/57600., 0, 7571/16695., 393/640.,	-92097/339200., 187/2100., 1/40.};
	// double[] b = { 0,1/5., 3/10.,4/5.,8/9.,1,1 };
	LinkedList<double[]> pointList = new LinkedList<double[]>();
	int dim = y0.length;
	double u0 = ns.getUKnotVector()[0];
	double um = ns.getUKnotVector()[ns.getUKnotVector().length - 1];
	double v0 = ns.getVKnotVector()[0];
	double vn = ns.getVKnotVector()[ns.getVKnotVector().length - 1];
	double maxDist = Math.min(Math.abs(ns.getUKnotVector()[0] -ns.getUKnotVector()[ns.getUKnotVector().length - 1]), Math.abs(ns.getVKnotVector()[0] - ns.getVKnotVector()[ns.getVKnotVector().length - 1]));
	double h = maxDist / 50;
	double tau;
	double vau;
	double [] vec1 = new double[2];
	double [] vec2 = new double[2];
	boolean closed = false;
	pointList.add(y0);
	double[] orientation = new double[2];
	if (!secondOrientation) {
		orientation = IntegralCurves.getVecField(ns, y0, conj);
	} else {
		orientation = Rn.times(null, -1, IntegralCurves.getVecField(ns, y0, conj));
	}
	boolean nearBy = false;
	double dist;
	double[] ori = orientation;
	LineSegment seg = new LineSegment();

	while (!nearBy) {
//		System.out.println("pointList.size() "+pointList.size());
		double[] v = new double[dim];
		double[] sumA = new double[dim];
		for (int i = 0; i < dim; i++) {
			v[i] = pointList.getLast()[i];
		}
		double[][] k = new double[b.length][2];
		if (Rn.innerProduct(orientation,getVecField(ns, getPointInDomain(u0, um, v0, vn, v), conj)) > 0) {
			k[0] = Rn.normalize(null,getVecField(ns, getPointInDomain(u0, um, v0, vn, v), conj));
		} else {
			k[0] = Rn.times(null, -1,  Rn.normalize(null,getVecField(ns, getPointInDomain(u0, um, v0, vn, v), conj)));
		}
		double[][] segment = new double[2][2];
		for (int l = 1; l < b.length; l++) {
			sumA = Rn.times(null, A[l][0], k[0]);
			for (int m = 1; m < l - 1; m++) {
				sumA = Rn.add(null, sumA, Rn.times(null, A[l][m], k[m]));
			}
			segment[0] = v;
			Rn.add(segment[1], v, Rn.times(null, h, sumA));
			seg.setSegment(segment);
//			if (segmentIntersectBoundary(seg, boundary)) {
//			System.out.println("out of domain 1");
//			double[] intersection = boundaryIntersection(ns, seg, boundary);
//			pointList.add(intersection);
//			IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
//			System.out.println("letztes element: " +
//					Arrays.toString(intObj.getPoints().getLast()));
//			return intObj;
//			}
			if(terminationCondition(ns, seg, pointList, boundary)){
				System.out.println("out of domain 1");
				pointList = setIntoDomain(ns, u0, um, v0, vn, pointList);
				IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
				System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
				return intObj;
			}
			double[] vf = getVecField(ns, getPointInDomain(u0, um, v0, vn, segment[1]), conj);
			if(Rn.innerProduct(orientation,Rn.normalize(null,vf)) > 0) {
				k[l] = Rn.normalize(null, vf);
			} else {
				k[l] = Rn.times(null, -1, Rn.normalize(null, vf));
			}
		}
		double[] Phi1 = new double[dim];
		double[] Phi2 = new double[dim];
		for (int l = 0; l < b.length; l++) {
			Phi1 = Rn.add(null, Phi1, Rn.times(null, c1[l], k[l]));
			Phi2 = Rn.add(null, Phi2, Rn.times(null, c2[l], k[l]));
		}
		v = Rn.add(null, v, Rn.times(null, h, Phi2));
		tau = Rn.euclideanNorm(Rn.subtract(null, Phi2, Phi1));
		vau = Rn.euclideanNorm(pointList.getLast()) + 1;
		if (tau <= tol * vau) {
			segment[0] = pointList.getLast();
			segment[1] = Rn.add(null, pointList.getLast(), Rn.times(null, h, Phi1));
			seg.setSegment(segment);
			if(terminationCondition(ns, seg, pointList, boundary)){
				System.out.println("out of domain 2");
				double[] last = pointList.pollLast();
				pointList.pollLast();
				pointList.add(last);
				pointList = setIntoDomain(ns, u0, um, v0, vn, pointList);
				IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
				System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
				return intObj;
			}
			pointList.add(segment[1]);
			if (Rn.innerProduct(orientation, getVecField(ns, getPointInDomain(u0, um, v0, vn, pointList.getLast()), conj)) > 0) {
				orientation = getVecField(ns, getPointInDomain(u0, um, v0, vn, pointList.getLast()), conj);
			} else {
				orientation = Rn.times(null, -1, getVecField(ns, getPointInDomain(u0, um, v0, vn, pointList.getLast()), conj));
			}
		}
		if ((tau <= tol * vau / 2 || tau >= tol * vau)) {
			double hOld = h;
			h = h * Math.sqrt(tol * vau / tau);
			if(h > maxDist / 2){
				h = hOld;
			}
		}
		if(pointList.size() == 2){
			vec1 = Rn.subtract(null, pointList.getLast(), pointList.getFirst());
		}
		if(pointList.size() > 2){
			double[][] lastSegment = new double[2][2];
			lastSegment[1] = pointList.pollLast();
			lastSegment[0] = pointList.getLast();
			vec2 = Rn.subtract(null, lastSegment[1], lastSegment[0]);
			seg.setSegment(lastSegment);
			dist = distLineSegmentPoint(y0, seg);
			if(Rn.innerProduct(vec1, vec2) < 0){
				closed = true;
			}
			if(dist < umbilicStop && closed){
				nearBy = true;
				System.out.println("closed");
				pointList = setIntoDomain(ns, u0, um, v0, vn, pointList);
				IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
				return intObj;
			}
			else{
				pointList.add(lastSegment[1]);
			}
		}
	}
		System.out.println("u.size() " + pointList.size());
		pointList = setIntoDomain(ns, u0, um, v0, vn, pointList);
		IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
		System.out.println("letzter Punkt:"+Arrays.toString(intObj.getPoints().getLast()));
		return intObj;
	}
	
	
	public static IntObjects rungeKutta(NURBSSurface ns, double[] y0, double tol, boolean secondOrientation, boolean conj, List<double[]> umbilics, double umbilicStop, List<LineSegment> boundary) {
	double[][] A = { { 0, 0, 0, 0 }, { 0.5, 0, 0, 0 }, { 0, 0.75, 0, 0 },{
	2 / 9., 1 / 3., 4 / 9., 0 } };
	double[] c1 = { 2 / 9., 1 / 3., 4 / 9., 0 };
	double[] c2 = { 7 / 24., 0.25, 1 / 3., 1 / 8. };
	double[] b = { 0, 0.5, 0.75, 1 };
	// double[][] A =	{{0,0,0,0,0,0,0},{1/5.,0,0,0,0,0,0},{3/40.,9/40.,0,0,0,0,0},{44/45.,-56/15.,32/9.,0,0,0,0},{19372/6561.,-25360/2187.,64448/6561.,-212/729.,0,0,0},{9017/3168.,-355/33.,46732/5247.,49/176.,-5103/18656.,0,0},{35/384.,	0, 500/1113., 125/192., -2187/6784., 11/84.,0}};
	// double[] c1 = {35/384., 0, 500/1113., 125/192., -2187/6784., 11/84.,	0 };
	// double[] c2 = {5179/57600., 0, 7571/16695., 393/640.,	-92097/339200., 187/2100., 1/40.};
	// double[] b = { 0,1/5., 3/10.,4/5.,8/9.,1,1 };
	LinkedList<double[]> pointList = new LinkedList<double[]>();
	int dim = y0.length;
	double u0 = ns.getUKnotVector()[0];
	double um = ns.getUKnotVector()[ns.getUKnotVector().length - 1];
	double v0 = ns.getVKnotVector()[0];
	double vn = ns.getVKnotVector()[ns.getVKnotVector().length - 1];
	double maxDist = Math.min(Math.abs(ns.getUKnotVector()[0] -ns.getUKnotVector()[ns.getUKnotVector().length - 1]), Math.abs(ns.getVKnotVector()[0] - ns.getVKnotVector()[ns.getVKnotVector().length - 1]));
	double h = maxDist / 50;
	double tau;
	double vau;
	double [] vec1 = new double[2];
	double [] vec2 = new double[2];
	boolean closed = false;
	pointList.add(y0);
	double[] orientation = new double[2];
	if (!secondOrientation) {
		orientation = IntegralCurves.getVecField(ns, y0, conj);
	} else {
		orientation = Rn.times(null, -1, IntegralCurves.getVecField(ns, y0, conj));
	}
	boolean nearBy = false;
	double dist;
	double[] ori = orientation;
	LineSegment seg = new LineSegment();

	while (!nearBy) {
//		System.out.println("pointList.size() "+pointList.size());
		double[] v = new double[dim];
		double[] sumA = new double[dim];
		for (int i = 0; i < dim; i++) {
			v[i] = pointList.getLast()[i];
		}
		double[][] k = new double[b.length][2];
		if (Rn.innerProduct(orientation,getVecField(ns, getPointInDomain(u0, um, v0, vn, v), conj)) > 0) {
			k[0] = Rn.normalize(null,getVecField(ns, getPointInDomain(u0, um, v0, vn, v), conj));
		} else {
			k[0] = Rn.times(null, -1,  Rn.normalize(null,getVecField(ns, getPointInDomain(u0, um, v0, vn, v), conj)));
		}
		double[][] segment = new double[2][2];
		for (int l = 1; l < b.length; l++) {
			sumA = Rn.times(null, A[l][0], k[0]);
			for (int m = 1; m < l - 1; m++) {
				sumA = Rn.add(null, sumA, Rn.times(null, A[l][m], k[m]));
			}
			segment[0] = v;
			Rn.add(segment[1], v, Rn.times(null, h, sumA));
			seg.setSegment(segment);
//			if (segmentIntersectBoundary(seg, boundary)) {
//			System.out.println("out of domain 1");
//			double[] intersection = boundaryIntersection(ns, seg, boundary);
//			pointList.add(intersection);
//			IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
//			System.out.println("letztes element: " +
//					Arrays.toString(intObj.getPoints().getLast()));
//			return intObj;
//			}
			if(terminationCondition(ns, seg, pointList, boundary)){
				System.out.println("out of domain 1");
				pointList = setIntoDomain(ns, u0, um, v0, vn, pointList);
				IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
				System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
				return intObj;
			}
			double[] vf = getVecField(ns, getPointInDomain(u0, um, v0, vn, segment[1]), conj);
			if(Rn.innerProduct(orientation,Rn.normalize(null,vf)) > 0) {
				k[l] = Rn.normalize(null, vf);
			} else {
				k[l] = Rn.times(null, -1, Rn.normalize(null, vf));
			}
		}
		double[] Phi1 = new double[dim];
		double[] Phi2 = new double[dim];
		for (int l = 0; l < b.length; l++) {
			Phi1 = Rn.add(null, Phi1, Rn.times(null, c1[l], k[l]));
			Phi2 = Rn.add(null, Phi2, Rn.times(null, c2[l], k[l]));
		}
		v = Rn.add(null, v, Rn.times(null, h, Phi2));
		tau = Rn.euclideanNorm(Rn.subtract(null, Phi2, Phi1));
		vau = Rn.euclideanNorm(pointList.getLast()) + 1;
		if (tau <= tol * vau) {
			segment[0] = pointList.getLast();
			segment[1] = Rn.add(null, pointList.getLast(), Rn.times(null, h, Phi1));
			seg.setSegment(segment);
			if(terminationCondition(ns, seg, pointList, boundary)){
				System.out.println("out of domain 2");
				double[] last = pointList.pollLast();
				pointList.pollLast();
				pointList.add(last);
				pointList = setIntoDomain(ns, u0, um, v0, vn, pointList);
				IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
				System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
				return intObj;
			}
			pointList.add(segment[1]);
			if (Rn.innerProduct(orientation, getVecField(ns, getPointInDomain(u0, um, v0, vn, pointList.getLast()), conj)) > 0) {
				orientation = getVecField(ns, getPointInDomain(u0, um, v0, vn, pointList.getLast()), conj);
			} else {
				orientation = Rn.times(null, -1, getVecField(ns, getPointInDomain(u0, um, v0, vn, pointList.getLast()), conj));
			}
		}
		if ((tau <= tol * vau / 2 || tau >= tol * vau)) {
			double hOld = h;
			h = h * Math.sqrt(tol * vau / tau);
			if(h > maxDist / 2){
				h = hOld;
			}
		}
		if(pointList.size() == 2){
			vec1 = Rn.subtract(null, pointList.getLast(), pointList.getFirst());
		}
		if(pointList.size() > 2){
			double[][] lastSegment = new double[2][2];
			lastSegment[1] = pointList.pollLast();
			lastSegment[0] = pointList.getLast();
			vec2 = Rn.subtract(null, lastSegment[1], lastSegment[0]);
			seg.setSegment(lastSegment);
			dist = distLineSegmentPoint(y0, seg);
			if(Rn.innerProduct(vec1, vec2) < 0){
				closed = true;
				// System.out.println(" innerproduct > 0");
				// System.out.println("vec1 " + Arrays.toString(vec1));
				// System.out.println("vec2 " + Arrays.toString(vec1));
			}
			if(dist < umbilicStop && closed){
				nearBy = true;
				System.out.println("closed");
				pointList = setIntoDomain(ns, u0, um, v0, vn, pointList);
				IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
				return intObj;
			}
			else{
				pointList.add(lastSegment[1]);
			}
		}
	}
		System.out.println("u.size() " + pointList.size());
		pointList = setIntoDomain(ns, u0, um, v0, vn, pointList);
		IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
		System.out.println("letzter Punkt:"+Arrays.toString(intObj.getPoints().getLast()));
		return intObj;
	}
	
//	/**
//	 * <p><strong>computes the curvature lines</strong></p>
//	 * 
//	 * this is a step adaptive runge kutta (Bogacki–Shampine) method 
//
//	 * 
//	 * @param ns
//	 * @param y0 
//	 * @param tol
//	 * @param secondOrientation --> is true if we use the direction of the given vectorfield an false if we use the opposite direction 
//	 * @param max 
//	 * @param eps --> if we obtain a closed curve then eps is the maximal distance between the start point and the last point
//	 * @return
//	 */
////	}
//	
//	public static IntObjects rungeKuttaConjugateLine(NURBSSurface ns, double[] y0,double tol, boolean secondOrientation, boolean conj, List<double[]> umbilics, double umbilicStop, LinkedList<LineSegment> boundary) {
//		double[][] A = { { 0, 0, 0, 0 }, { 0.5, 0, 0, 0 }, { 0, 0.75, 0, 0 },{ 2 / 9., 1 / 3., 4 / 9., 0 } };
//		double[] c1 = { 2 / 9., 1 / 3., 4 / 9., 0 };
//		double[] c2 = { 7 / 24., 0.25, 1 / 3., 1 / 8. };
//		double[] b = { 0, 0.5, 0.75, 1 };
//		double[] vecField = getVecField(ns, y0[0], y0[1], conj);
////		double[][] A = {{0,0,0,0,0,0,0},{1/5.,0,0,0,0,0,0},{3/40.,9/40.,0,0,0,0,0},{44/45.,-56/15.,32/9.,0,0,0,0},{19372/6561.,-25360/2187.,64448/6561.,-212/729.,0,0,0},{9017/3168.,-355/33.,46732/5247.,49/176.,-5103/18656.,0,0},{35/384., 0, 500/1113., 125/192., -2187/6784., 11/84.,0}};
////		double[] c1 = {35/384., 0, 500/1113., 125/192., -2187/6784., 11/84., 0 };
////		double[] c2 = {5179/57600., 0, 7571/16695., 393/640., -92097/339200., 187/2100., 1/40.};
////		double[] b = { 0,1/5., 3/10.,4/5.,8/9.,1,1 };
//		boolean left = false;
//		boolean right = false;
//		boolean down = false;
//		boolean up = false;
//		LinkedList<double[]> u = new LinkedList<double[]>();
////		int dim = y0.length;
//		System.out.println("y0 " + Arrays.toString(y0));
//		System.out.println(" surface point in rungekutta " + Arrays.toString(ns.getSurfacePoint(y0[0], y0[1])));
//		double maxDist = Math.min(Math.abs(ns.getUKnotVector()[0] - ns.getUKnotVector()[ns.getUKnotVector().length - 1]), Math.abs(ns.getVKnotVector()[0] - ns.getVKnotVector()[ns.getVKnotVector().length - 1]));
//		double h = maxDist / 50;
//		double tau;
//		double vau;
//		u.add(y0);
//		double[] orientation = new double[2];
//		if (!secondOrientation) {
//			orientation = vecField;
//		} else {
//			Rn.times(orientation, -1, vecField);
//		}
//		double[] U = ns.getUKnotVector();
//		double[] V = ns.getVKnotVector();
//		double u0 = U[0];
//		double um = U[U.length - 1];
//		double v0 = V[0];
//		double vn = V[V.length - 1];
//		if(y0[0] < u0 || y0[0] > um || y0[1] < v0 || y0[1] > vn){
//			System.out.println(" OUT OF DOMAIN !");
//		}
//		boolean nearBy = false;
//		double[] ori = orientation;
//		LineSegment seg = new LineSegment();
//		
//		while (!nearBy) {
//			double[] v = u.getLast();
//			if(u.size() > 1){
//				u.pollLast();
//				double[] last = u.getLast();
//				if(Math.abs(Rn.euclideanDistance(v, last))< 0.00001){
//					System.out.println("v " + Arrays.toString(v));
//					System.out.println("last " + Arrays.toString(last));
//				}
//				u.add(v);
//			}
//			double[] sumA = new double[2];
//			double[][] k = new double[b.length][2];
//			if (Rn.innerProduct(orientation,getVecField(ns, v[0], v[1], conj)) > 0) {
//				Rn.normalize(k[0],getVecField(ns, v[0], v[1], conj));
//			} else {
//				Rn.times(k[0], -1, Rn.normalize(null,getVecField(ns, v[0], v[1], conj)));
//			}
//			double[][] segment = new double[2][2];
//			for (int l = 1; l < b.length; l++) {
//				sumA = Rn.times(null, A[l][0], k[0]);
//				for (int m = 1; m < l - 1; m++) {
//					sumA = Rn.add(null, sumA, Rn.times(null, A[l][m], k[m]));
//				}
//				segment[0]= v;
//				Rn.add(segment[1], v, Rn.times(null, h, sumA));
//				if(ns.getClosingDir() == ClosingDir.uClosed){
//					segment[1] = goOverUBoundary(u0, um, segment[1],left,right);
//					if(left){
//						double[][] leftSegment = {{u0,v0},{u0,vn}};
//					}
//					else if(right){
//						double[][] rightSegment = {{um,v0},{um,vn}};
//					}
//					else{
//					seg.setSegment(segment);
//					if(terminationCondition(ns, seg, u, boundary)){
//						System.out.println("out of domain 1");
//						IntObjects intObj = new IntObjects(u, ori, nearBy, conj);
//						System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
//						return intObj;
//						}
//					}
////					
//				}
//				else if(ns.getClosingDir() == ClosingDir.vClosed){
//					double[] finalPoint = goOverVBoundary(v0, vn, segment[1],down,up);
//					double[][] shiftedSegment = {segment[0], finalPoint};
//					LineSegment shiftedSeg = new LineSegment();
//					shiftedSeg.setSegment(shiftedSegment);
//					if(down){
//						System.out.println("DDDDDOOOOWWWWWNNN");
//						System.out.println("down");
//						double[][] downSegment = {{u0,v0},{um,v0}};
//						LineSegment downSeg = new LineSegment();
//						downSeg.setSegment(downSegment);
//						LinkedList<LineSegment> downList = new LinkedList<LineSegment>();
//						downList.add(downSeg);
//						double[] intersection = boundaryIntersection(ns, downSeg, downList);
//						double [][] firstSegment = {segment[0], intersection};
//						LineSegment firstSeg = new LineSegment();
//						firstSeg.setSegment(firstSegment);
//						if(terminationCondition(ns, firstSeg, u, boundary)){
//							System.out.println("out of domain 1 VBoundary first segment down");
//							IntObjects intObj = new IntObjects(u, ori, nearBy, conj);
//							System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
//							return intObj;
//						}
//						u.add(intersection);
//						System.out.println();
//						System.out.println("u.add(intersection) down" + Arrays.toString(intersection));
//						System.out.println();
//						double[] second = {intersection[0], vn};
//						double[][] secondSegment = {second, segment[1]};
//						LineSegment secondSeg = new LineSegment();
//						secondSeg.setSegment(secondSegment);
//						if(terminationCondition(ns, secondSeg, u, downList)){
//							System.out.println("out of domain 1 VBoundary second segment down");
//							IntObjects intObj = new IntObjects(u, ori, nearBy, conj);
//							System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
//							return intObj;
//						}
//						u.add(second);
//						System.out.println();
//						System.out.println("u.add(second) down" + Arrays.toString(second));
//						System.out.println();
//						u.add(segment[1]);
//						
//					}
//					else if(up){
//						System.out.println("up");
//						double[][] upSegment = {{u0,vn},{um,vn}};
//						LineSegment upSeg = new LineSegment();
//						upSeg.setSegment(upSegment);
//						LinkedList<LineSegment> upList = new LinkedList<LineSegment>();
//						upList.add(upSeg);
//						double[] intersection = boundaryIntersection(ns, upSeg, upList);
//						double [][] firstSegment = {segment[0], intersection};
//						LineSegment firstSeg = new LineSegment();
//						firstSeg.setSegment(firstSegment);
//						if(terminationCondition(ns, firstSeg, u, upList)){
//							System.out.println("out of domain 1 VBoundary first segment up");
//							IntObjects intObj = new IntObjects(u, ori, nearBy, conj);
//							System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
//							return intObj;
//						}
//
//						u.add(intersection);
//						System.out.println();
//						System.out.println("u.add(intersection) up" + Arrays.toString(intersection));
//						System.out.println();
//						double[] second = {intersection[0], v0};
//						double[][] secondSegment = {second, segment[1]};
//						LineSegment secondSeg = new LineSegment();
//						secondSeg.setSegment(secondSegment);
//						if(terminationCondition(ns, secondSeg, u, upList)){
//							System.out.println("out of domain 1 VBoundary second segment up");
//							IntObjects intObj = new IntObjects(u, ori, nearBy, conj);
//							System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
//							return intObj;
//						}
//						u.add(second);
//						System.out.println();
//						System.out.println("u.add(second) up" + Arrays.toString(second));
//						System.out.println();
//						u.add(segment[1]);
//					}
//					else{
//						seg.setSegment(segment);
//						if(terminationCondition(ns, seg, u, boundary)){
//							System.out.println("out of domain 1");
//							IntObjects intObj = new IntObjects(u, ori, nearBy, conj);
//							System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
//							return intObj;
//						}
//					}
//				}
//				else{
//					seg.setSegment(segment);
//					if(terminationCondition(ns, seg, u, boundary)){
//						System.out.println("out of domain 1");
//						IntObjects intObj = new IntObjects(u, ori, nearBy, conj);
//						System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
//						return intObj;
//					}
//				}
//				
//				if (Rn.innerProduct(orientation,Rn.normalize(null,getVecField(ns, segment[1][0], segment[1][1], conj))) > 0) {
//					k[l] = Rn.normalize(null, getVecField(ns, segment[1][0], segment[1][1], conj));
//				} else  {
//					k[l] = Rn.times(null, -1, Rn.normalize(null, getVecField(ns,  segment[1][0], segment[1][1], conj)));
//				}
//			}
//			double[] Phi1 = new double[2];
//			double[] Phi2 = new double[2];
//			for (int l = 0; l < b.length; l++) {
//				Phi1 = Rn.add(null, Phi1, Rn.times(null, c1[l], k[l]));
//				Phi2 = Rn.add(null, Phi2, Rn.times(null, c2[l], k[l]));
//			}
//			v = Rn.add(null, v, Rn.times(null, h, Phi2));
//			tau = Rn.euclideanNorm(Rn.subtract(null, Phi2, Phi1));
//			vau = Rn.euclideanNorm(u.getLast()) + 1;
//			if (tau <= tol * vau) {
//				segment[0] = u.getLast();
//				segment[1] = Rn.add(null, u.getLast(), Rn.times(null, h, Phi1));
//				if(ns.getClosingDir() == ClosingDir.uClosed){
//					segment[1] = goOverUBoundary(u0, um, segment[1], left,right);
//				}
//				if(ns.getClosingDir() == ClosingDir.vClosed){
//					segment[1] = goOverVBoundary(v0, vn, segment[1],down,up);
//				}
//				seg.setSegment(segment);
////				
//				if(terminationCondition(ns, seg, u, boundary)){
//					System.out.println("out of domain 2");
//					IntObjects intObj = new IntObjects(u, ori, nearBy, conj);
//					System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
//					return intObj;
//				}
//				u.add(segment[1]);
//				if (Rn.innerProduct(orientation,getVecField(ns, u.getLast()[0],u.getLast()[1], conj)) > 0) {
//					orientation = getVecField(ns,u.getLast()[0], u.getLast()[1], conj);
//				} else {
//					orientation = Rn.times(null, -1, getVecField(ns, u.getLast()[0],u.getLast()[1], conj));
//				}
//			}
//			if ((tau <= tol * vau / 2 || tau >= tol * vau)) {
//				double hOld = h;
//				h = h * Math.sqrt(tol * vau / tau);
//				if(h > maxDist / 2){
//					h = hOld;
//				}
//			}
//		}
//		System.out.println("u.size() " + u.size());
//		IntObjects intObj = new IntObjects(u, ori, nearBy, conj);
//		System.out.println("letzter Punkt: "+Arrays.toString(intObj.getPoints().getLast()));
//		return intObj;
//	}
	
//	public static LinkedList<double[]> setIntoDomain(double u0, double um, double v0, double vn ,LinkedList<double[]> pointList){
//		LinkedList<double[]> domainList = new LinkedList<double[]>();
//		for (double[] p : pointList) {
//			double[] domainPoint = getPointInDomain(u0, um, v0, vn, p);
//			if(pointIsInDomain(u0, um, v0, vn, domainPoint)){
//				domainList.add(domainPoint);
//			}
//			else{
//				System.out.println("domainPoint " + Arrays.toString(domainPoint));
//			}
//		}
//		return domainList;
//		
//	}
	
	public static double modInterval(double left, double right, double x){
		double xShift = x - left;
		double length = right - left;
		double mod = xShift % length;
		if(Math.signum(mod) < 0){
//			System.out.println("KLEINER NULL");
			return right + mod;
		}
		else{
			if(mod >= length){
				System.out.println("GROESSER LENGTH");
			}
			return left + mod;
		}
	}
	
//	public static int getModInterval(double left, double right, double x){
//		double xShift = x - left;
//		double interval = right - left;
////		if(xShift >= 0 && xShift <= interval){
////			return 0;
////		}
//		if(Math.signum(xShift) < 0){
//			xShift = xShift - interval;
//			return (int)(xShift / interval);
//		}
//		else{
//			int mod = (int)(-xShift / interval);
//			System.out.println("mod = " + mod);
//			return -mod;
//		}
//		
////		double xShift = x - left;
////		double interval = right - left;
////		if(Math.signum(xShift) < 0){
////			xShift = xShift - interval;
////		}
////		if(xShift % interval == 0){
////			System.out.println("shit");
////		}
////		return (int)(xShift / interval);
//
//		
//	}
	
	public static int getModInterval(double left, double right, double x){
		if(x >= left && x <= right){
			return 0;
		}
		else{
			double length = right - left;
			double newLeft = left;
			double newRight = right;
			int index = 0;
			if(x < left){
				while(x < newLeft){
					newLeft = newLeft - length;
					index--;
					if(x >= newLeft){
						return index;
					}
				}
			}
			else{
				while(x > newRight){
					newRight = newRight + length;
					index++;
					if(x <= newRight){
						return index;
					}
				}
				return index;
			}
		}
		return 0;
	}
	
	public static int[] getModDomain(double u0, double um, double v0, double vn, double [] point){
		int[] domain = new int[2];
		domain[0] = getModInterval(u0, um, point[0]);
		domain[1] = getModInterval(v0, vn, point[1]);
		return domain;
	}
	
	
	public static boolean pointsAreInDifferentDomains(double u0, double um, double v0, double vn, double[] point1, double[] point2){
		int[] domain1 = getModDomain(u0, um, v0, vn, point1);
		int[] domain2 = getModDomain(u0, um, v0, vn, point2);
		if(domain1[0] == domain2[0] && domain1[1] == domain2[1]){
			return false;
		}
		return true;
	}
	
//	public static boolean pointsAreInDifferentUDomains(double u0, double um, double v0, double vn, double[] point1, double[] point2){
//		int[] domain1 = getModDomain(u0, um, v0, vn, point1);
//		int[] domain2 = getModDomain(u0, um, v0, vn, point2);
//		if(domain1[0] == domain2[0]){
//			return false;
//		}
//		return true;
//	}
	
//	public static double getShiftedBound(double domain1, double domain2, double){
//		return 0;
//	}
	
	public static double[][] getShiftedBoundaryIntersectionPoints(double u0, double um, double v0, double vn, double[] point1, double[] point2){
		System.out.println("GET SHIFTED");
		double[][] intersectionPoints = new double[2][2];
		int[] domain1 = getModDomain(u0, um, v0, vn, point1);
		int[] domain2 = getModDomain(u0, um, v0, vn, point2);
		double[][] seg = new double[2][2];
		if(domain1[0] > domain2[0]){ // left
			double Shift =  um - u0;
			seg[0] = getPointInDomain(u0, um, v0, vn, point1);
			seg[1] = getPointInDomain(u0, um, v0, vn, point2);
			seg[1][0] = seg[1][0] - Shift;
			double[][] line = {{u0,v0},{u0,vn}};
			double[] leftIntersection = intersectionPoint(seg, line);
			if(leftIntersection[0] != u0){
				leftIntersection[0] = u0;
			}
			double[] rightIntersection = {um, leftIntersection[1]};
			intersectionPoints[0] = leftIntersection;
			intersectionPoints[1] = rightIntersection;
			System.out.println("left");
			
		}else if(domain1[0] < domain2[0]){ // right
			double Shift =  um - u0;
			seg[0] = getPointInDomain(u0, um, v0, vn, point1);
			seg[1] = getPointInDomain(u0, um, v0, vn, point2);
			seg[1][0] = seg[1][0] + Shift;
			double[][] line = {{um,v0},{um,vn}};
			double[] rightIntersection = intersectionPoint(seg, line);
			if(rightIntersection[0] != um){
				rightIntersection[0] = um;
			}
			double[] leftIntersection = {u0, rightIntersection[1]};
			intersectionPoints[0] = rightIntersection;
			intersectionPoints[1] = leftIntersection;
			System.out.println("right");
		}else if(domain1[1] > domain2[1]){ // lower
			double Shift =  vn - v0;
			seg[0] = getPointInDomain(u0, um, v0, vn, point1);
			seg[1] = getPointInDomain(u0, um, v0, vn, point2);
			seg[1][1] = seg[1][1] - Shift;
			double[][] line = {{u0,v0},{um,v0}};
			double[] lowerIntersection = intersectionPoint(seg, line);
			if(lowerIntersection[1] != v0){
				lowerIntersection[1] = v0;
			}
			double[] upperIntersection = {lowerIntersection[0],vn};
			intersectionPoints[0] = lowerIntersection;
			intersectionPoints[1] = upperIntersection;
			System.out.println("lower");
		}
		else{ // upper
			double Shift =  vn - v0;
			seg[0] = getPointInDomain(u0, um, v0, vn, point1);
			seg[1] = getPointInDomain(u0, um, v0, vn, point2);
			seg[1][1] = seg[1][1] + Shift;
			double[][] line = {{u0,vn},{um,vn}};
			double[] upperIntersection = intersectionPoint(seg, line);
			if(upperIntersection[1] != vn){
				upperIntersection[1] = vn;
			}
			double[] lowerIntersection = {upperIntersection[0],v0};
			intersectionPoints[0] = upperIntersection;
			intersectionPoints[1] = lowerIntersection;
			System.out.println("upper");
		}
		return intersectionPoints;
	}

	private static void flipClosedBoundaryPoint(NURBSSurface ns, double u0, double um, double v0, double vn, double[] point){
		if(ns.getClosingDir() == ClosingDir.uClosed){
			if(point[0] == u0){
				point[0] = um;
			}
			else{
				point[0] = u0;
			}
		}
		if(ns.getClosingDir() == ClosingDir.vClosed){
			if(point[1] == v0){
				point[1] = vn;
			}
			else{
				point[1] = v0;
			}
		}
	}
	
	private static LinkedList<double[]> setIntoDomain(NURBSSurface ns, double u0, double um, double v0, double vn, LinkedList<double[]> pointList){
		LinkedList<double[]> domainList = new LinkedList<double[]>();
		int counter = 0;
		double[][] seg = new double[2][2];
		domainList.add(pointList.getFirst());
		seg[0] = pointList.getFirst().clone();
		domainList.add(pointList.getFirst());
		for (double[] p : pointList) {
			if(counter != 0){
				seg[1] = p.clone();
				if(ns.isClosedBoundaryPoint(seg[0]) && pointsAreInDifferentDomains(u0, um, v0, vn, seg[0], seg[1])){
					flipClosedBoundaryPoint(ns, u0, um, v0, vn, seg[0]);
//					domainList.add(seg[0]);
					domainList.add(getPointInDomain(u0, um, v0, vn, seg[1]));
				} else {
					if(pointsAreInDifferentDomains(u0, um, v0, vn, seg[0], seg[1])){
						double[][] intersections = getShiftedBoundaryIntersectionPoints(u0, um, v0, vn, seg[0], seg[1]);
						domainList.add(intersections[0]);
						domainList.add(intersections[1]);
					}
					double[] domainPoint = getPointInDomain(u0, um, v0, vn, p);
					double[] check = domainList.getLast();
					if(!Arrays.equals(check, domainPoint)){
						domainList.add(domainPoint);
					}
					else{
						System.out.println("DOPPEL PUNKT");
					}
				}
				seg[0] = p.clone();
			}
			counter++;
		}
		return domainList;
	}
	
	public static boolean pointIsInU(double u0, double um, double[] point){
		if(point[0] < u0 || point[0] > um){
			return false;
		}
		return true;
	}
	
	public static boolean pointIsInV(double v0, double vn, double[] point){
		if(point[1] < v0 || point[1] > vn){
			return false;
		}
		return true;
	}
	
	public static boolean pointIsInDomain(double u0, double um, double v0, double vn, double[] point){
		if(!pointIsInU(u0, um, point) || !pointIsInV(v0, vn, point)){
			return false;
		}
		return true;
	}
	public static double[] getPointInDomain(double u0, double um , double v0, double vn, double[] point){
		double[] domainPoint = {point[0], point[1]};
		if(pointIsInDomain(u0, um, v0, vn, point)){
			return domainPoint;
		}
		if(!pointIsInU(u0, um, point)){
			domainPoint[0] = modInterval(u0, um, point[0]);
		}
		if(!pointIsInV(v0, vn, point)){
			domainPoint[1] = modInterval(v0, vn, point[1]);
		}
		return domainPoint;
	}
	
//	public static LinkedList<LineSegment> createCurveSegmentsInDomain(NURBSSurface ns, LinkedList<double[]> pointList){
//		LinkedList<LineSegment> segList = new LinkedList<LineSegment>();
//		double[] firstPoint = pointList.getFirst();
//		boolean first = true;
//		
//		for (double[] u : pointList) {
//			if(!first){
//				double[][] segment = {}
//			}
//			first = false;
//			
//		}
//		return segList;
//	}
	
	/**
	 * <p><strong>computes the curvature lines</strong></p>
	 * 
	 * this is a step adaptive runge kutta (Bogacki–Shampine) method 
	 * 
	 * @param ns
	 * @param y0 
	 * @param tol
	 * @param secondOrientation --> is true if we use the direction of the given vectorfield an false if we use the opposite direction 
	 * @param max 
	 * @param eps --> if we obtain a closed curve then eps is the maximal distance between the start point and the last point
	 * @return
	 */
//	}
	
//	public static IntObjects rungeKutta(NURBSSurface ns, double[] y0,double tol, boolean secondOrientation, boolean conj, List<double[]> umbilics, double umbilicStop, LinkedList<LineSegment> boundary) {
//		double[][] A = { { 0, 0, 0, 0 }, { 0.5, 0, 0, 0 }, { 0, 0.75, 0, 0 },{ 2 / 9., 1 / 3., 4 / 9., 0 } };
//		double[] c1 = { 2 / 9., 1 / 3., 4 / 9., 0 };
//		double[] c2 = { 7 / 24., 0.25, 1 / 3., 1 / 8. };
//		double[] b = { 0, 0.5, 0.75, 1 };
//		double[] vecField = getVecField(ns, y0[0], y0[1], conj);
////		double[][] A = {{0,0,0,0,0,0,0},{1/5.,0,0,0,0,0,0},{3/40.,9/40.,0,0,0,0,0},{44/45.,-56/15.,32/9.,0,0,0,0},{19372/6561.,-25360/2187.,64448/6561.,-212/729.,0,0,0},{9017/3168.,-355/33.,46732/5247.,49/176.,-5103/18656.,0,0},{35/384., 0, 500/1113., 125/192., -2187/6784., 11/84.,0}};
////		double[] c1 = {35/384., 0, 500/1113., 125/192., -2187/6784., 11/84., 0 };
////		double[] c2 = {5179/57600., 0, 7571/16695., 393/640., -92097/339200., 187/2100., 1/40.};
////		double[] b = { 0,1/5., 3/10.,4/5.,8/9.,1,1 };
//		boolean left = false;
//		boolean right = false;
//		boolean down = false;
//		boolean up = false;
//		LinkedList<double[]> u = new LinkedList<double[]>();
//		int dim = y0.length;
//		System.out.println("y0 " + Arrays.toString(y0));
//		double maxDist = Math.min(Math.abs(ns.getUKnotVector()[0] - ns.getUKnotVector()[ns.getUKnotVector().length - 1]), Math.abs(ns.getVKnotVector()[0] - ns.getVKnotVector()[ns.getVKnotVector().length - 1]));
//		double h = maxDist / 50;
//		double tau;
//		double vau;
//		u.add(y0);
//		double[] orientation = new double[2];
//		if (!secondOrientation) {
//			orientation = vecField;
//		} else {
//			Rn.times(orientation, -1, vecField);
//		}
//		double[] U = ns.getUKnotVector();
//		double[] V = ns.getVKnotVector();
//		double u0 = U[0];
//		double um = U[U.length - 1];
//		double v0 = V[0];
//		double vn = V[V.length - 1];
//		boolean nearBy = false;
//		double[] ori = orientation;
//		LineSegment seg = new LineSegment();
//		
//		while (!nearBy) {
//			double[] v = new double[dim];
//			double[] sumA = new double[dim];
//			for (int i = 0; i < dim; i++) {
//				v[i] = u.getLast()[i]; 
//			}
//			double[][] k = new double[b.length][2];
//
//			if (Rn.innerProduct(orientation,getVecField(ns, v[0], v[1], conj)) > 0) {
//				Rn.normalize(k[0],getVecField(ns, v[0], v[1], conj));
//			} else {
//				Rn.times(k[0], -1, Rn.normalize(null,getVecField(ns, v[0], v[1], conj)));
//			}
//			double[][] segment = new double[2][2];
//			for (int l = 1; l < b.length; l++) {
//				sumA = Rn.times(null, A[l][0], k[0]);
//				for (int m = 1; m < l - 1; m++) {
//					sumA = Rn.add(null, sumA, Rn.times(null, A[l][m], k[m]));
//				}
////				LinkedList<double[]> nextPoints = new LinkedList<double[]>();
////				LinkedList<double[][]> nextSegments = new LinkedList<double[][]>();
////				double[] nextPoint = new double[2];
//				segment[0][0]= v[0];
//				segment[0][1]= v[1];
//				segment[1][0]= v[0] + h * sumA[0];
//				segment[1][1]= v[1] + h * sumA[1];
////				if(ns.getClosingDir() == ClosingDir.uClosed){
//////					System.out.println("go over V boundary");
//////					System.out.println("check: S(u0,v0) = " + Arrays.toString(ns.getSurfacePoint(u0, v0)) + " S(um,v0) = " + Arrays.toString(ns.getSurfacePoint(um, v0)));
////					nextPoint = goOverUBoundary(u0, um, segment[1],left,right);
////					if(left){
////						double[][] leftSegment = {{u0,v0},{u0,vn}};
////						double [] intersection = intersectionPoint(segment, leftSegment);
////						intersection[0] = u0;
////						double[][] seg1 = {segment[0], intersection};
////						nextPoints.add(intersection);
////						nextSegments.add(seg1);
////						double [] shiftedPoint = {um, intersection[1]}; 
//////						nextPoints.add(shiftedPoint);
////						nextPoints.add(nextPoint);
////						double[][] seg2 = {shiftedPoint, nextPoint};
////						nextSegments.add(seg2);
////						seg.setSegment(seg2);
////					}
////					else if(right){
////						double[][] rightSegment = {{um,v0},{um,vn}};
////						double [] intersection = intersectionPoint(segment, rightSegment);
////						intersection[0] = um;
////						double[][] seg1 = {segment[0], intersection};
////						nextPoints.add(intersection);
////						nextSegments.add(seg1);
////						double [] shiftedPoint = {u0, intersection[1]}; 
////						nextPoints.add(shiftedPoint);
////						nextPoints.add(nextPoint);
////						double[][] seg2 = {shiftedPoint, nextPoint};
////						nextSegments.add(seg2);
////						seg.setSegment(seg2);
////					}
////					else{
////						nextPoints.add(segment[1]);
////						nextSegments.add(segment);
////						seg.setSegment(segment);
////
////					}
////					
////				}
////				else if(ns.getClosingDir() == ClosingDir.vClosed){
//////					System.out.println("go over U boundary");
//////					System.out.println("check: S(u0,v0) = " + Arrays.toString(ns.getSurfacePoint(u0, v0)) + " S(u0,vn) = " + Arrays.toString(ns.getSurfacePoint(u0, vn)));
////					nextPoint = goOverVBoundary(v0, vn, segment[1], down, up);
////					if(down){
////						double[][] downSegment = {{u0,v0},{um,v0}};
////						double [] intersection = intersectionPoint(segment, downSegment);
////						intersection[1] = v0;
////						double[][] seg1 = {segment[0], intersection};
////						nextPoints.add(intersection);
////						nextSegments.add(seg1);
////						double [] shiftedPoint = {intersection[0], vn}; 
////						nextPoints.add(shiftedPoint);
////						nextPoints.add(nextPoint);
////						double[][] seg2 = {shiftedPoint, nextPoint};
////						nextSegments.add(seg2);
////						seg.setSegment(seg2);
////					}
////					else if(up){
////						double[][] downSegment = {{u0,vn},{um,vn}};
////						double [] intersection = intersectionPoint(segment, downSegment);
////						intersection[1] = vn;
////						double[][] seg1 = {segment[0], intersection};
////						nextPoints.add(intersection);
////						nextSegments.add(seg1);
////						double [] shiftedPoint = {intersection[0], v0}; 
////						nextPoints.add(shiftedPoint);
////						nextPoints.add(nextPoint);
////						double[][] seg2 = {shiftedPoint, nextPoint};
////						nextSegments.add(seg2);
////						seg.setSegment(seg2);
////					}
////					else{
////						nextPoints.add(segment[1]);
////						nextSegments.add(segment);
////						seg.setSegment(segment);
////
////					}
////				}
////				else{
////					nextPoints.add(segment[1]);
////					nextSegments.add(segment);
//					seg.setSegment(segment);
////				}
////				u.addAll(nextPoints);
//				if (segmentIntersectBoundary(seg, boundary)) {
////					if(left || right || down || up){
////						System.out.println("segmentIntersectBoundary the segment = "+seg.toString());
////					}
//					System.out.println("out of domain 1");
//					double[] intersection = boundaryIntersection(seg, boundary);
//					if(isOutOfDomain(ns, intersection)){
//						intersection = projectPointIntoDomain(ns, intersection);
//					}
////					u.pollLast();
//					u.add(intersection);
//					IntObjects intObj = new IntObjects(u, ori, nearBy, conj);
//					System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
//					return intObj;
//				}
//				if (Rn.innerProduct(orientation,Rn.normalize(null,getVecField(ns, segment[1][0], segment[1][1], conj))) > 0) {
//					k[l] = Rn.normalize(null, getVecField(ns, segment[1][0], segment[1][1], conj));
//				} else  {
//					k[l] = Rn.times(null, -1, Rn.normalize(null, getVecField(ns,  segment[1][0], segment[1][1], conj)));
//				}
//			}
//			double[] Phi1 = new double[dim];
//			double[] Phi2 = new double[dim];
//			for (int l = 0; l < b.length; l++) {
//				Phi1 = Rn.add(null, Phi1, Rn.times(null, c1[l], k[l]));
//				Phi2 = Rn.add(null, Phi2, Rn.times(null, c2[l], k[l]));
//			}
//			v = Rn.add(null, v, Rn.times(null, h, Phi2));
//			tau = Rn.euclideanNorm(Rn.subtract(null, Phi2, Phi1));
//			vau = Rn.euclideanNorm(u.getLast()) + 1;
//			if (tau <= tol * vau) {
//				segment[0] = u.getLast();
//				segment[1] = Rn.add(null, u.getLast(), Rn.times(null, h, Phi1));
//				if(ns.getClosingDir() == ClosingDir.uClosed){
////					System.out.println("go over V boundary");
//					segment[1] = goOverUBoundary(u0, um, segment[1], left,right);
//				}
//				if(ns.getClosingDir() == ClosingDir.vClosed){
////					System.out.println("go over U boundary");
//					segment[1] = goOverVBoundary(v0, vn, segment[1],down,up);
//				}
//				seg.setSegment(segment);
//				u.add(segment[1]);
//				
//				if (segmentIntersectBoundary(seg, boundary)) {
//					if(left || right || down || up){
//						System.out.println("segmentIntersectBoundary the segment = "+seg.toString());
//					}
//					System.out.println("alles richtig segmentIntersectBoundary the segment = "+seg.toString());
//					System.out.println("out of domain 2");
//					double[] intersection = boundaryIntersection(seg, boundary);
//					if(isOutOfDomain(ns, intersection)){
//						intersection = projectPointIntoDomain(ns, intersection);
//					}
//					u.pollLast();
//					u.add(intersection);
//					IntObjects intObj = new IntObjects(u, ori, nearBy, conj);
//					System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
//					return intObj;
//				}
//				if (Rn.innerProduct(orientation,getVecField(ns, u.getLast()[0],u.getLast()[1], conj)) > 0) {
//					orientation = getVecField(ns,u.getLast()[0], u.getLast()[1], conj);
//				} else {
//					orientation = Rn.times(null, -1, getVecField(ns, u.getLast()[0],u.getLast()[1], conj));
//				}
//			}
//			if ((tau <= tol * vau / 2 || tau >= tol * vau)) {
//				double hOld = h;
//				h = h * Math.sqrt(tol * vau / tau);
//				if(h > maxDist / 2){
//					h = hOld;
//				}
//			}
//		}
//		System.out.println("u.size() " + u.size());
//		IntObjects intObj = new IntObjects(u, ori, nearBy, conj);
//		System.out.println("letzter Punkt: "+Arrays.toString(intObj.getPoints().getLast()));
//		return intObj;
//	}
	

	/**
	 * computes geodesics via the exponetial map
	 * @param ns
	 * @param y0
	 * @param eps
	 * @param tol
	 * @return
	 */
	
	public static LinkedList<double[]> geodesicExponential(NURBSSurface ns, double[] y0, double eps, double tol){
		double[][] A = { { 0, 0, 0, 0 }, { 0.5, 0, 0, 0 }, { 0, 0.75, 0, 0 },{ 2 / 9., 1 / 3., 4 / 9., 0 } };
		double[] c1 = { 2 / 9., 1 / 3., 4 / 9., 0 };
		double[] c2 = { 7 / 24., 0.25, 1 / 3., 1 / 8. };
		double[] b = { 0, 0.5, 0.75, 1 };
		LinkedList<double[]> u = new LinkedList<double[]>();
		int dim = y0.length;
		double h = 1 / 1000.;
		double tau;
		double vau;
		u.add(y0);
		double[] Udomain = { ns.getUKnotVector()[0], ns.getUKnotVector()[ns.getUKnotVector().length - 1] };
		double[] Vdomain = { ns.getVKnotVector()[0], ns.getVKnotVector()[ns.getVKnotVector().length - 1] };
		double u1 = Udomain[0];
		double u2 = Udomain[1];
		double v1 = Vdomain[0];
		double v2 = Vdomain[1];
		boolean nearBy = false;
		boolean first = true;
		double dist;

		while (!nearBy ) {
			double[] v = new double[dim];
			double[] sumA = new double[dim];
			for (int i = 0; i < dim; i++) {
				v[i] = u.getLast()[i]; // initialisiere das AWP v = y0
			}
			double[][] k = new double[b.length][4];
			ChristoffelInfo c0 = NURBSChristoffelUtility.christoffel(ns, v[0], v[1]);
			k[0][0] = v[2];
			k[0][1] = v[3];
			k[0][2] = -(c0.getG111() * v[2] * v[2] + 2 * c0.getG121() * v[2] * v[3] + c0.getG221() * v[3] * v[3]);
			k[0][3] = -(c0.getG112() * v[2] * v[2] + 2 * c0.getG122() * v[2] * v[3] + c0.getG222() * v[3] * v[3]);
			for (int l = 1; l < b.length; l++) {
				sumA = Rn.times(null, A[l][0], k[0]);
				for (int m = 1; m < l - 1; m++) {
					sumA = Rn.add(null, sumA, Rn.times(null, A[l][m], k[m]));
				}
				if (!((v[0] + h * sumA[0]) < u2) || !((v[0] + h * sumA[0]) > u1) || !((v[1] + h * sumA[1]) < v2) || !((v[1] + h * sumA[1]) > v1)) {
					System.out.println("1. out of domain");
					return u;
				}
				ChristoffelInfo cl = NURBSChristoffelUtility.christoffel(ns, v[0] + h * sumA[0], v[1] + h * sumA[1]);
				k[l][0] = v[2] + h * sumA[2];
				k[l][1] = v[3] + h * sumA[3];
				k[l][2] = -(cl.getG111() * (v[2] + h * sumA[2]) * (v[2] + h * sumA[2]) + 2 * cl.getG121() * (v[2] + h * sumA[2]) * (v[3] + h * sumA[3]) + cl.getG221() * (v[3] + h * sumA[3]) * (v[3] + h * sumA[3]));
				k[l][3] = -(cl.getG112() * (v[2] + h * sumA[2]) * (v[2] + h * sumA[2]) + 2 * cl.getG122() * (v[2] + h * sumA[2]) * (v[3] + h * sumA[3]) + cl.getG222() * (v[3] + h * sumA[3]) * (v[3] + h * sumA[3]));
			}
			double[] Phi1 = new double[dim];
			double[] Phi2 = new double[dim];
			for (int l = 0; l < b.length; l++) {
				Phi1 = Rn.add(null, Phi1, Rn.times(null, c1[l], k[l]));
				Phi2 = Rn.add(null, Phi2, Rn.times(null, c2[l], k[l]));
			}
				v = Rn.add(null, v, Rn.times(null, h, Phi2));
				tau = Rn.euclideanNorm(Rn.add(null, Phi2,Rn.times(null, -1, Phi1)));
				vau = Rn.euclideanNorm(u.getLast()) + 1;
				if (tau <= tol * vau) {
					u.add(Rn.add(null, u.getLast(), Rn.times(null, h, Phi1)));
					if (u.getLast()[0] >= u2 || u.getLast()[0] <= u1
							|| u.getLast()[1] >= v2 || u.getLast()[1] <= v1) {
						u.pollLast();
						System.out.println("2. out of domain");
						return u;
					}
				}
				if ((tau >= tol * vau)) {
					h = h * StrictMath.pow(tol * vau / tau, 1 / 2.);
				}
				dist = Rn.euclideanDistance(u.getLast(), y0);
				if (!(dist < eps) && first) {
					first = false;
				}
				if (dist < eps && !first) {
					nearBy = true;
				}
			}
		return u;
	}
	
	
	
	private static double distanceLinePoint(LinkedList<double[]> line, double[] a, double[] b){
		double dist = Double.MAX_VALUE;
		for (double [] l : line) {
			double[] point = new double[2];
			point[0] = l[0];
			point[1] = l[1];
			if(Rn.euclideanDistance(point, b) < Math.abs(dist)){
				
				dist = Rn.euclideanDistance(point, b);
			}
		}
		return dist;
	}
	
	/*
	 * computes the distance between a line and a point 
	 * 
	 */
	private static double orientedInnerproductDistanceLinePoint(LinkedList<double[]> line, double[] a, double[] b){
		double dist = Double.MAX_VALUE;
		int sign = 0;
		for (double [] l : line) {
			double[] point = new double[2];
			point[0] = l[0];
			point[1] = l[1];
			// homogeneous coords
			double[] pointH = new double[3];
			pointH[0] = point[0];
			pointH[1] = point[1];
			pointH[2] = 1;
			double[] aH = new double[3];
			aH[0] = a[0];
			aH[1] = a[1];
			aH[2] = 1;
			double[] bH = new double[3];
			bH[0] = b[0];
			bH[1] = b[1];
			bH[2] = 1;
			if(Rn.euclideanDistance(point, b) < Math.abs(dist)){
				if( Rn.innerProduct(Rn.crossProduct(null, aH, bH), pointH) < 0 ){
					sign = -1;
				}else{
					sign = 1;
				}
				dist = Rn.euclideanDistance(point, b) * sign;
			}
		}
		return dist;
	}
	
	public static LinkedList<double[]> geodesicExponentialGivenByTwoPoints(NURBSSurface ns, double[] a, double[] b, double eps, double tol, double nearby){
		int sign1 = 1;
		double angle1 = 0;
		double h = 1/5.;
		double[] startDirection = Rn.add(null, b, Rn.times(null, -1, a));
		System.out.println("Startrichtung " + Arrays.toString(startDirection));
//		double[] y01 = {a[0], a[1], startDirection[0], startDirection[1]};
		double[] y01 = {a[0], a[1], 1. , 0};
		LinkedList<double[]> line1 = IntegralCurves.geodesicExponential(ns, y01, eps, tol);
		double dist1 = IntegralCurves.orientedInnerproductDistanceLinePoint(line1, a, b);
		if(dist1 < 0){
			sign1 = -1;
		}
		System.out.println("SIGN1 " + sign1);
		int counter = 0;
		while(sign1 == 1){
			counter++;
			System.out.println(counter);
			angle1 = angle1 + h;
			y01[2] = Math.cos(angle1);
			y01[3] = Math.sin(angle1);
			line1 = IntegralCurves.geodesicExponential(ns, y01, eps, tol);
			dist1 = IntegralCurves.orientedInnerproductDistanceLinePoint(line1, a, b);
			if(dist1 < 0){
				sign1 = -1;
			}
		}
		System.out.println("counter "+ counter);
		int sign2 = sign1;
		double dist2 = 0;
		double[] y02 = {a[0], a[1], y01[2], y01[3]};
		double angle2 = angle1;
		LinkedList<double[]> line2 = new LinkedList<double[]>();
		while(sign1 == sign2 ){
			angle2 = angle2 + h;
			y02[2] = Math.cos(angle2);
			y02[3] = Math.sin(angle2);
			line2 = IntegralCurves.geodesicExponential(ns, y02, eps, tol);
			dist2 = IntegralCurves.orientedInnerproductDistanceLinePoint(line2, a, b);
			System.out.println("dist2 " + dist2);
			if(dist2 < 0){
				sign2 = -1;
			}else{
				sign2 = 1;
			}
		}
		double dist = 10;
		// Bisection
		double angle;
		LinkedList<double[]> line = new LinkedList<double[]>();
		double[] y0 = y01;
		System.out.println("Start Bisection");
		System.out.println("angle1 " + angle1 + " angle2 " + angle2);
		System.out.println("dist1 " + dist1 + " dist2 " + dist2);
		while(Math.abs(dist) > nearby){
			angle = 0.5 * angle1 + 0.5 * angle2;
			y0[2] = Math.cos(angle);
			y0[3] = Math.sin(angle);
			line = IntegralCurves.geodesicExponential(ns, y0, eps, tol);
			dist = IntegralCurves.orientedInnerproductDistanceLinePoint(line, a, b);
			if(dist == 0 ){
				System.out.println("1 case");
				return line;
			}
//			else if(angle1 == angle || angle2 == angle){
//				return line;
//			}
//			else if( dist == dist1 ){
//				angle1 = angle;
//			}
//			else if( dist == dist2 ){
//				angle2 = angle;
//			}
			else if( dist1 * dist < 0 ){
				System.out.println("2. case");
				angle2 = angle;
				dist2 = dist;
			}else{
				System.out.println("3. case");
				angle1 = angle;
				dist1 = dist;
			}
		}
		return line;
	}

	public static LinkedList<double[]> geodesicSegmentBetweenTwoPoints(NURBSSurface ns, double[] a, double[] b, double eps, double tol, double nearby){
		LinkedList<double[]> line = IntegralCurves.geodesicExponentialGivenByTwoPoints(ns, a, b, eps, tol,nearby);
		double dist = IntegralCurves.distanceLinePoint(line, a, b);
		LinkedList<double[]> segment = new LinkedList<double[]>();
		for (double[] l : line) {
			segment.add(l);
			double[] linePoint = new double[2];
			linePoint[0] = l[0];
			linePoint[1] = l[1];
			
			if(Rn.euclideanDistance(linePoint, b) == Math.abs(dist)){
				
				return segment;
			}
		}
		return segment;
	}
	
	public static void main(String[] args){
		double[] start = {1,2};
		double[] end = {3,4};
		double[][] segment = new double[2][2];
		segment[0] = start;
		segment[1] = end;
		LineSegment seg = new LineSegment();
		seg.setSegment(segment);
		double[] point = {0,6};
		double dist = distLineSegmentPoint(point, seg);
		System.out.println("dist " + dist);
	}
	
	
}
