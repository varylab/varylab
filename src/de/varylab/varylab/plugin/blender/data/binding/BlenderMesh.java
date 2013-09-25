//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.09.02 at 09:24:02 AM CEST 
//


package de.varylab.varylab.plugin.blender.data.binding;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BlenderMesh complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
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
     * Gets the value of the faces property.
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
     * Sets the value of the faces property.
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
     * Gets the value of the vertices property.
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
     * Sets the value of the vertices property.
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