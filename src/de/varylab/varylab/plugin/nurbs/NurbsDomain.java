package de.varylab.varylab.plugin.nurbs;

import static de.varylab.varylab.plugin.nurbs.math.NurbsDomainUtility.intersectionPoint;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.ClosingDir;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;

public class NurbsDomain {
	
	private static Logger logger = Logger.getLogger(NurbsDomain.class.getSimpleName());
	
	private double uMin, uMax, vMin, vMax;
	private ClosingDir closDir = null;
	
	public NurbsDomain(NURBSSurface surf) {
		closDir = getClosingDir(surf);
		double[] U = surf.getUKnotVector();
		uMin = U[0];
		uMax = U[U.length-1];
		double[] V = surf.getVKnotVector();
		vMin = V[0];
		vMax = V[V.length-1];
	}
	
	private ClosingDir determineClosingCondition(NURBSSurface surf){
		if(isClosedUDir(surf) && isClosedVDir(surf)){
			return ClosingDir.uvClosed;
		}
		if(isClosedUDir(surf)){
			return ClosingDir.uClosed;
		}
		if(isClosedVDir(surf)){
			return ClosingDir.vClosed;
		}
		else{
			return ClosingDir.nonClosed;
		}
	}
	
	public void setClosingDir(ClosingDir dir){
		closDir = dir;
	}
	
	private ClosingDir getClosingDir(NURBSSurface surf){
		if(closDir == null){
			return determineClosingCondition(surf);
		}
		else{
			return closDir;
		}
	}
	
	private boolean isClosedUDir(NURBSSurface surf){
		double[][][] controlMesh = surf.getControlMesh();
		int m = controlMesh.length;
		int n = controlMesh[0].length;
		for (int j = 0; j < n; j++) {
			if(Rn.euclideanDistance(controlMesh[0][j], controlMesh[m - 1][j]) > 0.0001){
				return false;
			}
		}
		return true;
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
	
	private boolean isUBoundaryValue(double boundaryValue){
		return (boundaryValue == uMin || boundaryValue == uMax);
	}
	
	private double[] projectOntoBoundary(double[] point){
		LinkedList<Double> boundaryValues = getBoundaryValues();
		double boundaryValue = getMinBoundValue(point, boundaryValues);
		if(isUBoundaryValue(boundaryValue)){
			point[0] = boundaryValue;
		}
		else{
			point[1] = boundaryValue;
		}
		return point;
	}
	
	public double[] boundaryIntersection(LineSegment seg){
		double minDist = Double.MAX_VALUE;
		double[] intersection = null;
		for (LineSegment lS : getBoundarySegments()) {
			if(Rn.euclideanDistance(seg.getSegment()[0], intersectionPoint(seg, lS)) < minDist){
				intersection = intersectionPoint(seg, lS);
				minDist = Rn.euclideanDistance(seg.getSegment()[0], intersectionPoint(seg, lS));
			}
		}
		if(isNotAtBoundary(intersection)){
			intersection = projectOntoBoundary(intersection);
		}
		return intersection;
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
	
	
	private boolean isClosedVDir(NURBSSurface surf){
		double[][][] controlMesh = surf.getControlMesh();
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
		if(closDir == ClosingDir.uClosed){
			if(point[0] == uMin || point[0] == uMax){
				return true;
			}
			else return false;
		}
		if(closDir == ClosingDir.vClosed){
			if(point[1] == vMin || point[1] == vMax){
				return true;
			}
			else return false;
		}
		if(closDir == ClosingDir.uvClosed){
			if(point[0] == uMin || point[0] == uMax || point[1] == vMin || point[1] == vMax){
				return true;
			}
			else return false;
		}
		return false;
	}
	
	
	
	public double[] getGluedBoundaryValues(){
		double[] closedBoundaryValues = new double[2];
		if(closDir == ClosingDir.nonClosed){
			return null;
		}
		else if(closDir == ClosingDir.uClosed){
			closedBoundaryValues[0] = uMin;
			closedBoundaryValues[1] = uMax;
		}
		else if(closDir == ClosingDir.vClosed){
			closedBoundaryValues[0] = vMin;
			closedBoundaryValues[1] = vMax;
		}
		return closedBoundaryValues;
	}
	
	public List<double[]> getBoundaryVerticesUV() {
		List<double[]> boundaryVerts = new LinkedList<double[]>();
		double[] boundVert1 = new double[2];
		boundVert1[0] = uMin;
		boundVert1[1] = vMin;
		double[] boundVert2 = new double[2];
		boundVert2[0] = uMax;
		boundVert2[1] = vMin;
		double[] boundVert3 = new double[2];
		boundVert3[0] = uMax;
		boundVert3[1] = vMax;
		double[] boundVert4 = new double[2];
		boundVert4[0] = uMin;
		boundVert4[1] = vMax;
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
		if(closDir == ClosingDir.nonClosed){
			boundarySegments.add(b1);
			boundarySegments.add(b2);
			boundarySegments.add(b3);
			boundarySegments.add(b4);
		}
		else if(closDir == ClosingDir.uClosed){
			boundarySegments.add(b1);
			boundarySegments.add(b3);
		}
		else if(closDir == ClosingDir.vClosed){
			boundarySegments.add(b2);
			boundarySegments.add(b4);
		}
		return boundarySegments;
	}
	
	public LinkedList<Double> determineClosedBoundaryValues(){
		LinkedList<Double> closedBoundaryValues = new LinkedList<Double>();
		if(closDir == ClosingDir.vClosed){
			double v0 = vMin;
			double vn = vMax;
			closedBoundaryValues.add(v0);
			closedBoundaryValues.add(vn);
		}
		else{
			double u0 = uMin;
			double um = uMax;
			closedBoundaryValues.add(u0);
			closedBoundaryValues.add(um);
		}
		return closedBoundaryValues;
	}
	
	
	
	public LinkedList<Double> getBoundaryValues(){
		LinkedList<Double> boundaryValues = new LinkedList<Double>();
		if(closDir == ClosingDir.uClosed){
			double v0 = vMin;
			double vn = vMax;
			boundaryValues.add(v0);
			boundaryValues.add(vn);
		}
		else if(closDir == ClosingDir.vClosed){
			double u0 = uMin;
			double um = uMax;
			boundaryValues.add(u0);
			boundaryValues.add(um);
		}
		else{
			double u0 = uMin;
			double um = uMax;
			double v0 = vMin;
			double vn = vMax;
			boundaryValues.add(u0);
			boundaryValues.add(um);
			boundaryValues.add(v0);
			boundaryValues.add(vn);
		}
		return boundaryValues;
	}

	public ClosingDir getClosingDir() {
		return closDir;
	}
	
	
	public boolean pointIsInU(double[] point){
		if(point[0] < uMin || point[0] > uMax){
			return false;
		}
		return true;
	}
	
	public boolean pointIsInV(double[] point){
		if(point[1] < vMin || point[1] > vMax){
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
		if(closDir == ClosingDir.uvClosed){
			return false;
		}
		if(closDir == ClosingDir.uClosed){
			if(point[1] > vMax || point[1] < vMin){
				return true;
			}
			return false;
		}
		if(closDir == ClosingDir.vClosed){
			if(point[0] > uMax || point[0] < uMin){
				return true;
			}
			return false;
		}
		else{
			if(point[1] > vMax || point[1] < vMin || point[0] > uMax || point[0] < uMin){
				return true;
			}
			return false;
		}
	}
	
	
	public double modInterval(double left, double right, double x){
		double xShift = x - left;
		double length = right - left;
		double mod = xShift % length;
		if(Math.signum(mod) < 0){
			return right + mod;
		}
		else{
			if(mod >= length){
				logger.info("GROESSER LENGTH");
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
			domainPoint[0] = modInterval(uMin, uMax, point[0]);
		}
		if(!pointIsInV(point)){
			domainPoint[1] = modInterval(vMin, vMax, point[1]);
		}
		return domainPoint;
	}
	
	
	public boolean isNotAtBoundary(double[] point){
		if(point[0] != uMin && point[0] != uMax && point[1] != vMin && point[1] != vMax){
			return true;
		}
		return false;
	}
	
	
	public int[] getModDomain(double [] point){
		int[] domain = new int[2];
		domain[0] = getModInterval(uMin, uMax, point[0]);
		domain[1] = getModInterval(vMin, vMax, point[1]);
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
	
	
	public void flipClosedBoundaryPoint(double[] point){
		if(closDir == ClosingDir.uClosed){
			if(point[0] == uMin){
				point[0] = uMax;
			}
			else{
				point[0] = uMin;
			}
		}
		if(closDir == ClosingDir.vClosed){
			if(point[1] == vMin){
				point[1] = vMax;
			}
			else{
				point[1] = vMin;
			}
		}
	}

	public double uRange() {
		return uMax-uMin;
	}

	public double getUMin() {
		return uMin;
	}

	public double getUMax() {
		return uMax;
	}

	public double getVMin() {
		return vMin;
	}

	public double getVMax() {
		return vMax;
	}

	public double vRange() {
		return vMax-vMin;
	}

}