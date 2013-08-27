package de.varylab.varylab.plugin.nurbs.math;

import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurfaceFactory;

public class NurbsSurfaceUtility {

	public static void addNurbsMesh(NURBSSurface surf, HalfedgeLayer layer, int u, int v) {
		NURBSSurfaceFactory qmf = new NURBSSurfaceFactory();
		qmf.setGenerateVertexNormals(true);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateEdgesFromFaces(true);
		qmf.setULineCount(u);
		qmf.setVLineCount(v);
		qmf.setSurface(surf);
		qmf.update();
		layer.set(qmf.getGeometry());
		layer.addAdapter(qmf.getUVAdapter(), false);
//		layer.update();
	}

	
	public static double[] uniformKnotVector(int m, int deg) {
		double[] U = new double[m + deg + 1];
		int j = 0;
		for (int i = 0; i < U.length; i++) {
			if(i < deg+1) {
				U[i] = j;
				continue;
			} else if(i < m+1) {
				++j;
				U[i] = j;
			} else { // i >= p+m
				U[i] = j;
			}
		}
		return U;
	}

}
