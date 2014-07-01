package de.varylab.varylab.functional;

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

/*
 * TODO: Calculate Hessian!!
 */

public class BetaSquaredFunctional<V extends Vertex<V, E, F>, E extends Edge<V, E, F>, F extends Face<V, E, F>>
		implements Functional<V, E, F> {

	public enum WillmoreMode {NORMAL, SQUARES, WEIGHTED}
		
	private WillmoreMode mode = WillmoreMode.NORMAL;
	
	public void setMode(String mode) {
		if(mode.equals("normal")) this.mode = WillmoreMode.NORMAL;
		if(mode.equals("squares")) this.mode = WillmoreMode.SQUARES;
		if(mode.equals("weighted")) this.mode = WillmoreMode.WEIGHTED;
	}
	
	public void setMode(WillmoreMode mode) {
		this.mode = mode;
	}
	
	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> void evaluate(HDS hds,
			DomainValue x, Energy E, Gradient G, Hessian H) {
		if (E != null) {
			E.set(evaluate(hds, x));
		}
		if (G != null) {
			evaluateGradient(hds, x, G);
		}
		if (H != null) {
			evaluateHessian(hds, x, H);
		}
	}

	// Calculate the energy of a given configuration
	@SuppressWarnings("incomplete-switch")
	public double evaluate(HalfEdgeDataStructure<V, E, F> hds, DomainValue x) {
		double result = 0.0;
		switch(mode) {
		case NORMAL:
			result = -hds.numVertices() * Math.PI;
			break;
		case SQUARES:
			result = 0.0;
			//result = -1.0/3.0*Math.pow(Math.PI, 2)*hds.numVertices();
		}
		
		for (E e : hds.getPositiveEdges()) {
//			if(!HalfEdgeUtils.isBoundaryEdge(e)) {
//				continue;
//			}
			double beta = 0.0;
			
			if(e.getLeftFace() == null) {
				e = e.getOppositeEdge();
			}
			
			if (e.getRightFace() == null) { // boundary edge
				double betaBnd = BetaBnd(hds, x, e);
				beta = (mode==WillmoreMode.NORMAL)?betaBnd:Math.pow(betaBnd,2);
				double betaInf = BetaInfinity(hds,x,e);
				beta += (mode==WillmoreMode.NORMAL)?betaInf:Math.pow(betaInf,2);
			} else {
				beta = (mode==WillmoreMode.NORMAL)?Beta(hds, x, e):Math.pow(Beta(hds, x, e),2);
			}
			
			switch(mode) {
			case NORMAL:
			case SQUARES:
				result += beta;
				break;
			case WEIGHTED:
				double nI = (double) HalfEdgeUtils.outgoingEdges(e.getStartVertex()).size();
				double nJ = (double) HalfEdgeUtils.outgoingEdges(e.getTargetVertex()).size();
				result += (nI+nJ)*beta;
				break;
			}
			//if(print) System.out.println(Math.min(e.getStartVertex().getIndex()+1,e.getTargetVertex().getIndex()+1)+"->"+Math.max(e.getStartVertex().getIndex()+1,e.getTargetVertex().getIndex()+1)+": "+beta/Math.PI);
			
		}
		//System.out.println("########################################");
		//print = false;
		//System.out.println(result);
		return result;
	}

	public void evaluateGradient(
		//input
			HalfEdgeDataStructure<V, E, F> hds,
			DomainValue x,
		//output
			Gradient G
	) {
//		G.setZero();
		for (E e : hds.getPositiveEdges()) {
//			if(!HalfEdgeUtils.isBoundaryEdge(e)) {
//				continue;
//			}
			if(e.getRightFace() == null || e.getLeftFace() == null) {
				//boundary edges
				BetaBndD(hds, x, e, G);
				BetaInfinityD(hds, x, e, G);
			} else {
				//interior edge
				BetaD(hds, x, e, G);
			}
		}
		
	}

	public void evaluateHessian(
		// input
			HalfEdgeDataStructure<V, E, F> hds, DomainValue x,
		// output
			Hessian hess) {
		for (E e: hds.getPositiveEdges()) {
			double[] 
				       vi = new double[3], 
				       vj = new double[3], 
				       vk = new double[3], 
				       vl = new double[3];
			FunctionalUtils.getPosition(e.getStartVertex(), x, vi);
			FunctionalUtils.getPosition(e.getTargetVertex(), x, vj);
			FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vk);
			FunctionalUtils.getPosition(e.getOppositeEdge().getNextEdge().getTargetVertex(), x, vl);
			
			int i = e.getStartVertex().getIndex();
			int j = e.getTargetVertex().getIndex();
			int k = e.getNextEdge().getTargetVertex().getIndex();
			int l = e.getOppositeEdge().getNextEdge().getTargetVertex().getIndex();
			
			double[] dkd = new double[4];
			
			BetaDkD(vl, vk, vi, vj, e, dkd);
			for(int m=0;m<3;m++) {
				hess.add(i+m, l+m, dkd[0]);
				hess.add(i+m, k+m, dkd[1]);
				hess.add(i+m, i+m, dkd[2]);
				hess.add(i+m, j+m, dkd[3]);
			}
			
			BetaDkD(vk, vl, vj, vi, e, dkd);
			for(int m=0;m<3;m++) {
				hess.add(j+m, k+m, dkd[0]);
				hess.add(j+m, l+m, dkd[1]);
				hess.add(j+m, j+m, dkd[2]);
				hess.add(j+m, i+m, dkd[3]);
			}
			
			BetaDkD(vi, vj, vk, vl, e, dkd);
			for(int m=0;m<3;m++) {
				hess.add(k+m, i+m, dkd[0]);
				hess.add(k+m, j+m, dkd[1]);
				hess.add(k+m, k+m, dkd[2]);
				hess.add(k+m, l+m, dkd[3]);
			}
			
			BetaDkD(vj, vi, vl, vk, e, dkd);
			for(int m=0;m<3;m++) {
				hess.add(l+m, j+m, dkd[0]);
				hess.add(l+m, i+m, dkd[1]);
				hess.add(l+m, l+m, dkd[2]);
				hess.add(l+m, k+m, dkd[3]);
			}
			
		}
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return hds.numVertices() * 3;
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(
			HDS hds) {
		int[][] nzp = new int[3*hds.numVertices()][3*hds.numVertices()]; 
		
		for(int i=0;i<3*hds.numVertices();i++) {
			for (int j=0;j<3*hds.numVertices();j++) {
				nzp[i][j] = 1;
			}
		}
		
		return nzp;
	}

	@Override
	public boolean hasHessian() {
		return false;

	}

	// "copy" of Peter Schroeder's code
	// translate vi to the origin and apply a Moebius transformation M with
	// center vi
	// and radius |vj-vi| to the vertices.
	// output: a and b are the vectors connecting the images of the vertices
	// vk->vj and
	// vj->vl respectively.
	// return: square of the radius of the sphere used for the Moebius
	// transformation
	// Hence the angle between the circumcircles of the triangles
	// is now the angle between a and b!
	public double Moebius(
		// input
			double[] vi, double[] vj, double[] vk, double[] vl,
		// output
			double[] a, double[] b) 
	{
		// translate vi to origin
		vj = Rn.subtract(null, vj, vi);
		vk = Rn.subtract(null, vk, vi);
		vl = Rn.subtract(null, vl, vi);

		// calculate moebius transform
		double lj2 = Rn.innerProduct(vj, vj);
		double lk2 = Rn.innerProduct(vk, vk);
		double ll2 = Rn.innerProduct(vl, vl);
		Rn.subtract(a, vj, Rn.times(null, lj2 / lk2, vk));
		Rn.subtract(b, Rn.times(null, lj2 / ll2, vl), vj);
		return lj2;
	}

	// Scaled version of the above. no division needed. if only interested in
	// the angle between a and b this is perfectly fine.
	public double MoebiusScaled(
		// input
			double[] vi, double[] vj, double[] vk, double[] vl,
		// output
			double[] a, double[] b) 
	{
		// translate vi to origin
		vj = Rn.subtract(null, vj, vi);
		vk = Rn.subtract(null, vk, vi);
		vl = Rn.subtract(null, vl, vi);
		double lj2 = Rn.innerProduct(vj, vj);
		double lk2 = Rn.innerProduct(vk, vk);
		double ll2 = Rn.innerProduct(vl, vl);
		Rn.subtract(a, Rn.times(null, lk2, vj), Rn.times(null, lj2, vk));
		Rn.subtract(b, Rn.times(null, lj2, vl), Rn.times(null, ll2, vj));
		return lj2;
	}

	// Calculate the external angle of two circumcircles adjacent to the given
	// edge e in hds
	public double Beta(HalfEdgeDataStructure<V, E, F> hds, DomainValue x, E e) {
		double[] 
		       vi = new double[3], 
		       vj = new double[3], 
		       vk = new double[3], 
		       vl = new double[3], 
		       a_kj = new double[3], 
		       b_jl = new double[3];
		
		FunctionalUtils.getPosition(e.getStartVertex(), x, vi);
		FunctionalUtils.getPosition(e.getTargetVertex(), x, vj);
		FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vk);
		FunctionalUtils.getPosition(e.getOppositeEdge().getNextEdge().getTargetVertex(), x, vl);
		MoebiusScaled(vi, vj, vk, vl, a_kj, b_jl);
		
		return FunctionalUtils.angle(a_kj, b_jl);
	}

	// Calculate beta for boundary edges
	public double BetaBnd(HalfEdgeDataStructure<V, E, F> hds, DomainValue x, E e) {
		double[] vi = new double[3], vj = new double[3], vk = new double[3];
		
		if (e.getLeftFace() == null) {
			e = e.getOppositeEdge();
		} 

		FunctionalUtils.getPosition(e.getStartVertex(), x, vi);
		FunctionalUtils.getPosition(e.getTargetVertex(), x, vj);
		FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vk);

		return Math.PI - FunctionalUtils.angle(vi,vk,vj);
	}

	// Calculate beta for edges connected to infinity
	public double BetaInfinity(HalfEdgeDataStructure<V, E, F> hds,
			DomainValue x, E e) {
		double[] 
		       vi = new double[3], 
		       vj = new double[3], 
		       vk = new double[3];
		
		if (e.getRightFace() == null) {
			e = e.getOppositeEdge();
		} 
		
		FunctionalUtils.getPosition(e.getStartVertex(), x, vi);
		FunctionalUtils.getPosition(e.getTargetVertex(), x, vj);
		FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(),x,vk);
		
		return Math.PI - FunctionalUtils.angle(vi, vj, vk);
	}

	// Calculate the derivative of the Moebius transformation D (3x3 matrix)
	public void MoebiusD(
		// input
			double[] vi, double[] vj, double[] vk, double[] vl,
		// output
			double[][] D) 
	{
		double el = Rn.euclideanNorm(Rn.subtract(null, vj, vi));
		double[] tmp = Rn.subtract(null, vk, vi);
		double kl2 = Rn.innerProduct(tmp, tmp);
		double scaling = el * el / (kl2 * kl2);

		FunctionalUtils.outerProduct(tmp, D);
		Rn.times(D, -2.0, D);
		for (int i = 0; i < 3; i++) {
			D[i][i] += kl2;
		}
		Rn.times(D, scaling, D);
	}

	public void BetaD(
		// input
			HalfEdgeDataStructure<V, E, F> hds, DomainValue x, E e,
		// output
			Gradient G) 
	{
		int 
			i = e.getStartVertex().getIndex(), 
			j = e.getTargetVertex().getIndex(), 
			k = e.getNextEdge().getTargetVertex().getIndex(), 
			l = e.getOppositeEdge().getNextEdge().getTargetVertex().getIndex();

		double[] 
		       vi = new double[3],
		       vj = new double[3],
		       vk = new double[3], 
		       vl = new double[3];

		FunctionalUtils.getPosition(e.getStartVertex(), x, vi);
		FunctionalUtils.getPosition(e.getTargetVertex(), x, vj);
		FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vk);
		FunctionalUtils.getPosition(e.getOppositeEdge().getNextEdge().getTargetVertex(), x, vl);
		
		double[] dk = new double[3];

		// derivative wrt i
		BetaDk(vl, vk, vi, vj, e, dk);
		FunctionalUtils.addVectorToGradient(G, 3*i, dk);

		// derivative wrt j
		BetaDk(vk, vl, vj, vi, e, dk);
		FunctionalUtils.addVectorToGradient(G, 3*j, dk);

		// derivative wrt k
		BetaDk(vi, vj, vk, vl, e, dk);
		FunctionalUtils.addVectorToGradient(G, 3*k, dk);

		// derivative wrt l
		BetaDk(vj, vi, vl, vk, e, dk);
		FunctionalUtils.addVectorToGradient(G, 3*l, dk);
	}

	public void BetaDk(
		// input
			double[] vi, double[] vj, double[] vk, double[] vl, E e,
		// output
			double[] dk) 
	{
		double[] 
		       	a_kj = new double[3],
		       	b_jl = new double[3],
		       	angleGrad = new double[3];

		Moebius(vi, vj, vk, vl, a_kj, b_jl);
		
		if(Math.sin(FunctionalUtils.angle(a_kj, b_jl))<10E-14) {
			for(int i = 0; i < 3; i++) {
				dk[i]=0.0;
			}
		}
		else {
			FunctionalUtils.angleGradient(a_kj, b_jl, angleGrad);
			
			double[][] DpM = new double[3][3];
			MoebiusD(vi, vj, vk, vl, DpM);
	
			for (int i = 0; i < DpM.length; i++) {
				dk[i] = -Rn.innerProduct(angleGrad, DpM[i]);
				if(mode == WillmoreMode.SQUARES) {
					dk[i] *= 2*FunctionalUtils.angle(a_kj, b_jl);
				}
				if(mode == WillmoreMode.WEIGHTED) {
					double nI = (double) HalfEdgeUtils.outgoingEdges(e.getStartVertex()).size();
					double nJ = (double) HalfEdgeUtils.outgoingEdges(e.getTargetVertex()).size();
					dk[i] *= 2*FunctionalUtils.angle(a_kj, b_jl)*(nI+nJ);
				}
			}
		}
	}

	public void BetaBndD(
		// input
			HalfEdgeDataStructure<V, E, F> hds, DomainValue x, E e,
		// output
			Gradient G) 
	{
		double[] 
		       vi = new double[3],
		       vj = new double[3],
		       vk = new double[3],
		       di = new double[3],
		       dj = new double[3],
		       dk = new double[3];
		
		if (e.getLeftFace() == null) {
			e = e.getOppositeEdge();
		} 
		
		int 
			i = e.getStartVertex().getIndex(),
			j = e.getTargetVertex().getIndex(),
			k = e.getNextEdge().getTargetVertex().getIndex();

		FunctionalUtils.getPosition(e.getStartVertex(), x, vi);
		FunctionalUtils.getPosition(e.getTargetVertex(), x, vj);
		FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vk);

		FunctionalUtils.angleGradient(vj, vk, vi, dj, dk, di);
		//derivative with respect to vi
		if(mode == WillmoreMode.SQUARES) {
			di = Rn.times(null, 2*BetaBnd(hds, x, e), di);
			dj = Rn.times(null, 2*BetaBnd(hds, x, e), dj);
			dk = Rn.times(null, 2*BetaBnd(hds, x, e), dk);
		}
		
		if(mode == WillmoreMode.WEIGHTED) {
			double nI = (double) HalfEdgeUtils.outgoingEdges(e.getStartVertex()).size();
			double nJ = (double) HalfEdgeUtils.outgoingEdges(e.getTargetVertex()).size();
			di = Rn.times(null, 2*(nI+nJ)*BetaBnd(hds, x, e), di);
			dj = Rn.times(null, 2*(nI+nJ)*BetaBnd(hds, x, e), dj);
			dk = Rn.times(null, 2*(nI+nJ)*BetaBnd(hds, x, e), dk);
		}		
		FunctionalUtils.addVectorToGradient(G, 3*i, Rn.negate(null, di));
		FunctionalUtils.addVectorToGradient(G, 3*j, Rn.negate(null, dj));
		FunctionalUtils.addVectorToGradient(G, 3*k, Rn.negate(null, dk));
	}

	public void BetaInfinityD(
		// input
			HalfEdgeDataStructure<V, E, F> hds, DomainValue x, E e,
		// output
			Gradient G) 
	{
		double[] 
		       vi = new double[3], 
		       vj = new double[3], 
		       vk = new double[3],
		       di = new double[3],
		       dj = new double[3],
		       dk = new double[3];
		
		if (e.getLeftFace() != null) {
			e = e.getOppositeEdge();
		} 
		
		int 
			i = e.getStartVertex().getIndex(),
			j = e.getTargetVertex().getIndex(),
			k = e.getNextEdge().getTargetVertex().getIndex();

		FunctionalUtils.getPosition(e.getStartVertex(), x, vi);
		FunctionalUtils.getPosition(e.getTargetVertex(), x, vj);
		FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vk);

		FunctionalUtils.angleGradient(vi, vj, vk, di, dj, dk);

		if(mode == WillmoreMode.SQUARES) {
			di = Rn.times(null, 2*BetaInfinity(hds, x, e), di);
			dj = Rn.times(null, 2*BetaInfinity(hds, x, e), dj);
			dk = Rn.times(null, 2*BetaInfinity(hds, x, e), dk);
		}
		
		if(mode == WillmoreMode.WEIGHTED) {
			double nI = (double) HalfEdgeUtils.outgoingEdges(e.getStartVertex()).size();
			double nJ = (double) HalfEdgeUtils.outgoingEdges(e.getTargetVertex()).size();
			di = Rn.times(null, 2*(nI+nJ)*BetaInfinity(hds, x, e), di);
			dj = Rn.times(null, 2*(nI+nJ)*BetaInfinity(hds, x, e), dj);
			dk = Rn.times(null, 2*(nI+nJ)*BetaInfinity(hds, x, e), dk);
		}		
		// Note: angleGradient points in the opposite direction since the angle 
		//       is Pi-angle(vi,vj,vk)!
		FunctionalUtils.addVectorToGradient(G, 3*i, Rn.negate(null, di));
		FunctionalUtils.addVectorToGradient(G, 3*j, Rn.negate(null, dj));
		FunctionalUtils.addVectorToGradient(G, 3*k, Rn.negate(null, dk));
	}
	
	private void BetaDkD (
		// input	
			double[] vi, double[] vj, double[] vk, double[] vl, E e,
		// output
			double [] dkd
		)
	{
		double [] a = new double[3],
				b = new double[3],
				c = new double[3],
				d = new double[3],
				A = new double[3],
				B = new double[3],
				C = new double[3],
				D = new double[3],				
				a_kj = new double[3],
				b_jl = new double[3];
		
		Rn.subtract(a, vj, vk);
		Rn.subtract(b, vl, vj);
		Rn.subtract(c, vi, vl);
		Rn.subtract(d, vk, vi);
		
		Rn.normalize(A, a);
		Rn.normalize(B, b);
		Rn.normalize(C, c);
		Rn.normalize(D, d);
		
		MoebiusScaled(vi, vj, vk, vl, a_kj, b_jl);
		double beta = FunctionalUtils.angle(a_kj, b_jl);
		
		double aTerm = Math.cos(beta)/Rn.euclideanNormSquared(a)-Rn.innerProduct(B, C)/(Rn.euclideanNorm(a)*Rn.euclideanNorm(d));
		double bTerm = Rn.innerProduct(A, C)/(Rn.euclideanNorm(b)*Rn.euclideanNorm(d))+Rn.innerProduct(C, D)/(Rn.euclideanNorm(b)*Rn.euclideanNorm(a));
		double cTerm = -(Rn.innerProduct(A, B)/(Rn.euclideanNorm(c)*Rn.euclideanNorm(d))+Rn.innerProduct(B, D)/(Rn.euclideanNorm(c)*Rn.euclideanNorm(a)));
		double dTerm = -(Math.cos(beta)/Rn.euclideanNormSquared(d)-Rn.innerProduct(B, C)/(Rn.euclideanNorm(a)*Rn.euclideanNorm(d)));
		
		// "second derivatives" in order i,j,k,l
		dkd[0] = cTerm-dTerm;
		dkd[1] = aTerm-bTerm;
		dkd[2] = dTerm-aTerm;
		dkd[3] = bTerm-cTerm;
		
		if(beta<10E-14) {
			Rn.times(dkd, 0, dkd);
		}
		else {
			if(mode == WillmoreMode.NORMAL) {
				Rn.times(dkd, -1/Math.sin(beta), dkd);
			}
			if(mode == WillmoreMode.SQUARES) {
				Rn.times(dkd, -beta/Math.sin(beta), dkd);
			}
			if(mode == WillmoreMode.WEIGHTED) {
				double nI = (double) HalfEdgeUtils.outgoingEdges(e.getStartVertex()).size();
				double nJ = (double) HalfEdgeUtils.outgoingEdges(e.getTargetVertex()).size();
				Rn.times(dkd, -beta*(nI+nJ)/Math.sin(beta), dkd);
			}
		}
	}

	@Override
	public boolean hasGradient() {
		return true;
	}
	
}
