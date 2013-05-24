package de.varylab.varylab.plugin.remeshing;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition4d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.varylab.varylab.utilities.NodeIndexComparator;

public class FitTexturePlugin extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.TextureRemeshing;
	}

	@Override
	public String getAlgorithmName() {
		return "Transform Texture";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		Set<V> vertices = new TreeSet<V>(new NodeIndexComparator<V>());
		vertices.addAll(hi.getSelection().getVertices(hds));
		if(vertices.size() == 2) {
			Iterator<V> vi = vertices.iterator();
			V 	v1 = vi.next(),
				v2 = vi.next();
				
			double[] 
			    tex1 = a.getD(TexturePosition2d.class,v1),
			    tex2 = a.getD(TexturePosition2d.class,v2),
			    dir  = Rn.subtract(null, tex2, tex1);
			
			MatrixBuilder mb = MatrixBuilder.euclidean();
			mb.scale(1.0/Rn.euclideanNorm(dir));
			mb.rotateFromTo(new double[]{dir[0],dir[1],0}, new double[]{1,0,0});
			mb.translate(Rn.negate(null, new double[]{tex1[0],tex1[1],0.0}));
			
			Matrix T = mb.getMatrix();
			for(V v : hds.getVertices()) {
				double[] coord = a.getD(TexturePosition4d.class, v);
				T.transformVector(coord);
				a.set(TexturePosition.class, v, coord);
			}
		} else {
			throw new RuntimeException("Select at least two vertices");
		}
		hi.set(hds);
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
	}
	
}
