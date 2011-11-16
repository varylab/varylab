package de.varylab.varylab.plugin.nurbs.math;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.data.ChristoffelInfo;

public class NURBSChristoffelUtility {
	
	public static ChristoffelInfo christoffel(NURBSSurface ns, double u, double v){
		
		ChristoffelInfo cI = new ChristoffelInfo();
		double[] U = ns.getUKnotVector();
		double[] V = ns.getVKnotVector();
		int p = ns.getUDegree();
		int q = ns.getVDegree();
		
		double[][][]SKL1 = new double[p+1][q+1][4];
		double[][][]SKL = new double[p+1][q+1][3];

		int nl = ns.getControlMesh().length-1;
		int ml = ns.getControlMesh()[0].length-1;
		NURBSAlgorithm.SurfaceDerivatives(ml, p, U, nl, q, V, ns.getControlMesh(), u, v, 4, SKL1);		
		double [][][] Aders = new double[SKL1.length][SKL1[0].length][3];
		double [][] wders = new double[SKL1.length][SKL1[0].length];
		for (int i = 0; i < SKL1.length; i++) {
			for (int j = 0; j < SKL1[0].length; j++) {
				wders[i][j]=SKL1[i][j][3];
				Aders[i][j][0] = SKL1[i][j][0];
				Aders[i][j][1] = SKL1[i][j][1];
				Aders[i][j][2] = SKL1[i][j][2];
			}
		}
		NURBSAlgorithm.RatSurfaceDerivs(Aders, wders, p+q, SKL);
		cI.setSu(SKL[1][0]);
		cI.setSv(SKL[0][1]);
		cI.setSuv(SKL[1][1]);
		if(p <= 1) {
			cI.setSuu(new double[]{0,0,0});
		} else {
			cI.setSuu(SKL[2][0]);
		}
		if(q <= 1) {
			cI.setSvv(new double[]{0,0,0});
		} else {
			cI.setSvv(SKL[0][2]);
		}
		
		// partial derivatives of the metric tensor
		
		double Eu = 2 * Rn.innerProduct(cI.getSuu(), cI.getSu());
		double Ev = 2 * Rn.innerProduct(cI.getSuv(), cI.getSu());
		double Fu = Rn.innerProduct(cI.getSuu(), cI.getSv()) + Rn.innerProduct(cI.getSu(), cI.getSuv());
		double Fv = Rn.innerProduct(cI.getSvv(), cI.getSu()) + Rn.innerProduct(cI.getSv(), cI.getSuv());
		double Gu = 2 * Rn.innerProduct(cI.getSuv(), cI.getSv());
		double Gv = 2 * Rn.innerProduct(cI.getSvv(), cI.getSv());
		
		// inverse of the metric tensor
		
		double detMetric = Rn.innerProduct(cI.getSu(), cI.getSu()) * Rn.innerProduct(cI.getSv(), cI.getSv()) - 2 * Rn.innerProduct(cI.getSu(), cI.getSv());
		double g11 = 1/detMetric * Rn.innerProduct(cI.getSv(), cI.getSv());
		double g12 = -1/detMetric * Rn.innerProduct(cI.getSu(), cI.getSv());
		double g21 = -1/detMetric * Rn.innerProduct(cI.getSu(), cI.getSv());
		double g22 = 1/detMetric * Rn.innerProduct(cI.getSu(), cI.getSu());
		
		cI.setG111(0.5 * (g11 * Eu + g12 * (2 *Fu - Ev)));
		cI.setG121(0.5 * (g11 * Ev + g12 * Gu));
		cI.setG112(0.5 * (g21 * Eu + g22 * (2 *Fu - Ev)));
		cI.setG122(0.5 * (g21 * Ev + g22 * Gu));
		cI.setG211(cI.getG121());
		cI.setG212(cI.getG122());
		cI.setG221(0.5 * (g11 * (2 * Fv - Gu) + g12 * Gv));
		cI.setG222(0.5 * (g21 * (2 * Fv - Gu) + g22 * Gv));

		return cI;
	}

}
