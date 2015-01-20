
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für adminAddSetToTemplate complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="adminAddSetToTemplate">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="templateID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="tagName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="isRichText" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adminAddSetToTemplate", propOrder = {
    "templateID",
    "tagName",
    "isRichText"
})
public class AdminAddSetToTemplate {

    protected long templateID;
    protected String tagName;
    protected boolean isRichText;

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
     * Ruft den Wert der tagName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * Legt den Wert der tagName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTagName(String value) {
        this.tagName = value;
    }

    /**
     * Ruft den Wert der isRichText-Eigenschaft ab.
     * 
     */
    public boolean isIsRichText() {
        return isRichText;
    }

    /**
     * Legt den Wert der isRichText-Eigenschaft fest.
     * 
     */
    public void setIsRichText(boolean value) {
        this.isRichText = value;
    }

}
