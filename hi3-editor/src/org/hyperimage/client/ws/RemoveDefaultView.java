
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für removeDefaultView complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="removeDefaultView">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="objectID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "removeDefaultView", propOrder = {
    "objectID"
})
public class RemoveDefaultView {

    protected long objectID;

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

}
