package de.varylab.varylab.plugin.remeshing;

import geom3d.Point;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.varylab.discreteconformal.heds.CoEdge;
import de.varylab.discreteconformal.heds.CoFace;
import de.varylab.discreteconformal.heds.CoHDS;
import de.varylab.discreteconformal.heds.CoVertex;
import de.varylab.discreteconformal.heds.bsp.KdTree;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;

public class RemeshingUtility {

	
	/**
	 * Cut at the surface boundary and create vertices at 
	 * edge intersections only
	 * @param r
	 * @param source
	 */
	public static void cutTargetBoundary(Set<VFace> overlap, Set<VVertex> vertexOverlap, CoHDS source, Set<CoVertex> boundaryFeatures) {
		// collect intersection candidates
		Set<VEdge> checkEdges = new HashSet<VEdge>();
		for (VFace f : overlap) {
			for (VEdge e : HalfEdgeUtils.boundaryEdges(f)) {
				if (HalfEdgeUtils.isBoundaryEdge(e)) {
					continue;
				}
				if (e.isPositive()) {
					checkEdges.add(e);
				}
			}
		}
		// check against surface boundary
		Collection<CoEdge> sourceBoundary = HalfEdgeUtils.boundaryEdges(source);
		Map<VFace, List<VVertex>> cutMap = new HashMap<VFace, List<VVertex>>();
		for (VEdge e : checkEdges) {
			for (CoEdge sb : sourceBoundary) {
				Point p = getIntersection(e, sb, 1E-6);
				if (p == null) continue;
				
				Point s = e.getStartVertex().getTexCoord();
				Point t = e.getTargetVertex().getTexCoord();
				double length = s.distanceTo(t);
				double distS = s.distanceTo(p) / length;
				double distT = t.distanceTo(p) / length;
				VVertex intersectionVertex = null;
				List<VFace> cutFaces = new LinkedList<VFace>();
				if (distS < 1E-5) {
					intersectionVertex = e.getStartVertex();
					cutFaces.addAll(HalfEdgeUtils.facesIncidentWithVertex(intersectionVertex));
				} else 
				if (distT < 1E-5) {
					intersectionVertex = e.getTargetVertex();
					cutFaces.addAll(HalfEdgeUtils.facesIncidentWithVertex(intersectionVertex));
				} else {
					intersectionVertex = TopologyAlgorithms.splitEdge(e);
					cutFaces.add(e.getLeftFace());
					cutFaces.add(e.getRightFace());
				}
				intersectionVertex.setTexCoord(p);
				Point pos = getEdgeIntersectionPos(sb, p);
				intersectionVertex.setPosition(pos);
				for (VFace f : cutFaces) {
					if (!cutMap.containsKey(f)) {
						cutMap.put(f, new LinkedList<VVertex>());
					}
					List<VVertex> faceCutList = cutMap.get(f); 
					faceCutList.add(intersectionVertex);
				}
				// remove from vertex overlap if we have an old vertex
				vertexOverlap.remove(intersectionVertex);
			}
		}
		// insert edges
		for (VFace f : cutMap.keySet()) {
			List<VVertex> cvList = cutMap.get(f);
			if (cvList.size() < 2) continue;
			VVertex v1 = cvList.get(0);
			VVertex v2 = cvList.get(1);
			if (v1 == v2) {
				continue;
			}
			if (HalfEdgeUtils.findEdgeBetweenVertices(v1, v2) != null) {
				continue;
			}
			splitFaceAt(f, v1, v2);
		}
	}
	
	
	
	public static boolean isFeatureVertexInConvexTextureFace(CoVertex fv, VFace f) {
//		double[] fp = fv.getPosition().get();
		
		return false;
	}
	
	
	
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HEDS extends HalfEdgeDataStructure<V, E, F>
	> E splitFaceAt(F f, V v1, V v2) {
		
		HalfEdgeDataStructure<V, E, F> hds = f.getHalfEdgeDataStructure();
		// edges
		E inv1 = null;
		E outv1 = null;
		for (E e : HalfEdgeUtils.incomingEdges(v1)) {
			if (e.getLeftFace() == f) {
				inv1 = e;
			}
			if (e.getRightFace() == f) {
				outv1 = e.getOppositeEdge();
			}
		}
		assert inv1 != null && outv1 != null;
		
		E inv2 = null;
		E outv2 = null;
		for (E e : HalfEdgeUtils.incomingEdges(v2)) {
			if (e.getLeftFace() == f) {
				inv2 = e;
			}
			if (e.getRightFace() == f) {
				outv2 = e.getOppositeEdge();
			}
		}
		assert inv2 != null && outv2 != null;
		
		// no degenerate split
		if (outv1 == inv2 || outv2 == inv1) {
			return null;
		}
		
		E ne1 = hds.addNewEdge();
		E ne2 = hds.addNewEdge();
		ne1.linkOppositeEdge(ne2);
		ne1.setTargetVertex(v1);
		ne2.setTargetVertex(v2);
		ne1.linkNextEdge(outv1);
		ne1.linkPreviousEdge(inv2);
		ne2.linkNextEdge(outv2);
		ne2.linkPreviousEdge(inv1);
		
		// faces
		F newface = hds.addNewFace();
		ne2.setLeftFace(f);
		E e = ne1;
		do {
			e.setLeftFace(newface);
			e = e.getNextEdge();
		} while (e != ne1);
		return ne2;
	}
	
	
	public static Point getEdgeIntersectionPos(CoEdge e, Point texPos) {
		Point s = e.getStartVertex().getTextureCoord();
		Point t = e.getTargetVertex().getTextureCoord();
		Point sp = e.getStartVertex().getPosition();
		Point tp = e.getTargetVertex().getPosition();
		double l1 = s.distanceTo(t);
		double l2 = sp.distanceTo(tp);
		geom3d.Vector v = s.vectorTo(t).normalize();
		geom3d.Vector vp = sp.vectorTo(tp).normalize();
		geom3d.Vector v2 = s.vectorTo(texPos);
		double d1 = v.dot(v2);
		double d2 = d1 * l2 / l1;
		Point r = new Point(sp);
		return r.add(vp.times(d2)).asPoint();
	}
	
	
	public static CoVertex insertVertexStellar(CoFace f) {
		HalfEdgeDataStructure<CoVertex, CoEdge, CoFace> hds = f.getHalfEdgeDataStructure();
		CoVertex v = hds.addNewVertex();
		List<CoEdge> boundary = HalfEdgeUtils.boundaryEdges(f);
		hds.removeFace(f);
		CoEdge firstEdge = null;
		CoEdge lastEdge = null;
		for (CoEdge be : boundary) {
			CoVertex v1 = be.getStartVertex();
			CoEdge e1 = hds.addNewEdge(); 
			CoEdge e2 = hds.addNewEdge();
			e2.linkNextEdge(e1);
			e1.linkNextEdge(be);
			be.linkNextEdge(e2);
			e1.setTargetVertex(v1);
			e2.setTargetVertex(v);
			CoFace newf = hds.addNewFace();
			e1.setLeftFace(newf);
			e2.setLeftFace(newf);
			be.setLeftFace(newf);
			if (firstEdge == null) {
				firstEdge = e1;
			}
			if (lastEdge != null) {
				e1.linkOppositeEdge(lastEdge);
			}
			lastEdge = e2;
		}
		assert lastEdge != null;
		lastEdge.linkOppositeEdge(firstEdge);
		return v;
	}
	
	
	
//	public static CoEdge findIntersectingEdge(CoEdge edge, CoHDS hds, KdTree<CoVertex> kd) {
//		 Vector<CoVertex> snList = kd.collectKNearest(edge.getStartVertex(), 5);
//		 Vector<CoVertex> tnList = kd.collectKNearest(edge.getTargetVertex(), 5);
//		 List<CoVertex> nearVertices = new LinkedList<CoVertex>();
//		 nearVertices.addAll(snList);
//		 nearVertices.addAll(tnList);
//		 Set<CoEdge> checkEdges = new HashSet<CoEdge>();
//		 for (CoVertex v : nearVertices) {
//			 checkEdges.addAll(HalfEdgeUtils.incomingEdges(v));
//		 }
//		 // check edge incident with nearest point
//		 for (VEdge e : checkEdges) {
//			 Point in = getIntersection(e, edge);
//			 if (in != null) {
//				 return e;
//			 }
//		 }
//		 // brute force check
//		 for (CoEdge e : hds.getPositiveEdges()) {
//			 Point in = getIntersection(e, edge);
//			 if (in != null) {
//				 return e;
//			 }	 
//		 }
//		 return null;
//	}
	
	
	
	
	public static Point getIntersection(VEdge e1, CoEdge e2, double eps) {
		Point p1 = new Point(e1.getStartVertex().texcoord);
		Point p2 = new Point(e1.getTargetVertex().texcoord);
		Point p3 = e2.getStartVertex().getTextureCoord();
		Point p4 = e2.getTargetVertex().getTextureCoord();
		double uanom = (p4.get(0) - p3.get(0))*(p1.get(1) - p3.get(1)) - (p1.get(0) - p3.get(0))*(p4.get(1) - p3.get(1));
		double ubnom = (p2.get(0) - p1.get(0))*(p1.get(1) - p3.get(1)) - (p1.get(0) - p3.get(0))*(p2.get(1) - p1.get(1));
		double denom = (p2.get(0) - p1.get(0))*(p4.get(1) - p3.get(1)) - (p4.get(0) - p3.get(0))*(p2.get(1) - p1.get(1));
		if (denom == 0) {
			if (uanom == 0 && ubnom == 0) {
				return p1;
			}
			return null;
		}
		double ua = uanom / denom;
		double ub = ubnom / denom;
		if (-eps <= ua && ua <= 1 + eps && -eps <= ub && ub <= 1 + eps) {
			Point r = new Point(p1);
			geom3d.Vector v = p1.vectorTo(p2).times(ua);
			r.add(v);
			return r;
		} else {
			return null;
		}
	}
	
	
	/**
	 * Assumes that all points are 2d
	 * @param hds
	 * @param kd
	 * @param p
	 * @return
	 */
	public static CoFace getContainingFace(VVertex v, CoHDS hds, KdTree<CoVertex> kd, int lookUp) {
		Vector<CoVertex> nearbyVertices = kd.collectKNearest(v, lookUp);
		Set<CoFace> checkFaces = new HashSet<CoFace>();
		for (CoVertex nv : nearbyVertices) {
			checkFaces.addAll(HalfEdgeUtils.facesIncidentWithVertex(nv));
		}
		// check faces incident with nearest points
		Point vPos = new Point(v.position);
		for (CoFace f : checkFaces) {
			if (isInConvexTextureFace(vPos, f)) {
				return f;
			}
		}
		// brute force check
		for (CoFace f : hds.getFaces()) {
			if (isInConvexTextureFace(vPos, f)) {
				return f;
			}
		}
		return null;
	}
	
	
	private static boolean isInConvexTextureFace(Point p, CoFace f) {
		List<CoEdge> bList = HalfEdgeUtils.boundaryEdges(f);
		boolean sideFlag = false;
		boolean firstEdge = true;
		for (CoEdge e : bList) {
			Point sp = e.getStartVertex().getTextureCoord();
			Point tp = e.getTargetVertex().getTextureCoord();
			geom3d.Vector v1 = sp.vectorTo(tp);
			v1.set(2, 0);
			geom3d.Vector v2 = sp.vectorTo(p);
			v2.set(2, 0);
			geom3d.Vector v2r = new geom3d.Vector(-v2.get(1), v2.get(0), 0);
			double dot = v1.dot(v2r);
			if(Math.abs(dot) <= 1E-6) {
				continue;
			}
			if (firstEdge) {
				sideFlag = dot > 0;
				firstEdge = false;
			} else {
				if (dot > 0 != sideFlag) {
					return false;
				}
			}
		}
		return true;
	}	
	
	public static Rectangle2D getTextureBoundingBox(CoHDS hds) {
		double minX = hds.getVertex(0).getTextureCoord().x();
		double maxX = hds.getVertex(0).getTextureCoord().x();
		double minY = hds.getVertex(0).getTextureCoord().y();
		double maxY = hds.getVertex(0).getTextureCoord().y();
		for (CoVertex v : hds.getVertices()) {
			if (v.getTextureCoord().x() < minX) {
				minX = v.getTextureCoord().x(); 
			}
			if (v.getTextureCoord().x() > maxX) {
				maxX = v.getTextureCoord().x();
			}
			if (v.getTextureCoord().y() < minY) {
				minY = v.getTextureCoord().y(); 
			}
			if (v.getTextureCoord().y() > maxY) {
				maxY = v.getTextureCoord().y();
			}			
		}
		minX = Math.floor(minX) - 1;
		maxX = Math.ceil(maxX) + 1;
		minY = Math.floor(minY) - 1;
		maxY = Math.ceil(maxY) + 1;
		return new Rectangle2D.Double(minX,minY,maxX-minX,maxY-minY);
	}



	public static void projectOntoBoundary(VHDS r, CoHDS hds) {
		for (VVertex v : HalfEdgeUtils.boundaryVertices(r)) {
			CoEdge e = RemeshingUtility.findClosestEdge(v,hds);
			double[] newPos = RemeshingUtility.projectOntoEdge(v,e);
			v.position = newPos;
		}
	}



	public static double[] projectOntoEdge(VVertex v, CoEdge e) {
		double[] 
		       projectedVertex = new double[]{0.0,0.0,0.0},
		       v1 = e.getStartVertex().getTextureCoord().get(),
		       v2 = e.getTargetVertex().getTextureCoord().get(),
		       ev = Rn.subtract(null, v2, v1),
		       sv = Rn.subtract(null, v.position, v1);
		double factor = Rn.innerProduct(ev, sv)/Rn.innerProduct(ev, ev);
		if(factor > 1) {
			System.arraycopy(v2, 0, projectedVertex, 0, 3);
		} else if(factor < 0) {
			System.arraycopy(v1, 0, projectedVertex, 0, 3);
		} else {
			Rn.add(projectedVertex, v1, Rn.times(null, factor, ev));
		}
		return projectedVertex;
	}



	public static CoEdge findClosestEdge(VVertex v, CoHDS hds) {
		CoEdge closestEdge = null;
		double distance = Double.POSITIVE_INFINITY;
		for(CoEdge e: HalfEdgeUtils.boundaryEdges(hds)) {
			double d = Rn.euclideanDistanceSquared(v.position, projectOntoEdge(v,e));
			if(d < distance) {
				closestEdge = e;
				distance = d;
			}
		}
		return closestEdge;
	}

	public static VVertex bruteForceNearest(Point p, VHDS r) {
		VVertex closest = r.getVertex(0);
		double distance = p.distanceTo(r.getVertex(0).getPosition());
		for(VVertex v:r.getVertices()) {
			if(p.distanceTo(v.getPosition()) < distance) {
				closest = v;
				distance = p.distanceTo(v.getPosition());
			}
		}
		return closest;
	}
}
