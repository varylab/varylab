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
 * <p>Java-Klasse für uv_point complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="uv_point">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="U" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="V" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="Id" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "uv_point", propOrder = {
    "u",
    "v",
    "id"
})
public class UvPoint {

    @XmlElement(name = "U")
    protected double u;
    @XmlElement(name = "V")
    protected double v;
    @XmlElement(name = "Id")
    protected int id;

    /**
     * Ruft den Wert der u-Eigenschaft ab.
     * 
     */
    public double getU() {
        return u;
    }

    /**
     * Legt den Wert der u-Eigenschaft fest.
     * 
     */
    public void setU(double value) {
        this.u = value;
    }

    /**
     * Ruft den Wert der v-Eigenschaft ab.
     * 
     */
    public double getV() {
        return v;
    }

    /**
     * Legt den Wert der v-Eigenschaft fest.
     * 
     */
    public void setV(double value) {
        this.v = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     */
    public int getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     */
    public void setId(int value) {
        this.id = value;
    }

}
