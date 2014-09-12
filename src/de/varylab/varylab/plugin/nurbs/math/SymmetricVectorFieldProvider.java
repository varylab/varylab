package de.varylab.varylab.plugin.nurbs.math;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurveFactory.SymmetricDir;
import de.varylab.varylab.plugin.nurbs.math.IntegralCurveFactory.VectorFields;

public class SymmetricVectorFieldProvider implements VectorFieldProvider {
	
	private SymmetricDir symDir = SymmetricDir.CURVATURE;
	private NURBSSurface surface = null;
	private double[] vecField = null;
	
	public SymmetricVectorFieldProvider(NURBSSurface surf, SymmetricDir sd, double[] vecField) {
		symDir = sd;
		surface = surf;
		this.vecField  = vecField;
	}
	
	public double[] getVectorField(double[] uv, VectorFields vf) {
		double[] vec = vecField;
		if(symDir == SymmetricDir.CURVATURE){
				vec = getSymConjDirWRTCuvatureDirection(uv);
		}
		else if(symDir == SymmetricDir.DIRECTION){
			vec = getSymConjDirWRTDirection(uv);
		}
		if(vf == VectorFields.FIRST){
			return getConj(vec, uv);
		} else {
			return vec;
		}
	}
	
	/**
	 * 
	 * @param p
	 * @return a given direction if the surface is not a surface of revolution  </br>
	 * else in the case of a surface of revolution:</br>
	 * 1.case (gaussian curvature K >= 0):</br> 
	 * a direction will be returned such that the conjugate direction appears with the same angle with respect to the rotation axis</br>
	 *  <table>
	 * <tr><td><td><td><td><td><td>l<td>m<td><td><td><td>-v1
	 * <tr><td>(v1,<td>1)<td><td><td>*<td>m<td>n<td>*<td><td><td>1<td><td>= -l * v1^2 + n  = 0 <=> v1 = sqrt(n/l)
	 * </table> 
	 * </br>
	 * 2.case (gaussian curvature K < 0):</br> 
	 * an assymptotic direction will be returned
	 */
	
	private double[] getSymConjDirSurfaceOfRevolution(double[] p) {
		double[] dir = {1,1};
		if(!surface.isSurfaceOfRevolution()){
			return dir;
		}
		else{
			CurvatureInfo ci =  NURBSCurvatureUtility.curvatureAndDirections(surface, p);
			double[][] sF = ci.getSecondFundamental();
			double l = sF[0][0];
			double n = sF[1][1];
			double K = ci.getGaussCurvature();
			if(K >= 0){
				dir[0] = Math.sqrt(n / l);
				return dir;
				
			}
			else{
				return getAssymptoticDirection(p);
				
			}
		}
	}
	
	
	private double[] getSymConjDirWRTCuvatureDirection(double[] point) {
		double[] dir = {1,1};
		if(surface.isSurfaceOfRevolution()){
			return getSymConjDirSurfaceOfRevolution(point);
		}
		CurvatureInfo ci =  NURBSCurvatureUtility.curvatureAndDirections(surface, point);
		double[] w1 = ci.getPrincipalDirections()[0];
		double[] w2 = ci.getPrincipalDirections()[1];
			double K = ci.getGaussCurvature();
			if(K > 0){
				double k1 = ci.getMinCurvature();
				double k2 = ci.getMaxCurvature();
			double theta;
			if(k2 == 0){
				theta = Math.PI / 2.;
			}
			else{
				theta = Math.atan(Math.sqrt(k1 / k2));
			}
			dir[0] = Math.cos(theta) * w1[0] + Math.sin(theta) * w2[0];
			dir[1] = Math.cos(theta) * w1[1] + Math.sin(theta) * w2[1];
			Rn.normalize(dir, dir);
			return dir;
		}
		else{
			return getAssymptoticDirection(point);
		}

//		}
	}
	
	public double[] getSymConjDirWRTDirection(double[] point) {
		double[] dir = {1,1};
		CurvatureInfo ci =  NURBSCurvatureUtility.curvatureAndDirections(surface, point);
		double[] w1 = ci.getPrincipalDirections()[0];
		double[] w2 = ci.getPrincipalDirections()[1];
		double K = ci.getGaussCurvature();
		if(K > 0){
			double k1 = ci.getMinCurvature();
			double k2 = ci.getMaxCurvature();
			double[] e1 = ci.getCurvatureDirections()[0];
			double[] e2 = ci.getCurvatureDirections()[1];
			double[] v = Rn.normalize(null, Rn.add(null, Rn.times(null, vecField[0], e1), Rn.times(null, vecField[1], e2)));
			double delta = 0.;
			if(Rn.innerProduct(v, e1) > 1){
				delta = 0.;
			}
			else if(Rn.innerProduct(v, e1) < -1){
				delta = Math.PI;
			}
			else{
				delta = 2. * Math.acos(Rn.innerProduct(v, e1));
			}
			double theta;
			if(k2 == 0){
				theta = Math.PI / 2.;
			}
			else{
				double q = k1 / k2;
				double p = Math.tan(delta) * (1 + q) / 2;
				theta = Math.atan(p + Math.sqrt(p * p + q));
			}
			dir[0] = Math.cos(theta) * w1[0] + Math.sin(theta) * w2[0];
			dir[1] = Math.cos(theta) * w1[1] + Math.sin(theta) * w2[1];
			Rn.normalize(dir, dir);
			return dir;
		} else {
			return getAssymptoticDirection(point);
		}
	}
	
	/**
	 
	 * <table>
	 * <tr><td><td><td><td><td><td>l<td>m<td><td><td><td>v1
	 * <tr><td>(v1<td>v2)<td><td><td>*<td>m<td>n<td>*<td><td><td>v2<td><td>= l * v1^2 + 2 * m * v1 * v2 + n * v2^2 = 0
	 * </table> 
	 * </br>
	 * Let K be the gaussian curvature</br>
	 * 1.case: assume n != 0 and set v1 = 1, then </br>
	 * v2^2  + 2 * (m/n) * v2 + l/n = 0 <=> v2 = (-m + sqrt(m ^ 2 - l * n)) / n = (-m + sqrt(-K)) / n
	 * @param ns
	 * @param p
	 * @return assymptotic direction at a point p
	 */
	
	private double[] getAssymptoticDirection(double[] p){
		CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(surface, p);
		double[][] sF = ci.getSecondFundamental();
		double[] assymptotic = new double[2];
		double K = ci.getGaussCurvature();
		if(K >= 0){
			throw new IllegalArgumentException("there exits no assypmtotic direction at points with positive gaussian curvature");
		}
		double m = sF[0][1];
		double n = sF[1][1];
		if(n != 0){
			assymptotic[0] = 1;
			assymptotic[1] = (-m + Math.sqrt(-K)) / n;
		}
		else{
			assymptotic[0] = 0;
			assymptotic[1] = 1;
		}
//		double[] curveDir = ci.getCurvatureDirections()[0];
//		if(curveDir[0] * assymptotic[1] - assymptotic[0] * curveDir[1] < 0){
//			assymptotic = reflectAtCurveDir(assymptotic, p);
//		}
		return assymptotic;
	}
	
	private double[] reflectAtCurveDir(double[] v, double[] p){
		CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(surface, p);
		double[] curveDir = ci.getCurvatureDirections()[0];
		Rn.normalize(curveDir, curveDir);
		double proj = Rn.innerProduct(curveDir, v);
		Rn.times(curveDir, proj, curveDir);
		double[] reflection = Rn.subtract(null, curveDir, v);
		Rn.times(reflection, 2., reflection);
		return Rn.add(null, v, reflection);
	}
	
	private double[] getConj(double[] v, double[] p){
		CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(surface, p);
		double[][] sF = ci.getSecondFundamental();
		double K = ci.getGaussCurvature();
		if(K < 0 && symDir == SymmetricDir.CURVATURE && surface.isSurfaceOfRevolution()){
			double[] w = new double[2];
			w[0] = v[0];
			w[1] = -v[1];
			return w;
		}
		else if(K < 0 && symDir == SymmetricDir.CURVATURE){
			return reflectAtCurveDir(v, p);
		}
		else {
			double[] b = new double[2];
			b[0] = v[0] * sF[0][0] + v[1] * sF[0][1];
			b[1] = v[0] * sF[1][0] + v[1] * sF[1][1];
			double[] w = new double[2];
			w[0] = -b[1];
			w[1] = b[0];
			return w;
		}
		
	}

//	private double[] getConj(double[] v, double[] p){
//		CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(surface, p);
//		double[][] sF = ci.getSecondFundamental();
//		double K = ci.getGaussCurvature();
//		if(!(surface.isSurfaceOfRevolution() && symDir == SymmetricDir.CURVATURE) && K > 0){
//			double[] b = new double[2];
//			b[0] = v[0] * sF[0][0] + v[1] * sF[0][1];
//			b[1] = v[0] * sF[1][0] + v[1] * sF[1][1];
//			double[] w = new double[2];
//			w[0] = -b[1];
//			w[1] = b[0];
//			return w;
//			/**
//			 *  We assume that symmetric direction w.r.t. curvature direction is chosen and the other
//			 *  asymptotic direction on a surface of revolution is a reflection at the 
//			 *  canonical axis in the domain
//			 */
//		} else {
//			double[] w = new double[2];
//			w[0] = v[0];
//			w[1] = -v[1];
//			return w;
//		}
//		
//	}
}