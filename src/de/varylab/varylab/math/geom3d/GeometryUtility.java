package de.varylab.varylab.math.geom3d;

import java.util.LinkedList;
import java.util.List;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;

public final class GeometryUtility {

	public static IndexedLineSet createVectorSet(List<Vector> vecList, List<Point> posList, double scale) {
		IndexedLineSetFactory ilf = new IndexedLineSetFactory();
		if (vecList.size() == 0) {
			ilf.update();
			return ilf.getIndexedLineSet();
		}
		ilf.setVertexCount(vecList.size() * 2);
		ilf.setEdgeCount(vecList.size());
		
		double[][] vData = new double[posList.size() * 2][];
		int[][] iData = new int[posList.size()][2];
		for (int i = 0; i < vData.length; i += 2) {
			Vector vec = new Vector(vecList.get(i / 2));
			vec.scaleTo(scale);
			Point start = new Point(posList.get(i / 2)).add(vec).asPoint();
			vec.times(-1);
			Point end = new Point(posList.get(i / 2)).add(vec).asPoint(); 
			vData[i] = start.get();
			vData[i + 1] = end.get();
			iData[i / 2][0] = i;
			iData[i / 2][1] = i + 1;
		}
		ilf.setVertexCoordinates(vData);
		ilf.setEdgeIndices(iData);
		ilf.update();
		return ilf.getIndexedLineSet();
	}
	
	
	public static IndexedFaceSet createQuad(Point a, Point b, Point c, Point d){
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(4);
		ifsf.setFaceCount(1);
		double[][] vertex = new double[][]{a.get(), b.get(), c.get(), d.get()};
		int[][] index = new int[][]{{0, 1, 2, 3}};
		ifsf.setVertexCoordinates(vertex);
		ifsf.setFaceIndices(index);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}
	
	public static IndexedFaceSet createQuads(LinkedList<Quad> quads){
		int number = quads.size();
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(number*4);
		ifsf.setFaceCount(number);
		double[][]vertices = new double[number*4][];
		for (int i = 0; i < vertices.length; i+=4) {
			vertices[i]=quads.get(i /4).getA().get();
			vertices[i+1]=quads.get(i /4).getB().get();
			vertices[i+2]=quads.get(i / 4).getC().get();
			vertices[i+3]=quads.get(i / 4).getD().get();
		}
		int[][] index = new int[number][4];
		int j = 0;
		for (int i = 0; i < index.length; i++) {
			
			index[i][0] = j;
			index[i][1] = j+1;
			index[i][2] = j+2;
			index[i][3] = j+3;
			j+=4;
		}
		if (number == 0) {
			ifsf.update();
			return ifsf.getIndexedFaceSet();
		}
		ifsf.setVertexCoordinates(vertices);
		ifsf.setFaceIndices(index);
		ifsf.setGenerateFaceNormals(true);
		ifsf.update();
		return ifsf.getIndexedFaceSet();
	}
	
	
	
}
