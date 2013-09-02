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

import de.varylab.varylab.plugin.grasshopper.data.binding.RVLLineSet;


public class RVLLineSetFactory {
	
	private static Logger
		log = Logger.getLogger(RVLLineSetFactory.class.getName());
	private static JAXBContext
		jaxbContex = null;
    private final static QName 
    	_RVLLineSet_QNAME = new QName("http://schemas.datacontract.org/2004/07/GHVaryLab", "RVLLineSet");
	
	static {
		try {
			ClassLoader parentLoader = RVLLineSetFactory.class.getClassLoader();
			jaxbContex = JAXBContext.newInstance("de.varylab.varylab.plugin.grasshopper.data.binding", parentLoader);
		} catch (Exception e) {
			log.severe(e.getMessage());
		}
	}
	
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/GHVaryLab", name = "RVLLineSet")
    public static JAXBElement<RVLLineSet> createRVLLineSet(RVLLineSet value) {
        return new JAXBElement<RVLLineSet>(_RVLLineSet_QNAME, RVLLineSet.class, null, value);
    }
	
	public static RVLLineSet loadRVLLineSet(Reader in) throws RVLLineSetException {
		try {
			Unmarshaller u = jaxbContex.createUnmarshaller();
			u.setSchema(null);
			@SuppressWarnings("unchecked")
			JAXBElement<RVLLineSet> element = (JAXBElement<RVLLineSet>)u.unmarshal(in);
			return element.getValue();
		} catch (Throwable t) {
			throw new RVLLineSetException(t);
		}
	}
	
	public static String lineSetToXML(RVLLineSet lineSet) throws RVLLineSetException {
		try {
			Marshaller m = jaxbContex.createMarshaller();
			StringWriter sw = new StringWriter();
			JAXBElement<RVLLineSet> lineSetElement = createRVLLineSet(lineSet);
			m.marshal(lineSetElement, sw);
			return sw.getBuffer().toString();
		} catch (Throwable t) {
			throw new RVLLineSetException(t);
		}
	}
	
}
