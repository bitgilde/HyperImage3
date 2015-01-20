
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für hiRoles.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="hiRoles">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="GUEST"/>
 *     &lt;enumeration value="USER"/>
 *     &lt;enumeration value="ADMIN"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "hiRoles")
@XmlEnum
public enum HiRoles {

    GUEST,
    USER,
    ADMIN;

    public String value() {
        return name();
    }

    public static HiRoles fromValue(String v) {
        return valueOf(v);
    }

}
