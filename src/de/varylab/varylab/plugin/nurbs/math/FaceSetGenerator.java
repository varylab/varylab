package de.varylab.varylab.plugin.nurbs.math;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.IntersectionPointDistanceComparator;
import de.varylab.varylab.plugin.nurbs.IntersectionPointIndexComparator;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.ClosingDir;
import de.varylab.varylab.plugin.nurbs.data.FaceSet;
import de.varylab.varylab.plugin.nurbs.data.IndexedCurveList;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint.ClosedBoundary;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint.FaceVertex;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;

public class FaceSetGenerator {
	
	private static Logger logger = Logger.getLogger(FaceSetGenerator.class.getName());

	private LinkedList<IntersectionPoint> leftBound = new LinkedList<IntersectionPoint>();
	private LinkedList<IntersectionPoint> rightBound = new LinkedList<IntersectionPoint>();
	private LinkedList<IntersectionPoint> upperBound = new LinkedList<IntersectionPoint>();
	private LinkedList<IntersectionPoint> lowerBound = new LinkedList<IntersectionPoint>();
	private double dilation = 0;
	private NURBSSurface ns;
	LinkedList<IntersectionPoint> ipList;
	List<double[]> boundaryVerts;

	
	public FaceSetGenerator(NURBSSurface surface, double d, LinkedList<IntersectionPoint> ipl) {
		ipList = ipl;
		dilation = d;
		ns = surface;
		for (IntersectionPoint ip : ipList) {
			ClosedBoundary cb = ip.getClosedBoundary(surface, d);
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
		boundaryVerts = surface.getBoundaryVerticesUV();
	}
	
	

	public void determineOrientedNbrs(){
		for (IntersectionPoint ip : ipList) {
			System.out.println("the point");
			for (IntersectionPoint nbr : ip.getNbrs()) {
				System.out.println(nbr.toString());
			}
			ip.makeOrientedNbrs();
		}
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
//			log.info("firstCoord "+Arrays.toString(firstCoordFromIndexedSegment)+" iP "+Arrays.toString(iP.point)+" iP.sameIndexDist " + iP.sameIndexDist);
		}
		Collections.sort(sameIndexList, new IntersectionPointDistanceComparator());
		return sameIndexList;
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
	
	
	public void determineLocalNbrs(){
//		LinkedList<IntersectionPoint> points = new LinkedList<IntersectionPoint>();
		logger.info("Boundary ");
	
		for (Double value : ns.getBoundaryValuesPastIntersection(dilation)) {
			logger.info(value.toString());
		
		}
		for (IntersectionPoint iP1 : ipList) {
			ClosedBoundary cb = iP1.getClosedBoundary(ns, dilation);
			if(cb != ClosedBoundary.interior){
				logger.info(cb + " coords " + Arrays.toString(iP1.getPoint()));
			}
			LinkedList<IndexedCurveList> iP1CurveList = new LinkedList<IndexedCurveList>();
//			LinkedList<Integer> indexList = getIndexListFromIntersectionPoint(iP1);
			iP1.setIndexList(getIndexListFromIntersectionPoint(iP1));
			
	
			// add for each curve intersecting this intersectionPoint all IntersectionPoints contained in this curve
	
			for (Integer index1 : iP1.getIndexList()){
				IndexedCurveList icl = new IndexedCurveList(index1, new LinkedList<IntersectionPoint>());
//				int shiftedIndex1 = getShiftedIndexFromIntersectionPointAndCurveIndex(iP1, index1);
				iP1CurveList.add(icl);
				
				for (IntersectionPoint iP2 : ipList) {
//					LinkedList<Integer> curveIndices = iP2.getIndexList();
//					int shiftedIndex2 = getShiftedIndexFromIntersectionPointAndCurveIndex(iP2, index1);
//					if(curveIndices.contains(index1) && !areOppositePoints(iP1, iP2)){
					if(pointsHaveGivenCurveIndexAndInSameDomain(iP1, iP2, index1)){
//					if(pointsHaveCurveIndexAndInSameDomain(iP1, iP2, index1)){
					
						icl.getCurveList().add(iP2);
					
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
			iP1.setNbrs(nbrs);
		}

		logger.info("list of all points and its nbrs");
		for (IntersectionPoint p : ipList) {
			logger.info("point = " + Arrays.toString(p.getPoint()));
			logger.info("nbrs");
			for (IntersectionPoint ip : p.getNbrs()) {
				System.out.println(Arrays.toString(ip.getPoint()));
			}
		}
	}
	
	
	
	/*
	 * returns the next vertex w.r.t. a face in order
	 */
	
	public static IntersectionPoint getNextNbrLocal(IntersectionPoint previous, IntersectionPoint p){
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
		if(ip.getFaceVertex() == FaceVertex.faceVertex){
			return true;
		}
		return false;
	}
	
	public IntersectionPoint getNextNbr(IntersectionPoint previous, IntersectionPoint current){
		if(current.getPoint()[0] == 0.6945708202006533 && current.getPoint()[1] == 0.5248006433647975){
			logger.info(" ");
			if(previous != null){
				logger.info("prev = " + Arrays.toString(previous.getPoint()));
			} else {
				logger.info("prev == null");
			}
			
			logger.info("special point");
			logger.info("unused nbrs");
			for (IntersectionPoint ip : current.getUnusedNbrs()) {
				logger.info(" " + Arrays.toString(ip.getPoint()));
			}
			logger.info("all nbrs");
			for (IntersectionPoint ip : current.getNbrs()) {
				logger.info(" " + Arrays.toString(ip.getPoint()));
			}
			if(previous != null){
				logger.info("next = " + Arrays.toString(getNextNbrLocal(previous, current).getPoint()));
			} else {
				logger.info("unusedNbrs.getLast() = " + Arrays.toString(current.getUnusedNbrs().getLast().getPoint()));
			}
			
		}
		if(ns.getClosingDir() == ClosingDir.nonClosed){
			IntersectionPoint next = null;
			if(previous == null){
//				logger.info("previous == null");
				next = current.getUnusedNbrs().pollLast();
//				logger.info("next = " + Arrays.toString(next.getPoint()));
				next.setPrevious(current);
			} else {
//				logger.info("previous != null");
				next = getNextNbrLocal(previous, current);
//				logger.info("next = " + Arrays.toString(next.getPoint()));
				current.getUnusedNbrs().remove(next);				
				next.setPrevious(current);
			}
			return next;
		} else {
			logger.info("getNextNbrClosedBoundary");
			return getNextNbrClosedBoundary(previous, current);
		}
	}
	
	public IntersectionPoint getNextNbrClosedBoundary(IntersectionPoint previous, IntersectionPoint current){
		IntersectionPoint next = null;
		logger.info("C U R R E N T = " + current);
		if(previous == null){
			logger.info("case 1 previous == null");
			IntersectionPoint nextLocal = current.getUnusedNbrs().pollLast();
			logger.info("next local " + Arrays.toString(nextLocal.getPoint()));
			if(!isClosedBoundaryPoint(nextLocal)){
				logger.info("case 1.1 next local != closed boundary");
				next = nextLocal;
				next.setPrevious(current);
			}
			else{
				logger.info("case 1.2 next local == closed oundary");
				if(!isFaceVertex(nextLocal)){
					logger.info("case 1.2.1. next local != face vertex");
					IntersectionPoint newCurr = getOppositePoint(nextLocal);
					logger.info("(getNextNbrLocal(p.getIntersectionPoint(), nextLocal.getParentHP()" + Arrays.toString(getNextNbrLocal(current, nextLocal).getPoint()));
					IntersectionPoint newPrevious = getOppositePoint(getNextNbrLocal(current, nextLocal));
					logger.info("newPrevious " + Arrays.toString(newPrevious.getPoint()));
					next = getNextNbrLocal(newPrevious, newCurr);
					next.setPrevious(newCurr);
				}
				else{
					logger.info("case 1.2.2 next local == face vertex");
					next = nextLocal;
					next.setPrevious(current);
				}
			}
		}
		else{
			logger.info("case 2 previous != null");
			IntersectionPoint nextLocal = getNextNbrLocal(previous, current);
//			logger.info("next local " + Arrays.toString(nextLocal.getPoint()));
			
			if(!isClosedBoundaryPoint(current)){
				current.getUnusedNbrs().remove(nextLocal);
				logger.info("case 2.1 p != closed boundary");
				if(!isClosedBoundaryPoint(nextLocal)){
					logger.info("2.1.1 next local != closed boundary");
					next = nextLocal;
					next.setPrevious(current);
				}
				else{
					logger.info("case 2.1.2 next local == closed boundary");
					if(!isFaceVertex(nextLocal)){
						logger.info("case 2.1.2.1 next local != face vertex");
						logger.info("next Local = "  + nextLocal);
						IntersectionPoint newCurr = getOppositePoint(nextLocal);
						logger.info("newCurr " + Arrays.toString(newCurr.getPoint()));
						IntersectionPoint newPrevious = getOppositePoint(getNextNbrLocal(current, nextLocal));
						logger.info("newPrevious " + Arrays.toString(newPrevious.getPoint()));
						logger.info("all orinted nbrs of newCurr");
						for (IntersectionPoint ip : newCurr.getNbrs()) {
							logger.info(Arrays.toString(ip.getPoint()));
						}
						next = getNextNbrLocal(newPrevious, newCurr);
//						logger.info("getNextNbrLocal(newPrevious, newP.getParentHP()); " + Arrays.toString(next.getPoint()));
						next.setPrevious(newCurr);
					}
					else{
						logger.info("case 2.1.2.2. next local == face vertex");
						next = nextLocal;
						next.setPrevious(current);
					}
				}
			}
			else{
				logger.info("2.2. p == closed boundary");
				if(!isClosedBoundaryPoint(nextLocal)){
					logger.info("2.2.1 next local != closed boundary");
					next = nextLocal;
					next.setPrevious(current);
				}
				else{
					logger.info("2.2.2. next local == closed boundary");
					IntersectionPoint newCurr = getOppositePoint(current);
					logger.info("newP " + Arrays.toString(newCurr.getPoint()));
					IntersectionPoint newPrevious = getOppositePoint(nextLocal);
					logger.info("newPrevious " + Arrays.toString(newPrevious.getPoint()));
					next = getNextNbrLocal(newPrevious, newCurr);
					next.setPrevious(newCurr);
				}
			}
		}
		logger.info("NEXT = " + Arrays.toString(next.getPoint()));
		return next;
	}
	
	
	/**
	 * 
	 * @param p
	 * @return all vertices of an adjacent face
	 * NOTE: last element of unusedNbrs is already removed
	 */
	public LinkedList<IntersectionPoint>  getAllFaceVertices(IntersectionPoint p){
		LinkedList<IntersectionPoint> allFaceVerts = new LinkedList<IntersectionPoint>();
		IntersectionPoint previous = null;
		allFaceVerts.add(p);
		while(true){
			IntersectionPoint next = getNextNbr(previous, allFaceVerts.getLast());
			previous = next.getPrevious();
			if(next != p){
				allFaceVerts.add(next);
			}
			else{
				break;
			}
		}
		LinkedList<IntersectionPoint> uniqueFaceVerts = new LinkedList<IntersectionPoint>();
		for (IntersectionPoint ip : allFaceVerts) {
			ClosedBoundary cb = ip.getClosedBoundary(ns, dilation);
			if(cb == ClosedBoundary.right || cb == ClosedBoundary.upper){
				uniqueFaceVerts.add(getOppositePoint(ip));
			}
			else{
				uniqueFaceVerts.add(ip);
			}
//			logger.info(Arrays.toString(uniqueFaceVerts.getLast().getPoint()));
		}
		return uniqueFaceVerts;
	}
	

	private IntersectionPoint getOppositePoint(IntersectionPoint ip){
		if(ip.getOpposite() == null){
			if(ip.getClosedBoundary(ns, dilation) == ClosedBoundary.interior){
				throw new IllegalArgumentException("IntersectionPoint " + Arrays.toString(ip.getPoint()) + " does not lie on the closed boundary");
			}
			else if(ip.getClosedBoundary(ns, dilation) == ClosedBoundary.left){
				logger.info("left and rightBound");
				for (IntersectionPoint right : rightBound) {
					logger.info(Arrays.toString(right.getPoint()));
					if(ip.getPoint()[1] == right.getPoint()[1]){
						right.setOpposite(ip);
						ip.setOpposite(right);
						return right;
					}
				}
			}
			else if(ip.getClosedBoundary(ns, dilation) == ClosedBoundary.right){
				logger.info("right and leftBound");
				for (IntersectionPoint left : leftBound) {
					logger.info(Arrays.toString(left.getPoint()));
					if(ip.getPoint()[1] == left.getPoint()[1]){
						left.setOpposite(ip);
						ip.setOpposite(left);
						return left;
					}
				}
			}
			else if(ip.getClosedBoundary(ns, dilation) == ClosedBoundary.upper){
				for (IntersectionPoint lower : lowerBound) {
					if(ip.getPoint()[0] == lower.getPoint()[0]){
						lower.setOpposite(ip);
						ip.setOpposite(lower);
						return lower;
					}
				}
			}
			else if(ip.getClosedBoundary(ns, dilation) == ClosedBoundary.lower){
				for (IntersectionPoint upper : upperBound) {
					if(ip.getPoint()[0] == upper.getPoint()[0]){
						upper.setOpposite(ip);
						ip.setOpposite(upper);
						return upper;
					}
				}
			}
			return null;
		}
		else{
			return ip.getOpposite();
		}
	}
	
	private IntersectionPoint getUniqueClosedBoundryPoint(IntersectionPoint ip){
		IntersectionPoint unique = ip;
		ClosedBoundary cb = ip.getClosedBoundary(ns, dilation);
		if(cb == ClosedBoundary.upper || cb == ClosedBoundary.right){
			unique = getOppositePoint(ip);
		}
		return unique;
	}
	
	
	
	
	private static boolean faceConsistsOnlyOfBoundaryVertices(LinkedList<IntersectionPoint> facePoints, List<Double> boundaryValues){
		 for (IntersectionPoint ip : facePoints) {
			if(!ip.isBoundaryPoint(boundaryValues)){
				if(Math.abs(ip.getPoint()[0] - 20.0) < 1){
					logger.info("boundary values:");
					for (Double value : boundaryValues) {
						logger.info(value.toString());
					}
					logger.info("not a boundary value " + Arrays.toString(ip.getPoint()));
				}
				return false;
			}
		}
		return true;
	}
	
	
	
	private boolean isValidBoundaryFaceVertex(IntersectionPoint ip){
		ClosedBoundary cb = ip.getClosedBoundary(ns, dilation);
		if(isFaceVertex(ip) && (cb == ClosedBoundary.left || cb == ClosedBoundary.lower)){
			return true;
		}
		return false;
	}
	
	private boolean isValidIntersectionPoint(IntersectionPoint ip){
		if(isClosedBoundaryPoint(ip) && !isValidBoundaryFaceVertex(ip)){
			return false;
		}
		return true;
	}
	
	
	public FaceSet createFaceSet(){
		FaceSet fS = new FaceSet();
		List<IntersectionPoint> points = new LinkedList<IntersectionPoint>();
		List<IntersectionPoint> validVerts = new LinkedList<IntersectionPoint>();
		determineLocalNbrs();
		determineOrientedNbrs();
		logger.info("ALL USE#D POINTS");

		logger.info("boundary values " + ns.getBoundaryValuesPastIntersection(dilation));
		logger.info("closind direction " + ns.getClosingDir());
		logger.info("closed boundary values " + Arrays.toString(ns.getClosedBoundaryValuesPastIntersection(dilation)));
		for (IntersectionPoint oriNbr : ipList) {
			if(isValidIntersectionPoint(oriNbr)){
				validVerts.add(oriNbr);
			}
			if(!isClosedBoundaryPoint(oriNbr)){
				points.add(oriNbr);
				logger.info("all starting points " + oriNbr.toString());
			}
		}
		logger.info("intersections size = " + ipList.size());
		double[][] verts = new double[validVerts.size()][2];
		LinkedList<int[]> faceVerts = new LinkedList<int[]>();
		int c = 0;
		for (IntersectionPoint ip: validVerts) {
			verts[c] = ip.getPoint();
			c++;
		}
		for (IntersectionPoint ip: points) {
			LinkedList<IntersectionPoint> unusedNbrs = new LinkedList<IntersectionPoint>();
			for (IntersectionPoint nbr : ip.getNbrs()) {
				unusedNbrs.add(nbr);
			}
			unusedNbrs.pollLast();
			ip.setUnusedNbrs(unusedNbrs);
		}
		fS.setVerts(verts);
		int faceIndexTest = 0;
		logger.info("All faces");
		Set<Double> boundaryValues = new HashSet<Double>();
		boundaryValues = ns.getBoundaryValuesPastIntersection(dilation);
		List<Double> boundValues = new LinkedList<Double>();
		for (Double d : boundaryValues) {
			boundValues.add(d);
		}
		for (IntersectionPoint ip : points) {
			while(!ip.getUnusedNbrs().isEmpty()){
				if(!isClosedBoundaryPoint(ip)){
					
					LinkedList<IntersectionPoint> facePoints = getAllFaceVertices(ip);
					logger.info("Face index = " + faceIndexTest);
					faceIndexTest ++;
					for (IntersectionPoint iP : facePoints) {
						logger.info(Arrays.toString(iP.getPoint()));
					}
					if(!faceConsistsOnlyOfBoundaryVertices(facePoints, boundValues)){
						LinkedList<Integer> ind = new LinkedList<Integer>();
						for (IntersectionPoint fP : facePoints) {
							if(fP.getClosedBoundary(ns, dilation) != ClosedBoundary.interior){
								logger.info("CREATE FACE SET: check unique boundary");
								logger.info("point before " + Arrays.toString(fP.getPoint()));
								fP = getUniqueClosedBoundryPoint(fP);
								logger.info("point past " + Arrays.toString(fP.getPoint()));
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
						logger.info("    null    FACE");
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
		logger.info("Faces in generateFaceSet");
		for (int i = 0; i < fS.getFaces().length; i++) {
			logger.info("face" + i + ". " + Arrays.toString(fS.getFaces()[i]));
		}
		return fS;
	}
	


}
