package de.varylab.varylab.plugin.remeshing;

import de.jreality.math.Rn;

public class LatticeUtility {

	public static double[] nextLatticePointInDirection(double[] startIJ, Slope ijSlope) {
		double[] nextIJ = new double[]{startIJ[0], startIJ[1]};  
		if((nextIJ[0] % 1 != 0) || (nextIJ[1] % 1 != 0)) {
			double 
				lx = (ijSlope.dx==0)?
						0:(((ijSlope.dx>0)?
								1-((nextIJ[0])%1):-((nextIJ[0])%1))/ijSlope.dx),
				ly = (ijSlope.dy==0)?
						0:(((ijSlope.dy>0)?
								1-((nextIJ[1])%1):-((nextIJ[1])%1))/ijSlope.dy);
			Rn.add(nextIJ,nextIJ,Rn.times(null,Math.max(lx, ly),ijSlope.toArray()));
		} else {
			Rn.add(nextIJ,nextIJ,ijSlope.toArray());
		}
		return nextIJ;
	}
	
}
