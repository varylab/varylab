package de.varylab.varylab.plugin.remeshing;

import static de.jtem.halfedgetools.util.GeometryUtility.isOnSegment;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import de.jreality.math.P2;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.EdgeVector;
import de.jtem.halfedgetools.adapter.type.generic.Position4d;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.halfedgetools.util.GeometryUtility;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;

public class TextureUtility {

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void createIntersectionVertices(
			double[][] segment,
			double tol,
			HDS surface, 
			AdapterSet as,
			Set<V> result, 
			boolean segmentOnly) 
	{
		double[] polygonLine = P2.lineFromPoints(null, segment[0], segment[1]);
		Set<E> positiveEdges = new HashSet<E>();
		for(E e: surface.getPositiveEdges()) {
			positiveEdges.add(e);
		}
		for (E edge : positiveEdges) {
			V s = edge.getStartVertex();
			V t = edge.getTargetVertex();
			double[] 
					st = as.getD(TexturePosition2d.class, s),
					sp = as.getD(Position4d.class, s),
					tt = as.getD(TexturePosition2d.class, t),
					tp = as.getD(Position4d.class, t);

			double[][] domainSegment = {{st[0],st[1],1}, {tt[0], tt[1], 1}};

			double[] domainEdgeLine = P2.lineFromPoints(null, domainSegment[0], domainSegment[1]);
			double[] newDomainPoint = P2.pointFromLines(null, polygonLine, domainEdgeLine);
			boolean snap = false;
			if(Pn.distanceBetween(newDomainPoint,domainSegment[0],Pn.EUCLIDEAN) < tol) {
				result.add(s);
				snap = true;
			}
			if(Pn.distanceBetween(newDomainPoint, domainSegment[1], Pn.EUCLIDEAN) < tol) {
				result.add(t);
				snap = true;
			}
			if(snap) {
				continue;
			}
			// split only if edge is really intersected
			double[][] surfaceSegment = {sp, tp};
			if ((!segmentOnly || GeometryUtility.isOnSegment(newDomainPoint, segment)) && isOnSegment(newDomainPoint, domainSegment)) {
				double[] newPoint = mapInnerPoint(newDomainPoint, domainSegment, surfaceSegment);
				
				V newVertex = TopologyAlgorithms.splitEdge(edge);
				as.set(Position.class, newVertex, newPoint);
				as.set(TexturePosition.class, newVertex, P2.imbedP2InP3(null, newDomainPoint));
				result.add(newVertex);
			}
		}
	}
	
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Selection cut(HDS hds, Set<V> vSet, AdapterSet a, boolean segmentOnly) {
		double[][] segment = new double[2][];
		
		Iterator<V> selectedIterator = vSet.iterator();;
		V start = selectedIterator.next();
		double[] p1 = a.getD(TexturePosition2d.class, start);
		segment[0] = new double[]{p1[0], p1[1], 1};
		V target = selectedIterator.next();
		double[] p2 = a.getD(TexturePosition2d.class, target);
		segment[1] = new double[]{p2[0], p2[1], 1}; 
		Set<V> result = new HashSet<V>();
		result.add(start);
		result.add(target);
		TextureUtility.createIntersectionVertices(segment, 1E-8, hds, a, result, segmentOnly);
		LinkedList<V> orderedResult = new LinkedList<V>(result);
		Collections.sort(orderedResult, new SegmentComparator<V,E,F>(Rn.subtract(null, p2, p1), a));
		Selection cutSelection = new Selection();
		cutSelection.addAll(result);
		V v1 = null;
		for (Iterator<V> it = orderedResult.iterator(); it.hasNext();) {
			if(v1 == null) {
				v1 = it.next();
			}
			if(it.hasNext()) {
				V v2 = it.next();
				F f = HalfEdgeUtilsExtra.findCommonFace(v1,v2);
				if(f != null) {
					E newEdge = RemeshingUtility.splitFaceAt(f, v1, v2);
					if (newEdge != null){
						cutSelection.add(newEdge);
					}
				}
				v1 = v2;
			}
		}
		return cutSelection;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Selection cutSegment(HDS hds, Set<V> vSet, AdapterSet a) {
		return cut(hds,vSet,a,true);
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Selection cutLine(HDS hds, Set<V> vSet, AdapterSet a) {
		return cut(hds,vSet,a, false);
	}
	
	private static class SegmentComparator<V extends Vertex<V, E, F>, E extends Edge<V, E, F>, F extends Face<V, E, F>> implements Comparator<V> {

		double[] v = new double[]{1.0, 0.0};
		AdapterSet as = null;
		
		public SegmentComparator(double[] v, AdapterSet as) {
			this.v = Rn.normalize(null, v);
			this.as = as;
		}

		@Override
		public int compare(V o1, V o2) {
			double[] t1 = as.getD(TexturePosition2d.class, o1);
			double[] t2 = as.getD(TexturePosition2d.class, o2);
			return Double.compare(Rn.innerProduct(v, t1), Rn.innerProduct(v, t2)); 
		}

	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> LinkedList<V> findCorners(HDS hds, AdapterSet a) {
		LinkedList<V> corners = new LinkedList<V>();
		E be = HalfEdgeUtils.boundaryEdges(hds).iterator().next();
		be = findNextTextureCorner(be, a);
		double[] ev = a.getD(EdgeVector.class, be);
		double[] ev2 = a.getD(EdgeVector.class, be.getOppositeEdge().getNextEdge());
		double orientation = Rn.determinant(new double[][]{{ev[0],ev[1]}, {ev2[0],ev2[1]}});
		E e = be;
		do {
			e = findNextTextureCorner(e, a);
			corners.add(e.getStartVertex());
		} while(e != be);
		if(orientation>0) {
			Collections.reverse(corners);
		}
		return corners;
	}

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> E findNextTextureCorner(E be, AdapterSet a) {
		double theta = 0.0;
		do {
			E pe = be.getOppositeEdge();
			be = be.getNextEdge();
			double[] e1 = a.getD(TexturePosition2d.class, be.getTargetVertex());
			double[] e2 = a.getD(TexturePosition2d.class, pe.getTargetVertex());
			double[] m = a.getD(TexturePosition2d.class, be.getStartVertex());
			double[] v1 = Rn.subtract(null, e1, m);
			double[] v2 = Rn.subtract(null, e2, m);
			theta = Rn.euclideanAngle(v1, v2);
		} while(Math.abs(Math.PI - theta) < 1E-3);
		return be;
	}
	

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] getDirection(V v1, V v2, AdapterSet a) {
		double[] p1 = a.getD(TexturePosition2d.class,v1);
		double[] p2 = a.getD(TexturePosition2d.class,v2);
		return Rn.subtract(null, p2, p1);
	}
	
	/**
	 * Map a point {@code p} on the segment {@code source} to the corresponding point on the
	 * segmern {@code target}
	 * @param p
	 * @param source
	 * @param target
	 * @return a point on the segment @param target
	 */
	public static double[] mapInnerPoint(double[] p, double[][] source, double[][] target) {
		return mapInnerPoint(p,source,target,Pn.EUCLIDEAN);
	}
	
	public static double[] mapInnerPoint(double[] p, double[][] source, double[][] target, int signature) {
		double l = Pn.distanceBetween(source[0], source[1], signature);
		double l1 = Pn.distanceBetween(source[0], p, signature) / l;
		double l2 = Pn.distanceBetween(source[1], p, signature) / l;
		if (Double.isNaN(l1)) {
			return target[0];
		}
		if (Double.isNaN(l2)) {
			return target[1];
		}
		return Rn.linearCombination(null, l1, target[1], l2, target[0]);
	}

}
