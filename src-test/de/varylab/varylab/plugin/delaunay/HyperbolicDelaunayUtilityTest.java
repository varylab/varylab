package de.varylab.varylab.plugin.delaunay;

import org.junit.Assert;
import org.junit.Test;

import de.jreality.math.Pn;

public class HyperbolicDelaunayUtilityTest {

	@Test
	public void testIsHyperbolicFace() {
		double[] normal = new double[]{0.0,0.0,-1.0};
		Assert.assertTrue(HyperbolicDelaunayUtility.isHyperbolicFace(normal));
	}

	@Test
	public void testTwoLowerFacesEdge() {
		double[] n1 = new double[]{0.2, 0.0, -1.0};
		double[] n2 = new double[]{0.3, 0.0, -1.0};
		Assert.assertTrue(HyperbolicDelaunayUtility.isHyperbolicEdge(n1, n2));
	}
	
	@Test
	public void testIsHyperbolicEdge() {
		double[] n1 = new double[]{-0.8526536780342775, 0.4536828249098126, -0.2591401159922384};
		double[] n2 = new double[]{0.3699301794890818, 0.6214168108062095, -0.6906466604209532};
		Assert.assertFalse(HyperbolicDelaunayUtility.isPositiveDefinite(n1, n2, Pn.HYPERBOLIC));
		Assert.assertTrue(HyperbolicDelaunayUtility.isHyperbolicEdge(n1, n2));
		
		n1 = new double[]{0.3699301794890818, 0.6214168108062095, -0.6906466604209532};
		n2 = new double[]{0.5509445616899742, 0.5347868898090237, -0.6406739205185688};
		Assert.assertFalse(HyperbolicDelaunayUtility.isPositiveDefinite(n1, n2, Pn.HYPERBOLIC));
		Assert.assertFalse(HyperbolicDelaunayUtility.isHyperbolicEdge(n1, n2));
		
		n1 = new double[]{0.5509445616899742, 0.5347868898090237, -0.6406739205185688};
		n2 = new double[]{0.5528647966597867, 0.5337548530935221, -0.6398798898335887};
		Assert.assertFalse(HyperbolicDelaunayUtility.isPositiveDefinite(n1, n2, Pn.HYPERBOLIC));
		Assert.assertFalse(HyperbolicDelaunayUtility.isHyperbolicEdge(n1, n2));
		
		n1 = new double[]{0.5528647966597867, 0.5337548530935221, -0.6398798898335887};
		n2 = new double[]{0.9485720671204741, 0.18210858802859317, -0.25893531169970385};
		Assert.assertFalse(HyperbolicDelaunayUtility.isPositiveDefinite(n1, n2, Pn.HYPERBOLIC));	
		Assert.assertFalse(HyperbolicDelaunayUtility.isHyperbolicEdge(n1, n2));
	}

}
