package de.varylab.varylab.utilities;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.varylab.opennurbs.ON;
import de.varylab.opennurbs.ONX_Model;
import de.varylab.opennurbs.ONX_Model_Object;
import de.varylab.opennurbs.ON_3dPoint;
import de.varylab.opennurbs.ON_3dPointArray;
import de.varylab.opennurbs.ON_4dPoint;
import de.varylab.opennurbs.ON_ArcCurve;
import de.varylab.opennurbs.ON_BinaryFile;
import de.varylab.opennurbs.ON_Brep;
import de.varylab.opennurbs.ON_Curve;
import de.varylab.opennurbs.ON_Geometry;
import de.varylab.opennurbs.ON_Interval;
import de.varylab.opennurbs.ON_LineCurve;
import de.varylab.opennurbs.ON_Mesh;
import de.varylab.opennurbs.ON_MeshFace;
import de.varylab.opennurbs.ON_NurbsCurve;
import de.varylab.opennurbs.ON_NurbsSurface;
import de.varylab.opennurbs.ON_Object;
import de.varylab.opennurbs.ON_PlaneSurface;
import de.varylab.opennurbs.ON_PolylineCurve;
import de.varylab.opennurbs.ON_Surface;
import de.varylab.opennurbs.OpenNurbsIO;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
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
						} else if(surfaces[i] instanceof ON_PlaneSurface) {
							ON_PlaneSurface ps = (ON_PlaneSurface) surfaces[i]; 
							ON_Interval uInt = ps.Extents(0);
							ON_Interval vInt = ps.Extents(1);
							double[][][] cm = new double[2][2][3];
							cm[0][0] = ps.PointAt(uInt.Min(), vInt.Min()).getCoordinates();
							cm[0][1] = ps.PointAt(uInt.Min(), vInt.Max()).getCoordinates();
							cm[1][0] = ps.PointAt(uInt.Max(), vInt.Min()).getCoordinates();
							cm[1][1] = ps.PointAt(uInt.Max(), vInt.Max()).getCoordinates();
							nsurfaces.add(new NURBSSurface(cm,1,1));
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

	public static void addPlaneSurface(ON_PlaneSurface ps, VHDS vhds, AdapterSet adapters) {
		ON_Interval uInt = ps.Extents(0);
		ON_Interval vInt = ps.Extents(1);
		VFace f = HalfEdgeUtils.addNGon(vhds, 4);
		VEdge e = f.getBoundaryEdge();
		VVertex v1 = e.getStartVertex();
		VVertex v2 = e.getTargetVertex();
		VVertex v3 = e.getNextEdge().getTargetVertex();
		VVertex v4 = e.getPreviousEdge().getStartVertex();
		adapters.set(Position.class, v1, ps.PointAt(uInt.Min(), vInt.Min()));
		adapters.set(Position.class, v2, ps.PointAt(uInt.Min(), vInt.Max()));
		adapters.set(Position.class, v3, ps.PointAt(uInt.Max(), vInt.Min()));
		adapters.set(Position.class, v4, ps.PointAt(uInt.Max(), vInt.Max()));
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

	public static List<IndexedFaceSet> getMeshes(ONX_Model model) {
		List<IndexedFaceSet> meshes = new LinkedList<>();
		for(ONX_Model_Object mo : model.get_object_table()) {
			ON_Object object = mo.get_object();
			if(object instanceof ON_Mesh) {
				ON_Mesh geom = (ON_Mesh)object;
				meshes.add(ONMeshtoIndexedFaceSet(geom));
					continue;
			}
		}
		return meshes;
	}

	private static IndexedFaceSet ONMeshtoIndexedFaceSet(ON_Mesh mesh) {
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		double[][] coords = ON_3dPointArrayToDoubleArrayArray(mesh.DoublePrecisionVertices());
		ifsf.setVertexCount(coords.length);
		ifsf.setVertexCoordinates(coords);
		int[][] faces = ON_MeshFacesToIntArrayArray(mesh.getF());
		ifsf.setFaceCount(faces.length);
		ifsf.setFaceIndices(faces);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}

	private static int[][] ON_MeshFacesToIntArrayArray(ON_MeshFace[] f) {
		int[][] faces = new int[f.length][];
		for (int i = 0; i < faces.length; i++) {
			faces[i] = ON_MeshFaceToIntArray(f[i]);
		}
		return faces;
	}

	private static int[] ON_MeshFaceToIntArray(ON_MeshFace onMeshFace) {
		int[] vi = onMeshFace.getVi();
		if(vi[2] == vi[3]) { //triangle
			return new int[] {vi[0], vi[1], vi[2]};
		} else { //quad
			return vi;
		}
	}

	private static double[][] ON_3dPointArrayToDoubleArrayArray(ON_3dPointArray onPoints) {
		double[][] points = new double[onPoints.Count()][];
		for (int i = 0; i < points.length; i++) {
			points[i] = ON_3dPointToDoubleArray(onPoints.At(i));
		}
		return points;
	}

	private static double[] ON_3dPointToDoubleArray(ON_3dPoint pt) {
		double[] coords = {pt.getX(), pt.getY(), pt.getZ(), 1.0};
		return coords;
	}
}
