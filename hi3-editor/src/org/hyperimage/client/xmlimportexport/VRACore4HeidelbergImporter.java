/*
 * Copyright 2014, 2015 bitGilde IT Solutions UG (haftungsbeschränkt)
 * All rights reserved. Use is subject to license terms.
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

package org.hyperimage.client.xmlimportexport;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiBase;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;
import org.hyperimage.client.ws.HiLanguage;
import org.hyperimage.client.ws.HiObject;
import org.hyperimage.client.ws.HiView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author Heinz-Günter Kuper
 */
public class VRACore4HeidelbergImporter extends XMLImporter {

    List<String[]> valueList = new ArrayList<>();
    public static final String IMAGERETRIEVALSERVICE = "http://kjc-sv016.kjc.uni-heidelberg.de:8600/images/service/download_uuid/";
    public static final String TAMBOTI_REPOS = "[Tamboti Import]";

    public VRACore4HeidelbergImporter(File inputFile, Document xmlDocument) {
        this.xmlDocument = xmlDocument; // inherited from superclass
        this.xmlDocument.normalize();
        this.inputFile = inputFile;
    }

    public void importXMLToProject() {
        HIRuntime.getGui().startIndicatingServiceActivity(true);
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.1")); //$NON-NLS-1$
        importSuccessful = false;
        languages.clear();
        templates.clear();
        objects.clear();
        views.clear();
        corruptViews.clear();
        objectMap.clear();
        objectIDMap.clear();
        viewMap.clear();
        viewIDMap.clear();

        // execute export in separate thread
        new Thread() {

            @Override
            public void run() {
                try {
                    rootElement = xmlDocument.getDocumentElement();
                    _DBG("Root element: " + rootElement.getTagName(), 3);

                    importLanguages();

                    setVRACore4HdlbgTemplate();

                    // Objects must be staged before views to ensure that the hashmap of uuids/works is populated.
                    stageObjectImport();
                    importObjects();

                    stageViewImport();
                    importViews();

                    //evaluateBinaries();
                } catch (HIWebServiceException wse) {
                    Logger.getLogger(VRACore4HeidelbergImporter.class.getName()).log(Level.SEVERE, null, wse);
                    HIRuntime.getGui().reportError(wse, null);
                    abortWithError(wse.toString());
                    return;
                } catch (Exception e) {
                    Logger.getLogger(VRACore4HeidelbergImporter.class.getName()).log(Level.SEVERE, null, e);
                    abortWithError(e.toString());
                    return;
                }

                // import successful --> update GUI
                HIRuntime.getGui().stopIndicatingServiceActivity();
                importSuccessful = true;

                // inform user of success
                HIRuntime.getGui().displayInfoDialog(Messages.getString("PeTALImporter.43"),
                        Messages.getString("PeTALImporter.44") + "\n"
                        + Messages.getString("PeTALImporter.45"));

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

    /**
     * Each VRA work file is imported into an individual object. The work file
     * is scanned for related IDs to establish if images (i.e. views) are to be
     * added to the object.
     *
     * All VRA image files are imported as views. If they are associated with an
     * object, then they are added to that object. Otherwise, the view is
     * initialised with an object wrapper (containing no object metadata).
     *
     * Question: current object model assumes a view can only be in one object,
     * not in multiple objects. If a single view is referenced by different
     * objects in the course of a VRA import, it might be necessary to add the
     * view to a (singleton) group and then reference it from the various
     * objects.
     *
     */
    private void stageObjectImport() throws HIWebServiceException {
        // Compile list of ojbects.
        NodeList workElements = rootElement.getElementsByTagNameNS(VRA_4_HDLBG_XMLNS, "work");

        if (workElements == null) {
            return;
        }

        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.12"));

        for (int i = 0; i < workElements.getLength(); i++) {
            _DBG(workElements.item(i).getLocalName());

            Element objectElement = (Element) workElements.item(i);
            objects.add(objectElement);
            _DBG("added work to list of objects.");
        }

        int counter = 1;

        for (Element workElement : objects) {
            HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.24")
                    + " " + counter + " " + Messages.getString("PeTALImporter.25") + " "
                    + objects.size() + " " + Messages.getString("PeTALImporter.26"));

            String strUUID = workElement.getAttribute("id");
            if (strUUID.startsWith("w_")) {
                strUUID = strUUID.substring(2);
            }
            // incremental import --> check if object already exists in project
            HiObject newObjectWithUUID = null;
            try {
                // try to fetch HIObject from server using uuid
                HiBase base = HIRuntime.getManager().getBaseElement(strUUID);
                if ( base != null && base instanceof HiObject ) newObjectWithUUID = (HiObject)base;
            } catch(HIWebServiceException e) {
                // element not found
            }
            // create element if not found
            if ( newObjectWithUUID == null ) newObjectWithUUID = HIRuntime.getManager().createObject(strUUID);

            objectMap.put(newObjectWithUUID, workElement);
            objectIDMap.put(strUUID, newObjectWithUUID);
            _DBG("work id: " + workElement.getAttribute("id"));
            _DBG("UUID: " + strUUID, 2);

            counter++;
        }
    }

    /**
     * removed all calls to sort order. not sure if that breaks anything
     */
    private void stageViewImport()
            throws HIWebServiceException {

        // Compile list of views.
        NodeList imageElements = rootElement.getElementsByTagNameNS(VRA_4_HDLBG_XMLNS, "image");

        if (imageElements == null) {
            return;
        }

        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.13"));

        for (int i = 0; i < imageElements.getLength(); i++) {
            Element viewElement = (Element) imageElements.item(i);
            views.add(viewElement);
            _DBG("added image to list of views.");
        }

        int counter = 1;

        for (Element imageElement : views) {
            HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.24") + " "
                    + counter + ": " + Messages.getString("PeTALImporter.27"));

            // Need both the UUID with and without prefix for the code below.
            String strUUIDWithPrefix = imageElement.getAttribute("id");
            String strUUID = "";
            String workUUIDWithPrefix = "";
            if (strUUIDWithPrefix.startsWith("i_")) {
                strUUID = strUUIDWithPrefix.substring(2);
            }
            
            String strFilename = imageElement.getAttribute("href");
            _DBG("filename: " + strFilename);
            if(strFilename == "") strFilename = strUUIDWithPrefix + ".jpg";

            // Format to retrieve remote image:
            // http://kjc-sv016.kjc.uni-heidelberg.de:8600/images/service/download_uuid/i_fd3e7e65-543e-40b2-b5fd-6cca95d85327
            URL urlImage = null;
            //if (this.isTamboti()) {
            try {
                urlImage = new URL(IMAGERETRIEVALSERVICE + strUUIDWithPrefix);
            } catch (MalformedURLException ex) {
                Logger.getLogger(VRACore4HeidelbergImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            //}

            File fileImage = getImage(urlImage, strFilename);

            // Retrieve the object/work associated with this view/image.
            // Assuming an image can only be associated with one work. Assocation with multiple works
            // currently not supported by HyperImage object model.
            NodeList imageRelationSetElements = imageElement.getElementsByTagNameNS(VRA_4_HDLBG_XMLNS, "relationSet");
            if (imageRelationSetElements != null) {
                // Iterate over relationSet elements.
                for (int i = 0; i < imageRelationSetElements.getLength(); i++) {
                    Node nodeRelationSet = imageRelationSetElements.item(i);
                    if (nodeRelationSet instanceof Element && nodeRelationSet.getNodeName().equals("relationSet")) {
                        NodeList listChildrenOfRelationSet = nodeRelationSet.getChildNodes();
                        if (listChildrenOfRelationSet != null) {
                            // Iterate over relation elements
                            for (int j = 0; j < listChildrenOfRelationSet.getLength(); j++) {
                                Node nodeRelationSetChild = listChildrenOfRelationSet.item(j);
                                if (nodeRelationSetChild instanceof Element
                                        && nodeRelationSetChild.hasAttributes()
                                        && nodeRelationSetChild.getNodeName().equals("relation")) {
                                    Element elementRelation = (Element) nodeRelationSetChild;
                                    workUUIDWithPrefix = elementRelation.getAttribute("relids");
                                }
                            }
                        }
                    }
                }
            }

            // find relation element and get relid attribute to find related work
            HiObject workForThisImage = objectIDMap.get(workUUIDWithPrefix.substring(2));

            if (DEBUG && workForThisImage == null) {
                _DBG("Unable to retrieve work with uuid " + workUUIDWithPrefix + ".");
            }

            byte[] arrayImageData = null;
            //String strContentType = "";
            try {
                arrayImageData = Files.readAllBytes(fileImage.toPath());
                //strContentType = Files.probeContentType(fileImage.toPath());
            } catch (IOException ex) {
                Logger.getLogger(VRACore4HeidelbergImporter.class.getName()).log(Level.SEVERE, null, ex);
            }

            //_DBG("content type: " + strContentType, 2);
            HiView newViewWithUUID = null;
            
            //if (this.isTamboti()) {
            // hardcoding file type jpg for the time being.
            // incremental import --> check if object already exists in project
            try {
                // try to fetch HIView from server using uuid
                HiBase base = HIRuntime.getManager().getBaseElement(strUUID);
                if ( base != null && base instanceof HiView ) newViewWithUUID = (HiView)base;
            } catch(HIWebServiceException e) {
                // element not found
            }
            // create element if not found
            if ( newViewWithUUID == null ) newViewWithUUID = HIRuntime.getManager().createView(workForThisImage, strFilename, TAMBOTI_REPOS, arrayImageData, strUUID);
            //}

            viewMap.put(newViewWithUUID, imageElement);
            viewIDMap.put(strUUID, newViewWithUUID);

            arrayImageData = null;

            counter++;
        }
    }

    private void importViews() throws HIWebServiceException {
        int counter = 1;

        for (Element viewElement : viewMap.values()) {
            HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.36")
                    + " (" + counter + " " + Messages.getString("PeTALImporter.25") + " "
                    + viewMap.values().size() + ") " + Messages.getString("PeTALImporter.35"));

            String strUUID = viewElement.getAttribute("id").substring(2);
            HiView view = viewIDMap.get(strUUID);

            _DBG("viewElement: " + viewElement.getTagName());
            String key = viewElement.getTagName(); // == "image"
            String lang = "en"; //descElement.getAttribute("xml:lang");
            if (checkLangInProject(lang)) {
                HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(view.getMetadata(), lang);
                if (record != null) {
                    String content = getContentFromElements(key, viewElement);
                    if (content != null) {
                        _DBG("content: " + content);
                        MetadataHelper.setValue("HIBase", "title", getTitleFromImageElement(viewElement), record);
                        MetadataHelper.setValue("HIBase", "source", getSourceFromImageElement(viewElement), record);
                        MetadataHelper.setValue("HIBase", "comment", content, record);
                    }

                    HIRuntime.getManager().updateFlexMetadataRecord(record);
                }
            }
            counter++;
        }
    }
    
    private String getTitleFromImageElement(Element imageElement) {
        String strImageTitle = "";

        NodeList imageTitleSetElements = imageElement.getElementsByTagNameNS(VRA_4_HDLBG_XMLNS, "titleSet");
        if (imageTitleSetElements != null) {
            // Iterate over titleSet elements.
            for (int i = 0; i < imageTitleSetElements.getLength(); i++) {
                Node nodeTitleSet = imageTitleSetElements.item(i);
                if (nodeTitleSet instanceof Element && nodeTitleSet.getNodeName().equals("titleSet")) {
                    NodeList listChildrenOfTitleSet = nodeTitleSet.getChildNodes();
                    if (listChildrenOfTitleSet != null) {
                        // Iterate over title elements
                        for (int j = 0; j < listChildrenOfTitleSet.getLength(); j++) {
                            Node nodeTitleSetChild = listChildrenOfTitleSet.item(j);
                            if (nodeTitleSetChild instanceof Element
                                    && nodeTitleSetChild.getNodeName().equals("title")) {
                                strImageTitle = nodeTitleSetChild.getTextContent();
                            }
                        }
                    }
                }
            }
        }

        return strImageTitle;
    }
    
    /**
     * This is interpreted to be the rights holder.
     * 
     * @param imageElement
     * @return Text value of rightsSet.notes
     */
    private String getSourceFromImageElement(Element imageElement) {
        String strImageSource = "";

        NodeList imageRightsSetElements = imageElement.getElementsByTagNameNS(VRA_4_HDLBG_XMLNS, "rightsSet");
        if (imageRightsSetElements != null) {
            // Iterate over rightsSet elements.
            for (int i = 0; i < imageRightsSetElements.getLength(); i++) {
                Node nodeRightsSet = imageRightsSetElements.item(i);
                if (nodeRightsSet instanceof Element && nodeRightsSet.getNodeName().equals("rightsSet")) {
                    NodeList listChildrenOfRightsSet = nodeRightsSet.getChildNodes();
                    if (listChildrenOfRightsSet != null) {
                        // Iterate over rightsSet elements
                        for (int j = 0; j < listChildrenOfRightsSet.getLength(); j++) {
                            Node nodeRightsSetChild = listChildrenOfRightsSet.item(j);
                            if (nodeRightsSetChild instanceof Element
                                    && nodeRightsSetChild.getNodeName().equals("notes")) {
                                strImageSource = nodeRightsSetChild.getTextContent();
                            }
                        }
                    }
                }
            }
        }

        return strImageSource;
    }

    private void importObjects() throws HIWebServiceException {
        int counter = 1;
        for (Element objectElement : objectMap.values()) {
            HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.41")
                    + " (" + counter + " " + Messages.getString("PeTALImporter.25")
                    + " " + objectMap.values().size() + ") "
                    + Messages.getString("PeTALImporter.35"));

            String strUUID = objectElement.getAttribute("id").substring(2);
            HiObject object = objectIDMap.get(strUUID);

            _DBG("objectElement: " + objectElement.getTagName());
            String lang = "en"; //descElement.getAttribute("xml:lang");
            if (checkLangInProject(lang)) {
                HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(object.getMetadata(), lang);
                if (record != null) {
                    String content = getContentFromElements("work", objectElement);
                    if (content != null) {
                        _DBG("content: " + content);
                        // add VRA Core 4 specified fields
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "work", splitVRACore4Fields("work", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "agent", splitVRACore4Fields("agent", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "culturalContext", splitVRACore4Fields("culturalContext", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "date", splitVRACore4Fields("date", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "description", splitVRACore4Fields("description", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "location", splitVRACore4Fields("location", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "material", splitVRACore4Fields("material", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "measurements", splitVRACore4Fields("measurements", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "relation", splitVRACore4Fields("relation", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "rights", splitVRACore4Fields("rights", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "source", splitVRACore4Fields("source", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "stateEdition", splitVRACore4Fields("stateEdition", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "stylePeriod", splitVRACore4Fields("stylePeriod", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "subject", splitVRACore4Fields("subject", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "technique", splitVRACore4Fields("technique", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "textref", splitVRACore4Fields("textref", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "title", splitVRACore4Fields("title", content), record);
                        MetadataHelper.setValue(VRA_4_HDLBG_NSPREFIX, "worktype", splitVRACore4Fields("worktype", content), record);
                    }

                    HIRuntime.getManager().updateFlexMetadataRecord(record);
                }
            }
            counter++;
        }
    }
    
    private String splitVRACore4Fields(String key, String fields) {
        String content = "";
        
        for (String field : fields.split("\n") ) {
            if ( key.equalsIgnoreCase("work") ) {
                // for root element "work" only add known fields: refid and source 
                // (id is already the uuid of the HIObject itself and does not need to be added as a metadata field)
                if ( field.startsWith("{#}bold{#}work.refid{#}regular{#}") ) content += "{#}bold{#}" + field.substring(15)+"\n";
                if ( field.startsWith("{#}bold{#}work.source{#}regular{#}") ) content += "{#}bold{#}" + field.substring(15)+"\n";
            } else {
                if ( field.startsWith("{#}bold{#}work."+key+"Set."+key+".") ) content += "{#}bold{#}"+ field.substring(20 + (2*key.length()))+"\n";
                else if ( field.startsWith("{#}bold{#}work."+key+"Set."+key) ) content += "{#}regular{#}"+ field.substring(21 + 13 + (2*key.length()))+"\n";
                // support universal "notes" field
                if ( field.startsWith("{#}bold{#}work."+key+"Set.notes") ) content += "{#}bold{#}"+ field.substring(19 + key.length())+"\n";
            }            
        }
        
        
        return content;
    }

    private String getContentFromElements(String key, Element contentRootElement) {
        String contentString = "";
        String tagString = contentRootElement.getTagName();
        NodeList children = contentRootElement.getChildNodes();
        String nextTag = "";

        if (contentRootElement.hasAttributes()) {
            NamedNodeMap attributes = contentRootElement.getAttributes();
            for (int j = 0; j < attributes.getLength(); j++) {
                Node attribute = attributes.item(j);
                String tagAttrPath = contentRootElement.getTagName() + "." + attribute.getNodeName();
                String attrValue = attribute.getNodeValue();
                valueList.add(new String[]{tagAttrPath, attrValue});
            }
        }

        for (int i = 0; i < children.getLength(); i++) {
            // Discard any nodes that do not have text content. I'm not sure if this is working. ~HGK
            if (children.item(i) instanceof Element && children.item(i).getTextContent().trim().length() > 0) {
                nextTag = tagString + "." + children.item(i).getNodeName();
                processChildNode(children.item(i).getChildNodes(), nextTag);
                if (children.item(i).hasAttributes()) {
                    NamedNodeMap attributes = children.item(i).getAttributes();
                    for (int j = 0; j < attributes.getLength(); j++) {
                        Node attribute = attributes.item(j);
                        tagString += attribute.getNodeName() + ": " + attribute.getNodeValue() + "\n";
                    }
                }
            }
            _DBG("Ignoring node <" + children.item(i).getNodeName() + "> with value '" + children.item(i).getNodeValue() + "'.", 3);
        }

        Iterator<String[]> it = valueList.iterator();
        for (; it.hasNext();) {
            String[] val = it.next();
            contentString += "{#}bold{#}" + val[0] + "{#}regular{#}: " + val[1] + "\n";
        }

        valueList.clear();

        return contentString;
    }

    private void processChildNode(NodeList listOfNodes, String tags) {
        for (int i = 0; i < listOfNodes.getLength(); i++) {
            if (listOfNodes.item(i) instanceof Element) {
                String tagPath = tags + "." + listOfNodes.item(i).getNodeName();
                if (listOfNodes.item(i).hasAttributes()) {
                    NamedNodeMap attributes = listOfNodes.item(i).getAttributes();
                    for (int j = 0; j < attributes.getLength(); j++) {
                        Node attribute = attributes.item(j);
                        String tagAttrPath = tags + "." + listOfNodes.item(i).getNodeName() + "." + attribute.getNodeName();
                        String attrValue = attribute.getNodeValue();
                        valueList.add(new String[]{tagAttrPath, attrValue});
                    }
                }
                if (listOfNodes.item(i).getChildNodes().getLength() >= 1) {
                    processChildNode(listOfNodes.item(i).getChildNodes(), tagPath);
                }
            } else if (listOfNodes.item(i) instanceof Text && listOfNodes.getLength() == 1) {
                String value = listOfNodes.item(i).getNodeValue();
                valueList.add(new String[]{tags, value});
            }
        }
    }

    private void importLanguages() {
        languages.clear();

        // Just adding English for the time being.
        languages.add("en");
        String defLang = "en";

        // add languages to project
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.8"));

        for (String lang : languages) {
            if (!checkLangInProject(lang)) {
                try {
                    HIRuntime.getManager().addLanguageToProject(lang);
                } catch (HIWebServiceException ex) {
                    Logger.getLogger(VRACore4HeidelbergImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        try {
            // set default language
            HIRuntime.getManager().updateProjectDefaultLanguage(defLang);
        } catch (HIWebServiceException ex) {
            Logger.getLogger(VRACore4HeidelbergImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        // disabled "remove unused languages" for VRA Core 4 import since it's non destructive
        // it adds elements to a project but should not alter project structure
    }

    private void setVRACore4HdlbgTemplate() {
        HIRuntime.getGui().setMessage(Messages.getString("PeTALImporter.vraTemplate"));

        // check if template is in project
        boolean templateIsInProject = false;
        for (HiFlexMetadataTemplate projTemplate : HIRuntime.getManager().getProject().getTemplates()) {
            if (projTemplate.getNamespacePrefix().equalsIgnoreCase(XMLImporter.VRA_4_HDLBG_NSPREFIX)
                    || projTemplate.getNamespaceURI().equalsIgnoreCase(XMLImporter.VRA_4_HDLBG_XMLNS)) {
                templates.add(projTemplate);
                templateIsInProject = true;
            }
        }
        
        if (!templateIsInProject) {
            HiFlexMetadataTemplate hdlbgTemplate = MetadataHelper.getVRACore4HdlbgExtTemplateBlueprint();
            
            try {
                // add template
                if (HIRuntime.getManager().addTemplateToProject(hdlbgTemplate)) {
                    templates.add(hdlbgTemplate);
                }
            } catch (HIWebServiceException ex) {
                Logger.getLogger(VRACore4HeidelbergImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
