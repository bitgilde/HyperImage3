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

package org.hyperimage.client.gui.dialogs;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.ResetButton;
import org.hyperimage.client.gui.SaveButton;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiFlexMetadataSet;

/**
 * @author Jens-Martin Loebel
 */
public class EditTemplateFieldDialog extends JDialog implements ActionListener {

    private SaveButton saveButton;
    private ResetButton cancelButton;
    private JPanel displayNamePanel;
    private TitledBorder displayNameBorder;
    private JTextField displayNameTextField;
    private JLabel tagInfoLabel;

    private HiFlexMetadataSet set;
    private String displayName;
    private Locale osLang;

    
    public EditTemplateFieldDialog(JFrame parent) {
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
    }

    public void updateLanguage() {
        this.setTitle(Messages.getString("EditTemplateFieldDialog.METADATENFELD_BEARBEITEN"));
        displayNameBorder.setTitle(Messages.getString("EditTemplateFieldDialog.ANZEIGENAME"));
        displayNamePanel.repaint();

        saveButton.setToolTipText(Messages.getString("EditTemplateFieldDialog.AENDERUNGEN_SPEICHERN"));
        cancelButton.setToolTipText(Messages.getString("EditTemplateFieldDialog.ABBRUCH"));
    }


    public void showEditFieldDialog(HiFlexMetadataSet set, Locale osLang) {
        this.set = set;
        this.osLang = osLang;
        displayName = MetadataHelper.getTemplateKeyDisplayName(null, set, MetadataHelper.localeToLangID(osLang));

        if ( displayName == null || displayName.length() == 0 ) displayName = "";
        displayNameTextField.setText(displayName);

        // update tag name info label
        this.setModal(false);
        this.setVisible(true);
        tagInfoLabel.setText(Messages.getString("EditTemplateFieldDialog.TAG")+": "+set.getTagname());
        updateLanguage();
        this.setVisible(false);
        this.setModal(true);
        
        // show dialog
        this.setVisible(true);
    }

    public String getChangedDisplayName() {
        return displayNameTextField.getText();
    }


    private void initComponents() {
        cancelButton = new ResetButton();
        saveButton = new SaveButton();
        displayNamePanel = new JPanel();
        displayNameTextField = new JTextField();
        tagInfoLabel = new JLabel();

        setModal(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        displayNameBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); // NOI18N
        displayNamePanel.setBorder(displayNameBorder);
        GroupLayout displayNamePanelLayout = new GroupLayout(displayNamePanel);
        displayNamePanel.setLayout(displayNamePanelLayout);
        displayNamePanelLayout.setHorizontalGroup(
            displayNamePanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(displayNamePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(displayNameTextField, GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
                .addContainerGap())
        );
        displayNamePanelLayout.setVerticalGroup(
            displayNamePanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(displayNamePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(displayNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tagInfoLabel.setText("Tag:"); // NOI18N

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(displayNamePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(tagInfoLabel, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(displayNamePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(tagInfoLabel))
                .addContainerGap())
        );

        pack();

    }



    // ------------------------------------------------------------------------------------------------------



    @Override
    public void actionPerformed(ActionEvent e) {
        if ( e.getActionCommand().equalsIgnoreCase("reset") ) {
            displayNameTextField.setText(displayName);
            this.setVisible(false);
        } else if ( e.getActionCommand().equalsIgnoreCase("save") ) {
            this.setVisible(false);
        }
    }

    
}
