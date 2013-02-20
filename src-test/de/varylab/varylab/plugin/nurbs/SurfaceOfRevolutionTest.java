package de.varylab.varylab.plugin.nurbs;

import java.util.Arrays;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSCurve.EndPoints;
import de.varylab.varylab.plugin.nurbs.math.PointProjectionSurfaceOfRevolution;
import de.varylab.varylab.utilities.MathUtility;


/**
 * 
 * @author seidel
 *
 */
public class SurfaceOfRevolutionTest {
	
	
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
		boolean col = PointProjectionSurfaceOfRevolution.pointsAreCollinear(p);
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
		double[] proj = PointProjectionSurfaceOfRevolution.NewtonMethod(nc, u, point);
//		System.out.println("proj = " + Arrays.toString(proj));
//		System.out.println(1/Math.sqrt(2));
		if(Math.abs(proj[0] - 1/Math.sqrt(2)) > 0.001){
			newton = false;
		}
		if(Math.abs(proj[1] - 1/Math.sqrt(2)) > 0.001){
			newton = false;
		}
		Assert.assertTrue(newton);
	}
	
//	@Test
//	public void isPossibleCurveControlPointsTest(){
//		System.out.println("isPossibleCurveControlPointsTest()");
//		double[] point = {0,1,0,1};
//		double[] p3 = MathUtility.get3DPoint(point);
//		double[] P0 = {-1,0,0,1};
//		double[] P1 = {1,0,0,1};
//		double[][] cP = new double[2][];
//		cP[0] = P0;
//		cP[1] = P1;
//		double[] U = {0,0,1,1};
//		int p = 1;
//		LinkedList<EndPoints> endList = new LinkedList<NURBSCurve.EndPoints>();
//		NURBSCurve nc = new NURBSCurve(cP, U, p,endList);
//		LinkedList<NURBSCurve> possibleCurves = nc.decomposeIntoBezierCurvesList();
//		System.out.println("original curve");
//		for (NURBSCurve n : possibleCurves) {
//			System.out.println(n.toString());
//		}
//		for (int i = 0; i < 25; i++) {
//			LinkedList<NURBSCurve> subdividedCurves = new LinkedList<NURBSCurve>();
//			possibleCurves = PointProjectionSurfaceOfRevolution.getPossibleCurves(possibleCurves, p3);
//			System.out.println("possiblePatches.size(): " + possibleCurves.size());
//			for (NURBSCurve np : possibleCurves) {
//				subdividedCurves.addAll(np.subdivideIntoTwoNewCurves());
//			}
//			possibleCurves = subdividedCurves;
//		}
//		int i = 0;
//		for (NURBSCurve n : possibleCurves) {
//			i++;
//			System.out.println(i + ". curve");
//			System.out.println(n.toString());
//			
//		}
//	}
	
	@Test
	public void isPossibleCurveControlPointsTest(){
		System.out.println("isPossibleCurveControlPointsTest()");
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
//		System.out.println("original curve");
//		for (NURBSCurve n : possibleCurves) {
//			System.out.println(n.toString());
//		}
		for (int i = 0; i < 10; i++) {
			LinkedList<NURBSCurve> subdividedCurves = new LinkedList<NURBSCurve>();
			
	//		System.out.println("possiblePatches.size(): " + possibleCurves.size());
			for (NURBSCurve np : possibleCurves) {
				subdividedCurves.addAll(np.subdivideIntoTwoNewCurves());
			}
			possibleCurves = subdividedCurves;
		}
		possibleCurves = PointProjectionSurfaceOfRevolution.getPossibleCurves(possibleCurves, p3);
		int i = 0;
		for (NURBSCurve n : possibleCurves) {
			i++;
//			System.out.println(i + ". curve");
//			System.out.println(n.toString());
			
		}
	}
	
//	@Test 
//	/**
//	 * TODO ist noch kein test
//	 */
//	public void pointProjectionTest(){
//		double[] point = {1., -0.5, 1., 1.};
//		double[] p = point.clone();
//		double[] U = {0.0, 0.0, 0.9972904907155478, 0.9972904907155478};
//		double[] V = {4.699620718690387, 4.699620718690387, 4.699620718690387, 6.266160958253849, 6.266160958253849, 6.266160958253849};
//		double[][][] Pw = {{{0.0, -1.0, 1.0, 1.0}, {0.7071067811865476, -0.7071067811865476, 0.7071067811865476, 0.7071067811865476}, {1.0, -1.0, 0.0, 1.0}}, 
//				{{0.0, 0.0, 1.0, 1.0}, {0.7071067811865476, 0.0, 0.7071067811865476, 0.7071067811865476}, {1.0, 0.0, 0.0, 1.0}}}; 
////		for (int i = 0; i < Pw.length; i++) {
////			for (int j = 0; j < Pw[0].length; j++) {
////				System.out.println("Pw["+i+"]["+j+"] = " + Arrays.toString(Pw[i][j]));
////			}
////		}
//		NURBSSurface ns = new NURBSSurface(U, V, Pw, 1, 2);
//		double[][] axis = PointProjectionSurfaceOfRevolution.getRotationAxis(ns);
//		System.out.println("original axis");
//		System.out.println("axis[0] = " + Arrays.toString(axis[0]));
//		System.out.println("axis[1] = " + Arrays.toString(axis[1]));
//		System.out.println(ns.toString());
//		double[][][]P = ns.getControlMesh().clone();
//		MatrixBuilder b1 = MatrixBuilder.euclidean();
//		//translation
//		b1.translate(Rn.times(null, -1, axis[0]));
//		Matrix M1 = b1.getMatrix();
//		double[] axis1 = M1.multiplyVector(axis[1]).clone();
////		p = M1.multiplyVector(p);
//		System.out.println("translated axis[1] = " + Arrays.toString(axis[1]));
//		System.out.println("translated axis1 = " + Arrays.toString(axis1));
//		double[] e1 = {0,1,0};
//		MatrixBuilder b2 = MatrixBuilder.euclidean();
//		b2.rotateFromTo(axis1, e1);
//		Matrix M2 = b2.getMatrix();
//		axis1 =  M2.multiplyVector(axis1);
////		p = M2.multiplyVector(p);
//		System.out.println("rotated axis1 = " + Arrays.toString(axis1));
//		double[] P00 = Pn.dehomogenize(null, P[0][0].clone());
//		System.out.println("P00 " + Arrays.toString(P00));
//		P00 = M1.multiplyVector(P00);
//		P00 = M2.multiplyVector(P00);
//		System.out.println(" first P00 " + Arrays.toString(P00));
//		P00[1] = 0;
//		P00[3] = 0;
//		double[] P00proj = {1,0,0,1};
//		MatrixBuilder b3 = MatrixBuilder.euclidean();
//		b3.rotateFromTo(P00, P00proj);
////		b3.rotateY(alpha);
//		Matrix M3 = b3.getMatrix();
//		P00 = M3.multiplyVector(P00);
//		System.out.println("second P00 " + Arrays.toString(P00));
////		p = M3.multiplyVector(p);
//		Matrix MCurve = M3;
//		MCurve.multiplyOnRight(M2);
//		MCurve.multiplyOnRight(M1);
////		System.out.println("new control mesh without");
//		double[][] controlPoints = new double[P.length][];
//		System.out.println("new control points");
//		for (int i = 0; i < P.length; i++) {
//			controlPoints[i] = MCurve.multiplyVector(P[i][0]);
//			System.out.println("cp["+i+"] "+Arrays.toString(controlPoints[i]));
//		}
////		for (int i = 0; i < P.length; i++) {
////			for (int j = 0; j < P[0].length; j++) {
////				P[i][j] = MCurve.multiplyVector(P[i][j]);
////				
////			}
////		}
//		NURBSCurve nc = new NURBSCurve(controlPoints, ns.getUKnotVector(), ns.getUDegree());
//		System.out.println("NURBS CURVE");
//		System.out.println(nc.toString());
//		System.out.println("p davor " + Arrays.toString(p));
//		p = MCurve.multiplyVector(p);
//		System.out.println("p danach " + Arrays.toString(p));
//		MatrixBuilder b4 = MatrixBuilder.euclidean();
////		double[] pTrans = new double[4];
//		double[] pTrans = {p[0],0, p[2],1};
////		pTrans[0] = p[0];
////		pTrans[1] = 0;
////		pTrans[2] = p[2];
////		pTrans[3] = 1;
////		double alpha =  Rn.euclideanAngle(MathUtility.getFirstComponents(p), MathUtility.getFirstComponents(pProj));
////		System.out.println("alpha " + alpha);
////		b4.rotateY(alpha);
//		b4.rotateFromTo(pTrans, P00proj);
//		Matrix M4 = b4.getMatrix();
//		p = M4.multiplyVector(p);
//		System.out.println("p nach drehung " + Arrays.toString(p));
//		if(Rn.innerProduct(controlPoints[0], p) < 0){
//			System.out.println("point flipped");
//			p[1] = -p[1];
//		}
//		
//		System.out.println("new point " + Arrays.toString(p));
//		System.out.println("new control mesh with");
//		for (int i = 0; i < P.length; i++) {
//			for (int j = 0; j < P[0].length; j++) {
//				Pn.dehomogenize(P[i][j], P[i][j]);
//				System.out.println("P["+i+"]["+j+"] = " + Arrays.toString(P[i][j]));
//			}
//		}
//		double[] closestPoint = new double[4];
//		closestPoint = PointProjectionSurfaceOfRevolution.getClosestPointOnCurve(nc, p);
//		System.out.println("CLOSEST POINT1: " + Arrays.toString(closestPoint));
//		Matrix MPoint = M4;
//		MPoint.multiplyOnRight(MCurve);
//		MPoint.invert();
//		closestPoint = MPoint.multiplyVector(closestPoint);
//		System.out.println("CLOSEST POINT2: " + Arrays.toString(closestPoint));
//	}
	
//	@Test
//	public void getClosestSubcurve(){
//		double[] point = {0,2,0,1};
//		double[] p3 = MathUtility.get3DPoint(point);
//		double[] P0 = {1,0,0,1};
//		double[] P1 = {0,1,0,1};
//		double[] P2 = {-1,0,0,1};
//		double[][] cP = new double[3][];
//		cP[0] = P0;
//		cP[1] = P1;
//		cP[2] = P2;
//		double[] U = {0,0,0,1,1,1};
//		int p = 2;
//		System.out.println("getClosestSubcurve");
//		NURBSCurve nc = new NURBSCurve(cP, U, p);
//		LinkedList<NURBSCurve> curveList = nc.decomposeIntoBezierCurvesList();
//		for (int i = 0; i < 5; i++) {
//			LinkedList<NURBSCurve> possibleCurveList = new LinkedList<NURBSCurve>();
//			for (NURBSCurve n : curveList) {
//				possibleCurveList.addAll(n.subdivideIntoTwoNewCurves());
//			}
//			curveList = possibleCurveList;
//		}
//		for (NURBSCurve n : curveList) {
//			System.out.println(n.toString());
//		}
//		double[][] cp = PointProjectionSurfaceOfRevolution.getClosestSubcurve(curveList, p3);
//		System.out.println("closest control points");
//		for (int i = 0; i < cp.length; i++) {
//			System.out.println("cp["+i+"]: " + Arrays.toString(cp[i]));
//		}
//	}
	
	
	
	

}
