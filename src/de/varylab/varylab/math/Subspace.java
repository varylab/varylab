package de.varylab.varylab.math;

import de.jreality.math.Rn;

public class Subspace {

	private int 
		dim = 0;
	
	private double[]
	    svector = new double[]{0,0,0};
	
	public Subspace(double[][] vectors) {
		if(vectors == null) {
			return;
		}
		dim = vectors.length;
		if(dim == 2) {
			Rn.crossProduct(svector, vectors[0], vectors[1]); 
		} else {
			Rn.normalize(svector, vectors[0]);
		}
	}
	
	public double[] projectOnto(double[] v) {
		double[] vp = new double[3];
		if(dim >= 2) {
			Rn.projectOntoComplement(vp, v, svector);
		} else { // dim <= 1
			Rn.projectOnto(vp, v, svector);
		}
		return vp;
	}
	
}
