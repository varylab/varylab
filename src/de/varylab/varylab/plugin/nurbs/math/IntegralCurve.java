package de.varylab.varylab.plugin.nurbs.math;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.ClosingDir;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;
import de.varylab.varylab.plugin.nurbs.data.IntObjects;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;
import de.varylab.varylab.plugin.nurbs.data.ValidSegment;

public class IntegralCurve {
	
	private NURBSSurface ns;
	private double u0, um, v0, vn;
	private ClosingDir closingDirection = null;
	int leftBoundaryCounter = 0;
	int rightBoundaryCounter = 0;
	int upperBoundaryCounter = 0;
	int lowerBoundaryCounter = 0;
	
	public IntegralCurve(NURBSSurface surface){
		ns = surface;
		double[] U = ns.getUKnotVector();
		double[] V = ns.getVKnotVector();
		u0 = U[0];
		um = U[U.length - 1];
		v0 = V[0];
		vn = V[V.length - 1];
		closingDirection = ns.getClosingDir();
		System.out.println("CLOSING DIRECTION " + closingDirection);
	}
	
	public double[] getSymmetricDirection(double[] p) {
		double[] dir = {1,1};
		if(!ns.isSurfaceOfRevolution()){
			return dir;
		}
		CurvatureInfo ci =  NURBSCurvatureUtility.curvatureAndDirections(ns, p[0], p[1]);
		double[][] sF = ci.getSecondFundamental();
		double l = sF[0][0];
		double n  = sF[1][1];
		if(l != 0){
			dir[0] = Math.sqrt(n / l);
			return dir;
		}
//		dir[0] = Math.sqrt(n / l);
		return dir;
	}
	
	private double[] getConj(double[] v, double[] p){
		CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(ns, p[0], p[1]);
		double[][] sF = ci.getSecondFundamental();
		double[] b = new double[2];
		b[0] = v[0] * sF[0][0] + v[1] * sF[1][0];
		b[1] = v[0] * sF[0][1] + v[1] * sF[1][1];
		double[] w = new double[2];
		w[0] = -b[1];
		w[1] = b[0];
//		double[] fu = ci.getSu();
//		double[] fv = ci.getSv();
//		double[] W = Rn.add(null, Rn.times(null, w[0], fu), Rn.times(null, w[1], fv));
//		Rn.normalize(W, W);
//		if(Math.abs(1 - Rn.euclideanNorm(W)) > 0.001){
//			System.out.println("length: " + Rn.euclideanNorm(w));
//		}
		return w;
	}
	
	private double[] getVecField(double[] p, boolean conj) {
		double[] vec = getSymmetricDirection(p);
		if(conj){
			return getConj(vec, p);
		}else{
			return vec;
		}
	}
	
	public boolean pointIsInU(double[] point){
		if(point[0] < u0 || point[0] > um){
			return false;
		}
		return true;
	}
	
	public boolean pointIsInV(double[] point){
		if(point[1] < v0 || point[1] > vn){
			return false;
		}
		return true;
	}
	
	public boolean pointIsInOriginalDomain(double[] point){
		if(!pointIsInU(point) || !pointIsInV(point)){
			return false;
		}
		return true;
	}
	
	public boolean pointIsOutsideOfExtendedDomain(double[] point){
		if(closingDirection == ClosingDir.uvClosed){
			return false;
		}
		if(closingDirection == ClosingDir.uClosed){
			if(point[1] > vn || point[1] < v0){
				return true;
			}
			return false;
		}
		if(closingDirection == ClosingDir.vClosed){
			if(point[0] > um || point[0] < u0){
				return true;
			}
			return false;
		}
		else{
			if(point[1] > vn || point[1] < v0 || point[0] > um || point[0] < u0){
				return true;
			}
			return false;
		}
	}
	
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
	
	public double[] getPointInOriginalDomain(double[] point){
		double[] domainPoint = {point[0], point[1]};
		if(pointIsInOriginalDomain(point)){
			return domainPoint;
		}
		if(!pointIsInU(point)){
			domainPoint[0] = modInterval(u0, um, point[0]);
		}
		if(!pointIsInV(point)){
			domainPoint[1] = modInterval(v0, vn, point[1]);
		}
		return domainPoint;
	}
	
	private boolean segmentIntersectBoundary(LineSegment ls){
		double[] point = ls.getSegment()[1];
		if(pointIsOutsideOfExtendedDomain(point)){
			return true;
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
	
	public boolean isNotAtBoundary(double[] point){
		if(point[0] != u0 && point[0] != um && point[1] != v0 && point[1] != vn){
			return true;
		}
		return false;
	}
	
	public static double getMinBoundValue(double[] point, LinkedList<Double> boundaryValues){
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
	
	private double[] boundaryIntersection(LineSegment seg, List<LineSegment> boundary){
		double minDist = Double.MAX_VALUE;
		double[] intersection = null;
		for (LineSegment lS : boundary) {
			if(Rn.euclideanDistance(seg.getSegment()[0], intersectionPoint(seg, lS)) < minDist){
				intersection = intersectionPoint(seg, lS);
				minDist = Rn.euclideanDistance(seg.getSegment()[0], intersectionPoint(seg, lS));
			}
		}
		if(isNotAtBoundary(intersection)){
			intersection = projectOntoBoundary(ns, intersection);
		}
		return intersection;
	}
	
	private boolean terminationCondition(NURBSSurface ns, LineSegment seg, LinkedList<double[]> u, List<LineSegment> boundary){
//		if(segmentIntersectBoundary(seg, boundary)) {
		if(segmentIntersectBoundary(seg)) {
			double[] intersection = boundaryIntersection(seg, boundary);
			u.add(intersection);
			return true;
		}
		return false;
	}
	
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
	
	public int[] getModDomain(double [] point){
		int[] domain = new int[2];
		domain[0] = getModInterval(u0, um, point[0]);
		domain[1] = getModInterval(v0, vn, point[1]);
		return domain;
	}
	
	public boolean pointsAreInDifferentShiftedtDomains(double[] point1, double[] point2){
		int[] domain1 = getModDomain(point1);
		int[] domain2 = getModDomain(point2);
		if(domain1[0] == domain2[0] && domain1[1] == domain2[1]){
			return false;
		}
		return true;
	}
	
	private void flipClosedBoundaryPoint(double[] point){
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
	
	public double[][] getShiftedBoundaryIntersectionPoints(double[] point1, double[] point2){
		System.out.println("GET SHIFTED");
		double[][] intersectionPoints = new double[2][2];
		int[] domain1 = getModDomain(point1);
		int[] domain2 = getModDomain(point2);
		double[][] seg = new double[2][2];
		if(domain1[0] > domain2[0]){ // left
			double Shift =  um - u0;
			seg[0] = getPointInOriginalDomain(point1);
			seg[1] = getPointInOriginalDomain(point2);
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
			seg[0] = getPointInOriginalDomain(point1);
			seg[1] = getPointInOriginalDomain(point2);
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
			seg[0] = getPointInOriginalDomain(point1);
			seg[1] = getPointInOriginalDomain(point2);
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
			seg[0] = getPointInOriginalDomain(point1);
			seg[1] = getPointInOriginalDomain(point2);
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
	
	private LinkedList<double[]> setIntoDomain(LinkedList<double[]> pointList){
		LinkedList<double[]> domainList = new LinkedList<double[]>();
		int counter = 0;
		double[][] seg = new double[2][2];
		domainList.add(pointList.getFirst());
		seg[0] = pointList.getFirst().clone();
		for (double[] p : pointList) {
			if(counter != 0){
				seg[1] = p.clone();
				if(counter == 1 && ns.isClosedBoundaryPoint(seg[0]) && pointsAreInDifferentShiftedtDomains(seg[0], seg[1])){
					flipClosedBoundaryPoint(pointList.getFirst());
					domainList.add(getPointInOriginalDomain(p));
				} else {
					if(pointsAreInDifferentShiftedtDomains(seg[0], seg[1])){
						double[][] intersections = getShiftedBoundaryIntersectionPoints(seg[0], seg[1]);
						domainList.add(intersections[0]);
						domainList.add(intersections[1]);
					}
					
				}
				double[] domainPoint = getPointInOriginalDomain(p);
				double[] check = domainList.getLast();
				if(!Arrays.equals(check, domainPoint)){
					domainList.add(domainPoint);
				}
				else{
					System.out.println("DOPPEL PUNKT");
				}
				seg[0] = p.clone();
			}
			counter++;
		}
		return domainList;
	}
	
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
	
	public boolean terminationConditionForVectorfieldPoints(double[] point, LinkedList<double[]> pointList, List<LineSegment> boundary){
		if(pointIsOutsideOfExtendedDomain(point)){
			double[] last = pointList.pollLast();
			double[] nextToLast = pointList.getLast();
			LineSegment ls = new LineSegment(nextToLast, last);
			double[] intersection =  boundaryIntersection(ls, boundary);
			pointList.add(intersection);
			System.out.println("terminationConditionForVectorfieldPoints");
			return true;
		}
		return false;
	}
	
	public boolean isCloseToBoundary(double[] point){
		double eps = 0.01;
		if(closingDirection == ClosingDir.nonClosed){
			if(Math.abs(u0 - point[0]) < eps){
				return true;
			}
			if(Math.abs(um - point[0]) < eps){
				return true;
			}
			if(Math.abs(v0 - point[1]) < eps){
				return true;
			}
			if(Math.abs(vn - point[1]) < eps){
				return true;
			}
		}
		if(closingDirection == ClosingDir.uClosed){
			if(Math.abs(v0 - point[1]) < eps){
				return true;
			}
			if(Math.abs(vn - point[1]) < eps){
				return true;
			}
		}

		if(closingDirection == ClosingDir.vClosed){
			if(Math.abs(u0 - point[0]) < eps){
				return true;
			}
			if(Math.abs(um - point[0]) < eps){
				return true;
			}
		}
		return false;
	}
	
	public boolean terminationConditionForPoints(double[] point, LinkedList<double[]> pointList,  List<LineSegment> boundary){
		if(pointIsOutsideOfExtendedDomain(point)){
			LineSegment ls = new LineSegment(pointList.getLast(), point);
			double[] intersection = boundaryIntersection(ls, boundary);
			pointList.add(intersection);
			System.out.println("terminationConditionForPoints pointIsOutsideOfExtendedDomain");
			return true;
		}
		if(isCloseToBoundary(point)){
//			System.out.println("is close to boundary");
			double[] last = pointList.pollLast();
			double[] nextToLast = pointList.getLast();
			LineSegment ls = new LineSegment(nextToLast, last);
			double[] intersection =  boundaryIntersection(ls, boundary);
			pointList.add(intersection);
			System.out.println("terminationConditionForPoints isCloseToBoundary");
			return true;
		}
		else{
//			System.out.println("LEFT distance " + Math.abs(u0 - point[0]));
		}
		
		return false;
	}

//	public IntObjects rungeKuttaConjugateLine(double[] y0, double tol, boolean secondOrientation, boolean conj, List<double[]> umbilics, double umbilicStop, List<LineSegment> boundary) {
//		System.out.println("INPUT BOUNDARY");
//		for (LineSegment bs : boundary) {
//			System.out.println(bs.toString());
//		}
//		double lowBound = 0.0000001;
//		double[][] A = { { 0, 0, 0, 0 }, { 0.5, 0, 0, 0 }, { 0, 0.75, 0, 0 },{2 / 9., 1 / 3., 4 / 9., 0 } };
//		double[] c1 = { 2 / 9., 1 / 3., 4 / 9., 0 };
//		double[] c2 = { 7 / 24., 0.25, 1 / 3., 1 / 8. };
//		double[] b = { 0, 0.5, 0.75, 1 };
//		double[] initialValue = y0.clone();
//		// double[][] A =	{{0,0,0,0,0,0,0},{1/5.,0,0,0,0,0,0},{3/40.,9/40.,0,0,0,0,0},{44/45.,-56/15.,32/9.,0,0,0,0},{19372/6561.,-25360/2187.,64448/6561.,-212/729.,0,0,0},{9017/3168.,-355/33.,46732/5247.,49/176.,-5103/18656.,0,0},{35/384.,	0, 500/1113., 125/192., -2187/6784., 11/84.,0}};
//		// double[] c1 = {35/384., 0, 500/1113., 125/192., -2187/6784., 11/84.,	0 };
//		// double[] c2 = {5179/57600., 0, 7571/16695., 393/640.,	-92097/339200., 187/2100., 1/40.};
//		// double[] b = { 0,1/5., 3/10.,4/5.,8/9.,1,1 };
//		LinkedList<double[]> pointList = new LinkedList<double[]>();
//		int dim = y0.length;
//		double maxDist = Math.min(Math.abs(ns.getUKnotVector()[0] -ns.getUKnotVector()[ns.getUKnotVector().length - 1]), Math.abs(ns.getVKnotVector()[0] - ns.getVKnotVector()[ns.getVKnotVector().length - 1]));
//		double h = maxDist / 1000;
//		double tau = 1000;
//		double vau;
//		double [] vec1 = new double[2];
//		double [] vec2 = new double[2];
//		boolean closed = false;
//		pointList.add(initialValue);
//		double[] orientation = new double[2];
//		if (!secondOrientation) {
//			orientation = getVecField(initialValue, conj);
//		} else {
//			orientation = Rn.times(null, -1, getVecField(initialValue, conj));
//		}
//		boolean nearBy = false;
//		double dist;
//		double[] ori = orientation;
//		LineSegment seg = new LineSegment();
//	
//		while (!nearBy) {
//			System.out.println("THE POINT " + Arrays.toString(pointList.getLast()));
//			double[] v = new double[dim];
//			double[] sumA = new double[dim];
//			for (int i = 0; i < dim; i++) {
//				v[i] = pointList.getLast()[i];
//			}
//			double[][] k = new double[b.length][2];
//			if (Rn.innerProduct(orientation,getVecField(getPointInDomain(v), conj)) > 0) {
//				k[0] = Rn.normalize(null,getVecField(getPointInDomain(v), conj));
//			} else {
//				k[0] = Rn.times(null, -1,  Rn.normalize(null,getVecField(getPointInDomain(v), conj)));
//			}
//			double[][] segment = new double[2][2];
//			for (int l = 1; l < b.length; l++) {
//				sumA = Rn.times(null, A[l][0], k[0]);
//				for (int m = 1; m < l - 1; m++) {
//					sumA = Rn.add(null, sumA, Rn.times(null, A[l][m], k[m]));
//				}
//				segment[0] = v;
//				Rn.add(segment[1], v, Rn.times(null, h, sumA));
//				seg.setSegment(segment);
//				if(terminationCondition(ns, seg, pointList, boundary)){
//					System.out.println("out of domain 1");
//					pointList = setIntoDomain(pointList);
//					IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
//					System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
//					return intObj;
//				}
//				double[] vf = getVecField(getPointInDomain(segment[1]), conj);
//				if(Rn.innerProduct(orientation,Rn.normalize(null,vf)) > 0) {
//					k[l] = Rn.normalize(null, vf);
//				} else {
//					k[l] = Rn.times(null, -1, Rn.normalize(null, vf));
//				}
//			}
//			double[] Phi1 = new double[dim];
//			double[] Phi2 = new double[dim];
//			for (int l = 0; l < b.length; l++) {
//				Phi1 = Rn.add(null, Phi1, Rn.times(null, c1[l], k[l]));
//				Phi2 = Rn.add(null, Phi2, Rn.times(null, c2[l], k[l]));
//			}
//			v = Rn.add(null, v, Rn.times(null, h, Phi2));
////			if(tau > lowBound){
//				tau = Rn.euclideanNorm(Rn.subtract(null, Phi2, Phi1));
////			}
//			
//			System.out.println("TAU " + tau);
//			vau = Rn.euclideanNorm(pointList.getLast()) + 1;
//			if (tau <= tol * vau) {
//				segment[0] = pointList.getLast();
//				segment[1] = Rn.add(null, pointList.getLast(), Rn.times(null, h, Phi1));
//				seg.setSegment(segment);
//				if(terminationCondition(ns, seg, pointList, boundary)){
//					System.out.println("out of domain 2");
//					double[] last = pointList.pollLast();
//					pointList.pollLast();
//					pointList.add(last);
//					pointList = setIntoDomain(pointList);
//					IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
//					System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
//					return intObj;
//				}
//				pointList.add(segment[1]);
//				if (Rn.innerProduct(orientation, getVecField(getPointInDomain(pointList.getLast()), conj)) > 0) {
//					orientation = getVecField(getPointInDomain(pointList.getLast()), conj);
//				} else {
//					orientation = Rn.times(null, -1, getVecField(getPointInDomain(pointList.getLast()), conj));
//				}
//			}
//			if ((tau <= tol * vau / 2 || tau >= tol * vau)) {
//				double hOld = h;
//				h = h * Math.sqrt(tol * vau / tau);
//				if(h > maxDist / 2 || h < lowBound){
//					h = hOld;
//				}
//				
//			}
////			else{
////				double hOld = h;
////				h = h * Math.sqrt(tol * vau / tau);
////				if(h > maxDist / 2 || h < lowBound){
////					h = hOld;
////				}
////			}
//			if(pointList.size() == 2){
//				vec1 = Rn.subtract(null, pointList.getLast(), pointList.getFirst());
//			}
//			if(pointList.size() > 2){
//				double[][] lastSegment = new double[2][2];
//				lastSegment[1] = pointList.pollLast();
//				lastSegment[0] = pointList.getLast();
//				vec2 = Rn.subtract(null, lastSegment[1], lastSegment[0]);
//				seg.setSegment(lastSegment);
//				dist = distLineSegmentPoint(y0, seg);
//				if(Rn.innerProduct(vec1, vec2) < 0){
//					closed = true;
//				}
//				if(dist < umbilicStop && closed){
//					nearBy = true;
//					System.out.println("closed");
//					pointList = setIntoDomain(pointList);
//					IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
//					return intObj;
//				}
//				else{
//					pointList.add(lastSegment[1]);
//				}
//			}
//		}
//		System.out.println("u.size() " + pointList.size());
//		pointList = setIntoDomain(pointList);
//		IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
//		System.out.println("letzter Punkt:"+Arrays.toString(intObj.getPoints().getLast()));
//		return intObj;
//	}
	
	/**
	 * this vector field is a representative of a continuous line field.
	 * @param orientation
	 * @param point
	 * @param conj
	 * @return normalized continuous vector field
	 */
	
	public double[] getContinuousNormalizedVectorField(double[] orientation, double[] vf){
		Rn.normalize(vf, vf);
		if (Rn.innerProduct(orientation,vf) < 0) {
			Rn.times(vf, -1, vf);
		} 
		return vf;
	}
	
	
	public IntObjects rungeKuttaConjugateLine(double[] y0, double tol, boolean secondOrientation, boolean conj, List<double[]> umbilics, double umbilicStop, List<LineSegment> boundary) {
//		System.out.println("INPUT BOUNDARY");
//		for (LineSegment bs : boundary) {
//			System.out.println(bs.toString());
//		}
		int counter = 0;
		double[] initialValue = y0.clone();
		LinkedList<double[]> pointList = new LinkedList<double[]>();
		double h = Math.max(um - u0, vn - v0) / 500.;
		double [] vec1 = new double[2];
		double [] vec2 = new double[2];
		boolean closed = false;
		pointList.add(initialValue);
		double[] orientation = new double[2];
		if (!secondOrientation) {
			orientation = getVecField(initialValue, conj);
		} else {
			orientation = Rn.times(null, -1, getVecField(initialValue, conj));
		}
		boolean nearBy = false;
		double dist;
		double[] ori = orientation;
		LineSegment seg = new LineSegment();
	
		while (!nearBy && counter < 2000) {
			counter++;
			if(counter == 2000){
				System.out.println("termination after 2000 steps");
			}
			System.out.println("THE POINT " + Arrays.toString(pointList.getLast()));
			double[] k1 = new double[2];
			double[] k2 = new double[2];
			double[] k3 = new double[2];
			double[] last = pointList.getLast().clone();
			double[] vectorfieldPoint = new double[2];
			// the current point is in the extended domain!!!
			k1 = getVecField(getPointInOriginalDomain(last), conj);
			k1 = getContinuousNormalizedVectorField(orientation, k1);		
			Rn.add(vectorfieldPoint, last, Rn.times(null, 0.5 * h, k1));
			
			if(terminationConditionForVectorfieldPoints(vectorfieldPoint, pointList, boundary)){
				pointList = setIntoDomain(pointList);
				IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
				return intObj;
			}
			
			k2 = getVecField(getPointInOriginalDomain(vectorfieldPoint), conj);
			k2 = getContinuousNormalizedVectorField(orientation, k2);	
			Rn.add(vectorfieldPoint, last, Rn.times(null, 0.75 * h, k2));
			
			if(terminationConditionForVectorfieldPoints(vectorfieldPoint, pointList, boundary)){
				pointList = setIntoDomain(pointList);
				IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
				return intObj;
			}
			k3 = getVecField(getPointInOriginalDomain(vectorfieldPoint), conj);
			k3 = getContinuousNormalizedVectorField(orientation, k3);
			
			double[] next = Rn.add(null, last, Rn.times(null, h, Rn.add(null, Rn.times(null, 2.0 / 9.0, k1), Rn.add(null, Rn.times(null, 1.0 / 3.0, k2), Rn.times(null, 4.0 / 9.0, k3)))));
//			double[] next = Rn.add(null, last, Rn.times(null, h, k1));
			if(terminationConditionForPoints(next, pointList, boundary)){
				pointList = setIntoDomain(pointList);
				IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
				return intObj;
			}
			Rn.subtract(orientation, next, pointList.getLast());
			pointList.add(next);
			
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
					pointList = setIntoDomain(pointList);
					IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
					return intObj;
				}
				else{
					pointList.add(lastSegment[1]);
				}
			}
			
		}
//		System.out.println("u.size() " + pointList.size());
		pointList = setIntoDomain(pointList);
		IntObjects intObj = new IntObjects(pointList, ori, nearBy, conj);
//		System.out.println("letzter Punkt:"+Arrays.toString(intObj.getPoints().getLast()));
		return intObj;
	}
	
	private ValidSegment isValidSegemnt(double[][] seg, int rightShift, int upShift){
		ValidSegment vs = new ValidSegment();
		vs.setValid(false);
		if (seg[0][0] == u0 && seg[1][0] == um){
//			System.out.println("leftShift");
//			System.out.println("seg = " + Arrays.toString(seg[0]) + " " + Arrays.toString(seg[1]));
			rightShift--;
		} else if (seg[0][0] == um && seg[1][0] == u0){
//			System.out.println("rightShift");
//			System.out.println("seg = " + Arrays.toString(seg[0]) + " " + Arrays.toString(seg[1]));
			rightShift++;
		} else if (seg[0][1] == v0 && seg[1][1] == vn){
//			System.out.println("downShift");
//			System.out.println("seg = " + Arrays.toString(seg[0]) + " " + Arrays.toString(seg[1]));
			upShift--;
		} else if (seg[0][1] == vn && seg[1][1] == v0){
//			System.out.println("upShift");
//			System.out.println("seg = " + Arrays.toString(seg[0]) + " " + Arrays.toString(seg[1]));
			upShift++;
		} else if (seg[0][0] == seg[1][0] && seg[0][1] == seg[1][1]){
//			System.out.println("not valid segment w.r.t. equal endpoints");
//			System.out.println("seg = " + Arrays.toString(seg[0]) + " " + Arrays.toString(seg[1]));
		} else {
			vs.setValid(true);
		}
		vs.setRightShift(rightShift);
		vs.setUpShift(upShift);
		return vs;
	}
	
	public int curveLine(NURBSSurface ns, double tol, List<double[]> umbilics, List<PolygonalLine> segments, int curveIndex, List<Integer> umbilicIndex, double[] y0, boolean maxMin, double umbilicStop) {
		LinkedList<LineSegment> currentSegments = new LinkedList<LineSegment>();
//		IntObjects intObj;
//		int noSegment;
		LinkedList<double[]> all = new LinkedList<double[]>();
		List<LineSegment> boundary = ns.getBoundarySegments();
//		IntObjects intObj = IntegralCurvesOriginal.rungeKuttaCurvatureLine(ns, y0, tol,false, maxMin, umbilics, umbilicStop, boundary );
		IntObjects intObj = rungeKuttaConjugateLine(y0, tol ,false, maxMin, umbilics, umbilicStop, boundary);
		
		Collections.reverse(intObj.getPoints());
//		System.out.println("intObj.getPoints() false and reversed");
//		for (double[] point : intObj.getPoints()) {
//			System.out.println(Arrays.toString(point));
//		}
		all.addAll(intObj.getPoints());
//		System.out.println("all.addAll(intObj.getPoints()); ");
//		for (double[] point : all) {
//			System.out.println(Arrays.toString(point));
//		}
		//only debugging
//		double[] last = all.pollLast().clone();
//		System.out.println("LLLAAASSSSTTT " + Arrays.toString(last));
		//end debugging
		System.out.println("first size" + all.size());
		boolean cyclic = false;
		if(!intObj.isNearby()){
//			all.pollLast();
//			intObj = IntegralCurvesOriginal.rungeKuttaCurvatureLine(ns, y0, tol,true, maxMin,  umbilics, umbilicStop, boundary);
			intObj = rungeKuttaConjugateLine(y0, tol, true , maxMin, umbilics, umbilicStop, boundary);
//			System.out.println("intObj.getPoints() true ");
//			for (double[] point : intObjSecond.getPoints()) {
//				System.out.println(Arrays.toString(point));
//			}
//			System.out.println("THE FIRST POINTS");
//			System.out.println("LLLAAASSSSTTT " + Arrays.toString(last));
//			all.add(last);
//			for (double[] point : all) {
//				System.out.println(Arrays.toString(point));
//			}
			all.addAll(intObj.getPoints());
//			System.out.println("all points concatinated from runge kutta derectly past adding");
//			for (double[] point : all) {
//				System.out.println(Arrays.toString(point));
//			}
		}else{
			//add the first element of a closed curve
			cyclic = true;
			System.out.println("add first");
			double[] first = new double [2];
			first[0] = all.getFirst()[0];
			first[1] = all.getFirst()[1];
			all.add(first);
		}
		int index = 0;
//		Integer shiftedIndex = 0;
		int rightShift = 0;
		int upShift = 0;
		double[] firstcurvePoint = all.getFirst();
//		System.out.println("all points concatinated from runge kutta");
//		for (double[] point : all) {
//			System.out.println(Arrays.toString(point));
//		}
		for (double[] secondCurvePoint : all) {
			index ++;
			if(index != 1){
				double[][]seg = new double[2][];
				seg[0] = firstcurvePoint.clone();
				seg[1] = secondCurvePoint.clone();
//				ValidSegment vs = isValidSegemnt(seg, u0, um, v0, vn, shiftedIndex);
				ValidSegment vs = isValidSegemnt(seg, rightShift, upShift);
				rightShift = vs.getRightShift();
				upShift = vs.getUpShift();
//				shiftedIndex = vs.getShiftedIndex();
				boolean segmentIsValid = vs.isValid();
				if(segmentIsValid){
//					System.out.println("shiftedIndex danach = " + shiftedIndex);
//					System.out.println("rightShifted danach = " + rightShift);
//					System.out.println("upShifted danach = " + upShift);
					LineSegment ls = new  LineSegment();
					ls.setIndexOnCurve(index) ;
					ls.setSegment(seg);
					ls.setCurveIndex(curveIndex);
					ls.setCyclic(cyclic);
					ls.setRightShift(rightShift);
					ls.setUpShift(upShift);
//					ls.setShiftedIndex(shiftedIndex);
					currentSegments.add(ls);
					firstcurvePoint = secondCurvePoint;
				}
				else{
					index--;
					firstcurvePoint = secondCurvePoint;
				}
			}
		}
		//begin check
//		System.out.println("check concatinated line");
//		for (LineSegment ls : currentSegments) {
//			System.out.println(ls.toString());
//		}
		//end check
		PolygonalLine currentLine = new PolygonalLine(currentSegments);
		currentLine.setDescription((maxMin?"max:":"min:") + "("+String.format("%.3f", y0[0]) +", "+String.format("%.3f", y0[1])+")");
		segments.add(currentLine);
		curveIndex ++;
		return curveIndex;
	}
	
	
	public LinkedList<PolygonalLine> computeIntegralLines(NURBSSurface ns, boolean firstVectorField, boolean secondVectorField, int curveIndex, double tol, double umbilicStop, List<double[]> singularities, List<double[]> startingPointsUV) {
		LinkedList<PolygonalLine> currentLines = new LinkedList<PolygonalLine>();
		LinkedList<Integer> umbilicIndex = new LinkedList<Integer>();
		
		for(double[] y0 : startingPointsUV) {
				if (firstVectorField){
					curveIndex = curveLine(ns, tol, singularities, currentLines, curveIndex, umbilicIndex, y0, true, umbilicStop);
				}
				if (secondVectorField){
					curveIndex = curveLine(ns, tol, singularities, currentLines, curveIndex, umbilicIndex, y0, false, umbilicStop);
				}
		}
		return currentLines;
	}
	
	

}
