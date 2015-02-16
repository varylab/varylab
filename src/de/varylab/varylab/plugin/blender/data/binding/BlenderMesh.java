//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.02.16 um 10:25:20 AM CET 
//


package de.varylab.varylab.plugin.blender.data.binding;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für BlenderMesh complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BlenderMesh">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Faces" type="{http://www.varylab.com/2013/09/BlenderMesh}face_list"/>
 *         &lt;element name="Vertices" type="{http://www.varylab.com/2013/09/BlenderMesh}vertex_list"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BlenderMesh", propOrder = {
    "faces",
    "vertices"
})
public class BlenderMesh {

    @XmlElement(name = "Faces", required = true)
    protected FaceList faces;
    @XmlElement(name = "Vertices", required = true)
    protected VertexList vertices;

    /**
     * Ruft den Wert der faces-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link FaceList }
     *     
     */
    public FaceList getFaces() {
        return faces;
    }

    /**
     * Legt den Wert der faces-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link FaceList }
     *     
     */
    public void setFaces(FaceList value) {
        this.faces = value;
    }

    /**
     * Ruft den Wert der vertices-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VertexList }
     *     
     */
    public VertexList getVertices() {
        return vertices;
    }

    /**
     * Legt den Wert der vertices-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VertexList }
     *     
     */
    public void setVertices(VertexList value) {
        this.vertices = value;
    }

}
