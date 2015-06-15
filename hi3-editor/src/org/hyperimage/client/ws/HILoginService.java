
package org.hyperimage.client.ws;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.10
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "HILoginService", targetNamespace = "http://ws.service.hyperimage.org/", wsdlLocation = "http://localhost:8080/HI3Author/HILoginService?wsdl")
public class HILoginService
    extends Service
{

    private final static URL HILOGINSERVICE_WSDL_LOCATION;
    private final static WebServiceException HILOGINSERVICE_EXCEPTION;
    private final static QName HILOGINSERVICE_QNAME = new QName("http://ws.service.hyperimage.org/", "HILoginService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://localhost:8080/HI3Author/HILoginService?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        HILOGINSERVICE_WSDL_LOCATION = url;
        HILOGINSERVICE_EXCEPTION = e;
    }

    public HILoginService() {
        super(__getWsdlLocation(), HILOGINSERVICE_QNAME);
    }

    public HILoginService(WebServiceFeature... features) {
        super(__getWsdlLocation(), HILOGINSERVICE_QNAME, features);
    }

    public HILoginService(URL wsdlLocation) {
        super(wsdlLocation, HILOGINSERVICE_QNAME);
    }

    public HILoginService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, HILOGINSERVICE_QNAME, features);
    }

    public HILoginService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public HILoginService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns HILogin
     */
    @WebEndpoint(name = "HILoginPort")
    public HILogin getHILoginPort() {
        return super.getPort(new QName("http://ws.service.hyperimage.org/", "HILoginPort"), HILogin.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns HILogin
     */
    @WebEndpoint(name = "HILoginPort")
    public HILogin getHILoginPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://ws.service.hyperimage.org/", "HILoginPort"), HILogin.class, features);
    }

    private static URL __getWsdlLocation() {
        if (HILOGINSERVICE_EXCEPTION!= null) {
            throw HILOGINSERVICE_EXCEPTION;
        }
        return HILOGINSERVICE_WSDL_LOCATION;
    }

}
