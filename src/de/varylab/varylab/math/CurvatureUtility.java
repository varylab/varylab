package de.varylab.varylab.math;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import geom3d.Basis;
import geom3d.Plane;
import geom3d.Point;
import geom3d.Quad;
import geom3d.Triangle;
import geom3d.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.EVD;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.varylab.discreteconformal.heds.bsp.KdTree;
import de.varylab.discreteconformal.heds.bsp.KdTree.KdPosition;
import de.varylab.discreteconformal.heds.bsp.KdUtility;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;

public class CurvatureUtility {

	
	/**
	 * Maps the curvature direction basis K1, K2, N to the texture domain
	 * as suggested by Alliez et al.
	 * TODO: untested
	 * @param B
	 * @param v
	 * @return
	 */
	public Basis toConformalDomain(Basis B, VVertex v) {
		Plane P = new Plane(B.getZ(), new Point());
		VVertex v2 = v.getIncomingEdge().getStartVertex();
		Vector eVec = new Vector(v2.getPosition());
		eVec.subtract(v.getPosition());
		Point eVecProjected = P.projectOnto(eVec.asPoint());
		double angleK1 = eVecProjected.getAngle(B.getX());
		double detK1 = new Basis(B.getX(), eVecProjected, B.getZ()).getDeterminant();
		angleK1 *= detK1 > 0 ? 1 : -1;
		double angleK2 = eVecProjected.getAngle(B.getY());
		double detK2 = new Basis(eVecProjected, B.getY(), B.getZ()).getDeterminant();
		angleK2 *= detK2 > 0 ? 1 : -1;
		
		Vector etVec = new Vector(v2.getTexCoord());
		etVec.subtract(v.getTexCoord());
		etVec.normalize();
		
		Vector r1 = new Vector(
			cos(angleK1) * etVec.x() - sin(angleK1) * etVec.y(), 
			sin(angleK1) * etVec.x() + cos(angleK1) * etVec.y(), 0);

		Vector r2 = new Vector(
			cos(angleK2) * etVec.x() - sin(angleK2) * etVec.y(), 
			sin(angleK2) * etVec.x() + cos(angleK2) * etVec.y(), 0);
		
		Vector r3 = new Vector(0, 0, 1);
		
		Basis R = new Basis(r1, r2, r3);
		return R;
	}
	
	
	
	public static double[] getMinMaxCurvatureAt(
		Point p,
		double scale,
		KdTree<VVertex> kd
	) {
		EVD evd = getTensorInformation(p, scale, kd);
		double[] eigVal= evd.getRealEigenvalues();
		LinkedList<Double> minMax = new LinkedList<Double>();
		minMax.add(eigVal[0]);
		minMax.add(eigVal[1]);
		minMax.add(eigVal[2]);
		int index = getIndexOfMinMagnitude(eigVal);
		minMax.remove(index);
		if(minMax.get(1)<minMax.get(0))
			minMax.addFirst(minMax.removeLast());
		eigVal[0]=minMax.get(0);
		eigVal[1]=minMax.get(1);
		return eigVal;
	}
	
	public static EVD getTensorInformation(
			Point p,
			double scale,
			KdTree<VVertex> kd
	) {
		KdPosition position = new KdTree.KdPosition(p);
		Collection<VFace> faces = KdUtility.collectFacesInRadius(kd, position, scale);
		Collection<VEdge> edges = KdUtility.collectEdgesInRadius(kd, position, scale);
		double area=0;
		for(VFace f :faces){
			area += toTriangle(f).computeArea();
		}
		DenseMatrix matrix = new DenseMatrix(3,3);
		DenseMatrix tmp = new DenseMatrix(3,3);
		double beta = 0;
		double edgeLength = 0;
		
		for(VEdge e : edges){
			beta = getAngle(e);
			edgeLength = getLength(e);
			
			Vector edge = getVector(e);
			edge.normalize();
			DenseMatrix  result = new DenseMatrix(3,3);
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					result.set(i, j, edge.get()[i]*edge.get()[j]);
				}
			}
			tmp = getEdgeCurvatureTensor(e);
			matrix.add(beta*edgeLength,tmp);
		}
		matrix.scale(1/area);
		EVD evd = null;
		try {
			evd = EVD.factorize(matrix);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return evd;
	}
	
	public static int getIndexOfMinMagnitude(double[] v){
		int r = 0;
		double val = abs(v[0]);
		for (int i = 1; i < v.length; i++){
			double tmp = abs(v[i]); 
			if (tmp < val) {
				r = i;
				val = tmp;
			}
		}
		return r;
	}
	
	public static Vector[] getSortedEigenVectors(EVD evd){
		Vector[] r = new Vector[]{new Vector(),new Vector(), new Vector()};
		double[] eigVal = evd.getRealEigenvalues();
		DenseMatrix eigVec = evd.getRightEigenvectors();
		double[][] eigVecArr = new double[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				eigVecArr[i][j]= eigVec.get(j,i);
			}
		}
		//get minimal magnitude
		int i3 = getIndexOfMinMagnitude(eigVal);
		int i1 = (i3 + 1) % 3;
		int i2 = (i3 + 2) % 3;
		double k1 = eigVal[i1];
		double k2 = eigVal[i2];
		r[0] = new Vector(k1 < k2 ? eigVecArr[i1] : eigVecArr[i2]);
		r[1] = new Vector(k1 < k2 ? eigVecArr[i2] : eigVecArr[i1]);
		r[2] = new Vector(eigVecArr[i3]);
		
		return r;
	}


	public static Basis getTensor(
			Point p,
			double scale,
			KdTree<VVertex> kd
		 ){
			KdPosition position = new KdPosition(p);
			Collection<VFace> faces = KdUtility.collectFacesInRadius(kd, position, scale);
			Collection<VEdge> edges = KdUtility.collectEdgesInRadius(kd, position, scale);
			double area=0;
			for(VFace f :faces){
				area += toTriangle(f).computeArea();
			}
			DenseMatrix matrix = new DenseMatrix(3,3);
			DenseMatrix tmp = new DenseMatrix(3,3);
			double beta = 0;
			double edgeLength = 0;
			
			for(VEdge e :edges){
				beta = getAngle(e);
				edgeLength = getLength(e);
				tmp = getEdgeCurvatureTensor(e);
				matrix.add(beta*edgeLength,tmp);
			}
			matrix.scale(1/area);
			Vector c1 = new Vector(matrix.getData()[0],matrix.getData()[1],matrix.getData()[2]);
			Vector c2 = new Vector(matrix.getData()[3],matrix.getData()[4],matrix.getData()[5]);
			Vector c3 = new Vector(matrix.getData()[6],matrix.getData()[7],matrix.getData()[8]);
			return new Basis(c1,c2,c3);
	}
	
	public static double meanEdgeLength(VHDS mesh) {
		double result = 0.0;
		for (VEdge e : mesh.getEdges()) {
			double[] s = e.getStartVertex().position;
			double[] t = e.getTargetVertex().position;
			result += Rn.euclideanDistance(s, t);
		}
		return result / mesh.numEdges() / 2;
	}
	
	public static double absoluteCurvatureAt(
			Point p,
			double scale,
			KdTree<VVertex> kd
	){
		KdPosition position = new KdPosition(p);
		Collection<VEdge> edges = KdUtility.collectEdgesInRadius(kd, position, scale);
		Collection<VFace> faces = incidentFaces(edges);
		double area=0;
		for(VFace f :faces)
			area += toTriangle(f).computeArea();
		DenseMatrix matrix = new DenseMatrix(3,3);
		DenseMatrix tmp = new DenseMatrix(3,3);
		double beta = 0;
		double edgeLength = 0;
		
		for(VEdge e :edges){
			beta = getAngle(e);
			edgeLength = getLength(e);
			tmp = getEdgeCurvatureTensor(e);
			matrix.add(beta*edgeLength,tmp);
		}
		matrix.scale(1/area);
		return getColumnsLength(matrix);
	}


	private static double getColumnsLength(DenseMatrix matrix) {
		Vector c1 = new Vector(matrix.getData()[0],matrix.getData()[1],matrix.getData()[2]);
		Vector c2 = new Vector(matrix.getData()[3],matrix.getData()[4],matrix.getData()[5]);
		return (abs(c1.getLength())+abs(c2.getLength()))/2.0;
	}

	private static DenseMatrix getEdgeCurvatureTensor(VEdge e) {
		Vector edge = getVector(e);
		edge.normalize();
		DenseMatrix  result = new DenseMatrix(3,3);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				result.set(i, j, edge.get()[i]*edge.get()[j]);
			}
		}
		return result;
	}
	
	public static Collection<VFace> incidentFaces(Collection<VEdge> edges) {
		HashSet<VFace> faces = new HashSet<VFace>(edges.size() / 3);
		for (VEdge e : edges) {
			if (e.getLeftFace() != null)
				faces.add(e.getLeftFace());
		}
		return new LinkedList<VFace>(faces);
	}
	
	public static double getSpacingDistance(double k, double eps){
		return 2*Math.sqrt(eps*(2/abs(k)-eps));
	}
	
	public static double getSpacingDistance(double k){
		double eps = 0.008;
		return 2*Math.sqrt(eps*(2/abs(k)-eps));
	}
	
	public static Quad createQuad(Basis basis, Point center,double kMin,double kMax, double scale ){
		Quad quad = new Quad();
		double distMin = getSpacingDistance(kMin);
		Vector dMin = new Vector(basis.getX()).scaleTo(distMin);
		
		double distMax = getSpacingDistance(kMax);
		Vector dMax = new Vector(basis.getY()).scaleTo(distMax);
		if(distMin>scale*20)
			dMin = new Vector(basis.getX()).scaleTo(scale*50);
		
		if (distMax>scale*20)
			dMax = new Vector(basis.getY()).scaleTo(scale*50);
		
		Point min1 = new Point(center).add(dMin).asPoint();
		
		quad.setA( new Point(min1).add(dMax).asPoint());
		quad.setB( new Point(min1).add(dMax.times(-1)).asPoint());
		Point min2 = new Point(center).add(dMin.times(-1)).asPoint();
		quad.setC( new Point(min2).add(dMax).asPoint());
		quad.setD(new Point(min2).add(dMax.times(-1)).asPoint());

		return quad;
	}
	public static Quad createQuad(Basis basis, Point center,double kMin,double kMax,double scale, double maxScale ){
		Quad quad = new Quad();
		
		double distMin = getSpacingDistance(kMin, scale);
		distMin = min(distMin, maxScale);
		Vector dMin = new Vector(basis.getX()).scaleTo(distMin);
		
		double distMax = getSpacingDistance(kMax, scale);
		distMax = min(distMax, maxScale);
		Vector dMax = new Vector(basis.getY()).scaleTo(distMax);
		
		Point min1 = new Point(center).add(dMin).asPoint();
		
		quad.setA( new Point(min1).add(dMax).asPoint());
		quad.setB( new Point(min1).add(dMax.times(-1)).asPoint());
		Point min2 = new Point(center).add(dMin.times(-1)).asPoint();
		quad.setC( new Point(min2).add(dMax).asPoint());
		quad.setD(new Point(min2).add(dMax.times(-1)).asPoint());

		return quad;
	}

	
    public static Triangle toTriangle(VFace f) {
    	List<VEdge> b = HalfEdgeUtils.boundaryEdges(f);
    	if (b.size() != 3) {
    		throw new RuntimeException("No triangle in toTriangle()");
    	}
    	Point p1 = new Point(b.get(0).getTargetVertex().position);
    	Point p2 = new Point(b.get(1).getTargetVertex().position);
    	Point p3 = new Point(b.get(2).getTargetVertex().position);
    	return new Triangle(p1, p2, p3);
    }
	
    
    public static Vector getNormal(VFace f) {
		List<VEdge> bList = HalfEdgeUtils.boundaryEdges(f);
    	Point a = new Point(bList.get(0).getTargetVertex().position);
    	Point b = new Point(bList.get(1).getTargetVertex().position);
    	Point c = new Point(bList.get(2).getTargetVertex().position);
		Vector v1 = a.vectorTo(b);
		Vector v2 = a.vectorTo(c);
    	return v1.getNormal(v2);
    }
    
    
    
    public static double getAngle(VEdge e) {
		return signedAngle(e);
    }
    
    
	public static double signedAngle(VEdge e) {
		VFace lf = e.getLeftFace();
		VFace rf = e.getRightFace();
		if (lf == null || rf == null) {
			return 0;
		}
		return curvatureSign(e) * getNormal(lf).getAngle(getNormal(rf));
	}
	
	/*
	 * 
	 * @param e, an MEdge
	 * @return -1,0,1 the sign of the angle between the left and the right face.
	 * 			negative is concave, positive if convex
	 *          
	 */
	private static double curvatureSign(VEdge e){
		Matrix m = MatrixBuilder.euclidean().getMatrix();
		for (int i = 0; i < 3; i++) {
			m.setEntry(i, 0, getVector(e).get(i));
			m.setEntry(i, 1, getNormal(e.getLeftFace()).get(i));
			m.setEntry(i, 2, getNormal(e.getRightFace()).get(i));
		}
		double det = m.getDeterminant() ;
		if(Math.abs(det)< 1E-7)
			return 0;
		else if (det<0)
			return -1;
		else 
			return 1;
	}
    
    
    public static double getLength(VEdge e){
    	Point start = new Point(e.getStartVertex().position);
		Point target = new Point(e.getTargetVertex().position);
		return start.distanceTo(target);
	}
    
    public static Vector getVector(VEdge e){
    	Point start = new Point(e.getStartVertex().position);
		Point target = new Point(e.getTargetVertex().position);
		return start.vectorTo(target);
    	
    }
	
}
