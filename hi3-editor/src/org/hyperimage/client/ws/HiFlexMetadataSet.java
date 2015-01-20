
package org.hyperimage.client.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für hiFlexMetadataSet complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="hiFlexMetadataSet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="displayNames" type="{http://ws.service.hyperimage.org/}hiFlexMetadataName" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="richText" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="tagname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hiFlexMetadataSet", propOrder = {
    "displayNames",
    "id",
    "richText",
    "tagname"
})
public class HiFlexMetadataSet {

    @XmlElement(nillable = true)
    protected List<HiFlexMetadataName> displayNames;
    protected long id;
    protected boolean richText;
    protected String tagname;

    /**
     * Gets the value of the displayNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the displayNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDisplayNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HiFlexMetadataName }
     * 
     * 
     */
    public List<HiFlexMetadataName> getDisplayNames() {
        if (displayNames == null) {
            displayNames = new ArrayList<HiFlexMetadataName>();
        }
        return this.displayNames;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der richText-Eigenschaft ab.
     * 
     */
    public boolean isRichText() {
        return richText;
    }

    /**
     * Legt den Wert der richText-Eigenschaft fest.
     * 
     */
    public void setRichText(boolean value) {
        this.richText = value;
    }

    /**
     * Ruft den Wert der tagname-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTagname() {
        return tagname;
    }

    /**
     * Legt den Wert der tagname-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTagname(String value) {
        this.tagname = value;
    }

}
