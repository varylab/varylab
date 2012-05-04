package de.varylab.varylab.plugin.nurbs.data;

import java.util.LinkedList;

import de.varylab.varylab.plugin.nurbs.NURBSSurface;


public class NURBSTree {
	
	private NURBSTreeNode dummy;
	
//	public NURBSTree(LinkedList<NURBSTreeNode> bezierList){
//		this.dummy = new NURBSTreeNode(bezierList);
//	}
	
	public NURBSTree(LinkedList<NURBSSurface> bezierList){
		this.dummy = new NURBSTreeNode(bezierList);
	}

	public NURBSTreeNode getDummy() {
		return dummy;
	}

	public void setDummy(NURBSTreeNode dummy) {
		this.dummy = dummy;
	}
	
	public NURBSTreeNode getNode(NURBSTreeNode ntn){
		for (NURBSTreeNode listNode : dummy.getBezierList()) {
			if(ntn.equals(listNode)){
				return listNode;
			}
			else if(ntn.isInside(listNode)){
				if(listNode.getFirstChild() == null){
					return null;
				}
				else{
					NURBSTreeNode testChild = listNode.childContainsThisNode(ntn);
					if(testChild.equals(ntn)){
						return testChild;
					}
					while(!testChild.equals(ntn)){
						if(testChild.getFirstChild() == null){
							return null;
						}
						else{
							testChild = testChild.childContainsThisNode(ntn);
							if(testChild.equals(ntn)){
								return testChild;
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public LinkedList<NURBSSurface> getAllChilds(NURBSSurface ns){
		NURBSTreeNode ntn = new NURBSTreeNode(ns);
		for (NURBSTreeNode listNode : dummy.getBezierList()) {
			if(ntn.equals(listNode)){
				return listNode.getAllChilds();
			}
			else if(ntn.isInside(listNode)){
				NURBSTreeNode testChild = listNode;
				while(!ntn.equals(testChild)){
					LinkedList<NURBSTreeNode> childs = testChild.getAllChildNodes();
					for (NURBSTreeNode child : childs) {
						if(ntn.isInside(child)){
							testChild = child;
						}
					}
				}
				return testChild.getAllChilds();
			}
		}
		return null;
	}
	
//	public NURBSTreeNode getNode(NURBSSurface ns){
//		for (NURBSTreeNode listNode : dummy.getBezierList()) {
//			if(ntn.equals(listNode)){
//				return listNode;
//			}
//			else if(ntn.isInside(listNode)){
//				if(listNode.getFirstChild() == null){
//					return null;
//				}
//				else{
//					NURBSTreeNode testChild = listNode.childContainsThisNode(ntn);
//					if(testChild.equals(ntn)){
//						return testChild;
//					}
//					while(!testChild.equals(ntn)){
//						if(testChild.getFirstChild() == null){
//							return null;
//						}
//						else{
//							testChild = testChild.childContainsThisNode(ntn);
//							if(testChild.equals(ntn)){
//								return testChild;
//							}
//						}
//					}
//				}
//			}
//		}
//		return null;
//	}
	
	
//	public void add(LinkedList<NURBSTreeNode> newPatches){
//		NURBSSurface firstNs = newPatches.getFirst().getNs();
//		NURBSSurface lastNs = newPatches.getLast().getNs();
//		double u0 = firstNs.getUKnotVector()[0];
//		double v0 = firstNs.getVKnotVector()[0];
//		double u1 = lastNs.getUKnotVector()[lastNs.getUKnotVector().length - 1];
//		double v1 = lastNs.getVKnotVector()[lastNs.getVKnotVector().length - 1];
//		for (NURBSTreeNode ntn : dummy.getBezierList()) {
//			if(ntn.ListEquals(u0, v0, u1, v1)){
//				if()
//			}
//			
//			
//		}
//	}

}
