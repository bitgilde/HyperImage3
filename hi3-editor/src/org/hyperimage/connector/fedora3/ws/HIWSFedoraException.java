
package org.hyperimage.connector.fedora3.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HIWSFedoraException complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HIWSFedoraException">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SOAPFaultMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="assetURN" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HIWSFedoraException", propOrder = {
    "soapFaultMessage",
    "assetURN",
    "message"
})
public class HIWSFedoraException {

    @XmlElement(name = "SOAPFaultMessage")
    protected String soapFaultMessage;
    protected String assetURN;
    protected String message;

    /**
     * Gets the value of the soapFaultMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSOAPFaultMessage() {
        return soapFaultMessage;
    }

    /**
     * Sets the value of the soapFaultMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSOAPFaultMessage(String value) {
        this.soapFaultMessage = value;
    }

    /**
     * Gets the value of the assetURN property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssetURN() {
        return assetURN;
    }

    /**
     * Sets the value of the assetURN property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssetURN(String value) {
        this.assetURN = value;
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

}
