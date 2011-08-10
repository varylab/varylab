package de.varylab.varylab.math;

import de.jreality.math.Rn;
import de.jtem.jpetsc.Vec;
import de.jtem.jtao.TaoAppAddCombinedObjectiveAndGrad;
import de.jtem.jtao.TaoApplication;

public class LeastSquaresSphereTao extends TaoApplication implements TaoAppAddCombinedObjectiveAndGrad {

	private double[][] 
	                 data = null;
	
	public LeastSquaresSphereTao(double[][] points) {
		data = points;
	}
	
	@Override
	public double evaluateObjectiveAndGradient(Vec x, Vec g) {
		SimpleEnergy E = new SimpleEnergy();
		g.set(0);
		double[] m = new double[]{x.getValue(0),x.getValue(1),x.getValue(2)};
		double r = x.getValue(3);
		for (int i = 0; i < data.length; i++) {
			double energy = Rn.euclideanDistanceSquared(m, data[i])-r*r;  
			E.add(energy*energy);
			g.add(0, 4*(m[0]-data[i][0])*energy);
			g.add(1, 4*(m[1]-data[i][1])*energy);
			g.add(2, 4*(m[2]-data[i][2])*energy);
			g.add(3, -4*r*energy);
		}
		g.assemble();
		return E.get();
	}

}