package de.varylab.varylab.plugin.interaction;

import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.tool.InputSlot;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;

public class DraggablePointComponent extends SceneGraphComponent {

	protected DragEventTool 
		dragTool = new DragEventTool(InputSlot.RIGHT_BUTTON);
	
	protected PointSet 
		point = new PointSet("Draggable point",1);

	public DraggablePointComponent(double[] coords, PointDragListener listener) {
		super("Draggable point");
		updateCoords(coords);
		addTool(dragTool);
		dragTool.addPointDragListener(listener);
		setGeometry(point);
	}

	public DraggablePointComponent(double[] coords) {
		super("Draggable point");
		updateCoords(coords);
		addPointDragListener(new DefaultPointDragListener());
		addTool(dragTool);
		setGeometry(point);
	}

	public DraggablePointComponent() {
		super("Draggable point");
		updateCoords(new double[]{0,0,0,0});
		addPointDragListener(new DefaultPointDragListener());
		addTool(dragTool);
		setGeometry(point);
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
		point.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Inlined(newCoords,4));
	}
	
	public class DefaultPointDragListener implements PointDragListener {
		@Override
		public void pointDragStart(PointDragEvent e) {
		}

		@Override
		public void pointDragged(PointDragEvent e) {
			double[] newCoords = new double[]{e.getX(),e.getY(), e.getZ(), 1.0};
			updateCoords(newCoords);
		}

		@Override
		public void pointDragEnd(PointDragEvent e) {
		}
	}
}
