package de.varylab.varylab.plugin.remeshing;

import java.awt.geom.Rectangle2D;
import java.util.List;

import de.jreality.math.Rn;
import de.jreality.scene.proxy.scene.SceneGraphComponent;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;

public abstract class Lattice <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	HDS extends HalfEdgeDataStructure<V, E, F>
> {

	protected HDS
		lhds = null;
	
	protected int 
		xRes,
		yRes;
	
	protected Compass 
		compass = null;
	
	protected Rectangle2D 
		bbox = null;
	
	@SuppressWarnings("unused")
	private SceneGraphComponent 
		root = new SceneGraphComponent();
	protected double[] 
	    ll = new double[2];
	
	private double[] 
	    v1 = new double[2],
		v2 = new double[2];
	
	public Lattice(HDS template, double[] v1, double[] v2, Rectangle2D bbox) {
		template.clear();
		lhds = template;
		ll[0] = bbox.getMinX();
		ll[1] = bbox.getMinY();
		this.bbox = bbox;
		System.arraycopy(v1, 0, this.v1, 0, 2);
		System.arraycopy(v2, 0, this.v2, 0, 2);
	}
		
	public HDS getHDS() {
		return lhds;
	}

	public LatticeLine2D<V, E, F, HDS> getClosestLatticeLine(double[] pt, double[] dir) {
		Slope s = compass.getClosestSlope(dir[0],dir[1]);
		int n = (int) Math.round(s.distance(pt));
		return new LatticeLine2D<V, E, F, HDS>(s, n, this);
	}

	public double[] getIJ(double[] xy) {
		double[] ij = new double[2];
		double det = v1[0]*v2[1]-v2[0]*v1[1];
		double[] b = Rn.subtract(null, xy, ll);
		Rn.times(ij,1/det,new double[]{v2[1]*b[0]-v2[0]*b[1],v1[0]*b[1]-v1[1]*b[0]});
		ij[0] = 1E-6*Math.round(ij[0]*1E6);
		ij[1] = 1E-6*Math.round(ij[1]*1E6);
		return ij;
	}
	
	public double[] getPos(double i, double j) {
		return getPos(new double[]{i,j});
	}
	
	public double[] getPos(double[] ij) {
		return new double[]{ij[0]*v1[0]+ij[1]*v2[0]+ll[0],ij[0]*v1[1]+ij[1]*v2[1]+ll[1]};
	}


	public V getVertex(int i, int j) {
		return lhds.getVertex(i*yRes+j);
	}

	public abstract List<E> insertEdge(V last, V v, boolean newVertices);

	public V getLatticeVertex(double[] pt) {
		V v = null;
		double[] ij = getIJ(pt); 
			
		if(ij[0] % 1 == 0 && ij[1] % 1 == 0) {
			v = lhds.getVertex((int)ij[0] * yRes + (int)ij[1]);
		} else {
			v = insertVertex(ij[0],ij[1]);
		}
		return v;
	}

	public abstract V insertVertex(double i, double j);

	public boolean isLatticePoint(double[] pt) {
		double[] ij = getIJ(pt);
		return ((ij[0]%1) == 0 )&&( (ij[1]%1) == 0);
	}

}
