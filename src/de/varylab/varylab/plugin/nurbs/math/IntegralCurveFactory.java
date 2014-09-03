package de.varylab.varylab.plugin.nurbs.math;

import static de.varylab.varylab.plugin.nurbs.math.NurbsDomainUtility.intersectionPoint;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.ClosingDir;
import de.varylab.varylab.plugin.nurbs.NurbsDomain;
import de.varylab.varylab.plugin.nurbs.data.IntObjects;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;
import de.varylab.varylab.plugin.nurbs.data.ValidSegment;

public class IntegralCurveFactory {
	
	public enum SymmetricDir{
		CURVATURE, DIRECTION, NO_SYMMETRY
	};
	
	public enum VectorFields {
		FIRST, SECOND, BOTH;
	}
	
	private static Logger logger = Logger.getLogger(IntegralCurveFactory.class.getName());
	
	double[][] basis;
	private double tol = 1E-4;
	
	private VectorFields vectorFields = VectorFields.BOTH;
	
	private double singularityNeighborhood = 1E-6;
	
	private VectorFieldProvider
		vectorFieldProvider = null;
	
	private NurbsDomain 
		domain = null;
	
	public IntegralCurveFactory(NurbsDomain d){
		domain = d;
		
	}
	
	private IntegralCurveFactory(NurbsDomain domain2, double singularityNeighborhood2, double tol2,	VectorFieldProvider vectorFieldProvider2, VectorFields vectorFields2) {
		this.domain = domain2;
		this.singularityNeighborhood = singularityNeighborhood2;
		this.tol = tol2;
		this.vectorFieldProvider = vectorFieldProvider2;
		this.vectorFields = vectorFields2;
	}

	/**
	 * 
	 * @param p
	 * @return a given direction if the surface is not a surface of revolution  </br>
	 * else in the case of a surface of revolution:</br>
	 * 1.case (gaussian curvature K >= 0):</br> 
	 * a direction will be returned such that the conjugate direction appears with the same angle with respect to the rotation axis</br>
	 *  <table>
	 * <tr><td><td><td><td><td><td>l<td>m<td><td><td><td>-v1
	 * <tr><td>(v1,<td>1)<td><td><td>*<td>m<td>n<td>*<td><td><td>1<td><td>= -l * v1^2 + n  = 0 <=> v1 = sqrt(n/l)
	 * </table> 
	 * </br>
	 * 2.case (gaussian curvature K < 0):</br> 
	 * an assymptotic direction will be returned
	 */
	
//	private double[] getSymConjDirSurfaceOfRevolution(double[] p) {
//		double[] dir = {1,1};
//		if(!ns.isSurfaceOfRevolution()){
//			return dir;
//		}
//		else{
//			CurvatureInfo ci =  NURBSCurvatureUtility.curvatureAndDirections(ns, p);
//			double[][] sF = ci.getSecondFundamental();
//			double l = sF[0][0];
//			double n = sF[1][1];
//			double K = ci.getGaussCurvature();
//			if(K >= 0){
//				dir[0] = Math.sqrt(n / l);
//				return dir;
//				
//			}
//			else{
//				return getAssymptoticDirection(ns, p);
//				
//			}
//		}
//	}
	
	public double[][] getShiftedBoundaryIntersectionPoints(double[] point1, double[] point2){
		logger.info("GET SHIFTED");
		double[][] intersectionPoints = new double[2][2];
		int[] domain1 = domain.getModDomain(point1);
		int[] domain2 = domain.getModDomain(point2);
		double[][] seg = new double[2][2];
		if(domain1[0] > domain2[0]){ // left
			double Shift =  domain.uRange();
			seg[0] = domain.getPointInOriginalDomain(point1);
			seg[1] = domain.getPointInOriginalDomain(point2);
			seg[1][0] = seg[1][0] - Shift;
			double[][] line = {{domain.getUMin(),domain.getVMin()},{domain.getUMin(),domain.getVMax()}};
			double[] leftIntersection = intersectionPoint(seg, line);
			if(leftIntersection[0] != domain.getUMin()){
				leftIntersection[0] = domain.getUMin();
			}
			double[] rightIntersection = {domain.getUMax(), leftIntersection[1]};
			intersectionPoints[0] = leftIntersection;
			intersectionPoints[1] = rightIntersection;
			logger.info("left");
			
		}else if(domain1[0] < domain2[0]){ // right
			double Shift =  domain.getUMax() - domain.getUMin();
			seg[0] = domain.getPointInOriginalDomain(point1);
			seg[1] = domain.getPointInOriginalDomain(point2);
			seg[1][0] = seg[1][0] + Shift;
			double[][] line = {{domain.getUMax(),domain.getVMin()},{domain.getUMax(),domain.getVMax()}};
			double[] rightIntersection = intersectionPoint(seg, line);
			if(rightIntersection[0] != domain.getUMax()){
				rightIntersection[0] = domain.getUMax();
			}
			double[] leftIntersection = {domain.getUMin(), rightIntersection[1]};
			intersectionPoints[0] = rightIntersection;
			intersectionPoints[1] = leftIntersection;
			logger.info("right");
		}else if(domain1[1] > domain2[1]){ // lower
			double Shift =  domain.vRange();
			seg[0] = domain.getPointInOriginalDomain(point1);
			seg[1] = domain.getPointInOriginalDomain(point2);
			seg[1][1] = seg[1][1] - Shift;
			double[][] line = {{domain.getUMin(),domain.getVMin()},{domain.getUMax(),domain.getVMin()}};
			double[] lowerIntersection = intersectionPoint(seg, line);
			if(lowerIntersection[1] != domain.getVMin()){
				lowerIntersection[1] = domain.getVMin();
			}
			double[] upperIntersection = {lowerIntersection[0],domain.getVMax()};
			intersectionPoints[0] = lowerIntersection;
			intersectionPoints[1] = upperIntersection;
			logger.info("lower");
		}
		else{ // upper
			double Shift =  domain.vRange();
			seg[0] = domain.getPointInOriginalDomain(point1);
			seg[1] = domain.getPointInOriginalDomain(point2);
			seg[1][1] = seg[1][1] + Shift;
			double[][] line = {{domain.getUMin(),domain.getVMax()},{domain.getUMax(),domain.getVMax()}};
			double[] upperIntersection = intersectionPoint(seg, line);
			if(upperIntersection[1] != domain.getVMax()){
				upperIntersection[1] = domain.getVMax();
			}
			double[] lowerIntersection = {upperIntersection[0],domain.getVMin()};
			intersectionPoints[0] = upperIntersection;
			intersectionPoints[1] = lowerIntersection;
			logger.info("upper");
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
				if(counter == 1 && domain.isClosedBoundaryPoint(seg[0]) && domain.pointsAreInDifferentShiftedtDomains(seg[0], seg[1])){
					domain.flipClosedBoundaryPoint(pointList.getFirst());
					domainList.add(domain.getPointInOriginalDomain(p));
				} else {
					if(domain.pointsAreInDifferentShiftedtDomains(seg[0], seg[1])){
						double[][] intersections = getShiftedBoundaryIntersectionPoints(seg[0], seg[1]);
						domainList.add(intersections[0]);
						domainList.add(intersections[1]);
					}
					
				}
				double[] domainPoint = domain.getPointInOriginalDomain(p);
				double[] check = domainList.getLast();
				if(!Arrays.equals(check, domainPoint)){
					domainList.add(domainPoint);
				}
				else{
//					logger.info("DOPPEL PUNKT");
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
	
	public boolean terminationConditionForVectorfieldPoints(double[] point, LinkedList<double[]> pointList){
		if(pointList == null || pointList.isEmpty()) {
			return true;
		}
		if(domain.pointIsOutsideOfExtendedDomain(point)){
			double[] last = pointList.pollLast();
			double[] nextToLast = pointList.getLast();
			LineSegment ls = new LineSegment(nextToLast, last);
			double[] intersection =  domain.boundaryIntersection(ls);
			pointList.add(intersection);
			logger.info("terminationConditionForVectorfieldPoints");
			return true;
		}
		return false;
	}
	
	public boolean isCloseToBoundary(double[] point){
		double eps = 0.01;
		if(domain.getClosingDir() == ClosingDir.nonClosed){
			if(Math.abs(domain.getUMin() - point[0]) < eps){
				return true;
			}
			if(Math.abs(domain.getUMax() - point[0]) < eps){
				return true;
			}
			if(Math.abs(domain.getVMin() - point[1]) < eps){
				return true;
			}
			if(Math.abs(domain.getVMax() - point[1]) < eps){
				return true;
			}
		}
		if(domain.getClosingDir() == ClosingDir.uClosed){
			if(Math.abs(domain.getVMin() - point[1]) < eps){
				return true;
			}
			if(Math.abs(domain.getVMax() - point[1]) < eps){
				return true;
			}
		}

		if(domain.getClosingDir() == ClosingDir.vClosed){
			if(Math.abs(domain.getUMin() - point[0]) < eps){
				return true;
			}
			if(Math.abs(domain.getUMax() - point[0]) < eps){
				return true;
			}
		}
		return false;
	}
	
	public boolean terminationConditionForPoints(double[] point, LinkedList<double[]> pointList,  List<LineSegment> boundary){
		if(domain.pointIsOutsideOfExtendedDomain(point)){
			LineSegment ls = new LineSegment(pointList.getLast(), point);
			double[] intersection = domain.boundaryIntersection(ls);
			pointList.add(intersection);
			logger.info("terminationConditionForPoints pointIsOutsideOfExtendedDomain");
			return true;
		}
		if(isCloseToBoundary(point)){
//			logger.info("is close to boundary");
			double[] last = pointList.pollLast();
			double[] nextToLast = pointList.getLast();
			LineSegment ls = new LineSegment(nextToLast, last);
			double[] intersection =  domain.boundaryIntersection(ls);
			pointList.add(intersection);
			logger.info("terminationConditionForPoints isCloseToBoundary");
			return true;
		}
		else{
//			logger.info("LEFT distance " + Math.abs(domain.getUMin() - point[0]));
		}
		
		return false;
	}


	
	/**
	 * the normalized vector field is a representative of a continuous line field i.e it is unique up to opposite direction.
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

	
	private double[] getVecField(double[] uv, VectorFields vf){
		return vectorFieldProvider.getVectorField(uv, vf);
	}
	
	
	public IntObjects rungeKutta(double[] startPoint, boolean secondOrientation, VectorFields vf) {
		double[][] A = { { 0, 0, 0, 0 }, { 0.5, 0, 0, 0 }, { 0, 0.75, 0, 0 },{2 / 9., 1 / 3., 4 / 9., 0 } };
		double[] c1 = { 2 / 9., 1 / 3., 4 / 9., 0 };
		double[] c2 = { 7 / 24., 0.25, 1 / 3., 1 / 8. };
//		double[][] A =	{{0,0,0,0,0,0,0},{1/5.,0,0,0,0,0,0},{3/40.,9/40.,0,0,0,0,0},{44/45.,-56/15.,32/9.,0,0,0,0},{19372/6561.,-25360/2187.,64448/6561.,-212/729.,0,0,0},{9017/3168.,-355/33.,46732/5247.,49/176.,-5103/18656.,0,0},{35/384.,	0, 500/1113., 125/192., -2187/6784., 11/84.,0}};
//		double[] c1 = {35/384., 0, 500/1113., 125/192., -2187/6784., 11/84.,	0 };
//		double[] c2 = {5179/57600., 0, 7571/16695., 393/640.,	-92097/339200., 187/2100., 1/40.};
//	 	double[] b = { 0,1/5., 3/10.,4/5.,8/9.,1,1 };
//		System.out.println("START IN RUNGE KUTTA");
		double tau;
		double etha;
		int counter = 0;
		double[] initialValue = startPoint.clone();
		LinkedList<double[]> pointList = new LinkedList<double[]>();
		double h = Math.max(domain.uRange(), domain.vRange()) / 100;
		double maxDist = Math.min(domain.uRange(), domain.vRange()) / 40;
		double [] vec1 = new double[2];
		double [] vec2 = new double[2];
		boolean closed = false;
		pointList.add(initialValue);
		double[] orientation = new double[2];
		if (!secondOrientation) {
			orientation = getVecField(initialValue, vf);
		} else {
			orientation = Rn.times(null, -1, getVecField(initialValue, vf));
		}
		boolean nearBy = false;
		double dist;
		double[] ori = orientation;
		LineSegment seg = new LineSegment();
	
		while (!nearBy && counter < 10000) {
			counter++;
			if(counter == 10000){
				System.out.println("termination after 10000 steps");
				logger.info("termination after 10000 steps");
			}
			double[] last = pointList.getLast().clone();
			double[] sumA = new double[2];
			double[][] k = new double[c1.length][2];
			double[] vectorfieldPoint = new double[2];
			// the current point is in the extended domain!!!
			
			k[0] = getVecField(domain.getPointInOriginalDomain(last), vf);
			k[0] = getContinuousNormalizedVectorField(orientation, k[0]);		
			
			for (int l = 1; l < c1.length; l++) {
				Rn.times(sumA, A[l][0], k[0]);
				for (int m = 1; m < l - 1; m++) {
					Rn.add(sumA, sumA, Rn.times(null, A[l][m], k[m]));
				}
				Rn.add(vectorfieldPoint, last, Rn.times(null, h, sumA));
				if(terminationConditionForVectorfieldPoints(vectorfieldPoint, pointList)){
					pointList = setIntoDomain(pointList);
					IntObjects intObj = new IntObjects(pointList, ori, nearBy, vf);
					return intObj;
				}
				k[l] = getVecField(domain.getPointInOriginalDomain(vectorfieldPoint), vf);
				k[l] = getContinuousNormalizedVectorField(orientation, k[l]);		
			}
			double[] Phi1 = new double[2];
			double[] Phi2 = new double[2];
			for (int l = 0; l < c1.length; l++) {
				Rn.add(Phi1, Phi1, Rn.times(null, c1[l], k[l]));
				Rn.add(Phi2, Phi2, Rn.times(null, c2[l], k[l]));
			}
			tau = Rn.euclideanNorm(Rn.subtract(null, Phi2, Phi1));
			etha = Rn.euclideanNorm(last) + 1;
			if(tau <= tol * etha){
				double[] next = new double[2];
				Rn.add(next, last, Rn.times(null, h, Phi1));
				if(terminationConditionForPoints(next, pointList, domain.getBoundarySegments())){
					pointList = setIntoDomain(pointList);
					IntObjects intObj = new IntObjects(pointList, ori, nearBy, vf);
		
					return intObj;
				}
				else{
					Rn.subtract(orientation, next, pointList.getLast());
					pointList.add(next);
				}
			}
			if ((tau <= tol * etha / 2 || tau >= tol * etha)) {
				double[] next = new double[2];
				Rn.add(next, last, Rn.times(null, h, Phi1));
				h = h * (tol * etha / tau);
				if(h > maxDist){
					h = maxDist;
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
				dist = distLineSegmentPoint(startPoint, seg);
				if(Rn.innerProduct(vec1, vec2) < 0){
					closed = true;
				}
				if(dist < singularityNeighborhood && closed){
					nearBy = true;
					pointList = setIntoDomain(pointList);
					IntObjects intObj = new IntObjects(pointList, ori, nearBy, vf);
					return intObj;
				}
				else{
					pointList.add(lastSegment[1]);
				}
			}
		}
		pointList = setIntoDomain(pointList);
		IntObjects intObj = new IntObjects(pointList, ori, nearBy, vf);
		return intObj;
	}
	
	public IntObjects rungeKuttaTest(double[] startPoint, boolean secondOrientation, VectorFields vf, List<double[]> singularities, double minSigularityDistance) {
		double[][] A = { { 0, 0, 0, 0 }, { 0.5, 0, 0, 0 }, { 0, 0.75, 0, 0 },{2 / 9., 1 / 3., 4 / 9., 0 } };
		double[] c1 = { 2 / 9., 1 / 3., 4 / 9., 0 };
		double[] c2 = { 7 / 24., 0.25, 1 / 3., 1 / 8. };
	//	double[] b = { 0, 0.5, 0.75, 1 };
	//	double[][] A =	{{0,0,0,0,0,0,0},{1/5.,0,0,0,0,0,0},{3/40.,9/40.,0,0,0,0,0},{44/45.,-56/15.,32/9.,0,0,0,0},{19372/6561.,-25360/2187.,64448/6561.,-212/729.,0,0,0},{9017/3168.,-355/33.,46732/5247.,49/176.,-5103/18656.,0,0},{35/384.,	0, 500/1113., 125/192., -2187/6784., 11/84.,0}};
	//	double[] c1 = {35/384., 0, 500/1113., 125/192., -2187/6784., 11/84.,	0 };
	//	double[] c2 = {5179/57600., 0, 7571/16695., 393/640.,	-92097/339200., 187/2100., 1/40.};
	// double[] b = { 0,1/5., 3/10.,4/5.,8/9.,1,1 };
		double tau;
		double etha;
		
		int counter = 0;
		double[] initialValue = startPoint.clone();
		LinkedList<double[]> pointList = new LinkedList<double[]>();
		double h = Math.max(domain.uRange(), domain.vRange()) / 100;
		double maxDist = Math.min(domain.uRange(), domain.vRange()) / 40;
		double [] vec1 = new double[2];
		double [] vec2 = new double[2];
		boolean closed = false;
		pointList.add(initialValue);
		double[] orientation = new double[2];
		if (!secondOrientation) {
	//		orientation = getConjugateVecField(initialValue, firstVectorField);
			orientation = getVecField(initialValue, vf);
		} else {
			orientation = Rn.times(null, -1, getVecField(initialValue, vf));
		}
		boolean nearBy = false;
		double dist;
		double[] ori = orientation;
		LineSegment seg = new LineSegment();
	
		while (!nearBy && counter < 200) {
			counter++;
			if(counter == 200){
				logger.info("termination after 2000 steps");
			}
	//		logger.info("THE POINT " + Arrays.toString(pointList.getLast()));
			System.out.println("h = " + h);
			double[] last = pointList.getLast().clone();
			double[] sumA = new double[2];
			double[][] k = new double[c1.length][2];
			double[] vectorfieldPoint = new double[2];
			// the current point is in the extended domain!!!
			
			k[0] = getVecField(domain.getPointInOriginalDomain(last), vf);
			k[0] = getContinuousNormalizedVectorField(orientation, k[0]);		
			
			for (int l = 1; l < c1.length; l++) {
				Rn.times(sumA, A[l][0], k[0]);
				for (int m = 1; m < l - 1; m++) {
					Rn.add(sumA, sumA, Rn.times(null, A[l][m], k[m]));
				}
				Rn.add(vectorfieldPoint, last, Rn.times(null, h, sumA));
				if(terminationConditionForVectorfieldPoints(vectorfieldPoint, pointList)){
					pointList = setIntoDomain(pointList);
					IntObjects intObj = new IntObjects(pointList, ori, nearBy, vf);
//					logger.info("the lines");
//					for (double[] p : intObj.getPoints()) {
//						logger.info(Arrays.toString(p));
//					}
					return intObj;
				}
				k[l] = getVecField(domain.getPointInOriginalDomain(vectorfieldPoint), vf);
				k[l] = getContinuousNormalizedVectorField(orientation, k[l]);		
			}
			
			double[] Phi1 = new double[2];
			double[] Phi2 = new double[2];
			for (int l = 0; l < c1.length; l++) {
				Rn.add(Phi1, Phi1, Rn.times(null, c1[l], k[l]));
				Rn.add(Phi2, Phi2, Rn.times(null, c2[l], k[l]));
			}
			tau = Rn.euclideanNorm(Rn.subtract(null, Phi2, Phi1));
			etha = Rn.euclideanNorm(last) + 1;
			if(tau <= tol * etha){
//				logger.info("nicht nachregeln");
				double[] next = new double[2];
				Rn.add(next, last, Rn.times(null, h, Phi1));
				if(terminationConditionForPoints(next, pointList, domain.getBoundarySegments())){
					pointList = setIntoDomain(pointList);
					IntObjects intObj = new IntObjects(pointList, ori, nearBy, vf);
					logger.info("the lines");
					for (double[] p : intObj.getPoints()) {
						logger.info(Arrays.toString(p));
					}
					return intObj;
				}
				else{
					Rn.subtract(orientation, next, pointList.getLast());
					pointList.add(next);
				}
			}
			if ((tau <= tol * etha / 2 || tau >= tol * etha)) {
				double hOld = h;
				h = h * (tol * etha / tau);
				if(h > maxDist){
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
				dist = distLineSegmentPoint(startPoint, seg);
				if(Rn.innerProduct(vec1, vec2) < 0){
					closed = true;
				}
				if(dist < minSigularityDistance && closed){
					nearBy = true;
					logger.info("closed");
					pointList = setIntoDomain(pointList);
					IntObjects intObj = new IntObjects(pointList, ori, nearBy, vf);
					logger.info("the lines");
					for (double[] p : intObj.getPoints()) {
						logger.info(Arrays.toString(p));
					}
					return intObj;
				}
				else{
					pointList.add(lastSegment[1]);
				}
			}
		}
		pointList = setIntoDomain(pointList);
		IntObjects intObj = new IntObjects(pointList, ori, nearBy, vf);
		logger.info("the lines");
		for (double[] p : intObj.getPoints()) {
			logger.info(Arrays.toString(p));
		}
		return intObj;
	}


	
	private ValidSegment isValidSegment(double[][] seg, int rightShift, int upShift){
		ValidSegment vs = new ValidSegment();
		vs.setValid(false);
		if (seg[0][0] == domain.getUMin() && seg[1][0] == domain.getUMax()){
			rightShift--;
		} else if (seg[0][0] == domain.getUMax() && seg[1][0] == domain.getUMin()){
			rightShift++;
		} else if (seg[0][1] == domain.getVMin() && seg[1][1] == domain.getVMax()){
			upShift--;
		} else if (seg[0][1] == domain.getVMax() && seg[1][1] == domain.getVMin()){
			upShift++;
		} else if (seg[0][0] == seg[1][0] && seg[0][1] == seg[1][1]){
		} else {
			vs.setValid(true);
		}
		vs.setRightShift(rightShift);
		vs.setUpShift(upShift);
		return vs;
	}
	
	public PolygonalLine curveLine(double[] startPoint, VectorFields vf) throws CurveException {
		try {
		LinkedList<LineSegment> currentSegments = new LinkedList<LineSegment>();
		LinkedList<double[]> all = new LinkedList<double[]>();
		IntObjects intObj = rungeKutta(startPoint, false, vf);
		Collections.reverse(intObj.getPoints());
		all.addAll(intObj.getPoints());
		logger.info("first size" + all.size());
		boolean cyclic = false;
		if(!intObj.isNearby()){
			intObj = rungeKutta(startPoint, true, vf);
			all.addAll(intObj.getPoints());
		}else{
			//add the first element of a closed curve
			cyclic = true;
			logger.info("add first");
			double[] first = new double [2];
			first[0] = all.getFirst()[0];
			first[1] = all.getFirst()[1];
			all.add(first);
		}
		int index = 0;
		int rightShift = 0;
		int upShift = 0;
		double[] firstcurvePoint = all.getFirst();
		for (double[] secondCurvePoint : all) {
			index ++;
			if(index != 1){
				double[][]seg = new double[2][];
				seg[0] = firstcurvePoint.clone();
				seg[1] = secondCurvePoint.clone();
				ValidSegment vs = isValidSegment(seg, rightShift, upShift);
				rightShift = vs.getRightShift();
				upShift = vs.getUpShift();
				boolean segmentIsValid = vs.isValid();
				if(segmentIsValid){
					LineSegment ls = new  LineSegment();
					ls.setIndexOnCurve(index) ;
					ls.setSegment(seg);
					ls.setCyclic(cyclic);
					ls.setRightShift(rightShift);
					ls.setUpShift(upShift);
					if(seg[0][0] != seg[1][0] && seg[1][0] != seg[1][1]){
						currentSegments.add(ls);
					}
					
					firstcurvePoint = secondCurvePoint;
				}
				else{
					index--;
					firstcurvePoint = secondCurvePoint;
				}
			}
		}
		PolygonalLine currentLine = new PolygonalLine(currentSegments);
		currentLine.setDescription((vf == VectorFields.FIRST?"max:":"min:") + "("+String.format("%.3f", startPoint[0]) +", "+String.format("%.3f", startPoint[1])+")");
		return currentLine;
		} catch (Exception e) {
			throw new CurveException(e.getMessage());
		}

	}
	
	
	public LinkedList<PolygonalLine> computeIntegralLines(List<double[]> startingPointsUV) throws CurveException {
		LinkedList<PolygonalLine> currentLines = new LinkedList<PolygonalLine>();
		for(double[] start : startingPointsUV) {
				currentLines.addAll(computeIntegralLine(start));
		}
		return currentLines;
	}
	
	public LinkedList<PolygonalLine> computeIntegralLine(double[] start) throws CurveException {
		LinkedList<PolygonalLine> currentLines = new LinkedList<PolygonalLine>();	
		switch (vectorFields) {
		case FIRST:
			currentLines.add(curveLine(start, VectorFields.FIRST));
			break;
		case SECOND:
			currentLines.add(curveLine(start, VectorFields.SECOND));
			break;
		case BOTH:
			currentLines.add(curveLine(start, VectorFields.FIRST));
			currentLines.add(curveLine(start, VectorFields.SECOND));
			break;
		}
		return currentLines;
	}

	public void setTol(double tol) {
		this.tol = tol;
	}

	public void setVectorFields(VectorFields vectorFields) {
		this.vectorFields = vectorFields;
	}

	public void setUmbillicStop(double umbilicStop) {
		singularityNeighborhood = umbilicStop;
	}

	public double getSingularityNeighborhood() {
		return singularityNeighborhood;
	}

	public void setSingularityNeighborhood(double singularityNeighborhood) {
		this.singularityNeighborhood = singularityNeighborhood;
	}

	public void setVectorFieldProvider(VectorFieldProvider vectorFieldProvider) {
		this.vectorFieldProvider = vectorFieldProvider;
	}

	public double getTol() {
		return tol;
	}

	public VectorFields getVectorFields() {
		return vectorFields;
	}

	public IntegralCurveFactory getCopy() {
		IntegralCurveFactory copy = new IntegralCurveFactory(domain,singularityNeighborhood,tol,vectorFieldProvider,vectorFields);
		return copy;
	}
	
	public class CurveException extends Exception {

		public CurveException(String message) {
			super(message);
		}

		private static final long serialVersionUID = 1L;
		
	}
}
