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
	
//	public NURBSTreeNode(LinkedList<NURBSTreeNode> bezierList) {
//		this.ns = null;
//		this.bezierList = bezierList;
//		this.firstChild = null;
//		this.secondChild = null;
//		this.thirdChild = null;
//		this.fourthChild = null;
//	}
	
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
	
//	public boolean ListEquals(double u0, double v0, double u1, double v1){
//		double u0Check = getNs().getUKnotVector()[0];
//		double v0Check = getNs().getVKnotVector()[0];
//		double u1Check = getNs().getUKnotVector()[getNs().getUKnotVector().length - 1];
//		double v1Check = getNs().getVKnotVector()[getNs().getVKnotVector().length - 1];
//		if(u0 == u0Check || v0 == v0Check || u1 == u1Check || v1 == v1Check){
//			return true;
//		}
//		else{
//			return false;
//		}
//		
//	}
	
	public LinkedList<NURBSSurface> getAllChilds(){
		LinkedList<NURBSSurface> childs = new LinkedList<NURBSSurface>();
		if(getFirstChild() != null){
			childs.add(firstChild.getNs());
			childs.add(secondChild.getNs());
			childs.add(thirdChild.getNs());
			childs.add(fourthChild.getNs());
		}
		else{
			LinkedList<NURBSSurface> nsList = getNs().subdivideIntoFourNewPatches();
			childs.addAll(nsList);
			NURBSTreeNode first  = new NURBSTreeNode(nsList.getFirst());
			setFirstChild(first);
			NURBSTreeNode second  = new NURBSTreeNode(nsList.get(1));
			setSecondChild(second);
			NURBSTreeNode third  = new NURBSTreeNode(nsList.get(2));
			setThirdChild(third);
			NURBSTreeNode fourth  = new NURBSTreeNode(nsList.getLast());
			setFourthChild(fourth);
		}
		return childs;
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
	
	public boolean equals(NURBSTreeNode ntn){
		double u0 = getU0();
		double u1 = getU1();
		double v0 = getV0();
		double v1 = getV1();
		double ntnU0 = ntn.getU0();
		double ntnU1 = ntn.getU1();
		double ntnV0 = ntn.getV0();
		double ntnV1 = ntn.getV1();
		if(u0 == ntnU0 || u1 == ntnU1 || v0 == ntnV0 || v1 == ntnV1){
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean isInside(NURBSTreeNode ntn){
		double u0 = getU0();
		double u1 = getU1();
		double v0 = getV0();
		double v1 = getV1();
		double ntnU0 = ntn.getU0();
		double ntnU1 = ntn.getU1();
		double ntnV0 = ntn.getV0();
		double ntnV1 = ntn.getV1();
		if(u0 >= ntnU0 || u1 <= ntnU1 || v0 >= ntnV0 || v1 <= ntnV1){
			return true;
		}
		else{
			return false;
		}
	}
	
	public NURBSTreeNode childContainsThisNode(NURBSTreeNode ntn){
		if(isInside(ntn.getFirstChild())){
			return ntn.getFirstChild();
		}
		else if(isInside(ntn.getSecondChild())){
			return ntn.getSecondChild();
		}
		else if(isInside(ntn.getThirdChild())){
			return ntn.getThirdChild();
		}
		else if(isInside(ntn.getFourthChild())){
			return ntn.getFourthChild();
		}
		else{
			return null;
		}
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
