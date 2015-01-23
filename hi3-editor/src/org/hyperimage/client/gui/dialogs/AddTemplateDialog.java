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
 * Copyright 2014, 2015 bitGilde IT Solutions UG (haftungsbeschränkt)
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

package org.hyperimage.client.gui.dialogs;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.ResetButton;
import org.hyperimage.client.gui.SaveButton;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;

/**
 * @author Jens-Martin Loebel
 */
public class AddTemplateDialog extends JDialog implements ActionListener {

    private JLabel infoLabel;
    private ResetButton cancelButton;
    private SaveButton saveButton;
    private JComboBox templateComboBox;
    private JPanel templatePanel;
    private TitledBorder templatePanelBorder;

    private boolean saveButtonClicked = false;
    private List<HiFlexMetadataTemplate> templates;
    private Vector<String> templateChoices = new Vector<String>();


    public AddTemplateDialog(JFrame parent) {
        super(parent);

        initComponents();
        updateLanguage();

        this.setBounds(
                (parent.getWidth() / 2) - (this.getWidth() / 2),
                (parent.getHeight() / 2) - (this.getHeight() / 2),
                this.getWidth(),
                this.getHeight());

        saveButton.addActionListener(this);
        cancelButton.addActionListener(this);
        templateComboBox.addActionListener(this);
    }

    public void updateLanguage() {
        this.setTitle(Messages.getString("AddTemplateDialog.TEMPLATE_HINZUFUEGEN"));
        templatePanelBorder.setTitle(Messages.getString("AddTemplateDialog.TEMPLATE_AUSWAEHLEN"));
        templatePanel.repaint();
        infoLabel.setText("<html>"+Messages.getString("AddTemplateDialog.HINWEIS")+"</html>");

        saveButton.setToolTipText(Messages.getString("AddTemplateDialog.TEMPLATE_HINZUFUEGEN"));
        cancelButton.setToolTipText(Messages.getString("AddTemplateDialog.ABBRUCH"));
        infoLabel.repaint();
    }


    public boolean showAddTemplateFieldDialog(List<HiFlexMetadataTemplate> templates) {
        this.templates = templates;
        saveButtonClicked = false;

        initTemplateChoices();

        // update on screen text
        this.setModal(false);
        this.setVisible(true);
        updateLanguage();
        this.setVisible(false);
        this.setModal(true);

        // show dialog
        this.setVisible(true);


        return saveButtonClicked;
    }

    public String getTemplateChoice() {
        if ( templateChoices.size() == 0 ) return "";
        return templateChoices.get(templateComboBox.getSelectedIndex());
    }


    private void initTemplateChoices() {
        templateChoices.removeAllElements();
        templateComboBox.removeAllItems();
        boolean dcFound = false;
        boolean cdwaFound = false;
        boolean vra4Found = false;
        boolean vra4HdlbgFound = false;
        boolean customFound = false;

        // scan templates
        saveButton.setEnabled(true);
        for ( HiFlexMetadataTemplate template : templates ) {
            if ( template.getNamespacePrefix().compareTo("dc") == 0 ) dcFound = true;
            if ( template.getNamespacePrefix().compareTo("cdwalite") == 0 ) cdwaFound = true;
            if ( template.getNamespacePrefix().compareTo("vra4") == 0 ) vra4Found = true;
            if ( template.getNamespacePrefix().compareTo("vra4hdlbg") == 0 ) vra4HdlbgFound = true;
            if ( template.getNamespacePrefix().equalsIgnoreCase("custom") ) customFound = true;
        }

        if ( !dcFound ) {
            templateChoices.addElement("dc");
            templateChoices.addElement("dcRichText");
            templateComboBox.addItem(Messages.getString("AddTemplateDialog.DUBLIN_CORE_LEGACY_SET"));
            templateComboBox.addItem(Messages.getString("AddTemplateDialog.DUBLIN_CORE_LEGACY_SET_RICH_TEXT"));
        }
        if ( !cdwaFound ) {
            templateChoices.addElement("cdwa");
            templateChoices.addElement("cdwaRichText");
            templateComboBox.addItem(Messages.getString("AddTemplateDialog.CDWA_LITE"));
            templateComboBox.addItem(Messages.getString("AddTemplateDialog.CDWA_LITE_RICH_TEXT"));
        }
        if ( !vra4Found ) {
            //templateChoices.addElement("vra4");
            templateChoices.addElement("vra4RichText");
            //templateComboBox.addItem(Messages.getString("AddTemplateDialog.VRA_CORE_4"));
            templateComboBox.addItem(Messages.getString("AddTemplateDialog.VRA_CORE_4_RICH_TEXT"));
        }
        if ( !vra4HdlbgFound ) {
            //templateChoices.addElement("vra4hdlbg");
            templateChoices.addElement("vra4hdlbgRichText");
            //templateComboBox.addItem(Messages.getString("AddTemplateDialog.VRA_CORE_4_HDLBG"));
            templateComboBox.addItem(Messages.getString("AddTemplateDialog.VRA_CORE_4_HDLBG_RICH_TEXT"));
        }
        if ( !customFound ) {
            templateChoices.addElement("custom");
            templateComboBox.addItem(Messages.getString("AddTemplateDialog.CUSTOM_EDITIERBAR"));
        }

        if ( templateComboBox.getItemCount() == 0 ) {
            templateComboBox.addItem(Messages.getString("AddTemplateDialog.KEINE_WEITEREN_TEMPLATES_VERFUEGBAR"));
            saveButton.setEnabled(false);
        }


    }


    
    private void initComponents() {

        templatePanel = new JPanel();
        templateComboBox = new JComboBox();
        cancelButton = new ResetButton();
        saveButton = new SaveButton();
        infoLabel = new JLabel();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setModal(true);

        templatePanelBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); // NOI18N
        templatePanel.setBorder(templatePanelBorder);
        GroupLayout templatePanelLayout = new GroupLayout(templatePanel);
        templatePanel.setLayout(templatePanelLayout);
        templatePanelLayout.setHorizontalGroup(
            templatePanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(templatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(templateComboBox, 0, 337, Short.MAX_VALUE)
                .addContainerGap())
        );
        templatePanelLayout.setVerticalGroup(
            templatePanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(templatePanelLayout.createSequentialGroup()
                .addComponent(templateComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        infoLabel.setVerticalAlignment(SwingConstants.TOP);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addGroup(Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(infoLabel, GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(templatePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(templatePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(infoLabel, GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }



    // ------------------------------------------------------------------------------------------------------



    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("reset")) {
            saveButtonClicked = false;
            this.setVisible(false);
        } else if (e.getActionCommand().equalsIgnoreCase("save")) {
            saveButtonClicked = true;
            this.setVisible(false);
        }
    }
}
