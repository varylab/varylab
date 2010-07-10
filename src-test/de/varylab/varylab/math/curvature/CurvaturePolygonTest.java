package de.varylab.varylab.math.curvature;

import geom3d.Point;
import geom3d.Vector;

import java.io.IOException;
import java.util.Collection;

import no.uib.cipr.matrix.EVD;

import org.junit.Test;

import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.varylab.discreteconformal.heds.bsp.KdTree;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.hds.adapter.VPositionAdapter;
import de.varylab.varylab.math.CurvatureUtility;


public class CurvaturePolygonTest {

	
	private static VHDS
		hds = new VHDS();
	private static KdTree<VVertex>
		kd = null;
	private static double
		meanEdgeLength = 0.0;
	
	static {
		ReaderOBJ reader = new ReaderOBJ();
		SceneGraphComponent c = null;
		IndexedFaceSet ifs = null;
		try {
			Input in = new Input("Obj File", CurvaturePolygonTest.class.getResourceAsStream("hyperboloid01.obj"));
			c =reader.read(in);
			ifs = (IndexedFaceSet)c.getChildComponent(0).getGeometry();
			ConverterJR2Heds converter = new ConverterJR2Heds();
			AdapterSet a = new AdapterSet(new VPositionAdapter());
			converter.ifs2heds(ifs, hds, a);
			kd = new KdTree<VVertex>(hds.getVertices(), 10, false);
			meanEdgeLength = CurvatureUtility.meanEdgeLength(hds);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testGaussBonnet() {
		Collection<VEdge> bEdges = HalfEdgeUtils.boundaryEdges(hds);
		Vector lastXVec = null;
//		double kSum = 0.0;
		for (VEdge e : bEdges) {
			Point s = e.getStartVertex().getPosition();
			Point t = e.getTargetVertex().getPosition();
			Point p = new Point();
			p.add(s);
			p.add(t);
			p.times(0.5);
			EVD evd = CurvatureUtility.getTensorInformation(p, meanEdgeLength * 6, kd);
			Vector[] eigen = CurvatureUtility.getSortedEigenVectors(evd);
			Vector eVec = s.vectorTo(t);
			Vector xVec = eigen[0];
			if (lastXVec != null && xVec.getAngle(lastXVec) > Math.PI / 2) {
				xVec.times(-1);
				System.err.println("turn");
			}
			double alpha = eVec.getAngle(xVec);
			
			System.out.println("Alpha " + e + ": " + alpha);
			lastXVec = xVec;
		}
	}
	
	
	
	public static double getAlpha(VEdge e) {
		return 0.0;
	}
	

	
	
}
