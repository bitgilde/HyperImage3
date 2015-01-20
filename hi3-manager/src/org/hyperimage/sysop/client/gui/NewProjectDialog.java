/* $Id: NewProjectDialog.java 18 2009-01-27 12:16:13Z hgkuper $ */

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
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.hyperimage.client.ws.HiProject;
import org.hyperimage.client.ws.HiUser;
import org.hyperimage.sysop.client.HISystemOperator;

/**
 * @author Heinz-Guenter Kuper
 */
public class NewProjectDialog extends JDialog {
	private JButton m_buttOK;
	private JButton m_buttCancel;
	private JComboBox m_cbUsers;
	private JComboBox m_cbLanguages;
	private JLabel m_labelAdmin;
	private JLabel m_labelLang;
	// JML
    private javax.swing.JLabel m_labelTitle;
    private javax.swing.JTextField m_tfTitle;
	
	private static HISystemOperator m_hiSysOp = null;
	private static List<HiUser> m_listUsers = null;
	private static final String[] m_strarrLang = new String[] { "en", "de" };
	private static HiProject m_project = null;

	/** Creates new form NewProjectDialog */
	public NewProjectDialog(java.awt.Frame parent, boolean modal, HISystemOperator hiSysOp) {
		super(parent, modal);
		m_hiSysOp = hiSysOp;
		initComponents();
		pimpComponents();
	}

	private void pimpComponents() {
		m_buttOK.setText("OK");
		m_buttCancel.setText("Cancel");
		
		m_buttOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				doOKAction();
			}	
		});
		
		m_buttCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCancelAction();
			}	
		});
		
		m_listUsers = m_hiSysOp.getAllUsersAlphabetically();
		String[] arrUsers = new String[m_listUsers.size()];
		for(int i = 0; i < m_listUsers.size(); i++) {
			HiUser usr = m_listUsers.get(i);
			arrUsers[i] = usr.getLastName() + ", " + usr.getFirstName();
		}
		
		m_cbUsers.setModel(new DefaultComboBoxModel(arrUsers));
		
		// TODO HGK There is a better way to do this.
		m_cbLanguages.setModel(new DefaultComboBoxModel(m_strarrLang));
	}
	
	protected void doOKAction() {
		m_project = m_hiSysOp.addProject(m_listUsers.get(m_cbUsers.getSelectedIndex()).getUserName(), m_strarrLang[m_cbLanguages.getSelectedIndex()], m_tfTitle.getText());	
		this.dispose();
	}

	protected void doCancelAction() {
		this.dispose();
	}

	private void initComponents() {

		m_labelAdmin = new JLabel();
		m_labelLang = new JLabel();
		m_cbUsers = new JComboBox();
		m_cbLanguages = new JComboBox();
		m_buttOK = new JButton();
		m_buttCancel = new JButton();
        // JML
        m_labelTitle = new javax.swing.JLabel();
		m_tfTitle = new javax.swing.JTextField();
		m_labelTitle.setText("Project Title:");
		 
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		m_labelAdmin.setText("Select Project Admin:");

		m_labelLang.setText("Select Default Lang:");

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(m_buttOK)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(m_buttCancel))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, m_labelAdmin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, m_labelLang))
                            .add(m_labelTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, m_cbUsers, 0, 200, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, m_tfTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, m_cbLanguages, 0, 200, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(m_labelAdmin)
                    .add(m_cbUsers, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(m_tfTitle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(m_labelTitle))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(m_labelLang)
                    .add(m_cbLanguages, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(31, 31, 31)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(m_buttCancel)
                    .add(m_buttOK))
                .addContainerGap(30, Short.MAX_VALUE))
        );

		pack();
	}// </editor-fold>

	public HiProject getNewlyAddedProject() {
		return m_project;
	}
}
