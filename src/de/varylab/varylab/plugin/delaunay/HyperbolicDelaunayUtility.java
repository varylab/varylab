package de.varylab.varylab.plugin.delaunay;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

import de.jreality.math.Pn;
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

public class HyperbolicDelaunayUtility {


	protected static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void poincareToHyperboloid(HDS hds, AdapterSet as) {
		for(V v : hds.getVertices()) {
			as.set(Position.class, v, poincareToHyperboloid(as.getD(Position2d.class,v)));
		}
	}
	
	protected static double[] poincareToHyperboloid(double[] src) {
		double[] dest = new double[src.length+1];
		int n = src.length;
		if(dest.length < src.length+1) {
			n = dest.length-1;
		}
		double sq = Rn.euclideanNormSquared(src);
		dest[n] = 1 + sq; 
		for(int i = 0; i < n; ++i) {
			dest[i] = 2*src[i];
		}
		Rn.times(dest, 1.0/(1.0-sq), dest);
		return dest;
	}
	
	protected static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void hyperboloidToPoincare(HDS hds, AdapterSet as) {
//		VHDS hds = hif.get(new VHDS());
//		AdapterSet as = hif.getAdapters();
		for(V v : hds.getVertices()) {
			as.set(Position.class, v, hyperboloidToPoincareDisc(as.getD(Position3d.class,v)));
		}
	}

	protected static double[] hyperboloidToPoincareDisc(double[] src) {
		double[] dest = new double[src.length-1];
		for (int i = 0; i < dest.length; i++) {
			dest[i] = src[i]/(src[src.length-1]+1);
		}
		return dest;
	}
	
	protected static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void hyperboloidToKlein(HDS hds, AdapterSet as) {
		for(V v : hds.getVertices()) {
			as.set(Position.class, v, hyperboloidToKleinDisc(as.getD(Position3d.class,v)));
		}
	}
	
	protected static double[] hyperboloidToKleinDisc(double[] src) {
		double[] dest = new double[src.length-1];
		for (int i = 0; i < dest.length; i++) {
			dest[i] = src[i]/src[src.length-1];
		}
		return dest;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void extractHyperbolicFaces(HDS hds, AdapterSet as) {
		HashSet<F> hyperbolicFaces = new HashSet<F>();
		TreeSet<E> hyperbolicEdges = new TreeSet<E>(new Comparator<E>(){
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
			if(HyperbolicDelaunayUtility.isHyperbolicFace(f,as)) { 
				hyperbolicFaces.add(f);
				for(E e : HalfEdgeUtils.boundaryEdges(f)) {
					if(!e.isPositive()) {
						e = e.getOppositeEdge();
					}
					if(hyperbolicEdges.contains(e)) {
						continue;
					}
					hyperbolicEdges.add(e);
				}
			}
		}
	//	for(E e : hyperbolicEdges) {
	//		System.out.print(e.getStartVertex().getIndex() + "-" + e.getTargetVertex().getIndex() +",");
	//	}
	//	System.out.println();
		for(E e : new ArrayList<E>(hds.getEdges())) {
			if(!e.isPositive()) {
				continue;
			}
			if(hyperbolicEdges.contains(e)) {
				continue; // if one of the adjacent faces is hyperbolic
			}
			if(HyperbolicDelaunayUtility.isHyperbolicEdge(e,as)) {
				hyperbolicEdges.add(e);
			}
		}
		HalfedgeUtility.retainFaces(hds, hyperbolicFaces);
		HalfedgeUtility.retainEdges(hds, hyperbolicEdges);
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> boolean isHyperbolicFace(F f, AdapterSet as) {
		return isHyperbolicFace(hyperbolicNormal(f, as));
	}
	
	protected static boolean isHyperbolicFace(double[] normal) {
		boolean insideCone = Pn.normSquared(normal, Pn.HYPERBOLIC) < 0;
		boolean isLowerFace = isHyperbolicLowerFace(normal);
		return isLowerFace && insideCone;
	}
	
	protected static boolean isPositiveDefinite(double[] e1, double[] e2, int metric) {
		double e1e1 = Pn.innerProduct(e1, e1, metric);
		double e2e2 = Pn.innerProduct(e2, e2, metric);
		double e1e2 = Pn.innerProduct(e1, e2, metric);
		return e1e1*(e1e1*e2e2-e1e2*e1e2) > 0;
	}
	
	protected static boolean isHyperbolicLowerFace(double[] hypNormal) {
		return Pn.innerProduct(new double[]{0.0,0.0,1.0}, hypNormal, Pn.HYPERBOLIC) > 0;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] hyperbolicNormal(F f, AdapterSet as) {
		double[] normal = as.getD(Normal.class, f);
		normal[2] *= -1.0;
		return normal;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> boolean isHyperbolicEdge(E e, AdapterSet as) {
		F f1 = e.getLeftFace();
		double[] n1 = hyperbolicNormal(f1,as);
		F f2 = e.getRightFace();
		double[] n2 = hyperbolicNormal(f2,as);
		return isHyperbolicEdge(n1, n2); 
	}

	protected static boolean isHyperbolicEdge(double[] n1, double[] n2) {
		boolean isPositiveDefinite = isPositiveDefinite(n1, n2, Pn.HYPERBOLIC);
		boolean isLowerFace1 = isHyperbolicLowerFace(n1);
		boolean isLowerFace2 = isHyperbolicLowerFace(n2);
		boolean intersectsLowerCone = intersectsLowerCone(n1,n2);
		return !isPositiveDefinite 
				&& (isLowerFace1 || isLowerFace2) 
				&& intersectsLowerCone;
	}

	private static boolean intersectsLowerCone(double[] n1, double[] n2) {
		if(n1[2] > 0 && n2[2] > 0) {
			return false;
		} else if(n1[2] < 0 && n2[2] < 0) {
			return (Pn.innerProduct(n1, n2, Pn.HYPERBOLIC) < 0);
		}
		if(n2[2] < 0) {
			return ((Rn.innerProduct(n2, n2)*n1[2] - Rn.innerProduct(n1, n2)*n2[2]) < 0);
		} else { //n1[2] < 0!
			return ((Rn.innerProduct(n1, n1)*n2[2] - Rn.innerProduct(n1, n2)*n1[2]) < 0);
		}
	}

	protected static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void poincareToKlein(HDS hds, AdapterSet as) {
		poincareToHyperboloid(hds, as);
		hyperboloidToKlein(hds, as);
	}
	
	protected static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void kleinToHyperboloid(HDS hds, AdapterSet as) {
		for(V v : hds.getVertices()) {
			as.set(Position.class, v, kleinToHyperboloid(as.getD(Position2d.class,v)));
		}
	}
	
	protected static double[] kleinToHyperboloid(double[] src) {
		double[] dest = new double[src.length+1];
		int n = src.length;
		if(dest.length < src.length+1) {
			n = dest.length-1;
		}
		double sq = Rn.euclideanNormSquared(src);
		dest[n] = 1; 
		for(int i = 0; i < n; ++i) {
			dest[i] = src[i];
		}
		Rn.times(dest, 1.0/Math.sqrt(1.0-sq), dest);
		return dest;
	}

	protected static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void kleinToPoincare(HDS hds, AdapterSet as) {
		kleinToHyperboloid(hds, as);
		hyperboloidToPoincare(hds, as);
	}
}
