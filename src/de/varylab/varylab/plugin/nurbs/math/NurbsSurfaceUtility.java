package de.varylab.varylab.plugin.nurbs.math;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurfaceFactory;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.data.SignedUV;
import de.varylab.varylab.plugin.nurbs.plugin.PointSelectionPlugin.Direction;
import de.varylab.varylab.plugin.nurbs.plugin.PointSelectionPlugin.Parameter;

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
	
	
	
	public static LinkedList<LinkedList<SignedUV>> getCommonPointsFromSelection(NURBSSurface ns, Parameter param, Direction dir, LinkedList<double[]> selPoints, double dist, int numberOfPoints){
		LinkedList<LinkedList<SignedUV>> commonPointList = new LinkedList<>();
		LinkedList<SignedUV> selPointsSigned = new LinkedList<>();
		for (double[] selPoint : selPoints) {
			SignedUV signedPoint = new SignedUV(selPoint, 1.0);
			selPointsSigned.add(signedPoint);
		}
		commonPointList.add(selPointsSigned);
		double currDist = 0.0;
		for (int j = 1; j <= numberOfPoints; j++) {
			LinkedList<SignedUV> commonPoints = new LinkedList<>();
			currDist = (double)j / (double)numberOfPoints * dist;
			for (double[] point : selPoints) {
				if(param != Parameter.V){
					if(dir != Direction.DOWN){
						double[] next = {point[0] + currDist, point[1]};
						double sign = -1.0;
						SignedUV signedPoint = new SignedUV(next, sign);
						commonPoints.add(signedPoint);
					}
					if(dir != Direction.UP){
						double[] next = {point[0] - currDist, point[1]};
						double sign = 1.0;
						SignedUV signedPoint = new SignedUV(next, sign);
						commonPoints.add(signedPoint);
					}
				}
				if(param != Parameter.U){
					if(dir != Direction.DOWN){
						double[] next = {point[0], point[1] + currDist};
						double sign = -1.0;
						SignedUV signedPoint = new SignedUV(next, sign);
						commonPoints.add(signedPoint);
					}
					if(dir != Direction.UP){
						double[] next = {point[0], point[1] - currDist};
						double sign = 1.0;
						SignedUV signedPoint = new SignedUV(next, sign);
						commonPoints.add(signedPoint);
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
	

	public static double[][] computeUmbilicalPoints(NURBSSurface surface, VHDS hds, AdapterSet as ) {
		List<double[]> singularities = surface.findUmbilics(hds, as);
		double[][] upoints = new double[singularities.size()][];
		for (int i = 0; i < singularities.size(); i++) {
			upoints[i] = surface.getSurfacePoint(singularities.get(i));
		}
		return upoints;
	}

}
