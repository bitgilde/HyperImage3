/*
 * Copyright 2015 bitGilde IT Solutions UG (haftungsbeschränkt)
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
package org.hyperimage.client.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.dialogs.EditTagDialog;
import org.hyperimage.client.gui.views.TagManagerView;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiGroup;

/**
 *
 * @author Jens-Martin Loebel <loebel@bitgilde.de>
 */
public class TagManager extends HIComponent implements ActionListener {

    private TagManagerView tagManagerView;
    private List<HiGroup> tags;
    
    private EditTagDialog editTagDialog;
    
    
    public TagManager() {
        super(Messages.getString("TagManager.title"), Messages.getString("TagManager.menuTitle"));
        
        loadTags();
        
        // init dialogs
        editTagDialog = new EditTagDialog(HIRuntime.getGui());
        
        // init views
        tagManagerView = new TagManagerView(tags);
        
        // register views
        views.add(tagManagerView);
        
        // attach listeners
        tagManagerView.getAddButton().addActionListener(this);
        tagManagerView.getRemoveButton().addActionListener(this);
        tagManagerView.getEditButton().addActionListener(this);

    }
    
    private void loadTags() {
        HIRuntime.getGui().startIndicatingServiceActivity();
        try {
            tags = HIRuntime.getManager().getTags();
            HIRuntime.getGui().stopIndicatingServiceActivity();
        } catch (HIWebServiceException wse) {
            HIRuntime.getGui().reportError(wse, this);
        }
    }
    
    private void updateSelectedTag(HiGroup tag) {
        // save changes on server
        try {
            HIRuntime.getGui().startIndicatingServiceActivity();
            HIRuntime.getManager().updateFlexMetadataRecords(tag.getMetadata());

            tagManagerView.updateList();

            // propagate changes
            HIRuntime.getGui().sendMessage(HIMessageTypes.TAG_ADDED, tag, this);
            HIRuntime.getGui().stopIndicatingServiceActivity();

        } catch (HIWebServiceException wse) {
            HIRuntime.getGui().reportError(wse, this);
        }
    }
    
    // -------------------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        /*
         * Add Tag to Project
         */
        if ( e.getActionCommand().compareTo("addTag") == 0) {
            HiGroup newTag = null;
            // add new tag on server
            try {
                HIRuntime.getGui().startIndicatingServiceActivity();
                newTag = HIRuntime.getManager().createTagGroup();
                // create default tag name
                for ( HiFlexMetadataRecord record : newTag.getMetadata() ) {
                    MetadataHelper.setValue("HIBase", "title", "Tag ("+newTag.getUUID().split("-")[4]+")", record);
                }
                HIRuntime.getManager().updateFlexMetadataRecords(newTag.getMetadata());

                // update tag list view
                tagManagerView.updateList();

                // propagate changes
                HIRuntime.getGui().sendMessage(HIMessageTypes.TAG_ADDED, newTag, this);
                HIRuntime.getGui().stopIndicatingServiceActivity();

            } catch (HIWebServiceException wse) {
                HIRuntime.getGui().reportError(wse, this);
            }
            if ( newTag != null ) {
                editTagDialog.setTag(newTag);
                editTagDialog.setVisible(true);
                if ( editTagDialog.saveSelected() ) {
                    updateSelectedTag(newTag);
                }
                tagManagerView.updateList();
            }
        }

        /*
         * Edit Tag Name
         */
        if ( e.getActionCommand().compareTo("editTag") == 0 ) {
            editTagDialog.setTag((HiGroup)tagManagerView.getSelectedTag());
            editTagDialog.setVisible(true);
            if ( editTagDialog.saveSelected() ) {
                updateSelectedTag(((HiGroup) tagManagerView.getSelectedTag()));
                
            }
        }
    
        /*
         * Remove Tag
         */
        if ( e.getActionCommand().compareTo("removeTag") == 0) {
            // display warning to user
            boolean removeTag = HIRuntime.getGui().displayUserYesNoDialog(
                    Messages.getString("TagManagerView.deleteTag"),
                    Messages.getString("TagManagerView.warning") + ": " + Messages.getString("TagManagerView.removeNote") + "\n\n"
                    + Messages.getString("TagManagerView.continueNote"));

            if (removeTag) {
                // remove metadata field from template on server
                try {
                    HIRuntime.getGui().startIndicatingServiceActivity();
                    HiGroup tag = tagManagerView.getSelectedTag();
                    boolean success = HIRuntime.getManager().deleteGroup(tag);

                    // propagate changes
                    HIRuntime.getGui().sendMessage(HIMessageTypes.TAG_REMOVED, tag, this);
                    HIRuntime.getGui().stopIndicatingServiceActivity();
                    
                    // update GUI
                    tagManagerView.updateList();

                } catch (HIWebServiceException wse) {
                    HIRuntime.getGui().reportError(wse, this);
                }
            }
        }
    }
    
}
