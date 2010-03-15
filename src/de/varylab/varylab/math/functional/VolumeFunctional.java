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

import java.util.List;

import de.jreality.math.Matrix;
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
import de.varylab.varylab.math.functional.PlanarFacesAdapters.VolumeWeight;

public class VolumeFunctional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> implements Functional<V, E, F> {

	private VolumeWeight<F>
		weight = null;
	private double
		scale = 1.0,
		alpha = 0.0;
	
	public VolumeFunctional(VolumeWeight<F> weight, double scale, double alpha) {
		this.weight = weight;
		this.scale = scale;
		this.alpha = alpha;
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
		
		E.setZero();
		for (F f : hds.getFaces()) { // flatness
			E.add(getDet2(x, f) * weight.getWeight(f) * weight.getWeight(f) * scale);
		}
		if (G != null) {
			evaluateGradient(hds, x, G, null, alpha);
		}
//		if (alpha != 0.0) {
//			for (V v : hds.getVertices()) { // minimal movement
//				if (HalfEdgeUtils.isBoundaryVertex(v)) continue;
//				int i = v.getIndex();
//				double[] startPoint = {startData[i * 3 + 0], startData[i * 3 + 1], startData[i * 3 + 2], 1.0};
//				result += alpha * Pn.distanceBetween(startPoint, v.getPosition(), Pn.EUCLIDEAN);
//			}
//		}
	}

	
	public double getDet2(DomainValue x, F f){
		List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
		if (boundary.size() != 4) return 0.0;
		de.jreality.math.Matrix mat = new de.jreality.math.Matrix();
		int i = 0;
		for (E edge : boundary){
			double[] vertexPos = getPosition(x, edge.getTargetVertex());
			mat.setColumn(i, vertexPos);
			i++;
		}
		double det = mat.getDeterminant();
		return det * det;
	}
	
	
	private double[] getPosition(DomainValue x, V v) {
		double[] pos = new double[4];
		pos[0] = x.get(v.getIndex() * 3 + 0);
		pos[1] = x.get(v.getIndex() * 3 + 1);
		pos[2] = x.get(v.getIndex() * 3 + 2);
		pos[3] = 1.0;
		return pos;
	}
	
	

	public void evaluateGradient(
		HalfEdgeDataStructure<V, E, F> data, 
		DomainValue x, 
		Gradient G, 
		double[] startData, 
		double alpha
	){
		G.setZero();
		for (F f : data.getFaces()){ // flatness
			List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
			if (boundary.size() != 4)
				continue;
			Matrix mat = new Matrix();
			int i = 0;
			for (E edge : boundary){
				double[] vertexPos = getPosition(x, edge.getTargetVertex());
				mat.setColumn(i, vertexPos);
				i++;
			}
			int j = 0;
			for (E edge : boundary){
				int vertexIndex = edge.getTargetVertex().getIndex();
				double weight2 = weight.getWeight(f) * weight.getWeight(f);
				G.add(vertexIndex * 3 + 0, VolumeFunctionalUtils.differentiateDet2(mat, 0, j) * weight2 * scale);
				G.add(vertexIndex * 3 + 1, VolumeFunctionalUtils.differentiateDet2(mat, 1, j) * weight2 * scale);
				G.add(vertexIndex * 3 + 2, VolumeFunctionalUtils.differentiateDet2(mat, 2, j) * weight2 * scale);
				j++;
			}
		}
//		if (alpha != 0.0) {
//			for (V v : data.getVertices()) { // minimal movement
//				if (HalfEdgeUtils.isBoundaryVertex(v)) continue;
//				int i = v.getIndex();
//				double[] pos = v.getPosition();
//				double[] startPoint = {startData[i * 3 + 0], startData[i * 3 + 1], startData[i * 3 + 2], 1.0};
//				double factor = Pn.distanceBetween(startPoint, v.getPosition(), Pn.EUCLIDEAN);
//				if (factor > 1E-4) {
//					result[i * 3 + 0] += alpha * (pos[0] - startPoint[0]) / factor;
//					result[i * 3 + 1] += alpha * (pos[1] - startPoint[1]) / factor;
//					result[i * 3 + 2] += alpha * (pos[2] - startPoint[2]) / factor;
//				}
//			}
//		}
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
    
}
