package de.varylab.varylab.plugin.nurbs.math;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;

public class NURBSCurvatureUtility {
	
	
	public static double[] getEigenvector(double[][] matrix, double my, double eps){
		double[] eig = {1,0};
		double v1 = matrix[0][0] - my;
		double v2 = matrix[1][0];
		double w1 = matrix[0][1];
		double w2 = matrix[1][1] - my;
		double normV = Math.sqrt(v1 * v1 + v2 * v2);
		if(normV == 0){
			return eig;
		}
		double sin = -v2 / normV;
		double cos = v1 / normV;
		double coeff2 = cos * w1 - sin * w2;
		if(normV > eps){
			eig[1] = 1;
			eig[0] = -coeff2 / normV;
		}
		else{
			eig[0] = 1;
			eig[1] = -normV / coeff2;
		}
		return eig;
	}
	
	/**
	 * 
	 * @param ns
	 * @param u
	 * @param v
	 * @return lambda my K H
	 */
	public static CurvatureInfo curvatureAndDirections(NURBSSurface ns, double[] point){
		double u = point[0];
		double v = point[1];
		CurvatureInfo dG = new CurvatureInfo();
//		System.out.println("CurvatureInfo");
//		System.out.println(ns.toString());
		double[] FFs = new double[6];
		double[] U = ns.getUKnotVector();
		double[] V = ns.getVKnotVector();
		int p = ns.getUDegree();
		int q = ns.getVDegree();
		
		double[][][]SKL1 = new double[p+1][q+1][4];
		double[][][]SKL = new double[p+1][q+1][3];

		
		int nl = ns.getControlMesh().length-1;
		int ml = ns.getControlMesh()[0].length-1;
		NURBSAlgorithm.SurfaceDerivatives(nl, p, U, ml, q, V, ns.getControlMesh(), u, v, 4, SKL1);		
		double [][][] Aders = new double[SKL1.length][SKL1[0].length][3];
		double [][] wders = new double[SKL1.length][SKL1[0].length];
		for (int i = 0; i < SKL1.length; i++) {
			for (int j = 0; j < SKL1[0].length; j++) {
				wders[i][j]=SKL1[i][j][3];
				Aders[i][j][0] = SKL1[i][j][0];
				Aders[i][j][1] = SKL1[i][j][1];
				Aders[i][j][2] = SKL1[i][j][2];
			}
		}
		NURBSAlgorithm.RatSurfaceDerivs(Aders, wders, p+q, SKL);
		dG.setSu(SKL[1][0]);
		dG.setSv(SKL[0][1]);
		
		
		dG.setSuv(SKL[1][1]);
		if(p <= 1) {
			dG.setSuu(new double[]{0,0,0});
		} else {
			dG.setSuu(SKL[2][0]);
		}
		if(q <= 1) {
			dG.setSvv(new double[]{0,0,0});
		} else {
			dG.setSvv(SKL[0][2]);
		}
		double E = Rn.innerProduct(SKL[1][0], SKL[1][0]);
		double F = Rn.innerProduct(SKL[1][0], SKL[0][1]);
		double G = Rn.innerProduct(SKL[0][1], SKL[0][1]);
		
		double[][] R = new double[2][2];
		R[0][0] = E;
		R[0][1] = F;
		R[1][0] = F;
		R[1][1] = G;
		dG.setRiemannianMetric(R);
		
		
		double[] normal = new double[3];
	
		Rn.crossProduct(normal, SKL[1][0], SKL[0][1]);
		Rn.normalize(normal, normal);
		
		dG.setNormal(normal);
		
		double l = Rn.innerProduct(normal,dG.getSuu());
		double m = Rn.innerProduct(normal, SKL[1][1]);
		double n = Rn.innerProduct(normal,dG.getSvv());
		
		double[][] secondF = new double[2][2];
		secondF[0][0] = l;
		secondF[0][1] = m;
		secondF[1][0] = m;
		secondF[1][1] = n;
		dG.setSecondFundamental(secondF);
		

		FFs[0] = E;
		FFs[1] = F;
		FFs[2] = G;
		FFs[3] = l;
		FFs[4] = m;
		double[][] W = new double[2][2];
		double a11 = (G*l-F*m)/(E*G-F*F);
		double a12 = (G*m-F*n)/(E*G-F*F);
		double a21 = (E*m-F*l)/(E*G-F*F);
		double a22 = (E*n-F*m)/(E*G-F*F);
		W[0][0] = a11;
		W[0][1] = a12;
		W[1][0] = a21;
		W[1][1] = a22;
		dG.setWeingartenOperator(W);
		
		//lambda
		double lambda = (a11 + a22) / 2 + Math.sqrt((a11-a22) * (a11-a22) + 4 * a12 * a21) / 2;
		
		//my
		double my = (a11 + a22) / 2 - Math.sqrt((a11-a22) * (a11-a22) + 4 * a12 * a21) / 2;
		if(lambda > my) {
			double tmp = my;
			my = lambda;
			lambda = tmp;
		}
		dG.setMinCurvature(lambda);
		dG.setMaxCurvature(my);
		//K	
		dG.setGaussCurvature(a11*a22-a12*a21);
		//H
		dG.setMeanCurvature((a11 + a22) / 2);
		
		double[][] w = new double[2][2];
		if(Math.abs(a12) > 0.00001){
//			System.out.println("a12 != 0");
//			System.out.println("a12 = " + a12);
			w[0][0] = 1; 
			w[0][1] = (lambda - a11) / a12; 
	
		}
		else if(Math.abs(a21) > 0.00001){
//			System.out.println("a21 != 0");
			w[0][0] = (lambda - a22)/a21; 
			w[0][1] = 1; 
	
		}
		else if(Math.abs(a11 - lambda) <= Math.abs(a22 - lambda)){
			w[0][0] = 1; 
			w[0][1] = 0; 
		}
		else if(Math.abs(a22 - lambda) < Math.abs(a11 - lambda)){
			w[0][0] = 0; 
			w[0][1] = 1;
		}
		if(Math.abs(a12) > 0.00001){
			w[1][0] = 1; 
			w[1][1] = (my - a11) / a12; 
		}
		else if(Math.abs(a21) > 0.00001){
			w[1][0] = (my - a22) / a21; 
			w[1][1] = 1; 
	
		}
		else if(Math.abs(a11 - my) <= Math.abs(a22 - my)){
			w[1][0] = 1; 
			w[1][1] = 0; 
		}
		else if(Math.abs(a22 - my) < Math.abs(a11 - my)){
			w[1][0] = 0; 
			w[1][1] = 1; 
		}
		//only TEST
//		w[0] = getEigenvector(W, lambda, 0.00001);
//		w[1] = getEigenvector(W, my, 0.00001);
		// end TEST
		double[][] e = new double[2][3];
		Rn.add(e[0], Rn.times(null, w[0][0], SKL[1][0]), Rn.times(null, w[0][1], SKL[0][1]));
		Rn.add(e[1], Rn.times(null, w[1][0], SKL[1][0]), Rn.times(null, w[1][1], SKL[0][1]));
		// the w's are the coefficients of the normalized curvature directions in the basis Su, Sv
		Rn.times(w[0], 1 / Math.sqrt(Rn.innerProduct(e[0],e[0])), w[0]);
		Rn.times(w[1], 1 / Math.sqrt(Rn.innerProduct(e[1],e[1])), w[1]);
		
		dG.setPrincipalDirections(w);
		Rn.normalize(e[0], e[0]);
		Rn.normalize(e[1], e[1]);
		dG.setCurvatureDirections(e);
		return dG;
	}
	
	public static void main(String[] args){
		
	}
	
}
