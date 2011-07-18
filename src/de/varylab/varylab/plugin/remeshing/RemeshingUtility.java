package de.varylab.varylab.plugin.remeshing;

import static java.lang.Math.abs;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition3d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.bsp.KdTree;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.varylab.discreteconformal.heds.CoVertex;
import de.varylab.varylab.hds.VFace;

public class RemeshingUtility {

	
	/**
	 * Cut at the surface boundary and create vertices at 
	 * edge intersections only
	 * @param r
	 * @param surface
	 */
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void cutTargetBoundary(
		Set<F> faceOverlap, 
		Set<V> vertexOverlap,
		HDS surface, 
		Set<V> boundaryFeatures,
		AdapterSet a
	) {
		// collect intersection candidates
		Set<E> checkEdges = new HashSet<E>();
		for (F f : faceOverlap) {
			for (E e : HalfEdgeUtils.boundaryEdges(f)) {
				if (HalfEdgeUtils.isBoundaryEdge(e)) {
					continue;
				}
				if (e.isPositive()) {
					checkEdges.add(e);
				}
			}
		}
		// check against surface boundary
		Collection<E> sourceBoundary = HalfEdgeUtils.boundaryEdges(surface);
		Map<F, List<V>> cutMap = new HashMap<F, List<V>>();
		for (E e : checkEdges) {
			for (E sb : sourceBoundary) {
				double[] p = getIntersection(e, sb, a, 1E-6);
				if (p == null) continue;
				double[] s = a.getD(TexturePosition2d.class, e.getStartVertex());
				double[] t = a.getD(TexturePosition2d.class, e.getTargetVertex());
				double l = Rn.euclideanDistance(s, t);
				double distS = Rn.euclideanDistance(s, p) / l;
				double distT = Rn.euclideanDistance(t, p) / l;
				V intersectionVertex = null;
				List<F> cutFaces = new LinkedList<F>();
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
				double[] pos = getEdgeIntersectionPos(sb, p, a);
				a.set(TexturePosition.class, intersectionVertex, p);
				a.set(Position.class, intersectionVertex, pos);
				for (F f : cutFaces) {
					if (!cutMap.containsKey(f)) {
						cutMap.put(f, new LinkedList<V>());
					}
					List<V> faceCutList = cutMap.get(f); 
					faceCutList.add(intersectionVertex);
				}
				// remove from vertex overlap if we have an old vertex
				vertexOverlap.remove(intersectionVertex);
			}
		}
		// insert edges
		for (F f : cutMap.keySet()) {
			List<V> cvList = cutMap.get(f);
			if (cvList.size() < 2) continue;
			V v1 = cvList.get(0);
			V v2 = cvList.get(1);
			if (v1 == v2) {
				continue;
			}
			if (HalfEdgeUtils.findEdgeBetweenVertices(v1, v2) != null) {
				continue;
			}
			splitFaceAt(f, v1, v2);
		}
		for (V v : vertexOverlap) {
			TopologyAlgorithms.removeVertex(v);
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
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Map<V, double[]> mapInnerVertices(HDS mesh, KdTree<V, E, F> meshKD, HDS remesh, AdapterSet a) {
		// map inner vertices
		Map<V, double[]> flatCoordMap = new HashMap<V, double[]>();
		for (V v : remesh.getVertices()) {
			double[] patternPoint = a.getD(Position3d.class, v);
			F f = RemeshingUtility.getContainingFace(v, mesh, a, meshKD);
			if (f == null) { 
				System.err.println("no face containing " + v + " found!");
				continue;
			}
			double[] bary = getBarycentricTexturePoint(patternPoint, f, a);
			double[] newPos = getPointFromBarycentric(bary, f, a);
			flatCoordMap.put(v, patternPoint);
			a.set(Position.class, v, newPos);
		}
		return flatCoordMap;
	}
	
	
	/**
	 * Convert to barycentric point in texture space
	 * @param p
	 * @param t
	 * @return
	 */
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] getBarycentricTexturePoint(double[] p , F f, AdapterSet a) {
		double[] A = a.getD(TexturePosition2d.class, f.getBoundaryEdge().getStartVertex());
		double[] B = a.getD(TexturePosition2d.class, f.getBoundaryEdge().getTargetVertex());
		double[] C = a.getD(TexturePosition2d.class, f.getBoundaryEdge().getNextEdge().getTargetVertex());
		double x1 = A[0], y1 = A[1];
		double x2 = B[0], y2 = B[1];
		double x3 = C[0], y3 = C[1];	
		double det = (x1 - x3)*(y2 - y3) - (y1 - y3)*(x2 - x3);
		double[] l = {0,0,0};
		l[0] = ((y2 - y3)*(p[0] - x3) - (x2 - x3)*(p[1] - y3)) / det;
		l[1] = ((x1 - x3)*(p[1] - y3) - (y1 - y3)*(p[0] - x3)) / det;
		l[2] = 1 - l[0] - l[1];
		return l;
	}
	
	
	/**
	 * Convert to cartesian point in position space 
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param <HDS>
	 * @param b
	 * @param f
	 * @param a
	 * @return
	 */
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] getPointFromBarycentric(double[] b, F f, AdapterSet a) {
		double[] A = a.getD(Position3d.class, f.getBoundaryEdge().getStartVertex());
		double[] B = a.getD(Position3d.class, f.getBoundaryEdge().getTargetVertex());
		double[] C = a.getD(Position3d.class, f.getBoundaryEdge().getNextEdge().getTargetVertex());
		double[] r = {0, 0, 0};
		r[0] = b[0]*A[0] + b[1]*B[0] + b[2]*C[0];
		r[1] = b[0]*A[1] + b[1]*B[1] + b[2]*C[1];
		r[2] = b[0]*A[2] + b[1]*B[2] + b[2]*C[2];
		return r;
	}
	
	
	
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
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
	

	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> F findCommonFace(V v1, V v2) {
		List<F> faceStar1 = HalfEdgeUtilsExtra.getFaceStar(v1);
		List<F> faceStar2 = HalfEdgeUtilsExtra.getFaceStar(v2);
		faceStar1.retainAll(faceStar2);
		if(faceStar1.size() == 0) {
			return null;
		} else {
			return faceStar1.get(0);
		}
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] getEdgeIntersectionPos(E e, double[] texPos, AdapterSet a) {
		double[] s = a.getD(TexturePosition2d.class, e.getStartVertex());
		double[] t = a.getD(TexturePosition2d.class, e.getTargetVertex());
		double[] sp = a.getD(Position3d.class, e.getStartVertex());
		double[] tp = a.getD(Position3d.class, e.getTargetVertex());
		double l1 = Rn.euclideanDistance(s, t);
		double l2 = Rn.euclideanDistance(sp, tp);
		double[] v = Rn.subtract(null, t, s);
		Rn.normalize(v, v);
		double[] vp = Rn.subtract(null, tp, sp);
		Rn.normalize(vp, vp);
		double[] v2 = Rn.subtract(null, texPos, s);
		double d1 = Rn.innerProduct(v, v2);
		double d2 = d1 * l2 / l1;
		return Rn.linearCombination(null, 1.0, sp, d2, vp);
	}
	
	
	/**
	 * Inserts a vertex into the face f
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param <HDS>
	 * @param f
	 * @param a
	 * @return
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> V insertVertexStellar(F f, AdapterSet a) {
		HalfEdgeDataStructure<V, E, F> hds = f.getHalfEdgeDataStructure();
		V v = hds.addNewVertex();
		List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
		hds.removeFace(f);
		E firstEdge = null;
		E lastEdge = null;
		for (E be : boundary) {
			V v1 = be.getStartVertex();
			E e1 = hds.addNewEdge(); 
			E e2 = hds.addNewEdge();
			e2.linkNextEdge(e1);
			e1.linkNextEdge(be);
			be.linkNextEdge(e2);
			e1.setTargetVertex(v1);
			e2.setTargetVertex(v);
			F newf = hds.addNewFace();
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
		if (lastEdge == null) {
			throw new RuntimeException("Cannot link last and first edge in insertVertexStellar()");
		}
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
	
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] getIntersection(E e1, E e2, AdapterSet a, double eps) {
		double[] p1 = a.getD(TexturePosition2d.class, e1.getStartVertex());
		double[] p2 = a.getD(TexturePosition2d.class, e1.getTargetVertex());
		double[] p3 = a.getD(TexturePosition2d.class, e2.getStartVertex());
		double[] p4 = a.getD(TexturePosition2d.class, e2.getTargetVertex());
		double uanom = (p4[0] - p3[0])*(p1[1] - p3[1]) - (p1[0] - p3[0])*(p4[1] - p3[1]);
		double ubnom = (p2[0] - p1[0])*(p1[1] - p3[1]) - (p1[0] - p3[0])*(p2[1] - p1[1]);
		double denom = (p2[0] - p1[0])*(p4[1] - p3[1]) - (p4[0] - p3[0])*(p2[1] - p1[1]);
		if (denom == 0) {
			if (uanom == 0 && ubnom == 0) {
				return p1;
			}
			return null;
		}
		double ua = uanom / denom;
		double ub = ubnom / denom;
		if (-eps <= ua && ua <= 1 + eps && -eps <= ub && ub <= 1 + eps) {
			double[] dir = Rn.subtract(null, p2, p1);
			return Rn.linearCombination(null, 1.0, p1, ua, dir);
		} else {
			return null;
		}
	}
	
	
	/**
	 * Determines the texture face of mesh that contains the texture vertex v
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param <HDS>
	 * @param v
	 * @param hds
	 * @param a
	 * @param meshKD a kdTree of mesh
	 * @return
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> F getContainingFace(V v, HDS mesh, AdapterSet a, KdTree<V, E, F> meshKD) {
		double[] p = a.getD(Position3d.class, v);
		Collection<V> nearbyVertices = meshKD.collectKNearest(p, 5);
		Set<F> checkFaces = new HashSet<F>();
		for (V nv : nearbyVertices) {
			checkFaces.addAll(HalfEdgeUtils.facesIncidentWithVertex(nv));
		}
		// check faces incident with nearest points
		for (F f : checkFaces) {
			if (isInConvexTextureFace(p, f, a)) {
				return f;
			}
		}
		// brute force check
		for (F f : mesh.getFaces()) {
			if (isInConvexTextureFace(p, f, a)) {
				return f;
			}
		}
		return null;
	}
	
	
	/**
	 * Checks if the given p is inside the texture face of f.
	 * The texture points of f are assumed to build a convex polygon.
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param <HDS>
	 * @param p
	 * @param f
	 * @param a
	 * @return
	 */
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> boolean isInConvexTextureFace(double[] p, F f, AdapterSet a) {
		List<E> bList = HalfEdgeUtils.boundaryEdges(f);
		boolean sideFlag = false;
		boolean firstEdge = true;
		for (E e : bList) {
			double[] sp = a.getD(TexturePosition3d.class, e.getStartVertex());
			double[] tp = a.getD(TexturePosition3d.class, e.getTargetVertex());
			double[] v1 = Rn.subtract(null, tp, sp);
			double[] v2 = Rn.subtract(null, p, sp);
			double[] v2r = {-v2[1], v2[0], 0};
			double dot = Rn.innerProduct(v1, v2r);
			if(abs(dot) <= 1E-4) {
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
	
	/**
	 * Calculates the bounding box of texture points.
	 * The result is rounded to integers such that all
	 * points are inside the box
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param <HDS>
	 * @param hds
	 * @param a
	 * @return
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Rectangle2D getTextureBoundingBox(HDS hds, AdapterSet a) {
		double[] T = a.getD(TexturePosition2d.class, hds.getVertex(0));
		double minX = T[0];
		double maxX = minX;
		double minY = T[1];
		double maxY = minY;
		for (V v : hds.getVertices()) {
			T = a.getD(TexturePosition2d.class, v);
			if (T[0] < minX) {
				minX = T[0]; 
			}
			if (T[0] > maxX) {
				maxX = T[0];
			}
			if (T[1] < minY) {
				minY = T[1]; 
			}
			if (T[1] > maxY) {
				maxY = T[1];
			}			
		}
		minX = Math.floor(minX) - 1;
		maxX = Math.ceil(maxX) + 1;
		minY = Math.floor(minY) - 1;
		maxY = Math.ceil(maxY) + 1;
		return new Rectangle2D.Double(minX,minY,maxX-minX,maxY-minY);
	}



	/**
	 * Projects the boundary vertices of remesh onto
	 * the boundary of mesh
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param <HDS>
	 * @param remesh
	 * @param mesh
	 * @param a
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void alignRemeshBoundary(HDS remesh, HDS mesh, AdapterSet a) {
		for (V v : HalfEdgeUtils.boundaryVertices(remesh)) {
			E e = RemeshingUtility.findClosestEdge(v, mesh, a);
			double[] newPos = RemeshingUtility.projectOntoEdge(v, e, a);
			a.set(Position.class, v, newPos);
		}
	}



	/**
	 * Projects the position of vertex v onto the edge e
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param <HDS>
	 * @param v
	 * @param e
	 * @param a
	 * @return
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	>  double[] projectOntoEdge(V v, E e, AdapterSet a) {
		double[] 
	       projectedVertex = new double[]{0.0, 0.0, 0.0},
	       vpos = a.getD(Position3d.class, v),
	       v1 = a.getD(TexturePosition3d.class, e.getStartVertex()),
	       v2 = a.getD(TexturePosition3d.class, e.getTargetVertex()),
	       ev = Rn.subtract(null, v2, v1),
	       sv = Rn.subtract(null, vpos, v1);
		double factor = Rn.innerProduct(ev, sv)/Rn.innerProduct(ev, ev);
		if(factor > 1) {
			projectedVertex = v2.clone();
		} else if(factor < 0) {
			projectedVertex = v1.clone();
		} else {
			Rn.add(projectedVertex, v1, Rn.times(null, factor, ev));
		}
		return projectedVertex;
	}



	/**
	 * Finds the edge in surface that is closest to vertex v
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param <HDS>
	 * @param v
	 * @param surface
	 * @param a
	 * @return
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> E findClosestEdge(V v, HDS surface, AdapterSet a) {
		E closestEdge = null;
		double[] vpos = a.getD(Position3d.class, v);
		double distance = Double.POSITIVE_INFINITY;
		for(E e: HalfEdgeUtils.boundaryEdges(surface)) {
			double[] epos = projectOntoEdge(v, e, a);
			double d = Rn.euclideanDistanceSquared(vpos, epos);
			if(d < distance) {
				closestEdge = e;
				distance = d;
			}
		}
		return closestEdge;
	}
	

	/**
	 * Finds the closest vertex in mesh to the affine position p
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param <HDS>
	 * @param p
	 * @param mesh
	 * @param a
	 * @return
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> V bruteForceNearest(double[] p, HDS mesh, AdapterSet a) {
		V closest = mesh.getVertex(0);
		double[] pos = a.getD(Position3d.class, closest); 
		double distance = Rn.euclideanDistance(pos, p);
		for(V v : mesh.getVertices()) {
			pos = a.getD(Position3d.class, v); 
			double dist = Rn.euclideanDistance(pos, p);
			if(dist < distance) {
				closest = v;
				distance = dist;
			}
		}
		return closest;
	}



	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Map<V, double[]> mapInnerVertices(HDS mesh, Map<F,F> faceMap, HDS remesh, AdapterSet a) {
		// map inner vertices
		Map<V, double[]> flatCoordMap = new HashMap<V, double[]>();
		for (V v : remesh.getVertices()) {
			double[] patternPoint = a.getD(Position3d.class, v);
			F f = null;
			for(F vf : HalfEdgeUtilsExtra.getFaceStar(v)) {
				f = faceMap.get(vf);
				if(f != null) {
					break;
				}
			}
			if (f == null) { 
				System.err.println("no face containing " + v + " found!");
				continue;
			}
			double[] bary = getBarycentricTexturePoint(patternPoint, f, a);
			double[] newPos = getPointFromBarycentric(bary, f, a);
			flatCoordMap.put(v, patternPoint);
			a.set(Position.class, v, newPos);
//			System.out.println(v.getIndex() + ": " + Arrays.toString(newPos));
		}
		return flatCoordMap;
	}
}
