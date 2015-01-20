
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für adminAddTemplateToProject complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="adminAddTemplateToProject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="template" type="{http://ws.service.hyperimage.org/}hiFlexMetadataTemplate" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adminAddTemplateToProject", propOrder = {
    "template"
})
public class AdminAddTemplateToProject {

    protected HiFlexMetadataTemplate template;

    /**
     * Ruft den Wert der template-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link HiFlexMetadataTemplate }
     *     
     */
    public HiFlexMetadataTemplate getTemplate() {
        return template;
    }

    /**
     * Legt den Wert der template-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link HiFlexMetadataTemplate }
     *     
     */
    public void setTemplate(HiFlexMetadataTemplate value) {
        this.template = value;
    }

}
