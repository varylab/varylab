package de.varylab.varylab.plugin.grasshopper.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.bsp.KdTree;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.halfedge.adapter.VPositionAdapter;
import de.varylab.varylab.plugin.grasshopper.data.binding.Face;
import de.varylab.varylab.plugin.grasshopper.data.binding.FaceList;
import de.varylab.varylab.plugin.grasshopper.data.binding.Line;
import de.varylab.varylab.plugin.grasshopper.data.binding.LineList;
import de.varylab.varylab.plugin.grasshopper.data.binding.RVLLineSet;
import de.varylab.varylab.plugin.grasshopper.data.binding.RVLMesh;
import de.varylab.varylab.plugin.grasshopper.data.binding.Vertex;
import de.varylab.varylab.plugin.grasshopper.data.binding.VertexList;

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
		iff.update();
		return iff.getIndexedFaceSet();
	}
	
	public static RVLMesh toRVLMesh(VHDS hds, AdapterSet a) {
		RVLMesh mesh = new RVLMesh();
		VertexList vList = new VertexList();
		FaceList fList = new FaceList();
		for (VVertex v : hds.getVertices()) {
			double[] p = a.getD(Position3d.class, v);
			Vertex rv = new Vertex();
			rv.setID(v.getIndex());
			rv.setX(p[0]);
			rv.setY(p[1]);
			rv.setZ(p[2]);
			vList.getVertex().add(rv);
		}
		mesh.setVertices(vList);
		for(VFace f : hds.getFaces()) {
			Face rf = new Face();
			List<VVertex> bv = HalfEdgeUtils.boundaryVertices(f);
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
			line.setA(index - 2);
			line.setB(index - 1);
			result.getVertices().getVertex().add(v1);
			result.getVertices().getVertex().add(v2);
			result.getLines().getLine().add(line);
		}
		return result;
	}
	
	
	public static VHDS toHDS(RVLLineSet lineSet) {
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
		
		// clean multiple vertices by range checks and grouping
		double EPS = 1E-5;
		KdTree<VVertex, VEdge, VFace> kd = new KdTree<VVertex, VEdge, VFace>(hds, a, 10, true);
		List<Set<VVertex>> vertexGroups = new ArrayList<Set<VVertex>>();
		Set<VVertex> grouped = new HashSet<VVertex>();
		for (VVertex v : hds.getVertices()) {
			if (grouped.contains(v)) continue;
			double[] pos = a.getD(Position3d.class, v);
			int counter = 1;
			Set<VVertex> group = new HashSet<VVertex>();
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

		for (Line l : lineSet.getLines().getLine()) {
			VVertex v1 = vMap.get(l.getA());
			VVertex v2 = vMap.get(l.getB());
			VEdge e1 = hds.addNewEdge();
			VEdge e2 = hds.addNewEdge();
			e1.linkNextEdge(e2);
			e1.linkPreviousEdge(e2);
			e1.linkOppositeEdge(e2);
			e1.setTargetVertex(v1);
			e2.setTargetVertex(v2);
		}
		return hds;
	}
	
	
	
}
