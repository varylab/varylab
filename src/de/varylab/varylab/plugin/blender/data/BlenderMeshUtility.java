package de.varylab.varylab.plugin.blender.data;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.VVertex;
import de.varylab.varylab.plugin.blender.data.binding.BlenderMesh;
import de.varylab.varylab.plugin.blender.data.binding.Face;
import de.varylab.varylab.plugin.blender.data.binding.FaceList;
import de.varylab.varylab.plugin.blender.data.binding.Vertex;
import de.varylab.varylab.plugin.blender.data.binding.VertexList;

public class BlenderMeshUtility {

	public static IndexedFaceSet toIndexedFaceSet(BlenderMesh mesh) {
		int numVerts = mesh.getVertices().getVertex().size();
		double[][] verts = new double[numVerts][];
		for (Vertex v : mesh.getVertices().getVertex()) {
			verts[v.getID()] = new double[] {v.getX(), v.getY(), v.getZ()};
		}
		int numFaces = mesh.getFaces().getFace().size();
		int[][] faces = new int[numFaces][];
		int faceId = 0;
		for (Face f : mesh.getFaces().getFace()) {
			faces[faceId] = new int[f.getVertices().size()];
			int j = 0;
			for(int i : f.getVertices()) {
				faces[faceId][j++] = i; 
			}
			faceId++;
		}
		IndexedFaceSetFactory iff = new IndexedFaceSetFactory();
		iff.setVertexCount(numVerts);
		iff.setFaceCount(numFaces);
		iff.setVertexCoordinates(verts);
		iff.setFaceIndices(faces);
		iff.update();
		return iff.getIndexedFaceSet();
	}
	
	public static BlenderMesh toBlenderMesh(VHDS hds, AdapterSet a) {
		BlenderMesh mesh = new BlenderMesh();
		VertexList vList = new VertexList();
		FaceList fList = new FaceList();
		for (VVertex v : hds.getVertices()) {
			double[] p = a.getD(Position3d.class, v);
			Vertex bv = new Vertex();
			bv.setID(v.getIndex());
			bv.setX(p[0]);
			bv.setY(p[1]);
			bv.setZ(p[2]);
			vList.getVertex().add(bv);
		}
		mesh.setVertices(vList);
		for(VFace f : hds.getFaces()) {
			Face bf = new Face();
			for(VVertex v : HalfEdgeUtils.boundaryVertices(f)) {
				bf.getVertices().add(v.getIndex());
			}
			fList.getFace().add(bf);
		}
		mesh.setFaces(fList);
		return mesh;
	}
	
}
