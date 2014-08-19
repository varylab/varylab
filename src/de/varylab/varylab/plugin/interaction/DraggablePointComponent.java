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
	
	private boolean useDefaultDraggListener = true;
	
	public DraggablePointComponent(double[] coords) {
		super("Draggable point");
		updateCoords(coords);
		addTool(dragTool);
		dragTool.addPointDragListener(this);
		setGeometry(point);
	}

	public void setUseDefaultDraggListener(boolean useDefaultDraggListener) {
		this.useDefaultDraggListener = useDefaultDraggListener;
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

	@Override
	public void pointDragStart(PointDragEvent e) {
	}

	@Override
	public void pointDragged(PointDragEvent e) {
		double[] newCoords = new double[]{e.getX(),e.getY(), e.getZ(), 1.0};
		if(useDefaultDraggListener) {
			updateCoords(newCoords);
		}
	}

	public void updateCoords(double[] newCoords) {
		point.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Inlined(newCoords,4));
	}

	@Override
	public void pointDragEnd(PointDragEvent e) {
	}

}
