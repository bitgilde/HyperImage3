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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.hyperimage.client.Messages;

import org.hyperimage.client.ws.HiUser;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class AccountSettingsView extends GUIView implements DocumentListener {

	private static final long serialVersionUID = -6285928022646976944L;

	
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JLabel emailLabel;
	private JTextField emailTextField;
	private JLabel firstNameLabel;
	private JTextField firstNameTextField;
	private JLabel lastNameLabel;
	private JTextField lastNameTextField;
	private JPasswordField newPasswordField;
	private JLabel newPasswordLabel;
	private JPanel settingsPanel;
	private JButton updateButton;
	private JPanel userDataPanel;
	private JLabel userNameLabel;
	private JTextField userNameTextField;
	private JPanel userPasswordPanel;

	private HiUser user;


        public AccountSettingsView(HiUser user) {
		super(Messages.getString("AccountSettingsView.EDITDATA"));

		this.user = user;
		
		initComponents();
                updateLanguage();
		setUserMetadata();
		
		setDisplayPanel(settingsPanel);

	}

        
        @Override
        public void updateLanguage() {            
		menuTitle.setText(Messages.getString("AccountSettingsView.EDITDATA")); //$NON-NLS-1$
		userDataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("AccountSettingsView.ACCOUNTDATA"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), java.awt.Color.blue)); // NOI18N
		firstNameLabel.setText(Messages.getString("AccountSettingsView.FIRSTNAME")+":");
		lastNameLabel.setText(Messages.getString("AccountSettingsView.LASTNAME")+":");
		emailLabel.setText(Messages.getString("AccountSettingsView.EMAIL")+":");
		userNameLabel.setText(Messages.getString("AccountSettingsView.USERNAME")+":");
		userPasswordPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("AccountSettingsView.ACCOUNTPASSWORD"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), java.awt.Color.blue)); // NOI18N
		newPasswordLabel.setText(Messages.getString("AccountSettingsView.NEWPASSWORD")+":");
		cancelButton.setText(Messages.getString("AccountSettingsView.CANCEL"));
		updateButton.setText(Messages.getString("AccountSettingsView.APPLYCHANGES"));
        }
	
	public JButton getUpdateButton() {
		return updateButton;
	}
	
	public JButton getCancelButton() {
		return cancelButton;
	}


	public void resetPassword() {
		this.newPasswordField.setText("");
		updateButton.setEnabled(hasMetadataChanges() || hasPasswordChanges());
	}

	public String getNewPassword() {
		return new String(newPasswordField.getPassword());
	}
	
	public void syncMetadataChanges() {
		if ( hasMetadataChanges() ) {
			user.setFirstName(firstNameTextField.getText());
			user.setLastName(lastNameTextField.getText());
			user.setEmail(emailTextField.getText());
			
			updateButton.setEnabled(hasMetadataChanges() || hasPasswordChanges());
		}
	}

	
	public boolean hasPasswordChanges() {
		// password needs to be at least 6 characters long
		return newPasswordField.getPassword().length > 5;
	}
	
	public boolean hasMetadataChanges() {
		boolean hasChanges = false;
		if ( firstNameTextField.getText().compareTo(user.getFirstName()) != 0 )
			hasChanges = true;
		if ( lastNameTextField.getText().compareTo(user.getLastName()) != 0 && lastNameTextField.getText().length() > 0 )
			hasChanges = true;
		if ( emailTextField.getText().compareTo(user.getEmail()) != 0 )
			hasChanges = true;
		
		return hasChanges;
	}


	private void setUserMetadata() {
		this.firstNameTextField.setText(user.getFirstName());
		this.lastNameTextField.setText(user.getLastName());
		this.emailTextField.setText(user.getEmail());
		this.userNameTextField.setText(user.getUserName());
	}

        
	private void initComponents() {
		settingsPanel = new JPanel();
		userDataPanel = new JPanel();
		firstNameLabel = new JLabel();
		firstNameTextField = new JTextField();
		lastNameLabel = new JLabel();
		lastNameTextField = new JTextField();
		emailLabel = new JLabel();
		emailTextField = new JTextField();
		userNameLabel = new JLabel();
		userNameTextField = new JTextField();
		userPasswordPanel = new JPanel();
		newPasswordLabel = new JLabel();
		newPasswordField = new JPasswordField();
		buttonPanel = new JPanel();
		cancelButton = new JButton();
		updateButton = new JButton();


		firstNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		firstNameLabel.setLabelFor(firstNameTextField);

		lastNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lastNameLabel.setLabelFor(lastNameTextField);

		emailLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		emailLabel.setLabelFor(emailTextField);

		userNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		userNameLabel.setLabelFor(userNameTextField);
		
		userNameTextField.setEditable(false);
		userNameTextField.setEnabled(false);

		GroupLayout userDataPanelLayout = new GroupLayout(userDataPanel);
		userDataPanel.setLayout(userDataPanelLayout);
		userDataPanelLayout.setHorizontalGroup(
				userDataPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(userDataPanelLayout.createSequentialGroup()
						.addContainerGap()
						.add(userDataPanelLayout.createParallelGroup(GroupLayout.LEADING, false)
								.add(firstNameLabel, GroupLayout.PREFERRED_SIZE, 113, GroupLayout.PREFERRED_SIZE)
								.add(lastNameLabel, GroupLayout.PREFERRED_SIZE, 113, GroupLayout.PREFERRED_SIZE)
								.add(emailLabel, GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
								.add(GroupLayout.TRAILING, userNameLabel, GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))
								.add(10, 10, 10)
								.add(userDataPanelLayout.createParallelGroup(GroupLayout.TRAILING)
										.add(firstNameTextField, GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
										.add(lastNameTextField, GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
										.add(emailTextField, GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
										.add(GroupLayout.LEADING, userNameTextField, GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE))
										.addContainerGap())
		);
		userDataPanelLayout.setVerticalGroup(
				userDataPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(userDataPanelLayout.createSequentialGroup()
						.add(userDataPanelLayout.createParallelGroup(GroupLayout.BASELINE)
								.add(firstNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.add(firstNameLabel))
								.addPreferredGap(LayoutStyle.RELATED)
								.add(userDataPanelLayout.createParallelGroup(GroupLayout.BASELINE)
										.add(lastNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.add(lastNameLabel))
										.addPreferredGap(LayoutStyle.RELATED)
										.add(userDataPanelLayout.createParallelGroup(GroupLayout.BASELINE)
												.add(emailTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.add(emailLabel))
												.addPreferredGap(LayoutStyle.RELATED)
												.add(userDataPanelLayout.createParallelGroup(GroupLayout.BASELINE)
														.add(userNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
														.add(userNameLabel)))
		);

		newPasswordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		newPasswordLabel.setLabelFor(newPasswordField);

		GroupLayout userPasswordPanelLayout = new GroupLayout(userPasswordPanel);
        userPasswordPanel.setLayout(userPasswordPanelLayout);
        userPasswordPanelLayout.setHorizontalGroup(
            userPasswordPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(userPasswordPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(newPasswordLabel, GroupLayout.PREFERRED_SIZE, 111, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(newPasswordField, GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                .addContainerGap())
        );
        userPasswordPanelLayout.setVerticalGroup(
            userPasswordPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(userPasswordPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                .add(newPasswordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(newPasswordLabel))
        );
        
		
        GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addContainerGap(199, Short.MAX_VALUE)
                .add(cancelButton)
                .add(11, 11, 11)
                .add(updateButton))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(buttonPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(cancelButton, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
                    .add(updateButton, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout settingsPanelLayout = new GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(settingsPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING, buttonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, userDataPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, userPasswordPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(settingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(userDataPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(userPasswordPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(buttonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        
        // -----
        
        updateButton.setEnabled(false);
        updateButton.setActionCommand("update"); // NOI18N
        cancelButton.setActionCommand("cancel"); // NOI18N

        firstNameTextField.getDocument().addDocumentListener(this);
        lastNameTextField.getDocument().addDocumentListener(this);
        emailTextField.getDocument().addDocumentListener(this);
        newPasswordField.getDocument().addDocumentListener(this);

        
	}


	// --------------------------------------------------------------------------------------------------


	@Override
	public void changedUpdate(DocumentEvent e) {
		updateButton.setEnabled(hasMetadataChanges() || hasPasswordChanges());
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateButton.setEnabled(hasMetadataChanges() || hasPasswordChanges());
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateButton.setEnabled(hasMetadataChanges() || hasPasswordChanges());
	}


}
