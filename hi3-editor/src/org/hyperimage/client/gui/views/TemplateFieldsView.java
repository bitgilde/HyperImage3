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

package org.hyperimage.client.gui.views;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.EditButton;
import org.hyperimage.client.gui.lists.TemplateFieldsListCellRenderer;
import org.hyperimage.client.ws.HiFlexMetadataSet;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;

/**
 *
 * @author Jens-Martin Loebel
 */
public class TemplateFieldsView extends GUIView implements ActionListener, ListSelectionListener {

    private JButton addFieldButton;
    private EditButton editFieldButton;
    private JButton removeFieldButton;
    private JPanel fieldsButtonPanel;
    private JList fieldsList;
    private DefaultListModel fieldsListModel;
    private JScrollPane fieldsListScrollPane;
    private JComboBox screenLanguageComboBox;
    private JLabel screenLanguageLabel;
    private JPanel templateFieldsPanel;

    private HiFlexMetadataTemplate template;
    private TemplateFieldsListCellRenderer fieldsListRenderer;
    private int lastFieldIndex = -1;
    private HiFlexMetadataSet savedSet = null;


    public TemplateFieldsView(HiFlexMetadataTemplate template) {
        super(Messages.getString("TemplateFieldsView.TEMPLATEFELDER"));
        this.template = template;

        initComponents();
        updateFieldsList();
        updateLanguage();

        // Drag and Drop
        fieldsList.setDragEnabled(true);
        fieldsList.setDropMode(DropMode.INSERT);

        // attach listeners
        fieldsList.addListSelectionListener(this);
        screenLanguageComboBox.addActionListener(this);
        updateButtonState();

        setDisplayPanel(templateFieldsPanel);
    }


    @Override
    public void updateLanguage() {
        menuTitle.setText(Messages.getString("TemplateFieldsView.TEMPLATEFELDER"));
        screenLanguageLabel.setText(Messages.getString("TemplateFieldsView.BILDSCHIRMSPRACHE"));
        addFieldButton.setToolTipText(Messages.getString("TemplateFieldsView.METADATENFELD_HINZUFUEGEN"));
        editFieldButton.setToolTipText(Messages.getString("TemplateFieldsView.METADATENFELD_ANZEIGENAME_BEARBEITEN"));
        removeFieldButton.setToolTipText(Messages.getString("TemplateFieldsView.METADATENFELD_LOESCHEN"));

        int index = screenLanguageComboBox.getSelectedIndex();
        // scan and set appropriate on screen language
        screenLanguageComboBox.removeAllItems();
	for ( Locale guiLang : HIRuntime.supportedLanguages )
            screenLanguageComboBox.addItem(guiLang.getDisplayLanguage());
        screenLanguageComboBox.setSelectedIndex(index);
    }


    public void setTemplate(HiFlexMetadataTemplate template) {
        if ( template == null ) return;

        savedSet = null;
        if ( this.template != null && this.template.getNamespacePrefix().compareTo(template.getNamespacePrefix()) == 0 && fieldsList.getSelectedIndex() >= 0 )
            savedSet = getSelectedSet();

        this.template = template;
        fieldsListRenderer.setReferenceTemplate(template);
        updateFieldsList();
    }

    public Locale getSelectedScreenLanguage() {
        return HIRuntime.supportedLanguages[screenLanguageComboBox.getSelectedIndex()];
    }

    public HiFlexMetadataSet getSelectedSet() {
        if ( fieldsList.getSelectedIndex() < 0 ) return null;

        return (HiFlexMetadataSet) fieldsListModel.get(fieldsList.getSelectedIndex());
    }

    public JButton getAddButton() {
        return this.addFieldButton;
    }
    public JButton getRemoveButton() {
        return this.removeFieldButton;
    }
    public JButton getEditButton() {
        return this.editFieldButton;
    }
    public JList getFieldsList() {
        return this.fieldsList;
    }


    private void updateFieldsList() {
        lastFieldIndex = -1;
        fieldsListModel.removeAllElements();
        for ( HiFlexMetadataSet set : template.getEntries() )
            fieldsListModel.addElement(set);

        if ( fieldsListModel.size() > 0 ) {
            if ( savedSet != null ) {
                int newIndex = 0;
                for ( int index=0; index < fieldsListModel.size(); index++ )
                    if ( template.getEntries().get(index).getId() == savedSet.getId() )
                        newIndex = index;
                fieldsList.setSelectedIndex(newIndex);
            } else fieldsList.setSelectedIndex(0);
        }
        updateButtonState();
    }


    private void updateButtonState() {
        editFieldButton.setEnabled(false);
        if ( fieldsList.getSelectedIndex() >= 0 && template.getNamespacePrefix().toLowerCase().compareTo("custom") == 0 )
            editFieldButton.setEnabled(true);

        if ( template.getNamespacePrefix().toLowerCase().compareTo("custom") == 0 ) {
            addFieldButton.setEnabled(true);
            if ( fieldsListModel.size() > 0 && fieldsList.getSelectedIndex() >= 0 )
                removeFieldButton.setEnabled(true);
            else removeFieldButton.setEnabled(false);
        } else {
            addFieldButton.setEnabled(false);
            removeFieldButton.setEnabled(false);
        }
    }


    @SuppressWarnings("unchecked")
    private void initComponents() {

        templateFieldsPanel = new JPanel();
        screenLanguageLabel = new JLabel();
        screenLanguageComboBox = new JComboBox();
        fieldsListScrollPane = new JScrollPane();
        fieldsList = new JList();
        fieldsButtonPanel = new JPanel();
        editFieldButton = new EditButton();
        addFieldButton = new JButton();
        removeFieldButton = new JButton();

        screenLanguageLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        fieldsListScrollPane.setViewportView(fieldsList);

        addFieldButton.setBorder(BorderFactory.createEmptyBorder());
        addFieldButton.setPreferredSize(new Dimension(24, 24));

        removeFieldButton.setBorder(BorderFactory.createEmptyBorder());
        removeFieldButton.setPreferredSize(new Dimension(24, 24));

        GroupLayout fieldsButtonPanelLayout = new GroupLayout(fieldsButtonPanel);
        fieldsButtonPanel.setLayout(fieldsButtonPanelLayout);
        fieldsButtonPanelLayout.setHorizontalGroup(
            fieldsButtonPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(fieldsButtonPanelLayout.createSequentialGroup()
                .addComponent(editFieldButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, 264, Short.MAX_VALUE)
                .addComponent(addFieldButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(removeFieldButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
        fieldsButtonPanelLayout.setVerticalGroup(
            fieldsButtonPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(fieldsButtonPanelLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(editFieldButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(removeFieldButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(addFieldButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

        GroupLayout templateFieldsPanelLayout = new GroupLayout(templateFieldsPanel);
        templateFieldsPanel.setLayout(templateFieldsPanelLayout);
        templateFieldsPanelLayout.setHorizontalGroup(
            templateFieldsPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(templateFieldsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(templateFieldsPanelLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(fieldsListScrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                    .addGroup(Alignment.TRAILING, templateFieldsPanelLayout.createSequentialGroup()
                        .addComponent(screenLanguageLabel, GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(screenLanguageComboBox, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE))
                    .addComponent(fieldsButtonPanel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        templateFieldsPanelLayout.setVerticalGroup(
            templateFieldsPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(templateFieldsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(templateFieldsPanelLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(screenLanguageComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(screenLanguageLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(fieldsListScrollPane, GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(fieldsButtonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );


        // ------

        addFieldButton.setActionCommand("addField"); //$NON-NLS-1$
        addFieldButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add.png"))); //$NON-NLS-1$
        addFieldButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add-active.png"))); //$NON-NLS-1$
        addFieldButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add-disabled.png"))); //$NON-NLS-1$
        removeFieldButton.setActionCommand("removeField"); //$NON-NLS-1$
        removeFieldButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove.png"))); //$NON-NLS-1$
        removeFieldButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove-active.png"))); //$NON-NLS-1$
        removeFieldButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove-disabled.png"))); //$NON-NLS-1$
        editFieldButton.setActionCommand("editField");

        // scan and set appropriate on screen language
	for ( Locale guiLang : HIRuntime.supportedLanguages )
            screenLanguageComboBox.addItem(guiLang.getDisplayLanguage());
        screenLanguageComboBox.setSelectedIndex(0);
        
        fieldsListModel = new DefaultListModel();
        fieldsList.setModel(fieldsListModel);
        fieldsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fieldsListRenderer = new TemplateFieldsListCellRenderer(template, HIRuntime.supportedLanguages[screenLanguageComboBox.getSelectedIndex()] );
        fieldsList.setCellRenderer(fieldsListRenderer);


    }



    // ------------------------------------------------------------------------------------------------------



    @Override
    public void actionPerformed(ActionEvent e) {
        if ( screenLanguageComboBox.getSelectedIndex() < 0 ) return;
        fieldsListRenderer.setScreenLanguage(HIRuntime.supportedLanguages[screenLanguageComboBox.getSelectedIndex()]);
        fieldsList.repaint();
    }



    // ------------------------------------------------------------------------------------------------------



    @Override
    public void valueChanged(ListSelectionEvent lse) {
        if ( fieldsList.getSelectedIndex() < 0 && fieldsListModel.size() > 0 && lastFieldIndex < fieldsListModel.size() ) {
            if ( lastFieldIndex < 0 ) lastFieldIndex = 0;
            fieldsList.setSelectedIndex(lastFieldIndex);
        }
        
        if ( lastFieldIndex != fieldsList.getSelectedIndex() ) {
            lastFieldIndex = fieldsList.getSelectedIndex();

            updateButtonState();
        }
    }


}
