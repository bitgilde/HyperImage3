
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für setProject complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="setProject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="project" type="{http://ws.service.hyperimage.org/}hiProject" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "setProject", propOrder = {
    "project"
})
public class SetProject {

    protected HiProject project;

    /**
     * Ruft den Wert der project-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link HiProject }
     *     
     */
    public HiProject getProject() {
        return project;
    }

    /**
     * Legt den Wert der project-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link HiProject }
     *     
     */
    public void setProject(HiProject value) {
        this.project = value;
    }

}
