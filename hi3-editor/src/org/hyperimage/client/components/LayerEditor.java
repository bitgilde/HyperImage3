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
 * Copyright 2006-2012 Humboldt-Universitaet zu Berlin
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
import java.util.Vector;

import javax.media.jai.PlanarImage;
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
import org.hyperimage.client.gui.dnd.ContentTransfer;
import org.hyperimage.client.gui.dnd.GroupTransferable;
import org.hyperimage.client.gui.dnd.LayerTransferable;
import org.hyperimage.client.gui.dnd.ObjectContentTransferable;
import org.hyperimage.client.gui.dnd.QuickInfoTransferable;
import org.hyperimage.client.gui.lists.LayerListCellRenderer;
import org.hyperimage.client.gui.lists.QuickInfoCell;
import org.hyperimage.client.gui.views.LayerListView;
import org.hyperimage.client.gui.views.LayerPolygonEditorView;
import org.hyperimage.client.gui.views.LayerPropertyEditorView;
import org.hyperimage.client.model.HILayer;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.util.WSImageLoaderThread;
import org.hyperimage.client.ws.GroupTypes;
import org.hyperimage.client.ws.HiBase;
import org.hyperimage.client.ws.HiBaseTypes;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiImageSizes;
import org.hyperimage.client.ws.HiLayer;
import org.hyperimage.client.ws.HiObjectContent;
import org.hyperimage.client.ws.HiQuickInfo;
import org.hyperimage.client.ws.HiView;

/**
 * @author Jens-Martin Loebel & Ulf Schoeneberg
 */
public class LayerEditor extends HIComponent implements ListSelectionListener, ActionListener {

	private LayerListView layerListView;
	private LayerPolygonEditorView polygonEditorView;
	private LayerPropertyEditorView propertyEditorView;

	private HiView view;
	private Vector<HILayer> layers;
	private int curLayerIndex = 0;

        
	
	/**
	 * This class implements the Drag and Drop handler for layer links.
	 * Only dropping a link is supported.
 	 * 
	 * Class: LinkTransferHandler
	 * @author Jens-Martin Loebel
	 *
	 */
	public class LinkTransferHandler extends TransferHandler {

		private static final long serialVersionUID = 1392755235239308382L;

		private LayerEditor editor;
		
		
		public LinkTransferHandler(LayerEditor editor) {
			this.editor = editor;
		}


//		-------- export methods ---------
		

		public int getSourceActions(JComponent c) {
		    return LINK;
		}

		protected Transferable createTransferable(JComponent c) {
			if ( layers.size() == 0 ) return null;
			return new LayerTransferable(layers.get(curLayerIndex));
		}

		protected void exportDone(JComponent c, Transferable t, int action) {
			// focus source component if drop failed
			if ( action == NONE ) HIRuntime.getGui().focusComponent(editor);
		}
		
		
		// -------- import methods ---------

		
		public boolean canImport(TransferSupport supp) {
			// focus target component if necessary
			HIRuntime.getGui().focusComponent(editor);

			boolean isLink = false;

			// no layers --> can´t import
			if ( layers.size() == 0 ) return false;

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
					
					// cannot link a layer to itself
					if ( layer.getModel().getId() == layers.get(curLayerIndex).getModel().getId() )
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
					if ( transfer.getContents().get(0).getBaseID() == layers.get(curLayerIndex).getModel().getId()
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
	
	
	
	// ------------------------------------------------------------------------------------------------

	
	
	
	/**
	 * This class implements the Drag and Drop handler for the Layer List.
 	 * As per SDD: sorting of layers is allowed, 
 	 * dragging a layer to another layer list creates a copy of that layer and it´s polygons
 	 * 
	 * Class: LayerTransferHandler
	 * @author Jens-Martin Loebel
	 *
	 */
	public class LayerTransferHandler extends TransferHandler {

		private static final long serialVersionUID = 141341015964690677L;

		private LayerEditor editor;
		
		
		public LayerTransferHandler(LayerEditor editor) {
			this.editor = editor;
		}
		
		
		
		// -------- export methods ---------


		public int getSourceActions(JComponent c) {
			return COPY_OR_MOVE+LINK;
		}

		protected Transferable createTransferable(JComponent c) {
			HILayer layer;

			layer = (HILayer) ((JList)c).getSelectedValue();

			return new LayerTransferable(layer);
		}

		protected void exportDone(JComponent c, Transferable t, int action) {
			// focus source component if drop failed
			if ( action == NONE ) HIRuntime.getGui().focusComponent(editor);
		}


		// -------- import methods ---------


		public boolean canImport(TransferSupport supp) {
			// focus target component if necessary
			HIRuntime.getGui().focusComponent(editor);

			if (!supp.isDataFlavorSupported(LayerTransferable.layerFlavor))
				return false;

			// check user role
			if ( !HIRuntime.getGui().checkEditAbility(true) )
				return false;

			// extract layer to set appropriate drop action
			try {
				HILayer layer = (HILayer) supp.getTransferable().getTransferData(LayerTransferable.layerFlavor);
				// try to find layer in our list
				boolean belongsToThisList = false;
				for ( HILayer viewLayer : layers )
					if ( viewLayer.getModel().getId() == layer.getModel().getId() )
						belongsToThisList = true;
				if ( belongsToThisList )
					// if layer is part of this layer list, sort layers --> MOVE action
					supp.setDropAction(MOVE);
				else
					// layer belongs to another layer list  --> COPY action
					supp.setDropAction(COPY);
				
				return true;
			} catch (UnsupportedFlavorException e) {
				// no layer or other error occurred
				return false;
			} catch (IOException e) {
				// no layer or other error occurred
				return false;
			}		
		}

		public boolean importData(TransferSupport supp) {
			if (!canImport(supp)) // check if we support this element
				return false;

			// check user role
			if ( !HIRuntime.getGui().checkEditAbility(false) )
				return false;

			// Fetch the Transferable and its data
			Transferable t = supp.getTransferable();
			HILayer layer = null;
			try {
				layer =  (HILayer) t.getTransferData(LayerTransferable.layerFlavor);
				if ( layer == null ) return false;
				
				// Fetch the drop location
				int index = layerListView.getLayerList().locationToIndex(supp.getDropLocation().getDropPoint());
				index = Math.max(0, index);
				index = Math.min(layers.size()-1, index);

				// try to find layer in our list
				int listIndex = -1; // index of layer in our list
				for ( int i=0; i < layers.size(); i++ )
					if ( layers.get(i).getModel().getId() == layer.getModel().getId() )
						listIndex = i;

				if ( listIndex >= 0 ) {
					// sort layers
					HILayer listLayer = layers.elementAt(listIndex);
					layers.remove(listIndex);
					layers.add(index, listLayer);
					layerListView.updateLayerList();
					layerListView.getLayerList().setSelectedIndex(index);
					polygonEditorView.repaintShapes();
					// update sort order on server
					updateSortOrder();
					// propagate changes
					HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, view, editor);
					
				} else {
					// create a copy of this layer in our list
					try {
						HIRuntime.getGui().startIndicatingServiceActivity();
						// create a new layer
						HiLayer newLayer = HIRuntime.getManager().createLayer(
								view, 
								layer.getModel().getRed(), 
								layer.getModel().getGreen(), 
								layer.getModel().getBlue(), 
								layer.getModel().getOpacity()
						);
						view.getLayers().add(newLayer); // update model
						
						// set a meaningful title
						String title = MetadataHelper.findValue("HIBase", "title",  //$NON-NLS-1$ //$NON-NLS-2$
								MetadataHelper.getDefaultMetadataRecord(
										layer.getModel(), 
										HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()
										)
								);
						if ( title == null || title.length() == 0 )
							title = Messages.getString("LayerEditor.2")+" L"+layer.getModel().getId(); //$NON-NLS-1$ //$NON-NLS-2$
						else title = Messages.getString("LayerEditor.4")+" "+title; //$NON-NLS-1$ //$NON-NLS-2$

						// update title on server
						MetadataHelper.setValue("HIBase", "title", title,  //$NON-NLS-1$ //$NON-NLS-2$
								MetadataHelper.getDefaultMetadataRecord(
										newLayer, 
										HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()
										)
								);
						HIRuntime.getManager().updateFlexMetadataRecord(
								MetadataHelper.getDefaultMetadataRecord(
										newLayer, 
										HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()
										));
						
						// copy polygons to new layer and update on server
						newLayer.setPolygons(layer.getModel().getPolygons());
						HIRuntime.getManager().updateLayerProperties(
								newLayer, 
								newLayer.getRed(), 
								newLayer.getGreen(), 
								newLayer.getBlue(),
								newLayer.getOpacity(), 
								newLayer.getPolygons()
						);
						
						// add to view / model
						layers.add(index, 
								new HILayer(
										newLayer, 
										polygonEditorView.getScaleX(), 
										polygonEditorView.getScaleY()
									)
						);
						
						// update sort order on server
						updateSortOrder();

						// update GUI
						HIRuntime.getGui().stopIndicatingServiceActivity();
						updateLayerGUI();
						polygonEditorView.updateScale();
						polygonEditorView.repaintShapes();
												
						// propagate changes
						HIRuntime.getGui().sendMessage(HIMessageTypes.CHILD_ADDED, view, editor);
						HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_ADDED, newLayer, editor);
						HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, view, editor);
						
					} catch (HIWebServiceException wse) {
						HIRuntime.getGui().reportError(wse, editor);
						return false;
					}
				}


				return true;

				
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
	}
	
	
	
	
	// --------------------------------------------------------------------------------------------------
	

	

	public LayerEditor(HiView view, PlanarImage image, long layerID) {

		super(Messages.getString("LayerEditor.8")+" ("+
                        (view.getUUID() == null ? "V"+view.getId() : view.getUUID())+
                        ")", Messages.getString("LayerEditor.11")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		this.view = view;

		// wrap model layers
		layers = new Vector<HILayer>();
		for ( HiLayer layer : view.getLayers() )
			layers.addElement(new HILayer(
					layer,
					image.getWidth(),
					image.getHeight()));
		
		sortLayers(); // sort layers

		// init views
		layerListView = new LayerListView(layers, HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());

		polygonEditorView = new LayerPolygonEditorView(layers, image, HIRuntime.getManager().getProjectPolygons());
		polygonEditorView.regenerateNewPolygonMenu(this);
		polygonEditorView.getSwitchToPolygonLayerItem().addActionListener(this);

		propertyEditorView = new LayerPropertyEditorView(this, HIRuntime.getManager().getProjectColors(), HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId(), layerListView.getLayerList());
		propertyEditorView.setSyncView(polygonEditorView);
		if ( layers.size() == 0 ) {
			propertyEditorView.setLayer(null);
			polygonEditorView.setUserSelectedLayer(null);
		} else {
			int userLayerIndex = 0;
			if ( layerID > 0 ) {
				// try to find user requested layer in list
				userLayerIndex = displayLayerByID(layerID);
				if ( userLayerIndex < 0 ) userLayerIndex = 0;
			} 

			curLayerIndex = userLayerIndex;
			propertyEditorView.setLayer(layers.get(userLayerIndex));
			polygonEditorView.setUserSelectedLayer(layers.get(userLayerIndex));
			if ( layers.get(userLayerIndex).getModel().getLinkInfo() != null )
				loadLinkPreviewImage(layers.get(userLayerIndex).getModel().getLinkInfo(), propertyEditorView.getLayerLinkPreview());
		}

		updateSortOrder(); // sync sort order with server

		// register views
		views.add(layerListView);
		views.add(polygonEditorView);
                views.add(propertyEditorView);
		
		// init Drag and Drop handler
		layerListView.getLayerList().setTransferHandler(new LayerTransferHandler(this));
		propertyEditorView.setLinkTransferHandler(new LinkTransferHandler(this));

		// attach listeners
		layerListView.getLayerList().addListSelectionListener(this);
		layerListView.attachActionListeners(this);
		polygonEditorView.getAddToLibraryItem().addActionListener(this);
		polygonEditorView.getAddLayerItem().addActionListener(this);
		propertyEditorView.getSaveButton().addActionListener(this);
		propertyEditorView.getResetButton().addActionListener(this);
		propertyEditorView.getAddToLibraryButton().addActionListener(this);
		propertyEditorView.setPopupMenuListeners(this);

                // attach context menu listener
		layerListView.getLayerList().addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) { }
			public void mousePressed(MouseEvent e) {
				if ( e.getSource() == layerListView.getLayerList() && e.isPopupTrigger() && !e.isConsumed() ) {
					e.consume();
					layerListView.showPopupMenu(e.getX(), e.getY());
				}
			}
			public void mouseReleased(MouseEvent e) {
				if ( e.getSource() == layerListView.getLayerList() && e.isPopupTrigger() && !e.isConsumed() ) {
					e.consume();
					layerListView.showPopupMenu(e.getX(), e.getY());
				}
			}
		});
                        }

	
	
	public LayerEditor(HiView view, PlanarImage image) {
		this(view, image, -1);
	}

	
	public void updateLanguage() {
		super.updateLanguage();
		polygonEditorView.regenerateNewPolygonMenu(this);
		updateLayerGUI();
		
		setTitle(Messages.getString("LayerEditor.8")+" (V"+view.getId()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
		getWindowMenuItem().setText(getTitle());
		HIRuntime.getGui().updateComponentTitle(this);

	}
	
	public HiBase getBaseElement() {
		return this.view;
	}
	
	

	public boolean requestClose() {
		// save polygon changes
		polygonEditorView.cleanupPolygons(); // remove all polygons with < 3 anchor points first
		
		for ( HILayer layer : layers ) {
			HiLayer model = layer.getModel();
			if ( layer.hasPolygonChanges() && HIRuntime.getGui().checkEditAbility(true) ) {
				// update layer and polygons
				layer.syncPolygonChanges();
				try {
					HIRuntime.getGui().startIndicatingServiceActivity();
					HIRuntime.getManager().updateLayerProperties(
							model.getId(), model.getRed(), model.getGreen(), model.getBlue(), model.getOpacity(), model.getPolygons() );
                                        model.setTimestamp(new Date().getTime());
					HIRuntime.getGui().stopIndicatingServiceActivity();
				} catch (HIWebServiceException wse) {
					HIRuntime.getGui().reportError(wse, this);
					return true;
				}

				// propagate changes
                                propertyEditorView.updateContent();
                                HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, layer.getModel(), this);
			}
		}
		return askToSaveOrCancelChanges();
	}
	
	
	
	public void receiveMessage(HIMessageTypes message, HiBase base ) {
		// respond to PREFERENCE_MODIFIED messages
		if ( message == HIMessageTypes.PREFERENCE_MODIFIED ) {
			polygonEditorView.regenerateNewPolygonMenu(this);
			propertyEditorView.regenerateProjectColors();
		}

		// respond to DEFAULT_LANGUAGE_CHANGED
		if ( message == HIMessageTypes.DEFAULT_LANGUAGE_CHANGED ) {
			// reload link info
			for ( HILayer layer : layers ) 
				if ( layer.getModel().getLinkInfo() != null ) {
					HIRuntime.getGui().startIndicatingServiceActivity();
					try {
						layer.getModel().setLinkInfo(HIRuntime.getManager().getBaseQuickInfo(layer.getModel().getLinkInfo().getBaseID()));
                                                layer.getModel().setTimestamp(new Date().getTime());
					} catch (HIWebServiceException wse) {
						HIRuntime.getGui().reportError(wse, this);
					}
					HIRuntime.getGui().stopIndicatingServiceActivity();
				}
			// update link GUI
			if ( layers.size() > 0 ) {
				propertyEditorView.updateLayerLink();
				loadLinkPreviewImage(((HILayer)layerListView.getLayerList().getSelectedValue()).getModel().getLinkInfo(), propertyEditorView.getLayerLinkPreview());
			}
			// refresh layer list
			((LayerListCellRenderer)layerListView.getLayerList().getCellRenderer()).setDefaultLang(HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());
			layerListView.getLayerList().repaint();
                        propertyEditorView.updateContent();
		}

		
		// respond to ENTITY_CHANGED messages
		if ( message == HIMessageTypes.ENTITY_CHANGED && layers.size() > 0 ) {
			if ( base instanceof HiLayer && base.getId() == layers.get(curLayerIndex).getModel().getId() ) {
				layers.get(curLayerIndex).getModel().setLinkInfo((((HiLayer)base).getLinkInfo()));
				propertyEditorView.updateLayerLink();
                                propertyEditorView.updateContent();
				if ( ((HiLayer)base).getLinkInfo() != null ) loadLinkPreviewImage(((HiLayer)base).getLinkInfo(), propertyEditorView.getLayerLinkPreview());
			}
			
			// reload quick info
			try {
				for ( int i=0; i < layers.size(); i++ ) {
					if ( layers.get(i).getModel().getLinkInfo() != null && layers.get(i).getModel().getLinkInfo().getBaseID() == base.getId() ) {
						HiQuickInfo info = HIRuntime.getManager().getBaseQuickInfo(base);
						layers.get(i).getModel().setLinkInfo(info);
						if ( i == curLayerIndex ) {
							propertyEditorView.updateLayerLink();
                                                        propertyEditorView.updateContent();
							loadLinkPreviewImage(info, propertyEditorView.getLayerLinkPreview());
						}
					}
				}
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
		}
		
		if ( message == HIMessageTypes.LANGUAGE_ADDED || message == HIMessageTypes.LANGUAGE_REMOVED ) {	
			// reload metadata for all layers
			HIRuntime.getGui().startIndicatingServiceActivity();
			try {
				for ( HiLayer layer : view.getLayers() ) {
					List<HiFlexMetadataRecord> records = HIRuntime.getManager().getFlexMetadataRecords(layer);
					while ( layer.getMetadata().size() > 0 ) layer.getMetadata().remove(0);
					for ( HiFlexMetadataRecord record : records )
						layer.getMetadata().add(record);
				}
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
			HIRuntime.getGui().stopIndicatingServiceActivity();

			// update GUI
			propertyEditorView.updateMetadataLanguages();
		}

	}
	
	
	
	// --------------------------------------------------------------------------------------------------
	
	
	
	public int displayLayerByID(long id) {
		for ( int i = 0 ; i < layerListView.getLayerList().getModel().getSize() ; i++ )
			if ( ((HILayer)layerListView.getLayerList().getModel().getElementAt(i)).getModel().getId() == id )
				layerListView.getLayerList().setSelectedIndex(i);
		// scroll to new selected index
		layerListView.getLayerList().scrollRectToVisible(
				new Rectangle(
						layerListView.getLayerList().indexToLocation(layerListView.getLayerList().getSelectedIndex())
				));
		return layerListView.getLayerList().getSelectedIndex();
	}
	

	private void savePropertyChanges() {
		if ( layers.size() == 0 ) return;
		HILayer layer = layers.get(curLayerIndex);

		if ( !HIRuntime.getGui().checkEditAbility(false) )
			return;
		
		try {
			HiLayer model = layer.getModel();
			if ( layer.hasChanges() || layer.hasPolygonChanges() ) {
				// update layer and polygons
				layer.syncChanges();
				layer.syncPolygonChanges();
				HIRuntime.getGui().startIndicatingServiceActivity();
				HIRuntime.getManager().updateLayerProperties(
						model, model.getRed(), model.getGreen(), model.getBlue(), model.getOpacity(), model.getPolygons() );
                                model.setTimestamp(new Date().getTime());
				HIRuntime.getGui().stopIndicatingServiceActivity();
				// propagate changes
                                propertyEditorView.updateContent();
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, layer.getModel(), this);
			}
		} catch (HIWebServiceException wse) {
			HIRuntime.getGui().reportError(wse, this);
			return;
		}		}

	private void saveMetadataChanges() {
		if ( layers.size() == 0 ) return;
		HILayer layer = layers.get(curLayerIndex);

		if ( !HIRuntime.getGui().checkEditAbility(false) )
			return;
		
		try {
			HIRuntime.getGui().startIndicatingServiceActivity();
			HiLayer model = layer.getModel();
			if ( propertyEditorView.hasMetadataChanges() ) {
				// update layer metadata
				propertyEditorView.syncMetadataChanges();
				HIRuntime.getManager().updateFlexMetadataRecords(
						model.getMetadata());
                                model.setTimestamp(new Date().getTime());

				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, layer.getModel(), this);
				// update GUI
                                propertyEditorView.updateContent();
				layerListView.repaint();
				HIRuntime.getGui().stopIndicatingServiceActivity();
			}
		} catch (HIWebServiceException wse) {
			HIRuntime.getGui().reportError(wse, this);
			return;
		}		
	}

	private boolean setLink(long targetID) {
		// check user role
		if ( ! HIRuntime.getGui().checkEditAbility(false) )
			return false;

		try {
			HIRuntime.getGui().startIndicatingServiceActivity();
			if ( HIRuntime.getManager().setLayerLink(layers.get(curLayerIndex).getModel().getId(), targetID) ) {
				HiQuickInfo info = HIRuntime.getManager().getBaseQuickInfo(targetID);
				layers.get(curLayerIndex).getModel().setLinkInfo(info);
                                layers.get(curLayerIndex).getModel().setTimestamp(new Date().getTime());
                                
				// update GUI
				propertyEditorView.updateLayerLink();
                                propertyEditorView.updateContent();
				loadLinkPreviewImage(info, propertyEditorView.getLayerLinkPreview());
				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, layers.get(curLayerIndex).getModel(), this);
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, view, this);
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
	
	private void removeLink() {
		// check user role
		if ( ! HIRuntime.getGui().checkEditAbility(false) )
			return;

		try {
			HIRuntime.getGui().startIndicatingServiceActivity();
			if ( HIRuntime.getManager().removeLayerLink(layers.get(curLayerIndex).getModel().getId()) ) {
				layers.get(curLayerIndex).getModel().setLinkInfo(null);
	                        layers.get(curLayerIndex).getModel().setTimestamp(new Date().getTime());
                                // update GUI
				propertyEditorView.updateLayerLink();
                                propertyEditorView.updateContent();
				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, layers.get(curLayerIndex).getModel(), this);
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, view, this);
			}
			HIRuntime.getGui().stopIndicatingServiceActivity();
			
		} catch (HIWebServiceException wse) {
			HIRuntime.getGui().reportError(wse, this);
			return;
		}
		
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
				thumbLoader.loadImage(viewID, cacheImage, HiImageSizes.HI_THUMBNAIL, cell, propertyEditorView);
			}
		}
	}

	
    private boolean askToSaveOrCancelChanges() {
        if (layers.size() == 0) return true;

        HILayer layer = layers.get(curLayerIndex);

        if ((propertyEditorView.hasMetadataChanges() || layer.hasChanges()) && HIRuntime.getGui().checkEditAbility(true)) {
            int decision = JOptionPane.showConfirmDialog(
                    HIRuntime.getGui(),
                    Messages.getString("LayerEditor.12")); //$NON-NLS-1$

            if (decision == JOptionPane.CANCEL_OPTION) {
                return false;
            } else if (decision == JOptionPane.YES_OPTION) {
                saveMetadataChanges();
                savePropertyChanges();
            } else {
                layer.resetChanges(); // undo color / opacity changes
            }
        }
        return true;
    }

	private void updateSortOrder() {
		String sortOrder = layerListView.getSortOrder();

		// check user role
		if ( !HIRuntime.getGui().checkEditAbility(true) )
			return;

		if ( view.getSortOrder().compareTo(sortOrder) != 0 ) {
			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				HIRuntime.getManager().updateViewSortOrder(view, sortOrder);
				HIRuntime.getGui().stopIndicatingServiceActivity();
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
		}
	}
	
	private void sortLayers() {
		long layerID;
		int index = 0;
		int contentIndex;

		// parse sort order string (don´t trust user input)
		for ( String contentIDString : view.getSortOrder().split(",") ) { //$NON-NLS-1$
			try {
				layerID = Long.parseLong(contentIDString);
				// find content belonging to the parsed id
				contentIndex = -1;
				
				for ( int i=0; i<layers.size(); i++ )
					if ( layers.get(i).getModel().getId() == layerID )
						contentIndex = i;
				// if content was found, sort to new index
				if ( contentIndex >= 0 ) if ( contentIndex != index ) {
					HILayer layer = layers.get(contentIndex);
					layers.remove(contentIndex);
					layers.add(index, layer);
					index = index+1;
				} else index = index + 1;
			} catch (NumberFormatException e) {}; // user messed with sort order string format, no problem
		}
	}


	private void updateLayerGUI() {
		layerListView.getLayerList().removeListSelectionListener(this);
		layerListView.updateLayerList();
		layerListView.getLayerList().addListSelectionListener(this);
		curLayerIndex = layerListView.getLayerList().getSelectedIndex();
		if ( layers.size() > 0 && curLayerIndex == -1 ) {
			curLayerIndex = 0;
			layerListView.getLayerList().setSelectedIndex(0);
		}
		if ( curLayerIndex >=0 ) {
			propertyEditorView.setLayer(layers.get(curLayerIndex));
			polygonEditorView.setUserSelectedLayer(layers.get(curLayerIndex));
			if ( layers.get(curLayerIndex).getModel().getLinkInfo() != null )
				loadLinkPreviewImage(layers.get(curLayerIndex).getModel().getLinkInfo(), propertyEditorView.getLayerLinkPreview());
		} else {
			propertyEditorView.setLayer(null);
			polygonEditorView.setUserSelectedLayer(null);
		}
		polygonEditorView.updateMenuOptions();
	}


	// ------------------------------------------------------------------------


	public void valueChanged(ListSelectionEvent e) {
		DefaultListModel model = (DefaultListModel) layerListView.getLayerList().getModel();
		if ( model.size() > 0 ) {
			// can´t deselect the layer list
			if ( layerListView.getLayerList().getSelectedIndex() == -1 ) {
				layerListView.getLayerList().setSelectedIndex(curLayerIndex);
				return;
			}

			if ( curLayerIndex != layerListView.getLayerList().getSelectedIndex() ) {

				if ( ! askToSaveOrCancelChanges() ) {
					layerListView.getLayerList().setSelectedIndex(curLayerIndex);
					return;
				}

				curLayerIndex = layerListView.getLayerList().getSelectedIndex();
				propertyEditorView.setLayer(layers.get(curLayerIndex));
				polygonEditorView.setUserSelectedLayer(layers.get(curLayerIndex));
				if ( layers.get(curLayerIndex).getModel().getLinkInfo() != null ) 
					loadLinkPreviewImage(layers.get(curLayerIndex).getModel().getLinkInfo(), propertyEditorView.getLayerLinkPreview());
			}
		} else {
			propertyEditorView.setLayer(null);
			polygonEditorView.setUserSelectedLayer(null);
		}
	}



	// ------------------------------------------------------------------------


       
	public void actionPerformed(ActionEvent e) {

		// add polygon to library
		if ( e.getActionCommand().equalsIgnoreCase("TO_LIBRARY") ) { //$NON-NLS-1$
			// check user role
			if ( !HIRuntime.getGui().checkEditAbility(false) )
				return;

			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				HIRuntime.getManager().addProjectPolygon(polygonEditorView.getSelectedPolygon().getChangedModel());
				polygonEditorView.regenerateNewPolygonMenu(this);
				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.PREFERENCE_MODIFIED, null, this);
				HIRuntime.getGui().stopIndicatingServiceActivity();
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
		}
		
		// switch to active polygon layer
		if ( e.getActionCommand().equalsIgnoreCase("SWITCH_TO_ACTIVE_LAYER") ) { //$NON-NLS-1$
			if ( layers.size() == 0 ) return;
			HILayer selectedLayer = polygonEditorView.getSelectedLayer();
			if ( selectedLayer != null )
				layerListView.getLayerList().setSelectedValue(selectedLayer, true);
		}

		// add color to library
		if ( e.getActionCommand().equalsIgnoreCase("colorToLibrary") ) { //$NON-NLS-1$
			// check user role
			if ( !HIRuntime.getGui().checkEditAbility(false) )
				return;

			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				HIRuntime.getManager().addProjectColor(layers.get(curLayerIndex).getColour());
				propertyEditorView.regenerateProjectColors();
//				propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.PREFERENCE_MODIFIED, null, this);
				HIRuntime.getGui().stopIndicatingServiceActivity();
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
		}
		}
		
		
		if ( e.getActionCommand().equalsIgnoreCase("visitLink") ) { //$NON-NLS-1$
			if ( layers.size() == 0 ) return;
			
			if ( layers.get(curLayerIndex).getModel().getLinkInfo() != null )
				HIRuntime.getGui().openContentEditor(layers.get(curLayerIndex).getModel().getLinkInfo(), this);
		}

		if ( e.getActionCommand().equalsIgnoreCase("removeLink") ) { //$NON-NLS-1$
			if ( layers.size() == 0 ) return;
			// check user role
			if ( !HIRuntime.getGui().checkEditAbility(false) )
				return;
			removeLink();
		}
		
		if ( e.getActionCommand().equalsIgnoreCase("add") ) { //$NON-NLS-1$

			// check user role
			if ( !HIRuntime.getGui().checkEditAbility(false) )
				return;

			// DEBUG
			// add a new layer (service request)
			HIRuntime.getGui().startIndicatingServiceActivity();
			try {
				HiLayer newLayer = HIRuntime.getManager().createLayer(view, 0, 156, 215, 0.5f);
				view.getLayers().add(newLayer); // update model
				// propagate changes
				HIRuntime.getGui().sendMessage(HIMessageTypes.CHILD_ADDED, view, this);
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_ADDED, newLayer, this);
				HILayer newHILayer = new HILayer(
						newLayer, 
						polygonEditorView.getScaleX(), 
						polygonEditorView.getScaleY());
				layers.add(newHILayer);
				// update sort order
				updateSortOrder();
				// update GUI
				updateLayerGUI();
				// select new layer
				layerListView.getLayerList().setSelectedValue(newHILayer, true);
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
			HIRuntime.getGui().stopIndicatingServiceActivity();


		} else if( e.getActionCommand().equalsIgnoreCase("remove") ) { //$NON-NLS-1$
			if ( layerListView.getLayerList().getModel().getSize() > 0 && ( layerListView.getLayerList().getSelectedIndex() >= 0 ) ) {

				// check user role
				if ( !HIRuntime.getGui().checkEditAbility(false) )
					return;

				// warn user
				if ( ! HIRuntime.getGui().displayUserYesNoDialog(
						Messages.getString("LayerEditor.21"), //$NON-NLS-1$
						Messages.getString("LayerEditor.22")+"\n\n"+Messages.getString("LayerEditor.24")) ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return;
				
				// remove selected layer (service request)
				HIRuntime.getGui().startIndicatingServiceActivity();

				HILayer layer = layers.get(layerListView.getLayerList().getSelectedIndex());
				if ( layer != null ) {
					try {
						HIRuntime.getManager().removeLayer(layer.getModel().getId());
						layers.remove(layer);
						view.getLayers().remove(layer.getModel()); // update model

						// update sort order
						updateSortOrder();

						// propagate changes
						HIRuntime.getGui().sendMessage(HIMessageTypes.CHILD_REMOVED, view, this);
						HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_REMOVED, layer.getModel(), this);
					} catch (HIWebServiceException wse) {
						HIRuntime.getGui().reportError(wse, this);
						return;
					}
				}

				// update GUI
				updateLayerGUI();
				HIRuntime.getGui().stopIndicatingServiceActivity();

			}
		}

                // check if view contains any layers. if not create one on the fly to add a shape to.
                if ( (layers.size() == 0) && (e.getActionCommand().startsWith("ADD_")) ) {
                    HIRuntime.getGui().startIndicatingServiceActivity();
                    try {
                        HiLayer newLayer = HIRuntime.getManager().createLayer(view, 0, 156, 215, 0.5f);
                        view.getLayers().add(newLayer); // update model
                        // propagate changes
                        HIRuntime.getGui().sendMessage(HIMessageTypes.CHILD_ADDED, view, this);
                        HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_ADDED, newLayer, this);
                        HILayer newHILayer = new HILayer(
                                newLayer,
                                polygonEditorView.getScaleX(),
                                polygonEditorView.getScaleY());
                        layers.add(newHILayer);
                        // update sort order
                        updateSortOrder();
                        // update GUI
                        updateLayerGUI();
                        // select new layer
                        layerListView.getLayerList().setSelectedValue(newHILayer, true);
                    } catch (HIWebServiceException wse) {
                        HIRuntime.getGui().reportError(wse, this);
                        return;
                    }
                    HIRuntime.getGui().stopIndicatingServiceActivity();
                }


		if ( (layers.size() > 0) && (layerListView.getLayerList().getSelectedIndex()!=-1) ) {
			HILayer layer = layers.get(curLayerIndex);

                        
			// adding polygons
			if ( e.getActionCommand().startsWith("ADD_") ) {
				if ( e.getActionCommand().equalsIgnoreCase("ADD_FREEFORM") ) {
                                    if ( polygonEditorView.polygonEditorViewCommand.equalsIgnoreCase("deselect") ) {
                                        polygonEditorView.polygonEditorViewCommand="";
                                        return;
                                    }
                                    polygonEditorView.polygonEditorViewCommand = "add_freeform";
                                    polygonEditorView.setPlaceFirstAnchorMode(true);
                                    polygonEditorView.updateToolbarButtons();
                                }
				else if ( e.getActionCommand().equalsIgnoreCase("ADD_RECTANGLE") ) {
                                    if ( polygonEditorView.polygonEditorViewCommand.equalsIgnoreCase("deselect") ) {
                                        polygonEditorView.polygonEditorViewCommand="";
                                        return;
                                    }
                                    polygonEditorView.polygonEditorViewCommand = "add_rectangle";
                                    polygonEditorView.setPlaceFirstAnchorMode(true);
                                    polygonEditorView.updateToolbarButtons();
                                }
                                else if ( e.getActionCommand().equalsIgnoreCase("ADD_FREE") ) {
                                    if ( polygonEditorView.polygonEditorViewCommand.equalsIgnoreCase("deselect") ) {
                                        polygonEditorView.polygonEditorViewCommand="";
                                        return;
                                    }
                                    polygonEditorView.polygonEditorViewCommand = "add_free";
                                    polygonEditorView.setPlaceFirstAnchorMode(true);
                                    polygonEditorView.updateToolbarButtons();
                                }
                                else if ( e.getActionCommand().equalsIgnoreCase("ADD_CIRCLE") ) {
                                    if ( polygonEditorView.polygonEditorViewCommand.equalsIgnoreCase("deselect") ) {
                                        polygonEditorView.polygonEditorViewCommand="";
                                        return;
                                    }
                                    polygonEditorView.polygonEditorViewCommand = "add_circle";
                                    polygonEditorView.setPlaceFirstAnchorMode(true);
                                    polygonEditorView.updateToolbarButtons();
                                }
                                else if ( e.getActionCommand().equalsIgnoreCase("ADD_ARROW") ) {
                                    if ( polygonEditorView.polygonEditorViewCommand.equalsIgnoreCase("deselect") ) {
                                        polygonEditorView.polygonEditorViewCommand="";
                                        return;
                                    }
                                    polygonEditorView.polygonEditorViewCommand = "add_arrow";
                                    polygonEditorView.setPlaceFirstAnchorMode(true);
                                    polygonEditorView.updateToolbarButtons();
                                }
                                else if ( e.getActionCommand().startsWith("ADD_LIB_") && e.getActionCommand().length() > 7 ) {
					try {
						int lib = Integer.parseInt(e.getActionCommand().substring(8));
						if ( lib >=0 && lib < HIRuntime.getManager().getProjectPolygons().size() )
							polygonEditorView.addLibraryPolygon(layer, HIRuntime.getManager().getProjectPolygons().get(lib));
					} catch (NumberFormatException nfe) {};
				}

			} else  // saving metadata
        
				if ( e.getActionCommand().equalsIgnoreCase("save") ) //$NON-NLS-1$
					if ( layer.hasChanges() || propertyEditorView.hasMetadataChanges() || layer.hasPolygonChanges() ) {

						// check user role
						if ( !HIRuntime.getGui().checkEditAbility(false) )
							return;
						
						saveMetadataChanges();
						savePropertyChanges();
					}

			if ( e.getActionCommand().equalsIgnoreCase("reset") ) { //$NON-NLS-1$
				layer.resetChanges();
				layer.resetPolygonChanges();
				propertyEditorView.resetMetadataChanges();
				polygonEditorView.repaintShapes();
				propertyEditorView.setLayer(layer);
				if ( layer.getModel().getLinkInfo() != null ) 
					loadLinkPreviewImage(layer.getModel().getLinkInfo(), propertyEditorView.getLayerLinkPreview());

			}
		}
	}

}
