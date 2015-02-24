//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.02.24 um 11:58:48 AM CET 
//


package de.varylab.varylab.plugin.grasshopper.data.binding;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für RVLUVList complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="RVLUVList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UVPoints" type="{http://schemas.datacontract.org/2004/07/GHVaryLab}uv_list"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RVLUVList", propOrder = {
    "uvPoints"
})
public class RVLUVList {

    @XmlElement(name = "UVPoints", required = true)
    protected UvList uvPoints;

    /**
     * Ruft den Wert der uvPoints-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link UvList }
     *     
     */
    public UvList getUVPoints() {
        return uvPoints;
    }

    /**
     * Legt den Wert der uvPoints-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link UvList }
     *     
     */
    public void setUVPoints(UvList value) {
        this.uvPoints = value;
    }

}
