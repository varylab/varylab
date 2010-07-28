package de.varylab.varylab.plugin.subdivision;

import java.awt.event.InputEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.KeyStroke;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.remeshing.RemeshingUtility;
import de.varylab.varylab.plugin.ui.image.ImageHook;
import de.varylab.varylab.utilities.SelectionUtility;

public class StripSubdivisionPlugin extends AlgorithmPlugin {

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
		LinkedList<E> stripEdges = new LinkedList<E>();
		LinkedList<V> stripVertices = new LinkedList<V>();
		
		SelectionUtility.generateStrip1D(f, fe, stripFaces, stripEdges);
		
		for(E se: stripEdges) {
			if(se.isPositive()) {
				V 	v1 = se.getStartVertex(),
				v2 = se.getTargetVertex(),
				v = TopologyAlgorithms.splitEdge(se);
				vc.set(v,Rn.times(null, 0.5, Rn.add(null,vc.get(v1),vc.get(v2))));
				stripVertices.addLast(v);
			}
		}
		for(int i = 0; i < stripFaces.size(); ++i) {
			RemeshingUtility.splitFaceAt(
					stripFaces.get(i),
					stripVertices.get(i),
					stripVertices.get(i+1));
		}
	}
	
	@Override
	public KeyStroke getKeyboardShortcut() {
		return KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
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
