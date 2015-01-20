
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für setLayerLink complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="setLayerLink">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="layerID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="linkID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "setLayerLink", propOrder = {
    "layerID",
    "linkID"
})
public class SetLayerLink {

    protected long layerID;
    protected long linkID;

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

    /**
     * Ruft den Wert der linkID-Eigenschaft ab.
     * 
     */
    public long getLinkID() {
        return linkID;
    }

    /**
     * Legt den Wert der linkID-Eigenschaft fest.
     * 
     */
    public void setLinkID(long value) {
        this.linkID = value;
    }

}
