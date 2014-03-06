package de.varylab.varylab.plugin.nurbs.data;

import java.util.LinkedList;

public class PolygonalLine {
	
	private LinkedList<LineSegment> pLine;
	private String description = null;
	private int 
			begin = 0,
			end = 0;

	

	public int getBegin() {
		return begin;
	}

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public PolygonalLine(){
		pLine = null;
	}
	
	public PolygonalLine(LinkedList<LineSegment > polygonalLine){
		pLine = polygonalLine;
		end = pLine.size()+1;
	}
	
	public LinkedList<LineSegment> getpLine(){
		LinkedList<LineSegment> newpLine = new LinkedList<LineSegment>();
		int count = 0;
		for (LineSegment ls : pLine) {
			if(count >= begin && count <= end){
				newpLine.add(ls);
			}
			count++;
		}
		return newpLine;
	}

	public int getCurveIndex(){
		return pLine.getFirst().getCurveIndex();
	}
	
	@Override
	public String toString(){
		String str = new String();
		for (LineSegment ls : pLine) {
			str = str + ls.toString() +"\n";
		}
		return str;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
