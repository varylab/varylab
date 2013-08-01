package de.varylab.varylab.plugin.nurbs;

import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import de.varylab.varylab.plugin.nurbs.NURBSCurve.EndPoints;
import de.varylab.varylab.plugin.nurbs.math.PointProjectionCurve;
import de.varylab.varylab.utilities.MathUtility;

public class PointProjectionCurveTest {
	
	@Test
	public void NewtonMethodTest(){
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
		double u = 0;
		boolean newton = true;
		double[] proj = PointProjectionCurve.NewtonMethod(nc, u, point);
		if(Math.abs(proj[0] - 1/Math.sqrt(2)) > 0.001){
			newton = false;
		}
		if(Math.abs(proj[1] - 1/Math.sqrt(2)) > 0.001){
			newton = false;
		}
		Assert.assertTrue(newton);
	}
	
	@Test
	public void isPossibleCurveControlPointsTest(){
		// projection onto a line
		double[] point = {0,1,0,1};
		double[] p3 = MathUtility.get3DPoint(point);
		double[] P0 = {-1,0,0,1};
		double[] P1 = {1,0,0,1};
		double[][] cP = new double[2][];
		cP[0] = P0;
		cP[1] = P1;
		double[] U = {0,0,1,1};
		int p = 1;
		LinkedList<EndPoints> endList = new LinkedList<NURBSCurve.EndPoints>();
		NURBSCurve nc = new NURBSCurve(cP, U, p,endList);
		LinkedList<NURBSCurve> possibleCurves = nc.decomposeIntoBezierCurvesList();
		for (int i = 0; i < 4; i++) {
			LinkedList<NURBSCurve> subdividedCurves = new LinkedList<NURBSCurve>();
			for (NURBSCurve np : possibleCurves) {
				subdividedCurves.addAll(np.subdivideIntoTwoNewCurves());
			}
			possibleCurves = subdividedCurves;
		}
		possibleCurves = PointProjectionCurve.getPossibleCurves(possibleCurves, p3);
		double[] U1 = {0.4375, 0.4375, 0.5, 0.5};
		double[] U2 = {0.5, 0.5, 0.5625, 0.5625};
//		System.out.println("possible curves");
//		for (NURBSCurve n : possibleCurves) {
//			System.out.println(n.toString());
//		}
		Assert.assertArrayEquals(U1, possibleCurves.getFirst().getUKnotVector(), 0.00001);
		Assert.assertArrayEquals(U2, possibleCurves.getLast().getUKnotVector(), 0.00001);
		
	}
	
	@Test
	public void getClosestPointTest(){
		double[][] Pw = {{0.0, 0.0, 0.0, 1.0}, {0.7071067811865475, 0.0, 0.0, 0.7071067811865476}, {1.0, 0.9999999999999999, 0.0, 1.0},
						{0.7071067811865476, 1.414213562373095, 0.0, 0.7071067811865476}, {0.0, 2.0, 0.0, 1.0}};
		double[] point = {1.0, 2.0, 0.5, 1.0};
		double[] U = {-1.570796326794897, -1.570796326794897, -1.570796326794897, 0.0, 0.0, 1.570796326794897, 1.570796326794897, 1.570796326794897};
		NURBSCurve nc = new NURBSCurve(Pw, U, 2);
		double[] closestPoint = {0.7071067811865475, 1.7071067811865475, 0.0, 1.0};
		Assert.assertArrayEquals(closestPoint, PointProjectionCurve.getClosestPoint(nc, point), 0.000001);
	}

}
