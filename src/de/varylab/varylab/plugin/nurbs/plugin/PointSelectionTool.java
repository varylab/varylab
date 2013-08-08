package de.varylab.varylab.plugin.nurbs.plugin;

import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.tools.ActionTool;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;

public class PointSelectionTool extends ActionTool {
	private NURBSSurface ns = null;
	
	private double[] selectedPoint = new double[2];
	
	public double[] getSelectedPoint() {
		return selectedPoint;
	}

	public PointSelectionTool() {
		super(InputSlot.SHIFT_LEFT_BUTTON);
		setDescription("Nurbs Point Selection Tool");
	}

	@Override
	public void activate(ToolContext tc) {
		if (ns != null) {
			double[] oc = tc.getCurrentPick().getObjectCoordinates();
			selectedPoint = ns.getClosestPointDomain(oc);
			fire(this);
		}
	}

	@Override
	public void deactivate(ToolContext tc) {
	}

	@Override
	public void perform(ToolContext tc) {

	}

	public void setSurface(NURBSSurface surface) {
		ns = surface;
	}
}
