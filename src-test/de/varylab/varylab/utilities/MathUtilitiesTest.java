package de.varylab.varylab.utilities;

import org.junit.Assert;
import org.junit.Test;

import de.jreality.math.Matrix;
import de.jreality.math.Pn;

public class MathUtilitiesTest {

	@Test
	public void testMakeMappingMatrix() throws Exception {
		double[] s1 = {2,0,2,1};  
		double[] s2 = {1,1,0,0};  
		double[] s3 = {1,0,8,0};  
		double[] s4 = {3,4,0,1};  
		
		double[] t1 = {2,0,1,0};  
		double[] t2 = {0,3,0,2};  
		double[] t3 = {1,0,4,0};  
		double[] t4 = {0,3,0,5};
		
		double[][] S = {s1,s2,s3,s4};
		double[][] T = {t1,t2,t3,t4};
		Matrix R = MathUtility.makeMappingMatrix(S, T);
		Assert.assertArrayEquals(t1, R.multiplyVector(s1), 1E-15);
		Assert.assertArrayEquals(t2, R.multiplyVector(s2), 1E-15);
		Assert.assertArrayEquals(t3, R.multiplyVector(s3), 1E-15);
		Assert.assertArrayEquals(t4, R.multiplyVector(s4), 1E-15);
	}
	
	@Test
	public void testGetDiagonalIntersection() throws Exception {
		double[] N = {0,0,1,0};
		double[] p1 = {0,0,0,1};
		double[] p2 = {1,0,0,1};
		double[] p3 = {1,1,0,1};
		double[] p4 = {0,1,0,1};
		double[] r = MathUtility.getDiagonalIntersection(N, p1, p2, p3, p4);
		Pn.dehomogenize(r, r);
		double[] expected = {0.5, 0.5, 0.0, 1.0};
		Assert.assertArrayEquals(expected, r, 1E-15);
	}
	
}
