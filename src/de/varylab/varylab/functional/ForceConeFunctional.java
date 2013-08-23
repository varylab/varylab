package de.varylab.varylab.functional;

import static de.jtem.halfedgetools.functional.FunctionalUtils.getPosition;
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

public class ForceConeFunctional <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> implements Functional<V, E, F> {

	double[] 
		pv = new double[3],
		pvi = new double[3],
		pv1 = new double[3],
		pvi1 = new double[3],
		pvim = new double[3],
		pvip = new double[3],
		veci = new double[3],
		vecip = new double[3],
		tmp = new double[3];
	
	/*
	 * Additional terms
	 */
	//angle conditions
	final int addTerm = 0;
	//distance conditions
	final int distTerm = 0;
	//going vertical conditions
	final int vertTerm = 0;

	
	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluate(HDS hds,
		DomainValue x, 
		Energy E, 
		Gradient G, 
		Hessian H
	) {
		double fac;
		double check = 0.;
		int numc = 0;
		
		/*
		 * 
		 * Cone energy
		 */
		for (E e : hds.getPositiveEdges()) {
			V v = e.getStartVertex();
			V vi = e.getTargetVertex();
			getPosition(v, x, pv);
			getPosition(vi, x, pvi);
			Rn.subtract(veci, pv, pvi);
			if (E != null) {
				E.add(Math.pow(veci[0]*veci[0] + veci[1]*veci[1] - veci[2]*veci[2],2));
				if(Math.abs(Math.atan(veci[2]*veci[2]/ (veci[0]*veci[0] + veci[1]*veci[1]))-Math.atan(1)) > 0.1){
					check += Math.abs(Math.atan(veci[2]*veci[2]/ (veci[0]*veci[0] + veci[1]*veci[1]))-Math.atan(1));
					numc++;
				}
			}
			if (G != null) {
				fac =2*(veci[0]*veci[0] + veci[1]*veci[1] - veci[2]*veci[2]);
				G.add(3*v.getIndex(), 2*veci[0]*fac);
				G.add(3*v.getIndex()+1, 2*veci[1]*fac);
				G.add(3*v.getIndex()+2, -2*veci[2]*fac);
				
				G.add(3*vi.getIndex(), -2*veci[0]*fac);
				G.add(3*vi.getIndex()+1, -2*veci[1]*fac);
				G.add(3*vi.getIndex()+2, 2*veci[2]*fac);
			}
		}
		
		double ip, ip2d;
		//Special case of valence 6 edges (?)
		double phi = 2*Math.PI*Math.sin(Math.PI/4)/3;
		double denom, denom2d;
		double n_veci, n_vecip;
		
		check = check/numc;
		System.out.println("average angle defect "+check);
		System.out.println("numc "+numc); 

		
		/*
		 * Angle terms: Additional energy term for spreading edges equally on cones 
		 */
		if(addTerm == 0){
			
		}
		
		else{
		
		for (V v : hds.getVertices()) {
			for (E e1:HalfEdgeUtils.incomingEdges(v)){
				for (E e2:HalfEdgeUtils.incomingEdges(v)){
					if(e1==e2)
						continue;
					
					V v1 = e1.getStartVertex();
					V v2 = e2.getStartVertex();
					
					getPosition(v1, x, pvi1);
					getPosition(v2, x, pvi);
					getPosition(v, x,  pv);
					Rn.subtract(veci, pv, pvi);
					Rn.subtract(vecip, pv, pvi1);
					ip = Rn.innerProduct(veci, vecip);
					denom = Rn.euclideanNorm(veci)*Rn.euclideanNorm(vecip);
					
					ip2d = Rn.innerProduct(new double[]{veci[0], veci[1]}, new double[]{vecip[0], vecip[1]});
					n_veci=Rn.euclideanNorm(new double[]{veci[0], veci[1]});
					n_vecip=Rn.euclideanNorm(new double[]{vecip[0], vecip[1]});
					denom2d = n_veci*n_vecip;
					
					/*
					 * First try: force maximal angles on cone (using 2d projection)
					 */
					double eps = 0.0001;
					if(addTerm == 1){
						fac = -1;
						if(!(n_veci < eps || n_vecip <eps)){
					if (E != null) {			
						//E.add( Math.pow(Math.cos(phi) - (ip/denom),2));
						E.add( fac*ip2d/denom2d);
						}
					
					if (G != null) {
						
						G.add(3*v.getIndex(), fac * ( 
								(veci[0] +vecip[0])/denom2d  
								- ip2d*vecip[0]/(n_veci * Math.pow(n_vecip,3) )    
							    - ip2d*veci[0]/(n_vecip * Math.pow(n_veci,3) )
								));
						G.add(3*v.getIndex()+1, fac * ( 
								(veci[1] +vecip[1])/denom2d 
								- ip2d*vecip[1]/(n_veci * Math.pow(Rn.euclideanNorm(new double[]{vecip[0], vecip[1]}),3) )    
							    - ip2d*veci[1]/(Rn.euclideanNorm(new double[]{vecip[0], vecip[1]}) * Math.pow(Rn.euclideanNorm(new double[]{veci[0], veci[1]}),3) )
								));
						//G.add(3*v.getIndex()+2, fac * ( (-veci[2] -vecip[2])/denom  + ip*(veci[2]/(denom*Rn.euclideanNormSquared(veci)) + veci[2]/(denom*Rn.euclideanNormSquared(veci)))  ) );
						
						G.add(3*v1.getIndex(), fac * ( 
								(-veci[0])/denom2d  
								+ ip2d*vecip[0]/(n_veci * Math.pow(n_vecip,3) )    
								));
						G.add(3*v1.getIndex() +1, fac * ( 
								(-veci[1])/denom2d  
								+ ip2d*vecip[1]/(n_veci * Math.pow(n_vecip,3) )    
								));						//G.add(3*v1.getIndex()+2,fac * ( (veci[2])/denom  - ip*(vecip[2]/(denom*Rn.euclideanNormSquared(vecip)) )  ));
						
						G.add(3*v2.getIndex(), fac * ( 
								(-vecip[0])/denom2d  
								+ ip2d*veci[0]/(n_vecip* Math.pow(n_veci,3) )    
								));
						G.add(3*v2.getIndex() +1, fac * ( 
								(-vecip[1])/denom2d  
								+ ip2d*veci[1]/(n_vecip * Math.pow(n_veci,3) )    
								));		//G.add(3*v2.getIndex()+2, fac * ( (vecip[2])/denom  - ip*(veci[2]/(denom*Rn.euclideanNormSquared(veci)) )  ) );

						
						/*fac = 2*(Math.cos(phi) - (ip/(Rn.euclideanNorm(veci)*Rn.euclideanNorm(vecip))));
						G.add(3*v.getIndex(), fac * ( (-veci[0] -vecip[0])/denom  + ip*(veci[0]/(denom*Rn.euclideanNormSquared(veci)) + veci[0]/(denom*Rn.euclideanNormSquared(veci)))  ) );
						G.add(3*v.getIndex()+1, fac * ( (-veci[1] -vecip[1])/denom  + ip*(veci[1]/(denom*Rn.euclideanNormSquared(veci)) + veci[1]/(denom*Rn.euclideanNormSquared(veci)))  ) );
						G.add(3*v.getIndex()+2, fac * ( (-veci[2] -vecip[2])/denom  + ip*(veci[2]/(denom*Rn.euclideanNormSquared(veci)) + veci[2]/(denom*Rn.euclideanNormSquared(veci)))  ) );
						
						G.add(3*v1.getIndex(), fac * ( (veci[0])/denom  - ip*(vecip[0]/(denom*Rn.euclideanNormSquared(vecip)) )  ) );
						G.add(3*v1.getIndex()+1, fac * ( (veci[1])/denom  - ip*(vecip[1]/(denom*Rn.euclideanNormSquared(vecip)) )  ));
						G.add(3*v1.getIndex()+2,fac * ( (veci[2])/denom  - ip*(vecip[2]/(denom*Rn.euclideanNormSquared(vecip)) )  ));
						
						G.add(3*v2.getIndex(), fac * ( (vecip[0])/denom  - ip*(veci[0]/(denom*Rn.euclideanNormSquared(veci)) )  ) );
						G.add(3*v2.getIndex()+1,fac * ( (vecip[1])/denom  - ip*(veci[1]/(denom*Rn.euclideanNormSquared(veci)) )  ) );
						G.add(3*v2.getIndex()+2, fac * ( (vecip[2])/denom  - ip*(veci[2]/(denom*Rn.euclideanNormSquared(veci)) )  ) );
*/					}
					}
					}
					/*
					 * Additional energy term for spreading edges equally on cones 
					 * Second try: force 90 degree angles.
					 */
					else if(addTerm == 2){
					if (E != null) {			
						E.add( Math.pow(ip/denom,2) );
					}
					
					if (G != null) {
						fac = 2*ip/denom;
						G.add(3*v.getIndex(),- fac * ( (-veci[0] -vecip[0])/denom  + ip*(veci[0]/(denom*Rn.euclideanNormSquared(veci)) + veci[0]/(denom*Rn.euclideanNormSquared(veci)))  ) );
						G.add(3*v.getIndex()+1, -fac * ( (-veci[1] -vecip[1])/denom  + ip*(veci[1]/(denom*Rn.euclideanNormSquared(veci)) + veci[1]/(denom*Rn.euclideanNormSquared(veci)))  ) );
						G.add(3*v.getIndex()+2, -fac * ( (-veci[2] -vecip[2])/denom  + ip*(veci[2]/(denom*Rn.euclideanNormSquared(veci)) + veci[2]/(denom*Rn.euclideanNormSquared(veci)))  ) );
						
						G.add(3*v1.getIndex(),- fac * ( (veci[0])/denom  - ip*(vecip[0]/(denom*Rn.euclideanNormSquared(vecip)) )  ) );
						G.add(3*v1.getIndex()+1, -fac * ( (veci[1])/denom  - ip*(vecip[1]/(denom*Rn.euclideanNormSquared(vecip)) )  ));
						G.add(3*v1.getIndex()+2,-fac * ( (veci[2])/denom  - ip*(vecip[2]/(denom*Rn.euclideanNormSquared(vecip)) )  ));
						
						G.add(3*v2.getIndex(),- fac * ( (vecip[0])/denom  - ip*(veci[0]/(denom*Rn.euclideanNormSquared(veci)) )  ) );
						G.add(3*v2.getIndex()+1,-fac * ( (vecip[1])/denom  - ip*(veci[1]/(denom*Rn.euclideanNormSquared(veci)) )  ) );
						G.add(3*v2.getIndex()+2, -fac * ( (vecip[2])/denom  - ip*(veci[2]/(denom*Rn.euclideanNormSquared(veci)) )  ) );
					}
					}
					
					/*
					 * Thirs try: force 90 degree angles.
					 */
					else if(addTerm == 3){
					if (E != null) {			
						E.add(ip/denom);
					}
					
					if (G != null) {
						fac = 1;
						G.add(3*v.getIndex(),- fac * ( (-veci[0] -vecip[0])/denom  + ip*(veci[0]/(denom*Rn.euclideanNormSquared(veci)) + veci[0]/(denom*Rn.euclideanNormSquared(veci)))  ) );
						G.add(3*v.getIndex()+1, -fac * ( (-veci[1] -vecip[1])/denom  + ip*(veci[1]/(denom*Rn.euclideanNormSquared(veci)) + veci[1]/(denom*Rn.euclideanNormSquared(veci)))  ) );
						G.add(3*v.getIndex()+2, -fac * ( (-veci[2] -vecip[2])/denom  + ip*(veci[2]/(denom*Rn.euclideanNormSquared(veci)) + veci[2]/(denom*Rn.euclideanNormSquared(veci)))  ) );
						
						G.add(3*v1.getIndex(),- fac * ( (veci[0])/denom  - ip*(vecip[0]/(denom*Rn.euclideanNormSquared(vecip)) )  ) );
						G.add(3*v1.getIndex()+1, -fac * ( (veci[1])/denom  - ip*(vecip[1]/(denom*Rn.euclideanNormSquared(vecip)) )  ));
						G.add(3*v1.getIndex()+2,-fac * ( (veci[2])/denom  - ip*(vecip[2]/(denom*Rn.euclideanNormSquared(vecip)) )  ));
						
						G.add(3*v2.getIndex(),- fac * ( (vecip[0])/denom  - ip*(veci[0]/(denom*Rn.euclideanNormSquared(veci)) )  ) );
						G.add(3*v2.getIndex()+1,-fac * ( (vecip[1])/denom  - ip*(veci[1]/(denom*Rn.euclideanNormSquared(veci)) )  ) );
						G.add(3*v2.getIndex()+2, -fac * ( (vecip[2])/denom  - ip*(veci[2]/(denom*Rn.euclideanNormSquared(veci)) )  ) );
					}
					}
					
					
				}
			}
		}
		}
					/*
					 * Add term to maximize distance between vertices (or better: define envelope)
					 * 
					 */
					if(distTerm == 0){
						
					}
					else if (distTerm == 1){
						
						for (V v1 : hds.getVertices()) {
							for (V v2 : hds.getVertices()){
						
								if(v1.getIndex()>v2.getIndex())
									continue;

								getPosition(v1, x, pv);
								getPosition(v2, x, pvi);
								
								if (E != null) {			
									E.add( - Rn.euclideanDistanceSquared(pv, pvi));
								}
								
								if (G != null) {
									
									G.add(3*v1.getIndex(),	 - 2*(pv[0]-pvi[0]));
									G.add(3*v1.getIndex()+1, - 2*(pv[1]-pvi[1]));
									G.add(3*v1.getIndex()+2, - 2*(pv[2]-pvi[2]));
							
									G.add(3*v2.getIndex(),	  2*(pv[0]-pvi[0]));
									G.add(3*v2.getIndex()+1,  2*(pv[1]-pvi[1]));
									G.add(3*v2.getIndex()+2,  2*(pv[2]-pvi[2]));
								}
								
								
							}
						}
					}
					
					else if (distTerm == 2){
						
						for (V v1 : hds.getVertices()) {
							for (V v2 : hds.getVertices()){
						
								if(v1.getIndex()>v2.getIndex())
									continue;

								getPosition(v1, x, pv);
								getPosition(v2, x, pvi);
								double f = 0.1;
								if (E != null) {			
									E.add(- f *Math.pow(Rn.euclideanDistanceSquared(pv, pvi),2));
								}
								
								if (G != null) {
									fac = -2* Rn.euclideanDistanceSquared(pv, pvi);
									G.add(3*v1.getIndex(),	f* fac * 2*(pv[0]-pvi[0]));
									G.add(3*v1.getIndex()+1,f* fac * 2*(pv[1]-pvi[1]));
									G.add(3*v1.getIndex()+2,f* fac * 2*(pv[2]-pvi[2]));
							
									G.add(3*v2.getIndex(),	 -f* fac* 2*(pv[0]-pvi[0]));
									G.add(3*v2.getIndex()+1, -f* fac* 2*(pv[1]-pvi[1]));
									G.add(3*v2.getIndex()+2, -f* fac* 2*(pv[2]-pvi[2]));
								}
								
								
							}
						}
					}
					
					else if (distTerm == 3){
						
						for (V v1 : hds.getVertices()) {
							for (V v2 : hds.getVertices()){
						
								if(v1.getIndex()>v2.getIndex())
									continue;

								getPosition(v1, x, pv);
								getPosition(v2, x, pvi);
								double f = 0.01;
								if (E != null) {			
									E.add(f/Math.pow(0.1 + Rn.euclideanDistanceSquared(pv, pvi),1));
								}
								
								if (G != null) {
									fac = -f/ Math.pow(0.1 + Rn.euclideanDistanceSquared(pv, pvi),2) ;
									G.add(3*v1.getIndex(),	f* fac * 2*(pv[0]-pvi[0]));
									G.add(3*v1.getIndex()+1,f* fac * 2*(pv[1]-pvi[1]));
									G.add(3*v1.getIndex()+2,f* fac * 2*(pv[2]-pvi[2]));
							
									G.add(3*v2.getIndex(),	 -f* fac* 2*(pv[0]-pvi[0]));
									G.add(3*v2.getIndex()+1, -f* fac* 2*(pv[1]-pvi[1]));
									G.add(3*v2.getIndex()+2, -f* fac* 2*(pv[2]-pvi[2]));
								}
								
								
							}
						}
					}
					else if (distTerm == 4){
						
						for (V v1 : hds.getVertices()) {
							for (V v2 : hds.getVertices()){
						
								if(v1.getIndex()>v2.getIndex())
									continue;

								getPosition(v1, x, pv);
								getPosition(v2, x, pvi);
								
								if (E != null) {			
									E.add( - ( Math.pow(pv[0] - pvi[0],2) + Math.pow(pv[1] - pvi[1],2)  +  0. *Math.pow(pv[2] - pvi[2],2) ) );
								}
								
								if (G != null) {
									
									G.add(3*v1.getIndex(),	 - 2*(pv[0]-pvi[0]));
									G.add(3*v1.getIndex()+1, - 2*(pv[1]-pvi[1]));
									G.add(3*v1.getIndex()+2, - 0.*2*(pv[2]-pvi[2]));
							
									G.add(3*v2.getIndex(),	  2*(pv[0]-pvi[0]));
									G.add(3*v2.getIndex()+1,  2*(pv[1]-pvi[1]));
									G.add(3*v2.getIndex()+2, 0.* 2*(pv[2]-pvi[2]));
								}
								
								
							}
						}
					}
					
					
					if(vertTerm == 0){
						
					}
					else if (vertTerm == 1){
						
						for (E e : hds.getPositiveEdges()) {
							V v1 = e.getStartVertex();
							V v2 = e.getTargetVertex();
							getPosition(v1, x, pv);
							getPosition(v2, x, pvi);
							
							if (E != null) {			
								E.add( -  Math.pow(pv[2] - pvi[2],2) ) ;
							}
							
							if (G != null) {
								
								G.add(3*v1.getIndex()+2, - 2*(pv[2]-pvi[2]));
			
								G.add(3*v2.getIndex()+2,  2*(pv[2]-pvi[2]));
							}
						}
					}
					
					else if (vertTerm == 2){
						
						for (E e : hds.getPositiveEdges()) {
							V v1 = e.getStartVertex();
							V v2 = e.getTargetVertex();
							getPosition(v1, x, pv);
							getPosition(v2, x, pvi);
							
							if (E != null) {			
								E.add(1/(0.001 + Math.pow(pv[2] - pvi[2],2)) ) ;
							}
							
							if (G != null) {
								
								G.add(3*v1.getIndex()+2, - 2* (pv[2] - pvi[2])/Math.pow((0.001 + Math.pow(pv[2] - pvi[2],2)),2) );
			
								G.add(3*v2.getIndex()+2,  2* (pv[2] - pvi[2])/Math.pow((0.001 + Math.pow(pv[2] - pvi[2],2)),2) );
							}
						}
					}
					
				
			
		
		//System.out.println(E.get());
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
