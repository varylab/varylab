package de.varylab.varylab.plugin.nurbs.math;
//import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import compgeom.RLineSegment2D;
import compgeom.RPoint2D;
import compgeom.algorithms.BentleyOttmann;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.EventPointSegmentList;
import de.varylab.varylab.plugin.nurbs.IntersectionPointDistanceComparator;
import de.varylab.varylab.plugin.nurbs.IntersectionPointIndexComparator;
import de.varylab.varylab.plugin.nurbs.TreeSegmentComparator;
import de.varylab.varylab.plugin.nurbs.data.EventPoint;
import de.varylab.varylab.plugin.nurbs.data.EventPointYComparator;
import de.varylab.varylab.plugin.nurbs.data.FaceSet;
import de.varylab.varylab.plugin.nurbs.data.HalfedgePoint;
import de.varylab.varylab.plugin.nurbs.data.IndexedCurveList;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.Partition;
import de.varylab.varylab.plugin.nurbs.type.PartitionComparator;

public class LineSegmentIntersection {
//	private static HalfedgeInterface 
//	hif = null;
	
	
//	public static LinkedList<LineSegment> preSelection(double[] U, double[] V, LinkedList<LineSegment> segList){
//		double u0 = U[0];
//		double u1 = U[U.length - 1];
//		double v0 = V[0];
//		double v1 = V[V.length - 1];
//		System.out.println("START");
//		int curves = 120;
//		double uFactor = curves / (u1 - u0);
//		double vFactor = curves / (v1 - v0);
//		
//		Partition[][] partition = new Partition[curves + 1][curves + 1];
//		System.out.println("Start ini");
//		double startIni = System.currentTimeMillis();
//		for (int i = 0; i < partition.length; i++) {
//			for (int j = 0; j < partition.length; j++) {
//				partition[i][j] = new Partition();
//			}
//		}
//		double endIni = System.currentTimeMillis();
//		System.out.println("time for initializing: " + (startIni - endIni));
//		
//		for(LineSegment ls : segList){
//			double uStart = uFactor * (ls.getSegment()[0][0] - u0);
//			double vStart = vFactor * (ls.getSegment()[0][1] - v0);
//			double uEnd = uFactor * (ls.getSegment()[1][0] - u0);
//			double vEnd = vFactor * (ls.getSegment()[1][1] - v0);
//			int uS = (int)uStart;
//			int vS = (int)vStart;
//			int uE = (int)uEnd;
//			int vE = (int)vEnd;
//			if(uS > uE){
//				int temp = uS;
//				uS = uE;
//				uE = temp;
//			}
//			if(vS > vE){
//				int temp = vS;
//				vS = vE;
//				vE = temp;
//			}
//			for (int i = uS; i <= uE; i++) {
//				for (int j = vS; j <= vE; j++) {
//					partition[i][j].getSegList().add(ls);
//					if(!partition[i][j].getIndexList().contains(ls.getCurveIndex())){
//						partition[i][j].getIndexList().add(ls.getCurveIndex());
//					}
//				}
//			}
//		}
//		System.out.println("END");
//		TreeSet<LineSegment> finalSegmentTree = new TreeSet<LineSegment>(new PartitionComparator());
//		for (int i = 0; i < partition.length; i++) {
//			for (int j = 0; j < partition.length; j++) {
//				if(partition[i][j].getIndexList().size() > 1){
//					for(LineSegment ls : partition[i][j].getSegList()){
//						finalSegmentTree.add(ls);
//					}
//				}
//			}
//		}
		
		public static LinkedList<LineSegment> preSelection(double[] U, double[] V, LinkedList<LineSegment> segList){
			double u0 = U[0];
			double u1 = U[U.length - 1];
			double v0 = V[0];
			double v1 = V[V.length - 1];
			System.out.println("START");
			int curves = 120;
			double uFactor = curves / (u1 - u0);
			double vFactor = curves / (v1 - v0);
			
			Partition[][] partition = new Partition[curves + 1][curves + 1];
			System.out.println("Start ini");
			double startIni = System.currentTimeMillis();
			for (int i = 0; i < partition.length; i++) {
				for (int j = 0; j < partition.length; j++) {
					partition[i][j] = new Partition();
				}
			}
			double endIni = System.currentTimeMillis();
			System.out.println("time for initializing: " + (startIni - endIni));
			
			for(LineSegment ls : segList){
				double uStart = uFactor * (ls.getSegment()[0][0] - u0);
				double vStart = vFactor * (ls.getSegment()[0][1] - v0);
				double uEnd = uFactor * (ls.getSegment()[1][0] - u0);
				double vEnd = vFactor * (ls.getSegment()[1][1] - v0);
				int uS = (int)uStart;
				int vS = (int)vStart;
				int uE = (int)uEnd;
				int vE = (int)vEnd;
				if(uS > uE){
					int temp = uS;
					uS = uE;
					uE = temp;
				}
				if(vS > vE){
					int temp = vS;
					vS = vE;
					vE = temp;
				}
				for (int i = uS; i <= uE; i++) {
					for (int j = vS; j <= vE; j++) {
						partition[i][j].getSegList().add(ls);
						if(!partition[i][j].getIndexList().contains(ls.getCurveIndex())){
							partition[i][j].getIndexList().add(ls.getCurveIndex());
						}
					}
				}
			}
			System.out.println("END");
			TreeSet<LineSegment> finalSegmentTree = new TreeSet<LineSegment>(new PartitionComparator());
			for (int i = 0; i < partition.length; i++) {
				for (int j = 0; j < partition.length; j++) {
					if(partition[i][j].getIndexList().size() > 1){
						for(LineSegment ls : partition[i][j].getSegList()){
							finalSegmentTree.add(ls);
						}
					}
				}
			}

		System.out.println("anfanglaenge: " + segList.size());
		System.out.println("endlaenge: " + finalSegmentTree.size());
		LinkedList<LineSegment> finalSegmentList = new LinkedList<LineSegment>();
		for (LineSegment ls : finalSegmentTree) {
			finalSegmentList.add(ls);
		}
		return finalSegmentList;
	}
	
	
	public static LinkedList<IntersectionPoint> BentleyOttmannAlgoritm(double[] U, double[] V, List<LineSegment> segments){
		double u0 = U[0];
		double u1 = U[U.length - 1];
		double v0 = V[0];
		double v1 = V[V.length - 1];
		Set<RLineSegment2D> RSegments = new HashSet<RLineSegment2D>();
		Map<RLineSegment2D, LineSegment> inverseMap = new HashMap<RLineSegment2D, LineSegment>();
		
		int segmentCounter = 0;
		for (LineSegment ls : segments) {	
			segmentCounter++;

			long p1X = (long)(ls.getSegment()[0][0] * 100000000);
			long p1Y = (long)(ls.getSegment()[0][1] * 100000000);
			long p2X = (long)(ls.getSegment()[1][0] * 100000000);
			long p2Y = (long)(ls.getSegment()[1][1] * 100000000);
			
			RPoint2D p1 = new RPoint2D(p1X, p1Y);
			RPoint2D p2 = new RPoint2D(p2X, p2Y);
			
			RLineSegment2D rSeg = new RLineSegment2D(p1, p2, ls.getCurveIndex(), ls.getIndexOnCurve());
			if(p1.isLeftOf(p2) || (!p1.isLeftOf(p2) && !p1.isRightOf(p2) && p1.isBelow(p2))){
			}
			else{
				rSeg = new RLineSegment2D(p2, p1,  ls.getCurveIndex(), ls.getIndexOnCurve());
			}
			inverseMap.put(rSeg, ls);
			RSegments.add(rSeg);
			
		}
		System.out.println("# segments = " + segmentCounter);
		System.out.println("START TO COMPUTE INTERSECTIONS");
		LinkedList<IntersectionPoint> intersectionPoints = new LinkedList<IntersectionPoint>();
		Map<RPoint2D, Set<RLineSegment2D>> intersections = BentleyOttmann.intersectionsMap(RSegments);
		for(RPoint2D point : intersections.keySet()){
			LinkedList<LineSegment> segList = new LinkedList<LineSegment>();
			for (RLineSegment2D lS2D : intersections.get(point)) {
				segList.add(inverseMap.get(lS2D));
			}
			IntersectionPoint ip = new IntersectionPoint();
			double x = point.x.doubleValue() / 100000000.;
			double y = point.y.doubleValue() / 100000000.;
			ip.setPoint(new double[2]);
			ip.getPoint()[0] = x;
			ip.getPoint()[1] = y;
			ip.setIntersectingSegments(segList);
			
			intersectionPoints.add(ip);
			double[] Point = ip.getPoint();
			if(Point[0] < u0){
				Point[0] = u0;
			}
			else if(Point[0] > u1){
				Point[0] = u1;
			}
			else if(Point[1] < v0){
				Point[1] = v0;
			}
			else if(Point[1] > v1){
				Point[1] = v1;
			}
			ip.setPoint(Point);
		}
		return intersectionPoints;
	}


	/*
	 * returns true iff c lies on the lefthand side of the line from a to b
	 */
	public static boolean orientation(double[] a, double[] b, double[] c){
		return (c[0] * (a[1] - b[1]) + c[1] * (b[0] - a[0]) + a[0] * b[1] - a[1] * b[0] > 0);
	}

	/*
	 * use homogeneous coords to find orientation
	 */
	public static boolean counterClockWiseOrder(double[] a, double[] b, double[] c){
//		double ccw = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0]);
		double ccw = c[0] * (a[1] -   b[1]) + c[1] * (b[0] -   a[0]) + a[0] * b[1] - a[1] * b[0];
		if(ccw >= 0){
			return true;
		}
		return false;
	}
	
	/*
	 * this constillation us used
	 * 
	 *   a     c
	 *    \   /
	 *     
	 *    /   \
	 *   d     b 
	 */
//	public static boolean interchangedEndpoints(double[] a, double[] b, double[] c, double[]d, int i){
//		if(a[i] <= c[i] && d[i] <= b[i]){
//			return true;
//		}
//		return false;
//	}
	
	
	
	public static double[] intersectionPoint(LineSegment first, LineSegment second, EventPoint p){
		double s1 = first.getSegment()[0][0];
		double s2 = first.getSegment()[0][1];
		double t1 = first.getSegment()[1][0];
		double t2 = first.getSegment()[1][1];
		double p1 = second.getSegment()[0][0];
		double p2 = second.getSegment()[0][1];
		double q1 = second.getSegment()[1][0];
		double q2 = second.getSegment()[1][1];
		double[] result;
		if(isClosedToHorizontal(second)){
//			System.out.println("second "+ Arrays.toString(second.segment[0]) + " " + Arrays.toString(second.segment[1]));
//			System.out.println("horizontal second");
			result = new double[2];
			result[0] = s1 + ((t1 - s1) * (s2 - p.getPoint()[1]) / (s2 - t2));
			if(result[0] < p.getPoint()[0]){
				result[0] = p.getPoint()[0];
			}
			result[1] = p.getPoint()[1];
			return result;
		}
		else if(isClosedToHorizontal(first)){
//			System.out.println("first "+ Arrays.toString(first.segment[0]) + " " + Arrays.toString(first.segment[1]));
//			System.out.println("horizontal first");
			result = new double[2];
			result[0] = p1 + ((q1 - p1) * (p2 - p.getPoint()[1]) / (p2 - q2));
			if(result[0] < p.getPoint()[0]){
				result[0] = p.getPoint()[0];
			}
			result[1] = p.getPoint()[1];
			return result;
		}
		else{
			double lambda = ((p1 - s1) * (s2 - t2) - (p2 - s2) * (s1 - t1)) / ((q2 - p2) * (s1 - t1) - (q1 - p1) * (s2 - t2));
			result = Rn.add(null, second.getSegment()[0],Rn.times(null, lambda, Rn.add(null, second.getSegment()[1], Rn.times(null, -1, second.getSegment()[0]))));
			//new
			double currEps = Math.min(conditionalEps(first), conditionalEps(second)) ;
			if(p.getPoint()[1] < result[1] && (result[1] - p.getPoint()[1]) < currEps){
				result[1] = p.getPoint()[1] - result[1];
			}
			//
			return result;
			
		}
	}
		
	
	

	
	public static boolean isHorizontal(LineSegment ls){
		if(ls.getSegment()[0][1] != ls.getSegment()[1][1]){
			return false;
		}
		return true;
	}
	
	
	public static boolean twoSegmentIntersection(LineSegment seg1, LineSegment seg2){
		double[] p1 = {seg1.getSegment()[0][0], seg1.getSegment()[0][1], 1};
		double[] p2 = {seg1.getSegment()[1][0], seg1.getSegment()[1][1], 1}; 
		double[] p3 = {seg2.getSegment()[0][0], seg2.getSegment()[0][1], 1}; 
		double[] p4 = {seg2.getSegment()[1][0], seg2.getSegment()[1][1], 1}; 
		double[] normal1 = Rn.crossProduct(null, p1, p2);
		double[] normal2 = Rn.crossProduct(null, p3, p4);
		if(Math.signum(Rn.innerProduct(p3, normal1)) == Math.signum(Rn.innerProduct(p4, normal1))){
			return false;
		}
		else if(Math.signum(Rn.innerProduct(p1, normal2)) == Math.signum(Rn.innerProduct(p2, normal2))){
			return false;
		}

		return true;
		
		
//		if(LineSegmentIntersection.counterClockWiseOrder(p1, p3, p4) == LineSegmentIntersection.counterClockWiseOrder(p2, p3, p4)){
//		return false;
//	}
//	else if(LineSegmentIntersection.counterClockWiseOrder(p1, p2, p3) == LineSegmentIntersection.counterClockWiseOrder(p1, p2, p4)){
//		return false;
//	}
//	else{
//		return true;
//	}	
	}
	

	
//	public static boolean twoSegmentIntersection(LineSegment seg1, LineSegment seg2){
//		double[] p1 = seg1.getSegment()[0];
//		double[] p2 = seg1.getSegment()[1]; 
//		double[] p3 = seg2.getSegment()[0]; 
//		double[] p4 = seg2.getSegment()[1];
//		double lengthSeg1 = Rn.euclideanDistance(p1, p2);
//		double lengthSeg2 = Rn.euclideanDistance(p3, p4);
//		double[] p2MinusP1 = Rn.add(null, p2, Rn.times(null, -1, p1));
//		double[] q2 = Rn.add(null, p2, Rn.times(null, lengthSeg1 / 100, p2MinusP1));	
////		System.out.println("p2 "+Arrays.toString(p2)+"q2 "+Arrays.toString(q2));
//		double[] q1 = Rn.add(null, p1, Rn.times(null, lengthSeg1 / -100, p2MinusP1));
////		System.out.println("p1 "+Arrays.toString(p1)+"q1 "+Arrays.toString(q1));
//		double[] p4MinusP3 = Rn.add(null, p4, Rn.times(null, -1, p3));
//		double[] q4 = Rn.add(null, p4, Rn.times(null, lengthSeg2 / 100, p4MinusP3));	
////		System.out.println("p4 "+Arrays.toString(p4)+"q4 "+Arrays.toString(q4));
//		double[] q3 = Rn.add(null, p3, Rn.times(null, lengthSeg2 / -100, p4MinusP3));
////		System.out.println("p3 "+Arrays.toString(p3)+"q3 "+Arrays.toString(q3));
//		
//		if(LineSegmentIntersection.counterClockWiseOrder(q1, q3, q4) == LineSegmentIntersection.counterClockWiseOrder(q2, q3, q4)){
//			return false;
//		}
//		else if(LineSegmentIntersection.counterClockWiseOrder(q1, q2, q3) == LineSegmentIntersection.counterClockWiseOrder(q1, q2, q4)){
//			return false;
//		}
//		else{
//			return true;
//		}
//		
////		if(LineSegmentIntersection.counterClockWiseOrder(p1, p3, p4) == LineSegmentIntersection.counterClockWiseOrder(p2, p3, p4)){
////		return false;
////	}
////	else if(LineSegmentIntersection.counterClockWiseOrder(p1, p2, p3) == LineSegmentIntersection.counterClockWiseOrder(p1, p2, p4)){
////		return false;
////	}
////	else{
////		return true;
////	}	
//	}

	public static LinkedList<HalfedgePoint> findAllNbrs(LinkedList<IntersectionPoint> intersectionPoints){
		LinkedList<HalfedgePoint> points = new LinkedList<HalfedgePoint>();
		
		for (IntersectionPoint iP1 : intersectionPoints) {
			LinkedList<IndexedCurveList> iP1CurveList = new LinkedList<IndexedCurveList>();
			LinkedList<Integer> indexList = getIndexListFromIntersectionPoint(iP1);
	
			// add for each curve intersecting this intersectionPoint all IntersectionPoints contained in this curve

			for (Integer i : indexList){
				IndexedCurveList icl = new IndexedCurveList(i, new LinkedList<IntersectionPoint>());
				iP1CurveList.add(icl);
				for (IntersectionPoint iP2 : intersectionPoints) {
					LinkedList<Integer> curveIndex = getIndexListFromIntersectionPoint(iP2);
					if(curveIndex.contains(i)){
						icl.getCurveList().add(iP2);
					}
				}
			}
			
			LinkedList<IntersectionPoint> nbrs = new LinkedList<IntersectionPoint>();
			
			for (IndexedCurveList icl : iP1CurveList) {
				boolean cyclic = getCyclicFromCurveIndexAndIntersectionPoint(icl.getIndex(), iP1);
				
				// sorting each curveList(i.e. all IntersectionPoints contained in this curve) w.r.t. indexOnCurve
				
				IntersectionPointIndexComparator ipic = new IntersectionPointIndexComparator();
				ipic.curveIndex = icl.getIndex();
				Collections.sort(icl.getCurveList(), ipic);
				
				
				// add for each indexOnCurve all IntersectionPoints with same index in a list
				
				LinkedList<LinkedList<IntersectionPoint>> indexOrderList = new LinkedList<LinkedList<IntersectionPoint>>();
				int before = -1;
				for (IntersectionPoint iP : icl.getCurveList()) {
					int indexOnCurve = getIndexOnCurveFromCurveIndexAndIntersectionPoint(icl.getIndex(), iP);
					if(indexOnCurve != before){
						indexOrderList.add(new LinkedList<IntersectionPoint>());
					}
					before = indexOnCurve;
					indexOrderList.getLast().add(iP);
				}
				
				
				// sort all same indexed IntersectionPoints w.r.t. euclidian distance
				
				for (LinkedList<IntersectionPoint> sameList : indexOrderList) {
					if(sameList.size() > 1){
						sortSameIndex(sameList, icl.getIndex(), getIndexOnCurveFromCurveIndexAndIntersectionPoint(icl.getIndex(), sameList.getFirst()));
					}
				}
				
				// get back the original list in order
				
				LinkedList<IntersectionPoint> mapList = new LinkedList<IntersectionPoint>();
				for (LinkedList<IntersectionPoint> list : indexOrderList) {
					mapList.addAll(list);
				}
				
				// fill the map
				
				int i = 0;
				Map<IntersectionPoint, Integer> map = new HashMap<IntersectionPoint, Integer>();
				Map<Integer, IntersectionPoint> inverseMap = new HashMap<Integer, IntersectionPoint>();
				for (IntersectionPoint iP : mapList) {
					i++;
					map.put(iP, i);
					inverseMap.put(i, iP);
				}
				
				// get both (if possible) nbrs on this curve
				
				int index = map.get(iP1);
				if(index > 1){
					nbrs.add(inverseMap.get(index - 1));
				}
				if(index < mapList.size()){
					nbrs.add(inverseMap.get(index + 1));
				}
				if(index == mapList.size() && cyclic){
					nbrs.add(inverseMap.get(1));
				}
				if(index == 1 && cyclic){
					nbrs.add(inverseMap.get(mapList.size()));
				}
			}
			HalfedgePoint hp = new HalfedgePoint(iP1, nbrs);
			iP1.setParentHP(hp);
			points.add(hp);
		}
		return points;
	}
	
	private static boolean getCyclicFromCurveIndexAndIntersectionPoint(int curveIndex, IntersectionPoint iP){
		boolean cyclic = false;
		for (LineSegment ls : iP.getIntersectingSegments()) {
			if(curveIndex == ls.getCurveIndex()){
				cyclic = ls.isCyclic();
			}
		}
		return cyclic;
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
	
	private static double[] getFirstSegmentCoordsFromCurveIndex_IndexOnCurveAndIntersectionPoint(int curveIndex,int indexOnCurve, IntersectionPoint iP){
		for (LineSegment seg : iP.getIntersectingSegments()) {
			if(seg.getCurveIndex() == curveIndex && seg.getIndexOnCurve() == indexOnCurve){
				return seg.getSegment()[0];			
			}
		}
		return null;
	}
	
	private static LinkedList<IntersectionPoint> sortSameIndex(LinkedList<IntersectionPoint> sameIndexList, int curveIndex, int indexOnCurve){
		for (IntersectionPoint iP : sameIndexList) {
			double[] firstCoordFromIndexedSegment = getFirstSegmentCoordsFromCurveIndex_IndexOnCurveAndIntersectionPoint(curveIndex,indexOnCurve, iP);
			iP.setSameIndexDist(Rn.euclideanDistance(iP.getPoint(), firstCoordFromIndexedSegment));
//			System.out.println("firstCoord "+Arrays.toString(firstCoordFromIndexedSegment)+" iP "+Arrays.toString(iP.point)+" iP.sameIndexDist " + iP.sameIndexDist);
		}
		Collections.sort(sameIndexList, new IntersectionPointDistanceComparator());
		return sameIndexList;
	}

	
	private static LinkedList<Integer> getIndexListFromIntersectionPoint(IntersectionPoint iP){
		 LinkedList<Integer> indexList = new LinkedList<Integer>();
		 for (LineSegment seg : iP.getIntersectingSegments()) {
			if(!indexList.contains(seg.getCurveIndex())){
				indexList.add(seg.getCurveIndex());
			}
		}
		return indexList;
	}
	
	

	public static LinkedList<HalfedgePoint> orientedNbrs (LinkedList<HalfedgePoint> halfPoints){
		for (HalfedgePoint hP : halfPoints) {
			LinkedList<IntersectionPoint> orientedList = new LinkedList<IntersectionPoint>();
			LinkedList<IntersectionPoint> allNbrs = new LinkedList<IntersectionPoint>();
			for (IntersectionPoint iP : hP.getNbrs()) {
				if(iP.getPoint() != null){
					allNbrs.add(iP);
				}
			}
			orientedList.add(allNbrs.getFirst());
			int bound = allNbrs.size();
			IntersectionPoint lastOrientedPoint = orientedList.getLast();
			boolean sameNbr = false;
			while(orientedList.size() < bound){
				double angle = Double.MAX_VALUE;
				boolean leftOrientation = false;
				IntersectionPoint next = new IntersectionPoint();
				IntersectionPoint before = allNbrs.getFirst();
				if(orientedList.getLast() == lastOrientedPoint && orientedList.size() > 1){
					sameNbr = true;
				}
				boolean firstPoint = true;
				for (IntersectionPoint nonOrientedNbr : allNbrs) {
					if(orientedList.getLast() == before && before == nonOrientedNbr && firstPoint == false && !sameNbr){
						next = nonOrientedNbr;
						angle = 0;
						leftOrientation = true;
						sameNbr = true;
					}
					firstPoint = false;
					before = nonOrientedNbr;
					double[] first = orientedList.getLast().getPoint();
					double[] middle = hP.getPoint().getPoint();
					double[] last = nonOrientedNbr.getPoint();
					double[] a = Rn.normalize(null, Rn.subtract(null, first, middle));
					double[] b = Rn.normalize(null, Rn.subtract(null, last, middle));
					if(first != last && allNbrs.size() > 1 && LineSegmentIntersection.orientation(first, middle, last)){
						leftOrientation = true;
						if(angle > Math.acos(Rn.innerProduct(a, b))){
							angle = Math.acos(Rn.innerProduct(a, b));
							next = nonOrientedNbr;
						}
					}
				}
				if(!leftOrientation){
					angle = Double.MIN_VALUE;

					for (IntersectionPoint nonOrientedNbr : allNbrs) {
						double[] first = orientedList.getLast().getPoint();
						double[] middle = hP.getPoint().getPoint();
						double[] last = nonOrientedNbr.getPoint();;
						double[] a = Rn.normalize(null, Rn.subtract(null, first, middle));
						double[] b = Rn.normalize(null, Rn.subtract(null, last, middle));
						if(first != last && allNbrs.size() > 1){
							if(angle < Math.acos(Rn.innerProduct(a, b))){
								angle = Math.acos(Rn.innerProduct(a, b));
								next = nonOrientedNbr;
							}
						}
					}
				}
				lastOrientedPoint = orientedList.getLast();
				orientedList.add(next);
			}
			IntersectionPoint before = null;
			LinkedList<IntersectionPoint> ori = new LinkedList<IntersectionPoint>();
			for (IntersectionPoint iP : orientedList) {
				if(before != iP){
					ori.add(iP);
				}
				before = iP;
			}
			if(ori.getLast() == ori.getFirst() && ori.size() > 1){
				ori.pollLast();
			}
			ori.add(ori.getFirst());
			hP.setNbrs(ori);
		}
		
		
		return halfPoints;
	}
	/*
	 * returns the next vertex w.r.t. a face in order
	 */
	
	public static IntersectionPoint getNextNbr(IntersectionPoint before, HalfedgePoint point){
		boolean isEqual = false;
		for (IntersectionPoint iP : point.getNbrs()) {
			if(iP.getPoint() == before.getPoint() && !isEqual){
				isEqual = true;
			}
			else if(isEqual){
				return iP;
			}
		}
		return null;
	}
	
	
	/*
	 * 
	 */
	
	private static LinkedList<IntersectionPoint>  allAjacentNbrs(HalfedgePoint point , LinkedList<HalfedgePoint> halfedgePoints){
		LinkedList<IntersectionPoint> allAjacentNbrs = new LinkedList<IntersectionPoint>();
		LinkedList<HalfedgePoint> allNbrs = new LinkedList<HalfedgePoint>();
		IntersectionPoint firstIP = point.getUnusedNbrs().getLast();
		HalfedgePoint first = firstIP.getParentHP();
		allNbrs.add(point);
		allAjacentNbrs.add(point.getPoint());
		allNbrs.add(first);
		allAjacentNbrs.add(first.getPoint());
		IntersectionPoint before = point.getPoint();
		HalfedgePoint bP = point;
		
		// remove the start direction from unused nbrs
		
		LinkedList<IntersectionPoint> removedStartDirectionFirst = new LinkedList<IntersectionPoint>();
		for (IntersectionPoint ip : bP.getUnusedNbrs()) {
			if(ip.getParentHP() != first){
				removedStartDirectionFirst.add(ip);
			}
		}
		bP.setUnusedNbrs(removedStartDirectionFirst);
		
		while(point != allNbrs.getLast()){
			IntersectionPoint next = getNextNbr(before, allNbrs.getLast());
			before = allAjacentNbrs.getLast();
			bP = before.getParentHP();
			
			LinkedList<IntersectionPoint> removedStartDirection = new LinkedList<IntersectionPoint>();
			for (IntersectionPoint ip : bP.getUnusedNbrs()) {
				if(ip.getParentHP() != next.getParentHP()){
					removedStartDirection.add(ip);
				}
			}
			bP.setUnusedNbrs(removedStartDirection);
			
			allAjacentNbrs.add(next);
//			if(next.getParentHP() == null){
//				System.out.println("NEXT");
//				System.out.println(Arrays.toString(next.getPoint()));
//				PointSetFactory psf = new PointSetFactory();
//				psf.setVertexCount(1);
//				psf.setVertexCoordinates(next.getPoint());
//				psf.update();
//				SceneGraphComponent sgc = new SceneGraphComponent("geodesic segment");
//				SceneGraphComponent minCurveComp = new SceneGraphComponent("Geodesic Segment");
//				sgc.addChild(minCurveComp);
//				sgc.setGeometry(psf.getGeometry());
//				Appearance labelAp = new Appearance();
//				sgc.setAppearance(labelAp);
//				DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(labelAp, false);
//				DefaultPointShader pointShader = (DefaultPointShader)dgs.getPointShader();
//				pointShader.setDiffuseColor(Color.magenta);
//				hif.getActiveLayer().addTemporaryGeometry(sgc);
//				
//			}
			HalfedgePoint hP = next.getParentHP();
			allNbrs.add(hP);
		}
		allAjacentNbrs.pollLast();
		return allAjacentNbrs;
	}
	
	private static boolean faceContainsAllBoundaryVerts(LinkedList<IntersectionPoint> allAjacentNbrs, LinkedList<double[]> boundaryVerts){
		boolean containsAll = false;
		int numberOfBoundaryVerts = boundaryVerts.size();
		int counter = 0;
		for(double[] bV : boundaryVerts){
			for (IntersectionPoint iP : allAjacentNbrs) {
				if(Arrays.equals(bV,iP.getPoint())){
					counter++;
				}
				if(counter == numberOfBoundaryVerts){
					containsAll = true;
					break;
				}
			}
		}
		return containsAll;
	}
	
//	private static boolean faceContainsAllBoundaryVerts(LinkedList<IntersectionPoint> allAjacentNbrs, LinkedList<double[]> boundaryVerts){
//		
//	}
	
	public static FaceSet createFaceSet(LinkedList<HalfedgePoint> orientedNbrs, LinkedList<double[]> boundaryVerts){
		System.out.println("boundary Verts:");
		for (double[] bV : boundaryVerts) {
			System.out.println(Arrays.toString(bV));
		}
		FaceSet fS = new FaceSet();
		double[][] verts = new double[orientedNbrs.size()][2];
		LinkedList<int[]> faceNbrs = new LinkedList<int[]>();
		int c = 0;

		for (HalfedgePoint hP: orientedNbrs) {
			LinkedList<IntersectionPoint> unusedNbrs = new LinkedList<IntersectionPoint>();
			for (IntersectionPoint iP : hP.getNbrs()) {
				unusedNbrs.add(iP);
			}
			unusedNbrs.pollLast();
			hP.setUnusedNbrs(unusedNbrs);
			verts[c] = hP.getPoint().getPoint();
			c++;
		}
		
		fS.setVerts(verts);
		
		for (HalfedgePoint hP : orientedNbrs) {
			while(!hP.getUnusedNbrs().isEmpty()){
				LinkedList<IntersectionPoint> facePoints = allAjacentNbrs(hP, orientedNbrs);
				if(!LineSegmentIntersection.faceContainsAllBoundaryVerts(facePoints, boundaryVerts)){
				LinkedList<Integer> ind = new LinkedList<Integer>();
				for (IntersectionPoint fP : facePoints) {
					for (int i = 0; i < verts.length; i++) {
						if(fP.getPoint() == verts[i]){
							ind.add(i);
						}
					}
				}
				int[] index = new int[ind.size()];
				int count = 0;
				for (Integer i : ind) {
					index[count] = i;
					count++;
				}
				faceNbrs.add(index);
				}
			}
		}
	
		int[][] faceIndex = new int[faceNbrs.size()][];
		int counter = 0;
		for (int[] fs : faceNbrs) {
				faceIndex[counter] = fs;
				counter++;
		}
		fS.setFaces(faceIndex);
		return fS;
	}
	

	

	
	public static enum PointStatus {
		upper,
		containsInterior,
		lower
	}
	
	/*
	 *  plane sweep algorithm from "Computational Geometry"
	 */
	
	private static double conditionalEps(LineSegment ls){
		return Rn.euclideanDistance(ls.getSegment()[0], ls.getSegment()[1]) / 100;
	}
	
	public static LinkedList<IntersectionPoint> findIntersections(List<LineSegment> segments){
		LinkedList<IntersectionPoint> interPoints = new LinkedList<IntersectionPoint>();
		LinkedList<double[]> currentIntersections = new LinkedList<double[]>();
		
		PriorityQueue<EventPoint> eventPoints = new PriorityQueue<EventPoint>(3 * segments.size(), new EventPointYComparator());
		
//		for (LineSegment s : segments) {
//			if(s.segment[0][1] < s.segment[1][1] || (s.segment[0][1] == s.segment[1][1] && s.segment[0][0] > s.segment[1][0])){
//				double[] temp = s.segment[0];
//				s.segment[0] = s.segment[1];
//				s.segment[1] = temp;
//			}
//			EventPoint first = new EventPoint(s.segment[0], PointStatus.upper, s);
//			EventPoint second = new EventPoint(s.segment[1], PointStatus.lower, s);
//			eventPoints.add(first);
//			eventPoints.add(second);
//		}
		
		for (LineSegment s : segments) {
//			System.out.println(s.toString() + "endpoints" + Arrays.toString(s.segment[0]) + Arrays.toString(s.segment[1]));
			
			// new preProcessing
			if(isClosedToHorizontal(s)){
				double y = (s.getSegment()[0][1] + s.getSegment()[1][1]) / 2;
				s.getSegment()[0][1] = y;
				s.getSegment()[1][1] = y;
			}
			//
			EventPoint first = new EventPoint();
			EventPoint second = new EventPoint();
			if(s.getSegment()[0][1] < s.getSegment()[1][1] || (s.getSegment()[0][1] == s.getSegment()[1][1] && s.getSegment()[0][0] > s.getSegment()[1][0])){
				first = new EventPoint(s.getSegment()[1], PointStatus.upper, s);
				second = new EventPoint(s.getSegment()[0], PointStatus.lower, s);
			}
			else{
				first = new EventPoint(s.getSegment()[0], PointStatus.upper, s);
				second = new EventPoint(s.getSegment()[1], PointStatus.lower, s);
			}
			eventPoints.add(first);
			eventPoints.add(second);
		}
		TreeSegmentComparator tsc = new TreeSegmentComparator();
		TreeSet<LineSegment> T = new TreeSet<LineSegment>(tsc);
		LinkedList<EventPointSegmentList> eventPointSegmentList = new LinkedList<EventPointSegmentList>();
		tsc.eventPointSegmentList = eventPointSegmentList;
		LinkedList<LineSegment> Up = new LinkedList<LineSegment>();
		LinkedList<LineSegment> Cp = new LinkedList<LineSegment>();
		LinkedList<LineSegment> Lp = new LinkedList<LineSegment>();
		EventPoint testPoint = new EventPoint();
		while(!eventPoints.isEmpty()){
			EventPoint p = eventPoints.poll();
//			System.out.println("EventPoint: " + Arrays.toString(p.point) + " curveIndex = " + p.segment.curveIndex + " indexOnCurve = " + p.segment.indexOnCurve);
			tsc.p = p;
			EventPoint next = eventPoints.peek();
			if(next == null || p.getPoint()[0] != next.getPoint()[0] || p.getPoint()[1] != next.getPoint()[1]){
				if(p.getStatus() == PointStatus.upper){
					Up.add(p.getSegment());
					testPoint = p;
				}
				else if(p.getStatus() == PointStatus.containsInterior){
					Cp.add(p.getSegment());
					testPoint = p;
				}
				else{
					Lp.add(p.getSegment());
				}
				handleEventPoint(p,testPoint, T, eventPoints, Up, Cp, Lp, interPoints, currentIntersections, eventPointSegmentList);
				Up.clear();
				Cp.clear();
				Lp.clear();
			}else{
				if(p.getStatus() == PointStatus.upper){
					Up.add(p.getSegment());
					testPoint = p;
				}
				else if(p.getStatus() == PointStatus.containsInterior){
					Cp.add(p.getSegment());
					testPoint = p;
				}
				else{
					Lp.add(p.getSegment());
				}
			}
		}
//		System.out.println("Intersections: ");
//		for (IntersectionPoint ip : interPoints) {
//			System.out.println(ip.toString());
//		}
		return interPoints;
	}
	
	public static void handleEventPoint(EventPoint p, EventPoint testPoint, TreeSet<LineSegment> T, PriorityQueue<EventPoint> eventPoints, LinkedList<LineSegment> Up, LinkedList<LineSegment> Cp, LinkedList<LineSegment> Lp, LinkedList<IntersectionPoint> interPoints, LinkedList<double[]> currentIntersections, LinkedList<EventPointSegmentList> eventPointSegmentList){
		
		
		LinkedList<LineSegment> segments = new LinkedList<LineSegment>();
		
		//search for all EventPoints on the sweepline
		if(Cp.size() + Up.size() > 0){
			segments.addAll(Cp);
			segments.addAll(Up);
			EventPointSegmentList pSegments = new EventPointSegmentList();
			pSegments.setP(p);
			pSegments.setAllSegments(new LinkedList<LineSegment>());
			pSegments.getAllSegments().addAll(segments);
			eventPointSegmentList.add(pSegments);
			
			while(eventPointSegmentList.peekFirst().getP().getPoint()[1] != p.getPoint()[1]){
				eventPointSegmentList.pollFirst();
			}
		}
		
		
		LinkedList<LineSegment> allSegments = new LinkedList<LineSegment>();
		allSegments.addAll(Lp);
		allSegments.addAll(Cp);
		allSegments.addAll(Up);
		int firstCurveIndex = allSegments.peekFirst().getCurveIndex();
		boolean moreThanOneCurve = false;
		for (LineSegment aS : allSegments) {
			if(firstCurveIndex != aS.getCurveIndex()){
				moreThanOneCurve = true;
			}
		}
		if(moreThanOneCurve){
			IntersectionPoint iP = new IntersectionPoint();
			iP.setPoint(p.getPoint());
			iP.setIntersectingSegments(allSegments);
			interPoints.add(iP);
		}
		T.removeAll(Cp);
		T.removeAll(Lp);
		T.addAll(Cp);
		T.addAll(Up);
		if(Up.size() + Cp.size() == 0){
			if(T.lower(p.getSegment()) != null && T.higher(p.getSegment()) != null){
				LineSegment sl = T.lower(p.getSegment());
				LineSegment sr = T.higher(p.getSegment());
				findNewEvent(sl, sr, p, currentIntersections, eventPoints);
			}
		}
		else{
			LineSegment leftmost = testPoint.getSegment();
			
			while(T.lower(leftmost) != null && (Up.contains(T.lower(leftmost)) || Cp.contains(T.lower(leftmost)))){
				leftmost = T.lower(leftmost);
			}
			if(T.lower(leftmost) != null){
				LineSegment sl = T.lower(leftmost);
				findNewEvent(sl, leftmost, p, currentIntersections, eventPoints);
			}
			LineSegment rightmost = testPoint.getSegment();
			while(T.higher(rightmost) != null && (Up.contains(T.higher(rightmost)) || Cp.contains(T.higher(rightmost)))){
				rightmost = T.higher(rightmost);
			}
			if(T.higher(rightmost) != null){
				LineSegment sr = T.higher(rightmost);
				findNewEvent(rightmost, sr, p, currentIntersections, eventPoints);
			}
		}
	}
	
	public static void findNewEvent(LineSegment sl, LineSegment sr, EventPoint p, LinkedList<double[]> currentIntersections, PriorityQueue<EventPoint> eventPoints){
		boolean intersection = twoSegmentIntersection(sl, sr);

		double[] intersectionPoint = intersectionPoint(sl, sr, p);
		double[] reversedIntersectionPoint = intersectionPoint(sr, sl, p);

		if((intersection && intersectionPoint[1] < p.getPoint()[1]) || (intersection && intersectionPoint[1] == p.getPoint()[1] && intersectionPoint[0] >= p.getPoint()[0])
				||(intersection && reversedIntersectionPoint[1] < p.getPoint()[1]) || (intersection && reversedIntersectionPoint[1] == p.getPoint()[1] && reversedIntersectionPoint[0] >= p.getPoint()[0])){
//			System.out.println("drinn mit Intersectionpoint: " + Arrays.toString(intersectionPoint));
			boolean isSelected = false;
			for (double[] ci  : currentIntersections) {
				if((intersectionPoint[0] == ci[0] && intersectionPoint[1] == ci[1]) || (reversedIntersectionPoint[0] == ci[0] && reversedIntersectionPoint[1] == ci[1])){
					isSelected = true;
				}
			}
			if(!isSelected){

				if(!Rn.equals(intersectionPoint, sl.getSegment()[1]) && !Rn.equals(reversedIntersectionPoint, sl.getSegment()[1]) && !Rn.equals(intersectionPoint, sl.getSegment()[0]) && !Rn.equals(reversedIntersectionPoint, sl.getSegment()[0])){
//				if(!Rn.equals(intersectionPoint, sl.segment[1]) && !Rn.equals(reversedIntersectionPoint, sl.segment[1])){
//					System.out.println("new IntersectionPoint left "+sl.curveIndex+"|"+sl.indexOnCurve +" "+ Arrays.toString(intersectionPoint));
					EventPoint left = new EventPoint(intersectionPoint, PointStatus.containsInterior, sl);
					eventPoints.add(left);
					currentIntersections.add(intersectionPoint);
					currentIntersections.add(reversedIntersectionPoint);
				}
//				if(!Rn.equals(intersectionPoint, sr.segment[1]) && !Rn.equals(reversedIntersectionPoint, sr.segment[1]) && !Rn.equals(intersectionPoint, sr.segment[0]) && !Rn.equals(reversedIntersectionPoint, sr.segment[0])){
				if(!Rn.equals(intersectionPoint, sr.getSegment()[1]) && !Rn.equals(reversedIntersectionPoint, sr.getSegment()[1])){
//					System.out.println("new IntersectionPoint right " +sr.curveIndex+"|"+sr.indexOnCurve +" "+ Arrays.toString(intersectionPoint));
					EventPoint right = new EventPoint(intersectionPoint, PointStatus.containsInterior, sr);
					eventPoints.add(right);
					currentIntersections.add(intersectionPoint);
					currentIntersections.add(reversedIntersectionPoint);
				}
			}
		}
	}
	
	private static boolean isClosedToHorizontal(LineSegment ls){
		double eps = Rn.euclideanDistance(ls.getSegment()[0], ls.getSegment()[1]) / 100000;
		if(Math.abs(ls.getSegment()[0][1] - ls.getSegment()[1][1]) < eps){
			return true;
		}
		return false;
	}
	

	
	
	public static void main(String[] args){
			
	}
}
