/*
 * Copyright 2014 Leuphana Universität Lüneburg. All rights reserved.
 *
 * Copyright 2014, 2015 bitGilde IT Solutions UG (haftungsbeschränkt). All rights reserved.
 * http://bitgilde.de/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For further information on HyperImage visit http://hyperimage.ws/
 */

/* @author Jens-Martin Loebel */

//
// Definitions for schema: http://www.w3.org/2005/08/addressing
//
//
// Constructor for XML Schema item {http://www.w3.org/2005/08/addressing}MetadataType
//
function www_w3_org_2005_08_addressing_MetadataType () {
    this.typeMarker = 'www_w3_org_2005_08_addressing_MetadataType';
    this._any = [];
}

//
// accessor is www_w3_org_2005_08_addressing_MetadataType.prototype.getAny
// element get for any
// - xs:any
// - required element
// - array
//
// element set for any
// setter function is is www_w3_org_2005_08_addressing_MetadataType.prototype.setAny
//
function www_w3_org_2005_08_addressing_MetadataType_getAny() { return this._any;}

www_w3_org_2005_08_addressing_MetadataType.prototype.getAny = www_w3_org_2005_08_addressing_MetadataType_getAny;

function www_w3_org_2005_08_addressing_MetadataType_setAny(value) { this._any = value;}

www_w3_org_2005_08_addressing_MetadataType.prototype.setAny = www_w3_org_2005_08_addressing_MetadataType_setAny;
//
// Serialize {http://www.w3.org/2005/08/addressing}MetadataType
//
function www_w3_org_2005_08_addressing_MetadataType_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    var anyHolder = this._any;
    var anySerializer = null;
    var anyXmlTag = null;
    var anyXmlNsDef = null;
    var anyData = null;
    var anyStartTag;
    if (anyHolder != null && !anyHolder.raw) {
     anySerializer = cxfjsutils.interfaceObject.globalElementSerializers[anyHolder.qname];
     anyXmlTag = 'cxfjsany4:' + anyHolder.localName;
     anyXmlNsDef = 'xmlns:cxfjsany4=\'' + anyHolder.namespaceURI + '\'';
     anyStartTag = '<' + anyXmlTag + ' ' + anyXmlNsDef + '>';
     anyEndTag = '</' + anyXmlTag + '>';
     anyEmptyTag = '<' + anyXmlTag + ' ' + anyXmlNsDef + '/>';
     anyData = anyHolder.object;
    }
    if (anyHolder != null && anyHolder.raw) {
     xml = xml + anyHolder.xml;
    } else {
     if (anyHolder == null || anyData == null) {
      throw 'null value for required any item';
     }
     for (var ax = 0;ax < anyData.length;ax ++) {
      if (anyData[ax] == null) {
       xml = xml + anyEmptyTag;
      } else {
       if (anySerializer) {
        xml = xml + anySerializer.call(anyData[ax], cxfjsutils, anyXmlTag, anyXmlNsDef);
       } else {
        xml = xml + anyStartTag;
        xml = xml + cxfjsutils.escapeXmlEntities(anyData[ax]);
        xml = xml + anyEndTag;
       }
      }
     }
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

www_w3_org_2005_08_addressing_MetadataType.prototype.serialize = www_w3_org_2005_08_addressing_MetadataType_serialize;

function www_w3_org_2005_08_addressing_MetadataType_deserialize (cxfjsutils, element) {
    var newobject = new www_w3_org_2005_08_addressing_MetadataType();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    var anyObject = [];
    var matcher = new org_apache_cxf_any_ns_matcher(org_apache_cxf_any_ns_matcher.ANY, 'http://www.w3.org/2005/08/addressing', [], null);
    var anyNeeded = 0;
    var anyAllowed = 1;
    while (anyNeeded > 0 || anyAllowed > 0) {
     var anyURI;
     var anyLocalPart;
     var anyMatched = false;
     if (curElement) {
      anyURI = cxfjsutils.getElementNamespaceURI(curElement);
      anyLocalPart = cxfjsutils.getNodeLocalName(curElement);
      var anyQName = '{' + anyURI + '}' + anyLocalPart;
      cxfjsutils.trace('any match: ' + anyQName);
      anyMatched = matcher.match(anyURI, anyLocalPart)
      cxfjsutils.trace(' --> ' + anyMatched);
     }
     if (anyMatched) {
      anyDeserializer = cxfjsutils.interfaceObject.globalElementDeserializers[anyQName];
      cxfjsutils.trace(' deserializer: ' + anyDeserializer);
      if (anyDeserializer) {
       var anyValue = anyDeserializer(cxfjsutils, curElement);
      } else {
       var anyValue = curElement.nodeValue;
      }
      anyObject.push(anyValue);
      anyNeeded--;
      anyAllowed--;
      curElement = cxfjsutils.getNextElementSibling(curElement);
     } else {
      if (anyNeeded > 0) {
       throw 'not enough ws:any elements';
      }
     }
    }
    var anyHolder = new org_apache_cxf_any_holder(anyURI, anyLocalPart, anyValue);
    newobject.setAny(anyHolder);
    return newobject;
}

//
// Constructor for XML Schema item {http://www.w3.org/2005/08/addressing}ReferenceParametersType
//
function www_w3_org_2005_08_addressing_ReferenceParametersType () {
    this.typeMarker = 'www_w3_org_2005_08_addressing_ReferenceParametersType';
    this._any = [];
}

//
// accessor is www_w3_org_2005_08_addressing_ReferenceParametersType.prototype.getAny
// element get for any
// - xs:any
// - required element
// - array
//
// element set for any
// setter function is is www_w3_org_2005_08_addressing_ReferenceParametersType.prototype.setAny
//
function www_w3_org_2005_08_addressing_ReferenceParametersType_getAny() { return this._any;}

www_w3_org_2005_08_addressing_ReferenceParametersType.prototype.getAny = www_w3_org_2005_08_addressing_ReferenceParametersType_getAny;

function www_w3_org_2005_08_addressing_ReferenceParametersType_setAny(value) { this._any = value;}

www_w3_org_2005_08_addressing_ReferenceParametersType.prototype.setAny = www_w3_org_2005_08_addressing_ReferenceParametersType_setAny;
//
// Serialize {http://www.w3.org/2005/08/addressing}ReferenceParametersType
//
function www_w3_org_2005_08_addressing_ReferenceParametersType_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    var anyHolder = this._any;
    var anySerializer = null;
    var anyXmlTag = null;
    var anyXmlNsDef = null;
    var anyData = null;
    var anyStartTag;
    if (anyHolder != null && !anyHolder.raw) {
     anySerializer = cxfjsutils.interfaceObject.globalElementSerializers[anyHolder.qname];
     anyXmlTag = 'cxfjsany5:' + anyHolder.localName;
     anyXmlNsDef = 'xmlns:cxfjsany5=\'' + anyHolder.namespaceURI + '\'';
     anyStartTag = '<' + anyXmlTag + ' ' + anyXmlNsDef + '>';
     anyEndTag = '</' + anyXmlTag + '>';
     anyEmptyTag = '<' + anyXmlTag + ' ' + anyXmlNsDef + '/>';
     anyData = anyHolder.object;
    }
    if (anyHolder != null && anyHolder.raw) {
     xml = xml + anyHolder.xml;
    } else {
     if (anyHolder == null || anyData == null) {
      throw 'null value for required any item';
     }
     for (var ax = 0;ax < anyData.length;ax ++) {
      if (anyData[ax] == null) {
       xml = xml + anyEmptyTag;
      } else {
       if (anySerializer) {
        xml = xml + anySerializer.call(anyData[ax], cxfjsutils, anyXmlTag, anyXmlNsDef);
       } else {
        xml = xml + anyStartTag;
        xml = xml + cxfjsutils.escapeXmlEntities(anyData[ax]);
        xml = xml + anyEndTag;
       }
      }
     }
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

www_w3_org_2005_08_addressing_ReferenceParametersType.prototype.serialize = www_w3_org_2005_08_addressing_ReferenceParametersType_serialize;

function www_w3_org_2005_08_addressing_ReferenceParametersType_deserialize (cxfjsutils, element) {
    var newobject = new www_w3_org_2005_08_addressing_ReferenceParametersType();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    var anyObject = [];
    var matcher = new org_apache_cxf_any_ns_matcher(org_apache_cxf_any_ns_matcher.ANY, 'http://www.w3.org/2005/08/addressing', [], null);
    var anyNeeded = 1;
    var anyAllowed = 1;
    while (anyNeeded > 0 || anyAllowed > 0) {
     var anyURI;
     var anyLocalPart;
     var anyMatched = false;
     if (curElement) {
      anyURI = cxfjsutils.getElementNamespaceURI(curElement);
      anyLocalPart = cxfjsutils.getNodeLocalName(curElement);
      var anyQName = '{' + anyURI + '}' + anyLocalPart;
      cxfjsutils.trace('any match: ' + anyQName);
      anyMatched = matcher.match(anyURI, anyLocalPart)
      cxfjsutils.trace(' --> ' + anyMatched);
     }
     if (anyMatched) {
      anyDeserializer = cxfjsutils.interfaceObject.globalElementDeserializers[anyQName];
      cxfjsutils.trace(' deserializer: ' + anyDeserializer);
      if (anyDeserializer) {
       var anyValue = anyDeserializer(cxfjsutils, curElement);
      } else {
       var anyValue = curElement.childNodes[0].nodeValue;
      }
      anyObject.push(anyValue);
      anyNeeded--;
      anyAllowed--;
      curElement = cxfjsutils.getNextElementSibling(curElement);
     } else {
      if (anyNeeded > 0) {
       throw 'not enough ws:any elements';
      }
     }
    }
    var anyHolder = new org_apache_cxf_any_holder(anyURI, anyLocalPart, anyValue);
    newobject.setAny(anyHolder);
    return newobject;
}

//
// Constructor for XML Schema item {http://www.w3.org/2005/08/addressing}ProblemActionType
//
function www_w3_org_2005_08_addressing_ProblemActionType () {
    this.typeMarker = 'www_w3_org_2005_08_addressing_ProblemActionType';
    this._Action = null;
    this._SoapAction = null;
}

//
// accessor is www_w3_org_2005_08_addressing_ProblemActionType.prototype.getAction
// element get for Action
// - element type is {http://www.w3.org/2005/08/addressing}AttributedURIType
// - optional element
//
// element set for Action
// setter function is is www_w3_org_2005_08_addressing_ProblemActionType.prototype.setAction
//
function www_w3_org_2005_08_addressing_ProblemActionType_getAction() { return this._Action;}

www_w3_org_2005_08_addressing_ProblemActionType.prototype.getAction = www_w3_org_2005_08_addressing_ProblemActionType_getAction;

function www_w3_org_2005_08_addressing_ProblemActionType_setAction(value) { this._Action = value;}

www_w3_org_2005_08_addressing_ProblemActionType.prototype.setAction = www_w3_org_2005_08_addressing_ProblemActionType_setAction;
//
// accessor is www_w3_org_2005_08_addressing_ProblemActionType.prototype.getSoapAction
// element get for SoapAction
// - element type is {http://www.w3.org/2001/XMLSchema}anyURI
// - optional element
//
// element set for SoapAction
// setter function is is www_w3_org_2005_08_addressing_ProblemActionType.prototype.setSoapAction
//
function www_w3_org_2005_08_addressing_ProblemActionType_getSoapAction() { return this._SoapAction;}

www_w3_org_2005_08_addressing_ProblemActionType.prototype.getSoapAction = www_w3_org_2005_08_addressing_ProblemActionType_getSoapAction;

function www_w3_org_2005_08_addressing_ProblemActionType_setSoapAction(value) { this._SoapAction = value;}

www_w3_org_2005_08_addressing_ProblemActionType.prototype.setSoapAction = www_w3_org_2005_08_addressing_ProblemActionType_setSoapAction;
//
// Serialize {http://www.w3.org/2005/08/addressing}ProblemActionType
//
function www_w3_org_2005_08_addressing_ProblemActionType_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     xml = xml + ' ';
     xml = xml + 'xmlns:jns0=\'http://www.w3.org/2005/08/addressing\' ';
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    // block for local variables
    {
     if (this._Action != null) {
      xml = xml + this._Action.serialize(cxfjsutils, 'jns0:Action', null);
     }
    }
    // block for local variables
    {
     if (this._SoapAction != null) {
      xml = xml + '<jns0:SoapAction>';
      xml = xml + cxfjsutils.escapeXmlEntities(this._SoapAction);
      xml = xml + '</jns0:SoapAction>';
     }
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

www_w3_org_2005_08_addressing_ProblemActionType.prototype.serialize = www_w3_org_2005_08_addressing_ProblemActionType_serialize;

function www_w3_org_2005_08_addressing_ProblemActionType_deserialize (cxfjsutils, element) {
    var newobject = new www_w3_org_2005_08_addressing_ProblemActionType();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    cxfjsutils.trace('processing Action');
    if (curElement != null && cxfjsutils.isNodeNamedNS(curElement, 'http://www.w3.org/2005/08/addressing', 'Action')) {
     var value = null;
     if (!cxfjsutils.isElementNil(curElement)) {
      value = cxfjsutils.getNodeText(curElement);
      item = value;
     }
     newobject.setAction(item);
     var item = null;
     if (curElement != null) {
      curElement = cxfjsutils.getNextElementSibling(curElement);
     }
    }
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    cxfjsutils.trace('processing SoapAction');
    if (curElement != null && cxfjsutils.isNodeNamedNS(curElement, 'http://www.w3.org/2005/08/addressing', 'SoapAction')) {
     var value = null;
     if (!cxfjsutils.isElementNil(curElement)) {
      value = cxfjsutils.getNodeText(curElement);
      item = value;
     }
     newobject.setSoapAction(item);
     var item = null;
     if (curElement != null) {
      curElement = cxfjsutils.getNextElementSibling(curElement);
     }
    }
    return newobject;
}

//
// Simple type (enumeration) {http://www.w3.org/2005/08/addressing}FaultCodesType
//
// - tns:InvalidAddressingHeader
// - tns:InvalidAddress
// - tns:InvalidEPR
// - tns:InvalidCardinality
// - tns:MissingAddressInEPR
// - tns:DuplicateMessageID
// - tns:ActionMismatch
// - tns:MessageAddressingHeaderRequired
// - tns:DestinationUnreachable
// - tns:ActionNotSupported
// - tns:EndpointUnavailable
//
// Simple type (enumeration) {http://www.w3.org/2005/08/addressing}RelationshipType
//
// - http://www.w3.org/2005/08/addressing/reply
//
// Constructor for XML Schema item {http://www.w3.org/2005/08/addressing}EndpointReferenceType
//
function www_w3_org_2005_08_addressing_EndpointReferenceType () {
    this.typeMarker = 'www_w3_org_2005_08_addressing_EndpointReferenceType';
    this._Address = null;
    this._ReferenceParameters = null;
    this._Metadata = null;
    this._any = [];
}

//
// accessor is www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.getAddress
// element get for Address
// - element type is {http://www.w3.org/2005/08/addressing}AttributedURIType
// - required element
//
// element set for Address
// setter function is is www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.setAddress
//
function www_w3_org_2005_08_addressing_EndpointReferenceType_getAddress() { return this._Address;}

www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.getAddress = www_w3_org_2005_08_addressing_EndpointReferenceType_getAddress;

function www_w3_org_2005_08_addressing_EndpointReferenceType_setAddress(value) { this._Address = value;}

www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.setAddress = www_w3_org_2005_08_addressing_EndpointReferenceType_setAddress;
//
// accessor is www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.getReferenceParameters
// element get for ReferenceParameters
// - element type is {http://www.w3.org/2005/08/addressing}ReferenceParametersType
// - optional element
//
// element set for ReferenceParameters
// setter function is is www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.setReferenceParameters
//
function www_w3_org_2005_08_addressing_EndpointReferenceType_getReferenceParameters() { return this._ReferenceParameters;}

www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.getReferenceParameters = www_w3_org_2005_08_addressing_EndpointReferenceType_getReferenceParameters;

function www_w3_org_2005_08_addressing_EndpointReferenceType_setReferenceParameters(value) { this._ReferenceParameters = value;}

www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.setReferenceParameters = www_w3_org_2005_08_addressing_EndpointReferenceType_setReferenceParameters;
//
// accessor is www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.getMetadata
// element get for Metadata
// - element type is {http://www.w3.org/2005/08/addressing}MetadataType
// - optional element
//
// element set for Metadata
// setter function is is www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.setMetadata
//
function www_w3_org_2005_08_addressing_EndpointReferenceType_getMetadata() { return this._Metadata;}

www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.getMetadata = www_w3_org_2005_08_addressing_EndpointReferenceType_getMetadata;

function www_w3_org_2005_08_addressing_EndpointReferenceType_setMetadata(value) { this._Metadata = value;}

www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.setMetadata = www_w3_org_2005_08_addressing_EndpointReferenceType_setMetadata;
//
// accessor is www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.getAny
// element get for any
// - xs:any
// - required element
// - array
//
// element set for any
// setter function is is www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.setAny
//
function www_w3_org_2005_08_addressing_EndpointReferenceType_getAny() { return this._any;}

www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.getAny = www_w3_org_2005_08_addressing_EndpointReferenceType_getAny;

function www_w3_org_2005_08_addressing_EndpointReferenceType_setAny(value) { this._any = value;}

www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.setAny = www_w3_org_2005_08_addressing_EndpointReferenceType_setAny;
//
// Serialize {http://www.w3.org/2005/08/addressing}EndpointReferenceType
//
function www_w3_org_2005_08_addressing_EndpointReferenceType_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     xml = xml + ' ';
     xml = xml + 'xmlns:jns0=\'http://www.w3.org/2005/08/addressing\' ';
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    // block for local variables
    {
    	xml += '<jns0:Address>'+this._Address+'</jns0:Address>'; 
//     xml = xml + this._Address.serialize(cxfjsutils, 'jns0:Address', null);
    }
    // block for local variables
    {
     if (this._ReferenceParameters != null) {
      xml = xml + this._ReferenceParameters.serialize(cxfjsutils, 'jns0:ReferenceParameters', null);
     }
    }
    // block for local variables
    {
     if (this._Metadata != null) {
      xml = xml + this._Metadata.serialize(cxfjsutils, 'jns0:Metadata', null);
     }
    }
    var anyHolder = this._any;
    var anySerializer = null;
    var anyXmlTag = null;
    var anyXmlNsDef = null;
    var anyData = null;
    var anyStartTag;
    if (anyHolder != null && !anyHolder.raw) {
     anySerializer = cxfjsutils.interfaceObject.globalElementSerializers[anyHolder.qname];
     anyXmlTag = 'cxfjsany6:' + anyHolder.localName;
     anyXmlNsDef = 'xmlns:cxfjsany6=\'' + anyHolder.namespaceURI + '\'';
     anyStartTag = '<' + anyXmlTag + ' ' + anyXmlNsDef + '>';
     anyEndTag = '</' + anyXmlTag + '>';
     anyEmptyTag = '<' + anyXmlTag + ' ' + anyXmlNsDef + '/>';
     anyData = anyHolder.object;
    }
    if (anyHolder != null && anyHolder.raw) {
     xml = xml + anyHolder.xml;
    } else {
     if (anyHolder == null || anyData == null) {
      throw 'null value for required any item';
     }
     for (var ax = 0;ax < anyData.length;ax ++) {
      if (anyData[ax] == null) {
       xml = xml + anyEmptyTag;
      } else {
       if (anySerializer) {
        xml = xml + anySerializer.call(anyData[ax], cxfjsutils, anyXmlTag, anyXmlNsDef);
       } else {
        xml = xml + anyStartTag;
        xml = xml + cxfjsutils.escapeXmlEntities(anyData[ax]);
        xml = xml + anyEndTag;
       }
      }
     }
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

www_w3_org_2005_08_addressing_EndpointReferenceType.prototype.serialize = www_w3_org_2005_08_addressing_EndpointReferenceType_serialize;

function www_w3_org_2005_08_addressing_EndpointReferenceType_deserialize (cxfjsutils, element) {
    var newobject = new www_w3_org_2005_08_addressing_EndpointReferenceType();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    cxfjsutils.trace('processing Address');
    var value = null;
    if (!cxfjsutils.isElementNil(curElement)) {
     value = cxfjsutils.getNodeText(curElement);
     item = value;
    }
    newobject.setAddress(item);
    var item = null;
    if (curElement != null) {
     curElement = cxfjsutils.getNextElementSibling(curElement);
    }
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    cxfjsutils.trace('processing ReferenceParameters');
    if (curElement != null && cxfjsutils.isNodeNamedNS(curElement, 'http://www.w3.org/2005/08/addressing', 'ReferenceParameters')) {
     var value = null;
     if (!cxfjsutils.isElementNil(curElement)) {
      item = www_w3_org_2005_08_addressing_ReferenceParametersType_deserialize(cxfjsutils, curElement);
     }
     newobject.setReferenceParameters(item);
     var item = null;
     if (curElement != null) {
      curElement = cxfjsutils.getNextElementSibling(curElement);
     }
    }
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    cxfjsutils.trace('processing Metadata');
    if (curElement != null && cxfjsutils.isNodeNamedNS(curElement, 'http://www.w3.org/2005/08/addressing', 'Metadata')) {
     var value = null;
     if (!cxfjsutils.isElementNil(curElement)) {
      item = www_w3_org_2005_08_addressing_MetadataType_deserialize(cxfjsutils, curElement);
     }
     newobject.setMetadata(item);
     var item = null;
     if (curElement != null) {
      curElement = cxfjsutils.getNextElementSibling(curElement);
     }
    }
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    var anyObject = [];
    var matcher = new org_apache_cxf_any_ns_matcher(org_apache_cxf_any_ns_matcher.OTHER, 'http://www.w3.org/2005/08/addressing', [], null);
    var anyNeeded = 0;
    var anyAllowed = 0;
    while (anyNeeded > 0 || anyAllowed > 0) {
     var anyURI;
     var anyLocalPart;
     var anyMatched = false;
     if (curElement) {
      anyURI = cxfjsutils.getElementNamespaceURI(curElement);
      anyLocalPart = cxfjsutils.getNodeLocalName(curElement);
      var anyQName = '{' + anyURI + '}' + anyLocalPart;
      cxfjsutils.trace('any match: ' + anyQName);
      anyMatched = matcher.match(anyURI, anyLocalPart)
      cxfjsutils.trace(' --> ' + anyMatched);
     }
     if (anyMatched) {
      anyDeserializer = cxfjsutils.interfaceObject.globalElementDeserializers[anyQName];
      cxfjsutils.trace(' deserializer: ' + anyDeserializer);
      if (anyDeserializer) {
       var anyValue = anyDeserializer(cxfjsutils, curElement);
      } else {
       var anyValue = curElement.nodeValue;
      }
      anyObject.push(anyValue);
      anyNeeded--;
      anyAllowed--;
      curElement = cxfjsutils.getNextElementSibling(curElement);
     } else {
      if (anyNeeded > 0) {
       throw 'not enough ws:any elements';
      }
     }
    }
    var anyHolder = new org_apache_cxf_any_holder(anyURI, anyLocalPart, anyValue);
    newobject.setAny(anyHolder);
    return newobject;
}

//
// Constructor for XML Schema item {http://www.w3.org/2005/08/addressing}AttributedAnyType
//
function www_w3_org_2005_08_addressing_AttributedAnyType () {
    this.typeMarker = 'www_w3_org_2005_08_addressing_AttributedAnyType';
    this._any = null;
}

//
// accessor is www_w3_org_2005_08_addressing_AttributedAnyType.prototype.getAny
// element get for any
// - xs:any
// - required element
//
// element set for any
// setter function is is www_w3_org_2005_08_addressing_AttributedAnyType.prototype.setAny
//
function www_w3_org_2005_08_addressing_AttributedAnyType_getAny() { return this._any;}

www_w3_org_2005_08_addressing_AttributedAnyType.prototype.getAny = www_w3_org_2005_08_addressing_AttributedAnyType_getAny;

function www_w3_org_2005_08_addressing_AttributedAnyType_setAny(value) { this._any = value;}

www_w3_org_2005_08_addressing_AttributedAnyType.prototype.setAny = www_w3_org_2005_08_addressing_AttributedAnyType_setAny;
//
// Serialize {http://www.w3.org/2005/08/addressing}AttributedAnyType
//
function www_w3_org_2005_08_addressing_AttributedAnyType_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     xml = xml + ' ';
     xml = xml + 'xmlns:jns0=\'http://www.w3.org/2005/08/addressing\' ';
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    var anyHolder = this._any;
    var anySerializer = null;
    var anyXmlTag = null;
    var anyXmlNsDef = null;
    var anyData = null;
    var anyStartTag;
    if (anyHolder != null && !anyHolder.raw) {
     anySerializer = cxfjsutils.interfaceObject.globalElementSerializers[anyHolder.qname];
     anyXmlTag = 'cxfjsany7:' + anyHolder.localName;
     anyXmlNsDef = 'xmlns:cxfjsany7=\'' + anyHolder.namespaceURI + '\'';
     anyStartTag = '<' + anyXmlTag + ' ' + anyXmlNsDef + '>';
     anyEndTag = '</' + anyXmlTag + '>';
     anyEmptyTag = '<' + anyXmlTag + ' ' + anyXmlNsDef + '/>';
     anyData = anyHolder.object;
    }
    if (anyHolder != null && anyHolder.raw) {
     xml = xml + anyHolder.xml;
    } else {
     if (anyHolder == null || anyData == null) {
      throw 'null value for required any item';
     }
     if (anySerializer) {
      xml = xml + anySerializer.call(anyData, cxfjsutils, anyXmlTag, anyXmlNsDef);
     } else {
      xml = xml + anyStartTag;
      xml = xml + cxfjsutils.escapeXmlEntities(anyData);
      xml = xml + anyEndTag;
     }
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

www_w3_org_2005_08_addressing_AttributedAnyType.prototype.serialize = www_w3_org_2005_08_addressing_AttributedAnyType_serialize;

function www_w3_org_2005_08_addressing_AttributedAnyType_deserialize (cxfjsutils, element) {
    var newobject = new www_w3_org_2005_08_addressing_AttributedAnyType();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    var anyObject = null;
    var matcher = new org_apache_cxf_any_ns_matcher(org_apache_cxf_any_ns_matcher.ANY, 'http://www.w3.org/2005/08/addressing', [], null);
    var anyNeeded = 1;
    var anyAllowed = 1;
    while (anyNeeded > 0 || anyAllowed > 0) {
     var anyURI;
     var anyLocalPart;
     var anyMatched = false;
     if (curElement) {
      anyURI = cxfjsutils.getElementNamespaceURI(curElement);
      anyLocalPart = cxfjsutils.getNodeLocalName(curElement);
      var anyQName = '{' + anyURI + '}' + anyLocalPart;
      cxfjsutils.trace('any match: ' + anyQName);
      anyMatched = matcher.match(anyURI, anyLocalPart)
      cxfjsutils.trace(' --> ' + anyMatched);
     }
     if (anyMatched) {
      anyDeserializer = cxfjsutils.interfaceObject.globalElementDeserializers[anyQName];
      cxfjsutils.trace(' deserializer: ' + anyDeserializer);
      if (anyDeserializer) {
       var anyValue = anyDeserializer(cxfjsutils, curElement);
      } else {
       var anyValue = curElement.nodeValue;
      }
      anyObject = anyValue;
      anyNeeded--;
      anyAllowed--;
      curElement = cxfjsutils.getNextElementSibling(curElement);
     } else {
      if (anyNeeded > 0) {
       throw 'not enough ws:any elements';
      }
     }
    }
    var anyHolder = new org_apache_cxf_any_holder(anyURI, anyLocalPart, anyValue);
    newobject.setAny(anyHolder);
    return newobject;
}

//
// Definitions for schema: http://ws.service.hyperimage.org/
//  http://hyperimage.eu:8080/HIEditor_2.0-Service/HILoginService?xsd=1
//
//
// Constructor for XML Schema item {http://ws.service.hyperimage.org/}getPPGLocationResponse
//
function ws_service_hyperimage_org__getPPGLocationResponse () {
    this.typeMarker = 'ws_service_hyperimage_org__getPPGLocationResponse';
    this._return = null;
}

//
// accessor is ws_service_hyperimage_org__getPPGLocationResponse.prototype.getReturn
// element get for return
// - element type is {http://www.w3.org/2001/XMLSchema}string
// - optional element
//
// element set for return
// setter function is is ws_service_hyperimage_org__getPPGLocationResponse.prototype.setReturn
//
function ws_service_hyperimage_org__getPPGLocationResponse_getReturn() { return this._return;}

ws_service_hyperimage_org__getPPGLocationResponse.prototype.getReturn = ws_service_hyperimage_org__getPPGLocationResponse_getReturn;

function ws_service_hyperimage_org__getPPGLocationResponse_setReturn(value) { this._return = value;}

ws_service_hyperimage_org__getPPGLocationResponse.prototype.setReturn = ws_service_hyperimage_org__getPPGLocationResponse_setReturn;
//
// Serialize {http://ws.service.hyperimage.org/}getPPGLocationResponse
//
function ws_service_hyperimage_org__getPPGLocationResponse_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     xml = xml + ' ';
     xml = xml + 'xmlns:jns0=\'http://www.w3.org/2005/08/addressing\' ';
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    // block for local variables
    {
     if (this._return != null) {
      xml = xml + '<return>';
      xml = xml + cxfjsutils.escapeXmlEntities(this._return);
      xml = xml + '</return>';
     }
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

ws_service_hyperimage_org__getPPGLocationResponse.prototype.serialize = ws_service_hyperimage_org__getPPGLocationResponse_serialize;

function ws_service_hyperimage_org__getPPGLocationResponse_deserialize (cxfjsutils, element) {
    var newobject = new ws_service_hyperimage_org__getPPGLocationResponse();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    cxfjsutils.trace('processing return');
    if (curElement != null && cxfjsutils.isNodeNamedNS(curElement, '', 'return')) {
     var value = null;
     if (!cxfjsutils.isElementNil(curElement)) {
      value = cxfjsutils.getNodeText(curElement);
      item = value;
     }
     newobject.setReturn(item);
     var item = null;
     if (curElement != null) {
      curElement = cxfjsutils.getNextElementSibling(curElement);
     }
    }
    return newobject;
}

//
// Constructor for XML Schema item {http://ws.service.hyperimage.org/}HIMaintenanceModeException
//
function ws_service_hyperimage_org__HIMaintenanceModeException () {
    this.typeMarker = 'ws_service_hyperimage_org__HIMaintenanceModeException';
    this._message = null;
}

//
// accessor is ws_service_hyperimage_org__HIMaintenanceModeException.prototype.getMessage
// element get for message
// - element type is {http://www.w3.org/2001/XMLSchema}string
// - optional element
//
// element set for message
// setter function is is ws_service_hyperimage_org__HIMaintenanceModeException.prototype.setMessage
//
function ws_service_hyperimage_org__HIMaintenanceModeException_getMessage() { return this._message;}

ws_service_hyperimage_org__HIMaintenanceModeException.prototype.getMessage = ws_service_hyperimage_org__HIMaintenanceModeException_getMessage;

function ws_service_hyperimage_org__HIMaintenanceModeException_setMessage(value) { this._message = value;}

ws_service_hyperimage_org__HIMaintenanceModeException.prototype.setMessage = ws_service_hyperimage_org__HIMaintenanceModeException_setMessage;
//
// Serialize {http://ws.service.hyperimage.org/}HIMaintenanceModeException
//
function ws_service_hyperimage_org__HIMaintenanceModeException_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     xml = xml + ' ';
     xml = xml + 'xmlns:jns0=\'http://www.w3.org/2005/08/addressing\' ';
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    // block for local variables
    {
     if (this._message != null) {
      xml = xml + '<message>';
      xml = xml + cxfjsutils.escapeXmlEntities(this._message);
      xml = xml + '</message>';
     }
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

ws_service_hyperimage_org__HIMaintenanceModeException.prototype.serialize = ws_service_hyperimage_org__HIMaintenanceModeException_serialize;

function ws_service_hyperimage_org__HIMaintenanceModeException_deserialize (cxfjsutils, element) {
    var newobject = new ws_service_hyperimage_org__HIMaintenanceModeException();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    cxfjsutils.trace('processing message');
    if (curElement != null && cxfjsutils.isNodeNamedNS(curElement, '', 'message')) {
     var value = null;
     if (!cxfjsutils.isElementNil(curElement)) {
      value = cxfjsutils.getNodeText(curElement);
      item = value;
     }
     newobject.setMessage(item);
     var item = null;
     if (curElement != null) {
      curElement = cxfjsutils.getNextElementSibling(curElement);
     }
    }
    return newobject;
}

//
// Constructor for XML Schema item {http://ws.service.hyperimage.org/}logout
//
function ws_service_hyperimage_org__logout () {
    this.typeMarker = 'ws_service_hyperimage_org__logout';
    this._editorRef = null;
}

//
// accessor is ws_service_hyperimage_org__logout.prototype.getEditorRef
// element get for editorRef
// - element type is {http://www.w3.org/2005/08/addressing}EndpointReferenceType
// - optional element
//
// element set for editorRef
// setter function is is ws_service_hyperimage_org__logout.prototype.setEditorRef
//
function ws_service_hyperimage_org__logout_getEditorRef() { return this._editorRef;}

ws_service_hyperimage_org__logout.prototype.getEditorRef = ws_service_hyperimage_org__logout_getEditorRef;

function ws_service_hyperimage_org__logout_setEditorRef(value) { this._editorRef = value;}

ws_service_hyperimage_org__logout.prototype.setEditorRef = ws_service_hyperimage_org__logout_setEditorRef;
//
// Serialize {http://ws.service.hyperimage.org/}logout
//
function ws_service_hyperimage_org__logout_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     xml = xml + ' ';
     xml = xml + 'xmlns:jns0=\'http://www.w3.org/2005/08/addressing\' ';
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    // block for local variables
    {
     if (this._editorRef != null) {
      xml = xml + this._editorRef.serialize(cxfjsutils, 'editorRef', null);
     }
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

ws_service_hyperimage_org__logout.prototype.serialize = ws_service_hyperimage_org__logout_serialize;

function ws_service_hyperimage_org__logout_deserialize (cxfjsutils, element) {
    var newobject = new ws_service_hyperimage_org__logout();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    cxfjsutils.trace('processing editorRef');
    if (curElement != null && cxfjsutils.isNodeNamedNS(curElement, '', 'editorRef')) {
     var value = null;
     if (!cxfjsutils.isElementNil(curElement)) {
      item = www_w3_org_2005_08_addressing_EndpointReferenceType_deserialize(cxfjsutils, curElement);
     }
     newobject.setEditorRef(item);
     var item = null;
     if (curElement != null) {
      curElement = cxfjsutils.getNextElementSibling(curElement);
     }
    }
    return newobject;
}

//
// Constructor for XML Schema item {http://ws.service.hyperimage.org/}authenticate
//
function ws_service_hyperimage_org__authenticate () {
    this.typeMarker = 'ws_service_hyperimage_org__authenticate';
    this._username = null;
    this._password = null;
}

//
// accessor is ws_service_hyperimage_org__authenticate.prototype.getUsername
// element get for username
// - element type is {http://www.w3.org/2001/XMLSchema}string
// - optional element
//
// element set for username
// setter function is is ws_service_hyperimage_org__authenticate.prototype.setUsername
//
function ws_service_hyperimage_org__authenticate_getUsername() { return this._username;}

ws_service_hyperimage_org__authenticate.prototype.getUsername = ws_service_hyperimage_org__authenticate_getUsername;

function ws_service_hyperimage_org__authenticate_setUsername(value) { this._username = value;}

ws_service_hyperimage_org__authenticate.prototype.setUsername = ws_service_hyperimage_org__authenticate_setUsername;
//
// accessor is ws_service_hyperimage_org__authenticate.prototype.getPassword
// element get for password
// - element type is {http://www.w3.org/2001/XMLSchema}string
// - optional element
//
// element set for password
// setter function is is ws_service_hyperimage_org__authenticate.prototype.setPassword
//
function ws_service_hyperimage_org__authenticate_getPassword() { return this._password;}

ws_service_hyperimage_org__authenticate.prototype.getPassword = ws_service_hyperimage_org__authenticate_getPassword;

function ws_service_hyperimage_org__authenticate_setPassword(value) { this._password = value;}

ws_service_hyperimage_org__authenticate.prototype.setPassword = ws_service_hyperimage_org__authenticate_setPassword;
//
// Serialize {http://ws.service.hyperimage.org/}authenticate
//
function ws_service_hyperimage_org__authenticate_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     xml = xml + ' ';
     xml = xml + 'xmlns:jns0=\'http://www.w3.org/2005/08/addressing\' ';
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    // block for local variables
    {
     if (this._username != null) {
      xml = xml + '<username>';
      xml = xml + cxfjsutils.escapeXmlEntities(this._username);
      xml = xml + '</username>';
     }
    }
    // block for local variables
    {
     if (this._password != null) {
      xml = xml + '<password>';
      xml = xml + cxfjsutils.escapeXmlEntities(this._password);
      xml = xml + '</password>';
     }
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

ws_service_hyperimage_org__authenticate.prototype.serialize = ws_service_hyperimage_org__authenticate_serialize;

function ws_service_hyperimage_org__authenticate_deserialize (cxfjsutils, element) {
    var newobject = new ws_service_hyperimage_org__authenticate();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    cxfjsutils.trace('processing username');
    if (curElement != null && cxfjsutils.isNodeNamedNS(curElement, '', 'username')) {
     var value = null;
     if (!cxfjsutils.isElementNil(curElement)) {
      value = cxfjsutils.getNodeText(curElement);
      item = value;
     }
     newobject.setUsername(item);
     var item = null;
     if (curElement != null) {
      curElement = cxfjsutils.getNextElementSibling(curElement);
     }
    }
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    cxfjsutils.trace('processing password');
    if (curElement != null && cxfjsutils.isNodeNamedNS(curElement, '', 'password')) {
     var value = null;
     if (!cxfjsutils.isElementNil(curElement)) {
      value = cxfjsutils.getNodeText(curElement);
      item = value;
     }
     newobject.setPassword(item);
     var item = null;
     if (curElement != null) {
      curElement = cxfjsutils.getNextElementSibling(curElement);
     }
    }
    return newobject;
}

//
// Constructor for XML Schema item {http://ws.service.hyperimage.org/}getVersionIDResponse
//
function ws_service_hyperimage_org__getVersionIDResponse () {
    this.typeMarker = 'ws_service_hyperimage_org__getVersionIDResponse';
    this._return = null;
}

//
// accessor is ws_service_hyperimage_org__getVersionIDResponse.prototype.getReturn
// element get for return
// - element type is {http://www.w3.org/2001/XMLSchema}string
// - optional element
//
// element set for return
// setter function is is ws_service_hyperimage_org__getVersionIDResponse.prototype.setReturn
//
function ws_service_hyperimage_org__getVersionIDResponse_getReturn() { return this._return;}

ws_service_hyperimage_org__getVersionIDResponse.prototype.getReturn = ws_service_hyperimage_org__getVersionIDResponse_getReturn;

function ws_service_hyperimage_org__getVersionIDResponse_setReturn(value) { this._return = value;}

ws_service_hyperimage_org__getVersionIDResponse.prototype.setReturn = ws_service_hyperimage_org__getVersionIDResponse_setReturn;
//
// Serialize {http://ws.service.hyperimage.org/}getVersionIDResponse
//
function ws_service_hyperimage_org__getVersionIDResponse_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     xml = xml + ' ';
     xml = xml + 'xmlns:jns0=\'http://www.w3.org/2005/08/addressing\' ';
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    // block for local variables
    {
     if (this._return != null) {
      xml = xml + '<return>';
      xml = xml + cxfjsutils.escapeXmlEntities(this._return);
      xml = xml + '</return>';
     }
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

ws_service_hyperimage_org__getVersionIDResponse.prototype.serialize = ws_service_hyperimage_org__getVersionIDResponse_serialize;

function ws_service_hyperimage_org__getVersionIDResponse_deserialize (cxfjsutils, element) {
    var newobject = new ws_service_hyperimage_org__getVersionIDResponse();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    cxfjsutils.trace('processing return');
    if (curElement != null && cxfjsutils.isNodeNamedNS(curElement, '', 'return')) {
     var value = null;
     if (!cxfjsutils.isElementNil(curElement)) {
      value = cxfjsutils.getNodeText(curElement);
      item = value;
     }
     newobject.setReturn(item);
     var item = null;
     if (curElement != null) {
      curElement = cxfjsutils.getNextElementSibling(curElement);
     }
    }
    return newobject;
}

//
// Constructor for XML Schema item {http://ws.service.hyperimage.org/}getPPGLocation
//
function ws_service_hyperimage_org__getPPGLocation () {
    this.typeMarker = 'ws_service_hyperimage_org__getPPGLocation';
}

//
// Serialize {http://ws.service.hyperimage.org/}getPPGLocation
//
function ws_service_hyperimage_org__getPPGLocation_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     xml = xml + ' ';
     xml = xml + 'xmlns:jns0=\'http://www.w3.org/2005/08/addressing\' ';
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

ws_service_hyperimage_org__getPPGLocation.prototype.serialize = ws_service_hyperimage_org__getPPGLocation_serialize;

function ws_service_hyperimage_org__getPPGLocation_deserialize (cxfjsutils, element) {
    var newobject = new ws_service_hyperimage_org__getPPGLocation();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    return newobject;
}

//
// Constructor for XML Schema item {http://ws.service.hyperimage.org/}logoutResponse
//
function ws_service_hyperimage_org__logoutResponse () {
    this.typeMarker = 'ws_service_hyperimage_org__logoutResponse';
    this._return = '';
}

//
// accessor is ws_service_hyperimage_org__logoutResponse.prototype.getReturn
// element get for return
// - element type is {http://www.w3.org/2001/XMLSchema}boolean
// - required element
//
// element set for return
// setter function is is ws_service_hyperimage_org__logoutResponse.prototype.setReturn
//
function ws_service_hyperimage_org__logoutResponse_getReturn() { return this._return;}

ws_service_hyperimage_org__logoutResponse.prototype.getReturn = ws_service_hyperimage_org__logoutResponse_getReturn;

function ws_service_hyperimage_org__logoutResponse_setReturn(value) { this._return = value;}

ws_service_hyperimage_org__logoutResponse.prototype.setReturn = ws_service_hyperimage_org__logoutResponse_setReturn;
//
// Serialize {http://ws.service.hyperimage.org/}logoutResponse
//
function ws_service_hyperimage_org__logoutResponse_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     xml = xml + ' ';
     xml = xml + 'xmlns:jns0=\'http://www.w3.org/2005/08/addressing\' ';
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    // block for local variables
    {
     xml = xml + '<return>';
     xml = xml + cxfjsutils.escapeXmlEntities(this._return);
     xml = xml + '</return>';
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

ws_service_hyperimage_org__logoutResponse.prototype.serialize = ws_service_hyperimage_org__logoutResponse_serialize;

function ws_service_hyperimage_org__logoutResponse_deserialize (cxfjsutils, element) {
    var newobject = new ws_service_hyperimage_org__logoutResponse();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    cxfjsutils.trace('processing return');
    var value = null;
    if (!cxfjsutils.isElementNil(curElement)) {
     value = cxfjsutils.getNodeText(curElement);
     item = (value == 'true');
    }
    newobject.setReturn(item);
    var item = null;
    if (curElement != null) {
     curElement = cxfjsutils.getNextElementSibling(curElement);
    }
    return newobject;
}

//
// Constructor for XML Schema item {http://ws.service.hyperimage.org/}HIParameterException
//
function ws_service_hyperimage_org__HIParameterException () {
    this.typeMarker = 'ws_service_hyperimage_org__HIParameterException';
    this._message = null;
}

//
// accessor is ws_service_hyperimage_org__HIParameterException.prototype.getMessage
// element get for message
// - element type is {http://www.w3.org/2001/XMLSchema}string
// - optional element
//
// element set for message
// setter function is is ws_service_hyperimage_org__HIParameterException.prototype.setMessage
//
function ws_service_hyperimage_org__HIParameterException_getMessage() { return this._message;}

ws_service_hyperimage_org__HIParameterException.prototype.getMessage = ws_service_hyperimage_org__HIParameterException_getMessage;

function ws_service_hyperimage_org__HIParameterException_setMessage(value) { this._message = value;}

ws_service_hyperimage_org__HIParameterException.prototype.setMessage = ws_service_hyperimage_org__HIParameterException_setMessage;
//
// Serialize {http://ws.service.hyperimage.org/}HIParameterException
//
function ws_service_hyperimage_org__HIParameterException_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     xml = xml + ' ';
     xml = xml + 'xmlns:jns0=\'http://www.w3.org/2005/08/addressing\' ';
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    // block for local variables
    {
     if (this._message != null) {
      xml = xml + '<message>';
      xml = xml + cxfjsutils.escapeXmlEntities(this._message);
      xml = xml + '</message>';
     }
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

ws_service_hyperimage_org__HIParameterException.prototype.serialize = ws_service_hyperimage_org__HIParameterException_serialize;

function ws_service_hyperimage_org__HIParameterException_deserialize (cxfjsutils, element) {
    var newobject = new ws_service_hyperimage_org__HIParameterException();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    cxfjsutils.trace('processing message');
    if (curElement != null && cxfjsutils.isNodeNamedNS(curElement, '', 'message')) {
     var value = null;
     if (!cxfjsutils.isElementNil(curElement)) {
      value = cxfjsutils.getNodeText(curElement);
      item = value;
     }
     newobject.setMessage(item);
     var item = null;
     if (curElement != null) {
      curElement = cxfjsutils.getNextElementSibling(curElement);
     }
    }
    return newobject;
}

//
// Constructor for XML Schema item {http://ws.service.hyperimage.org/}authenticateResponse
//
function ws_service_hyperimage_org__authenticateResponse () {
    this.typeMarker = 'ws_service_hyperimage_org__authenticateResponse';
    this._return = null;
}

//
// accessor is ws_service_hyperimage_org__authenticateResponse.prototype.getReturn
// element get for return
// - element type is {http://www.w3.org/2005/08/addressing}EndpointReferenceType
// - optional element
//
// element set for return
// setter function is is ws_service_hyperimage_org__authenticateResponse.prototype.setReturn
//
function ws_service_hyperimage_org__authenticateResponse_getReturn() { return this._return;}

ws_service_hyperimage_org__authenticateResponse.prototype.getReturn = ws_service_hyperimage_org__authenticateResponse_getReturn;

function ws_service_hyperimage_org__authenticateResponse_setReturn(value) { this._return = value;}

ws_service_hyperimage_org__authenticateResponse.prototype.setReturn = ws_service_hyperimage_org__authenticateResponse_setReturn;
//
// Serialize {http://ws.service.hyperimage.org/}authenticateResponse
//
function ws_service_hyperimage_org__authenticateResponse_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     xml = xml + ' ';
     xml = xml + 'xmlns:jns0=\'http://www.w3.org/2005/08/addressing\' ';
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    // block for local variables
    {
     if (this._return != null) {
      xml = xml + this._return.serialize(cxfjsutils, 'return', null);
     }
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

ws_service_hyperimage_org__authenticateResponse.prototype.serialize = ws_service_hyperimage_org__authenticateResponse_serialize;

function ws_service_hyperimage_org__authenticateResponse_deserialize (cxfjsutils, element) {
    var newobject = new ws_service_hyperimage_org__authenticateResponse();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    cxfjsutils.trace('curElement: ' + cxfjsutils.traceElementName(curElement));
    cxfjsutils.trace('processing return');
    if (curElement != null && cxfjsutils.isNodeNamedNS(curElement, '', 'return')) {
     var value = null;
     if (!cxfjsutils.isElementNil(curElement)) {
      item = www_w3_org_2005_08_addressing_EndpointReferenceType_deserialize(cxfjsutils, curElement);
     }
     newobject.setReturn(item);
     var item = null;
     if (curElement != null) {
      curElement = cxfjsutils.getNextElementSibling(curElement);
     }
    }
    return newobject;
}

//
// Constructor for XML Schema item {http://ws.service.hyperimage.org/}getVersionID
//
function ws_service_hyperimage_org__getVersionID () {
    this.typeMarker = 'ws_service_hyperimage_org__getVersionID';
}

//
// Serialize {http://ws.service.hyperimage.org/}getVersionID
//
function ws_service_hyperimage_org__getVersionID_serialize(cxfjsutils, elementName, extraNamespaces) {
    var xml = '';
    if (elementName != null) {
     xml = xml + '<';
     xml = xml + elementName;
     xml = xml + ' ';
     xml = xml + 'xmlns:jns0=\'http://www.w3.org/2005/08/addressing\' ';
     if (extraNamespaces) {
      xml = xml + ' ' + extraNamespaces;
     }
     xml = xml + '>';
    }
    if (elementName != null) {
     xml = xml + '</';
     xml = xml + elementName;
     xml = xml + '>';
    }
    return xml;
}

ws_service_hyperimage_org__getVersionID.prototype.serialize = ws_service_hyperimage_org__getVersionID_serialize;

function ws_service_hyperimage_org__getVersionID_deserialize (cxfjsutils, element) {
    var newobject = new ws_service_hyperimage_org__getVersionID();
    cxfjsutils.trace('element: ' + cxfjsutils.traceElementName(element));
    var curElement = cxfjsutils.getFirstElementChild(element);
    var item;
    return newobject;
}

//
// Definitions for schema: null
//  http://hyperimage.eu:8080/HIEditor_2.0-Service/HILoginService?wsdl#types1
//
//
// Definitions for service: {http://ws.service.hyperimage.org/}HILoginService
//

// Javascript for {http://ws.service.hyperimage.org/}HILogin

function ws_service_hyperimage_org__HILogin () {
    this.jsutils = new CxfApacheOrgUtil();
    this.jsutils.interfaceObject = this;
    this.synchronous = false;
    this.url = null;
    this.client = null;
    this.response = null;
    this.globalElementSerializers = [];
    this.globalElementDeserializers = [];
    this.globalElementSerializers['{http://www.w3.org/2005/08/addressing}EndpointReferenceType'] = www_w3_org_2005_08_addressing_EndpointReferenceType_serialize;
    this.globalElementDeserializers['{http://www.w3.org/2005/08/addressing}EndpointReferenceType'] = www_w3_org_2005_08_addressing_EndpointReferenceType_deserialize;
    this.globalElementSerializers['{http://www.w3.org/2005/08/addressing}AttributedAnyType'] = www_w3_org_2005_08_addressing_AttributedAnyType_serialize;
    this.globalElementDeserializers['{http://www.w3.org/2005/08/addressing}AttributedAnyType'] = www_w3_org_2005_08_addressing_AttributedAnyType_deserialize;
    this.globalElementSerializers['{http://www.w3.org/2005/08/addressing}ProblemActionType'] = www_w3_org_2005_08_addressing_ProblemActionType_serialize;
    this.globalElementDeserializers['{http://www.w3.org/2005/08/addressing}ProblemActionType'] = www_w3_org_2005_08_addressing_ProblemActionType_deserialize;
    this.globalElementSerializers['{http://www.w3.org/2005/08/addressing}EndpointReferenceType'] = www_w3_org_2005_08_addressing_EndpointReferenceType_serialize;
    this.globalElementDeserializers['{http://www.w3.org/2005/08/addressing}EndpointReferenceType'] = www_w3_org_2005_08_addressing_EndpointReferenceType_deserialize;
    this.globalElementSerializers['{http://www.w3.org/2005/08/addressing}EndpointReferenceType'] = www_w3_org_2005_08_addressing_EndpointReferenceType_serialize;
    this.globalElementDeserializers['{http://www.w3.org/2005/08/addressing}EndpointReferenceType'] = www_w3_org_2005_08_addressing_EndpointReferenceType_deserialize;
    this.globalElementSerializers['{http://www.w3.org/2005/08/addressing}MetadataType'] = www_w3_org_2005_08_addressing_MetadataType_serialize;
    this.globalElementDeserializers['{http://www.w3.org/2005/08/addressing}MetadataType'] = www_w3_org_2005_08_addressing_MetadataType_deserialize;
    this.globalElementSerializers['{http://www.w3.org/2005/08/addressing}EndpointReferenceType'] = www_w3_org_2005_08_addressing_EndpointReferenceType_serialize;
    this.globalElementDeserializers['{http://www.w3.org/2005/08/addressing}EndpointReferenceType'] = www_w3_org_2005_08_addressing_EndpointReferenceType_deserialize;
    this.globalElementSerializers['{http://www.w3.org/2005/08/addressing}MetadataType'] = www_w3_org_2005_08_addressing_MetadataType_serialize;
    this.globalElementDeserializers['{http://www.w3.org/2005/08/addressing}MetadataType'] = www_w3_org_2005_08_addressing_MetadataType_deserialize;
    this.globalElementSerializers['{http://www.w3.org/2005/08/addressing}ReferenceParametersType'] = www_w3_org_2005_08_addressing_ReferenceParametersType_serialize;
    this.globalElementDeserializers['{http://www.w3.org/2005/08/addressing}ReferenceParametersType'] = www_w3_org_2005_08_addressing_ReferenceParametersType_deserialize;
    this.globalElementSerializers['{http://www.w3.org/2005/08/addressing}ProblemActionType'] = www_w3_org_2005_08_addressing_ProblemActionType_serialize;
    this.globalElementDeserializers['{http://www.w3.org/2005/08/addressing}ProblemActionType'] = www_w3_org_2005_08_addressing_ProblemActionType_deserialize;
    this.globalElementSerializers['{http://www.w3.org/2005/08/addressing}EndpointReferenceType'] = www_w3_org_2005_08_addressing_EndpointReferenceType_serialize;
    this.globalElementDeserializers['{http://www.w3.org/2005/08/addressing}EndpointReferenceType'] = www_w3_org_2005_08_addressing_EndpointReferenceType_deserialize;
    this.globalElementSerializers['{http://www.w3.org/2005/08/addressing}AttributedAnyType'] = www_w3_org_2005_08_addressing_AttributedAnyType_serialize;
    this.globalElementDeserializers['{http://www.w3.org/2005/08/addressing}AttributedAnyType'] = www_w3_org_2005_08_addressing_AttributedAnyType_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}getPPGLocationResponse'] = ws_service_hyperimage_org__getPPGLocationResponse_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}getPPGLocationResponse'] = ws_service_hyperimage_org__getPPGLocationResponse_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}HIMaintenanceModeException'] = ws_service_hyperimage_org__HIMaintenanceModeException_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}HIMaintenanceModeException'] = ws_service_hyperimage_org__HIMaintenanceModeException_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}logout'] = ws_service_hyperimage_org__logout_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}logout'] = ws_service_hyperimage_org__logout_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}authenticate'] = ws_service_hyperimage_org__authenticate_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}authenticate'] = ws_service_hyperimage_org__authenticate_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}getVersionIDResponse'] = ws_service_hyperimage_org__getVersionIDResponse_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}getVersionIDResponse'] = ws_service_hyperimage_org__getVersionIDResponse_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}getPPGLocation'] = ws_service_hyperimage_org__getPPGLocation_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}getPPGLocation'] = ws_service_hyperimage_org__getPPGLocation_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}logoutResponse'] = ws_service_hyperimage_org__logoutResponse_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}logoutResponse'] = ws_service_hyperimage_org__logoutResponse_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}HIParameterException'] = ws_service_hyperimage_org__HIParameterException_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}HIParameterException'] = ws_service_hyperimage_org__HIParameterException_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}authenticateResponse'] = ws_service_hyperimage_org__authenticateResponse_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}authenticateResponse'] = ws_service_hyperimage_org__authenticateResponse_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}getVersionID'] = ws_service_hyperimage_org__getVersionID_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}getVersionID'] = ws_service_hyperimage_org__getVersionID_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}getPPGLocationResponse'] = ws_service_hyperimage_org__getPPGLocationResponse_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}getPPGLocationResponse'] = ws_service_hyperimage_org__getPPGLocationResponse_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}HIMaintenanceModeException'] = ws_service_hyperimage_org__HIMaintenanceModeException_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}HIMaintenanceModeException'] = ws_service_hyperimage_org__HIMaintenanceModeException_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}logout'] = ws_service_hyperimage_org__logout_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}logout'] = ws_service_hyperimage_org__logout_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}authenticate'] = ws_service_hyperimage_org__authenticate_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}authenticate'] = ws_service_hyperimage_org__authenticate_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}getVersionIDResponse'] = ws_service_hyperimage_org__getVersionIDResponse_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}getVersionIDResponse'] = ws_service_hyperimage_org__getVersionIDResponse_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}getPPGLocation'] = ws_service_hyperimage_org__getPPGLocation_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}getPPGLocation'] = ws_service_hyperimage_org__getPPGLocation_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}logoutResponse'] = ws_service_hyperimage_org__logoutResponse_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}logoutResponse'] = ws_service_hyperimage_org__logoutResponse_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}HIParameterException'] = ws_service_hyperimage_org__HIParameterException_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}HIParameterException'] = ws_service_hyperimage_org__HIParameterException_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}authenticateResponse'] = ws_service_hyperimage_org__authenticateResponse_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}authenticateResponse'] = ws_service_hyperimage_org__authenticateResponse_deserialize;
    this.globalElementSerializers['{http://ws.service.hyperimage.org/}getVersionID'] = ws_service_hyperimage_org__getVersionID_serialize;
    this.globalElementDeserializers['{http://ws.service.hyperimage.org/}getVersionID'] = ws_service_hyperimage_org__getVersionID_deserialize;
}

function ws_service_hyperimage_org__getPPGLocation_op_onsuccess(client, responseXml) {
    if (client.user_onsuccess) {
     var responseObject = null;
     var element = responseXml.documentElement;
     this.jsutils.trace('responseXml: ' + this.jsutils.traceElementName(element));
     element = this.jsutils.getFirstElementChild(element);
     this.jsutils.trace('first element child: ' + this.jsutils.traceElementName(element));
     while (!this.jsutils.isNodeNamedNS(element, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body')) {
      element = this.jsutils.getNextElementSibling(element);
      if (element == null) {
       throw 'No env:Body in message.'
      }
     }
     element = this.jsutils.getFirstElementChild(element);
     this.jsutils.trace('part element: ' + this.jsutils.traceElementName(element));
     this.jsutils.trace('calling ws_service_hyperimage_org__getPPGLocationResponse_deserializeResponse');
     responseObject = ws_service_hyperimage_org__getPPGLocationResponse_deserializeResponse(this.jsutils, element);
     client.user_onsuccess(responseObject);
    }
}

ws_service_hyperimage_org__HILogin.prototype.getPPGLocation_onsuccess = ws_service_hyperimage_org__getPPGLocation_op_onsuccess;

function ws_service_hyperimage_org__getPPGLocation_op_onerror(client) {
    if (client.user_onerror) {
     var httpStatus;
     var httpStatusText;
     try {
      httpStatus = client.req.status;
      httpStatusText = client.req.statusText;
     } catch(e) {
      httpStatus = -1;
      httpStatusText = 'Error opening connection to server';
     }
     client.user_onerror(httpStatus, httpStatusText);
    }
}

ws_service_hyperimage_org__HILogin.prototype.getPPGLocation_onerror = ws_service_hyperimage_org__getPPGLocation_op_onerror;

//
// Operation {http://ws.service.hyperimage.org/}getPPGLocation
// Wrapped operation.
//
function ws_service_hyperimage_org__getPPGLocation_op(successCallback, errorCallback) {
    this.client = new CxfApacheOrgClient(this.jsutils);
    var xml = null;
    var args = new Array(0);
    xml = this.getPPGLocation_serializeInput(this.jsutils, args);
    this.client.user_onsuccess = successCallback;
    this.client.user_onerror = errorCallback;
    var closureThis = this;
    this.client.onsuccess = function(client, responseXml) { closureThis.getPPGLocation_onsuccess(client, responseXml); };
    this.client.onerror = function(client) { closureThis.getPPGLocation_onerror(client); };
    var requestHeaders = [];
    requestHeaders['SOAPAction'] = '';
    this.jsutils.trace('synchronous = ' + this.synchronous);
    this.client.request(this.url, xml, null, this.synchronous, requestHeaders);
}

ws_service_hyperimage_org__HILogin.prototype.getPPGLocation = ws_service_hyperimage_org__getPPGLocation_op;

function ws_service_hyperimage_org__getPPGLocation_serializeInput(cxfjsutils, args) {
    var wrapperObj = new ws_service_hyperimage_org__getPPGLocation();
    var xml;
    xml = cxfjsutils.beginSoap11Message("xmlns:jns0='http://www.w3.org/2005/08/addressing' xmlns:jns1='http://ws.service.hyperimage.org/' ");
    // block for local variables
    {
     xml = xml + wrapperObj.serialize(cxfjsutils, 'jns1:getPPGLocation', null);
    }
    xml = xml + cxfjsutils.endSoap11Message();
    return xml;
}

ws_service_hyperimage_org__HILogin.prototype.getPPGLocation_serializeInput = ws_service_hyperimage_org__getPPGLocation_serializeInput;

function ws_service_hyperimage_org__getPPGLocationResponse_deserializeResponse(cxfjsutils, partElement) {
    var returnObject = ws_service_hyperimage_org__getPPGLocationResponse_deserialize (cxfjsutils, partElement);

    return returnObject;
}
function ws_service_hyperimage_org__getVersionID_op_onsuccess(client, responseXml) {
    if (client.user_onsuccess) {
     var responseObject = null;
     var element = responseXml.documentElement;
     this.jsutils.trace('responseXml: ' + this.jsutils.traceElementName(element));
     element = this.jsutils.getFirstElementChild(element);
     this.jsutils.trace('first element child: ' + this.jsutils.traceElementName(element));
     while (!this.jsutils.isNodeNamedNS(element, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body')) {
      element = this.jsutils.getNextElementSibling(element);
      if (element == null) {
       throw 'No env:Body in message.'
      }
     }
     element = this.jsutils.getFirstElementChild(element);
     this.jsutils.trace('part element: ' + this.jsutils.traceElementName(element));
     this.jsutils.trace('calling ws_service_hyperimage_org__getVersionIDResponse_deserializeResponse');
     responseObject = ws_service_hyperimage_org__getVersionIDResponse_deserializeResponse(this.jsutils, element);
     client.user_onsuccess(responseObject);
    }
}

ws_service_hyperimage_org__HILogin.prototype.getVersionID_onsuccess = ws_service_hyperimage_org__getVersionID_op_onsuccess;

function ws_service_hyperimage_org__getVersionID_op_onerror(client) {
    if (client.user_onerror) {
     var httpStatus;
     var httpStatusText;
     try {
      httpStatus = client.req.status;
      httpStatusText = client.req.statusText;
     } catch(e) {
      httpStatus = -1;
      httpStatusText = 'Error opening connection to server';
     }
     client.user_onerror(httpStatus, httpStatusText);
    }
}

ws_service_hyperimage_org__HILogin.prototype.getVersionID_onerror = ws_service_hyperimage_org__getVersionID_op_onerror;

//
// Operation {http://ws.service.hyperimage.org/}getVersionID
// Wrapped operation.
//
function ws_service_hyperimage_org__getVersionID_op(successCallback, errorCallback) {
    this.client = new CxfApacheOrgClient(this.jsutils);
    var xml = null;
    var args = new Array(0);
    xml = this.getVersionID_serializeInput(this.jsutils, args);
    this.client.user_onsuccess = successCallback;
    this.client.user_onerror = errorCallback;
    var closureThis = this;
    this.client.onsuccess = function(client, responseXml) { closureThis.getVersionID_onsuccess(client, responseXml); };
    this.client.onerror = function(client) { closureThis.getVersionID_onerror(client); };
    var requestHeaders = [];
    requestHeaders['SOAPAction'] = '';
    this.jsutils.trace('synchronous = ' + this.synchronous);
    this.client.request(this.url, xml, null, this.synchronous, requestHeaders);
}

ws_service_hyperimage_org__HILogin.prototype.getVersionID = ws_service_hyperimage_org__getVersionID_op;

function ws_service_hyperimage_org__getVersionID_serializeInput(cxfjsutils, args) {
    var wrapperObj = new ws_service_hyperimage_org__getVersionID();
    var xml;
    xml = cxfjsutils.beginSoap11Message("xmlns:jns0='http://www.w3.org/2005/08/addressing' xmlns:jns1='http://ws.service.hyperimage.org/' ");
    // block for local variables
    {
     xml = xml + wrapperObj.serialize(cxfjsutils, 'jns1:getVersionID', null);
    }
    xml = xml + cxfjsutils.endSoap11Message();
    return xml;
}

ws_service_hyperimage_org__HILogin.prototype.getVersionID_serializeInput = ws_service_hyperimage_org__getVersionID_serializeInput;

function ws_service_hyperimage_org__getVersionIDResponse_deserializeResponse(cxfjsutils, partElement) {
    var returnObject = ws_service_hyperimage_org__getVersionIDResponse_deserialize (cxfjsutils, partElement);

    return returnObject;
}
function ws_service_hyperimage_org__authenticate_op_onsuccess(client, responseXml) {
    if (client.user_onsuccess) {
     var responseObject = null;
     var element = responseXml.documentElement;
     this.jsutils.trace('responseXml: ' + this.jsutils.traceElementName(element));
     element = this.jsutils.getFirstElementChild(element);
     this.jsutils.trace('first element child: ' + this.jsutils.traceElementName(element));
     while (!this.jsutils.isNodeNamedNS(element, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body')) {
      element = this.jsutils.getNextElementSibling(element);
      if (element == null) {
       throw 'No env:Body in message.'
      }
     }
     element = this.jsutils.getFirstElementChild(element);
     this.jsutils.trace('part element: ' + this.jsutils.traceElementName(element));
     this.jsutils.trace('calling ws_service_hyperimage_org__authenticateResponse_deserializeResponse');
     responseObject = ws_service_hyperimage_org__authenticateResponse_deserializeResponse(this.jsutils, element);
     client.user_onsuccess(responseObject);
    }
}

ws_service_hyperimage_org__HILogin.prototype.authenticate_onsuccess = ws_service_hyperimage_org__authenticate_op_onsuccess;

function ws_service_hyperimage_org__authenticate_op_onerror(client) {
    if (client.user_onerror) {
     var httpStatus;
     var httpStatusText;
     try {
      httpStatus = client.req.status;
      httpStatusText = client.req.statusText;
     } catch(e) {
      httpStatus = -1;
      httpStatusText = 'Error opening connection to server';
     }
     client.user_onerror(httpStatus, httpStatusText);
    }
}

ws_service_hyperimage_org__HILogin.prototype.authenticate_onerror = ws_service_hyperimage_org__authenticate_op_onerror;

//
// Operation {http://ws.service.hyperimage.org/}authenticate
// Wrapped operation.
// parameter username
// - simple type {http://www.w3.org/2001/XMLSchema}string// parameter password
// - simple type {http://www.w3.org/2001/XMLSchema}string//
function ws_service_hyperimage_org__authenticate_op(successCallback, errorCallback, username, password) {
    this.client = new CxfApacheOrgClient(this.jsutils);
    var xml = null;
    var args = new Array(2);
    args[0] = username;
    args[1] = password;
    xml = this.authenticate_serializeInput(this.jsutils, args);
    this.client.user_onsuccess = successCallback;
    this.client.user_onerror = errorCallback;
    var closureThis = this;
    this.client.onsuccess = function(client, responseXml) { closureThis.authenticate_onsuccess(client, responseXml); };
    this.client.onerror = function(client) { closureThis.authenticate_onerror(client); };
    var requestHeaders = [];
    requestHeaders['SOAPAction'] = '';
    this.jsutils.trace('synchronous = ' + this.synchronous);
    this.client.request(this.url, xml, null, this.synchronous, requestHeaders);
}

ws_service_hyperimage_org__HILogin.prototype.authenticate = ws_service_hyperimage_org__authenticate_op;

function ws_service_hyperimage_org__authenticate_serializeInput(cxfjsutils, args) {
    var wrapperObj = new ws_service_hyperimage_org__authenticate();
    wrapperObj.setUsername(args[0]);
    wrapperObj.setPassword(args[1]);
    var xml;
    xml = cxfjsutils.beginSoap11Message("xmlns:jns0='http://www.w3.org/2005/08/addressing' xmlns:jns1='http://ws.service.hyperimage.org/' ");
    // block for local variables
    {
     xml = xml + wrapperObj.serialize(cxfjsutils, 'jns1:authenticate', null);
    }
    xml = xml + cxfjsutils.endSoap11Message();
    return xml;
}

ws_service_hyperimage_org__HILogin.prototype.authenticate_serializeInput = ws_service_hyperimage_org__authenticate_serializeInput;

function ws_service_hyperimage_org__authenticateResponse_deserializeResponse(cxfjsutils, partElement) {
    var returnObject = ws_service_hyperimage_org__authenticateResponse_deserialize (cxfjsutils, partElement);

    return returnObject;
}
function ws_service_hyperimage_org__logout_op_onsuccess(client, responseXml) {
    if (client.user_onsuccess) {
     var responseObject = null;
     var element = responseXml.documentElement;
     this.jsutils.trace('responseXml: ' + this.jsutils.traceElementName(element));
     element = this.jsutils.getFirstElementChild(element);
     this.jsutils.trace('first element child: ' + this.jsutils.traceElementName(element));
     while (!this.jsutils.isNodeNamedNS(element, 'http://schemas.xmlsoap.org/soap/envelope/', 'Body')) {
      element = this.jsutils.getNextElementSibling(element);
      if (element == null) {
       throw 'No env:Body in message.'
      }
     }
     element = this.jsutils.getFirstElementChild(element);
     this.jsutils.trace('part element: ' + this.jsutils.traceElementName(element));
     this.jsutils.trace('calling ws_service_hyperimage_org__logoutResponse_deserializeResponse');
     responseObject = ws_service_hyperimage_org__logoutResponse_deserializeResponse(this.jsutils, element);
     client.user_onsuccess(responseObject);
    }
}

ws_service_hyperimage_org__HILogin.prototype.logout_onsuccess = ws_service_hyperimage_org__logout_op_onsuccess;

function ws_service_hyperimage_org__logout_op_onerror(client) {
    if (client.user_onerror) {
     var httpStatus;
     var httpStatusText;
     try {
      httpStatus = client.req.status;
      httpStatusText = client.req.statusText;
     } catch(e) {
      httpStatus = -1;
      httpStatusText = 'Error opening connection to server';
     }
     client.user_onerror(httpStatus, httpStatusText);
    }
}

ws_service_hyperimage_org__HILogin.prototype.logout_onerror = ws_service_hyperimage_org__logout_op_onerror;

//
// Operation {http://ws.service.hyperimage.org/}logout
// Wrapped operation.
// parameter editorRef
// - Object constructor is www_w3_org_2005_08_addressing_EndpointReferenceType
//
function ws_service_hyperimage_org__logout_op(successCallback, errorCallback, editorRef) {
    this.client = new CxfApacheOrgClient(this.jsutils);
    var xml = null;
    var args = new Array(1);
    args[0] = editorRef;
    xml = this.logout_serializeInput(this.jsutils, args);
    this.client.user_onsuccess = successCallback;
    this.client.user_onerror = errorCallback;
    var closureThis = this;
    this.client.onsuccess = function(client, responseXml) { closureThis.logout_onsuccess(client, responseXml); };
    this.client.onerror = function(client) { closureThis.logout_onerror(client); };
    var requestHeaders = [];
    requestHeaders['SOAPAction'] = '';
    this.jsutils.trace('synchronous = ' + this.synchronous);
    this.client.request(this.url, xml, null, this.synchronous, requestHeaders);
}

ws_service_hyperimage_org__HILogin.prototype.logout = ws_service_hyperimage_org__logout_op;

function ws_service_hyperimage_org__logout_serializeInput(cxfjsutils, args) {
    var wrapperObj = new ws_service_hyperimage_org__logout();
    wrapperObj.setEditorRef(args[0]);
    var xml;
    xml = cxfjsutils.beginSoap11Message("xmlns:jns0='http://www.w3.org/2005/08/addressing' xmlns:jns1='http://ws.service.hyperimage.org/' ");
    // block for local variables
    {
     xml = xml + wrapperObj.serialize(cxfjsutils, 'jns1:logout', null);
    }
    xml = xml + cxfjsutils.endSoap11Message();
    return xml;
}

ws_service_hyperimage_org__HILogin.prototype.logout_serializeInput = ws_service_hyperimage_org__logout_serializeInput;

function ws_service_hyperimage_org__logoutResponse_deserializeResponse(cxfjsutils, partElement) {
    var returnObject = ws_service_hyperimage_org__logoutResponse_deserialize (cxfjsutils, partElement);

    return returnObject;
}
function ws_service_hyperimage_org__HILogin_ws_service_hyperimage_org__HILoginPort () {
  this.url = 'http://hyperimage.eu:8080/HIEditor_2.0-Service/HILoginService';
}
ws_service_hyperimage_org__HILogin_ws_service_hyperimage_org__HILoginPort.prototype = new ws_service_hyperimage_org__HILogin;
