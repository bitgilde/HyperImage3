
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für createLayer complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="createLayer">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="viewID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="red" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="green" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="blue" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="opacity" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="uuid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createLayer", propOrder = {
    "viewID",
    "red",
    "green",
    "blue",
    "opacity",
    "uuid"
})
public class CreateLayer {

    protected long viewID;
    protected int red;
    protected int green;
    protected int blue;
    protected float opacity;
    protected String uuid;

    /**
     * Ruft den Wert der viewID-Eigenschaft ab.
     * 
     */
    public long getViewID() {
        return viewID;
    }

    /**
     * Legt den Wert der viewID-Eigenschaft fest.
     * 
     */
    public void setViewID(long value) {
        this.viewID = value;
    }

    /**
     * Ruft den Wert der red-Eigenschaft ab.
     * 
     */
    public int getRed() {
        return red;
    }

    /**
     * Legt den Wert der red-Eigenschaft fest.
     * 
     */
    public void setRed(int value) {
        this.red = value;
    }

    /**
     * Ruft den Wert der green-Eigenschaft ab.
     * 
     */
    public int getGreen() {
        return green;
    }

    /**
     * Legt den Wert der green-Eigenschaft fest.
     * 
     */
    public void setGreen(int value) {
        this.green = value;
    }

    /**
     * Ruft den Wert der blue-Eigenschaft ab.
     * 
     */
    public int getBlue() {
        return blue;
    }

    /**
     * Legt den Wert der blue-Eigenschaft fest.
     * 
     */
    public void setBlue(int value) {
        this.blue = value;
    }

    /**
     * Ruft den Wert der opacity-Eigenschaft ab.
     * 
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Legt den Wert der opacity-Eigenschaft fest.
     * 
     */
    public void setOpacity(float value) {
        this.opacity = value;
    }

    /**
     * Ruft den Wert der uuid-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Legt den Wert der uuid-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUuid(String value) {
        this.uuid = value;
    }

}
