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

package org.hyperimage.client.gui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.jdesktop.layout.GroupLayout;

/**
 * @author Jens-Martin Loebel
 */
public class HISimpleTextFieldControl extends HITextFieldControl {

	private static final long serialVersionUID = -2003562702192926354L;

	
	private JPanel singleLinePanel;
	private JTextField textField;
	private String title;
	private TitledBorder titleBorder;
	
	
	public HISimpleTextFieldControl(String title) {
		super();
		
		this.title = title;
		initComponents();
	}


	public String getText() {
		return textField.getText();
	}
	
	public void setEditable(boolean editable) {
		textField.setEditable(editable);
	}

	public void setText(String text) {
		textField.setText(text);
	}

	public void updateTitle(String title) {
		this.title = title;
		titleBorder.setTitle(title);
		repaint();
		doLayout();
	}

	private void initComponents() {
		singleLinePanel = new JPanel();
		textField = new JTextField();

		titleBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title);
		singleLinePanel.setBorder(titleBorder);

		GroupLayout slPanelLayout = new GroupLayout(singleLinePanel);
		singleLinePanel.setLayout(slPanelLayout);
		slPanelLayout.setHorizontalGroup(
				slPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(slPanelLayout.createSequentialGroup()
						.addContainerGap()
						.add(textField, GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
						.addContainerGap())
		);
		slPanelLayout.setVerticalGroup(
				slPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		);

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.LEADING)
				.add(singleLinePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.LEADING)
				.add(singleLinePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		);
	}
}
