package de.varylab.varylab.plugin.nurbs.plugin;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.basic.View;
import de.jreality.scene.Appearance;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position4d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.varylab.plugin.interaction.DraggablePointComponent;
import de.varylab.varylab.plugin.nurbs.adapter.NurbsWeightAdapter;

public class SurfaceProjectionPlugin extends ShrinkPanelPlugin implements ActionListener {

	private JButton
		pointButton = new JButton("create point");
	
	private HalfedgeInterface 
		hif = null;

	private NurbsManagerPlugin 
		nurbsManager = null;
	
	public SurfaceProjectionPlugin() {
		shrinkPanel.setShrinked(true);
		shrinkPanel.setLayout(new GridBagLayout());
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		shrinkPanel.add(pointButton, rc);
		pointButton.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e){
		
		if(pointButton == e.getSource()){
			double[] point = computeBarycenter(); 
			double[] surfacePoint = nurbsManager.getActiveNurbsSurface().getClosestPoint(point);
			
			DraggablePointComponent draggablePoint = new DraggablePointComponent(point);
			final SceneGraphComponent projectedPoint = new SceneGraphComponent("Projected point");
			
			draggablePoint.addPointDragListener(new PointDragListener() {
				
				@Override
				public void pointDragged(PointDragEvent e) {
					double[] p = new double[]{e.getX(), e.getY(), e.getZ(), 1.0};
					double[] pp = nurbsManager.getActiveNurbsSurface().getClosestPoint(p);
					PointSet ps = (PointSet) projectedPoint.getGeometry();
					ps.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Inlined(pp,4));
				}
				
				@Override
				public void pointDragStart(PointDragEvent e) {
				}
				
				@Override
				public void pointDragEnd(PointDragEvent e) {
				}
			});

			PointSetFactory psfi = new PointSetFactory();
			psfi.setVertexCount(1);
			psfi.update();
			psfi.setVertexCoordinates(surfacePoint);
			psfi.update();
			
			projectedPoint.setGeometry(psfi.getGeometry());
			Appearance iAp = new Appearance();
			projectedPoint.setAppearance(iAp);
			DefaultGeometryShader idgs = ShaderUtility.createDefaultGeometryShader(iAp, false);
			DefaultPointShader ipointShader = (DefaultPointShader)idgs.getPointShader();
			ipointShader.setDiffuseColor(Color.orange);
			draggablePoint.addChild(projectedPoint);
			hif.getActiveLayer().addTemporaryGeometry(draggablePoint);
		}
	}
	
	private double[] computeBarycenter() {
		double[] barycenter = new double[4];
		AdapterSet as = hif.getAdapters();
		for(Vertex<?,?,?> v : hif.get().getVertices()) {
			Rn.add(barycenter, barycenter, Pn.dehomogenize(null, as.getD(Position4d.class, v)));
		}
		return barycenter;
	}

	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addAdapter(new NurbsWeightAdapter(), true);
		nurbsManager = c.getPlugin(NurbsManagerPlugin.class);
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
}
