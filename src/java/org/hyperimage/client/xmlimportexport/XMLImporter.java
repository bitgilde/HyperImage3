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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiInscription;
import org.hyperimage.client.ws.HiLanguage;
import org.hyperimage.client.ws.HiLayer;
import org.hyperimage.client.ws.HiLightTable;
import org.hyperimage.client.ws.HiObject;
import org.hyperimage.client.ws.HiText;
import org.hyperimage.client.ws.HiView;
import org.hyperimage.client.ws.Hiurl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Heinz-Günter Kuper
 * @author Jens-Martin Loebel
 */
public class XMLImporter implements ErrorHandler {
    public enum XMLFormat {

        PETAL_2_0_XML, PETAL_3_0_XML, VRA_4_XML, UNRECOGNISED
    }

    protected static final boolean DEBUG = false;
    protected static final String DBGIND = ">>>>";
    
    private String strNamespace = "";
    protected static final String XHTML_XMLNS = "http://www.w3.org/1999/xhtml";
    protected static final String XLINK_XMLNS = "http://www.w3.org/1999/xlink";
    protected static final String RDF_XMLNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    protected static final String DC_1_1_XMLNS = "http://purl.org/dc/elements/1.1/";
    protected static final String HIBASE_XMLNS = "http://www.hyperimage.eu/PeTAL/HIView/1.0";
    protected static final String HIBASE_2_0_XMLNS = "http://hyperimage.ws/PeTAL/HIBase/2.0";
    protected static final String SVG_XMLNS = "http://www.w3.org/2000/svg";
    protected static final String PeTAL_2_0_XMLNS = "http://www.hyperimage.eu/PeTAL/2.0";
    protected static final String PeTAL_3_0_XMLNS = "http://hyperimage.ws/PeTAL/3.0";
    public static final String VRA_4_XMLNS = "http://www.vraweb.org/vracore4.htm";
    public static final String IMPORT_STAGING_DIR = "tmp_ImportStagingDirectory";
    private XMLFormat xmlFormat = null;
    Document xmlDocument = null;
    boolean isValidXML = true;
    File inputFile = null;
    //String version = null;
    boolean importSuccessful = false;
    String startRef = "";
    Element rootElement = null;
    ArrayList<String> languages = new ArrayList<>();
    ArrayList<String> corruptViews = new ArrayList<>();
    ArrayList<Element> texts = new ArrayList<>();
    ArrayList<Element> objects = new ArrayList<>();
    ArrayList<Element> views = new ArrayList<>();
    ArrayList<Element> inscriptions = new ArrayList<>();
    ArrayList<Element> layers = new ArrayList<>();
    ArrayList<Element> urls = new ArrayList<>();
    ArrayList<Element> litas = new ArrayList<>();
    ArrayList<Element> groups = new ArrayList<>();
    Element xmlImportGroup;
    ArrayList<HiFlexMetadataTemplate> templates = new ArrayList<>();
    HashMap<HiText, Element> textMap = new HashMap<>();
    HashMap<String, HiText> textIDMap = new HashMap<>();
    HashMap<Hiurl, Element> urlMap = new HashMap<>();
    HashMap<String, Hiurl> urlIDMap = new HashMap<>();
    HashMap<HiLightTable, Element> litaMap = new HashMap<>();
    HashMap<String, HiLightTable> litaIDMap = new HashMap<>();
    HashMap<HiGroup, Element> groupMap = new HashMap<>();
    HashMap<String, HiGroup> groupIDMap = new HashMap<>();
    HashMap<HiObject, Element> objectMap = new HashMap<>();
    HashMap<String, HiObject> objectIDMap = new HashMap<>();
    HashMap<HiView, Element> viewMap = new HashMap<>();
    HashMap<String, HiView> viewIDMap = new HashMap<>();
    HashMap<HiInscription, Element> inscriptionMap = new HashMap<>();
    HashMap<String, HiInscription> inscriptionIDMap = new HashMap<>();
    HashMap<HiLayer, Element> layerMap = new HashMap<>();
    HashMap<String, HiLayer> layerIDMap = new HashMap<>();

    public boolean wasImportSuccessful() {
        return importSuccessful;
    }
    
    public String serializeElement(Element element) {
        DOMImplementationLS lsImpl = (DOMImplementationLS)element.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
        LSSerializer serializer = lsImpl.createLSSerializer();
        serializer.getDomConfig().setParameter("xml-declaration", false);
        
        return serializer.writeToString(element);
    }

    protected void abortWithError(String errorMessage) {
        HIRuntime.getGui().stopIndicatingServiceActivity();
        if (errorMessage == null) {
            errorMessage = "";
        }

        // TODO display error message
        System.out.println("IMPORT ERROR!");
        System.out.println("--> " + errorMessage);
        if (errorMessage.length() == 0) {
            HIRuntime.getGui().displayInfoDialog(Messages.getString("PeTALImporter.2"), Messages.getString("PeTALImporter.3") + "\n\n"
                    + Messages.getString("PeTALImporter.4"));
        } else {
            HIRuntime.getGui().displayInfoDialog(Messages.getString("PeTALImporter.2"), Messages.getString("PeTALImporter.5") + "\n"
                    + Messages.getString("PeTALImporter.6") + " " + errorMessage + "\n\n"
                    + Messages.getString("PeTALImporter.7"));
        }
        HIRuntime.getGui().triggerProjectUpdate();
    }
    
    protected boolean checkLangInProject(String lang) {
        boolean langIsInProject = false;
        for (HiLanguage projLang : HIRuntime.getManager().getProject().getLanguages()) {
            if (projLang.getLanguageId().equalsIgnoreCase(lang)) {
                langIsInProject = true;
            }
        }
        return langIsInProject;
    }
    
    protected boolean checkTemplateInProject(String nsPrefix, String schema) {
        boolean templateIsInProject = false;
        for (HiFlexMetadataTemplate projTemplate : HIRuntime.getManager().getProject().getTemplates()) {
            if ( nsPrefix != null && projTemplate.getNamespacePrefix().equalsIgnoreCase(nsPrefix) ) templateIsInProject = true;
            if ( schema != null && projTemplate.getNamespaceURI().equalsIgnoreCase(schema) ) templateIsInProject = true;
        }
        
        return templateIsInProject;
    }

    public boolean loadAndValidateXMLFile(File inputFile) {
        this.inputFile = inputFile;
        importSuccessful = false;
        startRef = "";

        try {
            // try to read and parse file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            xmlDocument = builder.parse(inputFile);
            isValidXML = true;

            strNamespace = xmlDocument.getDocumentElement().getAttribute("xmlns");

            if (DEBUG) {
                System.out.println(DBGIND + " Namespace: " + strNamespace);
            }

            switch (strNamespace) {
                case PeTAL_3_0_XMLNS:
                    xmlFormat = XMLFormat.PETAL_3_0_XML;
                    // validate PeTAL 3.0 against schema
                    final Schema schema = sf.newSchema(getClass().getResource("/resources/schema/PeTAL_schema_3.0.xsd"));
                    factory.setSchema(schema);
                    builder = factory.newDocumentBuilder();
                    builder.setErrorHandler(this);
                    xmlDocument = builder.parse(inputFile);
                    if ( !isValidXML ) return false;
                    break;
                case PeTAL_2_0_XMLNS:
                    xmlFormat = XMLFormat.PETAL_2_0_XML;
                    break;
                //case VRA_4_XMLNS:
                //    xmlFormat = XMLFormat.VRA_4_XML;
                //    break;
                default:
                        xmlFormat = XMLFormat.UNRECOGNISED;
                    break;
            }

            return true;
        } catch (SAXParseException spe) {
            xmlDocument = null;
            spe.printStackTrace();
            return false;
        } catch (SAXException sxe) {
            xmlDocument = null;
            sxe.printStackTrace();
            return false;
        } catch (ParserConfigurationException pce) {
            xmlDocument = null;
            pce.printStackTrace();
            return false;
        } catch (IOException ioe) {
            xmlDocument = null;
            ioe.printStackTrace();
            return false;
        }
    }

    public boolean isPeTAL2_0() {
        return (xmlFormat != null && xmlFormat.equals(XMLFormat.PETAL_2_0_XML));
    }

    public boolean isPeTAL3_0() {
        return (xmlFormat != null && xmlFormat.equals(XMLFormat.PETAL_3_0_XML));
    }

    public boolean isVRA4() {
        return (xmlFormat != null && xmlFormat.equals(XMLFormat.VRA_4_XML));
    }

    /**
     * Returns a local or remote image as a file. -- HGK
     *
     * @param imageElement The view element as extracted from the XML file.
     * @return The image.
     */
    protected File getImage(Element imageElement) {
        File retFile = null;

        String strSrc = imageElement.getAttribute("src");
        _DBG("Retrieving from " + strSrc);
        if (strSrc.startsWith("img")) { // if first three letters == "img" then local
            String pathToFile = inputFile.getParent() + "/" + imageElement.getAttribute("src");
            if (File.separator.compareTo("\\") == 0) {
                pathToFile = pathToFile.replaceAll("/", "\\" + File.separator);
            } else {
                pathToFile = pathToFile.replaceAll("/", File.separator);
            }
            retFile = new File(pathToFile);
        } else if (strSrc.startsWith("http")) { // remote file
            String fileName = getFileNameFromURL(strSrc);
            _DBG("Filename: " + fileName);
            //retFile = new File(fileName);
            retFile = new File("tmp_import/" + fileName);

            try {
                URL urlImage = new URL(strSrc);

                InputStream is = urlImage.openStream();
                OutputStream os = new FileOutputStream(retFile);

                byte[] b = new byte[2048];
                int length;

                while ((length = is.read(b)) != -1) {
                    os.write(b, 0, length);
                }

                is.close();
                os.close();
            } catch (IOException ioe) {
                Logger.getLogger(XMLImporter.class.getName()).log(Level.SEVERE, "Error retrieving remote image.", ioe);
            }
        }
        return retFile;
    }
    
    /**
     * Returns remote image via URL and string name.
     * 
     * @param urlImage
     * @param strName
     * @return 
     */
    protected File getImage(URL urlImage, String strName) {
        File fileImage = instantiateFileInImportStagingDirectory(strName);

        // Check if image file has already been downloaded during another import.
        // Should be safe since file names are based on UUIDs.
        if (!Files.exists(fileImage.toPath())) {
            // Otherwise, retrieve remote file.
            try {
                OutputStream os;
                try (InputStream is = urlImage.openStream()) {
                    os = new FileOutputStream(fileImage);
                    byte[] b = new byte[2048];
                    int length;
                    while ((length = is.read(b)) != -1) {
                        os.write(b, 0, length);
                    }
                }
                os.close();
            } catch (IOException ioe) {
                Logger.getLogger(XMLImporter.class.getName()).log(Level.SEVERE, "Error retrieving remote image.", ioe);
            }
        }

        return fileImage;
    }
    
    
    private File instantiateFileInImportStagingDirectory(String strFilename) {
        File dirImport = new File(IMPORT_STAGING_DIR);

        //if (!dirImport.exists()) {
        if (!Files.exists(dirImport.toPath())) {
            _DBG("Creating dir " + IMPORT_STAGING_DIR + " for import …", 3);

            try {
                dirImport.mkdir();
            } catch (SecurityException se) {
                Logger.getLogger(XMLImporter.class.getName()).log(Level.SEVERE, "Error creating dir for import.", se);
            }
        }
        
        _DBG("Saving file to "+ IMPORT_STAGING_DIR + File.separator + strFilename);
        return new File(IMPORT_STAGING_DIR + File.separator + strFilename);
    }

    /**
     * Return the last part of a URL, assumed to be the filename. -- HGK
     *
     * @param strURL URL containing the filename after the final forward slash.
     * @return Filename.
     */
    private String getFileNameFromURL(String strURL) {
        return strURL.substring(strURL.lastIndexOf('/') + 1, strURL.length());
    }

    public Document getXMLDocument() {
        return xmlDocument;
    }
    
    ///////////////////// UTILITY FUNCTIONS /////////////////////
    protected void _DBG(String strDebugMessage) {
        _DBG(strDebugMessage, 1);
    }
    
    protected void _DBG(String strDebugMessage, int nLevel) {
        String strIndents = "";
        for( int i = 0; i < nLevel; i++ ){
            strIndents += DBGIND;
        }
        
        if( DEBUG ) System.out.println(strIndents + " " + strDebugMessage);
    }
    
    
    
    // ------------------------------------------------------------------------
    

    public void warning(SAXParseException exception) throws SAXException {
        if (DEBUG) {
            System.out.println("XML warning: " + exception.getLocalizedMessage());
        }
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        if (DEBUG) {
            System.out.println("XML error: " + exception.getLocalizedMessage());
        }
        isValidXML = false;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        if (DEBUG) {
            System.out.println("XML fatal error: " + exception.getLocalizedMessage());
        }
        isValidXML = false;
    }

}
