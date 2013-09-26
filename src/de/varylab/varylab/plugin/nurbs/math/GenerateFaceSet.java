package de.varylab.varylab.plugin.nurbs.math;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sunflow.system.UI;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.IntersectionPointDistanceComparator;
import de.varylab.varylab.plugin.nurbs.IntersectionPointIndexComparator;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.data.FaceSet;
import de.varylab.varylab.plugin.nurbs.data.HalfedgePoint;
import de.varylab.varylab.plugin.nurbs.data.IndexedCurveList;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint.ClosedBoundary;

public class GenerateFaceSet {
	
	private LinkedList<IntersectionPoint> leftBound = new LinkedList<IntersectionPoint>();
	private LinkedList<IntersectionPoint> rightBound = new LinkedList<IntersectionPoint>();
	private LinkedList<IntersectionPoint> upperBound = new LinkedList<IntersectionPoint>();
	private LinkedList<IntersectionPoint> lowerBound = new LinkedList<IntersectionPoint>();
	private double dilation = 0;
	private NURBSSurface ns;
	
	
	public GenerateFaceSet(NURBSSurface n, double d, LinkedList<IntersectionPoint> ipList) {
		for (IntersectionPoint ip : ipList) {
			ClosedBoundary cb = ip.getClosedBoundary(n, d);
			if(cb == ClosedBoundary.left){
				leftBound.add(ip);
			}
			else if(cb == ClosedBoundary.right){
				rightBound.add(ip);
			}
			else if(cb == ClosedBoundary.upper){
				upperBound.add(ip);
			}
			else if(cb == ClosedBoundary.lower){
				lowerBound.add(ip);
			}
		}
		dilation = d;
		ns = n;
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
	
	private boolean areOppositePoints(IntersectionPoint ip1, IntersectionPoint ip2){
		IntersectionPoint opposite = getOppositePoint(ip1);
		if(opposite == null){
			return false;
		} else {
			if(!Arrays.equals(opposite.getPoint(), ip2.getPoint())){
				return false;
			} else {
				return true;
			}
		}
		
	}
	
	private static int getShiftedIndexFromIntersectionPointAndCurveIndex(IntersectionPoint ip, int curveIndex){
		LinkedList<LineSegment> intersectingSegments = ip.getIntersectingSegments();
		int shiftedIndex = 0;
		for (LineSegment ls : intersectingSegments) {
			if(ls.getCurveIndex() == curveIndex){
				shiftedIndex = ls.getShiftedIndex();
			}
		}
		return shiftedIndex;
	}
	
	private static int getRightShiftFromIntersectionPointAndCurveIndex(IntersectionPoint ip, int curveIndex){
		LinkedList<LineSegment> intersectingSegments = ip.getIntersectingSegments();
		int rightShift = 0;
		for (LineSegment ls : intersectingSegments) {
			if(ls.getCurveIndex() == curveIndex){
				rightShift = ls.getRightShift();
			}
		}
		return rightShift;
	}
	private static int getUpShiftFromIntersectionPointAndCurveIndex(IntersectionPoint ip, int curveIndex){
		LinkedList<LineSegment> intersectingSegments = ip.getIntersectingSegments();
		int upShift = 0;
		for (LineSegment ls : intersectingSegments) {
			if(ls.getCurveIndex() == curveIndex){
				upShift = ls.getUpShift();
			}
		}
		return upShift;
	}
	
	private boolean pointsHaveGivenCurveIndexAndInSameDomain(IntersectionPoint ip1, IntersectionPoint ip2, int curveIndex){
		if(ip1.getIndexList().contains(curveIndex) && ip2.getIndexList().contains(curveIndex)){
			int rightShift1 = getRightShiftFromIntersectionPointAndCurveIndex(ip1, curveIndex);
			int rightshift2 = getRightShiftFromIntersectionPointAndCurveIndex(ip2, curveIndex);
			int upShift1 = getUpShiftFromIntersectionPointAndCurveIndex(ip1, curveIndex);
			int upShift2 = getUpShiftFromIntersectionPointAndCurveIndex(ip2, curveIndex);
			if((rightShift1 == rightshift2) && (upShift1 == upShift2)){
				return true;
			}
		}
		return false;
	}
	
	private boolean pointsHaveCurveIndexAndInSameDomain(IntersectionPoint ip1, IntersectionPoint ip2, int curveIndex){
		if(ip1.getIndexList().contains(curveIndex) && ip2.getIndexList().contains(curveIndex)){
			int shiftedIndex1 = getShiftedIndexFromIntersectionPointAndCurveIndex(ip1, curveIndex);
			int shiftedIndex2 = getShiftedIndexFromIntersectionPointAndCurveIndex(ip2, curveIndex);
			if(shiftedIndex1 == shiftedIndex2){
				return true;
			}
		}
		return false;
	}
	
	
	public LinkedList<HalfedgePoint> findAllLocalNbrs(NURBSSurface ns, double dilation, LinkedList<IntersectionPoint> intersectionPoints){
		LinkedList<HalfedgePoint> points = new LinkedList<HalfedgePoint>();
		System.out.println("Boundary ");
		for (Double value : ns.getBoundaryValuesPastIntersection(dilation)) {
			System.out.println(value);
		}
		System.out.println("CLOSEDBoundary past intersection algorithm" + Arrays.toString(ns.getClosedBoundaryValuesPastIntersection(dilation)));
		System.out.println("CLOSED BOUNDARY PIONTS");
		for (IntersectionPoint iP1 : intersectionPoints) {
			ClosedBoundary cb = iP1.getClosedBoundary(ns, dilation);
			if(cb != ClosedBoundary.interior){
				System.out.println(cb + " coords " + Arrays.toString(iP1.getPoint()));
			}
			LinkedList<IndexedCurveList> iP1CurveList = new LinkedList<IndexedCurveList>();
//			LinkedList<Integer> indexList = getIndexListFromIntersectionPoint(iP1);
			iP1.setIndexList(getIndexListFromIntersectionPoint(iP1));
			if(iP1.getIndexList().size() > 2){
				System.out.println("indexList:");
				for (Integer index : iP1.getIndexList()) {
					System.out.println(index);
				}
				System.out.println("at intersection point " + Arrays.toString(iP1.getPoint()));
			}
	
			// add for each curve intersecting this intersectionPoint all IntersectionPoints contained in this curve
			double[] check = {6.283185307, -0.867038755};
			for (Integer index1 : iP1.getIndexList()){
				IndexedCurveList icl = new IndexedCurveList(index1, new LinkedList<IntersectionPoint>());
//				int shiftedIndex1 = getShiftedIndexFromIntersectionPointAndCurveIndex(iP1, index1);
				iP1CurveList.add(icl);
				if(Arrays.equals(check, iP1.getPoint())){
					System.out.println("all points on lines intersecting at " + Arrays.toString(check));
					System.out.println(" iP1.getIndexList()");
					for (Integer index : iP1.getIndexList()) {
						System.out.println(index);
					}
					System.out.println("getIndexListFromIntersectionPoint(iP1)");
					for (Integer index : getIndexListFromIntersectionPoint(iP1)) {
						System.out.println(index);
					}
					
				}
				System.out.println("IP1 IS THE PIONT " + Arrays.toString(iP1.getPoint()) + " and the index = " + index1);
				for (IntersectionPoint iP2 : intersectionPoints) {
//					LinkedList<Integer> curveIndices = iP2.getIndexList();
//					int shiftedIndex2 = getShiftedIndexFromIntersectionPointAndCurveIndex(iP2, index1);
//					if(curveIndices.contains(index1) && !areOppositePoints(iP1, iP2)){
					if(pointsHaveGivenCurveIndexAndInSameDomain(iP1, iP2, index1)){
//					if(pointsHaveCurveIndexAndInSameDomain(iP1, iP2, index1)){
						System.out.println("pointsAreOnSameLineAndInSameDomain(iP1, iP2) iP2 is : " + Arrays.toString(iP2.getPoint()));
						icl.getCurveList().add(iP2);
						if(Arrays.equals(check, iP1.getPoint())){
							System.out.println(Arrays.toString(iP2.getPoint()));
						}
					}
				}
				
			}
			
			
			
			
			
			LinkedList<IntersectionPoint> nbrs = new LinkedList<IntersectionPoint>();
			
			for (IndexedCurveList icl : iP1CurveList) {
				boolean cyclic = getCyclicFromCurveIndexAndIntersectionPoint(icl.getIndex(), iP1);
				
				// sort each curveList w.r.t. indexOnCurve
				
				IntersectionPointIndexComparator ipic = new IntersectionPointIndexComparator();
				ipic.curveIndex = icl.getIndex();
				Collections.sort(icl.getCurveList(), ipic);
				
				
				// add for each indexOnCurve all IntersectionPoints with the same index to the list
				
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
//		System.out.println("CHECK      !!!!!!!!!!!!!!!!!!!!!!!!!");
//		points = orientedNbrs(points);
//		double check = -1.570796326794897;
//		int point = 0;
//		for (HalfedgePoint hp : points) {
//			System.out.println("hp = " + Arrays.toString(hp.getPoint().getPoint()));
//			if(hpContainsIndex(hp, check)){
//				point++;
//				System.out.println(point+". HP");
//				System.out.println("point = " + Arrays.toString(hp.getPoint().getPoint()));
//				System.out.println("nbrs");
//				for (IntersectionPoint ip : hp.getNbrs()) {
//					System.out.println(Arrays.toString(ip.getPoint()));
//				}
//			}
//		}
		
//		System.out.println("FIRST CHECK");
//		for (IntersectionPoint ip : intersectionPoints) {
//			System.out.println(ip.getParentHP().toString());
//		}
		for (HalfedgePoint hp : points) {
			
			double[] check = {6.283185307, -0.867038755};
			if(Arrays.equals(check, hp.getIntersectionPoint().getPoint())){
				System.out.println("nbr check");
				System.out.println("all nbrs of " + Arrays.toString(check));
				for (IntersectionPoint ip : hp.getNbrs()) {
					System.out.println(Arrays.toString(ip.getPoint()));
				}
			}
		}
		return points;
	}
	
	
	/*
	 * 
	 */
	
//	private LinkedList<IntersectionPoint>  allAjacentNbrs(NURBSSurface ns, HalfedgePoint point , LinkedList<HalfedgePoint> halfedgePoints){
//		LinkedList<IntersectionPoint> allAjacentNbrs = new LinkedList<IntersectionPoint>();
//		LinkedList<HalfedgePoint> allNbrs = new LinkedList<HalfedgePoint>();
//		IntersectionPoint firstIP = point.getUnusedNbrs().getLast();
//		HalfedgePoint first = firstIP.getParentHP();
//		allNbrs.add(point);
//		allAjacentNbrs.add(point.getPoint());
//		allNbrs.add(first);
//		allAjacentNbrs.add(first.getPoint());
//		IntersectionPoint before = point.getPoint();
//		HalfedgePoint bP = point;
//		
//		// remove the start direction from unused nbrs
//		
//		LinkedList<IntersectionPoint> removedStartDirectionFirst = new LinkedList<IntersectionPoint>();
//		for (IntersectionPoint ip : bP.getUnusedNbrs()) {
//			if(ip.getParentHP() != first){
//				removedStartDirectionFirst.add(ip);
//			}
//		}
//		bP.setUnusedNbrs(removedStartDirectionFirst);
//		
//		while(point != allNbrs.getLast()){
//			IntersectionPoint next = getNextNbr(ns, before, allNbrs.getLast());
//			before = allAjacentNbrs.getLast();
//			bP = before.getParentHP();
//			
//			LinkedList<IntersectionPoint> removedStartDirection = new LinkedList<IntersectionPoint>();
//			for (IntersectionPoint ip : bP.getUnusedNbrs()) {
//				// debug
//				if(ip.getParentHP() != next.getParentHP()){
//					removedStartDirection.add(ip);
//				}
//				// end debug
//			}
//			bP.setUnusedNbrs(removedStartDirection);
//			
//			allAjacentNbrs.add(next);
////			if(next.getParentHP() == null){
////				System.out.println("NEXT");
////				System.out.println(Arrays.toString(next.getPoint()));
////				PointSetFactory psf = new PointSetFactory();
////				psf.setVertexCount(1);
////				psf.setVertexCoordinates(next.getPoint());
////				psf.update();
////				SceneGraphComponent sgc = new SceneGraphComponent("geodesic segment");
////				SceneGraphComponent minCurveComp = new SceneGraphComponent("Geodesic Segment");
////				sgc.addChild(minCurveComp);
////				sgc.setGeometry(psf.getGeometry());
////				Appearance labelAp = new Appearance();
////				sgc.setAppearance(labelAp);
////				DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(labelAp, false);
////				DefaultPointShader pointShader = (DefaultPointShader)dgs.getPointShader();
////				pointShader.setDiffuseColor(Color.magenta);
////				hif.getActiveLayer().addTemporaryGeometry(sgc);
////				
////			}
//			HalfedgePoint hP = next.getParentHP();
//			allNbrs.add(hP);
//		}
//		allAjacentNbrs.pollLast();
//		return allAjacentNbrs;
//	}
	
	
	/*
	 * returns the next vertex w.r.t. a face in order
	 */
	
	public static IntersectionPoint getNextNbrLocal(IntersectionPoint previous, HalfedgePoint p){
		boolean isEqual = false;
		for (IntersectionPoint ip : p.getNbrs()) {
			if(ip == previous){
				isEqual = true;
			}
			else if(isEqual){
				return ip;
			}
		}
		return null;
	}
	
	private boolean isClosedBoundaryPoint(IntersectionPoint ip){
		if(ip.getClosedBoundary(ns, dilation) == ClosedBoundary.interior){
			return false;
		}
		return true;
	}
	
	private boolean isFaceVertex(IntersectionPoint ip){
		if(ip.getIndexList().size() > 2){
			return true;
		}
		return false;
	}
	
	
	
//	public IntersectionPoint getNextNbr(IntersectionPoint previous, HalfedgePoint p){
//		IntersectionPoint next = null;
//		if(previous == null){
//			next = p.getUnusedNbrs().pollLast();
//			next.getParentHP().setPrevious(p.getIntersectionPoint());
//		}
//		else{
//			next = getNextNbrLocal(previous, p);
//			p.getUnusedNbrs().remove(next);
//			next.getParentHP().setPrevious(p.getIntersectionPoint());
//		}
//		return next;
//	}
	
//	public IntersectionPoint getNextNbr(IntersectionPoint previous, HalfedgePoint p){
////		System.out.println();
////		if(previous != null){
////			System.out.println("PRIVIOUS = " + Arrays.toString(previous.getPoint()));
////		}
////		else{
////			System.out.println("PRIVIOUS = null");
////		}
////		System.out.println("point p  = " + Arrays.toString(p.getIntersectionPoint().getPoint()));
//		IntersectionPoint next = null;
//		if(previous == null){
//			System.out.println("previous == null");
//			IntersectionPoint nextLocal = p.getUnusedNbrs().pollLast();
//			if(!isClosedBoundaryPoint(nextLocal)){
//				next = nextLocal;
//				next.getParentHP().setPrevious(p.getIntersectionPoint());
//			}
//			else{
//				if(!isFaceVertex(nextLocal)){
//					IntersectionPoint newP = getOppositePoint(nextLocal);
//					IntersectionPoint newPrevious = getOppositePoint(getNextNbrLocal(p.getIntersectionPoint(), nextLocal.getParentHP()));
//					next = getNextNbr(newPrevious, newP.getParentHP());
//					next.getParentHP().setPrevious(newP);
//				}
//				else{
//					next = nextLocal;
//					next.getParentHP().setPrevious(p.getIntersectionPoint());
//				}
//			}
//		}
//		else{
//			IntersectionPoint nextLocal = getNextNbrLocal(previous, p);
//			p.getUnusedNbrs().remove(nextLocal);
//			if(!isClosedBoundaryPoint(p.getIntersectionPoint())){
//				if(!isClosedBoundaryPoint(nextLocal)){
//					next = nextLocal;
//					next.getParentHP().setPrevious(p.getIntersectionPoint());
//				}
//				else{
//					if(!isFaceVertex(nextLocal)){
//						IntersectionPoint newP = getOppositePoint(nextLocal);
//						IntersectionPoint newPrevious = getOppositePoint(getNextNbrLocal(p.getIntersectionPoint(), nextLocal.getParentHP()));
//						next = getNextNbr(newPrevious, newP.getParentHP());
//						next.getParentHP().setPrevious(newP);
//					}
//					else{
//						next = nextLocal;
//						next.getParentHP().setPrevious(p.getIntersectionPoint());
//					}
//				}
//			}
//			else{
//				if(!isClosedBoundaryPoint(nextLocal)){
//					next = nextLocal;
//					next.getParentHP().setPrevious(p.getIntersectionPoint());
//				}
//				else{
//					IntersectionPoint newP = getOppositePoint(p.getIntersectionPoint());
//					IntersectionPoint newPrevious = getOppositePoint(nextLocal);
//					next = getNextNbrLocal(newPrevious, newP.getParentHP());
//					next.getParentHP().setPrevious(newPrevious);
//				}
//			}
//		}
//		System.out.println("NEXT = " + Arrays.toString(next.getPoint()));
//		return next;
//	}
	
	
	
	public IntersectionPoint getNextNbr(IntersectionPoint previous, HalfedgePoint p){
		System.out.println("point p  = " + Arrays.toString(p.getIntersectionPoint().getPoint()));
//		System.out.println("unusedNbrs");
//		for (IntersectionPoint ip : p.getUnusedNbrs()) {
//			System.out.println(Arrays.toString(ip.getPoint()));
//		}
		System.out.println();
		if(previous != null){
			System.out.println("PRIVIOUS = " + Arrays.toString(previous.getPoint()));
		}
		else{
			System.out.println("PRIVIOUS = null");
		}
	
		IntersectionPoint next = null;
		if(previous == null){
			System.out.println("1. previous == null");
			IntersectionPoint nextLocal = p.getUnusedNbrs().pollLast();
			System.out.println("next local " + Arrays.toString(nextLocal.getPoint()));
			if(!isClosedBoundaryPoint(nextLocal)){
				System.out.println("1.1. next local != closed boundary");
				next = nextLocal;
				next.getParentHP().setPrevious(p.getIntersectionPoint());
			}
			else{
				System.out.println("1.2. next local == closed oundary");
				if(!isFaceVertex(nextLocal)){
					System.out.println("1.2.1. next local != face vertex");
					IntersectionPoint newP = getOppositePoint(nextLocal);
					System.out.println("(getNextNbrLocal(p.getIntersectionPoint(), nextLocal.getParentHP()" + Arrays.toString(getNextNbrLocal(p.getIntersectionPoint(), nextLocal.getParentHP()).getPoint()));
					IntersectionPoint newPrevious = getOppositePoint(getNextNbrLocal(p.getIntersectionPoint(), nextLocal.getParentHP()));
					System.out.println("newPrevious " + Arrays.toString(newPrevious.getPoint()));
					next = getNextNbrLocal(newPrevious, newP.getParentHP());
					next.getParentHP().setPrevious(newP);
				}
				else{
					System.out.println("1.2.2. next local == face vertex");
					next = nextLocal;
					next.getParentHP().setPrevious(p.getIntersectionPoint());
				}
			}
		}
		else{
			System.out.println("1. previous != null");
			IntersectionPoint nextLocal = getNextNbrLocal(previous, p);
			System.out.println("next local " + Arrays.toString(nextLocal.getPoint()));
		
			if(!isClosedBoundaryPoint(p.getIntersectionPoint())){
				p.getUnusedNbrs().remove(nextLocal);
				System.out.println("1. p != closed boundary");
				if(!isClosedBoundaryPoint(nextLocal)){
					System.out.println("1.1. next local != closed boundary");
					next = nextLocal;
					next.getParentHP().setPrevious(p.getIntersectionPoint());
				}
				else{
					System.out.println("1.2. next local == closed boundary");
					if(!isFaceVertex(nextLocal)){
						System.out.println("1.2.1. next local != face vertex");
						IntersectionPoint newP = getOppositePoint(nextLocal);
						System.out.println("newP " + Arrays.toString(newP.getPoint()));
						IntersectionPoint newPrevious = getOppositePoint(getNextNbrLocal(p.getIntersectionPoint(), nextLocal.getParentHP()));
						System.out.println("newPrevious " + Arrays.toString(newPrevious.getPoint()));
						System.out.println("all orinted nbrs of newP");
						for (IntersectionPoint ip : newP.getParentHP().getNbrs()) {
							System.out.println(Arrays.toString(ip.getPoint()));
						}
						next = getNextNbrLocal(newPrevious, newP.getParentHP());
						System.out.println("getNextNbrLocal(newPrevious, newP.getParentHP()); " + Arrays.toString(next.getPoint()));
						next.getParentHP().setPrevious(newP);
					}
					else{
						System.out.println("1.2.2. next local == face vertex");
						next = nextLocal;
						next.getParentHP().setPrevious(p.getIntersectionPoint());
					}
				}
			}
			else{
				System.out.println("2. p == closed boundary");
				if(!isClosedBoundaryPoint(nextLocal)){
					System.out.println("1.2. next local != closed boundary");
					next = nextLocal;
					next.getParentHP().setPrevious(p.getIntersectionPoint());
				}
				else{
					System.out.println("2.2. next local == closed boundary");
					IntersectionPoint newP = getOppositePoint(p.getIntersectionPoint());
					System.out.println("newP " + Arrays.toString(newP.getPoint()));
					IntersectionPoint newPrevious = getOppositePoint(nextLocal);
					System.out.println("newPrevious " + Arrays.toString(newPrevious.getPoint()));
					next = getNextNbrLocal(newPrevious, newP.getParentHP());
					next.getParentHP().setPrevious(newP);
				}
			}
		}
		System.out.println("NEXT = " + Arrays.toString(next.getPoint()));
		return next;
	}
	
	private LinkedList<IntersectionPoint>  getAllFaceVertices(HalfedgePoint p){
		LinkedList<IntersectionPoint> allFaceVerts = new LinkedList<IntersectionPoint>();
		IntersectionPoint previous = null;
		allFaceVerts.add(p.getIntersectionPoint());
		System.out.println("START getAllFaceVertices");
		System.out.println("unused nbrs:");
		for (IntersectionPoint ip : p.getUnusedNbrs()) {
			System.out.println(Arrays.toString(ip.getPoint()));
		}
		System.out.println();
		boolean first = true;
		while(first || p.getIntersectionPoint() != allFaceVerts.getLast()){
			first = false;
			IntersectionPoint current = getNextNbr(previous, allFaceVerts.getLast().getParentHP());
			previous = current.getParentHP().getPrevious();
//			if(current.getClosedBoundary(ns, dilation) != ClosedBoundary.interior){
//				current = getUniqueClosedBoundryPoint(current);
//			}
			allFaceVerts.add(current);
		}
		allFaceVerts.pollLast();
		System.out.println("all face verts");
		LinkedList<IntersectionPoint> uniqueFaceVerts = new LinkedList<IntersectionPoint>();
		for (IntersectionPoint ip : allFaceVerts) {
			ClosedBoundary cb = ip.getClosedBoundary(ns, dilation);
			if(cb == ClosedBoundary.right || cb == ClosedBoundary.upper){
				uniqueFaceVerts.add(getOppositePoint(ip));
			}
			else{
				uniqueFaceVerts.add(ip);
			}
			System.out.println(Arrays.toString(uniqueFaceVerts.getLast().getPoint()));
		}
		return uniqueFaceVerts;
	}
	
	private IntersectionPoint getOppositePoint(IntersectionPoint ip){
		if(ip.getClosedBoundary(ns, dilation) == ClosedBoundary.interior){
			throw new IllegalArgumentException("IntersectionPoint " + Arrays.toString(ip.getPoint()) + " does not lie on the closed boundary");
		}
		else if(ip.getClosedBoundary(ns, dilation) == ClosedBoundary.left){
			System.out.println("left and rightBound");
			for (IntersectionPoint right : rightBound) {
				System.out.println(Arrays.toString(right.getPoint()));
				if(ip.getPoint()[1] == right.getPoint()[1]){
					return right;
				}
			}
		}
		else if(ip.getClosedBoundary(ns, dilation) == ClosedBoundary.right){
			System.out.println("right and leftBound");
			for (IntersectionPoint left : leftBound) {
				System.out.println(Arrays.toString(left.getPoint()));
				if(ip.getPoint()[1] == left.getPoint()[1]){
					return left;
				}
			}
		}
		else if(ip.getClosedBoundary(ns, dilation) == ClosedBoundary.upper){
			for (IntersectionPoint lower : lowerBound) {
				if(ip.getPoint()[0] == lower.getPoint()[0]){
					return lower;
				}
			}
		}
		else if(ip.getClosedBoundary(ns, dilation) == ClosedBoundary.lower){
			for (IntersectionPoint upper : upperBound) {
				if(ip.getPoint()[0] == upper.getPoint()[0]){
					return upper;
				}
			}
		}
		System.out.println("ELSE");
		return null;
	}
	
	private IntersectionPoint getUniqueClosedBoundryPoint(IntersectionPoint ip){
		IntersectionPoint unique = ip;
		ClosedBoundary cb = ip.getClosedBoundary(ns, dilation);
		if(cb == ClosedBoundary.upper || cb == ClosedBoundary.right){
			unique = getOppositePoint(ip);
		}
		return unique;
	}
	
	
//	private IntersectionPoint getUniqueClosedBoundryPoint(IntersectionPoint nextLocal, IntersectionPoint p){
//		IntersectionPoint unique = nextLocal;
//		System.out.println("getUniqueClosedBoundryPoint");
//		if(nextLocal.getClosedBoundary(ns, dilation) == ClosedBoundary.interior){
//			throw new IllegalArgumentException("IntersectionPoint " + Arrays.toString(nextLocal.getPoint())+ " does not lie on the closed boundary");
//		}
//		else{
//			ClosedBoundary cb = nextLocal.getClosedBoundary(ns, dilation);
//			System.out.println("switch");
//			
//			if(cb == ClosedBoundary.upper || cb == ClosedBoundary.right){
//				if(getNextNbrLocal(p, nextLocal.getParentHP()).getClosedBoundary(ns, dilation) != ClosedBoundary.interior){
//					unique = getOppositePoint(nextLocal);
//					System.out.println("unique " + Arrays.toString(unique.getPoint()));
//					System.out.println("p " + Arrays.toString(p.getPoint()));
//					System.out.println("nextLocal " + Arrays.toString(nextLocal.getPoint()));
//					System.out.println("getNextNbrLocal(p, nextLocal.getParentHP()) " + Arrays.toString(getNextNbrLocal(p, nextLocal.getParentHP()).getPoint()));
//					IntersectionPoint newPrevious = getOppositePoint(getNextNbrLocal(p, nextLocal.getParentHP()));
//					System.out.println("newPrevious " + Arrays.toString(newPrevious.getPoint()));
//					unique.getParentHP().setPrevious(newPrevious);
//					System.out.println("unique.getPatrentHP() " + Arrays.toString(unique.getParentHP().getIntersectionPoint().getPoint()));
//				}
//				else{
//					unique = getOppositePoint(nextLocal);
//				}
//			}
//			else{
//				unique.getParentHP().setPrevious(p);
//			}
//		}
//		System.out.println("unique end " + Arrays.toString(unique.getPoint()));
//		return unique;
//	}
	
	
	private static boolean faceConsistsOnlyOfBoundaryVertices(LinkedList<IntersectionPoint> facePoints, List<Double> boundaryValues){
		 for (IntersectionPoint ip : facePoints) {
			if(!ip.isBoundaryPoint(boundaryValues)){
				if(Math.abs(ip.getPoint()[0] - 20.0) < 1){
					System.out.println("boundary values:");
					for (Double value : boundaryValues) {
						System.out.println(value);
					}
					System.out.println("not a boundary value " + Arrays.toString(ip.getPoint()));
				}
				return false;
			}
		}
		return true;
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
					double[] middle = hP.getIntersectionPoint().getPoint();
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
						double[] middle = hP.getIntersectionPoint().getPoint();
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
			for (IntersectionPoint ip : orientedList) {
				System.out.println("ip.getParentHP " + ip.getParentHP());
				if(before != ip){
					ori.add(ip);
				}
				before = ip;
			}
			if(ori.getLast() == ori.getFirst() && ori.size() > 1){
				ori.pollLast();
			}
			ori.add(ori.getFirst());
			hP.setNbrs(ori);
		}
		
		
		return halfPoints;
	}
	
	private boolean isValidBoundaryFaceVertex(HalfedgePoint hp){
//		if(!isClosedBoundaryPoint(hp.getIntersectionPoint())){
//			System.err.println("no closed boundary point");
//		}
//		else if(!isFaceVertex(hp.getIntersectionPoint())){
//			System.err.println("no closed boundary point");
//		}
		ClosedBoundary cb = hp.getIntersectionPoint().getClosedBoundary(ns, dilation);
		if(isFaceVertex(hp.getIntersectionPoint()) && (cb == ClosedBoundary.left || cb == ClosedBoundary.lower)){
			return true;
		}
		return false;
	}
	
	private boolean isValidHalfEdgePoint(HalfedgePoint hp){
		if(isClosedBoundaryPoint(hp.getIntersectionPoint()) && !isValidBoundaryFaceVertex(hp)){
			return false;
		}
		return true;
	}
	
	
	public FaceSet createFaceSet(NURBSSurface ns, LinkedList<HalfedgePoint> orientedNbrs, List<double[]> boundaryVerts, double dilation){
		FaceSet fS = new FaceSet();
		List<HalfedgePoint> points = new LinkedList<HalfedgePoint>();
		List<HalfedgePoint> validVerts = new LinkedList<HalfedgePoint>();
		System.out.println("ALL USE#D POINTS");
		System.out.println("boundary values " + ns.getBoundaryValuesPastIntersection(dilation));
		System.out.println("closind direction " + ns.getClosingDir());
		System.out.println("closed boundary values " + Arrays.toString(ns.getClosedBoundaryValuesPastIntersection(dilation)));
		System.out.println();
		for (HalfedgePoint hp : orientedNbrs) {
//			System.out.println("ip " + Arrays.toString(hp.getIntersectionPoint().getPoint()) + " getClosedBoundary " + hp.getIntersectionPoint().getClosedBoundary(ns, dilation));
//			if(!isClosedBoundaryPoint(hp.getIntersectionPoint())){
			if(isValidHalfEdgePoint(hp)){
				validVerts.add(hp);
				System.out.println("all starting points " + Arrays.toString(hp.getIntersectionPoint().getPoint()));
			}
			if(!isClosedBoundaryPoint(hp.getIntersectionPoint())){
				points.add(hp);
			}
		}
		System.out.println();
		double[][] verts = new double[validVerts.size()][2];
		LinkedList<int[]> faceVerts = new LinkedList<int[]>();
//		int c = 0;
//		for (HalfedgePoint hP: points) {
//			LinkedList<IntersectionPoint> unusedNbrs = new LinkedList<IntersectionPoint>();
//			for (IntersectionPoint iP : hP.getNbrs()) {
//				unusedNbrs.add(iP);
//			}
//			unusedNbrs.pollLast();
//			hP.setUnusedNbrs(unusedNbrs);
//			verts[c] = hP.getIntersectionPoint().getPoint();
//			c++;
//		}
		int c = 0;
		for (HalfedgePoint hP: validVerts) {
			verts[c] = hP.getIntersectionPoint().getPoint();
			c++;
		}
		for (HalfedgePoint hP: points) {
			LinkedList<IntersectionPoint> unusedNbrs = new LinkedList<IntersectionPoint>();
			for (IntersectionPoint iP : hP.getNbrs()) {
				unusedNbrs.add(iP);
			}
			unusedNbrs.pollLast();
			hP.setUnusedNbrs(unusedNbrs);
		}
		fS.setVerts(verts);
		int faceIndexTest = 0;
		System.out.println("All faces");
		Set<Double> boundaryValues = new HashSet<Double>();
		boundaryValues = ns.getBoundaryValuesPastIntersection(dilation);
		List<Double> boundValues = new LinkedList<Double>();
		for (Double d : boundaryValues) {
			boundValues.add(d);
		}
		for (HalfedgePoint hP : points) {
			while(!hP.getUnusedNbrs().isEmpty()){
				if(!isClosedBoundaryPoint(hP.getIntersectionPoint())){
					faceIndexTest ++;
					LinkedList<IntersectionPoint> facePoints = getAllFaceVertices(hP);
					System.out.println("Face index = " + faceIndexTest);
					for (IntersectionPoint iP : facePoints) {
						System.out.println(Arrays.toString(iP.getPoint()));
					}
					if(!faceConsistsOnlyOfBoundaryVertices(facePoints, boundValues)){
						LinkedList<Integer> ind = new LinkedList<Integer>();
						for (IntersectionPoint fP : facePoints) {
							if(fP.getClosedBoundary(ns, dilation) != ClosedBoundary.interior){
								System.out.println();
								System.out.println("CREATE FACE SET: check unique boundary");
								System.out.println();
								System.out.println("point before " + Arrays.toString(fP.getPoint()));
								fP = getUniqueClosedBoundryPoint(fP);
								System.out.println("point past " + Arrays.toString(fP.getPoint()));
							}
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
						faceVerts.add(index);
					}
					else{
						System.out.println();
						System.out.println("    null    FACE");
						System.out.println();
					}
				}
			}
		}
		int[][] faceIndex = new int[faceVerts.size()][];
		int counter = 0;
		for (int[] fs : faceVerts) {
				faceIndex[counter] = fs;
				counter++;
		}
		fS.setFaces(faceIndex);
		return fS;
	}
	
//	public FaceSet createFaceSet(NURBSSurface ns, LinkedList<HalfedgePoint> orientedNbrs, List<double[]> boundaryVerts, double dilation){
//		FaceSet fS = new FaceSet();
//		double[][] verts = new double[orientedNbrs.size()][2];
//		LinkedList<int[]> faceNbrs = new LinkedList<int[]>();
//		int c = 0;
//		for (HalfedgePoint hP: orientedNbrs) {
//			LinkedList<IntersectionPoint> unusedNbrs = new LinkedList<IntersectionPoint>();
//			for (IntersectionPoint iP : hP.getNbrs()) {
//				unusedNbrs.add(iP);
//			}
//			unusedNbrs.pollLast();
//			hP.setUnusedNbrs(unusedNbrs);
//			verts[c] = hP.getIntersectionPoint().getPoint();
//			c++;
//		}
//		fS.setVerts(verts);
//		int faceIndexTest = 0;
//		System.out.println("All faces");
//		LinkedList<Double> boundaryValues = ns.getBoundaryValuesPastIntersection(dilation);
//		for (HalfedgePoint hP : orientedNbrs) {
//			while(!hP.getUnusedNbrs().isEmpty()){
//				faceIndexTest ++;
//				LinkedList<IntersectionPoint> facePoints = getAllFaceVertices(hP);
//				System.out.println("Face index = " + faceIndexTest);
//				for (IntersectionPoint iP : facePoints) {
//					System.out.println(Arrays.toString(iP.getPoint()));
//				}
//				if(!faceConsistsOnlyOfBoundaryVertices(facePoints, boundaryValues)){
//					LinkedList<Integer> ind = new LinkedList<Integer>();
//					for (IntersectionPoint fP : facePoints) {
//						for (int i = 0; i < verts.length; i++) {
//							if(fP.getPoint() == verts[i]){
//								ind.add(i);
//							}
//						}
//					}
//					int[] index = new int[ind.size()];
//					int count = 0;
//					for (Integer i : ind) {
//						index[count] = i;
//						count++;
//					}
//					faceNbrs.add(index);
//				}
//				else{
//					System.out.println();
//					System.out.println("        FACE");
//					System.out.println();
//				}
//			}
//		}
//		int[][] faceIndex = new int[faceNbrs.size()][];
//		int counter = 0;
//		for (int[] fs : faceNbrs) {
//				faceIndex[counter] = fs;
//				counter++;
//		}
//		fS.setFaces(faceIndex);
//		return fS;
//	}

}
