
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für sysopAddUserToProject complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="sysopAddUserToProject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="userID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="projectID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="role" type="{http://ws.service.hyperimage.org/}hiRoles" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sysopAddUserToProject", propOrder = {
    "userID",
    "projectID",
    "role"
})
public class SysopAddUserToProject {

    protected long userID;
    protected long projectID;
    protected HiRoles role;

    /**
     * Ruft den Wert der userID-Eigenschaft ab.
     * 
     */
    public long getUserID() {
        return userID;
    }

    /**
     * Legt den Wert der userID-Eigenschaft fest.
     * 
     */
    public void setUserID(long value) {
        this.userID = value;
    }

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
     * Ruft den Wert der role-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link HiRoles }
     *     
     */
    public HiRoles getRole() {
        return role;
    }

    /**
     * Legt den Wert der role-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link HiRoles }
     *     
     */
    public void setRole(HiRoles value) {
        this.role = value;
    }

}
