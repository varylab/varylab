package de.varylab.varylab.plugin.nurbs;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import de.varylab.varylab.plugin.nurbs.math.NURBSAlgorithm;

public class KnotInsertionTests {
	
	@Test
	public void knotInsertionTest(){
		double[] uv = {0.33, 0.44};
		double[] U = {0.0,0.0,0.0,0.0,1.0,1.0,1.0,1.0};
		double[] V = {0.0,0.0,0.0,0.0,1.0,1.0,1.0,1.0};
		int p = 3;
		int q = 3;
		double[][][]cm = {{{-16.84591428200644, 9.165210069137606, 0.0, 1.0}, {-15.36140842573768, 3.743536507112545, -5.35712982914381, 1.0}, {-14.52233989828141, -1.484505856268766, 3.743536507112545, 1.0}, {-18.26587640539396, -5.550761027787562, 0.0, 1.0}}, 
						{{-8.84249140473135, 12.39239671320014, 10.06882232947512, 1.0}, {-6.454373288125074, 4.711692500331306, 6.045596313210488, 1.0}, {-4.066255171518796, -1.871768253556271, 4.582605034568804, 1.0}, {-8.455229007443846, -8.197054075918842, 0.0, 1.0}}, 
						{{6.389829555243823, 12.39239671320014, 0.0, 1.0}, {10.56365761489803, 4.973453194794152, 1.742680787793771, 1.0}, {10.28396810574596, -2.78972356564517, -5.851965114566728, 1.0}, {7.938879144393841, -8.00342287727509, -6.841635685412578, 1.0}}, 
						{{17.1686329464127, 7.938879144393843, 0.0, 1.0}, {18.65313880268147, 0.839068527456261, 8.713403938968852, 1.0}, {18.65313880268147, -1.419962123387515, -3.808080239993792, 1.0}, {17.62043907658145, -7.164354349818831, 0.0, 1.0}}};

		double[] s1 = new double[3];
		NURBSAlgorithm.SurfacePoint(p, U, q, V, cm, uv[0], uv[1], s1);
		NURBSSurface ns2 = NURBSAlgorithm.SurfaceKnotInsertion(U, V, cm, true, uv[0], 2);
		double[] U2 = ns2.getUKnotVector();
		double[] V2 = ns2.getVKnotVector();
		double[][][] cm2 = ns2.getControlMesh(); 
		double[] s2 = new double[3];
		NURBSAlgorithm.SurfacePoint(p, U2, q, V2, cm2, uv[0], uv[1], s2);
		double delta = 0.001;
		Assert.assertArrayEquals(null, s1, s2, delta);
		
		NURBSSurface ns3 = NURBSAlgorithm.SurfaceKnotInsertion(U, V, cm, false, uv[1], 3);
		double[] U3 = ns3.getUKnotVector();
		double[] V3 = ns3.getVKnotVector();
		double[][][] cm3 = ns3.getControlMesh(); 
		double[] s3 = new double[3];
		NURBSAlgorithm.SurfacePoint(p, U3, q, V3, cm3, uv[0], uv[1], s3);
		Assert.assertArrayEquals(null, s1, s3, delta);
	}
	
	@Test
	public void decomositionTest(){
		double delta = 0.001;
		double[] uv = {0.1234, 0.1234};
		double[] uInsertion = {0.11, 0.22, 0.33, 0.44, 0.55, 0.66, 0.77, 0.88, 0.99};
		double[] vInsertion = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		double[]U = {0.0,0.0,0.0,0.0,1.0,1.0,1.0,1.0};
		double[] V = {0.0,0.0,0.0,0.0,1.0,1.0,1.0,1.0};
		int p = 3;
		int q = 3;
		double[][][]Pw0 = {{{-16.84591428200644, 9.165210069137606, 0.0, 1.0}, {-15.36140842573768, 3.743536507112545, -5.35712982914381, 1.0}, {-14.52233989828141, -1.484505856268766, 3.743536507112545, 1.0}, {-18.26587640539396, -5.550761027787562, 0.0, 1.0}}, 
						{{-8.84249140473135, 12.39239671320014, 10.06882232947512, 1.0}, {-6.454373288125074, 4.711692500331306, 6.045596313210488, 1.0}, {-4.066255171518796, -1.871768253556271, 4.582605034568804, 1.0}, {-8.455229007443846, -8.197054075918842, 0.0, 1.0}}, 
						{{6.389829555243823, 12.39239671320014, 0.0, 1.0}, {10.56365761489803, 4.973453194794152, 1.742680787793771, 1.0}, {10.28396810574596, -2.78972356564517, -5.851965114566728, 1.0}, {7.938879144393841, -8.00342287727509, -6.841635685412578, 1.0}}, 
						{{17.1686329464127, 7.938879144393843, 0.0, 1.0}, {18.65313880268147, 0.839068527456261, 8.713403938968852, 1.0}, {18.65313880268147, -1.419962123387515, -3.808080239993792, 1.0}, {17.62043907658145, -7.164354349818831, 0.0, 1.0}}};
		double[] s0 = new double[3];
		NURBSAlgorithm.SurfacePoint(p, U, q, V, Pw0, uv[0], uv[1], s0);
		System.out.println("s0 " + Arrays.toString(s0));
		
		/**
		 * insert uInsertion into surface
		 */
		NURBSSurface ns1 = new NURBSSurface();
		double[]U1 = U;
		double[]V1 = V;
		double[][][]Pw1 = Pw0;
		
		for (int i = 0; i < uInsertion.length; i++) {
			ns1 = NURBSAlgorithm.SurfaceKnotInsertion(U1, V1, Pw1, true, uInsertion[i], 1);
//			System.out.println("ns1 " + ns1.toString());
			U1 = ns1.getUKnotVector();
			V1 = ns1.getVKnotVector();
			Pw1 = ns1.getControlMesh();
		}
		for (int i = 0; i < vInsertion.length; i++) {
			ns1 = NURBSAlgorithm.SurfaceKnotInsertion(U1, V1, Pw1, false, vInsertion[i], 1);
//			System.out.println("ns1 " + ns1.toString());
			U1 = ns1.getUKnotVector();
			V1 = ns1.getVKnotVector();
			Pw1 = ns1.getControlMesh();
		}
		
		
		double[] s1 = new double[3];
		NURBSAlgorithm.SurfacePoint(p, U1, q, V1, Pw1, uv[0], uv[1], s1);
//		System.out.println("s1 " + Arrays.toString(s1));
		
		NURBSSurface decomposed = NURBSAlgorithm.decomposeSurface(ns1);
		System.out.println("decomposed " + decomposed.toString());
		double[] Ud = ns1.getUKnotVector();
		double[] Vd = ns1.getVKnotVector();
		double[][][]Pwd = ns1.getControlMesh();

		double[] sDecomposed = new double[3];
		NURBSAlgorithm.SurfacePoint(p, Ud, q, Vd, Pwd, uv[0], uv[1], sDecomposed);
		System.out.println("sDecomdosed " + Arrays.toString(sDecomposed));
		Assert.assertArrayEquals(null, s1, sDecomposed, delta);
	}

}
