/* $Id: ServerSelectionDialog.java 16 2009-01-16 16:36:16Z hgkuper $ */

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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

import org.hyperimage.sysop.client.preferences.HIPreferenceManager;

//TODO HGK: Eclipsify

/**
 * @author Heinz-Guenter Kuper
 */
public class ServerSelectionDialog extends JDialog {
	private static final long serialVersionUID = -7224870203298950023L;
	private JButton jButton1;
	private JButton jButton2;
	private JComboBox jComboBox1;
	private JLabel jLabel1;
	private JLabel jLabel2;

	HIPreferenceManager m_prefs = null;
	private static final String CONNECT = "connect";
	private static final String CBEDIT = "comboBoxEdited";

	private static String m_serverURL = "";	// must either contain a valid URL, or remain empty.
	private static String m_strStatus = "Please choose a server.";

	public ServerSelectionDialog(Frame parent, boolean modal) {
		super(parent, modal);
		m_prefs = new HIPreferenceManager();
		initComponents();
		pimpComponents();
	}

	private void initComponents() {
		jLabel1 = new JLabel();
		jComboBox1 = new JComboBox();
		jButton1 = new JButton();
		jButton2 = new JButton();
		jLabel2 = new JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		jLabel1.setText("Server:");

		jComboBox1.setEditable(true);

		jButton1.setText("Connect to Server");

		jButton2.setText("Cancel");

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout.createSequentialGroup()
						.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
								.add(layout.createSequentialGroup()
										.addContainerGap()
										.add(jLabel1)
										.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
										.add(jComboBox1, 0, 504, Short.MAX_VALUE))
										.add(layout.createSequentialGroup()
												.add(145, 145, 145)
												.add(jButton1)
												.add(47, 47, 47)
												.add(jButton2)))
												.addContainerGap())
												.add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
														.addContainerGap(278, Short.MAX_VALUE)
														.add(jLabel2)
														.add(265, 265, 265))
		);
		layout.setVerticalGroup(
				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(layout.createSequentialGroup()
						.addContainerGap()
						.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
								.add(jLabel1)
								.add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
								.add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, Short.MAX_VALUE)
								.add(9, 9, 9)
								.add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
										.add(jButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.add(jButton1))
										.addContainerGap())
		);

		pack();
	}

	private void pimpComponents() {
		jLabel2.setText(m_strStatus);

		jComboBox1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				processConnectionEvent(ae);
			}
		});

		// Populate list
		ArrayDeque<String> serverDeq = m_prefs.getServerPref();
		jComboBox1.setModel(new DefaultComboBoxModel(serverDeq.toArray()));

		jButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				processConnectionEvent();
			}
		});
		jButton1.requestFocus();
		jButton1.getInputMap().put(KeyStroke.getKeyStroke(
				KeyEvent.VK_ENTER, 0),
				CONNECT);
		jButton1.getActionMap().put(CONNECT, new AbstractAction() {
			public void actionPerformed(ActionEvent ae) {
				processConnectionEvent();
			}				
		});


		jButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				System.exit(0);
			}
		});
	}

	protected void processConnectionEvent(ActionEvent ae) {
		String strAC = ae.getActionCommand();

		if( strAC.compareTo(CBEDIT) == 0 ) {
			JComboBox cb = (JComboBox)ae.getSource();
			m_serverURL = (String)cb.getSelectedItem();
			if( isValidURL(m_serverURL) ) {
				System.out.println("connect to " + m_serverURL);
				m_prefs.putServerPref(m_serverURL);
				this.dispose();
			} else {
				m_serverURL = "";
				System.out.println("Display error message and allow user to retry");
			}
		}
	}

	protected void processConnectionEvent() {
		m_serverURL = (String) jComboBox1.getSelectedItem();
		if( isValidURL(m_serverURL) ) {
			System.out.println("connect to the server " + jComboBox1.getSelectedItem());
			m_prefs.putServerPref(m_serverURL);
			this.dispose();
		} else {
			m_serverURL = "";
		}
	}

	private boolean isValidURL(String strURL) {
		boolean bRetVal = true;

		try {
			URL urlTest = new URL(strURL);
		} catch (MalformedURLException mue) {
			System.out.println("url exception: " + mue.getMessage());
			// FIXME HGK Display a status message.
			m_strStatus = mue.getLocalizedMessage();
			jLabel2.repaint();
			jLabel2.doLayout();
			this.doLayout();
			bRetVal = false;
		}

		return bRetVal;
	}

	public String getURL() {
		return m_serverURL;
	}
}
