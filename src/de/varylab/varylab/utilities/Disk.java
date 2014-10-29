package de.varylab.varylab.utilities;

import static de.jreality.scene.data.Attribute.COORDINATES;
import static de.jreality.scene.data.Attribute.INDICES;
import static de.jreality.scene.data.StorageModel.DOUBLE3_ARRAY;
import static de.jreality.scene.data.StorageModel.DOUBLE_ARRAY_ARRAY;
import static de.jreality.scene.data.StorageModel.INT_ARRAY_ARRAY;
import static de.jreality.writer.u3d.U3DAttribute.U3D_FLAG;
import static de.jreality.writer.u3d.U3DAttribute.U3D_NONORMALS;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;


public class Disk extends IndexedFaceSet{

	
	private static final double[]
	    zPosNormal = {0,0,-1},
	    zNegNormal = {0,0,1};
	
	public Disk(int resolution, double thickness){
		super("Disk");
		makeDisk(resolution, thickness);
		setVertexAttributes(U3D_NONORMALS, U3D_FLAG);
	}
	
	public Disk(int resolution){
		super("Disk");
		makeDiskNoThickness(resolution);
		setVertexAttributes(U3D_NONORMALS, U3D_FLAG);
	}
	
	
	private void makeDisk(int resolution, double thickness){
		double[][] verts = new double[resolution*4][3];
		double alpha = 0;
		double delta = 2*PI / resolution;
		for (int i = 0; i < resolution; i++){
			verts[i][0] = cos(alpha);
			verts[i][1] = sin(alpha);
			verts[i][2] = -thickness / 2;

			verts[i + resolution][0] = cos(alpha);
			verts[i + resolution][1] = sin(alpha);
			verts[i + resolution][2] = -thickness / 2;

			verts[i + 2*resolution][0] = cos(alpha);
			verts[i + 2*resolution][1] = sin(alpha);
			verts[i + 2*resolution][2] = thickness / 2;

			verts[i + 3*resolution][0] = cos(alpha);
			verts[i + 3*resolution][1] = sin(alpha);
			verts[i + 3*resolution][2] = thickness / 2;
			
			alpha += delta;
		}
		
		int[][] indices = new int[resolution+2][];
		
		indices[0] = new int[resolution];
		for(int i = 0; i < indices[0].length; ++i) {
			indices[0][i] = i;
		}
		for (int i = 0; i < resolution; ++i) {
			indices[i+1] = new int[]{i+resolution, i+2*resolution, (i+1)%resolution + 2*resolution, (i+1)%resolution + resolution};
		}
		indices[resolution+1] = new int[resolution];
		for(int i = 0; i < indices[0].length; ++i) {
			indices[resolution+1][i] = 3*resolution+i;
		}
		
		alpha = 0;
		double[][] normals = new double[4*resolution][];
		for(int i = 0; i < resolution; ++i) {
			normals[i] = zNegNormal;
			normals[i+resolution] = new double[]{-cos(alpha), -sin(alpha), 0};
			normals[i+2*resolution] = new double[]{-cos(alpha), -sin(alpha), 0};
			normals[i+3*resolution] = zNegNormal;
			alpha += delta;
		}
		int[][] edges = new int[2][resolution+1];
		for(int i = 0; i < resolution+1; ++i) {
			edges[0][i] = i%resolution;
		}
		for(int i = 0; i < resolution+1; ++i) {
			edges[1][i] = i%resolution + 3*resolution;
		}

		setNumPoints(verts.length);
		setNumFaces(indices.length);
		setNumEdges(edges.length);
		
		DataList vList = DOUBLE3_ARRAY.createReadOnly(verts);
		setVertexCountAndAttributes(COORDINATES, vList);
		
		DataList iList = INT_ARRAY_ARRAY.createReadOnly(indices);
		setFaceCountAndAttributes(INDICES, iList);
		
		DataList nList = DOUBLE_ARRAY_ARRAY.createReadOnly(normals);
		setVertexAttributes(Attribute.NORMALS, nList);
		
		DataList eList = INT_ARRAY_ARRAY.createReadOnly(edges);
		setEdgeAttributes(Attribute.INDICES, eList);
		
		setName("Disk");
	}
	
	
	
	
	private void makeDiskNoThickness(int resolution){
		double[][] verts = new double[resolution + 1][3];
		double[][] normals = new double[resolution + 1][];
		double alpha = 0;
		double delta = 2*PI / resolution;
		for (double[] p : verts){
			p[0] = cos(alpha);
			p[1] = sin(alpha);
			alpha += delta;
		}
		for (int i = 0; i < normals.length; i++)
			normals[i] = zPosNormal;
		
		int[][] indices = new int[resolution][3];
		for (int i = 0; i < indices.length; i++) {
			int[] face = indices[i];
			face[0] = resolution;
			face[1] = i;
			face[2] = (i + 1) % resolution;
		}
		setNumPoints(verts.length);
		setNumFaces(indices.length);
		DataList vList = DOUBLE3_ARRAY.createReadOnly(verts);
		setVertexCountAndAttributes(COORDINATES, vList);
		DataList iList = INT_ARRAY_ARRAY.createReadOnly(indices);
		setFaceCountAndAttributes(INDICES, iList);
		DataList nList = DOUBLE_ARRAY_ARRAY.createReadOnly(normals);
		setVertexAttributes(Attribute.NORMALS, nList);
	}
	
	
}
