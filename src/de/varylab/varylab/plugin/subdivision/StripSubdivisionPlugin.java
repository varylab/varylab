package de.varylab.varylab.plugin.subdivision;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

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
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.remeshing.RemeshingUtility;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class StripSubdivisionPlugin extends HalfedgeAlgorithmPlugin {

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
		VertexPositionCalculator vc = c.get(hds.getVertexClass(), VertexPositionCalculator.class);
		FaceBarycenterCalculator fc = c.get(hds.getFaceClass(), FaceBarycenterCalculator.class);
		if (vc == null || fc == null) {
			throw new CalculatorException("No Subdivision calculators found for " + hds);
		}
		Set<E> edges = hcp.getSelection().getEdges(hds);
		HashSet<F> faces = new HashSet<F>(hcp.getSelection().getFaces(hds));
		
		for(E e : edges) {
			F f = e.getLeftFace();
			if(faces.contains(f)) {
				subdivideStrip1D(f, e, vc);
			}
		}
		hcp.set(hds);	
	}

	private  <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HEDS extends HalfEdgeDataStructure<V, E, F>
	> void subdivideStrip1D(F f, E fe, VertexPositionCalculator vc)
	{
		LinkedList<F> stripFaces = new LinkedList<F>();
		LinkedList<V> stripVertices = new LinkedList<V>();
		
		generateStrip1D(f, fe, vc, stripFaces, stripVertices);
		for(int i = 0; i < stripFaces.size(); ++i) {
			RemeshingUtility.splitFaceAt(
					stripFaces.get(i),
					stripVertices.get(i),
					stripVertices.get(i+1));
		}
	}

	private  <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HEDS extends HalfEdgeDataStructure<V, E, F>
	> void generateStrip1D(F f, E fe, VertexPositionCalculator vc,
			LinkedList<F> stripFaces,
			LinkedList<V> stripVertices)
	{
		LinkedList<E> stripEdges = new LinkedList<E>();
		stripFaces.addFirst(f);
		E e = fe;
		stripEdges.addLast(e);
		
		F rf = e.getRightFace();
		while(e.getRightFace() != null) {
			if(HalfEdgeUtils.isInteriorFace(rf) && (HalfEdgeUtils.boundaryEdges(rf).size() % 2) != 0) {
				break;
			}
			stripFaces.addLast(rf);
			e = getOppositeEdgeInFace(e.getOppositeEdge());
			if(e == null) {
				break;
			}
			rf = e.getRightFace();
			stripEdges.addLast(e);
		}
		e = getOppositeEdgeInFace(fe);
		if(e == null) {
			return;
		}
		stripEdges.addFirst(e.getOppositeEdge());
		rf = e.getRightFace();
		while(rf != null) {
			if(HalfEdgeUtils.isInteriorFace(rf) && (HalfEdgeUtils.boundaryEdges(rf).size() % 2) != 0) {
				break;
			}
			stripFaces.addFirst(rf);
			e = getOppositeEdgeInFace(e.getOppositeEdge());
			if(e == null) {
				break;
			}
			rf = e.getRightFace();
			stripEdges.addFirst(e.getOppositeEdge());
		}
		for(E se: stripEdges) {
			V 	v1 = se.getStartVertex(),
				v2 = se.getTargetVertex(),
				v = TopologyAlgorithms.splitEdge(se);
			vc.set(v,Rn.times(null, 0.5, Rn.add(null,vc.get(v1),vc.get(v2))));
			stripVertices.addLast(v);
		}
	}


	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F> 
	> E getOppositeEdgeInFace(E e) {
		F f = e.getLeftFace();
		E oe = e;
		if((HalfEdgeUtils.boundaryEdges(f).size() % 2) == 0) {
			for(int i = 0; i < HalfEdgeUtils.boundaryEdges(f).size()/2; ++i) {
				oe = oe.getNextEdge();
			}
		} else {
			if(!HalfEdgeUtils.isInteriorFace(f)) {
				for(E be : HalfEdgeUtils.boundaryEdges(f)) {
					if(be.getRightFace() == null) {
						return be;
					}
				}
			} else {
				return null;
			}
		}
		return oe;
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Strip Subdivider", "Thilo Roerig");
		info.icon = ImageHook.getIcon("stripSubd.png", 16, 16);
		return info;
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Custom;
	}

	@Override
	public String getAlgorithmName() {
		return "Strip Subdivision";
	}

}
