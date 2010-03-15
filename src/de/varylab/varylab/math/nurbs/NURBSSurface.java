package de.varylab.varylab.math.nurbs;


public class NURBSSurface {

	protected double[][][] 
 	    controlMesh =
 	    	{{{0,4,2,1},	{1,4,0,1},	{2,4,-1,1}},//	{3,4,-2,1}	},
			{ {0,3,0,1},	{1,3,0,1},	{2,3,0,1}},//	{3,3,0,1}	},
			{ {0,2,0,1},	{1,2,0,1},	{2,2,1,1}}};//	{3,2,0,1}	}};
			//{ {0,1,0,1},	{1,1,-1,1},	{2,1,0,1},	{3,1,0,1}	}};
			//{ {0,0,0,1},	{1,0,0,1},	{2,0,0,1},	{3,0,0,1}	}};
 	protected double[]
 	    U = {0,0,0,1/3.0,2/3.0,1,1,1},
 	    V = {0,0,0,0.5,1,1,1};
 	protected int
 		p = 2,
 		q = 2;
	
 	
 	public NURBSSurface() {
 	}
 	
 	
	public void getSurfacePoint(double u, double v, double[] S) {
		NURBSAlgorithm.SurfacePoint(p, U, q, V, controlMesh, u, v, S);
	}
	
	
	public double[][][] getControlMesh() {
		return controlMesh;
	}
	
	public double[] getUKnotVector() {
		return U;
	}
	
	public double[] getVKnotVector() {
		return V;
	}
	
	public int getUDegree() {
		return p;
	}
	
	public int getVDegree() {
		return q;
	}

	public void setUKnotVector(double[] u) {
		U = u;
	}

	public void setVKnotVector(double[] v) {
		V = v;
	}

	public void setUDegree(int p) {
		this.p = p;
	}

	public void setVDegree(int q) {
		this.q = q;
	}
	
	/**
	 * Use homogeneous coordinates. The fourth coordinates will be the weights of the 
	 * control points
	 * @param cm
	 */
	public void setControlMesh(double[][][] cm) {
		this.controlMesh = cm;
		setDefaultKnots();
	}
	
	
	public void setSurfaceData(double[][][] cm, double[] U, double[] V, int p, int q) {
		this.controlMesh = cm;
		this.U = U;
		this.V = V;
		this.p = p;
		this.q = q;
	}
	
	public void setDefaultKnots() {
		int n = controlMesh.length;
		int m = controlMesh[0].length;
		U = new double[2 * (p+1) + n - p - 1];
		V = new double[2 * (q+1) + m - q - 1];
		for (int i = 0; i < (p+1); i++) {
			U[i] = 0.0;
			U[U.length - i - 1] = 1.0;
		}
		for (int i = 0; i < n - p - 1; i++) {
			U[p + 1 + i] = (i+1) / (double)(n - p);
		}
		for (int i = 0; i < (q+1); i++) {
			V[i] = 0.0;
			V[V.length - i - 1] = 1.0;
		}
		for (int i = 0; i < m - q - 1; i++) {
			V[q + 1 + i] = (i+1) / (double)(m - q);
		}
	}
	
	public int getNumUPoints() {
		return controlMesh.length;
	}
	
	public int getNumVPoints() {
		return controlMesh[0].length;
	}
	
	
}
