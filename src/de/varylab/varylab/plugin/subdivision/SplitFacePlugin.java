package de.varylab.varylab.plugin.subdivision;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.remeshing.RemeshingUtility;
import de.varylab.varylab.plugin.ui.image.ImageHook;

public class SplitFacePlugin extends HalfedgeAlgorithmPlugin {

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
		Set<F> faces = hcp.getSelection().getFaces(hds);
		HashSet<V> vertices = new HashSet<V>(hcp.getSelection().getVertices(hds));

		for(F f : faces) {
			List<V> vf = HalfEdgeUtils.boundaryVertices(f);
			List<V> splitVertices = new LinkedList<V>();
			for(V v : vf) {
				if(vertices.contains(v)) {
					splitVertices.add(v);
				}
			}
			if(splitVertices.size() == 2) {
				RemeshingUtility.splitFaceAt(f, splitVertices.get(0), splitVertices.get(1));
			}
		}
		hcp.set(hds);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Split Face At", "Stefan Sechelmann");
		info.icon = ImageHook.getIcon("splitFaceAt.png", 16, 16);
		return info;
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Custom;
	}

	@Override
	public String getAlgorithmName() {
		return "Split Face At Vertices";
	}
}
