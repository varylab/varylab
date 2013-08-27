package de.varylab.varylab.plugin.grasshopper.data;

import java.util.List;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.grasshopper.data.binding.Face;
import de.varylab.varylab.plugin.grasshopper.data.binding.FaceList;
import de.varylab.varylab.plugin.grasshopper.data.binding.RVLMesh;
import de.varylab.varylab.plugin.grasshopper.data.binding.Vertex;
import de.varylab.varylab.plugin.grasshopper.data.binding.VertexList;

public class RVLMeshUtility {

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
	
}
