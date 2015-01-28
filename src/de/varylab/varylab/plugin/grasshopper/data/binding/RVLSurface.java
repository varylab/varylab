//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.01.28 um 02:10:44 PM CET 
//


package de.varylab.varylab.plugin.grasshopper.data.binding;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für RVLSurface complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="RVLSurface">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ControlPoints" type="{http://schemas.datacontract.org/2004/07/GHVaryLab}vertex_list"/>
 *         &lt;element name="UVector" type="{http://schemas.datacontract.org/2004/07/GHVaryLab}knot_list"/>
 *         &lt;element name="VVector" type="{http://schemas.datacontract.org/2004/07/GHVaryLab}knot_list"/>
 *         &lt;element name="UDegree" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="VDegree" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="UCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="VCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RVLSurface", propOrder = {
    "controlPoints",
    "uVector",
    "vVector",
    "uDegree",
    "vDegree",
    "uCount",
    "vCount"
})
public class RVLSurface {

    @XmlElement(name = "ControlPoints", required = true)
    protected VertexList controlPoints;
    @XmlElement(name = "UVector", required = true)
    protected KnotList uVector;
    @XmlElement(name = "VVector", required = true)
    protected KnotList vVector;
    @XmlElement(name = "UDegree")
    protected int uDegree;
    @XmlElement(name = "VDegree")
    protected int vDegree;
    @XmlElement(name = "UCount")
    protected int uCount;
    @XmlElement(name = "VCount")
    protected int vCount;

    /**
     * Ruft den Wert der controlPoints-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link VertexList }
     *     
     */
    public VertexList getControlPoints() {
        return controlPoints;
    }

    /**
     * Legt den Wert der controlPoints-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link VertexList }
     *     
     */
    public void setControlPoints(VertexList value) {
        this.controlPoints = value;
    }

    /**
     * Ruft den Wert der uVector-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link KnotList }
     *     
     */
    public KnotList getUVector() {
        return uVector;
    }

    /**
     * Legt den Wert der uVector-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link KnotList }
     *     
     */
    public void setUVector(KnotList value) {
        this.uVector = value;
    }

    /**
     * Ruft den Wert der vVector-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link KnotList }
     *     
     */
    public KnotList getVVector() {
        return vVector;
    }

    /**
     * Legt den Wert der vVector-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link KnotList }
     *     
     */
    public void setVVector(KnotList value) {
        this.vVector = value;
    }

    /**
     * Ruft den Wert der uDegree-Eigenschaft ab.
     * 
     */
    public int getUDegree() {
        return uDegree;
    }

    /**
     * Legt den Wert der uDegree-Eigenschaft fest.
     * 
     */
    public void setUDegree(int value) {
        this.uDegree = value;
    }

    /**
     * Ruft den Wert der vDegree-Eigenschaft ab.
     * 
     */
    public int getVDegree() {
        return vDegree;
    }

    /**
     * Legt den Wert der vDegree-Eigenschaft fest.
     * 
     */
    public void setVDegree(int value) {
        this.vDegree = value;
    }

    /**
     * Ruft den Wert der uCount-Eigenschaft ab.
     * 
     */
    public int getUCount() {
        return uCount;
    }

    /**
     * Legt den Wert der uCount-Eigenschaft fest.
     * 
     */
    public void setUCount(int value) {
        this.uCount = value;
    }

    /**
     * Ruft den Wert der vCount-Eigenschaft ab.
     * 
     */
    public int getVCount() {
        return vCount;
    }

    /**
     * Legt den Wert der vCount-Eigenschaft fest.
     * 
     */
    public void setVCount(int value) {
        this.vCount = value;
    }

}
