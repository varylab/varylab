package de.varylab.varylab.plugin.grasshopper.data;

import java.io.Reader;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.varylab.varylab.plugin.grasshopper.data.binding.ObjectFactory;
import de.varylab.varylab.plugin.grasshopper.data.binding.RVLMesh;


public class RVLMeshFactory {
	
	private static Logger
		log = Logger.getLogger(RVLMeshFactory.class.getName());
	private static JAXBContext
		jaxbContex = null;
	
	static {
		try {
			ClassLoader parentLoader = RVLMeshFactory.class.getClassLoader();
			jaxbContex = JAXBContext.newInstance("de.varylab.varylab.plugin.grasshopper.data.binding", parentLoader);
		} catch (Exception e) {
			log.severe(e.getMessage());
		}
	}
	
	public static RVLMesh loadRVLMesh(Reader in) throws RVLMeshException {
		try {
			Unmarshaller u = jaxbContex.createUnmarshaller();
			u.setSchema(null);
			@SuppressWarnings("unchecked")
			JAXBElement<RVLMesh> element = (JAXBElement<RVLMesh>)u.unmarshal(in);
			return element.getValue();
		} catch (Throwable t) {
			throw new RVLMeshException(t);
		}
	}
	
	public static String meshToXML(RVLMesh mesh) throws RVLMeshException {
		try {
			Marshaller m = jaxbContex.createMarshaller();
			StringWriter sw = new StringWriter();
			ObjectFactory of = new ObjectFactory();
			JAXBElement<RVLMesh> meshElement = of.createRVLMesh(mesh);
			m.marshal(meshElement, sw);
			return sw.getBuffer().toString();
		} catch (Throwable t) {
			throw new RVLMeshException(t);
		}
	}
	
}
