//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.02.16 um 10:25:20 AM CET 
//


package de.varylab.varylab.plugin.blender.data.binding;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.varylab.varylab.plugin.blender.data.binding package. 
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

    private final static QName _BlenderMesh_QNAME = new QName("http://www.varylab.com/2013/09/BlenderMesh", "BlenderMesh");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.varylab.varylab.plugin.blender.data.binding
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BlenderMesh }
     * 
     */
    public BlenderMesh createBlenderMesh() {
        return new BlenderMesh();
    }

    /**
     * Create an instance of {@link Face }
     * 
     */
    public Face createFace() {
        return new Face();
    }

    /**
     * Create an instance of {@link Vertex }
     * 
     */
    public Vertex createVertex() {
        return new Vertex();
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
     * Create an instance of {@link JAXBElement }{@code <}{@link BlenderMesh }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.varylab.com/2013/09/BlenderMesh", name = "BlenderMesh")
    public JAXBElement<BlenderMesh> createBlenderMesh(BlenderMesh value) {
        return new JAXBElement<BlenderMesh>(_BlenderMesh_QNAME, BlenderMesh.class, null, value);
    }

}
