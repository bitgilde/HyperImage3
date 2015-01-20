
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für removeFromGroup complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="removeFromGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="baseID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="groupID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "removeFromGroup", propOrder = {
    "baseID",
    "groupID"
})
public class RemoveFromGroup {

    protected long baseID;
    protected long groupID;

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
     * Ruft den Wert der groupID-Eigenschaft ab.
     * 
     */
    public long getGroupID() {
        return groupID;
    }

    /**
     * Legt den Wert der groupID-Eigenschaft fest.
     * 
     */
    public void setGroupID(long value) {
        this.groupID = value;
    }

}
