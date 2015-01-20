
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für getGroupContentQuickInfo complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="getGroupContentQuickInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
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
@XmlType(name = "getGroupContentQuickInfo", propOrder = {
    "groupID"
})
public class GetGroupContentQuickInfo {

    protected long groupID;

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
