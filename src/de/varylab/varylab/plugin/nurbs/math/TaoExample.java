package de.varylab.varylab.plugin.nurbs.math;


import static de.jtem.jpetsc.InsertMode.INSERT_VALUES;
import de.jtem.jpetsc.Mat;
import de.jtem.jpetsc.Vec;
import de.jtem.jtao.Tao;
import de.jtem.jtao.Tao.GetSolutionStatusResult;
import de.jtem.jtao.TaoAppAddCombinedObjectiveAndGrad;
import de.jtem.jtao.TaoAppAddHess;
import de.jtem.jtao.TaoApplication;

public class TaoExample {
	public static void main(String[] args){
		Tao.Initialize();
		
		Tao t = new Tao(Tao.Method.CG);
		Vec v = new Vec(1);
		v.setValue(0, 100, INSERT_VALUES);
		v.assemble();
		Mat m = Mat.createSeqAIJ(1, 1, 1, null);
		m.assemble();
	
		TaoApplication app = new App1();
		app.setInitialSolutionVec(v);
		app.setHessianMat(m, m);
		t.setTolerances(0, 0, 0, 0);
		t.setApplication(app);
		t.solve();
		
		GetSolutionStatusResult res = t.getSolutionStatus();
		
		System.out.printf("iterations = " + res.iterate + "\n");
		
		double x = app.getSolutionVec().getValue(0);
		System.out.printf("pos = " + x  + "\n");
		System.out.printf("value = " + res.f + "\n");
		
		Tao.Finalize();
	}
	
	public static class App1 
		extends TaoApplication	
		implements TaoAppAddCombinedObjectiveAndGrad, TaoAppAddHess 
	{	
		@Override
		public double evaluateObjectiveAndGradient(Vec xv, Vec gv) {
			double x = xv.getValue(0);
			gv.setValue(0, 2*x, INSERT_VALUES);
			gv.assemble();
			return x*x;
		}
		@Override
		public PreconditionerType evaluateHessian(Vec x, Mat H, Mat Hpre) {
			H.setValue(0, 0, 2, INSERT_VALUES);
			H.assemble();
			return PreconditionerType.SAME_PRECONDITIONER;
		}
	}
}