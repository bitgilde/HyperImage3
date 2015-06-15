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

package org.hyperimage.client.gui.views;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.EditButton;
import org.hyperimage.client.gui.lists.TemplateListCellRenderer;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;

/**
 *
 * @author Jens-Martin Loebel
 */
public class TemplateSetView extends GUIView implements ListSelectionListener, MouseListener, MouseMotionListener {

    private JButton addTemplateButton;
    private EditButton editTemplateButton;
    private JButton removeTemplateButton;
    private JPanel templateButtonPanel;
    private JList templateList;
    private DefaultListModel templateListModel;
    private JScrollPane templateListScrollPane;
    private JPanel templateSetPanel;
    private int lastTemplateIndex = 0;

    private List<HiFlexMetadataTemplate> templates;
    

    public TemplateSetView(List<HiFlexMetadataTemplate> templates) {
        super(Messages.getString("TemplateSetView.TEMPLATES"));

        this.templates = templates;

        initComponents();
        updateLanguage();
        refreshTemplates();

        // Drag and Drop
        templateList.setDragEnabled(true);
        templateList.setDropMode(DropMode.INSERT);


        // attach listeners
        templateList.addListSelectionListener(this);
        templateList.addMouseListener(this);
        templateList.addMouseMotionListener(this);
        updateButtonState();

        setDisplayPanel(templateSetPanel);
    }

    
    @Override
    public void updateLanguage() {
        menuTitle.setText(Messages.getString("TemplateSetView.TEMPLATES"));
        addTemplateButton.setToolTipText(Messages.getString("TemplateSetView.TEMPLATE_ZUM_PROJEKT_HINZUFUEGEN"));
        editTemplateButton.setToolTipText(Messages.getString("TemplateSetView.TEMPLATE_BEARBEITEN"));
        removeTemplateButton.setToolTipText(Messages.getString("TemplateSetView.TEMPLATE_AUS_DEM_PROJEKT_LOESCHEN"));
    }

    public void setTemplates(List<HiFlexMetadataTemplate> templates) {
        this.templates = templates;
        refreshTemplates();
        updateButtonState();
    }


    private void refreshTemplates() {
        int savedIndex = -1;

        HiFlexMetadataTemplate template = null;
        if ( templateList.getSelectedIndex() >= 0 ) 
            template = (HiFlexMetadataTemplate) templateListModel.getElementAt(templateList.getSelectedIndex());

        // TODO disable listener
        templateListModel.removeAllElements();
        int i = 0;
        for (HiFlexMetadataTemplate temp : templates) {
            if ( temp.getNamespacePrefix().compareTo("HIBase") != 0 ) { // do not show the base template
            templateListModel.addElement(temp);
                if (template != null)
                    if (temp.getId() == template.getId())
                        savedIndex = i;
                i = i + 1;
            }
        }
        // TODO enable listener

        if ( savedIndex < 0 || savedIndex >= templateListModel.size() ) templateList.setSelectedIndex(0);
        else templateList.setSelectedIndex(savedIndex);

    }


    public HiFlexMetadataTemplate getSelectedTemplate() {
        if ( templateList.getSelectedIndex() < 0 ) return null;
        return (HiFlexMetadataTemplate) templateListModel.get(templateList.getSelectedIndex());
    }

    public JList getTemplateList() {
        return this.templateList;
    }

    public JButton getAddButton() {
        return this.addTemplateButton;
    }
    public JButton getRemoveButton() {
        return this.removeTemplateButton;
    }
    public JButton getEditButton() {
        return this.editTemplateButton;
    }

    private void updateButtonState() {
        editTemplateButton.setEnabled(false);
        if ( getSelectedTemplate() == null ) return;
        if ( templateList.getSelectedIndex() >= 0 && getSelectedTemplate().getNamespacePrefix().toLowerCase().compareTo("custom") == 0 )
            editTemplateButton.setEnabled(true);

        removeTemplateButton.setEnabled(false);
        if ( templateListModel.size() > 1 && getSelectedTemplate().getNamespacePrefix().compareTo("HIInternal") != 0 )
            removeTemplateButton.setEnabled(true);

        addTemplateButton.setEnabled(true);
    }



    @SuppressWarnings("unchecked")
    private void initComponents() {

        templateSetPanel = new JPanel();
        templateListScrollPane = new JScrollPane();
        templateList = new JList();
        templateButtonPanel = new JPanel();
        editTemplateButton = new EditButton();
        addTemplateButton = new JButton();
        removeTemplateButton = new JButton();

        templateListScrollPane.setViewportView(templateList);

        addTemplateButton.setBorder(BorderFactory.createEmptyBorder());
        addTemplateButton.setPreferredSize(new Dimension(24, 24));

        removeTemplateButton.setBorder(BorderFactory.createEmptyBorder());
        removeTemplateButton.setPreferredSize(new Dimension(24, 24));

        GroupLayout templateButtonPanelLayout = new GroupLayout(templateButtonPanel);
        templateButtonPanel.setLayout(templateButtonPanelLayout);
        templateButtonPanelLayout.setHorizontalGroup(
            templateButtonPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, templateButtonPanelLayout.createSequentialGroup()
                .addComponent(editTemplateButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, 168, Short.MAX_VALUE)
                .addComponent(addTemplateButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(removeTemplateButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
        templateButtonPanelLayout.setVerticalGroup(
            templateButtonPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(templateButtonPanelLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(removeTemplateButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(editTemplateButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(addTemplateButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

        GroupLayout templateSetPanelLayout = new GroupLayout(templateSetPanel);
        templateSetPanel.setLayout(templateSetPanelLayout);
        templateSetPanelLayout.setHorizontalGroup(
            templateSetPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, templateSetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(templateSetPanelLayout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(templateListScrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                    .addComponent(templateButtonPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        templateSetPanelLayout.setVerticalGroup(
            templateSetPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, templateSetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(templateListScrollPane, GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(templateButtonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );


        // ------

        addTemplateButton.setActionCommand("addTemplate"); //$NON-NLS-1$
        addTemplateButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add.png"))); //$NON-NLS-1$
        addTemplateButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add-active.png"))); //$NON-NLS-1$
        addTemplateButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/group-add-disabled.png"))); //$NON-NLS-1$
        removeTemplateButton.setActionCommand("removeTemplate"); //$NON-NLS-1$
        removeTemplateButton.setIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove.png"))); //$NON-NLS-1$
        removeTemplateButton.setPressedIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove-active.png"))); //$NON-NLS-1$
        removeTemplateButton.setDisabledIcon(new ImageIcon(getClass().getResource("/resources/icons/group-remove-disabled.png"))); //$NON-NLS-1$
        editTemplateButton.setActionCommand("editTemplate");

        templateListModel = new DefaultListModel();
        templateList.setModel(templateListModel);
        templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateList.setCellRenderer(new TemplateListCellRenderer());

    }



    // ------------------------------------------------------------------------------------------------------



    @Override
    public void valueChanged(ListSelectionEvent e) {
        if ( templateList.getSelectedIndex() < 0 && templateListModel.size() > 0 && lastTemplateIndex < templateListModel.size() ) {
            if ( lastTemplateIndex < 0 ) lastTemplateIndex = 0;
            templateList.setSelectedIndex(lastTemplateIndex);
        }

        if ( lastTemplateIndex != templateList.getSelectedIndex() ) {
            lastTemplateIndex = templateList.getSelectedIndex();

            updateButtonState();
        }
    }



    // ------------------------------------------------------------------------------------------------------


    private void updateTemplateToolTip(Point location) {
        int index = templateList.locationToIndex(location);
        HiFlexMetadataTemplate template = null;
        if ( index >= 0 ) template = (HiFlexMetadataTemplate) templateListModel.get(index);

        if ( template != null ) {
          String text = "<html>";
          text = text + "<b>Namespace Prefix:</b> "+template.getNamespacePrefix()+"<br>";
          text = text + "<b>Namespace URI:</b> "+template.getNamespaceURI()+"<br>";
          text = text + "<b>Schema Location:</b> "+template.getNamespaceURL()+"</html>";
          templateList.setToolTipText(text);
        } else templateList.setToolTipText("");

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // not needed
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // not needed
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // not needed
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        updateTemplateToolTip(e.getPoint());
    }

    @Override
    public void mouseExited(MouseEvent e) {
        templateList.setToolTipText("");
    }



    // ------------------------------------------------------------------------------------------------------

    

    @Override
    public void mouseDragged(MouseEvent e) {
        // not needed
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateTemplateToolTip(e.getPoint());
    }



}
