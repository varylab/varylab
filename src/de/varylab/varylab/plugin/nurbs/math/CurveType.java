package de.varylab.varylab.plugin.nurbs.math;

public enum CurveType{
	ASYMPTOTIC("Asymptotic"),
	CONJUGATE("Conjugate"), 
	CURVATURE("Curvature");
	
	private String displayName = ""; 
    private CurveType(String name) { this.displayName = name; }
    @Override
	public String toString() {
    	return displayName;
    }
}