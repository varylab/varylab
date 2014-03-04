package de.varylab.varylab.plugin.nurbs.data;

import java.util.Arrays;
import java.util.logging.Logger;

public class CurvatureInfo {
	
	private static Logger logger = Logger.getLogger(CurvatureInfo.class.getName());
	
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
	private double MeanCurvature;
	
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
		return MeanCurvature;
	}

	public void setMainCurvature(double mainCurvature) {
		MeanCurvature = mainCurvature;
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
		return MeanCurvature;
	}

	public void setMeanCurvature(double mainCurvature) {
		MeanCurvature = mainCurvature;
	}
	
	@Override
	public String toString(){
		String str = new String();
		logger.info("Su =  "+Arrays.toString(Su));
		logger.info("Sv = "+Arrays.toString(Sv));
		logger.info("Suu = "+Arrays.toString(Suu));
		logger.info("Suv = "+Arrays.toString(Suv));
		logger.info("Svv = "+Arrays.toString(Svv));
		logger.info("curvature directions at the manifold:");
		if(Weingartenoperator[0][1] == 0 && minCurvature == maxCurvature){
			logger.info("umbilic point");
		} else {
			logger.info(Arrays.toString(curvatureDirections[0]));
			logger.info(Arrays.toString(curvatureDirections[1]));
		}
		logger.info("curvature directions in the domain:");
		if(Weingartenoperator[0][1] == 0 && minCurvature == maxCurvature){
			logger.info("umbilic point");
		}else{
		logger.info(Arrays.toString(pricipalDirections[0]));
		logger.info(Arrays.toString(pricipalDirections[1]));
		}
		logger.info("curvatures:");
		logger.info("lambda: "+minCurvature);
		logger.info("my: "+maxCurvature);
		logger.info("shapeoperator:");
		logger.info(Weingartenoperator[0][0]+"  "+Weingartenoperator[0][1]);
		logger.info(Weingartenoperator[1][0]+"  "+Weingartenoperator[1][1]);
		logger.info("Gauss curvature: " + GaussCurvature);
		logger.info(" Mean curvature: " + MeanCurvature);
		return str;
	}
	
}


