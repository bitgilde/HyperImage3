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

package org.hyperimage.client.gui.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.hyperimage.client.Messages;
import org.hyperimage.client.gui.MetadataEditorControl;
import org.hyperimage.client.util.MetadataHelper;
import org.hyperimage.client.ws.GroupTypes;
import org.hyperimage.client.ws.HiFlexMetadataRecord;
import org.hyperimage.client.ws.HiGroup;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/**
 * @author Jens-Martin Loebel
 */
public class GroupPropertyEditorView extends GUIView 
	implements ItemListener {

	private static final long serialVersionUID = -2280958055442791286L;

	private HiGroup group = null;

	private JPanel editorPanel;
	private JCheckBox groupVisibleCheckBox;
	private MetadataEditorControl metadataEditorControl;
	private JPanel propertiesPanel;
	private TitledBorder propertiesBorder;
	private boolean propertyChanged = false;

	public GroupPropertyEditorView() {
		super(new Color(0x21, 0x49, 0x8A));
		
		initComponents();
		updateLanguage();
		
		setDisplayPanel(editorPanel);
				
	}
	
        public void updateContent() {
            if ( group != null ) metadataEditorControl.setTimeLabel(MetadataHelper.getFuzzyDate(group.getTimestamp()));
        }
	
	public void updateLanguage() {
		menuTitle.setText(Messages.getString("GroupPropertyEditorView.0")); //$NON-NLS-1$
		propertiesBorder.setTitle(Messages.getString("GroupPropertyEditorView.9")); //$NON-NLS-1$
		propertiesPanel.repaint();
		metadataEditorControl.setIdLabel(Messages.getString("GroupPropertyEditorView.1")); //$NON-NLS-1$
		metadataEditorControl.updateLanguage(Messages.getString("GroupPropertyEditorView.6"), null, Messages.getString("GroupPropertyEditorView.8")); //$NON-NLS-1$ //$NON-NLS-2$
		
		if ( group != null ) {
			if ( group.getType() == GroupTypes.HIGROUP_REGULAR ) 
				metadataEditorControl.setEmptyText(Messages.getString("GroupPropertyEditorView.2")+" (G"+group.getId()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			else
				metadataEditorControl.setEmptyText(""); //$NON-NLS-1$
			if ( group.getType() == GroupTypes.HIGROUP_IMPORT )
				metadataEditorControl.setIdLabel(Messages.getString("GroupPropertyEditorView.3")); //$NON-NLS-1$
			else if ( group.getType() == GroupTypes.HIGROUP_TRASH ) 
				metadataEditorControl.setIdLabel(Messages.getString("GroupPropertyEditorView.4")); //$NON-NLS-1$
                        else {
				if ( group.getUUID() == null ) metadataEditorControl.setIdLabel("G"+group.getId()); //$NON-NLS-1$
                                else metadataEditorControl.setIdLabel(group.getUUID());
                        }
                    metadataEditorControl.setTimeLabel(MetadataHelper.getFuzzyDate(group.getTimestamp()));
		}
        groupVisibleCheckBox.setText(Messages.getString("GroupPropertyEditorView.11")); //$NON-NLS-1$


	}

	public List<HiFlexMetadataRecord> getMetadata() {
		return metadataEditorControl.getMetadata();
	}
	
	public JButton getSaveButton() {
		return metadataEditorControl.getSaveButton();
	}
	
	public JButton getResetButton() {
		return metadataEditorControl.getResetButton();
	}

	
	public void setGroup(HiGroup group, String defLang) {
		this.group = group;
		if ( group != null ) {
			groupVisibleCheckBox.removeItemListener(this);
			if ( group.getType() == GroupTypes.HIGROUP_REGULAR ) {
				if ( group.getUUID() == null ) metadataEditorControl.setIdLabel("G"+group.getId()); //$NON-NLS-1$
                                else metadataEditorControl.setIdLabel(group.getUUID());
                                metadataEditorControl.setTimeLabel(MetadataHelper.getFuzzyDate(group.getTimestamp()));
				groupVisibleCheckBox.setSelected(group.isVisible());
				groupVisibleCheckBox.setEnabled(true);
				
				metadataEditorControl.setMetadata(this.group.getMetadata(), defLang);
				if ( group.getType() == GroupTypes.HIGROUP_REGULAR ) 
					metadataEditorControl.setEmptyText(Messages.getString("GroupPropertyEditorView.2")+" (G"+group.getId()+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				else
					metadataEditorControl.setEmptyText(""); //$NON-NLS-1$
			}
			else {
				if ( group.getType() == GroupTypes.HIGROUP_IMPORT )
					metadataEditorControl.setIdLabel(Messages.getString("GroupPropertyEditorView.3")); //$NON-NLS-1$
				else metadataEditorControl.setIdLabel(Messages.getString("GroupPropertyEditorView.4")); //$NON-NLS-1$
                                metadataEditorControl.setTimeLabel(MetadataHelper.getFuzzyDate(group.getTimestamp()));
				groupVisibleCheckBox.setSelected(false);
				groupVisibleCheckBox.setEnabled(false);
				metadataEditorControl.setMetadata(null, defLang);
			}
			propertyChanged = false;
			groupVisibleCheckBox.addItemListener(this);

		} else metadataEditorControl.setEmptyText(""); //$NON-NLS-1$
	}

	public boolean hasChanges() {
		return ( metadataEditorControl.hasChanges() || propertyChanged );
	}

	public boolean hasPropertyChanges() {
        // [JML] visibility properties are not part of the editor anymore. They will be handled by the PostPeTAL Generator
		return false;

//		return propertyChanged;
	}

	public boolean hasMetadataChanges() {
		return metadataEditorControl.hasChanges();
	}

	public void syncChanges() {
        // [JML] visibility properties are not part of the editor anymore. They will be handled by the PostPeTAL Generator
//		group.setVisible(groupVisibleCheckBox.isSelected());

		propertyChanged = false;
		metadataEditorControl.syncChanges();
	}

	public void resetChanges() {
        // [JML] visibility properties are not part of the editor anymore. They will be handled by the PostPeTAL Generator
//		groupVisibleCheckBox.setSelected(group.isVisible());

		propertyChanged = false;
		metadataEditorControl.resetChanges();
	}

	
	private void initComponents() {

		editorPanel = new JPanel();
		propertiesPanel = new JPanel();
		groupVisibleCheckBox = new JCheckBox();
		metadataEditorControl = new MetadataEditorControl("title", Messages.getString("GroupPropertyEditorView.6"), null, null, "comment", Messages.getString("GroupPropertyEditorView.8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
		propertiesBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Messages.getString("GroupPropertyEditorView.9"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Lucida Grande", 0, 13), Color.blue); //$NON-NLS-1$ //$NON-NLS-2$
		propertiesPanel.setBorder(propertiesBorder);

        GroupLayout propertiesPanelLayout = new GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(propertiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(groupVisibleCheckBox)
                .addContainerGap(236, Short.MAX_VALUE))
        );
        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(groupVisibleCheckBox)
        );
        
        // [JML] visibility properties are not part of the editor anymore. They will be handled by the PostPeTAL Generator
        propertiesPanel.setVisible(false);


        GroupLayout editorPanelLayout = new GroupLayout(editorPanel);
        editorPanel.setLayout(editorPanelLayout);
        editorPanelLayout.setHorizontalGroup(
            editorPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, editorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(editorPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(GroupLayout.LEADING, metadataEditorControl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, propertiesPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        editorPanelLayout.setVerticalGroup(
            editorPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(editorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(propertiesPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(metadataEditorControl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        
        // -----
		groupVisibleCheckBox.addItemListener(this);
		
        
	}

	public void itemStateChanged(ItemEvent e) {
		propertyChanged = ( groupVisibleCheckBox.isSelected() != group.isVisible() );
	}

}
