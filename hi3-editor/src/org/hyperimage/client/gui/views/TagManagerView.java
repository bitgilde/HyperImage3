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
package org.hyperimage.client.gui.views;

import java.awt.Dimension;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.exception.HIWebServiceException;
import org.hyperimage.client.gui.EditButton;
import org.hyperimage.client.gui.lists.TagListCellRenderer;
import org.hyperimage.client.ws.HiGroup;

/**
 *
 * @author Jens-Martin Loebel <loebel@bitgilde.de>
 */
public class TagManagerView extends GUIView implements ListSelectionListener {

    private static final long serialVersionUID = 1L;

    private List<HiGroup> tags;
    
    private JButton addTagButton;
    private EditButton editTagButton;
    private JButton removeTagButton;
    private JList tagList;
    private DefaultListModel tagListModel;
    private JPanel tagListPanel;
    private JScrollPane tagListScroll;
    

    public TagManagerView(List<HiGroup> tags) {
        super(Messages.getString("TagManagerView.title"));

        this.tags = tags;
        initComponents();
        updateLanguage();
        setTagList();
        updateButtonState();
        
        // Drag and Drop
        
        
        // attach listeners
        tagList.addListSelectionListener(this);

        
        setDisplayPanel(tagListPanel);

    }
    
    public JButton getAddButton() {
        return addTagButton;
    }
    
    public JButton getEditButton() {
        return editTagButton;
    }

    public JButton getRemoveButton() {
        return removeTagButton;
    }

    public void updateList() {
        setTagList();
        tagList.repaint();
    }

    private void setTagList() {
       int savedIndex = -1;
        
        HiGroup selectedTag = null; 
        if ( tagList.getSelectedIndex() >= 0 ) selectedTag = (HiGroup) tagList.getSelectedValue();
       
        tagListModel.removeAllElements();
        
        int i = 0;
        for (HiGroup tag : tags) {
            tagListModel.addElement(tag);
            if ( tag != null && selectedTag != null ) if ( tag.getId() == selectedTag.getId() ) savedIndex = i;
            i = i + 1;
        }
        
        if ( tagListModel.size() > 0 ) {
            if ( savedIndex < 0 ) tagList.setSelectedIndex(0);
            else tagList.setSelectedIndex(savedIndex);
        }
    }
    
    private void updateButtonState() {
        editTagButton.setEnabled(tagList.getSelectedIndex() >= 0);
        removeTagButton.setEnabled(tagList.getSelectedIndex() >= 0);
    }
    
    @Override
    public void updateLanguage() {
        menuTitle.setText(Messages.getString("TagManagerView.title"));
        addTagButton.setToolTipText(Messages.getString("TagManagerView.addTag"));
        editTagButton.setToolTipText(Messages.getString("TagManagerView.editTag"));
        removeTagButton.setToolTipText(Messages.getString("TagManagerView.removeTag"));
    }

    private void initComponents() {
        tagListPanel = new JPanel();
        tagListScroll = new JScrollPane();
        tagList = new JList();
        editTagButton = new EditButton();
        addTagButton = new JButton();
        removeTagButton = new JButton();

        tagListScroll.setViewportView(tagList);

        editTagButton.setPreferredSize(new Dimension(24, 24));

        addTagButton.setPreferredSize(new Dimension(24, 24));

        removeTagButton.setPreferredSize(new Dimension(24, 24));

        GroupLayout tagListPanelLayout = new GroupLayout(tagListPanel);
        tagListPanel.setLayout(tagListPanelLayout);
        tagListPanelLayout.setHorizontalGroup(tagListPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(tagListPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(tagListPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(tagListScroll, GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                                .addGroup(tagListPanelLayout.createSequentialGroup()
                                        .addComponent(editTagButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(addTagButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(removeTagButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
        );
        tagListPanelLayout.setVerticalGroup(tagListPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(tagListPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tagListScroll, GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(tagListPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(editTagButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(removeTagButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(addTagButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
        );
        
        // ------

        addTagButton.setActionCommand("addTag");
        addTagButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add.png")));
        addTagButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add-active.png")));
        addTagButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add-disabled.png")));
        removeTagButton.setActionCommand("removeTag");
        removeTagButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove.png")));
        removeTagButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove-active.png")));
        removeTagButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove-disabled.png")));
        editTagButton.setActionCommand("editTag");

        tagListModel = new DefaultListModel();
        tagList.setModel(tagListModel);
        tagList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tagList.setCellRenderer(new TagListCellRenderer());

    }

    // -------------------------------------------------------------------------
    
    @Override
    public void valueChanged(ListSelectionEvent e) {
        updateButtonState();
    }

    public HiGroup getSelectedTag() {
        return (HiGroup)tagList.getSelectedValue();
    }

}
