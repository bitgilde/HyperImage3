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

package org.hyperimage.client.components;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.dnd.ContentTransfer;
import org.hyperimage.client.gui.dnd.GroupTransferable;
import org.hyperimage.client.gui.dnd.LayerTransferable;
import org.hyperimage.client.gui.dnd.LocaleTransferable;
import org.hyperimage.client.gui.dnd.ObjectContentTransferable;
import org.hyperimage.client.gui.dnd.QuickInfoTransferable;
import org.hyperimage.client.gui.lists.QuickInfoCell;
import org.hyperimage.client.gui.views.ProjectSettingsView;
import org.hyperimage.client.model.HILayer;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.util.WSImageLoaderThread;
import org.hyperimage.client.ws.GroupTypes;
import org.hyperimage.client.ws.HIMaintenanceModeException_Exception;
import org.hyperimage.client.ws.HiBase;
import org.hyperimage.client.ws.HiBaseTypes;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiImageSizes;
import org.hyperimage.client.ws.HiObjectContent;
import org.hyperimage.client.ws.HiPreference;
import org.hyperimage.client.ws.HiProjectMetadata;
import org.hyperimage.client.ws.HiQuickInfo;

/**
 * @author Jens-Martin Loebel
 */
public class ProjectSettings extends HIComponent implements ActionListener, MouseListener {

	private ProjectSettingsView settingsView;
	
	class LanguageTransferHandler extends TransferHandler {
		
		private static final long serialVersionUID = 6024246448122451592L;
		
		boolean wasAdd = false;
		
		// -------- export methods ---------


		public int getSourceActions(JComponent c) {
			return MOVE;
		}

		protected Transferable createTransferable(JComponent c) {
			Locale lang;

			lang = (Locale) ((JList)c).getSelectedValue();

			return new LocaleTransferable(lang);
		}

		protected void exportDone(JComponent c, Transferable t, int action) {
			// nothing to do here
		}


		// -------- import methods ---------


		public boolean canImport(TransferSupport supp) {
			if ( supp.isDataFlavorSupported(LocaleTransferable.localeFlavor) ) {
				Locale lang;
				try {
					lang = (Locale) supp.getTransferable().getTransferData(LocaleTransferable.localeFlavor);
				} catch (UnsupportedFlavorException e) {
					return false; // error occurred or language empty
				} catch (IOException e) {
					return false; // error occurred or language empty
				}
				
				DefaultListModel model = (DefaultListModel) ((JList)supp.getComponent()).getModel();
				// can´t add language to same list
				if ( model.contains(lang) ) return false;
				
				// a project needs at least one language
				if ( supp.getComponent() == settingsView.getAvailableLangList() && settingsView.getProjectLangList().getModel().getSize() <= 1 )
					return false;
								
				return true;
			}
			
			return false;
		}

		public boolean importData(TransferSupport supp) {
			wasAdd = false;
			if (!canImport(supp)) // check if we can import this
				return false;
			
			// check user role
			if ( !HIRuntime.getGui().checkAdminAbility(false) )
				return false;
			
			supp.setDropAction(MOVE);
			
			if ( supp.isDataFlavorSupported(LocaleTransferable.localeFlavor) ) {
				Locale lang;
				try {
					lang = (Locale) supp.getTransferable().getTransferData(LocaleTransferable.localeFlavor);
				} catch (UnsupportedFlavorException e) {
					return false; // error occurred or language empty
				} catch (IOException e) {
					return false; // error occurred or language empty
				}
				
				// can´t play with the project´s default language
				if ( lang.equals(MetadataHelper.langToLocale(HIRuntime.getManager().getProject().getDefaultLanguage())) ) {
					// inform user
					HIRuntime.getGui().displayInfoDialog(Messages.getString("ProjectSettings.0"), Messages.getString("ProjectSettings.1")); //$NON-NLS-1$ //$NON-NLS-2$
					return false;
				}
				
				if ( supp.getComponent() == settingsView.getProjectLangList() ) {
					/* 
					 * *********************************
					 * handle adding language to project
					 * *********************************
					 */
					wasAdd = true;
					
					// warn the user that this is a complex operation
					boolean proceed = HIRuntime.getGui().displayUserYesNoDialog(
							Messages.getString("ProjectSettings.2"), //$NON-NLS-1$
							Messages.getString("ProjectSettings.3")+"\n\n" + //$NON-NLS-1$ //$NON-NLS-2$
							Messages.getString("ProjectSettings.5")+"\n" + //$NON-NLS-1$ //$NON-NLS-2$
							Messages.getString("ProjectSettings.7")+"\n\n" + //$NON-NLS-1$ //$NON-NLS-2$
							Messages.getString("ProjectSettings.9")); //$NON-NLS-1$
					if ( !proceed ) return false;
					
					proceed = HIRuntime.getGui().saveAllOpenWork();
					if ( !proceed ) return false;
					
					
					try {
						HIRuntime.getGui().startIndicatingServiceActivity();
						boolean languageAdded = HIRuntime.getManager().addLanguageToProject(MetadataHelper.localeToLangID(lang));
						
						if ( languageAdded ) {	
							DefaultListModel model = (DefaultListModel) settingsView.getProjectLangList().getModel();

							// update GUI
							model.insertElementAt(lang, settingsView.getProjectLangList().locationToIndex(supp.getDropLocation().getDropPoint()));
							updateDefaultLanguageOptions();
							model = (DefaultListModel) settingsView.getAvailableLangList().getModel();
							int removeIndex = model.indexOf(lang);
							if ( removeIndex >= 0 ) model.remove(removeIndex);
							
							// propagate changes
							HIRuntime.getGui().sendMessage(HIMessageTypes.LANGUAGE_ADDED, null, ProjectSettings.this);
							
						}
						
						HIRuntime.getGui().stopIndicatingServiceActivity();
						if ( languageAdded ) settingsView.updateStartElementLink();
						return languageAdded;
						
					} catch (HIWebServiceException wse) {
						HIRuntime.getGui().reportError(wse, ProjectSettings.this);
						return false;
					}
				} else {
					/* 
					 * *************************************
					 * handle removing language from project
					 * *************************************
					 */

					// warn the user that this is a complex operation
					boolean proceed = HIRuntime.getGui().displayUserYesNoDialog(
							Messages.getString("ProjectSettings.10"), //$NON-NLS-1$
							Messages.getString("ProjectSettings.11")+"\n\n" + //$NON-NLS-1$ //$NON-NLS-2$
							Messages.getString("ProjectSettings.13")+"\n" + //$NON-NLS-1$ //$NON-NLS-2$
							Messages.getString("ProjectSettings.15")+"\n" + //$NON-NLS-1$ //$NON-NLS-2$
							Messages.getString("ProjectSettings.17")+"\n\n" + //$NON-NLS-1$ //$NON-NLS-2$
							Messages.getString("ProjectSettings.19")); //$NON-NLS-1$
					if ( !proceed ) return false;
					
					proceed = HIRuntime.getGui().saveAllOpenWork();
					if ( !proceed ) return false;


					try {
						HIRuntime.getGui().startIndicatingServiceActivity();
						boolean languageRemoved = HIRuntime.getManager().removeLanguageFromProject(MetadataHelper.localeToLangID(lang));
						if ( languageRemoved ) {	
							// update GUI
							DefaultListModel model = (DefaultListModel) settingsView.getAvailableLangList().getModel();
							model.insertElementAt(lang, settingsView.getAvailableLangList().locationToIndex(supp.getDropLocation().getDropPoint()));
							updateDefaultLanguageOptions();
							model = (DefaultListModel) settingsView.getProjectLangList().getModel();
							int removeIndex = model.indexOf(lang);
							if ( removeIndex >= 0 ) model.remove(removeIndex);
							
							// propagate changes
							HIRuntime.getGui().sendMessage(HIMessageTypes.LANGUAGE_REMOVED, null, ProjectSettings.this);
						}
						
						HIRuntime.getGui().stopIndicatingServiceActivity();
						if ( languageRemoved ) settingsView.updateStartElementLink();
						return languageRemoved;
						
					} catch (HIWebServiceException wse) {
						HIRuntime.getGui().reportError(wse, ProjectSettings.this);
						return false;
					}
				}
				
			}
			
			return false;
		}
	}
	
	
	// ---------------------------------------------------------------------------------------------------
	
	
	/**
	 * This class implements the Drag and Drop handler for project start links.
	 * Only dropping a link is supported.
 	 * 
	 * Class: LinkTransferHandler
	 * @author Jens-Martin Loebel
	 *
	 */
	public class LinkTransferHandler extends TransferHandler {

		private static final long serialVersionUID = 1392755235239308382L;


//		-------- export methods ---------
		

		public int getSourceActions(JComponent c) {
		    return LINK;
		}

		protected Transferable createTransferable(JComponent c) {
			return null;
		}

		protected void exportDone(JComponent c, Transferable t, int action) {
			// focus source component if drop failed
			if ( action == NONE ) HIRuntime.getGui().focusComponent(ProjectSettings.this);
		}
		
		
		// -------- import methods ---------

		
		public boolean canImport(TransferSupport supp) {
			// focus target component if necessary
			HIRuntime.getGui().focusComponent(ProjectSettings.this);

			boolean isLink = false;

			// check user role
			if ( !HIRuntime.getGui().checkEditAbility(true) )
				return false;
			
			if ( supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor)
					|| supp.isDataFlavorSupported(LayerTransferable.layerFlavor)
					|| supp.isDataFlavorSupported(GroupTransferable.groupFlavor)
					|| supp.isDataFlavorSupported(ObjectContentTransferable.objecContentFlavor) )
				isLink = true;

			if ( ! isLink )
		        return false;

		    // check transfer data
		    if ( supp.isDataFlavorSupported(GroupTransferable.groupFlavor) ) {
		    	try {
					HiGroup group = (HiGroup) supp.getTransferable().getTransferData(GroupTransferable.groupFlavor);
				    // cannot create a link to import or trash
					if ( group.getType() != GroupTypes.HIGROUP_REGULAR )
						return false;
				} catch (UnsupportedFlavorException e) {
					return false; // group empty or error occurred
				} catch (IOException e) {
					return false; // group empty or error occurred
				}
		    }

		    supp.setDropAction(LINK); // can only link elements to a layer link field

		    return true;
		}

		public boolean importData(TransferSupport supp) {
		    if (!canImport(supp)) // check if we support transfer elements
		        return false;
		    
			// check user role
			if ( !HIRuntime.getGui().checkEditAbility(false) )
				return false;

		    long targetID = -1l;
		    
		    // try to extract link target ID
		    if ( supp.isDataFlavorSupported(GroupTransferable.groupFlavor) ) {
		    	// link object was a group
		    	try {
					HiGroup group = (HiGroup) supp.getTransferable().getTransferData(GroupTransferable.groupFlavor);
					if ( group == null ) return false;
					targetID = group.getId();
				} catch (UnsupportedFlavorException e) {
					return false; // group empty or error occurred
				} catch (IOException e) {
					return false; // group empty or error occurred
				}
		    }

		    if ( supp.isDataFlavorSupported(LayerTransferable.layerFlavor) ) {
		    	// link object was a layer
		    	try {
					HILayer layer = (HILayer) supp.getTransferable().getTransferData(LayerTransferable.layerFlavor);
					if ( layer == null ) return false;
					
					targetID = layer.getModel().getId();
				} catch (UnsupportedFlavorException e) {
					return false; // layer empty or error occurred
				} catch (IOException e) {
					return false; // layer empty or error occurred
				}
		    }
		    
		    if ( supp.isDataFlavorSupported(ObjectContentTransferable.objecContentFlavor) ) {
		    	// link object was object content
		    	try {
					HiObjectContent content  = (HiObjectContent) supp.getTransferable().getTransferData(ObjectContentTransferable.objecContentFlavor);
					if ( content == null ) return false;
					targetID = content.getId();
				} catch (UnsupportedFlavorException e) {
					return false; // object content empty or error occurred
				} catch (IOException e) {
					return false; // object content empty or error occurred
				}
		    }

		    if ( supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor) ) {
		    	// link object was group content or search result
		    	try {
					ContentTransfer transfer = (ContentTransfer) supp.getTransferable().getTransferData(QuickInfoTransferable.quickInfoFlavor);
					if ( transfer == null ) return false;					
					if ( transfer.getContents().size() != 1 ) return false; // can´t create a link to multiple objects
					
					// create link id
					targetID = transfer.getContents().get(0).getBaseID();

				} catch (UnsupportedFlavorException e) {
					return false; // object content empty or error occurred
				} catch (IOException e) {
					return false; // object content empty or error occurred
				}
		    }
		    
		    supp.setDropAction(LINK);

		    
		    
		    /* ************
		     * handle links
		     * ************
		     */
		    if ( targetID < 0 ) return false; // could not find any useable link target or text to import

		    // create link
		    return setLink(targetID);
		}

	}
	
	
	
	// ------------------------------------------------------------------------------------------------

    public ProjectSettings() {
        super(Messages.getString("ProjectSettings.20"), Messages.getString("ProjectSettings.21")); //$NON-NLS-1$ //$NON-NLS-2$
        HIRuntime.getGui().startIndicatingServiceActivity();
        try {
            // refresh project to get quota and used space
            HIRuntime.getManager().refreshProject();
        } catch (HIWebServiceException e) {
            if (e.getCause() instanceof HIMaintenanceModeException_Exception) {
                HIRuntime.getGui().stopIndicatingServiceActivity();
                HIRuntime.getGui().displayMaintenanceDialog(e);
            }
        }
        HIRuntime.getGui().stopIndicatingServiceActivity();

        // init views
        settingsView = new ProjectSettingsView();

        // register views
        views.add(settingsView);

        // init Drag and Drop handler
        LanguageTransferHandler handler = new LanguageTransferHandler();
        settingsView.getAvailableLangList().setTransferHandler(handler);
        settingsView.getProjectLangList().setTransferHandler(handler);
        settingsView.setLinkTransferHandler(new LinkTransferHandler());
        settingsView.updateStartElementLink();
        if (HIRuntime.getManager().getProject().getStartObjectInfo() != null) {
            loadLinkPreviewImage(HIRuntime.getManager().getProject().getStartObjectInfo(), settingsView.getElementLinkPreview());
        }

        // attach listeners
        settingsView.getDefaultLangComboBox().addActionListener(this);
        settingsView.getStartElementList().addMouseListener(this);
        settingsView.getSaveButton().addActionListener(this);
        settingsView.getResetButton().addActionListener(this);
        settingsView.getPreviewFieldComboBox().addActionListener(this);
    }

	
	public void updateLanguage() {
		super.updateLanguage();
		
		setTitle(Messages.getString("ProjectSettings.20")); //$NON-NLS-1$
		getWindowMenuItem().setText(getTitle());
		HIRuntime.getGui().updateComponentTitle(this);
	}
	
	
	/**
	 * respond to GUI close request, prompt user to save metadata or cancel operation
	 */
	public boolean requestClose() {
		return askToSaveOrCancelChanges();
	}

	
	public void receiveMessage(HIMessageTypes message, HiBase base ) {
		if ( message == HIMessageTypes.ENTITY_CHANGED || message == HIMessageTypes.CHILD_ADDED || message == HIMessageTypes.CHILD_REMOVED ) {
			if ( HIRuntime.getManager().getProject().getStartObjectInfo() != null
				 && base.getId() == HIRuntime.getManager().getProject().getStartObjectInfo().getBaseID() ) {
				// reload info
				HIRuntime.getGui().startIndicatingServiceActivity();
				try {
					HIRuntime.getManager().getProject().setStartObjectInfo(HIRuntime.getManager().getBaseQuickInfo(HIRuntime.getManager().getProject().getStartObjectInfo().getBaseID()));
				} catch (HIWebServiceException wse) {
					HIRuntime.getGui().reportError(wse, this);
				}
				HIRuntime.getGui().stopIndicatingServiceActivity();
				settingsView.updateStartElementLink();
				loadLinkPreviewImage(HIRuntime.getManager().getProject().getStartObjectInfo(), settingsView.getElementLinkPreview());
			}
                        settingsView.updateQuotaInfo();
		}
		
		if ( message == HIMessageTypes.CONTENT_MOVED_TO_TRASH ) {
			settingsView.updateStartElementLink();
			if ( HIRuntime.getManager().getProject().getStartObjectInfo() != null )
				loadLinkPreviewImage(HIRuntime.getManager().getProject().getStartObjectInfo(), settingsView.getElementLinkPreview());
		}
	}

	
	private boolean setLink(long targetID) {
		// check user role
		if ( ! HIRuntime.getGui().checkEditAbility(false) )
			return false;

		try {
			HIRuntime.getGui().startIndicatingServiceActivity();
			if ( HIRuntime.getManager().updateProjectStartElement(targetID) ) {
				HiQuickInfo info = HIRuntime.getManager().getBaseQuickInfo(targetID);
				HIRuntime.getManager().getProject().setStartObjectInfo(info);
				// update GUI
				settingsView.updateStartElementLink();
				loadLinkPreviewImage(info, settingsView.getElementLinkPreview());
			} else {
				HIRuntime.getGui().stopIndicatingServiceActivity();
				return false;
			}

			HIRuntime.getGui().stopIndicatingServiceActivity();
			
		} catch (HIWebServiceException wse) {
			HIRuntime.getGui().reportError(wse, this);
			return false;
		}
		return true;
		
	}
	
	private void loadLinkPreviewImage(HiQuickInfo info, QuickInfoCell cell) {
		if ( cell != null ) {
			if ( cell.needsPreview() ) {
				WSImageLoaderThread thumbLoader = new WSImageLoaderThread();
				long viewID = info.getBaseID();
				boolean cacheImage = true;
				if ( info.getContentType() == HiBaseTypes.HI_OBJECT )
					viewID = info.getRelatedID();
				if ( info.getContentType() == HiBaseTypes.HI_LAYER )
					cacheImage = false;
				thumbLoader.loadImage(viewID, cacheImage, HiImageSizes.HI_THUMBNAIL, cell, settingsView);
			}
		}
	}

	
	private void updateDefaultLanguageOptions() {
		settingsView.getDefaultLangComboBox().removeActionListener(this);
		settingsView.updateLanguageOptions();
		settingsView.getDefaultLangComboBox().addActionListener(this);
	}

	private boolean askToSaveOrCancelChanges() {
		if ( settingsView.hasChanges() && HIRuntime.getGui().checkEditAbility(true) ) {
			int decision = JOptionPane.showConfirmDialog(
					HIRuntime.getGui(), 
							Messages.getString("ProjectSettings.4")); //$NON-NLS-1$
			
			if ( decision == JOptionPane.CANCEL_OPTION ) 
				return false;
			else if ( decision == JOptionPane.YES_OPTION ) {
				saveMetadataChanges();
			} else {
				settingsView.resetChanges(); // undo project metadata changes
			}
		}
		return true;
	}

	private void saveMetadataChanges() {
		if ( !HIRuntime.getGui().checkEditAbility(false) )
			return;
		
		try {
			HIRuntime.getGui().startIndicatingServiceActivity();
			if ( settingsView.hasChanges() ) {
				// update project Metadata
				settingsView.syncChanges();
				for ( HiProjectMetadata metadata : HIRuntime.getManager().getProject().getMetadata() )					
					HIRuntime.getManager().updateProject(metadata.getLanguageID(), metadata.getTitle());

				// propagate changes
				HIRuntime.getGui().updateProjectTitle();
				
				HIRuntime.getGui().stopIndicatingServiceActivity();
			}
		} catch (HIWebServiceException wse) {
			HIRuntime.getGui().reportError(wse, this);
			return;
		}		
	}

	
	// ---------------------------------------------------------------------------------------------------
	

	public void actionPerformed(ActionEvent e) {
            if ( e.getSource() == settingsView.getPreviewFieldComboBox() ) {
                HiPreference titlePref = MetadataHelper.findPreference(HIRuntime.getManager().getProject(), "admin.preview.objectTitleField");
                try {
                    HIRuntime.getGui().startIndicatingServiceActivity();
                    // create oreference if it doesn't exist
                    if ( titlePref == null ) HIRuntime.getManager().createPreference("admin.preview.objectTitleField", settingsView.getSelectedPreviewField());
                    else {
                        titlePref.setValue(settingsView.getSelectedPreviewField());
                        HIRuntime.getManager().updatePreference(titlePref);
                    }
                    HIRuntime.getGui().stopIndicatingServiceActivity();
                } catch (HIWebServiceException wse) {
                    HIRuntime.getGui().reportError(wse, ProjectSettings.this);
		}
                // propagate changes
                HIRuntime.getGui().sendMessage(HIMessageTypes.PREFERENCE_MODIFIED, null, this);
                HIRuntime.getGui().sendMessage(HIMessageTypes.DEFAULT_LANGUAGE_CHANGED, null, this);
                
            } else if ( e.getActionCommand().equalsIgnoreCase("save") ) { //$NON-NLS-1$
			if ( HIRuntime.getGui().checkEditAbility(false) && settingsView.hasChanges() )
				saveMetadataChanges();
		} else if ( e.getActionCommand().equalsIgnoreCase("reset") ) { //$NON-NLS-1$
			settingsView.resetChanges(); // undo project metadata changes
		} else {
			// change default language
			String newDefLangID = MetadataHelper.localeToLangID((Locale) settingsView.getDefaultLangComboBox().getSelectedItem());
			
			if ( !newDefLangID.equalsIgnoreCase(HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()) ) {
				// warn user
				boolean changeDefLanguage = HIRuntime.getGui().displayUserYesNoDialog(Messages.getString("ProjectSettings.12"), Messages.getString("ProjectSettings.14")); //$NON-NLS-1$ //$NON-NLS-2$
				if ( changeDefLanguage ) {
					HIRuntime.getGui().startIndicatingServiceActivity();
					try {
						if ( HIRuntime.getManager().updateProjectDefaultLanguage(newDefLangID) ) {
							settingsView.updateStartElementLink();
							
							// propagate changes
							HIRuntime.getGui().sendMessage(HIMessageTypes.DEFAULT_LANGUAGE_CHANGED, null, this);
						}
					} catch (HIWebServiceException wse) {
						HIRuntime.getGui().reportError(wse, this);
					}
					HIRuntime.getGui().stopIndicatingServiceActivity();
				}
			}			
			settingsView.updateLanguageOptions();
			HIRuntime.getGui().updateProjectTitle();
		}
			
	}

	
	
	// ---------------------------------------------------------------------------------------------------
	


	public void mouseClicked(MouseEvent e) {
		if ( e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 && HIRuntime.getManager().getProject().getStartObjectInfo() != null )
			HIRuntime.getGui().openContentEditor(HIRuntime.getManager().getProject().getStartObjectInfo(), this);
	}

	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mousePressed(MouseEvent e) {
	}
	public void mouseReleased(MouseEvent e) {
	}

}
