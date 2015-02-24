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
 * <p>Java-Klasse für face complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="face">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="A" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="B" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="C" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="D" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="isTriangle" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "face", propOrder = {
    "a",
    "b",
    "c",
    "d",
    "isTriangle"
})
public class Face {

    @XmlElement(name = "A")
    protected int a;
    @XmlElement(name = "B")
    protected int b;
    @XmlElement(name = "C")
    protected int c;
    @XmlElement(name = "D")
    protected int d;
    protected boolean isTriangle;

    /**
     * Ruft den Wert der a-Eigenschaft ab.
     * 
     */
    public int getA() {
        return a;
    }

    /**
     * Legt den Wert der a-Eigenschaft fest.
     * 
     */
    public void setA(int value) {
        this.a = value;
    }

    /**
     * Ruft den Wert der b-Eigenschaft ab.
     * 
     */
    public int getB() {
        return b;
    }

    /**
     * Legt den Wert der b-Eigenschaft fest.
     * 
     */
    public void setB(int value) {
        this.b = value;
    }

    /**
     * Ruft den Wert der c-Eigenschaft ab.
     * 
     */
    public int getC() {
        return c;
    }

    /**
     * Legt den Wert der c-Eigenschaft fest.
     * 
     */
    public void setC(int value) {
        this.c = value;
    }

    /**
     * Ruft den Wert der d-Eigenschaft ab.
     * 
     */
    public int getD() {
        return d;
    }

    /**
     * Legt den Wert der d-Eigenschaft fest.
     * 
     */
    public void setD(int value) {
        this.d = value;
    }

    /**
     * Ruft den Wert der isTriangle-Eigenschaft ab.
     * 
     */
    public boolean isIsTriangle() {
        return isTriangle;
    }

    /**
     * Legt den Wert der isTriangle-Eigenschaft fest.
     * 
     */
    public void setIsTriangle(boolean value) {
        this.isTriangle = value;
    }

}
