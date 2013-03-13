package de.varylab.varylab.optimization;

import de.jtem.jtao.TaoApplication;

public interface OptimizationListener {

	public void optimizationStarted(TaoApplication app, int maxIterations);
	public void optimizationProgress(TaoApplication app, int interation);
	public void optimizationFinished(TaoApplication app);
	
}
