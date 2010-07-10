package de.varylab.varylab.plugin.remeshing;


import geom3d.Point;

import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

public class QuadLattice extends Lattice{

	public QuadLattice(Rectangle2D bbox) {
		super(new double[]{.5,0},new double[]{0,.5},bbox);
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
				VVertex v = lhds.addNewVertex();
				double xPos =  ll[0] + i * 0.5;
				double yPos =  ll[1] + j * 0.5;
				v.position = new double[]{xPos,yPos,1.0};
				v.texcoord = new double[]{xPos,yPos,1.0};
			}
		}
		for (int i = 0; i < xRes - 1; i++) {
			for (int j = 0; j < yRes - 1; j++) {
				VVertex v1 = lhds.getVertex(i*yRes + j); 
				VVertex v2 = lhds.getVertex((i+1)*yRes + j); 
				VVertex v3 = lhds.getVertex((i+1)*yRes + j + 1); 
				VVertex v4 = lhds.getVertex(i*yRes + j + 1); 
				HalfEdgeUtils.constructFaceByVertices(lhds, v1, v2, v3, v4);
			}
		}
	}
	
	@Override
	public VVertex insertVertex(double i, double j) {
		int il = (int) Math.floor(i);
		int jl = (int) Math.floor(j);
		VVertex 
			v1 = getVertex(il,jl),
			v2 = getVertex(il+1,jl);
		VFace f = HalfEdgeUtils.findEdgeBetweenVertices(v1,v2).getLeftFace();
		VVertex v = TopologyAlgorithms.splitFace(f);
		double[] position = getPos(i,j);
		Point pt = new Point(position[0], position[1], 0.0);
		v.setPosition(pt);
		v.setTexCoord(pt);
		return v;
	}
	
	@Override
	public List<VEdge> insertEdge(VVertex v1, VVertex v2, boolean newVertices) {
		VEdge e = HalfEdgeUtils.findEdgeBetweenVertices(v1,v2);
		if(e == null) {
			for(VFace f : HalfEdgeUtils.facesIncidentWithVertex(v1)) {
				if(HalfEdgeUtils.boundaryVertices(f).contains(v2)){
					RemeshingUtility.splitFaceAt(f,v1,v2);
					e = HalfEdgeUtils.findEdgeBetweenVertices(v1,v2);
					break;
				} 

			}
		}
		List<VEdge> edges = new LinkedList<VEdge>();
		edges.add(e);
		return edges;
	}
}