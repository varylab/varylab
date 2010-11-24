package de.varylab.varylab.plugin.remeshing;


import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;

public class QuadLattice <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	HDS extends HalfEdgeDataStructure<V, E, F>
> extends Lattice<V, E, F, HDS> {

	private AdapterSet
		a = null;
	
	public QuadLattice(HDS template, AdapterSet a, Rectangle2D bbox) {
		super(template, new double[]{.5,0},new double[]{0,.5},bbox);
		this.a = a;
		double xSpan = bbox.getWidth();
		double ySpan = bbox.getHeight();
		xRes = (int)Math.ceil(xSpan * 2);
		yRes = (int)Math.ceil(ySpan * 2);
		
		compass = new Compass(
				new Slope[]{new Slope(2,0), new Slope(2,2), new Slope(0,2), new Slope(-2,2)},
				new Slope[]{new Slope(1,0),new Slope(1,1), new Slope(0,1), new Slope(-1,1)});
		
		ll = new double[]{bbox.getMinX(),bbox.getMinY()};
		
		for (int i = 0; i < xRes; i++) {
			for (int j = 0; j < yRes; j++) {
				V v = lhds.addNewVertex();
				double xPos =  ll[0] + i * 0.5;
				double yPos =  ll[1] + j * 0.5;
				double[] p = new double[]{xPos, yPos, 0.0};
				double[] t = new double[]{xPos, yPos, 0.0};
				a.set(Position.class, v, p);
				a.set(TexturePosition.class, v, t);
			}
		}
		for (int i = 0; i < xRes - 1; i++) {
			for (int j = 0; j < yRes - 1; j++) {
				V v1 = lhds.getVertex(i*yRes + j); 
				V v2 = lhds.getVertex((i+1)*yRes + j); 
				V v3 = lhds.getVertex((i+1)*yRes + j + 1); 
				V v4 = lhds.getVertex(i*yRes + j + 1); 
				HalfEdgeUtils.constructFaceByVertices(lhds, v1, v2, v3, v4);
			}
		}
	}
	
	@Override
	public V insertVertex(double i, double j) {
		int il = (int) Math.floor(i);
		int jl = (int) Math.floor(j);
		V 
			v1 = getVertex(il,jl),
			v2 = getVertex(il+1,jl);
		F f = HalfEdgeUtils.findEdgeBetweenVertices(v1,v2).getLeftFace();
		V v = TopologyAlgorithms.splitFace(f);
		double[] position = getPos(i,j);
		double[] p = {position[0], position[1], 0.0};
		a.set(Position.class, v, p);
		a.set(TexturePosition.class, v, p.clone());
		return v;
	}
	
	@Override
	public List<E> insertEdge(V v1, V v2, boolean newVertices) {
		E e = HalfEdgeUtils.findEdgeBetweenVertices(v1,v2);
		if(e == null) {
			for(F f : HalfEdgeUtils.facesIncidentWithVertex(v1)) {
				if(HalfEdgeUtils.boundaryVertices(f).contains(v2)){
					RemeshingUtility.splitFaceAt(f,v1,v2);
					e = HalfEdgeUtils.findEdgeBetweenVertices(v1,v2);
					break;
				} 

			}
		}
		return Collections.singletonList(e);
	}
}