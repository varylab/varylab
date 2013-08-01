package de.varylab.varylab.plugin.nurbs.plugin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;

public class PointSelectionTool extends AbstractTool implements HalfedgeListener {
	private static final InputSlot LEFT_BUTTON = InputSlot.LEFT_BUTTON;
//		private VHDS hds;
//		private AdapterSet as;
		private LinkedList<double[]> points;
		private HalfedgeInterface hif;
		private NURBSSurface ns;
		
//		public double[] getPoint() {
//			return point;
//		}

		public PointSelectionTool(VHDS vhds, AdapterSet a, LinkedList<double[]> pts, HalfedgeInterface h, NURBSSurface n) {
			super(LEFT_BUTTON);
			System.out.println("constructor");
//			hds = vhds;
//			as = a;
			points = pts;
			hif = h;
			ns = n;
		}
		
		@Override
//		public void activate(ToolContext tc) {
//			System.out.println("now I am active. Activated by "+tc.getSource());
//			System.out.println("HALLO");
//			 if (tc.getCurrentPick() == null ){
//				 System.out.println("NIX");
//				 return;
//			 }
//			 int index = tc.getCurrentPick().getIndex();
//			 VFace f =  hds.getFace(index);
//			 LinkedList<VVertex> verts =  (LinkedList<VVertex>) HalfEdgeUtils.boundaryVertices(f);
//			 ArrayList<double[]> domainVerts = new ArrayList<double[]>();
//			 ArrayList<double[]> coords = new ArrayList<double[]>();
//			 for (VVertex v : verts) {
//				double[] q = as.getD(NurbsUVCoordinate.class, v);
//				domainVerts.add(as.getD(NurbsUVCoordinate.class, v));
//				coords.add(Pn.dehomogenize(null, ns.getSurfacePoint(q[0], q[1])));
//				System.out.println("vertex coords " + Arrays.toString(ns.getSurfacePoint(q[0], q[1])));
//			}
//		
//			point = new double[2];
//			double[] oc = tc.getCurrentPick().getObjectCoordinates();
//			double[] lambda = getBarycentricCoordinates(coords, oc);
////			double[] lambda = getCoordsFromBasis(coords, oc);
//			System.out.println("lambda " + Arrays.toString(lambda));
//			System.out.println("object coords " + Arrays.toString(oc));
//			double[] check = new double[4];
//			for (int i = 0; i < lambda.length; i++) {
//				Rn.add(check, Rn.times(null, lambda[i], coords.get(i)), check);
//			}
//			System.out.println("oc " + Arrays.toString(oc) + " check " + Arrays.toString(Pn.dehomogenize(null, check)));
//			for (int i = 0; i < lambda.length; i++) {
//				Rn.add(point, Rn.times(null, lambda[i], domainVerts.get(i)), point);
//			}
////			Rn.add(point, domainVerts.get(3), point);
//			System.out.println("point " + Arrays.toString(point));
//			double[] p = ns.getSurfacePoint(point[0], point[1]);
//			PointSetFactory psfPoint = new PointSetFactory();
//			psfPoint.setVertexCount(1);
//			psfPoint.setVertexCoordinates(p);
//			psfPoint.update();
//			SceneGraphComponent sgcPoint = new SceneGraphComponent("selected point");
//			SceneGraphComponent selectedPointComp = new SceneGraphComponent("selected point");
//			sgcPoint.addChild(selectedPointComp);
//			sgcPoint.setGeometry(psfPoint.getGeometry());
//			Appearance ApPoint = new Appearance();
//			sgcPoint.setAppearance(ApPoint);
//			DefaultGeometryShader dgsPoint = ShaderUtility.createDefaultGeometryShader(ApPoint, false);
//			DefaultPointShader pointShaderPoint = (DefaultPointShader)dgsPoint.getPointShader();
//			pointShaderPoint.setDiffuseColor(Color.red);
//			hif.getActiveLayer().addTemporaryGeometry(sgcPoint);
//			
//			PointSetFactory psf = new PointSetFactory();
//			psf.setVertexCount(1);
//			psf.setVertexCoordinates(oc);
//			psf.update();
//			SceneGraphComponent sgc = new SceneGraphComponent("point");
//			SceneGraphComponent sscg = new SceneGraphComponent("point");
//			sgc.addChild(sscg);
//			sgc.setGeometry(psf.getGeometry());
//			Appearance Ap = new Appearance();
//			sgc.setAppearance(Ap);
//			DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(Ap, false);
//			DefaultPointShader pointShader = (DefaultPointShader)dgs.getPointShader();
//			pointShader.setDiffuseColor(Color.BLACK);
//			hif.getActiveLayer().addTemporaryGeometry(sgc);
//		}
	 
		
		public void activate(ToolContext tc) {
			System.out.println("now I am active. Activated by "+tc.getSource());
			System.out.println("HALLO");
			 if (tc.getCurrentPick() == null ){
				 System.out.println("NIX");
				 return;
			 }
	
//			double[] point = new double[2];
			double[] oc = tc.getCurrentPick().getObjectCoordinates();
			double[] p =  ns.getClosestPoint(oc);
			points.add(ns.getClosestPointDomain(oc));
			System.out.println("size " + points.size());
//			Rn.add(point, domainVerts.get(3), point);
//			System.out.println("point " + Arrays.toString(point));
//			double[] p = ns.getSurfacePoint(point[0], point[1]);
			PointSetFactory psfPoint = new PointSetFactory();
			psfPoint.setVertexCount(1);
			psfPoint.setVertexCoordinates(p);
			psfPoint.update();
			SceneGraphComponent sgcPoint = new SceneGraphComponent("selected point");
			SceneGraphComponent selectedPointComp = new SceneGraphComponent("selected point");
			sgcPoint.addChild(selectedPointComp);
			sgcPoint.setGeometry(psfPoint.getGeometry());
			Appearance ApPoint = new Appearance();
			sgcPoint.setAppearance(ApPoint);
			DefaultGeometryShader dgsPoint = ShaderUtility.createDefaultGeometryShader(ApPoint, false);
			DefaultPointShader pointShaderPoint = (DefaultPointShader)dgsPoint.getPointShader();
			pointShaderPoint.setDiffuseColor(Color.red);
			hif.getActiveLayer().addTemporaryGeometry(sgcPoint);
			
			PointSetFactory psf = new PointSetFactory();
			psf.setVertexCount(1);
			psf.setVertexCoordinates(oc);
			psf.update();
			SceneGraphComponent sgc = new SceneGraphComponent("point");
			SceneGraphComponent sscg = new SceneGraphComponent("point");
			sgc.addChild(sscg);
			sgc.setGeometry(psf.getGeometry());
			Appearance Ap = new Appearance();
			sgc.setAppearance(Ap);
			DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(Ap, false);
			DefaultPointShader pointShader = (DefaultPointShader)dgs.getPointShader();
			pointShader.setDiffuseColor(Color.BLACK);
			hif.getActiveLayer().addTemporaryGeometry(sgc);
		}
		@Override
		public void deactivate(ToolContext tc) {
			System.out.println("No longer active. Deactivated by "+tc.getSource());
		}
	 
		@Override
		public void perform(ToolContext tc) {
			
		}

		@Override
		public void dataChanged(HalfedgeLayer layer) {
			addTool(layer);
		}

		private void addTool(HalfedgeLayer layer) {
			List<SceneGraphPath> paths = SceneGraphUtility.getPathsToNamedNodes(
					layer.getLayerRoot(), "Geometry");
			SceneGraphComponent comp;
			for (SceneGraphPath path : paths) {
				comp = path.getLastComponent();
				if (!comp.getTools().contains(this))
					comp.addTool(this);
			}
		}

		@Override
		public void adaptersChanged(HalfedgeLayer layer) {
		}

		@Override
		public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
			addTool(active);
		}

		@Override
		public void layerCreated(HalfedgeLayer layer) {
			addTool(layer);
		}

		@Override
		public void layerRemoved(HalfedgeLayer layer) {
		}
		
//		public static double[] getBarycentricCoordinates(ArrayList<double[]> points, double[] p){
//			double[] p0 = points.get(0);
//			double[] p1 = points.get(1);
//			double[] p2 = points.get(2);
//			double[] p3 = points.get(3);
//			Matrix T = new Matrix(p0[0] - p3[0], p1[0] - p3[0], p2[0] - p3[0], 0, 
//								  p0[1] - p3[1], p1[1] - p3[1], p2[1] - p3[1], 0,
//								  p0[2] - p3[2], p1[2] - p3[2], p2[2] - p3[2], 0, 
//								  0, 0, 0, 1);
////			if(T.getDeterminant() != 0){
////				System.out.println("full rank");
////				T.invert();
////			}
//			System.out.println(T.toString());
//			T.invert();
//			System.out.println(T.toString());
//			System.out.println();
//			System.out.println(" getBarycentricCoordinates");
//			System.out.println();
//			double[] lambda = T.multiplyVector(Rn.subtract(null, p, p3));
//			lambda[3] = 1 - lambda[0] - lambda[1] - lambda[2];
//			System.out.println("lambda " + Arrays.toString(lambda));
//			return lambda;
//		}
		
		
		
		public static double[] getBarycentricCoordinates(ArrayList<double[]> points, double[] p){
			double[] p0 = points.get(0);
			double[] p1 = points.get(1);
			double[] p2 = points.get(2);
			double[] p3 = points.get(3);
			Matrix T = new Matrix(p0[0] - p3[0], p1[0] - p3[0], p2[0] - p3[0], 0, 
								  p0[1] - p3[1], p1[1] - p3[1], p2[1] - p3[1], 0,
								  p0[2] - p3[2], p1[2] - p3[2], p2[2] - p3[2], 0, 
								  0, 0, 0, 1);
//			if(T.getDeterminant() != 0){
//				System.out.println("full rank");
//				T.invert();
//			}
			System.out.println(T.toString());
			T.invert();
			System.out.println(T.toString());
			System.out.println();
			System.out.println(" getBarycentricCoordinates");
			System.out.println();
			double[] lambda = T.multiplyVector(Rn.subtract(null, p, p3));
			lambda[3] = 1 - lambda[0] - lambda[1] - lambda[2];
			System.out.println("lambda " + Arrays.toString(lambda));
			return lambda;
		}
		
		public static double[] getCoordsFromBasis(ArrayList<double[]> points, double[] p){
			double[] p0 = points.get(0);
			double[] p1 = points.get(1);
			double[] p2 = points.get(2);
			Matrix T = new Matrix(p0[0], p1[0], p2[0], 0, 
					  			p0[1], p1[1], p2[1], 0,
					  			p0[2], p1[2], p2[2], 0, 
					  			0, 0, 0, 1);
			System.out.println(T.toString());
			T.invert();
			System.out.println(T.toString());
			double[] lambda = T.multiplyVector(p);
//			double[] lambda = new double[3];
			
			return lambda;
		}
		
		public static void main(String[] args){
			Matrix T = new Matrix(1, 2, 3, 0, 4, 5, 6, 0, 7, 8, 9, 0, 0, 0, 0, 1);
			System.out.println(T.toString());
			T.invert();
			System.out.println();
			System.out.println(T.toString());
			
			
		}
		
}
