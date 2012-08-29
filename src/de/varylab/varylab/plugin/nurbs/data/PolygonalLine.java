package de.varylab.varylab.plugin.nurbs.data;

import java.util.LinkedList;

public class PolygonalLine {
	
	private LinkedList<LineSegment>pLine;
	
	public PolygonalLine(){
		pLine = null;
	}
	
	public PolygonalLine(LinkedList<LineSegment > polygonalLine){
		pLine = polygonalLine;
	}

	public LinkedList<LineSegment> getpLine() {
		return pLine;
	}

	public void setpLine(LinkedList<LineSegment> pLine) {
		this.pLine = pLine;
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

}
