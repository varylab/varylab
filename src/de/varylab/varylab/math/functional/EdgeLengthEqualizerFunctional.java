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

import static de.jtem.halfedge.util.HalfEdgeUtils.incomingEdges;
import static de.jtem.halfedgetools.functional.FunctionalUtils.addVectorToGradient;
import static de.jtem.halfedgetools.functional.FunctionalUtils.getPosition;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;

public class EdgeLengthEqualizerFunctional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> implements Functional<V, E, F> {

	double[] 
		pv = new double[3],
		pvi = new double[3],
		pvim = new double[3],
		pvip = new double[3],
		veci = new double[3],
		vecip = new double[3],
		tmp = new double[3];
	
	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluate(HDS hds,
		DomainValue x, 
		Energy E, 
		Gradient G, 
		Hessian H
	) {
		for (V v : hds.getVertices()) {
			getPosition(v, x, pv);
			for (E e : incomingEdges(v)) {
				V vi = e.getStartVertex();
				V vim = e.getOppositeEdge().getPreviousEdge().getStartVertex();
				V vip = e.getNextEdge().getTargetVertex();
				getPosition(vi, x, pvi);
				getPosition(vip, x, pvip);
				double li = Rn.euclideanDistanceSquared(pv, pvi);
				double lip = Rn.euclideanDistanceSquared(pv, pvip);
				double difp = li - lip;
				if (E != null) E.add(difp * difp);
				if (G != null) {
					getPosition(vim, x, pvim);
					double lim = Rn.euclideanDistanceSquared(pv, pvim);
					Rn.subtract(veci, pv, pvi);
					Rn.subtract(vecip, pv, pvip);
					Rn.subtract(tmp, veci, vecip);
					Rn.times(tmp, 4 * difp, tmp);
					addVectorToGradient(G, 3*v.getIndex(), tmp);
					Rn.times(tmp, 4, veci);
					Rn.times(tmp, lip - 2*li + lim, tmp);
					addVectorToGradient(G, 3*vi.getIndex(), tmp);
				}
			}
		}
	}
	
	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return hds.numVertices() * 3;
	}

	@Override
	public <HDS extends de.jtem.halfedge.HalfEdgeDataStructure<V,E,F>> int[][] getNonZeroPattern(HDS hds) {
		return null;
	};

	@Override
    public boolean hasHessian() {
    	return false;
    }
	
}
