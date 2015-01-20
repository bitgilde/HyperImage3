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
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.ResetButton;
import org.hyperimage.client.gui.SaveButton;
import org.hyperimage.client.ws.HiFlexMetadataSet;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;

/**
 * @author Jens-Martin Loebel
 */
public class AddTemplateFieldDialog extends JDialog implements ActionListener, DocumentListener {

    private ResetButton cancelButton;
    private SaveButton saveButton;
    private JComboBox fieldTypeComboBox;
    private JPanel fieldTypePanel;
    private TitledBorder fieldTypeBorder;
    private JLabel infoLabel;
    private JPanel tagNamePanel;
    private TitledBorder tagNameBorder;
    private JTextField tagNameTextField;
    private HiFlexMetadataTemplate template;
    private boolean saveButtonClicked = false;


    public AddTemplateFieldDialog(JFrame parent) {
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
        tagNameTextField.getDocument().addDocumentListener(this);
    }

    public void updateLanguage() {
        this.setTitle(Messages.getString("AddTemplateFieldDialog.METADATENFELD_HINZUFUEGEN"));
        tagNameBorder.setTitle(Messages.getString("AddTemplateFieldDialog.XML_TAGNAME"));
        tagNamePanel.repaint();
        fieldTypeBorder.setTitle(Messages.getString("AddTemplateFieldDialog.FELDTYP"));
        fieldTypePanel.repaint();
        fieldTypeComboBox.setModel(new DefaultComboBoxModel(new String[]{Messages.getString("AddTemplateFieldDialog.SINGLE_LINE"), Messages.getString("AddTemplateFieldDialog.RICH_TEXT")}));

        saveButton.setToolTipText(Messages.getString("AddTemplateFieldDialog.METADATENFELD_HINZUFUEGEN"));
        cancelButton.setToolTipText(Messages.getString("AddTemplateFieldDialog.ABBRUCH"));
        infoLabel.repaint();
        updateButtonState();
    }

    public boolean showAddTemplateFieldDialog(HiFlexMetadataTemplate template) {
        this.template = template;
        saveButtonClicked = false;


        // update on screen text
        this.setModal(false);
        this.setVisible(true);
        tagNameTextField.setText("");
        fieldTypeComboBox.setSelectedIndex(0);
        updateButtonState(); // update buttons and label
        updateLanguage();
        this.setVisible(false);
        this.setModal(true);

        // show dialog
        this.setVisible(true);


        return saveButtonClicked;
    }

    public String getTagName() {
        return tagNameTextField.getText();
    }

    public boolean isRichText() {
        if (fieldTypeComboBox.getSelectedIndex() == 1) {
            return true;
        }
        return false;
    }

    private void updateButtonState() {
        saveButton.setEnabled(false);

        // validate tag name
        if ( tagNameTextField.getText().length() == 0 ) {
            infoLabel.setText("<html><font color='#FF0000'>"+Messages.getString("AddTemplateFieldDialog.HINWEIS_EMPTY")+"</font></html>");
        } else {
            // check for duplicate tag names
            boolean tagExists = false;
            for ( HiFlexMetadataSet set : template.getEntries() )
                if ( set.getTagname().compareTo(tagNameTextField.getText()) == 0 )
                    tagExists = true;

            if ( tagExists ) {
                infoLabel.setText("<html><font color='#FF0000'>"+Messages.getString("AddTemplateFieldDialog.HINWEIS_EXISTS")+"</font></html>");
            } else {
                if ( !tagNameTextField.getText().matches("[a-zA-Z][a-zA-Z0-9]*") )
                    infoLabel.setText("<html><font color='#FF0000'>"+Messages.getString("AddTemplateFieldDialog.HINWEIS_INVALID")+"</font></html>");
                else
                    saveButton.setEnabled(true); // tag name valid
            }

        }

        if ( saveButton.isEnabled() )
            infoLabel.setText("<html>"+Messages.getString("AddTemplateFieldDialog.HINWEIS")+"<br>"+Messages.getString("AddTemplateFieldDialog.HINWEIS_HINZUFUEGEN")+"</html>");

    }

    private void initComponents() {
        tagNamePanel = new JPanel();
        tagNameTextField = new JTextField();
        fieldTypePanel = new JPanel();
        fieldTypeComboBox = new JComboBox();
        infoLabel = new JLabel();
        cancelButton = new ResetButton();
        saveButton = new SaveButton();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setModal(true);

        tagNameBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); // NOI18N
        tagNamePanel.setBorder(tagNameBorder);
        GroupLayout tagNamePanelLayout = new GroupLayout(tagNamePanel);
        tagNamePanel.setLayout(tagNamePanelLayout);

        tagNamePanelLayout.setHorizontalGroup(
                tagNamePanelLayout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, tagNamePanelLayout.createSequentialGroup().addContainerGap().addComponent(tagNameTextField, GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE).addContainerGap()));
        tagNamePanelLayout.setVerticalGroup(
                tagNamePanelLayout.createParallelGroup(Alignment.LEADING).addGroup(tagNamePanelLayout.createSequentialGroup().addComponent(tagNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        fieldTypeBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); // NOI18N
        fieldTypePanel.setBorder(fieldTypeBorder);

        GroupLayout fieldTypePanelLayout = new GroupLayout(fieldTypePanel);
        fieldTypePanel.setLayout(fieldTypePanelLayout);
        fieldTypePanelLayout.setHorizontalGroup(
                fieldTypePanelLayout.createParallelGroup(Alignment.LEADING).addGroup(fieldTypePanelLayout.createSequentialGroup().addContainerGap().addComponent(fieldTypeComboBox, 0, 271, Short.MAX_VALUE).addContainerGap()));
        fieldTypePanelLayout.setVerticalGroup(
                fieldTypePanelLayout.createParallelGroup(Alignment.LEADING).addGroup(fieldTypePanelLayout.createSequentialGroup().addComponent(fieldTypeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        infoLabel.setText("Hinweis: "); // NOI18N
        infoLabel.setVerticalAlignment(SwingConstants.TOP);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(infoLabel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                    .addComponent(fieldTypePanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tagNamePanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tagNamePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(fieldTypePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(infoLabel, GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
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



    // ------------------------------------------------------------------------------------------------------



    @Override
    public void insertUpdate(DocumentEvent e) {
        updateButtonState();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateButtonState();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateButtonState();
    }
}
