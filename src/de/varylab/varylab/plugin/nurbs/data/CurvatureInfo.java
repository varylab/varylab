package de.varylab.varylab.plugin.nurbs.data;

import java.util.Arrays;

public class CurvatureInfo {
	
	private double [] Su;
	private double [] Sv;
	private double [] Suu;
	private double [] Suv;
	private double [] Svv;
	private double [] normal;

	private double [][] curvatureDirections;
	private double [][] pricipalDirections;
	private double minCurvature;
	private double maxCurvature;
	private double [][] Weingartenoperator;
	private double [][] RiemannianMetric;
	private double[][] secondFundamental;
	private double GaussCurvature;
	private double MainCurvature;
	
	public CurvatureInfo(double[][]cM,double[][]cD, double l, double m, double[][] W, double[][] R){
		curvatureDirections = cM;
		pricipalDirections = cD;
		minCurvature = l;
		maxCurvature = m;
		Weingartenoperator = W;
		RiemannianMetric = R;
		
	}
	

	public CurvatureInfo(){
		curvatureDirections = null;
		pricipalDirections = null;
		minCurvature = 0;
		maxCurvature = 0;
		Weingartenoperator = null;
		RiemannianMetric = null;
	}
	
	public double[][] getSecondFundamental() {
		return secondFundamental;
	}


	public void setSecondFundamental(double[][] secondFundamental) {
		this.secondFundamental = secondFundamental;
	}

	
	public double[][] getRiemannianMetric() {
		return RiemannianMetric;
	}

	public void setRiemannianMetric(double[][] riemannianMetric) {
		RiemannianMetric = riemannianMetric;
	}
	
	public double[] getSuu() {
		return Suu;
	}
	

	public void setSuu(double[] suu) {
		Suu = suu;
	}

	public double[] getSuv() {
		return Suv;
	}

	public void setSuv(double[] suv) {
		Suv = suv;
	}

	public double[] getSvv() {
		return Svv;
	}

	public void setSvv(double[] svv) {
		Svv = svv;
	}

	public double getMainCurvature() {
		return MainCurvature;
	}

	public void setMainCurvature(double mainCurvature) {
		MainCurvature = mainCurvature;
	}

	public double[] getSu() {
		return Su;
	}

	public void setSu(double[] su) {
		Su = su;
	}

	public double[] getSv() {
		return Sv;
	}

	public void setSv(double[] sv) {
		Sv = sv;
	}
	
	
	
	public double[] getNormal() {
		return normal;
	}

	public void setNormal(double[] normal) {
		this.normal = normal;
	}

	public double[][] getCurvatureDirections() {
		return curvatureDirections;
	}

	public void setCurvatureDirections(double[][] cuvatureDirectionsManifold) {
		this.curvatureDirections = cuvatureDirectionsManifold;
	}

	public double[][] getPrincipalDirections() {
		return pricipalDirections;
	}

	public void setPrincipalDirections(double[][] pd) {
		this.pricipalDirections = pd;
	}

	public double getMinCurvature() {
		return minCurvature;
	}

	public void setMinCurvature(double lambda) {
		this.minCurvature = lambda;
	}

	public double getMaxCurvature() {
		return maxCurvature;
	}

	public void setMaxCurvature(double my) {
		this.maxCurvature = my;
	}

	public double[][] getWeingartenOperator() {
		return Weingartenoperator;
	}

	public void setWeingartenOperator(double[][] weingartenoperator) {
		Weingartenoperator = weingartenoperator;
	}
	
	public double getGaussCurvature() {
		return GaussCurvature;
	}

	public void setGaussCurvature(double gaussCurvature) {
		GaussCurvature = gaussCurvature;
	}

	public double getMeanCurvature() {
		return MainCurvature;
	}

	public void setMeanCurvature(double mainCurvature) {
		MainCurvature = mainCurvature;
	}
	
	@Override
	public String toString(){
		String str = new String();
		System.out.println("Su =  "+Arrays.toString(Su));
		System.out.println("Sv = "+Arrays.toString(Sv));
		System.out.println("Suu = "+Arrays.toString(Suu));
		System.out.println("Suv = "+Arrays.toString(Suv));
		System.out.println("Svv = "+Arrays.toString(Svv));
		System.out.println("curvature directions at the manifold:");
		if(Weingartenoperator[0][1] == 0 && minCurvature == maxCurvature){
			System.out.println("umbilic point");
		} else {
			System.out.println(Arrays.toString(curvatureDirections[0]));
			System.out.println(Arrays.toString(curvatureDirections[1]));
		}
		System.out.println("curvature directions in the domain:");
		if(Weingartenoperator[0][1] == 0 && minCurvature == maxCurvature){
			System.out.println("umbilic point");
		}else{
		System.out.println(Arrays.toString(pricipalDirections[0]));
		System.out.println(Arrays.toString(pricipalDirections[1]));
		}
		System.out.println("curvatures:");
		System.out.println("lambda: "+minCurvature);
		System.out.println("my: "+maxCurvature);
		System.out.println("shapeoperator:");
		System.out.println(Weingartenoperator[0][0]+"  "+Weingartenoperator[0][1]);
		System.out.println(Weingartenoperator[1][0]+"  "+Weingartenoperator[1][1]);
		System.out.println("Gauss curvature: ");
		System.out.println(GaussCurvature);
		System.out.println(" Main curvature: ");
		System.out.println(MainCurvature);
		return str;
	}
	
}


