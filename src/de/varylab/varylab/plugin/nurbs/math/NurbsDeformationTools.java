package de.varylab.varylab.plugin.nurbs.math;

import java.util.Arrays;

import org.antlr.runtime.LegacyCommonTokenStream;
import org.netlib.util.doubleW;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;

public class NurbsDeformationTools {
	
	public static NURBSSurface[] splitInTheMiddle(NURBSSurface ns, boolean dir){
		return ns.splitInTheMiddle(dir);
	}
	
//	public static void stretch(NURBSSurface ns, double a, double b, double c){
//		double[][][] cm = ns.getControlMesh();
//		for (int i = 0; i < cm.length; i++) {
//			for (int j = 0; j < cm[0].length; j++) {
//				cm[i][j][0] *= a;
//				cm[i][j][1] *= b;
//				cm[i][j][2] *= c;
//			}
//		}
//	}
	
	public static NURBSSurface stretch(NURBSSurface ns, double a, double b, double c){
		NURBSSurface stretch = new NURBSSurface();
		stretch.setUDegree(ns.getUDegree());
		stretch.setVDegree(ns.getVDegree());
		stretch.setUKnotVector(ns.getUKnotVector());
		stretch.setVKnotVector(ns.getVKnotVector());
		double[][][] cm = ns.getControlMesh();
		double[][][] cmStretch = new double[cm.length][cm[0].length][4];
		for (int i = 0; i < cm.length; i++) {
			for (int j = 0; j < cm[0].length; j++) {
				cmStretch[i][j][0] = cm[i][j][0] * a;
				cmStretch[i][j][1] = cm[i][j][1] * b;
				cmStretch[i][j][2] = cm[i][j][2] * c;
				cmStretch[i][j][3] = cm[i][j][3];
			}
		}
		stretch.setControlMesh(cmStretch);
		return stretch;
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
	
	public static double[][] getMaxMinValues(NURBSSurface ns){
		double[][][] cm = ns.getControlMesh();
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double minZ = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		double maxZ = Double.MIN_VALUE;
		for (int i = 0; i < cm.length; i++) {
			for (int j = 0; j < cm[0].length; j++) {
				if(cm[i][j][0] < minX){
					minX = cm[i][j][0];
				}
				if(cm[i][j][1] < minY){
					minY = cm[i][j][1];
				}
				if(cm[i][j][2] < minZ){
					minZ = cm[i][j][2];
				}
				if(cm[i][j][0] > maxX){
					maxX = cm[i][j][0];
				}
				if(cm[i][j][1] > maxY){
					maxY = cm[i][j][1];
				}
				if(cm[i][j][2] > maxZ){
					maxZ = cm[i][j][2];
				}
			}
		}
		double[][] MaxMin = new double[2][3];
		MaxMin[0][0] = minX; MaxMin[0][1] = minY; MaxMin[0][2] = minZ; 
		MaxMin[1][0] = maxX; MaxMin[1][1] = maxY; MaxMin[1][2] = maxZ; 
		return null;
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
		return getMaxMinValues(ns)[1][0] - getMaxMinValues(ns)[0][0];
	}
	
	public static double getMaxDistDirectionY(NURBSSurface ns){
		return getMaxMinValues(ns)[1][1] - getMaxMinValues(ns)[0][1];
	}
	
	public static double getMaxDistDirectionZ(NURBSSurface ns){
		return getMaxMinValues(ns)[1][2] - getMaxMinValues(ns)[0][2];
	}
	
	public static void conicDeformationZ_X(NURBSSurface ns, double deformZ){
		double minValue =  getMinValueX(ns);
		System.out.println("firstValue = " + minValue);
		double maxDist = getMaxDistDirectionX(ns);
		System.out.println("maxDist = " + maxDist);
		double[][][] cm = ns.getControlMesh();
		for (int i = 0; i < cm.length; i++) {
			for (int j = 0; j < cm[0].length; j++) {
				double x = Math.abs(cm[i][j][0] - minValue);
				double factor = deformZ * x / maxDist;
				System.out.println("factor = " + factor);
				cm[i][j][2] *= factor;
			}
		}
	}
	
	public static void conicDeformationY_X(NURBSSurface ns, double deformY){
		double minValue =  getMinValueX(ns);
		System.out.println("firstValue = " + minValue);
		double maxDist = getMaxDistDirectionX(ns);
		System.out.println("maxDist = " + maxDist);
		double[][][] cm = ns.getControlMesh();
		for (int i = 0; i < cm.length; i++) {
			for (int j = 0; j < cm[0].length; j++) {
				double x = Math.abs(cm[i][j][0] - minValue);
				double factor = deformY * x / maxDist;
				System.out.println("factor = " + factor);
				cm[i][j][1] *= factor;
			}
		}
	}
	
	private static int[] setSplittedULineCount(NURBSSurface ns, int uLineCount, double u){
		double[] U = ns.getUKnotVector();
		double uLength = U[U.length - 1] - U[0];
		double localU = u - U[0];
		int uLineCount1 = (int)Math.round(uLineCount * localU / uLength) + 1;
		int uLineCount2 = uLineCount - uLineCount1 + 1;
		return new int[]{uLineCount1, uLineCount2};
	}
	
	private static int[] setSplittedVLineCount(NURBSSurface ns, int vLineCount, double v){
		double[] V = ns.getVKnotVector();
		double vLength = V[V.length - 1] - V[0];
		double localV = v - V[0];
		int vLineCount1 = (int)Math.round(vLineCount * localV / vLength) + 1;
		int vLineCount2 = vLineCount - vLineCount1 + 1;
		return new int[]{vLineCount1, vLineCount2};
	}
	
	public static int[][] getSplittedLineCounts(NURBSSurface ns, int uLineCount, int vLineCount, double uv, boolean dir){
		int [][]lineCounts = new int[2][2];
		if(dir){
			lineCounts[0][1] = vLineCount;
			lineCounts[1][1] = vLineCount;
			int[] uLineCounts = setSplittedULineCount(ns, uLineCount, uv);
			lineCounts[0][0] = uLineCounts[0];
			lineCounts[1][0] = uLineCounts[1];
		} else {
			lineCounts[0][0] = uLineCount;
			lineCounts[1][0] = uLineCount;
			int[] vLineCounts = setSplittedVLineCount(ns, vLineCount, uv);
			lineCounts[0][1] = vLineCounts[0];
			lineCounts[1][1] = vLineCounts[1];
		}
		return lineCounts;
	}
	
	public static double[] projectOntoVector(double[] p, double[] vec){
		double[] normalized = Rn.normalize(null, vec);
		double lambda = Rn.innerProduct(p, normalized);
		return Rn.times(null, lambda, normalized);
	}
	
	public static double[] get3DPoint(double[] p){
		double[] p3D = new double[3];
		p3D[0] = p[0] / p[3];
		p3D[1] = p[1] / p[3];
		p3D[2] = p[2] / p[3];
		return p3D;
	}
	
	public static double[][][] get3DControlMesh(double[][][]cm){
		double[][][] cm3D = new double[cm.length][cm[0].length][3];
		for (int i = 0; i < cm3D.length; i++) {
			for (int j = 0; j < cm3D[0].length; j++) {
				cm3D[i][j] = get3DPoint(cm[i][j]);
			}
		}
		return cm3D;
	}
	
	public static double getOrientedLength(double[] p, double[] vec){
		double length = Rn.euclideanNorm(p);
		if(Rn.innerProduct(p, vec) < 0){
			length = -length;
		}
		return length;
	}
	
	public static double[] getMinMaxValueWRTVector(NURBSSurface ns, double[] vec){
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		double[][][] cm3D = get3DControlMesh(ns.getControlMesh());
		for (int i = 0; i < cm3D.length; i++) {
			for (int j = 0; j < cm3D[0].length; j++) {
				double[] proj = projectOntoVector(cm3D[i][j], vec);
				double length = getOrientedLength(proj, vec);
				if(length < min){
					min = length;
				}
				if(length > max){
					max = length;
				}
			}
		}
		return new double[]{min,max};
	}
	
	/**
	 * 
	 * @param vec
	 * @param point
	 * @return
	 */
	public static double[] getOrthComplement(double[] vec, double[] point){
		double[] proj = projectOntoVector(point, vec);
		return Rn.subtract(null, point, proj);
	}
	
	
	/**
	 * 
	 * @param ns
	 * @param vec
	 * @param dir
	 * @param deformFactor
	 * @return
	 */
	public static NURBSSurface deform(NURBSSurface ns, double[] vec, double[] dir, double deformFactor){
		System.out.println(ns.toString());
		NURBSSurface deformNs = new NURBSSurface();
		deformNs.setUDegree(ns.getUDegree());
		deformNs.setVDegree(ns.getVDegree());
		deformNs.setUKnotVector(ns.getUKnotVector());
		deformNs.setVKnotVector(ns.getVKnotVector());
		double[][][] cm = ns.getControlMesh();
		double[][][] cm3D = get3DControlMesh(ns.getControlMesh());
		double[][][] cmDeform = new double[cm3D.length][cm3D[0].length][4];
		double[] orth = Rn.normalize(null, getOrthComplement(vec, dir));
		double[] MinMax = getMinMaxValueWRTVector(ns, vec);
		double dist = MinMax[1] - MinMax[0];
		for (int i = 0; i < cm3D.length; i++) {
			for (int j = 0; j < cm3D[0].length; j++) {
				double[] proj = projectOntoVector(cm3D[i][j], vec);
				double[] vecFromAxis = Rn.subtract(null, cm3D[i][j], proj);
				double pos = getOrientedLength(proj, vec) - MinMax[0];
				double currFactor = (deformFactor - 1) * pos / dist + 1;
				double[] projOrth = projectOntoVector(vecFromAxis, orth);
				double[] orthCompl = getOrthComplement(orth, vecFromAxis);
				Rn.times(projOrth, currFactor, projOrth);				
				Rn.add(cm3D[i][j], orthCompl, projOrth);
				Rn.add(cm3D[i][j], cm3D[i][j], proj);
				cmDeform[i][j][0] = cm3D[i][j][0];
				cmDeform[i][j][1] = cm3D[i][j][1];
				cmDeform[i][j][2] = cm3D[i][j][2];
				cmDeform[i][j][3] = cm[i][j][3];
			}
		}
		deformNs.setControlMesh(cmDeform);
		System.out.println(deformNs.toString());
		return deformNs;
		
	}
}
