package de.varylab.varylab.plugin.nurbs.data;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PolygonalLine {
	
	private List<PolygonalLineListener> 
		listeners = Collections.synchronizedList(new LinkedList<PolygonalLineListener>());
	
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
		fireLineChanged();
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
		fireLineChanged();
	}

	public int getMax() {
		return pLine.size()-1;
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
//		String str = new String();
//		for (LineSegment ls : pLine) {
//			str = str + ls.toString() +"\n";
//		}
//		return str;
		return "curve index = " + pLine.getFirst().getCurveIndex();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setCurveIndex(int index) {
		for(LineSegment ls : pLine) {
			ls.setCurveIndex(index);
		}
	}
	
	public void addPolygonalLineListener(PolygonalLineListener l) {
		listeners.add(l);
	}
	
	protected void fireLineChanged() {
		synchronized(listeners) {
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					for(PolygonalLineListener l : listeners) {
						l.lineChanged();
					}
				}
			};
			EventQueue.invokeLater(r);
		}
	}

}
