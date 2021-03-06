//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.02.24 um 11:58:48 AM CET 
//


package de.varylab.varylab.plugin.grasshopper.data.binding;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.varylab.varylab.plugin.grasshopper.data.binding package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _RVLUVList_QNAME = new QName("http://schemas.datacontract.org/2004/07/GHVaryLab", "RVLUVList");
    private final static QName _RVLSurface_QNAME = new QName("http://schemas.datacontract.org/2004/07/GHVaryLab", "RVLSurface");
    private final static QName _RVLLineSet_QNAME = new QName("http://schemas.datacontract.org/2004/07/GHVaryLab", "RVLLineSet");
    private final static QName _RVLMesh_QNAME = new QName("http://schemas.datacontract.org/2004/07/GHVaryLab", "RVLMesh");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.varylab.varylab.plugin.grasshopper.data.binding
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RVLLineSet }
     * 
     */
    public RVLLineSet createRVLLineSet() {
        return new RVLLineSet();
    }

    /**
     * Create an instance of {@link RVLUVList }
     * 
     */
    public RVLUVList createRVLUVList() {
        return new RVLUVList();
    }

    /**
     * Create an instance of {@link RVLSurface }
     * 
     */
    public RVLSurface createRVLSurface() {
        return new RVLSurface();
    }

    /**
     * Create an instance of {@link RVLMesh }
     * 
     */
    public RVLMesh createRVLMesh() {
        return new RVLMesh();
    }

    /**
     * Create an instance of {@link UvList }
     * 
     */
    public UvList createUvList() {
        return new UvList();
    }

    /**
     * Create an instance of {@link Face }
     * 
     */
    public Face createFace() {
        return new Face();
    }

    /**
     * Create an instance of {@link Line }
     * 
     */
    public Line createLine() {
        return new Line();
    }

    /**
     * Create an instance of {@link KnotList }
     * 
     */
    public KnotList createKnotList() {
        return new KnotList();
    }

    /**
     * Create an instance of {@link Vertex }
     * 
     */
    public Vertex createVertex() {
        return new Vertex();
    }

    /**
     * Create an instance of {@link UvPoint }
     * 
     */
    public UvPoint createUvPoint() {
        return new UvPoint();
    }

    /**
     * Create an instance of {@link FaceList }
     * 
     */
    public FaceList createFaceList() {
        return new FaceList();
    }

    /**
     * Create an instance of {@link VertexList }
     * 
     */
    public VertexList createVertexList() {
        return new VertexList();
    }

    /**
     * Create an instance of {@link LineList }
     * 
     */
    public LineList createLineList() {
        return new LineList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RVLUVList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/GHVaryLab", name = "RVLUVList")
    public JAXBElement<RVLUVList> createRVLUVList(RVLUVList value) {
        return new JAXBElement<RVLUVList>(_RVLUVList_QNAME, RVLUVList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RVLSurface }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/GHVaryLab", name = "RVLSurface")
    public JAXBElement<RVLSurface> createRVLSurface(RVLSurface value) {
        return new JAXBElement<RVLSurface>(_RVLSurface_QNAME, RVLSurface.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RVLLineSet }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/GHVaryLab", name = "RVLLineSet")
    public JAXBElement<RVLLineSet> createRVLLineSet(RVLLineSet value) {
        return new JAXBElement<RVLLineSet>(_RVLLineSet_QNAME, RVLLineSet.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RVLMesh }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.datacontract.org/2004/07/GHVaryLab", name = "RVLMesh")
    public JAXBElement<RVLMesh> createRVLMesh(RVLMesh value) {
        return new JAXBElement<RVLMesh>(_RVLMesh_QNAME, RVLMesh.class, null, value);
    }

}
