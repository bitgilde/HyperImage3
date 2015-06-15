
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für hiImageSizes.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="hiImageSizes">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="HI_THUMBNAIL"/>
 *     &lt;enumeration value="HI_PREVIEW"/>
 *     &lt;enumeration value="HI_FULL"/>
 *     &lt;enumeration value="HI_ORIGINAL"/>
 *     &lt;enumeration value="HI_NAV"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "hiImageSizes")
@XmlEnum
public enum HiImageSizes {

    HI_THUMBNAIL,
    HI_PREVIEW,
    HI_FULL,
    HI_ORIGINAL,
    HI_NAV;

    public String value() {
        return name();
    }

    public static HiImageSizes fromValue(String v) {
        return valueOf(v);
    }

}
