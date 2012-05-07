package de.varylab.varylab.plugin.nurbs.data;

import java.util.LinkedList;

import de.varylab.varylab.plugin.nurbs.NURBSSurface;


public class NURBSTree {
	
	private NURBSTreeNode dummy;
	
	public NURBSTree(LinkedList<NURBSSurface> bezierList){
		this.dummy = new NURBSTreeNode(bezierList);
	}

	public NURBSTreeNode getDummy() {
		return dummy;
	}

	public void setDummy(NURBSTreeNode dummy) {
		this.dummy = dummy;
	}

}
