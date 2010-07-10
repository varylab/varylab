package de.varylab.varylab.plugin.remeshing;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.jtem.halfedgetools.functional.Functional;
import de.varylab.jpetsc.InsertMode;
import de.varylab.jpetsc.Vec;
import de.varylab.jtao.Tao;
import de.varylab.jtao.Tao.GetSolutionStatusResult;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.hds.adapter.ConstantLengthAdapter;
import de.varylab.varylab.hds.adapter.MapWeightAdapter;
import de.varylab.varylab.hds.adapter.TextureWeightAdapter;
import de.varylab.varylab.math.CombinedFunctional;
import de.varylab.varylab.math.CombinedOptimizableTao;
import de.varylab.varylab.math.FixingConstraint;
import de.varylab.varylab.math.functional.EdgeLengthAdapters.Length;
import de.varylab.varylab.math.functional.EdgeLengthAdapters.WeightFunction;
import de.varylab.varylab.math.functional.SpringFunctional;

public class SpringRemeshingUtility {

	public static void relaxInterior(
		Lattice lattice,
		Collection<VVertex> fixedVerts,
		boolean fixBd, 
		boolean innerBd
	) {
		Length<VEdge> lengths = new ConstantLengthAdapter(0.0);
		WeightFunction<VEdge> weights = new TextureWeightAdapter();
		SpringFunctional<VVertex, VEdge, VFace>	
		 	fun = new SpringFunctional<VVertex, VEdge, VFace>(lengths, weights);
		optimize(
			fun,
			lattice.getHDS(),
			fixedVerts,
			fixBd, 
			innerBd
		);
	}
	
	
	public static void straightenBoundary(
		Lattice lattice,
		LinkedList<VEdge> polygonEdges,	
		Collection<VVertex> corners
	) {
		WeightFunction<VEdge> texWeights = new TextureWeightAdapter();
		Map<VEdge, Double> boundaryWeights = new HashMap<VEdge, Double>();
		for (VEdge e : polygonEdges) {
			double w = texWeights.getWeight(e);
			boundaryWeights.put(e, w);
			boundaryWeights.put(e.getOppositeEdge(), w);
		}
		
		WeightFunction<VEdge> weights = new MapWeightAdapter(0.0, boundaryWeights);
		Length<VEdge> lengths = new ConstantLengthAdapter(0.0);
		SpringFunctional<VVertex, VEdge, VFace>	
			fun = new SpringFunctional<VVertex, VEdge, VFace>(lengths, weights);
		optimize(
			fun, 
			lattice.getHDS(), 
			corners, 
			false, 
			false
		);
	}

	
	private static void optimize(
			Functional<VVertex, VEdge, VFace> fun,
			VHDS hds, 
			Collection<VVertex> fixedVerts, 
			boolean fixBoundary, 
			boolean innerBd
		) {
			int dim = hds.numVertices() * 3;
			FixingConstraint fixConstraint = new FixingConstraint(
				fixedVerts,
				true,true,true,
				fixBoundary, fixBoundary, fixBoundary, 
				innerBd,
				false,false,false
			);
			double acc = Math.pow(10, -6);
			int maxIter = 100;
			Tao.Initialize();
			
			List<Functional<VVertex, VEdge, VFace>> funs = new LinkedList<Functional<VVertex,VEdge,VFace>>();
			Map<Functional<?, ?, ?>, Double> coeffs = new HashMap<Functional<?,?,?>, Double>();
			funs.add(fun);
			coeffs.put(fun, 1.0);
			CombinedFunctional combFun = new CombinedFunctional(funs,coeffs,dim);
			CombinedOptimizableTao app = new CombinedOptimizableTao(hds, combFun);
			app.addConstraint(fixConstraint);
			
			Vec x = new Vec(dim);
			for (VVertex v : hds.getVertices()) {
				x.setValue(v.getIndex() * 3 + 0, v.position[0], InsertMode.INSERT_VALUES);
				x.setValue(v.getIndex() * 3 + 1, v.position[1], InsertMode.INSERT_VALUES);
				x.setValue(v.getIndex() * 3 + 2, v.position[2], InsertMode.INSERT_VALUES);
			}
			
			app.setInitialSolutionVec(x);
			Tao optimizer = new Tao(Tao.Method.CG);
			optimizer.setApplication(app);
			optimizer.setGradientTolerances(acc, acc, 0);
			optimizer.setMaximumIterates(maxIter);
			optimizer.setTolerances(0.0, 0.0, 0.0, 0.0);
			optimizer.solve();
			
			GetSolutionStatusResult stat = optimizer.getSolutionStatus();
			String status = stat.toString().replace("getSolutionStatus : ", "");
			System.out.println("optimization status ------------------------------------");
			System.out.println(status);
			
			for (VVertex v : hds.getVertices()) {
				int i = v.getIndex() * 3;
				v.position[0] = x.getValue(i + 0);
				v.position[1] = x.getValue(i + 1);
				v.position[2] = x.getValue(i + 2);
			}
		}
	
}
