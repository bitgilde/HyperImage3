/* $Id: EditUserDialog.java 18 2009-01-27 12:16:13Z hgkuper $ */

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

package org.hyperimage.sysop.client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.hyperimage.sysop.client.HISystemOperator;
import org.hyperimage.sysop.client.utility.DecoratedHIUser;

/**
 *
 * @author Heinz-Guenter Kuper
 */
public class EditUserDialog extends javax.swing.JDialog implements DocumentListener {
    private javax.swing.JButton m_buttCancel;
    private javax.swing.JButton m_buttOK;
    private javax.swing.JLabel m_labelEMail;
    private javax.swing.JLabel m_labelFirstName;
    private javax.swing.JLabel m_labelLastName;
    private javax.swing.JLabel m_labelPassword;
    private javax.swing.JPasswordField m_passwdfield;
    private javax.swing.JTextField m_txtFieldEMail;
    private javax.swing.JTextField m_txtfieldFirstName;
    private javax.swing.JTextField m_txtfieldLastName;
	private JLabel m_labelUsername;
	
	private static boolean m_bFieldsEdited = false;
	private static boolean m_bPasswdEdited = false;

    private static HISystemOperator m_hiSysOp = null;
    private static DecoratedHIUser m_user = null;

    /** Creates new form EditUser 
     * @param sysOp 
     * @param object */
    public EditUserDialog(java.awt.Frame parent, boolean modal, Object object, HISystemOperator sysOp) {
        super(parent, modal);
        m_hiSysOp = sysOp;
        m_user = (DecoratedHIUser) object;
        initComponents();
        pimpComponents();
    }

    private void pimpComponents() {
    	m_labelUsername.setText("Editing User " + m_user.getUserName());
    	
		m_txtfieldLastName.setText(m_user.getLastName());
		m_txtfieldLastName.setEditable(true);
		m_txtfieldLastName.getDocument().addDocumentListener(this);
		
		m_txtfieldFirstName.setText(m_user.getFirstName());
		m_txtfieldFirstName.setEditable(true);
		m_txtfieldFirstName.getDocument().addDocumentListener(this);
		
		m_txtFieldEMail.setText(m_user.getEmail());
		m_txtFieldEMail.setEditable(true);
		m_txtFieldEMail.getDocument().addDocumentListener(this);
		
		m_passwdfield.setText("dummytxt");
		m_passwdfield.setEditable(true);
		m_passwdfield.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				m_bPasswdEdited = true;
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				m_bPasswdEdited = true;
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				m_bPasswdEdited = true;
			}		
		});
		
		m_buttCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				performCancel();
			}	
		});
		
		m_buttOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performOK();
			}
		});
    }
		

    /**
     * If any field has been edited, then pass all new data to service, to ensure that at least the data remains consistent within itself.
     * The HI Authoring System does not as yet implement any locking, so there is a chance that someone else might be editing this user (e.g. the user herself).
     * By passing all the data back to the service we can at least ensure that the record is consistent. Whoever writes lasts wins.
     */
    protected void performOK() {
		if( m_bFieldsEdited ) {
			m_hiSysOp.modUser(m_user.getUserName(), m_txtfieldFirstName.getText(), m_txtfieldLastName.getText(), m_txtFieldEMail.getText());
		}
		
		if( m_bPasswdEdited ) {
			char[] passwd = m_passwdfield.getPassword();
			m_hiSysOp.modUserPasswd(m_user.getUserName(), new String(passwd));
			for( int i = 0; i < passwd.length; i++ ) {
				passwd[i] = 0;
			}
		}
		
		this.dispose();
	}

	protected void performCancel() {
		this.dispose();
	}

	private void initComponents() {

        m_labelLastName = new javax.swing.JLabel();
        m_txtfieldLastName = new javax.swing.JTextField();
        m_labelFirstName = new javax.swing.JLabel();
        m_txtfieldFirstName = new javax.swing.JTextField();
        m_labelEMail = new javax.swing.JLabel();
        m_txtFieldEMail = new javax.swing.JTextField();
        m_labelPassword = new javax.swing.JLabel();
        m_passwdfield = new javax.swing.JPasswordField();
        m_buttOK = new javax.swing.JButton();
        m_buttCancel = new javax.swing.JButton();
        m_labelUsername = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        m_labelLastName.setText("Last Name:");

        m_txtfieldLastName.setText("jTextField1");

        m_labelFirstName.setText("First Name:");

        m_txtfieldFirstName.setText("jTextField1");

        m_labelEMail.setText("E-Mail:");

        m_txtFieldEMail.setText("jTextField1");

        m_labelPassword.setText("Password:");

        m_passwdfield.setText("jPasswordField1");

        m_buttOK.setText("Save Changes");

        m_buttCancel.setText("Cancel");

        m_labelUsername.setText("Editing User ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(m_labelLastName)
                            .add(m_labelFirstName)
                            .add(m_labelEMail))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(m_txtfieldFirstName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                            .add(m_txtfieldLastName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                            .add(m_txtFieldEMail, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(m_labelPassword)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(m_passwdfield, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(38, 38, 38)
                        .add(m_buttCancel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 163, Short.MAX_VALUE)
                        .add(m_buttOK)
                        .add(21, 21, 21))
                    .add(m_labelUsername))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(31, 31, 31)
                .add(m_labelUsername)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 35, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(m_labelLastName)
                    .add(m_txtfieldLastName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(m_labelFirstName)
                    .add(m_txtfieldFirstName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(m_labelEMail)
                    .add(m_txtFieldEMail, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(34, 34, 34)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(m_labelPassword)
                    .add(m_passwdfield, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(42, 42, 42)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(m_buttCancel)
                    .add(m_buttOK))
                .add(48, 48, 48))
        );

        pack();
    }

	// DocumentListener method
	@Override
	public void changedUpdate(DocumentEvent e) {
		m_bFieldsEdited = true;
	}

	// DocumentListener method
	@Override
	public void insertUpdate(DocumentEvent e) {
		m_bFieldsEdited = true;	
	}

	// DocumentListener method
	@Override
	public void removeUpdate(DocumentEvent e) {
		m_bFieldsEdited = true;	
	}
}

