package de.varylab.varylab.optimization;

import de.jtem.jtao.Tao.GetSolutionStatusResult;

public interface OptimizationListener {

	public void optimizationStarted(int maxIterations);
	public void optimizationProgress(double[] solution, int interation);
	public void optimizationFinished(GetSolutionStatusResult status, double[] solution);
	
}
