package de.varylab.varylab.utilities;

import java.util.LinkedList;
import java.util.List;

import de.varylab.opennurbs.ONX_Model;
import de.varylab.opennurbs.ONX_Model_Object;
import de.varylab.opennurbs.ON_4dPoint;
import de.varylab.opennurbs.ON_Brep;
import de.varylab.opennurbs.ON_Geometry;
import de.varylab.opennurbs.ON_NurbsSurface;
import de.varylab.opennurbs.ON_Object;
import de.varylab.opennurbs.ON_Surface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;

public class OpenNurbsUtility {

	public static List<NURBSSurface> getNurbsSurfaces(ONX_Model model) {
		List<NURBSSurface> nsurfaces = new LinkedList<NURBSSurface>();
		for(ONX_Model_Object mo : model.get_object_table()) {
			ON_Object object = mo.get_object();
			if(object.isKindOfON_Geometry()) {
				ON_Geometry geom = ON_Geometry.Cast(object);
				if(geom.hasBrepForm()) {
					ON_Brep brep = geom.brepForm(null);
					ON_Surface[] surfaces = brep.getS();
					for (int i = 0; i < surfaces.length; i++) {
						ON_NurbsSurface nsurf = ON_NurbsSurface.Cast(surfaces[i]);
						if(nsurf != null) {
							nsurfaces.add(ONtoVarylabNurbsSurface(nsurf));
						}
					}
				}
			}
		}
		return nsurfaces;
	}

	private static NURBSSurface ONtoVarylabNurbsSurface(ON_NurbsSurface nsurf) {
		
		double[][][] 
				cv = new double[nsurf.CVCount(0)][nsurf.CVCount(1)][4];

		ON_4dPoint pt = new ON_4dPoint();
		for (int j = 0; j < cv.length; j++) {
			for(int k = 0; k < cv[0].length; ++k) {
				nsurf.getCV(j,k,pt);
				cv[j][k] = pt.getCoordinates();
			}
		}

		// OpenNurbs uses less entries in the knot vector, so the first and last entry
		// need to be doubled.
		double[]
				uKnot = new double[nsurf.KnotCount(0)+2],
				vKnot = new double[nsurf.KnotCount(1)+2];

		for (int j = 0; j < uKnot.length; j++) {
			if(j == 0) {
				uKnot[j] = nsurf.Knot(0, j);
			} else if( j == uKnot.length-1) {
				uKnot[j] = nsurf.Knot(0, j-2);
			} else {
				uKnot[j] = nsurf.Knot(0, j-1);
			}
		}
		for (int j = 0; j < vKnot.length; j++) {
			if(j == 0) {
				vKnot[j] = nsurf.Knot(1, j);
			} else if( j == vKnot.length-1) {
				vKnot[j] = nsurf.Knot(1, j-2);
			} else {
				vKnot[j] = nsurf.Knot(1, j-1);
			}
		}
		return new NURBSSurface(uKnot, vKnot, cv, nsurf.Degree(0), nsurf.Degree(1));
	}

}
