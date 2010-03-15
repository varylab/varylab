package de.varylab.varylab.hds;

import de.jtem.halfedge.Vertex;
import de.varylab.varylab.math.bsp.HasKdTreePosition;
import de.varylab.varylab.math.geom3d.Point;

public class VVertex extends Vertex<VVertex, VEdge, VFace> implements HasKdTreePosition {

	public double[]
	    position = null;
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

	@Override
	public Point getPosition() {
		return new Point(position);
	}

	@Override
	public void setPosition(Point p) {
		position = p.get();
	}
	
}
