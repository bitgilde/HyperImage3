
package org.hyperimage.client.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für hiObject complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="hiObject">
 *   &lt;complexContent>
 *     &lt;extension base="{http://ws.service.hyperimage.org/}hiBase">
 *       &lt;sequence>
 *         &lt;element name="defaultView" type="{http://ws.service.hyperimage.org/}hiObjectContent" minOccurs="0"/>
 *         &lt;element name="sortOrder" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="views" type="{http://ws.service.hyperimage.org/}hiObjectContent" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hiObject", propOrder = {
    "defaultView",
    "sortOrder",
    "views"
})
public class HiObject
    extends HiBase
{

    protected HiObjectContent defaultView;
    protected String sortOrder;
    @XmlElement(nillable = true)
    protected List<HiObjectContent> views;

    /**
     * Ruft den Wert der defaultView-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link HiObjectContent }
     *     
     */
    public HiObjectContent getDefaultView() {
        return defaultView;
    }

    /**
     * Legt den Wert der defaultView-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link HiObjectContent }
     *     
     */
    public void setDefaultView(HiObjectContent value) {
        this.defaultView = value;
    }

    /**
     * Ruft den Wert der sortOrder-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSortOrder() {
        return sortOrder;
    }

    /**
     * Legt den Wert der sortOrder-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSortOrder(String value) {
        this.sortOrder = value;
    }

    /**
     * Gets the value of the views property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the views property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getViews().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HiObjectContent }
     * 
     * 
     */
    public List<HiObjectContent> getViews() {
        if (views == null) {
            views = new ArrayList<HiObjectContent>();
        }
        return this.views;
    }

}
