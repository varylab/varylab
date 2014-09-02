package de.varylab.varylab.plugin.interaction;


public class ConstrainedDraggablePointComponent<C extends PointConstraint> extends DraggablePointComponent {

	protected C 
		constraint = null;

	public ConstrainedDraggablePointComponent(double[] p) {
		super(p);
	}

	public void setConstraint(C constraint) {
		this.constraint = constraint;
	}

	public void updateCoords(double[] newCoords) {
		super.updateCoords(newCoords);
		if(constraint != null) {
			coords = constraint.project(coords);
		}
	}

}
