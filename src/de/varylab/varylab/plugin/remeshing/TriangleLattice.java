package de.varylab.varylab.plugin.remeshing;

import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;

public class TriangleLattice <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	HDS extends HalfEdgeDataStructure<V, E, F>
> extends Lattice<V, E, F, HDS> {

	private AdapterSet
		a = null;
	private double 
		ystep = 0.5,
		xstep = 1.0/6.0;
	
	public TriangleLattice(HDS template, AdapterSet a, Rectangle2D bbox) {
		super(template, new double[]{1.0/3.0,0.0}, new double[]{1.0/6.0,0.5},bbox);
		this.a = a;
		double xSpan = bbox.getWidth();
		double ySpan = bbox.getHeight();
		yRes = (int)Math.ceil(ySpan / ystep);
		xRes = (int)Math.ceil(xSpan / xstep)+(yRes-1)/2;
		ll = new double[]{bbox.getMinX() - ((yRes-1)/2)*2*xstep, bbox.getMinY()};
		
		compass = new Compass(
				new Slope[]{new Slope(2,0), new Slope(3,3), new Slope(1,3), new Slope(0,6),new Slope(-1,3), new Slope(-3,3)},
				new Slope[]{new Slope(1,0), new Slope(1,1), new Slope(0,1), new Slope(-1,2), new Slope(-1,1), new Slope(-2,1)});
		
		for (int i = 0; i < xRes; i++) {
			for (int j = 0; j < yRes; j++) {
				V v = lhds.addNewVertex();
				double xPos = ll[0] + (2*i+j)*xstep ;
				double yPos = ll[1] + j * ystep;
				double[] p = new double[]{xPos, yPos, 0.0};
				double[] t = new double[]{xPos, yPos, 0.0};
				a.set(Position.class, v, p);
				a.set(TexturePosition.class, v, t);
			}
		}
		for (int i = 0; i < xRes  - 1; i++) {
			for (int j = 0; j < yRes - 1; j++) {
				V v1 = lhds.getVertex(i*yRes + j); 
				V v2 = lhds.getVertex((i+1)*yRes + j); 
				V v3 = lhds.getVertex((i+1)*yRes + j + 1); 
				V v4 = lhds.getVertex(i*yRes + j + 1); 
				HalfEdgeUtils.constructFaceByVertices(lhds, v1, v2, v4);
				HalfEdgeUtils.constructFaceByVertices(lhds, v2, v3, v4);
			}
		}
	}
	
	@Override
	public V insertVertex(double i, double j) {
		int il = (int) Math.floor(i);
		int jl = (int) Math.floor(j);
		V v = null;
		if(j%1 == 0) {
			V v1 = lhds.getVertex((int)(il*yRes + j));
			V v2 = lhds.getVertex((int)((il+1)*yRes + j));
			E re = HalfEdgeUtils.findEdgeBetweenVertices(v1, v2);
			F f = TopologyAlgorithms.removeEdgeFill(re);
			v = TopologyAlgorithms.splitFace(f);
			double[] p1 = a.getD(Position3d.class, v1);
			double[] p2 = a.getD(Position3d.class, v2);
			double[] t1 = a.getD(TexturePosition2d.class, v1);
			double[] t2 = a.getD(TexturePosition2d.class, v2);
			double[] p = Rn.times(null, 0.5, Rn.add(null, p1, p2));
			double[] t = Rn.times(null, 0.5, Rn.add(null, t1, t2));
			a.set(Position.class, v, p);
			a.set(TexturePosition.class, v, t);
		} else {
			V v1 = lhds.getVertex((il*yRes + jl));
			V v2 = lhds.getVertex(((il+1)*yRes + jl + 1));
			List<E> ne = insertEdge(v1,v2, true);
			v = ne.get(0).getTargetVertex();
			double[] pos = getPos(i,j);
			a.set(Position.class, v, pos.clone());
			a.set(TexturePosition.class, v, pos.clone());
		}
		return v;
	}
	
	@Override
	public List<E> insertEdge(V v1, V v2, boolean newVertices) {
		LinkedList<E> edges = new LinkedList<E>();
		E e = HalfEdgeUtils.findEdgeBetweenVertices(v1,v2);
		if(e == null) {
			for(E re: HalfEdgeUtilsExtra.get1Ring(v1)) {
				F rf = re.getRightFace();
				if(rf != null && HalfEdgeUtils.boundaryVertices(rf).contains(v2)) {
					if(newVertices) {
						F f = TopologyAlgorithms.removeEdgeFill(re);
						V v = TopologyAlgorithms.splitFace(f);
						edges.add(HalfEdgeUtils.findEdgeBetweenVertices(v1, v));
						edges.add(HalfEdgeUtils.findEdgeBetweenVertices(v, v2));
						double[] p1 = a.getD(Position3d.class, v1);
						double[] p2 = a.getD(Position3d.class, v2);
						double[] t1 = a.getD(TexturePosition2d.class, v1);
						double[] t2 = a.getD(TexturePosition2d.class, v2);
						double[] p = Rn.times(null, 0.5, Rn.add(null, p1, p2));
						double[] t = Rn.times(null, 0.5, Rn.add(null, t1, t2));
						a.set(Position.class, v, p);
						a.set(TexturePosition.class, v, t);
					} else {
						TopologyAlgorithms.flipEdge(re);
						E e12 = HalfEdgeUtils.findEdgeBetweenVertices(v1,v2);
						edges.add(e12);
					}
					break;
				}
			}
		} else {
			edges.add(e);
		}
		return edges;
	}
}
