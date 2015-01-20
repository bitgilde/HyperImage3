
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für setDefaultView complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="setDefaultView">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="objectID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="contentID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "setDefaultView", propOrder = {
    "objectID",
    "contentID"
})
public class SetDefaultView {

    protected long objectID;
    protected long contentID;

    /**
     * Ruft den Wert der objectID-Eigenschaft ab.
     * 
     */
    public long getObjectID() {
        return objectID;
    }

    /**
     * Legt den Wert der objectID-Eigenschaft fest.
     * 
     */
    public void setObjectID(long value) {
        this.objectID = value;
    }

    /**
     * Ruft den Wert der contentID-Eigenschaft ab.
     * 
     */
    public long getContentID() {
        return contentID;
    }

    /**
     * Legt den Wert der contentID-Eigenschaft fest.
     * 
     */
    public void setContentID(long value) {
        this.contentID = value;
    }

}
