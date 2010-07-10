package de.varylab.varylab.plugin.remeshing;

import geom3d.Point;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.varylab.discreteconformal.heds.CoVertex;
import de.varylab.varylab.hds.VVertex;

public class LatticePolygon2D {

	private List<VVertex> 
		verts = new LinkedList<VVertex>(),
		corners = new LinkedList<VVertex>();
	
	public LatticePolygon2D(List<VVertex> vertices, LinkedList<VVertex> corners) {
		verts = new LinkedList<VVertex>(vertices);
		this.corners = new LinkedList<VVertex>(corners);
	}
	
	public List<VVertex> getCorners() {
		return new LinkedList<VVertex>(corners);
	}
	
	public List<VVertex> getVertices() {
		return new LinkedList<VVertex>(verts);
	}

	public void snapTextureCorners(Collection<CoVertex> newCorners) {
		Iterator<VVertex> vvi = corners.iterator();
		Iterator<CoVertex> cvi = newCorners.iterator();
		while(vvi.hasNext() && cvi.hasNext()) {
			VVertex vv = vvi.next();
			CoVertex cv = cvi.next();
			vv.setPosition(new Point(cv.getTextureCoord()));
		}
	}
}
