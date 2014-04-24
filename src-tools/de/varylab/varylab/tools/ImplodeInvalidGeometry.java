package de.varylab.varylab.tools;

import java.io.File;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.plugin.JRViewer;
import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;

public class ImplodeInvalidGeometry {

	public static void main(String[] args) throws Exception {
		ReaderOBJ reader = new ReaderOBJ();
		SceneGraphComponent c = reader.read(new File("/Users/sechel/Dropbox/WandaFacade/paper/models/2surfaces1-minimalstretch_hex.obj"));
		IndexedFaceSet ifs = (IndexedFaceSet)SceneGraphUtility.getFirstGeometry(c);
		ifs = IndexedFaceSetUtility.implode(ifs, -1);
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addContentUI();
		v.setContent(ifs);
		v.startup();
	}

}
