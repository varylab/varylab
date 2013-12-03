package de.varylab.varylab.plugin.nurbs;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import de.varylab.varylab.plugin.nurbs.NURBSSurface.ClosingDir;
import de.varylab.varylab.plugin.nurbs.data.FaceSet;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint.ClosedBoundary;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint.FaceVertex;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;
import de.varylab.varylab.plugin.nurbs.math.GenerateFaceSet;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurve;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurvesOriginal;
import de.varylab.varylab.plugin.nurbs.math.LineSegmentIntersection;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurve.VecFieldCondition;

public class GenerateFaceSetTest {
	
	final double tol = 0.001;
	final double umbilicStop = 0.01;
	
	private NURBSSurface createOpenSpindle(){
		double[] U = {0.0, 0.0, 0.0, 1.570796326794897, 1.570796326794897, 3.141592653589793, 3.141592653589793, 4.71238898038469, 4.71238898038469, 6.283185307179586, 6.283185307179586, 6.283185307179586};
		double[] V = {-1.570796326794897, -1.570796326794897, -1.570796326794897, 0.0, 0.0, 1.570796326794897, 1.570796326794897, 1.570796326794897};
		int p = 2;
		int q = 2;
		double[][][] controlMesh = 
		{{{0.5, 0.0, -2.0, 1.0}, {0.7071067811865475, 0.0, -0.7071067811865476, 0.7071067811865476}, {1.0, 0.0, -6.123233995736766E-17, 1.0}, {0.7071067811865476, 0.0, 0.7071067811865475, 0.7071067811865476}, {0.5, 0.0, 2.0, 1.0}},
		{{0.3535533905932738, 0.3535533905932738, -1.4142135623730951, 0.7071067811865476}, {0.49999999999999994, 0.49999999999999983, -0.5000000000000001, 0.5000000000000001}, {0.7071067811865476, 0.7071067811865475, -4.329780281177467E-17, 0.7071067811865476}, {0.5000000000000001, 0.49999999999999994, 0.49999999999999994, 0.5000000000000001}, {0.3535533905932738, 0.3535533905932738, 1.4142135623730951, 0.7071067811865476}},
		{{0.0, 0.5, -2.0, 1.0}, {4.329780281177466E-17, 0.7071067811865475, -0.7071067811865476, 0.7071067811865476}, {6.123233995736766E-17, 1.0, -6.123233995736766E-17, 1.0}, {4.329780281177467E-17, 0.7071067811865476, 0.7071067811865475, 0.7071067811865476}, {0.0, 0.5, 2.0, 1.0}},
		{{-0.3535533905932738, 0.3535533905932738, -1.4142135623730951, 0.7071067811865476}, {-0.49999999999999983, 0.49999999999999994, -0.5000000000000001, 0.5000000000000001}, {-0.7071067811865475, 0.7071067811865476, -4.329780281177467E-17, 0.7071067811865476}, {-0.49999999999999994, 0.5000000000000001, 0.49999999999999994, 0.5000000000000001}, {-0.3535533905932738, 0.3535533905932738, 1.4142135623730951, 0.7071067811865476}},
		{{-0.5, 0.0, -2.0, 1.0}, {-0.7071067811865475, 8.659560562354932E-17, -0.7071067811865476, 0.7071067811865476}, {-1.0, 1.224646799147353E-16, -6.123233995736766E-17, 1.0}, {-0.7071067811865476, 8.659560562354932E-17, 0.7071067811865475, 0.7071067811865476}, {-0.5, 0.0, 2.0, 1.0}},
		{{-0.3535533905932738, -0.3535533905932738, -1.4142135623730951, 0.7071067811865476}, {-0.49999999999999994, -0.49999999999999983, -0.5000000000000001, 0.5000000000000001}, {-0.7071067811865476, -0.7071067811865475, -4.329780281177467E-17, 0.7071067811865476}, {-0.5000000000000001, -0.49999999999999994, 0.49999999999999994, 0.5000000000000001}, {-0.3535533905932738, -0.3535533905932738, 1.4142135623730951, 0.7071067811865476}},
		{{0.0, -0.5, -2.0, 1.0}, {-1.2989340843532393E-16, -0.7071067811865475, -0.7071067811865476, 0.7071067811865476}, {-1.83697019872103E-16, -1.0, -6.123233995736766E-17, 1.0}, {-1.29893408435324E-16, -0.7071067811865476, 0.7071067811865475, 0.7071067811865476}, {0.0, -0.5, 2.0, 1.0}},
		{{0.3535533905932738, -0.3535533905932738, -1.4142135623730951, 0.7071067811865476}, {0.4999999999999998, -0.49999999999999994, -0.5000000000000001, 0.5000000000000001}, {0.7071067811865474, -0.7071067811865476, -4.329780281177467E-17, 0.7071067811865476}, {0.4999999999999999, -0.5000000000000001, 0.49999999999999994, 0.5000000000000001}, {0.3535533905932738, -0.3535533905932738, 1.4142135623730951, 0.7071067811865476}},
		{{0.5, 0.0, -2.0, 1.0}, {0.7071067811865475, 0.0, -0.7071067811865476, 0.7071067811865476}, {1.0, 0.0, -6.123233995736766E-17, 1.0}, {0.7071067811865476, 0.0, 0.7071067811865475, 0.7071067811865476}, {0.5, 0.0, 2.0, 1.0}}};
		
		NURBSSurface openSpindle = new NURBSSurface(U, V, controlMesh, p, q);
		return openSpindle;
	}
	
	@SuppressWarnings("unused")
	private NURBSSurface createRhino_1Sphere(){
		double[] U = {-1.570796326794897, -1.570796326794897, -1.570796326794897, 0.0, 0.0, 1.570796326794897, 1.570796326794897, 1.570796326794897};
		double[] V = {0.0, 0.0, 0.0, 1.570796326794897, 1.570796326794897, 3.141592653589793, 3.141592653589793, 4.71238898038469, 4.71238898038469, 6.283185307179586, 6.283185307179586, 6.283185307179586};
		int p = 2;
		int q = 2;
		double[][][] controlMesh = 
		{{{0.0, 0.0, -1.0, 1.0}, {0.0, 0.0, -0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, -1.0, 1.0}, {0.0, 0.0, -0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, -1.0, 1.0}, {0.0, 0.0, -0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, -1.0, 1.0}, {0.0, 0.0, -0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, -1.0, 1.0}},
		{{0.7071067811865475, 0.0, -0.7071067811865476, 0.7071067811865476}, {0.49999999999999994, 0.49999999999999983, -0.5000000000000001, 0.5000000000000001}, {4.329780281177466E-17, 0.7071067811865475, -0.7071067811865476, 0.7071067811865476}, {-0.49999999999999983, 0.49999999999999994, -0.5000000000000001, 0.5000000000000001}, {-0.7071067811865475, 8.659560562354932E-17, -0.7071067811865476, 0.7071067811865476}, {-0.49999999999999994, -0.49999999999999983, -0.5000000000000001, 0.5000000000000001}, {-1.2989340843532393E-16, -0.7071067811865475, -0.7071067811865476, 0.7071067811865476}, {0.4999999999999998, -0.49999999999999994, -0.5000000000000001, 0.5000000000000001}, {0.7071067811865475, 0.0, -0.7071067811865476, 0.7071067811865476}},
		{{1.0, 0.0, -6.123233995736766E-17, 1.0}, {0.7071067811865476, 0.7071067811865475, -4.329780281177467E-17, 0.7071067811865476}, {6.123233995736766E-17, 1.0, -6.123233995736766E-17, 1.0}, {-0.7071067811865475, 0.7071067811865476, -4.329780281177467E-17, 0.7071067811865476}, {-1.0, 1.224646799147353E-16, -6.123233995736766E-17, 1.0}, {-0.7071067811865476, -0.7071067811865475, -4.329780281177467E-17, 0.7071067811865476}, {-1.83697019872103E-16, -1.0, -6.123233995736766E-17, 1.0}, {0.7071067811865474, -0.7071067811865476, -4.329780281177467E-17, 0.7071067811865476}, {1.0, 0.0, -6.123233995736766E-17, 1.0}},
		{{0.7071067811865476, 0.0, 0.7071067811865475, 0.7071067811865476}, {0.5000000000000001, 0.49999999999999994, 0.49999999999999994, 0.5000000000000001}, {4.329780281177467E-17, 0.7071067811865476, 0.7071067811865475, 0.7071067811865476}, {-0.49999999999999994, 0.5000000000000001, 0.49999999999999994, 0.5000000000000001}, {-0.7071067811865476, 8.659560562354932E-17, 0.7071067811865475, 0.7071067811865476}, {-0.5000000000000001, -0.49999999999999994, 0.49999999999999994, 0.5000000000000001}, {-1.29893408435324E-16, -0.7071067811865476, 0.7071067811865475, 0.7071067811865476}, {0.4999999999999999, -0.5000000000000001, 0.49999999999999994, 0.5000000000000001}, {0.7071067811865476, 0.0, 0.7071067811865475, 0.7071067811865476}},
		{{0.0, 0.0, 1.0, 1.0}, {0.0, 0.0, 0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, 1.0, 1.0}, {0.0, 0.0, 0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, 1.0, 1.0}, {0.0, 0.0, 0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, 1.0, 1.0}, {0.0, 0.0, 0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, 1.0, 1.0}}};
		
		NURBSSurface rhino_1Sphere = new NURBSSurface(U, V, controlMesh, p, q);
		return rhino_1Sphere;
	}
	
	
	private LinkedList<IntersectionPoint> getUnusedNbrs(LinkedList<IntersectionPoint> Nbrs){
		LinkedList<IntersectionPoint> unusedNbrs = new LinkedList<IntersectionPoint>();
		for (IntersectionPoint nbr : Nbrs) {
			unusedNbrs.add(nbr);
		}
		unusedNbrs.pollLast();
		return unusedNbrs;
	}
	
	private LinkedList<IntersectionPoint> getCombinatoricCase(){
		ClosedBoundary left = ClosedBoundary.left;
		ClosedBoundary right = ClosedBoundary.right;
		ClosedBoundary interior = ClosedBoundary.interior;
		
		IntersectionPoint ip1 = new IntersectionPoint(); ip1.setClosedBoundary(left); ip1.setFaceVertex(FaceVertex.noFaceVertex); 
		IntersectionPoint ip2 = new IntersectionPoint(); ip2.setClosedBoundary(interior);
		IntersectionPoint ip3 = new IntersectionPoint(); ip3.setClosedBoundary(interior);
		IntersectionPoint ip4 = new IntersectionPoint(); ip4.setClosedBoundary(interior);
		IntersectionPoint ip5 = new IntersectionPoint(); ip5.setClosedBoundary(interior);
		IntersectionPoint ip6 = new IntersectionPoint(); ip6.setClosedBoundary(right); ip6.setFaceVertex(FaceVertex.noFaceVertex);
		IntersectionPoint ip7 = new IntersectionPoint(); ip7.setClosedBoundary(left); ip7.setFaceVertex(FaceVertex.noFaceVertex);
		IntersectionPoint ip8 = new IntersectionPoint(); ip8.setClosedBoundary(right); ip8.setFaceVertex(FaceVertex.noFaceVertex);
		IntersectionPoint ip9 = new IntersectionPoint(); ip9.setClosedBoundary(interior);
		IntersectionPoint ip10 = new IntersectionPoint(); ip10.setClosedBoundary(left); ip10.setFaceVertex(FaceVertex.faceVertex);
		IntersectionPoint ip11 = new IntersectionPoint(); ip11.setClosedBoundary(interior);
		IntersectionPoint ip12 = new IntersectionPoint(); ip12.setClosedBoundary(right); ip12.setFaceVertex(FaceVertex.faceVertex);
		IntersectionPoint ip13 = new IntersectionPoint(); ip13.setClosedBoundary(interior);
		IntersectionPoint ip14 = new IntersectionPoint(); ip14.setClosedBoundary(left); ip14.setFaceVertex(FaceVertex.noFaceVertex);
		IntersectionPoint ip15 = new IntersectionPoint(); ip15.setClosedBoundary(right); ip15.setFaceVertex(FaceVertex.noFaceVertex);
		IntersectionPoint ip16 = new IntersectionPoint(); ip16.setClosedBoundary(left); ip16.setFaceVertex(FaceVertex.noFaceVertex);
		IntersectionPoint ip17 = new IntersectionPoint(); ip17.setClosedBoundary(interior);
		IntersectionPoint ip18 = new IntersectionPoint(); ip18.setClosedBoundary(interior);
		IntersectionPoint ip19 = new IntersectionPoint(); ip19.setClosedBoundary(interior);
		IntersectionPoint ip20 = new IntersectionPoint(); ip20.setClosedBoundary(interior);
		IntersectionPoint ip21 = new IntersectionPoint(); ip21.setClosedBoundary(right); ip21.setFaceVertex(FaceVertex.noFaceVertex);
		
		ip1.setOpposite(ip6);
		ip6.setOpposite(ip1);
		ip7.setOpposite(ip8);
		ip8.setOpposite(ip7);	
		ip10.setOpposite(ip12);
		ip12.setOpposite(ip10);
		ip14.setOpposite(ip15);
		ip15.setOpposite(ip14);
		ip16.setOpposite(ip21);
		ip21.setOpposite(ip16);
		
		LinkedList<IntersectionPoint> ip1Nbrs = new LinkedList<IntersectionPoint>();
		ip1Nbrs.add(ip7);ip1Nbrs.add(ip2); ip1Nbrs.add(ip1);
		LinkedList<IntersectionPoint> ip1UnusedNbrs = getUnusedNbrs(ip1Nbrs);
		ip1.setNbrs(ip1Nbrs);
		ip1.setUnusedNbrs(ip1UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip2Nbrs = new LinkedList<IntersectionPoint>();
		ip2Nbrs.add(ip1);ip2Nbrs.add(ip3);ip2Nbrs.add(ip7);ip2Nbrs.add(ip1);
		LinkedList<IntersectionPoint> ip2UnusedNbrs = getUnusedNbrs(ip2Nbrs);
		ip2.setNbrs(ip2Nbrs);
		ip2.setUnusedNbrs(ip2UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip3Nbrs = new LinkedList<IntersectionPoint>();
		ip3Nbrs.add(ip2);ip3Nbrs.add(ip4);ip3Nbrs.add(ip10);ip3Nbrs.add(ip2);
		LinkedList<IntersectionPoint> ip3UnusedNbrs = getUnusedNbrs(ip3Nbrs);
		ip3.setNbrs(ip3Nbrs);
		ip3.setUnusedNbrs(ip3UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip4Nbrs = new LinkedList<IntersectionPoint>();
		ip4Nbrs.add(ip3);ip4Nbrs.add(ip5);ip4Nbrs.add(ip11);ip4Nbrs.add(ip3);
		LinkedList<IntersectionPoint> ip4UnusedNbrs = getUnusedNbrs(ip4Nbrs);
		ip4.setNbrs(ip4Nbrs);
		ip4.setUnusedNbrs(ip4UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip5Nbrs = new LinkedList<IntersectionPoint>();
		ip5Nbrs.add(ip4);ip5Nbrs.add(ip6);ip5Nbrs.add(ip9);ip5Nbrs.add(ip4);
		LinkedList<IntersectionPoint> ip5UnusedNbrs = getUnusedNbrs(ip5Nbrs);
		ip5.setNbrs(ip5Nbrs);
		ip5.setUnusedNbrs(ip5UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip6Nbrs = new LinkedList<IntersectionPoint>();
		ip6Nbrs.add(ip5);ip6Nbrs.add(ip8);ip6Nbrs.add(ip5);
		LinkedList<IntersectionPoint> ip6UnusedNbrs = getUnusedNbrs(ip6Nbrs);
		ip6.setNbrs(ip6Nbrs);
		ip6.setUnusedNbrs(ip6UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip7Nbrs = new LinkedList<IntersectionPoint>();
		ip7Nbrs.add(ip10);ip7Nbrs.add(ip1);ip7Nbrs.add(ip2);ip7Nbrs.add(ip10);
		LinkedList<IntersectionPoint> ip7UnusedNbrs = getUnusedNbrs(ip7Nbrs);
		ip7.setNbrs(ip7Nbrs);
		ip7.setUnusedNbrs(ip7UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip8Nbrs = new LinkedList<IntersectionPoint>();
		ip8Nbrs.add(ip12);ip8Nbrs.add(ip9);ip8Nbrs.add(ip6);ip8Nbrs.add(ip12);
		LinkedList<IntersectionPoint> ip8UnusedNbrs = getUnusedNbrs(ip8Nbrs);
		ip8.setNbrs(ip8Nbrs);
		ip8.setUnusedNbrs(ip8UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip9Nbrs = new LinkedList<IntersectionPoint>();
		ip9Nbrs.add(ip12); ip9Nbrs.add(ip11); ip9Nbrs.add(ip5); ip9Nbrs.add(ip8); ip9Nbrs.add(ip12);
		LinkedList<IntersectionPoint> ip9UnusedNbrs = getUnusedNbrs(ip9Nbrs);
		ip9.setNbrs(ip9Nbrs);
		ip9.setUnusedNbrs(ip9UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip10Nbrs = new LinkedList<IntersectionPoint>();
		ip10Nbrs.add(ip14);ip10Nbrs.add(ip7);ip10Nbrs.add(ip3);ip10Nbrs.add(ip18);ip10Nbrs.add(ip14);
		LinkedList<IntersectionPoint> ip10UnusedNbrs = getUnusedNbrs(ip10Nbrs);
		ip10.setNbrs(ip10Nbrs);
		ip10.setUnusedNbrs(ip10UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip11Nbrs = new LinkedList<IntersectionPoint>();
		ip11Nbrs.add(ip13); ip11Nbrs.add(ip19); ip11Nbrs.add(ip4); ip11Nbrs.add(ip9); ip11Nbrs.add(ip13);
		LinkedList<IntersectionPoint> ip11UnusedNbrs = getUnusedNbrs(ip11Nbrs);
		ip11.setNbrs(ip11Nbrs);
		ip11.setUnusedNbrs(ip11UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip12Nbrs = new LinkedList<IntersectionPoint>();
		ip12Nbrs.add(ip15);ip12Nbrs.add(ip13);ip12Nbrs.add(ip9);ip12Nbrs.add(ip8);ip12Nbrs.add(ip15);
		LinkedList<IntersectionPoint> ip12UnusedNbrs = getUnusedNbrs(ip12Nbrs);
		ip12.setNbrs(ip12Nbrs);
		ip12.setUnusedNbrs(ip12UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip13Nbrs = new LinkedList<IntersectionPoint>();
		ip13Nbrs.add(ip15); ip13Nbrs.add(ip20); ip13Nbrs.add(ip11); ip13Nbrs.add(ip12); ip13Nbrs.add(ip15);
		LinkedList<IntersectionPoint> ip13UnusedNbrs = getUnusedNbrs(ip13Nbrs);
		ip13.setNbrs(ip13Nbrs);
		ip9.setUnusedNbrs(ip13UnusedNbrs);
		
	
		LinkedList<IntersectionPoint> intersections = new LinkedList<IntersectionPoint>();
		intersections.add(ip1); intersections.add(ip2); intersections.add(ip3); intersections.add(ip4); intersections.add(ip5); intersections.add(ip6); intersections.add(ip7); intersections.add(ip8);
		intersections.add(ip9); intersections.add(ip10); intersections.add(ip11); intersections.add(ip12); intersections.add(ip13); intersections.add(ip14); intersections.add(ip15); intersections.add(ip16);
		intersections.add(ip17); intersections.add(ip18); intersections.add(ip19); intersections.add(ip20); intersections.add(ip21);
		return intersections;
	}
	
//	@Test
//	// this test depends on the runge kutte method
//	public void OpenSpindleTestRungeKutta(){
//		NURBSSurface openSpindle = createOpenSpindle();
//		System.out.println(openSpindle);
//		double[] start1 = {5.585053606381854, 0.17453292519943298};
//		double[] start2 = {0.0, 0.17453292519943298};
//		List<double[]> startingPointsUV = new LinkedList<double[]>();
//		startingPointsUV.add(start1);
//		startingPointsUV.add(start2);
//		VecFieldCondition vfc = VecFieldCondition.conjugate;
//		IntegralCurve ic = new IntegralCurve(openSpindle, vfc, tol);
//		boolean firstVectorField = true;
//		boolean secondVectorField = true;
//		LinkedList<double[]> singularities = new LinkedList<double[]>();
//		LinkedList<PolygonalLine> currentLines = ic.computeIntegralLines(firstVectorField, secondVectorField, 1, umbilicStop, singularities, startingPointsUV);
//		LinkedList<LineSegment> allSegments = new LinkedList<LineSegment>();
//		for(PolygonalLine pl : currentLines){
//			allSegments.addAll(pl.getpLine());
//		}
//		double[] U = openSpindle.getUKnotVector();
//		double[] V = openSpindle.getVKnotVector();
////		allSegments = LineSegmentIntersection.preSelection(U, V, allSegments);
//		int shiftedIndex = currentLines.getLast().getCurveIndex();
//		List<LineSegment> completeDomainBoundarySegments = openSpindle.getCompleteDomainBoundarySegments();
//
//		
//
//		System.out.println("IN TEST all boundary segments");
//		for (LineSegment bs : completeDomainBoundarySegments) {
//			System.out.println("boundary segment ");
//			bs.setCurveIndex(bs.getCurveIndex() + shiftedIndex);
//			System.out.println(bs);
//		}
//		allSegments.addAll(completeDomainBoundarySegments);	
//		System.out.println();
//		System.out.println("IN TEST all segments");
//		for (LineSegment ls : allSegments) {
//			System.out.println(ls.toString());
//		}
//		
//		allSegments = LineSegmentIntersection.preSelection(U, V, allSegments);
//		double dilation = 1000000000.0;
//		LinkedList<IntersectionPoint> intersections = LineSegmentIntersection.BentleyOttmannAlgoritm(U, V, allSegments, dilation);
//		System.out.println();
//		System.out.println("all intersection points");
//		for (IntersectionPoint ip : intersections) {
//			System.out.println(ip.toString());
//		}
//	
//		GenerateFaceSet gfs = new GenerateFaceSet(openSpindle, dilation, intersections);
//		LinkedList<IntersectionPoint> localNbrs = gfs.findAllLocalNbrs(openSpindle, dilation, intersections);
//		LinkedList<IntersectionPoint> oriented = gfs.orientedNbrs(localNbrs);
//		System.out.println("LOCAL NBRS");
//		for (IntersectionPoint ip : oriented) {
//			System.out.println(ip);
//		}
//		IntersectionPoint ip2 = new IntersectionPoint();
//		IntersectionPoint ip5 = new IntersectionPoint();
//		IntersectionPoint ip8 = new IntersectionPoint();
//		IntersectionPoint ip9 = new IntersectionPoint();
//		IntersectionPoint ip11 = new IntersectionPoint();
//		IntersectionPoint ip12 = new IntersectionPoint();
//		IntersectionPoint ip13 = new IntersectionPoint();
//		IntersectionPoint ip19 = new IntersectionPoint();
//		for (IntersectionPoint ip : oriented) {
//			if(ip.getPoint()[0] == 5.911644905378303 && ip.getPoint()[1] == 0.7123512635333257){
//				ip9 = ip;
//				LinkedList<IntersectionPoint> unusedNbrs = getUnusedNbrs(ip9.getNbrs());		
//				ip9.setUnusedNbrs(unusedNbrs);
//			}
//			if(ip.getPoint()[0] == 5.307499215 && ip.getPoint()[1] == 1.570796326){
//				LinkedList<IntersectionPoint> unusedNbrs = getUnusedNbrs(ip.getNbrs());		
//				ip.setUnusedNbrs(unusedNbrs);
//				ip5 = ip;
//			}
//			if(ip.getPoint()[0] == 0.280160435 && ip.getPoint()[1] == 1.570796326){
//				LinkedList<IntersectionPoint> unusedNbrs = getUnusedNbrs(ip.getNbrs());		
//				ip.setUnusedNbrs(unusedNbrs);
//				ip2 = ip;
//			}
//			if(ip.getPoint()[0] == 6.283185307 && ip.getPoint()[1] == 1.247303652){
//				LinkedList<IntersectionPoint> unusedNbrs = getUnusedNbrs(ip.getNbrs());		
//				ip.setUnusedNbrs(unusedNbrs);
//				ip8 = ip;
//			}
//			if(ip.getPoint()[0] == 5.585053606 && ip.getPoint()[1] == 0.174532925){
//				LinkedList<IntersectionPoint> unusedNbrs = getUnusedNbrs(ip.getNbrs());		
//				ip.setUnusedNbrs(unusedNbrs);
//				ip11 = ip;
//			}
//			if(ip.getPoint()[0] == 6.283185307 && ip.getPoint()[1] == 0.174532925){
////				LinkedList<IntersectionPoint> unusedNbrs = getUnusedNbrs(ip.getNbrs());		
////				ip.setUnusedNbrs(unusedNbrs);
//				ip12 = ip;
//			}
//			if(ip.getPoint()[0] == 0.280160435 && ip.getPoint()[1] == 1.570796326){
////				LinkedList<IntersectionPoint> unusedNbrs = getUnusedNbrs(ip.getNbrs());		
////				ip.setUnusedNbrs(unusedNbrs);
//				ip2 = ip;
//			}
//			if(ip.getPoint()[0] == 5.914101770660268 && ip.getPoint()[1] == -0.36949489651049927){
//				LinkedList<IntersectionPoint> unusedNbrs = getUnusedNbrs(ip.getNbrs());		
//				ip.setUnusedNbrs(unusedNbrs);
//				ip13 = ip;
//			}
//			if(ip.getPoint()[0] == 4.418973406 && ip.getPoint()[1] == -1.570796326){
////				LinkedList<IntersectionPoint> unusedNbrs = getUnusedNbrs(ip.getNbrs());		
////				ip.setUnusedNbrs(unusedNbrs);
//				ip19 = ip;
//			}
//		}
//		// check case 1.1
//		ip11.getUnusedNbrs().pollLast();
//		ip11.getUnusedNbrs().pollLast();
//		ip11.getUnusedNbrs().pollLast();
//		Assert.assertTrue(ip9 == gfs.getNextNbr(null, ip11));
//		// check case 1.2.1
//		ip9.getUnusedNbrs().add(ip8); // ?????????????????????????
//		Assert.assertTrue(ip2 == gfs.getNextNbr(null, ip9));
//		// check case 1.2.2
//		ip13.getUnusedNbrs().pollLast();
//		ip13.getUnusedNbrs().pollLast();
//		Assert.assertTrue(ip12 == gfs.getNextNbr(null, ip13));
//		// check case 2.1.1
//		Assert.assertTrue(ip19 == gfs.getNextNbr(ip13, ip11));
//		// check case 2.1.2.1
//		Assert.assertTrue(ip2 == gfs.getNextNbr(ip5, ip9));
//		// check case 2.1.2.2
//		Assert.assertTrue(ip12 == gfs.getNextNbr(ip8, ip9));
//		// check case 2.2.1
//		Assert.assertTrue(ip9 == gfs.getNextNbr(ip13, ip12));
//		// check case 2.2.2
//		Assert.assertTrue(ip2 == gfs.getNextNbr(ip9, ip8));
//		System.out.println("CHECK");
//		for (IntersectionPoint ip : ip9.getUnusedNbrs()) {
//			System.out.println(ip);
//		}
//	}
	
	@Test
	// this test depends on the runge kutte method
	public void OpenSpindleTest(){
			
		NURBSSurface openSpindle = createOpenSpindle();
		
		LinkedList<IntersectionPoint> intersections = getCombinatoricCase();
		
		double dilation = 1000000000.0;
		GenerateFaceSet gfs = new GenerateFaceSet(openSpindle, dilation, intersections);
//		GenerateFaceSet gfs = new GenerateFaceSet(openSpindle, dilation, intersections);
//		LinkedList<IntersectionPoint> localNbrs = gfs.findAllLocalNbrs(openSpindle, dilation, intersections);
//		LinkedList<IntersectionPoint> oriented = gfs.orientedNbrs(localNbrs);
//		System.out.println("LOCAL NBRS");
//		for (IntersectionPoint ip : oriented) {
//			System.out.println(ip);
//		}

		gfs.getNextNbr(intersections.get(12), intersections.get(10));
		
		// check case 2.1.1
		Assert.assertTrue(intersections.get(18) == gfs.getNextNbr(intersections.get(12), intersections.get(10)));
		// check case 2.1.2.1
//		Assert.assertTrue(ip2 == gfs.getNextNbr(ip5, ip9));
		Assert.assertTrue(intersections.get(1) == gfs.getNextNbr(intersections.get(4), intersections.get(8)));
//		// check case 2.1.2.2
//		Assert.assertTrue(ip12 == gfs.getNextNbr(ip8, ip9));
		Assert.assertTrue(intersections.get(11) == gfs.getNextNbr(intersections.get(7), intersections.get(8)));
//		// check case 2.2.1
//		Assert.assertTrue(ip9 == gfs.getNextNbr(ip13, ip12));
		Assert.assertTrue(intersections.get(8) == gfs.getNextNbr(intersections.get(12), intersections.get(11)));
//		// check case 2.2.2
//		Assert.assertTrue(ip2 == gfs.getNextNbr(ip9, ip8));
		Assert.assertTrue(intersections.get(1) == gfs.getNextNbr(intersections.get(8), intersections.get(7)));
//		// check case 1.1	
//		Assert.assertTrue(ip9 == gfs.getNextNbr(null, ip11));
//		// check case 1.2.1
//		Assert.assertTrue(ip2 == gfs.getNextNbr(null, ip9));
//		// check case 1.2.2
//		Assert.assertTrue(ip12 == gfs.getNextNbr(null, ip13));
//		// check case 2.1.1
//		Assert.assertTrue(ip19 == gfs.getNextNbr(ip13, ip11));
//		// check case 2.1.2.1
//		Assert.assertTrue(ip2 == gfs.getNextNbr(ip5, ip9));
//		// check case 2.1.2.2
//		Assert.assertTrue(ip12 == gfs.getNextNbr(ip8, ip9));
//		// check case 2.2.1
//		Assert.assertTrue(ip9 == gfs.getNextNbr(ip13, ip12));
//		// check case 2.2.2
//		Assert.assertTrue(ip2 == gfs.getNextNbr(ip9, ip8));
	}
	
	
//	@Test
//	public void getNextNbrTest(){
//		double[] U = {-3,3};
//		double[] V = {-2,2};
//		int p = 2;
//		int q = 2;
//		double[][][] controlMesh = {{{0.0, 0.0, -1.0, 1.0}, {0.0, 0.0, -0.7071067811865476, 0.7071067811865476}}};
//		NURBSSurface ns = new NURBSSurface(U, V, controlMesh, p, q);
//		double[] p1 = {-3,2}, p2 = {-2,2}, p3 = {-1,2}, p4 = {0,2}, p5 = {1,2}, p6 = {3,2}, p7 = {-3,1}, p8 = {3,1}, p9 = {2.5,0.5}, p10 = {0,0}, p11 = {2,0},
//		p12 = {3,0}, p13 = {2.5,-0.5}, p14 = {-3,-1}, p15 = {3,-1}, p16 = {-3,-2}, p17 = {-2,-2}, p18 = {-1,-2}, p19 = {0,-2}, p20 = {1,-2}, p21 = {3,-2};
//		LineSegment ls1 = new LineSegment(p6, p1);
//		LineSegment ls2 = new LineSegment(p1, p16);
//		LineSegment ls3 = new LineSegment(p16, p21);
//		LineSegment ls4 = new LineSegment(p21, p6);
//		LineSegment ls5 = new LineSegment(p7, p2);
//		LineSegment ls6 = new LineSegment(p10, p3);
//		LineSegment ls7 = new LineSegment(p14, p17);
//		LineSegment ls8 = new LineSegment(p10, p18);
//		LineSegment ls9 = new LineSegment(p19, p11);
//		LineSegment ls10 = new LineSegment(p20, p13);
//		LineSegment ls11 = new LineSegment(p11, p13);
//		LineSegment ls12 = new LineSegment(p13, p12);
//		LineSegment ls13 = new LineSegment(p13, p15);
//		LineSegment ls14 = new LineSegment(p4, p11);
//		LineSegment ls15 = new LineSegment(p5, p9);
//		LineSegment ls16 = new LineSegment(p9, p8);
//		LineSegment ls17 = new LineSegment(p11, p9);
//		LineSegment ls18 = new LineSegment(p9, p12);
//	}
	
//	@Test
//	public void debugShpere(){
//		double[] U = {-1.570796326794897, -1.570796326794897, -1.570796326794897, 0.0, 0.0, 1.570796326794897, 1.570796326794897, 1.570796326794897};
//		double[] V = {0.0, 0.0, 0.0, 1.570796326794897, 1.570796326794897, 3.141592653589793, 3.141592653589793, 4.71238898038469, 4.71238898038469, 6.283185307179586, 6.283185307179586, 6.283185307179586};
//		int p = 2;
//		int q = 2;
//		double[][][] controlMesh = 
//		{{{0.0, 0.0, -1.0, 1.0}, {0.0, 0.0, -0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, -1.0, 1.0}, {0.0, 0.0, -0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, -1.0, 1.0}, {0.0, 0.0, -0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, -1.0, 1.0}, {0.0, 0.0, -0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, -1.0, 1.0}},
//		{{0.7071067811865475, 0.0, -0.7071067811865476, 0.7071067811865476}, {0.49999999999999994, 0.49999999999999983, -0.5000000000000001, 0.5000000000000001}, {4.329780281177466E-17, 0.7071067811865475, -0.7071067811865476, 0.7071067811865476}, {-0.49999999999999983, 0.49999999999999994, -0.5000000000000001, 0.5000000000000001}, {-0.7071067811865475, 8.659560562354932E-17, -0.7071067811865476, 0.7071067811865476}, {-0.49999999999999994, -0.49999999999999983, -0.5000000000000001, 0.5000000000000001}, {-1.2989340843532393E-16, -0.7071067811865475, -0.7071067811865476, 0.7071067811865476}, {0.4999999999999998, -0.49999999999999994, -0.5000000000000001, 0.5000000000000001}, {0.7071067811865475, 0.0, -0.7071067811865476, 0.7071067811865476}},
//		{{1.0, 0.0, -6.123233995736766E-17, 1.0}, {0.7071067811865476, 0.7071067811865475, -4.329780281177467E-17, 0.7071067811865476}, {6.123233995736766E-17, 1.0, -6.123233995736766E-17, 1.0}, {-0.7071067811865475, 0.7071067811865476, -4.329780281177467E-17, 0.7071067811865476}, {-1.0, 1.224646799147353E-16, -6.123233995736766E-17, 1.0}, {-0.7071067811865476, -0.7071067811865475, -4.329780281177467E-17, 0.7071067811865476}, {-1.83697019872103E-16, -1.0, -6.123233995736766E-17, 1.0}, {0.7071067811865474, -0.7071067811865476, -4.329780281177467E-17, 0.7071067811865476}, {1.0, 0.0, -6.123233995736766E-17, 1.0}},
//		{{0.7071067811865476, 0.0, 0.7071067811865475, 0.7071067811865476}, {0.5000000000000001, 0.49999999999999994, 0.49999999999999994, 0.5000000000000001}, {4.329780281177467E-17, 0.7071067811865476, 0.7071067811865475, 0.7071067811865476}, {-0.49999999999999994, 0.5000000000000001, 0.49999999999999994, 0.5000000000000001}, {-0.7071067811865476, 8.659560562354932E-17, 0.7071067811865475, 0.7071067811865476}, {-0.5000000000000001, -0.49999999999999994, 0.49999999999999994, 0.5000000000000001}, {-1.29893408435324E-16, -0.7071067811865476, 0.7071067811865475, 0.7071067811865476}, {0.4999999999999999, -0.5000000000000001, 0.49999999999999994, 0.5000000000000001}, {0.7071067811865476, 0.0, 0.7071067811865475, 0.7071067811865476}},
//		{{0.0, 0.0, 1.0, 1.0}, {0.0, 0.0, 0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, 1.0, 1.0}, {0.0, 0.0, 0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, 1.0, 1.0}, {0.0, 0.0, 0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, 1.0, 1.0}, {0.0, 0.0, 0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, 1.0, 1.0}}};
//		NURBSSurface ns = new NURBSSurface(U, V, controlMesh, p, q);
//		System.out.println("closing direction = " + ns.getClosingDir());
//		double[] p1 = {0.17453292519943298, 0.0};
//		double[] p2 = {0.17453292519943298, 5.585053606381854};
//		List<double[]> startingPointsUV = new LinkedList<double[]>();
//		startingPointsUV.add(p1);
//		startingPointsUV.add(p2);
//		List<LineSegment> boundary = ns.getBoundarySegments();
//		System.out.println("CHECK BOUNDARY");
//		for (LineSegment bs : boundary) {
//			System.out.println(bs.toString());
//		}
//		int curveIndex = 1;
//		
////		IntObjects intObj =  IntegralCurves.rungeKuttaConjugateLine(ns, p1, tol, false, false, null, umbilicStop, boundary);
////		for (double[] point : intObj.getPoints()) {
////			System.out.println(Arrays.toString(point));
////		}
//		
//		List<PolygonalLine> currentLines = IntegralCurvesOriginal.computeIntegralLines(ns, true, true, curveIndex, tol, umbilicStop, null, startingPointsUV);
//		int counter = 0;
//		for (PolygonalLine pl : currentLines) {
//			counter ++;
//			System.out.println(counter + ". line:");
//			for (LineSegment ls : pl.getpLine()) {
//				System.out.println(ls.toString());
//			}
//			
//		}
//		
//	}
	
}
