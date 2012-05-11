package de.varylab.varylab.plugin.meshoptimizer;

import java.io.StringReader;

import de.jtem.halfedgetools.functional.Functional;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.varylab.hds.VEdge;
import de.varylab.varylab.hds.VFace;
import de.varylab.varylab.hds.VHDS;
import de.varylab.varylab.hds.VVertex;
import de.varylab.varylab.math.functional.NURBSSurfaceFunctional;
import de.varylab.varylab.plugin.VarylabOptimizerPlugin;
import de.varylab.varylab.plugin.io.NurbsIO;
import de.varylab.varylab.plugin.nurbs.NURBSSurface;

public class NURBSSurfaceOptimizer extends VarylabOptimizerPlugin {

	private NURBSSurfaceFunctional<VVertex, VEdge, VFace> 
		functional = new NURBSSurfaceFunctional<VVertex, VEdge, VFace>();
	private NURBSSurface
		S = null;
	
	@Override
	public Functional<VVertex, VEdge, VFace> getFunctional(VHDS hds) {
		functional.setNURBSSurface(S);
		return functional;
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		StringReader sphereReader = new StringReader(nurbsSphere);
		S = NurbsIO.readNURBS(sphereReader);
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
	
	
	private String
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
