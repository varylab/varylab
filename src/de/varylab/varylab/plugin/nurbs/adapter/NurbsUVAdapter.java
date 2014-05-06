package de.varylab.varylab.plugin.nurbs.adapter;

import java.util.HashMap;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.type.NurbsUVCoordinate;

@NurbsUVCoordinate
public class NurbsUVAdapter extends AbstractAdapter<double[]> {
	
	Map<Integer,double[]>
		indexUVMap = null;
	NURBSSurface ns = null;	
	
	private MeshParameters 
		meshParameters = null;
		
	public NurbsUVAdapter(NURBSSurface ns, Map<Integer,double[]> indexMap, int uLines, int vLines) {
		super(double[].class,true,false);
		indexUVMap = new HashMap<Integer, double[]>(indexMap);
		meshParameters = new MeshParameters(uLines, vLines);
		this.ns = ns;
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Vertex.class.isAssignableFrom(nodeClass);
	}
	
	@Override
	public <
		V extends Vertex<V,E,F>, 
		E extends Edge<V,E,F>, 
		F extends Face<V,E,F>
	> double[] getV(V v, AdapterSet a) {
		return indexUVMap.get(v.getIndex());
	}

	public NURBSSurface getSurface() {
		return ns;
	}

	public int getULineCount() {
		return meshParameters.getNumULines();
	}
	
	public int getVLineCount() {
		return meshParameters.getNumVLines();
	}
	
	private class MeshParameters {
		
		private int 
			numULines = 10,
			numVLines = 10;

		public MeshParameters(int u, int v) {
			numULines = u;
			numVLines = v;
		}
		
		public int getNumULines() {
			return numULines;
		}

		public int getNumVLines() {
			return numVLines;
		}
	}
	
}
