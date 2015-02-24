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

import de.varylab.varylab.plugin.grasshopper.data.binding.RVLUVList;


public class RVLUVListFactory {
	
	private static Logger
		log = Logger.getLogger(RVLUVListFactory.class.getName());
	private static JAXBContext
		jaxbContex = null;
    private final static QName 
    	_RVLLineSet_QNAME = new QName("http://schemas.datacontract.org/2004/07/GHVaryLab", "RVLUVList");
	
	static {
		try {
			ClassLoader parentLoader = RVLUVListFactory.class.getClassLoader();
			jaxbContex = JAXBContext.newInstance("de.varylab.varylab.plugin.grasshopper.data.binding", parentLoader);
		} catch (Exception e) {
			log.severe(e.getMessage());
		}
	}
	
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/GHVaryLab", name = "RVLUVList")
    public static JAXBElement<RVLUVList> createRVLUVList(RVLUVList value) {
        return new JAXBElement<RVLUVList>(_RVLLineSet_QNAME, RVLUVList.class, null, value);
    }
	
	public static RVLUVList loadRVLUVList(Reader in) throws RVLUVListException {
		try {
			Unmarshaller u = jaxbContex.createUnmarshaller();
			u.setSchema(null);
			@SuppressWarnings("unchecked")
			JAXBElement<RVLUVList> element = (JAXBElement<RVLUVList>)u.unmarshal(in);
			return element.getValue();
		} catch (Throwable t) {
			throw new RVLUVListException(t);
		}
	}
	
	public static String lineSetToXML(RVLUVList lineSet) throws RVLUVListException {
		try {
			Marshaller m = jaxbContex.createMarshaller();
			StringWriter sw = new StringWriter();
			JAXBElement<RVLUVList> lineSetElement = createRVLUVList(lineSet);
			m.marshal(lineSetElement, sw);
			return sw.getBuffer().toString();
		} catch (Throwable t) {
			throw new RVLUVListException(t);
		}
	}
	
}
