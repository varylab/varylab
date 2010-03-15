package de.varylab.varylab.math.geom3d;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static java.awt.Color.BLACK;
import static java.awt.Color.ORANGE;
import static java.awt.Color.WHITE;

import java.awt.Color;
import java.util.HashSet;

import de.jreality.scene.Appearance;
public class AppearanceContext {
	
	private static final AppearanceContext 
		defaultContext= new AppearanceContext();
	
	private double scale = 1;
	
	private Color 
		pointColor = BLACK,
		lineColor = BLACK,
		vectorColor = ORANGE,
		circleColor = WHITE;
	
	
	private Appearance 
		pointAppearance = new Appearance(),
		circleAppearance = new Appearance(),
		lineAppearance = new Appearance(),
		vectorAppearance = new Appearance();
		
		
	
	private HashSet<JRGeom3D> objects = new HashSet<JRGeom3D>();

	public AppearanceContext(){
		buildPointAppearance(1);
		buildCircleAppearance(1);
		buildLineAppearance(1);
		buildPlaneAppearance();
		buildVectorAppearance(1);
		buildSphereAppearance();
		buildTriangleAppearance();
		
	}

	public AppearanceContext(double scale){
		this.scale = scale;
		buildPointAppearance(scale);
		buildCircleAppearance(scale);
		buildLineAppearance(scale);
		buildPlaneAppearance();
		buildVectorAppearance(scale);
		buildSphereAppearance();
		buildTriangleAppearance();
		this.scale = scale;
	}
	
	
	private void buildTriangleAppearance() {
		// TODO Auto-generated method stub
		
	}

	public AppearanceContext getDefault(){
		return defaultContext;
	}

	

	public void setPointAppearance(Appearance pointAppearance) {
		this.pointAppearance = pointAppearance;
	}

	

	public void setCircleAppearance(Appearance circleAppearance) {
		this.circleAppearance = circleAppearance;
	}

	
	

	

	public void setLineAppearance(Appearance lineAppearance) {
		this.lineAppearance = lineAppearance;
	}

	
	public void setVectorAppearance(Appearance vectorAppearance) {
		this.vectorAppearance = vectorAppearance;
	}

	

	protected Appearance getPointAppearance() {
		return pointAppearance;
	}

	protected Appearance getCircleAppearance() {
		return circleAppearance;
	}

	protected Appearance getLineAppearance() {
		return lineAppearance;
	}

	public Appearance getVectorAppearance() {
		return vectorAppearance;
	}


	private void buildVectorAppearance(double scale) {
		vectorAppearance.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR,vectorColor );
		vectorAppearance.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, vectorColor);
		vectorAppearance.setAttribute(VERTEX_DRAW, true);
		vectorAppearance.setAttribute(FACE_DRAW, false);
		vectorAppearance.setAttribute(EDGE_DRAW, true);
		vectorAppearance.setAttribute(SPHERES_DRAW, true);
		vectorAppearance.setAttribute(TUBES_DRAW, true);
		vectorAppearance.setAttribute(POINT_RADIUS, 0.05*scale);
		vectorAppearance.setAttribute(TUBE_RADIUS, 0.05*scale);
	}

	private void buildSphereAppearance() {
		// TODO Auto-generated method stub
		
	}

	

	private void buildPlaneAppearance() {
		// TODO Auto-generated method stub
		
	}

	private void buildLineAppearance(double scale) {
		lineAppearance.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, lineColor);
		lineAppearance.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, lineColor);
		lineAppearance.setAttribute(VERTEX_DRAW, true);
		lineAppearance.setAttribute(FACE_DRAW, false);
		lineAppearance.setAttribute(EDGE_DRAW, true);
		lineAppearance.setAttribute(SPHERES_DRAW, true);
		lineAppearance.setAttribute(TUBES_DRAW, true);
		lineAppearance.setAttribute(POINT_RADIUS, 0.05*scale);
		lineAppearance.setAttribute(TUBE_RADIUS, 0.05*scale);
	}

	private void buildCircleAppearance(double scale) {
		circleAppearance.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, circleColor);
		circleAppearance.setAttribute(POINT_SHADER + "." + SMOOTH_SHADING, true);
		circleAppearance.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, circleColor);
		circleAppearance.setAttribute(LINE_SHADER + "." + SMOOTH_SHADING, true);
		circleAppearance.setAttribute(VERTEX_DRAW, true);
		circleAppearance.setAttribute(EDGE_DRAW, true);
		circleAppearance.setAttribute(FACE_DRAW, false);
		circleAppearance.setAttribute(SPHERES_DRAW, true);
		circleAppearance.setAttribute(TUBES_DRAW, true);
		circleAppearance.setAttribute(POINT_RADIUS, 0.01*scale);
		circleAppearance.setAttribute(TUBE_RADIUS, 0.01*scale);
	}

	private void buildPointAppearance(double scale) {
		pointAppearance.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR,pointColor);
		pointAppearance.setAttribute(POINT_SHADER + "." + SMOOTH_SHADING, true);
		pointAppearance.setAttribute(SPHERES_DRAW, true);
		pointAppearance.setAttribute(VERTEX_DRAW, true);
		pointAppearance.setAttribute(POINT_RADIUS, 0.2*scale);
	}
 public void setPointSize(double scale){
	 pointAppearance.setAttribute(POINT_RADIUS, 0.2*scale);
 }
	public HashSet<JRGeom3D> getObjects() {
		return objects;
	}

	public void setObjects(HashSet<JRGeom3D> objects) {
		this.objects = objects;
	}
	

	public static AppearanceContext getDefaultContext() {
		return defaultContext;
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
		buildPointAppearance(scale);
		buildCircleAppearance(scale);
		buildLineAppearance(scale);
		buildVectorAppearance(scale);
	}

	public Color getPointColor() {
		return pointColor;
	}

	public void setPointColor(Color pointColor) {
		this.pointColor = pointColor;
		buildPointAppearance(scale);
	}

	public Color getLineColor() {
		return lineColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
		buildLineAppearance(scale);
	}

	public Color getVectorColor() {
		return vectorColor;
	}

	public void setVectorColor(Color vectorColor) {
		this.vectorColor = vectorColor;
		buildVectorAppearance(scale);
	}

	public Color getCircleColor() {
		return circleColor;
	}

	public void setCircleColor(Color circleColor) {
		this.circleColor = circleColor;
		buildCircleAppearance(scale);
	}

}
