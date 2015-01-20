
package org.hyperimage.connector.fedora3.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.hyperimage.connector.fedora3.ws package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _AssetURN_QNAME = new QName("http://connector.ws.hyperimage.org/", "assetURN");
    private final static QName _Token_QNAME = new QName("http://connector.ws.hyperimage.org/", "token");
    private final static QName _GetAssetPreviewDataResponse_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "getAssetPreviewDataResponse");
    private final static QName _ParentURN_QNAME = new QName("http://connector.ws.hyperimage.org/", "parentURN");
    private final static QName _Username_QNAME = new QName("http://connector.ws.hyperimage.org/", "username");
    private final static QName _GetAssetData_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "getAssetData");
    private final static QName _GetAssetPreviewData_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "getAssetPreviewData");
    private final static QName _GetHierarchyLevelResponse_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "getHierarchyLevelResponse");
    private final static QName _Authenticate_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "authenticate");
    private final static QName _HIWSLoggedException_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "HIWSLoggedException");
    private final static QName _GetMetadataRecord_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "getMetadataRecord");
    private final static QName _HIWSNotBinaryException_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "HIWSNotBinaryException");
    private final static QName _Session_QNAME = new QName("http://connector.ws.hyperimage.org/", "session");
    private final static QName _HIWSDCMetadataException_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "HIWSDCMetadataException");
    private final static QName _HIWSAuthException_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "HIWSAuthException");
    private final static QName _HIWSAssetNotFoundException_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "HIWSAssetNotFoundException");
    private final static QName _GetWSVersion_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "getWSVersion");
    private final static QName _GetMetadataRecordResponse_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "getMetadataRecordResponse");
    private final static QName _HIWSUTF8EncodingException_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "HIWSUTF8EncodingException");
    private final static QName _GetWSVersionResponse_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "getWSVersionResponse");
    private final static QName _GetReposInfo_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "getReposInfo");
    private final static QName _HIWSXMLParserException_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "HIWSXMLParserException");
    private final static QName _AuthenticateResponse_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "authenticateResponse");
    private final static QName _GetAssetDataResponse_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "getAssetDataResponse");
    private final static QName _GetHierarchyLevel_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "getHierarchyLevel");
    private final static QName _GetReposInfoResponse_QNAME = new QName("http://fedora3.connector.hyperimage.org/", "getReposInfoResponse");
    private final static QName _GetAssetPreviewDataResponseReturn_QNAME = new QName("", "return");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.hyperimage.connector.fedora3.ws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link HIWSDCMetadataException }
     * 
     */
    public HIWSDCMetadataException createHIWSDCMetadataException() {
        return new HIWSDCMetadataException();
    }

    /**
     * Create an instance of {@link GetAssetDataResponse }
     * 
     */
    public GetAssetDataResponse createGetAssetDataResponse() {
        return new GetAssetDataResponse();
    }

    /**
     * Create an instance of {@link HIWSAuthException }
     * 
     */
    public HIWSAuthException createHIWSAuthException() {
        return new HIWSAuthException();
    }

    /**
     * Create an instance of {@link HIWSAssetNotFoundException }
     * 
     */
    public HIWSAssetNotFoundException createHIWSAssetNotFoundException() {
        return new HIWSAssetNotFoundException();
    }

    /**
     * Create an instance of {@link HIWSNotBinaryException }
     * 
     */
    public HIWSNotBinaryException createHIWSNotBinaryException() {
        return new HIWSNotBinaryException();
    }

    /**
     * Create an instance of {@link GetHierarchyLevelResponse }
     * 
     */
    public GetHierarchyLevelResponse createGetHierarchyLevelResponse() {
        return new GetHierarchyLevelResponse();
    }

    /**
     * Create an instance of {@link Authenticate }
     * 
     */
    public Authenticate createAuthenticate() {
        return new Authenticate();
    }

    /**
     * Create an instance of {@link HiHierarchyLevel }
     * 
     */
    public HiHierarchyLevel createHiHierarchyLevel() {
        return new HiHierarchyLevel();
    }

    /**
     * Create an instance of {@link HIWSLoggedException }
     * 
     */
    public HIWSLoggedException createHIWSLoggedException() {
        return new HIWSLoggedException();
    }

    /**
     * Create an instance of {@link GetHierarchyLevel }
     * 
     */
    public GetHierarchyLevel createGetHierarchyLevel() {
        return new GetHierarchyLevel();
    }

    /**
     * Create an instance of {@link AuthenticateResponse }
     * 
     */
    public AuthenticateResponse createAuthenticateResponse() {
        return new AuthenticateResponse();
    }

    /**
     * Create an instance of {@link GetReposInfoResponse }
     * 
     */
    public GetReposInfoResponse createGetReposInfoResponse() {
        return new GetReposInfoResponse();
    }

    /**
     * Create an instance of {@link GetAssetPreviewDataResponse }
     * 
     */
    public GetAssetPreviewDataResponse createGetAssetPreviewDataResponse() {
        return new GetAssetPreviewDataResponse();
    }

    /**
     * Create an instance of {@link GetWSVersion }
     * 
     */
    public GetWSVersion createGetWSVersion() {
        return new GetWSVersion();
    }

    /**
     * Create an instance of {@link GetMetadataRecordResponse }
     * 
     */
    public GetMetadataRecordResponse createGetMetadataRecordResponse() {
        return new GetMetadataRecordResponse();
    }

    /**
     * Create an instance of {@link HiMetadataRecord }
     * 
     */
    public HiMetadataRecord createHiMetadataRecord() {
        return new HiMetadataRecord();
    }

    /**
     * Create an instance of {@link HiTypedDatastream }
     * 
     */
    public HiTypedDatastream createHiTypedDatastream() {
        return new HiTypedDatastream();
    }

    /**
     * Create an instance of {@link HIWSXMLParserException }
     * 
     */
    public HIWSXMLParserException createHIWSXMLParserException() {
        return new HIWSXMLParserException();
    }

    /**
     * Create an instance of {@link GetMetadataRecord }
     * 
     */
    public GetMetadataRecord createGetMetadataRecord() {
        return new GetMetadataRecord();
    }

    /**
     * Create an instance of {@link GetAssetPreviewData }
     * 
     */
    public GetAssetPreviewData createGetAssetPreviewData() {
        return new GetAssetPreviewData();
    }

    /**
     * Create an instance of {@link HIWSUTF8EncodingException }
     * 
     */
    public HIWSUTF8EncodingException createHIWSUTF8EncodingException() {
        return new HIWSUTF8EncodingException();
    }

    /**
     * Create an instance of {@link GetReposInfo }
     * 
     */
    public GetReposInfo createGetReposInfo() {
        return new GetReposInfo();
    }

    /**
     * Create an instance of {@link GetWSVersionResponse }
     * 
     */
    public GetWSVersionResponse createGetWSVersionResponse() {
        return new GetWSVersionResponse();
    }

    /**
     * Create an instance of {@link GetAssetData }
     * 
     */
    public GetAssetData createGetAssetData() {
        return new GetAssetData();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://connector.ws.hyperimage.org/", name = "assetURN")
    public JAXBElement<String> createAssetURN(String value) {
        return new JAXBElement<String>(_AssetURN_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://connector.ws.hyperimage.org/", name = "token")
    public JAXBElement<String> createToken(String value) {
        return new JAXBElement<String>(_Token_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAssetPreviewDataResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "getAssetPreviewDataResponse")
    public JAXBElement<GetAssetPreviewDataResponse> createGetAssetPreviewDataResponse(GetAssetPreviewDataResponse value) {
        return new JAXBElement<GetAssetPreviewDataResponse>(_GetAssetPreviewDataResponse_QNAME, GetAssetPreviewDataResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://connector.ws.hyperimage.org/", name = "parentURN")
    public JAXBElement<String> createParentURN(String value) {
        return new JAXBElement<String>(_ParentURN_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://connector.ws.hyperimage.org/", name = "username")
    public JAXBElement<String> createUsername(String value) {
        return new JAXBElement<String>(_Username_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAssetData }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "getAssetData")
    public JAXBElement<GetAssetData> createGetAssetData(GetAssetData value) {
        return new JAXBElement<GetAssetData>(_GetAssetData_QNAME, GetAssetData.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAssetPreviewData }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "getAssetPreviewData")
    public JAXBElement<GetAssetPreviewData> createGetAssetPreviewData(GetAssetPreviewData value) {
        return new JAXBElement<GetAssetPreviewData>(_GetAssetPreviewData_QNAME, GetAssetPreviewData.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetHierarchyLevelResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "getHierarchyLevelResponse")
    public JAXBElement<GetHierarchyLevelResponse> createGetHierarchyLevelResponse(GetHierarchyLevelResponse value) {
        return new JAXBElement<GetHierarchyLevelResponse>(_GetHierarchyLevelResponse_QNAME, GetHierarchyLevelResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Authenticate }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "authenticate")
    public JAXBElement<Authenticate> createAuthenticate(Authenticate value) {
        return new JAXBElement<Authenticate>(_Authenticate_QNAME, Authenticate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HIWSLoggedException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "HIWSLoggedException")
    public JAXBElement<HIWSLoggedException> createHIWSLoggedException(HIWSLoggedException value) {
        return new JAXBElement<HIWSLoggedException>(_HIWSLoggedException_QNAME, HIWSLoggedException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetMetadataRecord }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "getMetadataRecord")
    public JAXBElement<GetMetadataRecord> createGetMetadataRecord(GetMetadataRecord value) {
        return new JAXBElement<GetMetadataRecord>(_GetMetadataRecord_QNAME, GetMetadataRecord.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HIWSNotBinaryException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "HIWSNotBinaryException")
    public JAXBElement<HIWSNotBinaryException> createHIWSNotBinaryException(HIWSNotBinaryException value) {
        return new JAXBElement<HIWSNotBinaryException>(_HIWSNotBinaryException_QNAME, HIWSNotBinaryException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://connector.ws.hyperimage.org/", name = "session")
    public JAXBElement<String> createSession(String value) {
        return new JAXBElement<String>(_Session_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HIWSDCMetadataException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "HIWSDCMetadataException")
    public JAXBElement<HIWSDCMetadataException> createHIWSDCMetadataException(HIWSDCMetadataException value) {
        return new JAXBElement<HIWSDCMetadataException>(_HIWSDCMetadataException_QNAME, HIWSDCMetadataException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HIWSAuthException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "HIWSAuthException")
    public JAXBElement<HIWSAuthException> createHIWSAuthException(HIWSAuthException value) {
        return new JAXBElement<HIWSAuthException>(_HIWSAuthException_QNAME, HIWSAuthException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HIWSAssetNotFoundException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "HIWSAssetNotFoundException")
    public JAXBElement<HIWSAssetNotFoundException> createHIWSAssetNotFoundException(HIWSAssetNotFoundException value) {
        return new JAXBElement<HIWSAssetNotFoundException>(_HIWSAssetNotFoundException_QNAME, HIWSAssetNotFoundException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetWSVersion }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "getWSVersion")
    public JAXBElement<GetWSVersion> createGetWSVersion(GetWSVersion value) {
        return new JAXBElement<GetWSVersion>(_GetWSVersion_QNAME, GetWSVersion.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetMetadataRecordResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "getMetadataRecordResponse")
    public JAXBElement<GetMetadataRecordResponse> createGetMetadataRecordResponse(GetMetadataRecordResponse value) {
        return new JAXBElement<GetMetadataRecordResponse>(_GetMetadataRecordResponse_QNAME, GetMetadataRecordResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HIWSUTF8EncodingException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "HIWSUTF8EncodingException")
    public JAXBElement<HIWSUTF8EncodingException> createHIWSUTF8EncodingException(HIWSUTF8EncodingException value) {
        return new JAXBElement<HIWSUTF8EncodingException>(_HIWSUTF8EncodingException_QNAME, HIWSUTF8EncodingException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetWSVersionResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "getWSVersionResponse")
    public JAXBElement<GetWSVersionResponse> createGetWSVersionResponse(GetWSVersionResponse value) {
        return new JAXBElement<GetWSVersionResponse>(_GetWSVersionResponse_QNAME, GetWSVersionResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetReposInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "getReposInfo")
    public JAXBElement<GetReposInfo> createGetReposInfo(GetReposInfo value) {
        return new JAXBElement<GetReposInfo>(_GetReposInfo_QNAME, GetReposInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HIWSXMLParserException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "HIWSXMLParserException")
    public JAXBElement<HIWSXMLParserException> createHIWSXMLParserException(HIWSXMLParserException value) {
        return new JAXBElement<HIWSXMLParserException>(_HIWSXMLParserException_QNAME, HIWSXMLParserException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AuthenticateResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "authenticateResponse")
    public JAXBElement<AuthenticateResponse> createAuthenticateResponse(AuthenticateResponse value) {
        return new JAXBElement<AuthenticateResponse>(_AuthenticateResponse_QNAME, AuthenticateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAssetDataResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "getAssetDataResponse")
    public JAXBElement<GetAssetDataResponse> createGetAssetDataResponse(GetAssetDataResponse value) {
        return new JAXBElement<GetAssetDataResponse>(_GetAssetDataResponse_QNAME, GetAssetDataResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetHierarchyLevel }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "getHierarchyLevel")
    public JAXBElement<GetHierarchyLevel> createGetHierarchyLevel(GetHierarchyLevel value) {
        return new JAXBElement<GetHierarchyLevel>(_GetHierarchyLevel_QNAME, GetHierarchyLevel.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetReposInfoResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://fedora3.connector.hyperimage.org/", name = "getReposInfoResponse")
    public JAXBElement<GetReposInfoResponse> createGetReposInfoResponse(GetReposInfoResponse value) {
        return new JAXBElement<GetReposInfoResponse>(_GetReposInfoResponse_QNAME, GetReposInfoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "return", scope = GetAssetPreviewDataResponse.class)
    public JAXBElement<byte[]> createGetAssetPreviewDataResponseReturn(byte[] value) {
        return new JAXBElement<byte[]>(_GetAssetPreviewDataResponseReturn_QNAME, byte[].class, GetAssetPreviewDataResponse.class, ((byte[]) value));
    }

}
