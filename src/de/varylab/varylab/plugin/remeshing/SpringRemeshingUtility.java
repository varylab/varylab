package de.varylab.varylab.plugin.remeshing;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.jtem.jpetsc.InsertMode;
import de.jtem.jpetsc.Mat;
import de.jtem.jpetsc.Vec;
import de.jtem.jtao.Tao;
import de.jtem.jtao.Tao.GetSolutionStatusResult;
import de.jtem.jtao.TaoAppAddCombinedObjectiveAndGrad;
import de.jtem.jtao.TaoAppAddHess;
import de.jtem.jtao.TaoApplication;
import de.varylab.varylab.functional.SpringFunctional;
import de.varylab.varylab.functional.EdgeLengthAdapters.Length;
import de.varylab.varylab.functional.EdgeLengthAdapters.WeightFunction;
import de.varylab.varylab.math.tao.TaoDomainValue;
import de.varylab.varylab.math.tao.TaoEnergy;
import de.varylab.varylab.math.tao.TaoGradient;
import de.varylab.varylab.math.tao.TaoHessian;

public class SpringRemeshingUtility {

	
	
	private static class ConstantLengthAdapter <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> implements Length<E> {

		private double 
			length = 0.0;
		
		public ConstantLengthAdapter(double l0) {
			this.length = l0;
		}
		
		@Override
		public Double getTargetLength(E e) {
			return length;
		}
		
	}
	
	public static class TextureWeightAdapter <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> implements WeightFunction<E> {

		private AdapterSet
			a = null;
		
		public TextureWeightAdapter(AdapterSet a) {
			this.a = a;
		}
		
		@Override
		public Double getWeight(E e) {
			double[] s = a.getD(TexturePosition2d.class, e.getStartVertex());
			double[] t = a.getD(TexturePosition2d.class, e.getTargetVertex());
			return  1 / Rn.euclideanDistance(s, t);
		}

	}

	
	public static class MapWeightAdapter <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> implements WeightFunction<E> {

		private double
			defaultWeight = 1.0;
		private Map<E, Double> 
			weightMap = null;
		
		public MapWeightAdapter(double defaultWeight, Map<E, Double> weightMap) {
			this.defaultWeight = defaultWeight;
			this.weightMap = weightMap;
		}
		
		@Override
		public Double getWeight(E e) {
			if (!weightMap.containsKey(e)) {
				return defaultWeight;
			} else {
				return weightMap.get(e);
			}
		}

	}
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void relaxInterior(
		Lattice<V, E, F, HDS> lattice,
		Collection<V> fixedVerts,
		boolean fixBd, 
		boolean innerBd,
		AdapterSet a
	) {
		Length<E> lengths = new ConstantLengthAdapter<V, E, F>(0.0);
		WeightFunction<E> weights = new TextureWeightAdapter<V, E, F>(a);
		SpringFunctional<V, E, F> fun = new SpringFunctional<V, E, F>(lengths, weights, false);
		optimize(
			fun,
			lattice.getHDS(),
			fixedVerts,
			fixBd, 
			innerBd,
			a
		);
	}
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void straightenBoundary(
		Lattice<V, E, F, HDS> lattice,
		LinkedList<E> polygonEdges,	
		Collection<V> corners,
		AdapterSet a
	) {
		WeightFunction<E> texWeights = new TextureWeightAdapter<V, E, F>(a);
		Map<E, Double> boundaryWeights = new HashMap<E, Double>();
		for (E e : polygonEdges) {
			double w = texWeights.getWeight(e);
			boundaryWeights.put(e, w);
			boundaryWeights.put(e.getOppositeEdge(), w);
		}
		
		WeightFunction<E> weights = new MapWeightAdapter<V, E, F>(0.0, boundaryWeights);
		Length<E> lengths = new ConstantLengthAdapter<V, E, F>(0.0);
		SpringFunctional<V, E, F>	
			fun = new SpringFunctional<V, E, F>(lengths, weights, false);
		optimize(
			fun, 
			lattice.getHDS(), 
			corners, 
			false, 
			false,
			a
		);
	}

	
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void optimize(
		SpringFunctional<V, E, F> fun,
		HDS hds, 
		Collection<V> fixedVerts, 
		boolean fixBoundary, 
		boolean innerBd,
		AdapterSet a
	) {
		int dim = hds.numVertices() * 3;
		FixingConstraint<V, E, F, HDS> fixConstraint = new FixingConstraint<V, E, F, HDS>(
			fixedVerts,
			fixBoundary,
			innerBd,
			a
		);
		double acc = Math.pow(10, -6);
		int maxIter = 100;
		Tao.Initialize();
		SpringOptimizableTao<V, E, F, HDS> opt = new SpringOptimizableTao<V, E, F, HDS>(
			hds, 
			fun, 
			fixConstraint
		);
		
		Vec x = new Vec(dim);
		for (V v : hds.getVertices()) {
			double[] p = a.getD(Position3d.class, v);
			x.setValue(v.getIndex() * 3 + 0, p[0], InsertMode.INSERT_VALUES);
			x.setValue(v.getIndex() * 3 + 1, p[1], InsertMode.INSERT_VALUES);
			x.setValue(v.getIndex() * 3 + 2, p[2], InsertMode.INSERT_VALUES);
		}
		
		opt.setInitialSolutionVec(x);
		Tao optimizer = new Tao(Tao.Method.CG);
		optimizer.setApplication(opt);
		optimizer.setGradientTolerances(acc, acc, 0);
		optimizer.setMaximumIterates(maxIter);
		optimizer.setTolerances(0.0, 0.0, 0.0, 0.0);
		optimizer.solve();
		
		GetSolutionStatusResult stat = optimizer.getSolutionStatus();
		String status = stat.toString().replace("getSolutionStatus : ", "");
		System.out.println("optimization status ------------------------------------");
		System.out.println(status);
		
		for (V v : hds.getVertices()) {
			int i = v.getIndex() * 3;
			double[] p = {0,0,0};
			p[0] = x.getValue(i + 0);
			p[1] = x.getValue(i + 1);
			p[2] = x.getValue(i + 2);
			a.set(Position.class, v, p);
		}
	}
	
	
	public static class FixingConstraint <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> {

		private boolean
			fixBoundary = false,
			innerBoundary = false;
		private Collection<V>
			selectedVertices = null;
		private AdapterSet
			a = null;
		
		public FixingConstraint(
			Collection<V> selectedVertices, 
			boolean fixBoundary, 
			boolean innerBoundaryMovements,
			AdapterSet a
		) {
			this.selectedVertices = selectedVertices;
			this.fixBoundary = fixBoundary;
			this.innerBoundary = innerBoundaryMovements;
			this.a = a;
		}


		public void editGradient(HDS hds, int dim, DomainValue x, Gradient G) {
			for (V v : selectedVertices){
				int i = v.getIndex();
				G.set(i * 3 + 0, 0.0);
				G.set(i * 3 + 1, 0.0);
				G.set(i * 3 + 2, 0.0);
			}
			for (V v : hds.getVertices()){
				if (!HalfEdgeUtils.isBoundaryVertex(v)){
					continue;
				}
				if (selectedVertices.contains(v)) {
					continue;
				}
				int i = v.getIndex();
				if (innerBoundary) { // project onto boundary
					V v1 = null;
					for (E e : HalfEdgeUtils.incomingEdges(v)) {
						if (e.getLeftFace() == null){
							v1 = e.getStartVertex();
							break;
						}
					}
					assert v1 != null;
					double[] p = a.getD(Position3d.class, v);
					double[] p1 = a.getD(Position3d.class, v1);
					double[] w1 = Rn.subtract(null, p1, p);
					double[] grad = new double[] {G.get(i * 3 + 0), G.get(i * 3 + 1), G.get(i * 3 + 2)};
					Rn.projectOnto(grad, grad, w1);
					G.set(i * 3 + 0, grad[0]);
					G.set(i * 3 + 1, grad[1]);
					G.set(i * 3 + 2, grad[2]);
				} else if (fixBoundary) { // set zero
					G.set(i * 3 + 0, 0.0);
					G.set(i * 3 + 1, 0.0);
					G.set(i * 3 + 2, 0.0);
				}
			}
		}


		public void editHessian(HDS hds, int dim, DomainValue x, Hessian H) {
			for (V v : selectedVertices){
				int i = v.getIndex();
				for (int j = 0; j < dim; j++) {
					H.set(i * 3 + 0,j, 0.0);
					H.set(i * 3 + 1,j, 0.0);
					H.set(i * 3 + 2,j, 0.0);
					H.set(j,i * 3 + 0, 0.0);
					H.set(j,i * 3 + 1, 0.0);
					H.set(j,i * 3 + 2, 0.0);
				}
			}
			if (!fixBoundary) return;
			for (V v : hds.getVertices()){
				if (!HalfEdgeUtils.isBoundaryVertex(v)){
					continue;
				}
				if (selectedVertices.contains(v)) {
					continue;
				}
				int i = v.getIndex();
				for (int j = 0; j < dim; j++) {
					H.set(i * 3 + 0, j, 0.0);
					H.set(i * 3 + 1, j, 0.0);
					H.set(i * 3 + 2, j, 0.0);
				}
			}
		}
	}
	
	
	public static class SpringOptimizableTao <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> extends TaoApplication implements TaoAppAddCombinedObjectiveAndGrad, TaoAppAddHess {
		
		private HDS
			hds = null;
		private SpringFunctional<V, E, F>
			fun = null;
		private FixingConstraint<V, E, F, HDS>
			c = null;
		
		public SpringOptimizableTao(HDS hds, SpringFunctional<V, E, F> fun, FixingConstraint<V, E, F, HDS> constraint) {
			this.hds = hds;
			this.fun = fun;
			this.c = constraint;
		}
		
		
		private void applyConstraints(DomainValue x, Gradient G, Hessian H) {
			if (G != null) {
				c.editGradient(hds, getDomainDimension(), x, G);
			}
			if (H != null) {
				c.editHessian(hds, getDomainDimension(), x, H);
			}
		}
		
		
		@Override
		public double evaluateObjectiveAndGradient(Vec x, Vec g) {
			TaoDomainValue u = new TaoDomainValue(x);
			TaoGradient G = new TaoGradient(g);
			TaoEnergy E = new TaoEnergy();
			fun.evaluate(hds, u, E, G, null);
			applyConstraints(u, G, null);
			g.assemble();
			return E.get();
		}
		
		
		@Override
		public PreconditionerType evaluateHessian(Vec x, Mat H, Mat Hpre) {
			TaoDomainValue u = new TaoDomainValue(x);
			TaoHessian taoHess = new TaoHessian(H);
			fun.evaluate(hds, u, null, null, taoHess);
			applyConstraints(u, null, taoHess);
			H.assemble();
			return PreconditionerType.SAME_NONZERO_PATTERN;
		}
		
		
		public int getDomainDimension() {
			return hds.numVertices() * 3;
		}
		
	}
	
}
