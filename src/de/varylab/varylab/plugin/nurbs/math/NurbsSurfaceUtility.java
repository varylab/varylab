package de.varylab.varylab.plugin.nurbs.math;

import java.util.Arrays;
import java.util.LinkedList;

import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurfaceFactory;

public class NurbsSurfaceUtility {

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
		layer.addAdapter(qmf.getUVAdapter(), false);
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
			System.out.println("HALLLLLOOOOOOOOO");
			double[] V = ns.getVKnotVector();
			double v0 = V[0];
			double vm = V[V.length - 1];
			double length = vm - v0;
			double angle = 2 * Math.PI / (double)n;
			System.out.println("angle = " + angle);
			for (int i = 0; i < n; i++) {
				double phi = i * angle;
				System.out.println("phi = " + phi);
				if(phi <= Math.PI / 2.){
					double v = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					v = v0 + v;
					double [] p  = {point[0], v};
					System.out.println("p = " + Arrays.toString(p));
					points.add(p);
				}
				else if(Math.PI / 2. < phi && phi <= Math.PI){
					phi = phi - Math.PI / 2.;
					double v = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					v = v0 + v + length / 4.;
					double [] p  = {point[0], v};
					System.out.println("p = " + Arrays.toString(p));
					points.add(p);
				}
				else if(Math.PI < phi && phi <= 3. * Math.PI / 2.){
					phi = phi - Math.PI;
					double v = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					v = v0 + v + length / 2.;
					double [] p  = {point[0], v};
					System.out.println("p = " + Arrays.toString(p));
					points.add(p);
				}
				else{
					phi = phi - 3. * Math.PI / 2.;
					double v = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					v = v0 + v + 3. * length / 4.;
					double [] p  = {point[0], v};
					System.out.println("p = " + Arrays.toString(p));
					points.add(p);
				}
			}
			
		}
		else if(PointProjectionSurfaceOfRevolution.isSurfaceOfRevolutionVDir(ns)){
			System.out.println("drinn");
			double[] U = ns.getUKnotVector();
			double u0 = U[0];
			double un = U[U.length - 1];
			double length = un - u0;
			double angle = 2 * Math.PI / (double)n;
			System.out.println("angle = " + angle);
			for (int i = 0; i < n; i++) {
				double phi = i * angle;
				System.out.println("phi = " + phi);
				if(phi <= Math.PI / 2.){
					double u = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					u = u0 + u;
					double [] p  = {u, point[1]};
					System.out.println("p = " + Arrays.toString(p));
					points.add(p);
				}
				else if(Math.PI / 2. < phi && phi <= Math.PI){
					phi = phi - Math.PI / 2.;
					double u = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					u = u0 + u + length / 4.;
					double [] p  = {u, point[1]};
					System.out.println("p = " + Arrays.toString(p));
					points.add(p);
				}
				else if(Math.PI < phi && phi <= 3. * Math.PI / 2.){
					phi = phi - Math.PI;
					double u = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					u = u0 + u + length / 2.;
					double [] p  = {u, point[1]};
					System.out.println("p = " + Arrays.toString(p));
					points.add(p);
				}
				else{
					phi = phi - 3. * Math.PI / 2.;
					double u = Math.PI / 2. * ((1 + Math.sqrt(2)) * (1 - Math.cos(phi)) + Math.sin(phi)) / (Math.sqrt(2) + 2 * Math.sin(phi));
					u = u0 + u + 3. * length / 4.;
					double [] p  = {u, point[1]};
					System.out.println("p = " + Arrays.toString(p));
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

}
