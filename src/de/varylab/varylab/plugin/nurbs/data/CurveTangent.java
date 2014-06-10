package de.varylab.varylab.plugin.nurbs.data;

public class CurveTangent {

		private double[] tangent = null;
		private int curveIndex;
		
		public CurveTangent(){			
		}
		
		public CurveTangent(double[] tang, int ci){	
			tangent = tang;
			curveIndex = ci;
		}
		
		public double[] getTangent(){
			return tangent;
		}
		
		public void setTangent(double[] tang){
			tangent = tang;
		}
		
		public int getCurveIndex(){
			return curveIndex;
		}
		
		public void setCurveIndex(int ci){
			curveIndex = ci;
		}
	

}
