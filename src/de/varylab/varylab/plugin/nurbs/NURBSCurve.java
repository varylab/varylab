package de.varylab.varylab.plugin.nurbs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

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
	
	/**
	 * computes the k_th derivatives, where k is at least 2, we set the second derivative zero if
	 * p = 1
	 * @param u
	 * @return all k_th derivatives 
	 */
	
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
	
	/**
	 * insert u r times into the knotvector U
	 * @param nc NURBSCurve
	 * @param u inserted knot
	 * @param r multiplicity
	 * @return NURBSCurve with new knotvector and adepted control points
	 */
	
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
	
	/**
	 * insert u r times into the knotvector U
	 * @param u inserted knot
	 * @param r multiplicity
	 * @return NURBSCurve with new knotvector and adepted control points
	 */
	
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
	 * determine all distinct interior knots, insert each knot until its multiplicity is p
	 * @return NURBSCurve with new knotvector and adepted control points
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
	 * split this curve at each interior knot, such that each curve has a bezier representation
	 * @return Bezier subcurves
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
	
	/**
	 * split this curve at each interior knot, such that each curve has a bezier representation
	 * @return list of Bezier subcurves
	 */
	
	public LinkedList<NURBSCurve> decomposeIntoBezierCurvesList(){
		LinkedList<NURBSCurve> curveList = new LinkedList<NURBSCurve>();
		NURBSCurve[] Bezier = decomposeIntoBezierCurves();
		for (int i = 0; i < Bezier.length; i++) {
			curveList.add(Bezier[i]);
		}
		return curveList;
	}
	
	/**
	 * split this Bezier curve in the middle
	 * @return two Bezier curves
	 */
	
	public LinkedList<NURBSCurve> subdivideIntoTwoNewCurves(){
		LinkedList<NURBSCurve> newCurves = new LinkedList<NURBSCurve>();
		double uInsert = (U[U.length - 1] + U[0]) / 2.0;
		NURBSCurve ncInsert = CurveKnotInsertion(uInsert, 1);
		newCurves = ncInsert.decomposeIntoBezierCurvesList();
		return newCurves;
	}
	
	@Override
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
	
}
