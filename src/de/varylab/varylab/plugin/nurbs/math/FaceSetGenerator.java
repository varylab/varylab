package de.varylab.varylab.plugin.nurbs.math;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import de.jreality.math.Rn;
import de.varylab.varylab.plugin.nurbs.IntersectionPointDistanceComparator;
import de.varylab.varylab.plugin.nurbs.IntersectionPointIndexComparator;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.NURBSSurface.ClosingDir;
import de.varylab.varylab.plugin.nurbs.data.FaceSet;
import de.varylab.varylab.plugin.nurbs.data.IndexedCurveList;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint.GluedBoundary;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint.FaceVertex;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;

public class FaceSetGenerator {
	
	private static Logger logger = Logger.getLogger(FaceSetGenerator.class.getName());

	private LinkedList<IntersectionPoint> leftBound = new LinkedList<IntersectionPoint>();
	private LinkedList<IntersectionPoint> rightBound = new LinkedList<IntersectionPoint>();
	private LinkedList<IntersectionPoint> upperBound = new LinkedList<IntersectionPoint>();
	private LinkedList<IntersectionPoint> lowerBound = new LinkedList<IntersectionPoint>();
	private NURBSSurface ns;
	LinkedList<IntersectionPoint> ipList;
	List<double[]> boundaryVerts;
	LinkedList<IndexedCurveList> curves = null;

	

	
	public FaceSetGenerator(NURBSSurface surface, LinkedList<IntersectionPoint> ipl) {
		ipList = ipl;
		ns = surface;
		for (IntersectionPoint ip : ipList) {
			GluedBoundary cb = ip.getGluedBoundary(surface);
			if(cb == GluedBoundary.left){
				leftBound.add(ip);
			}
			else if(cb == GluedBoundary.right){
				rightBound.add(ip);
			}
			else if(cb == GluedBoundary.upper){
				upperBound.add(ip);
			}
			else if(cb == GluedBoundary.lower){
				lowerBound.add(ip);
			}
		}
		boundaryVerts = surface.getBoundaryVerticesUV();
	}
	
	private IndexedCurveList getListFromIndex(int index){
		for (IndexedCurveList icl : curves) {
			if(icl.getIndex() == index){
				return icl;
			}
		}
		return null;
	}
	
	
	public LinkedList<IntersectionPoint> getPrevNext(IntersectionPoint p, int curveIndex){
		LinkedList<IntersectionPoint> pointList = getListFromIndex(curveIndex).getCurveList();
		IntersectionPoint prev = pointList.getFirst();
		IntersectionPoint next = new IntersectionPoint();
		LinkedList<IntersectionPoint> prevNext = new LinkedList<>();
		IntersectionPoint curr = pointList.getFirst();
		boolean cyclic = getCyclicFromCurveIndexAndIntersectionPoint(curveIndex, p);
		for (int i = 0; i < pointList.size(); i++) {
			curr = pointList.get(i);
			if(curr == p){
				if(i != 0){
					prev = pointList.get(i - 1);
					if(pointsHaveGivenCurveIndexAndLieInSameDomain(curr, prev, curveIndex)){
						prevNext.add(prev);
					}
				}
				else if(i == 0 && cyclic){
					prev = pointList.getLast();
					prevNext.add(prev);
				}
				if(i < pointList.size() - 1){
					next = pointList.get(i + 1);
					if(pointsHaveGivenCurveIndexAndLieInSameDomain(curr, next, curveIndex)){
						prevNext.add(next);
					}
				}
				else if((i == pointList.size() - 1) && cyclic){
					next = pointList.getFirst();
					prevNext.add(next);
				}
				return prevNext;
			}
		}
		return null;
	}
	
	private boolean isBoundaryPoint(IntersectionPoint ip){
		double[] U = ns.getUKnotVector();
		double[] V = ns.getVKnotVector();
		double[] point = ip.getPoint();
		ClosingDir cd = ns.getClosingDir();
		if(cd == ClosingDir.nonClosed){
			if(point[0] == U[0] || point[0] == U[U.length - 1] || point[1] == V[0] || point[1] == V[V.length - 1]){
				return true;
			}
			else return false;
		}
		else if(cd == ClosingDir.uClosed){
			if(point[1] == V[0] || point[1] == V[V.length - 1]){
				return true;
			}
			else return false;
			
		}
		else if(cd == ClosingDir.vClosed){
			if(point[0] == U[0] || point[0] == U[U.length - 1]){
				return true;
			}
			else return false;
		}
		else return false;
	}
	
	
	private void determineCurves(){
		curves = new LinkedList<>();
		LinkedList<Integer> indexList = new LinkedList<>();
		for (IntersectionPoint ip : ipList) {
			if(isBoundaryPoint(ip)){
				ip.setBoundaryPoint(true);
			}
			else{
				ip.setBoundaryPoint(false);
			}
			if(liesOnGluedBoundary(ip)){
				ip.setOpposite(getOppositePoint(ip));
			}
		
			for (LineSegment ls : ip.getIntersectingSegments()) {
				if(!indexList.contains(ls.getCurveIndex())){
					indexList.add(ls.getCurveIndex());
				}
			}
		}
		
		for (Integer curveIndex : indexList) {
			LinkedList<IntersectionPoint> points = new LinkedList<>();
			IndexedCurveList icl = new IndexedCurveList(curveIndex, points);
			for (IntersectionPoint ip : ipList) {
				if(ip.containsIndex(curveIndex)){
					points.add(ip);
				}
			}
			IntersectionPointIndexComparator ipic = new IntersectionPointIndexComparator();
			ipic.curveIndex = icl.getIndex();
			Collections.sort(icl.getCurveList(), ipic);
			
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
			icl.getCurveList().clear();
			for (LinkedList<IntersectionPoint> sameList : indexOrderList) {
				if(sameList.size() > 1){
					sortSameIndex(sameList, icl.getIndex(), getIndexOnCurveFromCurveIndexAndIntersectionPoint(icl.getIndex(), sameList.getFirst()));
				}
				icl.getCurveList().addAll(sameList);
			}
			
			curves.add(icl);
		}
	}
	
	

	public void determineOrientedNbrs(){
		logger.info("all points");
		for (IntersectionPoint ip: ipList) {
			logger.info(Arrays.toString(ip.getPoint()));
			if(ip.getGluedBoundary() == GluedBoundary.upper){
				logger.info("upper");
			}
			else if(ip.getGluedBoundary() == GluedBoundary.lower){
				logger.info("lower");
			}
			else{
				logger.info("interrior");
			}
		}
		logger.info("ORIENTED NBRS");
		for (IntersectionPoint ip : ipList) {
			logger.info("the point = " + Arrays.toString(ip.getPoint()));
			ip.makeOrientedNbrs();
			for (IntersectionPoint nbr : ip.getNbrs()) {
				logger.info(Arrays.toString(nbr.getPoint()));
			}
		}
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
	
	
	private boolean pointsHaveGivenCurveIndexAndLieInSameDomain(IntersectionPoint ip1, IntersectionPoint ip2, int curveIndex){
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
		for (IntersectionPoint ip : ipList) {
			LinkedList<IntersectionPoint> nbrs = new LinkedList<>();
			LinkedList<Integer> indexList = ip.getIndexList();
			for (Integer index : indexList) {
				nbrs.addAll(getPrevNext(ip, index));
			}
			ip.setNbrs(nbrs);
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
	
	private boolean liesOnGluedBoundary(IntersectionPoint ip){		
		if(ip.getGluedBoundary(ns) == null){
			System.err.println("NULLLLL");
		}
		if(ip.getGluedBoundary(ns) == GluedBoundary.interior){
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
		if(ns.getClosingDir() == ClosingDir.nonClosed){
			IntersectionPoint next = null;
			if(previous == null){
				logger.info("previous == null");
				next = current.getUnusedNbrs().pollLast();
				logger.info("next = " + Arrays.toString(next.getPoint()));
				next.setPrevious(current);
			} else {
				logger.info("previous != null");
				next = getNextNbrLocal(previous, current);
				logger.info("next = " + Arrays.toString(next.getPoint()));
				current.getUnusedNbrs().remove(next);				
				next.setPrevious(current);
			}
			return next;
		} else {
			return getNextNbrGluedBoundary(previous, current);
		}
	}
	
	
	
	public IntersectionPoint getNextNbrGluedBoundary(IntersectionPoint previous, IntersectionPoint curr){
		IntersectionPoint next = null;
		logger.info("C U R R E N T = " + curr);
		if(previous == null){
			logger.info("case 1 previous == null");
			IntersectionPoint nextLocal = curr.getUnusedNbrs().pollLast();
			logger.info("next local " + Arrays.toString(nextLocal.getPoint()));
			if(!liesOnGluedBoundary(nextLocal)){
				logger.info("case 1.1 next local != glued boundary");
				next = nextLocal;
				next.setPrevious(curr);
			}
			else{
				next = handleNextLocalLiesOnGluedBoundary(curr, nextLocal);
			}
		}
		else{
			logger.info("case 2 previous != null");
			IntersectionPoint nextLocal = getNextNbrLocal(previous, curr);
			if(!liesOnGluedBoundary(curr)){
				curr.getUnusedNbrs().remove(nextLocal);
				logger.info("case 2.1 curr != glued boundary");
				if(!liesOnGluedBoundary(nextLocal)){
					logger.info("2.1.1 next local != glued boundary");
					next = nextLocal;
					next.setPrevious(curr);
				}
				else{
					logger.info("case 2.1.2 next local == glued boundary");
					next = handleNextLocalLiesOnGluedBoundary(curr, nextLocal);
				}
			}
			else{
				
				logger.info("2.2. curr == glued boundary AND curr is a face verte3x");
				if(!liesOnGluedBoundary(nextLocal)){
					logger.info("2.2.1 next local != glued boundary");
					next = nextLocal;
					next.setPrevious(curr);
				}
				else{
					logger.info("2.2.2. next local == glued boundary");
					IntersectionPoint newCurr = curr.getOpposite();
					logger.info("newP " + Arrays.toString(newCurr.getPoint()));
					IntersectionPoint newPrevious = nextLocal.getOpposite();
					logger.info("newPrevious " + Arrays.toString(newPrevious.getPoint()));
					next = getNextNbrLocal(newPrevious, newCurr);
					next.setPrevious(newCurr);
				}
			}
		}
		logger.info("NEXT = " + Arrays.toString(next.getPoint()));
		return next;
	}
	
	private IntersectionPoint handleNextLocalLiesOnGluedBoundary(IntersectionPoint curr, IntersectionPoint nextLocal){
		IntersectionPoint next = new IntersectionPoint();
		if(!isFaceVertex(nextLocal)){
			logger.info("case 1 next local != face vertex");
			if(!isGluedBigonPoint(nextLocal)){
				logger.info("case 1.1 next local != glued bigon point");
				logger.info("next Local = "  + nextLocal);
				IntersectionPoint newCurr = nextLocal.getOpposite();
				logger.info("newCurr " + Arrays.toString(newCurr.getPoint()));
				IntersectionPoint newPrevious = getNextNbrLocal(curr, nextLocal).getOpposite();
				logger.info("newPrevious " + Arrays.toString(newPrevious.getPoint()));
				next = getNextNbrLocal(newPrevious, newCurr);
				next.setPrevious(newCurr);
			}
			else{
				logger.info("case 1.2 next local == glued bigon point");
				System.err.println("case 2.1.2.1.2 next local == glued bigon point");
				IntersectionPoint newPrev = nextLocal;
				logger.info("next local = " + Arrays.toString(nextLocal.getPoint()));
				IntersectionPoint nextLocalBigon = getNextLocalBigonPoint(nextLocal.getOpposite()).getOpposite();
				logger.info("nextLocalBigon = " + Arrays.toString(nextLocalBigon.getPoint()));
				if(nextLocalBigon == getNextNbrLocal(curr, nextLocal)){
					next = getNextNbrLocal(newPrev, nextLocalBigon);
				} 
				else{
					next = getNextNbrLocal(newPrev, curr);
				}	
				next.setPrevious(nextLocalBigon);	
			}
		}
		else{
			logger.info("case 2. next local == face vertex");
			next = nextLocal;
			next.setPrevious(curr);
		}
	
		return next;
	}

	
	private IntersectionPoint getNextLocalBigonPoint(IntersectionPoint ip){
		for (IntersectionPoint nbr : ip.getNbrs()) {
			if(pointsAgreeInBothCurveIndices(ip, nbr)){
				System.err.println("agree");
				return nbr;
			}
		}
		return null;
	}
	
	private boolean pointsAgreeInBothCurveIndices(IntersectionPoint ip1, IntersectionPoint ip2){
		logger.info("ip1 = " + Arrays.toString(ip1.getPoint()));
		logger.info("ip2 = " + Arrays.toString(ip2.getPoint()));
		List<Integer> indexList1 = ip1.getIndexList();
		List<Integer> indexList2 = ip2.getIndexList();
		int index10 = indexList1.get(0);
		int index11 = indexList1.get(1);
		int index20 = indexList2.get(0);
		int index21 = indexList2.get(1);
		if((index10 == index20 || index10 == index21) && (index11 == index20 || index11 == index21)){
			return true;
		}
		return false;
	}
	
	
	private boolean isGluedBigonPoint(IntersectionPoint ip){
		System.err.println("isGluedBigonPoint(IntersectionPoint ip)");
		System.err.println("ip = " + Arrays.toString(ip.getPoint()) + "boundary point = " + ip.isBoundaryPoint());
		for (IntersectionPoint nbr : ip.getNbrs()) {
			System.out.println(Arrays.toString(nbr.getPoint()));
		}
		System.err.println("#nbrs = " + ip.getNbrs().size());
		if(ip.getOpposite() == null){
			System.err.println("opposite == null " + Arrays.toString(ip.getPoint()));
		}
		if(liesOnGluedBoundary(ip) && !ip.isBoundaryPoint() && ip.getOpposite().getNbrs().size() == 3){
			return true;
		}
		return false;
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
		IntersectionPoint next = new IntersectionPoint();
		do{
			next = getNextNbr(previous, allFaceVerts.getLast());
			previous = next.getPrevious();
			allFaceVerts.add(next);
		} while(getNextNbr(previous, allFaceVerts.getLast()) != p);
		
		LinkedList<IntersectionPoint> uniqueFaceVerts = new LinkedList<IntersectionPoint>();
		for (IntersectionPoint ip : allFaceVerts) {
			GluedBoundary cb = ip.getGluedBoundary(ns);
			if(cb == GluedBoundary.right || cb == GluedBoundary.upper){
				uniqueFaceVerts.add(getOppositePoint(ip));
			}
			else{
				uniqueFaceVerts.add(ip);
			}
		}
		return uniqueFaceVerts;
	}
	

	private IntersectionPoint getOppositePoint(IntersectionPoint ip){
		if(ip.getOpposite() == null){
			if(ip.getGluedBoundary(ns) == GluedBoundary.interior){
				throw new IllegalArgumentException("IntersectionPoint " + Arrays.toString(ip.getPoint()) + " does not lie on the glued boundary");
			}
			else if(ip.getGluedBoundary(ns) == GluedBoundary.left){
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
			else if(ip.getGluedBoundary(ns) == GluedBoundary.right){
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
			else if(ip.getGluedBoundary(ns) == GluedBoundary.upper){
				for (IntersectionPoint lower : lowerBound) {
					if(ip.getPoint()[0] == lower.getPoint()[0]){
						lower.setOpposite(ip);
						ip.setOpposite(lower);
						return lower;
					}
				}
			}
			else if(ip.getGluedBoundary(ns) == GluedBoundary.lower){
				for (IntersectionPoint upper : upperBound) {
					System.err.println("upperBound " + Arrays.toString(upper.getPoint()));
					if(ip.getPoint()[0] == upper.getPoint()[0]){
						upper.setOpposite(ip);
						ip.setOpposite(upper);
						return upper;
					}
				}
			}
			System.err.println("NNNUUULLLLL");
			return null;
		}
		else{
			return ip.getOpposite();
		}
	}
	
	private IntersectionPoint getUniqueClosedBoundryPoint(IntersectionPoint ip){
		IntersectionPoint unique = ip;
		GluedBoundary cb = ip.getGluedBoundary(ns);
		if(cb == GluedBoundary.upper || cb == GluedBoundary.right){
			unique = getOppositePoint(ip);
		}
		return unique;
	}
	
	
	
	
	private static boolean faceConsistsOnlyOfBoundaryVertices(LinkedList<IntersectionPoint> facePoints, List<Double> boundaryValues){
		 for (IntersectionPoint ip : facePoints) {
			if(!ip.isBoundaryPoint()){
				return false;
			}
		}
		return true;
	}
	
	
	
	private boolean isValidBoundaryFaceVertex(IntersectionPoint ip){
		GluedBoundary cb = ip.getGluedBoundary(ns);
		if(isFaceVertex(ip) && (cb == GluedBoundary.left || cb == GluedBoundary.lower)){
			return true;
		}
		return false;
	}
	
	private boolean isValidIntersectionPoint(IntersectionPoint ip){
		if(liesOnGluedBoundary(ip) && !isValidBoundaryFaceVertex(ip)){
			return false;
		}
		return true;
	}
	
	
	public FaceSet createFaceSet(){
		FaceSet fS = new FaceSet();
		List<IntersectionPoint> points = new LinkedList<IntersectionPoint>();
		List<IntersectionPoint> validVerts = new LinkedList<IntersectionPoint>();
		determineCurves();
		determineLocalNbrs();
		determineOrientedNbrs();
		logger.info("ALL USE#D POINTS");
		logger.info("boundary values " + ns.getBoundaryValues());
		logger.info("closind direction " + ns.getClosingDir());
		logger.info("closed boundary values " + Arrays.toString(ns.getGluedBoundaryValues()));
		for (IntersectionPoint oriNbr : ipList) {
			if(isValidIntersectionPoint(oriNbr)){
				validVerts.add(oriNbr);
			}
			if(!liesOnGluedBoundary(oriNbr)){
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
		
		List<Double> boundValues = ns.getBoundaryValues();
		for (IntersectionPoint ip : points) {
			while(!ip.getUnusedNbrs().isEmpty()){
				if(!liesOnGluedBoundary(ip)){
					
					LinkedList<IntersectionPoint> facePoints = getAllFaceVertices(ip);
					logger.info("Face index = " + faceIndexTest);
					faceIndexTest ++;
					for (IntersectionPoint iP : facePoints) {
						logger.info(Arrays.toString(iP.getPoint()));
					}
					if(!faceConsistsOnlyOfBoundaryVertices(facePoints, boundValues)){
						LinkedList<Integer> ind = new LinkedList<Integer>();
						for (IntersectionPoint fP : facePoints) {
							if(fP.getGluedBoundary(ns) != GluedBoundary.interior){
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
						// do not add bigons
						if(ind.size() > 2){
							faceVerts.add(index);
						}
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
	
	public FaceSet createFaceSetCombinatorialTest(){
		FaceSet fS = new FaceSet();
		List<IntersectionPoint> points = new LinkedList<IntersectionPoint>();
		List<IntersectionPoint> validVerts = new LinkedList<IntersectionPoint>();
		determineCurves();
		logger.info("ALL USE#D POINTS");

		logger.info("boundary values " + ns.getBoundaryValues());
		logger.info("closind direction " + ns.getClosingDir());
		logger.info("closed boundary values " + Arrays.toString(ns.getGluedBoundaryValues()));
		for (IntersectionPoint oriNbr : ipList) {
			if(isValidIntersectionPoint(oriNbr)){
				validVerts.add(oriNbr);
			}
			if(!liesOnGluedBoundary(oriNbr)){
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
		List<Double> boundValues = ns.getBoundaryValues();
		for (IntersectionPoint ip : points) {
			while(!ip.getUnusedNbrs().isEmpty()){
				if(!liesOnGluedBoundary(ip)){
					
					LinkedList<IntersectionPoint> facePoints = getAllFaceVertices(ip);
					logger.info("Face index = " + faceIndexTest);
					faceIndexTest ++;
					for (IntersectionPoint iP : facePoints) {
						logger.info(Arrays.toString(iP.getPoint()));
					}
					if(!faceConsistsOnlyOfBoundaryVertices(facePoints, boundValues)){
						LinkedList<Integer> ind = new LinkedList<Integer>();
						for (IntersectionPoint fP : facePoints) {
							if(fP.getGluedBoundary(ns) != GluedBoundary.interior){
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
						// do not add bigons
						if(ind.size() > 2){
							faceVerts.add(index);
						}
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
