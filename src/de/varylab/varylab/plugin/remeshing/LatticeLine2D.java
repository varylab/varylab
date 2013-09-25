package de.varylab.varylab.plugin.remeshing;

import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

public class LatticeLine2D <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	HDS extends HalfEdgeDataStructure<V, E, F>
> {

	protected double 
		a = 1,
		b = 0,
		c = 0;
	protected Lattice<V, E, F, HDS>
		lattice = null;
	
	// Create a line ax + by = c
	public LatticeLine2D(int a, int b, int c, Lattice<V, E, F, HDS> l) {
		this.a = a;
		this.b = b;
		this.c = c;
		lattice = l;
	}
	
	public LatticeLine2D(Slope m, int n, Lattice<V, E, F, HDS> l) {
		a = -m.dy;
		b = m.dx;
		c = n;
		lattice = l;
	}

	@Override
	public String toString() {
		return new String(a+"x + " + b + "y = " + c);
	}

	public double[] getNormal() {
		return new double[]{a,b};
	}

	public double getDistance() {
		return c;
	}
	
	public double evalI(double i) {
		return (c-a*i)/(1.0*b);
	}
	
	public double evalJ(double j) {
		return (c-b*j)/(1.0*a);
	}

	public double[] intersect(LatticeLine2D<V, E, F, HDS> l2) {
		double det = a*l2.b-l2.a*b;
		return Rn.times(null,1.0/det,new double[]{l2.b*c-b*l2.c,-l2.a*c+a*l2.c});
	}

	public Slope getSlope() {
		return new Slope(b,-a);
	}

	public boolean onLine(double[] xy) {
		return Math.abs(a*xy[0]+b*xy[1]-c) <= 1E-6;
	}
		
	public List<V> getOpenSegment(V start, V end, boolean newVertices, AdapterSet a) {
		List<V> segment = getSegment(start, end, newVertices, a);
		segment.remove(0);
		if(segment.size() != 0) {
			segment.remove(segment.size()-1);
		}
		return segment;
	}
	
	public List<V> getSegment(V start, V end, boolean newVertices, AdapterSet a) {
		LinkedList<V> segment = new LinkedList<V>();
		segment.add(start);
		if(end == start) {
			return segment;
		}
		double[] sp = a.getD(Position3d.class, start);
		double[] ep = a.getD(Position3d.class, end);
		double[] dir = Rn.subtract(null,ep,sp);
		double[] startIJ = lattice.getIJ(sp);
		Slope dirSlope = lattice.compass.getClosestSlope(dir[0],dir[1]);
		Slope ijSlope = lattice.compass.getIJSlope(dirSlope);
		startIJ = LatticeUtility.nextLatticePointInDirection(startIJ, ijSlope);
		double[] IJ = new double[]{Math.round(startIJ[0]),Math.round(startIJ[1])};
		V v = lattice.getVertex((int)IJ[0], (int)IJ[1]);
		double[] vpos = a.getD(Position3d.class, v);
		double[] diff = Rn.subtract(null, ep, vpos);
		while((dir[0]*diff[0]+dir[1]*diff[1]) >= 0) {
			List<E> ne = lattice.insertEdge(segment.getLast(),v, newVertices);
			if(ne.size() != 1) {
				segment.add(ne.get(0).getTargetVertex());
			}
			segment.add(v);
			if(v == end) break;
			Rn.add(IJ,IJ,ijSlope.toArray());
			v = lattice.getVertex((int)IJ[0],(int)IJ[1]);
			Rn.subtract(diff, ep, vpos);
		}
		if(segment.getLast() != end) {
			lattice.insertEdge(segment.getLast(),end,newVertices);
			segment.add(end);
		}
		return segment;
	}


}
