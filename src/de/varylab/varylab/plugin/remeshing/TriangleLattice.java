package de.varylab.varylab.plugin.remeshing;

import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VVertex;

public class TriangleLattice extends Lattice {

	private double ystep = 0.5;
	private double xstep = 1.0/6.0;
	
	public TriangleLattice(Rectangle2D bbox) {
		super(new double[]{1.0/3.0,0.0}, new double[]{1.0/6.0,0.5},bbox);
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
				VVertex v = lhds.addNewVertex();
				double xPos = ll[0] + (2*i+j)*xstep ;
				double yPos = ll[1] + j * ystep;
				v.position = new double[]{xPos,yPos,1.0};
				v.texcoord = new double[]{xPos,yPos,1.0};
			}
		}
		for (int i = 0; i < xRes  - 1; i++) {
			for (int j = 0; j < yRes - 1; j++) {
				VVertex v1 = lhds.getVertex(i*yRes + j); 
				VVertex v2 = lhds.getVertex((i+1)*yRes + j); 
				VVertex v3 = lhds.getVertex((i+1)*yRes + j + 1); 
				VVertex v4 = lhds.getVertex(i*yRes + j + 1); 
				HalfEdgeUtils.constructFaceByVertices(lhds, v1, v2, v4);
				HalfEdgeUtils.constructFaceByVertices(lhds, v2, v3, v4);
			}
		}
	}
	
	@Override
	public VVertex insertVertex(double i, double j) {
		VVertex v = null;
		int il = (int) Math.floor(i);
		int jl = (int) Math.floor(j);
		
		if(j%1 == 0) {
			VVertex v1 = lhds.getVertex((int)(il*yRes + j));
			VVertex v2 = lhds.getVertex((int)((il+1)*yRes + j));
			VEdge re = HalfEdgeUtils.findEdgeBetweenVertices(v1, v2);
			VFace f = TopologyAlgorithms.removeEdgeFill(re);
			v = TopologyAlgorithms.splitFace(f);
			v.position = Rn.times(null, 0.5, Rn.add(null, v1.position, v2.position));
			v.texcoord =  Rn.times(null, 0.5, Rn.add(null, v1.texcoord, v2.texcoord));
		} else {
			VVertex v1 = lhds.getVertex((il*yRes + jl));
			VVertex v2 = lhds.getVertex(((il+1)*yRes + jl + 1));
			List<VEdge> ne = insertEdge(v1,v2, true);
			v = ne.get(0).getTargetVertex();
			double[] pos = getPos(i,j);
			System.arraycopy(pos, 0, v.position, 0, 2);
			System.arraycopy(pos, 0, v.texcoord, 0, 2);
		}
		return v;
	}
	
	@Override
	public List<VEdge> insertEdge(VVertex v1, VVertex v2, boolean newVertices) {
		LinkedList<VEdge> edges = new LinkedList<VEdge>();
		VEdge e = HalfEdgeUtils.findEdgeBetweenVertices(v1,v2);
		VVertex v = null;
		if(e == null) {
			for(VEdge re: HalfEdgeUtilsExtra.get1Ring(v1)) {
				VFace rf = re.getRightFace();
				if(rf != null && HalfEdgeUtils.boundaryVertices(rf).contains(v2)) {
					if(newVertices) {
						VFace f = TopologyAlgorithms.removeEdgeFill(re);
						v = TopologyAlgorithms.splitFace(f);
						edges.add(HalfEdgeUtils.findEdgeBetweenVertices(v1, v));
						edges.add(HalfEdgeUtils.findEdgeBetweenVertices(v, v2));
						v.position = Rn.times(null, 0.5, Rn.add(null, v1.position, v2.position));
						v.texcoord =  Rn.times(null, 0.5, Rn.add(null, v1.texcoord, v2.texcoord));
					} else {
						TopologyAlgorithms.flipEdge(re);
						VEdge e12 = HalfEdgeUtils.findEdgeBetweenVertices(v1,v2);
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
