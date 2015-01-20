
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für moveToGroup complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="moveToGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="baseID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="fromGroupID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="toGroupID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "moveToGroup", propOrder = {
    "baseID",
    "fromGroupID",
    "toGroupID"
})
public class MoveToGroup {

    protected long baseID;
    protected long fromGroupID;
    protected long toGroupID;

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
     * Ruft den Wert der fromGroupID-Eigenschaft ab.
     * 
     */
    public long getFromGroupID() {
        return fromGroupID;
    }

    /**
     * Legt den Wert der fromGroupID-Eigenschaft fest.
     * 
     */
    public void setFromGroupID(long value) {
        this.fromGroupID = value;
    }

    /**
     * Ruft den Wert der toGroupID-Eigenschaft ab.
     * 
     */
    public long getToGroupID() {
        return toGroupID;
    }

    /**
     * Legt den Wert der toGroupID-Eigenschaft fest.
     * 
     */
    public void setToGroupID(long value) {
        this.toGroupID = value;
    }

}
