/*
 * Copyright 2014 Leuphana Universität Lüneburg. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hyperimage.client.xmlimportexport;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.HIRichTextFieldControl;
import org.hyperimage.client.model.HILayer;
import org.hyperimage.client.model.HIRichText;
import org.hyperimage.client.model.HIRichTextChunk;
import org.hyperimage.client.model.RelativePolygon;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiBase;
import org.hyperimage.client.ws.HiFlexMetadataName;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiFlexMetadataSet;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiInscription;
import org.hyperimage.client.ws.HiLayer;
import org.hyperimage.client.ws.HiLightTable;
import org.hyperimage.client.ws.HiObject;
import org.hyperimage.client.ws.HiPreference;
import org.hyperimage.client.ws.HiQuickInfo;
import org.hyperimage.client.ws.HiText;
import org.hyperimage.client.ws.HiView;
import org.hyperimage.client.ws.Hiurl;
import static org.hyperimage.client.xmlimportexport.XMLImporter.XLINK_XMLNS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Jens-Martin Loebel
 */
public class PeTAL3Importer extends XMLImporter {


    SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public PeTAL3Importer(File inputFile, Document xmlDocument) {
        this.xmlDocument = xmlDocument; // inherited from superclass.
        this.inputFile = inputFile;
    }

    private String getUTCDateTime(long timestamp) {
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return utcFormat.format(new Date(timestamp));
    }
    
    private long parseTimestamp(String timestamp) {
        try {
            return utcFormat.parse(timestamp).getTime();
        } catch (ParseException ex) {
            Logger.getLogger(PeTAL3Importer.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }
    
    private String getSingleLineTextContent(Element element, String namespace, String tagName) {
        String textContent = "";

        if (element == null) {
            return textContent;
        }
        if (element.getElementsByTagNameNS(namespace, tagName).getLength() > 0) {
            String temp = element.getElementsByTagNameNS(namespace, tagName).item(0).getTextContent();
            if (temp != null) {
                textContent = temp.trim();
            }
        }

        return textContent;
    }

    private String getMultiLineTextContent(Element element, String namespace, String tagName) {
        String textContent = new HIRichText().getModel();

        if (element == null) {
            return textContent;
        }
        if (element.getElementsByTagNameNS(namespace, tagName).getLength() > 0) {
            textContent = convertRichTextXMLField((Element) element.getElementsByTagNameNS(namespace, tagName).item(0));
        }

        // simplify text content
        HIRichTextFieldControl control = new HIRichTextFieldControl("import");
        control.setText(textContent);
        textContent = control.getText();

        return textContent;
    }

    private String getTextNodeContent(Node textNode) {
        String textContent = "";

        // convert text node, trim whitespace
        textContent = textNode.getTextContent();

        textContent = textContent.replaceAll("[ ]*[\t\n]+[\t\n ]*\\z", "");
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
            } else if ( subNode.getNodeType() == Node.ELEMENT_NODE 
                    && ((Element) subNode).getNamespaceURI().compareTo(XHTML_XMLNS) == 0
                    && ((Element) subNode).getLocalName().compareTo("br") == 0) {
                textContent = textContent + "\n";
            }
        }

        return textContent;
    }

    private String convertRichTextXMLField(Element richTextElement) {
        HIRichText hiText = new HIRichText();
        String chunkText;

        NodeList chunkNodes = richTextElement.getChildNodes();
        for (int i = 0; i < chunkNodes.getLength(); i++) {
            Node chunkNode = chunkNodes.item(i);
            
            if (chunkNode.getNodeType() == Node.TEXT_NODE) {
                chunkText = getTextNodeContent(chunkNode);
                boolean nextNode = (i < (chunkNodes.getLength() - 1));
                while (nextNode) {
                    nextNode = false;
                    if ( chunkNodes.item(i + 1).getNodeType() == Node.ELEMENT_NODE
                        && chunkNodes.item(i + 1).getNamespaceURI().compareTo(XHTML_XMLNS) == 0
                        && chunkNodes.item(i + 1).getLocalName().compareTo("br") == 0 ) {
                        chunkText += "\n";
                        nextNode = true;
                        i++;
                    } else if ( chunkNodes.item(i + 1).getNodeType() == Node.TEXT_NODE ) {
                        chunkText += getTextNodeContent(chunkNodes.item(i + 1));
                        nextNode = true;
                        i++;
                    }
                    nextNode = nextNode && (i < (chunkNodes.getLength() - 1));
                }
                // add regular rich text chunk
                if ( chunkText.length() > 0 ) hiText.addChunk(HIRichTextChunk.chunkTypes.REGULAR, chunkText);
            } else if ( chunkNode.getNodeType() == Node.ELEMENT_NODE ) {
                Element chunkElement = (Element) chunkNode;
                if (chunkElement.getNamespaceURI().compareTo(XHTML_XMLNS) == 0 // bold rich text chunk
                    && chunkElement.getLocalName().compareTo("b") == 0) {
                    chunkText = getSubElementText(chunkElement);
                    if ( chunkText.length() > 0 ) hiText.addChunk(HIRichTextChunk.chunkTypes.BOLD, chunkText);
                } else if (chunkElement.getNamespaceURI().compareTo(XHTML_XMLNS) == 0 // italic rich text chunk
                    && chunkElement.getLocalName().compareTo("i") == 0) {
                    chunkText = getSubElementText(chunkElement);
                    if ( chunkText.length() > 0 ) hiText.addChunk(HIRichTextChunk.chunkTypes.ITALIC, chunkText);
                } else if (chunkElement.getNamespaceURI().compareTo(XHTML_XMLNS) == 0 // underline rich text chunk
                    && chunkElement.getLocalName().compareTo("u") == 0) {
                    chunkText = getSubElementText(chunkElement);
                    if ( chunkText.length() > 0 ) hiText.addChunk(HIRichTextChunk.chunkTypes.UNDERLINE, chunkText);
                } else if (chunkElement.getNamespaceURI().compareTo(XHTML_XMLNS) == 0 // single line break chunk
                    && chunkElement.getLocalName().compareTo("br") == 0) {
                    chunkText = "\n";
                    hiText.addChunk(HIRichTextChunk.chunkTypes.REGULAR, chunkText);
                } else if (chunkElement.getNamespaceURI().compareTo(PeTAL_3_0_XMLNS) == 0 // link rich text chunk
                    && chunkElement.getLocalName().compareTo("link") == 0) {
                    String href = chunkElement.getAttributeNS(XLINK_XMLNS, "href");
                    chunkText = "";
                    if (href != null && href.length() > 0) chunkText = getSubElementText(chunkElement);
                    if ( chunkText.length() > 0 ) hiText.addChunk(HIRichTextChunk.chunkTypes.LINK, chunkText, href);
                } 
            } 
        }

        return hiText.getModel();
    }

    private void attachMetadataRecords(Element mdElement, List<HiFlexMetadataRecord> records,
            String nsURI1, String tagName1, String mdName1,
            String nsURI2, String tagName2, String mdName2,
            String nsURI3, String tagName3, String mdName3)
            throws HIWebServiceException {
        NodeList descElements = mdElement.getElementsByTagNameNS(RDF_XMLNS, "Description");
        
        attachMetadataRecords(descElements, records,
            nsURI1, tagName1, mdName1,
            nsURI2, tagName2, mdName2,
            nsURI3, tagName3, mdName3);
   }

    private void attachMetadataRecords(NodeList descElements, List<HiFlexMetadataRecord> records,
            String nsURI1, String tagName1, String mdName1,
            String nsURI2, String tagName2, String mdName2,
            String nsURI3, String tagName3, String mdName3)
            throws HIWebServiceException {
        for (int i = 0; i < descElements.getLength(); i++) {
            Element descElement = (Element) descElements.item(i);
            String lang = descElement.getAttribute("xml:lang");
            if (checkLangInProject(lang)) {
                String tagContent1 = null;
                if (tagName1 != null) {
                    tagContent1 = getSingleLineTextContent(descElement, nsURI1, tagName1);
                }
                String tagContent2 = null;
                if (tagName2 != null) {
                    tagContent2 = getSingleLineTextContent(descElement, nsURI2, tagName2);
                }
                String tagContent3 = null;
                if (tagName3 != null) {
                    tagContent3 = getMultiLineTextContent(descElement, nsURI3, tagName3);
                }

                HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(records, lang);
                if (record != null) {
                    if (mdName1 != null) {
                        MetadataHelper.setValue("HIBase", mdName1, tagContent1, record);
                    }
                    if (mdName2 != null) {
                        MetadataHelper.setValue("HIBase", mdName2, tagContent2, record);
                    }
                    if (mdName3 != null) {
                        MetadataHelper.setValue("HIBase", mdName3, tagContent3, record);
                    }
                }
                HIRuntime.getManager().updateFlexMetadataRecord(record);
            }
        }
    }

    private void importLanguages() throws HIWebServiceException {
        //compile list of languages
        languages.clear();
        NodeList langElements = rootElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "language");
        String defLang = null;
        for (int i = 0; i < langElements.getLength(); i++) {
            Element langElement = (Element) langElements.item(i);
            languages.add(langElement.getTextContent().trim());
            if (langElement.getAttribute("default") != null && langElement.getAttribute("default").equalsIgnoreCase("true")) {
                defLang = langElement.getTextContent().trim();
            }
        }
        // add languages to project
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.8"));
        for (String lang : languages)
            if (!checkLangInProject(lang)) HIRuntime.getManager().addLanguageToProject(lang);

        // set default language
        if ( defLang != null ) HIRuntime.getManager().updateProjectDefaultLanguage(defLang);
    }
    
    private void importProjectMetadata() throws HIWebServiceException {
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.9"));
        if (rootElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "projectMetadata").getLength() > 0) {
            Element projMetadataElement = (Element) rootElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "projectMetadata").item(0);
            NodeList mdElements = projMetadataElement.getElementsByTagNameNS(RDF_XMLNS, "Description");
            for (int i = 0; i < mdElements.getLength(); i++) {
                Element mdElement = (Element) mdElements.item(i);
                mdElement = (Element) mdElement.getElementsByTagNameNS(DC_1_1_XMLNS, "title").item(0);
                
                if (checkLangInProject(mdElement.getAttribute("xml:lang")))
                    HIRuntime.getManager().updateProject(mdElement.getAttribute("xml:lang"), mdElement.getTextContent().trim());
            }
        }
    }
    
    private void importTemplates() throws HIWebServiceException {
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.10"));
        NodeList templateElements = rootElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "template");

        for (int i = 0; i < templateElements.getLength(); i++) {
            Element templateElement = (Element) templateElements.item(i);
            if (!templateElement.getAttribute("nsPrefix").equalsIgnoreCase("HIBase")) {
                // check if template is in project
                // assume templates with same nsPrefix and schema URI always have the same fields
                if ( !checkTemplateInProject(templateElement.getAttribute("nsPrefix"), templateElement.getAttribute("schema"))) {
                    // build and add new template
                    HiFlexMetadataTemplate newTemplate = new HiFlexMetadataTemplate();
                    newTemplate.setNamespacePrefix(templateElement.getAttribute("nsPrefix")); //$NON-NLS-1$
                    newTemplate.setNamespaceURI(templateElement.getAttribute("schema")); //$NON-NLS-1$
                    newTemplate.setNamespaceURL(templateElement.getAttribute("schemaLocation")); //$NON-NLS-1$
                    HiFlexMetadataSet newSet = null;
                    HiFlexMetadataName newName = null;

                    NodeList keyElements = templateElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "key");
                    for (int a = 0; a < keyElements.getLength(); a++) {
                        Element keyElement = (Element) keyElements.item(a);
                        newSet = new HiFlexMetadataSet();
                        newSet.setRichText(false);
                        if (keyElement.getAttribute("richText") != null && keyElement.getAttribute("richText").equalsIgnoreCase("true")) {
                            newSet.setRichText(true);
                        }
                        newSet.setTagname(keyElement.getAttribute("tagName")); //$NON-NLS-1$

                        NodeList nameElements = keyElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "displayName");
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
                    HIRuntime.getManager().addTemplateToProject(newTemplate);

                // add user-defined fields to custom template if missing
                } else if (templateElement.getAttribute("nsPrefix").compareTo("custom") == 0) {
                    // find custom template in project
                    HiFlexMetadataTemplate customTemplate = MetadataHelper.getTemplateByNSPrefix("custom");
                    if (customTemplate != null) {
                        NodeList keyElements = templateElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "key");
                        for (int a = 0; a < keyElements.getLength(); a++) {                            
                            Element keyElement = (Element) keyElements.item(a);
                            // add field
                            if ( MetadataHelper.findTemplateEntryByTagName(keyElement.getAttribute("tagName"), customTemplate) == null ) {
                                boolean richText = false;
                                if (keyElement.getAttribute("richText") != null && keyElement.getAttribute("richText").equalsIgnoreCase("true"))
                                    richText = true;
                                HiFlexMetadataSet newSet = HIRuntime.getManager().addSetToTemplate(customTemplate, keyElement.getAttribute("tagName"), richText);
                                // set display names
                                if ( newSet != null ) {
                                NodeList nameElements = keyElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "displayName");
                                    for (int b = 0; b < nameElements.getLength(); b++) {
                                        Element nameElement = (Element) nameElements.item(b);
                                        HIRuntime.getManager().createSetDisplayName(newSet, nameElement.getAttribute("xml:lang"), nameElement.getTextContent().trim());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void importGroups() throws HIWebServiceException {
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.23"));
        groups.clear();
        xmlImportGroup = null;

        HiPreference groupSortOrderPref = MetadataHelper.findPreference(HIRuntime.getManager().getProject(), "groupSortOrder");
        String sortOrder = "";
        if ( groupSortOrderPref != null && groupSortOrderPref.getValue() != null )
            sortOrder = groupSortOrderPref.getValue();
        
        // compile group list
        NodeList projElements = rootElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "group");
        for (int i = 0; i < projElements.getLength(); i++) {
            Element groupElement = (Element) projElements.item(i);
            if (!groupElement.getAttribute("type").equalsIgnoreCase("import")) {
                groups.add(groupElement);
            } else if ( groupElement.getAttribute("type").equalsIgnoreCase("import") )
                xmlImportGroup = groupElement;
        }

        // import groups
        for (Element groupElement : groups) {
            // incremental import --> check if object already exists in project
            HiGroup group = null;
            try {
                // try to fetch HIGroup from server using uuid
                HiBase base = HIRuntime.getManager().getBaseElement(groupElement.getAttribute("id"));
                if ( base != null && base instanceof HiGroup ) group = (HiGroup)base;
            } catch(HIWebServiceException e) {
                // element not found
            }
            
            boolean isNewGroup = false;
            if ( group == null ) {
                group = HIRuntime.getManager().createGroup(groupElement.getAttribute("id"));
                isNewGroup = true;
                if (group != null) {
                    sortOrder = sortOrder + "," + group.getId();
                    if (sortOrder.startsWith(",")) {
                        sortOrder = sortOrder.substring(1);
                    }
                }                
            }
            long timestamp = parseTimestamp (groupElement.getAttribute("timestamp"));
            if ( isNewGroup || (group != null && group.getTimestamp() < timestamp ) ) {
                // add / update group metadata
                attachMetadataRecords(groupElement,
                    group.getMetadata(),
                    DC_1_1_XMLNS,
                    "title",
                    "title",
                    null, null, null,
                    HIBASE_2_0_XMLNS,
                    "annotation",
                    "comment");
            }
             
        }
        // update group sort order
        if ( groupSortOrderPref != null ) {
            groupSortOrderPref.setValue(sortOrder);
            HIRuntime.getManager().updatePreference(groupSortOrderPref);
        }
    }
    
    private void importTexts() throws HIWebServiceException {
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.11"));
        texts.clear();
        NodeList textElements = rootElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "text");
        int counter = 1;

        // Compile list of texts.
        for (int i = 0; i < textElements.getLength(); i++) {
            Element textElement = (Element) textElements.item(i);
            texts.add(textElement);
        }

        for (Element textElement : texts) {
            HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.34")
                    + " (" + counter + " " + Messages.getString("PeTALImporter.25")
                    + " " + texts.size() + ") " + Messages.getString("PeTALImporter.35"));

            HiText text = null;
            try {
                // try to fetch HIText from server using uuid
                HiBase base = HIRuntime.getManager().getBaseElement(textElement.getAttribute("id"));
                if (base != null && base instanceof HiText) {
                    text = (HiText) base;
                }
            } catch (HIWebServiceException e) {
                // element not found
            }

            boolean isNewText = false;
            if (text == null) {
                text = HIRuntime.getManager().createText(textElement.getAttribute("id"));
                isNewText = true;
            }
            long timestamp = parseTimestamp(textElement.getAttribute("timestamp"));
            if (isNewText || (text != null && text.getTimestamp() < timestamp)) {
                attachMetadataRecords(textElement,
                        text.getMetadata(),
                        DC_1_1_XMLNS,
                        "title",
                        "title",
                        null, null, null,
                        HIBASE_2_0_XMLNS,
                        "content",
                        "content");
            }
            counter++;
        }
    }

    private void importURLs() throws HIWebServiceException {
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.16"));
        NodeList urlElements = rootElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "url");
        urls.clear();

        // Compile list of URLs.
        for (int i = 0; i < urlElements.getLength(); i++) {
            Element urlElement = (Element) urlElements.item(i);
            urls.add(urlElement);
        }

        // Create URLs.
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.21"));
        for (Element urlElement : urls) {
            Hiurl url = null;
            try {
                // try to fetch HIURL from server using uuid
                HiBase base = HIRuntime.getManager().getBaseElement(urlElement.getAttribute("id"));
                if (base != null && base instanceof Hiurl) {
                    url = (Hiurl) base;
                }
            } catch (HIWebServiceException e) {
                // element not found
            }
            String title = getSubElementText(urlElement);
            String lastAccess = "";
            if ( urlElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "annotation").getLength() > 0 ) {
                lastAccess = convertRichTextXMLField((Element) urlElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "annotation").item(0));
                if ( lastAccess == null ) lastAccess = "";
            }

            boolean isNewURL = false;
            if (url == null) {
                url = HIRuntime.getManager().createURL(urlElement.getAttributeNS(XLINK_XMLNS, "href"), title, lastAccess, urlElement.getAttribute("id"));
                isNewURL = true;
            }
            long timestamp = parseTimestamp(urlElement.getAttribute("timestamp"));
            if (isNewURL || (url != null && url.getTimestamp() < timestamp)) {
                url.setUrl(urlElement.getAttributeNS(XLINK_XMLNS, "href"));
                url.setTitle(title);
                url.setLastAccess(lastAccess);
                HIRuntime.getManager().updateURL(url);
            }
            
        }
    }
    
    private void stageObjectImport() {
        objects.clear();
        views.clear();
        inscriptions.clear();
        layers.clear();
        NodeList projElements = null;
        // Compile list of ojbects.
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.12"));
        projElements = rootElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "object");
        for (int i = 0; i < projElements.getLength(); i++) {
            Element objectElement = (Element) projElements.item(i);
            objects.add(objectElement);
        }

        // Compile list of views.
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.13"));
        projElements = rootElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "view");
        for (int i = 0; i < projElements.getLength(); i++) {
            Element viewElement = (Element) projElements.item(i);
            views.add(viewElement);
        }
        // Compile list of inscriptions.
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.14"));
        projElements = rootElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "inscription");
        for (int i = 0; i < projElements.getLength(); i++) {
            Element inscriptionElement = (Element) projElements.item(i);
            inscriptions.add(inscriptionElement);
        }

        // Compile list of layers.
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.15"));
        projElements = rootElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "layer");
        for (int i = 0; i < projElements.getLength(); i++) {
            Element layerElement = (Element) projElements.item(i);
            layers.add(layerElement);
        }
    }
    
   
    private void evaluateBinaries() {
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.19"));
        corruptViews.clear();
        for (Element viewElement : views) {
            if (!(viewElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "original").getLength() > 0)
                    || !(viewElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "img").getLength() > 0)) {
                corruptViews.add(viewElement.getAttribute("id"));
            } else {
                // TODO verify hash
                Element origElement = (Element) viewElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "original").item(0);
                Element imgElement = (Element) viewElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "img").item(0);          
                
                // search / verify local file
                String pathToFile = inputFile.getParent() + "/" + imgElement.getAttribute("src");
                if (File.separator.compareTo("\\") == 0) {
                    pathToFile = pathToFile.replaceAll("/", "\\" + File.separator);
                } else {
                    pathToFile = pathToFile.replaceAll("/", File.separator);
                }
                    File binFile = new File(pathToFile);
                if (!binFile.exists() || !binFile.canRead() || !binFile.isFile()) {
                    corruptViews.add(viewElement.getAttribute("id"));
				}
                
            }
        }
    }

    private void importObjectsViewsInscriptionsLayers() throws HIWebServiceException {
        String sortOrder;

        stageObjectImport();
        evaluateBinaries();

        int counter = 1;

        for (Element objectElement : objects) {
            HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.24")
                    + " " + counter + " " + Messages.getString("PeTALImporter.25") + " "
                    + objects.size() + " " + Messages.getString("PeTALImporter.26"));
            HiObject object = null;

            try {
                // try to fetch HIObject from server using uuid
                HiBase base = HIRuntime.getManager().getBaseElement(objectElement.getAttribute("id"));
                if (base != null && base instanceof HiObject) {
                    object = (HiObject) base;
                }
            } catch (HIWebServiceException e) {
                // element not found
            }

            boolean isNewObject = false;
            if (object == null) {
                object = HIRuntime.getManager().createObject(objectElement.getAttribute("id"));
                isNewObject = true;
            }
            long timestamp = parseTimestamp(objectElement.getAttribute("timestamp"));
            if (isNewObject || (object != null && object.getTimestamp() < timestamp)) {
                // attach object metadata
                NodeList descElements = objectElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "metadata");
                for (int i = 0; i < descElements.getLength(); i++) {
                    Element descElement = (Element) descElements.item(i);
                    boolean foundMetadata = false;
                    if (descElement.getElementsByTagNameNS(RDF_XMLNS, "Description").getLength() > 0) {
                        descElement = (Element) descElement.getElementsByTagNameNS(RDF_XMLNS, "Description").item(0);
                        foundMetadata = true;
                    }
                    String lang = descElement.getAttribute("xml:lang");
                    if (checkLangInProject(lang) && foundMetadata) {
                        HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(object.getMetadata(), lang);
                        if (record != null) {
                            // go through project templates
                            for (HiFlexMetadataTemplate template : HIRuntime.getManager().getProject().getTemplates()) {
                                for (HiFlexMetadataSet set : template.getEntries()) {
                                    String content = "";
                                    String namespaceURI = template.getNamespaceURI();
                                    if (namespaceURI == null || namespaceURI.length() == 0) {
                                        namespaceURI = "unknown";
                                    }
                                    if (set.isRichText()) {
                                        content = getMultiLineTextContent(descElement, namespaceURI, set.getTagname());
                                    } else {
                                        content = getSingleLineTextContent(descElement, namespaceURI, set.getTagname());
                                    }

                                    if (content != null) {
                                        _DBG(content); // to see formatting of rich text. ~HGK
                                        MetadataHelper.setValue(template.getNamespacePrefix(), set.getTagname(), content, record);
                                    }
                                }
                            }
                            HIRuntime.getManager().updateFlexMetadataRecord(record);
                        }
                    }
                }
            }
            // gather views and inscriptions
            NodeList contentElements = objectElement.getChildNodes();
            String defaultView = null;
            sortOrder = (object != null) ? object.getSortOrder() : "";
            if ( sortOrder == null ) sortOrder = "";
            
            for (int i = 0; i < contentElements.getLength(); i++) {
                if (contentElements.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element contentElement = (Element) contentElements.item(i);
                    if (contentElement.getTagName().compareTo("view") == 0
                            && !corruptViews.contains(contentElement.getAttribute("id"))) {
                        /*
                         * create view
                         */
                        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.24") + " "
                                + counter + ": " + Messages.getString("PeTALImporter.27"));
                        Element origElement = (Element) contentElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "original").item(0);
                        Element imgElement = (Element) contentElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "img").item(0);
                        
                        boolean bytesRead = false;
                        byte[] data = null;
                        String filename = "unknown.jpg";
                        String repos = "[PeTAL 3.0 XML Import]";
                        
                            // read image data from local file
                            String pathToFile = inputFile.getParent() + "/" + imgElement.getAttribute("src");
                            if (File.separator.compareTo("\\") == 0) {
                                pathToFile = pathToFile.replaceAll("/", "\\" + File.separator);
                            } else {
                                pathToFile = pathToFile.replaceAll("/", File.separator);
                            }
                            File binFile = new File(pathToFile);
                            try {
                                data = HIRuntime.getBytesFromFile(binFile);
                                bytesRead = true;
                                filename = origElement.getAttribute("filename");
                            } catch (IOException ex) {
                                // ignore
                            }
							if (bytesRead && data != null) {
                            HiView view = null;
                            try {
                                // try to fetch HIView from server using uuid
                                HiBase base = HIRuntime.getManager().getBaseElement(contentElement.getAttribute("id"));
                                if (base != null && base instanceof HiView) {
                                    view = (HiView) base;
                                }
                            } catch (HIWebServiceException e) {
                                // element not found
                            }

                            boolean isNewView = false;
                            if (view == null) {
                                view = HIRuntime.getManager().createView(
                                        object, 
                                        filename, 
                                        repos, 
                                        data, 
                                        contentElement.getAttribute("id"));
                                isNewView = true;
                            }
                            timestamp = parseTimestamp(contentElement.getAttribute("timestamp"));
                            if (isNewView || (view != null && view.getTimestamp() < timestamp)) {
                                // attach view metadata
                                Element rdfElement = null;
                                for ( int cn=0; cn < contentElement.getChildNodes().getLength(); cn++ ) {
                                    Node child = contentElement.getChildNodes().item(cn);
                                    if ( child.getNodeType() == Node.ELEMENT_NODE
                                            && child.getNamespaceURI().compareTo(RDF_XMLNS) == 0 
                                            && child.getLocalName().compareTo("RDF") == 0 ) {
                                        rdfElement = (Element)child;
                                    }
                                }
                                if ( rdfElement != null && rdfElement.getElementsByTagNameNS(RDF_XMLNS, "Description").getLength() > 0 ) {
                                    attachMetadataRecords(rdfElement.getElementsByTagNameNS(RDF_XMLNS, "Description"),
                                        view.getMetadata(),
                                        DC_1_1_XMLNS,
                                        "title",
                                        "title",
                                        DC_1_1_XMLNS,
                                        "source",
                                        "source",
                                        HIBASE_2_0_XMLNS,
                                        "annotation",
                                        "comment");
                                }
                            }
                            if (!isNewView) HIRuntime.getManager().updateContentOwner(object, view);

                            data = null;
                            if ( isNewView ) sortOrder = sortOrder + "," + view.getId();
                            if (sortOrder.startsWith(",")) {
                                sortOrder = sortOrder.substring(1);
                            }

                            /*
                             * create layer(s)
                             */
                            String layerSortOrder = (view != null) ? view.getSortOrder() : "";
                            if ( layerSortOrder == null ) layerSortOrder = "";

                            NodeList layerElements = contentElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "layer");
                            // pre-sort layers by "order" attribute
                            Vector<Element>sortedLayers = new Vector<>();
                            for (int a = 0; a < layerElements.getLength(); a++) {
                                sortedLayers.addElement((Element) layerElements.item(a));
                            }
                            Collections.sort(sortedLayers, new Comparator() {
                                @Override
                                public int compare(Object o1, Object o2) {
                                    int order1=0;
                                    int order2=0;
                                    try {
                                        order1 = Integer.parseInt(((Element)o1).getAttribute("order"));
                                        order2 = Integer.parseInt(((Element)o2).getAttribute("order"));
                                    } catch (NumberFormatException e) {
                                        // ignore
                                    }
                                    return order1-order2;
                                }
                            });
                            // parse layer elements
                            for (int a = 0; a < sortedLayers.size(); a++) {
                                HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.24") + " " + counter + ": " + Messages.getString("PeTALImporter.28") + " : " + Messages.getString("PeTALImporter.29") + " " + (a + 1) + " " + Messages.getString("PeTALImporter.25") + " " + layerElements.getLength() + " " + Messages.getString("PeTALImporter.26"));
                                Element layerElement = sortedLayers.get(a);
                                if (layerElement.getElementsByTagNameNS(SVG_XMLNS, "g").getLength() > 0) {
                                    Element svgElement = (Element) layerElement.getElementsByTagNameNS(SVG_XMLNS, "g").item(0);
                                    String colorString = svgElement.getAttribute("fill");
                                    float opacity = 1.0f;
                                    if (svgElement.getAttribute("opacity") != null) {
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

                                    HiLayer layer = null;
                                    try {
                                        // try to fetch HILayer from server using uuid
                                        HiBase base = HIRuntime.getManager().getBaseElement(layerElement.getAttribute("id"));
                                        if (base != null && base instanceof HiLayer) {
                                            layer = (HiLayer) base;
                                        }
                                    } catch (HIWebServiceException e) {
                                        // element not found
                                    }

                                    boolean isNewLayer = false;
                                    if (layer == null) {
                                        layer = HIRuntime.getManager().createLayer(view, red, green, blue, opacity, layerElement.getAttribute("id"));
                                        isNewLayer = true;
                                    }
                                    
                                    if ( isNewLayer ) layerSortOrder +=  "," + layer.getId();
                                    
                                    layerIDMap.put(layer.getUUID(), layer); // add to map --> add layer links when all project elements are imported

                                    /*
                                     * create polygons
                                     */
                                    layer.setPolygons("");
                                    HILayer layerWrapper = new HILayer(layer, 100, 100);
                                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.24") + " " + counter + ": " + Messages.getString("PeTALImporter.28") + " : " + Messages.getString("PeTALImporter.29") + " " + (a + 1) + ": " + Messages.getString("PeTALImporter.30"));
                                    // import polygons
                                    NodeList polygonElements = svgElement.getElementsByTagNameNS(SVG_XMLNS, "polygon");
                                    for (int p = 0; p < polygonElements.getLength(); p++) {
                                        Element polygonElement = (Element) polygonElements.item(p);
                                        boolean isCustom = false;
                                        if (polygonElement.getAttribute("type") != null && polygonElement.getAttribute("type").compareTo("custom") == 0) {
                                            isCustom = true;
                                        }

                                        String points = polygonElement.getAttribute("points");
                                        if (points == null || points.length() == 0) {
                                            points = "";
                                        } else {
                                            if (isCustom) {
                                                points = "U;" + points;
                                            } else {
                                                points = "F;" + points;
                                            }
                                        }
                                        points = points.replaceAll(" ", ";");
                                        points = points.replaceAll(",", "#");

                                        RelativePolygon polygon = new RelativePolygon(points, 10000, 10000);
                                        layerWrapper.getRelativePolygons().addElement(polygon);

                                        if (isCustom) {
                                            HIRuntime.getManager().addProjectPolygon(polygon.getModel());
                                        }
                                    }
                                    // import rectangles
                                    NodeList rectElements = svgElement.getElementsByTagNameNS(SVG_XMLNS, "rect");
                                    for (int p = 0; p < rectElements.getLength(); p++) {
                                        Element rectElement = (Element) rectElements.item(p);

                                        int x = (int) (Float.parseFloat(rectElement.getAttribute("x")) * 10000f);
                                        int y = (int) (Float.parseFloat(rectElement.getAttribute("y")) * 10000f);
                                        int width = (int) (Float.parseFloat(rectElement.getAttribute("width")) * 10000f);
                                        int height = (int) (Float.parseFloat(rectElement.getAttribute("height")) * 10000f);

                                        RelativePolygon polygon = new RelativePolygon(
                                                RelativePolygon.HiPolygonTypes.HI_RECTANGLE, x, y, width, height, null, 10000, 10000);
                                        polygon.commitChangesToModel();
                                        layerWrapper.getRelativePolygons().addElement(polygon);
                                    }
                                    // import paths, limited support
                                    NodeList pathElements = svgElement.getElementsByTagNameNS(SVG_XMLNS, "path");
                                    for (int p = 0; p < pathElements.getLength(); p++) {
                                        Element pathElement = (Element) pathElements.item(p);
                                        String path = pathElement.getAttribute("d");
                                        if (path != null && path.length() > 0) {
                                            path = path.replaceAll("z", "");
                                            path = path.replaceAll("Z", "");
                                            for (String points : path.split("M")) {
                                                points = points.trim();
                                                if (points.length() > 0) {
                                                    points = "F;" + points;
                                                    points = points.replaceAll(" ", ";");
                                                    points = points.replaceAll(",", "#");

                                                    RelativePolygon polygon = new RelativePolygon(points, 10000, 10000);
                                                    layerWrapper.getRelativePolygons().addElement(polygon);
                                                }
                                            }
                                        }
                                    }                                    

                                    // sync with server
                                    layerWrapper.syncPolygonChanges();
                                    
                                    timestamp = parseTimestamp(layerElement.getAttribute("timestamp"));
                                    if (isNewLayer || (layer != null && layer.getTimestamp() < timestamp)) {
                                        // attach layer metadata and properties / polygons
                                        HIRuntime.getManager().updateLayerProperties(layer.getId(), red, green, blue, opacity, layerWrapper.getModel().getPolygons());
                                        attachMetadataRecords(layerElement,
                                                layer.getMetadata(),
                                                DC_1_1_XMLNS,
                                                "title",
                                                "title",
                                                null, null, null,
                                                HIBASE_2_0_XMLNS,
                                                "annotation",
                                                "comment");
                                    }
                                }
                            }
                            // update layer sort order
                            if (layerSortOrder.startsWith(",")) {
                                layerSortOrder = layerSortOrder.substring(1);
                            }

                            HIRuntime.getManager().updateViewSortOrder(view, layerSortOrder);
                        }
                    }
                    if (contentElement.getTagName().compareTo("inscription") == 0) {
                        /*
                         * create inscription
                         */
                        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.24") + " " + counter + ": " + Messages.getString("PeTALImporter.31"));
                        HiInscription inscription = null;
                        try {
                            // try to fetch HIInsctiption from server using uuid
                            HiBase base = HIRuntime.getManager().getBaseElement(contentElement.getAttribute("id"));
                            if (base != null && base instanceof HiInscription) {
                                inscription = (HiInscription) base;
                            }
                        } catch (HIWebServiceException e) {
                            // element not found
                        }

                        boolean isNewInscription = false;
                        if (inscription == null) {
                            inscription = HIRuntime.getManager().createInscription(object, contentElement.getAttribute("id"));
                            isNewInscription = true;
                        }
                        timestamp = parseTimestamp(contentElement.getAttribute("timestamp"));
                        if (isNewInscription || (inscription != null && inscription.getTimestamp() < timestamp)) {
                            // attach inscription content
                            attachMetadataRecords(contentElement,
                                    inscription.getMetadata(),
                                    null, null, null,
                                    null, null, null,
                                    HIBASE_2_0_XMLNS,
                                    "content",
                                    "content");
                        }
                        if (!isNewInscription) HIRuntime.getManager().updateContentOwner(object, inscription);
                        
                        if ( isNewInscription ) sortOrder = sortOrder + "," + inscription.getId();
                        if (sortOrder.startsWith(",")) {
                            sortOrder = sortOrder.substring(1);
                        }
                    }
                    if (contentElement.getTagName().compareTo("defaultView") == 0) {
                        defaultView = contentElement.getAttributeNS(XLINK_XMLNS, "href");
                        defaultView = defaultView.substring(1);
                    }
                }
                // set default view if applicable
                if (defaultView != null && defaultView.length() > 0 && !corruptViews.contains(defaultView) ) {
                    HiQuickInfo info = null;
                    try {
                        info = HIRuntime.getManager().getBaseQuickInfo(defaultView);
                    } catch (HIWebServiceException e) {
                        // ignore
                    }
                    if ( info != null ) HIRuntime.getManager().setDefaultView(object.getId(), info.getBaseID());
                }
            }
            
            // update object content sort order
            HIRuntime.getManager().updateObjectSortOrder(object, sortOrder);

            counter++;
        }
    }
    
    // DEBUG switch to new SVG-based light table format
    public void importLegacyLightTables() throws HIWebServiceException {
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.22"));
        litas.clear();
        int counter = 1;
        
        // Compile list of Light Tables
        NodeList litalements = rootElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "lita");
        for (int i = 0; i < litalements.getLength(); i++) {
            Element litalement = (Element) litalements.item(i);
            litas.add(litalement);
        }
        
        for (Element litaElement : litas) {

            HiLightTable lita = null;

            try {
                // try to fetch HIObject from server using uuid
                HiBase base = HIRuntime.getManager().getBaseElement(litaElement.getAttribute("id"));
                if (base != null && base instanceof HiLightTable) {
                    lita = (HiLightTable) base;
                }
            } catch (HIWebServiceException e) {
                // element not found
            }
            String title = "-";
            String litaXML;
            // try to extract title
            NodeList titleElements = litaElement.getElementsByTagName("title");
            for (int i = 0; i < titleElements.getLength(); i++) {
                Element titleElement = (Element) titleElements.item(i);
                String lang = titleElement.getAttribute("xml:lang");
                if ( lang.compareTo(HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()) == 0 ) {
                    title = titleElement.getTextContent();
                    if ( title == null || title.length() == 0 ) title = "-";
                }
            }

            // serialize legacy lita format
            litaXML = PeTALExporter.serializeXMLElement(litaElement);
            if (litaXML == null || litaXML.length() == 0 || litaXML.indexOf("<lita") < 0) {
                litaXML = MetadataHelper.getDefaultLightTableXML();
            } else {
                litaXML = litaXML.substring(litaXML.indexOf("<lita"));
            }
            HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.40") + " (" + counter + " " + Messages.getString("PeTALImporter.25") + " " + litaMap.values().size() + ") " + Messages.getString("PeTALImporter.35"));
            if ( litaXML == null ) litaXML="";
                        
            boolean isNewLita = false;
            if (lita == null) {
                lita = HIRuntime.getManager().createLightTable(title, litaXML, litaElement.getAttribute("id"));
                isNewLita = true;
            }
            long timestamp = parseTimestamp(litaElement.getAttribute("timestamp"));
            if (isNewLita || (lita != null && lita.getTimestamp() < timestamp)) {
                // update lita metadata and xml
                lita.setXml(litaXML);
                HIRuntime.getManager().updateLightTable(lita);

            }

            counter++;
        }
    }

    private void setStartRef() throws HIWebServiceException {
        startRef = xmlDocument.getDocumentElement().getAttribute("startRef");
        if (startRef == null || startRef.length() == 0) {
            if (DEBUG) {
                System.out.println(DBGIND + " unable so set start reference.");
            }
            return;
        }

        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.42"));
        HiQuickInfo info = null;
        try {
            info = HIRuntime.getManager().getBaseQuickInfo(startRef);
        } catch (HIWebServiceException e) {
            // ignore
        }
                
        if ( info != null ) {
            HIRuntime.getManager().updateProjectStartElement(info.getBaseID());
        }
    }


    public void importXMLToProject() {
  //      HIRuntime.getGui().displayInfoDialog(Messages.getString("HIClientGUI.159"), "PeTAL 3.0 Import is not yet implemented.");

        HIRuntime.getGui().startIndicatingServiceActivity(true);
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.1")); //$NON-NLS-1$
        importSuccessful = false;

        // execute import in separate thread
        new Thread() {

            @Override
            public void run() {
                try {
                    rootElement = xmlDocument.getDocumentElement();

                    importLanguages();

                    importProjectMetadata();

                    importTemplates();

                    importGroups();

                    importTexts();

                    importURLs();
                    
                    importObjectsViewsInscriptionsLayers();

                    importLegacyLightTables();

                    
                    /*
                     * set group memberships
                     */
                    NodeList projElements = null;
                    int counter = 1;

                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.32"));
                    for (Element groupElement : groups ) {
                        String sortOrder = "";
                        HiGroup group = (HiGroup) HIRuntime.getManager().getBaseElement(groupElement.getAttribute("id"));
                        if ( group != null && group.getSortOrder() != null ) sortOrder = group.getSortOrder();
                        List<HiQuickInfo> contents = HIRuntime.getManager().getGroupContents(group);

                        NodeList memberElements = groupElement.getElementsByTagNameNS(PeTAL_3_0_XMLNS, "member");
                        for (int i = 0; i < memberElements.getLength(); i++) {
                            Element memberElement = (Element) memberElements.item(i);
                            String ref = memberElement.getAttributeNS(XLINK_XMLNS, "href");
                            if ( ref != null ) ref = ref.substring(1);
                            
                            HiQuickInfo info = null;
                            try {
                                info = HIRuntime.getManager().getBaseQuickInfo(ref);
                            } catch (HIWebServiceException e) {
                                // ignore
                            }
                            boolean containsElement = false;
                            for ( HiQuickInfo content : contents ) 
                                if ( content.getBaseID() == info.getBaseID() ) containsElement = true;
                            if ( info != null && !containsElement ) {
                                HIRuntime.getManager().addToGroup(info.getBaseID(), group.getId());
                                sortOrder = sortOrder + "," + info.getBaseID();
                                if (sortOrder.startsWith(",")) {
                                    sortOrder = sortOrder.substring(1);
                                }
                            }
                        }
                        // update group membership sort order
                        HIRuntime.getManager().updateGroupSortOrder(group.getId(), sortOrder);
                    }

                    /*
                     * set layer links
                     */
                    HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.33"));
                    for (Element layerElement : layers ) {
                        HiLayer layer = layerIDMap.get(layerElement.getAttribute("id"));

                        String href = layerElement.getAttributeNS(XLINK_XMLNS, "href");
                        if ( href != null && href.length() > 0 ) href = href.substring(1);
                        // process link, find new id if applicable
                        if ( layer != null && href != null && href.length() > 0) {
                            HiQuickInfo info = null;
                            try {
                                info = HIRuntime.getManager().getBaseQuickInfo(href);
                            } catch (HIWebServiceException e) {
                                // ignore
                            }
                            if ( info != null ) HIRuntime.getManager().setLayerLink(layer.getId(), info.getBaseID());
                        }
                    }

                    setStartRef();

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
                        Messages.getString("PeTALImporter.44") + "\n"
                        + Messages.getString("PeTALImporter.45"));

                // inform user if xml contained legacy light tables
                if (litaMap.size() > 0) {
                    HIRuntime.getGui().displayInfoDialog(
                            Messages.getString("PeTALImporter.46"),
                            Messages.getString("PeTALImporter.47") + "\n\n"
                            + Messages.getString("PeTALImporter.48"));
                }

                for (String viewID : corruptViews) {
                    HIRuntime.getGui().displayInfoDialog(
                            Messages.getString("PeTALImporter.46"),
                            Messages.getString("PeTALImporter.49") + " " + viewID + " " + Messages.getString("PeTALImporter.50") + "\n\n"
                            + Messages.getString("PeTALImporter.51") + "\n"
                            + Messages.getString("PeTALImporter.52"));
                }
                HIRuntime.getGui().triggerProjectUpdate();
            }
        }.start();

    }
}
