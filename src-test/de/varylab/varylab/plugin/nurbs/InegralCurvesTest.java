package de.varylab.varylab.plugin.nurbs;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurvesOriginal;
import de.varylab.varylab.plugin.nurbs.math.NURBSCurvatureUtility;




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
		System.out.println("sin(pi / 4) + 0.5 = " + (Math.sin(Math.PI / 4.) + 0.5));
	}
	
//	@Test
//	public void isNotAtBoundaryTest(){
//		NURBSSurface ns = createRhino_1Sphere();
//		IntegralCurve ic = new IntegralCurve(ns, VecFieldCondition.conjugate);
//		double u0 = -1.570796326794897, um = 1.570796326794897, v0 = 0.0, vn = 6.283185307179586;
//		double [] testPoint1 = {1,1};
//		double [] testPoint2 = {u0,1};
//		double [] testPoint3 = {um,1};
//		double [] testPoint4 = {1,v0};
//		double [] testPoint5 = {1,vn};
//		double [] testPoint6 = {u0,vn};
//		Assert.assertTrue(ic.isNotAtBoundary(testPoint1));
//		Assert.assertTrue(!ic.isNotAtBoundary(testPoint2));
//		Assert.assertTrue(!ic.isNotAtBoundary(testPoint3));
//		Assert.assertTrue(!ic.isNotAtBoundary(testPoint4));
//		Assert.assertTrue(!ic.isNotAtBoundary(testPoint5));
//		Assert.assertTrue(!ic.isNotAtBoundary(testPoint6));
//		
//		
//	}
	
	
	
	@Test
	public void symmetricConjugateCurvatureDirectionTest(){
		NURBSSurface openSpindle = createOpenSpindle();
//		double[] start = {3.141592653589793, 0.0};
		double[] point = {3.378534153691202, 1.5074123596222162};
		CurvatureInfo ci =  NURBSCurvatureUtility.curvatureAndDirections(openSpindle, point);
		double[] w1 = ci.getPrincipalDirections()[0];
		double[] w2 = ci.getPrincipalDirections()[1];
		System.out.println("w1 = " + Arrays.toString(w1));
		System.out.println("w2 = " + Arrays.toString(w2));
		
		
	}
	
	

	
	@SuppressWarnings("unused")
	private double[][] surfacePoints(double[] point){
		double[][] points = new double[2][];
		NURBSSurface sphere = createRhino_1Sphere();
//		System.out.println(sphere.toString());
		double[][][] cm = sphere.getControlMesh();
		
		double[] U = sphere.getUKnotVector();
		double[] V = sphere.getVKnotVector();
		int p = sphere.getUDegree();
		int q = sphere.getVDegree();
		int n = U.length - p - 2;
		int m = V.length - q - 2;
//		System.out.println("U.length" + U.length);
//		System.out.println("V.length" + V.length);
//		System.out.println("cm.length = " + cm.length);
//		System.out.println("cm[0].length = " + cm[0].length);
		double[] P20 = cm[2][0];double[] P21 = cm[2][1];double[] P22 = cm[2][2];double[] P30 = cm[3][0];double[] P31 = cm[3][1];double[] P32 = cm[3][2];double[] P40 = cm[4][0];double[] P41 = cm[4][1];double[] P42 = cm[4][2];
		double w20 = P20[3];double w21 = P21[3];double w22 = P22[3];double w30 = P30[3];double w31 = P31[3];double w32 = P32[3];double w40 = P40[3];double w41 = P41[3];double w42 = P42[3];
		double Pi = 2 * U[5];
		double PiSquare = Pi * Pi;
//		double u5 = U[5];
		// for the point p1 = (1,0)
		double u = point[0]; double v = point[1];
		double[] p1 = {u,v};
		double Nu22 = (1 - 2 * u / Pi) * (1 - 2 * u / Pi);
//		double Nu22 = 4 * u * u / PiSquare - 4 * u / Pi + 1;
		double Nu32 = 4 * u / Pi - 8 * u * u / PiSquare;
		double Nu42 = 4 * u * u / PiSquare;
		double sumComp = Nu22 + Nu32 + Nu42;
//		System.out.println("comtuted  Nu22 = " + Nu22 + " Nu32 = " + Nu32 + " Nu42 = " + Nu42 + " sum = " + sumComp);
//		int i = NURBSAlgorithm.FindSpan(n, p, u, U);
//		double[] N = new double[p + 1];
//		NURBSAlgorithm.BasisFuns(i, u, p, U, N);
//		double sumAlgo = N[0] + N[1] + N[2];
//		System.out.println("algorithm Nu22 = " + N[0] + " Nu32 = " + N[1] + " Nu42 = " + N[2] + " sum = " + sumAlgo);
		// v = 0;
		double Nv02 = (1 - 2 * v / Pi) * (1 - 2 * v / Pi);
//		double Nv02 = 4 * v * v / PiSquare - 4 * v / Pi + 1;
		double Nv12 = 4 * v / Pi - 8 * v * v / PiSquare;
		double Nv22 = 4 * v * v / PiSquare;
//		System.out.println("comtuted  Nv02 = " + Nv02 + " Nv12 = " + Nv12 + " Nv22 = " + Nv22 + " sum = " + sumComp);
//		int j = NURBSAlgorithm.FindSpan(n, p, v, V);
//		double[] Nq = new double[q + 1];
//		NURBSAlgorithm.BasisFuns(j, v, q, V, Nq);
//		double sumAlgo = N[0] + N[1] + N[2];
//		System.out.println("algorithm Nv02 = " + Nq[0] + " Nv12 = " + Nq[1] + " Nv22 = " + Nq[2] + " sum = " + sumAlgo);
		
		//the derivatives of the basis functions

		double nu22 = 8 * u / PiSquare - 4 / Pi;
		double nu32 = 4 / Pi - 16 * u / PiSquare;
		double nu42 = 8 * u / PiSquare;

		double nv02 = 8 * v / PiSquare - 4 / Pi;
		double nv12 = 4 / Pi - 16 * v / PiSquare;
		double nv22 = 8 * v / PiSquare;
		
		// denominator
		double Nw2 = Nv02 * w20 + Nv12 * w21 + Nv22 * w22;
		double Nw3 = Nv02 * w30 + Nv12 * w31 + Nv22 * w32;
		double Nw4 = Nv02 * w40 + Nv12 * w41 + Nv22 * w42;
		
		double nw2 = nv02 * w20 + nv12 * w21 + nv22 * w22;
		double nw3 = nv02 * w30 + nv12 * w31 + nv22 * w32;
		double nw4 = nv02 * w40 + nv12 * w41 + nv22 * w42;
		
		//derivative in v direction
//		double nw2 = nv02 * w20 + nv12 * w21;
//		double nw3 = nv02 * w30 + nv12 * w31;
//		double nw4 = nvPisquare02 * w40 + nv12 * w41;
		double Sw = Nu22 * Nw2 + Nu32 * Nw3 + Nu42 * Nw4;
		double Suw = nu22 * Nw2 + nu32 * Nw3 + nu42 * Nw4;
		double Svw = Nu22 * nw2 + Nu32 * nw3 + Nu42 * nw4;
		//derivative in u direction
//		double denominator_u = nu22 * Nw2 + nu32 * Nw3 + nu42 * Nw4;;
		//derivative in v direction
//		double denominator_v = Nu22 * nw2 + Nu32 * nw3 + Nu42 * nw4;;
//		double denomSquare = denominator * denominator;
		
		// x component
//		double x20 = P20[0] * w20; double x21 = P21[0] * w21; double x22 = P22[0] * w22; double x30 = P30[0] * w30; double x31 = P31[0] * w31; double x32 = P32[0] * w32; double x40 = P40[0] * w40; double x41 = P41[0] * w41; double x42 = P42[0] * w42;
		double x20 = P20[0]; double x21 = P21[0]; double x22 = P22[0]; double x30 = P30[0]; double x31 = P31[0]; double x32 = P32[0]; double x40 = P40[0]; double x41 = P41[0]; double x42 = P42[0];
		double Nx2 = Nv02 * x20 + Nv12 * x21 + Nv22 * x22;
		double Nx3 = Nv02 * x30 + Nv12 * x31 + Nv22 * x32;
		double Nx4 = Nv02 * x40 + Nv12 * x41 + Nv22 * x42;
		
		double nx2 = nv02 * x20 + nv12 * x21 + nv22 * x22;
		double nx3 = nv02 * x30 + nv12 * x31 + nv22 * x32;
		double nx4 = nv02 * x40 + nv12 * x41 + nv22 * x42;
		//derivative in v direction
//		double nx2 = nv02 * x20 + nv12 * x21;
//		double nx3 = nv02 * x30 + nv12 * x31;
//		double nx4 = nv02 * x40 + nv12 * x41;
		// nominator
		double Sx = Nu22 * Nx2 + Nu32 * Nx3 + Nu42 * Nx4;
		double Sux = nu22 * Nx2 + nu32 * Nx3 + nu42 * Nx4;
		
		Sux = (Sux * Sw - Sx * Suw) / (Sw * Sw);
		double Svx = Nu22 * nx2 + Nu32 * nx3 + Nu42 * nx4;
		Svx = (Svx * Sw - Sx * Svw) / (Sw * Sw);
		//derivative in u direction
//		double nominator_u = nu22 * Nx2 + nu32 * Nx3 + nu42 * Nx4;;
		//derivative in v direction
//		double nominator_v = Nu22 * nx2 + Nu32 * nx3 + Nu42 * nx4;
		
		//complete derivatives	
		
		double[] surfacePoint = sphere.getSurfacePoint(p1[0], p1[1]);
		
		points[0] = surfacePoint;
//		System.out.println("surfacePoint = " + Arrays.toString(surfacePoint) + " norm = " + Rn.euclideanNorm(surfacePoint));
//		double Sx = nominator / denominator;

//		System.out.println("Sx = " + Sx);
//		double Fxu = (nominator_u * denominator - nominator * denominator_u) / denomSquare;
//		double Fxv = (nominator_v * denominator - nominator * denominator_v) / denomSquare;
		
		
		// y component
//		double y20 = P20[1] * w20; double y21 = P21[1] * w21; double y22 = P22[1] * w22; double y30 = P30[1] * w30; double y31 = P31[1] * w31; double y32 = P32[1] * w32; double y40 = P40[1] * w40; double y41 = P41[1] * w41; double y42 = P42[1] * w42;
		double y20 = P20[1]; double y21 = P21[1]; double y22 = P22[1]; double y30 = P30[1]; double y31 = P31[1]; double y32 = P32[1]; double y40 = P40[1]; double y41 = P41[1]; double y42 = P42[1];
		double Ny2 = Nv02 * y20 + Nv12 * y21 + Nv22 * y22;
		double Ny3 = Nv02 * y30 + Nv12 * y31 + Nv22 * y32;
		double Ny4 = Nv02 * y40 + Nv12 * y41 + Nv22 * y42;
		
		double ny2 = nv02 * y20 + nv12 * y21 + nv22 * y22;
		double ny3 = nv02 * y30 + nv12 * y31 + nv22 * y32;
		double ny4 = nv02 * y40 + nv12 * y41 + nv22 * y42;
		//derivative in v direction
//		double ny2 = nv02 * y20 + nv12 * y21;
		// nominator
		double Sy = Nu22 * Ny2 + Nu32 * Ny3 + Nu42 * Ny4;
		double Suy = nu22 * Ny2 + nu32 * Ny3 + nu42 * Ny4;
		Suy = (Suy * Sw - Sy * Suw) / (Sw * Sw);
		double Svy = Nu22 * ny2 + Nu32 * ny3 + Nu42 * ny4;
		Svy = (Svy * Sw - Sy * Svw) / (Sw * Sw);
		//derivative in u direction
//		nominator_u = Ny2 * (nu22 + nu32 + nu42);
		//derivative in v direction
//		nominator_v = ny2 * (Nu22 + Nu32 + Nu42);
				
		//complete derivatives
//		double Sy = nominator / denominator;
//		System.out.println("Sy = " + Sy);
//		double Fyu = (nominator_u * denominator - nominator * denominator_u) / denomSquare;
//		double Fyv = (nominator_v * denominator - nominator * denominator_v) / denomSquare;
		
		
		// z component
//		double z20 = P20[2] * w20; double z21 = P21[2] * w21; double z22 = P22[2] * w22; double z30 = P30[2] * w30; double z31 = P31[2] * w31; double z32 = P32[2] * w32; double z40 = P40[2] * w40; double z41 = P41[2] * w41; double z42 = P42[2] * w42;
		double z20 = P20[2]; double z21 = P21[2]; double z22 = P22[2]; double z30 = P30[2]; double z31 = P31[2]; double z32 = P32[2]; double z40 = P40[2]; double z41 = P41[2]; double z42 = P42[2];
//		double Nz2 = Nv02 * z20 + Nv12 * z21 + Nv22 * z22;
		double Nz2 = Nv02 * z20 + Nv12 * z21 + Nv22 * z22;
		double Nz3 = Nv02 * z30 + Nv12 * z31 + Nv22 * z32;
		double Nz4 = Nv02 * z40 + Nv12 * z41 + Nv22 * z42;
		
		double nz2 = nv02 * z20 + nv12 * z21 + nv22 * z22;
		double nz3 = nv02 * z30 + nv12 * z31 + nv22 * z32;
		double nz4 = nv02 * z40 + nv12 * z41 + nv22 * z42;
		//derivative in v direction
//		double nz2 = nv02 * z20 + nv12 * z21;
		// nominator
		double Sz = Nu22 * Nz2 + Nu32 * Nz3 + Nu42 * Nz4;
		double Suz = nu22 * Nz2 + nu32 * Nz3 + nu42 * Nz4;
		Suz = (Suz * Sw - Sz * Suw) / (Sw * Sw);
		double Svz = Nu22 * nz2 + Nu32 * nz3 + Nu42 * nz4;
		Svz = (Svz * Sw - Sz * Svw) / (Sw * Sw);
		//derivative in u direction
//		nominator_u = Nz2 * (nu22 + nu32 + nu42);
		//derivative in v direction
//		nominator_v = nz2 * (Nu22 + Nu32 + Nu42);
				
		//complete derivatives
//		double Sz = nominator / denominator;
//		System.out.println("Sz = " + Sz);
//		System.out.println("Sw = " + denominator);
		CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(sphere, point);
		double[] Su = ci.getSu();
		System.out.println("algorithm Su = " + Arrays.toString(Su) + " norm = " + Rn.euclideanNorm(Su));
		points[0] = Su;
		double[] resU = {Sux, Suy, Suz};
//		double[] res = {Sux / Suw, Suy / Suw, Suz / Suw};
		System.out.println("computed  Su = " + Arrays.toString(resU) + " norm = " + Rn.euclideanNorm(resU));
		
		double[] Sv = ci.getSv();
		System.out.println("algorithm Sv = " + Arrays.toString(Sv) + " norm = " + Rn.euclideanNorm(Sv));
		double[] resV = {Svx, Svy, Svz};
//		double[] res = {Sux / Suw, Suy / Suw, Suz / Suw};
		System.out.println("computed  Sv = " + Arrays.toString(resV) + " norm = " + Rn.euclideanNorm(resV));
		
		points[1] = resU;
//		System.out.println("surfacePoinR = " + Arrays.toString(res) + " norm = " + Rn.euclideanNorm(res));
//		double Fzu = (nominator_u * denominator - nominator * denominator_u) / denomSquare;
//		double Fzv = (nominator_v * denominator - nominator * denominator_v) / denomSquare;
		return points;
		
	}
	
	@Test
	public void partialDerivativeTest(){
//		double[] p = new double[2];
//		double step = 0.1;
//		for (int i = 0; i <= 10; i++) {
//			for (int j = 0; j <= 10; j++) {
//				p[0] = i * step;
//				p[1] = j * step;
//				System.out.println("p = " + Arrays.toString(p));
//				surfacePoints(p);
////				System.out.println("algorithm point = " + Arrays.toString(res[0]) + " norm = " + euclidianNorm(res[0]));
////				System.out.println(" computed point = " + Arrays.toString(res[1]) + " norm = " + euclidianNorm(res[1]));
//			}
//		}
		
//		double[] p = new double[2];
//		double step = 0.1;
//		for (int i = 0; i <= 10; i++) {
//		
//				p[0] = 0.;
//				p[1] = i * step;
//				System.out.println("p = " + Arrays.toString(p));
//				double [][]res = surfacePoints(p);
////				System.out.println("algorithm point = " + Arrays.toString(res[0]) + " norm = " + euclidianNorm(res[0]));
////				System.out.println(" computed point = " + Arrays.toString(res[1]) + " norm = " + euclidianNorm(res[1]));
//		}
		
		NURBSSurface sphere = createRhino_1Sphere();
		System.out.println(sphere.toString());
//		double[] V = sphere.getVKnotVector();
//		int n = 10;
//		double step = (V[V.length - 1] - V[0]) / (double)n;
//		double[] firstPoint = {0.0, V[0]};
//		double[] secondPoint = {0.0, V[0]};
//		for (int i = 1; i < n; i++) {
//			secondPoint[1] = V[0] + i * step;
//			System.out.println("dist = " + Rn.euclideanDistance(sphere.getSurfacePoint(firstPoint[0], firstPoint[1]), sphere.getSurfacePoint(secondPoint[0], secondPoint[1])));
//			firstPoint[1] = secondPoint[1];
//		}
		
		
		
//		double[] Fu = {Fxu, Fyu, Fzu};
//		System.out.println("Fu = " + Arrays.toString(Fu));
//		double[] Su = ci.getSu();
//		System.out.println("Su = " + Arrays.toString(Su));
//		double[] Fv = {Fxv, Fyv, Fzv};
//		System.out.println("Fv = " + Arrays.toString(Fv));
//		double[] Sv = ci.getSv();
//		System.out.println("Sv = " + Arrays.toString(Sv));
		
		

		
		
	}

}
