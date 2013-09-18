package de.varylab.varylab.plugin.nurbs;

import java.util.Arrays;


import org.junit.Assert;
import org.junit.Test;

import de.varylab.varylab.plugin.nurbs.math.IntegralCurves;

public class InegralCurvesTest {

	@Test
	public void modIntervalTest() {
		double left = 1.1;
		double right = 3.;
		double x = 3.5;
		double y = -0.7;
		double resultX = IntegralCurves.modInterval(left, right, x);
		double resultY = IntegralCurves.modInterval(left, right, y);
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
		resultX[0] = IntegralCurves.getModInterval(left, right, x);
		System.out.println("resx " + IntegralCurves.getModInterval(left, right, x));
		resultY[0] = IntegralCurves.getModInterval(left, right, y);
		System.out.println("resy " + IntegralCurves.getModInterval(left, right, y));
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
		Assert.assertTrue(IntegralCurves.pointsAreInDifferentDomains(u0, um, v0, vn, point1, point2));
		Assert.assertTrue(IntegralCurves.pointsAreInDifferentDomains(u0, um, v0, vn, point1, point3));
		
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
		double[][] intersections = IntegralCurves.getShiftedBoundaryIntersectionPoints(u0, um, v0, vn, p1, p2);
		double[][] upper = {{1,2}, {1,0}};
		Assert.assertArrayEquals(upper, intersections);
		System.out.println("int 1 " + Arrays.toString(intersections[0]));
		System.out.println("int 2 " + Arrays.toString(intersections[1]));
		p2[0] = 0.5;
		p2[1] = -1.;
		intersections = IntegralCurves.getShiftedBoundaryIntersectionPoints(u0, um, v0, vn, p1, p2);
		double[][] lower = {{1,0}, {1,2}};
		Assert.assertArrayEquals(lower, intersections);
		System.out.println("int 1 " + Arrays.toString(intersections[0]));
		System.out.println("int 2 " + Arrays.toString(intersections[1]));
		p1[0] = 1;
		p1[1] = 0.5;
		p2[0] = -1;
		p2[1] = 1.5;
		intersections = IntegralCurves.getShiftedBoundaryIntersectionPoints(u0, um, v0, vn, p1, p2);
		System.out.println("int 1 " + Arrays.toString(intersections[0]));
		System.out.println("int 2 " + Arrays.toString(intersections[1]));
		double[][] left = {{0,1}, {2,1}};
		Assert.assertArrayEquals(left, intersections);
		p2[0] = 3;
		intersections = IntegralCurves.getShiftedBoundaryIntersectionPoints(u0, um, v0, vn, p1, p2);
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
		System.out.println("left intersection " + Arrays.toString(IntegralCurves.intersectionPoint(lineLeft, seg)));
		System.out.println("right intersection " + Arrays.toString(IntegralCurves.intersectionPoint(lineRight, seg)));
		Assert.assertTrue(true);
	}

}
