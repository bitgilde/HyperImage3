
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für updateLightTable complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="updateLightTable">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="lightTable" type="{http://ws.service.hyperimage.org/}hiLightTable" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "updateLightTable", propOrder = {
    "lightTable"
})
public class UpdateLightTable {

    protected HiLightTable lightTable;

    /**
     * Ruft den Wert der lightTable-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link HiLightTable }
     *     
     */
    public HiLightTable getLightTable() {
        return lightTable;
    }

    /**
     * Legt den Wert der lightTable-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link HiLightTable }
     *     
     */
    public void setLightTable(HiLightTable value) {
        this.lightTable = value;
    }

}
