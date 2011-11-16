package de.varylab.varylab.plugin.nurbs.adapter;

import static de.varylab.varylab.plugin.nurbs.math.NURBSCurvatureUtility.curvatureAndDirections;
import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.PrincipalCurvatureMin;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NurbsUVCoordinate;
import de.varylab.varylab.plugin.nurbs.data.CurvatureInfo;

@PrincipalCurvatureMin
public class NurbsNormalAdapter extends AbstractTypedAdapter<VVertex, VEdge, VFace, double[]> {

	private NURBSSurface
		surface = null;
	
	public NurbsNormalAdapter(NURBSSurface s) {
		super(VVertex.class, null, null, double[].class, true, false);
		surface = s;
	}
	
	@Override
	public double getPriority() {
		return 100;
	}
	
	@Override
	public double[] getVertexValue(VVertex v, AdapterSet a) {
		double[] uv = a.getD(NurbsUVCoordinate.class, v);
		CurvatureInfo info = curvatureAndDirections(surface, uv[0], uv[1]);
		return info.getCurvatureDirectionsManifold()[0];
	}
	
}
