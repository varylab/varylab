package de.varylab.varylab.optimization;

import de.jtem.jpetsc.Vec;
import de.jtem.jtao.Tao.GetSolutionStatusResult;

public interface OptimizationListener {

	public void optimizationStarted(int maxIterations);
	public void optimizationProgress(Vec solution, int interation);
	public void optimizationFinished(GetSolutionStatusResult status, Vec solution);
	
}
