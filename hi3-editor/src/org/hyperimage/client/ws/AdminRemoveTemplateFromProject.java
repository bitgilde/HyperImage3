
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für adminRemoveTemplateFromProject complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="adminRemoveTemplateFromProject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="templateID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adminRemoveTemplateFromProject", propOrder = {
    "templateID"
})
public class AdminRemoveTemplateFromProject {

    protected long templateID;

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

}
