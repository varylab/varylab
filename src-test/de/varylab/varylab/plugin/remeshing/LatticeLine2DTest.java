package de.varylab.varylab.plugin.remeshing;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class LatticeLine2DTest {

	
	
	@SuppressWarnings("rawtypes")
	private LatticeLine2D ll;

	@Before
	public void createMock() {
		ll = Mockito.mock(LatticeLine2D.class);
		
	}
	
	@Test
	public void testNextIJPointInDirectionQuad() {
		
		double[] startIJ = new double[]{0.5, 0.5};
		Slope ijSlope = new Slope(2.0,2.0);
		Mockito.when(ll.nextIJPointInDirection(startIJ, ijSlope)).thenCallRealMethod();
		double[] nextIJ = ll.nextIJPointInDirection(startIJ,ijSlope);
		Assert.assertArrayEquals(new double[]{1.0, 1.0}, nextIJ, 0.0);
	}
	
	@Test
	public void testNextIJPointInDirectionTriangle() {
		double[] startIJ = new double[]{1.0/3.0, 1.0/3.0};
		Slope ijSlope = new Slope(3.0,3.0);
		Mockito.when(ll.nextIJPointInDirection(startIJ, ijSlope)).thenCallRealMethod();
		double[] nextIJ = ll.nextIJPointInDirection(startIJ,ijSlope);
		Assert.assertArrayEquals(new double[]{1.0, 1.0}, nextIJ, 0.0);
	}
}
