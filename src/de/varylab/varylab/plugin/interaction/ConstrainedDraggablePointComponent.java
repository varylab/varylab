package de.varylab.varylab.plugin.interaction;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;

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
		if(constraint != null) {
			newCoords = constraint.project(newCoords);
		}
		point.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Inlined(newCoords,4));
	}
}
