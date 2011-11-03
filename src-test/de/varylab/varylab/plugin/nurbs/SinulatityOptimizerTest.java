package de.varylab.varylab.plugin.nurbs;

import static de.varylab.varylab.plugin.nurbs.SingularityOptimizer.findSingularities;

import java.io.FileReader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.varylab.varylab.plugin.io.NurbsIO;

public class SinulatityOptimizerTest {

	private NURBSSurface
		surface = null;
	
	@Before
	public void init() throws Exception {
		FileReader reader = new FileReader("data/nurbs/hat.obj");
		surface = NurbsIO.readNURBS(reader);
	}
	
	@Test
	public void testFindSingularities() throws Exception {
		List<double[]> s = findSingularities(surface);
		org.junit.Assert.assertEquals(5, s.size());
	}
	
	
}
