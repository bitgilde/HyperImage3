
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für updateGroupProperties complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="updateGroupProperties">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="groupID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="visible" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "updateGroupProperties", propOrder = {
    "groupID",
    "visible"
})
public class UpdateGroupProperties {

    protected long groupID;
    protected boolean visible;

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

    /**
     * Ruft den Wert der visible-Eigenschaft ab.
     * 
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Legt den Wert der visible-Eigenschaft fest.
     * 
     */
    public void setVisible(boolean value) {
        this.visible = value;
    }

}
