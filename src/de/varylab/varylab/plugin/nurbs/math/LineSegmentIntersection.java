package de.varylab.varylab.plugin.nurbs.math;
//import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import compgeom.RLineSegment2D;
import compgeom.RPoint2D;
import compgeom.Rational;
import compgeom.algorithms.BentleyOttmann;
import de.varylab.varylab.plugin.nurbs.data.IntersectionPoint;
import de.varylab.varylab.plugin.nurbs.data.LineSegment;
import de.varylab.varylab.plugin.nurbs.data.Partition;
import de.varylab.varylab.plugin.nurbs.type.PartitionComparator;

public class LineSegmentIntersection {
		
	private static Logger logger = Logger.getLogger(LineSegmentIntersection.class.getName());


		public static LinkedList<LineSegment> preSelection(double[] U, double[] V, LinkedList<LineSegment> segList){
			double u0 = U[0];
			double u1 = U[U.length - 1];
			double v0 = V[0];
			double v1 = V[V.length - 1];
			int curves = 1000;
			double uFactor = curves / (u1 - u0);
			double vFactor = curves / (v1 - v0);
			
			Partition[][] partition = new Partition[curves + 1][curves + 1];
			logger.info("Start ini");
			double startIni = System.currentTimeMillis();
			for (int i = 0; i < partition.length; i++) {
				for (int j = 0; j < partition.length; j++) {
					partition[i][j] = new Partition();
				}
			}
			double endIni = System.currentTimeMillis();
			logger.info("time for initializing: " + (startIni - endIni));
			
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
			logger.info("END");
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

		logger.info("anfanglaenge: " + segList.size());
		logger.info("endlaenge: " + finalSegmentTree.size());
		LinkedList<LineSegment> finalSegmentList = new LinkedList<LineSegment>();
		logger.info("all selected segemnts");
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
		
		DecimalFormat df = new DecimalFormat();
	    df.setMaximumFractionDigits(99);
	    df.setMinimumFractionDigits(0);
	    df.setGroupingUsed(false);
	    
		int segmentCounter = 0;
		for (LineSegment ls : segments) {	
			segmentCounter++;
			Rational r1X = new Rational(df.format(ls.getSegment()[0][0]));
			Rational r1Y = new Rational(df.format(ls.getSegment()[0][1]));
			Rational r2X = new Rational(df.format(ls.getSegment()[1][0]));
			Rational r2Y = new Rational(df.format(ls.getSegment()[1][1]));
			RPoint2D p1 = new RPoint2D(r1X, r1Y);
			RPoint2D p2 = new RPoint2D(r2X, r2Y);				
			RLineSegment2D rSeg = new RLineSegment2D(p1, p2, ls.getCurveIndex(), ls.getIndexOnCurve());
			inverseMap.put(rSeg, ls);
			RSegments.add(rSeg);
		}
		logger.info("# segments = " + segmentCounter);
		logger.info("START TO COMPUTE INTERSECTIONS");
		LinkedList<IntersectionPoint> intersectionPoints = new LinkedList<IntersectionPoint>();
		Map<RPoint2D, Set<RLineSegment2D>> intersections = BentleyOttmann.intersectionsMap(RSegments);
		for(RPoint2D point : intersections.keySet()){
			LinkedList<LineSegment> segList = new LinkedList<LineSegment>();
			for (RLineSegment2D lS2D : intersections.get(point)) {
				segList.add(inverseMap.get(lS2D));
			}
			IntersectionPoint ip = new IntersectionPoint();
			double x = point.x.doubleValue();
			double y = point.y.doubleValue();
			ip.setPoint(new double[]{x,y});
			ip.setIntersectingSegments(segList);
			double[] result = ip.getPoint();
			logger.info("double[] result = ip.getPoint();" + Arrays.toString(result));
			
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

}
