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

/*
 * Copyright 2014 Leuphana Universität Lüneburg
 * All rights reserved.  Use is subject to license terms.
 */

package org.hyperimage.client.xmlimportexport;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.model.HILayer;
import org.hyperimage.client.model.HIRichText;
import org.hyperimage.client.model.HIRichTextChunk;
import org.hyperimage.client.model.HIRichTextChunk.chunkTypes;
import org.hyperimage.client.model.RelativePolygon;
import org.hyperimage.client.model.RelativePolygon.HiPolygonTypes;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.GroupTypes;
import org.hyperimage.client.ws.HiBaseTypes;
import org.hyperimage.client.ws.HiFlexMetadataName;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiFlexMetadataSet;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiInscription;
import org.hyperimage.client.ws.HiLanguage;
import org.hyperimage.client.ws.HiLayer;
import org.hyperimage.client.ws.HiLightTable;
import org.hyperimage.client.ws.HiObject;
import org.hyperimage.client.ws.HiObjectContent;
import org.hyperimage.client.ws.HiProject;
import org.hyperimage.client.ws.HiProjectMetadata;
import org.hyperimage.client.ws.HiQuickInfo;
import org.hyperimage.client.ws.HiText;
import org.hyperimage.client.ws.HiView;
import org.hyperimage.client.ws.Hiurl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This class serializes a given HyperImage project to the PeTAL 2.0 format.
 *
 * @author Jens-Martin Loebel
 */
// DEBUG - yes the whole class
public class PeTALExporter {

    public static Vector<HiView> projectViews = new Vector<HiView>();

	public static Document getProjectToPeTALXML(HiProject hiProject) {
		Document petalXML = null;
		Vector<Element> groupNodes = new Vector<Element>();
		HashMap<Long, Element> textNodes = new HashMap<Long, Element>();
		HashMap<Long, Element> objectNodes = new HashMap<Long, Element>();
		HashMap<Long, Element> litaNodes = new HashMap<Long, Element>();
		HashMap<Long, Element> urlNodes = new HashMap<Long, Element>();
		projectViews.removeAllElements();


		// build xml document
		DocumentBuilder xmlDocumentBuilder;
		try {
			xmlDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			return petalXML;
		}
		petalXML = xmlDocumentBuilder.newDocument();

		// create root project element
		Element project = petalXML.createElement("petal");
		// add attributes
		project.setAttribute("xmlns", "http://www.hyperimage.eu/PeTAL/2.0");
		project.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		project.setAttribute("xmlns:svg", "http://www.w3.org/2000/svg"); // add svg context
		project.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink"); // add xlink context
		project.setAttribute("xmlns:html", "http://www.w3.org/1999/xhtml"); // add xhtml context
		project.setAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"); // add RDF context
        project.setAttribute("xsi:schemaLocation", "http://www.hyperimage.eu/PeTAL/2.0 http://hyperimage.cms.hu-berlin.de/2.0/schema/PeTAL_schema_2.0.xsd");
        project.setAttribute("version", "2.0");

        // add project templates namespace and schemata
        boolean dcSchemaFound = false;
		for ( HiFlexMetadataTemplate template : hiProject.getTemplates() ) {
            String nsURI = template.getNamespaceURI();
            if ( nsURI == null ||nsURI.length() == 0 ) nsURI="unknown"; // namespace may not be empty
            if ( template.getNamespacePrefix().compareTo("HIClassic") == 0 )
                nsURI = "http://www.hyperimage.eu/PeTAL/HIClassic/1.0"; // legacy fix for invalid classic namespace
    		project.setAttribute("xmlns:"+template.getNamespacePrefix(), nsURI);
            if ( template.getNamespacePrefix().compareTo("dc") == 0 && template.getNamespaceURI().compareTo("http://purl.org/dc/elements/1.1/") == 0 )
                dcSchemaFound = true;
        }
        // make sure to include dublin core namespace
        if ( !dcSchemaFound )
            project.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/"); // add dublin core context

        if ( hiProject == null ) return petalXML;
		project.setAttribute("id", "P"+hiProject.getId());
		String startRef = "";
		if ( hiProject.getStartObjectInfo() != null )
			startRef = MetadataHelper.getDisplayableID(hiProject.getStartObjectInfo(), true);
        // create xlink URI context for start ref
        String serverURL = HIRuntime.getManager().getServerURL();
        if (!serverURL.endsWith("/"))
            serverURL = serverURL + "/";
        serverURL = serverURL.replaceAll("http://", "petal://");
        serverURL = serverURL.replaceAll("https://", "petals://");
        project.setAttribute("startRef", serverURL + "P" + HIRuntime.getManager().getProject().getId() + "/" + startRef);

		petalXML.appendChild(project);

		// export project languages
		Element tempLanguage;
		for ( HiLanguage lang : hiProject.getLanguages() )
		{
			tempLanguage = petalXML.createElement("language");
			if ( lang.getId() == hiProject.getDefaultLanguage().getId() )
				tempLanguage.setAttribute("standard", "true");
			tempLanguage.appendChild(petalXML.createTextNode(lang.getLanguageId()));
			project.appendChild(tempLanguage);
		}

		// export project metadata
        // create RDF context
        Element projMetadataElement = petalXML.createElement("projectMetadata");
        project.appendChild(projMetadataElement);
        Element RDFElement = petalXML.createElement("rdf:RDF");
        projMetadataElement.appendChild(RDFElement);
        Element mdElement;
		for ( HiProjectMetadata record : hiProject.getMetadata() ) {
            Element RDFDescriptionElement = petalXML.createElement("rdf:Description");
			RDFDescriptionElement.setAttribute("xml:lang", record.getLanguageID());
			RDFDescriptionElement.setAttribute("rdf:about", MetadataHelper.getProjectFullyQualifiedURI());
            RDFElement.appendChild(RDFDescriptionElement);

            mdElement = petalXML.createElement("dc:title");
			mdElement.setAttribute("xml:lang", record.getLanguageID());
			mdElement.appendChild(petalXML.createTextNode(validateUTF8(record.getTitle())));
            RDFDescriptionElement.appendChild(mdElement);
        }

		// export project templates
		Element tempTemplate;
		for ( HiFlexMetadataTemplate template : hiProject.getTemplates() ) {
			if ( template.getNamespacePrefix().compareTo("HIBase") != 0 ) { // don´t export base template
				tempTemplate = petalXML.createElement("template");
				tempTemplate.setAttribute("id", "T_"+template.getId()+"_"+template.getNamespacePrefix());
				tempTemplate.setAttribute("nsPrefix", template.getNamespacePrefix());
				tempTemplate.setAttribute("schema", template.getNamespaceURI());
                if ( template.getNamespacePrefix().compareTo("HIClassic") == 0 ) // legacy bugfix for invalid classic namespace
                    tempTemplate.setAttribute("schema", "http://www.hyperimage.eu/PeTAL/HIClassic/1.0");
				if ( template.getNamespaceURL().length() > 0 )
					tempTemplate.setAttribute("schemaLocation", template.getNamespaceURL());

				Element tempSet;
				for ( HiFlexMetadataSet set : template.getEntries() ) {
					tempSet = petalXML.createElement("key");
					tempSet.setAttribute("tagName", set.getTagname());
					if ( set.isRichText() ) tempSet.setAttribute("richText", "true");

					Element tempDisplayName;
					for ( HiFlexMetadataName name : set.getDisplayNames() ) {
						tempDisplayName = petalXML.createElement("displayName");
						tempDisplayName.setAttribute("xml:lang", name.getLanguage());
						tempDisplayName.appendChild(petalXML.createTextNode(name.getDisplayName()));
						tempSet.appendChild(tempDisplayName);
					}
					tempTemplate.appendChild(tempSet);
				}
				project.appendChild(tempTemplate);
			}
		}

		/* ******************************************************************
		 * scan groups, collect group members as we go and create group nodes
		 * ******************************************************************
		 */
		List<HiGroup> groups;
		try {
			groups = HIRuntime.getManager().getGroups();
			groups.add(0, HIRuntime.getManager().getImportGroup());

			float percentage = 100f / (float)(groups.size()-1);
			float progress = 0f;

			Element tempGroup;
			for ( int groupIndex = 0; groupIndex < groups.size(); groupIndex++ ) {
				HiGroup group = groups.get(groupIndex);
				tempGroup = petalXML.createElement("group");
				tempGroup.setAttribute("id", "G"+group.getId());

				// [JML] removed visibilty from PeTAL export
				/*
				if ( group.isVisible() && group.getType() == GroupTypes.HIGROUP_REGULAR )
					tempGroup.setAttribute("visible", "true");
				else
					tempGroup.setAttribute("visible", "false");
				*/

				if ( group.getType() == GroupTypes.HIGROUP_IMPORT )
					tempGroup.setAttribute("type", "import");
				else if ( group.getType() == GroupTypes.HIGROUP_REGULAR )
					tempGroup.setAttribute("type", "regular");
				else
					tempGroup.setAttribute("type", "trash");

				// export group metadata
                // create RDF context
                RDFElement = petalXML.createElement("rdf:RDF");
                tempGroup.appendChild(RDFElement);
                for (HiFlexMetadataRecord record : group.getMetadata()) {
                    Element RDFDescriptionElement = petalXML.createElement("rdf:Description");
        			RDFDescriptionElement.setAttribute("rdf:about", MetadataHelper.getBaseIDQualifiedURI(group));
                    RDFDescriptionElement.setAttribute("xml:lang", record.getLanguage());
                    RDFElement.appendChild(RDFDescriptionElement);

                    mdElement = getSingleLineBaseElementNode("title", "dc:title", "HIBase", record, petalXML);
                    mdElement.removeAttribute("xml:lang"); // not needed
                    RDFDescriptionElement.appendChild(mdElement);

                    mdElement = getMultiLineBaseElementNode("comment", "HIBase:annotation", "HIBase", record, petalXML);
                    mdElement.removeAttribute("xml:lang"); // not needed
                    RDFDescriptionElement.appendChild(mdElement);
                }

				// scan contents, add member, create xml nodes if necessary
				List<HiQuickInfo> contents = HIRuntime.getManager().getGroupContents(group);
				sortContents(contents, group.getSortOrder());
				Element tempContent;

				float contentPercentage = 0;
				if ( contents.size() > 0 ) contentPercentage = 1f / (float)contents.size();
				for ( int contentIndex = 0; contentIndex < contents.size(); contentIndex++ ) {
					HiQuickInfo content = contents.get(contentIndex);

					float updatedProgress = (percentage * (float)groupIndex) + (percentage*(contentPercentage*(float)contentIndex));

					if ( (int)updatedProgress != (int)progress ) {
						progress = updatedProgress;
						HIRuntime.getGui().setProgress((int)progress);
					}

					tempContent = petalXML.createElement("member");
                    // create xlink context
                    tempContent.setAttribute("xlink:type", "simple");
                    tempContent.setAttribute("xlink:href", MetadataHelper.getBaseIDQualifiedURI(content));

                    tempGroup.appendChild(tempContent);

					// create object nodes
					if ( content.getContentType() == HiBaseTypes.HI_OBJECT )
						if ( ! objectNodes.containsKey(content.getBaseID()) )
							objectNodes.put(content.getBaseID(), getObjectElement(
									(HiObject)HIRuntime.getManager().getBaseElement(content.getBaseID()),
									hiProject,
									petalXML
							));

					// create text nodes
					if ( content.getContentType() == HiBaseTypes.HI_TEXT )
						if ( ! textNodes.containsKey(content.getBaseID()) )
							textNodes.put(content.getBaseID(), getTextElement(
									(HiText)HIRuntime.getManager().getBaseElement(content.getBaseID()),
									petalXML
							));

					// create url nodes
					if ( content.getContentType() == HiBaseTypes.HIURL )
						if ( ! urlNodes.containsKey(content.getBaseID()) )
							urlNodes.put(content.getBaseID(), getURLElement(
									(Hiurl)HIRuntime.getManager().getBaseElement(content.getBaseID()),
									petalXML
							));

					// create light table nodes
					if ( content.getContentType() == HiBaseTypes.HI_LIGHT_TABLE )
						if ( ! litaNodes.containsKey(content.getBaseID()) )
							litaNodes.put(content.getBaseID(), getLightTableElement(
									(HiLightTable)HIRuntime.getManager().getBaseElement(content.getBaseID()),
									petalXML
							));

				}

				groupNodes.addElement(tempGroup);
			}



			// add cached nodes
			for ( Long key : textNodes.keySet() )
				project.appendChild(textNodes.get(key));
			for ( Long key : objectNodes.keySet() )
				project.appendChild(objectNodes.get(key));
			for ( Long key : litaNodes.keySet() )
				project.appendChild(litaNodes.get(key));
			for ( Long key : urlNodes.keySet() )
				project.appendChild(urlNodes.get(key));
			// add cached group nodes
			for ( Element group : groupNodes )
				project.appendChild(group);

		} catch (HIWebServiceException wse) {
			HIRuntime.getGui().reportError(wse, null);
			return petalXML;
		}
		HIRuntime.getGui().setProgress(-1);

		return petalXML;

	}

	public static String validateUTF8(String input) {
		return input;
//		if ( input == null ) return null;
//		return  new String(input.getBytes(Charset.forName("UTF-8")), Charset.forName("UTF-8"));

	}

	// TODO: refactor sorting functions
	private static void sortContents(List<HiQuickInfo> contents, String sortOrder) {
		if ( contents == null  ) return;

		long contentID;
		int index = 0;
		int contentIndex;

		// parse sort order string (don´t trust user input)
		for ( String contentIDString : sortOrder.split(",") ) { //$NON-NLS-1$
			try {
				contentID = Long.parseLong(contentIDString);
				// find content belonging to the parsed id
				contentIndex = -1;

				for ( int i=0; i<contents.size(); i++ )
					if ( contents.get(i).getBaseID() == contentID )
						contentIndex = i;
				// if content was found, sort to new index
				if ( contentIndex >= 0 ) if ( contentIndex != index ) {
					HiQuickInfo content = contents.get(contentIndex);
					contents.remove(contentIndex);
					contents.add(index, content);
					index = index+1;
				} else index = index + 1;
			} catch (NumberFormatException e) {}; // user messed with sort order string format, no problem
		}
	}

	// TODO: refactor sorting functions --> DRY
	private static void sortLayers(List<HiLayer> layers, String sortOrder) {
		long layerID;
		int index = 0;
		int contentIndex;

		// parse sort order string (don´t trust user input)
		for ( String contentIDString : sortOrder.split(",") ) {
			try {
				layerID = Long.parseLong(contentIDString);
				// find content belonging to the parsed id
				contentIndex = -1;

				for ( int i=0; i<layers.size(); i++ )
					if ( layers.get(i).getId() == layerID )
						contentIndex = i;
				// if content was found, sort to new index
				if ( contentIndex >= 0 ) if ( contentIndex != index ) {
					HiLayer layer = layers.get(contentIndex);
					layers.remove(contentIndex);
					layers.add(index, layer);
					index = index+1;
				} else index = index + 1;
			} catch (NumberFormatException e) {}; // user messed with sort order string format, no problem
		}
	}

	// TODO: refactor sorting functions --> DRY
	private static void sortObjectContent(List<HiObjectContent> contents, String sortOrder) {
		long contentID;
		int index = 0;
		int contentIndex;

		// parse sort order string (don´t trust user input)
		for ( String contentIDString : sortOrder.split(",") ) {
			try {
				contentID = Long.parseLong(contentIDString);
				// find content belonging to the parsed id
				contentIndex = -1;

				for ( int i=0; i<contents.size(); i++ )
					if ( contents.get(i).getId() == contentID )
						contentIndex = i;
				// if content was found, sort to new index
				if ( contentIndex >= 0 ) if ( contentIndex != index ) {
					HiObjectContent content = contents.get(contentIndex);
					contents.remove(contentIndex);
					contents.add(index, content);
					index = index+1;
				} else index = index + 1;
			} catch (NumberFormatException e) {}; // user messed with sort order string format, no problem
		}
	}



	private static Element getLightTableElement(HiLightTable lightTable,
			Document creator) {
		Element lightTableElement = creator.createElement("lita");
		lightTableElement.setAttribute("id", "X"+lightTable.getId());

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			factory.setNamespaceAware(false);
			builder = factory.newDocumentBuilder();
			if ( builder != null )
				builder.setErrorHandler(null);

			// parse external light table xml and adjust id
			builder.reset();
			// sanitize user XML to UTF-8
			ByteArrayInputStream input = new ByteArrayInputStream(MetadataHelper.convertToUTF8(lightTable.getXml()).getBytes());
			Document doc = builder.parse(input);
			// extract lita element
			lightTableElement = (Element) creator.adoptNode(doc.getElementsByTagName("lita").item(0));
			lightTableElement.setAttribute("id", "X"+lightTable.getId());

		} catch (ParserConfigurationException e) {
			// error could not create xml parser
			return lightTableElement;
		} catch (SAXException e) {
			return lightTableElement;
		} catch (IOException e) {
			return lightTableElement;
		}




		return lightTableElement;
	}



	private static Element getURLElement(Hiurl url, Document creator) {
		Element urlElement = creator.createElement("url");

        // create xlink context
		urlElement.setAttribute("id", "U"+url.getId());
		urlElement.setAttribute("xlink:href", url.getUrl());
		urlElement.setAttribute("xlink:type", "simple");
		urlElement.setAttribute("xlink:title", validateUTF8(url.getTitle()));

        urlElement.appendChild(creator.createTextNode(validateUTF8(url.getTitle())));

        Element annotationElement = creator.createElement("annotation");
		serializeMultiLineField(url.getLastAccess(), annotationElement, creator);

		urlElement.appendChild(annotationElement);
		urlElement.removeAttribute("xml:lang"); // not needed for urls

		return urlElement;
	}



	private static Element getTextElement(HiText text, Document creator) {
		Element textElement = creator.createElement("text");

		textElement.setAttribute("id", "T"+text.getId());

        // create RDF context
        Element RDFElement = creator.createElement("rdf:RDF");
        textElement.appendChild(RDFElement);
        Element mdElement;
		for ( HiFlexMetadataRecord record : text.getMetadata() ) {
            Element RDFDescriptionElement = creator.createElement("rdf:Description");
			RDFDescriptionElement.setAttribute("xml:lang", record.getLanguage());
   			RDFDescriptionElement.setAttribute("rdf:about", MetadataHelper.getBaseIDQualifiedURI(text));
            RDFElement.appendChild(RDFDescriptionElement);

            mdElement = getSingleLineBaseElementNode("title", "dc:title", "HIBase", record, creator);
			mdElement.removeAttribute("xml:lang"); // not needed
			RDFDescriptionElement.appendChild(mdElement);

            mdElement = getMultiLineBaseElementNode("content", "HIBase:content", "HIBase", record, creator);
			mdElement.removeAttribute("xml:lang"); // not needed
			RDFDescriptionElement.appendChild(mdElement);
		}
		return textElement;
	}



	private static Element getObjectElement(HiObject object, HiProject project, Document creator) {
		Element objectElement = creator.createElement("object");
		if ( object == null ) return objectElement;

		objectElement.setAttribute("id", "O"+object.getId());

		// set standard view if present
		if ( object.getDefaultView() != null ) {
			Element defViewElement = creator.createElement("standardView");

            // create xlink context
            defViewElement.setAttribute("xlink:type", "simple");
            defViewElement.setAttribute("xlink:href", MetadataHelper.getBaseIDQualifiedURI(object.getDefaultView()));

			objectElement.appendChild(defViewElement);
		}

		// export views and inscriptions
		sortObjectContent(object.getViews(), object.getSortOrder());
		for ( HiObjectContent content : object.getViews() )
			if ( content instanceof HiView )
				objectElement.appendChild(getViewElement((HiView)content, creator));
			else
				objectElement.appendChild(getInscriptionElement((HiInscription)content, creator));


		// export flex metadata
        // create rdf context
		Element metadataElement;
        Element RDFElement;
        Element RDFDescriptionElement;
		for ( HiFlexMetadataRecord record : object.getMetadata() ) {
			metadataElement = creator.createElement("metadata");
			metadataElement.setAttribute("xml:lang", record.getLanguage());
            // create RDF context
            RDFElement = creator.createElement("rdf:RDF");
            metadataElement.appendChild(RDFElement);
            RDFDescriptionElement = creator.createElement("rdf:Description");
			RDFDescriptionElement.setAttribute("xml:lang", record.getLanguage());
   			RDFDescriptionElement.setAttribute("rdf:about", MetadataHelper.getBaseIDQualifiedURI(object));
            RDFElement.appendChild(RDFDescriptionElement);
			// go through templates
			for ( HiFlexMetadataTemplate template : project.getTemplates() ) {
				if ( template.getNamespacePrefix().compareTo("HIBase") != 0 ) { // exclude base schema
					// go through record keys
					Element valueElement;
					for ( HiFlexMetadataSet set : template.getEntries() ) {
						if ( set.isRichText() )
							valueElement = getMultiLineBaseElementNode(set.getTagname(), template.getNamespacePrefix()+":"+set.getTagname(), template.getNamespacePrefix(), record, creator);
						else
							valueElement = getSingleLineBaseElementNode(set.getTagname(), template.getNamespacePrefix()+":"+set.getTagname(), template.getNamespacePrefix(), record, creator);
						valueElement.removeAttribute("xml:lang"); // not need in object flex metadata value

						RDFDescriptionElement.appendChild(valueElement);
					}

				}
			}
			objectElement.appendChild(metadataElement);
		}


		return objectElement;
	}









	private static Element getInscriptionElement(HiInscription inscription, Document creator) {
		Element inscriptionElement = creator.createElement("inscription");

		inscriptionElement.setAttribute("id", "I"+inscription.getId());

        // create RDF context
        Element RDFElement = creator.createElement("rdf:RDF");
        inscriptionElement.appendChild(RDFElement);
        Element mdElement;
		for ( HiFlexMetadataRecord record : inscription.getMetadata() ) {
            Element RDFDescriptionElement = creator.createElement("rdf:Description");
			RDFDescriptionElement.setAttribute("xml:lang", record.getLanguage());
   			RDFDescriptionElement.setAttribute("rdf:about", MetadataHelper.getBaseIDQualifiedURI(inscription));
            RDFElement.appendChild(RDFDescriptionElement);

            mdElement = getMultiLineBaseElementNode("content", "HIBase:content", "HIBase", record, creator);
			mdElement.removeAttribute("xml:lang"); // not needed
            RDFDescriptionElement.appendChild(mdElement);
        }

		return inscriptionElement;
	}



	private static Element getViewElement(HiView view, Document creator) {
		Element viewElement = creator.createElement("view");

		viewElement.setAttribute("id", "V"+view.getId());

		Element originalElement = creator.createElement("original");
		originalElement.setAttribute("filename", view.getFilename());
		originalElement.setAttribute("hash", view.getHash());
		viewElement.appendChild(originalElement);

		// original image size
		viewElement.appendChild(getImgElement(view.getWidth(), view.getHeight(), "img/V"+view.getId()+".original", "pict", creator));

		// export view metadata
        // create RDF context
        Element RDFElement = creator.createElement("rdf:RDF");
        viewElement.appendChild(RDFElement);
        Element mdElement;
		for ( HiFlexMetadataRecord record : view.getMetadata() ) {
            Element RDFDescriptionElement = creator.createElement("rdf:Description");
			RDFDescriptionElement.setAttribute("xml:lang", record.getLanguage());
   			RDFDescriptionElement.setAttribute("rdf:about", MetadataHelper.getBaseIDQualifiedURI(view));
            RDFElement.appendChild(RDFDescriptionElement);

            mdElement = getSingleLineBaseElementNode("title", "dc:title", "HIBase", record, creator);
			mdElement.removeAttribute("xml:lang"); // not needed
            RDFDescriptionElement.appendChild(mdElement);

            mdElement = getSingleLineBaseElementNode("source", "dc:source", "HIBase", record, creator);
			mdElement.removeAttribute("xml:lang"); // not needed
            RDFDescriptionElement.appendChild(mdElement);

            mdElement = getMultiLineBaseElementNode("comment", "HIBase:annotation", "HIBase", record, creator);
			mdElement.removeAttribute("xml:lang"); // not needed
            RDFDescriptionElement.appendChild(mdElement);
        }

		// store view for binary export reference
		boolean isInStore = false;
		for ( HiView storedView : projectViews )
			if ( storedView.getId() == view.getId() )
				isInStore = true;
		if ( !isInStore )
			projectViews.addElement(view);

		// export layers
		sortLayers(view.getLayers(), view.getSortOrder());
		// export layer in reverse order to comply with reader z-order
		for ( int zOrder = view.getLayers().size() ; zOrder >=1 ; zOrder-- ) {
			HiLayer layer = view.getLayers().get(zOrder-1);
			viewElement.appendChild(getLayerElement(layer, zOrder, creator));
		}


		return viewElement;
	}



	private static Node getLayerElement(HiLayer layer, int order,
			Document creator) {
		Element layerElement = creator.createElement("layer");
		layerElement.setAttribute("id", "L"+layer.getId());
		String color = "#";
		if ( Integer.toHexString(layer.getRed()).length() < 2 )
			color = color+"0"+Integer.toHexString(layer.getRed());
		else
			color = color+Integer.toHexString(layer.getRed());
		if ( Integer.toHexString(layer.getGreen()).length() < 2 )
			color = color+"0"+Integer.toHexString(layer.getGreen());
		else
			color = color+Integer.toHexString(layer.getGreen());
		if ( Integer.toHexString(layer.getBlue()).length() < 2 )
			color = color+"0"+Integer.toHexString(layer.getBlue());
		else
			color = color+Integer.toHexString(layer.getBlue());

        if ( layer.getLinkInfo() != null ) {
            // create xlink context
            layerElement.setAttribute("xlink:type", "simple");
            layerElement.setAttribute("xlink:href", MetadataHelper.getBaseIDQualifiedURI(layer.getLinkInfo()));
        }
		layerElement.setAttribute("order", Integer.toString(order));

		// export layer metadata
        // create RDF context
        Element RDFElement = creator.createElement("rdf:RDF");
        layerElement.appendChild(RDFElement);
        Element mdElement;
		for ( HiFlexMetadataRecord record : layer.getMetadata() ) {
            Element RDFDescriptionElement = creator.createElement("rdf:Description");
   			RDFDescriptionElement.setAttribute("rdf:about", MetadataHelper.getBaseIDQualifiedURI(layer));
			RDFDescriptionElement.setAttribute("xml:lang", record.getLanguage());
            RDFElement.appendChild(RDFDescriptionElement);

            mdElement = getSingleLineBaseElementNode("title", "dc:title", "HIBase", record, creator);
			mdElement.removeAttribute("xml:lang"); // not needed
            RDFDescriptionElement.appendChild(mdElement);

            mdElement = getMultiLineBaseElementNode("comment", "HIBase:annotation", "HIBase", record, creator);
			mdElement.removeAttribute("xml:lang"); // not needed
            RDFDescriptionElement.appendChild(mdElement);
        }

		// export polygons
        // create svg context
        Element svgElement = creator.createElement("svg:svg");
        svgElement.setAttribute("version", "1.1");
        svgElement.setAttribute("width", "100");
        svgElement.setAttribute("height", "100");
        Element gElement = creator.createElement("svg:g");
        svgElement.appendChild(gElement);
        gElement.setAttribute("transform", "scale(100)");
        gElement.setAttribute("stroke", "none");
        gElement.setAttribute("fill", color); // set layer color in SVG context
        gElement.setAttribute("opacity", Float.toString(layer.getOpacity())); // set layer opacity in SVG context
        gElement.setAttribute("fill-opacity", Float.toString(layer.getOpacity())); // set layer opacity in SVG context
        layerElement.appendChild(svgElement);

		HILayer wrapper = new HILayer(layer, 400, 400);
		for ( RelativePolygon polygon : wrapper.getRelativePolygons() )
			gElement.appendChild(getShapeElement(polygon, creator));

		return layerElement;
	}



	private static Element getShapeElement(RelativePolygon polygon,
			Document creator) {
		Element shapeElement;

		if ( polygon.getType() == HiPolygonTypes.HI_RECTANGLE ) {
            shapeElement = creator.createElement("svg:rect"); // place shape in svg context
            // process rectangles
            double x, y, width, height;
            x = polygon.getRelativePoint(0).x;
            y = polygon.getRelativePoint(0).y;
            width = x;
            height = y;
            for (int i=1; i < 4; i++) {
                if ( polygon.getRelativePoint(i).x < x )
                    x = polygon.getRelativePoint(i).x;
                if ( polygon.getRelativePoint(i).y < y )
                    y = polygon.getRelativePoint(i).y;
                if ( polygon.getRelativePoint(i).x > width )
                    width = polygon.getRelativePoint(i).x;
                if ( polygon.getRelativePoint(i).y > height )
                    height = polygon.getRelativePoint(i).y;
            }
            width = width-x;
            height = height-y;

            shapeElement.setAttribute("x", Double.toString(x));
            shapeElement.setAttribute("y", Double.toString(y));
            shapeElement.setAttribute("width", Double.toString(width));
            shapeElement.setAttribute("height", Double.toString(height));

        } else {
            shapeElement = creator.createElement("svg:polygon"); // place shape in svg context

            if ( polygon.getType() == HiPolygonTypes.HI_USERDESIGN )
                shapeElement.setAttribute("type", "custom");

        	String points = polygon.getModel();
            if (points.length() > 2)
                points = points.substring(2);
            points = points.replaceAll("#", ",");
            points = points.replaceAll(";", " ");
            shapeElement.setAttribute("points", points);
        }

		return shapeElement;
	}



	private static Element getImgElement(int width, int height, String src,
			String use, Document creator) {
		Element imgElement = creator.createElement("img");

		imgElement.setAttribute("width", Integer.toString(width));
		imgElement.setAttribute("height", Integer.toString(height));
		imgElement.setAttribute("src", src);
		imgElement.setAttribute("use", use);

		return imgElement;

	}



	private static List<Element> getSingleLineBaseElementNodes(String key, String elementTitle,
			String template, List<HiFlexMetadataRecord> records, Document creator) {

		List<Element> elements = new ArrayList<Element>();
		if ( records == null ) return elements;

		for ( HiFlexMetadataRecord record : records )
			elements.add(getSingleLineBaseElementNode(key, elementTitle, template, record, creator));

		return elements;
	}



	// convert single line metadata fields in multiple languages to xml element nodes
	private static Element getSingleLineBaseElementNode(String key,
			String elementTitle, String template, HiFlexMetadataRecord record, Document creator) {

		Element tempElement;
		tempElement = creator.createElement(elementTitle);
		tempElement.setAttribute("xml:lang", record.getLanguage());

		tempElement.appendChild(creator.createTextNode( validateUTF8(MetadataHelper.findValue(template, key, record)) ));

		return tempElement;
	}


	/**
	 * Helper function breaks a multi line string into seperate strings. Doing it the old fashioned
	 * way without using String.split to catch all cases (e.g. new line at the end of string).
	 * @param lines multi line string to break down
	 * @return
	 */
	private static Vector<String> breakdownLines(String lines) {
		Vector<String> cutLines = new Vector<String>();
		String line;
		do {
			if ( lines.indexOf("\n") < 0 )
				line = lines;
			else {
				line = lines.substring(0, lines.indexOf("\n"));
				lines = lines.substring(lines.indexOf("\n")+1);
				cutLines.addElement(line);
			}

		} while ( lines.indexOf("\n") >= 0 );
		cutLines.addElement(lines);

		return cutLines;
	}


	// convert multi line rich text metadata fields in multiple languages to xml element nodes
	/*
	 * NOTE: this is a mess due to project constraints
	 */
	private static void serializeMultiLineField(String model, Element multiLineElement, Document creator) {
		// extract rich text model
		HIRichText text = new HIRichText(model);
		HIRichTextChunk.chunkTypes chunkState = null;
		Node chunkNode = null;
		if ( text.getChunks().size() == 0 ) return;
		if ( text.getChunks().size() == 1 && text.getChunks().get(0).getValue().length() == 0 )
			return;

        chunkNode = multiLineElement;

		for ( HIRichTextChunk chunk : text.getChunks() ) {
			if ( chunk.getChunkType() != chunkTypes.LINK ) {
				if ( chunkState != chunk.getChunkType() ) {
					if ( chunk.getChunkType() == chunkTypes.REGULAR )
						chunkNode = multiLineElement;
					else {
						String tag = null;
						if ( chunk.getChunkType() == chunkTypes.BOLD ) tag = "html:b";
						if ( chunk.getChunkType() == chunkTypes.ITALIC ) tag = "html:i";
						if ( chunk.getChunkType() == chunkTypes.UNDERLINE ) tag = "html:u";
						if ( tag == null ) chunkNode = multiLineElement;
						else {
							chunkNode = creator.createElement(tag);
							multiLineElement.appendChild(chunkNode);
						}
					}
					chunkState = chunk.getChunkType();
				}

				// break down lines
				Vector<String> lines = breakdownLines(chunk.getValue());
				for ( int i=0; i < lines.size(); i++ ) {
					String line = lines.get(i);
					chunkNode.appendChild(creator.createTextNode(validateUTF8(line)));
					// add line break
					if ( i < (lines.size()-1) ) {
                        Element brElement = creator.createElement("html:br");
                        chunkNode.appendChild(brElement);
					}
				}

			} else { // handle links
				// break down lines
				Element linkElement;
				Vector<String> lines = breakdownLines(chunk.getValue());
				for ( int i=0; i < lines.size(); i++ ) {
					String line = lines.get(i);

                    // create xlink context
					if ( line.length() > 0 ) {
                        String serverURL = HIRuntime.getManager().getServerURL();
                        if ( !serverURL.endsWith("/") ) serverURL = serverURL+"/";
                        serverURL = serverURL.replaceAll("http://", "petal://");
                        serverURL = serverURL.replaceAll("https://", "petals://");
						linkElement = creator.createElement("link");
						linkElement.setAttribute("xlink:type", "simple");
						linkElement.setAttribute("xlink:href", serverURL+"P"+HIRuntime.getManager().getProject().getId()+"/"+chunk.getRef());
						linkElement.appendChild(creator.createTextNode(validateUTF8(line)));
						chunkNode.appendChild(linkElement);
					}
					// skip to new line
					if (  i < (lines.size()-1) ) {
                        Element brElement = creator.createElement("html:br");
                        chunkNode.appendChild(brElement);
					}
				}

			}
		}


	}

	private static Element getMultiLineBaseElementNode(String key,
			String elementTitle, String template, HiFlexMetadataRecord record, Document creator) {


		Element tempElement;

		tempElement = creator.createElement(elementTitle);
		tempElement.setAttribute("xml:lang", record.getLanguage());

		serializeMultiLineField(MetadataHelper.findValue(template, key, record), tempElement, creator);


		return tempElement;
	}



	private static List<Element> getMultiLineBaseElementNodes(String key, String elementTitle,
			String template, List<HiFlexMetadataRecord> records, Document creator) {

		List<Element> elements = new ArrayList<Element>();
		if ( records == null ) return elements;

		for ( HiFlexMetadataRecord record : records )
			elements.add(getMultiLineBaseElementNode(key, elementTitle, template, record, creator));


		return elements;
	}



	public static String serializeXMLDocument(Document doc) {
		OutputFormat xmlFormat = new OutputFormat();
		xmlFormat.setEncoding("UTF-8");
		xmlFormat.setVersion("1.0");
		xmlFormat.setIndenting(true);
		xmlFormat.setIndent(4);
		xmlFormat.setPreserveEmptyAttributes(true);

		// As a DOM Serializer
		ByteArrayOutputStream xmlStream = new ByteArrayOutputStream();
		XMLSerializer x = new XMLSerializer(xmlStream,xmlFormat);
		x.setOutputFormat(xmlFormat);
		x.setOutputByteStream(xmlStream);
		try {
			x.serialize(doc);
		} catch (IOException e) {
		}

		return xmlStream.toString();
	}

    	public static String serializeXMLElement(Element element) {
		OutputFormat xmlFormat = new OutputFormat();
		xmlFormat.setEncoding("UTF-8");
		xmlFormat.setVersion("1.0");
		xmlFormat.setIndenting(true);
		xmlFormat.setIndent(4);
		xmlFormat.setPreserveEmptyAttributes(true);

		// As a DOM Serializer
		ByteArrayOutputStream xmlStream = new ByteArrayOutputStream();
		XMLSerializer x = new XMLSerializer(xmlStream,xmlFormat);
		x.setOutputFormat(xmlFormat);
		x.setOutputByteStream(xmlStream);
		try {
			x.serialize(element);
		} catch (IOException e) {
            return "";
		}

		return xmlStream.toString();
	}

	// DEBUG
	public static void serializeXMLDocumentToFile(Document doc, File outputFile) {
		OutputFormat xmlFormat = new OutputFormat("XML","UTF-8",true);
		xmlFormat.setIndenting(true);
		xmlFormat.setIndent(4);
		xmlFormat.setPreserveEmptyAttributes(true);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outputFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}

		XMLSerializer xmlSerializer = new XMLSerializer(fos, xmlFormat);
		// As a DOM Serializer
		try {
			xmlSerializer.asDOMSerializer().serialize(doc.getDocumentElement());
		} catch (IOException e) {
		}

		if ( fos != null )
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

}
