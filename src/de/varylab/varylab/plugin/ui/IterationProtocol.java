package de.varylab.varylab.plugin.ui;

import java.util.Collection;

import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public interface IterationProtocol {

	public int getIteration();
	
	public VarylabOptimizerPlugin getOptimizer();
	
	public Collection<ProtocolValue> getValues();
	
}
