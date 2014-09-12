package de.varylab.varylab.plugin.nurbs.math;

import java.util.Arrays;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;

public class ConstructionTools {
	
	public static NURBSSurface constructOpenTorus(NURBSSurface ns){
		NURBSSurface openTorus = new NURBSSurface();
		openTorus = ns.SurfaceKnotInsertion(true, 0.1, 2);
		openTorus = openTorus.SurfaceKnotInsertion(true, 0.2, 2);
		openTorus = openTorus.SurfaceKnotInsertion(true, 0.3, 2);
		openTorus = openTorus.SurfaceKnotInsertion(true,  0.3926990816987241, 2);
		openTorus = openTorus.SurfaceKnotInsertion(true,  1.178097245096172, 2);
		
//		openTorus = openTorus.SurfaceKnotInsertion(true,  0.3926990816987241, 1);
		openTorus = openTorus.SurfaceKnotInsertion(true, 1.2744, 2);
		openTorus = openTorus.SurfaceKnotInsertion(true, 1.3744, 2);
		openTorus = openTorus.SurfaceKnotInsertion(true, 1.4744, 2);
		double u = openTorus.getControlMesh().length;
		double v = openTorus.getControlMesh()[0].length;
		for (int i = 0; i < u; i++) {
			for (int j = 0; j < v; j++) {
				if(j == 0 && i < 7){
					openTorus.getControlMesh()[i][j][1] = openTorus.getControlMesh()[i][j][1] + (u-i) * 0.02;
					openTorus.getControlMesh()[i][j][0] = 1.2;
					openTorus.getControlMesh()[i][j][3] = 1.;
				}
				if(j == 0 && (i + 7) > u){
					openTorus.getControlMesh()[i][j][1] = openTorus.getControlMesh()[i][j][1] + (i + 1) * 0.02;
					openTorus.getControlMesh()[i][j][0] = 1.2;
					openTorus.getControlMesh()[i][j][3] = 1.;
				}
				if(j == openTorus.getControlMesh()[0].length - 1 && i < 7){
					openTorus.getControlMesh()[i][j][1] = openTorus.getControlMesh()[i][j][1] - ((u-i) * 0.02);
					openTorus.getControlMesh()[i][j][0] = 1.2;
					openTorus.getControlMesh()[i][j][3] = 1.;
				}
				if(j == v - 1 && (i + 7) > u){
					openTorus.getControlMesh()[i][j][1] = openTorus.getControlMesh()[i][j][1] - (i + 1)* 0.02;
					openTorus.getControlMesh()[i][j][0] = 1.2;
					openTorus.getControlMesh()[i][j][3] = 1.;
				}
			}
		}
		return openTorus;
	}
	
	
	
	private static double[] rotate(double phi, double[] v){
		double[] rot = new double[2];
		rot[0] = Math.cos(phi) * v[0] - Math.sin(phi) * v[1];
		rot[1] = Math.sin(phi) * v[0] + Math.cos(phi) * v[1];
		return rot;
	}
	
	
	private static double[] getMidpoint(double[] v1, double[] v2){
		return Rn.add(null, v1, Rn.times(null, 0.5, Rn.subtract(null, v2, v1)));
	}
	
	private static double[] getPointXYZW(double[] xy, double z){
		double[] xyzw = new double[4];
		xyzw[0] = xy[0];
		xyzw[1] = xy[1];
		xyzw[2] = z;
		xyzw[3] = 1.;
		return xyzw;
	}
	
	private static double[] getPointXYZWeight(double[] xy, double z){
		double[] xyzw = new double[4];
		xyzw[0] = xy[0];
		xyzw[1] = xy[1];
		xyzw[2] = z;
		xyzw[3] = Math.sqrt(1.5);
		return xyzw;
	}
	
	private static double[][] getNGonVertsXY(int n){
		double phi = 2 * Math.PI / (double)n;
		double[][] xyCorners = new double[n + 1][2];
		double[] start = {1.,0.};
		for (int i = 0; i <= n; i++) {
			xyCorners[i] = rotate(i * phi, start);
		}
		xyCorners[n] = start;
		double[][] vertsXY = new double[2 * n + 1][];
//		vertsXY[0] = getMidpoint(xyCorners[0], xyCorners[1]);
		for (int i = 0; i < n; i++) {
			vertsXY[2 * i] = getMidpoint(xyCorners[i], xyCorners[i + 1]);
			vertsXY[2 * i + 1] = xyCorners[i + 1];
		}
		vertsXY[2 * n] = getMidpoint(xyCorners[0], xyCorners[1]);
		return vertsXY;
	}
	
	public static NURBSSurface constructNGon(int n){
		double[][][] controlMesh = new double[2 * n + 1][3][4];
		double[] summit = {0.,0.,0.3,1.};
		double[][] vertsXY = getNGonVertsXY(n);
		for (int i = 0; i < vertsXY.length; i++) {
			controlMesh[i][0] = getPointXYZW(vertsXY[i], 0.);
			controlMesh[i][1] = getPointXYZWeight(vertsXY[i], 0.3);
			controlMesh[i][2] = summit;
		}

		int p = 2;
		int q = 2;
		double[] U = new double[p + 2 * n + 1 + 1];
		U[0] = 0.;
		U[1] = 0.;
		U[2] = 0.;
		for (int i = 3; i < U.length - 3; i++) {
			U[i] = (int)((i - 1) / 2);
		}
		U[U.length - 3] =  (int)((U.length - 3) / 2);
		U[U.length - 2] =  (int)((U.length - 3) / 2);
		U[U.length - 1] =  (int)((U.length - 3) / 2);
		double[] V = {0.,0.,0.,1.,1.,1.};
		
		return new NURBSSurface(U, V, controlMesh, p, q);
	}
	
	
	public static void main(String[] args){
		int n = 4;
//		NURBSSurface ns = constructNGon(n);
//		System.out.println(ns.toString());
		double[][] verts = getNGonVertsXY(n);
		for (int i = 0; i < verts.length; i++) {
			System.out.println(Arrays.toString(verts[i]));
		}
		
	}

}




