package de.varylab.varylab.plugin.hyperbolicnets;

import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;

/**
 * A Factory for Bezier patches of arbitrary degree
 * @author Stefan Sechelmann
 *
 */
public class HyperboloidPatchFactory extends QuadMeshFactory{

	double weight = 1.0;
	
	private double[][][]
			quad = new double[2][2][4];
	
	public void setQuad(double[][][] quad) {
		for(int i = 0; i < 2; ++i) {
			for(int j = 0; j < 2; ++j) {
				System.arraycopy(quad[i][j], 0, this.quad[i][j], 0, 4);
			}
		}
	}
	
	public void setNLines(int n) {
		setULineCount(n);
		setVLineCount(n);
	}
	
	@Override
	protected void updateImpl() {
		double[][] S = new double[getVLineCount()*getULineCount()][4];
		for (int i = 0; i < getULineCount(); i++) {
			//insert bottom and top row of coordinates
			Rn.linearCombination(S[i], 
					(double)i/(getULineCount()-1), quad[1][0], 
					(double)(getULineCount()-1-i)/(getULineCount()-1), quad[0][0]);
			Rn.linearCombination(S[getULineCount()*(getVLineCount()-1)+i], 
					(double)i/(getULineCount()-1), quad[1][1], 
					(double)(getULineCount()-1-i)/(getULineCount()-1), quad[0][1]);
		}
		for(int i = 0; i < getULineCount(); i++) {
			for (int j = 1; j < getVLineCount()-1; j++) {
				Rn.linearCombination(S[j*getULineCount()+i], 
						(double)(getVLineCount()-1-j)/(getVLineCount()-1), S[i], 
						(double)j/(getVLineCount()-1), S[getULineCount()*(getVLineCount()-1)+i]);
			}
		}
		setVertexCoordinates(S);		
		super.updateImpl();
	}
	
	@Override
	public IndexedFaceSet getIndexedFaceSet() {
		return (IndexedFaceSet)getGeometry();
	}

	public static void main(String[] args) {
		HyperboloidPatchFactory hpf = new HyperboloidPatchFactory();
		hpf.setGenerateVertexNormals(true);
		hpf.setGenerateFaceNormals(true);
		hpf.setGenerateEdgesFromFaces(true);
		hpf.setNLines(5);

		double[][][] quadVerts = new double[][][]{
				{{0.0,0.0,0.0,1.0},{1.0,0.0,0.0,1.0}},
				{{0.0,1.0,0.0,1.0},{1.0,1.0,0.0,1.0}}};
		hpf.setQuad(quadVerts);
		hpf.setWeight(3);
		hpf.update();
	}

	public void setWeight(double w) {
		this.weight = w;
		double actWeight = quad[0][0][3]/quad[1][0][3]*quad[1][1][3]/quad[0][1][3];
		Rn.times(quad[0][0], w/actWeight, quad[0][0]);
	}
}