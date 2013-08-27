package de.varylab.varylab.optimization;

import de.jtem.jtao.Tao.GetSolutionStatusResult;

public interface OptimizationListener {

	public void optimizationStarted(AbstractOptimizationJob job, int maxIterations);
	public void optimizationProgress(AbstractOptimizationJob job, double[] solution, int interation);
	public void optimizationFinished(AbstractOptimizationJob job, GetSolutionStatusResult status, double[] solution);
	
}
