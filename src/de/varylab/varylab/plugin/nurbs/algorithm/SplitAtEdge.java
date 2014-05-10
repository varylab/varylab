package de.varylab.varylab.plugin.nurbs.algorithm;


import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.selection.Selection;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.math.NurbsDeformationTools;
import de.varylab.varylab.plugin.nurbs.math.NurbsSurfaceUtility;

public class SplitAtEdge extends AlgorithmPlugin {
	


	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hif) {

		Selection hes = hif.getSelection();
		if(hes.getEdges().size() != 2){
			throw new RuntimeException("Select a single edge to split nurbs surface.");
		}

		HalfedgeLayer layer = hif.getActiveLayer();
		NurbsUVAdapter uvAdapter = layer.getVolatileAdapters().query(NurbsUVAdapter.class);
		if(uvAdapter == null) {
			throw new RuntimeException("No nurbs surfaces on any layer to split.");
		}
		
		NURBSSurface surface = uvAdapter.getSurface();
		
		
		double[] s = new double[2];
		double[] t = new double[2];
		E e = hes.getEdges(hds).iterator().next();
		s = uvAdapter.getV(e.getStartVertex(), a);
		t = uvAdapter.getV(e.getTargetVertex(), a);

		NURBSSurface surf1 = new NURBSSurface();
		NURBSSurface surf2 = new NURBSSurface();
		int uLineCount = uvAdapter.getULineCount();
		int vLineCount = uvAdapter.getVLineCount();
		int surf1UCount = uLineCount;
		int surf1VCount = vLineCount;
		int surf2UCount = uLineCount;
		int surf2VCount = vLineCount;
		if(s[1] == t[1]){
			NURBSSurface[] surfs = surface.splitAtKnot(false, s[1]);
			surf1 = surfs[0];
			surf2 = surfs[1];
			int[][] lineCounts = NurbsDeformationTools.getSplittedLineCounts(surface, uLineCount, vLineCount, s[1], false);
			surf1UCount = lineCounts[0][0];
			surf1VCount = lineCounts[0][1];
			surf2UCount = lineCounts[1][0];
			surf2VCount = lineCounts[1][1];
		} else {
			NURBSSurface[] surfs = surface.splitAtKnot(true, s[0]);
			surf1 = surfs[0];
			surf2 = surfs[1];
			int[][] lineCounts = NurbsDeformationTools.getSplittedLineCounts(surface, uLineCount, vLineCount, s[0], true);
			surf1UCount = lineCounts[0][0];
			surf1VCount = lineCounts[0][1];
			surf2UCount = lineCounts[1][0];
			surf2VCount = lineCounts[1][1];
		}
		System.out.println("uLineCount = " + uLineCount + " vLineCount = " + vLineCount);
		HalfedgeLayer hel1 = new HalfedgeLayer(hif);
		hel1.setName("surface 1");
		System.out.println("surf1UCount = " + surf1UCount);
		System.out.println("surf1VCount = " + surf1VCount);
		
	
		hif.addLayer(hel1); //add and activate
		
		HalfedgeLayer hel2 = new HalfedgeLayer(hif);
		hel2.setName("surface 2");
		System.out.println("surf2UCount = " + surf2UCount);
		System.out.println("surf2VCount = " + surf2VCount);

		
		
		hif.addLayer(hel2); //add and activate
//		NurbsSurfaceUtility.addNurbsMesh(surf1,hel1,14,21);
//		NurbsSurfaceUtility.addNurbsMesh(surf2,hel2,14,21);
		NurbsSurfaceUtility.addNurbsMesh(surf1,hel1,surf1UCount,surf1VCount);
		NurbsSurfaceUtility.addNurbsMesh(surf2,hel2,surf2UCount,surf2VCount);
//		hif.update();
	}
	
	@Override
	public String getCategory() {
		return "NURBS";
	}
	

	@Override
	public String getAlgorithmName() {
		return "Split At Edge";
	}
	
}
