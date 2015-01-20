
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für adminDeleteRepository complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="adminDeleteRepository">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="repository" type="{http://ws.service.hyperimage.org/}hiRepository" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adminDeleteRepository", propOrder = {
    "repository"
})
public class AdminDeleteRepository {

    protected HiRepository repository;

    /**
     * Ruft den Wert der repository-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link HiRepository }
     *     
     */
    public HiRepository getRepository() {
        return repository;
    }

    /**
     * Legt den Wert der repository-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link HiRepository }
     *     
     */
    public void setRepository(HiRepository value) {
        this.repository = value;
    }

}
