package de.varylab.varylab.plugin.nurbs.math;

import de.varylab.varylab.plugin.nurbs.NURBSSurface;

public class NurbsDeformationTools {
	
	public static NURBSSurface[] splitInTheMiddle(NURBSSurface ns, boolean dir){
		return ns.splitInTheMiddle(dir);
	}
	
	public static void stretch(NURBSSurface ns, double a, double b, double c){
		double[][][] cm = ns.getControlMesh();
		for (int i = 0; i < cm.length; i++) {
			for (int j = 0; j < cm[0].length; j++) {
				cm[i][j][0] *= a;
				cm[i][j][1] *= b;
				cm[i][j][2] *= c;
			}
		}
	}
	
	public static double getMinValueX(NURBSSurface ns){
		double[][][] cm = ns.getControlMesh();
		double min = Double.MAX_VALUE;
		for (int i = 0; i < cm.length; i++) {
			for (int j = 0; j < cm[0].length; j++) {
				if(cm[i][j][0] < min){
					min = cm[i][j][0];
				}
			}
		}
		return min;
	}
	
	public static double getMaxValueX(NURBSSurface ns){
		double[][][] cm = ns.getControlMesh();
		double max = Double.MIN_VALUE;
		for (int i = 0; i < cm.length; i++) {
			for (int j = 0; j < cm[0].length; j++) {
				if(cm[i][j][0] > max){
					max = cm[i][j][0];
				}
			}
		}
		return max;
	}
	
	public static double getMaxDistDirectionX(NURBSSurface ns){
		return getMaxValueX(ns) - getMinValueX(ns);
	}
	
	public static void conicDeformation(NURBSSurface ns, double stretch){
		double minValue =  getMinValueX(ns);
		System.out.println("firstValue = " + minValue);
		double maxDist = getMaxDistDirectionX(ns);
		System.out.println("maxDist = " + maxDist);
		double[][][] cm = ns.getControlMesh();
		for (int i = 0; i < cm.length; i++) {
			for (int j = 0; j < cm[0].length; j++) {
				double x = Math.abs(cm[i][j][0] - minValue);
				double factor = stretch * x / maxDist;
				System.out.println("factor = " + factor);
				cm[i][j][2] *= factor;
			}
		}
	}
}
