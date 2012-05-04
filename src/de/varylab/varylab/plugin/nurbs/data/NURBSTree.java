package de.varylab.varylab.plugin.nurbs.data;

import java.util.LinkedList;

import de.varylab.varylab.plugin.nurbs.NURBSSurface;


public class NURBSTree {
	
	private NURBSTreeNode dummy;
	
	public NURBSTree(LinkedList<NURBSSurface> bezierList){
		this.dummy = new NURBSTreeNode(bezierList);
	}
	
	public LinkedList<NURBSSurface> getAllChilds(NURBSSurface ns){
		NURBSTreeNode ntn = new NURBSTreeNode(ns);
		for (NURBSTreeNode listNode : dummy.getBezierList()) {
			if(ntn.equals(listNode)){
//				System.out.println("in der dummy list");
				return listNode.getAllChilds();
			}
			else if(ntn.isInside(listNode)){
//				System.out.println("is inside dummy list");
				NURBSTreeNode testChild = listNode;
				while(!ntn.equals(testChild)){
					LinkedList<NURBSTreeNode> childs = testChild.getAllChildNodes();
					for (NURBSTreeNode child : childs) {
						if(ntn.isInside(child)){
							testChild = child;
//							System.out.println("innere schleife get all childs");
						}
					}
				}
//				System.out.println("return testChild.getAllChilds();");
				return testChild.getAllChilds();
			}
		}
		return null;
	}
	
	public String toString(){
		String str = new String();
		
		for (NURBSTreeNode listNode : dummy.getBezierList()) {
//			str = str + listNode.getNs().toString();
			str = str + "jetzt kommt 1. kind";
			str = str + listNode.getFirstChild().getNs().toString();
		}
		return str;
		
	}

}
