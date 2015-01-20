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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import org.hyperimage.client.gui.OptionButton;
import org.hyperimage.client.gui.lists.GroupContentsList;
import org.hyperimage.client.ws.HiQuickInfo;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Jens-Martin Loebel
 */
public class GenericContentsView extends GUIView {

	private static final long serialVersionUID = -7561733761464079441L;

	
	protected JPanel controlPanel;
	protected JLabel elementCountLabel;
	protected JScrollPane listScrollPane;
	protected JButton listStyleButton;
	protected OptionButton optionsButton;
	protected JPanel contentsPanel;
	protected JProgressBar loadingIndicator;
	protected JPanel contentsInfoPanel;
	protected GroupContentsList contentsList;
	protected JPanel contentsDisplayPanel;
	
	public ImageIcon iconStyleIcon;
	public ImageIcon listStyleIcon;

	protected List<HiQuickInfo> contents = null;
	protected int elementCount = 0;


	public GenericContentsView(String title) {
		super(title);

		initComponents();

		setDisplayPanel(contentsPanel);
	}

	
	public void setContents(List<HiQuickInfo> contents) {
		DefaultListModel model = (DefaultListModel) contentsList.getModel();
		model.removeAllElements();
		
		for ( HiQuickInfo info : contents )
			model.addElement(info);
		
		elementCount = model.getSize();		
		this.contents = contents;
		
		setLoading(false);
	}
	
	public List<HiQuickInfo> getContents() {
		return contents;
	}

	public void prepareElementLoading() {
		DefaultListModel model = (DefaultListModel) contentsList.getModel();
		
		model.removeAllElements();
		elementCount = 0;
		setLoading(true);
	}


	public GroupContentsList getContentsList() {
		return contentsList;
	}
	
	public JButton getListStyleButton() {
		return listStyleButton;
	}

	// DEBUG
	protected void setLoading(boolean loading) {
		optionsButton.setEnabled(!loading);

		loadingIndicator.setVisible(loading);		
	}

	
	
	protected void initComponents() {

		contentsPanel = new JPanel();
		contentsDisplayPanel = new JPanel();
		listScrollPane = new JScrollPane();
		contentsList = new GroupContentsList();
		controlPanel = new JPanel();
        loadingIndicator = new JProgressBar();
		optionsButton = new OptionButton();
		contentsInfoPanel = new JPanel();
		elementCountLabel = new JLabel();
		listStyleButton = new JButton();

        loadingIndicator.setIndeterminate(true);
        contentsInfoPanel.setLayout(new BorderLayout(10,0));
        contentsInfoPanel.add(loadingIndicator, BorderLayout.CENTER);

		// -----
		
		listScrollPane.setViewportView(contentsList);

		controlPanel.setLayout(new BorderLayout(10, 0));

		controlPanel.add(optionsButton, BorderLayout.WEST);

		elementCountLabel.setText("XX Elemente");
		contentsInfoPanel.add(elementCountLabel, BorderLayout.WEST);

		controlPanel.add(contentsInfoPanel, BorderLayout.CENTER);

		listStyleButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		listStyleButton.setPreferredSize(new Dimension(24, 24));
		controlPanel.add(listStyleButton, BorderLayout.EAST);

		GroupLayout searchResultsPanelLayout = new GroupLayout(contentsDisplayPanel);
		contentsDisplayPanel.setLayout(searchResultsPanelLayout);
		searchResultsPanelLayout.setHorizontalGroup(
				searchResultsPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(searchResultsPanelLayout.createSequentialGroup()
						.addContainerGap()
						.add(searchResultsPanelLayout.createParallelGroup(GroupLayout.LEADING)
								.add(GroupLayout.TRAILING, controlPanel, GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
								.add(GroupLayout.TRAILING, listScrollPane, GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE))
								.addContainerGap())
		);
		searchResultsPanelLayout.setVerticalGroup(
				searchResultsPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(GroupLayout.TRAILING, searchResultsPanelLayout.createSequentialGroup()
						.addContainerGap()
						.add(listScrollPane, GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
						.add(7, 7, 7)
						.add(controlPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap())
		);

		GroupLayout resultsPanelLayout = new GroupLayout(contentsPanel);
		contentsPanel.setLayout(resultsPanelLayout);
		resultsPanelLayout.setHorizontalGroup(
				resultsPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(contentsDisplayPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		resultsPanelLayout.setVerticalGroup(
				resultsPanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(contentsDisplayPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);

		// -----
		
		optionsButton.setEnabled(false);
		listStyleButton.setEnabled(false);
		
        iconStyleIcon = new ImageIcon(getClass().getResource("/resources/icons/group-iconstyle.png")); //$NON-NLS-1$
        listStyleIcon = new ImageIcon(getClass().getResource("/resources/icons/group-liststyle.png")); //$NON-NLS-1$
        listStyleButton.setIcon(iconStyleIcon);
        listStyleButton.setToolTipText("Icon Ansicht - Zur Listendarstellung wechseln"); //$NON-NLS-1$

        setLoading(false);
	}









}
