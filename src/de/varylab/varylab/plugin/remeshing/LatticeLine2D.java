package de.varylab.varylab.plugin.remeshing;

import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Rn;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VVertex;

public class LatticeLine2D {

	double 
		a = 1,
		b = 0,
		c = 0;

	Lattice lattice = null;
	
	// Create a line ax + by = c
	public LatticeLine2D(int a, int b, int c, Lattice l) {
		this.a = a;
		this.b = b;
		this.c = c;
		lattice = l;
	}
	
	public LatticeLine2D(Slope m, int n, Lattice l) {
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

	public double[] intersect(LatticeLine2D l2) {
		double det = a*l2.b-l2.a*b;
		return Rn.times(null,1.0/det,new double[]{l2.b*c-b*l2.c,-l2.a*c+a*l2.c});
	}

	public Slope getSlope() {
		return new Slope(b,-a);
	}

	public boolean onLine(double[] xy) {
		return Math.abs(a*xy[0]+b*xy[1]-c) <= 1E-6;
	}
		
	public List<VVertex> getOpenSegment(VVertex start, VVertex end, boolean newVertices) {
		List<VVertex> segment = getSegment(start,end,newVertices);
		segment.remove(0);
		if(segment.size() != 0) {
			segment.remove(segment.size()-1);
		}
		return segment;
	}
	
	public List<VVertex> getSegment(VVertex start, VVertex end, boolean newVertices) {
		LinkedList<VVertex> segment = new LinkedList<VVertex>();
		segment.add(start);
		if(end == start) {
			return segment;
		}
		double[] sp = start.position;
		double[] ep = end.position;
		double[] dir = Rn.subtract(null,ep,sp);
		double[] startIJ = lattice.getIJ(sp);
		Slope dirSlope = lattice.compass.getClosestSlope(dir[0],dir[1]);
		Slope ijSlope = lattice.compass.getIJSlope(dirSlope);
		if((startIJ[0] % 1 != 0) || (startIJ[1] % 1 != 0)) {
			double 
				lx = (ijSlope.dx==0)?0:(((ijSlope.dx>0)?1-((startIJ[0])%1):-((startIJ[0])%1))/ijSlope.dx),
				ly = (ijSlope.dy==0)?0:(((ijSlope.dy>0)?1-((startIJ[1])%1):-((startIJ[1])%1))/ijSlope.dy);
			Rn.add(startIJ,startIJ,Rn.times(null,Math.max(lx, ly),ijSlope.toArray()));
		} else {
			Rn.add(startIJ,startIJ,ijSlope.toArray());
		}
		double[] IJ = new double[]{Math.round(startIJ[0]),Math.round(startIJ[1])};
		VVertex v = lattice.getVertex((int)IJ[0],(int)IJ[1]);
		double[] diff = Rn.subtract(null, ep, v.position);
		while((dir[0]*diff[0]+dir[1]*diff[1]) >= 0) {
			List<VEdge> ne = lattice.insertEdge(segment.getLast(),v, newVertices);
			if(ne.size() != 1) {
				segment.add(ne.get(0).getTargetVertex());
			}
			segment.add(v);
			if(v == end) break;
			Rn.add(IJ,IJ,ijSlope.toArray());
			v = lattice.getVertex((int)IJ[0],(int)IJ[1]);
			Rn.subtract(diff,ep,v.position);
		}
		if(segment.getLast() != end) {
			lattice.insertEdge(segment.getLast(),end,newVertices);
			segment.add(end);
		}
		return segment;
	}
}
