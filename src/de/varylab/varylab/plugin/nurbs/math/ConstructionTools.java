package de.varylab.varylab.plugin.nurbs.math;

import java.util.Arrays;

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

}
