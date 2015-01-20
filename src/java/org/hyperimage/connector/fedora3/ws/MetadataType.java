
package org.hyperimage.connector.fedora3.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for metadataType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="metadataType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DC"/>
 *     &lt;enumeration value="OTHER"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "metadataType")
@XmlEnum
public enum MetadataType {

    DC,
    OTHER;

    public String value() {
        return name();
    }

    public static MetadataType fromValue(String v) {
        return valueOf(v);
    }

}
