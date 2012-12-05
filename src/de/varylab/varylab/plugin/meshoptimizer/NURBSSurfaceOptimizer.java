package de.varylab.varylab.plugin.meshoptimizer;

import static de.jreality.scene.Appearance.DEFAULT;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static java.awt.Color.ORANGE;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.jreality.scene.Appearance;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.flavor.UIFlavor;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.functional.NURBSSurfaceFunctional;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;
import de.varylab.varylab.plugin.io.NurbsIO;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;

public class NURBSSurfaceOptimizer extends VarylabOptimizerPlugin implements ActionListener ,UIFlavor{

	private NURBSSurfaceFunctional<VVertex, VEdge, VFace> 
		functional = new NURBSSurfaceFunctional<VVertex, VEdge, VFace>();
	
	private NURBSSurface
		S = null;
	
	private JPanel
		panel = new JPanel();
	
	private JCheckBox
		showLayerChecker = new JCheckBox("Show Surface"),
		wireFrameChecker = new JCheckBox("Wireframe");
	
	private Appearance
		refSurfaceAppearance = new Appearance("Reference Surface Appearance");
	
	private HalfedgeLayer
		refSurfaceLayer = null; 

	private HalfedgeInterface 
		hif = null;
	
	public NURBSSurfaceOptimizer() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.insets = new Insets(1,1,1,1);
		c1.fill = GridBagConstraints.BOTH;
		c1.anchor = GridBagConstraints.WEST;
		c1.weightx = 1.0;
		c1.gridwidth = 1;
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets = new Insets(1,1,1,1);
		c2.fill = GridBagConstraints.BOTH;
		c2.anchor = GridBagConstraints.WEST;
		c2.weightx = 1.0;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(showLayerChecker,c1);
		panel.add(wireFrameChecker,c2);
		showLayerChecker.addActionListener(this);
		wireFrameChecker.addActionListener(this);
	}
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		if (S == null) {
			StringReader sphereReader = new StringReader(nurbsSphere);
			S = NurbsIO.readNURBS(sphereReader);	
		}
		functional.setNURBSSurface(S);
		return functional;
	}

	@Override
	public JPanel getOptionPanel() {
		return panel;
	}

	@Override
	public String getName() {
		return "NURBS Reference Surface";
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("NURBS Reference Surface", "Varylab Group");
		return info;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		hif.checkContent();
		updateStates();
	}
	
	private void updateStates() {
		refSurfaceAppearance.setAttribute(FACE_DRAW, !wireFrameChecker.isSelected());
		refSurfaceAppearance.setAttribute(EDGE_DRAW, wireFrameChecker.isSelected());
		refSurfaceAppearance.setAttribute(VERTEX_DRAW, false);
		refSurfaceAppearance.setAttribute(LINE_SHADER + "." + TUBES_DRAW, false);
		refSurfaceAppearance.setAttribute(TRANSPARENCY_ENABLED, true);
		refSurfaceAppearance.setAttribute(POLYGON_SHADER + "." + TRANSPARENCY, 0.3);
		refSurfaceAppearance.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, ORANGE);
		refSurfaceAppearance.setAttribute(POLYGON_SHADER + "." + TEXTURE_2D, DEFAULT); 
		refSurfaceAppearance.setAttribute(LINE_SHADER + "." + TRANSPARENCY, 0.3);
		refSurfaceAppearance.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, ORANGE);
		refSurfaceLayer.setAppearance(refSurfaceAppearance);
		wireFrameChecker.setEnabled(showLayerChecker.isSelected());
		if (showLayerChecker.isSelected()) {
			hif.addLayer(refSurfaceLayer);
		} else {
			hif.removeLayer(refSurfaceLayer);
		}
	}
	
	
	private String
//	nurbsTest =
//	"v -16.84591428200644 9.165210069137606 0\n" +
//	"v -15.36140842573768 3.743536507112545 -5.35712982914381\n" +
//	"v -14.52233989828141 -1.484505856268766 3.743536507112545\n" +
//	"v -18.26587640539396 -5.550761027787562 0\n" +
//	"v -8.84249140473135 12.39239671320014 10.06882232947512\n" +
//	"v -6.454373288125074 4.711692500331306 6.045596313210488\n" +
//	"v -4.066255171518796 -1.871768253556271 4.582605034568804\n" +
//	"v -8.455229007443846 -8.197054075918842 0\n" +
//	"v 6.389829555243823 12.39239671320014 0\n" +
//	"v 10.56365761489803 4.973453194794152 1.742680787793771\n" +
//	"v 10.28396810574596 -2.78972356564517 -5.851965114566728\n" +
//	"v 7.938879144393841 -8.00342287727509 -6.841635685412578\n" +
//	"v 17.1686329464127 7.938879144393843 0\n" +
//	"v 18.65313880268147 0.839068527456261 8.713403938968853\n" +
//	"v 18.65313880268147 -1.419962123387515 -3.808080239993792\n" +
//	"v 17.62043907658145 -7.164354349818831 0\n" +
//	"cstype bspline\n" +
//	"deg 3 3\n" +
//	"surf 0 1 0 1 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16\n" +
//	"parm u 0 0 0 0 1 1 1 1\n" +
//	"parm v 0 0 0 0 1 1 1 1\n" +
//	"trim -18.96954805532564 -0 1 0.4664667256550173 1 2 0 0.6918471468526943 3 \\n" +
//	"-20.57285388576444 -0 4 0.6731328266466173 1 5 0 0.4742678212551149 6\n" +
//	"end\n" + "";
	
	
		nurbsSphere = "# Rhino\n" + 
				"\n" + 
				"# - trim loop\n" + 
				"vp 0 -1.570796326794897\n" + 
				"vp 6.283185307179586 -1.570796326794897\n" + 
				"cstype bspline\n" + 
				"deg 1\n" + 
				"curv2 1 2\n" + 
				"parm u 0 0 6.283185307179586 6.283185307179586\n" + 
				"end\n" + 
				"vp 6.283185307179586 -1.570796326794897\n" + 
				"vp 6.283185307179586 1.570796326794897\n" + 
				"cstype bspline\n" + 
				"deg 1\n" + 
				"curv2 3 4\n" + 
				"parm u -1.570796326794897 -1.570796326794897 1.570796326794897 1.570796326794897\n" + 
				"end\n" + 
				"vp 6.283185307179586 1.570796326794897\n" + 
				"vp 0 1.570796326794897\n" + 
				"cstype bspline\n" + 
				"deg 1\n" + 
				"curv2 5 6\n" + 
				"parm u 0 0 6.283185307179586 6.283185307179586\n" + 
				"end\n" + 
				"vp 0 1.570796326794897\n" + 
				"vp 0 -1.570796326794897\n" + 
				"cstype bspline\n" + 
				"deg 1\n" + 
				"curv2 7 8\n" + 
				"parm u -1.570796326794897 -1.570796326794897 1.570796326794897 1.570796326794897\n" + 
				"end\n" + 
				"v 0 0 -1 1\n" + 
				"v 0 0 -1 0.7071067811865476\n" + 
				"v 0 0 -1 1\n" + 
				"v 0 0 -1 0.7071067811865476\n" + 
				"v 0 0 -1 1\n" + 
				"v 0 0 -1 0.7071067811865476\n" + 
				"v 0 0 -1 1\n" + 
				"v 0 0 -1 0.7071067811865476\n" + 
				"v 0 0 -1 1\n" + 
				"v 0.9999999999999999 0 -1 0.7071067811865476\n" + 
				"v 0.9999999999999997 0.9999999999999994 -1 0.5000000000000001\n" + 
				"v 6.123233995736765e-17 0.9999999999999999 -1 0.7071067811865476\n" + 
				"v -0.9999999999999994 0.9999999999999997 -1 0.5000000000000001\n" + 
				"v -0.9999999999999999 1.224646799147353e-16 -1 \\\n" + 
				" 0.7071067811865476\n" + 
				"v -0.9999999999999997 -0.9999999999999994 -1 0.5000000000000001\n" + 
				"v -1.836970198721029e-16 -0.9999999999999999 -1 \\\n" + 
				" 0.7071067811865476\n" + 
				"v 0.9999999999999993 -0.9999999999999997 -1 0.5000000000000001\n" + 
				"v 0.9999999999999999 0 -1 0.7071067811865476\n" + 
				"v 1 0 -6.123233995736766e-17 1\n" + 
				"v 1 0.9999999999999999 -6.123233995736766e-17 0.7071067811865476\n" + 
				"v 6.123233995736766e-17 1 -6.123233995736766e-17 1\n" + 
				"v -0.9999999999999999 1 -6.123233995736766e-17 \\\n" + 
				" 0.7071067811865476\n" + 
				"v -1 1.224646799147353e-16 -6.123233995736766e-17 1\n" + 
				"v -1 -0.9999999999999999 -6.123233995736766e-17 \\\n" + 
				" 0.7071067811865476\n" + 
				"v -1.83697019872103e-16 -1 -6.123233995736766e-17 1\n" + 
				"v 0.9999999999999997 -1 -6.123233995736766e-17 \\\n" + 
				" 0.7071067811865476\n" + 
				"v 1 0 -6.123233995736766e-17 1\n" + 
				"v 1 0 0.9999999999999999 0.7071067811865476\n" + 
				"v 1 0.9999999999999997 0.9999999999999997 0.5000000000000001\n" + 
				"v 6.123233995736766e-17 1 0.9999999999999999 0.7071067811865476\n" + 
				"v -0.9999999999999997 1 0.9999999999999997 0.5000000000000001\n" + 
				"v -1 1.224646799147353e-16 0.9999999999999999 0.7071067811865476\n" + 
				"v -1 -0.9999999999999997 0.9999999999999997 0.5000000000000001\n" + 
				"v -1.83697019872103e-16 -1 0.9999999999999999 0.7071067811865476\n" + 
				"v 0.9999999999999996 -1 0.9999999999999997 0.5000000000000001\n" + 
				"v 1 0 0.9999999999999999 0.7071067811865476\n" + 
				"v 0 0 1 1\n" + 
				"v 0 0 1 0.7071067811865476\n" + 
				"v 0 0 1 1\n" + 
				"v 0 0 1 0.7071067811865476\n" + 
				"v 0 0 1 1\n" + 
				"v 0 0 1 0.7071067811865476\n" + 
				"v 0 0 1 1\n" + 
				"v 0 0 1 0.7071067811865476\n" + 
				"v 0 0 1 1\n" + 
				"cstype rat bspline\n" + 
				"deg 2 2\n" + 
				"surf 0 6.283185307179586 -1.570796326794897 1.570796326794897 1 2 \\\n" + 
				" 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 \\\n" + 
				" 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45\n" + 
				"parm u 0 0 0 1.570796326794897 1.570796326794897 3.141592653589793 \\\n" + 
				" 3.141592653589793 4.71238898038469 4.71238898038469 6.283185307179586 \\\n" + 
				" 6.283185307179586 6.283185307179586\n" + 
				"parm v -1.570796326794897 -1.570796326794897 -1.570796326794897 0 \\\n" + 
				" 0 1.570796326794897 1.570796326794897 1.570796326794897\n" + 
				"trim 0 6.283185307179586 1 -1.570796326794897 1.570796326794897 2 \\\n" + 
				" 0 6.283185307179586 3 -1.570796326794897 1.570796326794897 4\n" + 
				"end\n" + 
				"";

}
