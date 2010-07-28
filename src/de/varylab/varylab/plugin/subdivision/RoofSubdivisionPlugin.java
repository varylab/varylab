package de.varylab.varylab.plugin.subdivision;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class RoofSubdivisionPlugin extends AlgorithmPlugin {

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(
		HDS hds, 
		CalculatorSet c, 
		HalfedgeInterface hcp) throws CalculatorException 
	{
		HDS hds2 = hcp.createEmpty(hds);
		VertexPositionCalculator vc = c.get(hds.getVertexClass(), VertexPositionCalculator.class);
		FaceBarycenterCalculator fc = c.get(hds.getFaceClass(), FaceBarycenterCalculator.class);
		if (vc == null || fc == null) {
			throw new CalculatorException("No Subdivision calculators found for " + hds);
		}
		execute(hds, hds2, vc, fc);
		hcp.set(hds2);	
	}

	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HEDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(
			HEDS oldHeds, 
			HEDS newHeds, 
			VertexPositionCalculator vA,
			FaceBarycenterCalculator fA) 
	{
		
		Map<E, V> oldEtoNewV = new HashMap<E, V>();
		for(E e : oldHeds.getPositiveEdges()) {
			V v = newHeds.addNewVertex();
			vA.set(v,Rn.times(null, 0.5, 
					Rn.add(null,vA.get(e.getStartVertex()),vA.get(e.getTargetVertex()))));
			oldEtoNewV.put(e,v);
			oldEtoNewV.put(e.getOppositeEdge(),v);
		}
		
		Map<V, V> oldVtoNewV = new HashMap<V, V>();
		
		F f = oldHeds.getFace(0);
		Map<F,E> faceEdgeMap = new HashMap<F,E>();
		LinkedList<F> queue = new LinkedList<F>();
		HashSet<F> visited = new HashSet<F>();
		queue.offer(f);
		E be = f.getBoundaryEdge();
		while(HalfEdgeUtils.isBoundaryEdge(be)) {
			be = be.getNextEdge();
		}
		faceEdgeMap.put(f,be);
		while(!queue.isEmpty()) {
			f = queue.poll();
			visited.add(f);
			for(E e : HalfEdgeUtils.boundaryEdges(f)) {
				F rf = e.getRightFace();
				if((rf != null) && (!visited.contains(rf)) && !queue.contains(rf)) {
					queue.offer(rf);
					faceEdgeMap.put(rf,e.getOppositeEdge());
				}
			}
			int size = HalfEdgeUtils.boundaryEdges(f).size();
			if(!HalfEdgeUtils.isInteriorFace(f) && ((size%2) != 0)) {
				// FIXME: Cannot deal with more than one boundary edge!
				subdivideBd(f, faceEdgeMap.get(f), newHeds, oldVtoNewV,oldEtoNewV,vA, fA); 
			} else {
				subdivide(f, faceEdgeMap.get(f), newHeds, oldVtoNewV,oldEtoNewV, vA, fA);
			}
//			print(newHeds,vA);
		}
	}
	
	private  <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HEDS extends HalfEdgeDataStructure<V, E, F>
	> void subdivide(
			F f, 
			E fe,
			HEDS hds,
			Map<V, V> oldVtoNewV, 
			Map<E, V> oldEtoNewV,
			VertexPositionCalculator vc,
			FaceBarycenterCalculator fc) 
	{
		V vf = hds.addNewVertex();
		vc.set(vf, fc.get(f));
		E e = fe;
		do {
			V v1 = oldEtoNewV.get(e.getPreviousEdge());
			V oldStartVertex = e.getStartVertex();
			V v2 = oldVtoNewV.get(oldStartVertex);
			if(v2 == null) {
				v2 = hds.addNewVertex();
				vc.set(v2, vc.get(oldStartVertex));
				oldVtoNewV.put(oldStartVertex, v2);
			}
			V v3 = oldEtoNewV.get(e);
			HalfEdgeUtils.constructFaceByVertices(hds,v1,v2,v3,vf);
			
			e = e.getNextEdge();
		} while(e != fe);
	}
	
	private  <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HEDS extends HalfEdgeDataStructure<V, E, F>
	> void subdivideBd(
		F f, 
		E fe,
		HEDS hds,
		Map<V, V> oldVtoNewV, 
		Map<E, V> oldEtoNewV,
		VertexPositionCalculator vc,
		FaceBarycenterCalculator fc) 
	{
		E be = f.getBoundaryEdge();
		while(!HalfEdgeUtils.isBoundaryEdge(be)) {
			be = be.getNextEdge();
		}
		V ve = oldEtoNewV.get(be);
		E e = fe;
		do {
			E pe = e.getPreviousEdge();
			V v1 = oldEtoNewV.get(pe);
			
			V oldStartVertex = e.getStartVertex();
			V v2 = oldVtoNewV.get(oldStartVertex);
			
			if(v2 == null) {
				v2 = hds.addNewVertex();
				vc.set(v2, vc.get(oldStartVertex));
				oldVtoNewV.put(oldStartVertex, v2);
			}
			V v3 = oldEtoNewV.get(e);
			if((pe == be) || (e == be)) {
				HalfEdgeUtils.constructFaceByVertices(hds, v1,v2,v3);
			} else {
				HalfEdgeUtils.constructFaceByVertices(hds, v1,v2,v3,ve);
			}
			e = e.getNextEdge();
		} while(e != fe);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Roof remesher :-)", "Thilo Roerig");
		info.icon = ImageHook.getIcon("roofSubdivision.png", 16, 16);
		return info;
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Custom;
	}

	@Override
	public String getAlgorithmName() {
		return "Custom Roof";
	}
}
