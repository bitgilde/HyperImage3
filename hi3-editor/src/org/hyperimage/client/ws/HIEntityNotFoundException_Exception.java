
package org.hyperimage.client.ws;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.10
 * Generated source version: 2.2
 * 
 */
@WebFault(name = "HIEntityNotFoundException", targetNamespace = "http://ws.service.hyperimage.org/")
public class HIEntityNotFoundException_Exception
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private HIEntityNotFoundException faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public HIEntityNotFoundException_Exception(String message, HIEntityNotFoundException faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public HIEntityNotFoundException_Exception(String message, HIEntityNotFoundException faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: org.hyperimage.client.ws.HIEntityNotFoundException
     */
    public HIEntityNotFoundException getFaultInfo() {
        return faultInfo;
    }

}
