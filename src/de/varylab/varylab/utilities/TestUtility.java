package de.varylab.varylab.utilities;

import java.io.IOException;

import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.varylab.varylab.halfedge.VHDS;
import de.varylab.varylab.halfedge.adapter.VPositionAdapter;
import de.varylab.varylab.halfedge.adapter.VTexturePositionAdapter;

public class TestUtility {

	public static VHDS readOBJ(Class<?> root, String name) {
		ReaderOBJ reader = new ReaderOBJ();
		SceneGraphComponent c = null;
		IndexedFaceSet ifs = null;
		try {
			Input in = new Input("Obj File", root.getResourceAsStream(name));
			c =reader.read(in);
			ifs = (IndexedFaceSet)c.getChildComponent(0).getGeometry();
			ConverterJR2Heds converter = new ConverterJR2Heds();
			VHDS hds = new VHDS();
			AdapterSet a = new AdapterSet();
			a.add(new VPositionAdapter());
			a.add(new VTexturePositionAdapter());
			converter.ifs2heds(ifs, hds, a, null);
			return hds;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
