package de.varylab.varylab.plugin.nurbs.math;
//import java.awt.Color;
import java.util.Arrays;
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
import de.varylab.varylab.plugin.nurbs.TreeSegmentComparator;
import de.varylab.varylab.plugin.nurbs.data.EventPoint;
import de.varylab.varylab.plugin.nurbs.data.EventPointYComparator;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.Partition;
import de.varylab.varylab.plugin.nurbs.type.PartitionComparator;

public class LineSegmentIntersection {
		

		public static LinkedList<LineSegment> preSelection(double[] U, double[] V, LinkedList<LineSegment> segList){
			double u0 = U[0];
			double u1 = U[U.length - 1];
			double v0 = V[0];
			double v1 = V[V.length - 1];
//			System.out.println("START");
			int curves = 120;
			double uFactor = curves / (u1 - u0);
			double vFactor = curves / (v1 - v0);
//			System.out.println("all original segemtns");
//			for (LineSegment ls : segList) {
//				System.out.println(ls.toString());
//			}
			
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
		System.out.println("all selected segemnts");
		for (LineSegment ls : finalSegmentTree) {
			finalSegmentList.add(ls);
			System.out.println(ls.toString());
		}
		return finalSegmentList;
	}
	
	
	public static LinkedList<IntersectionPoint> BentleyOttmannAlgoritm(double[] U, double[] V, List<LineSegment> segments, double dilation){
		double u0 = U[0];
		double u1 = U[U.length - 1];
		double v0 = V[0];
		double v1 = V[V.length - 1];
		Set<RLineSegment2D> RSegments = new HashSet<RLineSegment2D>();
		Map<RLineSegment2D, LineSegment> inverseMap = new HashMap<RLineSegment2D, LineSegment>();
		
		int segmentCounter = 0;
		for (LineSegment ls : segments) {	
			segmentCounter++;

			long p1X = (long)(ls.getSegment()[0][0] * dilation);
			long p1Y = (long)(ls.getSegment()[0][1] * dilation);
			long p2X = (long)(ls.getSegment()[1][0] * dilation);
			long p2Y = (long)(ls.getSegment()[1][1] * dilation);
			
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
			double x = point.x.doubleValue() / dilation;
			double y = point.y.doubleValue() / dilation;
			ip.setPoint(new double[2]);
			ip.getPoint()[0] = x;
			ip.getPoint()[1] = y;
			ip.setIntersectingSegments(segList);
			double[] result = ip.getPoint();
			System.out.println("double[] result = ip.getPoint();" + Arrays.toString(result));
			
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
	
	public static boolean segmentIntersectsLine(LineSegment seg, LineSegment line){
		double[] p1 = {seg.getSegment()[0][0], seg.getSegment()[0][1], 1};
		double[] p2 = {seg.getSegment()[1][0], seg.getSegment()[1][1], 1}; 
		double[] p3 = {line.getSegment()[0][0], line.getSegment()[0][1], 1}; 
		double[] p4 = {line.getSegment()[1][0], line.getSegment()[1][1], 1}; 
		double[] normal = Rn.crossProduct(null, p3, p4);
		 if(Math.signum(Rn.innerProduct(p1, normal)) == Math.signum(Rn.innerProduct(p2, normal))){
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
	


	
	
	
//	public static LinkedList<HalfedgePoint> findAllNbrs(NURBSSurface ns, double dilation, LinkedList<IntersectionPoint> intersectionPoints){
//		LinkedList<HalfedgePoint> points = new LinkedList<HalfedgePoint>();
//		System.out.println("Boundary ");
//		for (Double value : ns.getBoundaryValuesPastIntersection(dilation)) {
//			System.out.println(value);
//		}
//		System.out.println("CLOSEDBoundary " + Arrays.toString(ns.getClosedBoundaryValuesPastIntersection(dilation)));
//		System.out.println("CLOSED BOUNDARY PIONTS");
//		for (IntersectionPoint iP1 : intersectionPoints) {
//			ClosedBoundary cb = iP1.getClosedBoundary(ns, dilation);
//			if(cb != ClosedBoundary.interior){
//				System.out.println(cb + " coords " + Arrays.toString(iP1.getPoint()));
//			}
//			LinkedList<IndexedCurveList> iP1CurveList = new LinkedList<IndexedCurveList>();
//			LinkedList<Integer> indexList = getIndexListFromIntersectionPoint(iP1);
//	
//			// add for each curve intersecting this intersectionPoint all IntersectionPoints contained in this curve
//
//			for (Integer i : indexList){
//				IndexedCurveList icl = new IndexedCurveList(i, new LinkedList<IntersectionPoint>());
//				int shiftedIndex1 = getShiftedIndexFromIntersectionPointAndCurveIndex(iP1, i);
//				iP1CurveList.add(icl);
//				for (IntersectionPoint iP2 : intersectionPoints) {
//					LinkedList<Integer> curveIndex = getIndexListFromIntersectionPoint(iP2);
//					int shiftedIndex2 = getShiftedIndexFromIntersectionPointAndCurveIndex(iP2, i);
//					if(curveIndex.contains(i) && shiftedIndex1 == shiftedIndex2){
//						icl.getCurveList().add(iP2);
//					}
//				}
//			}
//			
//			LinkedList<IntersectionPoint> nbrs = new LinkedList<IntersectionPoint>();
//			
//			for (IndexedCurveList icl : iP1CurveList) {
//				boolean cyclic = getCyclicFromCurveIndexAndIntersectionPoint(icl.getIndex(), iP1);
//				
//				// sort each curveList w.r.t. indexOnCurve
//				
//				IntersectionPointIndexComparator ipic = new IntersectionPointIndexComparator();
//				ipic.curveIndex = icl.getIndex();
//				Collections.sort(icl.getCurveList(), ipic);
//				
//				
//				// add for each indexOnCurve all IntersectionPoints with same index in a list
//				
//				LinkedList<LinkedList<IntersectionPoint>> indexOrderList = new LinkedList<LinkedList<IntersectionPoint>>();
//				int before = -1;
//				for (IntersectionPoint iP : icl.getCurveList()) {
//					int indexOnCurve = getIndexOnCurveFromCurveIndexAndIntersectionPoint(icl.getIndex(), iP);
//					if(indexOnCurve != before){
//						indexOrderList.add(new LinkedList<IntersectionPoint>());
//					}
//					before = indexOnCurve;
//					indexOrderList.getLast().add(iP);
//				}
//				
//				
//				// sort all same indexed IntersectionPoints w.r.t. euclidian distance
//				
//				for (LinkedList<IntersectionPoint> sameList : indexOrderList) {
//					if(sameList.size() > 1){
//						sortSameIndex(sameList, icl.getIndex(), getIndexOnCurveFromCurveIndexAndIntersectionPoint(icl.getIndex(), sameList.getFirst()));
//					}
//				}
//				
//				// get back the original list in order
//				
//				LinkedList<IntersectionPoint> mapList = new LinkedList<IntersectionPoint>();
//				for (LinkedList<IntersectionPoint> list : indexOrderList) {
//					mapList.addAll(list);
//				}
//				
//				// fill the map
//				
//				int i = 0;
//				Map<IntersectionPoint, Integer> map = new HashMap<IntersectionPoint, Integer>();
//				Map<Integer, IntersectionPoint> inverseMap = new HashMap<Integer, IntersectionPoint>();
//				for (IntersectionPoint iP : mapList) {
//					i++;
//					map.put(iP, i);
//					inverseMap.put(i, iP);
//				}
//				
//				// get both (if possible) nbrs on this curve
//				int index = map.get(iP1);
//				if(index > 1){
//					nbrs.add(inverseMap.get(index - 1));
//				}
//				if(index < mapList.size()){
//					nbrs.add(inverseMap.get(index + 1));
//				}
//				if(index == mapList.size() && cyclic){
//					nbrs.add(inverseMap.get(1));
//				}
//				if(index == 1 && cyclic){
//					nbrs.add(inverseMap.get(mapList.size()));
//				}
//			}
//			HalfedgePoint hp = new HalfedgePoint(iP1, nbrs);
//			iP1.setParentHP(hp);
//			points.add(hp);
//		}
////		System.out.println("CHECK      !!!!!!!!!!!!!!!!!!!!!!!!!");
////		points = orientedNbrs(points);
////		double check = -1.570796326794897;
////		int point = 0;
////		for (HalfedgePoint hp : points) {
////			System.out.println("hp = " + Arrays.toString(hp.getPoint().getPoint()));
////			if(hpContainsIndex(hp, check)){
////				point++;
////				System.out.println(point+". HP");
////				System.out.println("point = " + Arrays.toString(hp.getPoint().getPoint()));
////				System.out.println("nbrs");
////				for (IntersectionPoint ip : hp.getNbrs()) {
////					System.out.println(Arrays.toString(ip.getPoint()));
////				}
////			}
////		}
//		return points;
//	}
	
	
//	private static boolean hpContainsIndex(HalfedgePoint hp, double check){
//		for (LineSegment ls : hp.getPoint().getIntersectingSegments()) {
//			double[][] seg =  ls.getSegment();
//			if(seg[0][1] == check && seg[1][1] == check){
//				System.out.println("lineSegment " + ls.toString());
//				return true;
//			}
//		}
//		return false;
//	}
	

	
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
