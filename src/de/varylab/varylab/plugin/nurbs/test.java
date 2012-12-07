package de.varylab.varylab.plugin.nurbs;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSlider;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ContentType;

import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.ViewShrinkPanelPlugin;
import de.jreality.plugin.content.ContentTools;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.tutorial.util.TextSlider;
import de.jreality.util.SceneGraphUtility;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.varylab.discreteconformal.unwrapper.SphericalNormalizerPETScOld;

public class test {
	
	public static double[][] interpolateCurve(double[][] ct, double[][] c0, double[][] c1, double t, boolean correct){
		double[] edgeLength = interpolatedEdgeLength(c0, c1, t);
		double[] angles =  interpolateAngles(c0, c1, t);
		double[] start = c0[0];
		if (c0[0].length == 3) c0 = Pn.homogenize(null, c0);
		int n = c0.length, m = c0[0].length;
		ct = new double[n + 1][m];
		ct[0][0] = start[0];
		ct[0][1] = start[1];
		ct[0][2] = .0;
		if (m > 3) ct[0][3] = 1.0;
		double[] end = new double[m];
		for (int i = 1; i < n; i++) {
			end[0] = start[0] + Math.cos(angles[i - 1]) * edgeLength[i - 1];
			end[1] = start[1] + Math.sin(angles[i - 1]) * edgeLength[i - 1];
			ct[i][0] = end[0];
			ct[i][1] = end[1];
			ct[i][2] = 0;
			if (m > 3) ct[i][3] = 1.0;
			start = end;
		}
		ct[n] = ct[0].clone();
		ct[n][0] = start[0] + Math.cos(angles[n - 1]) * edgeLength[n - 1];
		ct[n][1] = start[1] + Math.sin(angles[n - 1]) * edgeLength[n - 1];
		double[][] edges = curveToEdges(ct,false);
		
		double [] weights = correct ? normalize(edges) : getWeights(edges);
		edges = normEdges(edges);
		ct = edgesToCurve(edges,weights, c0[0]);
//		if (correct) ct = cheapCorrect(ct);
		double[] baryt = Rn.add(null, Rn.times(null, (1 - t), getBarycenter(c0, true)),  Rn.times(null, t, getBarycenter(c1, true)));
		double[] currentBary = getBarycenter(ct, false);
		double[] diff = Rn.subtract(null, baryt, currentBary);
		for (int i = 0; i < ct.length; i++) {
			Rn.add(ct[i], ct[i], diff);
		}
		double[][] result = new double[ct.length - 1][4];
		for (int i = 0; i < result.length; i++) {
			result[i] = ct[i]; 
		}
		return result;
	}
	
	
	private static double[] getBarycenter(double[][] curve, boolean closed){
		double[] bc = {0.0, 0.0, 0.0, 0.0};
		int n = closed ? curve.length : curve.length-1;
		for (int i = 0; i < n; i++) {
			Rn.add(bc, bc, curve[i]);
		}
		double f = 1.0 / (double)n;
		Rn.times(bc, f, bc);
		return bc;
	}
	
	private static double[][] normEdges(double[][] edges){
		double[][] normE = new double[edges.length][];
		for (int i = 0; i < edges.length; i++) {
			edges[i][3] = 0.0;
			normE[i] = Rn.normalize(null, edges[i]);
			normE[i][3] = 1.0;
			edges[i][3] = 1.0;
		}
		return normE;
	}
	
	private static double[] getWeights(double[][] edges){
		double[] weights = new double[edges.length];
		for (int i = 0; i < weights.length; i++) {
			edges[i][3] = 0.0;
			weights[i] = Rn.euclideanNorm(edges[i]);
			edges[i][3] = 1.0;
		}
		return weights;
	}
	
	private static double[] normalize(double[][] curve){
		try {
			double[] weights = SphericalNormalizerPETScOld.normalize(curve);
			return weights;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
	
	private static double[][] cheapCorrect(double[][] curve)	{
		double[] diff = Rn.subtract(null, curve[0], curve[curve.length-1]);
		Rn.times(diff, 1.0/(curve.length-1), diff);
		for (int i = 0; i<curve.length; ++i)	{
			Rn.add(curve[i], diff, curve[i]);
		}
		return curve;
	}
	private static double[][] curveToEdges(double[][] curve, boolean closed){
		double[][] edges = new double[curve.length - 1][4];
		if(closed){
			edges = new double[curve.length][4];
		}
		for (int i = 0; i < curve.length - 1; i++) {
			Rn.subtract(edges[i], curve[i + 1], curve[i]); 
			edges[i][3] = 1.0;
		}
		if(closed){
			Rn.subtract(edges[curve.length], curve[0], curve[curve.length]); 
			edges[curve.length][3] = 1.0;
		}
		return edges;
	}
	
	private static double[][] edgesToCurve(double[][] edges, double[] weights, double[] startPoint){
		double[][] curve = new double[edges.length + 1][4];
		curve[0][0] = startPoint[0];
		curve[0][1] = startPoint[1];
		curve[0][2] = startPoint[2];
		curve[0][3] = 1.0;
		for (int i = 0; i < edges.length; i++) {
			edges[i][3] = 0.0;
			Rn.add(curve[i + 1], curve[i],Rn.times(null, weights[i], edges[i]));
			curve[i + 1][3] = 1.0;
		}
		return curve;
	}
		
	
	private static double[] interpolatedEdgeLength(double[][] c0, double[][] c1, double t){
		double[] edgeLength = new double[c0.length];
		for (int i = 0; i < edgeLength.length - 1; i++) {
			edgeLength[i] = (1 - t) * Rn.euclideanDistance(c0[i], c0[i + 1]) + t * Rn.euclideanDistance(c1[i], c1[i + 1]);
		}
		edgeLength[edgeLength.length - 1] = (1 - t) * Rn.euclideanDistance(c0[edgeLength.length - 1], c0[0]) + t * Rn.euclideanDistance(c1[edgeLength.length - 1], c1[0]);
		return edgeLength;
	}
	
	private static double[] interpolateAngles(double[][] c0, double[][] c1, double t){
		double[] angles = new double[c0.length];
		for (int i = 0; i < angles.length - 1; i++) {
			double[] start = Rn.subtract(null, c0[i + 1], c0[i]);
			double[] end = Rn.subtract(null, c1[i + 1], c1[i]);
			double startAngle = Math.atan2(start[1], start[0]);
			double endAngle = Math.atan2(end[1], end[0]);
			if(startAngle - endAngle < -Math.PI){
				startAngle = startAngle + 2 * Math.PI;
			}
			else if(startAngle - endAngle > Math.PI){
				endAngle = endAngle + 2 * Math.PI;
			}
			angles[i] = (1 - t) * startAngle + t * endAngle;
		}
		double[] start = Rn.subtract(null, c0[0], c0[angles.length - 1]);
		double[] end = Rn.subtract(null, c1[0], c1[angles.length - 1]);
		angles[angles.length - 1] = (1 - t) * Math.atan2(start[1], start[0]) + t * Math.atan2(end[1], end[0]);
		return angles;
	}
	
	static double time = .5;
	public static void main(String[] argv)	{
		final IndexedLineSet ils = IndexedLineSetUtility.circle(8);
		final double[][] sqverts = {{2,0,0,1},{1,1,0,1},{0,1,0,1},{-1,10,0,1},{-2,0,0,1},{-2,-2,0,1},{0,-2,0,1},{3,-3,0,1}};
		double[][] sqverts1 = {{1,0,0,1},{1,1,0,1},{0,1,0,1},{-1,1,0,1},{-1,0,0,1},{-1,-1,0,1},{0,-1,0,1},{1,-1,0,1}};
		IndexedLineSet square = IndexedLineSetUtility.createCurveFromPoints(sqverts, true);
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		final SceneGraphComponent children[] = new SceneGraphComponent[4];
		for (int i= 0; i<children.length; ++i)	{
			children[i] = SceneGraphUtility.createFullSceneGraphComponent("child"+i);
			world.addChild(children[i]);
		}
		children[0].setGeometry(ils);
		children[0].getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.red);
		children[1].setGeometry(square);
		children[1].getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", Color.blue);
		doInterpolate(ils, sqverts, children, time);
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.registerPlugin(new ContentTools());
//		v.addContentSupport(ContentType.TerrainAligned);
//		v.addVRSupport();
		
		//final JSlider slider = new JSliderVR(0, 1000, 100);
		final TextSlider.Double slider = new TextSlider.Double("t", JSlider.HORIZONTAL, 0.0, 1.0, time);
		slider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				time = slider.getValue().doubleValue();
				doInterpolate(ils, sqverts, children, time);
			}
		});
		
		ViewShrinkPanelPlugin plugin = new ViewShrinkPanelPlugin() {
			@Override
			public PluginInfo getPluginInfo() {
				return new PluginInfo("t Slider");
			}
		};
		plugin.getShrinkPanel().setLayout(new GridBagLayout());
		plugin.getShrinkPanel().add(slider);
		v.registerPlugin(plugin);
		v.setContent(world);
		
		v.startup();
		
		}

	private static void doInterpolate(IndexedLineSet ils, double[][] sqverts,
			SceneGraphComponent[] children, double t) {
		double[][] ct = interpolateCurve(null, 
				ils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null), sqverts, t, false);
		IndexedLineSet interp = IndexedLineSetUtility.createCurveFromPoints(ct, true);
		children[2].setGeometry(interp);
//		children[2].getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", 
//				AnimationUtility.linearInterpolation(Color.red, Color.black, t));
		ct = interpolateCurve(null, 
				ils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null), sqverts, t, true);
		interp = IndexedLineSetUtility.createCurveFromPoints(ct, true);
		children[3].setGeometry(interp);
		children[3].getAppearance().setAttribute("lineShader.polygonShader.diffuseColor", 
				Color.green);
	}

}
