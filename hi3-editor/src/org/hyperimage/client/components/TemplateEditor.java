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
import java.io.IOException;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.dialogs.AddTemplateDialog;
import org.hyperimage.client.gui.dialogs.AddTemplateFieldDialog;
import org.hyperimage.client.gui.dialogs.EditTemplateDialog;
import org.hyperimage.client.gui.dialogs.EditTemplateFieldDialog;
import org.hyperimage.client.gui.dnd.TemplateFieldTransferable;
import org.hyperimage.client.gui.dnd.TemplateTransferable;
import org.hyperimage.client.gui.views.TemplateFieldsView;
import org.hyperimage.client.gui.views.TemplateSetView;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiFlexMetadataSet;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;
import org.hyperimage.client.ws.HiPreference;

/**
 * @author Jens-Martin Loebel
 */
public class TemplateEditor extends HIComponent implements ListSelectionListener, ActionListener {

    /**
     * This class implements the Drag and Drop handler for templates.
     * As per SDD: sorting of templates is allowed.
     *
     * Class: TemplateTransferHandler
     * @author Jens-Martin Loebel
     *
     */
    public class TemplateTransferHandler extends TransferHandler {

        private TemplateEditor editor;

        public TemplateTransferHandler(TemplateEditor editor) {
            this.editor = editor;
        }

        // -------- export methods ---------
        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE + LINK;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            HiFlexMetadataTemplate template;

            template = (HiFlexMetadataTemplate) ((JList) c).getSelectedValue();

            return new TemplateTransferable(template);
        }

        @Override
        protected void exportDone(JComponent c, Transferable t, int action) {
            // focus source component if drop failed
            if (action == NONE) {
                HIRuntime.getGui().focusComponent(editor);

            }
        }

        // -------- import methods ---------
        @Override
        public boolean canImport(TransferSupport supp) {
            // focus target component if necessary
            HIRuntime.getGui().focusComponent(editor);

            if (!supp.isDataFlavorSupported(TemplateTransferable.templateFlavor))
                return false;

            // check user role
            if (!HIRuntime.getGui().checkAdminAbility(true))
                return false;

            // extract template to set appropriate drop action
            try {
                HiFlexMetadataTemplate template = (HiFlexMetadataTemplate) supp.getTransferable().getTransferData(TemplateTransferable.templateFlavor);
                supp.setDropAction(MOVE);
                return true;
            } catch (UnsupportedFlavorException e) {
                // no template or other error occurred
                return false;
            } catch (IOException e) {
                // no template or other error occurred
                return false;
            }
        }

        @Override
        public boolean importData(TransferSupport supp) {
            if (!canImport(supp)) // check if we support this element
                return false;

            // check user role
            if (!HIRuntime.getGui().checkAdminAbility(false))
                return false;

            // Fetch the Transferable and its data
            Transferable t = supp.getTransferable();
            HiFlexMetadataTemplate template = null;
            try {
                template = (HiFlexMetadataTemplate) t.getTransferData(TemplateTransferable.templateFlavor);
                if ( template == null )
                    return false;

                // Fetch the drop location
                int index = templateSetView.getTemplateList().locationToIndex(supp.getDropLocation().getDropPoint());
                index = Math.max(0, index);
                index = Math.min(templateSetView.getTemplateList().getModel().getSize() - 1, index);

                // try to find template in our list
                int listIndex = -1; // index of template in our list
                for (int i = 0; i < templateSetView.getTemplateList().getModel().getSize(); i++)
                    if ( ((HiFlexMetadataTemplate)templateSetView.getTemplateList().getModel().getElementAt(i)).getId() == template.getId())
                        listIndex = i;

                if ( listIndex < 0 ) return false;

                // sort templates
                HiFlexMetadataTemplate listTemplate = (HiFlexMetadataTemplate) templateSetView.getTemplateList().getModel().getElementAt(listIndex);
                DefaultListModel model = (DefaultListModel) templateSetView.getTemplateList().getModel();
                model.removeElementAt(listIndex);
                model.add(index, listTemplate);
                templateSetView.getTemplateList().setSelectedIndex(index);

                // update sort order on server
                boolean success = updateTemplateSortOrder();
                // propagate changes
                templateSetView.setTemplates(HIRuntime.getManager().getProject().getTemplates());
                HIRuntime.getGui().sendMessage(HIMessageTypes.TEMPLATE_CHANGED, null, editor);
                HIRuntime.getGui().sendMessage(HIMessageTypes.PREFERENCE_MODIFIED, null, editor);
                HIRuntime.getGui().stopIndicatingServiceActivity();

                return success;


            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }



    // --------------------------------------------------------------------------------------------------



    /**
     * This class implements the Drag and Drop handler for template fields.
     * As per SDD: sorting of template fields is allowed.
     *
     * Class: TemplateFieldTransferHandler
     * @author Jens-Martin Loebel
     *
     */
    public class TemplateFieldTransferHandler extends TransferHandler {

        private TemplateEditor editor;

        public TemplateFieldTransferHandler(TemplateEditor editor) {
            this.editor = editor;
        }

        // -------- export methods ---------
        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE + LINK;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            HiFlexMetadataSet set;

            set = (HiFlexMetadataSet) ((JList) c).getSelectedValue();

            return new TemplateFieldTransferable(set);
        }

        @Override
        protected void exportDone(JComponent c, Transferable t, int action) {
            // focus source component if drop failed
            if (action == NONE) {
                HIRuntime.getGui().focusComponent(editor);

            }
        }

        // -------- import methods ---------
        @Override
        public boolean canImport(TransferSupport supp) {
            // focus target component if necessary
            HIRuntime.getGui().focusComponent(editor);

            if (!supp.isDataFlavorSupported(TemplateFieldTransferable.fieldFlavor))
                return false;

            // check user role
            if (!HIRuntime.getGui().checkAdminAbility(true))
                return false;

            // extract template field to set appropriate drop action
            try {
                HiFlexMetadataSet set = (HiFlexMetadataSet) supp.getTransferable().getTransferData(TemplateFieldTransferable.fieldFlavor);
                supp.setDropAction(MOVE);
                return true;
            } catch (UnsupportedFlavorException e) {
                // no template field or other error occurred
                return false;
            } catch (IOException e) {
                // no template field or other error occurred
                return false;
            }
        }

        @Override
        public boolean importData(TransferSupport supp) {
            if (!canImport(supp)) // check if we support this element
                return false;

            // check user role
            if (!HIRuntime.getGui().checkAdminAbility(false))
                return false;

            // Fetch the Transferable and its data
            Transferable t = supp.getTransferable();
            HiFlexMetadataSet set = null;
            try {
                set = (HiFlexMetadataSet) t.getTransferData(TemplateFieldTransferable.fieldFlavor);
                if ( set == null )
                    return false;

                // Fetch the drop location
                int index = templateFieldsView.getFieldsList().locationToIndex(supp.getDropLocation().getDropPoint());
                index = Math.max(0, index);
                index = Math.min(templateFieldsView.getFieldsList().getModel().getSize() - 1, index);

                // try to find field in our list
                int listIndex = -1; // index of field in our list
                for (int i = 0; i < templateFieldsView.getFieldsList().getModel().getSize(); i++)
                    if ( ((HiFlexMetadataSet)templateFieldsView.getFieldsList().getModel().getElementAt(i)).getId() == set.getId())
                        listIndex = i;

                if ( listIndex < 0 ) return false;

                // sort template fields
                HiFlexMetadataSet listSet = (HiFlexMetadataSet) templateFieldsView.getFieldsList().getModel().getElementAt(listIndex);
                DefaultListModel model = (DefaultListModel) templateFieldsView.getFieldsList().getModel();
                model.remove(listIndex);
                model.add(index, listSet);
                templateFieldsView.getFieldsList().setSelectedIndex(index);

                // update sort order on server
                boolean success = updateFieldSortOrder();
                // propagate changes
                templateSetView.setTemplates(HIRuntime.getManager().getProject().getTemplates());
                HIRuntime.getGui().sendMessage(HIMessageTypes.TEMPLATE_CHANGED, null, editor);
                HIRuntime.getGui().sendMessage(HIMessageTypes.PREFERENCE_MODIFIED, null, editor);
                HIRuntime.getGui().stopIndicatingServiceActivity();

                return success;


            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }



    // --------------------------------------------------------------------------------------------------




    private TemplateSetView templateSetView;
    private TemplateFieldsView templateFieldsView;

    private AddTemplateDialog addTemplateDialog;
    private EditTemplateDialog editTemplateDialog;
    private AddTemplateFieldDialog addTemplateFieldDialog;
    private EditTemplateFieldDialog editTemplateFieldDialog;


    public TemplateEditor() {
        super(Messages.getString("TemplateEditor.PROJEKT_TEMPLATES"), Messages.getString("TemplateEditor.TEMPLATEEDITOR"));

        // init views
        templateSetView = new TemplateSetView(HIRuntime.getManager().getProject().getTemplates());
        templateFieldsView = new TemplateFieldsView(templateSetView.getSelectedTemplate());

        // init dialogs
        addTemplateDialog = new AddTemplateDialog(HIRuntime.getGui());
        editTemplateDialog = new EditTemplateDialog(HIRuntime.getGui());
        addTemplateFieldDialog = new AddTemplateFieldDialog(HIRuntime.getGui());
        editTemplateFieldDialog = new EditTemplateFieldDialog(HIRuntime.getGui());

        // register views
        views.add(templateSetView);
        views.add(templateFieldsView);

        // init Drag and Drop handler
        templateSetView.getTemplateList().setTransferHandler(new TemplateTransferHandler(this));
        templateFieldsView.getFieldsList().setTransferHandler(new TemplateFieldTransferHandler(this));

        // attach listeners
        templateSetView.getTemplateList().addListSelectionListener(this);
        templateSetView.getAddButton().addActionListener(this);
        templateSetView.getRemoveButton().addActionListener(this);
        templateSetView.getEditButton().addActionListener(this);
        templateFieldsView.getAddButton().addActionListener(this);
        templateFieldsView.getRemoveButton().addActionListener(this);
        templateFieldsView.getEditButton().addActionListener(this);

        updateLanguage();
    }

    @Override
    public void updateLanguage() {
        super.updateLanguage();

        addTemplateDialog.updateLanguage();
        addTemplateFieldDialog.updateLanguage();
        editTemplateDialog.updateLanguage();
        editTemplateFieldDialog.updateLanguage();

        setTitle(Messages.getString("TemplateEditor.PROJEKT_TEMPLATES"));
        getWindowMenuItem().setText(getTitle());
        HIRuntime.getGui().updateComponentTitle(this);
    }

    private boolean updateTemplateSortOrder() {
        // check user role
        if (!HIRuntime.getGui().checkAdminAbility(true))
            return false;

        // compile sort order
        String sortOrder = "";
        for (int i = 0; i < templateSetView.getTemplateList().getModel().getSize(); i++) {
            HiFlexMetadataTemplate template = (HiFlexMetadataTemplate) templateSetView.getTemplateList().getModel().getElementAt(i);
            sortOrder = sortOrder + "," + template.getId();
        }
        // remove leading ","
        if (sortOrder.length() > 0)
            sortOrder = sortOrder.substring(1);

        try {
            // update sort order on server
            HIRuntime.getGui().startIndicatingServiceActivity();
            HiPreference tempSortOrderPref = MetadataHelper.findPreference(HIRuntime.getManager().getProject(), "templateSortOrder");
            tempSortOrderPref.setValue(sortOrder);
            HIRuntime.getManager().updatePreference(tempSortOrderPref);
            HIRuntime.getGui().stopIndicatingServiceActivity();

            return true;
        } catch (HIWebServiceException wse) {
            HIRuntime.getGui().reportError(wse, this);
            return false;
        }

    }


    private boolean updateFieldSortOrder() {
        // check user role
        if (!HIRuntime.getGui().checkAdminAbility(true))
            return false;

        // compile sort order
        String sortOrder = "";
        for ( int i=0; i < templateFieldsView.getFieldsList().getModel().getSize(); i++ ) {
            HiFlexMetadataSet set = (HiFlexMetadataSet) templateFieldsView.getFieldsList().getModel().getElementAt(i);
            sortOrder = sortOrder + ","+set.getId();
        }
        // remove leading ","
        if (sortOrder.length() > 0)
            sortOrder = sortOrder.substring(1);

        // update sort order on server
        try {
            HIRuntime.getGui().startIndicatingServiceActivity();
            HIRuntime.getManager().updateTemplateSortOrder(templateSetView.getSelectedTemplate(), sortOrder);
            HIRuntime.getGui().stopIndicatingServiceActivity();

            return true;
        } catch (HIWebServiceException wse) {
            HIRuntime.getGui().reportError(wse, this);
            return false;
        }
        
    }



    // ------------------------------------------------------------------------------------------------------



    @Override
    public void valueChanged(ListSelectionEvent e) {
        if ( templateSetView.getSelectedTemplate() != null )
            templateFieldsView.setTemplate(templateSetView.getSelectedTemplate());
    }



    // ------------------------------------------------------------------------------------------------------



    @Override
    public void actionPerformed(ActionEvent e) {
        /*
         * Add Template
         *
         */
        if ( e.getActionCommand().compareTo("addTemplate") == 0  ) {
            // get user input and check if user clicked save button
            if ( addTemplateDialog.showAddTemplateFieldDialog(HIRuntime.getManager().getProject().getTemplates()) ) {
                // add new template on server
                try {
                    HIRuntime.getGui().startIndicatingServiceActivity();
                    // Dublin Core
                    if ( addTemplateDialog.getTemplateChoice().compareTo("dc") == 0 )
                        HIRuntime.getManager().addTemplateToProject(MetadataHelper.getDCTemplateBlueprint());
                    if ( addTemplateDialog.getTemplateChoice().compareTo("dcRichText") == 0 ) {
                        HiFlexMetadataTemplate dcTemp = MetadataHelper.getDCTemplateBlueprint();
                        for ( HiFlexMetadataSet set : dcTemp.getEntries() )
                            set.setRichText(true);
                        HIRuntime.getManager().addTemplateToProject(dcTemp);
                    }

                    // CDWA Lite
                    if ( addTemplateDialog.getTemplateChoice().compareTo("cdwa") == 0 )
                        HIRuntime.getManager().addTemplateToProject(MetadataHelper.getCDWALiteTemplateBlueprint());
                    if ( addTemplateDialog.getTemplateChoice().compareTo("cdwaRichText") == 0 ) {
                        HiFlexMetadataTemplate cdwaTemp = MetadataHelper.getCDWALiteTemplateBlueprint();
                        for ( HiFlexMetadataSet set : cdwaTemp.getEntries() )
                            set.setRichText(true);
                        HIRuntime.getManager().addTemplateToProject(cdwaTemp);
                    }
                    
                    // VRA Core 4
/*                    
                    if ( addTemplateDialog.getTemplateChoice().compareTo("vra4") == 0 )
                        HIRuntime.getManager().addTemplateToProject(MetadataHelper.getVRACore4TemplateBlueprint());
*/
                    if ( addTemplateDialog.getTemplateChoice().compareTo("vra4RichText") == 0 ) {
                        HiFlexMetadataTemplate vra4Template = MetadataHelper.getVRACore4TemplateBlueprint();
                        for ( HiFlexMetadataSet set : vra4Template.getEntries() )
                            set.setRichText(true);
                        HIRuntime.getManager().addTemplateToProject(vra4Template);
                    }

                    // Custom
                    if (addTemplateDialog.getTemplateChoice().compareTo("custom") == 0)
                        HIRuntime.getManager().addTemplateToProject(MetadataHelper.getCustomTemplateBlueprint());


                    // propagate changes
                    templateSetView.setTemplates(HIRuntime.getManager().getProject().getTemplates());
                    HIRuntime.getGui().sendMessage(HIMessageTypes.TEMPLATE_CHANGED, null, this);
                    HIRuntime.getGui().sendMessage(HIMessageTypes.PREFERENCE_MODIFIED, null, this);
                    HIRuntime.getGui().stopIndicatingServiceActivity();

                } catch (HIWebServiceException wse) {
                    HIRuntime.getGui().reportError(wse, this);
                }
            }
        }
        

        /*
         * Edit Template
         *
         */
        if ( e.getActionCommand().compareTo("editTemplate") == 0 && templateSetView.getSelectedTemplate() != null && templateSetView.getSelectedTemplate().getNamespacePrefix().equalsIgnoreCase("custom") ) {
            // display dialog
            if ( editTemplateDialog.showEditTemplateDialog(templateSetView.getSelectedTemplate()) ) {
                // user clicked save --> check for actual changes
                if ( editTemplateDialog.getChangedURI().compareTo(templateSetView.getSelectedTemplate().getNamespaceURI()) != 0
                     || editTemplateDialog.getChangedURL().compareTo(templateSetView.getSelectedTemplate().getNamespaceURL()) != 0 ) {

                    // update template URI / URL on server
                    try {
                        HIRuntime.getGui().startIndicatingServiceActivity();
                        HIRuntime.getManager().updateTemplate(templateSetView.getSelectedTemplate(), editTemplateDialog.getChangedURI(), editTemplateDialog.getChangedURL());
                        
                        // propagate changes
                        templateSetView.setTemplates(HIRuntime.getManager().getProject().getTemplates());
                        HIRuntime.getGui().sendMessage(HIMessageTypes.TEMPLATE_CHANGED, null, this);
                        HIRuntime.getGui().sendMessage(HIMessageTypes.PREFERENCE_MODIFIED, null, this);
                        HIRuntime.getGui().stopIndicatingServiceActivity();
                        
                    } catch (HIWebServiceException wse) {
                        HIRuntime.getGui().reportError(wse, this);
                    }

                }
            }
        }


        /*
         * Remove Template Set (Field)
         *
         */
         if ( e.getActionCommand().compareTo("removeTemplate") == 0 && templateSetView.getSelectedTemplate() != null && templateSetView.getSelectedTemplate().getNamespacePrefix().compareTo("HIInternal") != 0 ) {
            // display warning to user
            boolean removeTemplate = HIRuntime.getGui().displayUserYesNoDialog(
                    Messages.getString("TemplateEditor.TEMPLATE_LOESCHEN"),
                    Messages.getString("TemplateEditor.ACHTUNG")+": "+Messages.getString("TemplateEditor.TEMPLATE_NOTE")+"\n"+
                    Messages.getString("TemplateEditor.TEMPLATE_NOTE_INFO")+"\n\n"+
                    Messages.getString("TemplateEditor.CONTINUE_NOTE"));

            if ( removeTemplate ) {
                // remove template on server
                try {
                    HIRuntime.getGui().startIndicatingServiceActivity();
                    HIRuntime.getManager().removeTemplateFromProject(templateSetView.getSelectedTemplate());

                    // propagate changes
                    templateSetView.setTemplates(HIRuntime.getManager().getProject().getTemplates());
                    HIRuntime.getGui().sendMessage(HIMessageTypes.TEMPLATE_CHANGED, null, this);
                    HIRuntime.getGui().sendMessage(HIMessageTypes.PREFERENCE_MODIFIED, null, this);
                    HIRuntime.getGui().stopIndicatingServiceActivity();

                } catch (HIWebServiceException wse) {
                    HIRuntime.getGui().reportError(wse, this);
                }
            }
         }


        /*
         * Add Template Set (Field)
         *
         */
        if ( e.getActionCommand().compareTo("addField") == 0 && templateSetView.getSelectedTemplate() != null && templateSetView.getSelectedTemplate().getNamespacePrefix().equalsIgnoreCase("custom") ) {
            // get user input and check if user clicked save button
            if ( addTemplateFieldDialog.showAddTemplateFieldDialog(templateSetView.getSelectedTemplate()) ) {
                // add new set on server
                try {
                    HIRuntime.getGui().startIndicatingServiceActivity();
                    HIRuntime.getManager().addSetToTemplate(templateSetView.getSelectedTemplate(), addTemplateFieldDialog.getTagName(), addTemplateFieldDialog.isRichText());

                    // propagate changes
                    templateSetView.setTemplates(HIRuntime.getManager().getProject().getTemplates());
                    HIRuntime.getGui().sendMessage(HIMessageTypes.TEMPLATE_CHANGED, null, this);
                    HIRuntime.getGui().sendMessage(HIMessageTypes.PREFERENCE_MODIFIED, null, this);
                    HIRuntime.getGui().stopIndicatingServiceActivity();
                    
                } catch (HIWebServiceException wse) {
                    HIRuntime.getGui().reportError(wse, this);
                }
            }
        }


        /*
         * Remove Template Set (Field)
         *
         */
        if ( e.getActionCommand().compareTo("removeField") == 0 && templateFieldsView.getSelectedSet() != null && templateSetView.getSelectedTemplate().getNamespacePrefix().equalsIgnoreCase("custom") ) {
            // display warning to user
            boolean removeField = HIRuntime.getGui().displayUserYesNoDialog(
                    Messages.getString("TemplateEditor.METADATENFELD_LOESCHEN"),
                    Messages.getString("TemplateEditor.ACHTUNG")+": "+Messages.getString("TemplateEditor.REMOVE_NOTE")+"\n"+
                    Messages.getString("TemplateEditor.REMOVE_NOTE_INFO")+"\n\n"+
                    Messages.getString("TemplateEditor.REMOVE_CONTINUE"));

            if ( removeField ) {
                // remove metadata field from template on server
                try {
                    HIRuntime.getGui().startIndicatingServiceActivity();
                    HIRuntime.getManager().removeSetFromTemplate(templateSetView.getSelectedTemplate(), templateFieldsView.getSelectedSet());

                    // propagate changes
                    templateSetView.setTemplates(HIRuntime.getManager().getProject().getTemplates());
                    HIRuntime.getGui().sendMessage(HIMessageTypes.TEMPLATE_CHANGED, null, this);
                    HIRuntime.getGui().sendMessage(HIMessageTypes.PREFERENCE_MODIFIED, null, this);
                    HIRuntime.getGui().stopIndicatingServiceActivity();

                } catch (HIWebServiceException wse) {
                    HIRuntime.getGui().reportError(wse, this);
                }
            }
        }


        /*
         * Edit Field Display Name
         */
        if ( e.getActionCommand().compareTo("editField") == 0 && templateFieldsView.getSelectedSet() != null ) {
            editTemplateFieldDialog.showEditFieldDialog(templateFieldsView.getSelectedSet(), templateFieldsView.getSelectedScreenLanguage());

            // check for user changes
            String displayName = MetadataHelper.getTemplateKeyDisplayName(null, templateFieldsView.getSelectedSet(), MetadataHelper.localeToLangID(templateFieldsView.getSelectedScreenLanguage()));
            if ( displayName == null || displayName.length() == 0 ) displayName = "";
            if ( editTemplateFieldDialog.getChangedDisplayName().compareTo(displayName) != 0 ) {
                try {
                    if ( !MetadataHelper.setHasDisplayNameEntry(
                            templateFieldsView.getSelectedSet(), 
                            MetadataHelper.localeToLangID(templateFieldsView.getSelectedScreenLanguage())
                         ) ) {
                        // create new display name on server
                        HIRuntime.getManager().createSetDisplayName(
                                templateFieldsView.getSelectedSet(),
                                MetadataHelper.localeToLangID(templateFieldsView.getSelectedScreenLanguage()),
                                editTemplateFieldDialog.getChangedDisplayName()
                        );

                    } else {
                        // update display name on server
                        HIRuntime.getGui().startIndicatingServiceActivity();
                        HIRuntime.getManager().updateSetDisplayName(
                                templateFieldsView.getSelectedSet(),
                                MetadataHelper.localeToLangID(templateFieldsView.getSelectedScreenLanguage()),
                                editTemplateFieldDialog.getChangedDisplayName());
                    }
                    // propagate changes
                    templateSetView.setTemplates(HIRuntime.getManager().getProject().getTemplates());
                    HIRuntime.getGui().sendMessage(HIMessageTypes.TEMPLATE_CHANGED, null, this);
                    HIRuntime.getGui().sendMessage(HIMessageTypes.PREFERENCE_MODIFIED, null, this);

                    HIRuntime.getGui().stopIndicatingServiceActivity();
                } catch (HIWebServiceException wse) {
                    HIRuntime.getGui().reportError(wse, this);
                }

            }
        }
    }

}
