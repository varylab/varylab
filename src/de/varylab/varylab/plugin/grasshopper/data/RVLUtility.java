package de.varylab.varylab.plugin.grasshopper.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;
import de.jtem.halfedgetools.bsp.KdTree;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.halfedge.adapter.VPositionAdapter;
import de.varylab.varylab.plugin.grasshopper.data.binding.Face;
import de.varylab.varylab.plugin.grasshopper.data.binding.FaceList;
import de.varylab.varylab.plugin.grasshopper.data.binding.KnotList;
import de.varylab.varylab.plugin.grasshopper.data.binding.Line;
import de.varylab.varylab.plugin.grasshopper.data.binding.LineList;
import de.varylab.varylab.plugin.grasshopper.data.binding.RVLLineSet;
import de.varylab.varylab.plugin.grasshopper.data.binding.RVLMesh;
import de.varylab.varylab.plugin.grasshopper.data.binding.RVLSurface;
import de.varylab.varylab.plugin.grasshopper.data.binding.Vertex;
import de.varylab.varylab.plugin.grasshopper.data.binding.VertexList;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.PolygonalLine;
import de.varylab.varylab.utilities.NodeIndexComparator;

public class RVLUtility {

	public static IndexedFaceSet toIndexedFaceSet(RVLMesh mesh) {
		int numVerts = mesh.getVertices().getVertex().size();
		double[][] verts = new double[numVerts][];
		for (Vertex v : mesh.getVertices().getVertex()) {
			verts[v.getID()] = new double[] {v.getX(), v.getY(), v.getZ()};
		}
		int numFaces = mesh.getFaces().getFace().size();
		int[][] faces = new int[numFaces][];
		int faceId = 0;
		for (Face f : mesh.getFaces().getFace()) {
			if (f.isIsTriangle()) {
				faces[faceId++] = new int[]{f.getA(), f.getB(), f.getC()}; 
			} else {
				faces[faceId++] = new int[]{f.getA(), f.getB(), f.getC(), f.getD()};
			}
		}
		IndexedFaceSetFactory iff = new IndexedFaceSetFactory();
		iff.setVertexCount(numVerts);
		iff.setFaceCount(numFaces);
		iff.setVertexCoordinates(verts);
		iff.setFaceIndices(faces);
		iff.setGenerateFaceNormals(true);
		iff.setGenerateEdgesFromFaces(true);
		iff.update();
		return iff.getIndexedFaceSet();
	}
	
	public static <
		V extends de.jtem.halfedge.Vertex<V, E, F>,
		E extends de.jtem.halfedge.Edge<V,E,F>,
		F extends de.jtem.halfedge.Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> RVLMesh toRVLMesh(HDS hds, AdapterSet a, boolean useTextureCoords) {
		RVLMesh mesh = new RVLMesh();
		VertexList vList = new VertexList();
		FaceList fList = new FaceList();
		for (V v : hds.getVertices()) {
			double[] p = new double[3];
			if(useTextureCoords) {
				double[] tc = a.getD(TexturePosition2d.class, v);
				p[0] = tc[0];
				p[1] = tc[1];
			} else {
				p = a.getD(Position3d.class, v);
			}
			Vertex rv = new Vertex();
			rv.setID(v.getIndex());
			rv.setX(p[0]);
			rv.setY(p[1]);
			rv.setZ(p[2]);
			vList.getVertex().add(rv);
		}
		mesh.setVertices(vList);
		for(F f : hds.getFaces()) {
			Face rf = new Face();
			List<V> bv = HalfEdgeUtils.boundaryVertices(f);
			if (bv.size() == 3) {
				rf.setA(bv.get(0).getIndex());
				rf.setB(bv.get(1).getIndex());
				rf.setC(bv.get(2).getIndex());
				rf.setIsTriangle(true);
			} else 
			if (bv.size() == 4) {
				rf.setA(bv.get(0).getIndex());
				rf.setB(bv.get(1).getIndex());
				rf.setC(bv.get(2).getIndex());
				rf.setD(bv.get(3).getIndex());
				rf.setIsTriangle(false);
			}
			fList.getFace().add(rf);
		}
		mesh.setFaces(fList);
		return mesh;
	}
	
	public static RVLLineSet toRVLLineSet(VHDS hds, AdapterSet a) {
		RVLLineSet result = new RVLLineSet();
		result.setVertices(new VertexList());
		result.setLines(new LineList());
		int index = 0;
		for (VEdge e : hds.getPositiveEdges()) {
			VVertex vv1 = e.getStartVertex();
			VVertex vv2 = e.getTargetVertex();
			double[] p1 = a.getD(Position3d.class, vv1);
			double[] p2 = a.getD(Position3d.class, vv2);
			Vertex v1 = new Vertex();
			v1.setX(p1[0]);
			v1.setY(p1[1]);
			v1.setZ(p1[2]);
			v1.setID(index++);
			Vertex v2 = new Vertex();
			v2.setX(p2[0]);
			v2.setY(p2[1]);
			v2.setZ(p2[2]);
			v2.setID(index++);
			Line line = new Line();
			line.getInt().add(index - 2);
			line.getInt().add(index - 1);
			result.getVertices().getVertex().add(v1);
			result.getVertices().getVertex().add(v2);
			result.getLines().getLine().add(line);
		}
		return result;
	}
	
	public static RVLLineSet toRVLLineSet(List<PolygonalLine> uvCurves) {
		RVLLineSet result = new RVLLineSet();
		result.setVertices(new VertexList());
		result.setLines(new LineList());
		int index = 0;
		for(PolygonalLine pl : uvCurves) {
			boolean begin = true;
			Line line = new Line();
			for(LineSegment s : pl.getpLine()) {
				double[][] coords = s.getSegment();
				if(begin) {
					Vertex v1 = new Vertex();
					v1.setX(coords[0][0]);
					v1.setY(coords[0][1]);
					v1.setID(index);
					result.getVertices().getVertex().add(v1);
					
					line.getInt().add(index);
					++index;
					begin = false;
				}
				Vertex v2 = new Vertex();
				v2.setX(coords[1][0]);
				v2.setY(coords[1][1]);
				v2.setID(index);
				line.getInt().add(index);
				++index;
				result.getVertices().getVertex().add(v2);
			}
			result.getLines().getLine().add(line);
		}
		return result;
	}
	
	public static VHDS toHDS(RVLLineSet lineSet, boolean join) {
		VHDS hds = new VHDS();
		AdapterSet a = AdapterSet.createGenericAdapters();
		a.add(new VPositionAdapter());
		Map<Integer, VVertex> vMap = new HashMap<Integer, VVertex>();
		Map<VVertex, Integer> idMap = new HashMap<VVertex, Integer>();	
		for (Vertex v : lineSet.getVertices().getVertex()) {
			VVertex vv = hds.addNewVertex();
			double[] pos = {v.getX(), v.getY(), v.getZ()};
			a.set(Position.class, vv, pos);
			vMap.put(v.getID(), vv);
			idMap.put(vv, v.getID());
		}
		if (join) {
			// clean multiple vertices by range checks and grouping
			double EPS = 1E-5;
			KdTree<VVertex, VEdge, VFace> kd = new KdTree<VVertex, VEdge, VFace>(hds, a, 10, true);
			List<Set<VVertex>> vertexGroups = new ArrayList<Set<VVertex>>();
			Set<VVertex> grouped = new HashSet<VVertex>();
			for (VVertex v : hds.getVertices()) {
				if (grouped.contains(v)) continue;
				double[] pos = a.getD(Position3d.class, v);
				int counter = 1;
				Set<VVertex> group = new TreeSet<VVertex>(new NodeIndexComparator<VVertex>());
				vertexGroups.add(group);
				grouped.add(v);
				group.add(v);
				boolean allAtSameLocation = true;
				while (allAtSameLocation) {
					allAtSameLocation = true;
					Collection<VVertex> nearest = kd.collectKNearest(pos, counter++);
					for (VVertex vn : nearest) {
						double[] posNear = a.getD(Position3d.class, vn);
						double dist = Rn.euclideanDistance(pos, posNear);
						if (dist < EPS) {
							group.add(vn);
							grouped.add(vn);
						} else {
							allAtSameLocation = false;
						}
					}
					if (!allAtSameLocation || group.size() == hds.numVertices()) {
						break;
					}
				}
			}
			for (Set<VVertex> group : vertexGroups) {
				VVertex ref = group.iterator().next();
				for (VVertex v : group) {
					int id = idMap.get(v);
					vMap.put(id, ref);
					if (v != ref) {
						hds.removeVertex(v);
					}
				}
			}
		}

		for (Line l : lineSet.getLines().getLine()) {
			VVertex previous = null;
			for(Integer i : l.getInt()) {
				if(previous == null) {
					VVertex v1 = vMap.get(i);
					previous = v1;
				} else {
					VVertex v = vMap.get(i);
					VEdge e1 = hds.addNewEdge();
					VEdge e2 = hds.addNewEdge();
					e1.linkNextEdge(e2);
					e1.linkPreviousEdge(e2);
					e1.linkOppositeEdge(e2);
					e1.setTargetVertex(previous);
					e2.setTargetVertex(v);
					previous = v;
				}
			}
		}
		return hds;
	}

	public static NURBSSurface toNurbsSurface(RVLSurface surface) {
		double[][][] cm = new double[surface.getUCount()][surface.getVCount()][3];
		for(int u = 0; u < surface.getUCount(); ++u) {
			for(int v = 0; v < surface.getVCount(); ++v) {
				Vertex vert = surface.getControlPoints().getVertex().get(u*surface.getVCount()+v);
				cm[u][v] = new double[]{vert.getX(), vert.getY(), vert.getZ(), 1.0};
			}
		}
		
		double[] U = toDoubleArray(surface.getUVector());
		double[] V = toDoubleArray(surface.getVVector());
		return new NURBSSurface(U, V, cm, surface.getUDegree(), surface.getVDegree());
	}

	private static double[] toDoubleArray(KnotList knotList) {
		int i;
		double[] V = new double[knotList.getDouble().size()+2];
		i = 0;
		for(double d : knotList.getDouble()) {
			if(i == 0) {
				V[i++] = d;
			}
			V[i++] = d;
			if(i == V.length-1) {
				V[i++] = d;
			}
		}
		return V;
	}
	
	
	
}
