
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für hiBaseTypes.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="hiBaseTypes">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="HIGroup"/>
 *     &lt;enumeration value="HIInscription"/>
 *     &lt;enumeration value="HILayer"/>
 *     &lt;enumeration value="HIObject"/>
 *     &lt;enumeration value="HIText"/>
 *     &lt;enumeration value="HIURL"/>
 *     &lt;enumeration value="HILightTable"/>
 *     &lt;enumeration value="HIView"/>
 *     &lt;enumeration value="HIRepositoryItem"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "hiBaseTypes")
@XmlEnum
public enum HiBaseTypes {

    @XmlEnumValue("HIGroup")
    HI_GROUP("HIGroup"),
    @XmlEnumValue("HIInscription")
    HI_INSCRIPTION("HIInscription"),
    @XmlEnumValue("HILayer")
    HI_LAYER("HILayer"),
    @XmlEnumValue("HIObject")
    HI_OBJECT("HIObject"),
    @XmlEnumValue("HIText")
    HI_TEXT("HIText"),
    HIURL("HIURL"),
    @XmlEnumValue("HILightTable")
    HI_LIGHT_TABLE("HILightTable"),
    @XmlEnumValue("HIView")
    HI_VIEW("HIView"),
    @XmlEnumValue("HIRepositoryItem")
    HI_REPOSITORY_ITEM("HIRepositoryItem");
    private final String value;

    HiBaseTypes(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static HiBaseTypes fromValue(String v) {
        for (HiBaseTypes c: HiBaseTypes.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
