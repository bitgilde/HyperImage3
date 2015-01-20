
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für adminUpdateTemplate complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="adminUpdateTemplate">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="templateID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="templateURI" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="templateURL" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adminUpdateTemplate", propOrder = {
    "templateID",
    "templateURI",
    "templateURL"
})
public class AdminUpdateTemplate {

    protected long templateID;
    protected String templateURI;
    protected String templateURL;

    /**
     * Ruft den Wert der templateID-Eigenschaft ab.
     * 
     */
    public long getTemplateID() {
        return templateID;
    }

    /**
     * Legt den Wert der templateID-Eigenschaft fest.
     * 
     */
    public void setTemplateID(long value) {
        this.templateID = value;
    }

    /**
     * Ruft den Wert der templateURI-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplateURI() {
        return templateURI;
    }

    /**
     * Legt den Wert der templateURI-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplateURI(String value) {
        this.templateURI = value;
    }

    /**
     * Ruft den Wert der templateURL-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplateURL() {
        return templateURL;
    }

    /**
     * Legt den Wert der templateURL-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplateURL(String value) {
        this.templateURL = value;
    }

}
