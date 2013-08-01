package de.varylab.varylab.plugin.nurbs;

import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import de.jreality.math.Rn;
import de.varylab.varylab.utilities.MathUtility;

/**
 * 
 * @author seidel
 *
 */

public class NURBSCurveTest {
	
	@Test
	public void NURBSCurvePoint(){
		//circle
		double[] P0 = {1,0,0,1};
		double[] P1 = {1,1,0,1};
		double[] P2 = {0,2,0,2};
		double[][] cP = new double[3][];
		cP[0] = P0;
		cP[1] = P1;
		cP[2] = P2;
		double[] U = {0,0,0,1,1,1};
		int p = 2;
		NURBSCurve nc = new NURBSCurve(cP, U, p);
		boolean isCircle = true;
		for (int i = 0; i <= 10; i++) {
			double[] curvePoint = nc.getCurvePoint(i / 10.);
			curvePoint = MathUtility.get3DPoint(curvePoint);
			if(Math.abs(1 - Rn.euclideanNorm(curvePoint)) > 0.001){
				isCircle = false;
			}
		}
		Assert.assertTrue(isCircle);
	}
	
	@Test
	public void NURBSCurveDerivatives(){
		//circle
		double[] P0 = {1,0,0,1};
		double[] P1 = {1,1,0,1};
		double[] P2 = {0,2,0,2};
		double[][] cP = new double[3][];
		cP[0] = P0;
		cP[1] = P1;
		cP[2] = P2;
		double[] U = {0,0,0,1,1,1};
		int p = 2;
		NURBSCurve nc = new NURBSCurve(cP, U, p);
		boolean isOrth = true;
		for (int i = 0; i <= 10; i++) {
			double[][]Ck = nc.getCurveDerivs(i / 10.);
			double[] C = Ck[0];
			double[] Cu = Ck[1];
			if(Math.abs(Rn.innerProduct(C, Cu)) > 0.001){
				isOrth = false;
			}
		}
		Assert.assertTrue(isOrth);
	}
	
	@Test
	public void CurveKnotInsertion(){
		//circle
		double[] P0 = {1,0,0,1};
		double[] P1 = {1,1,0,1};
		double[] P2 = {0,2,0,2};
		double[][] cP = new double[3][];
		cP[0] = P0;
		cP[1] = P1;
		cP[2] = P2;
		double[] U = {0,0,0,1,1,1};
		int p = 2;
		NURBSCurve nc1 = new NURBSCurve(cP, U, p);
		double[][] points1 = new double[11][];
		for (int i = 0; i <= 10; i++) {
			points1[i] = nc1.getCurvePoint(i / 10.);
		}
		double u = 1/2.0;
		int r = 1;
		NURBSCurve nc2 = nc1.CurveKnotInsertion(u, r);
		for (int i = 0; i <= 10; i++) {
			Assert.assertArrayEquals(points1[i], nc2.getCurvePoint(i / 10.), 0.0001);
		}
	}
	
	@Test
	public void decomposeCurve(){
		double[] P0 = {0,0,0,1};
		double[] P1 = {4,4,0,4};
		double[] P2 = {3,2,0,1};
		double[] P3 = {4,1,0,1};
		double[] P4 = {5,-1,0,1};
		double[][] cP = new double[5][];
		cP[0] = P0;
		cP[1] = P1;
		cP[2] = P2;
		cP[3] = P3;
		cP[4] = P4;
		double[] U = {0,0,0,1,2,3,3,3};
		int p = 2;
		NURBSCurve nc = new NURBSCurve(cP, U, p);
		double[][] points1 = new double[11][];
		for (int i = 0; i <= 10; i++) {
			points1[i] = nc.getCurvePoint(3 * i / 10.);
		}
		NURBSCurve ncDecomp = nc.decomposeCurve();
		for (int i = 0; i <= 10; i++) {
			Assert.assertArrayEquals(points1[i], ncDecomp.getCurvePoint(3 * i / 10.), 0.0001);
		}
		
	}
	
	@Test
	public void decomposeIntoBezierCurvesList(){
		double[] P0 = {0,0,0,1};
		double[] P1 = {4,4,0,4};
		double[] P2 = {3,2,0,1};
		double[] P3 = {4,1,0,1};
		double[] P4 = {5,-1,0,1};
		double[][] cP = new double[5][];
		cP[0] = P0;
		cP[1] = P1;
		cP[2] = P2;
		cP[3] = P3;
		cP[4] = P4;
		double[] U = {0,0,0,1,2,3,3,3};
		int p = 2;
		NURBSCurve nc = new NURBSCurve(cP, U, p);
		LinkedList<NURBSCurve> curveList = nc.decomposeIntoBezierCurvesList();
		Assert.assertArrayEquals(nc.getCurvePoint(0.5), curveList.get(0).getCurvePoint(0.5), 0.00001);
		Assert.assertArrayEquals(nc.getCurvePoint(1.5), curveList.get(1).getCurvePoint(1.5), 0.00001);
		Assert.assertArrayEquals(nc.getCurvePoint(2.5), curveList.get(2).getCurvePoint(2.5), 0.00001);
	}
	
	@Test
	public void subdivideIntoTwoNewCurves(){
		double[] P0 = {1,0,0,1};
		double[] P1 = {1,1,0,1};
		double[] P2 = {0,2,0,2};
		double[][] cP = new double[3][];
		cP[0] = P0;
		cP[1] = P1;
		cP[2] = P2;
		double[] U = {0,0,0,1,1,1};
		int p = 2;
		NURBSCurve nc = new NURBSCurve(cP, U, p);
		LinkedList<NURBSCurve> curveList = nc.subdivideIntoTwoNewCurves();
		Assert.assertArrayEquals(nc.getCurvePoint(0.25), curveList.get(0).getCurvePoint(0.25), 0.00001);
		Assert.assertArrayEquals(nc.getCurvePoint(0.75), curveList.get(1).getCurvePoint(0.75), 0.00001);
	}
	
	@Test
	public void getClosestPointTest(){
		double[] point = {2,2,0,1};
		double[] P0 = {1,0,0,1};
		double[] P1 = {1,1,0,1};
		double[] P2 = {0,2,0,2};
		double[][] cP = new double[3][];
		cP[0] = P0;
		cP[1] = P1;
		cP[2] = P2;
		double[] U = {0,0,0,1,1,1};
		int p = 2;
		NURBSCurve nc = new NURBSCurve(cP, U, p);
		nc.getClosestPoint(point);
		double[] result = {0.7071067811865475, 0.7071067811865475, 0.0, 1.0};
		Assert.assertArrayEquals(result, nc.getClosestPoint(point), 0.00001);
	}

}
