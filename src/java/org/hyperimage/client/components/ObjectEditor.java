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

import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.ObjectContentCell;
import org.hyperimage.client.gui.dnd.ContentTransfer;
import org.hyperimage.client.gui.dnd.GroupTransferable;
import org.hyperimage.client.gui.dnd.LayerTransferable;
import org.hyperimage.client.gui.dnd.ObjectContentTransferable;
import org.hyperimage.client.gui.dnd.QuickInfoTransferable;
import org.hyperimage.client.gui.lists.ObjectContentsCellRenderer;
import org.hyperimage.client.gui.views.FlexMetadataEditorView;
import org.hyperimage.client.gui.views.ObjectContentEditView;
import org.hyperimage.client.gui.views.ObjectContentsListView;
import org.hyperimage.client.model.HILayer;
import org.hyperimage.client.util.WSImageLoaderThread;
import org.hyperimage.client.ws.GroupTypes;
import org.hyperimage.client.ws.HiBase;
import org.hyperimage.client.ws.HiBaseTypes;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiImageSizes;
import org.hyperimage.client.ws.HiInscription;
import org.hyperimage.client.ws.HiLayer;
import org.hyperimage.client.ws.HiObject;
import org.hyperimage.client.ws.HiObjectContent;
import org.hyperimage.client.ws.HiQuickInfo;
import org.hyperimage.client.ws.HiView;

/**
 * @author Jens-Martin Loebel
 */
public class ObjectEditor extends HIComponent implements ActionListener, ListSelectionListener, MouseListener {

	private ObjectContentsListView objectContentsListView;
	private ObjectContentEditView objectContentEditView;
	private FlexMetadataEditorView metadataEditorView;

	private int contentIndex = 0;

	private HiObject object;

	
	/**
	 * This class implements the Drag and Drop handler the layer preview. It allows directly linking a layer 
	 * to a drop target without having to open the layer editor (hotlinking).
 	 * 
	 * Class: LayerHotlinkTransferHandler
	 * @author Jens-Martin Loebel
	 *
	 */
	public class LayerHotlinkTransferHandler extends TransferHandler {

		private static final long serialVersionUID = -2298227072833126148L;
		
		
		public LayerHotlinkTransferHandler() {
		}
		
		
		// -------- export methods ---------


		public int getSourceActions(JComponent c) {
			return LINK;
		}

		protected Transferable createTransferable(JComponent c) {
			// dragging is not supported
			return null;
		}

		protected void exportDone(JComponent c, Transferable t, int action) {
			// nothing to be done here as dragging is not supported
		}


		// -------- import methods ---------

		

		public boolean canImport(TransferSupport supp) {
			// focus target component if necessary
			HIRuntime.getGui().focusComponent(ObjectEditor.this);

			if ( objectContentEditView.getLayerViewer().isVisible() )
				objectContentEditView.getLayerViewer().setState(supp.getDropLocation().getDropPoint().x, supp.getDropLocation().getDropPoint().y);
			else return false;
			
			if ( objectContentEditView.getLayerViewer().getActiveLayer() == null )
				return false;
			
			// check user role
			if ( !HIRuntime.getGui().checkEditAbility(true) )
				return false;

			if (!supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor)
					&& !supp.isDataFlavorSupported(LayerTransferable.layerFlavor)
					&& !supp.isDataFlavorSupported(GroupTransferable.groupFlavor)
					&& !supp.isDataFlavorSupported(ObjectContentTransferable.objecContentFlavor) )
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

			supp.setDropAction(LINK); // can only link elements to a layer
			
			return true;
		}

		public boolean importData(TransferSupport supp) {
			if (!canImport(supp)) return false; // check if we support this element

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

					// cannot link a layer to itself
					if ( layer.getModel().getId() == objectContentEditView.getLayerViewer().getActiveLayer().getModel().getId() )
						return false;
					
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
					
					// cannot link a layer to itself
					if ( transfer.getContents().get(0).getBaseID() == objectContentEditView.getLayerViewer().getActiveLayer().getModel().getId()
						 && transfer.getContents().get(0).getContentType() == HiBaseTypes.HI_LAYER )
						return false;

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

	
	// -----------------------------------------------------------------------------------------
	

	
	/**
	 * This class implements the Drag and Drop handler for the Object Content (HiView, HiInscription) List.
 	 * As per SDD: only dragging a content object into the group contents folder, and moving a content object
 	 * to another object content list, sorting object contents, is supported.
 	 * 
	 * Class: ObjectContentTransferHandler
	 * @author Jens-Martin Loebel
	 *
	 */
	public class ObjectContentTransferHandler extends TransferHandler {
		
		private static final long serialVersionUID = -2298227072833126148L;
		
		
		private ObjectEditor editor;
		private boolean wasSort = false; // indicated whether the object content move was a sort operation
				
		
		public ObjectContentTransferHandler(ObjectEditor editor) {
			this.editor = editor;
		}
		
		
		// -------- export methods ---------


		public int getSourceActions(JComponent c) {
			return COPY_OR_MOVE+LINK;
		}

		protected Transferable createTransferable(JComponent c) {
			HiObjectContent content;

			content = (HiObjectContent) ((JList)c).getSelectedValue();

			return new ObjectContentTransferable(content);
		}

		protected void exportDone(JComponent c, Transferable t, int action) {
			// check user role
			if ( !HIRuntime.getGui().checkEditAbility(true) )
				return;
			
			// focus source component if drop failed
			if ( action == NONE ) HIRuntime.getGui().focusComponent(editor);

			if ( action == MOVE && !wasSort ) {
				// delete view from this object (locally), update sort order, repaint content list
				try {
					HiObjectContent content = (HiObjectContent) t.getTransferData(ObjectContentTransferable.objecContentFlavor);
					object.getViews().remove(content);
					// update default view if necessary
					if ( object.getDefaultView() != null && object.getDefaultView().getId() == content.getId() )
						object.setDefaultView(null);
					refreshContentList();
					// update sort order on server
					updateSortOrder();
					// propagate changes
					HIRuntime.getGui().sendMessage(HIMessageTypes.CHILD_REMOVED, object, editor);
					HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, object, editor);
				} catch (UnsupportedFlavorException e) {
				} catch (IOException e) {
				}
				
				
			}
			// if this was not a MOVE action (e.g. a link), nothing needs to be done here
		}


		// -------- import methods ---------


		public boolean canImport(TransferSupport supp) {
			// focus target component if necessary
			HIRuntime.getGui().focusComponent(editor);

			// check user role
			if ( !HIRuntime.getGui().checkEditAbility(true) )
				return false;

			if (!supp.isDataFlavorSupported(ObjectContentTransferable.objecContentFlavor)
					&& !supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor))
				return false;
			
			// shortcut for group contents, can´t import them but will inform the user later
			// artop suggestion (give feedback for dragging objects into view list)
			if ( supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor) ) {
				try {
					ContentTransfer transfer = (ContentTransfer) supp.getTransferable().getTransferData(QuickInfoTransferable.quickInfoFlavor);
					if ( transfer == null ) return false;
					if ( transfer.getContents().size() != 1 ) return false;
					if ( transfer.getContents().get(0).getContentType() != HiBaseTypes.HI_OBJECT ) return false;
					return true; // user tried to drag a single object from the group contents view
				} catch (UnsupportedFlavorException e) {
					return false;
				} catch (IOException e) {
					return false;
				}
			}
			return true;
		}

		public boolean importData(TransferSupport supp) {
			if (!canImport(supp)) return false; // check if we support this element

			// check user role
			if ( !HIRuntime.getGui().checkEditAbility(false) )
				return false;

			// inform the user if he/she tried to drag an object into the view list
			if ( supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor) ) {
				HIRuntime.getGui().displayInfoDialog(Messages.getString("ObjectEditor.0"), Messages.getString("ObjectEditor.1")+"\n\n" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						Messages.getString("ObjectEditor.3")); //$NON-NLS-1$
				return false;
			}
						
			// Fetch the Transferable and its data
			Transferable t = supp.getTransferable();
			HiObjectContent content = null;
			try {
				wasSort = false;
				content =  (HiObjectContent) t.getTransferData(ObjectContentTransferable.objecContentFlavor);
				if ( content == null ) return false; // no content to be transferred

				// if content object came from another object editor --> this is a move
				// check if content object belongs to this object
				for ( HiObjectContent objContent : object.getViews() )
					if ( objContent.getId() == content.getId() )
						wasSort = true;
				supp.setDropAction(MOVE); // move action
				
			} catch (UnsupportedFlavorException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
			
			// import view, change object owner
			try {
				if ( wasSort ) {
					/* ***************************
					 * Sort this object´s contents
					 * ***************************
					 */
					// find source index
					int moveIndex = -1;
					int insertIndex = objectContentsListView.getContentsList().locationToIndex(supp.getDropLocation().getDropPoint());
					DefaultListModel model = (DefaultListModel) objectContentsListView.getContentsList().getModel();
					for ( int i=0 ; i < model.getSize() ; i++ )
						if ( ((HiObjectContent)model.get(i)).getId() == content.getId() )
							moveIndex = i;
					
					// nothing to do or content not found
					if ( moveIndex < 0 || moveIndex == insertIndex ) return false;

					// sort list
					HiObjectContent sortContent = (HiObjectContent) model.get(moveIndex);
					object.getViews().remove(sortContent);
					object.getViews().add(insertIndex, sortContent);
					HIRuntime.getGui().startIndicatingServiceActivity();
					refreshContentList();
					updateSortOrder();
					HIRuntime.getGui().stopIndicatingServiceActivity();
					
				} else {
					/* *************************************************
					 * Handle moving object contents from another object
					 * *************************************************
					 */
					HIRuntime.getGui().startIndicatingServiceActivity();
					HIRuntime.getManager().updateContentOwner(object, content); // sync to server				
					HIRuntime.getGui().stopIndicatingServiceActivity();
					DropLocation loc = supp.getDropLocation(); // Fetch the drop location
					DefaultListModel model = (DefaultListModel) objectContentsListView.getContentsList().getModel();
					int index = objectContentsListView.getContentsList().locationToIndex(loc.getDropPoint());
					index = Math.max(0, index); // make sure index is valid
					model.add(index, content);
					refreshContentList(); // update GUI
					// update sort order on server
					updateSortOrder();
					objectContentsListView.updateViewCount();

					// propagate changes
					HIRuntime.getGui().sendMessage(HIMessageTypes.CHILD_ADDED, object, editor);
				}
				
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, object, editor);

				return true;
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, editor);
				return false;
			}			
		}

	}

	// -----------------------------------------------------------------------------------------
	
	
	
	public ObjectEditor(HiObject object) {
		this(object, -1);
	}	
	
	public ObjectEditor(HiObject object, long contentID) {
            super(Messages.getString("ObjectEditor.4")+" ("+
                    (object.getUUID() == null ? "O"+object.getId() : object.getUUID())+
                    ")", Messages.getString("ObjectEditor.7")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            this.object = object;

		objectContentsListView = new ObjectContentsListView(
				HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()
				,this.object);

		objectContentEditView = new ObjectContentEditView(
				HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()		
		);

		metadataEditorView = new FlexMetadataEditorView(
				HIRuntime.getManager().getProject().getTemplates(),
				object,
				HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());

		if ( contentID <= 0 ) {
			// try to find the standard view and select it in the object content list
			if ( object.getDefaultView() == null && object.getViews().size() > 0 )
				objectContentsListView.getContentsList().setSelectedIndex(0);
			if ( object.getDefaultView() != null ) {
				for ( int i=0 ; i < object.getViews().size(); i++ )
					if ( ((HiObjectContent)objectContentsListView.getContentsList().getModel().getElementAt(i)).getId() == object.getDefaultView().getId() )
						objectContentsListView.getContentsList().setSelectedIndex(i);
			}
		} else {
			// user requested specific view/inscription as initial display, select that view if possible
			displayContentByID(contentID);
			// fall back
			if ( object.getViews().size() > 0 && objectContentsListView.getContentsList().getSelectedIndex() < 0 )
				objectContentsListView.getContentsList().setSelectedIndex(0);
		}
		
		// init and display standard view
		if ( objectContentsListView.getContentsList().getModel().getSize() > 0 ) {
			HiObjectContent content = (HiObjectContent)objectContentsListView.getContentsList().getSelectedValue();
			setPreviewContent(content);
			contentIndex = objectContentsListView.getContentsList().getSelectedIndex();
		}
		// load content previews (thumb images)
		loadContentPreviews();
		
		// sync sort order with server
		updateSortOrder();

		// check user role
		if ( ! HIRuntime.getGui().checkEditAbility(true) )
			objectContentsListView.getOptionsButton().setEnabled(false);

		// register views
		this.views.add(objectContentsListView);
		this.views.add(objectContentEditView);
		this.views.add(metadataEditorView);
		
		// init Drag and Drop
		objectContentsListView.getContentsList().setTransferHandler(new ObjectContentTransferHandler(this));
		objectContentEditView.getLayerViewer().setTransferHandler(new LayerHotlinkTransferHandler());

		// attach listeners
		objectContentsListView.getContentsList().addListSelectionListener(this);
		metadataEditorView.getSaveButton().addActionListener(this);
		metadataEditorView.getResetButton().addActionListener(this);
		objectContentsListView.getContentsList().addMouseListener(this);
		objectContentEditView.getLayerEditorButton().addActionListener(this);
		objectContentEditView.getLayerViewer().attachActionListener(this);
		objectContentEditView.getLayerViewer().addMouseListener(this);

		// attach listeners --> check user role
		if ( HIRuntime.getGui().checkEditAbility(true) ) {
			objectContentsListView.getOptionsButton().addActionListener(this);
			objectContentsListView.setMenuActionListener(this); // popup menu
		} else objectContentsListView.disablePopup();
	}

	
	public void updateLanguage() {
		super.updateLanguage();
		
		setTitle(Messages.getString("ObjectEditor.4")+" (O"+object.getId()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
		getWindowMenuItem().setText(getTitle());
		HIRuntime.getGui().updateComponentTitle(this);
	}


	public HiBase getBaseElement() {
		return this.object;
	}

	
	/**
	 * respond to GUI close request, prompt user to save metadata or cancel operation
	 */
	public boolean requestClose() {
		return askToSaveOrCancelChanges(true);
	}
	
	
	/**
	 * implementation of the HIComponent method
	 * The object editor will accept HiObject, HiView and HiLayer elements and check if they are currently being displayed.
	 * It will check for updated views/inscriptions as well as changes to the object itself.
	 * This method updates the GUI to reflect the changes made by other components.
	 */
	
        @Override
	public void receiveMessage(HIMessageTypes message, HiBase base ) {

		if ( message == HIMessageTypes.ENTITY_CHANGED ) {
			// check for updated object content (view or inscription)
			if ( base instanceof HiView || base instanceof HiInscription ) {
				// check if view belongs to editing object of this object editor

				HiObjectContent objContent = null; // get currently displayed view
				if ( objectContentsListView.getContentsList().getModel().getSize() > 0 )
					objContent = (HiObjectContent) objectContentsListView.getContentsList().getModel().getElementAt(contentIndex);

				for ( HiObjectContent content : object.getViews() )
					if ( content.getId() == base.getId() ) {
						if (content instanceof HiInscription)
							objectContentsListView.updateRendering((HiInscription)content); // update preview view list
						// update preview and metadata if this object content is currently selected by the user
						if ( objContent != null ) if ( objContent.getId() == base.getId() ) {
							objectContentEditView.updateContent();
							metadataEditorView.updateContent();
						}
					}
			} 
			if ( base instanceof HiLayer )
				// TODO check if layer belongs to currently displayed view
				objectContentEditView.updateContent();
		}
		
		if ( message == HIMessageTypes.CHILD_ADDED || message == HIMessageTypes.CHILD_REMOVED ) {
			if ( base instanceof HiView ) {
				// check if view belongs to editing object of this object editor

				HiObjectContent objContent = null; // get currently displayed view
				if ( objectContentsListView.getContentsList().getModel().getSize() > 0 )
					objContent = (HiObjectContent) objectContentsListView.getContentsList().getModel().getElementAt(contentIndex);
			
				for ( HiObjectContent content : object.getViews() )
					if ( content.getId() == base.getId() ) {
						if ( objContent != null ) if ( objContent.getId() == base.getId() )
							objectContentEditView.updateContent();
					}
			}
			if ( base instanceof HiObject ) {
				// check for updated object --> added/removed content
				// TODO implement update object request
			}
		}
		
		if ( message == HIMessageTypes.LANGUAGE_ADDED || message == HIMessageTypes.LANGUAGE_REMOVED ) {	
			// reload all base metadata
			HIRuntime.getGui().startIndicatingServiceActivity();
			try {
				List<HiFlexMetadataRecord> records = HIRuntime.getManager().getFlexMetadataRecords(this.object);
				while ( this.object.getMetadata().size() > 0 ) this.object.getMetadata().remove(0);
				for ( HiFlexMetadataRecord record : records )
					this.object.getMetadata().add(record);
				
				// do the same for all object views
				for ( HiObjectContent content : object.getViews() ) {
					records = HIRuntime.getManager().getFlexMetadataRecords(content);
					while ( content.getMetadata().size() > 0 ) content.getMetadata().remove(0);
					for ( HiFlexMetadataRecord record : records )
						content.getMetadata().add(record);
				}
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
			HIRuntime.getGui().stopIndicatingServiceActivity();

			// update GUI
			metadataEditorView.updateMetadataLanguages();
			objectContentEditView.updateMetadataLanguages();
		}


	}



	// -----------------------------------------------------------------------
	
	
	public void displayContentByID(long id) {
		for ( int i = 0 ; i < objectContentsListView.getContentsList().getModel().getSize() ; i++ )
			if ( ((HiObjectContent)objectContentsListView.getContentsList().getModel().getElementAt(i)).getId() == id )
				objectContentsListView.getContentsList().setSelectedIndex(i);
		// scroll to new selected index
		objectContentsListView.getContentsList().scrollRectToVisible(
				new Rectangle(
						objectContentsListView.getContentsList().indexToLocation(objectContentsListView.getContentsList().getSelectedIndex())
				));
	}
	

	
	private boolean setLink(long targetID) {
		if ( objectContentEditView.getLayerViewer().getActiveLayer() == null ) return false;
		if ( object.getViews().size() == 0 ) return false;
		
		HILayer layer = objectContentEditView.getLayerViewer().getActiveLayer();
		
		// check user role
		if ( ! HIRuntime.getGui().checkEditAbility(false) )
			return false;

		try {
			HIRuntime.getGui().startIndicatingServiceActivity();
			if ( HIRuntime.getManager().setLayerLink(layer.getModel().getId(), targetID) ) {
				HiQuickInfo info = HIRuntime.getManager().getBaseQuickInfo(targetID);
				layer.getModel().setLinkInfo(info);
				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, layer.getModel(), this);
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, (HiObjectContent)objectContentsListView.getContentsList().getSelectedValue(), this);
			} else {
				HIRuntime.getGui().stopIndicatingServiceActivity();
				return false;
			}

			HIRuntime.getGui().stopIndicatingServiceActivity();
			
		} catch (HIWebServiceException wse) {
			HIRuntime.getGui().reportError(wse, this);
			return false;
		}
		
		// inform the user
		HIRuntime.getGui().displayInfoDialog(Messages.getString("ObjectEditor.8"), Messages.getString("ObjectEditor.9")); //$NON-NLS-1$ //$NON-NLS-2$
		return true;
		
	}

	
	private boolean removeLink() {
		// check user role
		if ( ! HIRuntime.getGui().checkEditAbility(false) )
			return false;
		// sanity check
		if ( objectContentEditView.getLayerViewer().getSelectedLayer() == null )
			return false;
		
		try {
			HIRuntime.getGui().startIndicatingServiceActivity();
			if ( HIRuntime.getManager().removeLayerLink(objectContentEditView.getLayerViewer().getSelectedLayer().getModel().getId()) ) {
				objectContentEditView.getLayerViewer().getSelectedLayer().getModel().setLinkInfo(null);
				objectContentEditView.getLayerViewer().repaint();
				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, objectContentEditView.getLayerViewer().getSelectedLayer().getModel(), this);
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, (HiBase) objectContentsListView.getContentsList().getSelectedValue(), this);
			}
			HIRuntime.getGui().stopIndicatingServiceActivity();
			
		} catch (HIWebServiceException wse) {
			HIRuntime.getGui().reportError(wse, this);
			return false;
		}
		
		// inform the user
		HIRuntime.getGui().displayInfoDialog(Messages.getString("ObjectEditor.10"), Messages.getString("ObjectEditor.11")); //$NON-NLS-1$ //$NON-NLS-2$
		return true;		
	}

	
	private void updateSortOrder() {
		String sortOrder = objectContentsListView.getSortOrder();

		// check user role
		if ( !HIRuntime.getGui().checkEditAbility(true) )
			return;

		if ( object.getSortOrder().compareTo(sortOrder) != 0 ) {
			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				HIRuntime.getManager().updateObjectSortOrder(object, sortOrder);
				HIRuntime.getGui().stopIndicatingServiceActivity();
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
		}
	}


	private void openLayerEditor(HiLayer layer) {
		if ( object.getViews().size() > 0 ) {
			if ( layer == null ) {
				HiObjectContent content = (HiObjectContent) objectContentsListView.getContentsList().getModel().getElementAt(contentIndex);
				if ( content instanceof HiView )
					HIRuntime.getGui().openLayerEditor((HiView)content);
			} else
				HIRuntime.getGui().openContentEditor(layer.getId(), this);
		}		
	}

    private boolean askToSaveOrCancelChanges(boolean includeObjectMetadata) {
        if ((objectContentEditView.hasChanges() || (includeObjectMetadata && metadataEditorView.hasChanges())) && HIRuntime.getGui().checkEditAbility(true)) {
            int decision = HIRuntime.getGui().displayUserChoiceDialog(
                    Messages.getString("ObjectEditor.12"), Messages.getString("ObjectEditor.13")); //$NON-NLS-1$ //$NON-NLS-2$

            if (decision == JOptionPane.CANCEL_OPTION) {
                return false;
            } else if (decision == JOptionPane.YES_OPTION) {
                saveMetadataChanges();
		if (includeObjectMetadata) {
                    saveObjectMetadataChanges();
                }
            }
        }
        return true;
    }

	private void saveMetadataChanges() {
		if ( objectContentEditView.hasChanges() && HIRuntime.getGui().checkEditAbility(false) ) {
			objectContentEditView.syncChanges();

			HiObjectContent content = 
				(HiObjectContent) objectContentsListView.getContentsList().getModel().getElementAt(contentIndex);
			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				HIRuntime.getManager().updateFlexMetadataRecords(content.getMetadata());
                                content.setTimestamp(new Date().getTime());
                                objectContentEditView.updateStatusBar();
				HIRuntime.getGui().stopIndicatingServiceActivity();
				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, content, this);
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, object, this); // also update parent object
				// update GUI
				if ( content instanceof HiInscription ) objectContentsListView.updateRendering((HiInscription)content);
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}

		
		}
	}
	
	private void saveObjectMetadataChanges() {
		if ( metadataEditorView.hasChanges() && HIRuntime.getGui().checkEditAbility(false) ) {
			metadataEditorView.syncChanges();
			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				HIRuntime.getManager().updateFlexMetadataRecords(object.getMetadata());
                                object.setTimestamp(new Date().getTime());
                                metadataEditorView.updateLanguage();
				HIRuntime.getGui().stopIndicatingServiceActivity();
				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, object, this);
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
		}
	}


	private void loadContentPreviews() {
		ObjectContentsCellRenderer renderer = (ObjectContentsCellRenderer) objectContentsListView.getContentsList().getCellRenderer();
		for ( HiObjectContent content : object.getViews() ) {
			if ( content instanceof HiView ) {
				ObjectContentCell cell = renderer.getCellForContent(content);
				if ( cell != null ) {
					if ( cell.needsPreview() ) {
						WSImageLoaderThread thumbLoader = new WSImageLoaderThread();
						thumbLoader.loadImage(((HiView)content).getId(), HiImageSizes.HI_THUMBNAIL, cell, objectContentsListView.getContentsList());
					}
				}
			}
		}
	}

	private void setPreviewContent(HiObjectContent content) {
		objectContentEditView.setContentView(content);
		if ( content instanceof HiView && objectContentEditView.needsPreview() ) {
			WSImageLoaderThread previewLoader = new WSImageLoaderThread();
			previewLoader.loadImage(content.getId(), HiImageSizes.HI_PREVIEW, objectContentEditView, objectContentEditView.getDisplayPanel());
		} else objectContentEditView.updateStatusBar();
	}


	private void addInscription() {
		try {
			HIRuntime.getGui().startIndicatingServiceActivity();
			HiInscription inscription = HIRuntime.getManager().createInscription(object);
			 // update local model
			object.getViews().add(inscription);
			// propagate changes
			HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, object, this);
			HIRuntime.getGui().sendMessage(HIMessageTypes.CHILD_ADDED, object, this);
			HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_ADDED, inscription, this);
			// update GUI
			HIRuntime.getGui().stopIndicatingServiceActivity();
			refreshContentList();
			// update sort order on server
			updateSortOrder();
			objectContentsListView.updateViewCount();
		} catch (HIWebServiceException wse) {
			HIRuntime.getGui().reportError(wse, this);
			return;
		}
	}


	private void refreshContentList() {
		// remember old content object
		contentIndex = objectContentsListView.getContentsList().getSelectedIndex();
		HiObjectContent content = null;
		if ( contentIndex >= 0 ) content = (HiObjectContent) objectContentsListView.getContentsList().getSelectedValue();

		objectContentsListView.getContentsList().removeListSelectionListener(this);
		objectContentsListView.refreshList(); // update list
		// load preview images if necessary
		loadContentPreviews();
		// update GUI if necessary
		if ( objectContentsListView.getContentsList().getSelectedIndex() >=0 ) {
			if ( content == null || content.getId() != ((HiObjectContent)objectContentsListView.getContentsList().getSelectedValue()).getId() ) {
				contentIndex = objectContentsListView.getContentsList().getSelectedIndex();
				content = (HiObjectContent)objectContentsListView.getContentsList().getSelectedValue();
				setPreviewContent(content);
			}
		} else setPreviewContent(null);
		contentIndex = objectContentsListView.getContentsList().getSelectedIndex();
		objectContentsListView.updateViewCount();		

		objectContentsListView.getContentsList().addListSelectionListener(this);
	}


	private boolean isDefaultView() {
		if ( objectContentsListView.getContentsList().getModel().getSize() == 0 ) return false;
		if ( object.getDefaultView() == null ) return false;
		if ( object.getDefaultView().getId() == ((HiObjectContent)objectContentsListView.
				getContentsList().getSelectedValue()).getId() )
			return true;
		
		return false;
	}


	private void setDefaultView(HiObjectContent content) {
		HIRuntime.getGui().startIndicatingServiceActivity();
		if ( content == null ) {
			try {
				HIRuntime.getManager().removeDefaultView(object);
				object.setDefaultView(null);
				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, object, this); 
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
		} else {
			try {
				HIRuntime.getManager().setDefaultView(object.getId(), content.getId());
				object.setDefaultView(content);
				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, object, this); 
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
		}
		HIRuntime.getGui().stopIndicatingServiceActivity();

	}
	
	
	
	// -----------------------------------------------------------------------
	
	
	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand().equalsIgnoreCase("save") ) { //$NON-NLS-1$
			// check user role
			if ( !HIRuntime.getGui().checkEditAbility(false) )
				return;
			
			if ( metadataEditorView.hasChanges() )
				saveObjectMetadataChanges();

			if ( objectContentEditView.hasChanges() )
				saveMetadataChanges();
		}

		if ( e.getActionCommand().equalsIgnoreCase("reset") ) { //$NON-NLS-1$
			metadataEditorView.resetChanges();
			objectContentEditView.resetChanges();
		}
		
		if ( e.getActionCommand().equalsIgnoreCase("openLinkTarget") ) //$NON-NLS-1$
			if ( objectContentEditView.getLayerViewer().getSelectedLayer() != null
					&& objectContentEditView.getLayerViewer().getSelectedLayer().getModel().getLinkInfo() != null ) 
			HIRuntime.getGui().openContentEditor(objectContentEditView.getLayerViewer().getSelectedLayer().getModel().getLinkInfo(), this);

		if ( e.getActionCommand().equalsIgnoreCase("removeLink") ) //$NON-NLS-1$
			if ( HIRuntime.getGui().displayUserYesNoDialog(Messages.getString("ObjectEditor.18"), Messages.getString("ObjectEditor.19")) )  //$NON-NLS-1$ //$NON-NLS-2$
				removeLink();
		
		if ( e.getActionCommand().equalsIgnoreCase("editLayers") ) //$NON-NLS-1$
			openLayerEditor(null);

		if ( e.getActionCommand().equalsIgnoreCase("editLayer") ) //$NON-NLS-1$
			if ( objectContentEditView.getLayerViewer().getSelectedLayer() != null ) 
				openLayerEditor(objectContentEditView.getLayerViewer().getSelectedLayer().getModel());

		if ( e.getActionCommand().equalsIgnoreCase("moveToTrash") ) { //$NON-NLS-1$
			if ( ! HIRuntime.getGui().checkEditAbility(false) ) // check user role
				return;
                        String message = Messages.getString("ObjectEditor.24");
                        if ( objectContentsListView.getContentsList().getSelectedValue() instanceof HiInscription )
                                message = Messages.getString("ObjectEditor.deleteinscription");
			if ( ! HIRuntime.getGui().displayUserYesNoDialog(
					Messages.getString("ObjectEditor.23"), //$NON-NLS-1$
					message) ) //$NON-NLS-1$
				return;
			// DEBUG refactor into seperate function
			try {
				HiObjectContent content = (HiObjectContent)objectContentsListView.getContentsList().getSelectedValue();
				// move contents to trash
				HIRuntime.getGui().startIndicatingServiceActivity();
				HIRuntime.getManager().moveToTrash(content.getId());
				HIRuntime.getGui().stopIndicatingServiceActivity();
				// update local model
				object.getViews().remove(content);
				if ( object.getDefaultView() != null && object.getDefaultView().getId() == content.getId() )
					object.setDefaultView(null);
				// update GUI
				refreshContentList();
				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.MOVED_TO_TRASH, content, this);
				HIRuntime.getGui().sendMessage(HIMessageTypes.CHILD_REMOVED, object, this);
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, object, this);
				
			} catch (HIWebServiceException wse) {
					HIRuntime.getGui().reportError(wse, this);
					return;
				}
		}
		
		if ( e.getActionCommand().equalsIgnoreCase("options") ) //$NON-NLS-1$
			// display options popup menu
			objectContentsListView.showDefaultPopupMenu();

		
		if ( e.getActionCommand().equalsIgnoreCase("defaultView") ) //$NON-NLS-1$
			if ( HIRuntime.getGui().checkEditAbility(false) ) // check user role
				if ( objectContentsListView.getContentsList().getModel().getSize() > 0 ) {
					HiObjectContent content = (HiObjectContent)objectContentsListView.getContentsList().getSelectedValue();
					if ( isDefaultView() ) setDefaultView(null);
					else setDefaultView(content);
				}
		
		if ( e.getActionCommand().equalsIgnoreCase("newInscription") ) //$NON-NLS-1$
			if ( HIRuntime.getGui().checkEditAbility(false) ) 
				addInscription();
	}

	
	public void valueChanged(ListSelectionEvent e) {
		if ( objectContentsListView.getContentsList().getModel().getSize() > 0 ) {
			// you cannot de-select the content view list
			if ( objectContentsListView.getContentsList().getSelectedIndex() == -1 ) {
				objectContentsListView.setSelectedIndex(contentIndex);
				return;
			}
			// selected object content has changed:
			if ( contentIndex != objectContentsListView.getContentsList().getSelectedIndex() ) {
				// ask to use to save, discard or cancel the operation
				if ( objectContentEditView.hasChanges() && HIRuntime.getGui().checkEditAbility(true) )
					if ( ! askToSaveOrCancelChanges(false) ) {
						// cancel operation
						objectContentsListView.setSelectedIndex(contentIndex);
						return;
					}
				
				contentIndex = objectContentsListView.getContentsList().getSelectedIndex();
				objectContentsListView.setSelectedIndex(contentIndex); // notify GUI (standard view button)
				HiObjectContent content = (HiObjectContent)objectContentsListView.getContentsList().getSelectedValue();
				setPreviewContent(content);
			}
		}
	}

	
	// -----------------------------------------------------------------------


	@Override
	public void mouseClicked(MouseEvent e) {
		if ( e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 ) {
			// Open the layer editor if user double clicks on content list
			if ( e.getSource() == objectContentsListView.getContentsList() )
				openLayerEditor(null);
			
			// Open link if user double clicks on an active layer
			if ( e.getSource() == objectContentEditView.getLayerViewer() )
				if ( objectContentEditView.getLayerViewer().getActiveLayer() != null
						&& objectContentEditView.getLayerViewer().getActiveLayer().getModel().getLinkInfo() != null ) 
					HIRuntime.getGui().openContentEditor(objectContentEditView.getLayerViewer().getActiveLayer().getModel().getLinkInfo(), this);
		}
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
