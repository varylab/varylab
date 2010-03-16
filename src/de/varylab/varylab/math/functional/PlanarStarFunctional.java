/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universität Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

package de.varylab.varylab.math.functional;

import static de.varylab.varylab.math.functional.VolumeFunctionalUtils.differentiateDet2;

import java.util.Collection;
import java.util.List;

import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.math.CollectionUtility;

public class PlanarStarFunctional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> implements Functional<V, E, F> {

	private double
		scale = 1.0;
	
	public PlanarStarFunctional(double scale) {
		this.scale = scale;
	}
	
	
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluate(
		HDS hds,
		DomainValue x, 
		Energy E, 
		Gradient G, 
		Hessian H
	) {
		if(E != null) {
			E.setZero();
			for (V v: hds.getVertices()) { // flatness
				List<V> neighbors = HalfEdgeUtils.neighboringVertices(v);
				neighbors.add(v);
				Collection< Collection<V> > LL = CollectionUtility.subsets(neighbors,4);
				for(Collection<V> tets : LL) {
					double result = volume2(x,tets);
					E.add(result);
				}
			}
		}
		if (G != null) {
			evaluateGradient(hds, x, G);
		}
	}

	
	private double volume2(DomainValue x, Collection<V> neighbors) {
		double[][] tet = new double[4][4];
		int i = 0;
		for(V v : neighbors) {
			tet[i++] = getPosition(x,v);
		}
		double det = Rn.determinant(tet);
		return det*det;
	}

	public void evaluateGradient(
		HalfEdgeDataStructure<V, E, F> hds, 
		DomainValue x, 
		Gradient G 
	){
		G.setZero();
		for (V v : hds.getVertices()){ // flatness
			List<V> neighbors = HalfEdgeUtils.neighboringVertices(v);
			neighbors.add(v);
			for(Collection<V> tets : CollectionUtility.subsets(neighbors,4)) {
				Matrix mat = new Matrix();
				int i = 0;
				for(V w : tets) {
					mat.setColumn(i++, getPosition(x,w));
				}
				int j = 0;
				for (V w : tets){
					int vertexIndex = w.getIndex();
					G.add(vertexIndex * 3 + 0, differentiateDet2(mat, 0, j) * scale);
					G.add(vertexIndex * 3 + 1, differentiateDet2(mat, 1, j) * scale);
					G.add(vertexIndex * 3 + 2, differentiateDet2(mat, 2, j) * scale);
					j++;
				}
			}
		}
	}
	
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return hds.numVertices() * 3;
	}

	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(HDS hds) {
		return null;
	}
	
    
    public boolean hasHessian() {
    	return false;
    }


	public void setScale(double s) {
		scale = s;
	}

	private double[] getPosition(DomainValue x, V v) {
		double[] pos = new double[4];
		pos[0] = x.get(v.getIndex() * 3 + 0);
		pos[1] = x.get(v.getIndex() * 3 + 1);
		pos[2] = x.get(v.getIndex() * 3 + 2);
		pos[3] = 1.0;
		return pos;
	}
}
