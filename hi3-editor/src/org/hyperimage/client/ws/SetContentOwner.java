
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for setContentOwner complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="setContentOwner">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="objectID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="contentID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "setContentOwner", propOrder = {
    "objectID",
    "contentID"
})
public class SetContentOwner {

    protected long objectID;
    protected long contentID;

    /**
     * Gets the value of the objectID property.
     * 
     */
    public long getObjectID() {
        return objectID;
    }

    /**
     * Sets the value of the objectID property.
     * 
     */
    public void setObjectID(long value) {
        this.objectID = value;
    }

    /**
     * Gets the value of the contentID property.
     * 
     */
    public long getContentID() {
        return contentID;
    }

    /**
     * Sets the value of the contentID property.
     * 
     */
    public void setContentID(long value) {
        this.contentID = value;
    }

}
