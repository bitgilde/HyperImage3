
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für wSstates.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="wSstates">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AUTHENTICATED"/>
 *     &lt;enumeration value="PROJECT_SELECTED"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "wSstates")
@XmlEnum
public enum WSstates {

    AUTHENTICATED,
    PROJECT_SELECTED;

    public String value() {
        return name();
    }

    public static WSstates fromValue(String v) {
        return valueOf(v);
    }

}
