
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für sysopUpdateProjectQuota complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="sysopUpdateProjectQuota">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="projectID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="quota" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sysopUpdateProjectQuota", propOrder = {
    "projectID",
    "quota"
})
public class SysopUpdateProjectQuota {

    protected long projectID;
    protected long quota;

    /**
     * Ruft den Wert der projectID-Eigenschaft ab.
     * 
     */
    public long getProjectID() {
        return projectID;
    }

    /**
     * Legt den Wert der projectID-Eigenschaft fest.
     * 
     */
    public void setProjectID(long value) {
        this.projectID = value;
    }

    /**
     * Ruft den Wert der quota-Eigenschaft ab.
     * 
     */
    public long getQuota() {
        return quota;
    }

    /**
     * Legt den Wert der quota-Eigenschaft fest.
     * 
     */
    public void setQuota(long value) {
        this.quota = value;
    }

}
