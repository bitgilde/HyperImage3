/* $Id: HIFedora3Connector.java 133 2009-08-25 12:05:53Z jmloebel $ */

/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/HYPERIMAGE.LICENSE
 * or http://www.sun.com/cddl/cddl.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/HYPERIMAGE.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright 2006-2009 Humboldt-Universitaet zu Berlin
 * All rights reserved.  Use is subject to license terms.
 */

package org.hyperimage.connector.fedora3;

import info.fedora.definitions._1._0.api.FedoraAPIA;
import info.fedora.definitions._1._0.api.FedoraAPIAService;
import info.fedora.definitions._1._0.types.ArrayOfString;
import info.fedora.definitions._1._0.types.ComparisonOperator;
import info.fedora.definitions._1._0.types.Condition;
import info.fedora.definitions._1._0.types.DatastreamDef;
import info.fedora.definitions._1._0.types.FieldSearchQuery;
import info.fedora.definitions._1._0.types.FieldSearchResult;
import info.fedora.definitions._1._0.types.MIMETypedStream;
import info.fedora.definitions._1._0.types.ObjectFields;
import info.fedora.definitions._1._0.types.RepositoryInfo;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import org.hyperimage.connector.HIHierarchyLevel;
import org.hyperimage.connector.HIMetadataRecord;
import org.hyperimage.connector.HITypedDatastream;
import org.hyperimage.connector.exception.HIWSAssetNotFoundException;
import org.hyperimage.connector.exception.HIWSAuthException;
import org.hyperimage.connector.exception.HIWSDCMetadataException;
import org.hyperimage.connector.exception.HIWSLoggedException;
import org.hyperimage.connector.exception.HIWSNotBinaryException;
import org.hyperimage.connector.exception.HIWSUTF8EncodingException;
import org.hyperimage.connector.exception.HIWSXMLParserException;
import org.hyperimage.connector.utility.ConnectorPreferences;
import org.hyperimage.connector.utility.ImageHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.xml.ws.client.ClientTransportException;


/**
 * This class utilises the Service Endpoint Interface of the Fedora API-A (Version 3.0b).
 * It provides the functionality required by the HyperImage Server to import binary data and metadata
 * from a Fedora repository. This functionality is exposed as a Web service.
 * 
 * @author Heinz-Günter Kuper
 */
@WebService(name="HIFedora3Connector")
public class HIFedora3Connector {
	private static String REPOS_URL = "";
	
	// Fedora Exception Classes
	private static final String OBJECTNOTINLOWLEVELSTORAGE = "fedora.server.errors.ObjectNotInLowlevelStorageException";
	
	private static final String VERSION = "0.3";
	private static final BigInteger MAX_RESULTS = new BigInteger("10000");
	private static final Logger m_logger = Logger.getLogger(HIFedora3Connector.class.getName());
	static FedoraAPIA m_proxy;
	private static String m_ownerID = null;

	/**
	 * Constructor.
	 * 
	 * @throws MalformedURLException
	 */
	public HIFedora3Connector() throws MalformedURLException {
		// Get the URL from the preferences
		ConnectorPreferences prefs = new ConnectorPreferences();
		REPOS_URL = prefs.getFedoraURLPref();
		m_logger.info("Fedora URL: " + REPOS_URL);
		// create Service
		FedoraAPIAService service = null;
		service = new FedoraAPIAService(
				new URL(REPOS_URL),
				new QName("http://www.fedora.info/definitions/1/0/api/", "Fedora-API-A-Service")
		);

		// create proxy
		if( service != null ) {
			m_proxy = service.getFedoraAPIAServiceHTTPPort();
			m_logger.info("Fedora proxy created successfully.");
		}
	}

	/**
	 * Authenticate against the Fedora repository. The BindingProvider does not throw an exception
	 * if the login data is incorrect. A com.sun.xml.ws.client.ClientTransportException is thrown if
	 *  the client tries to access a Web service without valid authentication.
	 * 
	 * @param username	User ID for the Fedora repository.
	 * @param token		Password for the Fedora repository.
	 */
	public void authenticate(
			@WebParam(name="username",
					targetNamespace="http://connector.ws.hyperimage.org/")
					String username,
			@WebParam(name="token",
					targetNamespace="http://connector.ws.hyperimage.org/")
					String token) {
		((BindingProvider)m_proxy).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
		((BindingProvider)m_proxy).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, token);
		m_ownerID = username;
	}

	/**
	 * Return the version of the Web service currently being consumed.
	 * 
	 * @return The Web service version.
	 */
	public String getWSVersion() {
		return "HIFedora3Connector Version " + VERSION;
	}

	/**
	 * Describes the repository.
	 * 
	 * @return Information about the repository.
	 * 
	 * @throws HIWSAuthException	Password or username incorrect.
	 * @throws HIWSLoggedException	An (unknown) exception has been written to the application server log.
	 */
	public String getReposInfo() throws HIWSAuthException, HIWSLoggedException {
		String strReposInfo = "error";

		try {
			strReposInfo= "Fedora Repository " + m_proxy.describeRepository().getRepositoryVersion()
			+ "\n" + describeRepository();
		} catch( ClientTransportException cte ) {
			m_logger.log(Level.SEVERE, "ClientTransportException probably due to incorrect user credentials for repository.", cte);
			throw new HIWSAuthException(cte.getMessage(), cte.getCause());
		} catch( Exception e ) {
			m_logger.log(Level.SEVERE, "Unknown exception", e);
			throw new HIWSLoggedException(e.getMessage());
		}

		return strReposInfo;
	}
	
	/**
	 * may return null. better per asset or per repository.
	 * @param assetURN
	 * @return
	 */
//	public HIMetadataSchema getMetadataSchemeForRecord(String assetURN) {
//		return new HIMetadataSchema();
//	}
	
	/**
	 * Retrieve an asset's binary data from the repository. The byte array is returned,
	 * even if it is null, so this should always be checked in the HI Editor client.
	 * 
	 * @param session Not used.
	 * @param assetURN Points to the asset.
	 * @return The asset's binary data with MIME type.
	 * 
	 * @throws HIWSAuthException
	 * @throws HIWSAssetNotFoundException
	 * @throws HIWSNotBinaryException
	 * @throws HIWSLoggedException
	 */
	public HITypedDatastream getAssetData(
			@WebParam(name="session", targetNamespace="http://connector.ws.hyperimage.org/")
			String session,
			@WebParam(name="assetURN", targetNamespace="http://connector.ws.hyperimage.org/")
			String assetURN)
			throws HIWSAuthException, HIWSAssetNotFoundException, HIWSNotBinaryException, HIWSLoggedException {
		HITypedDatastream returnStream = new HITypedDatastream();
		byte[] bytes = null;
		String[] strarrIDs = assetURN.split(",");
		
		MIMETypedStream dstream = null;
		try {
			dstream = m_proxy.getDatastreamDissemination(strarrIDs[0], strarrIDs[1], null);
		} catch (SOAPFaultException sfe) {		
			if( isNotInLowLevelStorage(sfe) )
				throw new HIWSAssetNotFoundException(sfe.getMessage(), sfe.getCause());
			else {
				m_logger.log(Level.SEVERE, "Unknown SOAP fault", sfe);
				throw new HIWSLoggedException(sfe.getMessage());
			}
		} catch( ClientTransportException cte ) {
			m_logger.log(Level.SEVERE, "ClientTransportException probably due to incorrect user credentials for repository", cte);
			throw new HIWSAuthException(cte.getMessage(), cte.getCause());
		} catch( Exception e ) {
			m_logger.log(Level.SEVERE, "Unknown exception", e);
			throw new HIWSLoggedException(e.getMessage());
		}
		
		if( isBinary(dstream.getMIMEType()) )
			bytes = dstream.getStream();
		else {
			HIWSNotBinaryException ex = new HIWSNotBinaryException("Object "+ strarrIDs[0] +" with Datastream "+ strarrIDs[1] +" has an unrecognised format: " + dstream.getMIMEType());
			m_logger.throwing(this.getClass().getCanonicalName(), "getAssetData", ex);
			throw ex;
		}
		
		returnStream.setMIMEType(dstream.getMIMEType());
		returnStream.setByteArray(bytes);
		
		return returnStream;
	}

	/**
	 * TODO: This operation is still to be implemented.
	 * 
	 * @param session
	 * @param assetURN
	 * @return
	 */
	public byte[] getAssetPreviewData(
			@WebParam(name="session", targetNamespace="http://connector.ws.hyperimage.org/")
			String session,
			@WebParam(name="assetURN", targetNamespace="http://connector.ws.hyperimage.org/")
			String assetURN)
			throws HIWSAuthException, HIWSAssetNotFoundException, HIWSNotBinaryException, HIWSLoggedException{
		byte[] bytes = null;
		String[] strarrIDs = assetURN.split(",");
		
		MIMETypedStream dstream = null;
		try {
			dstream = m_proxy.getDatastreamDissemination(strarrIDs[0], strarrIDs[1], null);
		} catch (SOAPFaultException sfe) {		
			if( isNotInLowLevelStorage(sfe) )
				throw new HIWSAssetNotFoundException(sfe.getMessage(), sfe.getCause());
			else {
				m_logger.log(Level.SEVERE, "Unknown SOAP fault", sfe);
				throw new HIWSLoggedException(sfe.getMessage());
			}
		} catch( ClientTransportException cte ) {
			m_logger.log(Level.SEVERE, "ClientTransportException probably due to incorrect user credentials for repository", cte);
			throw new HIWSAuthException(cte.getMessage(), cte.getCause());
		} catch( Exception e ) {
			m_logger.log(Level.SEVERE, "Unknown exception", e);
			throw new HIWSLoggedException(e.getMessage());
		}
		
		if( isBinary(dstream.getMIMEType()) ) {
			// This is a workaround because at this stage there are no preview images in our Fedora repos.
			// Needed for JAI headless (to avoid problems with PELauncher).
			System.setProperty("java.awt.headless", "true");
			PlanarImage viewImage = JAI.create("stream",SeekableStream.wrapInputStream(new ByteArrayInputStream(dstream.getStream()), true));
			PlanarImage thumbImage = ImageHelper.scaleImageTo(viewImage, new Dimension(128,128));
			ByteArrayOutputStream outThumbnail = new ByteArrayOutputStream();
			JPEGEncodeParam jpegParam = new JPEGEncodeParam();
			// set encoding quality
			jpegParam.setQuality(0.8f);
			JAI.create("encode", thumbImage, outThumbnail, "JPEG", jpegParam);
			// create thumbnail
			bytes = outThumbnail.toByteArray();
		} else {
			HIWSNotBinaryException ex = new HIWSNotBinaryException("Object "+ strarrIDs[0] +" with Datastream "+ strarrIDs[1] +" has an unrecognised format: " + dstream.getMIMEType());
			m_logger.throwing(this.getClass().getCanonicalName(), "getAssetData", ex);
			throw ex;
		}
		
		return bytes;
	}

	/**
	 * Return the object's metadata.
	 * 
	 * @param session Not used.
	 * @param assetURN Points to the object.
	 * @return List containing the metadata in key-value pairs.
	 * 
	 * @throws HIWSLoggedException
	 * @throws HIWSXMLParserException
	 * @throws HIWSUTF8EncodingException
	 * @throws HIWSAssetNotFoundException
	 * @throws HIWSDCMetadataException 
	 */
	public List<HIMetadataRecord> getMetadataRecord(
			@WebParam(name="session", targetNamespace="http://connector.ws.hyperimage.org/")
			String session,
			@WebParam(name="assetURN", targetNamespace="http://connector.ws.hyperimage.org/")
			String assetURN)
			throws HIWSLoggedException, HIWSXMLParserException, HIWSUTF8EncodingException,
			HIWSAssetNotFoundException, HIWSDCMetadataException {
		List<HIMetadataRecord> listMRecords = new ArrayList<HIMetadataRecord>();

		DocumentBuilder docBuilder = null;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setNamespaceAware(true);
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			throw new HIWSXMLParserException(pce.getMessage(), pce.getCause());
		}

		MIMETypedStream dstream = null;
		try {
			dstream = m_proxy.getDatastreamDissemination(assetURN, "DC", null);
		} catch (SOAPFaultException sfe) {		
			if( isNotInLowLevelStorage(sfe) )
				throw new HIWSAssetNotFoundException(sfe.getMessage(), sfe.getCause());
			else {
				m_logger.log(Level.SEVERE, "Unknown SOAP fault", sfe);
				throw new HIWSLoggedException(sfe.getMessage());
			}
		}
		
		InputSource src = null;
		try {
			src = new InputSource(new StringReader(new String(dstream.getStream(), "UTF-8")));
		} catch (UnsupportedEncodingException uee) {
			throw new HIWSUTF8EncodingException(uee.getMessage(), uee.getCause());
		}

		Document doc = null;
		try {
			doc = docBuilder.parse(src);
		} catch (SAXException saxe) {
			throw new HIWSXMLParserException(saxe.getMessage(), saxe.getCause());
		} catch (IOException ioe) {
			m_logger.log(Level.SEVERE, "Unexpected IOException while trying to parse metadata", ioe);
			throw new HIWSLoggedException(ioe.getMessage());
		}

		if( doc.hasChildNodes() ) {
			Node node = doc.getDocumentElement().getFirstChild().getNextSibling();
			while( node != null ) {
				HIMetadataRecord record = assignDCMetadataToRecord(node);
				listMRecords.add(record);			
				node = node.getNextSibling();
				if( node != null && node.getNodeType() == Node.TEXT_NODE ) // Skip PCDATA nodes, since content has already been returned by getTextContent().
					node = node.getNextSibling();
			}
		}

		return listMRecords;
	}
	

	/**
	 * Return all child elements at the level of the hierarchy addressed by the parentURN.
	 * E.g. if the parentURN is the root, then all elements below the root are returned.
	 * 
	 * TODO [HGK] Update method signature, don't need hasPreview.
	 * 
	 * @param session
	 * @param parentURN
	 * 
	 * @return List containing the child elements of the parentURN.
	 * 
	 * @throws HIWSLoggedException
	 * @throws HIWSAuthException
	 */
	public List<HIHierarchyLevel> getHierarchyLevel(
			@WebParam(name="session", targetNamespace="http://connector.ws.hyperimage.org/")
			String session,
			@WebParam(name="parentURN", targetNamespace="http://connector.ws.hyperimage.org/")
			String parentURN)
			throws HIWSAuthException, HIWSLoggedException {	
		List<HIHierarchyLevel> listLevel = new ArrayList<HIHierarchyLevel>();
		
		FieldSearchResult result = null;
		try {
			result = m_proxy.findObjects(prepResultFields(), MAX_RESULTS, prepQuery(m_ownerID));
		} catch( ClientTransportException cte ) {
			throw new HIWSAuthException(cte.getMessage(), cte.getCause());
		} catch( Exception e ) {
			m_logger.log(Level.SEVERE, "Unknown exception", e);
			throw new HIWSLoggedException(e.getMessage());
		}

		FieldSearchResult.ResultList results = result.getResultList();

		for(ObjectFields ofield: results.getObjectFields()){
			HIHierarchyLevel hiLevel = new HIHierarchyLevel();
			String pid = ofield.getPid().getValue();
			for( String strDSID : listDatastreamIDs(pid))
				hiLevel.setURN(pid + "," + strDSID);
			
			// Don't know why Fedora returns a list of titles (multilingual?).
			// Seems to only ever contain one title, so I'll just take the first element.
			hiLevel.setDisplayName(ofield.getTitle().get(0));
			
			hiLevel.setChildren(false);
			hiLevel.setLevel(false);
			// TODO [HGK] setPreview(boolean) does nothing sensible at the moment.
			hiLevel.setPreview(false);
			listLevel.add(hiLevel);
		}

		return listLevel;
	}

	
	/////////////////////
	// UTILITY METHODS //
	/////////////////////

	/**
	 * Utility method to parse SOAP fault message generated by Fedora repository.
	 */
	private boolean isNotInLowLevelStorage(SOAPFaultException sfe) {
			String[] strarrMsg = sfe.getMessage().split(":");			
			if( strarrMsg[0].compareTo(OBJECTNOTINLOWLEVELSTORAGE) == 0 )
				return true;
			return false;
	}
	
	/**
	 * Parse the Dublin Core metadata returned by Fedora and load it into a HyperImage record.
	 * 
	 * @param node The current node being parsed.
	 * 
	 * @return Record contained metadata in key-value format.
	 * 
	 * @throws HIWSDCMetadataException 
	 */
	private HIMetadataRecord assignDCMetadataToRecord(Node node) throws HIWSDCMetadataException {
		HIMetadataRecord record = null;

		if( node != null ) {
			record = new HIMetadataRecord();
			record.setMetadataType(HIMetadataRecord.MetadataType.DC);

			if( node.getNodeName().compareToIgnoreCase("dc:title") == 0 ) {
				record.setKey("title");
			} else if( node.getNodeName().compareToIgnoreCase("dc:creator") == 0 ) {
				record.setKey("creator");
			} else if( node.getNodeName().compareToIgnoreCase("dc:subject") == 0 ) {
				record.setKey("subject");
			} else if( node.getNodeName().compareToIgnoreCase("dc:description") == 0 ) {
				record.setKey("description");
			} else if( node.getNodeName().compareToIgnoreCase("dc:publisher") == 0 ) {
				record.setKey("publisher");
			} else if( node.getNodeName().compareToIgnoreCase("dc:contributor") == 0 ) {
				record.setKey("contributor");
			} else if( node.getNodeName().compareToIgnoreCase("dc:date") == 0 ) {
				record.setKey("date");
			} else if( node.getNodeName().compareToIgnoreCase("dc:type") == 0 ) {
				record.setKey("type");
			} else if( node.getNodeName().compareToIgnoreCase("dc:format") == 0 ) {
				record.setKey("format");
			} else if( node.getNodeName().compareToIgnoreCase("dc:identifier") == 0 ) {
				record.setKey("identifier");
			} else if( node.getNodeName().compareToIgnoreCase("dc:source") == 0 ) {
				record.setKey("source");
			} else if( node.getNodeName().compareToIgnoreCase("dc:language") == 0 ) {
				record.setKey("language");
			} else if( node.getNodeName().compareToIgnoreCase("dc:relation") == 0 ) {
				record.setKey("relation");
			} else if( node.getNodeName().compareToIgnoreCase("dc:coverage") == 0 ) {
				record.setKey("coverage");
			} else if( node.getNodeName().compareToIgnoreCase("dc:rights") == 0 ) {
				record.setKey("rights");	
			} else {
				HIWSDCMetadataException ex = new HIWSDCMetadataException("Unrecognised metadata node (supposedly Dublin Core): " + node.getNodeName());
				m_logger.throwing(this.getClass().getCanonicalName(), "assignDCMetadataToRecord", ex);
				throw ex;
			}

			record.setValue(node.getTextContent());
		}

		return record;	
	}
	
	/**
	 * Determine the mimetype of the binary data.
	 * 
	 * @param mimetype
	 * 
	 * @return True if it is a format recognised by the HyperImage system.
	 */
	private boolean isBinary(String mimetype) {
		boolean bReturn = false;
		
		if( mimetype.equalsIgnoreCase("image/jpeg") )
			bReturn = true;
		
		return bReturn;
	}	
	
	/**
	 * List all datastreams pertaining to the persistent identifier.
	 * 
	 * @param pid
	 * 
	 * @return List of the datastreams.
	 */
	private List<String> listDatastreamIDs(String pid) {
		List<String> listDatastreamIDs = new ArrayList<String>();
		//String strDatastreams = "";
		
		List<DatastreamDef> dstreams = m_proxy.listDatastreams(pid, null);
		for( DatastreamDef dsdef : dstreams ) {
			if( isBinary(dsdef.getMIMEType()) )
				listDatastreamIDs.add(dsdef.getID());
			//strDatastreams += "ID: " + dsdef.getID() + " --- ";
			//strDatastreams += "Label: " + dsdef.getLabel() + " --- ";
			//strDatastreams += "MIMEType: " + dsdef.getMIMEType() + "\n";
		}
		
		return listDatastreamIDs;
	}

	/**
	 * Prepare the query regarding all objects in the repository belonging to the owner.
	 * 
	 * @param strOwnerId
	 * 
	 * @return Prepared query
	 */
	private FieldSearchQuery prepQuery(String strOwnerId) {
		FieldSearchQuery query = new FieldSearchQuery();
		
		Condition cond = new Condition();
		cond.setProperty("ownerId");
		cond.setOperator(ComparisonOperator.EQ);
		cond.setValue("true");

		FieldSearchQuery.Conditions conditions = new FieldSearchQuery.Conditions();
		conditions.getCondition().add(cond);
		
		JAXBElement<FieldSearchQuery.Conditions> jaxbConditionsElt =
			new JAXBElement<FieldSearchQuery.Conditions>(
					new QName("http://www.fedora.info/definitions/1/0/types/", "conditions"),
					FieldSearchQuery.Conditions.class,
					conditions);

		query.setConditions(jaxbConditionsElt);

		String terms = new String(strOwnerId);

		JAXBElement<String> jaxbTermsElt =
			new JAXBElement<String>(
					new QName("http://www.fedora.info/definitions/1/0/types/", "terms"),
					String.class,
					terms);

		query.setTerms(jaxbTermsElt);
		
		return query;
	}
	
	/**
	 * Prepare query of Fedora repository regarding persistent identifier, owner ID and title.
	 * 
	 * @return Prepared array.
	 */
	private ArrayOfString prepResultFields() {
		ArrayOfString resultFields = new ArrayOfString();
		resultFields.getItem().add("pid");
		resultFields.getItem().add("ownerId");
		resultFields.getItem().add("title");
		
		return resultFields;
	}
	
	/**
	 * Format the description of the repository.
	 * 
	 * @return String describing the repository in human-readable format.
	 */
	private String describeRepository() {
		RepositoryInfo repInfo = m_proxy.describeRepository();
		String strReturn = new String();

		ArrayOfString arrstrAdminEmail = repInfo.getAdminEmailList();
		for(String strAdminEmail : arrstrAdminEmail.getItem() )
			strReturn = "Admin Email(s): " + strAdminEmail + "\n";

		strReturn += "Default Export Format: " + repInfo.getDefaultExportFormat() + "\n";
		strReturn += "OAI Namespace: " + repInfo.getOAINamespace() + "\n";
		strReturn += "Base URL: " + repInfo.getRepositoryBaseURL() + "\n";
		strReturn += "Repository Name: " + repInfo.getRepositoryName() + "\n";
		strReturn += "PID Namespace: " + repInfo.getRepositoryPIDNamespace() + "\n";
		strReturn += "Repository Version: Fedora " + repInfo.getRepositoryVersion() + "\n";

		ArrayOfString arrstrRetainPIDs = repInfo.getRetainPIDs();
		for(String strRetainPID : arrstrRetainPIDs.getItem() )
			strReturn += "Retain PIDs: " + strRetainPID + "\n";

		strReturn += "Sample Access URL: " + repInfo.getSampleAccessURL() + "\n";
		strReturn += "Sample OAI Identifier: " + repInfo.getSampleOAIIdentifier() + "\n";
		strReturn += "Sample OAI URL: " + repInfo.getSampleOAIURL() + "\n";
		strReturn += "Sample PID: " + repInfo.getSamplePID() + "\n";
		strReturn += "Sample Search URL: " + repInfo.getSampleSearchURL() + "\n";

		return strReturn;
	}
}
