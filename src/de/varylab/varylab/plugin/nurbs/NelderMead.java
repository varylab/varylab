package de.varylab.varylab.plugin.nurbs;

import java.util.LinkedList;

import de.jreality.math.Rn;

public class NelderMead {
	
	/*
	 * simplex method Nelder Mead 
	 * Fredi Troesch "Ableitungsfreie Verfahren" 2.1
	 */
	
	
	final static double alpha = 0.5;
	final static double beta = 2.5;
	final static int gamma = 1;
	protected int dim;
	protected LinkedList<double[]> simplex;
	
	
	public NelderMead() {
//		super();
	}

	public NelderMead(int d, LinkedList<double[]> s){
		dim = d;
		simplex = s;
	}
	
	private double[] barycenter(){
		double[] sj = new double[dim];
		for (double[] point : simplex) {
			Rn.add(sj, sj, point);
		}
		Rn.times(sj, 1 / dim, sj);
		return sj;
	}
	
	public double[] reflection(double [] xj){
		double[] sj = barycenter(); 
		double[] xr = new double[dim];
		Rn.add(xr, sj, Rn.times(null, gamma, Rn.subtract(null, sj, xj)));
		return xr;
	}
	
	public double[] expansion(double[] sj,double[] xr){
		double[] xe = new double[dim];
		Rn.add(xe, sj, Rn.times(null, beta, Rn.subtract(null, xr, sj)));
		return xe;
	}
	
	public double[] partialContractionInterior(double[] sj,double[] xj){
		double[] xc = new double[dim];
		Rn.add(xc, sj, Rn.times(null, alpha, Rn.subtract(null, xj, sj)));
		return xc;
	}
	
	public double[] partialContractionExterior(double[] sj,double[] xr){
		double[] xc = new double[dim];
		Rn.add(xc, sj, Rn.times(null, alpha, Rn.subtract(null, xr, sj)));
		return xc;
	}
	
	public void totalContraction(double[] xj){
		for (double[] xi : simplex) {
			if(!Rn.equals(xi, xj)){
				Rn.times(xi, 0.5, Rn.subtract(null, xj, xi));
			}
		}
	}
	
	public void algorithm(){
		
	}

}
