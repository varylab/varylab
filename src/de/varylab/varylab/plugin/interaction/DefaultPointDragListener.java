package de.varylab.varylab.plugin.interaction;

import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;

public class DefaultPointDragListener implements PointDragListener {
	
	private DraggablePointComponent dpc = null;
	
	public DefaultPointDragListener(DraggablePointComponent dpc) {
		this.dpc = dpc;
	}
	
	@Override
	public void pointDragStart(PointDragEvent e) {
	}

	@Override
	public void pointDragged(PointDragEvent e) {
		double[] newCoords = new double[]{e.getX(),e.getY(), e.getZ(), 1.0};
		dpc.updateCoords(newCoords);
		dpc.updateComponent();
	}

	@Override
	public void pointDragEnd(PointDragEvent e) {
	}
}