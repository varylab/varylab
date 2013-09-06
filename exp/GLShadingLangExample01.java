import java.io.IOException;

import de.jreality.geometry.Primitives;
import de.jreality.plugin.JRViewer;
import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

 
public class GLShadingLangExample01 {
 
  public static void main(String[] args) throws IOException  {
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");

    SceneGraphComponent geom = SceneGraphUtility.createFullSceneGraphComponent("world_geom");
    Appearance ap = geom.getAppearance();
    DefaultGeometryShader dgs = (DefaultGeometryShader) 
    ShaderUtility.createDefaultGeometryShader(ap, true);
    dgs.setShowLines(false);
    dgs.setShowPoints(false);
    dgs.createPolygonShader("glsl");
    GlslProgram brickProg = null;		
//    world.setGeometry(SphereUtility.tessellatedIcosahedronSphere(3, true)); 
	ReaderOBJ reader = new ReaderOBJ();
	Input input = Input.getInput(args[0]);
	SceneGraphComponent c = reader.read(input);
	 geom.setGeometry(SceneGraphUtility.getFirstGeometry(c));
	 
	 SceneGraphComponent symm_line = new SceneGraphComponent();
	Appearance ap_line = new Appearance();
	symm_line.setAppearance(ap_line);
	 DefaultGeometryShader dgs_line = (DefaultGeometryShader) 
	 ShaderUtility.createDefaultGeometryShader(ap_line, true);
	 dgs_line.setShowLines(true);
	 dgs_line.setShowPoints(false);

	
	symm_line.setGeometry(Primitives.discreteTorusKnot(23, 7, 20, 30, 100));
	//indexed line set
    try {
      brickProg = new GlslProgram(ap, "polygonShader",   
          Input.getInput("de/jreality/jogl/shader/resources/brick.vert"),
          Input.getInput("de/jreality/jogl/shader/resources/brick.frag")
          );
    } catch (IOException e) {
      e.printStackTrace();
    }
    double[] brickSize = {.2, .25};
    double[] brickPct = {.5, .75};
    double[] mortarPct = new double[2];
    double[] lightPosition = {-2,0,4};
    mortarPct[0] = 1.0 - brickPct[0];
    mortarPct[1] = 1.0 - brickPct[1];
    brickProg.setUniform("SpecularContribution", .5);
    brickProg.setUniform("DiffuseContribution", 1.0);
    brickProg.setUniform("BrickColor", new double[]{.2,0f,.8});
    brickProg.setUniform("MortarColor", new double[] {.8,1f,.2});
    brickProg.setUniform("BrickSize", brickSize);
    brickProg.setUniform("BrickPct", brickPct);
    brickProg.setUniform("MortarPct", mortarPct);
    brickProg.setUniform("LightPosition", lightPosition);   
    
    world.addChild(geom);
    world.addChild(symm_line);
    
    JRViewer.display(world);
  }
}