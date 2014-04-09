package de.varylab.varylab.utilities;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.varylab.opennurbs.ON;
import de.varylab.opennurbs.ONX_Model;
import de.varylab.opennurbs.ONX_Model_Object;
import de.varylab.opennurbs.ON_3dPoint;
import de.varylab.opennurbs.ON_4dPoint;
import de.varylab.opennurbs.ON_ArcCurve;
import de.varylab.opennurbs.ON_BinaryFile;
import de.varylab.opennurbs.ON_Brep;
import de.varylab.opennurbs.ON_Curve;
import de.varylab.opennurbs.ON_Geometry;
import de.varylab.opennurbs.ON_Interval;
import de.varylab.opennurbs.ON_LineCurve;
import de.varylab.opennurbs.ON_NurbsCurve;
import de.varylab.opennurbs.ON_NurbsSurface;
import de.varylab.opennurbs.ON_Object;
import de.varylab.opennurbs.ON_PolylineCurve;
import de.varylab.opennurbs.ON_Surface;
import de.varylab.opennurbs.OpenNurbsIO;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;

public class OpenNurbsUtility {

	public static List<NURBSSurface> getNurbsSurfaces(ONX_Model model) {
		List<NURBSSurface> nsurfaces = new LinkedList<NURBSSurface>();
		for(ONX_Model_Object mo : model.get_object_table()) {
			ON_Object object = mo.get_object();
			if(object instanceof ON_Geometry) {
				ON_Geometry geom = (ON_Geometry)object;
				if(geom instanceof ON_NurbsSurface) {
					nsurfaces.add(ONtoVarylabNurbsSurface((ON_NurbsSurface)geom));
					continue;
				}
				if(geom.hasBrepForm()) {
					ON_Brep brep = geom.brepForm(null);
					ON_Surface[] surfaces = brep.getS();
					for (int i = 0; i < surfaces.length; i++) {
						if(surfaces[i] instanceof ON_NurbsSurface) {
							ON_NurbsSurface nsurf = (ON_NurbsSurface)surfaces[i];
							nsurfaces.add(ONtoVarylabNurbsSurface(nsurf));
						} else if(surfaces[i].HasNurbForm()){
							ON_NurbsSurface nsurf = new ON_NurbsSurface(-1L);
							surfaces[i].GetNurbForm(nsurf);
							nsurfaces.add(ONtoVarylabNurbsSurface(nsurf));
						} else {
							System.err.println("ON_Surface of type " + surfaces[i].getObjectType() + " not yet implemented.");
						}
					}
				}
			}
		}
		return nsurfaces;
	}
	
	public static List<ON_Curve> getCurves(ONX_Model model) {
		List<ON_Curve> curves = new LinkedList<ON_Curve>();
		for(ONX_Model_Object mo : model.get_object_table()) {
			ON_Object object = mo.get_object();
			if(object instanceof ON_Curve) {
				ON_Curve curve = (ON_Curve)object;
				if(curve instanceof ON_PolylineCurve) {
//					System.out.println(curve);
					curves.add(curve);
				} else if(curve instanceof ON_ArcCurve) {
//					System.out.println(curve);
					curves.add(curve);
				} else if(curve instanceof ON_LineCurve){
					curves.add(curve);
				} else if(curve instanceof ON_NurbsCurve){
					curves.add(curve);
				} else {
					System.err.println("No wrapper for " + curve.getObjectType() + " yet.");
				}

			}
		}
		return curves;
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

	public static void addPolylineCurve(
			ON_PolylineCurve plc, 
			VHDS vhds,
			AdapterSet adapters) {
//		ON_Polyline pline = plc.get_pline();
		double[] domain  = plc.get_t();
		List<VVertex> verts = new LinkedList<VVertex>();
		for(int i = 0; i < plc.PointCount(); ++i) {
			VVertex v = vhds.addNewVertex();
			verts.add(v);
			ON_3dPoint pt = new ON_3dPoint(-1L);
			plc.evPoint(domain[i], pt);
			adapters.set(Position.class, v, pt.getCoordinates());
		}
		addPolygon(vhds, verts);
	}
	
	public static void addLineCurve(
			ON_LineCurve lc, 
			VHDS vhds,
			AdapterSet adapters) {
		double min = lc.Domain().Min();
		double max = lc.Domain().Max();
		List<VVertex> verts = new LinkedList<VVertex>();
		VVertex start = vhds.addNewVertex();
		verts.add(start);
		ON_3dPoint pt = new ON_3dPoint(-1L);
		lc.evPoint(min, pt);
		adapters.set(Position.class, start, pt.getCoordinates());
		VVertex target = vhds.addNewVertex();
		verts.add(target);
		lc.evPoint(max, pt);
		adapters.set(Position.class, target, pt.getCoordinates());
		addPolygon(vhds, verts);
	}

	private static void addPolygon(VHDS vhds, List<VVertex> verts) {
		Iterator<VVertex> it = verts.iterator();
		VVertex v1 = it.next();
		VVertex v2 = v1;
		while(it.hasNext()) {
			v1 = v2;
			v2 = it.next();
			VEdge
				e1 = vhds.addNewEdge(),
				e2 = vhds.addNewEdge();
			e1.setIsPositive(true);
			e1.linkOppositeEdge(e2);
			e1.linkNextEdge(e2);
			e2.linkNextEdge(e1);
			e1.setTargetVertex(v1);
			e2.setTargetVertex(v2);
		}
	}

	public static void addArcCurve(ON_ArcCurve ac, VHDS vhds, AdapterSet adapters) {
		ON_Interval domain  = ac.Domain();
		List<VVertex> verts = new LinkedList<VVertex>();
		int n = 40;
		double step = (domain.Max()-domain.Min())/n;
		for(int i = 0; i <= n; ++i) {
			VVertex v = vhds.addNewVertex();
			verts.add(v);
			ON_3dPoint pt = new ON_3dPoint(-1L);
			ac.evPoint(domain.Min() + i*step , pt);
			adapters.set(Position.class, v, pt.getCoordinates());
		}
		addPolygon(vhds, verts);
	}

	public static void addNurbsCurve(ON_NurbsCurve nc, VHDS vhds, AdapterSet adapters) {
		ON_Interval domain  = nc.Domain();
		List<VVertex> verts = new LinkedList<VVertex>();
		int n = 40;
		double step = (domain.Max()-domain.Min())/n;
		for(int i = 0; i <= n; ++i) {
			VVertex v = vhds.addNewVertex();
			verts.add(v);
			ON_3dPoint pt = new ON_3dPoint(-1L);
			nc.evPoint(domain.Min() + i*step , pt);
			adapters.set(Position.class, v, pt.getCoordinates());
		}
		addPolygon(vhds, verts);
	}

	public static void write(NURBSSurface surf, File file) {
		ON_NurbsSurface on_surface = new ON_NurbsSurface(3, true, surf.getUDegree()+1, surf.getVDegree() + 1, surf.getNumUPoints(), surf.getNumVPoints());
		double[][][] cv = surf.getControlMesh();
		ON_4dPoint pt = new ON_4dPoint();
		for (int i = 0; i < surf.getNumUPoints(); i++) {
			for (int j = 0; j < surf.getNumVPoints(); j++) {
				pt.init(cv[i][j]);
				on_surface.setCV(i, j, pt);
			}
		}
		double[] uKnot = surf.getUKnotVector();
		double[] vKnot = surf.getVKnotVector();
		for (int i = 1; i < uKnot.length-1; i++) {
			on_surface.SetKnot(0, i-1, uKnot[i]);
		}
		for (int i = 1; i < vKnot.length-1; i++) {
			on_surface.SetKnot(1, i-1, vKnot[i]);
		}
		on_surface.CVCount(0);
		on_surface.Order(0);
		try {
			ON_BinaryFile bfile = new ON_BinaryFile(-1L);
			bfile.init(ON.ArchiveMode.WRITE3DM, file.getPath());
			OpenNurbsIO.ON_WriteOneObjectArchive(bfile, 5, on_surface);
		} catch (Exception e1) {
			System.err.println("Could not write to file " + file);
			e1.printStackTrace();
		}
	}
}
