package de.varylab.varylab.optimization;

public class ProtocolValue {

	private double
		value = 0.0;
	private String
		name = "";
	private long
		seriesId = -1;
	
	public ProtocolValue(double value, String name, long seriesId) {
		super();
		this.value = value;
		this.name = name;
		this.seriesId = seriesId;
	}

	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSeriesId() {
		return seriesId;
	}
	
}
