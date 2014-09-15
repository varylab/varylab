package de.varylab.varylab.plugin.nurbs.plugin;

import java.awt.Color;
import java.util.logging.Logger;

import de.jreality.geometry.PointSetFactory;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.job.JobMonitorPlugin;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;
import de.jtem.halfedgetools.JRHalfedgeViewer;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.halfedgetools.plugin.misc.VertexEditorPlugin;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.plugin.VarylabMain;
import de.varylab.varylab.plugin.generator.QuadMeshGenerator;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsUVAdapter;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsWeightAdapter;
import de.varylab.varylab.plugin.nurbs.algorithm.ComputeVectorFields;
import de.varylab.varylab.plugin.nurbs.algorithm.ExtractControlMesh;
import de.varylab.varylab.plugin.nurbs.algorithm.LinearDeformation;
import de.varylab.varylab.plugin.nurbs.algorithm.NgonNurbsGenerator;
import de.varylab.varylab.plugin.nurbs.algorithm.NurbsSurfaceFromMesh;
import de.varylab.varylab.plugin.nurbs.algorithm.ProjectToNurbsSurface;
import de.varylab.varylab.plugin.nurbs.algorithm.SplitAtEdge;
import de.varylab.varylab.plugin.nurbs.algorithm.SplitInTheMiddle;
import de.varylab.varylab.plugin.nurbs.algorithm.StretchXYZ;
import de.varylab.varylab.plugin.nurbs.algorithm.UVUnroll;
import de.varylab.varylab.startup.StaticSetup;

public class NurbsManagerPlugin extends Plugin implements HalfedgeListener {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(NurbsManagerPlugin.class.getName());
	
	private HalfedgeInterface 
		hif = null;

	private NURBSSurface activeNurbsSurface;

	public NurbsManagerPlugin() {
	}



	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		JRHalfedgeViewer.initHalfedgeFronted();
		hif.addAdapter(new NurbsWeightAdapter(), true);
		hif.addHalfedgeListener(this);
		
		c.getPlugin(VarylabMain.class);
		
		c.getPlugin(NurbsIOPlugin.class);
		c.getPlugin(IntegralCurvesPlugin.class);
		c.getPlugin(SurfaceProjectionPlugin.class);
		c.getPlugin(GeodesicsPlugin.class);
		
		c.getPlugin(ExtractControlMesh.class);
		c.getPlugin(NurbsSurfaceFromMesh.class);
		c.getPlugin(ProjectToNurbsSurface.class);
		
		c.getPlugin(VertexEditorPlugin.class);
		c.getPlugin(QuadMeshGenerator.class);
		c.getPlugin(ComputeVectorFields.class);
		c.getPlugin(UVUnroll.class);
		c.getPlugin(SplitInTheMiddle.class);
		c.getPlugin(SplitAtEdge.class);
		c.getPlugin(StretchXYZ.class);
		c.getPlugin(LinearDeformation.class);
		c.getPlugin(NgonNurbsGenerator.class);
		
		c.getPlugin(JobMonitorPlugin.class);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Nurbs Manager", "Nurbs Team");
		return info;
	}

	@SuppressWarnings("unused")
	private void addUmbilicalPoints(double[][] umbillicPoints, HalfedgeLayer layer) {
		PointSetFactory psfu = new PointSetFactory();
	
		psfu.setVertexCount(umbillicPoints.length);
	
		psfu.setVertexCoordinates(umbillicPoints);
		psfu.update();
		SceneGraphComponent sgcu = new SceneGraphComponent("Umbilics");
		SceneGraphComponent umbilicComp = new SceneGraphComponent("Max Curve");
		sgcu.addChild(umbilicComp);
		sgcu.setGeometry(psfu.getGeometry());
		Appearance uAp = new Appearance();
		sgcu.setAppearance(uAp);
		DefaultGeometryShader udgs = ShaderUtility.createDefaultGeometryShader(uAp, false);
		DefaultPointShader upointShader = (DefaultPointShader)udgs.getPointShader();
		upointShader.setDiffuseColor(Color.GREEN);
		layer.addTemporaryGeometry(sgcu);
		
	}

	private void updateActiveNurbsSurface(HalfedgeLayer layer) {
		AdapterSet as = layer.getAdapters();
		as.addAll(layer.getVolatileAdapters());
		NurbsUVAdapter nurbsUVAdapter = as.query(NurbsUVAdapter.class);
		if(nurbsUVAdapter == null) {
			nurbsUVAdapter = layer.getActiveAdapters().query(NurbsUVAdapter.class);
		}
		
		if(nurbsUVAdapter != null) {
			activeNurbsSurface = nurbsUVAdapter.getSurface();
		} else {
			activeNurbsSurface = null;
		}
	}
	
	public static void main(String[] args) {
		StaticSetup.initLogging();
		JRViewer v = new JRViewer();
		v.addContentUI();
		v.addBasicUI();
		v.registerPlugin(new NurbsManagerPlugin());
		v.startup();
	}
	
	@Override
	public void dataChanged(HalfedgeLayer layer) {
		updateActiveNurbsSurface(layer);
		
	}

	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
		updateActiveNurbsSurface(layer);
	}

	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		if(old == active) {
			return;
		}
		updateActiveNurbsSurface(active);
	}

	@Override
	public void layerCreated(HalfedgeLayer layer) {
	}

	@Override
	public void layerRemoved(HalfedgeLayer layer) {
	}

	public NURBSSurface getActiveNurbsSurface() {
		return activeNurbsSurface;
	}
}
