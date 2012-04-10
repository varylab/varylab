package de.varylab.varylab.plugin.nurbs;

import java.util.Arrays;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

public class KnotInsertionTests {
	
	@Test
	public void knotInsertionTest(){
		double[] uv = {0.33, 0.44};
		double []point = {0.1, 0.7};
		double[] U = {0.0,0.0,0.0,0.0,1.0,1.0,1.0,1.0};
		double[] V = {0.0,0.0,0.0,0.0,1.0,1.0,1.0,1.0};
		int p = 3;
		int q = 3;
		double[][][]cm = {{{-16.84591428200644, 9.165210069137606, 0.0, 1.0}, {-15.36140842573768, 3.743536507112545, -5.35712982914381, 1.0}, {-14.52233989828141, -1.484505856268766, 3.743536507112545, 1.0}, {-18.26587640539396, -5.550761027787562, 0.0, 1.0}}, 
						{{-8.84249140473135, 12.39239671320014, 10.06882232947512, 1.0}, {-6.454373288125074, 4.711692500331306, 6.045596313210488, 1.0}, {-4.066255171518796, -1.871768253556271, 4.582605034568804, 1.0}, {-8.455229007443846, -8.197054075918842, 0.0, 1.0}}, 
						{{6.389829555243823, 12.39239671320014, 0.0, 1.0}, {10.56365761489803, 4.973453194794152, 1.742680787793771, 1.0}, {10.28396810574596, -2.78972356564517, -5.851965114566728, 1.0}, {7.938879144393841, -8.00342287727509, -6.841635685412578, 1.0}}, 
						{{17.1686329464127, 7.938879144393843, 0.0, 1.0}, {18.65313880268147, 0.839068527456261, 8.713403938968852, 1.0}, {18.65313880268147, -1.419962123387515, -3.808080239993792, 1.0}, {17.62043907658145, -7.164354349818831, 0.0, 1.0}}};

		NURBSSurface ns = new NURBSSurface(U, V, cm, p, q);
		double[] s1 = ns.getSurfacePoint(point[0], point[1]);
		NURBSSurface ns2 = ns.SurfaceKnotInsertion(true, uv[0], 2);
		double[] s2 = ns2.getSurfacePoint(point[0], point[1]);
		double delta = 0.001;
		Assert.assertArrayEquals(null, s1, s2, delta);
		
		NURBSSurface ns3 = ns.SurfaceKnotInsertion(false, uv[1], 3);
		double[] s3 = ns3.getSurfacePoint(point[0], point[1]);
		Assert.assertArrayEquals(null, s1, s3, delta);
	}
	
	
	
	
	@Test
	public void decomositionTest(){
		double delta = 0.001;
		double[] uv = {0.1234, 0.1234};
		double[] uInsertion = {0.11, 0.22, 0.33, 0.44, 0.55, 0.66, 0.77, 0.88, 0.99};
		double[] vInsertion = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		double[] U = {0.0,0.0,0.0,0.0,1.0,1.0,1.0,1.0};
		double[] V = {0.0,0.0,0.0,0.0,1.0,1.0,1.0,1.0};
		int p = 3;
		int q = 3;
		double[][][]Pw0 = {{{-16.84591428200644, 9.165210069137606, 0.0, 1.0}, {-15.36140842573768, 3.743536507112545, -5.35712982914381, 1.0}, {-14.52233989828141, -1.484505856268766, 3.743536507112545, 1.0}, {-18.26587640539396, -5.550761027787562, 0.0, 1.0}}, 
						{{-8.84249140473135, 12.39239671320014, 10.06882232947512, 1.0}, {-6.454373288125074, 4.711692500331306, 6.045596313210488, 1.0}, {-4.066255171518796, -1.871768253556271, 4.582605034568804, 1.0}, {-8.455229007443846, -8.197054075918842, 0.0, 1.0}}, 
						{{6.389829555243823, 12.39239671320014, 0.0, 1.0}, {10.56365761489803, 4.973453194794152, 1.742680787793771, 1.0}, {10.28396810574596, -2.78972356564517, -5.851965114566728, 1.0}, {7.938879144393841, -8.00342287727509, -6.841635685412578, 1.0}}, 
						{{17.1686329464127, 7.938879144393843, 0.0, 1.0}, {18.65313880268147, 0.839068527456261, 8.713403938968852, 1.0}, {18.65313880268147, -1.419962123387515, -3.808080239993792, 1.0}, {17.62043907658145, -7.164354349818831, 0.0, 1.0}}};

		NURBSSurface ns0 = new NURBSSurface(U, V, Pw0, p, q);
		double[] s0 = ns0.getSurfacePoint(uv[0], uv[1]);
		NURBSSurface ns1 = new NURBSSurface(U, V, Pw0, p, q);

		/**
		 * insert uInsertion into surface
		 */
		
		for (int i = 0; i < uInsertion.length; i++) {
			ns1 = ns1.SurfaceKnotInsertion(true, uInsertion[i], 1);
		}
		
		/**
		 * insert vInsertion into surface
		 */
		
		for (int i = 0; i < vInsertion.length; i++) {
			ns1 = ns1.SurfaceKnotInsertion(false, vInsertion[i], 1);
		}
		
		double[] s1 = ns1.getSurfacePoint(uv[0], uv[1]);
		NURBSSurface decomposed = ns1.decomposeSurface();
		double[] sDecomposed = decomposed.getSurfacePoint(uv[0], uv[1]);
		NURBSSurface[][] BezierSurfaces = ns1.decomposeIntoBezierSurfaces();
		NURBSSurface Bezier = new NURBSSurface();
		for (int i = 0; i < BezierSurfaces.length; i++) {
			for (int j = 0; j < BezierSurfaces[0].length; j++) {
				if(BezierSurfaces[i][j].getUKnotVector()[0] <= uv[0] && BezierSurfaces[i][j].getUKnotVector()[BezierSurfaces[i][j].getUKnotVector().length - 1] >= uv[0] &&
						BezierSurfaces[i][j].getVKnotVector()[0] <= uv[1] && BezierSurfaces[i][j].getVKnotVector()[BezierSurfaces[i][j].getVKnotVector().length - 1] >= uv[1]){
					Bezier.setUKnotVector(BezierSurfaces[i][j].getUKnotVector());
					Bezier.setVKnotVector(BezierSurfaces[i][j].getVKnotVector());
					Bezier.setControlMesh(BezierSurfaces[i][j].getControlMesh());
					Bezier.setUDegree(p);
					Bezier.setVDegree(q);
				}
			}
		}
		System.out.println("Bezier " + Bezier.toString());
		double[] BezierPoint = Bezier.getSurfacePoint(uv[0], uv[1]);
		/**
		 * s0 is point at original surface
		 * s1 is point at surface after single knot insertion
		 * sDecomposed is point after decomposing
		 * BezierPoint is point at the patch which contains the domain point
		 */
		Assert.assertArrayEquals(null, s1, s0, delta);
		Assert.assertArrayEquals(null, sDecomposed, s0, delta);
		Assert.assertArrayEquals(null, BezierPoint, s0, delta);
	
	}
	
	@Test
	public void decompositionTestNotBezier(){
		// surface of revolution
		double delta = 0.001;
		double u = 0.2;
		double v = 0.3;
		double[] U = {0.0, 0.0, 0.0, 0.0, 6.08276253029822, 13.69853563616213, 17.82164126177979, 20.82164126177979, 20.82164126177979, 20.82164126177979, 20.82164126177979};
		double[] V = {0.0, 0.0, 0.0, 3.141592653589793, 3.141592653589793, 6.283185307179586, 6.283185307179586, 6.283185307179586};
		int p = 3;
		int q = 2;
		double[][][]Pw0 = {{{0.0, 8.0, 0.0, 1.0},{0.0, 5.656854249492381, 5.65685424949238, 0.7071067811865476},{0.0, 4.898587196589413E-16, 8.0, 1.0},{0.0, -5.65685424949238, 5.656854249492381, 0.7071067811865476},{0.0, -8.0, 9.797174393178826E-16, 1.0}}, 
						{{1.878837730442698, 8.762285965878977, 0.0, 1.0},{1.3285388999451744, 6.195871825168743, 6.195871825168742, 0.7071067811865476},{1.878837730442698, 5.365352730663731E-16, 8.762285965878977, 1.0},{1.3285388999451744, -6.195871825168742, 6.195871825168743, 0.7071067811865476},{1.878837730442698, -8.762285965878977, 1.073070546132746E-15, 1.0}},
						{{6.732195665770425, 10.3579035456024, 0.0, 1.0},{4.760381207540952, 7.324143835971641, 7.324143835971641, 0.7071067811865476},{6.732195665770425, 6.342386711499501E-16, 10.3579035456024, 1.0},{4.760381207540952, -7.324143835971641, 7.324143835971641, 0.7071067811865476},{6.732195665770425, -10.3579035456024, 1.2684773422999E-15, 1.0}},
						{{11.79896093016204, 4.652377985466189, 0.0, 1.0},{8.343125284672714, 3.2897280221661513, 3.289728022166151, 0.7071067811865476},{11.79896093016204, 2.84875990416239E-16, 4.652377985466189, 1.0},{8.343125284672714, -3.289728022166151, 3.2897280221661513, 0.7071067811865476},{11.79896093016204, -4.652377985466189, 5.69751980832478E-16, 1.0}}, 
						{{16.6099396548775, 7.289389793384495, 0.0, 1.0},{11.745000965063225, 5.154376953614183, 5.154376953614182, 0.7071067811865476},{16.6099396548775, 4.463463939102854E-16, 7.289389793384495, 1.0},{11.745000965063225, -5.154376953614182, 5.154376953614183, 0.7071067811865476},{16.6099396548775, -7.289389793384495, 8.926927878205708E-16, 1.0}}, 
						{{19.00504976463275, 7.100369463198012, 0.0, 1.0},{13.438599565359619, 5.020719396357201, 5.0207193963572, 0.7071067811865476},{19.00504976463275, 4.347722367934528E-16, 7.100369463198012, 1.0},{13.438599565359619, -5.0207193963572, 5.020719396357201, 0.7071067811865476},{19.00504976463275, -7.100369463198012, 8.695444735869056E-16, 1.0}}, 
						{{20.0, 7.0, 0.0, 1.0},{14.142135623730951, 4.949747468305833, 4.949747468305832, 0.7071067811865476},{20.0, 4.286263797015736E-16, 7.0, 1.0},{14.142135623730951, -4.949747468305832, 4.949747468305833, 0.7071067811865476},{20.0, -7.0, 8.572527594031472E-16, 1.0}}}; 
		NURBSSurface ns = new NURBSSurface(U, V, Pw0, p, q);
		NURBSSurface nsDecomposed = ns.decomposeSurface();
		double[] originalPoint = ns.getSurfacePoint(u, v);
		double[] decomposedPoint = nsDecomposed.getSurfacePoint(u, v);
		double[] bezierPoint = new double[4];
		LinkedList<NURBSSurface> bezierList = ns.decomposeIntoBezierSurfacesList();
		for (NURBSSurface bezier : bezierList) {
			double[] bU = bezier.getUKnotVector();
			double[] bV = bezier.getVKnotVector();
			double uStart = bU[0];
			double uEnd = bU[bU.length - 1];
			double vStart = bV[0];
			double vEnd = bV[bV.length - 1];
			if(u >= uStart && u <= uEnd && v >= vStart && v<= vEnd){
				bezierPoint = bezier.getSurfacePoint(u, v);
			}
		}
		Assert.assertArrayEquals(null, decomposedPoint, originalPoint, delta);
		Assert.assertArrayEquals(null, bezierPoint, originalPoint, delta);
	
	}

}
