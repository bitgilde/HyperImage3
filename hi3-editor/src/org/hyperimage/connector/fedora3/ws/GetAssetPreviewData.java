
package org.hyperimage.connector.fedora3.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getAssetPreviewData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getAssetPreviewData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://connector.ws.hyperimage.org/}session" minOccurs="0"/>
 *         &lt;element ref="{http://connector.ws.hyperimage.org/}assetURN" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getAssetPreviewData", propOrder = {
    "session",
    "assetURN"
})
public class GetAssetPreviewData {

    @XmlElement(namespace = "http://connector.ws.hyperimage.org/")
    protected String session;
    @XmlElement(namespace = "http://connector.ws.hyperimage.org/")
    protected String assetURN;

    /**
     * Gets the value of the session property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSession() {
        return session;
    }

    /**
     * Sets the value of the session property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSession(String value) {
        this.session = value;
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

}
