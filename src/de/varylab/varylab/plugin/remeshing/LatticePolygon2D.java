package de.varylab.varylab.plugin.remeshing;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition3d;

public class LatticePolygon2D <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	HDS extends HalfEdgeDataStructure<V, E, F>
> {

	private List<V> 
		verts = new LinkedList<V>(),
		corners = new LinkedList<V>();
	
	public LatticePolygon2D(List<V> vertices, LinkedList<V> corners) {
		verts = new LinkedList<V>(vertices);
		this.corners = new LinkedList<V>(corners);
	}
	
	public List<V> getCorners() {
		return new LinkedList<V>(corners);
	}
	
	public List<V> getVertices() {
		return new LinkedList<V>(verts);
	}

	public void snapTextureCorners(Collection<V> newCorners, AdapterSet a) {
		Iterator<V> vvi = corners.iterator();
		Iterator<V> cvi = newCorners.iterator();
		while(vvi.hasNext() && cvi.hasNext()) {
			V vv = vvi.next();
			V cv = cvi.next();
			double[] t = a.getD(TexturePosition3d.class, cv);
			a.set(Position.class, vv, t);
		}
	}
}
