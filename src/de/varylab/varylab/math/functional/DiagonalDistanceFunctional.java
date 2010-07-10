package de.varylab.varylab.math.functional;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.math.functional.PlanarFacesAdapters.VolumeWeight;

public class DiagonalDistanceFunctional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
>  implements Functional<V,E,F>{

	private VolumeWeight<F> weight = null;
	private double scale = 1.0;

	public DiagonalDistanceFunctional(VolumeWeight<F> weight, double scale) {
		this.weight = weight;
		this.scale  = scale;
	}

	private	double getDiagDistance2andGradient(F f, Gradient g, DomainValue x) {
		List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
		if (boundary.size() != 4)
			return 0.0;
		
		double[][] vertices  = new double[4][];
		int i = 0;
		for (E edge : boundary) { 
			vertices[i] = new double[3];
			FunctionalUtils.getPosition(edge.getTargetVertex(), x, vertices[i++]);
		}
		
/*
 * det = (v1-v0) * (v2-v0)x(v3-v1).
 *          a         d1      d2
 * val = det^2 / (d1xd2)^2
 *                  c
 */
		double []
		    a = Rn.subtract(null, vertices[1], vertices[0]), 
			d[] = {
				Rn.subtract(null, vertices[2], vertices[0]),  
				Rn.subtract(null, vertices[3], vertices[1])
			},
			c = Rn.crossProduct(null, d[0], d[1]);

		double det = Rn.innerProduct(a, c);
		double c2 = Rn.innerProduct(c, c);
		double weight2 = weight.getWeight(f)*weight.getWeight(f);
		
		if(g != null){
/*
 * val' =  2det * det' / c^2 - det^2 * 2 * c*c' / c^4
 * det' =  (a * c)' = a'*c + a*c'
 * c' = d[0]' x d[1] + d[0] x d[1]'  = +- e_? x d[?] 
 * a' = v1'-v0' = +- e_?
 * 
 * 
 * names : ?'<->?d
 */
			i = 0;
			double mE[][] = {{ 1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
			
			for (E edge : boundary) {
				int vertexIndex = edge.getTargetVertex().getIndex();
				for(int j = 0 ; j < 3 ; j++){
					double cd[];  
					if(i == 1 || i == 2)	cd = Rn.crossProduct(null, mE[j], d[(i+1)%2]);
					else cd = Rn.crossProduct(null, d[(i+1)%2], mE[j]);
					
					double	detd = Rn.innerProduct(a, cd);
					if(i < 2) detd += (i==0 ? -1.0:1.0) * c[j];  
					
					g.add(vertexIndex * 3 + j,2 * det * (detd - det * Rn.innerProduct(c, cd) / c2) / c2 * weight2*scale);
				}
				i++;
			}
		}
			
		return det * det / c2 * weight2;  
	}

	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluate(HDS hds, DomainValue x, Energy E, Gradient G, Hessian H) {
		double result = 0.0;
		if(E != null) {
			for (F f : hds.getFaces()) { 
				result += getDiagDistance2andGradient(f, G, x);
			}
			E.set(result);
		}
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return 3*hds.numVertices();
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(
			HDS hds) {
		return null;
	}

	@Override
	public boolean hasHessian() {
		return false;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}
}
