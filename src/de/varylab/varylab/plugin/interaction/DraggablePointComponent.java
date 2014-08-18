package de.varylab.varylab.plugin.interaction;

import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.tool.InputSlot;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;

public class DraggablePointComponent extends SceneGraphComponent implements PointDragListener {

	private DragEventTool 
		dragTool = new DragEventTool(InputSlot.RIGHT_BUTTON);
	
	private PointSet 
		point = new PointSet("Draggable point",1);
	
	public DraggablePointComponent(double[] coords) {
		super("Draggable point");
		point.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Inlined(coords,4));
		addTool(dragTool);
		dragTool.addPointDragListener(this);
		setGeometry(point);
	}
	
	public void addPointDragListener(PointDragListener l) {
		dragTool.addPointDragListener(l);
	}
	
	public void removePointDragListener(PointDragListener l) {
		dragTool.removePointDragListener(l);
	}

	@Override
	public void pointDragStart(PointDragEvent e) {
	}

	@Override
	public void pointDragged(PointDragEvent e) {
		point.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Inlined(new double[]{e.getX(),e.getY(), e.getZ(), 1.0},4));
	}

	@Override
	public void pointDragEnd(PointDragEvent e) {
	}

}
