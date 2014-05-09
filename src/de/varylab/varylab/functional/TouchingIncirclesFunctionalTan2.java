package de.varylab.varylab.functional;

import static java.lang.Math.sqrt;
import de.jreality.math.Rn;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.varylab.varylab.halfedge.VEdge;
import de.varylab.varylab.halfedge.VFace;
import de.varylab.varylab.halfedge.VVertex;

public class TouchingIncirclesFunctionalTan2 implements Functional<VVertex, VEdge, VFace> {

	private double[]
	    p1 = new double[3],
	    p2 = new double[3],
	    p3 = new double[3],
		p4 = new double[3],
		p5 = new double[3],
		p6 = new double[3];
	
	
	public TouchingIncirclesFunctionalTan2() {
	}

	@Override
	public <
		HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>
	> void evaluate(
		HDS hds, 
		DomainValue x, 
		Energy E, 
		Gradient G, 
		Hessian H
	) {
		if (E != null) {
			E.setZero();
			for (VEdge e : hds.getNegativeEdges()) {
				if (HalfEdgeUtils.isBoundaryEdge(e)) continue;
				VEdge eo = e.getOppositeEdge();
				VVertex v1 = e.getStartVertex();
				VVertex v2 = e.getTargetVertex();
				VVertex v3 = e.getPreviousEdge().getStartVertex();
				VVertex v4 = e.getNextEdge().getTargetVertex();
				VVertex v5 = eo.getNextEdge().getTargetVertex();
				VVertex v6 = eo.getPreviousEdge().getStartVertex();
				FunctionalUtils.getPosition(v1, x, p1);
				FunctionalUtils.getPosition(v2, x, p2);
				FunctionalUtils.getPosition(v3, x, p3);
				FunctionalUtils.getPosition(v4, x, p4);
				FunctionalUtils.getPosition(v5, x, p5);
				FunctionalUtils.getPosition(v6, x, p6);
				double[] a = Rn.subtract(null, p3, p1);
				double[] b = Rn.subtract(null, p4, p2);
				double[] e1 = Rn.subtract(null, p2, p1);
				double[] e2 = Rn.subtract(null, p1, p2);
				double[] as = Rn.subtract(null, p5, p1);
				double[] bs = Rn.subtract(null, p6, p2);
				double ta = tan2(a, e1);
				double tb = tan2(b, e2);
				double tas = tan2(as, e1);
				double tbs = tan2(bs, e2);
				double energy = ta*tbs - tas*tb;
				E.add(energy * energy);
			}
		}
		if (G != null) {
			G.setZero();
			for (VEdge e : hds.getNegativeEdges()) {
				if (HalfEdgeUtils.isBoundaryEdge(e)) continue;
				VEdge eo = e.getOppositeEdge();
				VVertex v1 = e.getStartVertex();
				VVertex v2 = e.getTargetVertex();
				VVertex v3 = e.getPreviousEdge().getStartVertex();
				VVertex v4 = e.getNextEdge().getTargetVertex();
				VVertex v5 = eo.getNextEdge().getTargetVertex();
				VVertex v6 = eo.getPreviousEdge().getStartVertex();
				FunctionalUtils.getPosition(v1, x, p1);
				FunctionalUtils.getPosition(v2, x, p2);
				FunctionalUtils.getPosition(v3, x, p3);
				FunctionalUtils.getPosition(v4, x, p4);
				FunctionalUtils.getPosition(v5, x, p5);
				FunctionalUtils.getPosition(v6, x, p6);
				final double
					v11 = p1[0],v12 = p1[1],v13 = p1[2],
					v21 = p2[0],v22 = p2[1],v23 = p2[2],
					v31 = p3[0],v32 = p3[1],v33 = p3[2],
					v41 = p4[0],v42 = p4[1],v43 = p4[2],
					v51 = p5[0],v52 = p5[1],v53 = p5[2],
					v61 = p6[0],v62 = p6[1],v63 = p6[2];
				// v1
				G.add(3 * v1.getIndex() + 0, dEdv11(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				G.add(3 * v1.getIndex() + 1, dEdv12(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				G.add(3 * v1.getIndex() + 2, dEdv13(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				
				// v2
				G.add(3 * v2.getIndex() + 0, dEdv21(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				G.add(3 * v2.getIndex() + 1, dEdv22(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				G.add(3 * v2.getIndex() + 2, dEdv23(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				
				// v3
				G.add(3 * v3.getIndex() + 0, dEdv31(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				G.add(3 * v3.getIndex() + 1, dEdv32(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				G.add(3 * v3.getIndex() + 2, dEdv33(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				
				// v4
				G.add(3 * v4.getIndex() + 0, dEdv41(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				G.add(3 * v4.getIndex() + 1, dEdv42(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				G.add(3 * v4.getIndex() + 2, dEdv43(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);	
				
				// v5
				G.add(3 * v5.getIndex() + 0, dEdv51(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				G.add(3 * v5.getIndex() + 1, dEdv52(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				G.add(3 * v5.getIndex() + 2, dEdv53(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);	
				
				// v6
				G.add(3 * v6.getIndex() + 0, dEdv61(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				G.add(3 * v6.getIndex() + 1, dEdv62(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
				G.add(3 * v6.getIndex() + 2, dEdv63(
					v11, v12, v13, 
					v21, v22, v23, 
					v31, v32, v33, 
					v41, v42, v43, 
					v51, v52, v53, 
					v61, v62, v63)
				);
			}
		}
	}
	
	private double tan2(double[] x, double[] e) {
		double[] cr = Rn.crossProduct(null, x, e);
		double crcr = Rn.innerProduct(cr, cr);
		double xe = Rn.innerProduct(x, e);
		double ee = Rn.innerProduct(e, e);
		double xx = Rn.innerProduct(x, x);
		double div = xe + sqrt(xx*ee);
		return crcr / (div*div);
	}
	
	public double Power(double a, double b) {
		return Math.pow(a, b);
	}
	
	public double Sqrt(double x) {
		if (x < 0) {
			throw new RuntimeException("No square root of negative numbers.");
		}
		return Math.sqrt(x);
	}
	
	
	/*
	 * 6 ------------------
	 */
	public double dEdv63(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*((-2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		          Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2))*
		          (v13 - v23 + ((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(-v23 + v63))/
		             Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2)))))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),3)) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (2*(v11 - v21)*(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63)) + 2*(-v12 + v22)*((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63))))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)))*
		     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	
	public double dEdv62(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*((-2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		          Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2))*
		          (v12 - v22 + ((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(-v22 + v62))/
		             Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2)))))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),3)) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (2*(-v11 + v21)*((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62)) + 2*(v13 - v23)*((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63))))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)))*
		     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	
	public double dEdv61(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*((-2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		          Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2))*
		          (v11 - v21 + ((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(-v21 + v61))/
		             Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2)))))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),3)) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (2*(v12 - v22)*((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62)) + 2*(-v13 + v23)*(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63))))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)))*
		     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	
	/*
	 * 5 ------------------
	 */
	public double dEdv53(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*((2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		          Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2))*
		          (-v13 + v23 + ((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(-v13 + v53))/
		             Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2)))))/
		        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),3)) - 
		       ((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		          (2*(-v11 + v21)*(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53)) + 2*(v12 - v22)*((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53))))/
		        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)))*
		     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	
	public double dEdv52(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*((2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		          Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2))*
		          (-v12 + v22 + ((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(-v12 + v52))/
		             Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2)))))/
		        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),3)) - 
		       ((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		          (2*(v11 - v21)*((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52)) + 2*(-v13 + v23)*((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53))))/
		        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)))*
		     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	
	public double dEdv51(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*((2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		          Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2))*
		          (-v11 + v21 + ((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(-v11 + v51))/
		             Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2)))))/
		        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),3)) - 
		       ((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		          (2*(-v12 + v22)*((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52)) + 2*(v13 - v23)*(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53))))/
		        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)))*
		     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	
	/*
	 * 4 ------------------
	 */
	public double dEdv43(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*((2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		          Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		          (v13 - v23 + ((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(-v23 + v43))/
		             Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))))*
		          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),3)*
		          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) - 
		       ((2*(v11 - v21)*(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43)) + 2*(-v12 + v22)*((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43)))*
		          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)))*
		     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	
	public double dEdv42(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*((2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		          Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		          (v12 - v22 + ((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(-v22 + v42))/
		             Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))))*
		          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),3)*
		          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) - 
		       ((2*(-v11 + v21)*((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42)) + 2*(v13 - v23)*((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43)))*
		          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)))*
		     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	
	public double dEdv41(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*((2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		          Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		          (v11 - v21 + ((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(-v21 + v41))/
		             Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))))*
		          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),3)*
		          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) - 
		       ((2*(v12 - v22)*((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42)) + 2*(-v13 + v23)*(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43)))*
		          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)))*
		     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	
	/*
	 * 3 ------------------
	 */
	public double dEdv33(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*((-2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		          Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (-v13 + v23 + ((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(-v13 + v33))/
		             Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),3)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) + 
		       ((2*(-v11 + v21)*(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33)) + 2*(v12 - v22)*((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33)))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)))*
		     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	
	public double dEdv32(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*((-2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		          Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (-v12 + v22 + ((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(-v12 + v32))/
		             Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),3)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) + 
		       ((2*(v11 - v21)*((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32)) + 2*(-v13 + v23)*((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33)))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)))*
		     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	
	public double dEdv31(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*((-2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		          Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (-v11 + v21 + ((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(-v11 + v31))/
		             Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),3)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) + 
		       ((2*(-v12 + v22)*((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32)) + 2*(v13 - v23)*(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33)))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)))*
		     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
		              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
		            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
		              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
		          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
		              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
		            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
		              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
		       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
		            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
		          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
		            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
		        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
		            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
		          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
		            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	
	/*
	 * 2 ------------------
	 */
	public double dEdv23(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*(-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
	       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)))*
	     ((2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2))*
	          (-v13 + v53 + ((-v13 + v23)*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2)))/
	             Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2)))))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),3)) - 
	       ((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (2*(v11 - v51)*(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53)) + 2*(-v12 + v52)*((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53))))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) - 
	       ((2*(-v11 + v41)*(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43)) + 2*(v12 - v42)*((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43)))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) + 
	       (2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (-v13 + 2*v23 - v43 + (-2*(Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(-v23 + v43) - 
	               2*(v13 - v23)*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2)))/
	             (2.*Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2)))))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),3)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) + 
	       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (2*(-v11 + v61)*(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63)) + 2*(v12 - v62)*((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63))))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) - 
	       (2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (-v13 + v33 + ((-v13 + v23)*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2)))/
	             Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),3)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) + 
	       ((2*(v11 - v31)*(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33)) + 2*(-v12 + v32)*((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33)))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) - 
	       (2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2))*
	          (-v13 + 2*v23 - v63 + (-2*(Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(-v23 + v63) - 
	               2*(v13 - v23)*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2)))/
	             (2.*Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))))))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),3)));
	}
	
	public double dEdv22(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*(-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
	       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)))*
	     ((2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2))*
	          (-v12 + v52 + ((-v12 + v22)*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2)))/
	             Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2)))))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),3)) - 
	       ((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (2*(-v11 + v51)*((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52)) + 2*(v13 - v53)*((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53))))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) - 
	       ((2*(v11 - v41)*((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42)) + 2*(-v13 + v43)*((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43)))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) + 
	       (2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (-v12 + 2*v22 - v42 + (-2*(Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(-v22 + v42) - 
	               2*(v12 - v22)*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2)))/
	             (2.*Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2)))))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),3)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) + 
	       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (2*(v11 - v61)*((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62)) + 2*(-v13 + v63)*((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63))))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) - 
	       (2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (-v12 + v32 + ((-v12 + v22)*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2)))/
	             Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),3)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) + 
	       ((2*(-v11 + v31)*((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32)) + 2*(v13 - v33)*((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33)))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) - 
	       (2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2))*
	          (-v12 + 2*v22 - v62 + (-2*(Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(-v22 + v62) - 
	               2*(v12 - v22)*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2)))/
	             (2.*Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))))))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),3)));
	}
	
	public double dEdv21(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*(-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
	       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)))*
	     ((2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2))*
	          (-v11 + v51 + ((-v11 + v21)*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2)))/
	             Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2)))))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),3)) - 
	       ((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (2*(v12 - v52)*((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52)) + 2*(-v13 + v53)*(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53))))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) - 
	       ((2*(-v12 + v42)*((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42)) + 2*(v13 - v43)*(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43)))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) + 
	       (2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (-v11 + 2*v21 - v41 + (-2*(Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(-v21 + v41) - 
	               2*(v11 - v21)*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2)))/
	             (2.*Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2)))))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),3)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) + 
	       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (2*(-v12 + v62)*((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62)) + 2*(v13 - v63)*(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63))))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) - 
	       (2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (-v11 + v31 + ((-v11 + v21)*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2)))/
	             Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),3)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) + 
	       ((2*(v12 - v32)*((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32)) + 2*(-v13 + v33)*(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33)))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) - 
	       (2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2))*
	          (-v11 + 2*v21 - v61 + (-2*(Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(-v21 + v61) - 
	               2*(v11 - v21)*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2)))/
	             (2.*Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))))))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),3)));
	}
	
	
	/*
	 * 1 ----------------------
	 */
	public double dEdv13(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*(-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
	       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)))*
	     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	            (2*(-v21 + v51)*(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53)) + 2*(v22 - v52)*((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53))))/
	          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
	       (2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (-v23 + v43 + ((v13 - v23)*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2)))/
	             Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),3)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) - 
	       ((2*(v21 - v41)*(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43)) + 2*(-v22 + v42)*((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43)))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) + 
	       (2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2))*
	          (2*v13 - v23 - v53 + (-2*(Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(-v13 + v53) - 
	               2*(-v13 + v23)*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2)))/
	             (2.*Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))))))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),3)) - 
	       (2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2))*
	          (-v23 + v63 + ((v13 - v23)*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2)))/
	             Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2)))))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),3)) + 
	       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (2*(v21 - v61)*(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63)) + 2*(-v22 + v62)*((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63))))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) + 
	       ((2*(-v21 + v31)*(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33)) + 2*(v22 - v32)*((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33)))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) - 
	       (2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (2*v13 - v23 - v33 + (-2*(Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(-v13 + v33) - 
	               2*(-v13 + v23)*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2)))/
	             (2.*Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2)))))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),3)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	
	public double dEdv12(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*(-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
	       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)))*
	     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	            (2*(v21 - v51)*((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52)) + 2*(-v23 + v53)*((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53))))/
	          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
	       (2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (-v22 + v42 + ((v12 - v22)*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2)))/
	             Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),3)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) - 
	       ((2*(-v21 + v41)*((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42)) + 2*(v23 - v43)*((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43)))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) + 
	       (2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2))*
	          (2*v12 - v22 - v52 + (-2*(Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(-v12 + v52) - 
	               2*(-v12 + v22)*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2)))/
	             (2.*Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))))))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),3)) - 
	       (2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2))*
	          (-v22 + v62 + ((v12 - v22)*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2)))/
	             Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2)))))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),3)) + 
	       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (2*(-v21 + v61)*((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62)) + 2*(v23 - v63)*((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63))))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) + 
	       ((2*(v21 - v31)*((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32)) + 2*(-v23 + v33)*((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33)))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) - 
	       (2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (2*v12 - v22 - v32 + (-2*(Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(-v12 + v32) - 
	               2*(-v12 + v22)*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2)))/
	             (2.*Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2)))))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),3)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	
	public double dEdv11(
		double v11, double v12, double v13, 
		double v21, double v22, double v23, 
		double v31, double v32, double v33, 
		double v41, double v42, double v43, 
		double v51, double v52, double v53, 
		double v61, double v62, double v63
	) {
		return 2*(-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	            (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	              Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
	       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)))*
	     (-(((Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	              Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	            (2*(-v22 + v52)*((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52)) + 2*(v23 - v53)*(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53))))/
	          (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	              Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	            Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	              Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2))) + 
	       (2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (-v21 + v41 + ((v11 - v21)*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2)))/
	             Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),3)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) - 
	       ((2*(v22 - v42)*((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42)) + 2*(-v23 + v43)*(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43)))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2)))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),2)) + 
	       (2*(Power((v12 - v22)*(-v21 + v41) - (v11 - v21)*(-v22 + v42),2) + Power(-((v13 - v23)*(-v21 + v41)) + (v11 - v21)*(-v23 + v43),2) + 
	            Power((v13 - v23)*(-v22 + v42) - (v12 - v22)*(-v23 + v43),2))*
	          (Power((-v12 + v22)*(-v11 + v51) - (-v11 + v21)*(-v12 + v52),2) + Power(-((-v13 + v23)*(-v11 + v51)) + (-v11 + v21)*(-v13 + v53),2) + 
	            Power((-v13 + v23)*(-v12 + v52) - (-v12 + v22)*(-v13 + v53),2))*
	          (2*v11 - v21 - v51 + (-2*(Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(-v11 + v51) - 
	               2*(-v11 + v21)*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2)))/
	             (2.*Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))))))/
	        (Power((v11 - v21)*(-v21 + v41) + (v12 - v22)*(-v22 + v42) + (v13 - v23)*(-v23 + v43) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v41,2) + Power(-v22 + v42,2) + Power(-v23 + v43,2))),2)*
	          Power((-v11 + v21)*(-v11 + v51) + (-v12 + v22)*(-v12 + v52) + (-v13 + v23)*(-v13 + v53) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v51,2) + Power(-v12 + v52,2) + Power(-v13 + v53,2))),3)) - 
	       (2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2))*
	          (-v21 + v61 + ((v11 - v21)*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2)))/
	             Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2)))))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),3)) + 
	       ((Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (2*(v22 - v62)*((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62)) + 2*(-v23 + v63)*(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63))))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) + 
	       ((2*(-v22 + v32)*((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32)) + 2*(v23 - v33)*(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33)))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),2)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)) - 
	       (2*(Power((-v12 + v22)*(-v11 + v31) - (-v11 + v21)*(-v12 + v32),2) + Power(-((-v13 + v23)*(-v11 + v31)) + (-v11 + v21)*(-v13 + v33),2) + 
	            Power((-v13 + v23)*(-v12 + v32) - (-v12 + v22)*(-v13 + v33),2))*
	          (2*v11 - v21 - v31 + (-2*(Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(-v11 + v31) - 
	               2*(-v11 + v21)*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2)))/
	             (2.*Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2)))))*
	          (Power((v12 - v22)*(-v21 + v61) - (v11 - v21)*(-v22 + v62),2) + Power(-((v13 - v23)*(-v21 + v61)) + (v11 - v21)*(-v23 + v63),2) + 
	            Power((v13 - v23)*(-v22 + v62) - (v12 - v22)*(-v23 + v63),2)))/
	        (Power((-v11 + v21)*(-v11 + v31) + (-v12 + v22)*(-v12 + v32) + (-v13 + v23)*(-v13 + v33) + 
	            Sqrt((Power(-v11 + v21,2) + Power(-v12 + v22,2) + Power(-v13 + v23,2))*(Power(-v11 + v31,2) + Power(-v12 + v32,2) + Power(-v13 + v33,2))),3)*
	          Power((v11 - v21)*(-v21 + v61) + (v12 - v22)*(-v22 + v62) + (v13 - v23)*(-v23 + v63) + 
	            Sqrt((Power(v11 - v21,2) + Power(v12 - v22,2) + Power(v13 - v23,2))*(Power(-v21 + v61,2) + Power(-v22 + v62,2) + Power(-v23 + v63,2))),2)));
	}
	

	@Override
	public boolean hasHessian() {
		return false;
	}

	@Override
	public <
		HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace>
	> int getDimension(HDS hds) {
		return hds.numVertices() * 3;
	}

	@Override
	public <
		HDS extends HalfEdgeDataStructure<VVertex, VEdge, VFace
	>> int[][] getNonZeroPattern(HDS hds) {
		return null;
	}
	
	@Override
	public boolean hasGradient() {
		return true;
	}
}