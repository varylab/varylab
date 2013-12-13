package de.varylab.varylab.plugin.nurbs;

import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import de.varylab.varylab.plugin.nurbs.data.FaceSet;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint.ClosedBoundary;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint.FaceVertex;
import de.varylab.varylab.plugin.nurbs.math.FaceSetGenerator;

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
	

	
	private LinkedList<IntersectionPoint> getCombinatoricCaseUClosed(){
		ClosedBoundary left = ClosedBoundary.left;
		ClosedBoundary right = ClosedBoundary.right;
		ClosedBoundary interior = ClosedBoundary.interior;
		
		IntersectionPoint ip1 = new IntersectionPoint(); ip1.setClosedBoundary(left); ip1.setFaceVertex(FaceVertex.noFaceVertex); ip1.setPoint(new double[]{1});  ip1.setBoundaryPoint(true);
		IntersectionPoint ip2 = new IntersectionPoint(); ip2.setClosedBoundary(interior); ip2.setPoint(new double[]{2});  ip2.setBoundaryPoint(true);
		IntersectionPoint ip3 = new IntersectionPoint(); ip3.setClosedBoundary(interior); ip3.setPoint(new double[]{3});  ip3.setBoundaryPoint(true);
		IntersectionPoint ip4 = new IntersectionPoint(); ip4.setClosedBoundary(interior); ip4.setPoint(new double[]{4});  ip4.setBoundaryPoint(true); 
		IntersectionPoint ip5 = new IntersectionPoint(); ip5.setClosedBoundary(interior); ip5.setPoint(new double[]{5});  ip5.setBoundaryPoint(true);
		IntersectionPoint ip6 = new IntersectionPoint(); ip6.setClosedBoundary(right); ip6.setFaceVertex(FaceVertex.noFaceVertex); ip6.setPoint(new double[]{6}); ip6.setBoundaryPoint(true);
		IntersectionPoint ip7 = new IntersectionPoint(); ip7.setClosedBoundary(left); ip7.setFaceVertex(FaceVertex.noFaceVertex); ip7.setPoint(new double[]{7}); ip7.setBoundaryPoint(false); 
		IntersectionPoint ip8 = new IntersectionPoint(); ip8.setClosedBoundary(right); ip8.setFaceVertex(FaceVertex.noFaceVertex); ip8.setPoint(new double[]{8}); ip8.setBoundaryPoint(false); 
		IntersectionPoint ip9 = new IntersectionPoint(); ip9.setClosedBoundary(interior); ip9.setPoint(new double[]{9}); ip9.setBoundaryPoint(false);
		IntersectionPoint ip10 = new IntersectionPoint(); ip10.setClosedBoundary(left); ip10.setFaceVertex(FaceVertex.faceVertex); ip10.setPoint(new double[]{10}); ip10.setBoundaryPoint(false);
		IntersectionPoint ip11 = new IntersectionPoint(); ip11.setClosedBoundary(interior); ip11.setPoint(new double[]{11}); ip11.setBoundaryPoint(false);
		IntersectionPoint ip12 = new IntersectionPoint(); ip12.setClosedBoundary(right); ip12.setFaceVertex(FaceVertex.faceVertex); ip12.setPoint(new double[]{12}); ip12.setBoundaryPoint(false);
		IntersectionPoint ip13 = new IntersectionPoint(); ip13.setClosedBoundary(interior); ip13.setPoint(new double[]{13}); ip13.setBoundaryPoint(false);
		IntersectionPoint ip14 = new IntersectionPoint(); ip14.setClosedBoundary(left); ip14.setFaceVertex(FaceVertex.noFaceVertex); ip14.setPoint(new double[]{14}); ip14.setBoundaryPoint(false);
		IntersectionPoint ip15 = new IntersectionPoint(); ip15.setClosedBoundary(right); ip15.setFaceVertex(FaceVertex.noFaceVertex); ip15.setPoint(new double[]{15}); ip15.setBoundaryPoint(false);
		IntersectionPoint ip16 = new IntersectionPoint(); ip16.setClosedBoundary(left); ip16.setFaceVertex(FaceVertex.noFaceVertex); ip16.setPoint(new double[]{16});  ip16.setBoundaryPoint(true);
		IntersectionPoint ip17 = new IntersectionPoint(); ip17.setClosedBoundary(interior); ip17.setPoint(new double[]{17});  ip17.setBoundaryPoint(true);
		IntersectionPoint ip18 = new IntersectionPoint(); ip18.setClosedBoundary(interior); ip18.setPoint(new double[]{18});  ip18.setBoundaryPoint(true);
		IntersectionPoint ip19 = new IntersectionPoint(); ip19.setClosedBoundary(interior); ip19.setPoint(new double[]{19});  ip19.setBoundaryPoint(true);
		IntersectionPoint ip20 = new IntersectionPoint(); ip20.setClosedBoundary(interior); ip20.setPoint(new double[]{20});  ip20.setBoundaryPoint(true);
		IntersectionPoint ip21 = new IntersectionPoint(); ip21.setClosedBoundary(right); ip21.setFaceVertex(FaceVertex.noFaceVertex); ip21.setPoint(new double[]{21});  ip21.setBoundaryPoint(true);
		
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
		
		LinkedList<IntersectionPoint> intersections = new LinkedList<IntersectionPoint>();
		intersections.add(ip1); intersections.add(ip2); intersections.add(ip3); intersections.add(ip4); intersections.add(ip5); intersections.add(ip6); intersections.add(ip7); intersections.add(ip8);
		intersections.add(ip9); intersections.add(ip10); intersections.add(ip11); intersections.add(ip12); intersections.add(ip13); intersections.add(ip14); intersections.add(ip15); intersections.add(ip16);
		intersections.add(ip17); intersections.add(ip18); intersections.add(ip19); intersections.add(ip20); intersections.add(ip21);
		
		LinkedList<IntersectionPoint> ip1Nbrs = new LinkedList<IntersectionPoint>();
		ip1Nbrs.add(ip7);ip1Nbrs.add(ip2); ip1Nbrs.add(ip7);
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
		ip13.setUnusedNbrs(ip13UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip14Nbrs = new LinkedList<IntersectionPoint>();
		ip14Nbrs.add(ip17); ip14Nbrs.add(ip16); ip14Nbrs.add(ip10); ip14Nbrs.add(ip17);
		LinkedList<IntersectionPoint> ip14UnusedNbrs = getUnusedNbrs(ip14Nbrs);
		ip14.setNbrs(ip14Nbrs);
		ip14.setUnusedNbrs(ip14UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip15Nbrs = new LinkedList<IntersectionPoint>();
		ip15Nbrs.add(ip21); ip15Nbrs.add(ip13); ip15Nbrs.add(ip12); ip15Nbrs.add(ip21);
		LinkedList<IntersectionPoint> ip15UnusedNbrs = getUnusedNbrs(ip15Nbrs);
		ip15.setNbrs(ip15Nbrs);
		ip15.setUnusedNbrs(ip15UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip16Nbrs = new LinkedList<IntersectionPoint>();
		ip16Nbrs.add(ip17); ip16Nbrs.add(ip14); ip16Nbrs.add(ip17); 
		LinkedList<IntersectionPoint> ip16UnusedNbrs = getUnusedNbrs(ip16Nbrs);
		ip16.setNbrs(ip16Nbrs);
		ip16.setUnusedNbrs(ip16UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip17Nbrs = new LinkedList<IntersectionPoint>();
		ip17Nbrs.add(ip18); ip17Nbrs.add(ip16); ip17Nbrs.add(ip14); ip17Nbrs.add(ip18);
		LinkedList<IntersectionPoint> ip17UnusedNbrs = getUnusedNbrs(ip17Nbrs);
		ip17.setNbrs(ip17Nbrs);
		ip17.setUnusedNbrs(ip17UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip18Nbrs = new LinkedList<IntersectionPoint>();
		ip18Nbrs.add(ip19); ip18Nbrs.add(ip17); ip18Nbrs.add(ip10); ip18Nbrs.add(ip19);
		LinkedList<IntersectionPoint> ip18UnusedNbrs = getUnusedNbrs(ip18Nbrs);
		ip18.setNbrs(ip18Nbrs);
		ip18.setUnusedNbrs(ip18UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip19Nbrs = new LinkedList<IntersectionPoint>();
		ip19Nbrs.add(ip20); ip19Nbrs.add(ip18); ip19Nbrs.add(ip11); ip19Nbrs.add(ip20);
		LinkedList<IntersectionPoint> ip19UnusedNbrs = getUnusedNbrs(ip19Nbrs);
		ip19.setNbrs(ip19Nbrs);
		ip19.setUnusedNbrs(ip19UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip20Nbrs = new LinkedList<IntersectionPoint>();
		ip20Nbrs.add(ip19); ip20Nbrs.add(ip13); ip20Nbrs.add(ip21); ip20Nbrs.add(ip19);
		LinkedList<IntersectionPoint> ip20UnusedNbrs = getUnusedNbrs(ip20Nbrs);
		ip20.setNbrs(ip20Nbrs);
		ip20.setUnusedNbrs(ip20UnusedNbrs);
		
		LinkedList<IntersectionPoint> ip21Nbrs = new LinkedList<IntersectionPoint>();
		ip21Nbrs.add(ip20); ip21Nbrs.add(ip15); ip21Nbrs.add(ip20);
		LinkedList<IntersectionPoint> ip21UnusedNbrs = getUnusedNbrs(ip21Nbrs);
		ip21.setNbrs(ip21Nbrs);
		ip21.setUnusedNbrs(ip21UnusedNbrs);
				
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
	public void getNextNbrCombinatoricTest(){
			
		NURBSSurface openSpindle = createOpenSpindle();
		
		LinkedList<IntersectionPoint> intersections = getCombinatoricCaseUClosed();
		
		double dilation = 1000000000.0;
		FaceSetGenerator fsg = new FaceSetGenerator(openSpindle, dilation, intersections);

		fsg.getNextNbr(intersections.get(12), intersections.get(10));
		
		// check case 2.1.1
		Assert.assertTrue(intersections.get(18) == fsg.getNextNbr(intersections.get(12), intersections.get(10)));
		// check case 2.1.2.1
//		Assert.assertTrue(ip2 == gfs.getNextNbr(ip5, ip9));
		Assert.assertTrue(intersections.get(1) == fsg.getNextNbr(intersections.get(4), intersections.get(8)));
//		// check case 2.1.2.2
//		Assert.assertTrue(ip12 == gfs.getNextNbr(ip8, ip9));
		Assert.assertTrue(intersections.get(11) == fsg.getNextNbr(intersections.get(7), intersections.get(8)));
//		// check case 2.2.1
//		Assert.assertTrue(ip9 == gfs.getNextNbr(ip13, ip12));
		Assert.assertTrue(intersections.get(8) == fsg.getNextNbr(intersections.get(12), intersections.get(11)));
//		// check case 2.2.2
//		Assert.assertTrue(ip2 == gfs.getNextNbr(ip9, ip8));
		Assert.assertTrue(intersections.get(1) == fsg.getNextNbr(intersections.get(8), intersections.get(7)));
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
	
	@Test
	/**
	 * this method tests all adjacent faces of the vertex ip9
	 */
	public void getAllFaceVerticesCombinatoricTest(){
		NURBSSurface openSpindle = createOpenSpindle();
		
		LinkedList<IntersectionPoint> intersections = getCombinatoricCaseUClosed();
		
		IntersectionPoint ip9 = intersections.get(8);
		IntersectionPoint ip2 = intersections.get(1);
		IntersectionPoint ip5 = intersections.get(4);
		IntersectionPoint ip4 = intersections.get(3);
		IntersectionPoint ip11 = intersections.get(10);
		IntersectionPoint ip13 = intersections.get(12);
		IntersectionPoint ip10 = intersections.get(9);
		IntersectionPoint ip3 = intersections.get(2);
		
		double dilation = 1000000000.0;
		FaceSetGenerator gfs = new FaceSetGenerator(openSpindle, dilation, intersections);
		
		IntersectionPoint ip = intersections.get(8);
		int i = 0;
		while(!ip.getUnusedNbrs().isEmpty()){
			
			LinkedList<IntersectionPoint> allFaceVerticies = gfs.getAllFaceVertices(ip);
		
			if(i == 0){
				int j = 0;
				for (IntersectionPoint fv : allFaceVerticies) {
					if(j == 0) Assert.assertTrue(ip9 == fv);
					if(j == 1) Assert.assertTrue(ip2 == fv);
					if(j == 2) Assert.assertTrue(ip5 == fv);
					j++;
				}
			}
			if(i == 1){
				int j = 0;
				for (IntersectionPoint fv : allFaceVerticies) {
					if(j == 0) Assert.assertTrue(ip9 == fv);
					if(j == 1) Assert.assertTrue(ip5 == fv);
					if(j == 2) Assert.assertTrue(ip4 == fv);
					if(j == 3) Assert.assertTrue(ip11 == fv);
					j++;
				}
			}
			if(i == 2){
				int j = 0;
				for (IntersectionPoint fv : allFaceVerticies) {
					if(j == 0) Assert.assertTrue(ip9 == fv);
					if(j == 1) Assert.assertTrue(ip11 == fv);
					if(j == 2) Assert.assertTrue(ip13 == fv);
					if(j == 3) Assert.assertTrue(ip10 == fv);
					j++;
				}
			}
			if(i == 3){
				int j = 0;
				for (IntersectionPoint fv : allFaceVerticies) {
					if(j == 0) Assert.assertTrue(ip9 == fv);
					if(j == 1) Assert.assertTrue(ip10 == fv);
					if(j == 2) Assert.assertTrue(ip3 == fv);
					if(j == 3) Assert.assertTrue(ip2 == fv);
					j++;
				}
			}
			i++;
		}
	}
	
	@Test
	/**
	 * this method tests all faces
	 */
	public void createFaceSetCombinatoricTest(){
		NURBSSurface openSpindle = createOpenSpindle();
		
		LinkedList<IntersectionPoint> intersections = getCombinatoricCaseUClosed();
		double dilation = 1000000000.0;
		FaceSetGenerator gfs = new FaceSetGenerator(openSpindle, dilation, intersections);
		gfs.setLocalNbrs(intersections);
		gfs.setOrientedNbrs(intersections);
		FaceSet fs =  gfs.createFaceSet();
		int[] face0 = {0, 4, 5, 1};
		int[]face1 = {0, 3, 4};
		int[]face2 = {1, 5, 9, 10, 6, 2};
		int[]face3 =  {2, 6, 4, 3};
		int[]face4 = {4, 6, 7, 5};
		int[]face5 = {6, 10, 11, 7};
		int[]face6 = {7, 11, 8};
		int[]face7 = {7, 8, 9, 5};
		Assert.assertArrayEquals(face0, fs.getFaces()[0]);
		Assert.assertArrayEquals(face1, fs.getFaces()[1]);
		Assert.assertArrayEquals(face2, fs.getFaces()[2]);
		Assert.assertArrayEquals(face3, fs.getFaces()[3]);
		Assert.assertArrayEquals(face4, fs.getFaces()[4]);
		Assert.assertArrayEquals(face5, fs.getFaces()[5]);
		Assert.assertArrayEquals(face6, fs.getFaces()[6]);
		Assert.assertArrayEquals(face7, fs.getFaces()[7]);
	}

	
}
