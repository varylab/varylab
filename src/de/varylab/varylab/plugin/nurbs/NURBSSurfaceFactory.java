package de.varylab.varylab.plugin.nurbs;

import java.util.HashMap;
import java.util.Map;

import de.jreality.geometry.QuadMeshFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jtem.halfedgetools.adapter.Adapter;
import de.varylab.varylab.plugin.nurbs.adapter.IndexedVectorField;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;
import de.varylab.varylab.plugin.nurbs.math.NURBSCurvatureUtility;

/**
 * A Factory for Bezier patches of arbitrary degree
 * @author Stefan Sechelmann
 *
 */
public class NURBSSurfaceFactory extends QuadMeshFactory{

	protected NURBSSurface
		surface = null;
	
	private Map<Integer, double[]> 
		indexUVMap = new HashMap<Integer,double[]>();

	private Map<Integer, double[]> 
		minCurvatureVFMap = new HashMap<Integer, double[]>(),
		maxCurvatureVFMap = new HashMap<Integer, double[]>();
	
	public void setSurface(NURBSSurface surface) {
		this.surface = surface;
	}
	
	@Override
	protected void updateImpl() {
		double[][] S = new double[getVLineCount()*getULineCount()][4];
		int p = surface.getUDegree();
		double uStart = surface.getUKnotVector()[p];
		double uEnd = surface.getUKnotVector()[surface.getUKnotVector().length - (p+1)];
		int q = surface.getVDegree();
		double vStart = surface.getVKnotVector()[q];
		double vEnd = surface.getVKnotVector()[surface.getVKnotVector().length - (q+1)];
		for(int j = 0; j < getVLineCount(); ++j) {
			for(int i = 0; i < getULineCount(); ++i) {
				double u = uStart + (i / (double)(getULineCount() - 1) * (uEnd - uStart));
				double v = vStart + (j / (double)(getVLineCount() - 1) * (vEnd - vStart));
				int index = i+j*getULineCount();
				surface.getSurfacePoint(u, v, S[index]);
				
				indexUVMap.put(index, new double[]{u,v});
				if(i == 0 || j == 0 || i == getULineCount()-1 || j == getVLineCount()-1) { // boundary of patch
					minCurvatureVFMap.put(index, new double[]{0.0,0.0,0.0});
					maxCurvatureVFMap.put(index, new double[]{0.0,0.0,0.0});
					continue;
				}
				double[] pt = {u, v};
				CurvatureInfo ci = NURBSCurvatureUtility.curvatureAndDirections(surface, pt);
				
				if(ci.getMinCurvature() == ci.getMaxCurvature()) { //umbillic point
					minCurvatureVFMap.put(index, new double[]{0.0,0.0,0.0});
					maxCurvatureVFMap.put(index, new double[]{0.0,0.0,0.0});
					continue;
				}
				minCurvatureVFMap.put(index, ci.getCurvatureDirections()[0]);
				maxCurvatureVFMap.put(index, ci.getCurvatureDirections()[1]);
				
			}
		}
		
		setVertexCoordinates(S);		
		super.updateImpl();
	}
	
	public NurbsUVAdapter getUVAdapter() {
		NurbsUVAdapter nurbsUVAdapter = new NurbsUVAdapter(surface,indexUVMap);
		return nurbsUVAdapter;
	}
	
	@Override
	public IndexedFaceSet getIndexedFaceSet() {
		return (IndexedFaceSet)getGeometry();
	}

	public Adapter<double[]> getMinCurvatureVectorField() {
		return new IndexedVectorField("MinCurvature",minCurvatureVFMap);
	}
	
	public Adapter<double[]> getMaxCurvatureVectorField() {
		return new IndexedVectorField("MaxCurvature",maxCurvatureVFMap);
	}
}