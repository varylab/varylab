package de.varylab.varylab.plugin.nurbs.math;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Logger;

import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurfaceFactory;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;

public class NurbsSurfaceUtility {

	private static Logger logger = Logger.getLogger(NurbsSurfaceUtility.class.getName());
	
	public static void addNurbsMesh(NURBSSurface surf, HalfedgeLayer layer, int u, int v) {
		NURBSSurfaceFactory qmf = new NURBSSurfaceFactory();
		qmf.setGenerateVertexNormals(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setULineCount(u);
		qmf.setVLineCount(v);
		qmf.setSurface(surf);
		qmf.update();
		layer.set(qmf.getGeometry());
		NurbsUVAdapter uvAdapter = qmf.getUVAdapter();
		layer.addAdapter(uvAdapter, false);
//		layer.update();
	}
	
	public static double[] uniformKnotVector(int m, int deg) {
		double[] U = new double[m + deg + 1];
		int j = 0;
		for (int i = 0; i < U.length; i++) {
			if(i < deg+1) {
				U[i] = j;
				continue;
			} else if(i < m+1) {
				++j;
				U[i] = j;
			} else { // i >= p+m
				U[i] = j;
			}
		}
		return U;
	}

	public static LinkedList<double[]> getEquidistantRotatedPoints(NURBSSurface ns, int n, double[] point){
		LinkedList<double[]> points = new LinkedList<double[]>();
		if(PointProjectionSurfaceOfRevolution.isSurfaceOfRevolutionUDir(ns)){
			double[] V = ns.getVKnotVector();
			double v0 = V[0];
			double vm = V[V.length - 1];
			double length = vm - v0;
			double angle = 2 * Math.PI / (double)n;
			logger.info("angle = " + angle);
			for (int i = 0; i < n; i++) {
				double phi = i * angle;
				logger.info("phi = " + phi);
				if(phi <= Math.PI / 2.){
					double v = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					v = v0 + v;
					double [] p  = {point[0], v};
					logger.info("p = " + Arrays.toString(p));
					points.add(p);
				}
				else if(Math.PI / 2. < phi && phi <= Math.PI){
					phi = phi - Math.PI / 2.;
					double v = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					v = v0 + v + length / 4.;
					double [] p  = {point[0], v};
					logger.info("p = " + Arrays.toString(p));
					points.add(p);
				}
				else if(Math.PI < phi && phi <= 3. * Math.PI / 2.){
					phi = phi - Math.PI;
					double v = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					v = v0 + v + length / 2.;
					double [] p  = {point[0], v};
					logger.info("p = " + Arrays.toString(p));
					points.add(p);
				}
				else{
					phi = phi - 3. * Math.PI / 2.;
					double v = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					v = v0 + v + 3. * length / 4.;
					double [] p  = {point[0], v};
					logger.info("p = " + Arrays.toString(p));
					points.add(p);
				}
			}
			
		}
		else if(PointProjectionSurfaceOfRevolution.isSurfaceOfRevolutionVDir(ns)){
			logger.info("drinn");
			double[] U = ns.getUKnotVector();
			double u0 = U[0];
			double un = U[U.length - 1];
			double length = un - u0;
			double angle = 2 * Math.PI / (double)n;
			logger.info("angle = " + angle);
			for (int i = 0; i < n; i++) {
				double phi = i * angle;
				logger.info("phi = " + phi);
				if(phi <= Math.PI / 2.){
					double u = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					u = u0 + u;
					double [] p  = {u, point[1]};
					logger.info("p = " + Arrays.toString(p));
					points.add(p);
				}
				else if(Math.PI / 2. < phi && phi <= Math.PI){
					phi = phi - Math.PI / 2.;
					double u = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					u = u0 + u + length / 4.;
					double [] p  = {u, point[1]};
					logger.info("p = " + Arrays.toString(p));
					points.add(p);
				}
				else if(Math.PI < phi && phi <= 3. * Math.PI / 2.){
					phi = phi - Math.PI;
					double u = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					u = u0 + u + length / 2.;
					double [] p  = {u, point[1]};
					logger.info("p = " + Arrays.toString(p));
					points.add(p);
				}
				else{
					phi = phi - 3. * Math.PI / 2.;
					double u = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					u = u0 + u + 3. * length / 4.;
					double [] p  = {u, point[1]};
					logger.info("p = " + Arrays.toString(p));
					points.add(p);
				}
			}
			
		}
		else{
			double[] V = ns.getVKnotVector();
			double step = (V[V.length - 1] - V[0]) / n;
			for (int i = 0; i < n; i++) {
				double[] p = {0.0, i * step};
				points.add(p);
			}
		}
		return points;
	}
	
	
	public static double computeA3(double x1, double x2, double y1, double y2){
		return ((y2 / x2 - 1) * (x1 - 1) / (x2 - 1) - y1 / x1 + 1) / ((x1 - 1) * (x2 * x2 - 1) / (x2 - 1) - (x1 * x1 -1));
	}
	
	public static double computeA2(double x1, double y1, double a3){
		return (y1 / x1 - (x1 * x1 - 1) * a3 - 1) / (x1 - 1);
	}
	
	public static double computeA1(double a2, double a3){
		return 1 - a2 - a3;
	}
	
	public static double getPolynomialValue(double a1, double a2, double a3, double t){
		return a3 * t * t * t + a2 * t * t + a1 * t;
	}
	
	public static LinkedList<LinkedList<double[]>> getCommonPointsFromSelection(NURBSSurface ns, boolean uDir, boolean  vDir, boolean up, boolean down, 
			LinkedList<double[]> selPoints, double dist, int numberOfPoints){
		LinkedList<LinkedList<double[]>> commonPointList = new LinkedList<>();
		commonPointList.add(selPoints);
		double currDist = 0.0;
		for (int j = 1; j <= numberOfPoints; j++) {
			LinkedList<double[]> commonPoints = new LinkedList<>();
			currDist = (double)j / (double)numberOfPoints * dist;
			for (double[] point : selPoints) {
				if(uDir){
					if(up){
						double[] next = {point[0] + currDist, point[1]};
						commonPoints.add(next);
					}
					if(down){
						double[] next = {point[0] - currDist, point[1]};
						commonPoints.add(next);
					}
				}
				if(vDir){
					if(up){
						double[] next = {point[0], point[1] + currDist};
						commonPoints.add(next);
					}
					if(down){
						double[] next = {point[0], point[1] - currDist};
						commonPoints.add(next);
					}
				}
			}
			commonPointList.add(commonPoints);
		}
		return commonPointList;
	}
	
	public static LinkedList<double[]> getPointsFromDistList(NURBSSurface ns, boolean uDir, boolean  vDir, boolean up, boolean down, 
			double[] point, double dist, int numberOfPoints){
		LinkedList<double[]> distPoints = new LinkedList<>();
		double currDist = 0.0;
		for (int i = 1; i <= numberOfPoints; i++) {
			currDist = (double)i / (double)numberOfPoints * dist;
			if(uDir){
				if(up){
					double[] next = {point[0] + currDist, point[1]};
					distPoints.add(next);
				}
				if(down){
					double[] next = {point[0] - currDist, point[1]};
					distPoints.add(next);
				}
			}
			if(vDir){
				if(up){
					double[] next = {point[0], point[1] + currDist};
					distPoints.add(next);
				}
				if(down){
					double[] next = {point[0], point[1] - currDist};
					distPoints.add(next);
				}
			}
		}
		return distPoints;
	}
	
	public static LinkedList<double[]> getPointsFromDistListUp(NURBSSurface ns, boolean uDir, boolean  vDir, double[] point, double dist, int numberOfPoints){
		LinkedList<double[]> distPoints = new LinkedList<>();
		double currDist = 0.0;
		for (int i = 1; i <= numberOfPoints; i++) {
			currDist = (double)i / (double)numberOfPoints * dist;
			if(uDir){
				double[] next = {point[0] + currDist, point[1]};
				distPoints.add(next);
			}
			if(vDir){
				double[] next = {point[0], point[1] + currDist};
				distPoints.add(next);
			}
		}
		return distPoints;
	}
	
	public static LinkedList<double[]> getPointsFromDistListDown(NURBSSurface ns, boolean uDir, boolean  vDir, double[] point, double dist, int numberOfPoints){
		LinkedList<double[]> distPoints = new LinkedList<>();
		double currDist = 0.0;
		for (int i = 1; i <= numberOfPoints; i++) {
			currDist = (double)i / (double)numberOfPoints * dist;
			if(uDir){
				double[] next = {point[0] - currDist, point[1]};
				distPoints.add(next);
			}
			if(vDir){
				double[] next = {point[0], point[1] - currDist};
				distPoints.add(next);
			}
		}
		return distPoints;
	}


	public static void addNurbsMesh(NURBSSurface surface, HalfedgeLayer newLayer) {
		addNurbsMesh(surface, newLayer, surface.getNumUPoints()*2, surface.getNumVPoints()*2);
	}

}
