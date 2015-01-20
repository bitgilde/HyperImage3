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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.HISimpleTextFieldControl;
import org.hyperimage.connector.fedora3.ws.HiMetadataRecord;
import org.hyperimage.connector.fedora3.ws.HiMetadataSchema;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Jens-Martin Loebel
 */
public class RepositoryMetadataView extends GUIView {

	private static final long serialVersionUID = 5187274828278004546L;
	
	private JPanel metadataPanel;
    private JPanel repositoryFieldsPanel;
	private GridBagConstraints gridBagConstraints = new GridBagConstraints();
	private JScrollPane fieldsScroll;
	private JLabel infoLabel;
    
    
	public RepositoryMetadataView() {
		super(Messages.getString("RepositoryMetadataView.0")); //$NON-NLS-1$
	
		initComponents();
		
		setDisplayPanel(metadataPanel);
		
	}
	
	
	public void setRepositoryFields(HiMetadataSchema repositorySchema) {
		// TODO implement, Fedora3 Connector does NOT support this at the moment
	}
	
	// DEBUG refactor and clean up, support namespaces
	public void setMetadataForElements(int elementCount, List<HiMetadataRecord> repositoryRecords) {
		repositoryFieldsPanel.removeAll();
		repositoryFieldsPanel.doLayout();
		repositoryFieldsPanel.repaint();
		fieldsScroll.doLayout();
		fieldsScroll.repaint();

		if ( elementCount != 1 ) {
			gridBagConstraints.gridy = 0;
			if ( elementCount == 0 ) infoLabel.setText(Messages.getString("RepositoryMetadataView.1")); //$NON-NLS-1$
			else infoLabel.setText(elementCount+" "+Messages.getString("RepositoryMetadataView.3")); //$NON-NLS-1$ //$NON-NLS-2$
			repositoryFieldsPanel.add(infoLabel, gridBagConstraints);
		} else {
			for ( int index = 0 ; index < repositoryRecords.size(); index++ ) {
				HiMetadataRecord record = repositoryRecords.get(index);
				HISimpleTextFieldControl control = addSingleLineField(record.getKey(), index, repositoryFieldsPanel);
				control.setText(record.getValue());
				control.setEditable(false);
			}
		}
		
		repositoryFieldsPanel.setVisible(false);
		repositoryFieldsPanel.doLayout();
		repositoryFieldsPanel.repaint();
		fieldsScroll.doLayout();
		fieldsScroll.repaint();
		repositoryFieldsPanel.setVisible(true);
		fieldsScroll.doLayout();
		fieldsScroll.repaint();
	}

	
	
	private HISimpleTextFieldControl addSingleLineField(String title, int index, JPanel panel) {
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 1.0d;
		gridBagConstraints.weighty = 0.0d;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = index;

		HISimpleTextFieldControl slPanel = new HISimpleTextFieldControl(title);
		panel.add(slPanel,gridBagConstraints);
		return slPanel;
	}

	
	private void initComponents() {

        metadataPanel = new JPanel();
        repositoryFieldsPanel = new JPanel();
        fieldsScroll = new JScrollPane(repositoryFieldsPanel);

        repositoryFieldsPanel.setBorder(BorderFactory.createTitledBorder(null, Messages.getString("RepositoryMetadataView.4"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), java.awt.Color.blue)); // NOI18N //$NON-NLS-1$ //$NON-NLS-2$

        GroupLayout metadataPanelLayout = new GroupLayout(metadataPanel);
        metadataPanel.setLayout(metadataPanelLayout);
        metadataPanelLayout.setHorizontalGroup(
            metadataPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(metadataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(fieldsScroll, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        metadataPanelLayout.setVerticalGroup(
            metadataPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(metadataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(fieldsScroll, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        
        // -----
        
        infoLabel = new JLabel();
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        
		repositoryFieldsPanel.setLayout(new GridBagLayout());
		fieldsScroll.setPreferredSize(new Dimension(400,500));
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.weightx = 1.0d;
		gridBagConstraints.weighty = 0.0d;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridx = 0;
        
    }
	
	
}
