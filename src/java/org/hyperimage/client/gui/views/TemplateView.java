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

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.hyperimage.client.HIRuntime;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.HiFlexMetadataSet;
import org.hyperimage.client.ws.HiFlexMetadataTemplate;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class TemplateView extends GUIView {

	private static final long serialVersionUID = 1510444418592115462L;

	
	private List<HiFlexMetadataTemplate> templates;

	private JLabel notImplementedLabel;
	private JButton tempButton;
	private JPanel templatePanel;
	private JScrollPane templateScroll;
	private JTree templateTree;
	private DefaultMutableTreeNode rootNode;
	private DefaultTreeModel model;


	public TemplateView(List<HiFlexMetadataTemplate> templates) {
		super("Projekt Templates");
		this.templates = templates;

		initComponents();
		initTemplateTree();

		setDisplayPanel(templatePanel);

	}


	private void initTemplateTree() {
		for ( HiFlexMetadataTemplate template : templates ) {
			if ( template.getNamespacePrefix().equalsIgnoreCase("HIBase") ) continue; // skip base template
			
			DefaultMutableTreeNode templateNode = new DefaultMutableTreeNode(template.getNamespacePrefix());
			model.insertNodeInto(templateNode, rootNode, 0);
			templateTree.expandRow(0);
			
			for ( int i=0; i < template.getEntries().size(); i++ ) {
				HiFlexMetadataSet set = template.getEntries().get(i);
				DefaultMutableTreeNode entryNode = new DefaultMutableTreeNode(MetadataHelper.getTemplateKeyDisplayName(template, set.getTagname(), HIRuntime.getGUILanguage().getLanguage()));
				model.insertNodeInto(entryNode, templateNode, i);
			}
		}
		
	}
	

	private void initComponents() {

		templatePanel = new JPanel();
		templateScroll = new JScrollPane();
		templateTree = new JTree();
		tempButton = new JButton();
		notImplementedLabel = new JLabel();

		templateScroll.setViewportView(templateTree);

		tempButton.setText("X");
		tempButton.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
		tempButton.setPreferredSize(new Dimension(24, 24));

		notImplementedLabel.setText("Editieren / Optionen noch nicht implementiert");

		GroupLayout templatePanelLayout = new GroupLayout(templatePanel);
		templatePanel.setLayout(templatePanelLayout);
		templatePanelLayout.setHorizontalGroup(
				templatePanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(templatePanelLayout.createSequentialGroup()
						.addContainerGap()
						.add(templatePanelLayout.createParallelGroup(GroupLayout.LEADING)
								.add(templateScroll, GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
								.add(GroupLayout.TRAILING, templatePanelLayout.createSequentialGroup()
										.add(notImplementedLabel, GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
										.addPreferredGap(LayoutStyle.RELATED)
										.add(tempButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
										.addContainerGap())
		);
		templatePanelLayout.setVerticalGroup(
				templatePanelLayout.createParallelGroup(GroupLayout.LEADING)
				.add(GroupLayout.TRAILING, templatePanelLayout.createSequentialGroup()
						.addContainerGap()
						.add(templateScroll, GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
						.add(13, 13, 13)
						.add(templatePanelLayout.createParallelGroup(GroupLayout.BASELINE)
								.add(notImplementedLabel)
								.add(tempButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addContainerGap())
		);

		// -----

		// init tree
		rootNode = new DefaultMutableTreeNode();
		model = new DefaultTreeModel(rootNode, true);
		model.setAsksAllowsChildren(true);
		templateTree.setModel(model);
		templateTree.setRootVisible(false);
		templateTree.setScrollsOnExpand(true);
		templateTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

	}



}
