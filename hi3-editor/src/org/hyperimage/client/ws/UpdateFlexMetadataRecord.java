
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für updateFlexMetadataRecord complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="updateFlexMetadataRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="record" type="{http://ws.service.hyperimage.org/}hiFlexMetadataRecord" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "updateFlexMetadataRecord", propOrder = {
    "record"
})
public class UpdateFlexMetadataRecord {

    protected HiFlexMetadataRecord record;

    /**
     * Ruft den Wert der record-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link HiFlexMetadataRecord }
     *     
     */
    public HiFlexMetadataRecord getRecord() {
        return record;
    }

    /**
     * Legt den Wert der record-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link HiFlexMetadataRecord }
     *     
     */
    public void setRecord(HiFlexMetadataRecord value) {
        this.record = value;
    }

}
