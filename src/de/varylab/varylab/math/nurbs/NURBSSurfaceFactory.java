package de.varylab.varylab.math.nurbs;

import de.jreality.geometry.QuadMeshFactory;
import de.jreality.scene.IndexedFaceSet;

/**
 * A Factory for Bezier patches of arbitrary degree
 * @author Stefan Sechelmann
 *
 */
public class NURBSSurfaceFactory extends QuadMeshFactory{

	protected NURBSSurface
		surface = null;
	
	public NURBSSurfaceFactory() {

	}

	public void setSurface(NURBSSurface surface) {
		this.surface = surface;
	}
	
	@Override
	protected void updateImpl() {
		double[][] S = new double[getULineCount() * getVLineCount()][4];
		for (int k = 0; k < getULineCount() * getULineCount(); k++) {
			int i = k / getVLineCount();
			int j = k % getVLineCount();
			double u = i / (double)(getULineCount() - 1);
			double v = j / (double)(getVLineCount() - 1);
			surface.getSurfacePoint(u, v, S[k]);
		}
		setVertexCoordinates(S);
		super.updateImpl();
	}
	
	
	public IndexedFaceSet getIndexedFaceSet() {
		return (IndexedFaceSet)getGeometry();
	}
	
	
}