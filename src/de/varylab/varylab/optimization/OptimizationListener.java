package de.varylab.varylab.optimization;

import de.jtem.jpetsc.Vec;
import de.jtem.jtao.Tao;

public interface OptimizationListener {

	public void optimizationFinished(Tao solver, Vec solution);
	
}
