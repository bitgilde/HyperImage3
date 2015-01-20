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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.ResetButton;
import org.hyperimage.client.gui.SaveButton;
import org.hyperimage.client.ws.CheckoutPermissions;
import org.hyperimage.client.ws.HiRepository;

/**
 *
 * @author Jens-Martin Loebel
 */
public class RepositoryDataDialog extends javax.swing.JDialog implements ActionListener, DocumentListener {

	private static final long serialVersionUID = -1212075569289384214L;


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel buttonPanel;
    private ResetButton cancelButton;
    private JPanel dataPanel;
    private JPanel displaynamePanel;
    private JTextField displaynameTextField;
    private JLabel infoLabel;
    private JPasswordField passwordField;
    private JPanel passwordPanel;
    private JComboBox reposTypeComboBox;
    private JPanel reposTypePanel;
    private SaveButton saveButton;
    private JPanel urlPanel;
    private JTextField urlTextField;
    private JPanel usernamePanel;
    private JTextField usernameTextField;
    // End of variables declaration//GEN-END:variables

    
    private TitledBorder usernameBorder;
    private TitledBorder passwordBorder;
    private TitledBorder urlBorder;
    private TitledBorder displaynameBorder;
    private TitledBorder reposTypeBorder;

	
	private JFrame parent;
    private HiRepository repos;


    /** Creates new form RepositoryDataDialog */
    public RepositoryDataDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        this.parent = parent;

        initComponents();
        updateLanguage();

        // attach listeners
        saveButton.addActionListener(this);
        cancelButton.addActionListener(this);
        urlTextField.getDocument().addDocumentListener(this);
        displaynameTextField.getDocument().addDocumentListener(this);
    }

    public void updateLanguage() {
    	usernameBorder.setTitle(Messages.getString("RepositoryDataDialog.0")); //$NON-NLS-1$
    	passwordBorder.setTitle(Messages.getString("RepositoryDataDialog.1")); //$NON-NLS-1$
    	urlBorder.setTitle(Messages.getString("RepositoryDataDialog.2")); //$NON-NLS-1$
    	displaynameBorder.setTitle(Messages.getString("RepositoryDataDialog.3")); //$NON-NLS-1$
    	reposTypeBorder.setTitle(Messages.getString("RepositoryDataDialog.4")); //$NON-NLS-1$
    	
    }
    

    public HiRepository showAddRepositoryDialog() {
        this.setBounds(
                (parent.getWidth() / 2) - (this.getWidth() / 2),
                (parent.getHeight() / 2) - (this.getHeight() / 2),
                this.getWidth(),
                this.getHeight()+30);

        this.repos = new HiRepository();

        usernameTextField.setText(""); //$NON-NLS-1$
        passwordField.setText(""); //$NON-NLS-1$
        urlTextField.setText(""); //$NON-NLS-1$
        displaynameTextField.setText(""); //$NON-NLS-1$

        infoLabel.setText(Messages.getString("RepositoryDataDialog.9")); //$NON-NLS-1$
        this.setTitle(Messages.getString("RepositoryDataDialog.10")); //$NON-NLS-1$
        saveButton.setEnabled(false);
		// DEBUG disable repos type selector
		reposTypePanel.setVisible(false);

        // show dialog
        this.setVisible(true);

        return repos;
    }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {


        dataPanel = new JPanel();
        usernamePanel = new JPanel();
        usernameTextField = new JTextField();
        passwordPanel = new JPanel();
        passwordField = new JPasswordField();
        urlPanel = new JPanel();
        urlTextField = new JTextField();
        displaynamePanel = new JPanel();
        displaynameTextField = new JTextField();
        reposTypePanel = new JPanel();
        reposTypeComboBox = new JComboBox();
        infoLabel = new JLabel();
        buttonPanel = new JPanel();
        saveButton = new SaveButton();
        cancelButton = new ResetButton();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        usernameBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "" , TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); //$NON-NLS-1$ //$NON-NLS-2$
        usernamePanel.setBorder(usernameBorder);
        GroupLayout usernamePanelLayout = new GroupLayout(usernamePanel);
        usernamePanel.setLayout(usernamePanelLayout);

        usernamePanelLayout.setHorizontalGroup(
            usernamePanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, usernamePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(usernameTextField, GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addContainerGap())
        );
        usernamePanelLayout.setVerticalGroup(
            usernamePanelLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(usernameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        passwordBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "" , TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); //$NON-NLS-1$ //$NON-NLS-2$
        passwordPanel.setBorder(passwordBorder);
        GroupLayout passwordPanelLayout = new GroupLayout(passwordPanel);
        passwordPanel.setLayout(passwordPanelLayout);

        passwordPanelLayout.setHorizontalGroup(
            passwordPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(passwordPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(passwordField, GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addContainerGap())
        );
        passwordPanelLayout.setVerticalGroup(
            passwordPanelLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        urlBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "" , TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); //$NON-NLS-1$ //$NON-NLS-2$
        urlPanel.setBorder(urlBorder);
        GroupLayout urlPanelLayout = new GroupLayout(urlPanel);
        urlPanel.setLayout(urlPanelLayout);

        urlPanelLayout.setHorizontalGroup(
            urlPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(urlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(urlTextField, GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addContainerGap())
        );
        urlPanelLayout.setVerticalGroup(
            urlPanelLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(urlTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        displaynameBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "" , TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); //$NON-NLS-1$ //$NON-NLS-2$
        displaynamePanel.setBorder(displaynameBorder);
        GroupLayout displaynamePanelLayout = new GroupLayout(displaynamePanel);
        displaynamePanel.setLayout(displaynamePanelLayout);

        displaynamePanelLayout.setHorizontalGroup(
            displaynamePanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(displaynamePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(displaynameTextField, GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                .addContainerGap())
        );
        displaynamePanelLayout.setVerticalGroup(
            displaynamePanelLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(displaynameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        reposTypeBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "" , TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); //$NON-NLS-1$ //$NON-NLS-2$
        reposTypePanel.setBorder(reposTypeBorder);
        reposTypeComboBox.setModel(new DefaultComboBoxModel(new String[] { "Fedora 3" })); //$NON-NLS-1$

        GroupLayout reposTypePanelLayout = new GroupLayout(reposTypePanel);
        reposTypePanel.setLayout(reposTypePanelLayout);
        reposTypePanelLayout.setHorizontalGroup(
            reposTypePanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(reposTypePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(reposTypeComboBox, GroupLayout.PREFERRED_SIZE, 158, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(239, Short.MAX_VALUE))
        );
        reposTypePanelLayout.setVerticalGroup(
            reposTypePanelLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(reposTypeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(372, Short.MAX_VALUE))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(buttonPanelLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

        GroupLayout dataPanelLayout = new GroupLayout(dataPanel);
        dataPanel.setLayout(dataPanelLayout);
        dataPanelLayout.setHorizontalGroup(
            dataPanelLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(usernamePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(passwordPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(urlPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(displaynamePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(reposTypePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(buttonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(dataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoLabel, GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)
                .addContainerGap())
        );
        dataPanelLayout.setVerticalGroup(
            dataPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, dataPanelLayout.createSequentialGroup()
                .addComponent(infoLabel, GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(usernamePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(passwordPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(urlPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(displaynamePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(reposTypePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dataPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(dataPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();

    }// </editor-fold>//GEN-END:initComponents

    

    // -----------------------------------------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        if ( e.getSource() == cancelButton )
            repos = null;
        if ( e.getSource() == saveButton ) {
            repos.setCheckoutPermission(CheckoutPermissions.CREATOR_ADMINS_USERS);
            repos.setUserName(usernameTextField.getText());
            repos.setPassword(String.valueOf(passwordField.getPassword()));
            repos.setUrl(urlTextField.getText());
            repos.setDisplayTitle(displaynameTextField.getText());
            // DEBUG
            repos.setRepoType("Fedora3"); //$NON-NLS-1$
        }

        this.setVisible(false);
    }

    // -----------------------------------------------------------------------------------------------


    @Override
    public void insertUpdate(DocumentEvent e) {
        if ( urlTextField.getText().length() > 0 && displaynameTextField.getText().length() > 0 )
            saveButton.setEnabled(true);
        else saveButton.setEnabled(false);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if ( urlTextField.getText().length() > 0 && displaynameTextField.getText().length() > 0 )
            saveButton.setEnabled(true);
        else saveButton.setEnabled(false);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if ( urlTextField.getText().length() > 0 && displaynameTextField.getText().length() > 0 )
            saveButton.setEnabled(true);
        else saveButton.setEnabled(false);
    }

}
