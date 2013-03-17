package de.varylab.varylab.math;

import de.jtem.jtao.Tao;
import de.jtem.jtao.TaoApplication;

public interface OptimizationListener {

	public void optimizationStarted(Tao solver, TaoApplication app, int maxIterations);
	public void optimizationProgress(Tao solver, TaoApplication app, int interation);
	public void optimizationFinished(Tao solver, TaoApplication app);
	
}
