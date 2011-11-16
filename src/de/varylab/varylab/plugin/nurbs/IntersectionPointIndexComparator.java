package de.varylab.varylab.plugin.nurbs;

import java.util.Comparator;

import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;

public class IntersectionPointIndexComparator implements Comparator<IntersectionPoint> {
	
	public int curveIndex;
	
	@Override	
	public int compare(IntersectionPoint ip1, IntersectionPoint ip2) {
		
		return (int)Math.signum(getIndexOnCurveFromCurveIndexAndIntersectionPoint(curveIndex, ip1)- getIndexOnCurveFromCurveIndexAndIntersectionPoint(curveIndex, ip2));
	}
	
	private static int getIndexOnCurveFromCurveIndexAndIntersectionPoint(int curveIndex, IntersectionPoint iP){
		int result = 0;
		for (LineSegment seg : iP.getIntersectingSegments()) {
			if(seg.getCurveIndex() == curveIndex && result < seg.getIndexOnCurve()){
				result = seg.getIndexOnCurve();
			}
		}
		return result;
	}
}

