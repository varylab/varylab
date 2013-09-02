//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2013.09.01 um 11:23:47 AM CEST 
//


package de.varylab.varylab.plugin.grasshopper.data.binding;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für RVLLineSet complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="RVLLineSet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Lines" type="{http://schemas.datacontract.org/2004/07/GHVaryLab}line_list"/>
 *         &lt;element name="Vertices" type="{http://schemas.datacontract.org/2004/07/GHVaryLab}vertex_list"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RVLLineSet", propOrder = {
    "lines",
    "vertices"
})
public class RVLLineSet {

    @XmlElement(name = "Lines", required = true)
    protected LineList lines;
    @XmlElement(name = "Vertices", required = true)
    protected VertexList vertices;

    /**
     * Ruft den Wert der lines-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LineList }
     *     
     */
    public LineList getLines() {
        return lines;
    }

    /**
     * Legt den Wert der lines-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LineList }
     *     
     */
    public void setLines(LineList value) {
        this.lines = value;
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
