
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für getScaledImage complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="getScaledImage">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="baseID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="arg1" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getScaledImage", propOrder = {
    "baseID",
    "arg1"
})
public class GetScaledImage {

    protected long baseID;
    protected double arg1;

    /**
     * Ruft den Wert der baseID-Eigenschaft ab.
     * 
     */
    public long getBaseID() {
        return baseID;
    }

    /**
     * Legt den Wert der baseID-Eigenschaft fest.
     * 
     */
    public void setBaseID(long value) {
        this.baseID = value;
    }

    /**
     * Ruft den Wert der arg1-Eigenschaft ab.
     * 
     */
    public double getArg1() {
        return arg1;
    }

    /**
     * Legt den Wert der arg1-Eigenschaft fest.
     * 
     */
    public void setArg1(double value) {
        this.arg1 = value;
    }

}
