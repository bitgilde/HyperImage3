
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für groupTypes.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="groupTypes">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="HIGROUP_REGULAR"/>
 *     &lt;enumeration value="HIGROUP_IMPORT"/>
 *     &lt;enumeration value="HIGROUP_TRASH"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "groupTypes")
@XmlEnum
public enum GroupTypes {

    HIGROUP_REGULAR,
    HIGROUP_IMPORT,
    HIGROUP_TRASH;

    public String value() {
        return name();
    }

    public static GroupTypes fromValue(String v) {
        return valueOf(v);
    }

}
