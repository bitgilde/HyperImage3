
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für removeLayerLink complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="removeLayerLink">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="layerID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "removeLayerLink", propOrder = {
    "layerID"
})
public class RemoveLayerLink {

    protected long layerID;

    /**
     * Ruft den Wert der layerID-Eigenschaft ab.
     * 
     */
    public long getLayerID() {
        return layerID;
    }

    /**
     * Legt den Wert der layerID-Eigenschaft fest.
     * 
     */
    public void setLayerID(long value) {
        this.layerID = value;
    }

}
