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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
import org.hyperimage.client.ws.HiFlexMetadataTemplate;

/**
 * @author Jens-Martin Loebel
 */
public class EditTemplateDialog extends JDialog implements ActionListener, DocumentListener {

    private ResetButton cancelButton;
    private SaveButton saveButton;
    private JLabel infoLabel;
    private JLabel nsPrefixLabel;
    private JPanel templateURIPanel;
    private TitledBorder templateURIBorder;
    private JTextField templateURITextField;
    private JPanel templateURLPanel;
    private TitledBorder templateURLBorder;
    private JTextField templateURLTextField;

    private boolean saveButtonClicked = false;


    public EditTemplateDialog(JFrame parent) {
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

        templateURITextField.getDocument().addDocumentListener(this);
        templateURLTextField.getDocument().addDocumentListener(this);
    }

    public void updateLanguage() {
        this.setTitle(Messages.getString("EditTemplateDialog.TEMPLATE_BEARBEITEN"));
        templateURIBorder.setTitle(Messages.getString("EditTemplateDialog.NAMESPACE_URI"));
        templateURIPanel.repaint();
        templateURLBorder.setTitle(Messages.getString("EditTemplateDialog.SCHEMA_URL"));
        templateURLPanel.repaint();
        infoLabel.repaint();
        nsPrefixLabel.repaint();

        saveButton.setToolTipText(Messages.getString("EditTemplateDialog.AENDERUNGEN_SPEICHERN"));
        cancelButton.setToolTipText(Messages.getString("EditTemplateDialog.ABBRUCH"));
        updateButtonState();
    }


    public boolean showEditTemplateDialog(HiFlexMetadataTemplate template) {
        saveButtonClicked = false;

        // update labels
        this.setModal(false);
        this.setVisible(true);
        nsPrefixLabel.setText(Messages.getString("EditTemplateDialog.PREFIX")+": "+template.getNamespacePrefix());
        infoLabel.setText("");
        templateURITextField.setText(template.getNamespaceURI());
        templateURLTextField.setText(template.getNamespaceURL());
        updateButtonState();
        updateLanguage();
        this.setVisible(false);
        this.setModal(true);

        // show dialog
        this.setVisible(true);

        return saveButtonClicked;
    }

    public String getChangedURI() {
        return templateURITextField.getText();
    }

    public String getChangedURL() {
        return templateURLTextField.getText();
    }


    private void updateButtonState() {
        saveButton.setEnabled(false);
        infoLabel.setText("");
        if ( templateURITextField.getText().length() == 0 && templateURLTextField.getText().length() == 0 ) {
            saveButton.setEnabled(true);
        } else {
            // try to validate URI and URL
            try {
                saveButton.setEnabled(true);
                if ( templateURITextField.getText().length() > 0 ) new URI(templateURITextField.getText());
                if ( templateURLTextField.getText().length() > 0 ) new URL(templateURLTextField.getText());

            } catch (URISyntaxException urie) {
                infoLabel.setText("<html><font color='#FF0000'>"+Messages.getString("EditTemplateDialog.HINWEIS_URI")+"</font></html>");
                saveButton.setEnabled(false);
            } catch (MalformedURLException urle) {
                infoLabel.setText("<html><font color='#FF0000'>"+Messages.getString("EditTemplateDialog.HINWEIS_URL")+"</font></html>");
                saveButton.setEnabled(false);
            }


        }
    }

    
    private void initComponents() {
        templateURIPanel = new JPanel();
        templateURITextField = new JTextField();
        templateURLPanel = new JPanel();
        templateURLTextField = new JTextField();
        infoLabel = new JLabel();
        cancelButton = new ResetButton();
        saveButton = new SaveButton();
        nsPrefixLabel = new JLabel();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setModal(true);

        templateURIBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); // NOI18N
        templateURIPanel.setBorder(templateURIBorder);
        GroupLayout templateURIPanelLayout = new GroupLayout(templateURIPanel);
        templateURIPanel.setLayout(templateURIPanelLayout);

        templateURIPanelLayout.setHorizontalGroup(
            templateURIPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, templateURIPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(templateURITextField, GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
                .addContainerGap())
        );
        templateURIPanelLayout.setVerticalGroup(
            templateURIPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(templateURIPanelLayout.createSequentialGroup()
                .addComponent(templateURITextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        templateURLBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); // NOI18N
        templateURLPanel.setBorder(templateURLBorder);
        GroupLayout templateURLPanelLayout = new GroupLayout(templateURLPanel);
        templateURLPanel.setLayout(templateURLPanelLayout);
        templateURLPanelLayout.setHorizontalGroup(
            templateURLPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(templateURLPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(templateURLTextField, GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
                .addContainerGap())
        );
        templateURLPanelLayout.setVerticalGroup(
            templateURLPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(templateURLPanelLayout.createSequentialGroup()
                .addComponent(templateURLTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        infoLabel.setText("Hinweis: "); // NOI18N
        infoLabel.setVerticalAlignment(SwingConstants.TOP);

        nsPrefixLabel.setText("Prefix: "); // NOI18N

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addGroup(Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(infoLabel, GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE))
                    .addGroup(Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(templateURLPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(templateURIPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(nsPrefixLabel, GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(templateURIPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(templateURLPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(infoLabel, GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(nsPrefixLabel))
                .addContainerGap())
        );

        pack();
    }



    // ------------------------------------------------------------------------------------------------------



    @Override
    public void actionPerformed(ActionEvent e) {
        if ( e.getActionCommand().equalsIgnoreCase("reset") ) {
            saveButtonClicked = false;
            this.setVisible(false);
        } else if ( e.getActionCommand().equalsIgnoreCase("save") ) {
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
