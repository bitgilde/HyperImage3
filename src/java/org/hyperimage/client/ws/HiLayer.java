
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für hiLayer complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="hiLayer">
 *   &lt;complexContent>
 *     &lt;extension base="{http://ws.service.hyperimage.org/}hiBase">
 *       &lt;sequence>
 *         &lt;element name="blue" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="green" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="linkInfo" type="{http://ws.service.hyperimage.org/}hiQuickInfo" minOccurs="0"/>
 *         &lt;element name="opacity" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="polygons" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="red" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hiLayer", propOrder = {
    "blue",
    "green",
    "linkInfo",
    "opacity",
    "polygons",
    "red"
})
public class HiLayer
    extends HiBase
{

    protected int blue;
    protected int green;
    protected HiQuickInfo linkInfo;
    protected float opacity;
    protected String polygons;
    protected int red;

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
     * Ruft den Wert der linkInfo-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link HiQuickInfo }
     *     
     */
    public HiQuickInfo getLinkInfo() {
        return linkInfo;
    }

    /**
     * Legt den Wert der linkInfo-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link HiQuickInfo }
     *     
     */
    public void setLinkInfo(HiQuickInfo value) {
        this.linkInfo = value;
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
     * Ruft den Wert der polygons-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPolygons() {
        return polygons;
    }

    /**
     * Legt den Wert der polygons-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPolygons(String value) {
        this.polygons = value;
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

}
