/*
 * Copyright 2010 Humboldt-Universität zu Berlin. All rights reserved.
 *
 * Copyright 2014, 2015 bitGilde IT Solutions UG (haftungsbeschränkt). All rights reserved.
 * http://bitgilde.de/
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
 *
 * For further information on HyperImage visit http://hyperimage.ws/
 */

/**
 *
 * Class: PRrest
 * @author Jens-Martin Loebel (loebel@bitgilde.de)
 * based on @author Ulf Schoeneberg
 * @version 2010.08 + fixes by JML 2014 / 2015
 *
 * NOTE BY JML: this implementation by Ulf S. is deprecated and broken, only hotfixes have been applied.
 * The HyperImage-Prometheus-connection as created by Ulf S. is not production ready
 * This should not be used in a production environment, data loss or corruption may occur
 * Implementation is not secure, tokens may leak
 * 
 * RESTful communication with PRometheus
 *
 * PRometheus methods for getting information and images
 * implemented in REST
 *
 */

package org.hyperimage.service.util;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.hyperimage.service.model.HIUser;
import org.hyperimage.service.model.PRCollection;
import org.hyperimage.service.model.PRImage;
import org.scribe.http.Request;
import org.scribe.http.Request.Verb;
import org.scribe.http.Response;
import org.scribe.oauth.Scribe;
import org.scribe.oauth.Token;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class PRrest {
    public ServerPreferences prefs;
    String base_url;
//    private static Document dom;
    public Token accessToken;
    public Scribe scribe;

//    private static Object resource(String string) {
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
    public PRrest (ServerPreferences serverPrefs, Scribe sc, Token aT)  {
        prefs = serverPrefs;
        base_url = prefs.getPrometheusAPIPref() + "/api/v1";
        scribe = sc;
        accessToken = aT;
    }

   
    

     public String about() {
        String ver = null;
        XPathExpression expr = null;
        Object result = null;

        Request request = new Request(Verb.GET, base_url + "/xml/about");        
//        Main.scribe.signRequest(request, accessToken);
        Response response = request.send();
        String xmlResponse = response.getBody();

        XPath xpath = XPathFactory.newInstance().newXPath();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //dbf.setNamespaceAware(true);
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            // Logger.getLogger(PRrest.class.getName()).log(Level.SEVERE, null, ex);
        }
        InputSource is = new InputSource(new StringReader(xmlResponse));
        try {
            Document dom = db.parse(is);
            try {
                expr = xpath.compile("/pandora/version/text()");
                result = expr.evaluate(dom, XPathConstants.NODESET);
            } catch (XPathExpressionException ex) {
                Logger.getLogger(PRrest.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SAXException ex) {
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        NodeList nodes = (NodeList) result;
     
        return nodes.item(0).getNodeValue();
    }
     
    
     public HIUser getPRUserInfo() {
        XPathExpression expr = null;
        Object result = null;
        HIUser user = new HIUser();
        user.setId(0);
        user.setPassword(accessToken.getRawString());

        Request request = new Request(Verb.GET, base_url + "/xml/account/show");
        scribe.signRequest(request, accessToken);
        Response response = request.send();
        String xmlResponse = response.getBody();

        XPath xpath = XPathFactory.newInstance().newXPath();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //dbf.setNamespaceAware(true);
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            // Logger.getLogger(PRrest.class.getName()).log(Level.SEVERE, null, ex);
        }
        InputSource is = new InputSource(new StringReader(xmlResponse));
        try {
            Document dom = db.parse(is);
            try {
                expr = xpath.compile("/account/firstname/text()");
                result = expr.evaluate(dom, XPathConstants.NODESET);
                user.setFirstName(((NodeList)result).item(0).getNodeValue());

                expr = xpath.compile("/account/lastname/text()");
                result = expr.evaluate(dom, XPathConstants.NODESET);
                user.setLastName(((NodeList)result).item(0).getNodeValue());

                expr = xpath.compile("/account/email/text()");
                result = expr.evaluate(dom, XPathConstants.NODESET);
                user.setEmail(((NodeList)result).item(0).getNodeValue());

                expr = xpath.compile("/account/id/text()");
                result = expr.evaluate(dom, XPathConstants.NODESET);
                user.setUserName("PR_"+((NodeList)result).item(0).getNodeValue());
                
            } catch (XPathExpressionException ex) {
                Logger.getLogger(PRrest.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SAXException | IOException ex) {
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        NodeList nodes = (NodeList) result;
     
        return user;
    }
     
    public List<PRCollection> getCollections() {
        List<PRCollection> collections = new ArrayList();
        
        Request request = new Request(Verb.GET, base_url + "/xml/collection/own/meta_image");
        scribe.signRequest(request, accessToken);
        Response response = request.send();
        String xmlResponse = response.getBody();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            // Logger.getLogger(PRrest.class.getName()).log(Level.SEVERE, null, ex);
        }
        InputSource is = new InputSource(new StringReader(xmlResponse));
        try {
            Document dom = db.parse(is);
            
            NodeList xmlCollections = dom.getElementsByTagName("collection");
            for ( int i=0; i < xmlCollections.getLength(); i++ ) {
                Element xmlCollection = (Element)xmlCollections.item(i);
                String id = xmlCollection.getElementsByTagName("id").item(0).getTextContent();
                String title = xmlCollection.getElementsByTagName("title").item(0).getTextContent();
                collections.add(new PRCollection(id, title));
            }
            
        } catch (SAXException | IOException ex) {
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return collections;
    }

    public List<PRImage> getCollectionImageInfo(String id) {
        ArrayList<PRImage> images = new ArrayList<PRImage>();
        
        Request request = new Request(Verb.GET, base_url + "/xml/collection/images/" + id);

        scribe.signRequest(request, accessToken);
        Response response = request.send();
        String xmlResponse = response.getBody();

        XPath xpath = XPathFactory.newInstance().newXPath();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
          //  Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Document dom = db.parse(new InputSource(new StringReader(xmlResponse)));
            
            NodeList xmlImages = dom.getElementsByTagName("image");
            for ( int i=0; i < xmlImages.getLength(); i++) {
                Element xmlImage = (Element)xmlImages.item(i);
                PRImage image = new PRImage();
                HashMap<String, String> metadata = image.getMetadata();
                
                NodeList xmlData = xmlImage.getChildNodes();
                for (int dataID=0; dataID < xmlData.getLength(); dataID++)
                    if ( xmlData.item(dataID).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE )
                        metadata.put(((Element)xmlData.item(dataID)).getTagName(), xmlData.item(dataID).getTextContent());
                image.setPID(metadata.get("pid"));
                
                images.add(image);
            }
            
        } catch (SAXException | IOException ex) {
         //   Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return images;
    }
    public List<PRImage> getCollectionImageInfo(PRCollection collection) {
        return getCollectionImageInfo(collection.getID());
    }
    
    public byte[] getImageData(String pid, String size) {
        Request request = null;
        if (size.contentEquals("small")) {
            request = new Request(Verb.GET, base_url + "/blob/image/small/" + pid);
        }
        if (size.contentEquals("medium")) {
            request = new Request(Verb.GET, base_url + "/blob/image/medium/" + pid);
        }
        if (size.contentEquals("large")) {
            request = new Request(Verb.GET, base_url + "/blob/image/large/" + pid);
        }
        scribe.signRequest(request, accessToken);       
        Response response = request.send();
        
        return response.getBodyPic();
    }
    
    
/*
    public List<PRImage> getHierarchyLevel(String urn) {
        XPathExpression pid_expr = null;
        XPathExpression title_expr = null;
        Object pid_result  = null;
        Object title_result = null;
        PRImage im = null;
        List<PRImage> imageList = new ArrayList();

        Request request = new Request(Verb.GET, base_url + "/xml/collection/images/" + urn);

//        Main.scribe.signRequest(request, accessToken);
        Response response = request.send();
        String xmlResponse = response.getBody();

        XPath xpath = XPathFactory.newInstance().newXPath();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
          //  Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Document dom = db.parse(new InputSource(new StringReader(xmlResponse)));
            try {
                pid_expr    = xpath.compile("/images/image/pid/text()");
                pid_result  = pid_expr.evaluate(dom, XPathConstants.NODESET);
                title_expr   = xpath.compile("/images/image/title/text()");
                title_result = title_expr.evaluate(dom, XPathConstants.NODESET);
            } catch (XPathExpressionException ex) {
                Logger.getLogger(PRrest.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SAXException | IOException ex) {
         //   Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        NodeList pid_nodes  = (NodeList) pid_result;
        NodeList title_nodes = (NodeList) title_result;
        for (int i = 0; i < pid_nodes.getLength(); i++) {
            im = new PRImage();
            try {
                im.setUrn( pid_nodes.item(i).getNodeValue() );
                im.setDisplayName( title_nodes.item(i).getNodeValue() );
                imageList.add(im);
            } catch (NullPointerException ex) {
                imageList.remove(im);
            }
        }
        return imageList;
    }


/*


    

    public static String setStatus(String urn) {
        String ver = null;

        Request request = new Request(Verb.PUT, base_url + "/xml/collection/meta_image_status/" + urn);
        Main.scribe.signRequest(request, accessToken);
        Response response = request.send();
        xmlResponse = response.getBody();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        InputSource is = new InputSource(new StringReader(xmlResponse));
        try {
            dom = db.parse(is);
        } catch (SAXException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        dom.getDocumentElement().normalize();
        NodeList nodeLst = dom.getElementsByTagName("collection");
        Node fstNode = nodeLst.item(0);
        if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

            Element fstElmnt = (Element) fstNode;
            NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("meta-image");
            Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
            NodeList fstNm = fstNmElmnt.getChildNodes();
            ver = ((Node) fstNm.item(0)).getNodeValue();
        }
        return ver;
    }





      public static Hashtable getMetadataRecord(String urn) {
        Hashtable h = new Hashtable();

        Request request = new Request(Verb.GET, base_url + "/xml/image/show/" + urn);
        Main.scribe.signRequest(request, accessToken);
        Response response = request.send();
        xmlResponse = response.getBody();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        InputSource is = new InputSource(new StringReader(xmlResponse));
        try {
            dom = db.parse(is);
        } catch (SAXException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        dom.getDocumentElement().normalize();
        NodeList nodeLst = dom.getElementsByTagName("image");
        Node fstNode = nodeLst.item(0);
        Element fstElmnt = (Element) fstNode;
        NodeList fstNmElmntLst = fstElmnt.getChildNodes();
        for (int s = 0; s < fstNmElmntLst.getLength(); s++) {
            Node node = fstNmElmntLst.item(s);
            Node node_a = node.getChildNodes().item(1);
            String key = node.getNodeName();
            String val = node.getTextContent();
            if (!val.isEmpty()) {
                h.put(key, val);
            }
        }
        return h;
    }



     public static boolean addMetadataRecord(String urn, Hashtable kV) {

        Request request;
         
        Enumeration e = kV.keys();
        Document bla;
        while( e.hasMoreElements() ) {

            request = new Request(Verb.POST, base_url + "/xml/image/add_meta_image_comment/");
            request.addBodyParameter("id", urn);
            request.addBodyParameter("collection", Main.urn);
            String key = (String) e.nextElement();
            String val = (String) kV.get(key);
            String[] keyLast = key.split("\\.");
            request.addBodyParameter("f[]", keyLast[1]);
            request.addBodyParameter("v[]", val);
            Main.scribe.signRequest(request, accessToken);
            Response response = request.send();
            xmlResponse = response.getBody();

            ByteArrayInputStream stream = new ByteArrayInputStream(xmlResponse.getBytes());
            DocumentBuilder builder = null;
            try {
                builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(PRrest.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                bla = builder.parse(stream);
            } catch (SAXException ex) {
                System.out.println(" Error: Metadata could not be POSTed to Prometheus ");
                //Logger.getLogger(PRrest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PRrest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return true;
    }
*/
}
