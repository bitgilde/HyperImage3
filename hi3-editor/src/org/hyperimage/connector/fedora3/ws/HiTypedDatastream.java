
package org.hyperimage.connector.fedora3.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for hiTypedDatastream complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="hiTypedDatastream">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="byteArray" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="MIMEType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hiTypedDatastream", propOrder = {
    "byteArray",
    "mimeType"
})
public class HiTypedDatastream {

    protected byte[] byteArray;
    @XmlElement(name = "MIMEType")
    protected String mimeType;

    /**
     * Gets the value of the byteArray property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getByteArray() {
        return byteArray;
    }

    /**
     * Sets the value of the byteArray property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setByteArray(byte[] value) {
        this.byteArray = ((byte[]) value);
    }

    /**
     * Gets the value of the mimeType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMIMEType() {
        return mimeType;
    }

    /**
     * Sets the value of the mimeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMIMEType(String value) {
        this.mimeType = value;
    }

}
