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

/*
 * Copyright 2015 bitGilde IT Solutions UG (haftungsbeschränkt)
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

package org.hyperimage.client;

import java.awt.Color;
import java.awt.image.renderable.RenderableImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.PlanarImage;
import javax.swing.JOptionPane;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.model.RelativePolygon;
import org.hyperimage.client.model.RelativePolygon.HiPolygonTypes;
import org.hyperimage.client.util.ImageHelper;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.GroupTypes;
import org.hyperimage.client.ws.HIEditor;
import org.hyperimage.client.ws.HIEditorService;
import org.hyperimage.client.ws.HILogin;
import org.hyperimage.client.ws.HILoginService;
import org.hyperimage.client.ws.HIMaintenanceModeException_Exception;
import org.hyperimage.client.ws.HIParameterException_Exception;
import org.hyperimage.client.ws.HIServiceException_Exception;
import org.hyperimage.client.ws.HiBase;
import org.hyperimage.client.ws.HiBaseTypes;
import org.hyperimage.client.ws.HiFlexMetadataName;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiFlexMetadataSet;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiImageSizes;
import org.hyperimage.client.ws.HiInscription;
import org.hyperimage.client.ws.HiLayer;
import org.hyperimage.client.ws.HiLightTable;
import org.hyperimage.client.ws.HiObject;
import org.hyperimage.client.ws.HiObjectContent;
import org.hyperimage.client.ws.HiPreference;
import org.hyperimage.client.ws.HiProject;
import org.hyperimage.client.ws.HiQuickInfo;
import org.hyperimage.client.ws.HiRepository;
import org.hyperimage.client.ws.HiRoles;
import org.hyperimage.client.ws.HiText;
import org.hyperimage.client.ws.HiUser;
import org.hyperimage.client.ws.HiView;
import org.hyperimage.client.ws.Hiurl;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Jens-Martin Loebel
 */
public class HIWebServiceManager {

	private static final String minimumVersionID = "3.0.3";
	
	private HILoginService loginService;
	private HIEditorService editorService;
	private HILogin loginPort;
	private HIEditor editor;
	private W3CEndpointReference editorRef;
	
	private HiGroup importGroup = null;
	private HiGroup trashGroup = null;
	private HiProject project = null;
	private HiUser curUser = null;
        private List<HiGroup> projectTags = null;
	private Vector<Color> projectColors;
	private Vector<String> projectPolygons;
	
	private HashMap<Long, PlanarImage> thumbnailCache;
	
	private String username, password;
	private HiRoles curRole = null;
	
	public static enum WSStates {OFFLINE, ONLINE, IN_PROJECT, RECONNECT}
	
	private WSStates state = WSStates.OFFLINE;
	
	private String serverURL;
	
	
	public HIWebServiceManager(String serverURL) throws MalformedURLException {
		HIRuntime.setManager(this);
		this.thumbnailCache = new HashMap<Long, PlanarImage>();
		this.projectColors = new Vector<Color>();
		this.projectPolygons = new Vector<String>();
		if ( !serverURL.endsWith("/") ) serverURL = serverURL+"/";
		this.serverURL = serverURL;

		loginService = new HILoginService(new URL(serverURL+"HILoginService?WSDL"), new QName("http://ws.service.hyperimage.org/", "HILoginService"));
		loginPort = loginService.getHILoginPort();
		String versionID = loginPort.getVersionID();
		System.out.println("Server Version: "+versionID);
		if ( versionID.compareTo(minimumVersionID) < 0 ) {
			JOptionPane.showMessageDialog(null, "FATAL ERROR: Server HI-WebService Version outdated!\n\nReported: "+versionID+"\nNeeded: "+minimumVersionID);
			System.exit(1);
		}

		editorService =  new HIEditorService(new URL(serverURL+"HIEditorService?WSDL"), new QName("http://ws.service.hyperimage.org/", "HIEditorService"));
	}
	
	
	public String getServerURL() {
		return this.serverURL;
	}
	
        public String getEndpointSession() {
            if ( state == WSStates.ONLINE || state == WSStates.IN_PROJECT ) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db;
                try {
                    db = dbf.newDocumentBuilder();
                    Document doc = db.parse(new ByteArrayInputStream(editorRef.toString().getBytes("UTF-8")));
                    return doc.getElementsByTagName("jaxws:objectId").item(0).getTextContent();
                } catch ( ParserConfigurationException | SAXException | IOException ex) {
                    Logger.getLogger(HIWebServiceManager.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }

            }
            return null;
        }
	public boolean login(String username, String password) throws HIWebServiceException {
		this.username = username;
		this.password = password;
		
		editor = null;
		curRole = null;
		thumbnailCache.clear();
		
		try {
			editorRef = loginPort.authenticate(username, password);
			if ( editorRef != null ) editor = editorService.getPort(editorRef, HIEditor.class);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
		if ( editor != null ) {
			curUser = getUserByUserName(username); // get current user
			state = WSStates.ONLINE; // set manager state
			return true;
		}
		
		return false;		
	}

	public boolean loginPR(String user, String token, String token_secret, String token_verifier) throws HIWebServiceException {
		this.username = "PR_"+user;
		this.password = "";
		
		editor = null;
		curRole = null;
		thumbnailCache.clear();
		
		try {
			editorRef = loginPort.authenticatePR(token, token_secret, token_verifier, user);
                        
			if ( editorRef != null ) editor = editorService.getPort(editorRef, HIEditor.class);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
		if ( editor != null ) {
			curUser = getUserByUserName(username); // get current user
			state = WSStates.ONLINE; // set manager state
			return true;
		}
		
		return false;		
	}
        
        
	public HiUser getCurrentUser() {
		return curUser;
	}
	
	public boolean reconnect() throws HIWebServiceException {
		try {
                        editorRef = loginPort.authenticate(username, password);
			editor = editorService.getPort(editorRef, HIEditor.class);
		} catch (HIParameterException_Exception e) {
			e.printStackTrace();
		} catch (HIMaintenanceModeException_Exception e) {
			// this happens when the service is in maintenance mode, only sysop may login at that time
			throw new HIWebServiceException(e);
		}
		if ( editor != null ) {
			if ( setProject(project) ) {
				state = WSStates.IN_PROJECT;
				return true;
			}
			else {
				state = WSStates.OFFLINE;
				curUser = null;
				editor = null;
			}
		}
		curUser = null;
		state = WSStates.OFFLINE;
		return false;
	}
	
	public boolean logout() {
		importGroup = null;
		trashGroup = null;
		project = null;
		curRole = null;
		curUser = null;
		thumbnailCache.clear();
		state = WSStates.OFFLINE;
		
		if ( editor == null )
			return false;
		
		return loginPort.logout(editorRef);
		
	}
	
	// ------------------------------------------

	public String getPPGLocation() {
		return loginPort.getPPGLocation();
	}
	
	// ------------------------------------------
	
	public HiProject getProject() {
		return project;
	}
        
	public boolean setProject(HiProject project) throws HIWebServiceException {
		boolean success;
		try {
			success = editor.setProject(project);
                        			
			if ( success ) {
				importGroup = editor.getImportGroup();
				trashGroup = editor.getTrashGroup();
				curRole = editor.getCurrentRole();
				thumbnailCache.clear();
				this.project = project;
				state = WSStates.IN_PROJECT;
				
				projectColors.removeAllElements();
				projectPolygons.removeAllElements();
                                projectTags = null;
				
				// find project color pref
				String projColors = MetadataHelper.findPreferenceValue(project, "colors");
				if ( projColors == null )
					// create preference, if it doesn´t exist
					projColors = createPreference("colors", "").getValue();

				if ( projColors.length() > 0 ) {
					// parse color string
					try {
						for ( String color : projColors.split(":") )
							projectColors.addElement(new Color(
									Integer.parseInt(color.split(",")[0]),
									Integer.parseInt(color.split(",")[1]),
									Integer.parseInt(color.split(",")[2])
							));
					} catch (NumberFormatException nfe) {
						System.out.println("Error parsing color pref!");
						MetadataHelper.setPreferenceValue(project, "colors", "");
						projectColors.removeAllElements();
						// DEBUG replace with standard method for setting colors - TODO: persist change
					}
				}
								
				// find project polygon pref
				String projPolygons = MetadataHelper.findPreferenceValue(project, "polygons");
				if ( projPolygons == null )
					// create preference, if it doesn´t exist
					projPolygons = createPreference("polygons", "").getValue();

				if ( projPolygons.length() > 0 ) {
					// parse polygon string
					try {
						for ( String polygon : projPolygons.split(":") )
							projectPolygons.addElement(polygon);
					} catch (NumberFormatException nfe) {
						System.out.println("Error parsing polygon pref!");
						MetadataHelper.setPreferenceValue(project, "polygons", "");
						projectPolygons.removeAllElements();
						// DEBUG replace with standard method for setting polygons - TODO: persist change
					}
				}

                                // find project template sort order pref
				String tempSortOrder = MetadataHelper.findPreferenceValue(project, "templateSortOrder");
				if ( tempSortOrder == null ) {
                                    // put HIInternal template last
                                    HiFlexMetadataTemplate hiInternalTemplate = null;
                                    for (HiFlexMetadataTemplate template : this.project.getTemplates())
                                        if (template.getNamespacePrefix().compareTo("HIInternal") == 0) //$NON-NLS-1$
                                            hiInternalTemplate = template;

                                    if (hiInternalTemplate != null) {
                                        this.project.getTemplates().remove(hiInternalTemplate);
                                        this.project.getTemplates().add(hiInternalTemplate);
                                    }

                                    // build initial sort order
                                    String initSortOrder = "";
                                    for ( int i=0 ; i < this.project.getTemplates().size(); i++ )
                                        initSortOrder = initSortOrder + ","+this.project.getTemplates().get(i).getId();
                                    // remove leading ","
                                    if (initSortOrder.length() > 0)
                                        initSortOrder = initSortOrder.substring(1);

                                    // create preference, if it doesn´t exist
                                    tempSortOrder = createPreference("templateSortOrder", initSortOrder).getValue();
                                }
				if ( tempSortOrder.length() > 0 )
					// sort templates
                                    sortTemplates(tempSortOrder);


				
				// sort project template sets
				for ( HiFlexMetadataTemplate template : this.project.getTemplates() )
					sortTemplate(template);

			}

		} catch (HIServiceException_Exception e) {
			e.printStackTrace();
			return false;
		} catch (HIMaintenanceModeException_Exception e) {
			throw new HIWebServiceException(e);
		}
			
		return success;
	}

    private void sortTemplates(String sortOrder) {
        if ( this.project == null || sortOrder == null || sortOrder.length() == 0 ) return;

        long setID;
        int index = 0;
        int setIndex;

        // parse sort order string (don´t trust user input)
        for (String setIDString : sortOrder.split(",")) { //$NON-NLS-1$
            try {
                setID = Long.parseLong(setIDString);
                // find content belonging to the parsed id
                setIndex = -1;

                for (int i = 0; i < this.project.getTemplates().size(); i++)
                    if (this.project.getTemplates().get(i).getId() == setID)
                        setIndex = i;

                // if content was found, sort to new index
                if (setIndex >= 0) {
                    if (setIndex != index) {
                        HiFlexMetadataTemplate template = project.getTemplates().get(setIndex);
                        this.project.getTemplates().remove(setIndex);
                        this.project.getTemplates().add(index, template);
                        index = index + 1;
                    } else
                        index = index + 1;
                }
            } catch (NumberFormatException e) {
                // user messed with sort order string format, no problem
            }
        }
    }

	
	private void sortTemplate(HiFlexMetadataTemplate template) {
		if ( template == null || template.getSortOrder() == null ) return;
		
		long setID;
		int index = 0;
		int setIndex;

		// parse sort order string (don´t trust user input)
		for ( String setIDString : template.getSortOrder().split(",") ) { //$NON-NLS-1$
			try {
				setID = Long.parseLong(setIDString);
				// find content belonging to the parsed id
				setIndex = -1;

				for ( int i=0; i<template.getEntries().size(); i++ )
					if ( template.getEntries().get(i).getId() == setID )
						setIndex = i;
				// if content was found, sort to new index
				if ( setIndex >= 0 ) if ( setIndex != index ) {
					HiFlexMetadataSet set = template.getEntries().get(setIndex);
					template.getEntries().remove(setIndex);
					template.getEntries().add(index, set);
					index = index+1;
				} else index = index + 1;
			} catch (NumberFormatException e) {}; // user messed with sort order string format, no problem
		}	
	}

	
	public Vector<Color> getProjectColors() {
		return projectColors;
	}

	public Vector<String> getProjectPolygons() {
		return projectPolygons;
	}

	public HiRoles getCurrentRole() {
		return curRole;
	}
	
	/**
	 * @deprecated
	 * NOTE: This method is deprecated. All Web Service functions have now been wrapped in @see HIWebServiceManager
	 * functions ( omitting admin* and sysop* method prefixes ) and should be used instead of calling the 
	 * web service directly.
	 * All SOAP and service errors will be wrapped in a single @see HIServiceException for easy parsing.
	 * @return direct link to the HI web service interface
	 */
	public HIEditor getWSPort() {
		return editor;
	}
	
	public boolean isConnected() {
		if ( editor != null ) return true;
		return false;
	}

	public HiGroup getImportGroup() {
		return importGroup;
	}

	public HiGroup getTrashGroup() {
		return trashGroup;
	}

	public WSStates getState() {
		return state;
	}
	
	public void setReconnect() {
		this.state = WSStates.RECONNECT;
	}

	public static byte[] getBytesFromFile(File file) {
		InputStream is;
		byte[] bytes = null;

		try {
			is = new FileInputStream(file);
			if ( file.length() > Integer.MAX_VALUE ) throw new IOException("File too big!");
			
			bytes = new byte[(int) file.length()];

			int numRead = is.read(bytes);

			if (numRead < bytes.length) throw new IOException("Error reading file!");

			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return bytes;
	}

	
	/* --------------------------------------------------------------
	 *  Wrapper functions for web service
	 * --------------------------------------------------------------
	 */

        public void refreshProject() throws HIWebServiceException {
            // reload project from server
            try {
                this.project = editor.getProject();
            } catch (Exception se) {
			throw new HIWebServiceException(se);
            }

            // sort project templates
            String tempSortOrder = MetadataHelper.findPreferenceValue(project, "templateSortOrder");
            if ( tempSortOrder != null ) sortTemplates(tempSortOrder);

            // sort project template sets
            for (HiFlexMetadataTemplate template : this.project.getTemplates())
                sortTemplate(template);
        }

	// ----------------------------------
	
	public String getVersionID() throws HIWebServiceException {
		try {
			return editor.getVersionID();
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------

	private String serializeProjectColorsPreference() {
		String colors = "";
		String colorString;
		
		for ( Color color : projectColors ) {
			colorString = color.getRed()+","+color.getGreen()+","+color.getBlue();
			if ( colors.length() == 0 )
				colors = colorString;
			else colors = colors+":"+colorString;
		}
		
		return colors;
	}

	private String serializeProjectPolygonsPreference() {
		String polygons = "";
		
		for ( String polygonString : projectPolygons ) {
			
			if ( polygons.length() == 0 )
				polygons = polygonString;
			else polygons = polygons+":"+polygonString;
		}
		
		return polygons;
	}
	
	// ----------------------------------
	
	public void addProjectColor(Color color) throws HIWebServiceException {
		// check if color already in project colors
		for ( Color projColor : projectColors )
			if ( projColor.getRed() == color.getRed() &&
				 projColor.getGreen() == color.getGreen() &&
				 projColor.getBlue() == color.getBlue() )
				return;
		
		projectColors.add(color);
		
		// serialize and update preferences on server
		HiPreference colorPref = MetadataHelper.findPreference(project, "colors");
		colorPref.setValue(serializeProjectColorsPreference());
		// sync changes with server
		updatePreference(colorPref);
	}

	// ----------------------------------
	
	public void removeProjectColor(Color color) throws HIWebServiceException {
		// find in project colors
		Color foundColor = null;
		for ( Color projColor : projectColors )
			if ( projColor.getRed() == color.getRed() &&
				 projColor.getGreen() == color.getGreen() &&
				 projColor.getBlue() == color.getBlue() )
				foundColor = projColor;

		if ( foundColor == null ) return;
		
		projectColors.remove(foundColor);
		
		// serialize and update preferences on server
		HiPreference colorPref = MetadataHelper.findPreference(project, "colors");
		colorPref.setValue(serializeProjectColorsPreference());
		// sync changes with server
		updatePreference(colorPref);
		
	}

	// ----------------------------------
	
	public void updateProjectColors() throws HIWebServiceException {
		// serialize and update preferences on server
		HiPreference colorPref = MetadataHelper.findPreference(project, "colors");
		colorPref.setValue(serializeProjectColorsPreference());
		// sync changes with server
		updatePreference(colorPref);
	}

	// ----------------------------------
	
	public void addProjectPolygon(String polygonModel) throws HIWebServiceException {
		// turn polygon into "user created polygon"
		if ( polygonModel.length() > 0 )
			polygonModel = RelativePolygon.getModelType(HiPolygonTypes.HI_USERDESIGN)+ polygonModel.substring(1);
		// check if polygon already in project polygons
		for ( String polygon : projectPolygons )
			if ( polygon.compareTo(polygonModel)  == 0)
				return;
		
		projectPolygons.add(polygonModel);
		
		// serialize and update preferences on server
		HiPreference polygonPref = MetadataHelper.findPreference(project, "polygons");
		polygonPref.setValue(serializeProjectPolygonsPreference());
		// sync changes with server
		updatePreference(polygonPref);	
	}

	// ----------------------------------
	
	public void removeProjectPolygon(String polygonModel) throws HIWebServiceException {
		// turn polygon into "user created polygon"
		if ( polygonModel.length() > 0 )
			polygonModel = RelativePolygon.getModelType(HiPolygonTypes.HI_USERDESIGN)+ polygonModel.substring(1);
		// check if polygon in project polygons
		String foundPolygon = null;
		for ( String polygon : projectPolygons )
			if ( polygon.compareTo(polygonModel)  == 0)
				foundPolygon = polygon;
		if ( foundPolygon == null ) return;
		
		projectPolygons.remove(foundPolygon);
		
		// serialize and update preferences on server
		HiPreference polygonPref = MetadataHelper.findPreference(project, "polygons");
		polygonPref.setValue(serializeProjectPolygonsPreference());
		// sync changes with server
		updatePreference(polygonPref);	
		
	}

	
	/* ------------------------------
	 * Bitstream (image) functions
	 * ------------------------------
	 */
	
	public byte[] getImageAsBitstream(HiView view, HiImageSizes size) throws HIWebServiceException {
		try {
			return editor.getImage(view.getId(), size);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}		
	}
	
	public PlanarImage getImage(HiView view, HiImageSizes size) throws HIWebServiceException {
		return getImage(view.getId(), size, true);
	}
		
	public PlanarImage getImage(HiQuickInfo info, HiImageSizes size) throws HIWebServiceException {
		PlanarImage image = null;
		
		if ( info.getContentType() == HiBaseTypes.HI_OBJECT )
			image = getImage(info.getRelatedID(), size, true);
		else if ( info.getContentType() == HiBaseTypes.HI_VIEW )
			image = getImage(info.getBaseID(), size, true);
		else if ( info.getContentType() == HiBaseTypes.HI_LAYER )
			image = getImage(info.getBaseID(), size, false);
		
		return image;
	}

	public PlanarImage getImage(long viewID, HiImageSizes size, boolean cacheImage) throws HIWebServiceException {
		PlanarImage image = null;


		// TODO handle low memory - manage cache
		if ( size == HiImageSizes.HI_THUMBNAIL ) {
			// check if thumbnail is in cache
			image = thumbnailCache.get(viewID);

		} 
		// if not, load from webservice
		if ( image == null ) {
			try {
				image = ImageHelper.convertByteArrayToPlanarImage(
						editor.getImage(viewID, size));

				// cache thumbnail if content was view, don�t cache objects as the default view might change
				if ( size == HiImageSizes.HI_THUMBNAIL && cacheImage )
					thumbnailCache.put(viewID, image);

			} catch (Exception se) {
				throw new HIWebServiceException(se);
			}
		}


		return image;
	}
	
	// ----------------------------------
		
	public RenderableImage getRenderableImage(long viewID) throws HIWebServiceException {
		RenderableImage image = null;


		try {
			image = ImageHelper.convertByteArrayToRenderableImage(
					editor.getImage(viewID, HiImageSizes.HI_FULL));
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}


		return image;
	}
	
	// ----------------------------------

	public HiPreference createPreference(String key, String value) throws HIWebServiceException {
		try {
			HiPreference pref = editor.createPreference(key, value);
			if ( this.project != null )
				project.getPreferences().add(pref);
			return pref;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------

	public boolean updatePreference(long prefID, String value) throws HIWebServiceException {
		if ( project == null ) return false;
		
		try {
			HiPreference projPref = null;
			for ( HiPreference pref : project.getPreferences() )
				if ( pref.getId() == prefID ) projPref = pref;
			if ( projPref == null ) return false;
			
			boolean prefUpdated = editor.updatePreference(prefID, value);
			if ( prefUpdated ) {
				projPref.setValue(value);
                                if ( projPref.getKey().compareTo("templateSortOrder") == 0 )
                                    sortTemplates(value);
                        }
			return prefUpdated;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean updatePreference(HiPreference pref) throws HIWebServiceException {
		return updatePreference(pref.getId(), pref.getValue());
	}

	// ----------------------------------

	public boolean deletePreference(long prefID) throws HIWebServiceException {
		if ( project == null ) return false;
		
		try {
			HiPreference projPref = null;
			for ( HiPreference pref : project.getPreferences() )
				if ( pref.getId() == prefID ) projPref = pref;
			if ( projPref == null ) return false;
			
			boolean prefDeleted = editor.deletePreference(prefID);
			if ( prefDeleted )
				project.getPreferences().remove(projPref);
			return prefDeleted;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean deletePreference(HiPreference pref) throws HIWebServiceException {
		return deletePreference(pref.getId());
	}

	// ----------------------------------
	
	public boolean addLanguageToProject(String language) throws HIWebServiceException {
		try {
			boolean languageAdded = editor.adminAddLanguageToProject(language);
			if ( languageAdded )
				this.project = editor.getProject();
			return languageAdded;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------
	
	public boolean removeLanguageFromProject(String language) throws HIWebServiceException {
		try {
			boolean languageRemoved = editor.adminRemoveLanguageFromProject(language);
			if ( languageRemoved )
				this.project = editor.getProject();
			return languageRemoved;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------
	
	public List<HiRepository> getRepositories() throws HIWebServiceException {
		List<HiRepository> repositories = null;
		
		try {
			repositories = editor.getRepositories();
			
			return repositories;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}


	// ----------------------------------

	public HiRepository createRepository(HiRepository newRepository) throws HIWebServiceException {

        HiRepository userRepos = null;
		try {
            userRepos = editor.adminCreateRepository(newRepository);
            
            return userRepos;
 		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------

	public boolean deleteRepository(HiRepository repository) throws HIWebServiceException {

		try {
            return editor.adminDeleteRepository(repository);
 		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------

	public boolean addTemplateToProject(HiFlexMetadataTemplate template) throws HIWebServiceException {
		try {
			boolean templateAdded = editor.adminAddTemplateToProject(template);
			// refresh templates
			if ( templateAdded )
				refreshProject();
			return templateAdded;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------

	public boolean updateTemplate(HiFlexMetadataTemplate template, String templateURI, String templateURL) throws HIWebServiceException {
		try {
			boolean templateUpdated = editor.adminUpdateTemplate(template.getId(), templateURI, templateURL);
			// refresh templates
			if ( templateUpdated )
				refreshProject();
			return templateUpdated;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------

	public boolean updateTemplateSortOrder(HiFlexMetadataTemplate template, String sortOrder) throws HIWebServiceException {
		try {
			boolean templateUpdated = editor.adminUpdateTemplateSortOrder(template.getId(), sortOrder);
			// refresh templates
			if ( templateUpdated )
				refreshProject();
			return templateUpdated;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

        // ----------------------------------

	public boolean removeTemplateFromProject(HiFlexMetadataTemplate template) throws HIWebServiceException {
                if ( template == null ) return false;

                try {
			boolean templateRemoved = editor.adminRemoveTemplateFromProject(template.getId());
			// refresh templates
			if ( templateRemoved )
				refreshProject();
			return templateRemoved;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

        // ----------------------------------

	public HiFlexMetadataSet addSetToTemplate(HiFlexMetadataTemplate template, String tagName, boolean isRichText) throws HIWebServiceException {
                if ( template == null ) return null;
                if ( tagName == null || tagName.length() == 0) return null;

                try {
                        HiFlexMetadataSet newSet = editor.adminAddSetToTemplate(template.getId(), tagName, isRichText);
			// refresh templates
			if ( newSet != null ) {
				refreshProject();
                                return newSet;
                        }
			return null;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

        // ----------------------------------

        public boolean removeSetFromTemplate(HiFlexMetadataTemplate template, HiFlexMetadataSet set) throws HIWebServiceException {
                if ( template == null ) return false;
                if ( set == null ) return false;

                try {
			boolean setRemoved = editor.adminRemoveSetFromTemplate(template.getId(), set.getId());
			// refresh templates
			if ( setRemoved )
				refreshProject();
			return setRemoved;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

        // ----------------------------------

	public HiFlexMetadataName createSetDisplayName(HiFlexMetadataSet set, String lang, String displayName) throws HIWebServiceException {
                if ( set == null ) return null;
                if ( lang == null || lang.length() == 0) return null;

                try {
                        HiFlexMetadataName newName = editor.adminCreateSetDisplayName(set.getId(), lang, displayName);
			// refresh templates
			if ( newName != null ) {
				refreshProject();
                                return newName;
                        }
			return null;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}


        // ----------------------------------

	public void updateSetDisplayName(HiFlexMetadataSet set, String lang, String displayName) throws HIWebServiceException {
                if ( set == null ) return;
                if ( lang == null || lang.length() == 0) return;

                try {
                        editor.adminUpdateSetDisplayName(set.getId(), lang, displayName);
			// refresh templates
                        refreshProject();

                } catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------

	public boolean addUserToProject(long userID, HiRoles role) throws HIWebServiceException {
		try {
			return editor.adminAddUserToProject(userID, role);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean addUserToProject(HiUser user, HiRoles role) throws HIWebServiceException {
		return addUserToProject(user.getId(), role);
	}

	// ----------------------------------

	public boolean addUserToProject(long userID, long projectID, HiRoles role) throws HIWebServiceException {
		if ( state == WSStates.IN_PROJECT )
			if ( project.getId() == projectID ) return addUserToProject(userID, role);
		try {
            return editor.sysopAddUserToProject(userID, projectID, role);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean addUserToProject(HiUser user, HiProject project, HiRoles role) throws HIWebServiceException {
		if ( state == WSStates.IN_PROJECT )
			if ( this.project.getId() == project.getId() ) return addUserToProject(user.getId(), role);
		return addUserToProject(user.getId(), project.getId(), role);
	}	
	
	// ----------------------------------

	public HiUser getUserByID(long userID) throws HIWebServiceException {
		try {
			return editor.adminGetUserByID(userID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	
	// ----------------------------------

	public HiUser getUserByUserName(String userName) throws HIWebServiceException {
		try {
            return editor.adminGetUserByUserName(userName);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	
	// ----------------------------------

	public boolean removeUserFromProject(long userID) throws HIWebServiceException {
		try {
			boolean userRemoved = editor.adminRemoveUserFromProject(userID);
			if ( userRemoved ) if ( userID == curUser.getId() ) {
				// user removed himself from the current project
				state = WSStates.ONLINE;
				importGroup = null;
				trashGroup = null;
				project = null;
				curRole = null;
				thumbnailCache.clear();
			}
            return userRemoved;

		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean removeUserFromProject(HiUser user) throws HIWebServiceException {
		return removeUserFromProject(user.getId());
	}

	// ----------------------------------

	public boolean removeUserFromProject(long userID, long projectID) throws HIWebServiceException {
		if ( state == WSStates.IN_PROJECT )
			if ( project.getId() == projectID )
				return removeUserFromProject(userID);
		try {
            return editor.sysopRemoveUserFromProject(userID, projectID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean removeUserFromProject(HiUser user, HiProject project) throws HIWebServiceException {
		return removeUserFromProject(user.getId(), project.getId());
	}
		
	// ----------------------------------

	public boolean setProjectRole(long userID, HiRoles role) throws HIWebServiceException {
		try {
			boolean roleSet = editor.adminSetProjectRole(userID, role);
			if ( roleSet ) if ( userID == curUser.getId() )
				curRole = role;
            return roleSet;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean setProjectRole(HiUser user, HiRoles role) throws HIWebServiceException {
		return setProjectRole(user.getId(), role);
	}	
	
	// ----------------------------------

	public boolean removeFromGroup(long baseID, long groupID) throws HIWebServiceException {
		try {
            return editor.removeFromGroup(baseID, groupID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean removeFromGroup(HiBase base, HiGroup group) throws HIWebServiceException {
		return removeFromGroup(base.getId(), group.getId());
	}

	// ----------------------------------

	public boolean copyToGroup(long baseID, long groupID) throws HIWebServiceException {
		try {
            return editor.copyToGroup(baseID, groupID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean copyToGroup(HiBase base, HiGroup group) throws HIWebServiceException {
		return copyToGroup(base.getId(), group.getId());
	}
	public boolean addToGroup(long baseID, long groupID) throws HIWebServiceException {
		return copyToGroup(baseID, groupID);
	}
	public boolean addToGroup(HiBase base, HiGroup group) throws HIWebServiceException {
		return copyToGroup(base.getId(), group.getId());
	}
	
	// ----------------------------------

	public HiFlexMetadataRecord createFlexMetadataRecord(long baseID, String language) throws HIWebServiceException {
		try {
            return editor.createFlexMetadataRecord(baseID, language);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public HiFlexMetadataRecord createFlexMetadataRecord(HiBase base, String language) throws HIWebServiceException {
		return createFlexMetadataRecord(base.getId(), language);
	}	
	
	// ----------------------------------

	public HiGroup createGroup() throws HIWebServiceException {
            return createGroup(null);
        }
	public HiGroup createGroup(String uuid) throws HIWebServiceException {
            try {
                return editor.createGroup(uuid);
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
	}
        
	// ----------------------------------

	public List<HiGroup> getTags() throws HIWebServiceException {
            if ( projectTags == null ) {
		try {
			projectTags = editor.getTagGroups();
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
            }
            return projectTags;
	}
        
	// ----------------------------------

	public ArrayList<HiGroup> getTagsForBaseElement(long baseID) throws HIWebServiceException {
            ArrayList<HiGroup> baseTags = new ArrayList<>();

            if ( projectTags == null ) getTags(); // refresh tag list if necessary
            
            List<Long> baseTagIDs;
            try {
                baseTagIDs = editor.getTagIDsForBase(baseID);
                
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
            
            for ( Long tagID : baseTagIDs ) {
                for ( HiGroup tag : projectTags ) if ( tag.getId() == tagID ) baseTags.add(tag);
            }
            
            return baseTags;
	}
                
	// ----------------------------------

	public ArrayList<HiGroup> getTagsForBaseElement(HiBase base) throws HIWebServiceException {
            return getTagsForBaseElement(base.getId());
        }
        
        // ----------------------------------

        public long getTagCountForElement(long baseID) throws HIWebServiceException {

            if ( projectTags == null ) getTags(); // refresh tag list if necessary
            
            List<Long> baseTagIDs;
            try {
                return editor.getTagCountForBase(baseID);
                
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
            
	}

        // ----------------------------------

	public long getTagCountForElement(HiBase base) throws HIWebServiceException {
            return getTagCountForElement(base.getId());
        }

        // ----------------------------------


	public HiGroup createTagGroup() throws HIWebServiceException {
            return createTagGroup(null);
        }
	public HiGroup createTagGroup(String uuid) throws HIWebServiceException {
            try {
                HiGroup newTag = editor.createTagGroup(uuid);
                if ( newTag != null && projectTags != null ) projectTags.add(newTag);
                return newTag;
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
	}
	
	// ----------------------------------

	public HiInscription createInscription(long objectID) throws HIWebServiceException {
            return createInscription(objectID, null);
        }
	public HiInscription createInscription(long objectID, String uuid) throws HIWebServiceException {
            try {
                return editor.createInscription(objectID, uuid);
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
	}
	public HiInscription createInscription(HiObject object, String uuid) throws HIWebServiceException {
            return createInscription(object.getId(), uuid);
        }
	public HiInscription createInscription(HiObject object) throws HIWebServiceException {
		return createInscription(object.getId());
	}
	
	// ----------------------------------

	public HiLayer createLayer(long viewID, int red, int green, int blue, float opacity) throws HIWebServiceException {
            return createLayer(viewID, red, green, blue, opacity, null);
        }
	public HiLayer createLayer(long viewID, int red, int green, int blue, float opacity, String uuid) throws HIWebServiceException {
            try {
                return editor.createLayer(viewID, red, green, blue, opacity, uuid);
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
	}
	public HiLayer createLayer(HiView view, int red, int green, int blue, float opacity, String uuid) throws HIWebServiceException {
		return createLayer(view.getId(), red, green, blue, opacity, uuid);        
        }
	public HiLayer createLayer(HiView view, int red, int green, int blue, float opacity) throws HIWebServiceException {
		return createLayer(view.getId(), red, green, blue, opacity);
	}	

	// ----------------------------------

	public boolean setLayerLink(long layerID, long linkID) throws HIWebServiceException {
		try {
            return editor.setLayerLink(layerID, linkID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean setLayerLink(HiLayer layer, HiBase link) throws HIWebServiceException {
		return setLayerLink(layer.getId(), link.getId());
	}	


	// ----------------------------------

	public boolean removeLayerLink(long layerID) throws HIWebServiceException {
		try {
            return editor.removeLayerLink(layerID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean removeLayerLink(HiLayer layer) throws HIWebServiceException {
		return removeLayerLink(layer.getId());
	}	

	// ----------------------------------

	public HiObject createObject() throws HIWebServiceException {
            return createObject(null);
        }
	public HiObject createObject(String uuid) throws HIWebServiceException {
		try {
            return editor.createObject(uuid);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	
	// ----------------------------------

	public HiText createText() throws HIWebServiceException {
            return createText(null);
        }
	public HiText createText(String uuid) throws HIWebServiceException {
            try {
                return editor.createText(uuid);
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
	}
	

	// ----------------------------------

	public HiLightTable createLightTable(String title, String xml) throws HIWebServiceException {
            return createLightTable(title, xml, null);
        }
	public HiLightTable createLightTable(String title, String xml, String uuid) throws HIWebServiceException {
            try {
                return editor.createLightTable(title, xml, uuid);
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
	}

	// ----------------------------------

	public boolean updateLightTable(HiLightTable lightTable) throws HIWebServiceException {
		try {
            return editor.updateLightTable(lightTable);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------

	public Hiurl createURL(String url, String title, String lastAccess) throws HIWebServiceException {
            return createURL(url, title, lastAccess, null);
        }
	public Hiurl createURL(String url, String title, String lastAccess, String uuid) throws HIWebServiceException {
		try {
            return editor.createURL(url, title, lastAccess, uuid);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	
	// ----------------------------------

	public HiView createView(long objectID, String filename, String repositoryIDString, byte[] data) throws HIWebServiceException {
            return createView(objectID, filename, repositoryIDString, data, null);
        }
	public HiView createView(long objectID, String filename, String repositoryIDString, byte[] data, String uuid) throws HIWebServiceException {
            try {
                return editor.createView(objectID, filename, repositoryIDString, data, uuid);
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
	}
	public HiView createView(HiObject object, String filename, String repositoryIDString, byte[] data, String uuid) throws HIWebServiceException {
            return createView(object.getId(), filename, repositoryIDString, data, uuid);
	}	
	public HiView createView(HiObject object, String filename, String repositoryIDString, byte[] data) throws HIWebServiceException {
            return createView(object.getId(), filename, repositoryIDString, data);
	}	
	public HiView createView(HiObject object, File file) throws HIWebServiceException {
            return createView(object, file, null);
        }
	public HiView createView(HiObject object, File file, String uuid) throws HIWebServiceException {
            // load file into memory
            byte[] data = getBytesFromFile(file);
            return createView(object.getId(), file.getName(), "[HIEditor 3.0 - Direct Import]", data, uuid);
	}	
	
	// ----------------------------------

	public boolean updateContentOwner(long objectID, long contentID) throws HIWebServiceException {
		try {
			return editor.updateContentOwner(objectID, contentID);
			
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean updateContentOwner(HiObject object, HiObjectContent content) throws HIWebServiceException {
		if ( updateContentOwner(object.getId(), content.getId()) ) {
			object.getViews().add(content);
			return true;
		}
		return false;
	}
	
	// ----------------------------------

	public boolean deleteFromProject(long baseID) throws HIWebServiceException {
		try {
			boolean success = editor.deleteFromProject(baseID);
			if ( success && project.getStartObjectInfo() != null && project.getStartObjectInfo().getBaseID() == baseID )
				project.setStartObjectInfo(null);

            return success;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean deleteFromProject(HiBase base) throws HIWebServiceException {
		return deleteFromProject(base.getId());
	}	
	
	// ----------------------------------

	public boolean deleteGroup(long groupID) throws HIWebServiceException {
            try {
                return editor.deleteGroup(groupID);
            } catch (Exception se) {
		throw new HIWebServiceException(se);
            }
	}
	public boolean deleteGroup(HiGroup group) throws HIWebServiceException {
            boolean success = deleteGroup(group.getId());
            if ( success &&  group.getType() == GroupTypes.HIGROUP_TAG && projectTags != null ) {
                projectTags.remove(group);
            }
            return success;
	}	
	
	// ----------------------------------

	public HiBase getBaseElement(String uuid) throws HIWebServiceException {
            try {
                return editor.getBaseElementByUUID(uuid);
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
	}
	public HiBase getBaseElement(long baseID) throws HIWebServiceException {
		try {
            return editor.getBaseElement(baseID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public HiBase getBaseElement(HiBase base) throws HIWebServiceException {
		return getBaseElement(base.getId());
	}	
	
	
	// ----------------------------------

	public List<HiText> getProjectTextElements() throws HIWebServiceException {
            try {
                return editor.getProjectTextElements();
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
        }

	// ----------------------------------

	public List<HiLightTable> getProjectLightTableElements() throws HIWebServiceException {
            try {
                return editor.getProjectLightTableElements();
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
        }        
        
	// ----------------------------------

	public HiQuickInfo getBaseQuickInfo(long baseID) throws HIWebServiceException {
            try {
                return editor.getBaseQuickInfo(baseID);
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
	}
	public HiQuickInfo getBaseQuickInfo(HiBase base) throws HIWebServiceException {
            return getBaseQuickInfo(base.getId());
	}
        public HiQuickInfo getBaseQuickInfo(String uuid) throws HIWebServiceException {
            try {
                return editor.getBaseQuickInfoByUUID(uuid);
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
        }
	public HiQuickInfo getContentInfo(HiQuickInfo content) throws HIWebServiceException {
            return getBaseQuickInfo(content.getBaseID());
	}
	
	// ----------------------------------

	public List<HiQuickInfo> getGroupContentQuickInfo(long groupID) throws HIWebServiceException {
		try {
            return editor.getGroupContentQuickInfo(groupID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public List<HiQuickInfo> getGroupContents(HiGroup group) throws HIWebServiceException {
		return getGroupContentQuickInfo(group.getId());
	}
	
	// ----------------------------------

	public List<HiGroup> getGroups() throws HIWebServiceException {
		try {
			return sortGroups(editor.getGroups());
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	
	private List<HiGroup> sortGroups(List<HiGroup> groups) {
		long contentID;
		int index = 0;
		int contentIndex;

		// parse sort order string (don't trust user input)
		for ( String contentIDString : MetadataHelper.findPreferenceValue(getProject(), "groupSortOrder").split(",") ) {
			try {
				contentID = Long.parseLong(contentIDString);
				// find content belonging to the parsed id
				contentIndex = -1;

				for ( int i=0; i<groups.size(); i++ )
					if ( ((HiGroup) groups.get(i)).getId() == contentID )
						contentIndex = i;
				// if content was found, sort to new index
				if ( contentIndex >= 0 ) if ( contentIndex != index ) {
					HiGroup group = (HiGroup) groups.get(contentIndex);
					groups.remove(contentIndex);
					groups.add(index, group);
					index = index+1;
				} else index = index + 1;
			} catch (NumberFormatException e) {}; // user messed with sort order string format, no problem
		}
		
		return groups;
	}
	
	// ----------------------------------
	
	public List<HiProject> getProjects() throws HIWebServiceException {
		try {
            return editor.getProjects();
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	
	// ----------------------------------

	public boolean moveToGroup(long baseID, long fromGroupID, long toGroupID) throws HIWebServiceException {
		try {
            return editor.moveToGroup(baseID, fromGroupID, toGroupID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean moveToGroup(HiBase base, HiGroup fromGroup, HiGroup toGroup) throws HIWebServiceException {
		return moveToGroup(base.getId(), fromGroup.getId(), toGroup.getId());
	}	
	
	// ----------------------------------

	public boolean moveToTrash(long baseID) throws HIWebServiceException {
		try {
            return editor.moveToTrash(baseID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean moveToTrash(HiBase base) throws HIWebServiceException {
		return moveToTrash(base.getId());
	}	
	
	// ----------------------------------

	public boolean removeLayer(long layerID) throws HIWebServiceException {
		try {
            return editor.removeLayer(layerID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean removeLayer(HiLayer layer) throws HIWebServiceException {
		return removeLayer(layer.getId());
	}
	
    // ----------------------------------
        
        
    public boolean setDefaultView(long objectID, long contentID) throws HIWebServiceException {
        try {
            return editor.setDefaultView(objectID, contentID);
        } catch (Exception se) {
            throw new HIWebServiceException(se);
        }
    }

    public boolean setDefaultView(HiObject object, HiObjectContent content) throws HIWebServiceException {
        return setDefaultView(object.getId(), content.getId());
    }
	
	
	// ----------------------------------

	public boolean updateFlexMetadataRecord(HiFlexMetadataRecord record) throws HIWebServiceException {
		try {
            return editor.updateFlexMetadataRecord(record);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	
	// ----------------------------------

	public List<HiFlexMetadataRecord> getFlexMetadataRecords(long baseID) throws HIWebServiceException {
		try {
            return editor.getFlexMetadataRecords(baseID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public List<HiFlexMetadataRecord> getFlexMetadataRecords(HiBase base) throws HIWebServiceException {
		return getFlexMetadataRecords(base.getId());
	}
	
	// ----------------------------------

	public boolean updateFlexMetadataRecords(List<HiFlexMetadataRecord> records) throws HIWebServiceException {
		try {
            return editor.updateFlexMetadataRecords(records);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	
	// ----------------------------------

	public boolean updateGroupProperties(long groupID, boolean visible) throws HIWebServiceException {
		try {
            return editor.updateGroupProperties(groupID, visible);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean updateGroupProperties(HiGroup group, boolean visible) throws HIWebServiceException {
		return updateGroupProperties(group.getId(), visible);
	}
	
	// ----------------------------------

	public void updateGroupSortOrder(long groupID, String sortOrder) throws HIWebServiceException {
		try {
            editor.updateGroupSortOrder(groupID, sortOrder);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public void updateGroupSortOrder(HiGroup group, String sortOrder) throws HIWebServiceException {
		updateGroupSortOrder(group.getId(), sortOrder);
		group.setSortOrder(sortOrder);
	}
	
	// ----------------------------------

	public boolean updateLayerProperties(long layerID, int red, int green, int blue, float opacity, String polygons) throws HIWebServiceException {
		try {
            return editor.updateLayerProperties(layerID, red, green, blue, opacity, polygons);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean updateLayerProperties(HiLayer layer, int red, int green, int blue, float opacity, String polygons) throws HIWebServiceException {
		return updateLayerProperties(layer.getId(), red, green, blue, opacity, polygons);
	}

	
	// ----------------------------------

	public void updateViewSortOrder(long viewID, String sortOrder) throws HIWebServiceException {
		try {
            editor.updateViewSortOrder(viewID, sortOrder);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public void updateViewSortOrder(HiView view, String sortOrder) throws HIWebServiceException {
		updateViewSortOrder(view.getId(), sortOrder);
		view.setSortOrder(sortOrder);
	}

	// ----------------------------------

	public void updateObjectSortOrder(long objectID, String sortOrder) throws HIWebServiceException {
		try {
            editor.updateObjectSortOrder(objectID, sortOrder);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public void updateObjectSortOrder(HiObject object, String sortOrder) throws HIWebServiceException {
		updateObjectSortOrder(object.getId(), sortOrder);
		object.setSortOrder(sortOrder);
	}

	// ----------------------------------

	public boolean updateProject(String languageID, String title) throws HIWebServiceException {
		try {
            boolean updated = editor.updateProjectMetadata(languageID, title);
            if ( updated ) this.project = editor.getProject();

            return updated;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	
	// ----------------------------------

	public boolean updateProjectStartElement(long baseID) throws HIWebServiceException {
		try {
            boolean success = editor.updateProjectStartElement(baseID);
			if ( success ) this.project = editor.getProject();

            return success;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean updateProjectStartElement(HiBase base) throws HIWebServiceException {
		return updateProjectStartElement(base.getId());
	}

	// ----------------------------------

	public boolean updateProjectDefaultLanguage(String language) throws HIWebServiceException {
		try {
			boolean success = editor.updateProjectDefaultLanguage(language);
			if ( success ) this.project = editor.getProject();

            return success;
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------

	public boolean updateURL(Hiurl url) throws HIWebServiceException {
		try {
            return editor.updateURL(url);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	
	// ----------------------------------


	public void removeDefaultView(long objectID) throws HIWebServiceException {
		try {
            editor.removeDefaultView(objectID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public void removeDefaultView(HiObject object) throws HIWebServiceException {
		removeDefaultView(object.getId());
	}
	
	// ----------------------------------

	public boolean updateUser(long userID, String firstName, String lastName, String email) throws HIWebServiceException {
		try {
            return editor.updateUser(userID, firstName, lastName, email);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean updateUser(HiUser user, String firstName, String lastName, String email) throws HIWebServiceException {
		return updateUser(user.getId(), firstName, lastName, email);
	}
	public boolean updateUser(String firstName, String lastName, String email) throws HIWebServiceException {
		return updateUser(curUser.getId(), firstName, lastName, email);
	}
	public boolean updateUser(HiUser user) throws HIWebServiceException {
		return updateUser(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
	}		

	
	// ----------------------------------

	public boolean updateUserPassword(long userID, String password) throws HIWebServiceException {
		try {
            return editor.updateUserPassword(userID, password);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean updateUserPassword(HiUser user, String password) throws HIWebServiceException {
		return updateUserPassword(user.getId(), password);
	}	
	public boolean updatePassword(String password) throws HIWebServiceException {
		return updateUserPassword(curUser.getId(), password);
	}	
	
	// ----------------------------------

	public HiProject createProject(String adminUsername, String defaultLanguage) throws HIWebServiceException {
		try {
            return editor.sysopCreateProject(adminUsername, defaultLanguage);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public HiProject createProject(HiUser adminUser, String defaultLanguage) throws HIWebServiceException {
		return createProject(adminUser.getUserName(), defaultLanguage);
	}
	
	// ----------------------------------

	public void updateProjectQuota(long projectID, long quota) throws HIWebServiceException {
            try {
                editor.sysopUpdateProjectQuota(projectID, quota);
            } catch (Exception se) {
                throw new HIWebServiceException(se);
            }
	}
	public void updateProjectQuota(HiProject project, long quota) throws HIWebServiceException {
            updateProjectQuota(project.getId(), quota);
        }
        
	// ----------------------------------

        public HiUser createUser(String firstName, String lastName, String userName, String password, String email) throws HIWebServiceException {
		try {
            return editor.sysopCreateUser(firstName, lastName, userName, password, email);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------

	public HiRoles getRoleInProject(long userID, long projectID) throws HIWebServiceException {
		try {
            return editor.adminGetRoleInProject(userID, projectID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public HiRoles getRoleInProject(HiUser user, HiProject project) throws HIWebServiceException {
		return getRoleInProject(user.getId(), project.getId());
	}
	
	// ----------------------------------

	public boolean deleteProject(long projectID) throws HIWebServiceException {
		try {
            return editor.sysopDeleteProject(projectID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean deleteProject(HiProject project) throws HIWebServiceException {
		return deleteProject(project.getId());
	}
	
	// ----------------------------------

	public boolean deleteUser(long userID) throws HIWebServiceException {
		try {
            return editor.sysopDeleteUser(userID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public boolean deleteUser(HiUser user) throws HIWebServiceException {
		return deleteUser(user.getId());
	}
	
	// ----------------------------------

	public List<HiProject> getProjectsForUser(long userID) throws HIWebServiceException {
		if ( userID == curUser.getId() )
			return getProjects();
		try {
            return editor.sysopGetProjectsForUser(userID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public List<HiProject> getProjectsForUser(HiUser user) throws HIWebServiceException {
		if ( user.getId() == curUser.getId() )
			return getProjects();
		return getProjectsForUser(user.getId());
	}
	public List<HiProject> getProjects(HiUser user) throws HIWebServiceException {
		if ( user.getId() == curUser.getId() )
			return getProjects();
		return getProjectsForUser(user);
	}
	
	// ----------------------------------

	public List<HiUser> getUsers() throws HIWebServiceException {
		try {
            return editor.sysopGetUsers();
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------

	public List<HiUser> getUsersAsAdmin() throws HIWebServiceException {
		try {
            return editor.adminGetUsers();
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------

	public List<HiUser> getProjectUsers() throws HIWebServiceException {
		try {
            return editor.adminGetProjectUsers();
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// ----------------------------------

	public List<HiUser> getUsersForProject(long projectID) throws HIWebServiceException {
		try {
            return editor.sysopGetUsersForProject(projectID);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	public List<HiUser> getUsersForProject(HiProject project) throws HIWebServiceException {
		return getUsersForProject(project.getId());
	}


	// ----------------------------------

	// DEBUG
	public List<HiQuickInfo> simpleSearch(String text, String language) throws HIWebServiceException {
		try {
            return editor.simpleSearch(text, language);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	public List<HiQuickInfo> fieldSearch(List<String> fields, List<String> contents, String language) throws HIWebServiceException {
		try {
            return editor.fieldSearch(fields, contents, language);
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}

	// DEBUG
	public void rebuildSearchIndex() throws HIWebServiceException {
		try {
            editor.rebuildSearchIndex();
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	


	
	/* DEBUG remove template
	
	// ----------------------------------

	public void dummy() throws HIWebServiceException {
		try {
            return editor
		} catch (Exception se) {
			throw new HIWebServiceException(se);
		}
	}
	
	 */
}
