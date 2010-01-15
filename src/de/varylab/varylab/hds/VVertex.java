package de.varylab.varylab.hds;

import de.jtem.halfedgetools.jreality.node.JRVertex;

public class VVertex extends JRVertex<VVertex, VEdge, VFace> {

	private Integer
		solverIndex = -1;
	private boolean
		variable = true;

	public Integer getSolverIndex() {
		return solverIndex;
	}
	
	public void setSolverIndex(Integer solverIndex) {
		this.solverIndex = solverIndex;
	}
	
	public boolean isVariable() {
		return variable;
	}
	public void setVariable(boolean variable) {
		this.variable = variable;
	}
	
}
