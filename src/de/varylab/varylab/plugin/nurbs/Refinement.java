package de.varylab.varylab.plugin.nurbs;

import java.util.LinkedList;

public class Refinement {
	
	protected LinkedList<double[]>umbilcs;
	protected NURBSSurface ns;
	protected double[][] point;
	protected double hu;
	protected double hv;
	protected int depth;
	int indexI;
	int indexJ;
	double u1;
	double v1;
	int counter;
	
	public Refinement(){
		
	}
	
	public Refinement(LinkedList<double[]> umb,NURBSSurface n,double u, double v,double[][] p,double hhu, double hhv,int d, int i, int j,int c){
		umbilcs = umb;
		ns = n;
		u1 =u;
		v1 = v;
		point =p;
		hu = hhu;
		hv = hhv;
		depth = d;
		indexI = i;
		indexJ = j;
		counter = c;
	}

	public LinkedList<double[]> getUmbilcs() {
		return umbilcs;
	}

	public void setUmbilcs(LinkedList<double[]> umbilcs) {
		this.umbilcs = umbilcs;
	}

	public NURBSSurface getNs() {
		return ns;
	}

	public void setNs(NURBSSurface ns) {
		this.ns = ns;
	}

	public double[][] getPoint() {
		return point;
	}

	public void setPoint(double[][] point) {
		this.point = point;
	}

	public double getHu() {
		return hu;
	}

	public void setHu(double hu) {
		this.hu = hu;
	}

	public double getHv() {
		return hv;
	}

	public void setHv(double hv) {
		this.hv = hv;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getIndexI() {
		return indexI;
	}

	public void setIndexI(int indexI) {
		this.indexI = indexI;
	}

	public int getIndexJ() {
		return indexJ;
	}

	public void setIndexJ(int indexJ) {
		this.indexJ = indexJ;
	}

	public double getU1() {
		return u1;
	}

	public void setU1(double u1) {
		this.u1 = u1;
	}

	public double getV1() {
		return v1;
	}

	public void setV1(double v1) {
		this.v1 = v1;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	

}
