package de.varylab.varylab.plugin.nurbs.data;

public class SignedUV {
	
	private double[] uv;
	private double sign = 1.0;

	public SignedUV(double[] p, double sign) {
		uv = p;
		this.sign = sign;
	}

	public double[] getPoint() {
		return uv;
	}

	public double getSign() {
		return sign;
	}
	

}
