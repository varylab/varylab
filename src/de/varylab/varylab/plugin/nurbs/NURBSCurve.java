package de.varylab.varylab.plugin.nurbs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.BoundaryLines;
import de.varylab.varylab.plugin.nurbs.math.NURBSAlgorithm;

/**
 * 
 * @author seidel
 *
 */

public class NURBSCurve {
	public enum EndPoints{P0, Pm};
	private double[][] controlPoints;
	private double[] U;
	private int p;
	private LinkedList<EndPoints> endPoints = new LinkedList<EndPoints>();

	public NURBSCurve() {
		
	}

	public NURBSCurve(double[][] cP, double[] knotVector, int  deg) {
		controlPoints = cP;
		U = knotVector;
		p = deg;
	}
	public NURBSCurve(double[][] cP, double[] knotVector, int  deg, LinkedList<EndPoints> end) {
		controlPoints = cP;
		U = knotVector;
		p = deg;
		endPoints = end;
	}
	
	
	public LinkedList<EndPoints> getEndPoints() {
		return endPoints;
	}

	public void setEndPoints(LinkedList<EndPoints> endPoints) {
		this.endPoints = endPoints;
	}

	public double[][] getControlPoints() {
		return controlPoints;
	}

	public void setControlPoints(double[][] controlPoints) {
		this.controlPoints = controlPoints;
	}

	public double[] getUKnotVector() {
		return U;
	}

	public void setUKnotvector(double[] u) {
		U = u;
	}

	public int getP() {
		return p;
	}

	public void setP(int p) {
		this.p = p;
	}
	
	public double[] getCurvePoint(double u){
		double[] C = new double[4];
		NURBSAlgorithm.CurvePoint(p, U, controlPoints, u, C);
		return C;
	}
	
	public double[][] getCurveDerivs(double u){
		double[][]CK1 = new double[p+1][4];
		double[][]CK = new double[p+1][3];
		NURBSAlgorithm.CurveDerivatives(p, U, controlPoints, u, 2, CK1);
		double[][]Aders = new double[CK1.length][3]; 
		double[] wders = new double[CK1.length];
		for (int i = 0; i < CK1.length; i++) {
			wders[i] = CK1[i][3];
			Aders[i][0] = CK1[i][0];
			Aders[i][1] = CK1[i][1];
			Aders[i][2] = CK1[i][2];
		}
		NURBSAlgorithm.RatCurveDerivs(Aders, wders, p, CK);
		double[][] CDerivs = new double[3][3];
		CDerivs[0] = CK[0];
		CDerivs[1] = CK[1];
		if(p == 1){
			CDerivs[2][0] = 0.0;
			CDerivs[2][1] = 0.0;
			CDerivs[2][2] = 0.0;
		}else{
			CDerivs[2] = CK[2];
		}
		return CDerivs;
	}
	
	public NURBSCurve CurveKnotInsertion(NURBSCurve nc, double u, int r){
		double[] UP = nc.getUKnotVector();
		double[][]Pw = nc.getControlPoints();
		
		int mult; // = s
		int k;
		int np = Pw.length - 1;
		int p = UP.length - np - 2;
		int nq = np;
		mult = NURBSSurface.getMultiplicity(u, UP);
		k = NURBSAlgorithm.FindSpan(np, p, u, UP);
		nq = np + r;
	
		double[] UQ = new double[nq + p + 2];
		double[][]Qw = new double[nq + 1][4];
		NURBSAlgorithm.CurveKnotIns(np, p, UP, Pw, u, k, mult, r, nq, UQ, Qw);
		NURBSCurve ncReturn = new NURBSCurve(Qw, UQ, p, nc.getEndPoints());
		return ncReturn;
	}
	
	public NURBSCurve CurveKnotInsertion(double u, int r){
		double[] UP = this.getUKnotVector();
		double[][]Pw = this.getControlPoints();
		
		int mult; // = s
		int k;
		int np = Pw.length - 1;
		int p = UP.length - np - 2;
		int nq = np;
		
		mult = NURBSSurface.getMultiplicity(u, UP);
		k = NURBSAlgorithm.FindSpan(np, p, u, UP);
		nq = np + r;
		
		double[] UQ = new double[nq + p + 2];
		double[][]Qw = new double[nq + 1][4];
		NURBSAlgorithm.CurveKnotIns(np, p, UP, Pw, u, k, mult, r, nq, UQ, Qw);
		NURBSCurve ncReturn = new NURBSCurve(Qw, UQ, p, endPoints);
		return ncReturn;
	}
	
	/**
	 * 
	 * @return
	 */
	
	public NURBSCurve decomposeCurve(){
		ArrayList<Double> newUKnots = new ArrayList<Double>();
		ArrayList<Integer> Umult = new ArrayList<Integer>();
		NURBSSurface.getAllNewKnots(U, newUKnots, Umult);
		NURBSCurve ncReturn = new NURBSCurve(controlPoints, U, p);
		for (int i = 0; i < newUKnots.size(); i++) {
			ncReturn = CurveKnotInsertion(ncReturn, newUKnots.get(i), Umult.get(i));
		}
		return ncReturn;
	}
	
	/**
	 * split this curve such that each curve has a bezier representation
	 * @return list of subcurves
	 */
	
	public NURBSCurve[] decomposeIntoBezierCurves(){
		NURBSCurve nsDecompose = decomposeCurve();
		double[] U = nsDecompose.getUKnotVector();
		LinkedList<EndPoints> originalEP = getEndPoints();
		double u0 = U[0];
		double um = U[U.length - 1];
		double[][]Pw = nsDecompose.getControlPoints();
		int p = NURBSSurface.getDegreeFromClampedKnotVector(U);
		double[] differentUknots = NURBSSurface.getAllDifferentKnotsFromFilledKnotVector(U, p);
		NURBSCurve[] BezierCurves = new NURBSCurve[differentUknots.length - 1];
		for (int i = 0; i < BezierCurves.length; i++) {
				double[] UknotVector = new double[2 * p + 2];
				for (int k = 0; k < UknotVector.length; k++) {
					if(k < UknotVector.length / 2){
						UknotVector[k] = differentUknots[i];
					}
					else{
						UknotVector[k] = differentUknots[i + 1];
					}
				}
				LinkedList<EndPoints> eP = new LinkedList<NURBSCurve.EndPoints>();
				if(UknotVector[0] == u0 && originalEP.contains(EndPoints.P0)){
					eP.add(EndPoints.P0);
				}
				if(UknotVector[UknotVector.length - 1] == um && originalEP.contains(EndPoints.Pm)){
					eP.add(EndPoints.Pm);
				}
				double[][]BezierControlPoints = new double[UknotVector.length - p - 1][4];
				for (int iB = 0; iB < BezierControlPoints.length; iB++) {
						BezierControlPoints[iB] = Pw[p * i + iB];
				}
				BezierCurves[i] = new NURBSCurve(BezierControlPoints, UknotVector, p, eP);
		}
		return BezierCurves;
	}
	
	public LinkedList<NURBSCurve> decomposeIntoBezierCurvesList(){
		LinkedList<NURBSCurve> curveList = new LinkedList<NURBSCurve>();
		NURBSCurve[] Bezier = decomposeIntoBezierCurves();
		for (int i = 0; i < Bezier.length; i++) {
			curveList.add(Bezier[i]);
		}
		return curveList;
	}
	
	/**
	 * split this curve at the middle
	 * @return list of subcurves
	 */
	
	public LinkedList<NURBSCurve> subdivideIntoTwoNewCurves(){
		LinkedList<NURBSCurve> newCurves = new LinkedList<NURBSCurve>();
		double uInsert = (U[U.length - 1] + U[0]) / 2.0;
		NURBSCurve ncInsert = CurveKnotInsertion(uInsert, 1);
		newCurves = ncInsert.decomposeIntoBezierCurvesList();
		return newCurves;
	}
	
	
	public String toString() {
		String str = new String();
		str = str + "NURBSCurve" + '\n' + "U knot vector" + '\n';
		for (int i = 0; i < U.length; i++) {
			str = str + U[i] + ", ";
		}
		
		str = str + '\n' + "p = " + p;
		
		str = str + '\n' + "control points";
		for (int i = 0; i < controlPoints.length; i++) {
			str = str + Arrays.toString(controlPoints[i]) + " ";
		}
		if(endPoints.size() == 0){
			str = str + '\n' + "no end points";
		}else{
			str = str + '\n' + "end points: ";
			for (EndPoints ep : endPoints) {
				str = str + " " + ep;
			}
		}
		return str;
	}
	
	public String endPointsToString(){
		String str = new String();
		if(endPoints.size() == 0){
			str = str +  "no end points";
		}else{
			str = str + '\n' + "end points: ";
			for (EndPoints ep : endPoints) {
				str = str + " " + ep;
			}
		}
		return str;
	}
	
	public static void main(String[] args){
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
		boolean orth = true;
		for (int i = 0; i <= 10; i++) {
			
			double[] CK0 = nc.getCurveDerivs(i/10.0)[0];
			System.out.println("point " + Arrays.toString(CK0));
			double[] CK1 = nc.getCurveDerivs(i/10.0)[1];
			System.out.println("deriv " + Arrays.toString(CK1));
			System.out.println();
			if(!(Math.abs(Rn.innerProduct(CK0, CK1)) < 0.001)){
				orth = false;
			}
			
		}
		System.out.println(orth);
//		System.out.println("C(0)" + Arrays.toString(nc.getCurvePoint(0)) + "CK[0] = " + Arrays.toString(nc.getCurveDerivs(0)[0]) + "CK[1] = " + Arrays.toString(nc.getCurveDerivs(0)[1]));
//		System.out.println("C(0)" + Arrays.toString(nc.getCurvePoint(0)));
//		System.out.println("C(0.1)" + Arrays.toString(nc.getCurvePoint(0.1)) + " length = " + Rn.euclideanNorm(nc.getCurvePoint(0.1)));
//		System.out.println("C(0.2)" + Arrays.toString(nc.getCurvePoint(0.2)) + " length = " + Rn.euclideanNorm(nc.getCurvePoint(0.2)));
//		System.out.println("C(0.3)" + Arrays.toString(nc.getCurvePoint(0.3)) + " length = " + Rn.euclideanNorm(nc.getCurvePoint(0.3)));
//		System.out.println("C(1)" + Arrays.toString(nc.getCurvePoint(1)) + " length = " + Rn.euclideanNorm(nc.getCurvePoint(1)));
//		
//		double[] P0 = {1,0,0,1};
//		double[] P1 = {0.7071067811865476,0.7071067811865476,0,0.7071067811865476};
//		double[] P2 = {0,1,0,1};
//		double[][] cP = new double[3][];
//		cP[0] = P0;
//		cP[1] = P1;
//		cP[2] = P2;
//		double[] U = {0,0,0,1,1,1};
//		int p = 2;
//		NURBSCurve nc = new NURBSCurve(cP, U, p);
//		System.out.println("C(0)" + Arrays.toString(nc.getCurvePoint(0)));
//		System.out.println("C(0.1)" + Arrays.toString(nc.getCurvePoint(0.1)) + " length = " + Rn.euclideanNorm(nc.getCurvePoint(0.1)));
//		System.out.println("C(0.2)" + Arrays.toString(nc.getCurvePoint(0.2)) + " length = " + Rn.euclideanNorm(nc.getCurvePoint(0.2)));
//		System.out.println("C(0.3)" + Arrays.toString(nc.getCurvePoint(0.3)) + " length = " + Rn.euclideanNorm(nc.getCurvePoint(0.3)));
//		System.out.println("C(0.4)" + Arrays.toString(nc.getCurvePoint(0.4)) + " length = " + Rn.euclideanNorm(nc.getCurvePoint(0.4)));
//		System.out.println("C(1)" + Arrays.toString(nc.getCurvePoint(1)));
//		double[] P0 = {0,0,0,1};
//		double[] P1 = {4,4,0,4};
//		double[] P2 = {3,2,0,1};
//		double[] P3 = {4,1,0,1};
//		double[] P4 = {5,-1,0,1};
//		double[][] cP = new double[5][];
//		cP[0] = P0;
//		cP[1] = P1;
//		cP[2] = P2;
//		cP[3] = P3;
//		cP[4] = P4;
//		double[] U = {0,0,0,1,2,3,3,3};
//		int p = 2;
//		NURBSCurve nc = new NURBSCurve(cP, U, p);
//		System.out.println("C(1)" + Arrays.toString(nc.getCurvePoint(1)));
	
	}
	

	

}
