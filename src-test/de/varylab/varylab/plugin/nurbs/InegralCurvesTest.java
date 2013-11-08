package de.varylab.varylab.plugin.nurbs;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import de.varylab.varylab.plugin.nurbs.math.IntegralCurve;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurvesOriginal;

public class InegralCurvesTest {
	
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

	@Test
	public void modIntervalTest() {
		double left = 1.1;
		double right = 3.;
		double x = 3.5;
		double y = -0.7;
		double resultX = IntegralCurvesOriginal.modInterval(left, right, x);
		double resultY = IntegralCurvesOriginal.modInterval(left, right, y);
		System.out.println(resultX);
		System.out.println(resultY);
		Assert.assertEquals(1.6, resultX, 0.0000001);
		Assert.assertEquals(1.2, resultY, 0.0000001);
		
	}
	
	@Test
	public void getModIntervalTest() {
		double left = 1.;
		double right = 3.;
		double x = 1;
		double y = 3;
		int[] resultX = new int[1];
		int[] resultXExpected = {0};
		int[] resultY = new int[1];
		int[] resultYExpected = {0};
		resultX[0] = IntegralCurvesOriginal.getModInterval(left, right, x);
		System.out.println("resx " + IntegralCurvesOriginal.getModInterval(left, right, x));
		resultY[0] = IntegralCurvesOriginal.getModInterval(left, right, y);
		System.out.println("resy " + IntegralCurvesOriginal.getModInterval(left, right, y));
		Assert.assertArrayEquals(resultXExpected, resultX);
		Assert.assertArrayEquals(resultYExpected, resultY);
	}
	
	@Test
	public void pointsAreInDifferentDomainsTest(){
		double u0 = 1.0;
		double um = 3.0;
		double v0 = 2.1;
		double vn = 2.3;
		double[] point1 = {2.0, 2.2};
		double[] point2 = {0.5, 2.0};
		double[] point3 = {2.0, 2.0};
		Assert.assertTrue(IntegralCurvesOriginal.pointsAreInDifferentDomains(u0, um, v0, vn, point1, point2));
		Assert.assertTrue(IntegralCurvesOriginal.pointsAreInDifferentDomains(u0, um, v0, vn, point1, point3));
		
	}
	
	@Test
	public void getShiftedBoundaryIntersectionPointsTest(){
		double u0 = 0.0;
		double um = 2.0;
		double v0 = 0.0;
		double vn = 2.0;
//		double[] q1 = {1,1};
//		double[] q2 = {1,2};
//		double[][] intersections = IntegralCurves.getShiftedBoundaryIntersectionPoints(u0, um, v0, vn, q1, q2);
//		System.out.println("int 1 " + Arrays.toString(intersections[0]));
//		System.out.println("int 2 " + Arrays.toString(intersections[1]));
//		System.out.println("jetzt richtig");
		double[] p1 = {1.5, 1};
		double[] p2 = {0.5, 3};
		double[][] intersections = IntegralCurvesOriginal.getShiftedBoundaryIntersectionPoints(u0, um, v0, vn, p1, p2);
		double[][] upper = {{1,2}, {1,0}};
		Assert.assertArrayEquals(upper, intersections);
		System.out.println("int 1 " + Arrays.toString(intersections[0]));
		System.out.println("int 2 " + Arrays.toString(intersections[1]));
		p2[0] = 0.5;
		p2[1] = -1.;
		intersections = IntegralCurvesOriginal.getShiftedBoundaryIntersectionPoints(u0, um, v0, vn, p1, p2);
		double[][] lower = {{1,0}, {1,2}};
		Assert.assertArrayEquals(lower, intersections);
		System.out.println("int 1 " + Arrays.toString(intersections[0]));
		System.out.println("int 2 " + Arrays.toString(intersections[1]));
		p1[0] = 1;
		p1[1] = 0.5;
		p2[0] = -1;
		p2[1] = 1.5;
		intersections = IntegralCurvesOriginal.getShiftedBoundaryIntersectionPoints(u0, um, v0, vn, p1, p2);
		System.out.println("int 1 " + Arrays.toString(intersections[0]));
		System.out.println("int 2 " + Arrays.toString(intersections[1]));
		double[][] left = {{0,1}, {2,1}};
		Assert.assertArrayEquals(left, intersections);
		p2[0] = 3;
		intersections = IntegralCurvesOriginal.getShiftedBoundaryIntersectionPoints(u0, um, v0, vn, p1, p2);
		System.out.println("int 1 " + Arrays.toString(intersections[0]));
		System.out.println("int 2 " + Arrays.toString(intersections[1]));
		double[][] right = {{2,1}, {0,1}};
		Assert.assertArrayEquals(right, intersections);
	}
	
	@Test
	public void intersectionPointTest(){
		double u0 = 0.0;
		double um = 2.0;
		double v0 = 0.0;
		double vn = 2.0;
		double[][] lineLeft = {{u0, v0},{u0, vn}};
		double[][] lineRight = {{um, v0},{um, vn}};
		double[][] seg = {{0,0.5},{2,1.5}};
		System.out.println("left intersection " + Arrays.toString(IntegralCurvesOriginal.intersectionPoint(lineLeft, seg)));
		System.out.println("right intersection " + Arrays.toString(IntegralCurvesOriginal.intersectionPoint(lineRight, seg)));
		Assert.assertTrue(true);
	}
	
	@Test
	public void isNotAtBoundaryTest(){
		NURBSSurface ns = createRhino_1Sphere();
		IntegralCurve ic = new IntegralCurve(ns);
		double u0 = -1.570796326794897, um = 1.570796326794897, v0 = 0.0, vn = 6.283185307179586;
		double [] testPoint1 = {1,1};
		double [] testPoint2 = {u0,1};
		double [] testPoint3 = {um,1};
		double [] testPoint4 = {1,v0};
		double [] testPoint5 = {1,vn};
		double [] testPoint6 = {u0,vn};
		Assert.assertTrue(ic.isNotAtBoundary(testPoint1));
		Assert.assertTrue(!ic.isNotAtBoundary(testPoint2));
		Assert.assertTrue(!ic.isNotAtBoundary(testPoint3));
		Assert.assertTrue(!ic.isNotAtBoundary(testPoint4));
		Assert.assertTrue(!ic.isNotAtBoundary(testPoint5));
		Assert.assertTrue(!ic.isNotAtBoundary(testPoint6));
		
		
	}

}
