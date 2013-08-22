package de.varylab.varylab.plugin.generator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

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
import de.jtem.halfedgetools.adapter.type.generic.EdgeVector;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.algorithm.computationalgeometry.ConvexHull;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class RandomPointsBall extends AlgorithmPlugin {

	private Random 
		rnd = new Random(123);
	private int
		extraPoints = 20;
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Generator;
	}

	@Override
	public String getAlgorithmName() {
		return "Random Points on Hyperboloid";
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(RandomPointsBall.class, "numPoints", extraPoints);
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		extraPoints = c.getProperty(RandomPointsBall.class, "numPoints", 20);
	}
	

	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		rnd = new Random(123);
		String numString = JOptionPane.showInputDialog(getOptionParent(), "Number of points", extraPoints);
		if (numString == null) return;
		extraPoints = Integer.parseInt(numString);
		HDS r = hi.createEmpty(hds);
		double[][] sites = new double[extraPoints][2];
		int i = 0;
		while(i < extraPoints) {
			sites[i] = new double[]{-1.0+2*rnd.nextDouble(), -1.0+2*rnd.nextDouble()};
			double sq = Rn.euclideanNormSquared(sites[i]);
			if(sq > .8) { continue; }
			double[] hypPos = toHyperboloid(null, sites[i]);
			V v = r.addNewVertex();
			a.set(Position.class, v, hypPos);
			++i;
		}
//		HalfedgeLayer sitesLayer = new HalfedgeLayer(hi);
//		sitesLayer.setName("Sites");
//		sitesLayer.set(r);
//		hi.addLayer(sitesLayer);
//		
		ConvexHull.convexHull(r, a, 1E-8);
//		HalfedgeLayer convexHull = new HalfedgeLayer(hi);
//		convexHull.setName("Convex hull");
//		convexHull.set(r);
//		hi.addLayer(convexHull);
//		
		extractHyperbolicFaces(r,a);
//		HalfedgeLayer hyperbolicFaces = new HalfedgeLayer(hi);
//		hyperbolicFaces.setName("Hyperbolic Faces");
//		hyperbolicFaces.set(r);
//		hi.addLayer(hyperbolicFaces);
		
//		toPoincareDisc(r, a);
		hi.set(r);
	}

	private double[] toHyperboloid(double[] dest, double[] src) {
		if(dest == null) {
			dest = new double[src.length+1];
		}
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
	

	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void toPoincareDisc(HDS hds, AdapterSet as) {
		for(V v : hds.getVertices()) {
			double[] coords = as.getD(Position3d.class, v);
			coords[0] /= coords[2]+1;
			coords[1] /= coords[2]+1;
			coords[2] = 0;
			as.set(Position.class, v, coords);
		}
	}
		
	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void extractHyperbolicFaces(HDS hds, AdapterSet as) {
		HashSet<F> hyperbolicFaces = new HashSet<F>();
		TreeSet<E> hyperbolicEdges = new TreeSet<E>(new Comparator<E>(){
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
			if(isHyperbolicFace(f,as)) { 
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
		for(E e : hyperbolicEdges) {
			System.out.print(e.getStartVertex().getIndex() + "-" + e.getTargetVertex().getIndex() +",");
		}
		System.out.println();
		for(E e : new ArrayList<E>(hds.getEdges())) {
			if(!e.isPositive()) {
				continue;
			}
			if(hyperbolicEdges.contains(e)) {
				continue; // if one of the adjacent faces is hyperbolic
			}
			if(isHyperbolicEdge(e,as)) {
				hyperbolicEdges.add(e);
			}
		}
		for(E e : hyperbolicEdges) {
			System.out.print(e.getStartVertex().getIndex() + "-" + e.getTargetVertex().getIndex() +",");
		}
		System.out.println(hds);
		removeNonHyperbolicFaces(hds, hyperbolicFaces);
		removeNonHyperbolicEdges(hds, hyperbolicEdges);
	}

	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void  removeNonHyperbolicEdges(HDS hds, Set<E> hyperbolicEdges) {
		for(E e : new ArrayList<E>(hds.getEdges())) {
			if(!e.isPositive()) {
				continue;
			}
//			boolean stop = e.getStartVertex().getIndex() + e.getTargetVertex().getIndex() == 2;
			if(!hyperbolicEdges.contains(e)) {
				System.out.println("Remove: " + e.getStartVertex().getIndex() + "-" + e.getTargetVertex().getIndex());
				TopologyAlgorithms.removeEdge(e);
			}
		}
	}

	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void  removeNonHyperbolicFaces(HDS hds, HashSet<F> hyperbolicFaces) {
		for(F f : new ArrayList<F>(hds.getFaces())) {
			if(!hyperbolicFaces.contains(f)) {
				TopologyAlgorithms.removeFace(f);
			}
		}
	}
	
	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> boolean isHyperbolicFace(F f, AdapterSet as) {
		E e = f.getBoundaryEdge();
		double[] e1 = as.getD(EdgeVector.class, e.getOppositeEdge());
		double[] e2 = as.getD(EdgeVector.class, e.getNextEdge());
		boolean isPositiveDefinite = isPositiveDefinite(e1, e2, Pn.HYPERBOLIC);
		boolean isLowerFace = isLowerFace(f, as);
		return isPositiveDefinite && isLowerFace;
	}

	private boolean isPositiveDefinite(double[] e1, double[] e2, int metric) {
		double e1e1 = Pn.innerProduct(e1, e1, metric);
		double e2e2 = Pn.innerProduct(e2, e2, metric);
		double e1e2 = Pn.innerProduct(e1, e2, metric);
		return e1e1*(e1e1*e2e2-e1e2*e1e2) > 0;
	}

	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> boolean isLowerFace(F f, AdapterSet as) {
		double[] normal = hyperbolicNormal(f, as);
		return Pn.innerProduct(new double[]{0.0,0.0,1.0},normal, Pn.HYPERBOLIC) > 0;
	}

	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] hyperbolicNormal(F f, AdapterSet as) {
		double[] normal = as.getD(Normal.class, f);
		normal[2] *= -1.0;
		return normal;
	}
	
	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> boolean isHyperbolicEdge(E e, AdapterSet as) {
		F f1 = e.getLeftFace();
		double[] n1 = hyperbolicNormal(f1,as);
		F f2 = e.getRightFace();
		double[] n2 = hyperbolicNormal(f2,as);
		
		boolean isPositiveDefinite = isPositiveDefinite(n1, n2, Pn.HYPERBOLIC);
		boolean isLowerFace1 = isLowerFace(f1,as);
		boolean isLowerFace2 = isLowerFace(f2,as);
		return !isPositiveDefinite && isLowerFace1 && isLowerFace2 && Pn.innerProduct(n1, n2, Pn.HYPERBOLIC) < 0; // || isHyperbolicFace(f1,as) || isHyperbolicFace(f2,as);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Create Random Sphere");
		info.icon = ImageHook.getIcon("RandomSphere.png",16,16);
		return info;
	}
}