package de.varylab.varylab.plugin.nurbs.math;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.data.ChristoffelInfo;
import de.varylab.varylab.plugin.nurbs.data.IntObjects;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;

public class IntegralCurves {

	public static double[] getMaxMinCurv(NURBSSurface ns, double u, double v,boolean max) {
		if (max) {
			return NURBSCurvatureUtility.curvatureAndDirections(ns, u, v).getCurvatureDirectionsDomain()[1];
		} else {	
			return NURBSCurvatureUtility.curvatureAndDirections(ns, u, v).getCurvatureDirectionsDomain()[0];
		}
	}
//	/**
//	 * <p><strong>computes the curvature lines</strong></p>
//	 * 
//	 * this is a step-size controlled runge kutta (Bogacki–Shampine) method with the following pseudo code</br>
//	 * </br>
//	 * <strong>while</strong> (!nearby)
//	 * 
//	 * @param ns
//	 * @param y0 
//	 * @param tol
//	 * @param secondOrientation --> is true if we use the direction of the given vectorfield an false if we use the opposite direction 
//	 * @param max 
//	 * @param eps --> if we obtain a closed curve then eps is the maximal distance between the start point and the last point
//	 * @return
//	 */
//	public static IntObjects rungeKutta(NURBSSurface ns, double[] y0,double tol, boolean secondOrientation, boolean max, double eps, List<double[]> umbilics, double umbilicStop) {
//		double[][] A = { { 0, 0, 0, 0 }, { 0.5, 0, 0, 0 }, { 0, 0.75, 0, 0 },{ 2 / 9., 1 / 3., 4 / 9., 0 } };
//		double[] c1 = { 2 / 9., 1 / 3., 4 / 9., 0 };
//		double[] c2 = { 7 / 24., 0.25, 1 / 3., 1 / 8. };
//		double[] b = { 0, 0.5, 0.75, 1 };
//		double stepSize = umbilicStop / 2;
//		LinkedList<double[]> u = new LinkedList<double[]>();
//		int dim = y0.length;
//		double h = stepSize;
//		double tau;
//		double vau;
//		u.add(y0);
//		double[] Udomain = { ns.getUKnotVector()[0], ns.getUKnotVector()[ns.getUKnotVector().length - 1] };
//		double[] Vdomain = { ns.getVKnotVector()[0], ns.getVKnotVector()[ns.getVKnotVector().length - 1] };
//		double u1 = Udomain[0];
//		double u2 = Udomain[1];
//		double v1 = Vdomain[0];
//		double v2 = Vdomain[1];
//		double[] orientation = new double[2];
//		if (!secondOrientation) {
//			orientation = IntegralCurves.getMaxMinCurv(ns, y0[0], y0[1], max);
//		} else {
//			orientation = Rn.times(null, -1,
//					IntegralCurves.getMaxMinCurv(ns, y0[0], y0[1], max));
//		}
//		boolean nearBy = false;
//		boolean first = true;
//		double dist;
//		double[] ori = orientation;
//		
//		while (!nearBy) {
//			double[] v = new double[dim];
//			double[] sumA = new double[dim];
//			for (int i = 0; i < dim; i++) {
//				v[i] = u.getLast()[i]; // initialisiere das AWP v = y0
//			}
//			double[][] k = new double[b.length][2];
//
//			if (Rn.innerProduct(orientation,IntegralCurves.getMaxMinCurv(ns, v[0], v[1], max)) > 0) {
//				k[0] = Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns, v[0], v[1], max));
//			} else {
//				k[0] = Rn.times(null, -1, Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns, v[0], v[1], max)));
//			}
//			for (int l = 1; l < b.length; l++) {
//				sumA = Rn.times(null, A[l][0], k[0]);
//				for (int m = 1; m < l - 1; m++) {
//					sumA = Rn.add(null, sumA, Rn.times(null, A[l][m], k[m]));
//				}
//				if ((v[0] + h * sumA[0]) >= u2 || (v[0] + h * sumA[0]) <= u1|| (v[1] + h * sumA[1]) >= v2|| (v[1] + h * sumA[1]) <= v1) {
//					System.out.println("out of domain 1");
//					double[] last = new double [2];//u.getLast();
//					System.out.println("letztes v"+Arrays.toString(v));
//					if((v[0] + h * sumA[0]) >= u2){
//						last[0] = u2;
//						last[1] = v[1];
//					}
//					else if((v[0] + h * sumA[0]) <= u1){
//						last[0] = u1;// - 0.1;
//						last[1] = v[1];
//					}
//					else if((v[1] + h * sumA[1]) >= v2){
//						last[0] = v[0];
//						last[1] = v2;// + 0.1;
//					}
//					else if((v[1] + h * sumA[1]) <= v1){
//						last[0] = v[0];
//						last[1] = v1;// - 0.1;
//					}
//					u.add(last);
//					IntObjects intObj = new IntObjects(u, ori, nearBy, max);
//					System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
//					return intObj;
//				}
//				if (Rn.innerProduct(orientation,Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns, v[0] + h* sumA[0], v[1] + h * sumA[1], max))) > 0) {
//					k[l] = Rn.normalize(null, IntegralCurves.getMaxMinCurv(ns,v[0] + h * sumA[0], v[1] + h * sumA[1], max));
//				} else  {
//					k[l] = Rn.times(null, -1, Rn.normalize(null, IntegralCurves.getMaxMinCurv(ns, v[0] + h * sumA[0], v[1] + h* sumA[1], max)));
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
////			tau = Rn.euclideanNorm(Rn.add(null, Phi2,Rn.times(null, -1, Phi1)));
//			vau = Rn.euclideanNorm(u.getLast()) + 1;
//			if (tau <= tol * vau) {
//				//new
//				
//				//
//				u.add(Rn.add(null, u.getLast(), Rn.times(null, h, Phi1)));
//				for (double[] umb : umbilics) {
//					if(Rn.euclideanDistance(u.getLast(), umb) < umbilicStop){
//						IntObjects intObj = new IntObjects(u, ori, nearBy, max);
//						intObj.setUmbilicIndex(umbilics.indexOf(umb));
//						System.out.println("near umbilic");
//						System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
//						return intObj;
//					}
//				}
//				if (u.getLast()[0] >= u2 || u.getLast()[0] <= u1
//						|| u.getLast()[1] >= v2 || u.getLast()[1] <= v1) {
//					System.out.println("out of domain 2");
//					double[] last = new double [2];
//					if(u.getLast()[0] >= u2){
//						last[0] = u2;// + 0.1;
//						last[1] = v[1];
//					}
//					else if(u.getLast()[0] <= u1){
//						last[0] = u1;// - 0.1;
//						last[1] = v[1];
//					}
//					else if(u.getLast()[1] >= v2){
//						last[0] = v[0];
//						last[1] = v2;// + 0.1;
//					}
//					else if(u.getLast()[1] <= v1){
//						last[0] = v[0];
//						last[1] = v1;// - 0.1;
//					}
//					u.pollLast();
//					u.add(last);
//					System.out.println("LAST: "+ Arrays.toString(u.getLast()));
//					IntObjects intObj = new IntObjects(u, ori, nearBy, max);
//					System.out.println("Last IntObj: " + Arrays.toString(intObj.getPoints().getLast()));
//					return intObj;
//				}
//				if (Rn.innerProduct(orientation,IntegralCurves.getMaxMinCurv(ns, u.getLast()[0],u.getLast()[1], max)) > 0) {
//					orientation = IntegralCurves.getMaxMinCurv(ns,u.getLast()[0], u.getLast()[1], max);
//				} else {
//					orientation = Rn.times(null, -1, IntegralCurves.getMaxMinCurv(ns, u.getLast()[0],u.getLast()[1], max));
//				}
//			}
//			if ((tau <= tol * vau / 2 || tau >= tol * vau)) {
////				double oldH = h;
////				System.out.println("schrittaenderung beantragt");
//				h = h * Math.sqrt(tol * vau / tau);
////				if(h > stepSize * 2){
//////					System.out.println("schrittaenderung nicht durchgefuehrt");
////					h = oldH;
////				}
////				h = h * StrictMath.pow(tol * vau / tau, 1 / 2.);
//			}
//			dist = Rn.euclideanDistance(u.getLast(), y0);
//			if (!(dist < eps) && first) {
//				first = false;
//			}
//			if (dist < eps && !first) {
//				nearBy = true;
//			}
//		}
//		IntObjects intObj = new IntObjects(u, ori, nearBy, max);
//		System.out.println("letzter Punkt: "+Arrays.toString(intObj.getPoints().getLast()));
//		return intObj;
//	}
	
	private static boolean segmentIntersectBoundary(LineSegment seg, LinkedList<LineSegment> boundary){
		for (LineSegment lS : boundary) {
			if(LineSegmentIntersection.twoSegmentIntersection(seg, lS)){
				return true;
			}
		}
		return false;
	}
	
	public static double[] intersectionPoint(LineSegment first, LineSegment second){
		double s1 = first.getSegment()[0][0];
		double s2 = first.getSegment()[0][1];
		double t1 = first.getSegment()[1][0];
		double t2 = first.getSegment()[1][1];
		double p1 = second.getSegment()[0][0];
		double p2 = second.getSegment()[0][1];
		double q1 = second.getSegment()[1][0];
		double q2 = second.getSegment()[1][1];
		double lambda = ((p1 - s1) * (s2 - t2) - (p2 - s2) * (s1 - t1)) / ((q2 - p2) * (s1 - t1) - (q1 - p1) * (s2 - t2));
		return Rn.add(null, second.getSegment()[0],Rn.times(null, lambda, Rn.add(null, second.getSegment()[1], Rn.times(null, -1, second.getSegment()[0]))));
	}
	
	private static double[] boundaryIntersection(LineSegment seg, LinkedList<LineSegment> boundary){
		double minDist = Double.MAX_VALUE;
		double[] intersection = null;
		for (LineSegment lS : boundary) {
			if(Rn.euclideanDistance(seg.getSegment()[0], intersectionPoint(seg, lS)) < minDist){
				intersection = intersectionPoint(seg, lS);
				minDist = Rn.euclideanDistance(seg.getSegment()[0], intersectionPoint(seg, lS));
			}
		}
		return intersection;
	}
	
	private static boolean isOutOfDomain(NURBSSurface ns, double[] point){
		double u0 = ns.getUKnotVector()[0];
		double u1 = ns.getUKnotVector()[ns.getUKnotVector().length - 1];
		double v0 = ns.getVKnotVector()[0];
		double v1 = ns.getVKnotVector()[ns.getVKnotVector().length - 1];
		if(point[0] < u0 || point[0] > u1 ||point[1] < v0 || point[1] > v1){
			return true;
		}
		return false;
	}
	
	private static double[] projectPointIntoDomain(NURBSSurface ns, double[] point){
		double u0 = ns.getUKnotVector()[0];
		double u1 = ns.getUKnotVector()[ns.getUKnotVector().length - 1];
		double v0 = ns.getVKnotVector()[0];
		double v1 = ns.getVKnotVector()[ns.getVKnotVector().length - 1];
		double[] domainPoint = point;
		if(point[0] < u0){
			domainPoint[0] = u0;
		}
		else if(point[0] > u1){
			domainPoint[0] = u1;
		}
		else if(point[1] < v0){
			domainPoint[1] = v0;
		}
		else if(point[1] > v1){
			domainPoint[1] = v1;
		}
		return domainPoint;
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
	
	/**
	 * <p><strong>computes the curvature lines</strong></p>
	 * 
	 * this is a step-size controlled runge kutta (Bogacki–Shampine) method with the following pseudo code</br>
	 * </br>
	 * <strong>while</strong> (!nearby)
	 * 
	 * @param ns
	 * @param y0 
	 * @param tol
	 * @param secondOrientation --> is true if we use the direction of the given vectorfield an false if we use the opposite direction 
	 * @param max 
	 * @param eps --> if we obtain a closed curve then eps is the maximal distance between the start point and the last point
	 * @return
	 */
	public static IntObjects rungeKutta(NURBSSurface ns, double[] y0,double tol, boolean secondOrientation, boolean max, List<double[]> umbilics, double umbilicStop, LinkedList<LineSegment> boundary) {
		double[][] A = { { 0, 0, 0, 0 }, { 0.5, 0, 0, 0 }, { 0, 0.75, 0, 0 },{ 2 / 9., 1 / 3., 4 / 9., 0 } };
		double[] c1 = { 2 / 9., 1 / 3., 4 / 9., 0 };
		double[] c2 = { 7 / 24., 0.25, 1 / 3., 1 / 8. };
		double[] b = { 0, 0.5, 0.75, 1 };
		LinkedList<double[]> u = new LinkedList<double[]>();
		int dim = y0.length;
		double maxDist = Math.min(Math.abs(ns.getUKnotVector()[0] - ns.getUKnotVector()[ns.getUKnotVector().length - 1]), Math.abs(ns.getVKnotVector()[0] - ns.getVKnotVector()[ns.getVKnotVector().length - 1]));
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
			orientation = Rn.times(null, -1,
					IntegralCurves.getMaxMinCurv(ns, y0[0], y0[1], max));
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
				k[0] = Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns, v[0], v[1], max));
			} else {
				k[0] = Rn.times(null, -1, Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns, v[0], v[1], max)));
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
				if (segmentIntersectBoundary(seg, boundary)) {
					System.out.println("out of domain 1");
					double[] intersection = boundaryIntersection(seg, boundary);
					if(isOutOfDomain(ns, intersection)){
						intersection = projectPointIntoDomain(ns, intersection);
					}
					u.add(intersection);
					IntObjects intObj = new IntObjects(u, ori, nearBy, max);
					System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
					return intObj;
				}
				if (Rn.innerProduct(orientation,Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns, v[0] + h * sumA[0], v[1] + h * sumA[1], max))) > 0) {
					k[l] = Rn.normalize(null, IntegralCurves.getMaxMinCurv(ns,v[0] + h * sumA[0], v[1] + h * sumA[1], max));
				} else  {
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
							intObj.setUmbilicIndex(umbilics.indexOf(umb));
							System.out.println("near umbilic");
							System.out.println("letztes element: " + Arrays.toString(intObj.getPoints().getLast()));
							return intObj;
						}
						else{
							u.add(lastSegment[1]);
						}
					}
				}
				if (segmentIntersectBoundary(seg, boundary)) {
					System.out.println("out of domain 2");
					double[] intersection = boundaryIntersection(seg, boundary);
					if(isOutOfDomain(ns, intersection)){
						intersection = projectPointIntoDomain(ns, intersection);
					}
					u.pollLast();
					u.add(intersection);
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
//					System.out.println(" innerproduct > 0");
//					System.out.println("vec1 " + Arrays.toString(vec1));
//					System.out.println("vec2 " + Arrays.toString(vec1));
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
		System.out.println("letzter Punkt: "+Arrays.toString(intObj.getPoints().getLast()));
		return intObj;
	}
	

	

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
