package de.varylab.varylab.plugin.remeshing;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Rn;
import de.jreality.scene.proxy.scene.SceneGraphComponent;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;

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

	protected double[] texInvTransform = new double[] {1.0, 0.0, 0.0, 1.0};
	
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

	public boolean isLatticePoint(double[] pt) {
		double[] ij = getIJ(pt);
		return ((ij[0]%1) == 0 )&&( (ij[1]%1) == 0);
	}

	public LinkedList<LatticeLine2D<V, E, F, HDS>> 
		findLatticeLines(LinkedList<V> corners, AdapterSet a, boolean cornersOnLattice) {
		
		LinkedList<LatticeLine2D<V, E, F, HDS>> lines = new LinkedList<LatticeLine2D<V, E, F, HDS>>();
		Iterator<V> ci = corners.iterator();
		V c2 = ci.next();
		V c1 = c2;
		do {
			c2 = ci.next();
			double[] 
		       v1 = a.getD(TexturePosition2d.class, c1),
		       v2 = a.getD(TexturePosition2d.class, c2);
			LatticeLine2D<V, E, F, HDS> line = getClosestLatticeLine(new double[]{v1[0],v1[1]}, new double[]{v2[0]-v1[0],v2[1]-v1[1]});
			if(cornersOnLattice && !lines.isEmpty()) { 
				forceIntersectionOnLattice(lines.getLast(), line);
			}
			lines.add(line);
			c1 = c2;
		} while(ci.hasNext());
		c2 = corners.getFirst();
		double[] 
		       v1 = a.getD(TexturePosition2d.class, c1),
		       v2 = a.getD(TexturePosition2d.class, c2);
		LatticeLine2D<V, E, F, HDS> line = getClosestLatticeLine(new double[]{v1[0],v1[1]}, new double[]{v2[0]-v1[0],v2[1]-v1[1]});
		if(cornersOnLattice) {
			forceIntersectionOnLattice(lines.getLast(), line);
		}
		lines.add(line);
		return lines;
	}
	
	private void forceIntersectionOnLattice(LatticeLine2D<V, E, F, HDS> line1, LatticeLine2D<V, E, F, HDS> line2) {
		double[] pt = line1.intersect(line2);
		while(!isLatticePoint(pt)) {
			line2.c += 1;
			pt = line1.intersect(line2);
		}
	}

	public LatticePolygon2D<V, E, F, HDS> 
		findLatticePolygon(LinkedList<LatticeLine2D<V, E, F, HDS>> lines, AdapterSet a, boolean addNewVertices) 
				throws RemeshingException {
		LinkedList<V> verts = new LinkedList<V>();
		LinkedList<V> corners = new LinkedList<V>();
		LatticeLine2D<V, E, F, HDS> l1 = lines.getLast();
		LatticeLine2D<V, E, F, HDS> l2 = lines.getLast();
		Iterator<LatticeLine2D<V, E, F, HDS>> li = lines.iterator();
		do {
			l1 = l2;
			l2 = li.next();
			double[] pt = l1.intersect(l2);
			V v = getLatticeVertex(pt);
			if(!verts.isEmpty()) {
				if(verts.getLast() == v) {
					throw new RemeshingException("Two boundary vertices have been identified. Please refine texture.");
				}
				List<V> segment = l1.getOpenSegment(verts.getLast(),v,addNewVertices, a);
				
				verts.addAll(segment);
			}
			verts.add(v);
			corners.add(v);
		} while(li.hasNext());
		List<V> segment = lines.getLast().getOpenSegment(verts.getLast(),verts.getFirst(),addNewVertices, a);
		verts.addAll(segment);
		return new LatticePolygon2D<V, E, F, HDS>(verts,corners);
	}

	public LinkedList<E> createInducedPolygon(LatticePolygon2D<V, E, F, HDS> polygon, boolean addNewVertices) {
		LinkedList<E> bdPolygon = new LinkedList<E>();
		List<V> boundaryVertices = polygon.getVertices();
		int n = boundaryVertices.size();
		for(int i = 0; i < n; ++i) {
			V v1 = boundaryVertices.get(i);
			V v2 = boundaryVertices.get((i + 1) % n);
			E e = HalfEdgeUtils.findEdgeBetweenVertices(v1, v2);
			if(e == null) {
				List<E> edges = insertEdge(boundaryVertices.get(i),boundaryVertices.get((i+1)%boundaryVertices.size()), addNewVertices);
				bdPolygon.addAll(edges);
			} else {
				bdPolygon.add(e);
			}
		}
		return bdPolygon;
	}
	
	public boolean checkQuantization(LinkedList<V> polygon, AdapterSet as) {
		LinkedList<double[]> edgeVectors = new LinkedList<double[]>();
		Iterator<V> it = polygon.iterator();
		V v1 = it.next();
		V v2 = it.next();
		while(it.hasNext()) {
			double[] edgeVector = TextureUtility.getDirection(v1,v2,as);
			Rn.matrixTimesVector(edgeVector, texInvTransform, edgeVector);
			if(!edgeVectors.isEmpty()) {
				double angle = Rn.euclideanAngle(Rn.negate(null, edgeVectors.getLast()), edgeVector);
				if(!isValidAngle(angle)) {
					return false;
				}
			}
			edgeVectors.add(edgeVector);
			v1 = v2;
			v2 = it.next();
		}
		double[] edgeVector = TextureUtility.getDirection(polygon.getLast(),polygon.getFirst(),as);
		Rn.matrixTimesVector(edgeVector, texInvTransform, edgeVector);
		edgeVectors.add(edgeVector);
		double angle = Rn.euclideanAngle(Rn.negate(null, edgeVectors.getLast()), edgeVectors.getFirst());
		if(!isValidAngle(angle)) {
			return false;
		}
		return true;
	}
	
	protected void setTexInvTransform(double[] texInvTransform) {
		this.texInvTransform = texInvTransform;
	}
	
	public abstract List<E> insertEdge(V last, V v, boolean newVertices);

	public abstract V insertVertex(double i, double j);
	
	protected abstract boolean isValidAngle(double angle);
}
