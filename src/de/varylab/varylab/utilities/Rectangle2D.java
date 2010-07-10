package de.varylab.varylab.utilities;

import java.util.Arrays;

public class Rectangle2D {

	double[] 
	       ll = new double[2],
	       ur = new double[2];
	
	public Rectangle2D(double[] v, double[] w) {
		for(int i = 0; i < v.length; ++i) {
			if(v[i] < w[i]) {
				ll[i] = v[i];
				ur[i] = w[i];
			} else {
				ll[i] = w[i];
				ur[i] = v[i];
			}
		}
	}
	
	public double getMinX() {
		return ll[0];
	}
	
	public double getMinY() {
		return ll[1];
	}

	public double getMaxX() {
		return ur[0];
	}

	public double getMaxY() {
		return ur[1];
	}

	public double getWidth() {
		return ur[0]-ll[0];
	}

	public double getHeight() {
		return ur[1]-ll[1];
	}
	
	@Override
	public String toString() {
		return "ll = " + Arrays.toString(ll) + "\n" +
		"ur = " + Arrays.toString(ur);
	}
}
