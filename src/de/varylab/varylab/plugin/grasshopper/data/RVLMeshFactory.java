package de.varylab.varylab.plugin.grasshopper.data;

import java.io.Reader;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.namespace.QName;

import de.varylab.varylab.plugin.grasshopper.data.binding.RVLMesh;


public class RVLMeshFactory {
	
	private static Logger
		log = Logger.getLogger(RVLMeshFactory.class.getName());
	private static JAXBContext
		jaxbContex = null;
    private final static QName 
    	_RVLMesh_QNAME = new QName("http://schemas.datacontract.org/2004/07/GHVaryLab", "RVLMesh");
	
	static {
		try {
			ClassLoader parentLoader = RVLMeshFactory.class.getClassLoader();
			jaxbContex = JAXBContext.newInstance("de.varylab.varylab.plugin.grasshopper.data.binding", parentLoader);
		} catch (Exception e) {
			log.severe(e.getMessage());
		}
	}
	
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/GHVaryLab", name = "RVLMesh")
    public static JAXBElement<RVLMesh> createRVLMesh(RVLMesh value) {
        return new JAXBElement<RVLMesh>(_RVLMesh_QNAME, RVLMesh.class, null, value);
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
			JAXBElement<RVLMesh> meshElement = createRVLMesh(mesh);
			m.marshal(meshElement, sw);
			return sw.getBuffer().toString();
		} catch (Throwable t) {
			throw new RVLMeshException(t);
		}
	}
	
}
