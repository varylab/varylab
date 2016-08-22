package de.varylab.varylab.utilities;

import org.junit.Assert;
import org.junit.Test;

public class GeometryUtilityTests {

	@Test
	public void testCreateIncircle01() {
		double[] v0 = {0.0, 0.0, 1.0};
		double[] v1 = {1.0, 0.0, 1.0};
		double[] v2 = {1.0, 1.0, 1.0};
		double[] v3 = {0.0, 1.0, 1.0};
		double[] circle = GeometryUtility.getIncircle(v0, v1, v2, v3);
		double[] expected = {0.5, 0.5, 1.0, 0.5};
		Assert.assertArrayEquals(expected, circle, 1E-20);
	}

	@Test
	public void testCreateIncircle02() {
		double[] v0 = {-5.0, 2.0, 1.0};
		double[] v1 = {-3.0, 0.0, 1.0};
		double[] v2 = {-1.0, 2.0, 1.0};
		double[] v3 = {-3.0, 4.0, 1.0};
		double[] circle = GeometryUtility.getIncircle(v0, v1, v2, v3);
		double[] expected = {-3.0, 2.0, 1.0, Math.sqrt(2)};
		Assert.assertArrayEquals(expected, circle, 1E-15);
	}
	
}
