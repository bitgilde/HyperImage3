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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class LoginDialog extends JDialog implements ActionListener, DocumentListener {

	private static final long serialVersionUID = -1231146231949571153L;

	
	private JPanel authPanel;
    private JLabel infoLabel;
    private JButton loginButton;
    private JPasswordField passwordField;
    private JPanel passwordPanel;
    private JButton quitButton;
    private JPanel userPanel;
    private JTextField userTextField;
    private JFrame parent;
    
    private boolean loginButtonClicked = false;
    
	
	public LoginDialog(JFrame parent) {
		super(parent);
		this.parent = parent;
		initComponents();
		
		// attach listeners
		userTextField.addActionListener(this);
		userTextField.getDocument().addDocumentListener(this);
		passwordField.addActionListener(this);
		passwordField.getDocument().addDocumentListener(this);
		loginButton.addActionListener(this);
		quitButton.addActionListener(this);
		
	}
	
	public boolean promptLogin(String username) {
		this.userTextField.setText(username);
 		this.passwordField.setText("");
 		
 		loginButtonClicked = false;
				
		this.setBounds(
				(parent.getWidth()/2) - (this.getWidth()/2), 
				(parent.getHeight()/2) - (this.getHeight()/2), 
				this.getWidth(), 
				this.getHeight());

		updateButtonStatus();		
		// DEBUG - seems to be a bug with the apple implementation
		this.setModal(false);
		this.setVisible(true);
		infoLabel.repaint();
		this.setVisible(false);
		this.setModal(true);
		// -----
		
		// set user input focus
		if ( userTextField.getText().length() == 0 )
			userTextField.requestFocusInWindow();
		else 
			passwordField.requestFocusInWindow();

		// activate dialog
		this.setVisible(true);
		
		return loginButtonClicked;
	}
	
	public String getUserName() {
		return userTextField.getText();
	}
	
	public String getPassword() {
		return String.valueOf(passwordField.getPassword());
	}
	
	
	public void setInfoLabel(String text, Color color) {
		infoLabel.setText(text);
		infoLabel.setForeground(color);
	}
	
	
    private void initComponents() {

        authPanel = new JPanel();
        loginButton = new JButton();
        quitButton = new JButton();
        userPanel = new JPanel();
        userTextField = new JTextField();
        passwordPanel = new JPanel();
        passwordField = new JPasswordField();
        infoLabel = new JLabel();

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        loginButton.setText("Login");
        loginButton.setFocusCycleRoot(true);

        quitButton.setText("Beenden");

        userPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Benutzername"));

        GroupLayout userPanelLayout = new GroupLayout(userPanel);
        userPanel.setLayout(userPanelLayout);
        userPanelLayout.setHorizontalGroup(
            userPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(userTextField, GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
        );
        userPanelLayout.setVerticalGroup(
            userPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(userTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        passwordPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Passwort"));

        GroupLayout passwordPanelLayout = new GroupLayout(passwordPanel);
        passwordPanel.setLayout(passwordPanelLayout);
        passwordPanelLayout.setHorizontalGroup(
            passwordPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(passwordField, GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
        );
        passwordPanelLayout.setVerticalGroup(
            passwordPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        infoLabel.setForeground(java.awt.Color.blue);
        infoLabel.setText("Bitte melden Sie sich an:");

        GroupLayout authPanelLayout = new GroupLayout(authPanel);
        authPanel.setLayout(authPanelLayout);
        authPanelLayout.setHorizontalGroup(
            authPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, authPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(authPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(authPanelLayout.createSequentialGroup()
                        .add(loginButton)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(quitButton))
                    .add(GroupLayout.LEADING, infoLabel, GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, passwordPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, userPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        authPanelLayout.setVerticalGroup(
            authPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(authPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(infoLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(userPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(passwordPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(authPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(quitButton)
                    .add(loginButton))
                .add(50, 50, 50))
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(authPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(authPanel, GroupLayout.PREFERRED_SIZE, 207, GroupLayout.PREFERRED_SIZE)
        );

        pack();
        
        // -----
        
		this.setTitle("HyperImage Login");
        this.setModal(true);
        
        
    }
    

    // -----------------------------------------------------------------------------------------------

    
    public void actionPerformed(ActionEvent e) {
    	if ( e.getSource() == loginButton || e.getSource() == passwordField ) {
    		if ( loginButton.isEnabled() ) {
    			loginButtonClicked = true;
    			this.setVisible(false);
    		}
    	}
    	
    	if ( e.getSource() == quitButton ) {
    		loginButtonClicked = false;
    		this.setVisible(false);
    	}

    	if ( e.getSource() == userTextField )
    		// focus on password field
    		passwordField.requestFocus();

	}
    

    // -----------------------------------------------------------------------------------------------

    
    private void updateButtonStatus() {
    	if ( userTextField.getText().length() > 0  && passwordField.getPassword().length > 0 )
    		loginButton.setEnabled(true); 
    	else loginButton.setEnabled(false);

    }
    
	public void changedUpdate(DocumentEvent e) {
		updateButtonStatus();
	}

	public void insertUpdate(DocumentEvent e) {
		updateButtonStatus();
	}
	public void removeUpdate(DocumentEvent e) {
		updateButtonStatus();
	}
	
}
