package de.varylab.varylab.plugin.nurbs.data;

import java.util.LinkedList;

import de.varylab.varylab.plugin.nurbs.NURBSSurface;

public class NURBSTreeNode {
	
	private NURBSSurface ns;
	private LinkedList<NURBSTreeNode> bezierList;
	private NURBSTreeNode firstChild;
	private NURBSTreeNode secondChild;
	private NURBSTreeNode thirdChild;
	private NURBSTreeNode fourthChild;
	
	
	public NURBSTreeNode() {
		this.ns = null;
		this.bezierList = null;
		this.firstChild = null;
		this.secondChild = null;
		this.thirdChild = null;
		this.fourthChild = null;
	}

	
	public NURBSTreeNode(LinkedList<NURBSSurface> bezierList) {
		this.ns = null;
		LinkedList<NURBSTreeNode> list = new LinkedList<NURBSTreeNode>();
		for (NURBSSurface ns : bezierList) {
			NURBSTreeNode ntn = new NURBSTreeNode(ns);
			list.add(ntn);
		}
		this.bezierList = list;
		this.firstChild = null;
		this.secondChild = null;
		this.thirdChild = null;
		this.fourthChild = null;
	}
	
	public NURBSTreeNode(NURBSSurface ns) {
		this.ns = ns;
		this.bezierList = null;
		this.firstChild = null;
		this.secondChild = null;
		this.thirdChild = null;
		this.fourthChild = null;
	}
	
	
	
	public LinkedList<NURBSTreeNode> getAllChildNodes(){
		LinkedList<NURBSTreeNode> childs = new LinkedList<NURBSTreeNode>();
		if(getFirstChild() != null){
			childs.add(firstChild);
			childs.add(secondChild);
			childs.add(thirdChild);
			childs.add(fourthChild);
		}
		else{
			LinkedList<NURBSSurface> nsList = getNs().subdivideIntoFourNewPatches();
			NURBSTreeNode first  = new NURBSTreeNode(nsList.getFirst());
			setFirstChild(first);
			childs.add(first);
			NURBSTreeNode second  = new NURBSTreeNode(nsList.get(1));
			setSecondChild(second);
			childs.add(second);
			NURBSTreeNode third  = new NURBSTreeNode(nsList.get(2));
			setThirdChild(third);
			childs.add(third);
			NURBSTreeNode fourth  = new NURBSTreeNode(nsList.getLast());
			setFourthChild(fourth);
			childs.add(fourth);
		}
		return childs;
	}
	
	public double getU0(){
		return ns.getUKnotVector()[0];
	}
	
	public double getU1(){
		return ns.getUKnotVector()[ns.getUKnotVector().length - 1];
	}
	
	public double getV0(){
		return ns.getVKnotVector()[0];
	}
	
	public double getV1(){
		return ns.getVKnotVector()[ns.getVKnotVector().length - 1];
	}
	
	public NURBSSurface getNs() {
		return ns;
	}

	public void setNs(NURBSSurface ns) {
		this.ns = ns;
	}	

	public LinkedList<NURBSTreeNode> getBezierList() {
		return bezierList;
	}

	public void setBezierList(LinkedList<NURBSTreeNode> bezierList) {
		this.bezierList = bezierList;
	}

	public NURBSTreeNode getFirstChild() {
		return firstChild;
	}

	public void setFirstChild(NURBSTreeNode firstChild) {
		this.firstChild = firstChild;
	}

	public NURBSTreeNode getSecondChild() {
		return secondChild;
	}

	public void setSecondChild(NURBSTreeNode secondChild) {
		this.secondChild = secondChild;
	}

	public NURBSTreeNode getThirdChild() {
		return thirdChild;
	}

	public void setThirdChild(NURBSTreeNode thirdChild) {
		this.thirdChild = thirdChild;
	}

	public NURBSTreeNode getFourthChild() {
		return fourthChild;
	}

	public void setFourthChild(NURBSTreeNode fourthChild) {
		this.fourthChild = fourthChild;
	}
	
	
	

}
