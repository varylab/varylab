package de.varylab.varylab.optimization;

import java.util.Collection;

import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public interface IterationProtocol {

	public int getIteration();
	
	public VarylabOptimizerPlugin getOptimizer();
	
	public Collection<ProtocolValue> getValues();
	
}
