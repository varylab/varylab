package de.varylab.varylab.plugin.delaunay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position2d;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.varylab.varylab.utilities.HalfedgeUtility;

public class EuclideanDelaunayUtility {

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void liftToParaboloid(HDS hds, AdapterSet as) {
		for(V v : hds.getVertices()) {
			as.set(Position.class, v, toParaboloid(as.getD(Position2d.class,v)));
		}
	}
	
	protected static double[] toParaboloid(double[] src) {
		double[] dest = new double[src.length+1];
		int n = src.length;
		if(dest.length < src.length+1) {
			n = dest.length-1;
		}
		dest[n] = Rn.euclideanNormSquared(src);
		for(int i = 0; i < n; ++i) {
			dest[i] = src[i];
		}
		return dest;
	}
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void extractEuclideanLowerFaces(HDS hds, AdapterSet as) {
		HashSet<F> lowerFaces = new HashSet<F>();
		TreeSet<E> lowerEdges = new TreeSet<E>(new Comparator<E>(){
			@Override
			public int compare(E a, E b){
				int v1 = a.getStartVertex().getIndex();
				int v2 = a.getTargetVertex().getIndex();
				if(v1>v2) {
					int tmp = v1;
					v1 = v2;
					v2 = tmp;
				}
				int w1 = b.getStartVertex().getIndex();
				int w2 = b.getTargetVertex().getIndex();
				if(w1>w2) {
					int tmp = w1;
					w1 = w2;
					w2 = tmp;
				}
				if(v1 - w1 == 0) return v2-w2;
	          return v1-w1;
	       }
		});
		for(F f : new ArrayList<F>(hds.getFaces())) {
			if(isEuclideanLowerFace(f,as)) { 
				lowerFaces.add(f);
				for(E e : HalfEdgeUtils.boundaryEdges(f)) {
					if(!e.isPositive()) {
						e = e.getOppositeEdge();
					}
					if(lowerEdges.contains(e)) {
						continue;
					}
					lowerEdges.add(e);
				}
			}
		}
		HalfedgeUtility.retainFaces(hds, lowerFaces);
		HalfedgeUtility.retainEdges(hds, lowerEdges);
	}
	
	protected static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> boolean isEuclideanLowerFace(F f, AdapterSet as) {
		double[] normal = as.getD(Normal.class, f);
		return Rn.innerProduct(new double[]{0.0,0.0,1.0},normal) > 0;
	}

	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void projectVertically(HDS hds, AdapterSet as) {
		for(V v : hds.getVertices()) {
			as.set(Position.class, v, projectVertically(as.getD(Position3d.class,v)));
		}
	}

	protected static double[] projectVertically(double[] src) {
		double[] dest = new double[src.length-1];
		for (int i = 0; i < dest.length; i++) {
			dest[i] = src[i];
		}
		return dest;
	}
}
