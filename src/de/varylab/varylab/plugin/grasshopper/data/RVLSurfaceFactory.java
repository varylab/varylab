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

import de.varylab.varylab.plugin.grasshopper.data.binding.RVLSurface;

public class RVLSurfaceFactory {
	
	private static Logger
		log = Logger.getLogger(RVLSurfaceFactory.class.getName());
	private static JAXBContext
		jaxbContex = null;
    private final static QName 
    	_RVLSurface_QNAME = new QName("http://schemas.datacontract.org/2004/07/GHVaryLab", "RVLSurface");
	
	static {
		try {
			ClassLoader parentLoader = RVLSurfaceFactory.class.getClassLoader();
			jaxbContex = JAXBContext.newInstance("de.varylab.varylab.plugin.grasshopper.data.binding", parentLoader);
		} catch (Exception e) {
			log.severe(e.getMessage());
		}
	}
	
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/GHVaryLab", name = "RVLSurface")
    public static JAXBElement<RVLSurface> createRVLSurface(RVLSurface value) {
        return new JAXBElement<RVLSurface>(_RVLSurface_QNAME, RVLSurface.class, null, value);
    }
	
	public static RVLSurface loadRVLSurface(Reader in) throws RVLSurfaceException {
		try {
			Unmarshaller u = jaxbContex.createUnmarshaller();
			u.setSchema(null);
			@SuppressWarnings("unchecked")
			JAXBElement<RVLSurface> element = (JAXBElement<RVLSurface>)u.unmarshal(in);
			return element.getValue();
		} catch (Throwable t) {
			throw new RVLSurfaceException(t);
		}
	}
	
	public static String surfaceToXML(RVLSurface surface) throws RVLSurfaceException {
		try {
			Marshaller m = jaxbContex.createMarshaller();
			StringWriter sw = new StringWriter();
			JAXBElement<RVLSurface> surfaceElement = createRVLSurface(surface);
			m.marshal(surfaceElement, sw);
			return sw.getBuffer().toString();
		} catch (Throwable t) {
			throw new RVLSurfaceException(t);
		}
	}
	
}
