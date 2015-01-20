
package info.fedora.definitions._1._0.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ObjectProfile complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ObjectProfile">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="pid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="objLabel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="objContentModel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="objType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="objCreateDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="objLastModDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="objDissIndexViewURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="objItemIndexViewURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObjectProfile", propOrder = {
    "pid",
    "objLabel",
    "objContentModel",
    "objType",
    "objCreateDate",
    "objLastModDate",
    "objDissIndexViewURL",
    "objItemIndexViewURL"
})
public class ObjectProfile {

    @XmlElement(required = true, nillable = true)
    protected String pid;
    @XmlElement(required = true, nillable = true)
    protected String objLabel;
    @XmlElement(required = true, nillable = true)
    protected String objContentModel;
    @XmlElement(required = true, nillable = true)
    protected String objType;
    @XmlElement(required = true, nillable = true)
    protected String objCreateDate;
    @XmlElement(required = true, nillable = true)
    protected String objLastModDate;
    @XmlElement(required = true, nillable = true)
    protected String objDissIndexViewURL;
    @XmlElement(required = true, nillable = true)
    protected String objItemIndexViewURL;

    /**
     * Gets the value of the pid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPid() {
        return pid;
    }

    /**
     * Sets the value of the pid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPid(String value) {
        this.pid = value;
    }

    /**
     * Gets the value of the objLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjLabel() {
        return objLabel;
    }

    /**
     * Sets the value of the objLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjLabel(String value) {
        this.objLabel = value;
    }

    /**
     * Gets the value of the objContentModel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjContentModel() {
        return objContentModel;
    }

    /**
     * Sets the value of the objContentModel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjContentModel(String value) {
        this.objContentModel = value;
    }

    /**
     * Gets the value of the objType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjType() {
        return objType;
    }

    /**
     * Sets the value of the objType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjType(String value) {
        this.objType = value;
    }

    /**
     * Gets the value of the objCreateDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjCreateDate() {
        return objCreateDate;
    }

    /**
     * Sets the value of the objCreateDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjCreateDate(String value) {
        this.objCreateDate = value;
    }

    /**
     * Gets the value of the objLastModDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjLastModDate() {
        return objLastModDate;
    }

    /**
     * Sets the value of the objLastModDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjLastModDate(String value) {
        this.objLastModDate = value;
    }

    /**
     * Gets the value of the objDissIndexViewURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjDissIndexViewURL() {
        return objDissIndexViewURL;
    }

    /**
     * Sets the value of the objDissIndexViewURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjDissIndexViewURL(String value) {
        this.objDissIndexViewURL = value;
    }

    /**
     * Gets the value of the objItemIndexViewURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjItemIndexViewURL() {
        return objItemIndexViewURL;
    }

    /**
     * Sets the value of the objItemIndexViewURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjItemIndexViewURL(String value) {
        this.objItemIndexViewURL = value;
    }

}
