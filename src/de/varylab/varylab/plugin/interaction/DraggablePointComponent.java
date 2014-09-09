package de.varylab.varylab.plugin.interaction;

import de.jreality.scene.Appearance;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.tool.InputSlot;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.PointDragListener;

public class DraggablePointComponent extends SceneGraphComponent {

	protected DragEventTool 
		dragTool = new DragEventTool(InputSlot.RIGHT_BUTTON);
	
	protected PointSet 
		point = new PointSet("Draggable point",1);
	
	protected double[]
		coords = null;

	public DraggablePointComponent(double[] coords, PointDragListener listener) {
		super("Draggable point");
		init(coords);
		addPointDragListener(listener);
	}

	public DraggablePointComponent(double[] coords) {
		super("Draggable point");
		init(coords);
	}

	private void init(double[] coords) {
		updateCoords(coords);
		addTool(dragTool);
		setGeometry(point);
		updateComponent();
		setAppearance(new Appearance());
	}

	public PointSet getPoint() {
		return point;
	}

	public void setPoint(PointSet point) {
		this.point = point;
	}

	public void addPointDragListener(PointDragListener l) {
		dragTool.addPointDragListener(l);
	}
	
	public void removePointDragListener(PointDragListener l) {
		dragTool.removePointDragListener(l);
	}

	public void updateCoords(double[] newCoords) {
		coords = newCoords.clone();
	}
	
	public void updateComponent() {
		point.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Inlined(coords,4));
	}
	
}
