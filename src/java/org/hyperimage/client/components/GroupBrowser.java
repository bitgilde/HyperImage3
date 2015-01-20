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

import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.SeekableStream;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.HIWebServiceManager;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.dnd.ContentTransfer;
import org.hyperimage.client.gui.dnd.GroupTransferable;
import org.hyperimage.client.gui.dnd.LayerTransferable;
import org.hyperimage.client.gui.dnd.ObjectContentTransferable;
import org.hyperimage.client.gui.dnd.QuickInfoTransferable;
import org.hyperimage.client.gui.dnd.RepositoryTransfer;
import org.hyperimage.client.gui.dnd.RepositoryTransferable;
import org.hyperimage.client.gui.lists.GroupContentsCellRenderer;
import org.hyperimage.client.gui.lists.GroupContentsList;
import org.hyperimage.client.gui.lists.GroupContentsList.HI_ListDisplayStyles;
import org.hyperimage.client.gui.lists.GroupListCellRenderer;
import org.hyperimage.client.gui.lists.QuickInfoCell;
import org.hyperimage.client.gui.views.GroupContentsView;
import org.hyperimage.client.gui.views.GroupListView;
import org.hyperimage.client.gui.views.GroupPropertyEditorView;
import org.hyperimage.client.model.HILayer;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.util.WSImageLoaderThread;
import org.hyperimage.client.ws.GroupTypes;
import org.hyperimage.client.ws.HiBase;
import org.hyperimage.client.ws.HiBaseTypes;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;
import org.hyperimage.client.ws.HiGroup;
import org.hyperimage.client.ws.HiImageSizes;
import org.hyperimage.client.ws.HiObject;
import org.hyperimage.client.ws.HiObjectContent;
import org.hyperimage.client.ws.HiPreference;
import org.hyperimage.client.ws.HiQuickInfo;
import org.hyperimage.client.ws.HiView;
import org.hyperimage.connector.fedora3.ws.HiMetadataRecord;
import org.hyperimage.connector.fedora3.ws.HiTypedDatastream;

/**
 * @author Jens-Martin Loebel
 */
public class GroupBrowser extends HIComponent 
	implements ListSelectionListener, ActionListener, MouseListener {

	private GroupListView groupListView;
	private GroupContentsView groupContentsView;
	private GroupPropertyEditorView metadataEditorView;
	private HIGroupContentLoader contentLoader;
	private HIComponent browser = this;
	private HI_ListDisplayStyles userSelectedStyle = HI_ListDisplayStyles.ICON_STYLE;

	private GroupContentsTransferHandler groupContentsHandler;
	
    /**
     * This class implements the Drag and Drop handler for the Group List. As
     * per SDD: only dragging a group into the group contents folder, creating a
     * link of this group in another group, is supported.
     *
     * Class: GroupTransferHandler
     *
     * @author Jens-Martin Loebel
     *
     */
    public class GroupTransferHandler extends TransferHandler {

        private static final long serialVersionUID = -5952319035170447213L;

        private GroupBrowser browser;

        public GroupTransferHandler(GroupBrowser browser) {
            this.browser = browser;
        }

        // -------- export methods ---------
        public int getSourceActions(JComponent c) {
            return LINK;
        }

        protected Transferable createTransferable(JComponent c) {
            HiGroup group;

            group = (HiGroup) ((JList) c).getSelectedValue();

            return new GroupTransferable(group);
        }

        protected void exportDone(JComponent c, Transferable t, int action) {
            // focus source component if drop failed
            if (action == NONE) {
                HIRuntime.getGui().focusComponent(browser);
            }
        }

		// -------- import methods ---------
        public boolean canImport(TransferSupport supp) {
            // focus target component if necessary
            HIRuntime.getGui().focusComponent(browser);

            // check user role
            if (!HIRuntime.getGui().checkEditAbility(true)) {
                return false;
            }

            if (supp.isDataFlavorSupported(GroupTransferable.groupFlavor)) {
                // groups can be sorted or put into another group as a link to that group
                groupListView.getList().setDropMode(DropMode.ON_OR_INSERT);

                // check group
                try {
                    HiGroup group = (HiGroup) supp.getTransferable().getTransferData(GroupTransferable.groupFlavor);
                    // can´t drag import or trash group
                    if (group.getType() != GroupTypes.HIGROUP_REGULAR) {
                        return false;
                    }
                } catch (UnsupportedFlavorException e) {
                    return false; // group empty or error occurred
                } catch (IOException e) {
                    return false; // group empty or error occurred
                }
                return true;

            } else if (supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor)
                    || supp.isDataFlavorSupported(LayerTransferable.layerFlavor)
                    || supp.isDataFlavorSupported(ObjectContentTransferable.objecContentFlavor)) {
                // everything else can only be put into a group
                groupListView.getList().setDropMode(DropMode.ON);
                if (!supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor)) {
                    supp.setDropAction(LINK);
                }
                return true;
            }

            return false;
        }

        public boolean importData(TransferSupport supp) {
            if (!canImport(supp)) // check if we can import this
                return false;

            // check user role
            if (!HIRuntime.getGui().checkEditAbility(false)) return false;

            if (!supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor))
                supp.setDropAction(LINK);

            /* *****
             * handle group list sort operations
             * *****/
            if (groupListView.getList().getDropLocation() != null
                    && groupListView.getList().getDropLocation().isInsert()) {
                // determine insert index
                int insertIndex = groupListView.getList().locationToIndex(supp.getDropLocation().getDropPoint());
                // cannot drag past import or trash group
                if (insertIndex == 0 || insertIndex == (groupListView.getList().getModel().getSize() - 1))
                    return false;

                // extract group to sort
                HiGroup sortGroup;
                try {
                    sortGroup = (HiGroup) supp.getTransferable().getTransferData(GroupTransferable.groupFlavor);
                    if (sortGroup == null) return false;
                } catch (UnsupportedFlavorException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                }

                DefaultListModel model = (DefaultListModel) groupListView.getList().getModel();
                int moveIndex = -1;
                for (int i = 1; i < (model.getSize() - 1); i++)
                    if (((HiGroup) model.get(i)).getId() == sortGroup.getId())
                        moveIndex = i;

                // nothing to do or group not found
                if (moveIndex < 0 || moveIndex == insertIndex) return false;

                // sort list
                groupListView.getList().removeListSelectionListener(browser);
                sortGroup = (HiGroup) model.get(moveIndex);
                model.remove(moveIndex);
                model.insertElementAt(sortGroup, insertIndex);
                groupListView.getList().setSelectedValue(sortGroup, true);
                groupListView.getList().addListSelectionListener(browser);

                // DEBUG
                // update sort order
                HiPreference groupSortOrderPref = MetadataHelper.findPreference(HIRuntime.getManager().getProject(), "groupSortOrder"); //$NON-NLS-1$
                String sortOrder = ""; //$NON-NLS-1$
                for (int i = 1; i < (model.getSize() - 1); i++)
                    sortOrder = sortOrder + "," + ((HiGroup) model.get(i)).getId(); //$NON-NLS-1$

                if (sortOrder.startsWith(",")) sortOrder = sortOrder.substring(1);
                groupSortOrderPref.setValue(sortOrder);
                HIRuntime.getGui().startIndicatingServiceActivity();
                try {
                    HIRuntime.getManager().updatePreference(groupSortOrderPref);
                } catch (HIWebServiceException wse) {
                    HIRuntime.getGui().reportError(wse, browser);
                }
                HIRuntime.getGui().stopIndicatingServiceActivity();
                // propagate changes
                HIRuntime.getGui().sendMessage(HIMessageTypes.GROUP_SORTORDER_CHANGED, null, browser);

                return true;
            }

            return groupContentsHandler.importData(supp);
        }
    }

    // -------------------------------------------------------------------------------------------------

    /**
     * This class implements the Drag and Drop handler for group content. As per
     * SDD: sorting of group contents is allowed as well as copying and moving
     * elements to another group or to/from the trash.
     *
     * Class: GroupContentsTransferHandler
     *
     * @author Jens-Martin Loebel
     *
     */
    public class GroupContentsTransferHandler extends TransferHandler {

        private static final long serialVersionUID = -2476221044117676754L;

        private GroupBrowser browser;
        private boolean wasSortAction = false;

        public GroupContentsTransferHandler(GroupBrowser browser) {
            this.browser = browser;
        }

		// -------- export methods ---------
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE + LINK;
        }

        protected Transferable createTransferable(JComponent c) {
            Vector<HiQuickInfo> elements = new Vector<HiQuickInfo>();
            if (c instanceof GroupContentsList) {
                GroupContentsList list = (GroupContentsList) c;

                // build quick info list
                for (Object object : list.getSelectedValues()) {
                    elements.add((HiQuickInfo) object);
                }
            }
            return new QuickInfoTransferable(
                    new ContentTransfer(elements, groupListView.getCurrentGroup(), browser)
            );
        }

        protected void exportDone(JComponent c, Transferable t, int action) {
            // check user role
            if (!HIRuntime.getGui().checkEditAbility(true)) return;

            // focus source component if drop failed
            if (action == NONE) HIRuntime.getGui().focusComponent(browser);

            // check if this was a transfer of group contents
            if (t.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor)) {
                try {
                    ContentTransfer transfer = (ContentTransfer) t.getTransferData(QuickInfoTransferable.quickInfoFlavor);
                    if (!wasSortAction && action == MOVE && (transfer.getTarget() instanceof GroupBrowser)) {
                        // if this was a MOVE action from another group browser, delete moved content from source group
                        for (HiQuickInfo content : transfer.getContents()) {
                            groupContentsView.removeContent(content);
                        }

                        // update sort order
                        updateContentSortOrder();

                        // propagate changes
                        HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, groupListView.getCurrentGroup(), null);
                    }

                } catch (UnsupportedFlavorException e) {
                    return; // error occurred, nothing to do
                } catch (IOException e) {
                    return; // error occurred, nothing to do
                }
            }
        }

        // -------- import methods ---------
        private boolean createLink(HiBase base, HiGroup targetGroup, Point location) {
            // check whether this group already contains a link to the requested base element
            for (HiQuickInfo content : groupContentsView.getContents()) {
                if (content.getBaseID() == base.getId()) return true; // nothing to be done here, link already in group
            }
            try {
                HIRuntime.getGui().startIndicatingServiceActivity();
                HIRuntime.getManager().copyToGroup(base, targetGroup);
                // generate quick info
                HiQuickInfo info = HIRuntime.getManager().getBaseQuickInfo(base.getId());
                // update GUI
                int index = groupContentsView.getContentsList().locationToIndex(location);
                if (targetGroup.getId() == groupListView.getCurrentGroup().getId()) {
                    groupContentsView.addContent(info, index);
                    // update sort order
                    updateContentSortOrder();
                }
                HIRuntime.getGui().stopIndicatingServiceActivity();

                loadContentPreview(info);
                // propagate changes
                groupListView.getCurrentGroup().setTimestamp(new Date().getTime());
                metadataEditorView.updateContent();
                HIRuntime.getGui().sendMessage(HIMessageTypes.GROUP_CONTENTS_CHANGED, targetGroup, browser);
                HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, targetGroup, browser);
                return true;
            } catch (HIWebServiceException wse) {
                HIRuntime.getGui().reportError(wse, browser);
                return false;
            }
        }

        public boolean canImport(TransferSupport supp) {
            // focus target component if necessary
            HIRuntime.getGui().focusComponent(browser);

            // check user role
            if (!HIRuntime.getGui().checkEditAbility(true)) return false;

            if (!supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor)
                    && !supp.isDataFlavorSupported(LayerTransferable.layerFlavor)
                    && !supp.isDataFlavorSupported(GroupTransferable.groupFlavor)
                    && !supp.isDataFlavorSupported(ObjectContentTransferable.objecContentFlavor)
                    && !supp.isDataFlavorSupported(RepositoryTransferable.repositoryFlavor)
                    && !supp.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                return false;

            // for direct import, only support COPY action
            if (supp.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                    && groupListView.getCurrentGroup().getType() != GroupTypes.HIGROUP_TRASH) {
                supp.setDropAction(COPY);
                return true;
            }
            // only group contents may be moved/copied. Other elements create links in the selected group
            if (supp.isDataFlavorSupported(RepositoryTransferable.repositoryFlavor)) supp.setDropAction(COPY);
            else if (!supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor)) supp.setDropAction(LINK);

            // scan contents
            if (supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor)) {
                try {
                    ContentTransfer transfer = (ContentTransfer) supp.getTransferable().getTransferData(QuickInfoTransferable.quickInfoFlavor);
                    if (transfer == null) return false;

                    // if this was a search result --> only support COPY action
                    if (transfer.getSource() instanceof SearchModule) supp.setDropAction(COPY);

                } catch (UnsupportedFlavorException e) {
                    return false; // error occurred
                } catch (IOException e) {
                    return false; // error occurred
                }
            }

            // we cannot create a link to the trash or import group
            if (supp.isDataFlavorSupported(GroupTransferable.groupFlavor)) {
                try {
                    HiGroup group = (HiGroup) supp.getTransferable().getTransferData(GroupTransferable.groupFlavor);
                    if (group.getType() != GroupTypes.HIGROUP_REGULAR) {
                        return false; // not a regular group --> cannot import
                    }
                } catch (UnsupportedFlavorException e) {
                    return false; // no group to be transferred or other error
                } catch (IOException e) {
                    return false; // no group to be transferred or other error
                }
            }

            // determine target group
            HiGroup dropGroup = groupListView.getCurrentGroup();
            if (!(supp.getComponent() instanceof GroupContentsList)) 
                dropGroup = groupListView.getGroup(groupListView.getList().getDropLocation().getIndex());

            // user cant drag things into the import group
            if (dropGroup.getType() == GroupTypes.HIGROUP_IMPORT
                    && !supp.isDataFlavorSupported(RepositoryTransferable.repositoryFlavor))
                return false;

            // can only drag group contents into trash
            if (!supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor))
                if (dropGroup.getType() == GroupTypes.HIGROUP_TRASH) return false;

            return true;
        }

        @SuppressWarnings("unchecked")
        public boolean importData(TransferSupport supp) {
            wasSortAction = false; // reset action status

            if (!canImport(supp)) return false; // check if we support transfer elements

            // check user role
            if (!HIRuntime.getGui().checkEditAbility(false)) return false;

            // determine target group
            HiGroup dropGroup = groupListView.getCurrentGroup();
            if (!(supp.getComponent() instanceof GroupContentsList))
                dropGroup = groupListView.getGroup(groupListView.getList().getDropLocation().getIndex());

            /* *********************
             * support direct import
             * *********************
             * NOTE: this feature might/will be removed as soon as a viable repository connection
             * and more image source repositories become available
             * UPDATE: 	2009-02-02 [JML], as previously mentioned, this feature has now been removed
             * 			images were never intended to be imported directly, rather a source repository should be used. This is now the case.
             * UPDATE:  2009-02-05 [JML]: temporarily re-enabled direct import at Uni Lueneburg´s request
             */
            if (supp.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                    && groupListView.getCurrentGroup().getType() != GroupTypes.HIGROUP_TRASH) {
                try {
                    List<File> files = (List<File>) supp.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                    // filter file list
                    Vector<File> importFiles = new Vector<File>();
                    for (File file : files) {
                        if (!file.isDirectory() && file.canRead()) {
                            importFiles.addElement(file);
                        }
                    }

                    if (importFiles.size() < 1) return false; // no files to import

                    // check if editor file upload/import feature is disabled 
                    if (System.getProperty("HI.feature.importDisabled") != null) return false;

                    // check if project has quota and upload would exceed quota
                    long uploadSize = 0;
                    for (File file : importFiles) uploadSize += file.length();
                    if ( HIRuntime.getManager().getProject().getQuota() > 0
                         && (HIRuntime.getManager().getProject().getUsed()+uploadSize) > HIRuntime.getManager().getProject().getQuota() ) {
                         HIRuntime.getGui().displayInfoDialog(Messages.getString("Groupbrowser.quotaexceeded"), Messages.getString("Groupbrowser.quotaexceededmsg"));
                         return false;
                    }

                    // ask the user if he/she really wants to do this
                    if (!HIRuntime.getGui().displayUserYesNoDialog(Messages.getString("GroupBrowser.4"), Messages.getString("GroupBrowser.5") + " " + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            importFiles.size() + " " + Messages.getString("GroupBrowser.8"))) //$NON-NLS-1$ //$NON-NLS-2$
                    {
                        return false;
                    }

                    // detect non jpeg image files
                    boolean needConversion = false;
                    for (File file : importFiles) {
                        String mimetype = URLConnection.guessContentTypeFromName(file.getName());
                        if ( mimetype == null ) mimetype = "application/octet-stream";
                        if ( mimetype.startsWith("image/") && !mimetype.endsWith("/jpeg") ) {
                            needConversion = true;
                        }
                    }
                    // inform user that some image files need to be converted to jpeg before upload to save transfer time
                    if (needConversion) {
                        if ( !HIRuntime.getGui().displayUserYesNoDialog(Messages.getString("GroupBrowser.4"), Messages.getString("GroupBrowser.conversionInfo")) )
                        {
                            return false;
                        }
                    }

                    // import files into selected group, preserving sort order only if this is not the import group
                    HIRuntime.getGui().startIndicatingServiceActivity();
                    for (File file : importFiles) {
                        try {
                            // create an empty object wrapper for file system content
                            HiObject importObject = HIRuntime.getManager().createObject();

                            String customExtenstion = "";
                            HiView importview = null;
                            String filename="import.jpg";
                            try {
                                File importFile = file;
                                filename = file.getName();
                                String mimetype = URLConnection.guessContentTypeFromName(file.getName());
                                if ( mimetype == null ) mimetype = "application/octet-stream";
                                if ( mimetype.startsWith("image/") && !mimetype.endsWith("/jpeg") ) {
                                    // convert image to jpeg if possible                                    
                                    try {
                                        File tempFile = null;
                                        if ( System.getProperty("java.io.tmpdir") != null ) {
                                            tempFile = new File(System.getProperty("java.io.tmpdir"), "HIEditor_tempConversion.jpg");
                                        } else tempFile = new File(file.getParent(), "HIEditor_tempConversion.jpg");
                                        if ( tempFile != null ) {
                                            if ( tempFile.exists() ) {
                                                tempFile.delete();
                                                tempFile.createNewFile();
                                            } else tempFile.createNewFile();

                                            // convert image file
                                            PlanarImage viewImage = JAI.create("stream",SeekableStream.wrapInputStream(
						new FileInputStream(file), true));
                                            FileOutputStream outJPEG = new FileOutputStream(tempFile);

                                            JPEGEncodeParam jpegParam = new JPEGEncodeParam();
                                            // set encoding quality
                                            jpegParam.setQuality(0.9f);
                                            JAI.create("encode",viewImage, outJPEG, "JPEG", jpegParam);
                                            outJPEG.close();
                                            
                                            importFile = tempFile;
                                            customExtenstion = ".jpg";
                                        }                                        
                                    } catch (Exception e) {
                                        System.out.println("Image conversion failed!");
                                        System.out.println("FILE: "+file.getName());
                                        e.printStackTrace();
                                    }
                                }
                                importview = HIRuntime.getManager().createView(importObject, filename+customExtenstion, "[HIEditor 3.0 - Direct Import]", HIWebServiceManager.getBytesFromFile(importFile));
                                HIRuntime.getManager().refreshProject();
                            } catch (HIWebServiceException wse) {
                                // TODO: handle image already in project case
                                HIRuntime.getGui().reportError(wse, browser);                                
                                return false;
                            }
                            if (importview == null) {
                                return false;
                            }

                            // add object to selected group, preserving sort order if this was not the import group
                            if (groupListView.getCurrentGroup().getType() != GroupTypes.HIGROUP_IMPORT) {
                                HIRuntime.getManager().copyToGroup(importObject, groupListView.getCurrentGroup());
                                // generate quick info
                                HiQuickInfo info = HIRuntime.getManager().getBaseQuickInfo(importObject.getId());
                                // update GUI
                                int index = groupContentsView.getContentsList().locationToIndex(supp.getDropLocation().getDropPoint());
                                groupContentsView.addContent(info, index);
                            }

                        } catch (HIWebServiceException wse) {
                            HIRuntime.getGui().reportError(wse, browser);
                            return false;
                        }

                    }
                    HIRuntime.getGui().stopIndicatingServiceActivity();
                    // update sort order
                    updateContentSortOrder();

                    // propagate changes
                    groupListView.getCurrentGroup().setTimestamp(new Date().getTime());
                    metadataEditorView.updateContent();
                    HIRuntime.getGui().sendMessage(HIMessageTypes.GROUP_CONTENTS_CHANGED, groupListView.getCurrentGroup(), null);
                    HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, groupListView.getCurrentGroup(), null);

                    return true;

                } catch (UnsupportedFlavorException e) {
                    return false; // file list empty or error occurred
                } catch (IOException e) {
                    return false; // file list empty or error occurred
                }
            }

            /* *************************
             * support repository import
             * *************************
             */
            if (supp.isDataFlavorSupported(RepositoryTransferable.repositoryFlavor)) {
		    	// DEBUG add dublin core template to project if it doesn´t already exists

                boolean dcTemplateFound = false;
                for (HiFlexMetadataTemplate template : HIRuntime.getManager().getProject().getTemplates()) {
                    if (template.getNamespacePrefix().equalsIgnoreCase("dc")) //$NON-NLS-1$
                    {
                        dcTemplateFound = true;
                    }
                }

                if (!dcTemplateFound) {
                    // add dc template to project
                    try {
                        HIRuntime.getManager().addTemplateToProject(MetadataHelper.getDCTemplateBlueprint());
                    } catch (HIWebServiceException wse) {
                        HIRuntime.getGui().reportError(wse, browser);
                        return false;
                    }
                }

                // import repository items
                try {
                    RepositoryTransfer transfer = (RepositoryTransfer) supp.getTransferable().getTransferData(RepositoryTransferable.repositoryFlavor);
                    if (transfer == null) {
                        return false;
                    }
                    if (transfer.getRepElements().size() < 1) {
                        return false; // no files to import
                    }
                    // import files into selected group, preserving sort order only if this is not the import group
                    HIRuntime.getGui().startIndicatingServiceActivity();
                    for (HiQuickInfo repInfo : transfer.getRepElements()) {
                        try {
                            // create an empty object wrapper for file system content
                            HiObject importObject = HIRuntime.getManager().createObject();
                            HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(importObject, HIRuntime.getManager().getProject().getDefaultLanguage());

                            // DEBUG sync dublin core metadata directly
                            List<HiMetadataRecord> repRecords = transfer.getSource().getRepositoryElementMetadata(repInfo);
                            if (repRecords != null) {
                                for (HiMetadataRecord repRecord : repRecords) {
                                    MetadataHelper.setValue("dc", repRecord.getKey(), repRecord.getValue(), record); //$NON-NLS-1$
                                }
                            }
                            HIRuntime.getManager().updateFlexMetadataRecord(record);

                            HiView importview = null;
                            try {
                                HiTypedDatastream repStream = transfer.getSource().getRepositoryElementData(repInfo);
                                // DEBUG pass on mime detection
                                String filename = "unknown.jpg"; //$NON-NLS-1$
                                if (repStream.getMIMEType() != null && repStream.getMIMEType().toLowerCase().startsWith("image/")) {
                                    if (repStream.getMIMEType().toLowerCase().indexOf("tiff") >= 0) {
                                        filename = "unkown.tif"; //$NON-NLS-1$
                                    }
                                    if (repStream.getMIMEType().toLowerCase().indexOf("gif") >= 0) {
                                        filename = "unkown.gif"; //$NON-NLS-1$
                                    }
                                    if (repStream.getMIMEType().toLowerCase().indexOf("png") >= 0) {
                                        filename = "unkown.png"; //$NON-NLS-1$
                                    }
                                }

                                importview = HIRuntime.getManager().createView(
                                        importObject,
                                        filename, // DEBUG mime type detection not implemented
                                        transfer.getSource().getActiveRepository().getModel().getUrl() + ":" + repInfo.getPreview(), //$NON-NLS-1$
                                        repStream.getByteArray());

                            } catch (HIWebServiceException wse) {
                                // TODO: handle image already in project case
                                HIRuntime.getGui().reportError(wse, browser);
                                return false;
                            }
                            if (importview == null) {
                                return false;
                            }

                            // add object to selected group, preserving sort order if this was not the import group
                            if (groupListView.getCurrentGroup().getType() != GroupTypes.HIGROUP_IMPORT) {
                                HIRuntime.getManager().copyToGroup(importObject, groupListView.getCurrentGroup());
                                // generate quick info
                                HiQuickInfo info = HIRuntime.getManager().getBaseQuickInfo(importObject.getId());
                                // update GUI
                                int index = groupContentsView.getContentsList().locationToIndex(supp.getDropLocation().getDropPoint());
                                groupContentsView.addContent(info, index);
                            }

                        } catch (HIWebServiceException wse) {
                            HIRuntime.getGui().reportError(wse, browser);
                            return false;
                        }

                    }
                    HIRuntime.getGui().stopIndicatingServiceActivity();
                    // update sort order
                    updateContentSortOrder();

                    // propagate changes
                    groupListView.getCurrentGroup().setTimestamp(new Date().getTime());
                    metadataEditorView.updateContent();
                    HIRuntime.getGui().sendMessage(HIMessageTypes.GROUP_CONTENTS_CHANGED, groupListView.getCurrentGroup(), null);
                    HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, groupListView.getCurrentGroup(), null);

                    return true;
                } catch (UnsupportedFlavorException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                }
            }

            // can´t put things into the import group
            if (dropGroup.getType() == GroupTypes.HIGROUP_IMPORT) {
                // inform user
                HIRuntime.getGui().displayInfoDialog(Messages.getString("GroupBrowser.20"), Messages.getString("GroupBrowser.21") + "\n" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        Messages.getString("GroupBrowser.23")); //$NON-NLS-1$
                return false;
            }

            // handle non-group content --> create links
            if (!supp.isDataFlavorSupported(QuickInfoTransferable.quickInfoFlavor)) {
                if (supp.isDataFlavorSupported(GroupTransferable.groupFlavor)) {
                    /* **************************************
                     * create a link of a group in this group
                     * **************************************
                     */
                    try {
                        HiGroup group = (HiGroup) supp.getTransferable().getTransferData(GroupTransferable.groupFlavor);
                        if (group == null) {
                            return false;
                        }

                        // check if group is the current group
                        if (group.getId() == dropGroup.getId()) {
                            return false; // can´t add a group to itself (infinite recursion)
                        }
                        return createLink(group, dropGroup, supp.getDropLocation().getDropPoint());

                    } catch (UnsupportedFlavorException e) {
                        return false; // group empty or another error occurred
                    } catch (IOException e) {
                        return false; // group empty or another error occurred
                    }
                }

                if (supp.isDataFlavorSupported(LayerTransferable.layerFlavor)) {
                    /* **************************************
                     * create a link of a layer in this group
                     * **************************************
                     */
                    try {
                        HILayer layer = (HILayer) supp.getTransferable().getTransferData(LayerTransferable.layerFlavor);
                        if (layer == null) {
                            return false;
                        }

                        return createLink(layer.getModel(), dropGroup, supp.getDropLocation().getDropPoint());

                    } catch (UnsupportedFlavorException e) {
                        return false; // layer empty or another error occurred
                    } catch (IOException e) {
                        return false; // layer empty or another error occurred
                    }
                }

                if (supp.isDataFlavorSupported(ObjectContentTransferable.objecContentFlavor)) {
                    /* ****************************************************
                     * create a link of a view or inscription in this group
                     * ****************************************************
                     */
                    try {
                        HiObjectContent content = (HiObjectContent) supp.getTransferable().getTransferData(ObjectContentTransferable.objecContentFlavor);
                        if (content == null) {
                            return false;
                        }

                        return createLink(content, dropGroup, supp.getDropLocation().getDropPoint());

                    } catch (UnsupportedFlavorException e) {
                        return false; // object content empty or another error occurred
                    } catch (IOException e) {
                        return false; // object content empty or another error occurred
                    }
                }

            } else {
                // handle group content --> copy or move | sort/reorder
                try {
                    boolean isSearch = false;
                    ContentTransfer transfer = (ContentTransfer) supp.getTransferable().getTransferData(QuickInfoTransferable.quickInfoFlavor);
                    if (transfer.getSourceGroup() == null || transfer.getSource() instanceof SearchModule) // is this a search result?
                    {
                        isSearch = true;
                    }

                    transfer.setTarget(browser); // set target component

                    // find the insertion index
                    int insertIndex = groupContentsView.getContentsList().locationToIndex(supp.getDropLocation().getDropPoint());
                    insertIndex = Math.max(0, insertIndex); // make sure index is valid
                    insertIndex = Math.min(groupContentsView.getContentsList().getModel().getSize() - 1, insertIndex);

                    if (!isSearch
                            && transfer.getSourceGroup().getId() == dropGroup.getId()
                            && !(supp.getComponent() instanceof GroupContentsList)) {
                        return false;
                    }

                    if (!isSearch
                            && transfer.getSourceGroup().getId() == dropGroup.getId()
                            && (supp.getComponent() instanceof GroupContentsList)) {
                        /* **********************************
                         * handle group contents reorder/sort
                         * **********************************
                         */
                        // can´t sort same group from different group browser, would confuse user
                        if (transfer.getSource() != browser) {
                            return false;
                        }

                        supp.setDropAction(MOVE); // sorting implies move action
                        wasSortAction = true; // set action status

                        DefaultListModel model = (DefaultListModel) groupContentsView.getContentsList().getModel();

                        if (model.getSize() == 0) {
                            return false; // sanity check: can´t sort an empty list
                        }
                        // insert reference point element
                        HiQuickInfo reference = new HiQuickInfo();
                        reference.setBaseID(-1);
                        reference.setTitle("Dummy"); //$NON-NLS-1$
                        model.add(insertIndex, reference);
                        groupContentsView.getContents().add(insertIndex, reference); // also update internal contents model

                        // now delete transfer contents from list
                        for (HiQuickInfo transferContent : transfer.getContents()) {
                            // find content index
                            int contentIndex = -1;
                            for (int i = 0; i < model.size(); i++) {
                                if (((HiQuickInfo) model.get(i)).getBaseID() == transferContent.getBaseID()) {
                                    contentIndex = i;
                                }
                            }
                            if (contentIndex < 0) { // sanity check
                                model.removeElement(reference);
                                groupContentsView.getContents().remove(reference);
                                return false;
                            }
                            // remove content
                            model.remove(contentIndex);
                            groupContentsView.getContents().remove(contentIndex); // also update internal contents model
                        }

                        // find index of reference object
                        insertIndex = model.indexOf(reference);

                        // add content back in after reference object
                        for (int i = transfer.getContents().size() - 1; i >= 0; i--) {
                            model.add(insertIndex, transfer.getContents().get(i));
                            groupContentsView.getContents().add(insertIndex, transfer.getContents().get(i)); // also update internal contents model
                        }

                        // remove reference object
                        model.removeElement(reference);
                        groupContentsView.getContents().remove(reference); // also update internal contents model

                        // sync sort order to server
                        if (!updateContentSortOrder()) {
                            // sorting failed, reload group contents from server
                            groupListView.getList().setEnabled(false);
                            groupContentsView.prepareElementLoading();
                            metadataEditorView.setGroup(groupListView.getCurrentGroup(), HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());
                            contentLoader.loadGroup(groupListView.getCurrentGroup());
                            return false;
                        }

                        // propagate changes
                        HIRuntime.getGui().sendMessage(HIMessageTypes.GROUPCONTENTS_SORTORDER_CHANGED, groupListView.getCurrentGroup(), browser);

                        return true;
                    } else {
                        /* ********************************************
                         * COPY or MOVE group contents to another group
                         * ********************************************
                         */

						// if source group was import or trash --> implies MOVE action
                        // because we can´t copy things from the import or out of the trash
                        if (isSearch) {
                            supp.setDropAction(COPY);
                        } else if (transfer.getSourceGroup().getType() != GroupTypes.HIGROUP_REGULAR) {
                            supp.setDropAction(MOVE);
                        }

                        // check if user wants to move things to the trash
                        boolean moveToTrash = false;
                        if (dropGroup.getType() == GroupTypes.HIGROUP_TRASH) {
                            moveToTrash = true;
                        }

                        // TODO refactor into separate function
                        if (moveToTrash) {
                            if (!HIRuntime.getGui().displayUserYesNoDialog(
                                    Messages.getString("GroupBrowser.11"), //$NON-NLS-1$
                                    Messages.getString("GroupBrowser.12") + "\n\n" + Messages.getString("GroupBrowser.14"))) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            {
                                return false;
                            }
                        }

                        boolean groupsOrLayersInSelection = false;
                        try {
                            HIRuntime.getGui().startIndicatingServiceActivity();
                            for (int i = transfer.getContents().size() - 1; i >= 0; i--) {
                                // check if element is already in group
                                boolean isInGroup = false;
                                if (dropGroup.getId() == groupListView.getCurrentGroup().getId()) {
                                    for (HiQuickInfo content : groupContentsView.getContents()) {
                                        if (content.getBaseID() == transfer.getContents().get(i).getBaseID()) {
                                            isInGroup = true;
                                        }
                                    }
                                }

                                boolean moveOrCopySuccessful = false;
                                if (!moveToTrash) {
                                    // move or copy (depending on user request) contents to target group
                                    if (supp.getDropAction() == MOVE) {
                                        moveOrCopySuccessful = HIRuntime.getManager().moveToGroup(transfer.getContents().get(i).getBaseID(), transfer.getSourceGroup().getId(), dropGroup.getId());
                                    } else if (!isInGroup) {
                                        moveOrCopySuccessful = HIRuntime.getManager().copyToGroup(transfer.getContents().get(i).getBaseID(), dropGroup.getId());
                                    }
                                } else {
                                    // move contents to trash
                                    // check if content is not layer or group as these elements can´t be moved to the trash
                                    if (!(transfer.getContents().get(i).getContentType() == HiBaseTypes.HI_GROUP) && !(transfer.getContents().get(i).getContentType() == HiBaseTypes.HI_LAYER)) {
                                        moveOrCopySuccessful = HIRuntime.getManager().moveToTrash(transfer.getContents().get(i).getBaseID());
                                        // notify GUI
                                        if (moveOrCopySuccessful) {
                                            HIRuntime.getGui().notifyItemSentToTrash(transfer.getContents().get(i).getBaseID());
                                        }
                                    } else {
                                        groupsOrLayersInSelection = true;
                                    }

                                }
                                if (!isInGroup && moveOrCopySuccessful
                                        && dropGroup.getId() == groupListView.getCurrentGroup().getId()) {
                                    // generate quick info
                                    HiQuickInfo info = HIRuntime.getManager().getBaseQuickInfo(transfer.getContents().get(i).getBaseID());
                                    loadContentPreview(info);

                                    // update GUI
                                    if (moveOrCopySuccessful) {
                                        groupContentsView.addContent(info, insertIndex);
                                    }
                                }
                            }
                            HIRuntime.getGui().stopIndicatingServiceActivity();

                            // inform user that his selection included elements that we can´t move to the trash
                            if (moveToTrash && groupsOrLayersInSelection) {
                                HIRuntime.getGui().displayInfoDialog(Messages.getString("GroupBrowser.15"), Messages.getString("GroupBrowser.16")); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                            // update sort order
                            updateContentSortOrder();

                            // propagate changes
                            groupListView.getCurrentGroup().setTimestamp(new Date().getTime());
                            metadataEditorView.updateContent();
                            HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, dropGroup, null);
                            if (dropGroup.getId() != groupListView.getCurrentGroup().getId()) {
                                HIRuntime.getGui().sendMessage(HIMessageTypes.GROUP_CONTENTS_CHANGED, dropGroup, null);
                            }
                            if (moveToTrash) {
                                HIRuntime.getGui().sendMessage(HIMessageTypes.CONTENT_MOVED_TO_TRASH, null, null);
                            }
                        } catch (HIWebServiceException wse) {
                            HIRuntime.getGui().reportError(wse, browser);
                            // TODO reload group
                            return false;
                        }
                        return true;
                    }

                } catch (UnsupportedFlavorException e) {
                    return false; // group empty or another error occurred
                } catch (IOException e) {
                    return false; // group empty or another error occurred
                }
            }

            return false;
        }
    }

	
	
	//	----------------------------------------------------------------------------------------

	
	
	/**
	 * DEBUG
	 * Class: HIGroupContentLoader
	 * Package: org.hyperimage.client.components
	 * @author Jens-Martin Loebel
	 *
	 */
	class HIGroupContentLoader implements Runnable 
	{		
		private Thread thread;
		private HiGroup group;


		public HIGroupContentLoader()
		{
			this.thread = new Thread(this); 
		}

		public void loadGroup(HiGroup group) {
			this.group = group;
			if ( thread.getState() == Thread.State.NEW )
				thread.start();
			else if ( thread.getState() == Thread.State.TERMINATED ) {
				thread = new Thread(this);
				thread.start();
			} else { 
				while ( thread.getState() == Thread.State.RUNNABLE ) ;
				thread = new Thread(this);
				thread.start();
			}
		}

		public void run() 
		{
			final List<HiQuickInfo> infoList;
			try {
				// DEBUG
				infoList = HIRuntime.getManager().getGroupContents(group);
				
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, browser);
				groupContentsView.setContents(null);
				groupListView.getList().setEnabled(true);
				return;
			}

			SwingUtilities.invokeLater(new Runnable()  {
				public void run() {
					if ( infoList != null ) {
						if ( infoList.size() >= HIRuntime.MAX_GROUP_ITEMS ) 
							setDisplayStyle(HI_ListDisplayStyles.LIST_STYLE);
						else
							setDisplayStyle(userSelectedStyle);
						groupContentsView.setContents(infoList);
					}
					groupListView.getList().setEnabled(true);
					updateContentSortOrder();

					// scan items and load/attach previews from WS if necessary
					if ( groupContentsView.getContentsList().getDisplayStyle() == GroupContentsList.HI_ListDisplayStyles.ICON_STYLE)
						loadContentPreviews();

				}
			});



		}
	}
	

	//	----------------------------------------------------------------------------------------


	public GroupBrowser() {
		this(HIRuntime.getManager().getImportGroup());
	}

	public GroupBrowser(HiGroup group) {
		super(Messages.getString("GroupBrowser.17"), Messages.getString("GroupBrowser.18")); //$NON-NLS-1$ //$NON-NLS-2$
		
		groupListView = new GroupListView(HIRuntime.getManager().getImportGroup(), 
				HIRuntime.getManager().getTrashGroup(), 
				HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());
		groupContentsView = new GroupContentsView();
		metadataEditorView = new GroupPropertyEditorView();


		contentLoader = new HIGroupContentLoader();
		try {
			HIRuntime.getGui().startIndicatingServiceActivity();
			groupListView.setGroups(HIRuntime.getManager().getGroups());
			HIRuntime.getGui().stopIndicatingServiceActivity();
		} catch (HIWebServiceException wse) {
			HIRuntime.getGui().reportError(wse, this);
		}

		// init group contents
		HIRuntime.getGui().startIndicatingServiceActivity();
		groupListView.getList().setEnabled(false);
		groupContentsView.prepareElementLoading();
		

		// set initial group if possible
		if ( group == null ) group = HIRuntime.getManager().getImportGroup();
		if ( group.getId() != HIRuntime.getManager().getImportGroup().getId() ) {
			int groupIndex = -1;
			for ( int i = 0 ; i <  groupListView.getList().getModel().getSize() ; i ++ )
				if (  ((HiGroup)groupListView.getList().getModel().getElementAt(i)).getId() == group.getId() ) {
					groupIndex = i;
					group = ((HiGroup)groupListView.getList().getModel().getElementAt(i));
				}
			if ( groupIndex == -1 ) group = HIRuntime.getManager().getImportGroup();
			else groupListView.getList().setSelectedIndex(groupIndex);
		}

		
		groupContentsView.setCurrentGroup(group);
		metadataEditorView.setGroup(group,HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());
		contentLoader.loadGroup(group);
		HIRuntime.getGui().stopIndicatingServiceActivity();
		updateTitle();

		// init Drag and Drop handler
		groupContentsHandler = new GroupContentsTransferHandler(this);
		groupContentsView.getContentsList().setTransferHandler(groupContentsHandler);
		groupListView.getList().setTransferHandler(new GroupTransferHandler(this));
		
		// attach listeners
		metadataEditorView.getSaveButton().addActionListener(this);
		metadataEditorView.getResetButton().addActionListener(this);
		
		groupContentsView.getContentsList().addMouseListener(this);
        groupContentsView.getListStyleButton().addActionListener(this);
        groupContentsView.optionsButton.addActionListener(this);
		groupContentsView.setMenuActionListener(this); // popup menu listeners

        groupListView.getList().addListSelectionListener(this);
		groupListView.attachActionListeners(this);
		
		// attach context menu listener
		groupListView.getList().addMouseListener(this);

		
		// register views
		this.views.add(groupListView);
		this.views.add(groupContentsView);
		this.views.add(metadataEditorView);
	}
	
	
	public void updateLanguage() {
		super.updateLanguage();
		if ( groupContentsView.getContentsList().getDisplayStyle() == HI_ListDisplayStyles.ICON_STYLE )
			groupContentsView.getListStyleButton().setToolTipText(Messages.getString("GroupBrowser.77")); //$NON-NLS-1$
		else 
			groupContentsView.getListStyleButton().setToolTipText(Messages.getString("GroupBrowser.78")); //$NON-NLS-1$
		updateTitle();
	}

	
	public HiBase getBaseElement() {
		// returns the group that is currently selected
		if ( groupListView.getList().getModel().getSize() > 0 && groupListView.getList().getSelectedIndex() >= 0 )
			return (HiGroup) groupListView.getList().getSelectedValue();
		else
			return null;
	}

	
	/**
	 * implementation of the HIComponent method
	 * The group browser will accept any element and check if it is an a currently displayed group.
	 * Additionally changes made to a group are also propagated.
	 * This method updates the GUI to reflect the changes made by other components.
	 */
	
	public void receiveMessage(HIMessageTypes message, HiBase base ) {
		/*
		 * respond to LANGUAGE_ADDED / LANGUAGE_REMOVED messages
		 */
		if ( message == HIMessageTypes.LANGUAGE_ADDED || message == HIMessageTypes.LANGUAGE_REMOVED ) {
			// reload all group metadata
			DefaultListModel model = (DefaultListModel) groupListView.getList().getModel(); 
			// skip import and trash group
			HIRuntime.getGui().startIndicatingServiceActivity();
			try {
				for ( int i=1 ; i < model.getSize()-1; i++ ) {
					HiGroup group = (HiGroup)model.get(i);
					List<HiFlexMetadataRecord> records = HIRuntime.getManager().getFlexMetadataRecords(group);
					while ( group.getMetadata().size() > 0 ) group.getMetadata().remove(0);
					for ( HiFlexMetadataRecord record : records )
						group.getMetadata().add(record);
				}
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
			HIRuntime.getGui().stopIndicatingServiceActivity();
			
			// update GUI
			metadataEditorView.setGroup(groupListView.getCurrentGroup(), HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());
		}
		

		
		/*
		 * respond to ENTITY_CHANGED messages
		 */
		if ( message == HIMessageTypes.ENTITY_CHANGED ) {
			if ( base instanceof HiGroup ) {
				// update group metadata and/or title
				groupListView.updateGroup((HiGroup)base);
				if ( groupListView.getCurrentGroup().getId() == base.getId() )
					metadataEditorView.setGroup((HiGroup)base,HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()); 
			}

			// update group contents if necessary
			for ( HiQuickInfo content : groupContentsView.getContents() ) {
				if ( content.getBaseID() == base.getId() || content.getRelatedID() == base.getId() ) {
					
					// DEBUG
					HiQuickInfo newInfo;
					try {
						HIRuntime.getGui().startIndicatingServiceActivity();
						newInfo = HIRuntime.getManager().getContentInfo(content);
						groupContentsView.updateQuickInfo(newInfo);
						HIRuntime.getGui().stopIndicatingServiceActivity();
						loadContentPreview(newInfo);
					} catch (HIWebServiceException wse) {
						HIRuntime.getGui().reportError(wse, this);
					}
				}
			}
		}
		
		/*
		 * respond to GROUP_CONTENTS_CHANGED messages
		 */
		if ( message == HIMessageTypes.GROUP_CONTENTS_CHANGED ) {
			HiGroup changedGroup = (HiGroup)base;
		
			if ( changedGroup.getId() == groupListView.getCurrentGroup().getId()
				 || groupListView.getCurrentGroup().getType() != GroupTypes.HIGROUP_REGULAR ) {
				// reload contents
				groupListView.getList().setEnabled(false);
				groupContentsView.prepareElementLoading();
				contentLoader.loadGroup(groupListView.getCurrentGroup());
			}
		}
		/*
		 * respond to MOVE_TO_TRASH
		 */
		// TODO: check all groups
		if ( message == HIMessageTypes.CONTENT_MOVED_TO_TRASH || message == HIMessageTypes.MOVED_TO_TRASH ) {
//			if ( groupListView.getCurrentGroup().getType() == GroupTypes.HIGROUP_TRASH ) {
				// reload contents
				groupListView.getList().setEnabled(false);
				groupContentsView.prepareElementLoading();
				contentLoader.loadGroup(groupListView.getCurrentGroup());
//			}
		}
		
		/*
		 * respond to DEFAULT_LANGUAGE_CHANGED
		 */
		if ( message == HIMessageTypes.DEFAULT_LANGUAGE_CHANGED ) {
			// reload contents
			groupListView.getList().setEnabled(false);
			groupContentsView.prepareElementLoading();
			contentLoader.loadGroup(groupListView.getCurrentGroup());
			// refresh group list
			((GroupListCellRenderer)groupListView.getList().getCellRenderer()).setDefaultLang(HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());
			groupListView.getList().repaint();
		}


		/*
		 * respond to ENTITY_ADDED / REMOVED messages
		 */
		// TODO implement
		if ( message == HIMessageTypes.ENTITY_ADDED || message == HIMessageTypes.ENTITY_REMOVED ) {
			// check if the entity was a group, if so update the group list
			if ( base instanceof HiGroup ) {
				// DEBUG
				try {
					groupListView.setGroups(HIRuntime.getManager().getGroups());
				} catch (HIWebServiceException wse) {
					HIRuntime.getGui().reportError(wse, this);
					return;
				}
			}
		}
		
		/*
		 * respond to GROUP_SORTORDER_CHANGED messages
		 */
		if ( message == HIMessageTypes.GROUP_SORTORDER_CHANGED ) {
			// DEBUG
			try {
				groupListView.setGroups(HIRuntime.getManager().getGroups());
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
		}
		
		/*
		 * respond to GROUPCONTENTS_SORTORDER_CHANGED messages
		 */
		if ( message == HIMessageTypes.GROUPCONTENTS_SORTORDER_CHANGED ) {
			if ( base != null ) {
				HiGroup group = (HiGroup)base;
				for ( int i=0; i < groupListView.getList().getModel().getSize(); i++ )
					if ( groupListView.getGroup(i).getId() == group.getId() ) {
						groupListView.getGroup(i).setSortOrder(group.getSortOrder());
						if ( groupListView.getGroup(i).getId() == groupListView.getCurrentGroup().getId() )
							groupContentsView.sortContents();
					}
			}
		}
		
	}
	
	
	/**
	 * respond to GUI close request, prompt user to save metadata or cancel operation
	 */
	public boolean requestClose() {
		return askToSaveOrCancelChanges();
	}
	
	
	/**
	 * Resets group list, metadata and contents view after a project change.
	 * Reloads all groups.
	 */
	public void resetBrowser() {
		discardChanges();
		
		try {
			HIRuntime.getGui().startIndicatingServiceActivity();
			groupListView.resetAllGroups(
					HIRuntime.getManager().getImportGroup(), 
					HIRuntime.getManager().getTrashGroup(),
					HIRuntime.getManager().getGroups()
			);
			HIRuntime.getGui().stopIndicatingServiceActivity();
		} catch (HIWebServiceException wse) {
			HIRuntime.getGui().reportError(wse, this);
		}
	}
	
	
	private void saveMetadataChanges() {
		boolean updateMetadata = metadataEditorView.hasMetadataChanges();
		
		if ( updateMetadata && HIRuntime.getGui().checkEditAbility(false) ) {
			metadataEditorView.syncChanges();
			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				HIRuntime.getManager().updateFlexMetadataRecords(
						metadataEditorView.getMetadata());                                
				HIRuntime.getGui().stopIndicatingServiceActivity();

				// propagate changes
                                groupListView.getCurrentGroup().setTimestamp(new Date().getTime());
                                metadataEditorView.updateContent();
				HIRuntime.getGui().sendMessage(
						HIMessageTypes.ENTITY_CHANGED, 
						groupContentsView.getCurrentGroup(), 
						this);
				// update GUI
				groupListView.updateContent();
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
			
		}

	}
		
    // [JML] visibility properties are not part of the editor anymore. They will be handled by the PostPeTAL Generator
/*
	private void savePropertyChanges() {
		boolean updateProperties = metadataEditorView.hasPropertyChanges();

		if ( updateProperties && HIRuntime.getGui().checkEditAbility(false) ) {
			metadataEditorView.syncChanges();
			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				HIRuntime.getManager().updateGroupProperties(
						groupListView.getCurrentGroup().getId(),
						groupListView.getCurrentGroup().isVisible());
				HIRuntime.getGui().stopIndicatingServiceActivity();

				// propagate changes
				HIRuntime.getGui().sendMessage(
						HIMessageTypes.ENTITY_CHANGED, 
						groupContentsView.getCurrentGroup(), 
						this);
				 // update GUI
				groupListView.updateContent();
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
		}	
	}
*/
	
	private void discardChanges() {
		if ( metadataEditorView.hasChanges() )
			metadataEditorView.resetChanges();
	}
	
	private boolean askToSaveOrCancelChanges() {
		if ( metadataEditorView.hasChanges() && HIRuntime.getGui().checkEditAbility(true) ) {
			int decision = JOptionPane.showConfirmDialog(
					HIRuntime.getGui(), 
							Messages.getString("GroupBrowser.19")); //$NON-NLS-1$
			
			if ( decision == JOptionPane.CANCEL_OPTION ) 
				return false;
			else if ( decision == JOptionPane.YES_OPTION ) {
				saveMetadataChanges();
		        // [JML] visibility properties are not part of the editor anymore. They will be handled by the PostPeTAL Generator
//				savePropertyChanges();
			}
		}
		return true;
	}

	private HiGroup createGroup() {
		HiGroup newGroup = null;
		if ( HIRuntime.getGui().checkEditAbility(false) ) { // check user role
			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				newGroup = HIRuntime.getManager().createGroup();
				HIRuntime.getGui().stopIndicatingServiceActivity();
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return null;
			}
		}

		return newGroup;
	}
	
	private void loadContentPreviews() {
		for ( HiQuickInfo info : groupContentsView.getContents() )
			loadContentPreview(info);
	}
	
	private void loadContentPreview(HiQuickInfo info) {
		GroupContentsCellRenderer renderer = (GroupContentsCellRenderer) groupContentsView.getContentsList().getCellRenderer();
		QuickInfoCell cell = renderer.getCellForContent(info);
		if ( cell != null ) {
			if ( cell.needsPreview() ) {
				WSImageLoaderThread thumbLoader = new WSImageLoaderThread();
				long viewID = info.getBaseID();
				boolean cacheImage = true;
				if ( info.getContentType() == HiBaseTypes.HI_OBJECT )
					viewID = info.getRelatedID();
				if ( info.getContentType() == HiBaseTypes.HI_LAYER )
					cacheImage = false;
				thumbLoader.loadImage(viewID, cacheImage, HiImageSizes.HI_THUMBNAIL, cell, groupContentsView.getContentsList());
			}
		}
	}

	
	private boolean updateContentSortOrder() {
		String sortOrder = groupContentsView.getSortOrder();

		// check user role
		if ( !HIRuntime.getGui().checkEditAbility(true) )
			return true;
                // assert
                if ( groupListView == null || groupListView.getCurrentGroup() == null ) return false;
		
		if ( groupListView.getCurrentGroup().getType() != GroupTypes.HIGROUP_REGULAR )
			return true; // don´t try to set sort order for import or trash group
		
		if ( groupListView.getCurrentGroup().getSortOrder().compareTo(sortOrder) != 0 ) {
			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				HIRuntime.getManager().updateGroupSortOrder(groupListView.getCurrentGroup(), sortOrder);
				HIRuntime.getGui().stopIndicatingServiceActivity();
                                // propagate changes
                                groupListView.getCurrentGroup().setTimestamp(new Date().getTime());
                                metadataEditorView.updateContent();
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return false;
			}
		}
		
		return true;
	}


	private void updateTitle() {
		HiGroup group = groupListView.getCurrentGroup();
		String title = MetadataHelper.findValue("HIBase", "title", MetadataHelper.getDefaultMetadataRecord(group, HIRuntime.getManager().getProject().getDefaultLanguage())); //$NON-NLS-1$ //$NON-NLS-2$
		if ( title == null || title.length() == 0 ) {
                    if ( group.getUUID() == null ) title ="G"+group.getId(); //$NON-NLS-1$
                    else title = group.getUUID();
                } else title ="\""+title+"\""; //$NON-NLS-1$ //$NON-NLS-2$
		if ( group.getType() == GroupTypes.HIGROUP_REGULAR ) setTitle(Messages.getString("GroupBrowser.25")+" ("+title+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if ( group.getType() == GroupTypes.HIGROUP_IMPORT ) setTitle(Messages.getString("GroupBrowser.28")); //$NON-NLS-1$
		if ( group.getType() == GroupTypes.HIGROUP_TRASH ) setTitle(Messages.getString("GroupBrowser.29")); //$NON-NLS-1$
		getWindowMenuItem().setText(getTitle());
		HIRuntime.getGui().updateComponentTitle(this);
	}
	
	

	//--------------------------------------------------------------------------------------



	//	DEBUG
	public void valueChanged(ListSelectionEvent e) {
		if ( groupListView.getList().getSelectedIndex() == -1 )
			groupListView.setSelectedGroup(groupContentsView.getCurrentGroup());

		if ( groupListView.getCurrentGroup().getId() != groupContentsView.getCurrentGroup().getId() ) {	
			// warn user about unsaved data
			if ( ! askToSaveOrCancelChanges() ) {
				groupListView.setSelectedGroup(groupContentsView.getCurrentGroup());
				return;
			}
			
			groupListView.getList().setEnabled(false);
			groupContentsView.prepareElementLoading();
			HiGroup group = groupListView.getCurrentGroup();
			groupContentsView.setCurrentGroup(group);
			metadataEditorView.setGroup(group,HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());
			
			if ( group != null )
				contentLoader.loadGroup(group);
			
			// update info title
			updateTitle();
		}
	}

	
	public void actionPerformed(ActionEvent e) {
		// DEBUG: refactor
		// ----- popup menu 
		if ( e.getActionCommand().startsWith("new") ) { //$NON-NLS-1$
			/* 
			 * content creation options
			 */
			try {
				HIRuntime.getGui().startIndicatingServiceActivity();
				HiBase newElement = null;
				if ( e.getActionCommand().equalsIgnoreCase("newText") ) //$NON-NLS-1$
					newElement = HIRuntime.getManager().createText(); // create text
				if ( e.getActionCommand().equalsIgnoreCase("newURL") )  //$NON-NLS-1$
					newElement = HIRuntime.getManager().createURL("", "", ""); // create URL //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if ( e.getActionCommand().equalsIgnoreCase("newLighttable") ) //$NON-NLS-1$
					newElement = HIRuntime.getManager().createLightTable("", MetadataHelper.getDefaultLightTableXML()); // create light table //$NON-NLS-1$
				if ( e.getActionCommand().equalsIgnoreCase("newObject") )  //$NON-NLS-1$
					newElement = HIRuntime.getManager().createObject(); // create empty object

				// add to current group
				HIRuntime.getManager().copyToGroup(newElement.getId(), groupListView.getCurrentGroup().getId());
				// generate quick info
				HiQuickInfo info = HIRuntime.getManager().getBaseQuickInfo(newElement.getId());
				HIRuntime.getGui().stopIndicatingServiceActivity();
				// update GUI
				groupContentsView.addContent(info);
				// update sort order
				updateContentSortOrder();
				// propagate changes
                                groupListView.getCurrentGroup().setTimestamp(new Date().getTime());
                                metadataEditorView.updateContent();
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_ADDED, newElement, this);
				HIRuntime.getGui().sendMessage(HIMessageTypes.GROUP_CONTENTS_CHANGED, groupListView.getCurrentGroup(), this);
				HIRuntime.getGui().sendMessage(HIMessageTypes.GROUPCONTENTS_SORTORDER_CHANGED, groupListView.getCurrentGroup(), this);
				// open new element
				HIRuntime.getGui().openContentEditor(info, this);			

			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
		}

		if ( e.getActionCommand().equalsIgnoreCase("groupFromSelection") ) { //$NON-NLS-1$
			// check user role
			if ( ! HIRuntime.getGui().checkEditAbility(false) )
				return;
			int[] indices = groupContentsView.getContentsList().getSelectedIndices();
			// create a new group
			HiGroup newGroup = createGroup();
			if ( newGroup != null ) {
				try {
					// set a meaningful title
					String groupTitle = ""; //$NON-NLS-1$
					HiGroup curGroup = groupListView.getCurrentGroup();
					if ( curGroup.getType() == GroupTypes.HIGROUP_IMPORT ) groupTitle = Messages.getString("GroupBrowser.40"); //$NON-NLS-1$
					else if ( curGroup.getType() == GroupTypes.HIGROUP_TRASH ) groupTitle=Messages.getString("GroupBrowser.41"); //$NON-NLS-1$
					else {
						String title = MetadataHelper.findValue(
								"HIBase",  //$NON-NLS-1$
								"title",  //$NON-NLS-1$
								MetadataHelper.getDefaultMetadataRecord(
										curGroup, 
										HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId()
								));
						if ( title == null || title.length() == 0 ) {
							if ( curGroup.getUUID() == null ) groupTitle = "G"+curGroup.getId(); //$NON-NLS-1$
                                                        else groupTitle = curGroup.getUUID();
                                                } else groupTitle = title;
					}
					HiFlexMetadataRecord record = MetadataHelper.getDefaultMetadataRecord(
							newGroup, 
							HIRuntime.getManager().getProject().getDefaultLanguage().getLanguageId());
					if ( record != null ) {
						// sync title to server
						MetadataHelper.setValue("HIBase", "title", Messages.getString("GroupBrowser.47")+" "+groupTitle, record); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						HIRuntime.getGui().startIndicatingServiceActivity();
						HIRuntime.getManager().updateFlexMetadataRecord(record);
						HIRuntime.getGui().stopIndicatingServiceActivity();
					}
					
					
					// add all selected elements to new group
					String sortOrder = ""; //$NON-NLS-1$
					for ( int index : indices ) {
						HiQuickInfo content = groupContentsView.getContents().get(index);
						HIRuntime.getManager().copyToGroup(content.getBaseID(), newGroup.getId());
						sortOrder = sortOrder+","+content.getBaseID(); //$NON-NLS-1$
					}
					// set sort order
					if ( sortOrder.length() > 0 ) sortOrder = sortOrder.substring(1);	
					HIRuntime.getManager().updateGroupSortOrder(newGroup, sortOrder);

					// propagate changes
                                        groupListView.getCurrentGroup().setTimestamp(new Date().getTime());
                                        metadataEditorView.updateContent();
					HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_ADDED, newGroup, this);
					HIRuntime.getGui().sendMessage(HIMessageTypes.GROUP_CONTENTS_CHANGED, newGroup, this);					
					groupListView.setGroups(HIRuntime.getManager().getGroups());
					groupListView.getList().setSelectedIndex(groupListView.getList().getModel().getSize()-2);
					// reload group contents if necessary
					if ( groupListView.getCurrentGroup().getType() != GroupTypes.HIGROUP_REGULAR ) {
						// reload contents
						groupListView.getList().setEnabled(false);
						groupContentsView.prepareElementLoading();
						contentLoader.loadGroup(groupListView.getCurrentGroup());
					}
				} catch (HIWebServiceException wse) {
					HIRuntime.getGui().reportError(wse, this);
					return;
				}
			}

		}

		if ( e.getActionCommand().equalsIgnoreCase("removeSelection") ) { //$NON-NLS-1$
			if ( ! HIRuntime.getGui().checkEditAbility(false) ) // check user role
				return;
			// warn the user
			if ( ! HIRuntime.getGui().displayUserYesNoDialog(
					Messages.getString("GroupBrowser.52"), //$NON-NLS-1$
					Messages.getString("GroupBrowser.53")+"\n\n"+Messages.getString("GroupBrowser.55")) ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return;
			try {
				int[] indices = groupContentsView.getContentsList().getSelectedIndices();
				// build list of GUI elements
				ArrayList<HiQuickInfo> contentsToRemove = new ArrayList<HiQuickInfo>(indices.length);
				for ( int index : indices ) contentsToRemove.add(groupContentsView.getContents().get(index));
				HIRuntime.getGui().startIndicatingServiceActivity();
				for ( HiQuickInfo content : contentsToRemove ) {
					// remove contents from group
					HIRuntime.getManager().removeFromGroup(content.getBaseID(), groupListView.getCurrentGroup().getId());
					// update GUI
					groupContentsView.removeContent(content);
				}
				HIRuntime.getGui().stopIndicatingServiceActivity();
				// propagate changes
                                groupListView.getCurrentGroup().setTimestamp(new Date().getTime());
                                metadataEditorView.updateContent();
				HIRuntime.getGui().sendMessage(HIMessageTypes.GROUP_CONTENTS_CHANGED, groupListView.getCurrentGroup(), this);
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, groupListView.getCurrentGroup(), this);
			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}
		}

		if ( e.getActionCommand().equalsIgnoreCase("moveSelectionToTrash") ) { //$NON-NLS-1$
			if ( ! HIRuntime.getGui().checkEditAbility(false) ) // check user role
				return;
			boolean deleteFromProject = false; // move to trash if this isn´t the trash group
			if ( groupListView.getCurrentGroup().getType() == GroupTypes.HIGROUP_TRASH )
				deleteFromProject = true; // otherwise remove permanently from project
			if ( deleteFromProject ) {
				if ( ! HIRuntime.getGui().displayUserYesNoDialog(
						Messages.getString("GroupBrowser.57"), //$NON-NLS-1$
				Messages.getString("GroupBrowser.58")+"\n\n"+Messages.getString("GroupBrowser.60")) ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return;
			} else {
				if ( ! HIRuntime.getGui().displayUserYesNoDialog(
						Messages.getString("GroupBrowser.61"), //$NON-NLS-1$
				Messages.getString("GroupBrowser.62")+"\n\n"+Messages.getString("GroupBrowser.64")) ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return;
			}
			// DEBUG refactor into separate function
			try {
				boolean groupsOrLayersInSelection = false;

				int[] indices = groupContentsView.getContentsList().getSelectedIndices();
				// build list of GUI elements
				ArrayList<HiQuickInfo> contentsToRemove = new ArrayList<HiQuickInfo>(indices.length);
				for ( int index : indices ) {
					HiQuickInfo content = groupContentsView.getContents().get(index);
					// check if selection included groups and/or layers
					if ( content.getContentType() == HiBaseTypes.HI_GROUP || content.getContentType() == HiBaseTypes.HI_LAYER )
						groupsOrLayersInSelection = true;
					else
						contentsToRemove.add(content);
				}
				// inform user that his selection included elements that we can´t move to the trash
				if ( groupsOrLayersInSelection )
					HIRuntime.getGui().displayInfoDialog(Messages.getString("GroupBrowser.65"), Messages.getString("GroupBrowser.66")); //$NON-NLS-1$ //$NON-NLS-2$
					
				HIRuntime.getGui().startIndicatingServiceActivity();
				for ( HiQuickInfo content : contentsToRemove ) {
					if ( deleteFromProject )
						// remove permanently
						HIRuntime.getManager().deleteFromProject(content.getBaseID());
					else {
						// move contents to trash
						HIRuntime.getManager().moveToTrash(content.getBaseID());
						HIRuntime.getGui().notifyItemSentToTrash(content.getBaseID());
					}
					// update GUI
					groupContentsView.removeContent(content);
				}
                                HIRuntime.getManager().refreshProject();
				HIRuntime.getGui().stopIndicatingServiceActivity();
				
				// propagate changes
                                groupListView.getCurrentGroup().setTimestamp(new Date().getTime());
                                metadataEditorView.updateContent();
				HIRuntime.getGui().sendMessage(HIMessageTypes.CONTENT_MOVED_TO_TRASH, groupListView.getCurrentGroup(), this);
				HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_CHANGED, groupListView.getCurrentGroup(), this);

			} catch (HIWebServiceException wse) {
				HIRuntime.getGui().reportError(wse, this);
				return;
			}		

		}

		// ----- buttons
		if ( e.getActionCommand().equalsIgnoreCase("options") ) { //$NON-NLS-1$
			// display options popup menu
			groupContentsView.showDefaultPopupMenu();
		} else if ( e.getActionCommand().equalsIgnoreCase("create") ) { //$NON-NLS-1$
			HiGroup newGroup = createGroup();
			if ( newGroup != null) {
					try {
						groupListView.setGroups(HIRuntime.getManager().getGroups());
						groupListView.getList().setSelectedIndex(groupListView.getList().getModel().getSize()-2);
						// propagate changes
						HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_ADDED, newGroup, this);
					} catch (HIWebServiceException wse) {
						HIRuntime.getGui().reportError(wse, this);
						return;
					}
			}
		} else if ( e.getActionCommand().equalsIgnoreCase("remove") ) { //$NON-NLS-1$
			// DEBUG: remove group
			if ( HIRuntime.getGui().checkEditAbility(false) ) { // check user role
				// warn user
				if ( ! HIRuntime.getGui().displayUserYesNoDialog(
						Messages.getString("GroupBrowser.70"), //$NON-NLS-1$
						Messages.getString("GroupBrowser.71")+"\n\n"+Messages.getString("GroupBrowser.73")) ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return;
				
				metadataEditorView.resetChanges();
				
				boolean groupDeleted = false;
				HiGroup deletedGroup = null;
				try {
					deletedGroup = groupListView.getCurrentGroup();
					HIRuntime.getGui().startIndicatingServiceActivity();
					groupDeleted = HIRuntime.getManager().deleteGroup(deletedGroup.getId());
					HIRuntime.getGui().stopIndicatingServiceActivity();
				} catch (HIWebServiceException wse) {
					HIRuntime.getGui().reportError(wse, this);
					return;
				}
				if ( groupDeleted ) {
					try {
						groupListView.setGroups(HIRuntime.getManager().getGroups());
						// propagate changes
						HIRuntime.getGui().sendMessage(HIMessageTypes.ENTITY_REMOVED, deletedGroup, this);
					} catch (HIWebServiceException wse) {
						HIRuntime.getGui().reportError(wse, this);
						return;
					}
				}
			}
			/*
			 * save metadata and property changes
			 */
		} else if ( e.getActionCommand().equalsIgnoreCase("save") ) //$NON-NLS-1$
			if ( metadataEditorView.hasChanges() ) {
				// check user role
				if ( !HIRuntime.getGui().checkEditAbility(false) )
					return;

				saveMetadataChanges();
		        // [JML] visibility properties are not part of the editor anymore. They will be handled by the PostPeTAL Generator
//				savePropertyChanges();
			}

		/*
		 * discard metadata and property changes
		 */
		if ( e.getActionCommand().equalsIgnoreCase("reset") ) //$NON-NLS-1$
			discardChanges();



		// change the group contents view style
		// DEBUG refactor GUI button update
		if ( e.getActionCommand().equalsIgnoreCase("changeListStyle") ) { //$NON-NLS-1$
			if ( groupContentsView.getContentsList().getDisplayStyle() == GroupContentsList.HI_ListDisplayStyles.ICON_STYLE ) {
				setDisplayStyle(HI_ListDisplayStyles.LIST_STYLE);
				userSelectedStyle = HI_ListDisplayStyles.LIST_STYLE;
			} else {
				setDisplayStyle(HI_ListDisplayStyles.ICON_STYLE);
				userSelectedStyle = HI_ListDisplayStyles.ICON_STYLE;
			}
		}
	}

	private void setDisplayStyle ( HI_ListDisplayStyles style ) {
		if ( groupContentsView.getContentsList().getDisplayStyle() == style )
			return;
		if ( style == HI_ListDisplayStyles.ICON_STYLE ) {
			groupContentsView.getContentsList().setDisplayStyle(HI_ListDisplayStyles.ICON_STYLE);
			groupContentsView.getListStyleButton().setIcon(groupContentsView.listStyleIcon);
			groupContentsView.getListStyleButton().setToolTipText(Messages.getString("GroupBrowser.77")); //$NON-NLS-1$
			// load/attach preview images if necessary
			loadContentPreviews();
		} else {
			groupContentsView.getContentsList().setDisplayStyle(HI_ListDisplayStyles.LIST_STYLE);
			groupContentsView.getListStyleButton().setIcon(groupContentsView.iconStyleIcon);
			groupContentsView.getListStyleButton().setToolTipText(Messages.getString("GroupBrowser.78")); //$NON-NLS-1$
		}
	}


	//	--------------------------------------------------------------------------------------


	public void mouseClicked(MouseEvent e) {
		if ( e.getSource() == groupContentsView.getContentsList() ) {
			if ( e.getClickCount() == 2)
				if ( e.getButton() == MouseEvent.BUTTON1 )
					if ( groupListView.getCurrentGroup().getType() != GroupTypes.HIGROUP_TRASH ) {
						HiQuickInfo content = groupContentsView.getSelectedElement();
						if ( content != null ) {
							// user clicked on a link to a group
							if ( content.getContentType() == HiBaseTypes.HI_GROUP ) {
								// try to find group in list and select group
								int index = -1;
								for ( int i=0; i < groupListView.getList().getModel().getSize(); i++ )
									if ( ((HiGroup)groupListView.getList().getModel().getElementAt(i)).getId() == content.getBaseID() )
										index = i; // remember index of group
								if ( index >= 0 ) 
									// select found group
									groupListView.getList().setSelectedIndex(index);
								else 
									// group not found, inform user
									HIRuntime.getGui().displayInfoDialog(Messages.getString("GroupBrowser.79"), Messages.getString("GroupBrowser.80")); //$NON-NLS-1$ //$NON-NLS-2$
							} else // open content user clicked on
								HIRuntime.getGui().openContentEditor(content, this);
						}
					} else
						HIRuntime.getGui().displayInfoDialog(Messages.getString("GroupBrowser.81"), Messages.getString("GroupBrowser.82")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}



	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if ( e.getSource() == groupListView.getList() && e.isPopupTrigger() && !e.isConsumed() ) {
			e.consume();
			groupListView.showPopupMenu(e.getX(), e.getY());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if ( e.getSource() == groupListView.getList() && e.isPopupTrigger() && !e.isConsumed() ) {
			e.consume();
			groupListView.showPopupMenu(e.getX(), e.getY());
		}
	}

}
