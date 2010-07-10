package de.varylab.varylab.plugin.remeshing;

import geom3d.Point;

import java.util.LinkedList;

import de.jreality.math.Matrix;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.varylab.discreteconformal.heds.CoEdge;
import de.varylab.discreteconformal.heds.CoHDS;
import de.varylab.discreteconformal.heds.CoVertex;

public class TextureUtility {

	public static Point transformCoord(Point tex, Matrix M) {
		double[] tmp = {0,0,0,1};
		tmp[0] = tex.z() != 0 ? tex.x() / tex.z() : tex.x();
		tmp[1] = tex.z() != 0 ? tex.y() / tex.z() : tex.y();
		tmp[2] = 0;
		tmp[3] = tex.z() != 0 ? tex.z() : 1;
		double[] texCoord = M.multiplyVector(tmp);
		return new Point(texCoord[0] / texCoord[3], texCoord[1] / texCoord[3], 1.0);
	}
	
	public static double[] transformCoord(double[] tex, Matrix M) {
		Point texPt = new Point(tex);
		Point transformedTex = TextureUtility.transformCoord(texPt,M);
		return new double[]{transformedTex.x(),transformedTex.y(), transformedTex.z()};
	}

	public static LinkedList<CoVertex> findCorners(CoHDS hds) {
		LinkedList<CoVertex> corners = new LinkedList<CoVertex>();
		CoEdge be = HalfEdgeUtils.boundaryEdges(hds).iterator().next();
		be = findNextTextureCorner(be);
		CoEdge e = be;
		do {
			e = findNextTextureCorner(e);
			corners.add(e.getStartVertex());
		} while(e != be);
		return corners;
	}

	public static CoEdge findNextTextureCorner(CoEdge be) {
		double theta = 0.0;
		do {
			CoEdge pe = be.getOppositeEdge();
			be = be.getNextEdge();
			Point e1 = new geom3d.Point(be.getTargetVertex().getTextureCoord());
			Point e2 = new geom3d.Point(pe.getTargetVertex().getTextureCoord());
			e1.subtract(be.getStartVertex().getTextureCoord());
			e2.subtract(be.getStartVertex().getTextureCoord());
			theta = e1.getAngle(e2);
		} while(Math.abs(Math.PI - theta) < 1E-3);
		return be;
	}
	
}
