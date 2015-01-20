
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
 *         &lt;element name="objectPID" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "objectPID"
})
@XmlRootElement(name = "ingestObjectResponse")
public class IngestObjectResponse {

    @XmlElement(required = true)
    protected String objectPID;

    /**
     * Gets the value of the objectPID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectPID() {
        return objectPID;
    }

    /**
     * Sets the value of the objectPID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectPID(String value) {
        this.objectPID = value;
    }

}
