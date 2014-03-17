package de.varylab.opennurbs;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import de.jreality.util.NativePathUtility;

public class FileDumpTest {
	
	public static void main(String[] args) {
		NativePathUtility.set("native");
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Import Rhino 3dm File");
		int result = chooser.showOpenDialog(null);
		if (result != JFileChooser.APPROVE_OPTION) return;
		File file = chooser.getSelectedFile();
		
		ONX_Model model = OpenNurbsIO.readFile(file.getPath());
		model.dump();
		ArrayList<ONX_Model_Object> m_object_table = model.get_object_table();
		
		for(ONX_Model_Object mo : m_object_table) {
			ON_Object object = mo.get_object();
//			System.out.println(object.getObjectType());
			if(object.isKindOfON_Geometry()) {
				ON_Geometry geom = ON_Geometry.Cast(object);
				if(geom.hasBrepForm()) {
//					System.out.println("\tHas brep form!");
					ON_Brep brep = geom.brepForm(null);
					ON_Surface[] surfaces = brep.getS();
					for (int i = 0; i < surfaces.length; i++) {
						System.out.println("Number of surfaces found: " + surfaces.length);
						ON_NurbsSurface nsurf = ON_NurbsSurface.Cast(surfaces[i]);
						if(nsurf != null) {
//							System.out.println("\t\tNurbsSurface found!");
							nsurf.Dump();
//							NURBSSurface vnsurf = nsurf.getVNurbsSurfac();
//							System.err.println(vnsurf.toString());
//							return;
						}
					}
				}
			}
		}
	}
	
}
