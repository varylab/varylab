package de.varylab.varylab.plugin.nurbs;

import java.io.FileReader;

import org.junit.Assert;
import org.junit.Test;

import de.varylab.varylab.plugin.io.NurbsIO;

public class ClosestPointTests {
	
	@Test
	public void testClosestPointTest1(){
		NURBSSurface ns = new NURBSSurface();
		try {
			ns = NurbsIO.readNURBS(new FileReader("data/nurbs/plane.obj"));
		} catch (Exception ex) {
			System.err.println("file not found");
		}
		double[] point = {0.,0.,1.,1.};
		double[] exactPoint1 = {0.,0.,0.,1.};
		double[] closestPoint = ns.getClosestPoint(point);
		double delta = 0.01;
		Assert.assertArrayEquals(null, exactPoint1, closestPoint,delta);
		point[1] = 20.;
		double[] exactPoint2 = {0.,10.,0.,1.};
		closestPoint = ns.getClosestPoint(point);
		Assert.assertArrayEquals(null, exactPoint2, closestPoint,delta);
	}
	
}
