
package org.hyperimage.client.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für hiProject complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="hiProject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="defaultLanguage" type="{http://ws.service.hyperimage.org/}hiLanguage" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="languages" type="{http://ws.service.hyperimage.org/}hiLanguage" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="metadata" type="{http://ws.service.hyperimage.org/}hiProjectMetadata" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="preferences" type="{http://ws.service.hyperimage.org/}hiPreference" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="quota" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="startObjectInfo" type="{http://ws.service.hyperimage.org/}hiQuickInfo" minOccurs="0"/>
 *         &lt;element name="templates" type="{http://ws.service.hyperimage.org/}hiFlexMetadataTemplate" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="used" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "hiProject", propOrder = {
    "defaultLanguage",
    "id",
    "languages",
    "metadata",
    "preferences",
    "quota",
    "startObjectInfo",
    "templates",
    "used"
})
public class HiProject {

    protected HiLanguage defaultLanguage;
    protected long id;
    @XmlElement(nillable = true)
    protected List<HiLanguage> languages;
    @XmlElement(nillable = true)
    protected List<HiProjectMetadata> metadata;
    @XmlElement(nillable = true)
    protected List<HiPreference> preferences;
    protected long quota;
    protected HiQuickInfo startObjectInfo;
    @XmlElement(nillable = true)
    protected List<HiFlexMetadataTemplate> templates;
    protected long used;

    /**
     * Ruft den Wert der defaultLanguage-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link HiLanguage }
     *     
     */
    public HiLanguage getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Legt den Wert der defaultLanguage-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link HiLanguage }
     *     
     */
    public void setDefaultLanguage(HiLanguage value) {
        this.defaultLanguage = value;
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
     * Gets the value of the languages property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the languages property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLanguages().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HiLanguage }
     * 
     * 
     */
    public List<HiLanguage> getLanguages() {
        if (languages == null) {
            languages = new ArrayList<HiLanguage>();
        }
        return this.languages;
    }

    /**
     * Gets the value of the metadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the metadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HiProjectMetadata }
     * 
     * 
     */
    public List<HiProjectMetadata> getMetadata() {
        if (metadata == null) {
            metadata = new ArrayList<HiProjectMetadata>();
        }
        return this.metadata;
    }

    /**
     * Gets the value of the preferences property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the preferences property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPreferences().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HiPreference }
     * 
     * 
     */
    public List<HiPreference> getPreferences() {
        if (preferences == null) {
            preferences = new ArrayList<HiPreference>();
        }
        return this.preferences;
    }

    /**
     * Ruft den Wert der quota-Eigenschaft ab.
     * 
     */
    public long getQuota() {
        return quota;
    }

    /**
     * Legt den Wert der quota-Eigenschaft fest.
     * 
     */
    public void setQuota(long value) {
        this.quota = value;
    }

    /**
     * Ruft den Wert der startObjectInfo-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link HiQuickInfo }
     *     
     */
    public HiQuickInfo getStartObjectInfo() {
        return startObjectInfo;
    }

    /**
     * Legt den Wert der startObjectInfo-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link HiQuickInfo }
     *     
     */
    public void setStartObjectInfo(HiQuickInfo value) {
        this.startObjectInfo = value;
    }

    /**
     * Gets the value of the templates property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the templates property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTemplates().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HiFlexMetadataTemplate }
     * 
     * 
     */
    public List<HiFlexMetadataTemplate> getTemplates() {
        if (templates == null) {
            templates = new ArrayList<HiFlexMetadataTemplate>();
        }
        return this.templates;
    }

    /**
     * Ruft den Wert der used-Eigenschaft ab.
     * 
     */
    public long getUsed() {
        return used;
    }

    /**
     * Legt den Wert der used-Eigenschaft fest.
     * 
     */
    public void setUsed(long value) {
        this.used = value;
    }

}
