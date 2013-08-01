package de.varylab.varylab.plugin.nurbs;

import org.junit.Assert;
import org.junit.Test;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.math.PointProjectionSurfaceOfRevolution;

/**
 * 
 * @author seidel
 *
 */
public class PointProjectionSurfaceOfRevolutionTest {
	
	
	@Test
	public void pointsAreCollinearTest(){
		double[]start = {1.2,3.6,0.7};
		double[]dir ={0.1,3.3,1.2};
		double[][]p = new double[10][];
		p[0] = start;
		double lambda = 0.231;
		for (int i = 1; i < p.length; i++) {
			p[i] = Rn.add(null, start, Rn.times(null, i * lambda, dir));
		}
		boolean col = PointProjectionSurfaceOfRevolution.pointsAreOnACommonLine(p);
		Assert.assertTrue(col);
	}
	
	@Test
	public void getMidpointFromCircle(){
		//translation to trans
		double[] trans = {12,23,34};
		double[] p1 = {1,0,0};
		double[] p2 = {0,1,0};
		double[] p3 = {-1,0,0};
		Rn.add(p1, trans, p1);
		Rn.add(p2, trans, p2);
		Rn.add(p3, trans, p3);
		double alpha = 0.32;
		double[] rotaxis = {1,1,1};
		//rotation around rotaxis
		MatrixBuilder b = MatrixBuilder.euclidean();
		b.rotate(alpha, rotaxis);
		Matrix M = b.getMatrix();
		p1 = M.multiplyVector(p1);
		p2 = M.multiplyVector(p2);
		p3 = M.multiplyVector(p3);
		trans = M.multiplyVector(trans);
		double[] m = PointProjectionSurfaceOfRevolution.getMidpointFromCircle(p1, p2, p3);
		Assert.assertArrayEquals(trans, m, 0.0001);
	}
	
	@Test
	public void getLinearCombinationTest(){
		double[] v = {1, 7};
		double[] w = {3, 634};
		double[] x = {14, 9};
		double[] comb = PointProjectionSurfaceOfRevolution.getLinearCombination(v, w, x);
		Assert.assertArrayEquals(x, Rn.add(null , Rn.times(null, comb[0], v), Rn.times(null, comb[1], w)), 0.0001);
	}
	
	
	@Test
	public void affinePlanesAreParallel(){
		double[] trans = {12,23,34};
		double[] p10 = {1,0,0.0};
		double[] p11 = {0,1,0};
		double[] p12 = {-1,0,0};
		double[] p20 = {1,0,1};
		double[] p21 = {0,1,1};
		double[] p22 = {-1,0,1};
		double alpha = 0.32;
		double[] rotaxis = {1,1,1};
		MatrixBuilder b = MatrixBuilder.euclidean();
		b.rotate(alpha, rotaxis);
		Matrix M = b.getMatrix();
		double[][] plane1 = new double[3][];
		plane1[0] = p10;
		plane1[1] = p11;
		plane1[2] = p12;
		for (int i = 0; i < plane1.length; i++) {
			Rn.add(plane1[i], plane1[i], trans);
			plane1[i] = M.multiplyVector(plane1[i]);
		}
		double[][] plane2 = new double[3][];
		plane2[0] = p20;
		plane2[1] = p21;
		plane2[2] = p22;
		for (int i = 0; i < plane2.length; i++) {
			Rn.add(plane2[i], plane2[i], trans);
			plane2[i] = M.multiplyVector(plane2[i]);
		}
		boolean parallel = PointProjectionSurfaceOfRevolution.affinePlanesAreParallel(plane1, plane2);
		Assert.assertTrue(parallel);
		
		double[] q10 = {1,0,0.1};
		double[] q11 = {0,1,0};
		double[] q12 = {-1,0,0};
		double[] q20 = {1,0,1};
		double[] q21 = {0,1,1};
		double[] q22 = {-1,0,1};
		plane1[0] = q10;
		plane1[1] = q11;
		plane1[2] = q12;
		
		plane2[0] = q20;
		plane2[1] = q21;
		plane2[2] = q22;
		
		for (int i = 0; i < plane1.length; i++) {
			Rn.add(plane1[i], plane1[i], trans);
			plane1[i] = M.multiplyVector(plane1[i]);
		}
		for (int i = 0; i < plane2.length; i++) {
			Rn.add(plane2[i], plane2[i], trans);
			plane2[i] = M.multiplyVector(plane2[i]);
		}
		parallel = PointProjectionSurfaceOfRevolution.affinePlanesAreParallel(plane1, plane2);
		Assert.assertFalse(parallel);
	}
	
	
	@Test
	public void getOrthVectorTest(){
		double[] v = {1,11,0};
		double[] w = {3,2,23};
		double[] p = {123,3,6};
		Assert.assertEquals(Rn.innerProduct(Rn.subtract(null, w, v), PointProjectionSurfaceOfRevolution.getOrthVector(v, w, p)),0.0 , 0.0001);
	}
	
	@Test
	public void projectionTest(){
		
		double[] U = {-1.570796326794897, -1.570796326794897, -1.570796326794897, 0.0, 0.0, 1.570796326794897, 1.570796326794897, 1.570796326794897};
		double[] V = {0.0, 0.0, 0.0, 1.570796326794897, 1.570796326794897, 3.141592653589793, 3.141592653589793, 4.570796326794897, 4.570796326794897, 4.570796326794897};
		int p = 2;
		int q = 2;
		double[][][]cm =
		{{{0.0, 0.0, -1.0, 1.0}, {0.0, 0.0, -0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, -1.0, 1.0}, {0.0, 0.0, -0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, -1.0, 1.0}, {0.0, 0.0, -0.7335083759463675, 0.7335083759463675}, {0.0, 0.0, -0.9519565260428136, 0.9519565260428136}}, 
		{{0.7071067811865475, 0.0, -0.7071067811865476, 0.7071067811865476}, {0.49999999999999994, 0.49999999999999983, -0.5000000000000001, 0.5000000000000001}, {4.329780281177466E-17, 0.7071067811865475, -0.7071067811865476, 0.7071067811865476}, {-0.49999999999999983, 0.49999999999999994, -0.5000000000000001, 0.5000000000000001}, {-0.7071067811865475, 8.659560562354932E-17, -0.7071067811865476, 0.7071067811865476}, {-0.5186687466888079, -0.4549296585513719, -0.518668746688808, 0.518668746688808}, {-0.08776082511602917, -0.6673894300277047, -0.6731349149596617, 0.6731349149596617}}, 
		{{1.0, 0.0, -6.123233995736766E-17, 1.0}, {0.7071067811865476, 0.7071067811865475, -4.329780281177467E-17, 0.7071067811865476}, {6.123233995736766E-17, 1.0, -6.123233995736766E-17, 1.0}, {-0.7071067811865475, 0.7071067811865476, -4.329780281177467E-17, 0.7071067811865476}, {-1.0, 1.224646799147353E-16, -6.123233995736766E-17, 1.0}, {-0.7335083759463675, -0.6433676930491116, -4.4914434237524626E-17, 0.7335083759463675}, {-0.12411254912414184, -0.9438311833296299, -5.829052562728829E-17, 0.9519565260428136}}, 
		{{0.7071067811865476, 0.0, 0.7071067811865475, 0.7071067811865476}, {0.5000000000000001, 0.49999999999999994, 0.49999999999999994, 0.5000000000000001}, {4.329780281177467E-17, 0.7071067811865476, 0.7071067811865475, 0.7071067811865476}, {-0.49999999999999994, 0.5000000000000001, 0.49999999999999994, 0.5000000000000001}, {-0.7071067811865476, 8.659560562354932E-17, 0.7071067811865475, 0.7071067811865476}, {-0.518668746688808, -0.454929658551372, 0.5186687466888079, 0.518668746688808}, {-0.08776082511602919, -0.6673894300277048, 0.6731349149596615, 0.6731349149596617}}, 
		{{0.0, 0.0, 1.0, 1.0}, {0.0, 0.0, 0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, 1.0, 1.0}, {0.0, 0.0, 0.7071067811865476, 0.7071067811865476}, {0.0, 0.0, 1.0, 1.0}, {0.0, 0.0, 0.7335083759463675, 0.7335083759463675}, {0.0, 0.0, 0.9519565260428136, 0.9519565260428136}}};
		// nsUDir is 1partOfSphere.obj
		NURBSSurface nsUDir = new NURBSSurface(U, V, cm, p, q);
		// nsVDir is 1partOfSphere_vDir.obj
		
		// inner projection test
		double[] p1 = {1,1,1,1};
		double[] proj1 = {0.5773502691896256, 0.5773502691896257, 0.5773502691896257, 1.0};
		Assert.assertArrayEquals(proj1, nsUDir.getClosestPoint(p1), 0.00001);
		// first boundary test
		double[] p2 = {0.9615889952176183, -0.10030422864824032, 0.4160591830043355, 1.0};
		double[] proj2 = {0.9177746389766972, 0.0, 0.39710163944661936, 1.0};
		Assert.assertArrayEquals(proj2, nsUDir.getClosestPoint(p2), 0.0000001);
		//other boundary test
		double[] p3 = {0.2524551382855123, -1.2532838423225807, 0.009902628759753437, 1.0};
		double[] proj3 = {-0.1303719101654941, -0.9914313670357733, 0.008185932896029424, 1.0};
		Assert.assertArrayEquals(proj3, nsUDir.getClosestPoint(p3), 0.000001);
		// nsVDir is 1partOfSphere_vDir.obj
		NURBSSurface nsVDir = nsUDir.interchangeUV();
		// inner projection test
		Assert.assertArrayEquals(proj1, nsVDir.getClosestPoint(p1), 0.00001);
		// first boundary test
		double[] p4 = {1.0208399526917762, -0.054572060534274935, 0.33618438327744715, 1.0};
		double[] proj4 = {0.9498203000349641, 0.0, 0.31279609594988655, 1.0};
		Assert.assertArrayEquals(proj4, nsVDir.getClosestPoint(p4), 0.00001);
		//other boundary test
		double[] p5 = {-0.06527914648665367, -0.956753302778938, 0.29962327454656623, 1.0};
		double[] proj5 = {-0.12442190899054859, -0.9461837535640332, 0.298756578261675, 1.0};
		Assert.assertArrayEquals(proj5, nsVDir.getClosestPoint(p5), 0.00001);
		
	}

}
