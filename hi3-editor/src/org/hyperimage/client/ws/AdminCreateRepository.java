
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für adminCreateRepository complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="adminCreateRepository">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="newRepository" type="{http://ws.service.hyperimage.org/}hiRepository" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adminCreateRepository", propOrder = {
    "newRepository"
})
public class AdminCreateRepository {

    protected HiRepository newRepository;

    /**
     * Ruft den Wert der newRepository-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link HiRepository }
     *     
     */
    public HiRepository getNewRepository() {
        return newRepository;
    }

    /**
     * Legt den Wert der newRepository-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link HiRepository }
     *     
     */
    public void setNewRepository(HiRepository value) {
        this.newRepository = value;
    }

}
