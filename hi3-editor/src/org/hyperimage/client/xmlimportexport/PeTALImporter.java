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
package org.hyperimage.client.xmlimportexport;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.HIRichTextFieldControl;
import org.hyperimage.client.model.HILayer;
import org.hyperimage.client.model.HIRichText;
import org.hyperimage.client.model.HIRichTextChunk;
import org.hyperimage.client.model.RelativePolygon;
import org.hyperimage.client.util.MetadataHelper;
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
import org.hyperimage.client.ws.HiPreference;
import org.hyperimage.client.ws.HiText;
import org.hyperimage.client.ws.HiView;
import org.hyperimage.client.ws.Hiurl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Jens-Martin Loebel
 */
public class PeTALImporter {

    Document petalXML = null;
    File inputFile = null;
    String version = null;
    boolean importSuccessful = false;
    String startRef = "";
    Vector<String> languages = new Vector<String>();
    Vector<String> brokenViews = new Vector<String>();
    Vector<Element> texts = new Vector<Element>();
    Vector<Element> objects = new Vector<Element>();
    Vector<Element> views = new Vector<Element>();
    Vector<Element> inscriptions = new Vector<Element>();
    Vector<Element> layers = new Vector<Element>();
    Vector<Element> urls = new Vector<Element>();
    Vector<Element> litas = new Vector<Element>();
    Vector<Element> groups = new Vector<Element>();
    Vector<HiFlexMetadataTemplate> templates = new Vector<HiFlexMetadataTemplate>();
    HashMap<HiText, Element> textMap = new HashMap<HiText, Element>();
    HashMap<String, HiText> textIDMap = new HashMap<String, HiText>();
    HashMap<Hiurl, Element> urlMap = new HashMap<Hiurl, Element>();
    HashMap<String, Hiurl> urlIDMap = new HashMap<String, Hiurl>();
    HashMap<HiLightTable, Element> litaMap = new HashMap<HiLightTable, Element>();
    HashMap<String, HiLightTable> litaIDMap = new HashMap<String, HiLightTable>();
    HashMap<HiGroup, Element> groupMap = new HashMap<HiGroup, Element>();
    HashMap<String, HiGroup> groupIDMap = new HashMap<String, HiGroup>();
    HashMap<HiObject, Element> objectMap = new HashMap<HiObject, Element>();
    HashMap<String, HiObject> objectIDMap = new HashMap<String, HiObject>();
    HashMap<HiView, Element> viewMap = new HashMap<HiView, Element>();
    HashMap<String, HiView> viewIDMap = new HashMap<String, HiView>();
    HashMap<HiInscription, Element> inscriptionMap = new HashMap<HiInscription, Element>();
    HashMap<String, HiInscription> inscriptionIDMap = new HashMap<String, HiInscription>();
    HashMap<HiLayer, Element> layerMap = new HashMap<HiLayer, Element>();
    HashMap<String, HiLayer> layerIDMap = new HashMap<String, HiLayer>();

    public String getPeTALVersion() {
        if (petalXML == null) {
            version = null;
        }

        return version;
    }

    public boolean wasImportSuccessful() {
        return importSuccessful;
    }

    private void abortWithError(String errorMessage) {
        HIRuntime.getGui().stopIndicatingServiceActivity();
        if ( errorMessage == null ) errorMessage = "";

        // TODO display error message
        System.out.println("IMPORT ERROR!");
        System.out.println("--> "+errorMessage);
        if (errorMessage.length() == 0)
            HIRuntime.getGui().displayInfoDialog(Messages.getString("PeTALImporter.2"), Messages.getString("PeTALImporter.3") + "\n\n" +
                    Messages.getString("PeTALImporter.4"));
        else
            HIRuntime.getGui().displayInfoDialog(Messages.getString("PeTALImporter.2"), Messages.getString("PeTALImporter.5") + "\n" +
                    Messages.getString("PeTALImporter.6")+" "+errorMessage+"\n\n"+
                    Messages.getString("PeTALImporter.7"));
        HIRuntime.getGui().triggerProjectUpdate();
    }

    private boolean checkLangInProject(String lang) {
        boolean langIsInProject = false;
        for (HiLanguage projLang : HIRuntime.getManager().getProject().getLanguages()) {
            if (projLang.getLanguageId().equalsIgnoreCase(lang)) {
                langIsInProject = true;
            }
        }
        return langIsInProject;
    }

        private String resolveDisplayableNewID(String oldURIID) {
        String newID = "";
        String oldID = oldURIID.substring(oldURIID.lastIndexOf("/") + 1);

        if (oldID.startsWith("T")) {
            HiText text = textIDMap.get(oldID);
            if (text != null) {
                newID = "T"+text.getId();
            }
        }
        if (oldID.startsWith("U")) {
            Hiurl url = urlIDMap.get(oldID);
            if (url != null) {
                newID = "U"+url.getId();
            }
        }
        if (oldID.startsWith("X")) {
            HiLightTable lita = litaIDMap.get(oldID);
            if (lita != null) {
                newID = "X"+lita.getId();
            }
        }
        if (oldID.startsWith("G")) {
            HiGroup group = groupIDMap.get(oldID);
            if (group != null) {
                newID = "G"+group.getId();
            }
        }
        if (oldID.startsWith("O")) {
            HiObject object = objectIDMap.get(oldID);
            if (object != null) {
                newID = "O"+object.getId();
            }
        }
        if (oldID.startsWith("V")) {
            HiView view = viewIDMap.get(oldID);
            if (view != null) {
                newID = "V"+view.getId();
            }
        }
        if (oldID.startsWith("I")) {
            HiInscription inscription = inscriptionIDMap.get(oldID);
            if (inscription != null) {
                newID = "I"+inscription.getId();
            }
        }
        if (oldID.startsWith("L")) {
            HiLayer layer = layerIDMap.get(oldID);
            if (layer != null) {
                newID = "L"+layer.getId();
            }
        }

        return newID;
    }

    private long resolveNewID(String oldURIID) {
        long newID = 0;
        String oldID = oldURIID.substring(oldURIID.lastIndexOf("/") + 1);

        if (oldID.startsWith("T")) {
            HiText text = textIDMap.get(oldID);
            if (text != null) {
                newID = text.getId();
            }
        }
        if (oldID.startsWith("U")) {
            Hiurl url = urlIDMap.get(oldID);
            if (url != null) {
                newID = url.getId();
            }
        }
        if (oldID.startsWith("X")) {
            HiLightTable lita = litaIDMap.get(oldID);
            if (lita != null) {
                newID = lita.getId();
            }
        }
        if (oldID.startsWith("G")) {
            HiGroup group = groupIDMap.get(oldID);
            if (group != null) {
                newID = group.getId();
            }
        }
        if (oldID.startsWith("O")) {
            HiObject object = objectIDMap.get(oldID);
            if (object != null) {
                newID = object.getId();
            }
        }
        if (oldID.startsWith("V")) {
            HiView view = viewIDMap.get(oldID);
            if (view != null) {
                newID = view.getId();
            }
        }
        if (oldID.startsWith("I")) {
            HiInscription inscription = inscriptionIDMap.get(oldID);
            if (inscription != null) {
                newID = inscription.getId();
            }
        }
        if (oldID.startsWith("L")) {
            HiLayer layer = layerIDMap.get(oldID);
            if (layer != null) {
                newID = layer.getId();
            }
        }

        return newID;
    }

    private String getTextNodeContent(Node textNode) {
        String textContent = "";

        // convert text node, trim whitespace
        textContent = textNode.getTextContent();

        textContent = textContent.replaceAll("[ ]*[\t\n]+[\t\n ]*\\z", "");
        if ( textContent.length() == 0 ) textContent = " ";
        textContent = textContent.replaceAll("[ ]*[\t\n]+[\t\n ]*", " ");

        return textContent;
    }

    private String getSubElementText(Element subElement) {
        String textContent = "";

        NodeList subNodes = subElement.getChildNodes();
        for (int i = 0; i < subNodes.getLength(); i++) {
            Node subNode = subNodes.item(i);
            if (subNode.getNodeType() == Node.TEXT_NODE) {
                textContent = textContent + getTextNodeContent(subNode);
            } else
                if (    ((Element) subNode).getNamespaceURI().compareTo("http://www.w3.org/1999/xhtml") == 0
                     && ((Element) subNode).getLocalName().compareTo("br") == 0)
                    textContent = textContent + "\n";
        }

        return textContent;
    }

    private void processSubNode(Element subElement, HIRichText hiText, HIRichTextChunk.chunkTypes chunkType) {
        String textContent = "";

        NodeList subNodes = subElement.getChildNodes();
        for (int i = 0; i < subNodes.getLength(); i++) {
            Node subNode = subNodes.item(i);
            if (subNode.getNodeType() == Node.TEXT_NODE) {
                if ( i < (subNodes.getLength()-1) || subNode.getTextContent().replaceAll("[ \t\n]*", "").length() > 0 )
                    textContent = textContent + getTextNodeContent(subNode);
            } else {
                if ( ((Element)subNode).getNamespaceURI().compareTo("http://www.w3.org/1999/xhtml") == 0
                     && ((Element)subNode).getLocalName().compareTo("br") == 0)
                    textContent = textContent + "\n";
                else if ( ((Element)subNode).getNamespaceURI().compareTo("http://www.hyperimage.eu/PeTAL/2.0") == 0
                     && ((Element)subNode).getLocalName().compareTo("link") == 0) {
                    if ( textContent.length() > 0 ) hiText.addChunk(chunkType, textContent);
                    textContent = "";
                    // add link chunk
                    String href = ((Element)subNode).getAttributeNS("http://www.w3.org/1999/xlink", "href");
                    if ( href != null && href.length() > 0 ) {
                        href = resolveDisplayableNewID(href);
                        String subContent = getSubElementText((Element)subNode);
                        if ( subContent.length() > 0 ) {
                            if ( href.length() > 0 )
                                hiText.addChunk(HIRichTextChunk.chunkTypes.LINK, subContent, href);
                            else
                                hiText.addChunk(chunkType, subContent, href);
                        }
                    }
                }

            }
        }
        if ( textContent.length() > 0 ) hiText.addChunk(chunkType, textContent);
    }


    private String convertRichTextXMLField(Element richTextElement) {
        String modelText = "";

        HIRichText hiText = new HIRichText();
        String chunkText = "";
        String chunkRef = "";

        NodeList chunkNodes = richTextElement.getChildNodes();
        for ( int i=0; i < chunkNodes.getLength(); i++  ) {
            Node chunkNode = chunkNodes.item(i);

            if (chunkNode.getNodeType() == Node.TEXT_NODE) {
                if ( chunkNode.getTextContent().replaceAll("[ \t\n]*", "").length() == 0 ) {
                    if ( !(i < (chunkNodes.getLength()-1)
                            && chunkNodes.item(i+1).getNodeType() == Node.ELEMENT_NODE
                            && !((Element)chunkNodes.item(i+1)).getTagName().equalsIgnoreCase("html:br")
                            && !((Element)chunkNodes.item(i+1)).getTagName().equalsIgnoreCase("link")) )
                        chunkText = chunkText + getTextNodeContent(chunkNode);

                } else chunkText = chunkText + getTextNodeContent(chunkNode);
            } else {
                Element chunkElement = (Element) chunkNode;
                if (chunkElement.getNamespaceURI().compareTo("http://www.w3.org/1999/xhtml") == 0 && chunkElement.getLocalName().compareTo("br") == 0) {
                    chunkText = chunkText + "\n";
                } else if (chunkElement.getNamespaceURI().compareTo("http://www.w3.org/1999/xhtml") == 0
                            && chunkElement.getLocalName().compareTo("b") == 0) {
                    if (chunkText.length() > 0)
                        hiText.addChunk(HIRichTextChunk.chunkTypes.REGULAR, chunkText);
                    chunkText = "";
                    processSubNode(chunkElement, hiText, HIRichTextChunk.chunkTypes.BOLD);

                } else if (chunkElement.getNamespaceURI().compareTo("http://www.w3.org/1999/xhtml") == 0 
                            && chunkElement.getLocalName().compareTo("i") == 0) {
                    if (chunkText.length() > 0)
                        hiText.addChunk(HIRichTextChunk.chunkTypes.REGULAR, chunkText);
                    chunkText = "";
                    processSubNode(chunkElement, hiText, HIRichTextChunk.chunkTypes.ITALIC);

                } else if (chunkElement.getNamespaceURI().compareTo("http://www.w3.org/1999/xhtml") == 0 
                            && chunkElement.getLocalName().compareTo("u") == 0) {
                    if (chunkText.length() > 0)
                        hiText.addChunk(HIRichTextChunk.chunkTypes.REGULAR, chunkText);
                    chunkText = "";
                    processSubNode(chunkElement, hiText, HIRichTextChunk.chunkTypes.UNDERLINE);

                } else if (chunkElement.getNamespaceURI().compareTo("http://www.hyperimage.eu/PeTAL/2.0") == 0 
                            && chunkElement.getLocalName().compareTo("link") == 0) {
                    if (chunkText.length() > 0)
                        hiText.addChunk(HIRichTextChunk.chunkTypes.REGULAR, chunkText);
                    chunkText = "";
                    // add link chunk
                    String href = chunkElement.getAttributeNS("http://www.w3.org/1999/xlink", "href");
                    if ( href != null && href.length() > 0 ) {
                        href = resolveDisplayableNewID(href);
                        String subContent = getSubElementText(chunkElement);
                        if ( subContent.length() > 0 ) {
                            if ( href.length() > 0 )
                                hiText.addChunk(HIRichTextChunk.chunkTypes.LINK, subContent, href);
                            else
                                hiText.addChunk(HIRichTextChunk.chunkTypes.REGULAR, subContent, href);
                        }
                    }


                }

            }
        }
        if (chunkText.length() > 0)
            hiText.addChunk(HIRichTextChunk.chunkTypes.REGULAR, chunkText);

        modelText = hiText.getModel();


        return modelText;
    }

    private String getSingleLineTextContent(Element element, String namespace, String tagName) {
        String textContent = "";

        if ( element == null ) return textContent;
        if ( element.getElementsByTagNameNS(namespace, tagName).getLength() > 0 ) {
            String temp = element.getElementsByTagNameNS(namespace, tagName).item(0).getTextContent();
            if ( temp != null ) textContent = temp.trim();
        }

        return textContent;
    }

    private String getMultiLineTextContent(Element element, String namespace, String tagName) {
        String textContent = new HIRichText().getModel();

        if ( element == null ) return textContent;
        if ( element.getElementsByTagNameNS(namespace, tagName).getLength() > 0 )
            textContent = convertRichTextXMLField((Element)element.getElementsByTagNameNS(namespace, tagName).item(0));

        // simplify text content
        HIRichTextFieldControl control = new HIRichTextFieldControl("import");
        control.setText(textContent);
        textContent = control.getText();

        return textContent;
    }

    public void attachMetadataRecords(Element mdElement, List<HiFlexMetadataRecord> records, String nsURI1, String tagName1, String mdName1, String nsURI2, String tagName2, String mdName2, String nsURI3, String tagName3, String mdName3) 
    throws HIWebServiceException {
        NodeList descElements = mdElement.getElementsByTagNameNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "Description");
        for (int i = 0; i < descElements.getLength(); i++) {
            Element descElement = (Element) descElements.item(i);
            String lang = descElement.getAttribute("xml:lang");
            if (checkLangInProject(lang)) {
                String tagContent1 = null;
                if ( tagName1 != null ) tagContent1 = getSingleLineTextContent(descElement, nsURI1, tagName1);
                String tagContent2 = null;
                if ( tagName2 != null ) tagContent2 = getSingleLineTextContent(descElement, nsURI2, tagName2);
                String tagContent3 = null;
                if ( tagName3 != null ) tagContent3 = getMultiLineTextContent(descElement, nsURI3, tagName3);

                HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(records, lang);
                if (record != null) {
                    if ( mdName1 != null ) MetadataHelper.setValue("HIBase", mdName1, tagContent1, record);
                    if ( mdName2 != null ) MetadataHelper.setValue("HIBase", mdName2, tagContent2, record);
                    if ( mdName3 != null ) MetadataHelper.setValue("HIBase", mdName3, tagContent3, record);
                }
                HIRuntime.getManager().updateFlexMetadataRecord(record);
            }
        }
    }


    public void importXMLToProject() {
        /* **********************
         * Import XML --> Project
         * **********************
         */
        // DEBUG
        HIRuntime.getGui().startIndicatingServiceActivity(true);
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.1")); //$NON-NLS-1$
        importSuccessful = false;
        languages.removeAllElements();
        templates.removeAllElements();
        texts.removeAllElements();
        objects.removeAllElements();
        views.removeAllElements();
        inscriptions.removeAllElements();
        layers.removeAllElements();
        urls.removeAllElements();
        litas.removeAllElements();
        groups.removeAllElements();
        brokenViews.removeAllElements();
        textMap.clear();
        textIDMap.clear();
        urlMap.clear();
        urlIDMap.clear();
        litaMap.clear();
        litaIDMap.clear();
        groupMap.clear();
        groupIDMap.clear();
        objectMap.clear();
        objectIDMap.clear();
        viewMap.clear();
        viewIDMap.clear();
        inscriptionMap.clear();
        inscriptionIDMap.clear();
        layerMap.clear();
        layerIDMap.clear();

        // execute export in separate thread
        new Thread() {

            @Override
            public void run() {
                try {
                    Element petalElement = petalXML.getDocumentElement();

                    /*
                     * compile list of languages
                     */
                    languages.removeAllElements();
                    NodeList langElements = petalElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "language");
                    String defLang = null;
                    for (int i = 0; i < langElements.getLength(); i++) {
                        Element langElement = (Element) langElements.item(i);
                        languages.addElement(langElement.getTextContent().trim());
                        if (langElement.getAttribute("standard") != null && langElement.getAttribute("standard").equalsIgnoreCase("true")) {
                            defLang = langElement.getTextContent().trim();
                        }
                    }
                    // add languages to project
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.8"));
                    for (String lang : languages) {
                        if (!checkLangInProject(lang)) {
                            HIRuntime.getManager().addLanguageToProject(lang);
                        }
                    }
                    // set default language
                    HIRuntime.getManager().updateProjectDefaultLanguage(defLang);
                    // remove unused languages
                    Vector<String> unusedLangs = new Vector<String>();
                    for (HiLanguage projLang : HIRuntime.getManager().getProject().getLanguages()) {
                        boolean langIsInProject = false;
                        for (String lang : languages) {
                            if (lang.equalsIgnoreCase(projLang.getLanguageId())) {
                                langIsInProject = true;
                            }
                        }

                        if (!langIsInProject) {
                            unusedLangs.addElement(projLang.getLanguageId());
                        }
                    }
                    for (String unusedLang : unusedLangs) {
                        HIRuntime.getManager().removeLanguageFromProject(unusedLang);
                    }

                    /*
                     * import project metadata
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.9"));
                    if (petalElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "projectMetadata").getLength() > 0) {
                        Element projMetadataElement = (Element) petalElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "projectMetadata").item(0);
                        NodeList mdElements = projMetadataElement.getElementsByTagNameNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "Description");
                        for (int i = 0; i < mdElements.getLength(); i++) {
                            Element mdElement = (Element) mdElements.item(i);
                            mdElement = (Element) mdElement.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "title").item(0);

                            if (checkLangInProject(mdElement.getAttribute("xml:lang"))) {
                                HIRuntime.getManager().updateProject(mdElement.getAttribute("xml:lang"), mdElement.getTextContent().trim());
                            }
                        }
                    }

                    /*
                     * compile list of templates
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.10"));
                    NodeList templateElements = petalElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "template");
                    for (int i = 0; i < templateElements.getLength(); i++) {
                        Element templateElement = (Element) templateElements.item(i);
                        if (!templateElement.getAttribute("nsPrefix").equalsIgnoreCase("HIBase")) {
                            // check if template is in project
                            boolean templateIsInProject = false;
                            for (HiFlexMetadataTemplate projTemplate : HIRuntime.getManager().getProject().getTemplates()) {
                                if (projTemplate.getNamespacePrefix().equalsIgnoreCase(templateElement.getAttribute("nsPrefix")) || projTemplate.getNamespaceURI().equalsIgnoreCase(templateElement.getAttribute("schema"))) {
                                    templates.add(projTemplate);
                                    templateIsInProject = true;
                                }
                            }

                            if (!templateIsInProject) {
                                // TODO remove unused templates and update sort order
                                // build and add new template
                                HiFlexMetadataTemplate newTemplate = new HiFlexMetadataTemplate();
                                newTemplate.setNamespacePrefix(templateElement.getAttribute("nsPrefix")); //$NON-NLS-1$
                                newTemplate.setNamespaceURI(templateElement.getAttribute("schema")); //$NON-NLS-1$
                                newTemplate.setNamespaceURL(templateElement.getAttribute("schemaLocation")); //$NON-NLS-1$
                                HiFlexMetadataSet newSet = null;
                                HiFlexMetadataName newName = null;

                                NodeList keyElements = templateElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "key");
                                for (int a = 0; a < keyElements.getLength(); a++) {
                                    Element keyElement = (Element) keyElements.item(a);
                                    newSet = new HiFlexMetadataSet();
                                    newSet.setRichText(false);
                                    if (keyElement.getAttribute("richText") != null && keyElement.getAttribute("richText").equalsIgnoreCase("true")) {
                                        newSet.setRichText(true);
                                    }
                                    newSet.setTagname(keyElement.getAttribute("tagName")); //$NON-NLS-1$

                                    NodeList nameElements = keyElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "displayName");
                                    for (int b = 0; b < nameElements.getLength(); b++) {
                                        Element nameElement = (Element) nameElements.item(b);

                                        newName = new HiFlexMetadataName();
                                        newName.setDisplayName(nameElement.getTextContent().trim());
                                        newName.setLanguage(nameElement.getAttribute("xml:lang")); //$NON-NLS-1$
                                        newSet.getDisplayNames().add(newName);
                                    }
                                    newTemplate.getEntries().add(newSet);
                                }
                                // add template
                                if ( HIRuntime.getManager().addTemplateToProject(newTemplate) )
                                    templates.add(newTemplate);
                            }
                        }
                    }

                    /*
                     * compile list of texts
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.11"));
                    NodeList projElements = petalElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "text");
                    for (int i = 0; i < projElements.getLength(); i++) {
                        Element textElement = (Element) projElements.item(i);
                        texts.addElement(textElement);
                    }
                    /*
                     * compile list of objects
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.12"));
                    projElements = petalElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "object");
                    for (int i = 0; i < projElements.getLength(); i++) {
                        Element objectElement = (Element) projElements.item(i);
                        objects.addElement(objectElement);
                    }
                    /*
                     * compile list of views
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.13"));
                    projElements = petalElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "view");
                    for (int i = 0; i < projElements.getLength(); i++) {
                        Element viewElement = (Element) projElements.item(i);
                        views.addElement(viewElement);
                    }
                    /*
                     * compile list of inscriptions
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.14"));
                    projElements = petalElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "inscription");
                    for (int i = 0; i < projElements.getLength(); i++) {
                        Element inscriptionElement = (Element) projElements.item(i);
                        inscriptions.addElement(inscriptionElement);
                    }
                    /*
                     * compile list of layers
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.15"));
                    projElements = petalElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "layer");
                    for (int i = 0; i < projElements.getLength(); i++) {
                        Element layerElement = (Element) projElements.item(i);
                        layers.addElement(layerElement);
                    }
                    /*
                     * compile list of URLs
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.16"));
                    projElements = petalElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "url");
                    for (int i = 0; i < projElements.getLength(); i++) {
                        Element urlElement = (Element) projElements.item(i);
                        urls.addElement(urlElement);
                    }
                    /*
                     * compile list of Light Tables
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.17"));
                    projElements = petalElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "lita");
                    for (int i = 0; i < projElements.getLength(); i++) {
                        Element litaElement = (Element) projElements.item(i);
                        litas.addElement(litaElement);
                    }
                    /*
                     * compile list of groups
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.18"));
                    projElements = petalElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "group");
                    for (int i = 0; i < projElements.getLength(); i++) {
                        Element groupElement = (Element) projElements.item(i);
                        if (!groupElement.getAttribute("type").equalsIgnoreCase("import") && !groupElement.getAttribute("type").equalsIgnoreCase("tag")) {
                            groups.addElement(groupElement);
                        }
                    }

                    /*
                     * check view binaries
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.19"));
                    for (Element viewElement : views) {
                        if ( !(viewElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "original").getLength() > 0)
                             || !(viewElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "img").getLength() > 0 ) )
                            brokenViews.addElement(viewElement.getAttribute("id"));
                        else {
                            // TODO verify hash
                            Element origElement = (Element) viewElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "original").item(0);
                            Element imgElement = (Element) viewElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "img").item(0);
                            String pathToFile = inputFile.getParent() + "/" + imgElement.getAttribute("src");
                            if ( File.separator.compareTo("\\") == 0 )
                                pathToFile = pathToFile.replaceAll("/", "\\"+File.separator);
                            else pathToFile = pathToFile.replaceAll("/", File.separator);
                            File binFile = new File(pathToFile);
                            if (!binFile.exists() || !binFile.canRead() || !binFile.isFile()) {
                                brokenViews.addElement(viewElement.getAttribute("id"));
                            }
                        }
                    }


                    /*
                     * create texts
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.20"));
                    for (Element textElement : texts) {
                        HiText newText = HIRuntime.getManager().createText();
                        textMap.put(newText, textElement);
                        textIDMap.put(textElement.getAttribute("id"), newText);
                    }

                    /*
                     * create urls
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.21"));
                    for (Element urlElement : urls) {
                        Hiurl newURL = HIRuntime.getManager().createURL(urlElement.getAttributeNS("http://www.w3.org/1999/xlink", "href"), urlElement.getAttributeNS("http://www.w3.org/1999/xlink", "title"), "");
                        urlMap.put(newURL, urlElement);
                        urlIDMap.put(urlElement.getAttribute("id"), newURL);
                    }

                    /*
                     * create Light Tables
                     */
                    // legacy Litas not supported!
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.22"));
                    for (Element litaElement : litas) {
                        // serialize legacy lita format
                        String litaXML = PeTALExporter.serializeXMLElement(litaElement);
                        if ( litaXML == null || litaXML.length() == 0 || litaXML.indexOf("<lita") < 0 )
                            litaXML = MetadataHelper.getDefaultLightTableXML();
                        else
                            litaXML = litaXML.substring(litaXML.indexOf("<lita"));
                        HiLightTable newLita = HIRuntime.getManager().createLightTable("-", litaXML);
                        litaMap.put(newLita, litaElement);
                        litaIDMap.put(litaElement.getAttribute("id"), newLita);
                    }

                    /*
                     * create groups
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.23"));
                    String sortOrder = "";
                    for (Element groupElement : groups) {
                        HiGroup newGroup = HIRuntime.getManager().createGroup();
                        if (newGroup != null) {
                            groupMap.put(newGroup, groupElement);
                            groupIDMap.put(groupElement.getAttribute("id"), newGroup);
                            sortOrder = sortOrder + "," + newGroup.getId();
                            if (sortOrder.startsWith(","))
                                sortOrder = sortOrder.substring(1);
                        }
                    }
                    // update group sort order
                    HiPreference groupSortOrderPref = MetadataHelper.findPreference(HIRuntime.getManager().getProject(), "groupSortOrder"); //$NON-NLS-1$
                    groupSortOrderPref.setValue(sortOrder);
                    HIRuntime.getManager().updatePreference(groupSortOrderPref);

                    /*
                     * create objects with views, inscriptions and layers
                     */
                    int counter = 1;
                    for (Element objectElement : objects) {
                        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.24")+" " + counter + " "+Messages.getString("PeTALImporter.25")+" " + objects.size() + " "+Messages.getString("PeTALImporter.26"));
                        HiObject newObject = HIRuntime.getManager().createObject();
                        objectMap.put(newObject, objectElement);
                        objectIDMap.put(objectElement.getAttribute("id"), newObject);


                        // gather views and inscriptions
                        NodeList contentElements = objectElement.getChildNodes();
                        String standardView = null;
                        sortOrder = "";
                        for (int i = 0; i < contentElements.getLength(); i++) {
                            if (contentElements.item(i).getNodeType() == Node.ELEMENT_NODE) {
                                Element contentElement = (Element) contentElements.item(i);
                                if (contentElement.getTagName().compareTo("view") == 0 && !brokenViews.contains(contentElement.getAttribute("id"))) {
                                    /*
                                     * create view
                                     */
                                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.24")+" " + counter + ": "+Messages.getString("PeTALImporter.27"));
                                    Element origElement = (Element) contentElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "original").item(0);
                                    Element imgElement = (Element) contentElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "img").item(0);
                                    String pathToFile = inputFile.getParent() + "/" + imgElement.getAttribute("src");
                                    if ( File.separator.compareTo("\\") == 0 )
                                        pathToFile = pathToFile.replaceAll("/", "\\"+File.separator);
                                    else pathToFile = pathToFile.replaceAll("/", File.separator);
                                    File binFile = new File(pathToFile);
                                    boolean bytesRead = false;
                                    byte[] data = null;
                                    try {
                                        data = HIRuntime.getBytesFromFile(binFile);
                                        bytesRead = true;
                                    } catch (IOException ex) {
                                        // ignore
                                    }
                                    if (bytesRead && data != null) {
                                        HiView newView = HIRuntime.getManager().createView(newObject, origElement.getAttribute("filename"), "[PeTAL 2.0 XML Import]", data);
                                        viewMap.put(newView, contentElement);
                                        viewIDMap.put(contentElement.getAttribute("id"), newView);
                                        data = null;
                                        sortOrder = sortOrder + "," + newView.getId();
                                        if (sortOrder.startsWith(",")) sortOrder = sortOrder.substring(1);

                                        /*
                                         * create layer
                                         */
                                        String layerSortOrder = "";
                                        NodeList layerElements = contentElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "layer");
                                        for (int a = 0; a < layerElements.getLength(); a++) {
                                            HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.24")+" " + counter + ": "+Messages.getString("PeTALImporter.28")+" : "+Messages.getString("PeTALImporter.29")+" " + (a + 1) + " "+Messages.getString("PeTALImporter.25")+" " + layerElements.getLength() + " "+Messages.getString("PeTALImporter.26"));
                                            Element layerElement = (Element) layerElements.item(a);
                                            if (layerElement.getElementsByTagNameNS("http://www.w3.org/2000/svg", "g").getLength() > 0) {
                                                Element svgElement = (Element) layerElement.getElementsByTagNameNS("http://www.w3.org/2000/svg", "g").item(0);
                                                String colorString = svgElement.getAttribute("fill");
                                                float opacity = 1.0f;
                                                if ( svgElement.getAttribute("opacity") != null ) {
                                                    try {
                                                        opacity = Float.parseFloat(svgElement.getAttribute("opacity"));
                                                } catch (NumberFormatException nfe) {
                                                        // ignore
                                                    }
                                                }

                                                Color color = Color.decode(colorString);
                                                int red = color.getRed();
                                                int green = color.getGreen();
                                                int blue = color.getBlue();

                                                HiLayer newLayer = HIRuntime.getManager().createLayer(newView, red, green, blue, opacity);
                                                layerMap.put(newLayer, layerElement);
                                                layerIDMap.put(layerElement.getAttribute("id"), newLayer);
                                                layerSortOrder = layerSortOrder + "," + newLayer.getId();
                                                if (layerSortOrder.startsWith(","))
                                                    layerSortOrder = layerSortOrder.substring(1);

                                                /*
                                                 * create polygons
                                                 */
                                                HILayer layerWrapper = new HILayer(newLayer, 100, 100);
                                                HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.24")+" " + counter + ": "+Messages.getString("PeTALImporter.28")+" : "+Messages.getString("PeTALImporter.29")+" " + (a + 1) +": "+Messages.getString("PeTALImporter.30"));
                                                // import polygons
                                                NodeList polygonElements = svgElement.getElementsByTagNameNS("http://www.w3.org/2000/svg", "polygon");
                                                for (int p = 0; p < polygonElements.getLength(); p++) {
                                                    Element polygonElement = (Element) polygonElements.item(p);
                                                    boolean isCustom = false;
                                                    if ( polygonElement.getAttribute("type") != null && polygonElement.getAttribute("type").compareTo("custom") == 0 )
                                                        isCustom = true;

                                                    String points = polygonElement.getAttribute("points");
                                                    if ( points == null || points.length() == 0)
                                                        points = "";
                                                    else {
                                                        if ( isCustom ) points = "U;"+points;
                                                        else points="F;"+points;
                                                    }
                                                    points = points.replaceAll(" ", ";");
                                                    points = points.replaceAll(",", "#");

                                                    RelativePolygon polygon = new RelativePolygon(points, 10000,10000);
                                                    layerWrapper.getRelativePolygons().addElement(polygon);

                                                    if ( isCustom )
                                                        HIRuntime.getManager().addProjectPolygon(polygon.getModel());
                                                }
                                                // import rectangles
                                                NodeList rectElements = svgElement.getElementsByTagNameNS("http://www.w3.org/2000/svg", "rect");
                                                for (int p = 0; p < rectElements.getLength(); p++) {
                                                    Element rectElement = (Element) rectElements.item(p);

                                                    int x = (int)(Float.parseFloat(rectElement.getAttribute("x"))*10000f);
                                                    int y = (int)(Float.parseFloat(rectElement.getAttribute("y"))*10000f);
                                                    int width = (int)(Float.parseFloat(rectElement.getAttribute("width"))*10000f);
                                                    int height = (int)(Float.parseFloat(rectElement.getAttribute("height"))*10000f);

                                                    RelativePolygon polygon = new RelativePolygon(
                                                            RelativePolygon.HiPolygonTypes.HI_RECTANGLE, x, y, width, height, null, 10000, 10000);
                                                    polygon.commitChangesToModel();
                                                    layerWrapper.getRelativePolygons().addElement(polygon);
                                                }

                                                // sync with server
                                                layerWrapper.syncPolygonChanges();
                                                HIRuntime.getManager().updateLayerProperties(newLayer.getId(), red, green, blue, opacity, layerWrapper.getModel().getPolygons());
                                            }
                                        }
                                        // update layer sort order
                                        HIRuntime.getManager().updateViewSortOrder(newView, layerSortOrder);
                                    }
                                }
                                if (contentElement.getTagName().compareTo("inscription") == 0) {
                                    /*
                                     * create inscription
                                     */
                                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.24")+" " + counter + ": "+Messages.getString("PeTALImporter.31"));
                                    HiInscription newInscription = HIRuntime.getManager().createInscription(newObject);
                                    inscriptionMap.put(newInscription, contentElement);
                                    inscriptionIDMap.put(contentElement.getAttribute("id"), newInscription);
                                    sortOrder = sortOrder + "," + newInscription.getId();
                                    if (sortOrder.startsWith(",")) sortOrder = sortOrder.substring(1);
                                }
                                if (contentElement.getTagName().compareTo("standardView") == 0) {
                                    standardView = contentElement.getAttributeNS("http://www.w3.org/1999/xlink", "href");
                                    standardView = standardView.substring(standardView.lastIndexOf("/") + 1);
                                }
                            }
                            // set default view if applicable
                            if (standardView != null && standardView.length() > 0 && !brokenViews.contains(standardView) && viewIDMap.get(standardView) != null) {
                                HIRuntime.getManager().setDefaultView(newObject, viewIDMap.get(standardView));
                            }
                        }
                        // update object content sort order
                        HIRuntime.getManager().updateObjectSortOrder(newObject, sortOrder);

                        counter++;
                    }

                    /*
                     * set group memberships
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.32"));
                    for (Element groupElement : groupMap.values()) {
                        long groupID = groupIDMap.get(groupElement.getAttribute("id")).getId();
                        sortOrder = "";
                        NodeList memberElements = groupElement.getElementsByTagNameNS("http://www.hyperimage.eu/PeTAL/2.0", "member");
                        for (int i = 0; i < memberElements.getLength(); i++) {
                            Element memberElement = (Element) memberElements.item(i);
                            long memberID = resolveNewID(memberElement.getAttributeNS("http://www.w3.org/1999/xlink", "href"));
                            if (memberID > 0) {
                                HIRuntime.getManager().addToGroup(memberID, groupID);
                                sortOrder = sortOrder+","+memberID;
                                if ( sortOrder.startsWith(",") )
                                sortOrder = sortOrder.substring(1);
                            }
                        }
                        // update group membership sort order
                        HIRuntime.getManager().updateGroupSortOrder(groupID, sortOrder);
                    }
                    
                    /*
                     * set layer links
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.33"));
                    for (Element layerElement : layerMap.values()) {
                        long layerID = layerIDMap.get(layerElement.getAttribute("id")).getId();
                        String href = layerElement.getAttributeNS("http://www.w3.org/1999/xlink", "href");
                        // process link, find new id if applicable
                        if ( href != null && href.length() > 0 ) {
                            long newID = resolveNewID(href);
                            if ( newID > 0 )
                                HIRuntime.getManager().setLayerLink(layerID, newID);
                        }
                    }

                    /*
                     * convert and add text content
                     */
                    counter = 1;
                    for (Element textElement : textMap.values()) {
                        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.34")+" ("+counter+" "+Messages.getString("PeTALImporter.25")+" "+textMap.values().size()+") "+Messages.getString("PeTALImporter.35"));
                        HiText text = textIDMap.get(textElement.getAttribute("id"));

                        attachMetadataRecords(textElement,
                                text.getMetadata(),
                                "http://purl.org/dc/elements/1.1/",
                                "title",
                                "title",
                                null, null, null,
                                "http://www.hyperimage.eu/PeTAL/HIView/1.0",
                                "content",
                                "content");
                        counter++;
                    }

                    /*
                     * convert and add view metadata
                     */
                    counter = 1;
                    for (Element viewElement : viewMap.values()) {
                        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.36")+" ("+counter+" "+Messages.getString("PeTALImporter.25")+" "+viewMap.values().size()+") "+Messages.getString("PeTALImporter.35"));
                        HiView view = viewIDMap.get(viewElement.getAttribute("id"));

                        // TODO FIXME --> only attach viewElement metadata, NOT layer metadata
                        attachMetadataRecords(viewElement,
                                view.getMetadata(),
                                "http://purl.org/dc/elements/1.1/",
                                "title",
                                "title",
                                "http://purl.org/dc/elements/1.1/",
                                "source",
                                "source",
                                "http://www.hyperimage.eu/PeTAL/HIView/1.0",
                                "annotation",
                                "comment");
                        counter++;
                    }

                    /*
                     * convert and add inscription metadata
                     */
                    counter = 1;
                    for (Element inscriptionElement : inscriptionMap.values()) {
                        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.37")+" ("+counter+" "+Messages.getString("PeTALImporter.25")+" "+inscriptionMap.values().size()+") "+Messages.getString("PeTALImporter.35"));
                        HiInscription inscription = inscriptionIDMap.get(inscriptionElement.getAttribute("id"));

                        attachMetadataRecords(inscriptionElement,
                                inscription.getMetadata(),
                                null, null, null,
                                null, null, null,
                                "http://www.hyperimage.eu/PeTAL/HIView/1.0",
                                "content",
                                "content");

                        counter++;
                    }

                    /*
                     * convert and add layer metadata
                     */
                    counter = 1;
                    for (Element layerElement : layerMap.values()) {
                        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.38")+" ("+counter+" "+Messages.getString("PeTALImporter.25")+" "+layerMap.values().size()+") "+Messages.getString("PeTALImporter.35"));
                        HiLayer layer = layerIDMap.get(layerElement.getAttribute("id"));

                        attachMetadataRecords(layerElement,
                                layer.getMetadata(),
                                "http://purl.org/dc/elements/1.1/",
                                "title",
                                "title",
                                null, null, null,
                                "http://www.hyperimage.eu/PeTAL/HIView/1.0",
                                "annotation",
                                "comment");
                        counter++;
                    }

                    /*
                     * convert and add group metadata
                     */
                    counter = 1;
                    for (Element groupElement : groupMap.values()) {
                        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.39")+" ("+counter+" "+Messages.getString("PeTALImporter.25")+" "+groupMap.values().size()+") "+Messages.getString("PeTALImporter.35"));
                        HiGroup group = groupIDMap.get(groupElement.getAttribute("id"));

                        attachMetadataRecords(groupElement,
                                group.getMetadata(),
                                "http://purl.org/dc/elements/1.1/",
                                "title",
                                "title",
                                null, null, null,
                                "http://www.hyperimage.eu/PeTAL/HIView/1.0",
                                "annotation",
                                "comment");
                        counter++;
                    }

                    /*
                     * convert and add lita xml --> limited legacy support
                     */
                    counter = 1;
                    for (Element litaElement : litaMap.values()) {
                        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.40")+" ("+counter+" "+Messages.getString("PeTALImporter.25")+" "+litaMap.values().size()+") "+Messages.getString("PeTALImporter.35"));
                        HiLightTable lita = litaIDMap.get(litaElement.getAttribute("id"));

                        String litaXML = lita.getXml();
                        litaXML = litaXML.replaceAll("<lita id=\"X[0123456789]+\">", "<lita id=\"X"+lita.getId()+"\">");
                        String convertedXML = "";
                        int refCounter = 0;
                        for (String refString : litaXML.split("ref=\"") ) {
                            if (refCounter > 0) {
                                convertedXML = convertedXML + "ref=\"";
                                String ref = refString.substring(0, refString.indexOf("\""));
                                ref = resolveDisplayableNewID("petal://legacy/"+ref);
                                if ( ref == null || ref.length() == 0 ) ref = "invalid";
                                convertedXML = convertedXML+ref+refString.substring(refString.indexOf("\""));
                            } else convertedXML = convertedXML+refString;
                            refCounter++;
                        }
                        lita.setXml(convertedXML);
                        HIRuntime.getManager().updateLightTable(lita);

                        counter++;
                    }


                    /*
                     * convert and add object flex metadata
                     */
                    counter = 1;
                    for (Element objectElement : objectMap.values()) {
                        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.41")+" (" + counter + " "+Messages.getString("PeTALImporter.25")+" " + objectMap.values().size() + ") "+Messages.getString("PeTALImporter.35"));
                        HiObject object = objectIDMap.get(objectElement.getAttribute("id"));

                        NodeList descElements = objectElement.getElementsByTagNameNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "Description");
                        for (int i = 0; i < descElements.getLength(); i++) {
                            Element descElement = (Element) descElements.item(i);
                            String lang = descElement.getAttribute("xml:lang");
                            if (checkLangInProject(lang)) {
                                HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(object.getMetadata(), lang);
                                if (record != null) {
                                    // go through project templates
                                    for (HiFlexMetadataTemplate template : templates) {
                                        for (HiFlexMetadataSet set : template.getEntries()) {
                                            String content = "";
                                            if (set.isRichText() == false)
                                                content = getSingleLineTextContent(descElement, template.getNamespaceURI(), set.getTagname());
                                            else
                                                content = getMultiLineTextContent(descElement, template.getNamespaceURI(), set.getTagname());

                                            if ( content != null ) MetadataHelper.setValue(template.getNamespacePrefix(), set.getTagname(), content, record);
                                        }
                                    }
                                    HIRuntime.getManager().updateFlexMetadataRecord(record);
                                }
                            }
                        }
                        counter++;
                    }

                    /*
                     * update start ref
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.42"));
                    long newStartRef = resolveNewID(startRef);
                    if (newStartRef > 0)
                        HIRuntime.getManager().updateProjectStartElement(newStartRef);

                } catch (HIWebServiceException wse) {
                    wse.printStackTrace();
                    HIRuntime.getGui().reportError(wse, null);
                    abortWithError("");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    abortWithError(e.getMessage());
                    return;
                }

                // import successful --> update GUI
                HIRuntime.getGui().stopIndicatingServiceActivity();
                importSuccessful = true;

                // inform user of success
                HIRuntime.getGui().displayInfoDialog(Messages.getString("PeTALImporter.43"),
                        Messages.getString("PeTALImporter.44")+"\n"+
                        Messages.getString("PeTALImporter.45"));

                // inform user if xml contained legacy light tables
                if ( litaMap.size() > 0 )
                    HIRuntime.getGui().displayInfoDialog(
                            Messages.getString("PeTALImporter.46"),
                            Messages.getString("PeTALImporter.47")+"\n\n"+
                            Messages.getString("PeTALImporter.48"));

                 for ( String viewID : brokenViews ) {
                    HIRuntime.getGui().displayInfoDialog(
                            Messages.getString("PeTALImporter.46"),
                            Messages.getString("PeTALImporter.49")+" "+viewID+" "+Messages.getString("PeTALImporter.50")+"\n\n"+
                            Messages.getString("PeTALImporter.51")+"\n"+
                            Messages.getString("PeTALImporter.52"));
                 }
                HIRuntime.getGui().triggerProjectUpdate();
            }
        }.start();
    }

    public boolean loadAndValidatePeTAL(File inputFile) {
        this.inputFile = inputFile;
        importSuccessful = false;
        startRef = "";

        try {
            // try to read and parse file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            petalXML = builder.parse(inputFile);

            // check if root node is petal
            if (petalXML.getDocumentElement().getTagName().compareTo("petal") != 0) {
                petalXML = null;
                return false;
            }

            // get version number
            version = petalXML.getDocumentElement().getAttribute("version");
            if (version == null || version.length() == 0 ) {
                petalXML = null;
                return false;
            }

            // get start element
            startRef = petalXML.getDocumentElement().getAttribute("startRef");
            if ( startRef == null || startRef.length() == 0 ) {
                petalXML = null;
                return false;
            }

            return true;
        } catch (SAXParseException spe) {
            petalXML = null;
            spe.printStackTrace();
            return false;
        } catch (SAXException sxe) {
            petalXML = null;
            sxe.printStackTrace();
            return false;
        } catch (ParserConfigurationException pce) {
            petalXML = null;
            pce.printStackTrace();
            return false;
        } catch (IOException ioe) {
            petalXML = null;
            ioe.printStackTrace();
            return false;
        }
    }
}
