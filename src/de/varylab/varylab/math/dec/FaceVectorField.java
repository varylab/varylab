package de.varylab.varylab.math.dec;

import java.util.HashMap;
import java.util.Map;

import de.jtem.halfedge.Face;

public class FaceVectorField {

	private Map<Face<?,?,?>,double[]>
		faceVectorMap = null;
		
	private String
		name = null;
		
	public FaceVectorField(String name, Map<Face<?,?,?>,double[]> vf) {
		this.name=name;
		faceVectorMap = new HashMap<Face<?,?,?>, double[]>(vf);
	}
	
	public double[] getVector(Face<?,?,?> f) {
		return faceVectorMap.get(f);
	}

	public String getName() {
		return name;
	}
	
}
