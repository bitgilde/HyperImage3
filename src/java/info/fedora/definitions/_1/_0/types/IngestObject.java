
package info.fedora.definitions._1._0.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="METSXML" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="logMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "metsxml",
    "logMessage"
})
@XmlRootElement(name = "ingestObject")
public class IngestObject {

    @XmlElement(name = "METSXML", required = true)
    protected byte[] metsxml;
    @XmlElement(required = true)
    protected String logMessage;

    /**
     * Gets the value of the metsxml property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getMETSXML() {
        return metsxml;
    }

    /**
     * Sets the value of the metsxml property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setMETSXML(byte[] value) {
        this.metsxml = ((byte[]) value);
    }

    /**
     * Gets the value of the logMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogMessage() {
        return logMessage;
    }

    /**
     * Sets the value of the logMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogMessage(String value) {
        this.logMessage = value;
    }

}
