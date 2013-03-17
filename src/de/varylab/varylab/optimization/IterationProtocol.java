package de.varylab.varylab.optimization;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.varylab.varylab.plugin.VarylabOptimizerPlugin;

public class IterationProtocol {

	private VarylabOptimizerPlugin
		optimizer = null;
	private List<ProtocolValue>
		values = new LinkedList<ProtocolValue>();
	private long
		id = -1;
	
	public IterationProtocol(VarylabOptimizerPlugin optimizer, List<ProtocolValue> values, long id) {
		this.optimizer = optimizer;
		this.values = values;
		this.id = id;
	}

	public VarylabOptimizerPlugin getOptimizer() {
		return optimizer;
	}
	public List<ProtocolValue> getValues() {
		return Collections.unmodifiableList(values);
	}
	public long getSeriesId() {
		return id;
	}
	
}
