
package org.hyperimage.client.ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für checkoutPermissions.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="checkoutPermissions">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CREATOR_ADMINS_USERS"/>
 *     &lt;enumeration value="CREATOR_ADMINS"/>
 *     &lt;enumeration value="CREATOR_ONLY"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "checkoutPermissions")
@XmlEnum
public enum CheckoutPermissions {

    CREATOR_ADMINS_USERS,
    CREATOR_ADMINS,
    CREATOR_ONLY;

    public String value() {
        return name();
    }

    public static CheckoutPermissions fromValue(String v) {
        return valueOf(v);
    }

}
