
package org.hyperimage.connector.fedora3.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getMetadataSchemeForRecordResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getMetadataSchemeForRecordResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://connector.ws.hyperimage.org/}hiMetadataSchema" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getMetadataSchemeForRecordResponse", propOrder = {
    "_return"
})
public class GetMetadataSchemeForRecordResponse {

    @XmlElement(name = "return")
    protected HiMetadataSchema _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link HiMetadataSchema }
     *     
     */
    public HiMetadataSchema getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link HiMetadataSchema }
     *     
     */
    public void setReturn(HiMetadataSchema value) {
        this._return = value;
    }

}
