package de.varylab.varylab.ui;


public class DoubleArrayPrettyPrinter implements PrettyPrinter<double[]> {

	private int precision = 4;
	
	public DoubleArrayPrettyPrinter() {
	}
	
	public DoubleArrayPrettyPrinter(int precision) {
		this.precision = precision;
	}
	
	@Override
	public String toString(double[] t) {
		String str = new String();
		for (int i = 0; i < t.length; i++) {
			str += ((i!=0)?", ":"") + String.format("%." + precision + "f", t[i]);
		}
		return "("+str+")";
	}

}
