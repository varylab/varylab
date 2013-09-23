package de.varylab.varylab.plugin.blender.data;

import java.io.Reader;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.varylab.varylab.plugin.blender.data.binding.BlenderMesh;
import de.varylab.varylab.plugin.blender.data.binding.ObjectFactory;

public class BlenderMeshFactory {
	
	private static Logger
		log = Logger.getLogger(BlenderMeshFactory.class.getName());
	private static JAXBContext
		jaxbContex = null;
	
	static {
		try {
			ClassLoader parentLoader = BlenderMeshFactory.class.getClassLoader();
			jaxbContex = JAXBContext.newInstance("de.varylab.varylab.plugin.blender.data.binding", parentLoader);
		} catch (Exception e) {
			log.severe(e.getMessage());
		}
	}
	
	public static BlenderMesh loadBlenderMesh(Reader in) throws BlenderMeshException {
		try {
			Unmarshaller u = jaxbContex.createUnmarshaller();
			u.setSchema(null);
			@SuppressWarnings("unchecked")
			JAXBElement<BlenderMesh> element = (JAXBElement<BlenderMesh>)u.unmarshal(in);
			return element.getValue();
		} catch (Throwable t) {
			throw new BlenderMeshException(t);
		}
	}
	
	public static String meshToXML(BlenderMesh mesh) throws BlenderMeshException {
		try {
			Marshaller m = jaxbContex.createMarshaller();
			StringWriter sw = new StringWriter();
			ObjectFactory of = new ObjectFactory();
			JAXBElement<BlenderMesh> meshElement = of.createBlenderMesh(mesh);
			m.marshal(meshElement, sw);
			return sw.getBuffer().toString();
		} catch (Throwable t) {
			throw new BlenderMeshException(t);
		}
	}
	
}
